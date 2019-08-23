package de.scaramanga.lily.core.application;

import de.scaramanga.lily.core.annotations.LilyModule;
import de.scaramanga.lily.core.communication.Answer;
import de.scaramanga.lily.core.communication.AnswerInfo;
import de.scaramanga.lily.core.communication.Broadcaster;
import de.scaramanga.lily.core.communication.Command;
import de.scaramanga.lily.core.communication.CommandInterceptor;
import de.scaramanga.lily.core.communication.Dispatcher;
import de.scaramanga.lily.core.communication.MessageInfo;
import de.scaramanga.lily.core.configuration.LilyConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static de.scaramanga.lily.core.communication.CommandInterceptor.ContinuationStrategy.*;

@Component
@Slf4j
class LilyDispatcher implements Dispatcher {

  private final ApplicationContext                  ac;
  private final LilyConfiguration                   properties;
  private       Map<String, Method>                 commands     = null;
  private       List<CommandInterceptor>            interceptors = new ArrayList<>();
  private       List<Broadcaster<? extends Answer>> broadcasters = new ArrayList<>();

  LilyDispatcher(ApplicationContext ac, LilyConfiguration properties) {

    this.ac         = ac;
    this.properties = properties;
  }

  @Override
  public Optional<Answer> dispatch(String message, MessageInfo messageInfo) {

    if (commands == null) {
      initializeCommands();
    }

    if (processedByInterceptor(message, messageInfo) || !message.startsWith(properties.getCommandPrefix())) {
      return Optional.empty();
    }

    Command command = generateCommand(message, messageInfo);

    Method method = commands.get(command.getName());

    try {
      return Optional.of((Answer) method.invoke(ac.getBean(method.getDeclaringClass()), command));
    } catch (NullPointerException e) {
      // Command not defined.
    } catch (Exception e) {
      LOGGER.error("", e);
    }

    return Optional.empty();
  }

  private boolean processedByInterceptor(String message, MessageInfo messageInfo) {

    for (CommandInterceptor interceptor : interceptors) {

      if (interceptor.process(Command.withMessageInfo(message, messageInfo)) == STOP) {
        return true;
      }
    }

    return false;
  }

  @Override
  public void addInterceptor(CommandInterceptor interceptor) {

    if (interceptors.contains(interceptor)) {
      return;
    }

    interceptors.add(0, interceptor);
  }

  @Override
  public <T extends Answer> void addBroadcaster(Broadcaster<T> broadcaster, Class<T> clazz) {

    if (!broadcasters.contains(broadcaster)) {
      broadcasters.add(broadcaster);
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T extends Answer<? extends AnswerInfo>> void broadcast(T broadcast, Class<T> type) {

    broadcasters.stream()
                .filter(broadcaster -> hasGenericParameter(broadcaster, type))
                .map(broadcaster -> (Broadcaster<T>) broadcaster)
                .forEach(broadcaster -> broadcaster.broadcast(broadcast));
  }

  private <T> boolean hasGenericParameter(Object object, Class<T> parameter) {

    return Arrays.stream(object.getClass().getGenericInterfaces())
                 .map(Type::getTypeName)
                 .filter(type -> type.startsWith(Broadcaster.class.getName()))
                 .filter(type -> type.contains("<"))
                 .map(name -> name.substring(name.indexOf('<') + 1, name.indexOf('>')))
                 .anyMatch(name -> canConsumeBroadcastType(name, parameter));
  }

  private void initializeCommands() {

    Collection<Object> beans = ac.getBeansWithAnnotation(LilyModule.class).values();
    commands = LilyAnnotationProcessor.getAllLilyCommands(beans
                                                              .stream().map(Object::getClass)
                                                              .collect(Collectors.toSet()));
  }

  private Command generateCommand(String message, MessageInfo messageInfo) {

    if (!message.startsWith(properties.getCommandPrefix())) {
      throw new IllegalArgumentException("Message ust start with defined Prefix.");
    }

    String[]     split = message.substring(properties.getCommandPrefix().length()).split(" ");
    List<String> args  = Arrays.asList(Arrays.copyOfRange(split, 1, split.length));

    return new Command() {
      @Override
      public String getName() {

        return split[0];
      }

      @Override
      public int getArgumentCount() {

        return args.size();
      }

      @Override
      public String getArgument(int i) {

        return args.get(i);
      }

      @Override
      public MessageInfo getMessageInfo() {

        return messageInfo;
      }
    };
  }

  private <T> boolean canConsumeBroadcastType(String broadcasterTypeName, Class<T> broadcastType) {

    if (broadcasterTypeName.equals(broadcastType.getTypeName())) {
      return true;
    }

    return Arrays.stream(broadcastType.getInterfaces())
                 .map(Class::getCanonicalName)
                 .anyMatch(name -> name.equals(broadcasterTypeName));
  }
}

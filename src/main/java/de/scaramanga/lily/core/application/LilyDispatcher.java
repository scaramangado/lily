package de.scaramanga.lily.core.application;

import de.scaramanga.lily.core.annotations.LilyModule;
import de.scaramanga.lily.core.communication.*;
import de.scaramanga.lily.core.configuration.LilyConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

import static de.scaramanga.lily.core.communication.CommandInterceptor.ContinueProcessing.STOP;

@Component
@Slf4j
class LilyDispatcher implements Dispatcher {

    private final ApplicationContext ac;

    private final LilyConfiguration properties;

    private Map<String, Method> commands = null;

    private List<CommandInterceptor> interceptors = new ArrayList<>();

    LilyDispatcher(ApplicationContext ac, LilyConfiguration properties) {
        this.ac = ac;
        this.properties = properties;
    }

    @Override
    public Optional<Answer> dispatch(String message, MessageInfo messageInfo) {

        if(commands == null) {
            initializeCommands();
        }

        if (processedByInterceptor(message, messageInfo) || !message.startsWith(properties.getCommandPrefix())) {
            return Optional.empty();
        }

        Command command = generateCommand(message, messageInfo);

        Method method = commands.get(command.getName());

        try {
            return Optional.of((Answer) method.invoke(ac.getBean(method.getDeclaringClass()), (Object) command));
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

    public void addInterceptor(CommandInterceptor interceptor) {

        if (interceptors.contains(interceptor)) {
            return;
        }

        interceptors.add(0, interceptor);
    }

    private void initializeCommands() {
        Collection<Object> beans = ac.getBeansWithAnnotation(LilyModule.class).values();
        commands = LilyAnnotationProcessor.getAllLilyCommands(beans
                .stream().map(Object::getClass).collect(Collectors.toSet()));
    }

    private Command generateCommand(String message, MessageInfo messageInfo) {

        if (!message.startsWith(properties.getCommandPrefix())) {
            throw new IllegalArgumentException("Message ust start with defined Prefix.");
        }

        String[] split = message.substring(properties.getCommandPrefix().length()).split(" ");
        List<String> args = Arrays.asList(Arrays.copyOfRange(split, 1, split.length));

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
}

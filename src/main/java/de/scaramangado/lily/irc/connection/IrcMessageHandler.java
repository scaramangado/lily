package de.scaramangado.lily.irc.connection;

import de.scaramangado.lily.core.communication.Answer;
import de.scaramangado.lily.core.communication.Dispatcher;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@Component
class IrcMessageHandler implements MessageHandler {

  private final Dispatcher                                   dispatcher;
  private       Map<String, Function<String, MessageAnswer>> functionMap;

  public IrcMessageHandler(Dispatcher dispatcher) {

    this.dispatcher = dispatcher;
  }

  private Map<String, Function<String, MessageAnswer>> getFunctionMap() {

    if (functionMap != null) {
      return functionMap;
    }

    final String PRIVMSG_REGEX = ":\\w+!\\w[\\w@.\\-]+ PRIVMSG #\\w+ :.*";
    final String PING_REGEX    = "PING :.*";

    functionMap = new HashMap<>();
    functionMap.put(PRIVMSG_REGEX, this::privateMessage);
    functionMap.put(PING_REGEX, this::ping);

    return functionMap;
  }

  private MessageAnswer privateMessage(String message) {

    String[] parts       = message.split(":");
    String   chatMessage = parts[2];

    String nick    = parts[1].split("!")[0];
    String channel = parts[1].split("#")[1].trim();

    return dispatcher
        .dispatch(chatMessage, IrcMessageInfo.with(nick, channel))
        .map(Answer::getText)
        .map(answerString -> composeMessageAnswer(answerString, parts))
        .orElse(MessageAnswer.ignoreAnswer());
  }

  private MessageAnswer composeMessageAnswer(String answerString, String[] originalMessageParts) {

    String[] messageSourceParts = originalMessageParts[1].split("PRIVMSG ");

    return MessageAnswer.sendLines("PRIVMSG " + messageSourceParts[1] + ":" + answerString);
  }

  private MessageAnswer ping(String message) {

    return MessageAnswer.sendLines(message.replace("PING", "PONG"));
  }

  @Override
  public MessageAnswer handleMessage(String message) {

    Optional<Function<String, MessageAnswer>> function = getFunctionMap().entrySet().stream()
                                                                         .filter(e -> message.matches(e.getKey()))
                                                                         .map(Map.Entry::getValue)
                                                                         .findFirst();

    Optional<MessageAnswer> action = function.map(fun -> fun.apply(message));

    return action.orElseGet(MessageAnswer::ignoreAnswer);
  }
}

package de.scaramanga.lily.irc.connection;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static de.scaramanga.lily.irc.connection.IrcAction.Type.*;

class MessageHandler {

    private Map<String, Function<String, IrcAction>> functionMap;

    MessageHandler() { }

    private Map<String, Function<String, IrcAction>> getFunctionMap() {

        if(functionMap != null) {
            return functionMap;
        }

        final String PRIVMSG_REGEX = ":\\w+!\\w[\\w@.]+ PRIVMSG #\\w+ :.*";
        final String PING_REGEX = "PING :.*";

        functionMap = new HashMap<>();
        functionMap.put(PRIVMSG_REGEX, this::privateMessage);
        functionMap.put(PING_REGEX, this::ping);

        return functionMap;
    }

    IrcAction handleMessage(String message) {

        Optional<Function<String, IrcAction>> function = getFunctionMap().entrySet().stream()
                .filter(e -> message.matches(e.getKey()))
                .map(Map.Entry::getValue)
                .findFirst();

        Optional<IrcAction> action = function.map(fun -> fun.apply(message));

        return action.orElseGet(() -> new IrcAction(IGNORE, null));
    }

    private IrcAction privateMessage(String message) {

        String[] parts = message.split(":");


        return new IrcAction(PARSE, parts[2]);
    }

    private IrcAction ping(String message) {
        return new IrcAction(ANSWER, message.replace("PING", "PONG"));
    }
}

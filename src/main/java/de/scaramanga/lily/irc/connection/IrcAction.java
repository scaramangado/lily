package de.scaramanga.lily.irc.connection;

import lombok.Getter;

import java.util.Optional;

@Getter
class IrcAction {

    private final Type type;

    private final Optional<String> answer;

    IrcAction(Type type, String answer) {
        this.type = type;
        this.answer = Optional.ofNullable(answer);
    }

    enum Type {
        ANSWER,
        PARSE,
        IGNORE
    }
}

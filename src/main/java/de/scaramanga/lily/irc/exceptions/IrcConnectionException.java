package de.scaramanga.lily.irc.exceptions;

public class IrcConnectionException extends RuntimeException {

    public IrcConnectionException(String message, Throwable cause) {
        super(message, cause);
    }

    public IrcConnectionException(String message) {
        super(message);
    }
}

package de.scaramangado.lily.irc.exception;

public class IrcConnectionException extends RuntimeException {

  public IrcConnectionException() {}

  public IrcConnectionException(String message) {

    super(message);
  }
}


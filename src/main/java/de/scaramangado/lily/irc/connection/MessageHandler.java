package de.scaramangado.lily.irc.connection;

@FunctionalInterface
public interface MessageHandler {

  MessageAnswer handleMessage(String message);
}

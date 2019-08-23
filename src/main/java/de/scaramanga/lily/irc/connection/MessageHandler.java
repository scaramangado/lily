package de.scaramanga.lily.irc.connection;

@FunctionalInterface
public interface MessageHandler {

  MessageAnswer handleMessage(String message);
}

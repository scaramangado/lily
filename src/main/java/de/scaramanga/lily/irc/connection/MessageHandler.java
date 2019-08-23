package de.scaramanga.lily.irc.connection;

import de.scaramanga.lily.irc.connection.MessageAnswer;

@FunctionalInterface
public interface MessageHandler {

  MessageAnswer handleMessage(String message);
}

package de.scaramanga.lily.irc.interfaces;

import de.scaramanga.lily.irc.connection.MessageAnswer;

@FunctionalInterface
public interface MessageHandler {

  MessageAnswer handleMessage(String message);
}

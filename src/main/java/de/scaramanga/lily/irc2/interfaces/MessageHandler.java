package de.scaramanga.lily.irc2.interfaces;

import de.scaramanga.lily.irc2.connection.MessageAnswer;

@FunctionalInterface
public interface MessageHandler {

    MessageAnswer handleMessage(String message);
}

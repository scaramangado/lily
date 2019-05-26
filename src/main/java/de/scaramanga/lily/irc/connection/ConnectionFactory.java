package de.scaramanga.lily.irc.connection;

import de.scaramanga.lily.irc.connection.actions.ConnectionAction;
import de.scaramanga.lily.irc.interfaces.MessageHandler;
import de.scaramanga.lily.irc.interfaces.RootMessageHandler;
import de.scaramanga.lily.irc.interfaces.SocketFactory;

import java.util.Queue;

interface ConnectionFactory {

  Connection getConnection(String host, Integer port, MessageHandler messageHandler, RootMessageHandler rootHandler,
                           SocketFactory socketFactory, Queue<ConnectionAction> actionQueue);

  static ConnectionFactory standardFactory() {

    return Connection::new;
  }
}

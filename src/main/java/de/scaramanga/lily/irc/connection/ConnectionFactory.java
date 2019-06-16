package de.scaramanga.lily.irc.connection;

import de.scaramanga.lily.irc.interfaces.MessageHandler;
import de.scaramanga.lily.irc.interfaces.RootMessageHandler;
import de.scaramanga.lily.irc.interfaces.SocketFactory;

interface ConnectionFactory {

  Connection getConnection(String host, Integer port, MessageHandler messageHandler, RootMessageHandler rootHandler,
                           SocketFactory socketFactory, ConnectionActionQueue actionQueue);

  static ConnectionFactory standardFactory() {

    return Connection::new;
  }
}

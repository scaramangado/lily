package de.scaramanga.lily.irc.connection;

import de.scaramanga.lily.irc.connection.ping.PingHandler;
import de.scaramanga.lily.irc.interfaces.MessageHandler;
import de.scaramanga.lily.irc.interfaces.RootMessageHandler;
import de.scaramanga.lily.irc.interfaces.SocketFactory;

interface ConnectionFactory {

  Connection getConnection(String host, Integer port, MessageHandler messageHandler, RootMessageHandler rootHandler,
                           SocketFactory socketFactory, ConnectionActionQueue actionQueue);

  static ConnectionFactory pingHandlerFactory(PingHandler pingHandler) {
    return (host, port, messageHandler, rootHandler, socketFactory, actionQueue) ->
        new Connection(host, port, messageHandler, rootHandler, socketFactory, actionQueue, pingHandler);
  }
}

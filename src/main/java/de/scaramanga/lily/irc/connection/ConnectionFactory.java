package de.scaramanga.lily.irc.connection;

import de.scaramanga.lily.irc.configuration.IrcProperties;
import de.scaramanga.lily.irc.connection.ping.PingHandler;
import de.scaramanga.lily.irc.interfaces.MessageHandler;
import de.scaramanga.lily.irc.interfaces.RootMessageHandler;
import de.scaramanga.lily.irc.interfaces.SocketFactory;

interface ConnectionFactory {

  Connection getConnection(IrcProperties properties, MessageHandler messageHandler, RootMessageHandler rootHandler,
                           SocketFactory socketFactory, ConnectionActionQueue actionQueue);

  static ConnectionFactory pingHandlerFactory(PingHandler pingHandler) {
    return (properties, messageHandler, rootHandler, socketFactory, actionQueue) ->
        new Connection(properties, messageHandler, rootHandler, socketFactory, actionQueue, pingHandler);
  }
}

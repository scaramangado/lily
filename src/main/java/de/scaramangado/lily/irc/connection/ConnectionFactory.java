package de.scaramangado.lily.irc.connection;

import de.scaramangado.lily.irc.configuration.IrcProperties;
import de.scaramangado.lily.irc.connection.ping.PingHandler;

interface ConnectionFactory {

  Connection getConnection(IrcProperties properties, MessageHandler messageHandler, RootMessageHandler rootHandler,
                           SocketFactory socketFactory, ConnectionActionQueue actionQueue);

  static ConnectionFactory pingHandlerFactory(PingHandler pingHandler) {

    return (properties, messageHandler, rootHandler, socketFactory, actionQueue) ->
        new Connection(properties, messageHandler, rootHandler, socketFactory, actionQueue, pingHandler);
  }
}

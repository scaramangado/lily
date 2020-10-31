package de.scaramangado.lily.irc.connection;

import de.scaramangado.lily.irc.configuration.IrcProperties;
import de.scaramangado.lily.irc.connection.ping.PingHandler;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class SingletonConnectionFactory implements ConnectionFactory {

  private final Map<ConnectionInfo, Connection> connections = new HashMap<>();
  private final ConnectionFactory               internalFactory;

  @Autowired
  public SingletonConnectionFactory(PingHandler pingHandler,
                                    IrcProperties properties) {

    this(ConnectionFactory.pingHandlerFactory(pingHandler));
  }

  SingletonConnectionFactory(ConnectionFactory internalFactory) {

    this.internalFactory = internalFactory;
  }

  @Override
  public Connection getConnection(IrcProperties properties, MessageHandler messageHandler,
                                  RootMessageHandler rootHandler, SocketFactory socketFactory,
                                  ConnectionActionQueue actionQueue) {

    ConnectionInfo info = new ConnectionInfo(properties.getHost(), properties.getPort(), messageHandler, rootHandler,
                                             socketFactory, actionQueue);

    connections.computeIfAbsent(info, i -> connectionForInfo(i, properties));

    return connections.get(info);
  }

  private Connection connectionForInfo(ConnectionInfo info, IrcProperties properties) {

    return internalFactory.getConnection(properties, info.messageHandler, info.rootHandler,
                                         info.socketFactory, info.actionQueue);
  }

  @EqualsAndHashCode
  @AllArgsConstructor
  private static class ConnectionInfo {

    private String                host;
    private Integer               port;
    private MessageHandler        messageHandler;
    private RootMessageHandler    rootHandler;
    private SocketFactory         socketFactory;
    private ConnectionActionQueue actionQueue;
  }
}

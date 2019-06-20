package de.scaramanga.lily.irc.connection;

import de.scaramanga.lily.irc.connection.ping.PingHandler;
import de.scaramanga.lily.irc.interfaces.MessageHandler;
import de.scaramanga.lily.irc.interfaces.RootMessageHandler;
import de.scaramanga.lily.irc.interfaces.SocketFactory;
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
  public SingletonConnectionFactory(PingHandler pingHandler) {

    this(ConnectionFactory.pingHandlerFactory(pingHandler));
  }

  SingletonConnectionFactory(ConnectionFactory internalFactory) {

    this.internalFactory = internalFactory;
  }

  @Override
  public Connection getConnection(String host, Integer port, MessageHandler messageHandler,
                                  RootMessageHandler rootHandler, SocketFactory socketFactory,
                                  ConnectionActionQueue actionQueue) {

    ConnectionInfo info = new ConnectionInfo(host, port, messageHandler, rootHandler, socketFactory, actionQueue);

    connections.computeIfAbsent(info, this::connectionForInfo);

    return connections.get(info);
  }

  private Connection connectionForInfo(ConnectionInfo info) {

    return internalFactory.getConnection(info.host, info.port, info.messageHandler, info.rootHandler,
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

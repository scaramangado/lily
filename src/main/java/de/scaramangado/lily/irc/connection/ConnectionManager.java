package de.scaramangado.lily.irc.connection;

import de.scaramangado.lily.core.communication.Broadcaster;
import de.scaramangado.lily.core.communication.Dispatcher;
import de.scaramangado.lily.irc.configuration.IrcProperties;
import de.scaramangado.lily.irc.connection.actions.BroadcastActionData;
import de.scaramangado.lily.irc.connection.actions.ConnectionAction;
import de.scaramangado.lily.irc.connection.actions.JoinActionData;
import de.scaramangado.lily.irc.connection.actions.LeaveActionData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import static de.scaramangado.lily.irc.connection.actions.ConnectionAction.ConnectionActionType.*;

@Component
@ConditionalOnProperty(value = "lily.irc.enabled")
@Slf4j
public class ConnectionManager implements Broadcaster<IrcAnswer> {

  private final IrcProperties  properties;
  private final MessageHandler messageHandler;
  private final RootMessageHandler    rootMessageHandler;
  private final SocketFactory         socketFactory;
  private final ExecutorService       executor  = Executors.newCachedThreadPool();
  private final AtomicBoolean         connected = new AtomicBoolean(false);
  private final ConnectionActionQueue actionQueue;
  private final ConnectionFactory     connectionFactory;

  public ConnectionManager(IrcProperties properties, MessageHandler messageHandler,
                           RootMessageHandler rootMessageHandler, SocketFactory socketFactory,
                           ConnectionActionQueue actionQueue, ConnectionFactory connectionFactory,
                           Dispatcher dispatcher) {

    this.properties         = properties;
    this.messageHandler     = messageHandler;
    this.rootMessageHandler = rootMessageHandler;
    this.socketFactory      = socketFactory;
    this.actionQueue        = actionQueue;
    this.connectionFactory  = connectionFactory;
    dispatcher.addBroadcaster(this, IrcAnswer.class);
  }

  @EventListener
  public void contextStart(ContextRefreshedEvent event) {

    if (!properties.isEnabled() || connected.get()) {
      return;
    }

    LOGGER.info("IRC enabled. Connecting...");

    executor.submit(
        connectionFactory.getConnection(properties, messageHandler, rootMessageHandler, socketFactory, actionQueue));
    connected.set(true);

    properties.getChannels().forEach(this::connectToChannel);
  }

  @EventListener
  public void contextClose(ContextClosedEvent event) {

    disconnect();
  }

  public void connectToChannel(String channel) {

    sendAction(new ConnectionAction(JOIN, JoinActionData.withChannelName(channel)));
  }

  public void leaveChannel(String channel) {

    sendAction(new ConnectionAction(LEAVE, LeaveActionData.withChannelName(channel)));
  }

  public void disconnect() {

    sendAction(new ConnectionAction(DISCONNECT));
    connected.set(false);
  }

  private void sendAction(ConnectionAction action) {

    executor.submit(() -> actionQueue.addAction(action));
  }

  @Override
  public void broadcast(IrcAnswer broadcast) {

    sendAction(new ConnectionAction(BROADCAST, BroadcastActionData.withMessage(broadcast.getText())));
  }

  @Override
  public void shutdown() {
    // Do nothing.
  }
}

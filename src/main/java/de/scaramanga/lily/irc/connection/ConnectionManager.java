package de.scaramanga.lily.irc.connection;

import de.scaramanga.lily.irc.configuration.IrcProperties;
import de.scaramanga.lily.irc.connection.actions.BroadcastActionData;
import de.scaramanga.lily.irc.connection.actions.ConnectionAction;
import de.scaramanga.lily.irc.connection.actions.JoinActionData;
import de.scaramanga.lily.irc.connection.actions.LeaveActionData;
import de.scaramanga.lily.irc.interfaces.RootMessageHandler;
import de.scaramanga.lily.irc.interfaces.MessageHandler;
import de.scaramanga.lily.irc.interfaces.SocketFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import static de.scaramanga.lily.irc.connection.actions.ConnectionAction.ConnectionActionType.*;

@Component
public class ConnectionManager {

    private final IrcProperties properties;
    private final MessageHandler messageHandler;
    private final RootMessageHandler rootMessageHandler;
    private final SocketFactory socketFactory;
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final AtomicBoolean connected = new AtomicBoolean(false);
    private final Queue<ConnectionAction> actionQueue;
    private final ConnectionFactory connectionFactory;

    ConnectionManager(IrcProperties properties, MessageHandler messageHandler,
                      RootMessageHandler rootMessageHandler, SocketFactory socketFactory,
                      Supplier<Queue<ConnectionAction>> actionQueueSupplier, ConnectionFactory connectionFactory) {
        this.properties = properties;
        this.messageHandler = messageHandler;
        this.rootMessageHandler = rootMessageHandler;
        this.socketFactory = socketFactory;
        this.actionQueue = actionQueueSupplier.get();
        this.connectionFactory = connectionFactory;
    }

    @Autowired
    public ConnectionManager(IrcProperties properties, MessageHandler messageHandler,
                             RootMessageHandler rootMessageHandler, SocketFactory socketFactory) {
        this(properties, messageHandler, rootMessageHandler, socketFactory, ConcurrentLinkedQueue::new,
                ConnectionFactory.standardFactory());
    }

    @EventListener
    public void contextStart(ContextRefreshedEvent event) {

        if (!properties.isEnabled() || connected.get()) {
            return;
        }

        executor.submit(connectionFactory.getConnection(properties.getHost(), properties.getPort(), messageHandler,
                rootMessageHandler, socketFactory, actionQueue));
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

    public void broadcast(String message) {
        sendAction(new ConnectionAction(BROADCAST, BroadcastActionData.withMessage(message)));
    }

    public void disconnect() {
        sendAction(new ConnectionAction(DISCONNECT));
        connected.set(false);
    }

    private void sendAction(ConnectionAction action) {
        executor.submit(() -> actionQueue.add(action));
    }
}

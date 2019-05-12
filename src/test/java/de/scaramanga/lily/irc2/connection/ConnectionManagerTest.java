package de.scaramanga.lily.irc2.connection;

import de.scaramanga.lily.irc2.configuration.Irc2Properties;
import de.scaramanga.lily.irc2.connection.actions.BroadcastActionData;
import de.scaramanga.lily.irc2.connection.actions.ConnectionAction;
import de.scaramanga.lily.irc2.connection.actions.JoinActionData;
import de.scaramanga.lily.irc2.connection.actions.LeaveActionData;
import de.scaramanga.lily.irc2.interfaces.MessageHandler;
import de.scaramanga.lily.irc2.interfaces.RootMessageHandler;
import de.scaramanga.lily.irc2.interfaces.SocketFactory;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

import static de.scaramanga.lily.irc2.connection.actions.ConnectionAction.ConnectionActionType.*;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = Irc2Properties.class)
class ConnectionManagerTest {

    private ConnectionManager manager;
    private MessageHandler messageHandlerMock = mock(MessageHandler.class);
    private RootMessageHandler rootMessageHandlerMock = mock(RootMessageHandler.class);
    private SocketFactory socketFactoryMock = mock(SocketFactory.class);
    private Queue<ConnectionAction> actionQueue;
    private Irc2Properties properties;
    private Connection connectionMock = mock(Connection.class);

    private static final String CHANNEL = "channel";
    private static final String MESSAGE = "message";

    @BeforeEach
    void setup() {

        actionQueue = new ConcurrentLinkedQueue<>();
        properties = new Irc2Properties();
        properties.setEnabled(true);
        properties.setChannels(new ArrayList<>());

        manager = new ConnectionManager(properties, messageHandlerMock, rootMessageHandlerMock, socketFactoryMock,
                () -> actionQueue, (a, b, c, d, e, f) -> connectionMock);
    }

    @Test
    void runsConnectionThreadOnStartup() {

        manager.contextStart(null);

        verify(connectionMock, timeout(1000)).call();
    }

    @Test
    void doesNotRunNewConnectionThreadOnRefresh() {

        manager.contextStart(null);
        manager.contextStart(null);

        verify(connectionMock, timeout(1000).times(1)).call();
        verifyNoMoreInteractions(connectionMock);
    }

    @Test
    void interruptsConnectionThreadOnShutdown() {

        manager.contextClose(null);

        assertDisconnectActionSent();
    }

    @Test
    void doesNotConnectIfDisabled() {

        properties.setEnabled(false);
        manager.contextStart(null);

        verifyZeroInteractions(connectionMock);
    }

    @Test
    void publishesJoinAction() {

        manager.connectToChannel(CHANNEL);

        await().atMost(1, TimeUnit.SECONDS).until(() -> actionQueue.peek() != null);

        ConnectionAction join = actionQueue.poll();

        SoftAssertions soft = new SoftAssertions();

        soft.assertThat(join.getType()).isEqualTo(JOIN);
        soft.assertThat(((JoinActionData) join.getData()).getChannelName()).isEqualTo(CHANNEL);
        soft.assertThat(actionQueue.poll()).isNull();

        soft.assertAll();
    }

    @Test
    void publishesLeaveAction() {

        manager.leaveChannel(CHANNEL);

        await().atMost(1, TimeUnit.SECONDS).until(() -> actionQueue.peek() != null);

        ConnectionAction leave = actionQueue.poll();

        SoftAssertions soft = new SoftAssertions();

        soft.assertThat(leave.getType()).isEqualTo(LEAVE);
        soft.assertThat(((LeaveActionData) leave.getData()).getChannelName()).isEqualTo(CHANNEL);
        soft.assertThat(actionQueue.poll()).isNull();

        soft.assertAll();
    }

    @Test
    void publishesDisconnectAction() {


        manager.disconnect();

        assertDisconnectActionSent();
    }

    @Test
    void publishesBroadcastAction() {

        manager.broadcast(MESSAGE);

        await().atMost(1, TimeUnit.SECONDS).until(() -> actionQueue.peek() != null);

        ConnectionAction leave = actionQueue.poll();

        SoftAssertions soft = new SoftAssertions();

        soft.assertThat(leave.getType()).isEqualTo(BROADCAST);
        soft.assertThat(((BroadcastActionData) leave.getData()).getMessage()).isEqualTo(MESSAGE);
        soft.assertThat(actionQueue.poll()).isNull();

        soft.assertAll();
    }

    private void assertDisconnectActionSent() {

        await().atMost(1, TimeUnit.SECONDS).until(() -> actionQueue.peek() != null);

        ConnectionAction disconnect = actionQueue.poll();

        SoftAssertions soft = new SoftAssertions();

        soft.assertThat(disconnect.getType()).isEqualTo(DISCONNECT);
        soft.assertThat(actionQueue.poll()).isNull();

        soft.assertAll();
    }
}

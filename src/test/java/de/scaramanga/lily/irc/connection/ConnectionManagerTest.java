package de.scaramanga.lily.irc.connection;

import de.scaramanga.lily.irc.configuration.IrcProperties;
import de.scaramanga.lily.irc.connection.actions.BroadcastActionData;
import de.scaramanga.lily.irc.connection.actions.ConnectionAction;
import de.scaramanga.lily.irc.connection.actions.JoinActionData;
import de.scaramanga.lily.irc.connection.actions.LeaveActionData;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import static de.scaramanga.lily.irc.connection.actions.ConnectionAction.ConnectionActionType.*;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = IrcProperties.class)
class ConnectionManagerTest {

  private              ConnectionManager     manager;
  private              MessageHandler        messageHandlerMock     = mock(MessageHandler.class);
  private              RootMessageHandler    rootMessageHandlerMock = mock(RootMessageHandler.class);
  private              SocketFactory         socketFactoryMock      = mock(SocketFactory.class);
  private              ConnectionActionQueue actionQueue;
  private              IrcProperties         properties;
  private              Connection            connectionMock         = mock(Connection.class);
  private static final String                CHANNEL                = "channel";
  private static final String                MESSAGE                = "message";

  @BeforeEach
  void setup() {

    actionQueue = new ConnectionActionQueue();
    properties  = new IrcProperties();
    properties.setEnabled(true);
    properties.setChannels(new ArrayList<>());

    manager = new ConnectionManager(properties, messageHandlerMock, rootMessageHandlerMock, socketFactoryMock,
                                    actionQueue, (a, b, c, d, e) -> connectionMock);
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

    await().atMost(1, TimeUnit.SECONDS).until(() -> actionQueue.showNextAction() != null);

    ConnectionAction join = actionQueue.nextAction();

    SoftAssertions soft = new SoftAssertions();

    soft.assertThat(join.getType()).isEqualTo(JOIN);
    soft.assertThat(((JoinActionData) join.getData()).getChannelName()).isEqualTo(CHANNEL);
    soft.assertThatThrownBy(() -> actionQueue.nextAction()).isExactlyInstanceOf(IndexOutOfBoundsException.class);

    soft.assertAll();
  }

  @Test
  void publishesLeaveAction() {

    manager.leaveChannel(CHANNEL);

    await().atMost(1, TimeUnit.SECONDS).until(() -> actionQueue.showNextAction() != null);

    ConnectionAction leave = actionQueue.nextAction();

    SoftAssertions soft = new SoftAssertions();

    soft.assertThat(leave.getType()).isEqualTo(LEAVE);
    soft.assertThat(((LeaveActionData) leave.getData()).getChannelName()).isEqualTo(CHANNEL);
    soft.assertThatThrownBy(() -> actionQueue.nextAction()).isExactlyInstanceOf(IndexOutOfBoundsException.class);

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

    await().atMost(1, TimeUnit.SECONDS).until(() -> actionQueue.showNextAction() != null);

    ConnectionAction leave = actionQueue.nextAction();

    SoftAssertions soft = new SoftAssertions();

    soft.assertThat(leave.getType()).isEqualTo(BROADCAST);
    soft.assertThat(((BroadcastActionData) leave.getData()).getMessage()).isEqualTo(MESSAGE);
    soft.assertThatThrownBy(() -> actionQueue.nextAction()).isExactlyInstanceOf(IndexOutOfBoundsException.class);;

    soft.assertAll();
  }

  private void assertDisconnectActionSent() {

    await().atMost(1, TimeUnit.SECONDS).until(() -> actionQueue.showNextAction() != null);

    ConnectionAction disconnect = actionQueue.nextAction();

    SoftAssertions soft = new SoftAssertions();

    soft.assertThat(disconnect.getType()).isEqualTo(DISCONNECT);
    soft.assertThatThrownBy(() -> actionQueue.nextAction()).isExactlyInstanceOf(IndexOutOfBoundsException.class);

    soft.assertAll();
  }
}

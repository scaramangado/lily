package de.scaramanga.lily.irc.connection;

import de.scaramanga.lily.irc.interfaces.MessageHandler;
import de.scaramanga.lily.irc.interfaces.RootMessageHandler;
import de.scaramanga.lily.irc.interfaces.SocketFactory;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class SingletonConnectionFactoryTest {

  private              SingletonConnectionFactory factory;
  private              ConnectionFactory          connectionFactoryMock;
  private static final String                     HOST_1               = "host";
  private static final String                     HOST_2               = "host2";
  private static final Integer                    PORT                 = 1;
  private static final MessageHandler             MESSAGE_HANDLER      = mock(MessageHandler.class);
  private static final RootMessageHandler         ROOT_MESSAGE_HANDLER = mock(RootMessageHandler.class);
  private static final SocketFactory              SOCKET_FACTORY       = mock(SocketFactory.class);
  private static final ConnectionActionQueue      ACTION_QUEUE         = new ConnectionActionQueue();
  private static final Connection                 CONNECTION_MOCK_1    = mock(Connection.class);
  private static final Connection                 CONNECTION_MOCK_2    = mock(Connection.class);

  @BeforeEach
  void setup() {

    connectionFactoryMock = mock(ConnectionFactory.class);
    factory               = new SingletonConnectionFactory(connectionFactoryMock);

    when(connectionFactoryMock
             .getConnection(HOST_1, PORT, MESSAGE_HANDLER, ROOT_MESSAGE_HANDLER, SOCKET_FACTORY, ACTION_QUEUE))
        .thenReturn(CONNECTION_MOCK_1);

    when(connectionFactoryMock
             .getConnection(HOST_2, PORT, MESSAGE_HANDLER, ROOT_MESSAGE_HANDLER, SOCKET_FACTORY, ACTION_QUEUE))
        .thenReturn(CONNECTION_MOCK_2);
  }

  @Test
  void producesConnectionWithCorrectInfo() {

    Connection actualConnection =
        factory.getConnection(HOST_1, PORT, MESSAGE_HANDLER, ROOT_MESSAGE_HANDLER, SOCKET_FACTORY, ACTION_QUEUE);

    assertThat(actualConnection).isEqualTo(CONNECTION_MOCK_1);

    verify(connectionFactoryMock)
        .getConnection(HOST_1, PORT, MESSAGE_HANDLER, ROOT_MESSAGE_HANDLER, SOCKET_FACTORY, ACTION_QUEUE);
  }

  @Test
  void producesConnectionOnlyOnce() {

    factory.getConnection(HOST_1, PORT, MESSAGE_HANDLER, ROOT_MESSAGE_HANDLER, SOCKET_FACTORY, ACTION_QUEUE);
    factory.getConnection(HOST_1, PORT, MESSAGE_HANDLER, ROOT_MESSAGE_HANDLER, SOCKET_FACTORY, ACTION_QUEUE);

    verify(connectionFactoryMock, times(1))
        .getConnection(HOST_1, PORT, MESSAGE_HANDLER, ROOT_MESSAGE_HANDLER, SOCKET_FACTORY, ACTION_QUEUE);
  }

  @Test
  void producesMultipleDifferentConnections() {

    Connection actualConnection1 =
        factory.getConnection(HOST_1, PORT, MESSAGE_HANDLER, ROOT_MESSAGE_HANDLER, SOCKET_FACTORY, ACTION_QUEUE);
    Connection actualConnection2 =
        factory.getConnection(HOST_2, PORT, MESSAGE_HANDLER, ROOT_MESSAGE_HANDLER, SOCKET_FACTORY, ACTION_QUEUE);

    SoftAssertions soft = new SoftAssertions();

    soft.assertThat(actualConnection1).isEqualTo(CONNECTION_MOCK_1);
    soft.assertThat(actualConnection2).isEqualTo(CONNECTION_MOCK_2);

    soft.assertAll();

    verify(connectionFactoryMock, times(2))
        .getConnection(anyString(), anyInt(), any(MessageHandler.class), any(RootMessageHandler.class),
                       any(SocketFactory.class), any(ConnectionActionQueue.class));
  }
}

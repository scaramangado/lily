package de.scaramanga.lily.irc.connection;

import de.scaramanga.lily.irc.configuration.IrcProperties;
import de.scaramanga.lily.irc.connection.actions.BroadcastActionData;
import de.scaramanga.lily.irc.connection.actions.ConnectionAction;
import de.scaramanga.lily.irc.connection.actions.JoinActionData;
import de.scaramanga.lily.irc.connection.actions.LeaveActionData;
import de.scaramanga.lily.irc.connection.ping.PingHandler;
import de.scaramanga.lily.irc.exception.IrcConnectionException;
import de.scaramanga.lily.irc.interfaces.MessageHandler;
import de.scaramanga.lily.irc.interfaces.RootMessageHandler;
import de.scaramanga.lily.irc.interfaces.SocketFactory;
import de.scaramanga.lily.testutils.InputStreamMock;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static de.scaramanga.lily.irc.connection.actions.ConnectionAction.ConnectionActionType.*;
import static org.assertj.core.api.Assertions.*;
import static org.awaitility.Awaitility.*;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.*;

class ConnectionTest {

  private static final String                HOST           = "host";
  private static final Integer               PORT           = 1;
  private static final String                CRLF           = "\n\r";
  private static final String                CHANNEL        = "channel";
  private static final String                MESSAGE        = "message";
  private static final List<String>          STRING_LIST    = List.of("a", "b", "c");
  private static final String[]              STRING_LIST_LF = new String[]{ "a" + CRLF, "b" + CRLF, "c" + CRLF };
  private static final String                EXPECTED_REGEX = "test.*";
  private static final String                REGEX_TRIGGER  = "testAbc";
  private              IrcProperties         properties     = new IrcProperties();
  private              MessageHandler        messageHandlerMock;
  private              RootMessageHandler    rootHandlerMock;
  private              Socket                socketMock;
  private              SocketFactory         socketFactoryMock;
  private              InputStreamMock       inputStreamMock;
  private              List<String>          outputBuffer;
  private              List<String>          output;
  private              ConnectionActionQueue actionQueueMock;
  private              LocalDateTime         currentTime;
  private              Connection            connection;
  private              PingHandler           pingHandlerMock;

  @BeforeEach
  void setup() throws IOException {

    properties.setHost(HOST);
    properties.setPort(PORT);
    properties.setTimeBetweenReconnects(0);

    actionQueueMock    = mock(ConnectionActionQueue.class);
    messageHandlerMock = mock(MessageHandler.class);
    rootHandlerMock    = mock(RootMessageHandler.class);
    inputStreamMock    = InputStreamMock.getInputStreamMock();
    socketMock         = mock(Socket.class);
    pingHandlerMock    = mock(PingHandler.class);
    InputStream  socketInputStreamMock  = inputStreamMock.getMock();
    OutputStream socketOutputStreamMock = mock(OutputStream.class);

    socketFactoryMock = mock(SocketFactory.class);
    when(socketFactoryMock.getSocket(any(), anyInt())).thenReturn(socketMock);

    outputBuffer = new ArrayList<>();
    output       = new ArrayList<>();

    when(socketMock.getInputStream()).thenReturn(socketInputStreamMock);
    when(socketMock.getOutputStream()).thenReturn(socketOutputStreamMock);

    doCallRealMethod().when(socketOutputStreamMock).write(any(byte[].class));
    doAnswer(this::writeToBuffer).when(socketOutputStreamMock).write(any(byte[].class), any(int.class), any(int.class));
    doAnswer(this::flushOutputStream).when(socketOutputStreamMock).flush();

    when(messageHandlerMock.handleMessage(REGEX_TRIGGER)).thenReturn(MessageAnswer.ignoreAnswer());

    connection = new Connection(properties, messageHandlerMock, rootHandlerMock, socketFactoryMock, actionQueueMock,
                                () -> currentTime, pingHandlerMock);
  }

  @Test
  void connectsToServer() {

    when(rootHandlerMock.joinMessages()).thenReturn(STRING_LIST);

    connection.call(false, true);

    assertThat(output).containsExactly(STRING_LIST_LF);
  }

  @Test
  void connectsToTheChannel() {

    ConnectionAction join = new ConnectionAction(JOIN, JoinActionData.withChannelName(CHANNEL));
    when(actionQueueMock.nextAction()).thenReturn(join).thenThrow(IndexOutOfBoundsException.class);
    when(actionQueueMock.hasAction()).thenReturn(true).thenReturn(false);
    doCallRealMethod().when(actionQueueMock).forAllActions(any());

    connection.call(false, false);

    assertThat(output).containsExactly("JOIN #" + CHANNEL + CRLF);
  }

  @Test
  void leavesChannel() {

    ConnectionAction leave = new ConnectionAction(LEAVE, LeaveActionData.withChannelName(CHANNEL));
    when(actionQueueMock.nextAction()).thenReturn(leave).thenThrow(IndexOutOfBoundsException.class);
    when(actionQueueMock.hasAction()).thenReturn(true).thenReturn(false);
    doCallRealMethod().when(actionQueueMock).forAllActions(any());

    connection.call(false, false);

    assertThat(output).containsExactly("PART #" + CHANNEL + CRLF);
  }

  @Test
  void sendsMessagesAfterHandling() throws IOException {

    inputStreamMock.provideLine(MESSAGE + CRLF);

    when(messageHandlerMock.handleMessage(MESSAGE)).thenReturn(MessageAnswer.sendLines(STRING_LIST));
    connection.socketSetup();
    connection.receiveLine();

    assertThat(output).containsExactly(STRING_LIST_LF);
  }

  @Test
  void ignoresMessages() {

    inputStreamMock.provideLine(MESSAGE + CRLF);

    when(messageHandlerMock.handleMessage(MESSAGE)).thenReturn(MessageAnswer.ignoreAnswer());
    connection.call(false, false);

    assertThat(output).isEmpty();
  }

  @Test
  void broadcasts() {

    ConnectionAction broadcast = new ConnectionAction(BROADCAST, BroadcastActionData.withMessage(MESSAGE));
    when(actionQueueMock.nextAction()).thenReturn(broadcast).thenThrow(IndexOutOfBoundsException.class);
    when(actionQueueMock.hasAction()).thenReturn(true).thenReturn(false);
    doCallRealMethod().when(actionQueueMock).forAllActions(any());

    connection.addChannelToList("a");
    connection.addChannelToList("b");

    connection.call(false, false);

    String messageFormat = "PRIVMSG #%s :" + MESSAGE + CRLF;

    assertThat(output)
        .containsExactlyInAnyOrder(String.format(messageFormat, "a"), String.format(messageFormat, "b"));
  }

  @Test
  void disconnectsFromChannelsAndServer() {

    ConnectionAction join = new ConnectionAction(JOIN, JoinActionData.withChannelName(CHANNEL));
    when(actionQueueMock.nextAction()).thenReturn(join).thenThrow(IndexOutOfBoundsException.class);
    when(actionQueueMock.hasAction()).thenReturn(true).thenReturn(false);
    doCallRealMethod().when(actionQueueMock).forAllActions(any());

    connection.call(false, false);

    output.clear();
    reset(actionQueueMock);

    ConnectionAction disconnect = new ConnectionAction(DISCONNECT, null);
    when(actionQueueMock.nextAction()).thenReturn(disconnect).thenThrow(IndexOutOfBoundsException.class);
    when(actionQueueMock.hasAction()).thenReturn(true).thenReturn(false);
    doCallRealMethod().when(actionQueueMock).forAllActions(any());
    connection.call(false, false);

    assertThat(output).containsExactly("PART #" + CHANNEL + CRLF, "QUIT" + CRLF);
  }

  @Test
  void awaitsMessageAndCallsCallback() {

    AtomicBoolean callbackCalled = new AtomicBoolean(false);

    connection
        .awaitMessage(EXPECTED_REGEX)
        .thenCall(message -> callbackCalled.set(message.equals(REGEX_TRIGGER)));

    inputStreamMock.provideLine(REGEX_TRIGGER + CRLF);
    connection.call(false, false);

    await("Callback not called").atMost(5, TimeUnit.SECONDS).untilTrue(callbackCalled);
  }

  @Test
  void deletesCallbackAfterTriggering() {

    AtomicInteger count = new AtomicInteger(0);

    connection
        .awaitMessage(EXPECTED_REGEX)
        .thenCall(message -> count.incrementAndGet());

    inputStreamMock.provideLine(REGEX_TRIGGER + CRLF);
    connection.call(false, false);

    inputStreamMock.provideLine(REGEX_TRIGGER + CRLF);
    connection.call(false, false);

    assertThat(count.get()).isEqualTo(1);
  }

  @Test
  void callsFallbackOnTimeout() {

    AtomicBoolean fallbackCalled = new AtomicBoolean(false);

    LocalDateTime baseTime = LocalDateTime.of(2019, 5, 30, 16, 29, 0);
    currentTime = baseTime;

    connection
        .awaitMessage(EXPECTED_REGEX)
        .atMost(Duration.ofMinutes(1L))
        .onTimeoutCall(() -> fallbackCalled.set(true))
        .thenDoNothing();

    SoftAssertions soft = new SoftAssertions();

    currentTime = baseTime.plus(Duration.ofSeconds(59L));
    connection.call(false, false);

    soft.assertThat(fallbackCalled.get()).isFalse();

    currentTime = baseTime.plus(Duration.ofMinutes(60L));
    connection.call(false, false);

    soft.assertThat(fallbackCalled.get()).isTrue();

    soft.assertAll();
  }

  @Test
  void callsPingHandlerWhenNothingReceived() {

    connection.call(false, false);

    verify(pingHandlerMock).checkedForMessage(connection, false);
  }

  @Test
  void callsPingHandlerWhenMessageWasReceived() {

    inputStreamMock.provideLine(MESSAGE + CRLF);

    when(messageHandlerMock.handleMessage(MESSAGE)).thenReturn(MessageAnswer.ignoreAnswer());
    connection.call(false, false);

    verify(pingHandlerMock).checkedForMessage(connection, true);
  }

  @Test
  void ignoresMaxRetryCountIfCorrespondingMethodIsCalled() {

    properties.setTriesToReconnect(1);

    when(socketFactoryMock.getSocket(any(), anyInt()))
        .thenThrow(IrcConnectionException.class)
        .thenThrow(IrcConnectionException.class)
        .thenReturn(socketMock);

    connection.reconnect();

    verify(socketFactoryMock, times(3)).getSocket(any(), anyInt());
  }

  @Test
  void ignoresMaxRetryCountAndCallsRunnable() {

    AtomicBoolean called = new AtomicBoolean(false);

    properties.setTriesToReconnect(1);

    when(socketFactoryMock.getSocket(any(), anyInt()))
        .thenThrow(IrcConnectionException.class)
        .thenThrow(IrcConnectionException.class)
        .thenReturn(socketMock);

    connection.reconnect(() -> called.set(true));

    verify(socketFactoryMock, times(3)).getSocket(any(), anyInt());
    assertThat(called.get()).isTrue();
  }

  @Test
  void abortsReconnectingWhenMaxIsSet() {

    when(socketFactoryMock.getSocket(any(), anyInt())).thenThrow(IrcConnectionException.class);

    connection.reconnect(2);

    verify(socketFactoryMock, times(2)).getSocket(any(), anyInt());
  }

  @Test
  void abortReconnectAndCallsFallbackWhenMaxIsSetAndFails() {

    AtomicBoolean result = new AtomicBoolean(true);

    when(socketFactoryMock.getSocket(any(), anyInt())).thenThrow(IrcConnectionException.class);

    connection.reconnect(2, result::set);

    verify(socketFactoryMock, times(2)).getSocket(any(), anyInt());
    assertThat(result.get()).isFalse();
  }

  @Test
  void abortsReconnectAndCallsFallbackWhenMaxIsSetAndSucceeds() {

    AtomicBoolean result = new AtomicBoolean(false);

    when(socketFactoryMock.getSocket(any(), anyInt()))
        .thenThrow(IrcConnectionException.class)
        .thenReturn(socketMock);

    connection.reconnect(3, result::set);

    verify(socketFactoryMock, times(2)).getSocket(any(), anyInt());
    assertThat(result.get()).isTrue();
  }

  @Test
  void delaysReconnectWithoutMaxRetries() {

    properties.setTimeBetweenReconnects(1);

    when(socketFactoryMock.getSocket(any(), anyInt()))
        .thenThrow(IrcConnectionException.class)
        .thenReturn(socketMock);

    ExecutorService service = Executors.newSingleThreadExecutor();
    service.submit(() -> connection.reconnect());

    verify(socketFactoryMock, after(900L).atMost(1)).getSocket(any(), anyInt());
    verify(socketFactoryMock, after(1100L).times(2)).getSocket(any(), anyInt());
  }

  @Test
  void delaysReconnectWithMaxRetries() {

    properties.setTimeBetweenReconnects(1);

    when(socketFactoryMock.getSocket(any(), anyInt()))
        .thenThrow(IrcConnectionException.class)
        .thenReturn(socketMock);

    ExecutorService service = Executors.newSingleThreadExecutor();
    service.submit(() -> connection.reconnect(3));

    verify(socketFactoryMock, after(900L).atMost(1)).getSocket(any(), anyInt());
    verify(socketFactoryMock, after(1100L).times(2)).getSocket(any(), anyInt());
  }

  @Test
  void sendsInitialMessagesAfterReconnecting() {
    fail("Test case 'sendsInitialMessagesAfterReconnecting' not implemented.");
  }

  private Answer<Void> writeToBuffer(InvocationOnMock invocation) {

    String line = new String((byte[]) invocation.getArgument(0));
    outputBuffer.add(line.substring(0, invocation.getArgument(2)));
    return null;
  }

  private Answer<Void> flushOutputStream(@SuppressWarnings("unused") InvocationOnMock invocation) {

    output.addAll(outputBuffer);
    outputBuffer.clear();
    return null;
  }
}

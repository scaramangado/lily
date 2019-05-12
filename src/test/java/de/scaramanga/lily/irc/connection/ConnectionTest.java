package de.scaramanga.lily.irc.connection;

import de.scaramanga.lily.irc.connection.actions.BroadcastActionData;
import de.scaramanga.lily.irc.connection.actions.ConnectionAction;
import de.scaramanga.lily.irc.connection.actions.JoinActionData;
import de.scaramanga.lily.irc.connection.actions.LeaveActionData;
import de.scaramanga.lily.irc.interfaces.MessageHandler;
import de.scaramanga.lily.irc.interfaces.RootMessageHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.*;

import static de.scaramanga.lily.irc.connection.actions.ConnectionAction.ConnectionActionType.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class ConnectionTest {

    private static final String HOST = "host";
    private static final Integer PORT = 1;
    private static final String CRLF = "\n\r";
    private static final String CHANNEL = "channel";
    private static final String MESSAGE = "message";
    private static final List<String> STRING_LIST = List.of("a", "b", "c");
    private static final String[] STRING_LIST_LF = new String[] { "a" + CRLF, "b" + CRLF, "c" + CRLF };

    private MessageHandler messageHandlerMock;
    private RootMessageHandler rootHandlerMock;
    private Socket socketMock;
    private InputStream socketInputStreamMock;
    private List<String> outputBuffer;
    private List<String> output;
    private Queue<ConnectionAction> actionQueueMock;
    private Connection connection;

    @BeforeEach
    void setup() throws IOException {

        //noinspection unchecked
        actionQueueMock = (Queue<ConnectionAction>) mock(Queue.class);
        messageHandlerMock = mock(MessageHandler.class);
        rootHandlerMock = mock(RootMessageHandler.class);
        socketMock = mock(Socket.class);
        socketInputStreamMock = mock(InputStream.class);
        OutputStream socketOutputStreamMock = mock(OutputStream.class);

        outputBuffer = new ArrayList<>();
        output = new ArrayList<>();

        when(socketMock.getInputStream()).thenReturn(socketInputStreamMock);
        when(socketMock.getOutputStream()).thenReturn(socketOutputStreamMock);

        doCallRealMethod().when(socketOutputStreamMock).write(any(byte[].class));
        doAnswer(this::writeToBuffer).when(socketOutputStreamMock).write(any(byte[].class), any(int.class), any(int.class));
        doAnswer(this::flushOutputStream).when(socketOutputStreamMock).flush();

        connection = new Connection(HOST, PORT, messageHandlerMock, rootHandlerMock, (a, b) -> socketMock, actionQueueMock);
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
        when(actionQueueMock.poll()).thenReturn(join).thenReturn(null);

        connection.call(false, false);

        assertThat(output).containsExactly("JOIN #" + CHANNEL + CRLF);
    }

    @Test
    void leavesChannel() {

        ConnectionAction leave = new ConnectionAction(LEAVE, LeaveActionData.withChannelName(CHANNEL));
        when(actionQueueMock.poll()).thenReturn(leave).thenReturn(null);

        connection.call(false, false);

        assertThat(output).containsExactly("PART #" + CHANNEL + CRLF);
    }

    @Test
    void sendsMessagesAfterHandling() throws IOException {

        setupInputStream(MESSAGE + CRLF);

        when(messageHandlerMock.handleMessage(MESSAGE)).thenReturn(MessageAnswer.sendLines(STRING_LIST));
        connection.socketSetup();
        connection.receiveLine();

        assertThat(output).containsExactly(STRING_LIST_LF);
    }

    @Test
    void ignoresMessages() throws IOException {

        setupInputStream(MESSAGE + CRLF);

        when(messageHandlerMock.handleMessage(MESSAGE)).thenReturn(MessageAnswer.ignoreAnswer());
        connection.call(false, false);

        assertThat(output).isEmpty();
    }

    @Test
    void broadcasts() {

        ConnectionAction broadcast = new ConnectionAction(BROADCAST, BroadcastActionData.withMessage(MESSAGE));
        when(actionQueueMock.poll()).thenReturn(broadcast).thenReturn(null);

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
        when(actionQueueMock.poll()).thenReturn(join).thenReturn(null);
        connection.call(false, false);

        output.clear();

        ConnectionAction disconnect = new ConnectionAction(DISCONNECT, null);
        when(actionQueueMock.poll()).thenReturn(disconnect).thenReturn(null);
        connection.call(false, false);

        assertThat(output).containsExactly("PART #" + CHANNEL + CRLF, "QUIT" + CRLF);
    }

    private Answer<Void> writeToBuffer(InvocationOnMock invocation) {

        String line = new String((byte[]) invocation.getArgument(0));
        outputBuffer.add(line.substring(0, invocation.getArgument(2)));
        return null;
    }

    private Answer<Void> flushOutputStream(InvocationOnMock invocation) {

        output.addAll(outputBuffer);
        outputBuffer.clear();
        return null;
    }

    private void setupInputStream(String message) throws IOException {

        when(socketInputStreamMock.available()).thenReturn(message.getBytes().length);
        //noinspection ResultOfMethodCallIgnored
        doAnswer(invocation -> provideInputStream(message, invocation))
                .when(socketInputStreamMock).read(any(byte[].class), any(int.class), any(int.class));

        verifyNoMoreInteractions(socketInputStreamMock);
    }

    private Object provideInputStream(String message, InvocationOnMock invocation) {

        byte[] answerMessage = message.getBytes();

        Object[] args = invocation.getArguments();
        byte[] bytes = (byte[]) args[0];
        int startPosition = (int) args[1];

        System.arraycopy(answerMessage, 0, bytes, startPosition, answerMessage.length);

        try {
            resetInputStream();
        } catch (IOException e) {
            // be silent
        }

        return answerMessage.length;
    }

    private void resetInputStream() throws IOException {
        when(socketInputStreamMock.available()).thenReturn(0);
        when(socketInputStreamMock.read()).thenReturn(0);
    }
}

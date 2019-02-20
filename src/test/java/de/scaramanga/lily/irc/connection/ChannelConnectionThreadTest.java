package de.scaramanga.lily.irc.connection;


import de.scaramanga.lily.core.communication.Dispatcher;
import de.scaramanga.lily.irc.exceptions.IrcConnectionInterruptedException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockito.invocation.InvocationOnMock;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Optional;

import static de.scaramanga.lily.irc.connection.IrcAction.Type.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ChannelConnectionThreadTest {

    private final Socket socket = mock(Socket.class);
    private final Dispatcher dispatcher = mock(Dispatcher.class);
    private final MessageHandler messageHandler = mock(MessageHandler.class);

    private final InputStream socketInputStream = mock(InputStream.class);
    private final ByteArrayOutputStream socketOutputStream = spy(ByteArrayOutputStream.class);

    private static final String USERNAME = "user";
    private static final String PASSWORD = "pw";
    private static final String CHANNEL = "channel";
    private static final String PRIVMSG = "PRIVMSG";
    private static final String CRLF = "\r\n";

    private static final String ANSWER_MESSAGE = "answerMessage";
    private static final String PARSE_MESSAGE = "parseMessage";
    private static final String IGNORE_MESSAGE = "ignoreMessage";

    private static final String PARSED_ANSWER = "parsedAnswer";

    private final ChannelConnectionThread connection = new ChannelConnectionThread(
            new ConnectionInfo("", 0, USERNAME, PASSWORD, CHANNEL),
            dispatcher,
            messageHandler) {

        @Override
        protected Socket ircSocket(ConnectionInfo info) {
            return socket;
        }
    };

    @BeforeAll
    void setup() throws IOException {

        when(socket.getInputStream()).thenReturn(socketInputStream);
        when(socket.getOutputStream()).thenReturn(socketOutputStream);

        when(messageHandler.handleMessage(ANSWER_MESSAGE))
                .thenReturn(new IrcAction(ANSWER, ANSWER_MESSAGE));

        when(messageHandler.handleMessage(PARSE_MESSAGE))
                .thenReturn(new IrcAction(PARSE, PARSE_MESSAGE));
        when(dispatcher.dispatch(eq(PARSE_MESSAGE), any(String[].class)))
                .thenReturn(Optional.of(PARSED_ANSWER));

        when(messageHandler.handleMessage(IGNORE_MESSAGE))
                .thenReturn(new IrcAction(IGNORE, null));

        resetInputStream();
    }

    @Test
    @Order(1)
    void connectsToTheChannel() {

        connection.call(false, true);

        byte[] expectedOutput = ("PASS " + PASSWORD + CRLF +
                "NICK " + USERNAME + CRLF +
                "USER " + USERNAME + CRLF +
                "JOIN " + "#" + CHANNEL + CRLF).getBytes();

        byte[] actualOutput = socketOutputStream.toByteArray();
        socketOutputStream.reset();

        assertThat(actualOutput)
                .as("Does not connect with correct commands.")
                .isEqualTo(expectedOutput);
    }

    @Test
    @Order(2)
    void answers() throws IOException, IrcConnectionInterruptedException {

        byte[] answerMessage = (ANSWER_MESSAGE + CRLF).getBytes();

        //noinspection ResultOfMethodCallIgnored
        doAnswer(invocation -> answerMessageInputStream(answerMessage, invocation))
                .when(socketInputStream).read(any(byte[].class), any(int.class), any(int.class));

        when(socketInputStream.available()).thenReturn(answerMessage.length).thenReturn(0);

        connection.listen(false);

        byte[] actualOutput = socketOutputStream.toByteArray();
        socketOutputStream.reset();
        resetInputStream();

        assertThat(actualOutput)
                .as("Did not answer correctly. Answer: " + new String(actualOutput))
                .isEqualTo(answerMessage);
    }

    @Test
    @Order(2)
    void parse() throws IOException, IrcConnectionInterruptedException {

        byte[] parseMessage = (PARSE_MESSAGE + CRLF).getBytes();

        //noinspection ResultOfMethodCallIgnored
        doAnswer(invocation -> answerMessageInputStream(parseMessage, invocation))
                .when(socketInputStream).read(any(byte[].class), any(int.class), any(int.class));

        when(socketInputStream.available()).thenReturn(parseMessage.length).thenReturn(0);

        connection.listen(false);

        byte[] actualOutput = socketOutputStream.toByteArray();
        socketOutputStream.reset();
        resetInputStream();

        assertThat(new String(actualOutput))
                .as("Did not parse message correctly.")
                .isEqualTo(PRIVMSG + " #" + CHANNEL + " :" + PARSED_ANSWER + CRLF);
    }

    @Test
    @Order(2)
    void ignore() throws IOException, IrcConnectionInterruptedException {

        byte[] ignoreMessage = (IGNORE_MESSAGE + CRLF).getBytes();

        //noinspection ResultOfMethodCallIgnored
        doAnswer(invocation -> answerMessageInputStream(ignoreMessage, invocation))
                .when(socketInputStream).read(any(byte[].class), any(int.class), any(int.class));

        when(socketInputStream.available()).thenReturn(ignoreMessage.length).thenReturn(0);

        connection.listen(false);

        int lengthOfOutput = socketOutputStream.toByteArray().length;
        socketOutputStream.reset();
        resetInputStream();

        assertThat(lengthOfOutput)
                .as("Did not ignore message.")
                .isEqualTo(0);
    }

    @Test
    @Order(Integer.MAX_VALUE)
    void disconnectsWhenInterrupted() throws IOException {

        connection.interrupt();

        connection.call(true, false);

        byte[] actualOutput = socketOutputStream.toByteArray();

        verify(socketInputStream).close();
        verify(socketOutputStream).close();
        verify(socket).close();

        assertThat(new String(actualOutput))
                .as("")
                .isEqualTo("PART #" + CHANNEL + CRLF + "QUIT" + CRLF);
    }

    private Object answerMessageInputStream(byte[] answerMessage, InvocationOnMock invocation) {

        Object[] args = invocation.getArguments();
        byte[] bytes = (byte[]) args[0];
        int startPosition = (int) args[1];

        System.arraycopy(answerMessage, 0, bytes, startPosition, answerMessage.length);

        return answerMessage.length;
    }

    private void resetInputStream() throws IOException {
        when(socketInputStream.available()).thenReturn(0);
        when(socketInputStream.read()).thenReturn(0);
    }
}

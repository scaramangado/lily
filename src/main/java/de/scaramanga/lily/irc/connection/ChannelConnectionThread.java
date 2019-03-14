package de.scaramanga.lily.irc.connection;

import de.scaramanga.lily.core.communication.Answer;
import de.scaramanga.lily.core.communication.Dispatcher;
import de.scaramanga.lily.core.communication.MessageInfo;
import de.scaramanga.lily.irc.exceptions.IrcConnectionException;
import de.scaramanga.lily.irc.exceptions.IrcConnectionInterruptedException;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

import static de.scaramanga.lily.irc.connection.IrcAction.Type.ANSWER;
import static de.scaramanga.lily.irc.connection.IrcAction.Type.PARSE;

@Slf4j
class ChannelConnectionThread implements Callable<Void> {

    private BufferedReader reader;

    private BufferedWriter writer;

    private final ConnectionInfo info;

    private final String channel;

    private final Dispatcher dispatcher;

    private final MessageHandler messageHandler;

    private static final String PRIVMSG = "PRIVMSG";

    private static final String CRLF = "\r\n";

    private static AtomicBoolean interrupted = new AtomicBoolean(false);

    ChannelConnectionThread(ConnectionInfo info, Dispatcher dispatcher, MessageHandler messageHandler) {
        this.info = info;
        this.dispatcher = dispatcher;
        this.messageHandler = messageHandler;
        this.channel = "#" + info.getChannel();
    }

    @Override
    public Void call() {
        return call(true, true);
    }

    Void call(boolean listen, boolean establishConnection) {

        Socket socket = ircSocket(info);

        if (establishConnection) {
            establishConnection(socket);
        }

        if (listen) {
            try {
                listen(true);
            } catch (IrcConnectionInterruptedException e){
                disconnectSilently();
            } finally {
                closeSilently(reader);
                closeSilently(writer);
                closeSilently(socket);
            }
        }

        return null;
    }

    private void authenticate() throws IOException {
        writer.write("PASS " + info.getPassword() + CRLF);
        writer.write("NICK " + info.getUsername() + CRLF);
        writer.write("USER " + info.getUsername() + CRLF);
        writer.write("JOIN " + channel + CRLF);
        writer.flush();
    }

    private void disconnectSilently() {

        try {
            writer.write("PART " + channel + CRLF);
            writer.write("QUIT" + CRLF);
            writer.flush();
        } catch (IOException e){
            LOGGER.debug("Couldn't send disconnect messages to IRC.", e);
        }
    }

    private void establishConnection(Socket socket) {
        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            authenticate();
        } catch (IOException e) {
            LOGGER.error("Can't authenticate.", e);
            closeSilently(socket);
            throw new IrcConnectionException("Can't authenticate.", e);
        }
    }

    void interrupt() {
        interrupted.set(true);
    }

    @SuppressWarnings("squid:S2189") // Holding connection
    void listen(boolean stayAlive) throws IrcConnectionInterruptedException {

        try {

            do {
                if (interrupted.get()) {
                    throw new IrcConnectionInterruptedException();
                }
                if (reader.ready()) {
                    readMessage(reader.readLine());
                }
                Thread.sleep(50);
            } while (stayAlive && !interrupted.get());
        } catch (InterruptedException e) {
            disconnectSilently();
            Thread.currentThread().interrupt();
            LOGGER.info("Disconnecting from channel #{}.", info.getChannel());
        } catch(IOException e) {
            LOGGER.error("IOError.", e);
        }
    }

    private void readMessage(String message) {

        IrcAction action = messageHandler.handleMessage(message);

        if (action.getType() == PARSE) {

            String command = action.getAnswer().orElse("");

            dispatcher.dispatch(command, MessageInfo.empty()).ifPresent(this::sendMessage);
        }

        if (action.getType() == ANSWER) {
            action.getAnswer().ifPresent(this::sendLine);
        }
    }

    private void sendMessage(Answer answer) {

        try {
            writer.write(PRIVMSG + " " + channel + " :" + answer.getText() + CRLF);
            writer.flush();
        } catch (IOException e) {
            LOGGER.error("Couldn't send line.", e);
        }
    }

    private void sendLine(String line) {

        try {
            writer.write(line + CRLF);
            writer.flush();
        } catch (IOException e) {
            LOGGER.error("Couldn't send line.", e);
        }
    }

    protected Socket ircSocket(ConnectionInfo info) {

        Socket socket;
        try {
            socket = new Socket(info.getHost(), info.getPort());
        } catch (IOException e) {
            throw new IrcConnectionException("Can't create IRC socket.", e);
        }

        return socket;
    }

    private void closeSilently(Closeable closeable) {
        try {
            closeable.close();
        } catch (IOException e) {
            LOGGER.error("Error while closing.", e);
        }
    }
}

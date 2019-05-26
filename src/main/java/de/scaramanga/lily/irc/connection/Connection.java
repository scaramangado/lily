package de.scaramanga.lily.irc.connection;

import de.scaramanga.lily.irc.connection.actions.BroadcastActionData;
import de.scaramanga.lily.irc.connection.actions.ConnectionAction;
import de.scaramanga.lily.irc.connection.actions.ConnectionAction.ConnectionActionType;
import de.scaramanga.lily.irc.connection.actions.ConnectionActionData;
import de.scaramanga.lily.irc.connection.actions.JoinActionData;
import de.scaramanga.lily.irc.connection.actions.LeaveActionData;
import de.scaramanga.lily.irc.exception.IrcConnectionException;
import de.scaramanga.lily.irc.interfaces.MessageHandler;
import de.scaramanga.lily.irc.interfaces.RootMessageHandler;
import de.scaramanga.lily.irc.interfaces.SocketFactory;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static de.scaramanga.lily.irc.connection.MessageAnswer.AnswerType.SEND_LINES;
import static de.scaramanga.lily.irc.connection.actions.ConnectionAction.ConnectionActionType.*;

@Slf4j
class Connection implements Callable<Void> {

  private final Supplier<Socket>        socketSupplier;
  private final MessageHandler          messageHandler;
  private final RootMessageHandler      rootHandler;
  private final Queue<ConnectionAction> actionQueue;
  private final List<String>            channels    = new ArrayList<>();
  private final AtomicBoolean           interrupted = new AtomicBoolean(false);

  private BufferedReader reader;
  private Writer         writer;

  private static final String CRLF               = "\n\r";
  private static final String WRONG_DATA_MESSAGE = "Action data of wrong type.";

  Connection(String host, Integer port, MessageHandler messageHandler, RootMessageHandler rootHandler,
             SocketFactory socketFactory, Queue<ConnectionAction> actionQueue) {

    this.messageHandler = messageHandler;
    this.rootHandler    = rootHandler;
    this.actionQueue    = actionQueue;
    this.socketSupplier = () -> socketFactory.getSocket(host, port);
  }

  @Override
  public Void call() {

    try {
      call(true, true);
    } catch (IrcConnectionException e) {
      Thread.currentThread().interrupt();
    }

    return null;
  }

  void call(boolean keepAlive, boolean establishConnection) {

    socketSetup();

    if (establishConnection) {
      List<String> join = rootHandler.joinMessages();
      join.forEach(this::sendLine);
    }

    do {

      emptyQueue();

      try {
        receiveLine();
        Thread.sleep(50);
      } catch (IOException | InterruptedException e) {
        LOGGER.error("IRC connection interrupted.", e);
        keepAlive = false;
        disconnect(null);
        Thread.currentThread().interrupt();
      }
    } while (keepAlive && !interrupted.get());
  }

  void socketSetup() {

    Socket socket = socketSupplier.get();

    try {
      reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      writer = new OutputStreamWriter(socket.getOutputStream());
    } catch (IOException e) {
      LOGGER.error("Can't get socket streams.");
      throw new IrcConnectionException();
    }
  }

  void receiveLine() throws IOException {

    if (reader.ready()) {
      String message = reader.readLine();
      LOGGER.info(message);
      handleMessage(message);
    }
  }

  void addChannelToList(String channel) {

    channels.add(channel);
  }

  private void sendLine(String line) {

    try {
      writer.write(line + CRLF);
      writer.flush();
    } catch (IOException e) {
      LOGGER.error("Can't send line.");
    }
  }

  private void joinChannel(ConnectionActionData data) {

    if (!(data instanceof JoinActionData)) {
      throw new IllegalArgumentException(WRONG_DATA_MESSAGE);
    }

    String channel = ((JoinActionData) data).getChannelName();
    sendLine("JOIN #" + channel);

    if (!channels.contains(channel)) {
      channels.add(channel);
    }
  }

  private void leaveChannel(ConnectionActionData data) {

    if (!(data instanceof LeaveActionData)) {
      throw new IllegalArgumentException(WRONG_DATA_MESSAGE);
    }

    String channel = ((LeaveActionData) data).getChannelName();
    sendLine("PART #" + channel);

    channels.remove(channel);
  }

  private void disconnect(@SuppressWarnings({ "unused", "squid:S1172" }) ConnectionActionData data) {

    new ArrayList<>(channels).forEach(channel -> leaveChannel(LeaveActionData.withChannelName(channel)));
    sendLine("QUIT");
    interrupted.set(true);
  }

  private void broadcast(ConnectionActionData data) {

    if (!(data instanceof BroadcastActionData)) {
      throw new IllegalArgumentException(WRONG_DATA_MESSAGE);
    }

    channels.forEach(channel -> sendLine("PRIVMSG #" + channel + " :" + ((BroadcastActionData) data).getMessage()));
  }

  private void handleMessage(String message) {

    MessageAnswer answer = messageHandler.handleMessage(message);

    if (answer.getAnswerType() == SEND_LINES) {
      answer.getLines().forEach(this::sendLine);
    }
  }

  private void performAction(ConnectionAction action) {

    Map<ConnectionActionType, Consumer<ConnectionActionData>> actionMap = new EnumMap<>(ConnectionActionType.class);

    actionMap.put(JOIN, this::joinChannel);
    actionMap.put(LEAVE, this::leaveChannel);
    actionMap.put(DISCONNECT, this::disconnect);
    actionMap.put(BROADCAST, this::broadcast);

    if (!actionMap.containsKey(action.getType())) {
      throw new UnsupportedOperationException("Unknown action.");
    }

    actionMap.get(action.getType()).accept(action.getData());
  }

  private void emptyQueue() {

    ConnectionAction action;

    while ((action = actionQueue.poll()) != null) {
      performAction(action);
    }
  }
}

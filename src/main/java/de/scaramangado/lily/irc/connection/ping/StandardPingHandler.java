package de.scaramangado.lily.irc.connection.ping;

import de.scaramangado.lily.irc.configuration.IrcProperties;
import de.scaramangado.lily.irc.configuration.ReconnectStrategy;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

@Component
@Slf4j
public class StandardPingHandler implements PingHandler {

  private final IrcProperties           properties;
  private final Supplier<LocalDateTime> currentTimeSupplier;
  private       Map<Reconnectable, State> reconnectableStates = new HashMap<>();

  @Autowired
  public StandardPingHandler(IrcProperties properties) {

    this(properties, LocalDateTime::now);
  }

  StandardPingHandler(IrcProperties properties,
                      Supplier<LocalDateTime> currentTimeSupplier) {

    this.properties          = properties;
    this.currentTimeSupplier = currentTimeSupplier;
  }

  @Override
  public void checkedForMessage(Reconnectable reconnectable, boolean received) {

    if (properties.getReconnectStrategy() == ReconnectStrategy.NO_RECONNECT) {
      return;
    }

    checkReconnectable(reconnectable);

    if (received) {

      messageReceived(reconnectable);
      return;
    }

    checkForDisconnects();
  }

  private void checkForDisconnects() {

    LocalDateTime disconnectTime = currentTimeSupplier.get().minusSeconds(properties.getPingTimeout());

    reconnectableStates
        .entrySet()
        .stream()
        .filter(e -> e.getValue().getReconnectState() == ReconnectState.NOT_RUNNING)
        .filter(e -> e.getValue().lastMessageReceived.isBefore(disconnectTime))
        .map(Map.Entry::getKey)
        .forEach(this::pingReconnectable);
  }

  private void pingReconnectable(Reconnectable reconnectable) {

    String random = reconnectable.sendPing();
    reconnectableStates.get(reconnectable).setReconnectState(ReconnectState.PING);

    reconnectable
        .awaitMessage(".*PONG.*" + random)
        .atMost(Duration.ofSeconds(1))
        .onTimeoutCall(() -> requestReconnect(reconnectable))
        .thenCall(s -> messageReceived(reconnectable));
  }

  private void requestReconnect(Reconnectable reconnectable) {

    reconnectableStates.get(reconnectable).setReconnectState(ReconnectState.RUNNING);

    switch (properties.getReconnectStrategy()) {
      case TRY_FOREVER:
        reconnectable.reconnect();
        break;
      case ABORT:
        reconnectable.reconnect(properties.getTriesToReconnect());
        break;
      default:
        LOGGER.error("Unknown reconnect strategy.");
    }
  }

  private void messageReceived(Reconnectable reconnectable) {

    State state = reconnectableStates.get(reconnectable);
    state.setLastMessageReceived(currentTimeSupplier.get());
    state.setReconnectState(ReconnectState.NOT_RUNNING);
  }

  private void checkReconnectable(Reconnectable reconnectable) {

    reconnectableStates.putIfAbsent(reconnectable, new State());
  }

  @Getter
  @Setter
  private static class State {

    private LocalDateTime  lastMessageReceived;
    private ReconnectState reconnectState;
  }

  private enum ReconnectState {
    RUNNING, NOT_RUNNING, PING
  }
}

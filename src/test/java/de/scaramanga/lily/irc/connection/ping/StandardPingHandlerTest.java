package de.scaramanga.lily.irc.connection.ping;

import de.scaramanga.lily.irc.await.AwaitMessageBuilder;
import de.scaramanga.lily.irc.configuration.IrcProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.UUID;
import java.util.function.Consumer;

import static de.scaramanga.lily.irc.configuration.ReconnectStrategy.*;
import static org.mockito.Mockito.*;

class StandardPingHandlerTest {

  private StandardPingHandler pingHandler;
  private Reconnectable       reconnectableMock;
  private IrcProperties       properties;
  private LocalDateTime       testStart = LocalDateTime.now();
  private LocalDateTime       now;

  @BeforeEach
  void setup() {

    properties        = new IrcProperties();
    pingHandler       = new StandardPingHandler(properties, () -> LocalDateTime.from(now));
    reconnectableMock = mock(Reconnectable.class);
  }

  @Test
  void startsReconnectionAfterAwaitFailed() {

    furtherMessagesAfterSeconds(CheckedMessages.checked(300, false, false));

    verify(reconnectableMock, times(1)).sendPing();
    verify(reconnectableMock).reconnect();
  }

  @Test
  void doesNotReconnectWhenDisabled() {

    properties.setReconnectStrategy(NO_RECONNECT);
    furtherMessagesAfterSeconds(CheckedMessages.checked(300, false, false));

    verifyZeroInteractions(reconnectableMock);
  }

  @Test
  void doesNotReconnectIfMessageWithinTimeWindow() {

    properties.setPingTimeout(10);
    furtherMessagesAfterSeconds(CheckedMessages.checked(5, true, false),
                                CheckedMessages.checked(14, false, false));

    verifyZeroInteractions(reconnectableMock);
  }

  @Test
  void setsNumberOfReconnectTriesWhenConfigured() {

    properties.setReconnectStrategy(ABORT);
    properties.setTriesToReconnect(42);

    furtherMessagesAfterSeconds(CheckedMessages.checked(300, false, false));

    verify(reconnectableMock).reconnect(42);
  }

  @Test
  void pingsAgainAfterSecondPeriod() {

    furtherMessagesAfterSeconds(CheckedMessages.checked(300, false, true),
                                CheckedMessages.checked(600, false, true));

    verify(reconnectableMock, times(2)).sendPing();
  }

  private void furtherMessagesAfterSeconds(CheckedMessages... checks) {

    checkedMessage(0, true, false);

    Arrays.stream(checks)
          .forEach(c -> checkedMessage(c.afterSeconds, c.received, c.pingSuccessful));
  }

  private void checkedMessage(int seconds, boolean received, boolean pingSuccessful) {

    String random = UUID.randomUUID().toString();

    AwaitMessageBuilder builderMock = mock(AwaitMessageBuilder.class);
    when(reconnectableMock.sendPing()).thenReturn(random);
    when(reconnectableMock.awaitMessage(".*PONG.*" + random)).thenReturn(builderMock);
    when(builderMock.atMost(any())).thenReturn(builderMock);

    if (pingSuccessful) {
      doAnswer(this::callSuccess).when(builderMock).thenCall(any());
      when(builderMock.onTimeoutCall(any())).thenReturn(builderMock);
    } else {
      doAnswer(this::runFallback).when(builderMock).onTimeoutCall(any(Runnable.class));
      doNothing().when(builderMock).thenCall(any());
    }

    now = testStart.plusSeconds(seconds);
    pingHandler.checkedForMessage(reconnectableMock, received);
  }

  private Object callSuccess(InvocationOnMock invocation) {

    Consumer<String> success = invocation.getArgument(0);
    success.accept("");

    return null;
  }

  private Object runFallback(InvocationOnMock invocation) {

    Runnable fallback = invocation.getArgument(0);
    fallback.run();
    return invocation.getMock();
  }

  @Getter
  @AllArgsConstructor
  private static class CheckedMessages {

    private int     afterSeconds;
    private boolean received;
    private boolean pingSuccessful;

    static CheckedMessages checked(int afterSeconds, boolean received, boolean pingSuccessful) {

      return new CheckedMessages(afterSeconds, received, pingSuccessful);
    }
  }
}

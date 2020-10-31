package de.scaramangado.lily.discord.connection;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.annotation.Nonnull;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class DiscordEventListenerTest {

  private       DiscordEventListener<TestEvent> listener;
  private final AtomicBoolean                   listenerCalled = new AtomicBoolean(false);

  @BeforeEach
  void setup() {

    listener = event -> listenerCalled.set(true);
  }

  @Test
  void listenerIsCalledWhenTestEventReceived() {

    whenEventReceived(new TestEvent());

    thenListenerIsCalled(true);
  }

  @Test
  void listenerIsNotCalledWhenGenericEventReceived() {

    whenEventReceived(new GenericEvent() {
      @Nonnull
      @Override
      public JDA getJDA() {

        return mock(JDA.class);
      }

      @Override
      public long getResponseNumber() {

        return 0;
      }
    });

    thenListenerIsCalled(false);
  }

  @Test
  void listenerIsNotCalledWhenOtherEventIsReceived() {

    whenEventReceived(new MessageReceivedEvent(mock(JDA.class), 0, mock(Message.class)));

    thenListenerIsCalled(false);
  }

  private void whenEventReceived(GenericEvent event) {

    listener.onEvent(event);
  }

  private void thenListenerIsCalled(boolean called) {

    assertThat(listenerCalled.get()).isEqualTo(called);
  }

  private static class TestEvent implements GenericEvent {

    @Nonnull
    @Override
    public JDA getJDA() {

      return mock(JDA.class);
    }

    @Override
    public long getResponseNumber() {

      return 0;
    }
  }
}

package de.scaramangado.lily.discord;

import de.scaramangado.lily.discord.configuration.DiscordProperties;
import de.scaramangado.lily.discord.connection.DiscordEventListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;

import javax.security.auth.login.LoginException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;
import static org.awaitility.Awaitility.*;
import static org.mockito.Mockito.*;

class DiscordManagerTest {

  private DiscordManager                             manager;
  private DiscordProperties                          properties              = new DiscordProperties();
  private JDABuilder                                 jdaBuilderMock          = mock(JDABuilder.class, RETURNS_SELF);
  private AtomicInteger                              jdaBuildCount           = new AtomicInteger(0);
  private DiscordEventListener<MessageReceivedEvent> messageReceivedListener = event -> {};

  @BeforeEach
  void setup() throws LoginException {

    doAnswer(this::loginPerformed).when(jdaBuilderMock).build();

    properties.setEnabled(true);
    manager = new DiscordManager(properties, token -> jdaBuilderMock, messageReceivedListener);
  }

  @Test
  @SuppressWarnings("squid:S2699")
    // Awaitility
  void connectsOnStartup() {

    manager.contextStart(null);

    await().atMost(500, TimeUnit.MILLISECONDS).until(() -> jdaBuildCount.get() == 1);
  }

  @Test
  @SuppressWarnings("squid:S2925")
    // Verify no change
  void onlyConnectsOnce() throws InterruptedException {

    manager.contextStart(null);

    await().atMost(500, TimeUnit.MILLISECONDS).until(() -> jdaBuildCount.get() == 1);

    manager.contextStart(null);

    Thread.sleep(500);
    Assertions.assertThat(jdaBuildCount.get()).isEqualTo(1);
  }

  @Test
  void addsMessageReceivedListener() {

    manager.contextStart(null);

    await().atMost(500, TimeUnit.MILLISECONDS).until(() -> jdaBuildCount.get() == 1);

    ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
    verify(jdaBuilderMock).addEventListeners(captor.capture());

    assertThat(captor.getAllValues()).contains(messageReceivedListener);
  }

  @Test
  void doesNotThrowAnExceptionWhenLoginFails() throws LoginException {

    when(jdaBuilderMock.build()).thenThrow(LoginException.class);

    assertThatCode(() -> manager.contextStart(null)).doesNotThrowAnyException();
  }

  @Test
  @SuppressWarnings("squid:S2925")
    // Verify no change
  void doesNotConnectWhenDisabled() throws InterruptedException {

    properties.setEnabled(false);

    manager.contextStart(null);

    Thread.sleep(500);
    Assertions.assertThat(jdaBuildCount.get()).isEqualTo(0);
  }

  private Object loginPerformed(InvocationOnMock invocation) {

    jdaBuildCount.incrementAndGet();
    return mock(JDA.class);
  }
}

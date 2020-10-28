package de.scaramanga.lily.discord;

import de.scaramanga.lily.discord.configuration.DiscordProperties;
import de.scaramanga.lily.discord.connection.DiscordEventListener;
import de.scaramanga.lily.discord.connection.JdaBuilderFactory;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.security.auth.login.LoginException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
public class DiscordManager {

  private final AtomicBoolean                              connected = new AtomicBoolean(false);
  private final DiscordProperties                          properties;
  private final JdaBuilderFactory                          jdaBuilderFactory;
  private final DiscordEventListener<MessageReceivedEvent> messageReceivedListener;

  public DiscordManager(DiscordProperties properties,
                        JdaBuilderFactory jdaBuilderFactory,
                        DiscordEventListener<MessageReceivedEvent> messageReceivedListener) {

    this.properties              = properties;
    this.jdaBuilderFactory       = jdaBuilderFactory;
    this.messageReceivedListener = messageReceivedListener;
  }

  @EventListener
  public void contextStart(ContextRefreshedEvent event) {

    if (!properties.isEnabled() || connected.get()) {
      return;
    }

    LOGGER.info("Discord enabled. Connecting...");

    ExecutorService executor = Executors.newSingleThreadExecutor();
    executor.execute(this::connectToDiscord);
    connected.set(true);
  }

  private void connectToDiscord() {

    try {

      jdaBuilderFactory.getBuilder(properties.getToken())
                       .addEventListeners((DiscordEventListener<ReadyEvent>) this::onConnect, messageReceivedListener)
                       .build();
    } catch (LoginException e) {
      LOGGER.error("Could not connect to Discord.", e);
      connected.set(false);
    }
  }

  @SuppressWarnings("squid:S1172") // Lambda
  private void onConnect(ReadyEvent event) {

    LOGGER.info("Connected to Discord successfully.");
  }
}

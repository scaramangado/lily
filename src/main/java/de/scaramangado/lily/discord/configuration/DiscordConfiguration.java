package de.scaramangado.lily.discord.configuration;

import de.scaramangado.lily.core.communication.Dispatcher;
import de.scaramangado.lily.discord.connection.DiscordEventListener;
import de.scaramangado.lily.discord.connection.JdaBuilderFactory;
import de.scaramangado.lily.discord.connection.MessageListener;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DiscordConfiguration {

  private final Dispatcher        dispatcher;
  private final DiscordProperties properties;

  public DiscordConfiguration(Dispatcher dispatcher,
                              DiscordProperties properties) {

    this.dispatcher = dispatcher;
    this.properties = properties;
  }

  @Bean
  public JdaBuilderFactory standardJDABuilderFactory() {

    return JdaBuilderFactory.standardFactory();
  }

  @Bean
  public DiscordEventListener<MessageReceivedEvent> messageListener() {

    return new MessageListener(dispatcher, properties);
  }
}

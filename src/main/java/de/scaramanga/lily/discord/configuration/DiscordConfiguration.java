package de.scaramanga.lily.discord.configuration;

import de.scaramanga.lily.discord.connection.DiscordEventListener;
import de.scaramanga.lily.discord.connection.JdaBuilderFactory;
import de.scaramanga.lily.discord.connection.MessageListener;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DiscordConfiguration {

  @Bean
  public JdaBuilderFactory standardJDABuilderFactory() {
    return JdaBuilderFactory.standardFactory();
  }

  @Bean
  public DiscordEventListener<MessageReceivedEvent> messageListener() {
    return new MessageListener();
  }
}

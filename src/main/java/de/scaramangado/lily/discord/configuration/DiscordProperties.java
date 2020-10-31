package de.scaramangado.lily.discord.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties("lily.discord")
@Component
@Getter
@Setter
public class DiscordProperties {

  private boolean enabled = false;
  private boolean enableDirectMessages = false;
  private String  token;
}

package de.scaramangado.lily.core.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties("lily")
public class LilyProperties {

  private String commandPrefix = "!";
}

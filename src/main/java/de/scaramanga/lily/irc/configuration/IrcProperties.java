package de.scaramanga.lily.irc.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

import static de.scaramanga.lily.irc.configuration.ReconnectStrategy.*;

@ConfigurationProperties("lily.irc")
@Component
@Getter
@Setter
public class IrcProperties {

  private boolean           enabled               = false;
  private String            host                  = null;
  private Integer           port                  = 6667;
  private String            username              = null;
  private String            password              = null;
  private List<String>      channels              = null;
  private Integer           pingTimeout           = 30;
  private Integer           triesToReconnect      = 10;
  private Integer           timeBetweenReconnects = 10;
  private ReconnectStrategy reconnectStrategy     = TRY_FOREVER;
}

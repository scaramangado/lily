package de.scaramanga.lily.irc2.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@ConfigurationProperties("lily.irc2")
@Component
@Getter
@Setter
public class Irc2Properties {

    private boolean enabled = false;

    private String host = null;

    private Integer port = 6667;

    private String username = null;

    private String password = null;

    private List<String> channels = null;
}

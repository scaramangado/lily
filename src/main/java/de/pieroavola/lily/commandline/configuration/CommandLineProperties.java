package de.pieroavola.lily.commandline.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@ConfigurationProperties("lily.commandline")
@Component
public class CommandLineProperties {

    private boolean activate = false;
}

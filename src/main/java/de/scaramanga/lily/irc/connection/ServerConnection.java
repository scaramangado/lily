package de.scaramanga.lily.irc.connection;

import de.scaramanga.lily.core.communication.Dispatcher;
import de.scaramanga.lily.irc.configuration.IrcProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ServerConnection implements ApplicationListener<ContextRefreshedEvent> {

    private final IrcProperties properties;

    private final Dispatcher dispatcher;

    public ServerConnection(IrcProperties properties, Dispatcher dispatcher) {
        this.properties = properties;
        this.dispatcher = dispatcher;
    }


    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {

        if (!properties.isEnabled()) {
            return;
        }

        LOGGER.info("Connecting to IRC...");

        ConnectionInfo info = ConnectionInfo.builder()
                .host(properties.getHost())
                .port(properties.getPort())
                .username(properties.getUsername())
                .password(properties.getPassword())
                .channel(properties.getChannel())
                .build();

        ChannelConnection channel = new ChannelConnection(info, dispatcher);
        channel.connect();
    }
}

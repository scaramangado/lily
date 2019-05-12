package de.scaramanga.lily.irc2.configuration;

import de.scaramanga.lily.irc2.exception.IrcConnectionException;
import de.scaramanga.lily.irc2.interfaces.SocketFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.net.Socket;

@Configuration
public class Irc2Configuration {

    @Bean
    public SocketFactory getSocketFactory() {
        return this::getSocket;
    }

    private Socket getSocket(String host, Integer port) {

        Socket socket;

        try {
            socket = new Socket(host, port);
        } catch (IOException e) {
            throw new IrcConnectionException("Socket not available.");
        }

        return socket;
    }
}

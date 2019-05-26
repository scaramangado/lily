package de.scaramanga.lily.irc.configuration;

import de.scaramanga.lily.irc.exception.IrcConnectionException;
import de.scaramanga.lily.irc.interfaces.SocketFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.net.Socket;

@Configuration
public class IrcConfiguration {

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

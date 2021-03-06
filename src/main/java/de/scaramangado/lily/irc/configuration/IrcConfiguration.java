package de.scaramangado.lily.irc.configuration;

import de.scaramangado.lily.irc.connection.SocketFactory;
import de.scaramangado.lily.irc.exception.IrcConnectionException;
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

  @SuppressWarnings("squid:S4818") // Needed for IRC
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

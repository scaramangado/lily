package de.scaramanga.lily.irc.connection;

import java.net.Socket;

@FunctionalInterface
public interface SocketFactory {

  Socket getSocket(String host, Integer port);
}

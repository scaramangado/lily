package de.scaramangado.lily.irc.connection.ping;

/**
 * This interface is implemented by classes that can handle sending pings and requesting reconnects.
 */
public interface PingHandler {

  /**
   * To be called by the object handling a connection on every iteration af the loop.
   *
   * @param reconnectable
   *     The object that checked for messages.
   * @param received
   *     Boolean specifying whether a message was received in the current iteration.
   */
  void checkedForMessage(Reconnectable reconnectable, boolean received);
}

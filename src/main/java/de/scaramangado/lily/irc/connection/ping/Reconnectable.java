package de.scaramangado.lily.irc.connection.ping;

import de.scaramangado.lily.irc.await.Awaiting;

import java.util.function.Consumer;

/**
 * This interface is implemented by classes, that can reconnect to a server.
 */
public interface Reconnectable extends Awaiting {

  /**
   * Try reconnecting indefinitely.
   */
  void reconnect();

  /**
   * Try reconnecting indefinitely.
   *
   * @param onSuccess
   *     Method that is called on success.
   */
  void reconnect(Runnable onSuccess);

  /**
   * Reconnect with maximum number of tries.
   *
   * @param maxTries
   *     Maximum number of tries.
   */
  void reconnect(int maxTries);

  /**
   * Reconnect with maximum number of tries.
   *
   * @param maxTries
   *     Maximum number of tries.
   * @param success
   *     Method called on success or failure.
   */
  void reconnect(int maxTries, Consumer<Boolean> success);

  /**
   * Request that a ping is sent to the server.
   *
   * @return Random string sent with the ping.
   */
  String sendPing();
}

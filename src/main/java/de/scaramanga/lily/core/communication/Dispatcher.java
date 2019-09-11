package de.scaramanga.lily.core.communication;

import java.util.Optional;

/**
 * Interface of an object capable of forwarding commands and returning the result.
 */
public interface Dispatcher {

  /**
   * Dispatches a message and returns the result as an optional.
   *
   * @param message
   *     The content of the message.
   * @param info
   *     Additional information about the message.
   *
   * @return The answer to be displayed.
   */
  Optional<Answer> dispatch(String message, MessageInfo info);

  /**
   * Adds an interceptor, which can analyze messages before using the command algorithm.
   *
   * @param interceptor
   *     The interceptor object.
   */
  void addInterceptor(CommandInterceptor interceptor);

  /**
   * Add a broadcaster, which can publish answers without dispatching a message.
   *
   * @param broadcaster
   *     The broadcaster object.
   */
  <T extends Answer> void addBroadcaster(Broadcaster<T> broadcaster, Class<T> clazz);

  /**
   * Broadcast a message.
   *
   * @param broadcast
   *     The answer object that is broadcasted.
   * @param type
   *     The broadcast type. Only broadcasters for this type and it's superclasses will receive this message.
   */
  <T extends Answer<? extends AnswerInfo>> void broadcast(T broadcast, Class<T> type);
}

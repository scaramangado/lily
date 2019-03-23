package de.scaramanga.lily.core.communication;

import java.util.Optional;

/**
 * Interface of an object capable of forwarding commands and returning the result.
 */
public interface Dispatcher {

    Optional<Answer> dispatch(String message, MessageInfo info);

    void addInterceptor(CommandInterceptor interceptor);

    void addBroadcaster(Broadcaster<? extends Answer> broadcaster);

    <T extends Answer<? extends AnswerInfo>> void broadcast(T broadcast, Class<T> type);
}

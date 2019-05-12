package de.scaramanga.lily.core.communication;

/**
 * This interface is implemented by classes that are able to forward messages which are not answers to dispatches
 * messages.
 *
 * @param <T> The type of Answer the object can process.
 */
public interface Broadcaster<T extends Answer> {

    /**
     * Publish a broadcast.
     *
     * @param broadcast The object containing information to be broadcasted.
     */
    void broadcast(T broadcast);

    /**
     * Shutdown the broadcaster. Subsequent calls of the broadcast method may result in a RuntimeException.
     */
    void shutdown();
}

package de.scaramanga.lily.core.communication;

/**
 * Interface describing class that is able to process any message before dispatching takes place.
 */
@FunctionalInterface
public interface CommandInterceptor {

    /**
     * Processes a message.
     *
     * @param message The intercepted message.
     * @return Should dispatching continue after processing?
     */
    ContinuationStrategy process(Command message);

    enum ContinuationStrategy {
        CONTINUE, STOP
    }
}

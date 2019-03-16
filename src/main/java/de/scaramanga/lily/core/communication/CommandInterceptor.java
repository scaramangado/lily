package de.scaramanga.lily.core.communication;

/**
 * Interface describing class that is able to process any message before dispatching takes place.
 */
public interface CommandInterceptor {

    /**
     * Processes a message.
     *
     * @param message The intercepted message.
     * @return Should dispatching continue after processing?
     */
    ContinueProcessing process(Command message);

    enum ContinueProcessing {
        CONTINUE, STOP
    }
}

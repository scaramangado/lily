package de.pieroavola.lily.core.exceptions;

/**
 * The general purpose runtime exception used by Lily classes.
 */
public class LilyRuntimeException extends RuntimeException {

    /**
     * Constructor.
     *
     * @param message The human readable error message.
     */
    public LilyRuntimeException(String message) {
        super(message);
    }

    /**
     * Constructor.
     *
     * @param message The human readable error message.
     * @param cause The cause of the exception.
     */
    public LilyRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }
}

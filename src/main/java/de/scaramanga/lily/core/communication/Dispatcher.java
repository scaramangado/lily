package de.scaramanga.lily.core.communication;

import java.util.Optional;

/**
 * Interface of an object capable of forwarding commands and returning the result.
 */
public interface Dispatcher {

    Optional<Answer> dispatch(String message, MessageInfo info);
}

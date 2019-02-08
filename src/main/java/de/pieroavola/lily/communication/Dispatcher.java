package de.pieroavola.lily.communication;

import java.util.Optional;

/**
 * Interface of an object capable of forwarding commands and returning the result.
 */
public interface Dispatcher {

    Optional<String> dispatch(String command, String[] args);
}

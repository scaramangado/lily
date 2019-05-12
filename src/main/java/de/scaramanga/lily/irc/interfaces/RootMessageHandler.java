package de.scaramanga.lily.irc.interfaces;

import java.util.List;

/**
 * Interface describing a class that handles the communication with the root of the IRC server.
 */
public interface RootMessageHandler {

    /**
     * Retrieves the answers to a received root message.
     *
     * @param rootMessage the root message.
     * @return the answers as a list of strings.
     */
    List<String> answer(String rootMessage);

    /**
     * Retrieves the messages that should be sent to the IRC server upon entering.
     *
     * @return the messages as a list of strings.
     */
    List<String> joinMessages();
}

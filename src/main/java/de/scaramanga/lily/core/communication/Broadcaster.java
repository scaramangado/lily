package de.scaramanga.lily.core.communication;

public interface Broadcaster<T extends Answer> {

    void broadcast(T broadcast);

    void shutdown();
}

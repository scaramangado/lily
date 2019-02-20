package de.scaramanga.lily.irc.connection;

import de.scaramanga.lily.core.communication.Dispatcher;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
class ChannelConnection {

    private final ConnectionInfo info;

    private final Dispatcher dispatcher;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();


    ChannelConnection(ConnectionInfo info, Dispatcher dispatcher) {
        this.info = info;
        this.dispatcher = dispatcher;
    }

    void connect() {
        executor.submit(new ChannelConnectionThread(info, dispatcher, new MessageHandler()));
    }
}

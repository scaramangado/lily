package de.scaramanga.lily.irc2.connection;

import de.scaramanga.lily.irc2.connection.actions.ConnectionAction;
import de.scaramanga.lily.irc2.interfaces.MessageHandler;
import de.scaramanga.lily.irc2.interfaces.RootMessageHandler;
import de.scaramanga.lily.irc2.interfaces.SocketFactory;

import java.util.Queue;

interface ConnectionFactory {

    Connection getConnection(String host, Integer port, MessageHandler messageHandler, RootMessageHandler rootHandler,
                             SocketFactory socketFactory, Queue<ConnectionAction> actionQueue);

    static ConnectionFactory standardFactory() {
        return Connection::new;
    }
}

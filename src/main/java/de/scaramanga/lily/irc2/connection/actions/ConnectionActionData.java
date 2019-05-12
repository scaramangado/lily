package de.scaramanga.lily.irc2.connection.actions;

import java.util.Properties;

public abstract class ConnectionActionData {

    protected final Properties data = new Properties();

    public static ConnectionActionData empty() {
        return new ConnectionActionData() {};
    }
}

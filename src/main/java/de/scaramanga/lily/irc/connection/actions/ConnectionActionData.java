package de.scaramanga.lily.irc.connection.actions;

import java.util.Properties;

public abstract class ConnectionActionData {

  protected final Properties data = new Properties();

  public static ConnectionActionData empty() {

    return new ConnectionActionData() { };
  }
}

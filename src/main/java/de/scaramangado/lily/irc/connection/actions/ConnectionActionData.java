package de.scaramangado.lily.irc.connection.actions;

public interface ConnectionActionData {

  static ConnectionActionData empty() {

    return new ConnectionActionData() { };
  }
}

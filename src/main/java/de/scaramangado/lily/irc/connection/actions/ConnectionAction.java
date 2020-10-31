package de.scaramangado.lily.irc.connection.actions;

import lombok.Getter;

@Getter
public class ConnectionAction {

  private final ConnectionActionType type;
  private final ConnectionActionData data;

  public ConnectionAction(ConnectionActionType type) {

    this(type, ConnectionActionData.empty());
  }

  public ConnectionAction(ConnectionActionType type, ConnectionActionData data) {

    this.type = type;
    this.data = data;
  }

  public enum ConnectionActionType {
    JOIN,
    LEAVE,
    BROADCAST,
    DISCONNECT
  }
}

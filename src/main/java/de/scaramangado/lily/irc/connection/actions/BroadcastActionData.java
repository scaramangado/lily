package de.scaramangado.lily.irc.connection.actions;

import lombok.Getter;

@Getter
public class BroadcastActionData implements ConnectionActionData {

  private final String message;

  private BroadcastActionData(String message) {

    this.message = message;
  }

  public static BroadcastActionData withMessage(String message) {

    return new BroadcastActionData(message);
  }
}

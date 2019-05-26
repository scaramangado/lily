package de.scaramanga.lily.irc.connection.actions;

public class BroadcastActionData extends ConnectionActionData {

  private static final String MESSAGE = "message";

  private BroadcastActionData(String message) {

    data.setProperty(MESSAGE, message);
  }

  public String getMessage() {

    return data.getProperty(MESSAGE);
  }

  public static BroadcastActionData withMessage(String message) {

    return new BroadcastActionData(message);
  }
}

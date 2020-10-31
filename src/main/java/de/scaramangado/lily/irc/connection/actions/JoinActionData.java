package de.scaramangado.lily.irc.connection.actions;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JoinActionData implements ConnectionActionData {

  private String channelName;

  public static JoinActionData withChannelName(String channelName) {

    JoinActionData data = new JoinActionData();
    data.setChannelName(channelName);
    return data;
  }
}

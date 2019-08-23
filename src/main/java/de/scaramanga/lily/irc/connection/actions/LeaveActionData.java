package de.scaramanga.lily.irc.connection.actions;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LeaveActionData implements ConnectionActionData {

  private String channelName;

  public static LeaveActionData withChannelName(String channelName) {

    LeaveActionData data = new LeaveActionData();
    data.setChannelName(channelName);
    return data;
  }
}

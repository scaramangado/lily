package de.scaramanga.lily.irc.connection;

import de.scaramanga.lily.core.communication.MessageInfo;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode
public class IrcMessageInfo implements MessageInfo {

  private String nick;
  private String channel;

  static IrcMessageInfo with(String nick, String channel) {
    return new IrcMessageInfo(nick, channel);
  }
}

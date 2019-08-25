package de.scaramanga.lily.discord.connection;

import de.scaramanga.lily.core.communication.MessageInfo;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Message;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class DiscordMessageInfo implements MessageInfo {

  private Message message;

  public static DiscordMessageInfo withMessage(Message message) {
    return new DiscordMessageInfo(message);
  }
}

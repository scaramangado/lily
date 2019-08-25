package de.scaramanga.lily.discord.connection;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

@Slf4j
public class MessageListener implements DiscordEventListener<MessageReceivedEvent> {

  @Override
  public void handleEvent(MessageReceivedEvent event) {

    if (event.isFromType(ChannelType.TEXT) &&
        event.getAuthor() != event.getGuild().getSelfMember().getUser()) {
      LOGGER
          .info(String.format("#%s <%s>: %s", event.getChannel().getName(), event.getAuthor().getAsTag(),
                              event.getMessage().getContentDisplay()));
    }
  }
}

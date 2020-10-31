package de.scaramangado.lily.discord.connection;

import de.scaramangado.lily.core.communication.Answer;
import de.scaramangado.lily.core.communication.Dispatcher;
import de.scaramangado.lily.discord.configuration.DiscordProperties;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Optional;

@Slf4j
public class MessageListener implements DiscordEventListener<MessageReceivedEvent> {

  private final Dispatcher        dispatcher;
  private final DiscordProperties properties;

  public MessageListener(Dispatcher dispatcher, DiscordProperties properties) {

    this.dispatcher = dispatcher;
    this.properties = properties;
  }

  @Override
  public void handleEvent(MessageReceivedEvent event) {

    if (isTextChannelMessage(event) || isPrivateMessage(event)) {

      final String messageContent = event.getMessage().getContentDisplay();

      LOGGER.debug("Received discord message: " + messageContent);
      Optional<Answer> maybeAnswer = dispatcher
          .dispatch(messageContent, DiscordMessageInfo.withMessage(event.getMessage()));

      maybeAnswer.ifPresent(a -> sendAnswer(a, event.getMessage().getChannel()));
    }
  }

  private boolean isTextChannelMessage(MessageReceivedEvent event) {

    return
        event.isFromType(ChannelType.TEXT) && event.getAuthor() != event.getGuild().getSelfMember().getUser();
  }

  private boolean isPrivateMessage(MessageReceivedEvent event) {

    return
        properties.isEnableDirectMessages() && event.isFromType(ChannelType.PRIVATE) && !event.getAuthor().isBot();
  }

  private void sendAnswer(Answer answer, MessageChannel channel) {

    channel.sendMessage(answer.getText()).queue();
  }
}

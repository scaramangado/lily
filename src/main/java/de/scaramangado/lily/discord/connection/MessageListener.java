package de.scaramangado.lily.discord.connection;

import de.scaramangado.lily.core.communication.Answer;
import de.scaramangado.lily.core.communication.Dispatcher;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Optional;

@Slf4j
public class MessageListener implements DiscordEventListener<MessageReceivedEvent> {

  private final Dispatcher dispatcher;

  public MessageListener(Dispatcher dispatcher) {

    this.dispatcher = dispatcher;
  }

  @Override
  public void handleEvent(MessageReceivedEvent event) {

    if (event.isFromType(ChannelType.TEXT) &&
        event.getAuthor() != event.getGuild().getSelfMember().getUser()) {

      final String messageContent = event.getMessage().getContentDisplay();

      LOGGER.debug("Received discord message: " + messageContent);
      Optional<Answer> maybeAnswer = dispatcher
          .dispatch(messageContent, DiscordMessageInfo.withMessage(event.getMessage()));

      maybeAnswer.ifPresent(a -> sendAnswer(a, event.getMessage().getChannel()));
    }
  }

  private void sendAnswer(Answer answer, MessageChannel channel) {

    channel.sendMessage(answer.getText()).queue();
  }
}
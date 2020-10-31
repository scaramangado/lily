package de.scaramangado.lily.discord.connection;

import de.scaramangado.lily.core.communication.Answer;
import de.scaramangado.lily.core.communication.Dispatcher;
import de.scaramangado.lily.core.communication.MessageInfo;
import de.scaramangado.lily.discord.configuration.DiscordProperties;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class MessageListenerTest {

  private       MessageListener      listener;
  private final Dispatcher           dispatcherMock = mock(Dispatcher.class);
  private final MessageReceivedEvent eventMock      = mock(MessageReceivedEvent.class);
  private final Message              messageMock    = mock(Message.class);
  private final User                 botUser        = mock(User.class);
  private final User                 humanUser      = mock(User.class);
  private final MessageChannel       channelMock    = mock(MessageChannel.class);
  private final DiscordProperties    properties     = new DiscordProperties();

  private static final String MESSAGE = "TEST_MESSAGE";
  private static final String ANSWER  = "TEST_ANSWER";

  @BeforeEach
  void setup() {

    when(dispatcherMock.dispatch(anyString(), any(MessageInfo.class))).thenReturn(Optional.empty());

    when(messageMock.getContentDisplay()).thenReturn(MESSAGE);
    when(messageMock.getChannel()).thenReturn(channelMock);

    when(eventMock.getMessage()).thenReturn(messageMock);
    when(eventMock.isFromType(ChannelType.TEXT)).thenReturn(true);

    Guild  guildMock  = mock(Guild.class);
    Member selfMember = mock(Member.class);

    when(eventMock.getGuild()).thenReturn(guildMock);
    when(guildMock.getSelfMember()).thenReturn(selfMember);
    when(selfMember.getUser()).thenReturn(botUser);

    properties.setEnableDirectMessages(true);

    listener = new MessageListener(dispatcherMock, properties);
  }

  @Test
  void dispatchesMessages() {

    givenMessageSentBy(humanUser);

    listener.onEvent(eventMock);

    ArgumentCaptor<MessageInfo> messageInfoCaptor = ArgumentCaptor.forClass(MessageInfo.class);
    verify(dispatcherMock).dispatch(eq(MESSAGE), messageInfoCaptor.capture());

    MessageInfo messageInfo = messageInfoCaptor.getValue();

    assertThat(messageInfo).isInstanceOf(DiscordMessageInfo.class);

    DiscordMessageInfo discordMessageInfo = (DiscordMessageInfo) messageInfo;
    assertThat(discordMessageInfo.getMessage()).isEqualTo(messageMock);
  }

  @Test
  void doesNothingWhenDispatcherReturnsEmpty() {

    givenMessageSentBy(humanUser);

    listener.onEvent(eventMock);

    verify(messageMock, times(1)).getContentDisplay();
    verifyNoMoreInteractions(channelMock, messageMock);
  }

  @Test
  void doesNothingWhenMessageFromSelfUser() {

    givenMessageSentBy(botUser);

    listener.onEvent(eventMock);

    verifyNoMoreInteractions(channelMock, messageMock);
  }

  @Test
  void answersWhenDispatcherReturnsAnswer() {

    when(dispatcherMock.dispatch(eq(MESSAGE), any(MessageInfo.class)))
        .thenReturn(Optional.of(Answer.ofText(ANSWER)));

    MessageAction messageAction = mock(MessageAction.class);
    when(channelMock.sendMessage(anyString())).thenReturn(messageAction);

    givenMessageSentBy(humanUser);

    listener.onEvent(eventMock);

    verify(channelMock).sendMessage(ANSWER);
    verify(messageAction).queue();
  }

  @Test
  void dispatchesPrivateMessages() {

    givenMessageSentBy(humanUser);
    givenPrivateMessage();

    listener.onEvent(eventMock);

    ArgumentCaptor<MessageInfo> messageInfoCaptor = ArgumentCaptor.forClass(MessageInfo.class);
    verify(dispatcherMock).dispatch(eq(MESSAGE), messageInfoCaptor.capture());

    MessageInfo messageInfo = messageInfoCaptor.getValue();

    assertThat(messageInfo).isInstanceOf(DiscordMessageInfo.class);

    DiscordMessageInfo discordMessageInfo = (DiscordMessageInfo) messageInfo;
    assertThat(discordMessageInfo.getMessage()).isEqualTo(messageMock);
  }

  @Test
  void doesNothingWhenPrivateMessageFromBot() {

    givenMessageSentByBot();
    givenPrivateMessage();

    listener.onEvent(eventMock);

    verifyNoMoreInteractions(channelMock, messageMock);
  }

  @Test
  void doesNothingWhenPrivateMessagesDisabled() {

    givenMessageSentBy(humanUser);
    givenPrivateMessage();

    properties.setEnableDirectMessages(false);

    listener.onEvent(eventMock);

    verifyNoMoreInteractions(channelMock, messageMock);
  }

  private void givenMessageSentBy(User user) {

    when(messageMock.getAuthor()).thenReturn(user);
    when(eventMock.getAuthor()).thenReturn(user);
  }

  private void givenMessageSentByBot() {

    final User bot = mock(User.class);
    when(bot.isBot()).thenReturn(true);

    when(messageMock.getAuthor()).thenReturn(bot);
    when(eventMock.getAuthor()).thenReturn(bot);
  }

  private void givenPrivateMessage() {

    when(eventMock.isFromType(ChannelType.TEXT)).thenReturn(false);
    when(eventMock.isFromType(ChannelType.PRIVATE)).thenReturn(true);
  }
}

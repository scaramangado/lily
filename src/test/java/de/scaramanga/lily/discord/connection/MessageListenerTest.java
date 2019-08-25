package de.scaramanga.lily.discord.connection;

import de.scaramanga.lily.core.communication.Answer;
import de.scaramanga.lily.core.communication.Dispatcher;
import de.scaramanga.lily.core.communication.MessageInfo;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class MessageListenerTest {

  private MessageListener      listener;
  private Dispatcher           dispatcherMock = mock(Dispatcher.class);
  private MessageReceivedEvent eventMock      = mock(MessageReceivedEvent.class);
  private Message              messageMock    = mock(Message.class);
  private User                 botUser        = mock(User.class);
  private User                 humanUser      = mock(User.class);
  private MessageChannel       channelMock    = mock(MessageChannel.class);

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

    listener = new MessageListener(dispatcherMock);
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

  private void givenMessageSentBy(User user) {

    when(messageMock.getAuthor()).thenReturn(user);
    when(eventMock.getAuthor()).thenReturn(user);
  }
}
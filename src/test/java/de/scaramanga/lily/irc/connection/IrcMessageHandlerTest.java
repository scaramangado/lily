package de.scaramanga.lily.irc.connection;

import de.scaramanga.lily.core.communication.Answer;
import de.scaramanga.lily.core.communication.Dispatcher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static de.scaramanga.lily.irc.connection.MessageAnswer.AnswerType.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = { IrcMessageHandler.class, IrcMessageHandlerTest.IrcMessageHandlerTestConfiguration.class })
class IrcMessageHandlerTest {

    private IrcMessageHandler messageHandler;
    private Dispatcher dispatcher;

    private static final String PING = "PING :abc";
    private static final String PONG = "PONG :abc";
    private static final String UNKNOWN = "abc";
    private static final String DISPATCH = ":user!user@user.tld PRIVMSG #channel :abc";
    private static final String ANSWER = "PRIVMSG #channel :def";

    @Autowired
    public IrcMessageHandlerTest(IrcMessageHandler messageHandler, Dispatcher dispatcher) {
        this.messageHandler = messageHandler;
        this.dispatcher = dispatcher;
    }

    @Configuration
    public static class IrcMessageHandlerTestConfiguration {

        @Bean
        public Dispatcher dispatcher() {
            return mock(Dispatcher.class);
        }
    }

    @Test
    void pongsWhenPinged() {

        MessageAnswer pong = messageHandler.handleMessage(PING);

        assertThat(pong.getAnswerType()).isEqualTo(SEND_LINES);
        assertThat(pong.getLines()).containsExactly(PONG);
    }

    @Test
    void ignoresUnknownPattern() {

        MessageAnswer ignore = messageHandler.handleMessage(UNKNOWN);

        assertThat(ignore.getAnswerType()).isEqualTo(IGNORE);
    }

    @Test
    void dispatchesMessageAndResponds() {

        when(dispatcher.dispatch("abc", null)).thenReturn(Optional.of(Answer.ofText("def")));

        MessageAnswer answer = messageHandler.handleMessage(DISPATCH);

        assertThat(answer.getAnswerType()).isEqualTo(SEND_LINES);
        assertThat(answer.getLines()).containsExactly(ANSWER);
    }

    @Test
    void dispatchesMessageAndIgnores() {

        when(dispatcher.dispatch("abc", null))
                .thenReturn(Optional.empty());

        MessageAnswer ignore = messageHandler.handleMessage(DISPATCH);

        assertThat(ignore.getAnswerType()).isEqualTo(IGNORE);
    }
}

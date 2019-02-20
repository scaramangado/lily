package de.scaramanga.lily.irc.connection;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static de.scaramanga.lily.irc.connection.IrcAction.Type.ANSWER;
import static de.scaramanga.lily.irc.connection.IrcAction.Type.PARSE;
import static org.assertj.core.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MessageHandlerTest {

    private MessageHandler messageHandler = new MessageHandler();

    @Test
    void recognizesPing() {

        final String ping = "PING :testPing";
        final String pong = "PONG :testPing";

        final IrcAction result = messageHandler.handleMessage(ping);

        assertThat(result.getAnswer()
                .isPresent())
                .as("No answer.").isTrue();

        SoftAssertions soft = new SoftAssertions();

        soft.assertThat(result.getType())
                .as("Type is not ANSWER.")
                .isEqualTo(ANSWER);

        soft.assertThat(result.getAnswer().get())
                .as("Answer is not correct PONG command.")
                .isEqualTo(pong);

        soft.assertAll();
    }

    @Test
    void recognizesChatMessage() {

        final String message = "test";
        final String chatMessage = ":user!user@user.tld PRIVMSG #channel :" + message;

        IrcAction result = messageHandler.handleMessage(chatMessage);

        assertThat(result.getAnswer()
                .isPresent())
                .as("Nothing to parse.").isTrue();

        SoftAssertions soft = new SoftAssertions();

        soft.assertThat(result.getType())
                .as("Type is not PARSE.")
                .isEqualTo(PARSE);

        soft.assertThat(result.getAnswer().get())
                .as("String to parse is incorrect.")
                .isEqualTo(message);

        soft.assertAll();
    }
}

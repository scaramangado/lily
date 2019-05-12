package de.scaramanga.lily.core.application;

import de.scaramanga.lily.core.communication.*;
import de.scaramanga.lily.core.configuration.LilyConfiguration;
import de.scaramanga.lily.core.testmodules.ValidLilyCommands;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static de.scaramanga.lily.core.communication.CommandInterceptor.ContinuationStrategy.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = { LilyConfiguration.class, ValidLilyCommands.class })
class LilyDispatcherTest {

    private final GenericApplicationContext applicationContext;
    private final LilyConfiguration properties;
    private LilyDispatcher dispatcher;

    private static final MessageInfo testMessageInfo = new MessageInfo() {};
    private static final String INVALID_COMMAND = "invalid";

    private static final CommandInterceptor continueInterceptor = mock(CommandInterceptor.class);
    private static final CommandInterceptor stopInterceptor = mock(CommandInterceptor.class);

    private Broadcaster<Answer> answerBroadcaster;
    private Broadcaster<TestAnswer> testAnswerBroadcaster;
    private List<Answer> answerBroadcasts = new ArrayList<>();
    private List<TestAnswer> testAnswerBroadcasts = new ArrayList<>();

    @Autowired
    LilyDispatcherTest(GenericApplicationContext applicationContext, LilyConfiguration properties) {
        this.applicationContext = applicationContext;
        this.properties = properties;
        this.properties.setCommandPrefix("");
    }

    @BeforeEach
    void setup() {

        dispatcher = new LilyDispatcher(applicationContext, properties);

        when(continueInterceptor.process(any(Command.class))).thenReturn(CONTINUE);
        when(stopInterceptor.process(any(Command.class))).thenReturn(STOP);

        answerBroadcaster = new Broadcaster<>() {
            @Override
            public void broadcast(Answer broadcast) {
                answerBroadcasts.add(broadcast);
            }

            @Override
            public void shutdown() {
                //
            }
        };

        testAnswerBroadcaster = new Broadcaster<>() {
            @Override
            public void broadcast(TestAnswer broadcast) {
                testAnswerBroadcasts.add(broadcast);
            }

            @Override
            public void shutdown() {
                //
            }
        };
    }

    @Test
    void runsCorrectCommand() {

        SoftAssertions soft = new SoftAssertions();

        Optional<Answer> answer = dispatcher.dispatch(ValidLilyCommands.COMMAND_ONE, testMessageInfo);

        soft.assertThat(answer.isPresent())
                .as("No answer.")
                .isTrue();

        answer.ifPresent(s ->
                soft.assertThat(s.getText())
                .as("Wrong answer.")
                .isEqualTo(ValidLilyCommands.RESULT_ONE));

        soft.assertAll();
    }

    @Test
    void emptyOptionalForMissingCommand() {

        Optional<Answer> answer = dispatcher.dispatch(INVALID_COMMAND, testMessageInfo);

        assertThat(answer.isPresent()).as("Answer for missing command.").isFalse();
    }

    @Test
    void interceptsCommandAndContinuesProcessing() {

        dispatcher.addInterceptor(continueInterceptor);
        Optional<Answer> answer = dispatcher.dispatch(ValidLilyCommands.COMMAND_ONE, testMessageInfo);

        verify(continueInterceptor).process(any(Command.class));

        assertThat(answer.orElse(Answer.ofText("")).getText())
                .withFailMessage("Wrong answer.")
                .isEqualTo(ValidLilyCommands.RESULT_ONE);
    }

    @Test
    void interceptsCommandAndStopsProcessing() {

        dispatcher.addInterceptor(stopInterceptor);
        Optional<Answer> answer = dispatcher.dispatch(ValidLilyCommands.COMMAND_ONE, testMessageInfo);

        verify(stopInterceptor).process(any(Command.class));

        assertThat(answer.isPresent())
                .withFailMessage("Answer not empty.")
                .isFalse();
    }

    @Test
    void broadcastsToAllRegisteredBroadcasters() {

        dispatcher.addBroadcaster(answerBroadcaster);
        dispatcher.addBroadcaster(testAnswerBroadcaster);

        TestAnswer answer = new TestAnswer();

        dispatcher.broadcast(answer, TestAnswer.class);

        SoftAssertions soft = new SoftAssertions();

        soft.assertThat(answerBroadcasts).containsExactly(answer);
        soft.assertThat(testAnswerBroadcasts).containsExactly(answer);

        soft.assertAll();
    }

    @Test
    void doesNotBroadcastToBroadcastersWithSubclass() {

        dispatcher.addBroadcaster(answerBroadcaster);
        dispatcher.addBroadcaster(testAnswerBroadcaster);

        Answer answer = Answer.ofText("test");

        dispatcher.broadcast(answer, Answer.class);

        SoftAssertions soft = new SoftAssertions();

        soft.assertThat(answerBroadcasts).containsExactly(answer);
        soft.assertThat(testAnswerBroadcasts).isEmpty();

        soft.assertAll();
    }

    private class TestAnswer implements Answer {

        @Override
        public String getText() {
            return null;
        }

        @Override
        public AnswerInfo getAnswerInfo() {
            return null;
        }
    }
}

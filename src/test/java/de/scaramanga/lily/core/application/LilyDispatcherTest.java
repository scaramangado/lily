package de.scaramanga.lily.core.application;

import de.scaramanga.lily.core.communication.Answer;
import de.scaramanga.lily.core.communication.Command;
import de.scaramanga.lily.core.communication.CommandInterceptor;
import de.scaramanga.lily.core.communication.MessageInfo;
import de.scaramanga.lily.core.configuration.LilyConfiguration;
import de.scaramanga.lily.core.testmodules.ValidLilyCommands;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static de.scaramanga.lily.core.communication.CommandInterceptor.ContinueProcessing.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = LilyConfiguration.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LilyDispatcherTest {

    private final GenericApplicationContext applicationContext;
    private final LilyConfiguration properties;
    private LilyDispatcher dispatcher;

    private static final MessageInfo testMessageInfo = new MessageInfo() {};
    private static final String INVALID_COMMAND = "invalid";

    private static final CommandInterceptor continueInterceptor = mock(CommandInterceptor.class);
    private static final CommandInterceptor stopInterceptor = mock(CommandInterceptor.class);

    @Autowired
    LilyDispatcherTest(GenericApplicationContext applicationContext, LilyConfiguration properties) {
        this.applicationContext = applicationContext;
        this.properties = properties;
        this.properties.setCommandPrefix("");
    }

    @BeforeAll
    void setup() {

        applicationContext.registerBean(ValidLilyCommands.class, ValidLilyCommands::new);

        when(continueInterceptor.process(any(Command.class))).thenReturn(CONTINUE);
        when(stopInterceptor.process(any(Command.class))).thenReturn(STOP);
    }

    @BeforeEach
    void reinitialize() {
        dispatcher = new LilyDispatcher(applicationContext, properties);
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
}
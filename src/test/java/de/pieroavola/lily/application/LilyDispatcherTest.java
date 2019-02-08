package de.pieroavola.lily.application;

import de.pieroavola.lily.testmodules.ValidLilyCommands;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = { LilyDispatcher.class })
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LilyDispatcherTest {

    @Autowired private GenericApplicationContext applicationContext;
    private final LilyDispatcher dispatcher;

    private static final String[] testArgs = new String[0];
    private static final String INVALID_COMMAND = "invalid";

    LilyDispatcherTest(@Autowired GenericApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        dispatcher = applicationContext.getBean(LilyDispatcher.class);
    }

    @BeforeAll
    void setup() {
        applicationContext.registerBean(ValidLilyCommands.class, ValidLilyCommands::new);
    }

    @Test
    void runsCorrectCommand() {

        SoftAssertions soft = new SoftAssertions();

        Optional<String> answer = dispatcher.dispatch(ValidLilyCommands.COMMAND_ONE, testArgs);

        soft.assertThat(answer.isPresent())
                .as("No answer.")
                .isTrue();

        answer.ifPresent(s ->
                soft.assertThat(s)
                .as("Wrong answer.")
                .isEqualTo(ValidLilyCommands.RESULT_ONE));

        soft.assertAll();
    }

    @Test
    void emptyOptionalForMissingCommand() {

        Optional<String> answer = dispatcher.dispatch(INVALID_COMMAND, testArgs);

        assertThat(answer.isPresent()).as("Answer for missing command.").isFalse();
    }
}
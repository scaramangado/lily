package de.scaramanga.lily.core.application;

import de.scaramanga.lily.core.testmodules.DuplicateLilyCommands;
import de.scaramanga.lily.core.testmodules.InvalidLilyCommands;
import de.scaramanga.lily.core.testmodules.ValidLilyCommands;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.util.Arrays;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;

class LilyAnnotationProcessorTest {

    @Test
    void correctlyCollectsValidCommands() throws Exception {

        Map<String, Method> commands = getCommandsOfClasses(ValidLilyCommands.class);

        SoftAssertions soft = new SoftAssertions();

        soft.assertThat(commands.size())
                .as(String.format("The number of detected commands is wrong. Detected Methods: %s",
                        commands.values().stream().map(Method::getName).collect(Collectors.joining(", "))))
                .isEqualTo(3);

        Map<String, String> expectedResults = new HashMap<>();
        expectedResults.put(ValidLilyCommands.COMMAND_ONE, ValidLilyCommands.RESULT_ONE);
        expectedResults.put(ValidLilyCommands.COMMAND_TWO1, ValidLilyCommands.RESULT_TWO);
        expectedResults.put(ValidLilyCommands.COMMAND_TWO2, ValidLilyCommands.RESULT_TWO);

        for (Entry<String, String> methodCall : expectedResults.entrySet()) {

            Method method = commands.get(methodCall.getKey());
            Object invocationResult = method.invoke(new ValidLilyCommands(),
                    new Object[] { Arrays.array("") });
            String result = (String) invocationResult;

            soft.assertThat(result)
                    .as("Invocation did not yield the expected result.")
                    .isEqualTo(methodCall.getValue());
        }

        soft.assertAll();
    }

    @Test
    void doesNotBindToExistingCommand() {

        Map<String, Method> commands = getCommandsOfClasses(DuplicateLilyCommands.class);

        assertThat(commands.size())
                .as("Registered duplicate commands.")
                .isEqualTo(1);
    }

    @Test
    void doesNotBindCommandsToMethodWithInvalidSignature() {

        Map<String, Method> commands = getCommandsOfClasses(InvalidLilyCommands.class);

        assertThat(commands.size())
                .as(String.format("Bound the method(s) with invalid signature: %s",
                        commands.values().stream().map(Method::getName).collect(Collectors.joining(", "))))
                .isEqualTo(0);
    }

    @Test
    void collectsAllValidCommandsFromPackage() {

        Map<String, Method> commands = LilyAnnotationProcessor.getAllLilyCommands("de.scaramanga.lily.testmodules");

        assertThat(commands.size())
                .as("The number of collected commands is incorrect.")
                .isEqualTo(4);
    }

    private Map<String, Method> getCommandsOfClasses(Class<?>... classes) {

        return LilyAnnotationProcessor.getAllLilyCommands(Set.of(classes));
    }
}

package de.scaramangado.lily.core.application;

import de.scaramangado.lily.core.communication.Answer;
import de.scaramangado.lily.core.communication.Command;
import de.scaramangado.lily.core.testmodules.DuplicateLilyCommands;
import de.scaramangado.lily.core.testmodules.InvalidLilyCommands;
import de.scaramangado.lily.core.testmodules.ValidLilyCommands;
import de.scaramangado.lily.core.testmodules.WhitespaceLilyCommands;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class LilyAnnotationProcessorTest {

  @Test
  void correctlyCollectsValidCommands() throws Exception {

    Map<String, Method> commands = getCommandsOfClasses(ValidLilyCommands.class);

    SoftAssertions soft = new SoftAssertions();

    soft.assertThat(commands)
        .as(String.format("The number of detected commands is wrong. Detected Methods: %s",
                          commands.values().stream().map(Method::getName).collect(Collectors.joining(", "))))
        .hasSize(3);

    Map<String, String> expectedResults = new HashMap<>();
    expectedResults.put(ValidLilyCommands.COMMAND_ONE, ValidLilyCommands.RESULT_ONE);
    expectedResults.put(ValidLilyCommands.COMMAND_TWO1, ValidLilyCommands.RESULT_TWO);
    expectedResults.put(ValidLilyCommands.COMMAND_TWO2, ValidLilyCommands.RESULT_TWO);

    for (Entry<String, String> methodCall : expectedResults.entrySet()) {

      Method method           = commands.get(methodCall.getKey());
      Object invocationResult = method.invoke(new ValidLilyCommands(), Mockito.mock(Command.class));
      Answer result           = (Answer) invocationResult;

      soft.assertThat(result.getText())
          .as("Invocation did not yield the expected result.")
          .isEqualTo(methodCall.getValue());
    }

    soft.assertAll();
  }

  @Test
  void doesNotBindToExistingCommand() {

    Map<String, Method> commands = getCommandsOfClasses(DuplicateLilyCommands.class);

    assertThat(commands)
        .as("Registered duplicate commands.")
        .hasSize(1);
  }

  @Test
  void doesNotBindCommandsToMethodWithInvalidSignature() {

    Map<String, Method> commands = getCommandsOfClasses(InvalidLilyCommands.class);

    //noinspection RedundantOperationOnEmptyContainer
    assertThat(commands)
        .as(String.format("Bound the method(s) with invalid signature: %s",
                          commands.values().stream().map(Method::getName).collect(Collectors.joining(", "))))
        .isEmpty();
  }

  @Test
  void collectsAllValidCommandsFromPackage() {

    Map<String, Method> commands =
        LilyAnnotationProcessor.getAllLilyCommands("de.scaramangado.lily.core.testmodules");

    assertThat(commands)
        .as("The number of collected commands is incorrect.")
        .hasSize(4);
  }

  @Test
  void doesNotBindToCommandWithWhitespace() {

    Map<String, Method> commands = getCommandsOfClasses(WhitespaceLilyCommands.class);

    assertThat(commands)
        .as("Bound command with whitespace.")
        .isEmpty();
  }

  private Map<String, Method> getCommandsOfClasses(Class<?>... classes) {

    return LilyAnnotationProcessor.getAllLilyCommands(new HashSet<>(Arrays.asList(classes)));
  }
}

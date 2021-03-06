package de.scaramangado.lily.core.testmodules;

import de.scaramangado.lily.core.annotations.LilyCommand;
import de.scaramangado.lily.core.annotations.LilyModule;
import de.scaramangado.lily.core.communication.Answer;
import de.scaramangado.lily.core.communication.Command;

@LilyModule
public class ValidLilyCommands {

  public static final String COMMAND_ONE  = "c1";
  public static final String COMMAND_TWO1 = "c21";
  public static final String COMMAND_TWO2 = "c22";
  public static final String RESULT_ONE   = "ONE";
  public static final String RESULT_TWO   = "TWO";

  @LilyCommand(COMMAND_ONE)
  public Answer commandOne(Command command) {

    return Answer.ofText(RESULT_ONE);
  }

  @LilyCommand({ COMMAND_TWO1, COMMAND_TWO2 })
  public Answer commandTwo(Command command) {

    return Answer.ofText(RESULT_TWO);
  }
}

package de.scaramangado.lily.core.testmodules;

import de.scaramangado.lily.core.annotations.LilyCommand;
import de.scaramangado.lily.core.annotations.LilyModule;
import de.scaramangado.lily.core.communication.Answer;
import de.scaramangado.lily.core.communication.Command;

@LilyModule
public class WhitespaceLilyCommands {

  @LilyCommand("!quote 3")
  public Answer whitespaceCommand(Command command) {

    return Answer.ofText("");
  }
}

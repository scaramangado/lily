package de.scaramanga.lily.core.testmodules;

import de.scaramanga.lily.core.annotations.LilyCommand;
import de.scaramanga.lily.core.annotations.LilyModule;
import de.scaramanga.lily.core.communication.Answer;
import de.scaramanga.lily.core.communication.Command;

@LilyModule
public class WhitespaceLilyCommands {

  @LilyCommand("!quote 3")
  public Answer whitespaceCommand(Command command) {

    return Answer.ofText("");
  }
}

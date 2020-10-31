package de.scaramangado.lily.core.testmodules;

import de.scaramangado.lily.core.annotations.LilyCommand;
import de.scaramangado.lily.core.annotations.LilyModule;
import de.scaramangado.lily.core.communication.Answer;
import de.scaramangado.lily.core.communication.Command;

@LilyModule
public class InvalidLilyCommands {

  @LilyCommand("noArgument")
  public Answer noArgument() {

    return Answer.ofText("");
  }

  @LilyCommand("noStringReturned")
  public void noStringReturned(Command command) { }

  @LilyCommand("wrongTypeOfArgument")
  public Answer wrongTypeOfArgument(String[] args) { return Answer.ofText(""); }

  @LilyCommand("wrongReturnType")
  public String wrongReturnType(Command command) {

    return "";
  }
}

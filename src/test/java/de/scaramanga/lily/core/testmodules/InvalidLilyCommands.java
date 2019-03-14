package de.scaramanga.lily.core.testmodules;

import de.scaramanga.lily.core.annotations.LilyCommand;
import de.scaramanga.lily.core.annotations.LilyModule;
import de.scaramanga.lily.core.communication.Command;

@LilyModule
public class InvalidLilyCommands {

    @LilyCommand("noArgument")
    public String noArgument() {
        return "";
    }

    @LilyCommand("noStringReturned")
    public void noStringReturned(Command command) { }

    @LilyCommand("wrongTypeOfArgument")
    public String wrongTyppeOfArgument(String[] args) { return ""; }
}

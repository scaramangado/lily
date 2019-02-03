package de.pieroavola.lily.testmodules;

import de.pieroavola.lily.annotations.LilyCommand;
import de.pieroavola.lily.annotations.LilyModule;

@LilyModule
public class InvalidLilyCommands {

    @LilyCommand("noArgument")
    public String noArgument() {
        return "";
    }

    @LilyCommand("noStringReturned")
    public void noStringReturned(String[] args) { }
}

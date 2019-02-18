package de.scaramanga.lily.core.testmodules;

import de.scaramanga.lily.core.annotations.LilyCommand;
import de.scaramanga.lily.core.annotations.LilyModule;

@LilyModule
public class InvalidLilyCommands {

    @LilyCommand("noArgument")
    public String noArgument() {
        return "";
    }

    @LilyCommand("noStringReturned")
    public void noStringReturned(String[] args) { }
}

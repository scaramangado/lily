package de.scaramanga.lily.core.testmodules;

import de.scaramanga.lily.core.annotations.LilyCommand;
import de.scaramanga.lily.core.annotations.LilyModule;

@LilyModule
public class DuplicateLilyCommands {

    public static final String COMMAND = "command";

    public static final String RESULT_ONE = "ONE";
    public static final String RESULT_TWO = "TWO";

    @LilyCommand(COMMAND)
    public String first(String[] args) {
        return RESULT_ONE;
    }

    @LilyCommand(COMMAND)
    public String second(String[] args) {
        return RESULT_TWO;
    }
}

package de.scaramanga.lily.core.testmodules;

import de.scaramanga.lily.core.annotations.LilyCommand;
import de.scaramanga.lily.core.annotations.LilyModule;
import de.scaramanga.lily.core.communication.Command;

@LilyModule
public class DuplicateLilyCommands {

    public static final String COMMAND = "command";

    public static final String RESULT_ONE = "ONE";
    public static final String RESULT_TWO = "TWO";

    @LilyCommand(COMMAND)
    public String first(Command command) {
        return RESULT_ONE;
    }

    @LilyCommand(COMMAND)
    public String second(Command command) {
        return RESULT_TWO;
    }
}

package de.pieroavola.lily.core.testmodules;

import de.pieroavola.lily.core.annotations.LilyCommand;
import de.pieroavola.lily.core.annotations.LilyModule;

@LilyModule
public class ValidLilyCommands {

    public static final String COMMAND_ONE = "c1";
    public static final String COMMAND_TWO1 = "c21";
    public static final String COMMAND_TWO2 = "c22";

    public static final String RESULT_ONE = "ONE";
    public static final String RESULT_TWO = "TWO";

    @LilyCommand(COMMAND_ONE)
    public String commandOne(String[] args) {
        return RESULT_ONE;
    }

    @LilyCommand({COMMAND_TWO1, COMMAND_TWO2})
    public String commandTwo(String[] args) {
        return RESULT_TWO;
    }
}

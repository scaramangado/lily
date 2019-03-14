package de.scaramanga.lily.core.testmodules;

import de.scaramanga.lily.core.annotations.LilyCommand;
import de.scaramanga.lily.core.annotations.LilyModule;
import de.scaramanga.lily.core.communication.Answer;
import de.scaramanga.lily.core.communication.Command;

@LilyModule
public class DuplicateLilyCommands {

    public static final String COMMAND = "command";

    public static final String RESULT_ONE = "ONE";
    public static final String RESULT_TWO = "TWO";

    @LilyCommand(COMMAND)
    public Answer first(Command command) {
        return Answer.ofText(RESULT_ONE);
    }

    @LilyCommand(COMMAND)
    public Answer second(Command command) {
        return Answer.ofText(RESULT_TWO);
    }
}

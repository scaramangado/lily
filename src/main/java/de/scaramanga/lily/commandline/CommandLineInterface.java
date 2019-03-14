package de.scaramanga.lily.commandline;

import de.scaramanga.lily.core.communication.Answer;
import de.scaramanga.lily.core.communication.Dispatcher;
import de.scaramanga.lily.core.communication.MessageInfo;

import java.util.Optional;
import java.util.Scanner;

public class CommandLineInterface {

    private final Dispatcher dispatcher;

    public CommandLineInterface(Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    public void run() {

        Scanner scanner = new Scanner(System.in);
        boolean interrupted = false;

        while (!interrupted) {

            String input = scanner.nextLine();

            if (input.equals("quit")) {
                interrupted = true;
            }

            Optional<Answer> answer = dispatcher.dispatch(input, MessageInfo.empty());

            answer.map(Answer::getText).ifPresent(System.out::println);
        }
    }
}

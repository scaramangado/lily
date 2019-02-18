package de.scaramanga.lily.commandline;

import de.scaramanga.lily.core.communication.Dispatcher;

import java.util.Arrays;
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

            String[] line = input.split(" ");

            if (line.length == 0) {
                continue;
            }

            String command = line[0];
            String[] args = new String[0];

            if (line.length > 1) {
                args = Arrays.copyOfRange(line, 1, line.length);
            }

            Optional<String> answer = dispatcher.dispatch(command, args);

            answer.ifPresent(System.out::println);
        }
    }
}

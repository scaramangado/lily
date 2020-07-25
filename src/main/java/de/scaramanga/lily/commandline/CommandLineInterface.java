package de.scaramanga.lily.commandline;

import de.scaramanga.lily.commandline.util.LilyScanner;
import de.scaramanga.lily.core.communication.Answer;
import de.scaramanga.lily.core.communication.Dispatcher;
import de.scaramanga.lily.core.communication.MessageInfo;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

class CommandLineInterface {

  private final Dispatcher  dispatcher;
  private final LilyScanner scanner;
  private final PrintStream printStream;

  @SuppressWarnings("squid:S106") // CLI
  CommandLineInterface(Dispatcher dispatcher) {

    this(dispatcher, LilyScanner::new, () -> System.out);
  }

  @SuppressWarnings("squid:S4829") // Command line mode only for debug purposes
  CommandLineInterface(Dispatcher dispatcher, Function<InputStream, LilyScanner> scannerSupplier,
                       Supplier<PrintStream> printStreamSupplier) {

    this.dispatcher  = dispatcher;
    this.scanner     = scannerSupplier.apply(System.in);
    this.printStream = printStreamSupplier.get();
  }

  void run() {

    run(true);
  }

  void run(boolean keepAlive) {

    boolean interrupted = false;

    String input = scanner.nextLine();

    if (input.equals("quit")) {
      interrupted = true;
    }

    Optional<Answer> answer = dispatcher.dispatch(input, MessageInfo.empty());

    answer.map(Answer::getText).ifPresent(printStream::println);

    if (keepAlive && !interrupted) {
      run(true);
    }
  }
}

package de.scaramangado.lily.commandline;

import de.scaramangado.lily.commandline.util.LilyScanner;
import de.scaramangado.lily.core.communication.Answer;
import de.scaramangado.lily.core.communication.Dispatcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.PrintStream;
import java.util.Optional;

import static org.mockito.Mockito.*;

class CommandLineInterfaceTest {

  private CommandLineInterface commandLineInterface;
  private Dispatcher           dispatcherMock;
  private LilyScanner          scannerMock;
  private PrintStream          printStreamMock;

  @BeforeEach
  void setup() {

    dispatcherMock  = mock(Dispatcher.class);
    scannerMock     = mock(LilyScanner.class);
    printStreamMock = mock(PrintStream.class);

    commandLineInterface = new CommandLineInterface(dispatcherMock, i -> scannerMock, () -> printStreamMock);
  }

  @Test
  void runsUntilQuit() {

    when(scannerMock.nextLine()).thenReturn("test").thenReturn("test").thenReturn("quit");
    when(dispatcherMock.dispatch(any(), any())).thenReturn(Optional.empty());
    commandLineInterface.run();

    verify(scannerMock, times(3)).nextLine();
  }

  @Test
  void printsAnswer() {

    String message = "test";
    String answer  = "answer";

    when(scannerMock.nextLine()).thenReturn(message);
    when(dispatcherMock.dispatch(eq(message), any())).thenReturn(Optional.of(Answer.ofText(answer)));

    commandLineInterface.run(false);

    verify(printStreamMock).println(answer);
  }

  @Test
  void doesNotPrintEmptyAnswer() {

    String message = "test";

    when(scannerMock.nextLine()).thenReturn(message);
    when(dispatcherMock.dispatch(eq(message), any())).thenReturn(Optional.empty());

    commandLineInterface.run(false);

    verifyNoInteractions(printStreamMock);
  }
}

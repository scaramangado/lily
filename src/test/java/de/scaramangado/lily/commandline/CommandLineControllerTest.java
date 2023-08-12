package de.scaramangado.lily.commandline;

import de.scaramangado.lily.commandline.configuration.CommandLineProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.GenericApplicationContext;

import static org.mockito.Mockito.*;

class CommandLineControllerTest {

  private CommandLineController     controller;
  private GenericApplicationContext applicationContextMock;
  private CommandLineProperties     properties;
  private CommandLineInterface      commandLineInterfaceMock;

  @BeforeEach
  void setup() {

    applicationContextMock   = mock(GenericApplicationContext.class);
    properties               = new CommandLineProperties();
    commandLineInterfaceMock = mock(CommandLineInterface.class);

    properties.setEnabled(true);

    controller = new CommandLineController(applicationContextMock, properties, () -> commandLineInterfaceMock);
  }

  @Test
  void registersCommandlineOnStartupBeanWhenActivated() {

    controller.onApplicationEvent(null);

    verify(applicationContextMock).registerBean(eq(null), eq(CommandLineInterface.class),
                                                argThat(supplier -> supplier.get().equals(commandLineInterfaceMock)),
                                                any());
    verify(commandLineInterfaceMock).run();
  }

  @Test
  void doesNotRegisterWhenDeactivated() {

    properties.setEnabled(false);

    controller.onApplicationEvent(null);

    verifyNoInteractions(applicationContextMock, commandLineInterfaceMock);
  }
}

package de.scaramanga.lily.commandline;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.stereotype.Controller;

import de.scaramanga.lily.commandline.configuration.CommandLineProperties;
import de.scaramanga.lily.core.communication.Dispatcher;

import java.util.function.Supplier;

@Controller
@Slf4j
public class CommandLineController implements ApplicationListener<ContextRefreshedEvent> {

  private final GenericApplicationContext      applicationContext;
  private final CommandLineProperties          properties;
  private final Supplier<CommandLineInterface> commandLineInterfaceSupplier;

  @Autowired
  public CommandLineController(GenericApplicationContext applicationContext, CommandLineProperties properties,
                               Dispatcher dispatcher) {

    this(applicationContext, properties, () -> new CommandLineInterface(dispatcher));
  }

  CommandLineController(GenericApplicationContext applicationContext, CommandLineProperties properties,
                        Supplier<CommandLineInterface> commandLineInterfaceSupplier) {

    this.applicationContext           = applicationContext;
    this.properties                   = properties;
    this.commandLineInterfaceSupplier = commandLineInterfaceSupplier;
  }

  @Override
  public void onApplicationEvent(ContextRefreshedEvent event) {

    LOGGER.info("Loaded Command Line.");

    if (properties.isEnabled()) {
      LOGGER.info("Command line active. Loading Interface.");
      CommandLineInterface commandLineInterface = commandLineInterfaceSupplier.get();
      applicationContext.registerBean(CommandLineInterface.class, () -> commandLineInterface,
                                      bean -> bean.setLazyInit(false));
      commandLineInterface.run();
    } else {
      LOGGER.info("Command line deactivated.");
    }
  }
}

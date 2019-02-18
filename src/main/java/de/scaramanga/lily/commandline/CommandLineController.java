package de.scaramanga.lily.commandline;

import de.scaramanga.lily.commandline.configuration.CommandLineProperties;
import de.scaramanga.lily.core.communication.Dispatcher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.stereotype.Controller;

@Controller
@Slf4j
public class CommandLineController implements ApplicationListener<ContextRefreshedEvent> {

    private final GenericApplicationContext applicationContext;
    private final CommandLineProperties properties;
    private final Dispatcher dispatcher;

    public CommandLineController(GenericApplicationContext applicationContext, CommandLineProperties properties,
                                 Dispatcher dispatcher) {
        this.applicationContext = applicationContext;
        this.properties = properties;
        this.dispatcher = dispatcher;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {

        LOGGER.info("Loaded Command Line.");

        if (properties.isEnabled()) {
            LOGGER.info("Command line active. Loading Interface.");
            CommandLineInterface commandLineInterface = new CommandLineInterface(dispatcher);
            applicationContext.registerBean(CommandLineInterface.class, () -> commandLineInterface);
            commandLineInterface.run();
        } else {
            LOGGER.info("Command line deactivated.");
        }
    }
}

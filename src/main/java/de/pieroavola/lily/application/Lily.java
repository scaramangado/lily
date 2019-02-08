package de.pieroavola.lily.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Class used to bootstrap a Spring Boot based Lily application.
 */
public class Lily {

    private static final Logger LOGGER = LoggerFactory.getLogger(Lily.class);

    private Lily() { }

    /**
     * Starts the Spring Boot based Lily application.
     *
     * @param primarySource the entry point of the application.
     * @param args the command line arguments.
     * @return the Spring Boot application context.
     */
    @SuppressWarnings("UnusedReturnValue") // Public method of framework
    public static ConfigurableApplicationContext run(Class<?> primarySource, String... args) {

        final SpringApplication application = new SpringApplication(primarySource);
        application.setBanner(Lily::printBanner);

        return application.run(args);
    }

    @SuppressWarnings({"squid:S1174", "unused"}) // Used as lambda.
    private static void printBanner(Environment environment, Class<?> sourceClass, PrintStream out) {

        try {
            final URL bannerUrl = Lily.class.getClassLoader().getResource("default_banner.txt");

            if (bannerUrl == null) {
                throw new NullPointerException("Cannot load resource.");
            }

            final String banner = new String(Files
                    .readAllBytes(Paths.get(bannerUrl.toURI())));

            out.println(banner);
        } catch (URISyntaxException | IOException e) {
            LOGGER.error("Could not load default banner from text file.", e);
        }
    }
}

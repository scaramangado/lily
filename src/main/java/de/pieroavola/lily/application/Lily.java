package de.pieroavola.lily.application;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Class used to bootstrap a Spring Boot based Lily application.
 */
public class Lily {

    /**
     * Private Constructor.
     */
    private Lily() { }

    /**
     * Starts the Spring Boot based Lily application.
     *
     * @param primarySource the entry point of the application.
     * @param args the command line arguments.
     * @return the Spring Boot application context.
     */
    public static ConfigurableApplicationContext run(Class<?> primarySource, String... args) {

        return SpringApplication.run(primarySource, args);
    }
}

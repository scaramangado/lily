package de.scaramangado.lily.core.application;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

import java.io.PrintStream;

/**
 * Class used to bootstrap a Spring Boot based Lily application.
 */
@Slf4j
public class Lily {

  private Lily() {

  }

  /**
   * Starts the Spring Boot based Lily application.
   *
   * @param primarySource
   *     the entry point of the application.
   * @param args
   *     the command line arguments.
   *
   * @return the Spring Boot application context.
   */
  @SuppressWarnings("UnusedReturnValue") // Public method of framework
  public static ConfigurableApplicationContext run(Class<?> primarySource, String... args) {

    final SpringApplication application = new SpringApplication(primarySource);
    application.setBanner(Lily::printBanner);

    return application.run(args);
  }

  @SuppressWarnings({ "squid:S1174", "unused" }) // Used as lambda.
  private static void printBanner(Environment environment, Class<?> sourceClass, PrintStream out) {

    final String defaultBanner =
        " ▄            ▄▄▄▄▄▄▄▄▄▄▄  ▄       ▄         ▄\n" +
        "▐░▌          ▐░░░░░░░░░░░▌▐░▌     ▐░▌       ▐░▌\n" +
        "▐░▌           ▀▀▀▀█░█▀▀▀▀ ▐░▌     ▐░▌       ▐░▌\n" +
        "▐░▌               ▐░▌     ▐░▌     ▐░▌       ▐░▌\n" +
        "▐░▌               ▐░▌     ▐░▌     ▐░█▄▄▄▄▄▄▄█░▌\n" +
        "▐░▌               ▐░▌     ▐░▌     ▐░░░░░░░░░░░▌\n" +
        "▐░▌               ▐░▌     ▐░▌      ▀▀▀▀█░█▀▀▀▀\n" +
        "▐░▌               ▐░▌     ▐░▌          ▐░▌\n" +
        "▐░█▄▄▄▄▄▄▄▄▄  ▄▄▄▄█░█▄▄▄▄ ▐░█▄▄▄▄▄▄▄▄▄ ▐░▌\n" +
        "▐░░░░░░░░░░░▌▐░░░░░░░░░░░▌▐░░░░░░░░░░░▌▐░▌\n" +
        " ▀▀▀▀▀▀▀▀▀▀▀  ▀▀▀▀▀▀▀▀▀▀▀  ▀▀▀▀▀▀▀▀▀▀▀  ▀\n";

    out.println(defaultBanner);
  }
}

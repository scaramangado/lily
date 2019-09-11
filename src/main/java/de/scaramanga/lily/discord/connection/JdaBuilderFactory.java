package de.scaramanga.lily.discord.connection;

import net.dv8tion.jda.api.JDABuilder;

@FunctionalInterface
public interface JdaBuilderFactory {

  JDABuilder getBuilder(String token);

  static JdaBuilderFactory standardFactory() {

    return JDABuilder::new;
  }
}

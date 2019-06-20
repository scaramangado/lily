package de.scaramanga.lily.irc.await;

public interface Awaiting {

  AwaitMessageBuilder awaitMessage(String regex);
}

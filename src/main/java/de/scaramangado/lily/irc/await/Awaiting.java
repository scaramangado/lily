package de.scaramangado.lily.irc.await;

public interface Awaiting {

  AwaitMessageBuilder awaitMessage(String regex);
}

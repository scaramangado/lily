package de.scaramangado.lily.irc.await;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.function.Consumer;

@Getter
public
class AwaitMessage {

  private final String           regex;
  private final LocalDateTime    timeout;
  private final Runnable         fallback;
  private final Consumer<String> messageCallback;

  AwaitMessage(String regex, Consumer<String> messageCallback, LocalDateTime timeout,
               Runnable fallback) {

    this.regex           = regex;
    this.messageCallback = messageCallback;
    this.timeout         = timeout;
    this.fallback        = fallback;
  }

  public static AwaitMessage empty() {

    return new AwaitMessage("", s -> {}, null, () -> {});
  }
}

package de.scaramanga.lily.irc.await;

import lombok.Getter;

import java.time.LocalDateTime;
import java.time.temporal.TemporalAmount;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Getter
public class AwaitMessageBuilder {

  private final String                  regex;
  private final Consumer<AwaitMessage>  addMessageConsumer;
  private final Supplier<LocalDateTime> currentTimeSupplier;
  private       Runnable                fallback = () -> {};
  private       TemporalAmount          timeout  = null;

  private AwaitMessageBuilder(String regex,
                              Consumer<AwaitMessage> addMessageConsumer,
                              Supplier<LocalDateTime> currentTimeSupplier) {

    this.regex               = regex;
    this.addMessageConsumer  = addMessageConsumer;
    this.currentTimeSupplier = currentTimeSupplier;
  }

  public static AwaitMessageBuilder withRegex(String regex, Consumer<AwaitMessage> addMessageConsumer,
                                              Supplier<LocalDateTime> currentTimeSupplier) {

    return new AwaitMessageBuilder(regex, addMessageConsumer, currentTimeSupplier);
  }

  public AwaitMessageBuilder atMost(TemporalAmount amount) {

    timeout = amount;
    return this;
  }

  public AwaitMessageBuilder onTimeoutCall(Runnable fallback) {

    this.fallback = fallback;
    return this;
  }

  public void thenCall(Consumer<String> messageCallback) {

    addMessageConsumer.accept(new AwaitMessage(regex, messageCallback, fromNow(timeout), fallback));
  }

  public void thenDoNothing() {

    addMessageConsumer.accept(new AwaitMessage(regex, message -> {}, fromNow(timeout), fallback));
  }

  private LocalDateTime fromNow(TemporalAmount amount) {

    return amount != null ? currentTimeSupplier.get().plus(amount) : null;
  }
}

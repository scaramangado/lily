package de.scaramangado.lily.discord.connection;

import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.hooks.EventListener;

import javax.annotation.Nonnull;

@FunctionalInterface
public interface DiscordEventListener<T extends GenericEvent> extends EventListener {

  @Override
  @SuppressWarnings("unchecked") // ClassCastException caught
  default void onEvent(@Nonnull GenericEvent event) {

    try {
      handleEvent((T) event);
    } catch (ClassCastException e) {
      // Do nothing
    }
  }

  void handleEvent(T event);
}

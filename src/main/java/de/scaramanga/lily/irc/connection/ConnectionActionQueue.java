package de.scaramanga.lily.irc.connection;

import de.scaramanga.lily.irc.connection.actions.ConnectionAction;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Service
public class ConnectionActionQueue {

  private ConcurrentLinkedQueue<ConnectionAction> queue = new ConcurrentLinkedQueue<>();

  public ConnectionAction nextAction() {

    return returnIfNotNull(queue::poll);
  }

  public void addAction(ConnectionAction action) {

    if (!queue.add(action)) {
      throw new IllegalStateException("Could not add action.");
    }
  }

  ConnectionAction showNextAction() {

    return returnIfNotNull(queue::peek);
  }

  private ConnectionAction returnIfNotNull(Supplier<ConnectionAction> supplier) {

    ConnectionAction action = supplier.get();

    if (action == null) {
      throw new IndexOutOfBoundsException("Queue is empty.");
    }

    return action;
  }

  public void forNextAction(Consumer<ConnectionAction> performOnAction) {

    performOnAction.accept(nextAction());
  }

  public boolean hasAction() {

    return !queue.isEmpty();
  }

  public void forAllActions(Consumer<ConnectionAction> performOnAction) {

    while (hasAction()) {
      performOnAction.accept(nextAction());
    }
  }
}

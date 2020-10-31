package de.scaramangado.lily.irc.connection;

import de.scaramangado.lily.irc.connection.actions.ConnectionAction;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.mock;

class ConnectionActionQueueTest {

  private ConnectionActionQueue               actionQueue;
  private Map<String, List<ConnectionAction>> consumerMap = new HashMap<>();

  @BeforeEach
  void setup() {

    actionQueue = new ConnectionActionQueue();
  }

  @ParameterizedTest
  @EnumSource(ConnectionAction.ConnectionActionType.class)
  void returnsAddedObject(ConnectionAction.ConnectionActionType type) {

    ConnectionAction expectedAction = new ConnectionAction(type);

    actionQueue.addAction(expectedAction);
    ConnectionAction actualAction = actionQueue.nextAction();

    assertThat(actualAction).isEqualTo(actualAction);
  }

  @Test
  void removesPolledAction() {

    actionQueue.addAction(new ConnectionAction(ConnectionAction.ConnectionActionType.JOIN));
    actionQueue.nextAction();

    throwsWhenPeekingWhileEmpty();
  }

  @ParameterizedTest
  @EnumSource(ConnectionAction.ConnectionActionType.class)
  void allowsPeeking(ConnectionAction.ConnectionActionType type) {

    ConnectionAction expectedAction = new ConnectionAction(type);

    actionQueue.addAction(expectedAction);

    ConnectionAction actualAction1 = actionQueue.showNextAction();
    ConnectionAction actualAction2 = actionQueue.nextAction();

    SoftAssertions soft = new SoftAssertions();

    soft.assertThat(actualAction1).as("Peek not correct.").isEqualTo(expectedAction);
    soft.assertThat(actualAction2).as("Poll not correct.").isEqualTo(expectedAction);

    soft.assertAll();
  }

  @Test
  void firstInFirstOutOrder() {

    ConnectionAction expectedAction1 = new ConnectionAction(ConnectionAction.ConnectionActionType.JOIN);
    ConnectionAction expectedAction2 = new ConnectionAction(ConnectionAction.ConnectionActionType.LEAVE);

    actionQueue.addAction(expectedAction1);
    actionQueue.addAction(expectedAction2);

    ConnectionAction actualAction1 = actionQueue.nextAction();
    ConnectionAction actualAction2 = actionQueue.nextAction();

    SoftAssertions soft = new SoftAssertions();

    soft.assertThat(actualAction1).isEqualTo(expectedAction1);
    soft.assertThat(actualAction2).isEqualTo(expectedAction2);

    soft.assertAll();
  }

  @Test
  void throwsWhenPollingWhileEmpty() {

    assertThatThrownBy(() -> actionQueue.nextAction())
        .isExactlyInstanceOf(IndexOutOfBoundsException.class);
  }

  @Test
  void throwsWhenPeekingWhileEmpty() {

    assertThatThrownBy(() -> actionQueue.showNextAction())
        .isExactlyInstanceOf(IndexOutOfBoundsException.class);
  }

  @Test
  void returnsCorrectStateInformation() {

    boolean actualBefore = actionQueue.hasAction();

    actionQueue.addAction(mock(ConnectionAction.class));
    boolean actualFilled = actionQueue.hasAction();

    actionQueue.nextAction();
    boolean actualAfter = actionQueue.hasAction();

    SoftAssertions soft = new SoftAssertions();

    soft.assertThat(actualBefore).isFalse();
    soft.assertThat(actualFilled).isTrue();
    soft.assertThat(actualAfter).isFalse();

    soft.assertAll();
  }

  @Test
  void consumesNextAction() {

    ConnectionAction actionMock = mock(ConnectionAction.class);
    actionQueue.addAction(actionMock);

    String id = UUID.randomUUID().toString();

    actionQueue.forNextAction(consumerWithId(id));

    assertThat(consumerMap.get(id)).containsExactly(actionMock);
  }

  @Test
  void removesActionAfterConsuming() {

    ConnectionAction actionMock = mock(ConnectionAction.class);
    actionQueue.addAction(actionMock);

    actionQueue.forNextAction(a -> {});

    throwsWhenPollingWhileEmpty();
  }

  @Test
  void throwsWhenConsumingWhileEmpty() {

    assertThatThrownBy(() -> actionQueue.forNextAction(a -> {}))
        .isExactlyInstanceOf(IndexOutOfBoundsException.class);
  }

  @Test
  void consumesAllActions() {

    List<ConnectionAction> actions = new ArrayList<>();
    Arrays.stream(ConnectionAction.ConnectionActionType.values())
          .map(ConnectionAction::new)
          .forEach(combine(actions::add, actionQueue::addAction));

    String id = UUID.randomUUID().toString();

    actionQueue.forAllActions(consumerWithId(id));

    assertThat(consumerMap.get(id)).containsExactlyElementsOf(actions);
  }

  @Test
  void emptyAfterConsumingAll() {

    Arrays.stream(ConnectionAction.ConnectionActionType.values())
          .map(ConnectionAction::new)
          .forEach(actionQueue::addAction);

    actionQueue.forAllActions(a -> {});

    assertThat(actionQueue.hasAction()).isFalse();
  }

  @Test
  void doesNothingWhenConsumingAllWhileEmpty() {

    String id = UUID.randomUUID().toString();

    actionQueue.forAllActions(consumerWithId(id));

    assertThat(consumerMap.get(id)).isEmpty();
  }

  private Consumer<ConnectionAction> consumerWithId(String id) {

    List<ConnectionAction> oldValue = consumerMap.put(id, new ArrayList<>());

    if (oldValue != null) {
      throw new IllegalArgumentException("ID already in use.");
    }

    return a -> consumerMap.get(id).add(a);
  }

  private <T> Consumer<T> combine(Consumer<T> consumer1, Consumer<T> consumer2) {

    return t -> {
      consumer1.accept(t);
      consumer2.accept(t);
    };
  }
}

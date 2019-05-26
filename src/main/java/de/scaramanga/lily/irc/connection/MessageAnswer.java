package de.scaramanga.lily.irc.connection;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@Getter
public class MessageAnswer {

  private final AnswerType   answerType;
  private final List<String> lines;

  private MessageAnswer(AnswerType answerType, List<String> lines) {

    this.answerType = answerType;
    this.lines      = lines;
  }

  public static MessageAnswer sendLines(List<String> lines) {

    return new MessageAnswer(AnswerType.SEND_LINES, lines);
  }

  public static MessageAnswer sendLines(String... lines) {

    return sendLines(Arrays.asList(lines));
  }

  public static MessageAnswer ignoreAnswer() {

    return new MessageAnswer(AnswerType.IGNORE, null);
  }

  public enum AnswerType {
    SEND_LINES,
    IGNORE
  }
}

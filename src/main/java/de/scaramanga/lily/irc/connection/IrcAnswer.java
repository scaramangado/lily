package de.scaramanga.lily.irc.connection;

import de.scaramanga.lily.core.communication.Answer;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class IrcAnswer implements Answer<IrcAnswerInfo> {

  private String        text;
  private IrcAnswerInfo answerInfo;

  public static IrcAnswer with(String text, IrcAnswerInfo answerInfo) {
    return new IrcAnswer(text, answerInfo);
  }
}

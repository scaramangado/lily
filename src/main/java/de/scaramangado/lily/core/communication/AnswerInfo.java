package de.scaramangado.lily.core.communication;

public interface AnswerInfo {

  static AnswerInfo empty() {

    return new AnswerInfo() { };
  }
}

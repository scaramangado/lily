package de.scaramanga.lily.core.communication;

public interface AnswerInfo {

    static AnswerInfo empty() {
        return new AnswerInfo() { };
    }
}

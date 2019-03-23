package de.scaramanga.lily.irc.communication;

import de.scaramanga.lily.core.communication.Answer;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class IrcAnswer implements Answer<IrcAnswerInfo> {

    private final String text;
    private final IrcAnswerInfo answerInfo;

    @Override
    public String getText() {
        return text;
    }

    @Override
    public IrcAnswerInfo getAnswerInfo() {
        return answerInfo;
    }
}

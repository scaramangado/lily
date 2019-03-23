package de.scaramanga.lily.core.communication;

public interface Answer<T extends AnswerInfo> {

    String getText();

    T getAnswerInfo();

    static Answer<AnswerInfo> ofText(String text) {

        return new Answer<AnswerInfo>() {
            @Override
            public String getText() {
                return text;
            }

            @Override
            public AnswerInfo getAnswerInfo() {
                return AnswerInfo.empty();
            }
        };
    }
}

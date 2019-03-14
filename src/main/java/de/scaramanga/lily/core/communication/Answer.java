package de.scaramanga.lily.core.communication;

public interface Answer {

    String getText();

    AnswerInfo getAnswerInfo();

    static Answer ofText(String text) {

        return new Answer() {
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

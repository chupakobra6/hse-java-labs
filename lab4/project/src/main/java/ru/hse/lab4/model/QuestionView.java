package ru.hse.lab4.model;

import java.util.List;

public record QuestionView(
        int id,
        String text,
        List<String> answers,
        int level
) {
    public static QuestionView from(Question question) {
        return new QuestionView(question.id(), question.text(), question.answers(), question.level());
    }
}

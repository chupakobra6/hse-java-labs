package ru.hse.lab4.model;

import java.util.List;
import java.util.Objects;

public record Question(
        int id,
        String text,
        List<String> answers,
        int correctAnswer,
        int level
) {
    public Question {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Текст вопроса не может быть пустым");
        }
        answers = List.copyOf(answers);
        if (answers.size() != 4) {
            throw new IllegalArgumentException("У вопроса должно быть 4 ответа");
        }
        for (String answer : answers) {
            if (answer == null || answer.isBlank()) {
                throw new IllegalArgumentException("Ответ не может быть пустым");
            }
        }
        if (correctAnswer < 1 || correctAnswer > 4) {
            throw new IllegalArgumentException("Номер правильного ответа должен быть от 1 до 4");
        }
        if (level < 1 || level > 15) {
            throw new IllegalArgumentException("Уровень вопроса должен быть от 1 до 15");
        }
    }

    public boolean isCorrect(int answer) {
        return correctAnswer == answer;
    }

    public String answerText(int answer) {
        if (answer < 1 || answer > 4) {
            throw new IllegalArgumentException("Номер ответа должен быть от 1 до 4");
        }
        return answers.get(answer - 1);
    }

    public String correctAnswerText() {
        return answerText(correctAnswer);
    }

    public static Question fromTsv(String line) {
        Objects.requireNonNull(line, "Строка вопроса не может быть null");
        String[] parts = line.split("\\t");
        if (parts.length < 7) {
            throw new IllegalArgumentException("В строке вопроса должно быть минимум 7 колонок");
        }
        List<String> answers = List.of(parts[1], parts[2], parts[3], parts[4]);
        int correctAnswer = Integer.parseInt(parts[5].trim());
        int level = Integer.parseInt(parts[6].trim());
        return new Question(0, parts[0], answers, correctAnswer, level);
    }
}

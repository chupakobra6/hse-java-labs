package ru.hse.lab4.game;

import java.util.LinkedHashSet;
import java.util.Set;
import ru.hse.lab4.model.GameStatus;
import ru.hse.lab4.model.Question;

final class GameState {
    private final String id;
    private final String playerName;
    private final int safeLevel;
    private final Set<Integer> usedQuestionIds = new LinkedHashSet<>();
    private final Set<String> usedHints = new LinkedHashSet<>();
    private final Set<Integer> disabledAnswers = new LinkedHashSet<>();
    private final Set<Integer> triedAnswers = new LinkedHashSet<>();

    private int level = 1;
    private int lastPrize = 0;
    private GameStatus status = GameStatus.ACTIVE;
    private Question currentQuestion;
    private boolean doubleDipActive;
    private String message = "Игра началась";

    GameState(String id, String playerName, int safeLevel) {
        this.id = id;
        this.playerName = playerName;
        this.safeLevel = safeLevel;
    }

    String id() {
        return id;
    }

    String playerName() {
        return playerName;
    }

    int safeLevel() {
        return safeLevel;
    }

    Set<Integer> usedQuestionIds() {
        return usedQuestionIds;
    }

    Set<String> usedHints() {
        return usedHints;
    }

    Set<Integer> disabledAnswers() {
        return disabledAnswers;
    }

    Set<Integer> triedAnswers() {
        return triedAnswers;
    }

    int level() {
        return level;
    }

    void setLevel(int level) {
        this.level = level;
    }

    int lastPrize() {
        return lastPrize;
    }

    void setLastPrize(int lastPrize) {
        this.lastPrize = lastPrize;
    }

    GameStatus status() {
        return status;
    }

    void setStatus(GameStatus status) {
        this.status = status;
    }

    Question currentQuestion() {
        return currentQuestion;
    }

    void setCurrentQuestion(Question currentQuestion) {
        this.currentQuestion = currentQuestion;
        usedQuestionIds.add(currentQuestion.id());
        disabledAnswers.clear();
        triedAnswers.clear();
        doubleDipActive = false;
    }

    boolean doubleDipActive() {
        return doubleDipActive;
    }

    void activateDoubleDip() {
        doubleDipActive = true;
    }

    String message() {
        return message;
    }

    void setMessage(String message) {
        this.message = message;
    }
}

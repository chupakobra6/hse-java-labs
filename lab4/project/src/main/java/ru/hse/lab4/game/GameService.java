package ru.hse.lab4.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import ru.hse.lab4.model.RecordEntry;
import ru.hse.lab4.data.LeaderboardRepository;
import ru.hse.lab4.data.QuestionRepository;
import ru.hse.lab4.model.GameStatus;
import ru.hse.lab4.model.GameView;
import ru.hse.lab4.model.HintType;
import ru.hse.lab4.model.MoneyLadder;
import ru.hse.lab4.model.Question;
import ru.hse.lab4.model.QuestionView;

public final class GameService {
    private static final int MAX_USED_HINTS = 4;

    private final QuestionRepository questions;
    private final LeaderboardRepository leaderboard;
    private final Random random;
    private final Map<String, GameState> games = new ConcurrentHashMap<>();

    public GameService(QuestionRepository questions, LeaderboardRepository leaderboard, Random random) {
        this.questions = questions;
        this.leaderboard = leaderboard;
        this.random = random;
    }

    public GameView start(String playerName, int safeLevel) {
        String normalizedName = normalizeName(playerName);
        if (safeLevel < 0 || safeLevel > 15) {
            throw new IllegalArgumentException("Несгораемый уровень должен быть от 0 до 15");
        }
        GameState state = new GameState(UUID.randomUUID().toString(), normalizedName, safeLevel);
        state.setCurrentQuestion(questions.randomByLevel(1, state.usedQuestionIds(), random));
        games.put(state.id(), state);
        return view(state, null, null);
    }

    public GameView answer(String gameId, int answer) {
        GameState state = activeGame(gameId);
        if (answer < 1 || answer > 4) {
            throw new IllegalArgumentException("Ответ должен быть от 1 до 4");
        }
        if (state.disabledAnswers().contains(answer)) {
            throw new IllegalArgumentException("Этот вариант ответа недоступен");
        }

        Question question = state.currentQuestion();
        if (question.isCorrect(answer)) {
            int prize = MoneyLadder.amountForLevel(state.level());
            state.setLastPrize(prize);
            if (state.level() == 15) {
                finish(state, GameStatus.WON, prize, 15, "Поздравляем, вы выиграли 3 000 000!");
                return view(state, null, null);
            }
            state.setLevel(state.level() + 1);
            state.setCurrentQuestion(questions.randomByLevel(state.level(), state.usedQuestionIds(), random));
            state.setMessage("Верно. Следующий вопрос на " + MoneyLadder.amountForLevel(state.level()) + " рублей.");
            return view(state, null, null);
        }

        if (state.doubleDipActive() && state.triedAnswers().isEmpty()) {
            state.triedAnswers().add(answer);
            state.disabledAnswers().add(answer);
            state.setMessage("Первый ответ неверный. Подсказка «Право на ошибку» даёт вторую попытку.");
            return view(state, null, null);
        }

        int prize = guaranteedPrize(state);
        finish(state, GameStatus.LOST, prize, state.level() - 1, "Неверный ответ. Правильный вариант: " + question.correctAnswerText() + ".");
        return view(state, null, null);
    }

    public GameView stop(String gameId) {
        GameState state = activeGame(gameId);
        int prize = state.lastPrize();
        finish(state, GameStatus.STOPPED, prize, state.level() - 1, "Игра остановлена. Забранный выигрыш: " + prize + " рублей.");
        return view(state, null, null);
    }

    public GameView useHint(String gameId, HintType hint) {
        GameState state = activeGame(gameId);
        String key = hint.name().toLowerCase();
        if (state.usedHints().contains(key)) {
            throw new IllegalArgumentException("Эта подсказка уже использована");
        }
        if (state.usedHints().size() >= MAX_USED_HINTS) {
            throw new IllegalArgumentException("Можно использовать только четыре подсказки из пяти");
        }

        state.usedHints().add(key);
        return switch (hint) {
            case FIFTY_FIFTY -> useFiftyFifty(state);
            case AUDIENCE -> view(state, audienceVotes(state.currentQuestion()), null);
            case PHONE -> view(state, null, phoneMessage(state.currentQuestion()));
            case DOUBLE_DIP -> {
                state.activateDoubleDip();
                state.setMessage("Подсказка «Право на ошибку» активирована для текущего вопроса.");
                yield view(state, null, null);
            }
            case SWITCH -> {
                Question replacement = questions.randomByLevel(state.level(), state.usedQuestionIds(), random);
                state.setCurrentQuestion(replacement);
                state.setMessage("Вопрос заменён на другой вопрос того же уровня.");
                yield view(state, null, null);
            }
        };
    }

    public GameView view(String gameId) {
        GameState state = games.get(gameId);
        if (state == null) {
            throw new IllegalArgumentException("Игра не найдена");
        }
        return view(state, null, null);
    }

    public List<RecordEntry> topRecords() {
        return leaderboard.top10();
    }

    private GameView useFiftyFifty(GameState state) {
        List<Integer> wrong = new ArrayList<>();
        for (int answer = 1; answer <= 4; answer++) {
            if (answer != state.currentQuestion().correctAnswer()) {
                wrong.add(answer);
            }
        }
        Collections.shuffle(wrong, random);
        state.disabledAnswers().add(wrong.get(0));
        state.disabledAnswers().add(wrong.get(1));
        state.setMessage("Два неверных варианта скрыты.");
        return view(state, null, null);
    }

    private Map<Integer, Integer> audienceVotes(Question question) {
        int correct = 45 + random.nextInt(26);
        int remaining = 100 - correct;
        List<Integer> wrongAnswers = new ArrayList<>();
        for (int answer = 1; answer <= 4; answer++) {
            if (answer != question.correctAnswer()) {
                wrongAnswers.add(answer);
            }
        }
        Collections.shuffle(wrongAnswers, random);
        Map<Integer, Integer> votes = new LinkedHashMap<>();
        votes.put(question.correctAnswer(), correct);
        int first = random.nextInt(remaining + 1);
        int second = random.nextInt(remaining - first + 1);
        int third = remaining - first - second;
        votes.put(wrongAnswers.get(0), first);
        votes.put(wrongAnswers.get(1), second);
        votes.put(wrongAnswers.get(2), third);
        stateOrder(votes);
        return votes;
    }

    private void stateOrder(Map<Integer, Integer> votes) {
        Map<Integer, Integer> ordered = new LinkedHashMap<>();
        for (int answer = 1; answer <= 4; answer++) {
            ordered.put(answer, votes.getOrDefault(answer, 0));
        }
        votes.clear();
        votes.putAll(ordered);
    }

    private String phoneMessage(Question question) {
        boolean confident = random.nextInt(100) < 75;
        int suggested = confident ? question.correctAnswer() : randomWrongAnswer(question.correctAnswer());
        String prefix = confident ? "Друг уверен" : "Друг сомневается";
        return prefix + ": кажется, правильный ответ — " + suggested + ". " + question.answerText(suggested);
    }

    private int randomWrongAnswer(int correctAnswer) {
        List<Integer> wrong = new ArrayList<>();
        for (int answer = 1; answer <= 4; answer++) {
            if (answer != correctAnswer) {
                wrong.add(answer);
            }
        }
        return wrong.get(random.nextInt(wrong.size()));
    }

    private GameState activeGame(String gameId) {
        GameState state = games.get(gameId);
        if (state == null) {
            throw new IllegalArgumentException("Игра не найдена");
        }
        if (state.status() != GameStatus.ACTIVE) {
            throw new IllegalArgumentException("Игра уже завершена");
        }
        return state;
    }

    private int guaranteedPrize(GameState state) {
        int answeredLevel = state.level() - 1;
        if (state.safeLevel() > 0 && answeredLevel >= state.safeLevel()) {
            return MoneyLadder.amountForLevel(state.safeLevel());
        }
        return 0;
    }

    private void finish(GameState state, GameStatus status, int prize, int reachedLevel, String message) {
        state.setStatus(status);
        state.setLastPrize(prize);
        state.setMessage(message);
        leaderboard.save(state.playerName(), prize, Math.max(0, reachedLevel));
    }

    private GameView view(GameState state, Map<Integer, Integer> audienceVotes, String phoneMessage) {
        QuestionView question = state.status() == GameStatus.ACTIVE ? QuestionView.from(state.currentQuestion()) : null;
        return new GameView(
                state.id(),
                state.playerName(),
                state.status(),
                state.message(),
                state.level(),
                state.status() == GameStatus.ACTIVE ? MoneyLadder.amountForLevel(state.level()) : 0,
                state.safeLevel(),
                MoneyLadder.amountForLevel(state.safeLevel()),
                state.lastPrize(),
                question,
                new LinkedHashSet<>(state.usedHints()),
                state.usedHints().size(),
                new LinkedHashSet<>(state.disabledAnswers()),
                MoneyLadder.ascending(),
                leaderboard.top10(),
                audienceVotes,
                phoneMessage
        );
    }

    private String normalizeName(String playerName) {
        if (playerName == null || playerName.isBlank()) {
            return "Игрок";
        }
        String value = playerName.trim();
        return value.length() > 40 ? value.substring(0, 40) : value;
    }
}

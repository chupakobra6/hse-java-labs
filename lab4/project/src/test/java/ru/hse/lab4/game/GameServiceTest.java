package ru.hse.lab4.game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.Random;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ru.hse.lab4.data.Database;
import ru.hse.lab4.data.LeaderboardRepository;
import ru.hse.lab4.data.QuestionRepository;
import ru.hse.lab4.model.GameStatus;
import ru.hse.lab4.model.GameView;
import ru.hse.lab4.model.HintType;
import ru.hse.lab4.model.Question;

class GameServiceTest {
    @TempDir
    Path tempDir;

    private QuestionRepository questions;
    private GameService service;

    @BeforeEach
    void setUp() {
        Database database = new Database(tempDir.resolve("game.db"));
        questions = new QuestionRepository(database);
        LeaderboardRepository leaderboard = new LeaderboardRepository(database);
        questions.initialize();
        leaderboard.initialize();
        service = new GameService(questions, leaderboard, new Random(2));
    }

    @Test
    void startsGameWithFirstQuestionAndSelectedSafeLevel() {
        GameView game = service.start("  Игорь  ", 10);

        assertEquals("Игорь", game.playerName());
        assertEquals(GameStatus.ACTIVE, game.status());
        assertEquals(1, game.level());
        assertEquals(100_000, game.safeAmount());
        assertNotNull(game.question());
        assertEquals(1, game.question().level());
    }

    @Test
    void fiftyFiftyHidesExactlyTwoWrongAnswers() {
        GameView game = service.start("Игрок", 0);
        Question question = questions.findById(game.question().id());

        GameView withHint = service.useHint(game.gameId(), HintType.FIFTY_FIFTY);

        assertEquals(2, withHint.disabledAnswers().size());
        assertFalse(withHint.disabledAnswers().contains(question.correctAnswer()));
        assertEquals(1, withHint.hintsUsed());
        assertTrue(withHint.usedHints().contains("fifty_fifty"));
    }

    @Test
    void allowsOnlyFourHintsOutOfFive() {
        GameView game = service.start("Игрок", 0);

        service.useHint(game.gameId(), HintType.FIFTY_FIFTY);
        service.useHint(game.gameId(), HintType.AUDIENCE);
        service.useHint(game.gameId(), HintType.PHONE);
        GameView afterFourthHint = service.useHint(game.gameId(), HintType.DOUBLE_DIP);

        assertEquals(4, afterFourthHint.hintsUsed());
        assertThrows(IllegalArgumentException.class, () -> service.useHint(game.gameId(), HintType.SWITCH));
    }

    @Test
    void wrongAnswerReturnsSelectedGuaranteedPrize() {
        GameView game = service.start("Игрок", 5);
        for (int expectedLevel = 1; expectedLevel <= 5; expectedLevel++) {
            Question question = questions.findById(game.question().id());
            game = service.answer(game.gameId(), question.correctAnswer());
            if (expectedLevel < 5) {
                assertEquals(expectedLevel + 1, game.level());
            }
        }

        Question question = questions.findById(game.question().id());
        int wrongAnswer = question.correctAnswer() == 1 ? 2 : 1;
        GameView lost = service.answer(game.gameId(), wrongAnswer);

        assertEquals(GameStatus.LOST, lost.status());
        assertEquals(5_000, lost.currentPrize());
        assertEquals(1, lost.topRecords().size());
        assertEquals(5_000, lost.topRecords().getFirst().prize());
    }

    @Test
    void doubleDipGivesOneSecondAttemptOnCurrentQuestion() {
        GameView game = service.start("Игрок", 0);
        Question question = questions.findById(game.question().id());
        int wrongAnswer = question.correctAnswer() == 1 ? 2 : 1;

        service.useHint(game.gameId(), HintType.DOUBLE_DIP);
        GameView afterWrongAttempt = service.answer(game.gameId(), wrongAnswer);
        GameView afterCorrectAttempt = service.answer(game.gameId(), question.correctAnswer());

        assertEquals(GameStatus.ACTIVE, afterWrongAttempt.status());
        assertTrue(afterWrongAttempt.disabledAnswers().contains(wrongAnswer));
        assertEquals(2, afterCorrectAttempt.level());
        assertEquals(500, afterCorrectAttempt.currentPrize());
    }
}

package ru.hse.lab4.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Path;
import java.util.Random;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ru.hse.lab4.model.Question;

class QuestionRepositoryTest {
    @TempDir
    Path tempDir;

    @Test
    void importsBundledQuestionsIntoSqlite() {
        QuestionRepository repository = repository();

        assertEquals(1458, repository.count());
        Question question = repository.randomByLevel(5, Set.of(), new Random(1));
        assertNotNull(question.text());
        assertEquals(5, question.level());
        assertEquals(4, question.answers().size());
    }

    @Test
    void findsQuestionByIdAfterImport() {
        QuestionRepository repository = repository();
        Question random = repository.randomByLevel(1, Set.of(), new Random(3));

        Question found = repository.findById(random.id());

        assertEquals(random, found);
        assertThrows(IllegalArgumentException.class, () -> repository.findById(-1));
    }

    private QuestionRepository repository() {
        Database database = new Database(tempDir.resolve("questions.db"));
        QuestionRepository repository = new QuestionRepository(database);
        repository.initialize();
        return repository;
    }
}

package ru.hse.lab4.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import ru.hse.lab4.model.Question;

public final class QuestionRepository {
    private final Database database;

    public QuestionRepository(Database database) {
        this.database = database;
    }

    public void initialize() {
        try (Connection conn = database.connect(); Statement statement = conn.createStatement()) {
            statement.executeUpdate("""
                    create table if not exists questions (
                        id integer primary key autoincrement,
                        text text not null,
                        answer1 text not null,
                        answer2 text not null,
                        answer3 text not null,
                        answer4 text not null,
                        correct_answer integer not null,
                        level integer not null
                    )
                    """);
            if (count(conn) == 0) {
                importQuestions(conn);
            }
        } catch (SQLException | IOException ex) {
            throw new IllegalStateException("Не удалось инициализировать вопросы", ex);
        }
    }

    public int count() {
        try (Connection conn = database.connect()) {
            return count(conn);
        } catch (SQLException ex) {
            throw new IllegalStateException("Не удалось посчитать вопросы", ex);
        }
    }

    public Question randomByLevel(int level, Set<Integer> excludedIds, Random random) {
        List<Question> all = findByLevel(level);
        List<Question> candidates = all.stream()
                .filter(question -> !excludedIds.contains(question.id()))
                .toList();
        if (candidates.isEmpty()) {
            candidates = all;
        }
        if (candidates.isEmpty()) {
            throw new IllegalStateException("Нет вопросов уровня " + level);
        }
        return candidates.get(random.nextInt(candidates.size()));
    }

    public Question findById(int id) {
        try (Connection conn = database.connect();
             PreparedStatement statement = conn.prepareStatement("""
                     select id, text, answer1, answer2, answer3, answer4, correct_answer, level
                     from questions
                     where id = ?
                     """)) {
            statement.setInt(1, id);
            ResultSet rs = statement.executeQuery();
            if (!rs.next()) {
                throw new IllegalArgumentException("Вопрос не найден: " + id);
            }
            return mapQuestion(rs);
        } catch (SQLException ex) {
            throw new IllegalStateException("Не удалось получить вопрос " + id, ex);
        }
    }

    private List<Question> findByLevel(int level) {
        try (Connection conn = database.connect();
             PreparedStatement statement = conn.prepareStatement("""
                     select id, text, answer1, answer2, answer3, answer4, correct_answer, level
                     from questions
                     where level = ?
                     order by id
                     """)) {
            statement.setInt(1, level);
            ResultSet rs = statement.executeQuery();
            List<Question> result = new ArrayList<>();
            while (rs.next()) {
                result.add(mapQuestion(rs));
            }
            return result;
        } catch (SQLException ex) {
            throw new IllegalStateException("Не удалось получить вопрос уровня " + level, ex);
        }
    }

    private int count(Connection conn) throws SQLException {
        try (Statement statement = conn.createStatement();
             ResultSet rs = statement.executeQuery("select count(*) from questions")) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private void importQuestions(Connection conn) throws SQLException, IOException {
        InputStream input = QuestionRepository.class.getResourceAsStream("/questions.txt");
        if (input == null) {
            throw new IOException("Файл /questions.txt не найден в ресурсах");
        }
        String sql = """
                insert into questions(text, answer1, answer2, answer3, answer4, correct_answer, level)
                values (?, ?, ?, ?, ?, ?, ?)
                """;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));
             PreparedStatement statement = conn.prepareStatement(sql)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                Question question = Question.fromTsv(line);
                statement.setString(1, question.text());
                for (int index = 0; index < 4; index++) {
                    statement.setString(index + 2, question.answers().get(index));
                }
                statement.setInt(6, question.correctAnswer());
                statement.setInt(7, question.level());
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private Question mapQuestion(ResultSet rs) throws SQLException {
        return new Question(
                rs.getInt("id"),
                rs.getString("text"),
                List.of(
                        rs.getString("answer1"),
                        rs.getString("answer2"),
                        rs.getString("answer3"),
                        rs.getString("answer4")
                ),
                rs.getInt("correct_answer"),
                rs.getInt("level")
        );
    }
}

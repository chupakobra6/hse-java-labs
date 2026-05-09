package ru.hse.lab4;

import java.nio.file.Path;
import java.util.Random;
import ru.hse.lab4.data.Database;
import ru.hse.lab4.data.LeaderboardRepository;
import ru.hse.lab4.data.QuestionRepository;
import ru.hse.lab4.game.GameService;
import ru.hse.lab4.http.ApiServer;

public final class MillionaireApplication {
    private MillionaireApplication() {
    }

    public static void main(String[] args) throws Exception {
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
        Path dbPath = Path.of(System.getProperty("millionaire.db", "data/millionaire.db"));

        Database database = new Database(dbPath);
        QuestionRepository questions = new QuestionRepository(database);
        LeaderboardRepository leaderboard = new LeaderboardRepository(database);
        questions.initialize();
        leaderboard.initialize();

        GameService gameService = new GameService(questions, leaderboard, new Random());
        ApiServer server = new ApiServer(port, gameService);
        server.start();
        System.out.println("Millionaire web app started: http://localhost:" + port);
        System.out.println("SQLite database: " + database.path());
    }
}

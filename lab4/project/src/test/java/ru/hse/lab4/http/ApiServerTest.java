package ru.hse.lab4.http;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Random;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ru.hse.lab4.data.Database;
import ru.hse.lab4.data.LeaderboardRepository;
import ru.hse.lab4.data.QuestionRepository;
import ru.hse.lab4.game.GameService;

class ApiServerTest {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @TempDir
    Path tempDir;

    @Test
    void servesHtmlAndGameApi() throws Exception {
        int port = freePort();
        ApiServer server = createServer(port);
        server.start();
        try {
            HttpClient client = HttpClient.newHttpClient();

            HttpResponse<String> page = client.send(
                    HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/"))
                            .timeout(Duration.ofSeconds(5))
                            .GET()
                            .build(),
                    HttpResponse.BodyHandlers.ofString()
            );
            assertEquals(200, page.statusCode());
            assertTrue(page.body().contains("Кто хочет стать миллионером?"));
            assertTrue(page.body().contains("stage-card"));
            assertTrue(page.body().contains("lifeline"));

            HttpResponse<String> start = client.send(
                    HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/game/start"))
                            .timeout(Duration.ofSeconds(5))
                            .header("Content-Type", "application/json")
                            .POST(HttpRequest.BodyPublishers.ofString("{\"playerName\":\"Тест\",\"safeLevel\":5}"))
                            .build(),
                    HttpResponse.BodyHandlers.ofString()
            );
            assertEquals(200, start.statusCode());
            JsonNode game = MAPPER.readTree(start.body());
            assertEquals("ACTIVE", game.get("status").asText());
            assertEquals(1, game.get("level").asInt());
            assertTrue(game.get("question").get("answers").size() == 4);
        } finally {
            server.stop();
        }
    }

    private ApiServer createServer(int port) throws IOException {
        Database database = new Database(tempDir.resolve("api.db"));
        QuestionRepository questions = new QuestionRepository(database);
        LeaderboardRepository leaderboard = new LeaderboardRepository(database);
        questions.initialize();
        leaderboard.initialize();
        return new ApiServer(port, new GameService(questions, leaderboard, new Random(4)));
    }

    private int freePort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }
}

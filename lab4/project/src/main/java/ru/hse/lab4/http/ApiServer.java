package ru.hse.lab4.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import ru.hse.lab4.game.GameService;
import ru.hse.lab4.model.HintType;

public final class ApiServer {
    private final HttpServer server;
    private final GameService gameService;
    private final ObjectMapper mapper = new ObjectMapper();

    public ApiServer(int port, GameService gameService) throws IOException {
        this.server = HttpServer.create(new InetSocketAddress(port), 0);
        this.gameService = gameService;
        this.server.createContext("/", this::handle);
    }

    public void start() {
        server.start();
    }

    public void stop() {
        server.stop(0);
    }

    private void handle(HttpExchange exchange) throws IOException {
        try {
            String path = exchange.getRequestURI().getPath();
            if (path.equals("/") || path.equals("/index.html")) {
                sendResource(exchange, "/static/index.html", "text/html; charset=utf-8");
                return;
            }
            if (path.startsWith("/static/")) {
                sendStatic(exchange, path);
                return;
            }
            if (path.equals("/api/records") && exchange.getRequestMethod().equals("GET")) {
                sendJson(exchange, 200, gameService.topRecords());
                return;
            }
            if (path.equals("/api/game/start") && exchange.getRequestMethod().equals("POST")) {
                StartRequest request = readJson(exchange, StartRequest.class);
                sendJson(exchange, 200, gameService.start(request.playerName(), request.safeLevel()));
                return;
            }
            if (path.startsWith("/api/game/")) {
                handleGameRoute(exchange, path);
                return;
            }
            sendJson(exchange, 404, Map.of("error", "Маршрут не найден"));
        } catch (IllegalArgumentException | IllegalStateException ex) {
            sendJson(exchange, 400, Map.of("error", ex.getMessage()));
        } catch (Exception ex) {
            sendJson(exchange, 500, Map.of("error", "Внутренняя ошибка сервера: " + ex.getMessage()));
        } finally {
            exchange.close();
        }
    }

    private void handleGameRoute(HttpExchange exchange, String path) throws IOException {
        String[] parts = path.split("/");
        if (parts.length < 4) {
            sendJson(exchange, 404, Map.of("error", "Маршрут игры не найден"));
            return;
        }
        String gameId = parts[3];
        String method = exchange.getRequestMethod();
        if (parts.length == 4 && method.equals("GET")) {
            sendJson(exchange, 200, gameService.view(gameId));
            return;
        }
        if (parts.length == 5 && parts[4].equals("answer") && method.equals("POST")) {
            AnswerRequest request = readJson(exchange, AnswerRequest.class);
            sendJson(exchange, 200, gameService.answer(gameId, request.answer()));
            return;
        }
        if (parts.length == 5 && parts[4].equals("stop") && method.equals("POST")) {
            sendJson(exchange, 200, gameService.stop(gameId));
            return;
        }
        if (parts.length == 6 && parts[4].equals("hint") && method.equals("POST")) {
            sendJson(exchange, 200, gameService.useHint(gameId, HintType.fromPath(parts[5])));
            return;
        }
        sendJson(exchange, 404, Map.of("error", "Маршрут игры не найден"));
    }

    private <T> T readJson(HttpExchange exchange, Class<T> type) throws IOException {
        try (InputStream input = exchange.getRequestBody()) {
            return mapper.readValue(input, type);
        }
    }

    private void sendJson(HttpExchange exchange, int status, Object body) throws IOException {
        byte[] bytes = mapper.writeValueAsBytes(body);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream output = exchange.getResponseBody()) {
            output.write(bytes);
        }
    }

    private void sendStatic(HttpExchange exchange, String path) throws IOException {
        String resource = path.replaceFirst("^/static", "/static");
        String contentType = contentType(resource);
        sendResource(exchange, resource, contentType);
    }

    private void sendResource(HttpExchange exchange, String resource, String contentType) throws IOException {
        try (InputStream input = ApiServer.class.getResourceAsStream(resource)) {
            if (input == null) {
                sendJson(exchange, 404, Map.of("error", "Файл не найден"));
                return;
            }
            byte[] bytes = input.readAllBytes();
            exchange.getResponseHeaders().set("Content-Type", contentType);
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream output = exchange.getResponseBody()) {
                output.write(bytes);
            }
        }
    }

    private String contentType(String path) {
        if (path.endsWith(".css")) {
            return "text/css; charset=utf-8";
        }
        if (path.endsWith(".js")) {
            return "application/javascript; charset=utf-8";
        }
        if (path.endsWith(".jpg") || path.endsWith(".jpeg")) {
            return "image/jpeg";
        }
        if (path.endsWith(".png")) {
            return "image/png";
        }
        return "application/octet-stream";
    }

    public record StartRequest(String playerName, int safeLevel) {
    }

    public record AnswerRequest(int answer) {
    }
}

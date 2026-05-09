package ru.hse.lab4.data;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class Database {
    private final Path path;

    public Database(Path path) {
        this.path = path.toAbsolutePath();
    }

    public Connection connect() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
            Files.createDirectories(path.getParent());
        } catch (Exception ex) {
            throw new SQLException("Не удалось подготовить SQLite", ex);
        }
        return DriverManager.getConnection("jdbc:sqlite:" + path);
    }

    public Path path() {
        return path;
    }
}

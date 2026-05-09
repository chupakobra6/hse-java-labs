package ru.hse.lab4.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import ru.hse.lab4.model.RecordEntry;

public final class LeaderboardRepository {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final Database database;

    public LeaderboardRepository(Database database) {
        this.database = database;
    }

    public void initialize() {
        try (Connection conn = database.connect(); Statement statement = conn.createStatement()) {
            statement.executeUpdate("""
                    create table if not exists records (
                        id integer primary key autoincrement,
                        player_name text not null,
                        prize integer not null,
                        reached_level integer not null,
                        finished_at text not null
                    )
                    """);
        } catch (SQLException ex) {
            throw new IllegalStateException("Не удалось инициализировать таблицу рекордов", ex);
        }
    }

    public void save(String playerName, int prize, int reachedLevel) {
        try (Connection conn = database.connect();
             PreparedStatement statement = conn.prepareStatement("""
                     insert into records(player_name, prize, reached_level, finished_at)
                     values (?, ?, ?, ?)
                     """)) {
            statement.setString(1, playerName);
            statement.setInt(2, prize);
            statement.setInt(3, reachedLevel);
            statement.setString(4, LocalDateTime.now().format(FORMATTER));
            statement.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalStateException("Не удалось сохранить рекорд", ex);
        }
    }

    public List<RecordEntry> top10() {
        try (Connection conn = database.connect();
             PreparedStatement statement = conn.prepareStatement("""
                     select id, player_name, prize, reached_level, finished_at
                     from records
                     order by prize desc, reached_level desc, finished_at asc
                     limit 10
                     """)) {
            ResultSet rs = statement.executeQuery();
            List<RecordEntry> result = new ArrayList<>();
            while (rs.next()) {
                result.add(new RecordEntry(
                        rs.getLong("id"),
                        rs.getString("player_name"),
                        rs.getInt("prize"),
                        rs.getInt("reached_level"),
                        rs.getString("finished_at")
                ));
            }
            return result;
        } catch (SQLException ex) {
            throw new IllegalStateException("Не удалось получить TOP 10", ex);
        }
    }
}

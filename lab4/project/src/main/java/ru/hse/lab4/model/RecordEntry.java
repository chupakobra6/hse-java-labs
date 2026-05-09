package ru.hse.lab4.model;

public record RecordEntry(
        long id,
        String playerName,
        int prize,
        int reachedLevel,
        String finishedAt
) {
}

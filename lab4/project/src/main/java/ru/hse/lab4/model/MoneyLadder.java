package ru.hse.lab4.model;

import java.util.List;

public final class MoneyLadder {
    private static final List<Integer> ASCENDING = List.of(
            500,
            1_000,
            2_000,
            3_000,
            5_000,
            10_000,
            15_000,
            25_000,
            50_000,
            100_000,
            200_000,
            400_000,
            800_000,
            1_500_000,
            3_000_000
    );

    private MoneyLadder() {
    }

    public static int amountForLevel(int level) {
        if (level <= 0) {
            return 0;
        }
        if (level > ASCENDING.size()) {
            throw new IllegalArgumentException("Уровень должен быть от 1 до 15");
        }
        return ASCENDING.get(level - 1);
    }

    public static List<Integer> ascending() {
        return ASCENDING;
    }
}

package ru.hse.lab4.model;

import java.util.List;
import java.util.Map;
import java.util.Set;

public record GameView(
        String gameId,
        String playerName,
        GameStatus status,
        String message,
        int level,
        int levelAmount,
        int safeLevel,
        int safeAmount,
        int currentPrize,
        QuestionView question,
        Set<String> usedHints,
        int hintsUsed,
        Set<Integer> disabledAnswers,
        List<Integer> moneyLadder,
        List<RecordEntry> topRecords,
        Map<Integer, Integer> audienceVotes,
        String phoneMessage
) {
}

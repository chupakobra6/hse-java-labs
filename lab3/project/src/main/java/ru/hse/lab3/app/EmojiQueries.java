package ru.hse.lab3.app;

import java.util.ArrayList;
import java.util.List;
import ru.hse.lab3.emoji.AnimalEmoji;
import ru.hse.lab3.emoji.Emoji;
import ru.hse.lab3.emoji.FacialEmoji;
import ru.hse.lab3.emoji.SmilingEmoji;

public final class EmojiQueries {
    private EmojiQueries() {
    }

    public static FacialEmoji strongestFacialEmoji(Emoji[] emojis) {
        FacialEmoji strongest = null;
        for (Emoji emoji : emojis) {
            if (emoji instanceof FacialEmoji facial) {
                if (strongest == null || facial.getPower() > strongest.getPower()) {
                    strongest = facial;
                }
            }
        }
        return strongest;
    }

    public static List<String> animalTags(Emoji[] emojis) {
        List<String> tags = new ArrayList<>();
        for (Emoji emoji : emojis) {
            if (emoji instanceof AnimalEmoji animal) {
                tags.add(animal.getTag());
            }
        }
        return tags;
    }

    public static List<String> smileReasonsWithPowerAtLeast(Emoji[] emojis, int minPower) {
        List<String> reasons = new ArrayList<>();
        for (Emoji emoji : emojis) {
            if (emoji instanceof SmilingEmoji smiling && smiling.getPower() >= minPower) {
                reasons.add(smiling.getSmileReason());
            }
        }
        return reasons;
    }
}

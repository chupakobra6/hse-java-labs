package ru.hse.lab3.emoji;

import java.util.Objects;
import ru.hse.lab3.common.EmojiCategory;

public class SmilingEmoji extends FacialEmoji {
    private String smileReason;

    public SmilingEmoji() {
        this("smile", "smile", ":)", 8, "хорошее настроение");
    }

    public SmilingEmoji(String name, String tag, String expression, int power, String smileReason) {
        super(name, tag, EmojiCategory.SMILING, expression, power);
        setSmileReason(smileReason);
    }

    public String getSmileReason() {
        return smileReason;
    }

    public void setSmileReason(String smileReason) {
        this.smileReason = requireText(smileReason, "Причина улыбки");
    }

    @Override
    public String describe(boolean detailed) {
        return super.describe(detailed) + ", smileReason=" + smileReason;
    }

    @Override
    public String searchableText() {
        return (super.searchableText() + " " + smileReason).toLowerCase();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof SmilingEmoji other)) {
            return false;
        }
        return super.equals(obj) && Objects.equals(smileReason, other.smileReason);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), smileReason);
    }
}

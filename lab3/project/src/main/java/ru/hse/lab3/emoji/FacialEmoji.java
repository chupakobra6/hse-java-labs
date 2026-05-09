package ru.hse.lab3.emoji;

import java.util.Objects;
import ru.hse.lab3.common.EmojiCategory;

public class FacialEmoji extends Emoji {
    private String expression;
    private int power;

    public FacialEmoji() {
        this("face", "face", ":|", 5);
    }

    public FacialEmoji(String name, String tag, String expression, int power) {
        this(name, tag, EmojiCategory.FACIAL, expression, power);
    }

    protected FacialEmoji(String name, String tag, EmojiCategory category, String expression, int power) {
        super(name, tag, category);
        setExpression(expression);
        setPower(power);
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = requireText(expression, "Выражение лицевого эмодзи");
    }

    public int getPower() {
        return power;
    }

    public void setPower(int power) {
        if (power < 0 || power > 10) {
            throw new IllegalArgumentException("Сила эмодзи должна быть в диапазоне от 0 до 10");
        }
        this.power = power;
    }

    @Override
    public String describe(boolean detailed) {
        return super.describe(detailed) + ", expression=" + expression + ", power=" + power;
    }

    @Override
    public String searchableText() {
        return (super.searchableText() + " " + expression + " " + power).toLowerCase();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof FacialEmoji other)) {
            return false;
        }
        return super.equals(obj)
                && power == other.power
                && Objects.equals(expression, other.expression);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), expression, power);
    }
}

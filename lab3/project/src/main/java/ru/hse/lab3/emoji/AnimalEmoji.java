package ru.hse.lab3.emoji;

import java.util.Objects;
import ru.hse.lab3.common.EmojiCategory;

public class AnimalEmoji extends Emoji {
    private String animalPart;

    public AnimalEmoji() {
        this("cat", "cat", "голова");
    }

    public AnimalEmoji(String name, String tag, String animalPart) {
        super(name, tag, EmojiCategory.ANIMAL);
        setAnimalPart(animalPart);
    }

    public String getAnimalPart() {
        return animalPart;
    }

    public void setAnimalPart(String animalPart) {
        this.animalPart = requireText(animalPart, "Часть животного в эмодзи");
    }

    @Override
    public String describe(boolean detailed) {
        return super.describe(detailed) + ", animalPart=" + animalPart;
    }

    @Override
    public String searchableText() {
        return (super.searchableText() + " " + animalPart).toLowerCase();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof AnimalEmoji other)) {
            return false;
        }
        return super.equals(obj) && Objects.equals(animalPart, other.animalPart);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), animalPart);
    }
}

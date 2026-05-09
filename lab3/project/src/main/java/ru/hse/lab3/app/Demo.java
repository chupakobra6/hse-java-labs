package ru.hse.lab3.app;

import java.util.Arrays;
import java.util.List;
import ru.hse.lab3.common.Searchable;
import ru.hse.lab3.emoji.AnimalEmoji;
import ru.hse.lab3.emoji.Emoji;
import ru.hse.lab3.emoji.FacialEmoji;
import ru.hse.lab3.emoji.SmilingEmoji;

public class Demo {
    public static void main(String[] args) {
        Emoji[] emojis = buildCatalog();

        System.out.println("Каталог эмодзи:");
        for (Emoji emoji : emojis) {
            System.out.println(" - " + emoji);
        }

        System.out.println();
        System.out.println("Самое сильное лицевое эмодзи:");
        System.out.println(EmojiQueries.strongestFacialEmoji(emojis));

        System.out.println();
        System.out.println("Теги всех животных эмодзи:");
        System.out.println(EmojiQueries.animalTags(emojis));

        System.out.println();
        System.out.println("Причины улыбающихся эмодзи с силой не менее 8:");
        System.out.println(EmojiQueries.smileReasonsWithPowerAtLeast(emojis, 8));

        System.out.println();
        System.out.println("Демонстрация интерфейса Searchable:");
        Searchable[] searchableItems = emojis;
        for (Searchable item : searchableItems) {
            if (item.matches("smile")) {
                System.out.println(" - найдено по запросу smile: " + item.searchableText());
            }
        }

        System.out.println();
        System.out.println("Проверка equals:");
        Emoji first = new SmilingEmoji("радость", "joy", ":-)", 10, "удачная сдача лабораторной");
        Emoji second = new SmilingEmoji("радость", "joy", ":-)", 10, "удачная сдача лабораторной");
        System.out.println("first.equals(second) = " + first.equals(second));
    }

    public static Emoji[] buildCatalog() {
        List<Emoji> emojis = Arrays.asList(
                new Emoji("нейтральный знак", "neutral"),
                new Emoji("предупреждение", "warning"),
                new FacialEmoji("задумчивость", "thinking", ":-/", 6),
                new FacialEmoji("удивление", "surprise", ":-O", 7),
                new FacialEmoji("грусть", "sad", ":-(", 2),
                new FacialEmoji("спокойствие", "calm", ":-|", 5),
                new SmilingEmoji("радость", "joy", ":-)", 10, "удачная сдача лабораторной"),
                new SmilingEmoji("улыбка", "smile", ":)", 8, "хорошая погода"),
                new SmilingEmoji("смех", "laugh", ":-D", 9, "смешная история"),
                new SmilingEmoji("облегчение", "relief", "^_^", 8, "закрытый дедлайн"),
                new SmilingEmoji("довольство", "satisfied", "=)", 7, "получился рабочий код"),
                new SmilingEmoji("ирония", "irony", ";)", 6, "удачная шутка"),
                new AnimalEmoji("кот", "cat", "голова"),
                new AnimalEmoji("собака", "dog", "голова"),
                new AnimalEmoji("лиса", "fox", "морда"),
                new AnimalEmoji("панда", "panda", "голова"),
                new AnimalEmoji("лев", "lion", "голова"),
                new AnimalEmoji("птица", "bird", "тело"),
                new AnimalEmoji("рыба", "fish", "тело"),
                new AnimalEmoji("медведь", "bear", "голова")
        );
        return emojis.toArray(new Emoji[0]);
    }
}

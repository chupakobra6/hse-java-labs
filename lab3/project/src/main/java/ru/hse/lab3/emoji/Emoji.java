package ru.hse.lab3.emoji;

import java.time.LocalDate;
import java.util.Objects;
import ru.hse.lab3.common.EmojiCategory;
import ru.hse.lab3.common.Searchable;

public class Emoji implements Searchable {
    private String name;
    private String tag;
    private EmojiCategory category;
    private LocalDate createdAt;

    {
        createdAt = LocalDate.now();
    }

    public Emoji() {
        this("emoji", "emoji", EmojiCategory.GENERIC);
    }

    public Emoji(String name, String tag) {
        this(name, tag, EmojiCategory.GENERIC);
    }

    public Emoji(String name, String tag, EmojiCategory category) {
        setName(name);
        setTag(tag);
        setCategory(category);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = requireText(name, "Название эмодзи");
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = requireText(tag, "Тег эмодзи");
    }

    public EmojiCategory getCategory() {
        return category;
    }

    public void setCategory(EmojiCategory category) {
        this.category = Objects.requireNonNull(category, "Категория эмодзи не может быть null");
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDate createdAt) {
        this.createdAt = Objects.requireNonNull(createdAt, "Дата создания не может быть null");
    }

    public boolean matchesTag(String tag) {
        return matchesTag(tag, true);
    }

    public boolean matchesTag(String tag, boolean ignoreCase) {
        if (tag == null) {
            return false;
        }
        return ignoreCase ? this.tag.equalsIgnoreCase(tag.trim()) : this.tag.equals(tag.trim());
    }

    public String describe() {
        return describe(false);
    }

    public String describe(boolean detailed) {
        String base = "%s [tag=%s, category=%s]".formatted(name, tag, category);
        return detailed ? base + ", createdAt=" + createdAt : base;
    }

    @Override
    public String searchableText() {
        return (name + " " + tag + " " + category).toLowerCase();
    }

    @Override
    public boolean matches(String query) {
        return query != null && searchableText().contains(query.trim().toLowerCase());
    }

    @Override
    public String toString() {
        return describe(true);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Emoji other)) {
            return false;
        }
        return Objects.equals(name, other.name)
                && Objects.equals(tag, other.tag)
                && category == other.category;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, tag, category);
    }

    protected static String requireText(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " не может быть пустым");
        }
        return value.trim();
    }
}

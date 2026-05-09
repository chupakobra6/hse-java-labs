package ru.hse.lab3.common;

public interface Searchable {
    String searchableText();

    boolean matches(String query);
}

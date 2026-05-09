package ru.hse.lab4.model;

import java.util.Locale;

public enum HintType {
    FIFTY_FIFTY,
    AUDIENCE,
    PHONE,
    DOUBLE_DIP,
    SWITCH;

    public static HintType fromPath(String value) {
        return switch (value.toLowerCase(Locale.ROOT)) {
            case "fifty-fifty" -> FIFTY_FIFTY;
            case "audience" -> AUDIENCE;
            case "phone" -> PHONE;
            case "double-dip" -> DOUBLE_DIP;
            case "switch" -> SWITCH;
            default -> throw new IllegalArgumentException("Неизвестная подсказка: " + value);
        };
    }
}

package com.mindsoft.utils;

public class StringUtils {

    public static String capitalize(String str) {
        String[] words = str.split("\\s");
        StringBuilder titleCase = new StringBuilder();
        for (String word : words) {
            if (word.length() > 0) {
                titleCase.append(word.substring(0, 1).toUpperCase());
                if (word.length() > 1) {
                    titleCase.append(word.substring(1).toLowerCase());
                }
                titleCase.append(" ");
            }
        }
        return titleCase.toString().trim();
    }
}

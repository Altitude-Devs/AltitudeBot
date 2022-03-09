package com.alttd.templates;

public class Parser {
    public static String parse(String message, Template... templates) {
        for (Template template : templates) {
            message = template.apply(message);
        }
        return message;
    }
}

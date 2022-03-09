package com.alttd.templates;

public class Template {

    private final String key;
    private final String replacement;

    private Template(String key, String replacement) {
        this.key = key;
        this.replacement = replacement;
    }

    public static Template of(String key, String replacement) {
        return new Template("<" + key + ">", replacement);
    }

    protected String apply(String string) {
        return string.replaceAll(key, replacement);
    }
}

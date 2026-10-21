package com.otavi.mobmod.core;

/**
 * A text in every language the mod ships. Adding a language means adding a field here and a provider in datagen.
 */
public record Translation(String enUs, String ptBr) {
    public String get(String locale) {
        return switch (locale) {
            case "en_us" -> this.enUs;
            case "pt_br" -> this.ptBr;
            default -> throw new IllegalArgumentException("Unsupported locale: " + locale);
        };
    }
}

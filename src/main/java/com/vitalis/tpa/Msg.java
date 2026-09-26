package com.vitalis.tpa;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

/** MiniMessage + legacy colors (proven in sister plugins). */
public final class Msg {
    private static final LegacyComponentSerializer LEGACY =
            LegacyComponentSerializer.builder().character('&').hexColors().build();
    private static final MiniMessage MINI = MiniMessage.miniMessage();

    private Msg() {}

    public static Component color(String text) {
        if (text == null) return Component.empty();
        if (text.indexOf('<') >= 0 && text.indexOf('>') > 0) {
            try {
                return MINI.deserialize(legacyToMini(text));
            } catch (Exception e) {
                return LEGACY.deserialize(text.replaceAll("<[^>]*>", ""));
            }
        }
        return LEGACY.deserialize(text);
    }

    private static final java.util.Map<Character, String> CODES = new java.util.HashMap<>();

    static {
        CODES.put('0', "black");
        CODES.put('1', "dark_blue");
        CODES.put('2', "dark_green");
        CODES.put('3', "dark_aqua");
        CODES.put('4', "dark_red");
        CODES.put('5', "dark_purple");
        CODES.put('6', "gold");
        CODES.put('7', "gray");
        CODES.put('8', "dark_gray");
        CODES.put('9', "blue");
        CODES.put('a', "green");
        CODES.put('b', "aqua");
        CODES.put('c', "red");
        CODES.put('d', "light_purple");
        CODES.put('e', "yellow");
        CODES.put('f', "white");
        CODES.put('k', "obfuscated");
        CODES.put('l', "bold");
        CODES.put('m', "strikethrough");
        CODES.put('n', "underlined");
        CODES.put('o', "italic");
        CODES.put('r', "reset");
    }

    private static String legacyToMini(String text) {
        StringBuilder out = new StringBuilder(text.length() + 16);
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '&' && i + 1 < text.length()) {
                String tag = CODES.get(Character.toLowerCase(text.charAt(i + 1)));
                if (tag != null) {
                    out.append('<').append(tag).append('>');
                    i++;
                    continue;
                }
            }
            out.append(c);
        }
        return out.toString();
    }
}

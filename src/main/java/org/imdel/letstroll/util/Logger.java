package org.imdel.letstroll.util;

import org.bukkit.Bukkit;

public class Logger {
    private static final String RESET = "\u001B[0m";
    private static final String BOLD = "\u001B[1m";
    private static final String CYAN = "\u001B[36m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String RED = "\u001B[31m";
    private static final String PURPLE = "\u001B[35m";

    private static final String PREFIX = BOLD + "[" + CYAN + "LetsTroll" + RESET + BOLD + "]" + RESET + " ";

    public static void info(String message) {
        send(GREEN + "INFO" + RESET, message);
    }

    public static void warn(String message) {
        send(YELLOW + "WARN" + RESET, message);
    }

    public static void error(String message) {
        send(RED + "ERROR" + RESET, message);
    }

    public static void fancy(String message) {
        Bukkit.getConsoleSender().sendMessage(PREFIX + PURPLE + message + RESET);
    }

    public static void startup(String version, boolean isLatest) {
        String mcVersion = Bukkit.getVersion().split("\\s")[0].replaceAll("[^0-9.]", "");

        final int w = 34;
        final String b = "▓";
        String empty = PREFIX + PURPLE + b + " ".repeat(w) + b + RESET;

        Bukkit.getConsoleSender().sendMessage("");
        Bukkit.getConsoleSender().sendMessage(PREFIX + BOLD + PURPLE + b.repeat(w + 2) + RESET);
        Bukkit.getConsoleSender().sendMessage(empty);
        Bukkit.getConsoleSender().sendMessage(center("Let's Troll version" + version, w, b));
        Bukkit.getConsoleSender().sendMessage(center("by IMDelewer", w, b));
        Bukkit.getConsoleSender().sendMessage(center("Minecraft: " + mcVersion, w, b));
        Bukkit.getConsoleSender().sendMessage(empty);

        if (isLatest) {
            Bukkit.getConsoleSender().sendMessage(center( GREEN + "You're running the latest version!", w, b));
        } else {
            Bukkit.getConsoleSender().sendMessage(center( YELLOW + "A newer version is available!", w, b));
            String link = PREFIX + PURPLE + "→ https://github.com/IMDelewer/LetsTroll/releases" + RESET;
            Bukkit.getConsoleSender().sendMessage(center(link, w, b));
        }

        Bukkit.getConsoleSender().sendMessage(empty);
        Bukkit.getConsoleSender().sendMessage(PREFIX + BOLD + PURPLE + b.repeat(w + 2) + RESET);
        Bukkit.getConsoleSender().sendMessage("");
    }


    private static void send(String level, String message) {
        Bukkit.getConsoleSender().sendMessage(PREFIX + "[" + level + "] " + message);
    }

    private static String center(String text, int width, String borderChar) {
        int padding = Math.max(0, (width - text.length()) / 2);
        int rightPadding = Math.max(0, width - text.length() - padding);
        return PREFIX + PURPLE + borderChar + " ".repeat(padding) + text + " ".repeat(rightPadding) + borderChar + RESET;
    }

}

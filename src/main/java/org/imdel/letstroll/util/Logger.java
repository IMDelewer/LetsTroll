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
        Bukkit.getConsoleSender().sendMessage(PREFIX + message);
    }

    public static void startup(String version, boolean isLatest) {
        String mcVersion = Bukkit.getVersion().split("\\s")[0].replaceAll("[^0-9.]", "");

        final int w = 30;
        final String b = "▓";
        String empty = PURPLE + b + " ".repeat(w) + b + RESET;

        Bukkit.getConsoleSender().sendMessage("");
        Bukkit.getConsoleSender().sendMessage(BOLD + PURPLE + b.repeat(w + 2) + RESET);
        Bukkit.getConsoleSender().sendMessage(empty);
        Bukkit.getConsoleSender().sendMessage(center("Let's Troll v" + version, w, b));
        Bukkit.getConsoleSender().sendMessage(center("by IMDelewer", w, b));
        Bukkit.getConsoleSender().sendMessage(center("Minecraft: " + mcVersion, w, b));
        Bukkit.getConsoleSender().sendMessage(empty);

        if (isLatest) {
            Bukkit.getConsoleSender().sendMessage(center("You're running the latest version!", w, b));
        } else {
            String update = "A newer version is available!";
            Bukkit.getConsoleSender().sendMessage("https://github.com/IMDelewer/LetsTroll/releases");
        }

        Bukkit.getConsoleSender().sendMessage(empty);
        Bukkit.getConsoleSender().sendMessage(BOLD + PURPLE + b.repeat(w + 2) + RESET);
        Bukkit.getConsoleSender().sendMessage("");
    }

    private static void send(String level, String message) {
        Bukkit.getConsoleSender().sendMessage(PREFIX + "[" + level + "] " + message);
    }

    private static String center(String text, int width, String borderChar) {
        int padding = Math.max(0, (width - text.length()) / 2);
        return PURPLE + borderChar + " ".repeat(padding) + text + " ".repeat(width - text.length() - padding) + borderChar + RESET;
    }
}

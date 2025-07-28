package org.imdel.letstroll.util;

import org.bukkit.Bukkit;

public class Logger {
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_BOLD = "\u001B[1m";
    private static final String ANSI_CYAN = "\u001B[36m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_PURPLE = "\u001B[35m";

    private static final String PREFIX = ANSI_BOLD + "[" + ANSI_CYAN + "LetsTroll" + ANSI_RESET + ANSI_BOLD + "]" + ANSI_RESET + " ";

    public static void info(String message) {
        send(ANSI_GREEN + "INFO" + ANSI_RESET, message);
    }

    public static void warn(String message) {
        send(ANSI_YELLOW + "WARN" + ANSI_RESET, message);
    }

    public static void error(String message) {
        send(ANSI_RED + "ERROR" + ANSI_RESET, message);
    }

    public static void fancy(String message) {
        Bukkit.getConsoleSender().sendMessage(PREFIX + message);
    }

    public static void startup(String version) {
        String[] lines = {
                "",
                ANSI_BOLD + ANSI_PURPLE + "▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓" + ANSI_RESET,
                ANSI_PURPLE + "▓                      ▓" + ANSI_RESET,
                ANSI_PURPLE + "▓  Let's Troll v" + version + "  ▓" + ANSI_RESET,
                ANSI_PURPLE + "▓     by IMDelewer       ▓" + ANSI_RESET,
                ANSI_PURPLE + "▓                      ▓" + ANSI_RESET,
                ANSI_BOLD + ANSI_PURPLE + "▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓" + ANSI_RESET,
                ""
        };
        for (String line : lines) {
            Bukkit.getConsoleSender().sendMessage(line);
        }
    }

    private static void send(String level, String message) {
        Bukkit.getConsoleSender().sendMessage(PREFIX + "[" + level + "] " + message);
    }
}

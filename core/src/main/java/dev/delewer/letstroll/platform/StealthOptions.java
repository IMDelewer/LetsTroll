package dev.delewer.letstroll.platform;

public record StealthOptions(boolean creative,
                             boolean hideEntity,
                             boolean hideFromTab,
                             boolean silentJoinQuit,
                             boolean hideChat,
                             boolean muteChat,
                             boolean hideFromList,
                             boolean ignoreWorld,
                             boolean invulnerable,
                             boolean mobsIgnore,
                             boolean noHunger,
                             boolean noclipOnSprint) {

    public static StealthOptions full() {
        return new StealthOptions(true, true, true, true, true, true, true, true, true, true, true, true);
    }
}

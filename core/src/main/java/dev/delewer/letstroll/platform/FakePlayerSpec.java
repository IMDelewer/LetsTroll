package dev.delewer.letstroll.platform;

public record FakePlayerSpec(String skinOwner,
                             boolean copyTargetSkin,
                             double distance,
                             boolean facePlayer,
                             boolean visibleOnlyToTarget,
                             long lifetimeTicks) {
}

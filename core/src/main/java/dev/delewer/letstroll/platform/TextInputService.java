package dev.delewer.letstroll.platform;

import java.util.function.Consumer;

import net.kyori.adventure.text.Component;

public interface TextInputService {

    void request(PlayerRef viewer, Component prompt, String initial, Consumer<String> onConfirm);
}

package net.theevilreaper.tamias.game.listener;

import net.kyori.adventure.text.Component;
import net.minestom.server.event.player.PlayerChatEvent;
import net.theevilreaper.tamias.game.util.GameMessages;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public final class PlayerChatListener implements Consumer<PlayerChatEvent> {

    @Override
    public void accept(@NotNull PlayerChatEvent event) {
        event.setChatFormat(this::buildChatLayout);
    }

    private @NotNull Component buildChatLayout(@NotNull PlayerChatEvent event) {
        return GameMessages.buildChatLayout(event.getPlayer(), Component.text(event.getMessage()));
    }
}

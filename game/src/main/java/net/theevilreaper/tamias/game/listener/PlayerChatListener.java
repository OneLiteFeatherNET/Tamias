package net.theevilreaper.tamias.game.listener;

import net.kyori.adventure.text.Component;
import net.minestom.server.event.player.PlayerChatEvent;
import net.theevilreaper.tamias.game.util.GameMessages;

import java.util.function.Consumer;

public final class PlayerChatListener implements Consumer<PlayerChatEvent> {

    @Override
    public void accept(PlayerChatEvent event) {
        event.setFormattedMessage(buildChatLayout(event));
    }

    private Component buildChatLayout(PlayerChatEvent event) {
        return GameMessages.buildChatLayout(event.getPlayer(), Component.text(event.getRawMessage()));
    }
}

package net.theevilreaper.tamias.setup.listener;

import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public final class PlayerUseItemListener implements Consumer<PlayerUseItemEvent> {

    private final Tag<Byte> itemTag;
    private final Consumer<Player> openInvFunction;

    public PlayerUseItemListener(@NotNull Tag<Byte> itemTag, @NotNull Consumer<Player>  openInvFunction) {
        this.itemTag = itemTag;
        this.openInvFunction = openInvFunction;
    }

    @Override
    public void accept(@NotNull PlayerUseItemEvent event) {
        var stack = event.getItemStack();
        if (stack.hasTag(itemTag) && stack.getTag(itemTag) == (byte) 0) {
            this.openInvFunction.accept(event.getPlayer());
        }
    }
}

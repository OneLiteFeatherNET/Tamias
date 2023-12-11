package net.theevilreaper.tamias.setup.listener;

import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.tag.Tag;
import net.theevilreaper.tamias.setup.TamiasSetup;
import net.theevilreaper.tamias.setup.event.MapSavedEvent;
import net.theevilreaper.tamias.setup.state.SaveState;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public final class PlayerUseItemListener implements Consumer<PlayerUseItemEvent> {

    private final Tag<Byte> itemTag;
    private final Consumer<Player> openInvFunction;
    private final Function<@NotNull Path, @NotNull SaveState> saveFunction;

    public PlayerUseItemListener(
            @NotNull Tag<Byte> itemTag,
            @NotNull Consumer<Player> openInvFunction,
            @NotNull Function<@NotNull Path, @NotNull SaveState> saveFunction
    ) {
        this.itemTag = itemTag;
        this.openInvFunction = openInvFunction;
        this.saveFunction = saveFunction;
    }

    @Override
    public void accept(@NotNull PlayerUseItemEvent event) {
        var stack = event.getItemStack();

        if (!stack.hasTag(itemTag)) return;

        byte itemId = stack.getTag(itemTag);

        if (itemId == 0x00) {
            this.openInvFunction.accept(event.getPlayer());
            return;
        }

        var player = event.getPlayer();

        System.out.printf("Saving map %s%n", player.getTag(TamiasSetup.MAP_PATH_TAG));
        this.saveFunction.apply(Paths.get(player.getTag(TamiasSetup.MAP_PATH_TAG)));
        EventDispatcher.call(new MapSavedEvent(player));
    }
}

package net.theevilreaper.tamias.setup.listener;

import de.icevizion.aves.util.functional.PlayerConsumer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.theevilreaper.tamias.common.util.Tags;
import net.theevilreaper.tamias.setup.TamiasSetup;
import net.theevilreaper.tamias.setup.data.SetupData;
import net.theevilreaper.tamias.setup.event.MapSetupFinishEvent;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Function;

public final class PlayerUseItemListener implements Consumer<PlayerUseItemEvent> {

    private final PlayerConsumer openInvFunction;
    private final Function<Player, SetupData> saveFunction;

    public PlayerUseItemListener(
            @NotNull PlayerConsumer openInvFunction,
            @NotNull Function<Player, SetupData>  saveFunction
    ) {
        this.openInvFunction = openInvFunction;
        this.saveFunction = saveFunction;
    }

    @Override
    public void accept(@NotNull PlayerUseItemEvent event) {
        var stack = event.getItemStack();

        if (!stack.hasTag(Tags.ITEM_TAG)) return;

        byte itemId = stack.getTag(Tags.ITEM_TAG);

        Player player = event.getPlayer();
        if (itemId == 0x00) {
            this.openInvFunction.accept(player);
            return;
        }
        SetupData setupData = saveFunction.apply(player);
        if (setupData == null) return;

        if (itemId == 0x02) {
            setupData.openInventory();
            return;
        }

        EventDispatcher.call(new MapSetupFinishEvent(setupData));
    }
}

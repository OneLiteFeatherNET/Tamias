package net.theevilreaper.tamias.setup.listener.item;

import de.icevizion.aves.util.functional.PlayerConsumer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.item.ItemStack;
import net.theevilreaper.tamias.common.util.Tags;
import net.theevilreaper.tamias.setup.data.SetupData;
import net.theevilreaper.tamias.setup.event.MapSetupFinishEvent;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Function;

public final class PlayerUseItemListener implements Consumer<PlayerUseItemEvent> {

    private final PlayerConsumer invOpener;
    private final Function<Player, SetupData> saveFunction;

    public PlayerUseItemListener(@NotNull PlayerConsumer invOpener, @NotNull Function<Player, SetupData> saveFunction) {
        this.invOpener = invOpener;
        this.saveFunction = saveFunction;
    }

    @Override
    public void accept(@NotNull PlayerUseItemEvent event) {
        ItemStack stack = event.getItemStack();

        if (!stack.hasTag(Tags.ITEM_TAG)) return;

        byte itemId = stack.getTag(Tags.ITEM_TAG);

        Player player = event.getPlayer();
        if (itemId == 0x00) {
            this.invOpener.accept(player);
            return;
        }

        SetupData setupData = this.saveFunction.apply(player);
        if (setupData == null) return;

        if (itemId == 0x02) {
            setupData.openInventory();
            return;
        }

        EventDispatcher.call(new MapSetupFinishEvent(setupData));
    }
}

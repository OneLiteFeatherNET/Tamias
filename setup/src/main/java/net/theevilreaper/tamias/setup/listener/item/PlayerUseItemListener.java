package net.theevilreaper.tamias.setup.listener.item;

import net.onelitefeather.guira.event.SetupFinishEvent;
import net.theevilreaper.aves.map.BaseMap;
import net.theevilreaper.aves.util.functional.PlayerConsumer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.item.ItemStack;
import net.theevilreaper.tamias.common.util.Tags;
import net.theevilreaper.tamias.setup.TamiasSetup;
import net.theevilreaper.tamias.setup.data.InstanceSetupData;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

import static net.theevilreaper.tamias.setup.util.SetupItems.OVERVIEW_FLAG;

public final class PlayerUseItemListener implements Consumer<PlayerUseItemEvent> {

    private final PlayerConsumer invOpener;
    private final Function<UUID, Optional<InstanceSetupData<? extends BaseMap>>> saveFunction;

    public PlayerUseItemListener(
            @NotNull PlayerConsumer invOpener,
            @NotNull Function<UUID, Optional<InstanceSetupData<? extends BaseMap>>> saveFunction
    ) {
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

        if (!player.hasTag(TamiasSetup.SETUP_TAG)) return;

        Optional<InstanceSetupData<? extends BaseMap>> fetchedData = this.saveFunction.apply(player.getUuid());
        if (fetchedData.isEmpty()) return;

        InstanceSetupData<? extends BaseMap> setupData = fetchedData.get();

        if (itemId == OVERVIEW_FLAG) {
            setupData.openInventory(player);
            return;
        }
        EventDispatcher.call(new SetupFinishEvent<>(setupData));
    }
}

package net.theevilreaper.tamias.setup.inventory;

import net.theevilreaper.aves.inventory.GlobalInventoryBuilder;
import net.theevilreaper.aves.inventory.InventoryLayout;
import net.theevilreaper.aves.inventory.click.ClickHolder;
import net.theevilreaper.aves.inventory.util.LayoutCalculator;
import net.theevilreaper.aves.util.functional.PlayerConsumer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.inventory.InventoryCloseEvent;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.theevilreaper.tamias.setup.TamiasSetup;
import net.theevilreaper.tamias.setup.util.SetupItems;
import org.jetbrains.annotations.NotNull;

import static net.theevilreaper.aves.inventory.util.InventoryConstants.CANCEL_CLICK;

@SuppressWarnings("java:S3252")
public class ConfirmInventory extends GlobalInventoryBuilder {

    private static final int[] DECO_SLOTS;
    private static final int[] CONFIRM_SLOTS;
    private static final int[] CANCEL_SLOTS;

    private static final ItemStack CONFIRM_STACK;
    private static final ItemStack CANCEL_STACK;

    static {
        DECO_SLOTS = LayoutCalculator.quad(0, InventoryType.CHEST_4_ROW.getSize() - 1);
        CONFIRM_SLOTS = LayoutCalculator.from(10, 11, 19, 20);
        CANCEL_SLOTS = LayoutCalculator.from(14, 15, 23, 24);

        CONFIRM_STACK = ItemStack.builder(Material.GREEN_CONCRETE)
                .customName(Component.text("Confirm", NamedTextColor.GREEN))
                .build();

        CANCEL_STACK = ItemStack.builder(Material.RED_CONCRETE)
                .customName(Component.text("Cancel", NamedTextColor.RED))
                .build();
    }

    public ConfirmInventory(@NotNull PlayerConsumer consumer) {
        super(Component.text("Confirm"), InventoryType.CHEST_4_ROW);

        InventoryLayout layout = InventoryLayout.fromType(getType());
        layout.setItems(DECO_SLOTS, SetupItems.DECORATION, CANCEL_CLICK);

        layout.setItems(CONFIRM_SLOTS, CONFIRM_STACK, (player, i, click) -> {
            player.setTag(TamiasSetup.DELETE_TAG, true);
            EventDispatcher.call(new InventoryCloseEvent(player.getOpenInventory(), player, false));
            player.closeInventory();
            return ClickHolder.cancelClick();
        });

        layout.setItems(CANCEL_SLOTS, CANCEL_STACK, (player, i, click) -> {
            player.closeInventory();
            return ClickHolder.cancelClick();
        });

        setLayout(layout);

        setCloseFunction(closeEvent -> {
            Player player = closeEvent.getPlayer();
            if (!player.hasTag(TamiasSetup.DELETE_TAG)) return;
            consumer.accept(player);
            player.removeTag(TamiasSetup.DELETE_TAG);
        });

        this.invalidateDataLayout();
    }
}

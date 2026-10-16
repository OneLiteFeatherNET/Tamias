package net.theevilreaper.tamias.setup.inventory;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.entity.Player;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.theevilreaper.aves.inventory.PersonalInventoryBuilder;
import net.theevilreaper.aves.inventory.click.ClickHolder;
import net.theevilreaper.aves.inventory.layout.InventoryLayout;
import net.theevilreaper.aves.inventory.util.LayoutCalculator;
import net.theevilreaper.tamias.common.ground.GroundData;
import net.theevilreaper.tamias.common.map.builder.GameMapBuilder;
import net.theevilreaper.tamias.setup.util.GroundBlocks;
import net.theevilreaper.tamias.setup.util.SetupItems;
import net.theevilreaper.tamias.setup.util.SetupMessages;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;

import static net.theevilreaper.aves.inventory.util.InventoryConstants.CANCEL_CLICK;
import static net.theevilreaper.tamias.setup.util.SetupMessages.DELETE_CLICK;
import static net.theevilreaper.tamias.setup.util.SetupMessages.EDIT_CLICK;
import static net.theevilreaper.tamias.setup.util.SetupMessages.NO_SPACE_SEPARATOR;

/**
 * The {@link GroundPresetInventory} lists the ground presets of a game map.
 * A preset can be edited with a left click and removed with a right click.
 * The inventory is used during the setup process.
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 */
@SuppressWarnings("java:S3252")
public class GroundPresetInventory extends PersonalInventoryBuilder {

    /**
     * Index which is passed to the editor when a new preset should be created.
     */
    public static final int NEW_PRESET = -1;

    private static final int[] PRESET_SLOTS = LayoutCalculator.from(10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25);
    private static final int[] FILL_SLOTS = LayoutCalculator.from(28, 29, 31, 33, 34);
    private static final int BACK_SLOT = 30;
    private static final int ADD_SLOT = 32;

    /**
     * The maximum amount of presets a map can have.
     */
    public static final int MAX_PRESETS = PRESET_SLOTS.length;

    private static final ItemStack EMPTY_ITEM = ItemStack.builder(Material.PAPER)
            .customName(Component.text("No presets", NamedTextColor.YELLOW))
            .lore(
                    Component.empty(),
                    NO_SPACE_SEPARATOR.append(Component.space()).append(Component.text("The global default presets are used", NamedTextColor.GRAY)),
                    Component.empty()
            )
            .build();
    private static final ItemStack BACK_ITEM = ItemStack.builder(Material.ARROW)
            .customName(Component.text("Back", NamedTextColor.RED))
            .build();
    private static final ItemStack ADD_ITEM = ItemStack.builder(Material.LIME_DYE)
            .customName(Component.text("Add preset", NamedTextColor.GREEN))
            .build();

    /**
     * Creates a new {@link GroundPresetInventory} instance.
     *
     * @param player       the player who owns the inventory
     * @param mapBuilder   the map builder which holds the presets
     * @param openEditor   opens the editor for the given preset index or {@link #NEW_PRESET}
     * @param openParent   opens the inventory this overview was opened from
     * @param onChange     called after the presets were changed
     */
    public GroundPresetInventory(
            Player player,
            GameMapBuilder mapBuilder,
            IntConsumer openEditor,
            Runnable openParent,
            Runnable onChange
    ) {
        super(Component.text("Ground presets"), InventoryType.CHEST_5_ROW, player);
        this.setCancelEmptySlotClicks(true);
        InventoryLayout layout = InventoryLayout.fromType(getType());
        layout.setItems(LayoutCalculator.quad(0, getType().getSize() - 1), SetupItems.DECORATION, CANCEL_CLICK);
        layout.setItems(FILL_SLOTS, SetupItems.DECORATION, CANCEL_CLICK);
        layout.setItem(BACK_SLOT, BACK_ITEM, (clicker, slot, click, stack, result) -> {
            result.accept(ClickHolder.cancelClick());
            openParent.run();
        });
        layout.setItem(ADD_SLOT, ADD_ITEM, (clicker, slot, click, stack, result) -> {
            result.accept(ClickHolder.cancelClick());
            if (mapBuilder.getGroundPresets().size() >= MAX_PRESETS) {
                clicker.sendMessage(SetupMessages.GROUND_PRESET_LIMIT);
                return;
            }
            openEditor.accept(NEW_PRESET);
        });
        this.setLayout(layout);

        this.setDataLayoutFunction(dataLayout -> {
            dataLayout = dataLayout == null ? InventoryLayout.fromType(getType()) : dataLayout;
            dataLayout.setItems(PRESET_SLOTS, SetupItems.DECORATION, CANCEL_CLICK);

            List<GroundData> presets = mapBuilder.getGroundPresets();
            if (presets.isEmpty()) {
                dataLayout.setItem(PRESET_SLOTS[PRESET_SLOTS.length / 2], EMPTY_ITEM, CANCEL_CLICK);
                return dataLayout;
            }

            for (int i = 0; i < presets.size() && i < PRESET_SLOTS.length; i++) {
                int index = i;
                dataLayout.setItem(PRESET_SLOTS[i], getPresetItem(index, presets.get(i)), (clicker, slot, click, stack, result) -> {
                    result.accept(ClickHolder.cancelClick());
                    if (click instanceof Click.Left) {
                        openEditor.accept(index);
                        return;
                    }
                    if (click instanceof Click.Right) {
                        mapBuilder.removeGroundPreset(index);
                        onChange.run();
                    }
                });
            }
            return dataLayout;
        });

        this.invalidateLayout();
        this.register();
    }

    /**
     * Creates the item which represents a preset in the overview.
     *
     * @param index      the index of the preset
     * @param groundData the preset to display
     * @return the created item
     */
    private static ItemStack getPresetItem(int index, GroundData groundData) {
        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());
        lore.add(NO_SPACE_SEPARATOR.append(Component.space())
                .append(Component.text("Ground: ", NamedTextColor.GRAY))
                .append(Component.text(GroundBlocks.displayName(groundData.groundBlock()), NamedTextColor.GREEN)));
        if (groundData.hasAdditionalBlocks()) {
            List<String> names = groundData.additionalBlocks().stream().map(GroundBlocks::displayName).toList();
            lore.add(NO_SPACE_SEPARATOR.append(Component.space())
                    .append(Component.text("Additional: ", NamedTextColor.GRAY))
                    .append(Component.text(String.join(", ", names), NamedTextColor.AQUA)));
        }
        lore.add(Component.empty());
        lore.add(EDIT_CLICK);
        lore.add(DELETE_CLICK);
        lore.add(Component.empty());

        return ItemStack.builder(GroundBlocks.displayMaterial(groundData.groundBlock()))
                .customName(Component.text("Preset #" + (index + 1), NamedTextColor.DARK_GREEN))
                .lore(lore)
                .build();
    }
}

package net.theevilreaper.tamias.setup.inventory;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.entity.Player;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.instance.block.Block;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.theevilreaper.aves.inventory.PersonalInventoryBuilder;
import net.theevilreaper.aves.inventory.click.ClickHolder;
import net.theevilreaper.aves.inventory.function.InventoryClick;
import net.theevilreaper.aves.inventory.layout.InventoryLayout;
import net.theevilreaper.aves.inventory.util.LayoutCalculator;
import net.theevilreaper.tamias.common.ground.GroundData;
import net.theevilreaper.tamias.common.map.builder.GameMapBuilder;
import net.theevilreaper.tamias.setup.util.GroundBlocks;
import net.theevilreaper.tamias.setup.util.SetupItems;
import net.theevilreaper.tamias.setup.util.SetupMessages;
import net.theevilreaper.tamias.setup.util.SetupSounds;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import static net.theevilreaper.aves.inventory.util.InventoryConstants.CANCEL_CLICK;
import static net.theevilreaper.tamias.setup.util.SetupMessages.DELETE_CLICK;
import static net.theevilreaper.tamias.setup.util.SetupMessages.SET_BLOCK_CLICK;

/**
 * The {@link GroundPresetEditInventory} edits a single ground preset of a game map.
 * A block is set by clicking a slot while the block item is on the cursor, a right click clears the slot.
 * Changes are only applied to the map builder when the preset is saved.
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 */
@SuppressWarnings("java:S3252")
public class GroundPresetEditInventory extends PersonalInventoryBuilder {

    private static final int GROUND_SLOT = 10;
    private static final int[] ADDITIONAL_SLOTS = LayoutCalculator.from(12, 13, 14, 15, 16);
    private static final int[] FILL_SLOTS = LayoutCalculator.from(11, 19, 20, 22, 24, 25);
    private static final int BACK_SLOT = 21;
    private static final int SAVE_SLOT = 23;

    private static final ItemStack BACK_ITEM = ItemStack.builder(Material.ARROW)
            .customName(Component.text("Back", NamedTextColor.RED))
            .build();
    private static final ItemStack SAVE_ITEM = ItemStack.builder(Material.LIME_DYE)
            .customName(Component.text("Save preset", NamedTextColor.GREEN))
            .build();

    private final GameMapBuilder mapBuilder;
    private final Block[] additionalBlocks;
    private int index;
    private @Nullable Block groundBlock;

    /**
     * Creates a new {@link GroundPresetEditInventory} instance.
     *
     * @param player       the player who owns the inventory
     * @param mapBuilder   the map builder which holds the presets
     * @param openOverview opens the preset overview
     * @param onChange     called after a preset was saved
     */
    public GroundPresetEditInventory(Player player, GameMapBuilder mapBuilder, Runnable openOverview, Runnable onChange) {
        super(Component.text("Edit ground preset"), InventoryType.CHEST_4_ROW, player);
        this.mapBuilder = mapBuilder;
        this.additionalBlocks = new Block[ADDITIONAL_SLOTS.length];
        this.index = GroundPresetInventory.NEW_PRESET;
        this.setCancelEmptySlotClicks(true);

        InventoryLayout layout = InventoryLayout.fromType(getType());
        layout.setItems(LayoutCalculator.quad(0, getType().getSize() - 1), SetupItems.DECORATION, CANCEL_CLICK);
        layout.setItems(FILL_SLOTS, SetupItems.DECORATION, CANCEL_CLICK);
        layout.setItem(BACK_SLOT, BACK_ITEM, (clicker, slot, click, stack, result) -> {
            result.accept(ClickHolder.cancelClick());
            openOverview.run();
        });
        layout.setItem(SAVE_SLOT, SAVE_ITEM, (clicker, slot, click, stack, result) -> {
            result.accept(ClickHolder.cancelClick());
            if (this.save(clicker)) {
                onChange.run();
                openOverview.run();
            }
        });
        this.setLayout(layout);

        this.setDataLayoutFunction(dataLayout -> {
            dataLayout = dataLayout == null ? InventoryLayout.fromType(getType()) : dataLayout;
            dataLayout.setItem(GROUND_SLOT, getBlockItem("Ground block", this.groundBlock), blockClick(block -> this.groundBlock = block));
            for (int i = 0; i < ADDITIONAL_SLOTS.length; i++) {
                int additionalIndex = i;
                dataLayout.setItem(
                        ADDITIONAL_SLOTS[i],
                        getBlockItem("Additional block", this.additionalBlocks[i]),
                        blockClick(block -> this.additionalBlocks[additionalIndex] = block)
                );
            }
            return dataLayout;
        });

        this.invalidateLayout();
        this.register();
    }

    /**
     * Loads the preset with the given index into the editor and opens it.
     *
     * @param index the index of the preset or {@link GroundPresetInventory#NEW_PRESET} for a new one
     */
    public void edit(int index) {
        List<GroundData> presets = this.mapBuilder.getGroundPresets();
        this.index = index >= 0 && index < presets.size() ? index : GroundPresetInventory.NEW_PRESET;
        Arrays.fill(this.additionalBlocks, null);
        this.groundBlock = null;

        if (this.index != GroundPresetInventory.NEW_PRESET) {
            GroundData groundData = presets.get(this.index);
            this.groundBlock = groundData.groundBlock();
            if (groundData.hasAdditionalBlocks()) {
                List<Block> blocks = groundData.additionalBlocks();
                for (int i = 0; i < blocks.size() && i < this.additionalBlocks.length; i++) {
                    this.additionalBlocks[i] = blocks.get(i);
                }
            }
        }
        this.invalidateDataLayout();
        this.open();
    }

    /**
     * Applies the edited preset to the map builder.
     *
     * @param player the player who saves the preset
     * @return {@code true} if the preset was saved
     */
    boolean save(Player player) {
        if (this.groundBlock == null) {
            player.sendMessage(SetupMessages.GROUND_BLOCK_REQUIRED);
            return false;
        }
        List<Block> additional = Arrays.stream(this.additionalBlocks).filter(Objects::nonNull).toList();
        GroundData groundData = new GroundData(this.groundBlock, additional.isEmpty() ? null : additional);

        if (this.index == GroundPresetInventory.NEW_PRESET || this.index >= this.mapBuilder.getGroundPresets().size()) {
            if (this.mapBuilder.getGroundPresets().size() >= GroundPresetInventory.MAX_PRESETS) {
                player.sendMessage(SetupMessages.GROUND_PRESET_LIMIT);
                return false;
            }
            this.mapBuilder.addGroundPreset(groundData);
            this.index = this.mapBuilder.getGroundPresets().size() - 1;
        } else {
            this.mapBuilder.setGroundPreset(this.index, groundData);
        }
        player.playSound(SetupSounds.DATA_SET);
        return true;
    }

    /**
     * Creates the click handling for a block slot.
     * A click with an item on the cursor sets the block, a right click without an item clears the slot.
     *
     * @param setter receives the new block or {@code null} if the slot was cleared
     * @return the created click
     */
    private InventoryClick blockClick(Consumer<@Nullable Block> setter) {
        return (clicker, slot, click, stack, result) -> {
            result.accept(ClickHolder.cancelClick());
            ItemStack cursor = clicker.getInventory().getCursorItem();

            if (cursor.isAir()) {
                if (click instanceof Click.Right) {
                    setter.accept(null);
                    this.invalidateDataLayout();
                }
                return;
            }

            Block block = GroundBlocks.fromItem(cursor);
            if (block == null) {
                clicker.sendMessage(SetupMessages.getInvalidGroundBlock(cursor.material()));
                return;
            }
            setter.accept(block);
            clicker.playSound(SetupSounds.DATA_SET);
            this.invalidateDataLayout();
        };
    }

    /**
     * Creates the item for a block slot.
     *
     * @param name  the name of the slot
     * @param block the block in the slot or {@code null} if the slot is empty
     * @return the created item
     */
    private static ItemStack getBlockItem(String name, @Nullable Block block) {
        if (block == null) {
            return ItemStack.builder(Material.LIGHT_GRAY_STAINED_GLASS_PANE)
                    .customName(Component.text(name, NamedTextColor.GRAY))
                    .lore(Component.empty(), SET_BLOCK_CLICK, Component.empty())
                    .build();
        }
        return ItemStack.builder(GroundBlocks.displayMaterial(block))
                .customName(Component.text(name + ": ", NamedTextColor.GRAY)
                        .append(Component.text(GroundBlocks.displayName(block), NamedTextColor.GREEN)))
                .lore(Component.empty(), SET_BLOCK_CLICK, DELETE_CLICK, Component.empty())
                .build();
    }

    /**
     * Returns the index of the preset which is edited.
     *
     * @return the preset index or {@link GroundPresetInventory#NEW_PRESET}
     */
    int getIndex() {
        return index;
    }

    /**
     * Returns the ground block of the edited preset.
     *
     * @return the ground block or {@code null} if none is set
     */
    @Nullable Block getGroundBlock() {
        return groundBlock;
    }
}

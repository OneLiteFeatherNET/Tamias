package net.theevilreaper.tamias.setup.inventory;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.block.Block;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.theevilreaper.aves.inventory.PersonalInventoryBuilder;
import net.theevilreaper.aves.inventory.click.ClickHolder;
import net.theevilreaper.aves.inventory.layout.InventoryLayout;
import net.theevilreaper.aves.inventory.util.LayoutCalculator;
import net.theevilreaper.tamias.common.ground.GroundCategory;
import net.theevilreaper.tamias.common.ground.GroundData;
import net.theevilreaper.tamias.common.map.builder.GameMapBuilder;
import net.theevilreaper.tamias.setup.util.GroundBlocks;
import net.theevilreaper.tamias.setup.util.SetupItems;
import net.theevilreaper.tamias.setup.util.SetupMessages;
import net.theevilreaper.tamias.setup.util.SetupSounds;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.SequencedSet;

import static net.theevilreaper.aves.inventory.util.InventoryConstants.CANCEL_CLICK;
import static net.theevilreaper.tamias.setup.util.SetupMessages.CHOOSE_CLICK;
import static net.theevilreaper.tamias.setup.util.SetupMessages.CLEAR_CLICK;
import static net.theevilreaper.tamias.setup.util.SetupMessages.NO_SPACE_SEPARATOR;
import static net.theevilreaper.tamias.setup.util.SetupMessages.loreValue;

/**
 * The {@link GroundPresetEditInventory} edits a single ground preset of a game map.
 * The ground block and the decoration blocks are picked from the {@link GroundCategory} entries.
 * Changes are only applied to the map builder when the preset is saved.
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 */
@SuppressWarnings("java:S3252")
public class GroundPresetEditInventory extends PersonalInventoryBuilder {

    /**
     * The maximum amount of decoration blocks a preset can have.
     */
    public static final int MAX_DECORATION_BLOCKS = 5;

    static final int GROUND_SLOT = 11;
    static final int DECORATION_SLOT = 15;
    static final int BACK_SLOT = 21;
    static final int SAVE_SLOT = 23;
    private static final int[] FILL_SLOTS = LayoutCalculator.from(10, 12, 13, 14, 16, 19, 20, 22, 24, 25);

    private static final ItemStack BACK_ITEM = ItemStack.builder(Material.ARROW)
            .customName(Component.text("Back", NamedTextColor.RED))
            .lore(Component.empty(), NO_SPACE_SEPARATOR.append(Component.space()).append(Component.text("Discards unsaved changes", NamedTextColor.GRAY)), Component.empty())
            .build();
    private static final ItemStack SAVE_ITEM = ItemStack.builder(Material.LIME_DYE)
            .customName(Component.text("Save preset", NamedTextColor.GREEN))
            .lore(Component.empty(), NO_SPACE_SEPARATOR.append(Component.space()).append(Component.text("Applies the preset to the map", NamedTextColor.GRAY)), Component.empty())
            .build();

    private final GameMapBuilder mapBuilder;
    private final SequencedSet<Block> decorationBlocks;
    private final BlockPickerInventory groundPicker;
    private final BlockPickerInventory decorationPicker;
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
        super(Component.text("Preset Editor"), InventoryType.CHEST_4_ROW, player);
        this.mapBuilder = mapBuilder;
        this.decorationBlocks = new LinkedHashSet<>();
        this.index = GroundPresetInventory.NEW_PRESET;
        this.groundPicker = new BlockPickerInventory(
                Component.text("Choose Ground Block"),
                player,
                false,
                block -> block.equals(this.groundBlock),
                this::pickGround,
                this::open
        );
        this.decorationPicker = new BlockPickerInventory(
                Component.text("Choose Decoration Blocks"),
                player,
                true,
                this.decorationBlocks::contains,
                block -> this.toggleDecoration(player, block),
                this::open
        );
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
            dataLayout.setItem(GROUND_SLOT, getGroundItem(), (clicker, slot, click, stack, result) -> {
                result.accept(ClickHolder.cancelClick());
                if (click instanceof Click.Left) {
                    this.groundPicker.openCategories();
                }
            });
            dataLayout.setItem(DECORATION_SLOT, getDecorationItem(), (clicker, slot, click, stack, result) -> {
                result.accept(ClickHolder.cancelClick());
                if (click instanceof Click.Left) {
                    this.decorationPicker.openCategories();
                    return;
                }
                if (click instanceof Click.Right) {
                    this.decorationBlocks.clear();
                    this.refresh();
                }
            });
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
        this.groundBlock = null;
        this.decorationBlocks.clear();

        if (this.index != GroundPresetInventory.NEW_PRESET) {
            GroundData groundData = presets.get(this.index);
            this.groundBlock = groundData.groundBlock();
            if (groundData.hasAdditionalBlocks()) {
                this.decorationBlocks.addAll(groundData.additionalBlocks());
            }
        }
        this.refresh();
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
        List<Block> additional = this.decorationBlocks.isEmpty() ? null : List.copyOf(this.decorationBlocks);
        GroundData groundData = new GroundData(this.groundBlock, additional);

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

    private void pickGround(Block block) {
        this.groundBlock = block;
        this.refresh();
        this.open();
    }

    private void toggleDecoration(Player player, Block block) {
        if (!this.decorationBlocks.remove(block)) {
            if (this.decorationBlocks.size() >= MAX_DECORATION_BLOCKS) {
                player.sendMessage(SetupMessages.getDecorationLimit(MAX_DECORATION_BLOCKS));
                return;
            }
            this.decorationBlocks.add(block);
        }
        this.refresh();
    }

    /**
     * Updates the editor and both pickers after the edited preset changed.
     */
    private void refresh() {
        this.invalidateDataLayout();
        this.groundPicker.invalidateDataLayout();
        this.decorationPicker.invalidateDataLayout();
    }

    private ItemStack getGroundItem() {
        Material material = this.groundBlock == null
                ? Material.LIGHT_GRAY_STAINED_GLASS_PANE
                : GroundBlocks.displayMaterial(this.groundBlock);
        Component current = this.groundBlock == null
                ? loreValue("Current", "not chosen", NamedTextColor.RED)
                : loreValue("Current", GroundBlocks.displayName(this.groundBlock), NamedTextColor.GREEN);
        return ItemStack.builder(material)
                .customName(Component.text("Ground block", NamedTextColor.YELLOW))
                .lore(Component.empty(), current, Component.empty(), CHOOSE_CLICK, Component.empty())
                .build();
    }

    private ItemStack getDecorationItem() {
        Material material = this.decorationBlocks.isEmpty()
                ? Material.LIGHT_GRAY_STAINED_GLASS_PANE
                : GroundBlocks.displayMaterial(this.decorationBlocks.getFirst());
        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());
        lore.add(loreValue("Selected", this.decorationBlocks.size() + "/" + MAX_DECORATION_BLOCKS, NamedTextColor.AQUA));
        for (Block block : this.decorationBlocks) {
            lore.add(Component.text("   - ", NamedTextColor.DARK_GRAY).append(Component.text(GroundBlocks.displayName(block), NamedTextColor.WHITE)));
        }
        lore.add(Component.empty());
        lore.add(CHOOSE_CLICK);
        if (!this.decorationBlocks.isEmpty()) {
            lore.add(CLEAR_CLICK);
        }
        lore.add(Component.empty());

        return ItemStack.builder(material)
                .customName(Component.text("Decoration blocks", NamedTextColor.YELLOW))
                .lore(lore)
                .build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unregister() {
        this.groundPicker.unregister();
        this.decorationPicker.unregister();
        super.unregister();
    }

    int getIndex() {
        return index;
    }

    @Nullable Block getGroundBlock() {
        return groundBlock;
    }

    List<Block> getDecorationBlocks() {
        return List.copyOf(decorationBlocks);
    }

    BlockPickerInventory getGroundPicker() {
        return groundPicker;
    }

    BlockPickerInventory getDecorationPicker() {
        return decorationPicker;
    }
}

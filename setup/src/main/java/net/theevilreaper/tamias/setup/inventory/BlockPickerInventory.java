package net.theevilreaper.tamias.setup.inventory;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.block.Block;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.theevilreaper.aves.inventory.PersonalInventoryBuilder;
import net.theevilreaper.aves.inventory.click.ClickHolder;
import net.theevilreaper.aves.inventory.layout.InventoryLayout;
import net.theevilreaper.aves.inventory.util.LayoutCalculator;
import net.theevilreaper.tamias.common.ground.GroundCategory;
import net.theevilreaper.tamias.setup.util.GroundBlocks;
import net.theevilreaper.tamias.setup.util.SetupItems;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static net.theevilreaper.aves.inventory.util.InventoryConstants.CANCEL_CLICK;
import static net.theevilreaper.tamias.setup.util.SetupMessages.ADD_CLICK;
import static net.theevilreaper.tamias.setup.util.SetupMessages.NO_SPACE_SEPARATOR;
import static net.theevilreaper.tamias.setup.util.SetupMessages.OPEN_CLICK;
import static net.theevilreaper.tamias.setup.util.SetupMessages.REMOVE_CLICK;
import static net.theevilreaper.tamias.setup.util.SetupMessages.SELECT_CLICK;
import static net.theevilreaper.tamias.setup.util.SetupMessages.loreValue;

/**
 * The {@link BlockPickerInventory} lets the player pick blocks from the {@link GroundCategory} entries.
 * It first shows all categories, a click on a category shows its blocks.
 * Selected blocks are highlighted, so the inventory can be used for single and multi selection.
 * The inventory is used during the setup process.
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @see GroundCategory
 * @since 1.0.0
 */
@SuppressWarnings("java:S3252")
public class BlockPickerInventory extends PersonalInventoryBuilder {

    static final int[] CONTENT_SLOTS = LayoutCalculator.from(
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
    );
    static final int BACK_SLOT = 49;

    private static final GroundCategory[] CATEGORIES = GroundCategory.values();

    private final Predicate<Block> isSelected;
    private final Consumer<Block> onPick;
    private final boolean multiSelect;
    private @Nullable GroundCategory category;

    /**
     * Creates a new {@link BlockPickerInventory} instance.
     *
     * @param title       the title of the inventory
     * @param player      the player who owns the inventory
     * @param multiSelect if multiple blocks can be selected at once
     * @param isSelected  checks if a block is currently selected
     * @param onPick      called with the block which was clicked
     * @param onBack      called when the player leaves the category overview
     */
    public BlockPickerInventory(
            Component title,
            Player player,
            boolean multiSelect,
            Predicate<Block> isSelected,
            Consumer<Block> onPick,
            Runnable onBack
    ) {
        super(title, InventoryType.CHEST_6_ROW, player);
        if (CATEGORIES.length > CONTENT_SLOTS.length) {
            throw new IllegalStateException("The picker can't display more than " + CONTENT_SLOTS.length + " categories");
        }
        this.multiSelect = multiSelect;
        this.isSelected = isSelected;
        this.onPick = onPick;
        this.setCancelEmptySlotClicks(true);

        InventoryLayout layout = InventoryLayout.fromType(getType());
        layout.setItems(LayoutCalculator.quad(0, getType().getSize() - 1), SetupItems.DECORATION, CANCEL_CLICK);
        this.setLayout(layout);

        this.setDataLayoutFunction(dataLayout -> {
            dataLayout = dataLayout == null ? InventoryLayout.fromType(getType()) : dataLayout;
            dataLayout.setItems(CONTENT_SLOTS, SetupItems.DECORATION, CANCEL_CLICK);
            if (this.category == null) {
                this.applyCategories(dataLayout);
                dataLayout.setItem(BACK_SLOT, getBackItem("Back to the editor"), (clicker, slot, click, stack, result) -> {
                    result.accept(ClickHolder.cancelClick());
                    onBack.run();
                });
            } else {
                this.applyBlocks(dataLayout, this.category);
                dataLayout.setItem(BACK_SLOT, getBackItem("Back to the categories"), (clicker, slot, click, stack, result) -> {
                    result.accept(ClickHolder.cancelClick());
                    this.showCategory(null);
                });
            }
            return dataLayout;
        });

        this.invalidateLayout();
        this.register();
    }

    /**
     * Opens the inventory with the category overview.
     */
    public void openCategories() {
        this.showCategory(null);
        this.open();
    }

    /**
     * Switches the displayed content to the given category.
     *
     * @param category the category to show or {@code null} to show the category overview
     */
    void showCategory(@Nullable GroundCategory category) {
        this.category = category;
        this.invalidateDataLayout();
    }

    /**
     * Returns the category which is currently displayed.
     *
     * @return the category or {@code null} if the category overview is displayed
     */
    @Nullable GroundCategory getCategory() {
        return category;
    }

    private void applyCategories(InventoryLayout dataLayout) {
        for (int i = 0; i < CATEGORIES.length; i++) {
            GroundCategory entry = CATEGORIES[i];
            dataLayout.setItem(CONTENT_SLOTS[i], getCategoryItem(entry), (clicker, slot, click, stack, result) -> {
                result.accept(ClickHolder.cancelClick());
                this.showCategory(entry);
            });
        }
    }

    private void applyBlocks(InventoryLayout dataLayout, GroundCategory entry) {
        List<Block> blocks = entry.blocks();
        for (int i = 0; i < blocks.size() && i < CONTENT_SLOTS.length; i++) {
            Block block = blocks.get(i);
            dataLayout.setItem(CONTENT_SLOTS[i], getBlockItem(block), (clicker, slot, click, stack, result) -> {
                result.accept(ClickHolder.cancelClick());
                this.onPick.accept(block);
            });
        }
    }

    private ItemStack getCategoryItem(GroundCategory entry) {
        long selected = entry.blocks().stream().filter(this.isSelected).count();
        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());
        lore.add(loreValue("Blocks", String.valueOf(entry.blocks().size()), NamedTextColor.WHITE));
        if (selected > 0) {
            lore.add(loreValue("Selected", String.valueOf(selected), NamedTextColor.GREEN));
        }
        lore.add(Component.empty());
        lore.add(OPEN_CLICK);
        lore.add(Component.empty());

        return ItemStack.builder(GroundBlocks.displayMaterial(entry.icon()))
                .customName(Component.text(entry.displayName(), NamedTextColor.GOLD))
                .lore(lore)
                .glowing(selected > 0)
                .build();
    }

    private ItemStack getBlockItem(Block block) {
        boolean selected = this.isSelected.test(block);
        Component action;
        if (this.multiSelect) {
            action = selected ? REMOVE_CLICK : ADD_CLICK;
        } else {
            action = SELECT_CLICK;
        }
        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());
        if (selected) {
            lore.add(NO_SPACE_SEPARATOR.append(Component.space()).append(Component.text("Selected", NamedTextColor.GREEN)));
            lore.add(Component.empty());
        }
        lore.add(action);
        lore.add(Component.empty());

        return ItemStack.builder(GroundBlocks.displayMaterial(block))
                .customName(Component.text(GroundBlocks.displayName(block), selected ? NamedTextColor.GREEN : NamedTextColor.WHITE))
                .lore(lore)
                .glowing(selected)
                .build();
    }

    private static ItemStack getBackItem(String description) {
        return ItemStack.builder(Material.ARROW)
                .customName(Component.text("Back", NamedTextColor.RED))
                .lore(Component.empty(), NO_SPACE_SEPARATOR.append(Component.space()).append(Component.text(description, NamedTextColor.GRAY)), Component.empty())
                .build();
    }
}

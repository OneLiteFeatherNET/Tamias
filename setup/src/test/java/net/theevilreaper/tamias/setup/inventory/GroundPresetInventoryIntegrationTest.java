package net.theevilreaper.tamias.setup.inventory;

import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.block.Block;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.item.Material;
import net.minestom.testing.Env;
import net.minestom.testing.extension.MicrotusExtension;
import net.theevilreaper.aves.inventory.InventoryBuilder;
import net.theevilreaper.aves.inventory.layout.InventoryLayout;
import net.theevilreaper.aves.inventory.slot.EmptySlot;
import net.theevilreaper.aves.inventory.slot.ISlot;
import net.theevilreaper.tamias.common.ground.GroundCategory;
import net.theevilreaper.tamias.common.ground.GroundData;
import net.theevilreaper.tamias.common.map.builder.GameMapBuilder;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MicrotusExtension.class)
class GroundPresetInventoryIntegrationTest {

    private static final int FIRST_PRESET_SLOT = 10;
    private static final int ADD_SLOT = 32;

    @Test
    void testCreatePresetWithPickers(@NotNull Env env) {
        Player player = env.createPlayer(env.createFlatInstance());
        GameMapBuilder builder = new GameMapBuilder();
        AtomicInteger overviewOpened = new AtomicInteger();
        AtomicInteger changes = new AtomicInteger();
        GroundPresetEditInventory editor = new GroundPresetEditInventory(player, builder, overviewOpened::incrementAndGet, changes::incrementAndGet);

        editor.edit(GroundPresetInventory.NEW_PRESET);
        env.tick();

        click(editor, player, GroundPresetEditInventory.SAVE_SLOT, new Click.Left(GroundPresetEditInventory.SAVE_SLOT));
        assertTrue(builder.getGroundPresets().isEmpty(), "A preset without ground block must not be saved");
        assertEquals(0, changes.get());

        click(editor, player, GroundPresetEditInventory.GROUND_SLOT, new Click.Left(GroundPresetEditInventory.GROUND_SLOT));
        env.tick();
        BlockPickerInventory groundPicker = editor.getGroundPicker();
        assertSame(groundPicker.getInventory(), player.getOpenInventory());

        pick(env, groundPicker, player, GroundCategory.SAND, Block.SAND);
        assertEquals(Block.SAND, editor.getGroundBlock());
        env.tick();
        assertSame(editor.getInventory(), player.getOpenInventory(), "Picking a ground block must return to the editor");

        BlockPickerInventory decorationPicker = editor.getDecorationPicker();
        decorationPicker.openCategories();
        env.tick();
        int gravelSlot = pick(env, decorationPicker, player, GroundCategory.SAND, Block.GRAVEL);
        env.tick();
        assertTrue(decorationPicker.getInventory().getItemStack(gravelSlot).has(DataComponents.ENCHANTMENT_GLINT_OVERRIDE),
                "A selected decoration block must be highlighted");

        click(editor, player, GroundPresetEditInventory.SAVE_SLOT, new Click.Left(GroundPresetEditInventory.SAVE_SLOT));
        assertEquals(List.of(new GroundData(Block.SAND, List.of(Block.GRAVEL))), builder.getGroundPresets());
        assertEquals(1, changes.get());
        assertEquals(1, overviewOpened.get());

        editor.unregister();
    }

    @Test
    void testDecorationToggleAndLimit(@NotNull Env env) {
        Player player = env.createPlayer(env.createFlatInstance());
        GroundPresetEditInventory editor = new GroundPresetEditInventory(player, new GameMapBuilder(), () -> {}, () -> {});
        editor.edit(GroundPresetInventory.NEW_PRESET);
        BlockPickerInventory decorationPicker = editor.getDecorationPicker();
        decorationPicker.openCategories();
        env.tick();
        decorationPicker.showCategory(GroundCategory.STONE);
        env.tick();

        List<Block> stoneBlocks = GroundCategory.STONE.blocks();
        for (int i = 0; i <= GroundPresetEditInventory.MAX_DECORATION_BLOCKS; i++) {
            int slot = BlockPickerInventory.CONTENT_SLOTS[i];
            click(decorationPicker, player, slot, new Click.Left(slot));
        }
        assertEquals(stoneBlocks.subList(0, GroundPresetEditInventory.MAX_DECORATION_BLOCKS), editor.getDecorationBlocks());

        int firstSlot = BlockPickerInventory.CONTENT_SLOTS[0];
        click(decorationPicker, player, firstSlot, new Click.Left(firstSlot));
        assertFalse(editor.getDecorationBlocks().contains(stoneBlocks.getFirst()), "A second click must deselect the block");

        editor.unregister();
    }

    @Test
    void testPickerNavigation(@NotNull Env env) {
        Player player = env.createPlayer(env.createFlatInstance());
        GroundPresetEditInventory editor = new GroundPresetEditInventory(player, new GameMapBuilder(), () -> {}, () -> {});
        editor.edit(GroundPresetInventory.NEW_PRESET);
        BlockPickerInventory picker = editor.getGroundPicker();
        picker.openCategories();
        env.tick();
        assertNull(picker.getCategory());

        int woodSlot = BlockPickerInventory.CONTENT_SLOTS[GroundCategory.WOOD.ordinal()];
        click(picker, player, woodSlot, new Click.Left(woodSlot));
        env.tick();
        assertEquals(GroundCategory.WOOD, picker.getCategory());
        assertEquals(Material.OAK_PLANKS, picker.getInventory().getItemStack(BlockPickerInventory.CONTENT_SLOTS[0]).material());

        click(picker, player, BlockPickerInventory.BACK_SLOT, new Click.Left(BlockPickerInventory.BACK_SLOT));
        env.tick();
        assertNull(picker.getCategory(), "Back must return to the categories");

        click(picker, player, BlockPickerInventory.BACK_SLOT, new Click.Left(BlockPickerInventory.BACK_SLOT));
        env.tick();
        assertSame(editor.getInventory(), player.getOpenInventory(), "Back in the categories must return to the editor");

        editor.unregister();
    }

    @Test
    void testEditExistingPreset(@NotNull Env env) {
        Player player = env.createPlayer(env.createFlatInstance());
        GameMapBuilder builder = new GameMapBuilder()
                .addGroundPreset(new GroundData(Block.ICE, List.of(Block.SPONGE)));
        GroundPresetEditInventory editor = new GroundPresetEditInventory(player, builder, () -> {}, () -> {});

        editor.edit(0);
        env.tick();
        assertEquals(0, editor.getIndex());
        assertEquals(Block.ICE, editor.getGroundBlock());
        assertEquals(List.of(Block.SPONGE), editor.getDecorationBlocks(), "Blocks outside the categories must be kept");

        click(editor, player, GroundPresetEditInventory.DECORATION_SLOT, new Click.Right(GroundPresetEditInventory.DECORATION_SLOT));
        assertTrue(editor.getDecorationBlocks().isEmpty());

        click(editor, player, GroundPresetEditInventory.SAVE_SLOT, new Click.Left(GroundPresetEditInventory.SAVE_SLOT));
        assertEquals(List.of(new GroundData(Block.ICE, null)), builder.getGroundPresets());

        editor.unregister();
    }

    @Test
    void testOverviewEditAndRemove(@NotNull Env env) {
        Player player = env.createPlayer(env.createFlatInstance());
        GameMapBuilder builder = new GameMapBuilder()
                .addGroundPreset(new GroundData(Block.SAND, null))
                .addGroundPreset(new GroundData(Block.ICE, null));
        List<Integer> editedIndices = new ArrayList<>();
        AtomicInteger changes = new AtomicInteger();
        GroundPresetInventory overview = new GroundPresetInventory(player, builder, editedIndices::add, () -> {}, changes::incrementAndGet);

        overview.open();
        env.tick();

        assertEquals(Material.SAND, overview.getInventory().getItemStack(FIRST_PRESET_SLOT).material());
        assertEquals(Material.ICE, overview.getInventory().getItemStack(FIRST_PRESET_SLOT + 1).material());

        click(overview, player, FIRST_PRESET_SLOT + 1, new Click.Left(FIRST_PRESET_SLOT + 1));
        assertEquals(List.of(1), editedIndices);

        click(overview, player, FIRST_PRESET_SLOT, new Click.Right(FIRST_PRESET_SLOT));
        assertEquals(List.of(new GroundData(Block.ICE, null)), builder.getGroundPresets());
        assertEquals(1, changes.get());

        click(overview, player, ADD_SLOT, new Click.Left(ADD_SLOT));
        assertEquals(List.of(1, GroundPresetInventory.NEW_PRESET), editedIndices);

        overview.unregister();
    }

    @Test
    void testOverviewRespectsPresetLimit(@NotNull Env env) {
        Player player = env.createPlayer(env.createFlatInstance());
        GameMapBuilder builder = new GameMapBuilder();
        for (int i = 0; i < GroundPresetInventory.MAX_PRESETS; i++) {
            builder.addGroundPreset(new GroundData(Block.SAND, null));
        }
        List<Integer> editedIndices = new ArrayList<>();
        GroundPresetInventory overview = new GroundPresetInventory(player, builder, editedIndices::add, () -> {}, () -> {});

        overview.open();
        env.tick();

        click(overview, player, ADD_SLOT, new Click.Left(ADD_SLOT));
        assertTrue(editedIndices.isEmpty());

        overview.unregister();
    }

    /**
     * Opens the given category in the picker and clicks the given block.
     *
     * @return the slot of the clicked block
     */
    private static int pick(Env env, BlockPickerInventory picker, Player player, GroundCategory category, Block block) {
        picker.showCategory(category);
        env.tick();
        int index = category.blocks().indexOf(block);
        assertTrue(index >= 0, block.key().asString() + " is not part of " + category);
        int slot = BlockPickerInventory.CONTENT_SLOTS[index];
        click(picker, player, slot, new Click.Left(slot));
        return slot;
    }

    /**
     * Executes the click handling of the given slot like the inventory listener would do it.
     */
    private static void click(InventoryBuilder builder, Player player, int slot, Click click) {
        ISlot clickedSlot = getSlot(builder.getDataLayout(), slot);
        if (clickedSlot == null) {
            clickedSlot = getSlot(builder.getLayout(), slot);
        }
        assertNotNull(clickedSlot, "No slot found for index " + slot);
        clickedSlot.getClick().onClick(player, slot, click, clickedSlot.getItem(), holder -> {});
    }

    private static ISlot getSlot(InventoryLayout layout, int slot) {
        if (layout == null) return null;
        ISlot clickedSlot = layout.getSlot(slot);
        return clickedSlot instanceof EmptySlot ? null : clickedSlot;
    }
}

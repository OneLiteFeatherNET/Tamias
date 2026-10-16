package net.theevilreaper.tamias.setup.inventory;

import net.minestom.server.entity.Player;
import net.minestom.server.instance.block.Block;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.testing.Env;
import net.minestom.testing.extension.MicrotusExtension;
import net.theevilreaper.aves.inventory.InventoryBuilder;
import net.theevilreaper.aves.inventory.layout.InventoryLayout;
import net.theevilreaper.aves.inventory.slot.EmptySlot;
import net.theevilreaper.aves.inventory.slot.ISlot;
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

    private static final int GROUND_SLOT = 10;
    private static final int FIRST_ADDITIONAL_SLOT = 12;
    private static final int EDITOR_SAVE_SLOT = 23;
    private static final int FIRST_PRESET_SLOT = 10;
    private static final int ADD_SLOT = 32;

    @Test
    void testCreatePresetWithCursorItems(@NotNull Env env) {
        Player player = env.createPlayer(env.createFlatInstance());
        GameMapBuilder builder = new GameMapBuilder();
        AtomicInteger overviewOpened = new AtomicInteger();
        AtomicInteger changes = new AtomicInteger();
        GroundPresetEditInventory editor = new GroundPresetEditInventory(player, builder, overviewOpened::incrementAndGet, changes::incrementAndGet);

        editor.edit(GroundPresetInventory.NEW_PRESET);
        env.tick();

        click(editor, player, EDITOR_SAVE_SLOT, new Click.Left(EDITOR_SAVE_SLOT));
        assertTrue(builder.getGroundPresets().isEmpty(), "A preset without ground block must not be saved");
        assertEquals(0, changes.get());

        player.getInventory().setCursorItem(ItemStack.of(Material.SAND));
        click(editor, player, GROUND_SLOT, new Click.Left(GROUND_SLOT));
        player.getInventory().setCursorItem(ItemStack.of(Material.RED_SAND));
        click(editor, player, FIRST_ADDITIONAL_SLOT, new Click.Left(FIRST_ADDITIONAL_SLOT));
        env.tick();

        assertEquals(Block.SAND, editor.getGroundBlock());
        assertEquals(Material.RED_SAND, editor.getInventory().getItemStack(FIRST_ADDITIONAL_SLOT).material());
        assertEquals(Material.RED_SAND, player.getInventory().getCursorItem().material(), "The cursor item must stay");

        click(editor, player, EDITOR_SAVE_SLOT, new Click.Left(EDITOR_SAVE_SLOT));
        assertEquals(List.of(new GroundData(Block.SAND, List.of(Block.RED_SAND))), builder.getGroundPresets());
        assertEquals(1, changes.get());
        assertEquals(1, overviewOpened.get());

        editor.unregister();
    }

    @Test
    void testInvalidCursorItemIsRejected(@NotNull Env env) {
        Player player = env.createPlayer(env.createFlatInstance());
        GroundPresetEditInventory editor = new GroundPresetEditInventory(player, new GameMapBuilder(), () -> {}, () -> {});

        editor.edit(GroundPresetInventory.NEW_PRESET);
        env.tick();

        player.getInventory().setCursorItem(ItemStack.of(Material.TNT));
        click(editor, player, GROUND_SLOT, new Click.Left(GROUND_SLOT));
        assertNull(editor.getGroundBlock());

        editor.unregister();
    }

    @Test
    void testEditAndClearExistingPreset(@NotNull Env env) {
        Player player = env.createPlayer(env.createFlatInstance());
        GameMapBuilder builder = new GameMapBuilder()
                .addGroundPreset(new GroundData(Block.ICE, List.of(Block.PACKED_ICE)));
        GroundPresetEditInventory editor = new GroundPresetEditInventory(player, builder, () -> {}, () -> {});

        editor.edit(0);
        env.tick();
        assertEquals(0, editor.getIndex());
        assertEquals(Block.ICE, editor.getGroundBlock());

        player.getInventory().setCursorItem(ItemStack.AIR);
        click(editor, player, FIRST_ADDITIONAL_SLOT, new Click.Right(FIRST_ADDITIONAL_SLOT));
        player.getInventory().setCursorItem(ItemStack.of(Material.SNOW_BLOCK));
        click(editor, player, GROUND_SLOT, new Click.Left(GROUND_SLOT));
        env.tick();

        click(editor, player, EDITOR_SAVE_SLOT, new Click.Left(EDITOR_SAVE_SLOT));
        assertEquals(List.of(new GroundData(Block.SNOW_BLOCK, null)), builder.getGroundPresets());

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

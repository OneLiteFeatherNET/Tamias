package net.theevilreaper.tamias.setup.inventory;

import de.icevizion.aves.inventory.slot.ISlot;
import de.icevizion.aves.map.MapEntry;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.server.play.OpenWindowPacket;
import net.minestom.server.network.packet.server.play.WindowItemsPacket;
import net.minestom.testing.Collector;
import net.minestom.testing.Env;
import net.minestom.testing.TestConnection;
import net.minestom.testing.extension.MicrotusExtension;
import net.theevilreaper.tamias.setup.MapDataTestBase;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MicrotusExtension.class)
class MapSetupInventoryIntegrationTest extends MapDataTestBase {

    private static Supplier<List<MapEntry>> mapEntries;

    @BeforeAll
    static void setUp() {
        mapEntries = () -> List.of(testMapEntry);
    }

    @AfterAll
    static void tearDown() {
        mapEntries = null;
    }

    @Test
    void testMapSetupInventory(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        TestConnection connection = env.createConnection();
        Player player = connection.connect(instance, Pos.ZERO).join();

        assertEquals(1, mapEntries.get().size());

        MapSetupInventory mapSetupInventory = new MapSetupInventory(mapEntries);

        Collector<OpenWindowPacket> windowPacketCollector = connection.trackIncoming(OpenWindowPacket.class);
        Collector<WindowItemsPacket> windowItemsPacketCollector = connection.trackIncoming(WindowItemsPacket.class);

        mapSetupInventory.open(player);

        // We need to tick the server to send the packets
        env.tick();

        List<OpenWindowPacket> packets = windowPacketCollector.collect();
        List<WindowItemsPacket> itemsPackets = windowItemsPacketCollector.collect();

        windowPacketCollector.assertCount(1);
        windowItemsPacketCollector.assertCount(2);

        assertEquals(1, packets.size(), "The packet size must be at least 1");
        assertEquals(2, itemsPackets.size(), "There should only be two item");

        OpenWindowPacket windowPacket = packets.getFirst();
        Inventory inventory = mapSetupInventory.getInventory();

        assertEquals(inventory.getWindowId(), windowPacket.windowId());
        assertEquals(inventory.getTitle(), windowPacket.title());

        assertNotNull(mapSetupInventory.getDataLayout());

        int slot = InventoryType.CHEST_1_ROW.getSize();
        ISlot mapSlot = mapSetupInventory.getDataLayout().getSlot(slot);

        assertNotNull(mapSlot, "The slot must not be null");

        ItemStack invStack = itemsPackets.getLast().items().get(slot);

        assertNotNull(invStack, "The invStack must not be null");
        assertNotEquals(ItemStack.AIR, invStack);
        assertEquals(mapSlot.getItem().material(), invStack.material());

        env.destroyInstance(instance, true);
    }

}
package net.theevilreaper.tamias.setup.inventory;

import net.minestom.server.inventory.AbstractInventory;
import net.theevilreaper.aves.inventory.InventoryLayout;
import net.theevilreaper.aves.inventory.slot.ISlot;
import net.theevilreaper.aves.util.functional.PlayerConsumer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.server.play.OpenWindowPacket;
import net.minestom.server.network.packet.server.play.WindowItemsPacket;
import net.minestom.testing.Collector;
import net.minestom.testing.Env;
import net.minestom.testing.TestConnection;
import net.minestom.testing.extension.MicrotusExtension;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MicrotusExtension.class)
class ConfirmInventoryIntegrationTest {

    @Test
    void testConfirmInventory(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        TestConnection connection = env.createConnection();
        Player player = connection.connect(instance, Pos.ZERO);
        PlayerConsumer consumer = pConsumer -> {
            assertEquals(player, pConsumer);
        };
        ConfirmInventory confirmInventory = new ConfirmInventory(consumer);

        Collector<OpenWindowPacket> windowPacketCollector = connection.trackIncoming(OpenWindowPacket.class);
        Collector<WindowItemsPacket> windowItemsPacketCollector = connection.trackIncoming(WindowItemsPacket.class);

        player.openInventory(confirmInventory.getInventory());

        windowPacketCollector.assertSingle();
        windowItemsPacketCollector.assertSingle();

        AbstractInventory inventory = player.getOpenInventory();

        assertNotNull(inventory);

        List<OpenWindowPacket> packets = windowPacketCollector.collect();
        List<WindowItemsPacket> itemsPackets = windowItemsPacketCollector.collect();

        windowPacketCollector.assertCount(1);
        windowItemsPacketCollector.assertCount(1);

        assertEquals(1, packets.size(), "The packet size must be at least 1");
        assertEquals(1, itemsPackets.size(), "There should only be one item list");

        OpenWindowPacket windowPacket = packets.getFirst();

        assertEquals(inventory.getWindowId(), windowPacket.windowId());
        //assertEquals(inventory.getTitle(), windowPacket.title());

        int size = confirmInventory.getType().getSize();

        InventoryLayout layout = confirmInventory.getLayout();
        List<ItemStack> packetItems = itemsPackets.getFirst().items();
        assertNotNull(layout);
        assertNotNull(packetItems);

        assertEquals(size, packetItems.size());
        assertEquals(size, layout.getSize());

        for (int i = 0; i < size; i++) {
            ISlot originalSlot = layout.getSlot(i);
            assertNotNull(originalSlot);

            ItemStack packetItem = packetItems.get(i);
            assertNotNull(packetItem);
            assertEquals(originalSlot.getItem().material(), packetItem.material());
        }

        env.destroyInstance(instance, true);
        confirmInventory.unregister();
    }
}

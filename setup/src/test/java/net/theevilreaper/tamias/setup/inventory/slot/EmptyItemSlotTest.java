package net.theevilreaper.tamias.setup.inventory.slot;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.minestom.server.component.DataComponents;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.testing.Env;
import net.minestom.testing.extension.MicrotusExtension;
import net.theevilreaper.tamias.setup.dialog.event.PlayerDialogRequestEvent;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MicrotusExtension.class)
class EmptyItemSlotTest {

    @Test
    void testEmptySlot(@NotNull Env env) {
        EmptyItemSlot emptyItemSlot = new EmptyItemSlot(PlayerDialogRequestEvent.Target.SETUP_NAME);

        assertNotNull(emptyItemSlot);
        assertNotNull(emptyItemSlot.getClick(), "The click can not be null");
        assertNotNull(emptyItemSlot.getItem(), "The item can not be null");

        ItemStack slotItem = emptyItemSlot.getItem();
        assertEquals(Material.BARRIER, slotItem.material(), "The material must be BARRIER");

        assertTrue(slotItem.has(DataComponents.CUSTOM_NAME));
        Component displayName = slotItem.get(DataComponents.CUSTOM_NAME);
        assertNotNull(displayName);

        String data = PlainTextComponentSerializer.plainText().serialize(displayName);
        assertEquals("No data set!", data);
    }
}

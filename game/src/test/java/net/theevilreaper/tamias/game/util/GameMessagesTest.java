package net.theevilreaper.tamias.game.util;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GameMessagesTest {

    @Test
    void mapBuildingMessageIsPrefixed() {
        assertEquals("SuicideTNT » The map is being built...",
                PlainTextComponentSerializer.plainText().serialize(GameMessages.MAP_BUILDING));
    }
}

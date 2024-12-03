package net.theevilreaper.tamias.setup.commands.type;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class SpawnTypeTest {

    @Test
    void testInvalidFromString() {
        assertNull(SpawnType.fromString("edge"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"spawn", "spectator", "bomber", "survivor"})
    void testValidFromString(@NotNull String name) {
        assertNotNull(SpawnType.fromString(name));
    }

    @Test
    void  testGetAsArray() {
        String[] array = SpawnType.getAsArray();
        assertEquals(4, array.length);
    }

}
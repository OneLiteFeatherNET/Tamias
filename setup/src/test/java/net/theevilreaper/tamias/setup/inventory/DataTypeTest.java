package net.theevilreaper.tamias.setup.inventory;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class DataTypeTest {

    @ParameterizedTest(name = "Test valid ordinal value {0}")
    @ValueSource(ints = {0,1,2,3, 4})
    void testFromOrdinal(int value) {
        assertNotNull(DataType.fromOrdinal(value));
    }

    @ParameterizedTest(name = "Test invalid ordinal value {0}")
    @ValueSource(ints = {-1, 5})
    void testInvalidFromOrdinal(int value) {
        assertNull(DataType.fromOrdinal(value));
    }
}

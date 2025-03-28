package net.theevilreaper.tamias.setup.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FormatHelperTest {

    @Test
    void testFormat() {
        String formatted = FormatHelper.DECIMAL_FORMAT.format(1.23);
        assertEquals("1.23", formatted);
    }
}
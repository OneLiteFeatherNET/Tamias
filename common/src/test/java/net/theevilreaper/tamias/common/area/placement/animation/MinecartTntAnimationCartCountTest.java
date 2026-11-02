package net.theevilreaper.tamias.common.area.placement.animation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MinecartTntAnimationCartCountTest {

    @Test
    void testOnePositionUsesOneCart() {
        assertEquals(1, MinecartTntAnimation.cartCount(1));
    }

    @Test
    void testTenPositionsUseOneCart() {
        assertEquals(1, MinecartTntAnimation.cartCount(10));
    }

    @Test
    void testElevenPositionsUseTwoCarts() {
        assertEquals(2, MinecartTntAnimation.cartCount(11));
    }

    @Test
    void testFortyPositionsUseFourCarts() {
        assertEquals(4, MinecartTntAnimation.cartCount(40));
    }

    @Test
    void testOneHundredPositionsAreCappedAtFourCarts() {
        assertEquals(4, MinecartTntAnimation.cartCount(100));
    }
}

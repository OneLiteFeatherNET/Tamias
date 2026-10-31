package net.theevilreaper.tamias.game.projectile;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ArrowProjectileTest {

    @Test
    void testDamageRoundsUpTheScaledSpeed() {
        assertEquals(1, ArrowProjectile.damageFor(0.3), "0.3 blocks per tick times the base damage 2.0 rounds up to 1");
    }

    @Test
    void testDamageIsCappedAtTheIntegerLimit() {
        assertEquals(Integer.MAX_VALUE, ArrowProjectile.damageFor(1e12), "Huge speeds should clamp to the integer limit");
    }
}

package net.theevilreaper.tamias.common.firework;

import net.kyori.adventure.text.format.TextColor;
import net.minestom.server.item.component.FireworkExplosion;
import net.minestom.server.item.component.FireworkList;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FireworkEffectTest {

    @Test
    void testBuilderUsageFlow() {
        FireworkEffect builder = FireworkEffect.builder();
        assertNotNull(builder);

        builder.flightDuration((byte) 1)
                .effect(fireworkEffectDataBuilder -> {
                   fireworkEffectDataBuilder.shape(FireworkExplosion.Shape.BURST)
                           .trail()
                           .fade(TextColor.color(10, 10, 10));
                });
        FireworkList fireworkList = builder.build();
        assertNotNull(fireworkList);

        assertEquals(1, fireworkList.flightDuration());
        assertEquals(1, fireworkList.explosions().size());

        FireworkExplosion explosion = fireworkList.explosions().getFirst();
        assertNotNull(explosion);
        assertNotEquals(FireworkExplosion.Shape.LARGE_BALL, explosion.shape());
        assertTrue(explosion.hasTrail());
        assertFalse(explosion.hasTwinkle());
        assertEquals(TextColor.color(10, 10, 10), explosion.fadeColors().getFirst());
        assertTrue(explosion.colors().isEmpty());
    }
}

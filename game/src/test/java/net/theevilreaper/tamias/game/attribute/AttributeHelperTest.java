package net.theevilreaper.tamias.game.attribute;

import net.minestom.server.entity.Player;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.instance.Instance;
import net.minestom.testing.Env;
import net.minestom.testing.extension.MicrotusExtension;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MicrotusExtension.class)
class AttributeHelperTest {

    @Test
    void testMovementDisable(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        Player player = env.createPlayer(instance);
        double speed = player.getAttribute(Attribute.MOVEMENT_SPEED).getBaseValue();

        AttributeHelper.disableMovement(player);
        assertEquals(0.0, player.getAttribute(Attribute.MOVEMENT_SPEED).getValue());
        assertEquals(0.0, player.getAttribute(Attribute.JUMP_STRENGTH).getValue());
        assertEquals(speed, player.getAttribute(Attribute.MOVEMENT_SPEED).getBaseValue());

        env.destroyInstance(instance, true);
    }

    @Test
    void testMovementEnable(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        Player player = env.createPlayer(instance);
        double speed = player.getAttribute(Attribute.MOVEMENT_SPEED).getValue();

        AttributeHelper.disableMovement(player);
        AttributeHelper.disableMovement(player);
        assertEquals(0.0, player.getAttribute(Attribute.MOVEMENT_SPEED).getValue());

        AttributeHelper.enableMovement(player);
        assertEquals(speed, player.getAttribute(Attribute.MOVEMENT_SPEED).getValue());
        assertTrue(player.getAttribute(Attribute.MOVEMENT_SPEED).modifiers().isEmpty());
        assertTrue(player.getAttribute(Attribute.JUMP_STRENGTH).modifiers().isEmpty());

        env.destroyInstance(instance, true);
    }

}
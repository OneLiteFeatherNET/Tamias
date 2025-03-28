package net.theevilreaper.tamias.setup.util;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.Direction;
import net.minestom.testing.Env;
import net.minestom.testing.TestConnection;
import net.minestom.testing.extension.MicrotusExtension;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MicrotusExtension.class)
class DirectionUtilIntegrationTest {

    @ParameterizedTest(name = "Test valid direction mapping from pitch {0}")
    @ValueSource(floats = {-51, 51})
    void testInvalidDirectionMappingFromYaw(float pitch, @NotNull Env env) {
        Instance instance = env.createFlatInstance();
        TestConnection connection = env.createConnection();
        Pos spawnPos = Pos.ZERO.withPitch(pitch);
        Player player = connection.connect(instance, spawnPos).join();

        assertNotNull(player);
        assertEquals(spawnPos, player.getPosition(), "The player should be spawned at " + spawnPos);

        Optional<Direction> direction = DirectionUtil.parseDirection(player);
        assertFalse(direction.isPresent(), "The direction should not be present");

        env.destroyInstance(instance, true);
    }

    @Test
    void testValidDirectionMapping(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        TestConnection connection = env.createConnection();
        Pos spawnPos = Pos.ZERO.withYaw(0);
        Player player = connection.connect(instance, spawnPos).join();

        assertNotNull(player);
        assertEquals(spawnPos, player.getPosition(), "The player should be spawned at " + spawnPos);

        Optional<Direction> fetchedDirection = DirectionUtil.parseDirection(player);
        assertTrue(fetchedDirection.isPresent(), "The direction should be present");

        Direction direction = fetchedDirection.get();
        assertEquals(Direction.SOUTH, direction, "The direction should be " + Direction.SOUTH);
        env.destroyInstance(instance, true);
    }
}

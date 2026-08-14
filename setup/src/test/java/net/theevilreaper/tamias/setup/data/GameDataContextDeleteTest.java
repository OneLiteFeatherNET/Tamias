package net.theevilreaper.tamias.setup.data;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.testing.Env;
import net.minestom.testing.extension.MicrotusExtension;
import net.theevilreaper.tamias.common.map.builder.GameMapBuilder;
import net.theevilreaper.tamias.setup.MapDataTestBase;
import net.theevilreaper.tamias.setup.map.MapDataCategory;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(MicrotusExtension.class)
class GameDataContextDeleteTest extends MapDataTestBase {

    @Test
    void testHandleDataContextDeleteIgnoresStalePoint(@NotNull Env env) {
        Player player = env.createPlayer(env.createFlatInstance());

        GameData gameData = (GameData) SetupDataFactory.create(player, testMapEntry, false);
        gameData.setPosition(MapDataCategory.BOMBER_SPAWN, player);

        GameMapBuilder builder = (GameMapBuilder) gameData.getMapBuilder();
        Pos capturedPos = builder.getBomberInitialSpawn();
        assertNotNull(capturedPos, "position must be captured before the delete guard can be tested");

        Pos stalePoint = new Pos(capturedPos.x() + 50, capturedPos.y(), capturedPos.z());
        gameData.handleDataContextDelete(MapDataCategory.BOMBER_SPAWN, stalePoint);
        assertNotNull(builder.getBomberInitialSpawn(), "a delete request for a point that no longer matches must be ignored");

        gameData.handleDataContextDelete(MapDataCategory.BOMBER_SPAWN, capturedPos);
        assertNull(builder.getBomberInitialSpawn(), "a delete request for the currently stored point must remove it");

        gameData.reset();
    }
}

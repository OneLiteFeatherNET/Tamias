package net.theevilreaper.tamias.setup.data;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.minestom.testing.Env;
import net.minestom.testing.extension.MicrotusExtension;
import net.theevilreaper.tamias.common.map.builder.GameMapBuilder;
import net.theevilreaper.tamias.setup.MapDataTestBase;
import net.theevilreaper.tamias.setup.map.MapDataCategory;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MicrotusExtension.class)
class GameDataAreaPositionTest extends MapDataTestBase {

    @Test
    void testAreaCornersSnapToTheBlockThePlayerStandsOn(@NotNull Env env) {
        Player player = env.createPlayer(env.createFlatInstance());

        GameData gameData = (GameData) SetupDataFactory.create(player, testMapEntry, false);
        gameData.setPosition(MapDataCategory.AREA_LOWER_CORNER, player);
        gameData.setPosition(MapDataCategory.AREA_UPPER_CORNER, player);

        GameMapBuilder builder = (GameMapBuilder) gameData.getMapBuilder();
        int expectedY = player.getPosition().blockY() - 1;

        assertEquals(expectedY, (int) builder.getAreaDataBuilder().lowerCorner().y());
        assertEquals(expectedY, (int) builder.getAreaDataBuilder().upperCorner().y());

        gameData.reset();
    }

    @Test
    void testUpperCornerTakesTheHeightOfTheLowerCorner(@NotNull Env env) {
        Player player = env.createPlayer(env.createFlatInstance(), new Pos(0, 42, 0));

        GameData gameData = (GameData) SetupDataFactory.create(player, testMapEntry, false);
        gameData.setPosition(MapDataCategory.AREA_LOWER_CORNER, player);

        player.refreshPosition(new Pos(10, 45, 10));
        gameData.setPosition(MapDataCategory.AREA_UPPER_CORNER, player);

        GameMapBuilder builder = (GameMapBuilder) gameData.getMapBuilder();
        assertEquals(new Vec(0, 41, 0), builder.getAreaDataBuilder().lowerCorner());
        assertEquals(new Vec(10, 41, 10), builder.getAreaDataBuilder().upperCorner());

        gameData.reset();
    }

    @Test
    void testCornerUsesTheBlockBelowOnPartialBlocks(@NotNull Env env) {
        Player player = env.createPlayer(env.createFlatInstance(), new Pos(0, 41.5, 0));

        GameData gameData = (GameData) SetupDataFactory.create(player, testMapEntry, false);
        gameData.setPosition(MapDataCategory.AREA_LOWER_CORNER, player);

        GameMapBuilder builder = (GameMapBuilder) gameData.getMapBuilder();
        assertEquals(41, builder.getAreaDataBuilder().lowerCorner().blockY());

        gameData.reset();
    }
}

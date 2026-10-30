package net.theevilreaper.tamias.setup.listener;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.testing.Env;
import net.minestom.testing.extension.MicrotusExtension;
import net.onelitefeather.guira.SetupDataService;
import net.theevilreaper.tamias.common.map.builder.GameMapBuilder;
import net.theevilreaper.tamias.setup.MapDataTestBase;
import net.theevilreaper.tamias.setup.data.GameData;
import net.theevilreaper.tamias.setup.data.SetupDataFactory;
import net.theevilreaper.tamias.setup.event.PositionSetEvent;
import net.theevilreaper.tamias.setup.map.MapDataCategory;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(MicrotusExtension.class)
class PositionSetListenerTest extends MapDataTestBase {

    @Test
    void testAreaCornerIsIgnoredWhileInAir(@NotNull Env env) {
        Player player = env.createPlayer(env.createFlatInstance(), new Pos(0, 50, 0));
        player.refreshOnGround(false);

        GameData gameData = (GameData) SetupDataFactory.create(player, testMapEntry, false);
        SetupDataService dataService = SetupDataService.create();
        dataService.add(player.getUuid(), gameData);

        PositionSetListener listener = new PositionSetListener(dataService);
        listener.accept(new PositionSetEvent(player, player.getPosition(), MapDataCategory.AREA_LOWER_CORNER));

        GameMapBuilder builder = (GameMapBuilder) gameData.getMapBuilder();
        assertNull(builder.getAreaDataBuilder().lowerCorner());

        gameData.reset();
    }

    @Test
    void testAreaCornerIsSetOnGround(@NotNull Env env) {
        Player player = env.createPlayer(env.createFlatInstance(), new Pos(0, 42, 0));
        player.refreshOnGround(true);

        GameData gameData = (GameData) SetupDataFactory.create(player, testMapEntry, false);
        SetupDataService dataService = SetupDataService.create();
        dataService.add(player.getUuid(), gameData);

        PositionSetListener listener = new PositionSetListener(dataService);
        listener.accept(new PositionSetEvent(player, player.getPosition(), MapDataCategory.AREA_LOWER_CORNER));

        GameMapBuilder builder = (GameMapBuilder) gameData.getMapBuilder();
        assertNotNull(builder.getAreaDataBuilder().lowerCorner());

        gameData.reset();
    }
}

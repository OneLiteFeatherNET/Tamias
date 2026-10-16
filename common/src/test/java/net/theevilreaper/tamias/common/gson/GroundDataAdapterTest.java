package net.theevilreaper.tamias.common.gson;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minestom.server.instance.block.Block;
import net.theevilreaper.tamias.common.ground.GroundData;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GroundDataAdapterTest {

    @Test
    void testDeserializeWithAdditionalBlocks() {
        String json = """
                { "ground": "minecraft:sand", "additional": ["minecraft:red_sand", "minecraft:sandstone"] }
                """;
        GroundData groundData = GsonUtil.GSON.fromJson(json, GroundData.class);
        assertEquals(Block.SAND, groundData.groundBlock());
        assertEquals(List.of(Block.RED_SAND, Block.SANDSTONE), groundData.additionalBlocks());
    }

    @Test
    void testDeserializeWithoutAdditionalBlocks() {
        GroundData groundData = GsonUtil.GSON.fromJson("{ \"ground\": \"minecraft:ice\" }", GroundData.class);
        assertEquals(Block.ICE, groundData.groundBlock());
        assertFalse(groundData.hasAdditionalBlocks());
    }

    @Test
    void testSerializeRoundTrip() {
        GroundData groundData = new GroundData(Block.GRASS_BLOCK, List.of(Block.MOSS_BLOCK));
        JsonObject object = GsonUtil.GSON.toJsonTree(groundData).getAsJsonObject();
        assertEquals("minecraft:grass_block", object.get("ground").getAsString());
        assertEquals("minecraft:moss_block", object.getAsJsonArray("additional").get(0).getAsString());

        GroundData parsed = GsonUtil.GSON.fromJson(object, GroundData.class);
        assertEquals(groundData, parsed);
    }

    @Test
    void testSerializeOmitsEmptyAdditionalBlocks() {
        JsonObject object = GsonUtil.GSON.toJsonTree(new GroundData(Block.ICE, null)).getAsJsonObject();
        assertFalse(object.has("additional"));
    }

    @Test
    void testUnknownBlockFails() {
        assertThrows(
                JsonParseException.class,
                () -> GsonUtil.GSON.fromJson("{ \"ground\": \"minecraft:not_a_block\" }", GroundData.class)
        );
    }

    @Test
    void testMissingGroundFails() {
        assertThrows(
                JsonParseException.class,
                () -> GsonUtil.GSON.fromJson("{ \"additional\": [\"minecraft:sand\"] }", GroundData.class)
        );
    }

    @Test
    void testAirGroundFails() {
        assertThrows(
                JsonParseException.class,
                () -> GsonUtil.GSON.fromJson("{ \"ground\": \"minecraft:air\" }", GroundData.class)
        );
    }
}

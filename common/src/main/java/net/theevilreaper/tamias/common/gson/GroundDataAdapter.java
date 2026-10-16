package net.theevilreaper.tamias.common.gson;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import net.minestom.server.instance.block.Block;
import net.theevilreaper.tamias.common.ground.GroundData;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Converts a {@link GroundData} from and to its json representation.
 * Blocks are stored by their namespaced key, for example {@code minecraft:sand}.
 * A block which is not in its default state is stored with its properties, for example
 * {@code minecraft:redstone_lamp[lit=true]}.
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 */
public final class GroundDataAdapter implements JsonDeserializer<GroundData>, JsonSerializer<GroundData> {

    private static final String GROUND_KEY = "ground";
    private static final String ADDITIONAL_KEY = "additional";

    @Override
    public GroundData deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
        JsonObject object = jsonElement.getAsJsonObject();
        if (!object.has(GROUND_KEY)) {
            throw new JsonParseException("A ground preset requires a '" + GROUND_KEY + "' block");
        }
        Block groundBlock = parseBlock(object.get(GROUND_KEY).getAsString());

        List<Block> additionalBlocks = null;
        if (object.has(ADDITIONAL_KEY)) {
            JsonArray array = object.getAsJsonArray(ADDITIONAL_KEY);
            additionalBlocks = new ArrayList<>(array.size());
            for (JsonElement element : array) {
                additionalBlocks.add(parseBlock(element.getAsString()));
            }
        }

        try {
            return new GroundData(groundBlock, additionalBlocks == null ? null : List.copyOf(additionalBlocks));
        } catch (IllegalArgumentException exception) {
            throw new JsonParseException("Invalid ground preset: " + exception.getMessage(), exception);
        }
    }

    @Override
    public JsonElement serialize(GroundData groundData, Type type, JsonSerializationContext context) {
        JsonObject object = new JsonObject();
        object.addProperty(GROUND_KEY, writeBlock(groundData.groundBlock()));
        if (groundData.hasAdditionalBlocks()) {
            JsonArray array = new JsonArray();
            for (Block block : groundData.additionalBlocks()) {
                array.add(writeBlock(block));
            }
            object.add(ADDITIONAL_KEY, array);
        }
        return object;
    }

    private static String writeBlock(Block block) {
        Block defaultBlock = Block.fromKey(block.key());
        if (defaultBlock != null && defaultBlock.stateId() == block.stateId()) {
            return block.key().asString();
        }
        return block.state();
    }

    private static Block parseBlock(String value) {
        Block block;
        try {
            block = value.indexOf('[') >= 0 ? Block.fromState(value) : Block.fromKey(value);
        } catch (IllegalArgumentException exception) {
            throw new JsonParseException("Invalid block state '" + value + "' in ground preset", exception);
        }
        if (block == null) {
            throw new JsonParseException("Unknown block '" + value + "' in ground preset");
        }
        return block;
    }
}

package net.theevilreaper.tamias.common.gson;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.utils.Direction;
import net.theevilreaper.tamias.common.map.layer.GameAreaData;
import net.theevilreaper.tamias.common.util.DirectionFaceHelper;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;

public final class GameAreaAdapter implements JsonDeserializer<GameAreaData>, JsonSerializer<GameAreaData> {

    @Override
    public GameAreaData deserialize(@NotNull JsonElement jsonElement, @NotNull Type type, @NotNull JsonDeserializationContext context) throws JsonParseException {
        JsonObject object = jsonElement.getAsJsonObject();
        Vec lowerCorner = context.deserialize(object.get("lowerCorner"), Vec.class);
        Vec upperCorner = context.deserialize(object.get("upperCorner"), Vec.class);
        String facing = object.get("facing").getAsString();
        Direction direction = DirectionFaceHelper.parseDirection(facing);
        return GameAreaData.builder().lowerCorner(lowerCorner).upperCorner(upperCorner).facing(direction).build();
    }

    @Override
    public JsonElement serialize(@NotNull GameAreaData gameAreaData, @NotNull Type type, @NotNull JsonSerializationContext context) {
        JsonObject object = new JsonObject();
        object.add("lowerCorner", context.serialize(gameAreaData.lowerCorner(), Vec.class));
        object.add("upperCorner", context.serialize(gameAreaData.upperCorner(), Vec.class));
        object.addProperty("facing", gameAreaData.facing().name());
        return object;
    }
}

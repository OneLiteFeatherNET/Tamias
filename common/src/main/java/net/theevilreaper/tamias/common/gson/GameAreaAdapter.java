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
import net.theevilreaper.tamias.common.map.layer.AreaData;
import net.theevilreaper.tamias.common.util.DirectionFaceHelper;

import java.lang.reflect.Type;

public final class GameAreaAdapter implements JsonDeserializer<AreaData>, JsonSerializer<AreaData> {

    @Override
    public AreaData deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
        JsonObject object = jsonElement.getAsJsonObject();
        Vec lowerCorner = context.deserialize(object.get("lowerCorner"), Vec.class);
        Vec upperCorner = context.deserialize(object.get("upperCorner"), Vec.class);
        String facing = object.get("facing").getAsString();
        Direction direction = DirectionFaceHelper.parseDirection(facing);
        return AreaData.builder().lowerCorner(lowerCorner).upperCorner(upperCorner).facing(direction).build();
    }

    @Override
    public JsonElement serialize(AreaData areaData, Type type, JsonSerializationContext context) {
        JsonObject object = new JsonObject();
        object.add("lowerCorner", context.serialize(areaData.lowerCorner(), Vec.class));
        object.add("upperCorner", context.serialize(areaData.upperCorner(), Vec.class));
        object.addProperty("facing", areaData.facing().name());
        return object;
    }
}

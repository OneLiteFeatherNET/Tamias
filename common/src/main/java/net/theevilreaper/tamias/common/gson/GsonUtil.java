package net.theevilreaper.tamias.common.gson;

import com.google.gson.Gson;
import de.icevizion.aves.file.gson.PositionGsonAdapter;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.theevilreaper.tamias.common.map.layer.GameAreaData;

public final class GsonUtil {

    public static final Gson GSON;

    static {
        PositionGsonAdapter positionGsonAdapter = new PositionGsonAdapter();
        GSON = new Gson().newBuilder()
                .registerTypeAdapter(Pos.class, positionGsonAdapter)
                .registerTypeAdapter(Vec.class, positionGsonAdapter)
                .registerTypeAdapter(GameAreaData.class, new GameAreaAdapter())
                .create();
    }

    private GsonUtil() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }
}

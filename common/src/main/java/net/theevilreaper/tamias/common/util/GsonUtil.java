package net.theevilreaper.tamias.common.util;

import com.google.gson.Gson;
import de.icevizion.aves.file.gson.PositionGsonAdapter;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;

public class GsonUtil {

    public static final Gson GSON;

    static {
        PositionGsonAdapter positionGsonAdapter = new PositionGsonAdapter();
        GSON = new Gson().newBuilder()
                .registerTypeAdapter(Pos.class, positionGsonAdapter)
                .registerTypeAdapter(Vec.class, positionGsonAdapter)
                .create();
    }

    private GsonUtil() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }
}

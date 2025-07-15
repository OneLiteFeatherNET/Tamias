package net.theevilreaper.tamias.common.gson;

import com.google.gson.Gson;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.theevilreaper.aves.file.gson.PositionGsonAdapter;
import net.theevilreaper.tamias.common.area.GameArea;
import net.theevilreaper.tamias.common.map.layer.AreaData;

/**
 * The utility class provides a {@link Gson} instance with some custom adapters.
 * The class cannot be instantiated.
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @see Gson
 * @since 1.0.0
 */
public final class GsonUtil {

    public static final Gson GSON;

    static {
        PositionGsonAdapter positionGsonAdapter = new PositionGsonAdapter();
        GSON = new Gson().newBuilder()
                .registerTypeAdapter(Pos.class, positionGsonAdapter)
                .registerTypeAdapter(Vec.class, positionGsonAdapter)
                .registerTypeAdapter(AreaData.class, new GameAreaAdapter())
                .create();
    }

    /**
     * Prevent instantiation of this class.
     */
    private GsonUtil() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }
}

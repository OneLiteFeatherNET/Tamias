package net.theevilreaper.tamias.common.ground;

import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@SuppressWarnings("java:S3252")
public final class TamiasGroundDataRegistry implements GroundDataRegistry {

    private final List<GroundData> groundData;
    private @Nullable GroundData lastData;

    TamiasGroundDataRegistry() {
        this.groundData = new ArrayList<>();
        this.initDefaults();
    }

    private void initDefaults() {
        this.groundData.add(new GroundData(Block.ORANGE_TERRACOTTA, null, Block.YELLOW_TERRACOTTA));
        this.groundData.add(new GroundData(Block.ICE, null, Block.PACKED_ICE));
        this.groundData.add(new GroundData(Block.GREEN_TERRACOTTA, null, Block.LIME_TERRACOTTA));
        this.groundData.add(new GroundData(Block.RED_TERRACOTTA, null, Block.PINK_TERRACOTTA));
    }

    @Override
    public void add(GroundData groundData) {
        this.groundData.add(groundData);
    }

    @Override
    public GroundData getRandomData() {
        List<GroundData> candidates = new ArrayList<>(this.groundData);
        // The same ground shouldn't be used twice in a row, if there is another one
        if (candidates.size() > 1) {
            candidates.remove(this.lastData);
        }
        int randomId = ThreadLocalRandom.current().nextInt(0, candidates.size());
        this.lastData = candidates.get(randomId);
        return this.lastData;
    }

    @Override
    public @UnmodifiableView List<GroundData> getGroundData() {
        return Collections.unmodifiableList(this.groundData);
    }

    static final class RegistryInstance {

        static final GroundDataRegistry INSTANCE;

        static {
            INSTANCE = new TamiasGroundDataRegistry();
        }

        private RegistryInstance() {
            throw new UnsupportedOperationException("This class cannot be instantiated");
        }
    }
}

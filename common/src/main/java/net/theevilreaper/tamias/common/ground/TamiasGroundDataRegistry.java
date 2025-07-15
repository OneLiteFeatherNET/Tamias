package net.theevilreaper.tamias.common.ground;

import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@SuppressWarnings("java:S3252")
public final class TamiasGroundDataRegistry implements GroundDataRegistry {

    private final List<GroundData> groundData;

    TamiasGroundDataRegistry() {
        this.groundData = new ArrayList<>();
        this.initDefaults();
    }

    private void initDefaults() {
        this.groundData.add(new GroundData(Block.ORANGE_TERRACOTTA, null));
        this.groundData.add(new GroundData(Block.ICE, null));
        this.groundData.add(new GroundData(Block.GREEN_TERRACOTTA, null));
        this.groundData.add(new GroundData(Block.RED_TERRACOTTA, null));
    }

    @Override
    public void add(@NotNull GroundData groundData) {
        this.groundData.add(groundData);
    }

    @Override
    public @NotNull GroundData getRandomData() {
        Collections.shuffle(this.groundData);
        int randomId = ThreadLocalRandom.current().nextInt(0, this.groundData.size());
        return this.groundData.get(randomId);
    }

    @Override
    public @NotNull @UnmodifiableView List<GroundData> getGroundData() {
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

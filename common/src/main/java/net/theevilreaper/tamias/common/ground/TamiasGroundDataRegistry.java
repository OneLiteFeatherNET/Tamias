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

    /**
     * Adds the default grounds. The additional blocks are mostly scattered as accents, so they don't change how
     * the ground plays: blocks which change the movement or deal damage (e.g. soul sand, slime, magma) are left out.
     * Some grounds also get a {@link SpeedBlock} which matches the ground, the others stay without one on purpose.
     */
    private void initDefaults() {
        this.groundData.add(new GroundData(Block.ORANGE_TERRACOTTA, List.of(SpeedBlock.of(Block.ORANGE_CONCRETE))));
        this.groundData.add(new GroundData(Block.ICE, List.of(SpeedBlock.of(Block.PACKED_ICE))));
        this.groundData.add(new GroundData(Block.GREEN_TERRACOTTA, List.of(SpeedBlock.of(Block.GREEN_CONCRETE))));
        this.groundData.add(new GroundData(Block.RED_TERRACOTTA, List.of(SpeedBlock.of(Block.RED_CONCRETE))));
        this.groundData.add(new GroundData(Block.WHITE_TERRACOTTA, List.of(Block.LIGHT_BLUE_GLAZED_TERRACOTTA, Block.YELLOW_GLAZED_TERRACOTTA,
                SpeedBlock.of(Block.LIGHT_BLUE_CONCRETE))));
        this.groundData.add(new GroundData(Block.MOSS_BLOCK, List.of(Block.MOSSY_COBBLESTONE, Block.MOSSY_STONE_BRICKS)));
        this.groundData.add(new GroundData(Block.SANDSTONE, List.of(Block.CHISELED_SANDSTONE, Block.CUT_SANDSTONE)));
        this.groundData.add(new GroundData(Block.QUARTZ_BLOCK, List.of(Block.CHISELED_QUARTZ_BLOCK, Block.QUARTZ_PILLAR)));
        this.groundData.add(new GroundData(Block.PRISMARINE_BRICKS, List.of(Block.DARK_PRISMARINE, Block.PRISMARINE,
                SpeedBlock.of(Block.SEA_LANTERN))));
        this.groundData.add(new GroundData(Block.DEEPSLATE_TILES, List.of(Block.CRACKED_DEEPSLATE_TILES, Block.DEEPSLATE_BRICKS)));
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

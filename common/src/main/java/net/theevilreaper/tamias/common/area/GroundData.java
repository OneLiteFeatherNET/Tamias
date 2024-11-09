package net.theevilreaper.tamias.common.area;

import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@SuppressWarnings("java:S3252")
public record GroundData(@NotNull Block groundBlock, @Nullable List<Block> additionalBlocks) {

    public boolean hasAdditionalBlocks() {
        return additionalBlocks != null && !additionalBlocks.isEmpty();
    }
}

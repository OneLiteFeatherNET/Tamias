package net.theevilreaper.tamias.common.firework;

import net.minestom.server.item.component.FireworkExplosion;
import net.minestom.server.item.component.FireworkList;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class FireworkEffectBuilder implements FireworkEffect {

    private final List<FireworkExplosion> fireworkExplosions;
    private byte flightDuration;

    FireworkEffectBuilder() {
        this.fireworkExplosions = new ArrayList<>();
    }

    @Override
    public @NotNull FireworkEffect flightDuration(byte duration) {
        this.flightDuration = duration;
        return this;
    }

    @Override
    public @NotNull FireworkEffect effect(@NotNull FireworkExplosion fireworkExplosion) {
        this.fireworkExplosions.add(fireworkExplosion);
        return this;
    }

    @Override
    public @NotNull FireworkEffect effect(@NotNull Consumer<FireworkEffectDataBuilder> builder) {
        FireworkEffectDataBuilder dataBuilder = FireworkEffectDataBuilder.builder();
        builder.accept(dataBuilder);
        this.fireworkExplosions.add(dataBuilder.build());
        return this;
    }

    @Override
    public @NotNull FireworkEffect effects(@NotNull List<FireworkExplosion> fireworkExplosion) {
        this.fireworkExplosions.addAll(fireworkExplosion);
        return this;
    }

    @Override
    public @NotNull FireworkList build() {
        return new FireworkList(this.flightDuration, this.fireworkExplosions);
    }
}

package net.theevilreaper.tamias.common.firework;

import net.kyori.adventure.util.RGBLike;
import net.minestom.server.item.component.FireworkExplosion;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public final class FireworkEffectDataBuilderImpl implements FireworkEffectDataBuilder {

    private final List<RGBLike> colors;
    private final List<RGBLike> fades;

    private FireworkExplosion.Shape shape;
    private boolean trail;
    private boolean twinkle;

    FireworkEffectDataBuilderImpl() {
        this.colors = new ArrayList<>();
        this.fades = new ArrayList<>();
    }

    @Override
    public @NotNull FireworkEffectDataBuilder shape(FireworkExplosion.@NotNull Shape shape) {
        this.shape = shape;
        return this;
    }

    @Override
    public @NotNull FireworkEffectDataBuilder color(@NotNull RGBLike color) {
        this.colors.add(color);
        return this;
    }

    @Override
    public @NotNull FireworkEffectDataBuilder fade(@NotNull RGBLike fade) {
        this.fades.add(fade);
        return this;
    }

    @Override
    public @NotNull FireworkEffectDataBuilder twinkle() {
        this.twinkle = !this.twinkle;
        return this;
    }

    @Override
    public @NotNull FireworkEffectDataBuilder trail() {
        this.trail = !this.trail;
        return this;
    }

    @Override
    public @NotNull FireworkExplosion build() {
        return new FireworkExplosion(this.shape, this.colors, this.fades, this.trail, this.twinkle);
    }
}

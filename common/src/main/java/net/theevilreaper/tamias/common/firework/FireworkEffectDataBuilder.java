package net.theevilreaper.tamias.common.firework;

import net.kyori.adventure.util.RGBLike;
import net.minestom.server.item.component.FireworkExplosion;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public sealed interface FireworkEffectDataBuilder permits FireworkEffectDataBuilderImpl {

    @Contract(pure = true)
    static @NotNull FireworkEffectDataBuilder builder() {
        return new FireworkEffectDataBuilderImpl();
    }

    @NotNull FireworkEffectDataBuilder shape(@NotNull FireworkExplosion.Shape shape);

    @NotNull FireworkEffectDataBuilder color(@NotNull RGBLike color);

    @NotNull FireworkEffectDataBuilder fade(@NotNull RGBLike fade);

    @NotNull FireworkEffectDataBuilder twinkle();

    @NotNull FireworkEffectDataBuilder trail();

    @NotNull FireworkExplosion build();
}

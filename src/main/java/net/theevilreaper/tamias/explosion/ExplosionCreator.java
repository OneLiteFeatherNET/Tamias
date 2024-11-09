package net.theevilreaper.tamias.explosion;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Explosion;
import net.minestom.server.instance.ExplosionSupplier;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public final class ExplosionCreator implements ExplosionSupplier {

    @Contract(value = "_, _, _, _, _ -> new", pure = true)
    @Override
    public Explosion createExplosion(float centerX, float centerY, float centerZ, float strength, CompoundBinaryTag additionalData) {
        return new Explosion(centerX, centerY, centerZ, strength) {
            @Override
            protected List<Point> prepare(@NotNull Instance instance) {
                return Collections.emptyList();
            }
        };
    }
}

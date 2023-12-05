package net.theevilreaper.tamias.explosion;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Explosion;
import net.minestom.server.instance.ExplosionSupplier;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

import java.util.Collections;
import java.util.List;

public final class ExplosionCreator implements ExplosionSupplier {

    @Contract(value = "_, _, _, _, _ -> new", pure = true)
    @Override
    public @NotNull Explosion createExplosion(float centerX, float centerY, float centerZ, float strength, NBTCompound additionalData) {
        return new Explosion(centerX, centerY, centerZ, strength) {
            @Override
            protected List<Point> prepare(@NotNull Instance instance) {
                return Collections.emptyList();
            }
        };
    }
}

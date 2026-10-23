package net.theevilreaper.tamias.common.explosion;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Explosion;
import net.minestom.server.instance.ExplosionSupplier;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.server.play.ExplosionPacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.utils.PacketSendingUtils;
import net.minestom.server.utils.WeightedList;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * Creates explosions which don't destroy any block.
 * Minestom always sends the small explosion particle, so the packet is sent here with the large one of a TNT.
 */
public final class ExplosionCreator implements ExplosionSupplier {

    @Contract(value = "_, _, _, _, _ -> new", pure = true)
    @Override
    public Explosion createExplosion(float centerX, float centerY, float centerZ, float strength, @Nullable CompoundBinaryTag additionalData) {
        return new Explosion(centerX, centerY, centerZ, strength) {
            @Override
            protected List<Point> prepare(Instance instance) {
                return Collections.emptyList();
            }

            @Override
            public void apply(Instance instance) {
                ExplosionPacket packet = new ExplosionPacket(
                        new Vec(getCenterX(), getCenterY(), getCenterZ()), getStrength(), 0, Vec.ZERO,
                        Particle.EXPLOSION_EMITTER, SoundEvent.ENTITY_GENERIC_EXPLODE, WeightedList.of()
                );
                PacketSendingUtils.sendGroupedPacket(instance.getPlayers(), packet);
            }
        };
    }
}

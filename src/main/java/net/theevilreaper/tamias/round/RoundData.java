package net.theevilreaper.tamias.round;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.potion.TimedPotion;
import net.minestom.server.utils.validate.Check;
import net.theevilreaper.tamias.config.GameConfig;
import net.theevilreaper.tamias.round.events.RoundFinishEvent;
import net.theevilreaper.tamias.util.Messages;
import org.jetbrains.annotations.NotNull;

import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author theEvilReaper
 * @version 1.0.0
 * @since
 **/

public final class RoundData {

    private final Lock lock;
    private List<Pos> spawnPositions;

    public RoundData() {
        this.lock = new ReentrantLock();
        this.spawnPositions = null;
    }

    public void setSpawnPositions(@NotNull List<Pos> positions) {
        Check.argCondition(positions.isEmpty(), "The spawn positions can't be empty");
        this.spawnPositions = positions;
    }

    public void decreaseTNTCount(@NotNull Player player) {
        if (this.spawnPositions == null) return;
        player.sendMessage(Messages.CHOOSING_NEW_TNT);
        if (!hasTNT()) {
            EventDispatcher.call(new RoundFinishEvent(GameConfig.SURVIVOR_ID));
            this.spawnPositions = null;
            return;
        }
        Pos pos = null;
        try {
            lock.lock();
            Collections.shuffle(spawnPositions);
            pos = spawnPositions.remove(0);
        } finally {
            lock.unlock();
        }

        final Pos copy = Pos.fromPoint(pos);

        MinecraftServer.getSchedulerManager().buildTask(
                () -> {
                    player.teleport(copy);
                    for (TimedPotion activeEffect : player.getActiveEffects()) {
                        player.removeEffect(activeEffect.getPotion().effect());
                    }
                }
        ).delay(2, ChronoUnit.SECONDS).schedule();

    }

    public boolean hasTNT() {
        return !this.spawnPositions.isEmpty();
    }
}

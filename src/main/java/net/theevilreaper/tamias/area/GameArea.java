package net.theevilreaper.tamias.area;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.timer.Task;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;

public final class GameArea {

    private final Instance instance;
    private final Vec start;
    private final Vec end;

    private final List<Vec> areaPositions;


    public GameArea(@NotNull Instance instance, @NotNull Vec start, @NotNull Vec end) {
        this.instance = instance;
        var distance = start.sub(end);
        Check.argCondition(distance.equals(Vec.ZERO), "NPE");
        this.start = start;
        this.end = end;
        this.areaPositions = new ArrayList<>();
        calculatePositions();

    }

    void calculatePositions() {
        for (int x = (int) start.x(); x < end.x(); x++) {
            for (int y = (int) start.y(); y < end.y(); y++) {
                for (int z = (int) start.z(); z < end.z(); z++) {
                    areaPositions.add(new Vec(x, y, z));
                }
            }
        }
    }

    private Task build() {
        var posList = new ArrayList<>(areaPositions);
        posList.sort(Comparator.comparing(pos -> {
            var x = pos.x();
            var z = pos.z();
            var y = pos.y();
            return Math.sqrt(x * x + z * z + y * y);
        }));
        return buildFromQueue(posList);
    }
    private Task buildFromQueue(List<Vec> posList) {

        var queue = new LinkedBlockingDeque<>(posList);
        return MinecraftServer.getSchedulerManager().buildTask(() -> {
            var pos = queue.poll();
            if (pos == null) {
                return;
            }
            for (Player onlinePlayer : MinecraftServer.getConnectionManager().getOnlinePlayers()) {
                float progress = (float) queue.size() / posList.size();
                onlinePlayer.setLevel(100 - (int) (progress * 100));
                onlinePlayer.setExp(1 - progress);
            }
        }).repeat(5, ChronoUnit.MILLIS).schedule();
    }

}

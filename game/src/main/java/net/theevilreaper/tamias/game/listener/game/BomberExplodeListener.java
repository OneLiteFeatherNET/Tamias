package net.theevilreaper.tamias.game.listener.game;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.sound.SoundEvent;
import net.theevilreaper.tamias.common.config.BomberAssistConfig;
import net.theevilreaper.tamias.common.config.GameConfig;
import net.theevilreaper.tamias.common.util.Tags;
import net.theevilreaper.tamias.game.player.BomberAssistState;
import net.theevilreaper.tamias.game.player.TamiasPlayer;
import net.theevilreaper.tamias.game.attribute.AttributeHelper;
import net.theevilreaper.tamias.game.event.BomberExplodeEvent;
import net.theevilreaper.tamias.game.team.TeamHelper;
import net.theevilreaper.tamias.game.util.EntityHelper;
import net.theevilreaper.tamias.game.util.GameMessages;
import net.theevilreaper.xerus.api.team.TeamService;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.LongSupplier;

/**
 * Handles the explosion of a bomber. Every survivor within {@link #INFECTION_RADIUS} becomes a bomber.
 * A bomber who was shot infects nobody, otherwise the shooter would be blown up by a close hit.
 * The bomber hears a hit sound and every player gets told who was blown up.
 * The converted survivors only spawn on a TNT when the round goes on, so the last survivor doesn't take a TNT.
 * <p>
 * An explosion that reaches nobody, or a shot bomber, counts as a near miss for the bomber assist when a survivor
 * was within the near miss distance. Each near miss grows the radius of the next explosion a bit, see
 * {@link BomberAssistConfig}. A grown explosion shows its radius as a ring, so the survivors see why they were hit.
 */
@SuppressWarnings("java:S3252")
public class BomberExplodeListener implements Consumer<BomberExplodeEvent> {

    private static final Potion BLINDNESS = new Potion(PotionEffect.BLINDNESS, (byte) 1, Integer.MAX_VALUE);
    static final Sound HIT_SOUND = Sound.sound(SoundEvent.ENTITY_ARROW_HIT_PLAYER, Sound.Source.MASTER, 1f, 1f);
    static final double INFECTION_RADIUS = 3.0;

    private static final int RING_POINTS_PER_BLOCK = 2;

    private final TeamService teamService;
    private final BooleanSupplier roundEndCheck;
    private final BomberAssistConfig assistConfig;
    private final LongSupplier clock;

    /**
     * Creates a new listener with the default assist settings.
     *
     * @param teamService   the team service which contains the survivor and the bomber team
     * @param roundEndCheck checks if the round can end after survivors were converted, returns true if it ended
     */
    public BomberExplodeListener(TeamService teamService, BooleanSupplier roundEndCheck) {
        this(teamService, roundEndCheck, BomberAssistConfig.DEFAULT, System::currentTimeMillis);
    }

    /**
     * Creates a new listener.
     *
     * @param teamService   the team service which contains the survivor and the bomber team
     * @param roundEndCheck checks if the round can end after survivors were converted, returns true if it ended
     * @param assistConfig  the settings of the bomber assist
     * @param clock         the current time in milliseconds, for the decay of the near misses
     */
    public BomberExplodeListener(TeamService teamService, BooleanSupplier roundEndCheck,
                                 BomberAssistConfig assistConfig, LongSupplier clock) {
        this.teamService = teamService;
        this.roundEndCheck = roundEndCheck;
        this.assistConfig = assistConfig;
        this.clock = clock;
    }

    @Override
    public void accept(BomberExplodeEvent event) {
        Player player = event.getPlayer();
        Instance instance = player.getInstance();
        Pos pos = player.getPosition().asPos();
        instance.explode((float) pos.x(), (float) pos.y(), (float) pos.z(), 1);
        EntityHelper.hide(player);
        player.addEffect(BLINDNESS);
        AttributeHelper.disableMovement(player);

        long now = this.clock.getAsLong();
        @Nullable BomberAssistState assist = player instanceof TamiasPlayer tamiasPlayer ? tamiasPlayer.getBomberAssist() : null;
        if (event.isShot()) {
            // A bomber who is shot right next to a survivor tried hard enough, being shot far away doesn't count
            recordNearMiss(instance, player, event.getPosition(), assist, now);
            return;
        }

        double radius = INFECTION_RADIUS + (assist == null ? 0 : assist.bonus(this.assistConfig, now));
        if (radius > INFECTION_RADIUS) {
            showRadius(instance, event.getPosition(), radius);
        }

        // Collect first, converting a survivor teleports them
        List<Player> survivors = nearbySurvivors(instance, player, event.getPosition(), radius);
        if (survivors.isEmpty()) {
            recordNearMiss(instance, player, event.getPosition(), assist, now);
            return;
        }
        if (assist != null) {
            assist.reset();
        }

        player.playSound(HIT_SOUND);
        // Announced before the conversion, so the survivor is still shown in the survivor color
        Audience players = Audience.audience(MinecraftServer.getConnectionManager().getOnlinePlayers());
        survivors.forEach(survivor -> players.sendMessage(GameMessages.getDeathMessage(survivor, player)));
        if (player instanceof TamiasPlayer bomber) {
            survivors.forEach(_ -> bomber.addKill());
        }
        survivors.forEach(survivor -> TeamHelper.moveToTNTTeam(this.teamService, survivor));
        // The round ends when the last survivor was blown up, nobody has to spawn on a TNT then
        if (this.roundEndCheck.getAsBoolean()) return;
        survivors.forEach(TeamHelper::spawnAsBomber);
    }

    /**
     * Counts a near miss for the bomber when a survivor was close to the explosion.
     */
    private void recordNearMiss(Instance instance, Player bomber, Point position, @Nullable BomberAssistState assist,
                                long now) {
        if (assist == null || !this.assistConfig.enabled()) return;
        if (nearbySurvivors(instance, bomber, position, this.assistConfig.nearMissDistance()).isEmpty()) return;
        assist.recordNearMiss(this.assistConfig, now);
    }

    private static List<Player> nearbySurvivors(Instance instance, Player bomber, Point position, double radius) {
        return instance.getNearbyEntities(position, radius).stream()
                .filter(entity -> entity != bomber)
                .filter(Player.class::isInstance)
                .map(Player.class::cast)
                .filter(target -> GameConfig.SURVIVOR_KEY.asString().equals(target.getTag(Tags.TEAM_KEY)))
                .toList();
    }

    /**
     * Draws the radius of the explosion as a ring of flames around the bomber.
     */
    private static void showRadius(Instance instance, Point center, double radius) {
        int points = (int) Math.ceil(2 * Math.PI * radius * RING_POINTS_PER_BLOCK);
        for (int i = 0; i < points; i++) {
            double angle = 2 * Math.PI * i / points;
            instance.sendGroupedPacket(new ParticlePacket(Particle.FLAME,
                    center.x() + Math.cos(angle) * radius, center.y() + 0.1, center.z() + Math.sin(angle) * radius,
                    0, 0, 0, 0, 1));
        }
    }
}

package net.theevilreaper.tamias.game.listener.game;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.MinecraftServer;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.sound.SoundEvent;
import net.theevilreaper.aves.util.functional.PlayerConsumer;
import net.theevilreaper.aves.util.functional.VoidConsumer;
import net.theevilreaper.tamias.common.config.GameConfig;
import net.theevilreaper.tamias.common.util.Tags;
import net.theevilreaper.tamias.game.player.TamiasPlayer;
import net.theevilreaper.tamias.game.attribute.AttributeHelper;
import net.theevilreaper.tamias.game.event.BomberExplodeEvent;
import net.theevilreaper.tamias.game.util.EntityHelper;
import net.theevilreaper.tamias.game.util.ExplosionRange;
import net.theevilreaper.tamias.game.util.GameMessages;

import java.util.List;
import java.util.function.Consumer;

/**
 * Handles the explosion of a bomber. Every survivor whose hit box is within {@link #INFECTION_RADIUS} of the TNT
 * becomes a bomber, with the lag of the bomber compensated, see {@link ExplosionRange}.
 * A bomber who was shot infects nobody, otherwise the shooter would be blown up by a close hit.
 * The bomber hears a hit sound and every player gets told who was blown up.
 */
@SuppressWarnings("java:S3252")
public class BomberExplodeListener implements Consumer<BomberExplodeEvent> {

    private static final Potion BLINDNESS = new Potion(PotionEffect.BLINDNESS, (byte) 1, Integer.MAX_VALUE);
    static final Sound HIT_SOUND = Sound.sound(SoundEvent.ENTITY_ARROW_HIT_PLAYER, Sound.Source.MASTER, 1f, 1f);
    static final double INFECTION_RADIUS = 3.0;

    private final PlayerConsumer survivorConverter;
    private final VoidConsumer roundEndCheck;

    /**
     * Creates a new listener.
     *
     * @param survivorConverter switches a survivor in the explosion radius to the bomber team
     * @param roundEndCheck     checks if the round can end after survivors were converted
     */
    public BomberExplodeListener(PlayerConsumer survivorConverter, VoidConsumer roundEndCheck) {
        this.survivorConverter = survivorConverter;
        this.roundEndCheck = roundEndCheck;
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
        if (event.isShot()) return;

        // Collect first, converting a survivor teleports them
        Point center = ExplosionRange.center(event.getPosition(), TamiasPlayer.DISGUISE_BOUNDING_BOX);
        long rewindMillis = ExplosionRange.rewindMillis(player.getLatency());
        List<Player> survivors = instance.getPlayers().stream()
                .filter(target -> target != player)
                .filter(target -> GameConfig.SURVIVOR_KEY.asString().equals(target.getTag(Tags.TEAM_KEY)))
                .filter(target -> isReached(center, target, rewindMillis))
                .toList();
        if (survivors.isEmpty()) return;

        player.playSound(HIT_SOUND);
        // Announced before the conversion, so the survivor is still shown in the survivor color
        Audience players = Audience.audience(MinecraftServer.getConnectionManager().getOnlinePlayers());
        survivors.forEach(survivor -> players.sendMessage(GameMessages.getDeathMessage(survivor, player)));
        if (player instanceof TamiasPlayer bomber) {
            survivors.forEach(_ -> bomber.addKill());
        }
        survivors.forEach(this.survivorConverter::accept);
        this.roundEndCheck.apply();
    }

    /**
     * A survivor is reached at the current position or where the bomber saw them, see {@link ExplosionRange}.
     */
    private static boolean isReached(Point center, Player target, long rewindMillis) {
        BoundingBox box = target.getBoundingBox();
        if (ExplosionRange.reaches(center, INFECTION_RADIUS, target.getPosition(), box)) return true;
        return target instanceof TamiasPlayer tamiasTarget
                && ExplosionRange.reaches(center, INFECTION_RADIUS, tamiasTarget.getPositionMillisAgo(rewindMillis), box);
    }
}

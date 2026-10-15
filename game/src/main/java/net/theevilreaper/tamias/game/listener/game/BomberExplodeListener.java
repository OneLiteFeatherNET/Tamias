package net.theevilreaper.tamias.game.listener.game;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import net.theevilreaper.aves.util.functional.PlayerConsumer;
import net.theevilreaper.aves.util.functional.VoidConsumer;
import net.theevilreaper.tamias.common.config.GameConfig;
import net.theevilreaper.tamias.common.util.Tags;
import net.theevilreaper.tamias.game.attribute.AttributeHelper;
import net.theevilreaper.tamias.game.event.BomberExplodeEvent;
import net.theevilreaper.tamias.game.util.EntityHelper;

import java.util.List;
import java.util.function.Consumer;

/**
 * Handles the explosion of a bomber. Every survivor within {@link #INFECTION_RADIUS} becomes a bomber.
 */
@SuppressWarnings("java:S3252")
public class BomberExplodeListener implements Consumer<BomberExplodeEvent> {

    private static final Potion BLINDNESS = new Potion(PotionEffect.BLINDNESS, (byte) 1, Integer.MAX_VALUE);
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

        // Collect first, converting a survivor teleports them and changes the nearby entities
        List<Player> survivors = instance.getNearbyEntities(event.getPosition(), INFECTION_RADIUS).stream()
                .filter(entity -> entity != player)
                .filter(Player.class::isInstance)
                .map(Player.class::cast)
                .filter(target -> GameConfig.SURVIVOR_KEY.asString().equals(target.getTag(Tags.TEAM_KEY)))
                .toList();
        if (survivors.isEmpty()) return;

        survivors.forEach(this.survivorConverter::accept);
        this.roundEndCheck.apply();
    }
}

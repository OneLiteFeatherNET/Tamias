package net.theevilreaper.tamias.game.listener.game;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import net.theevilreaper.tamias.game.attribute.AttributeHelper;
import net.theevilreaper.tamias.game.event.BomberExplodeEvent;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

@SuppressWarnings("java:S3252")
public class BomberExplodeListener implements Consumer<BomberExplodeEvent> {

    private static final Potion BLINDNESS = new Potion(PotionEffect.BLINDNESS, (byte) 1, Integer.MAX_VALUE);

    @Override
    public void accept(@NotNull BomberExplodeEvent event) {
        Player player = event.getPlayer();
        Instance instance = player.getInstance();
        Pos pos = Pos.fromPoint(player.getPosition());
        instance.explode((float) pos.x(), (float) pos.y(), (float) pos.z(), 1);
        player.addEffect(BLINDNESS);
        AttributeHelper.disableMovement(player);
    }
}

package net.theevilreaper.tamias.game.listener.game;

import de.icevizion.aves.util.functional.PlayerConsumer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.theevilreaper.tamias.common.config.GameConfig;
import net.theevilreaper.tamias.common.util.Tags;
import net.theevilreaper.tamias.game.attribute.AttributeHelper;
import net.theevilreaper.tamias.game.event.BomberRequireSpawnEvent;
import net.theevilreaper.tamias.game.stamina.ExplodeBar;
import net.theevilreaper.tamias.game.stamina.StaminaBar;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public final class BomberReviveListener implements Consumer<BomberRequireSpawnEvent> {

    private final Function<Player, StaminaBar> barGetter;
    private final Supplier<Pos> spawnPos;
    private final PlayerConsumer itemSetter;

    public BomberReviveListener(
            @NotNull Function<Player, StaminaBar> barGetter,
            @NotNull Supplier<Pos> spawnPos,
            @NotNull PlayerConsumer itemSetter) {
        this.barGetter = barGetter;
        this.spawnPos = spawnPos;
        this.itemSetter = itemSetter;
    }

    @Override
    public void accept(@NotNull BomberRequireSpawnEvent event) {
        Player player = event.getPlayer();

        if (!player.hasTag(Tags.TEAM_ID)) return;

        byte teamId = player.getTag(Tags.TEAM_ID);

        if (teamId != GameConfig.TNT_ID) return;

        Pos newSpawnPos = this.spawnPos.get();

        if (Objects.isNull(newSpawnPos)) {
            event.setCancelled(true);
            //TODO: Light spectator mode is here required
            return;
        }

        ExplodeBar staminaBar = (ExplodeBar) this.barGetter.apply(player);

        staminaBar.resetToDefaults();

        player.getActiveEffects().removeIf(effect -> {
            player.removeEffect(effect.potion().effect());
            return true;
        });

        player.teleport(newSpawnPos);
        AttributeHelper.enableMovement(player);
        this.itemSetter.accept(player);
    }
}

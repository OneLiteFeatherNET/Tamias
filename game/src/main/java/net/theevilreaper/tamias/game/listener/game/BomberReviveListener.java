package net.theevilreaper.tamias.game.listener.game;

import de.icevizion.aves.util.functional.PlayerConsumer;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.potion.TimedPotion;
import net.minestom.server.sound.SoundEvent;
import net.theevilreaper.tamias.common.config.GameConfig;
import net.theevilreaper.tamias.common.util.Tags;
import net.theevilreaper.tamias.game.attribute.AttributeHelper;
import net.theevilreaper.tamias.game.event.BomberRequireSpawnEvent;
import net.theevilreaper.tamias.game.stamina.ExplodeBar;
import net.theevilreaper.tamias.game.stamina.StaminaBar;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@SuppressWarnings("java:S3252")
public final class BomberReviveListener implements Consumer<BomberRequireSpawnEvent> {

    private static final Sound RESPAWN = Sound.sound(SoundEvent.BLOCK_ANVIL_PLACE, Sound.Source.MASTER, 1f, 0.65f);

    private final Function<Player, StaminaBar> barGetter;
    private final Optional<Supplier<Pos>> spawnPos;
    private final PlayerConsumer itemSetter;

    public BomberReviveListener(
            @NotNull Function<Player, StaminaBar> barGetter,
            @NotNull Optional<Supplier<Pos>>  spawnPos,
            @NotNull PlayerConsumer itemSetter
    ) {
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

        if (this.spawnPos.isEmpty()) {
            event.setCancelled(true);
            //TODO: Light spectator mode is here required
            return;
        }
        Pos newSpawnPos = this.spawnPos.get().get().add(0, 1,0);

        ExplodeBar staminaBar = (ExplodeBar) this.barGetter.apply(player);
        staminaBar.resetToDefaults();

        List<TimedPotion> activeEffects = new ArrayList<>(player.getActiveEffects());
        for (TimedPotion activeEffect : activeEffects) {
            player.removeEffect(activeEffect.potion().effect());
        }

        Instance instance = player.getInstance();
        instance.setBlock(newSpawnPos, Block.AIR);

        player.teleport(newSpawnPos.add(0.5, 1, 0.5));
        player.playSound(RESPAWN);
        AttributeHelper.enableMovement(player);
        this.itemSetter.accept(player);
    }
}

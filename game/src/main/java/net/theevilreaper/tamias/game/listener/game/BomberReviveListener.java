package net.theevilreaper.tamias.game.listener.game;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.potion.TimedPotion;
import net.minestom.server.sound.SoundEvent;
import net.theevilreaper.aves.util.functional.PlayerConsumer;
import net.theevilreaper.aves.util.functional.VoidConsumer;
import net.theevilreaper.tamias.common.config.GameConfig;
import net.theevilreaper.tamias.common.util.Tags;
import net.theevilreaper.tamias.game.attribute.AttributeHelper;
import net.theevilreaper.tamias.game.event.BomberRequireSpawnEvent;
import net.theevilreaper.tamias.game.stamina.ExplodeBar;
import net.theevilreaper.tamias.game.stamina.StaminaBar;
import net.theevilreaper.tamias.game.util.EntityHelper;
import net.theevilreaper.tamias.game.util.GameMessages;
import net.theevilreaper.tamias.game.util.Items;
import net.theevilreaper.tamias.game.util.SpectatorHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@SuppressWarnings("java:S3252")
public final class BomberReviveListener implements Consumer<BomberRequireSpawnEvent> {

    private static final Sound RESPAWN = Sound.sound(SoundEvent.BLOCK_ANVIL_PLACE, Sound.Source.MASTER, 1f, 0.65f);

    private final Function<Player, StaminaBar> barGetter;
    private final Supplier<Pos> tntSupplier;
    private final PlayerConsumer staminaRemover;
    private final VoidConsumer roundEndCheck;

    /**
     * Creates a new listener.
     *
     * @param barGetter      returns the stamina bar of a player
     * @param tntSupplier    supplies the position of a free TNT block or null if no TNT is left
     * @param staminaRemover stops and removes the stamina bar of a player who became a spectator
     * @param roundEndCheck  checks if the round can end after the player became a spectator
     */
    public BomberReviveListener(
            Function<Player, StaminaBar> barGetter,
            Supplier<Pos> tntSupplier,
            PlayerConsumer staminaRemover,
            VoidConsumer roundEndCheck
    ) {
        this.barGetter = barGetter;
        this.tntSupplier = tntSupplier;
        this.staminaRemover = staminaRemover;
        this.roundEndCheck = roundEndCheck;
    }

    @Override
    public void accept(BomberRequireSpawnEvent event) {
        Player player = event.getPlayer();

        if (!player.hasTag(Tags.TEAM_KEY)) return;

        String teamKey = player.getTag(Tags.TEAM_KEY);

        if (!GameConfig.BOMBER_KEY.asString().equals(teamKey)) return;

        // Each call consumes a TNT, so it must only be called once
        Pos tntPos = this.tntSupplier.get();
        if (tntPos == null) {
            event.setCancelled(true);
            this.staminaRemover.accept(player);
            SpectatorHelper.enable(player);
            Audience.audience(MinecraftServer.getConnectionManager().getOnlinePlayers())
                    .sendMessage(GameMessages.getSpectatorMessage(player));
            this.roundEndCheck.apply();
            return;
        }

        ExplodeBar staminaBar = (ExplodeBar) this.barGetter.apply(player);
        staminaBar.resetToDefaults();

        List<TimedPotion> activeEffects = new ArrayList<>(player.getActiveEffects());
        for (TimedPotion activeEffect : activeEffects) {
            player.removeEffect(activeEffect.potion().effect());
        }

        Instance instance = player.getInstance();
        instance.setBlock(tntPos, Block.AIR);

        // Stand exactly where the removed TNT block was, so the disguise replaces it seamlessly
        // Without an explicit velocity the client keeps its momentum and slides away from the TNT position
        player.teleport(tntPos.add(0.5, 0, 0.5), Vec.ZERO).thenRun(() -> EntityHelper.switchToTNT(player));
        player.playSound(RESPAWN);
        AttributeHelper.enableMovement(player);
        Items.setBombItem(player);
    }
}

package net.theevilreaper.tamias.game.listener.game;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.theevilreaper.tamias.game.event.RoleToBomberChangeEvent;
import net.theevilreaper.tamias.game.stamina.StaminaBar;
import net.theevilreaper.tamias.game.stamina.StaminaFactory;
import net.theevilreaper.tamias.game.stamina.StaminaService;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Supplier;

public final class RoleToBomberChangeListener implements Consumer<RoleToBomberChangeEvent> {

    private final StaminaService staminaService;
    private final Supplier<Pos> spawnSupplier;

    public RoleToBomberChangeListener(@NotNull StaminaService staminaService, @NotNull Supplier<Pos> spawnSupplier) {
        this.staminaService = staminaService;
        this.spawnSupplier = spawnSupplier;
    }

    @Override
    public void accept(@NotNull RoleToBomberChangeEvent event) {
        Player player = event.getPlayer();

        Pos spawnPos = this.spawnSupplier.get();

        if (spawnPos == null) return;

        staminaService.removeStaminaBar(player.getUuid());

        StaminaBar staminaBar = StaminaFactory.createExplodeBar(player);
        staminaService.add(player.getUuid(), staminaBar);

        staminaBar.start();

        player.teleport(spawnPos);
    }
}

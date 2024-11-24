package net.theevilreaper.tamias.game.util;

import de.icevizion.xerus.api.team.Team;
import net.kyori.adventure.text.format.TextColor;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.projectile.FireworkRocketMeta;
import net.minestom.server.item.ItemComponent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.component.FireworkExplosion;
import net.minestom.server.item.component.FireworkList;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class FireworkUtil {

    private static final int FIREWORK_ENTITY_SPAWN_COUNT = 3;

    public static void spawnFireworkForWinner(@NotNull Team team) {
        if (team.isEmpty()) return;

        for (int i = 0; i < FIREWORK_ENTITY_SPAWN_COUNT; i++) {
            for (Player player : team.getPlayers()) {
                Entity entity = new Entity(EntityType.FIREWORK_ROCKET);
                FireworkRocketMeta rocketMeta = (FireworkRocketMeta) entity.getEntityMeta();
                FireworkList fireworkInfo = new FireworkList((byte) 20, List.of(
                   new FireworkExplosion(
                           FireworkExplosion.Shape.BURST,
                           List.of(TextColor.color(20, 20, 20)),
                           List.of(TextColor.color(20, 20, 20)),
                           false,
                           true
                   )
                ));
                ItemStack rocketInfo = ItemStack.builder(Material.FIREWORK_STAR)
                                .set(ItemComponent.FIREWORKS, fireworkInfo)
                        .build();
                rocketMeta.setFireworkInfo(rocketInfo);
                entity.setInstance(player.getInstance(), player.getPosition().add(0, 0.5, 0));
                //entity.scheduleRemove();
             }
        }
    }

    private FireworkUtil() {
        throw new UnsupportedOperationException("The FireworkUtil class can not be instantiated");
    }

}

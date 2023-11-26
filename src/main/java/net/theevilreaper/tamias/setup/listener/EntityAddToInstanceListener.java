package net.theevilreaper.tamias.setup.listener;

import net.minestom.server.entity.Player;
import net.minestom.server.event.instance.AddEntityToInstanceEvent;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.function.Consumer;

public class EntityAddToInstanceListener implements Consumer<AddEntityToInstanceEvent> {

    private final UUID mainInstanceId;
    private final Consumer<Player> itemSetter;

    public EntityAddToInstanceListener(@NotNull UUID mainInstanceId, @NotNull Consumer<Player> itemSetter) {
        this.mainInstanceId = mainInstanceId;
        this.itemSetter = itemSetter;
    }

    @Override
    public void accept(@NotNull AddEntityToInstanceEvent event) {
        if (event.getInstance().getUniqueId().equals(this.mainInstanceId) && event.getEntity() instanceof Player player) {
            itemSetter.accept(player);
        }
    }
}

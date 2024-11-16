package net.theevilreaper.tamias.setup.listener.entity;

import net.minestom.server.entity.Player;
import net.minestom.server.event.instance.AddEntityToInstanceEvent;
import net.theevilreaper.tamias.setup.util.SetupItems;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.function.Consumer;

public class EntityAddToInstanceListener implements Consumer<AddEntityToInstanceEvent> {

    private final UUID mainInstanceId;
    private final SetupItems items;

    public EntityAddToInstanceListener(@NotNull UUID mainInstanceId, @NotNull SetupItems items) {
        this.mainInstanceId = mainInstanceId;
        this.items = items;
    }

    @Override
    public void accept(@NotNull AddEntityToInstanceEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (event.getInstance().getUniqueId().equals(this.mainInstanceId)) {
            items.setOverViewItem(player);
            return;
        }
        items.setSaveItem(player);
    }
}

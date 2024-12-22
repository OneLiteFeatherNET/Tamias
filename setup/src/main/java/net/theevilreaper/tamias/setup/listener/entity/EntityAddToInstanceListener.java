package net.theevilreaper.tamias.setup.listener.entity;

import net.minestom.server.entity.Player;
import net.minestom.server.event.instance.AddEntityToInstanceEvent;
import net.minestom.server.instance.Instance;
import net.theevilreaper.tamias.setup.util.SetupItems;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class EntityAddToInstanceListener implements Consumer<AddEntityToInstanceEvent> {

    private final Supplier<Instance> instanceSupplier;
    private final SetupItems items;

    public EntityAddToInstanceListener(@NotNull Supplier<Instance> instanceSupplier, @NotNull SetupItems items) {
        this.instanceSupplier = instanceSupplier;
        this.items = items;
    }

    @Override
    public void accept(@NotNull AddEntityToInstanceEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        Instance mainInstance = this.instanceSupplier.get();
        if (event.getInstance().getUniqueId().equals(mainInstance.getUniqueId())) {
            items.setOverViewItem(player);
            return;
        }
        items.setSaveItem(player);
    }
}

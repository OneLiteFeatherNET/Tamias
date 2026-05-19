package net.theevilreaper.tamias.setup.listener.entity;

import net.minestom.server.entity.Player;
import net.minestom.server.event.instance.AddEntityToInstanceEvent;
import net.minestom.server.instance.Instance;
import net.theevilreaper.tamias.setup.util.SetupItems;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class EntityAddToInstanceListener implements Consumer<AddEntityToInstanceEvent> {

    private final Supplier<Instance> instanceSupplier;

    public EntityAddToInstanceListener(Supplier<Instance> instanceSupplier) {
        this.instanceSupplier = instanceSupplier;
    }

    @Override
    public void accept(AddEntityToInstanceEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        Instance mainInstance = this.instanceSupplier.get();
        if (event.getInstance().getUuid().equals(mainInstance.getUuid())) {
            SetupItems.setOverViewItem(player);
            return;
        }
        SetupItems.setSaveItem(player);
    }
}

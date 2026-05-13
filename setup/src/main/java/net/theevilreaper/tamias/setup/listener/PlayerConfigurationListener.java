package net.theevilreaper.tamias.setup.listener;

import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.instance.Instance;

import java.util.function.Consumer;
import java.util.function.Supplier;

public final class PlayerConfigurationListener implements Consumer<AsyncPlayerConfigurationEvent> {

    private final Supplier<Instance> instanceSupplier;

    public PlayerConfigurationListener(Supplier<Instance> instanceSupplier) {
        this.instanceSupplier = instanceSupplier;
    }

    @Override
    public void accept(AsyncPlayerConfigurationEvent event) {
        event.setSpawningInstance(this.instanceSupplier.get());
    }
}

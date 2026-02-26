package net.theevilreaper.tamias.setup.dialog;

import net.kyori.adventure.key.Key;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.Nullable;

public interface DialogTemplate<K> {

    default void open(Player player) {
        this.open(player, null);
    }

    void open(Player player, @Nullable K data);

    /**
     * Gets the key of this dialog template.
     *
     * @return the given key
     */
    Key key();

}

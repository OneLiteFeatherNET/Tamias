package net.theevilreaper.tamias.setup.dialog;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface DialogRegistry {

    @Nullable DialogTemplate<?> get(@Nullable Key key);

    boolean contains(@NotNull Key key);
}

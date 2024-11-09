package net.theevilreaper.tamias.game.util;

import org.jetbrains.annotations.UnknownNullability;

@FunctionalInterface
public interface ValueGetter<T> {
    @UnknownNullability T getValue();
}

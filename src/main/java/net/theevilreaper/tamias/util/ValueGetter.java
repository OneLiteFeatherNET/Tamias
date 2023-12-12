package net.theevilreaper.tamias.util;

import org.jetbrains.annotations.UnknownNullability;

@FunctionalInterface
public interface ValueGetter<T> {
    @UnknownNullability T getValue();
}

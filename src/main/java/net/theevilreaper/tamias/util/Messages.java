package net.theevilreaper.tamias.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 **/
public final class Messages {

    public static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final Component PREFIX;

    static {
        PREFIX = Component.text("");
    }

    private Messages() { }

    @Contract(value = "_ -> new", pure = true)
    public static @NotNull Component withPrefix(@NotNull String component) {
        return PREFIX.append(MINI_MESSAGE.deserialize(component));
    }

    @Contract(value = "_ -> new", pure = true)
    public static @NotNull Component withPrefix(@NotNull Component component) {
        return PREFIX.append(Component.space()).append(component);
    }

    @Contract(value = "_ -> new", pure = true)
    public static @NotNull Component withMini(@NotNull String text) {
        return MiniMessage.miniMessage().deserialize(text);
    }

    @Contract(value = "_, _ -> new", pure = true)
    public static @NotNull Component withMini(@NotNull String text, @NotNull TagResolver... resolvers) {
        return MINI_MESSAGE.deserialize(text, resolvers);
    }

    @Contract(value = "_ -> new", pure = true)
    public static @NotNull Component withMiniPrefix(@NotNull String text) {
        return PREFIX.append(Component.space()).append(MINI_MESSAGE.deserialize(text));
    }

    @Contract(value = "_, _ -> new", pure = true)
    public static @NotNull Component withMiniPrefix(@NotNull String text, @NotNull TagResolver... resolvers) {
        return PREFIX.append(Component.space()).append(MINI_MESSAGE.deserialize(text, resolvers));
    }
    @Contract
    public static @NotNull Component getTimeComponent(int time) {
        var secondString = time > 1 ? "seconds" : "second";
        return withMiniPrefix("<gray>Restart in <red>" + time + " <gray>" + secondString);
    }
}

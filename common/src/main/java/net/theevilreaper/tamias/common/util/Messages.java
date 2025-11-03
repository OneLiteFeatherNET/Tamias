package net.theevilreaper.tamias.common.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.Contract;

/**
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 **/
public abstract class Messages {

    public static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    protected static final Component PREFIX;
    private static final Component SEPARATOR;

    static {
        PREFIX = MINI_MESSAGE.deserialize("<color:#FF6347>SuicideTNT</color>").append(Component.space());
        SEPARATOR = Component.space().append(Component.text(">>")).append(Component.space());
    }

    protected Messages() { }

    @Contract(value = "_ -> new", pure = true)
    public static Component withPrefix(String component) {
        return PREFIX.append(MINI_MESSAGE.deserialize(component));
    }

    @Contract(value = "_ -> new", pure = true)
    public static Component withPrefix(Component component) {
        return PREFIX.append(Component.space()).append(component);
    }

    @Contract(value = "_ -> new", pure = true)
    public static Component withMini(String text) {
        return MINI_MESSAGE.deserialize(text);
    }

    @Contract(value = "_, _ -> new", pure = true)
    public static Component withMini(String text, TagResolver... resolvers) {
        return MINI_MESSAGE.deserialize(text, resolvers);
    }

    @Contract(value = "_ -> new", pure = true)
    public static Component withMiniPrefix(String text) {
        return PREFIX.append(Component.space()).append(MINI_MESSAGE.deserialize(text));
    }

    @Contract(value = "_, _ -> new", pure = true)
    public static Component withMiniPrefix(String text, TagResolver... resolvers) {
        return PREFIX.append(Component.space()).append(MINI_MESSAGE.deserialize(text, resolvers));
    }
}

package net.theevilreaper.tamias.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 **/
public final class Messages {

    public static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    public static final Component TNT_MESSAGE;
    private static final Component PREFIX;
    private static final Component SEPARATOR;
    private static final Component LEAVE_PART;
    private static final Component JOIN_PART;
    private static final Component DEATH_PART;
    private static final Component SHOT_PART;


    static {
        PREFIX = MINI_MESSAGE.deserialize("<gradient:#00ff33:#fffafe:0.2>Suicide<gradient:#fffafe:#ff0008>TNT</gradient></gradient>").append(Component.space());
        TNT_MESSAGE = PREFIX.append(Component.space()).append(MINI_MESSAGE.deserialize("<red>You are now a TNT! <yellow>Right click <red>your TNT near players to blow them up!"));
        SEPARATOR = Component.space().append(Component.text(">>")).append(Component.space());
        LEAVE_PART = withMini("<gray>left the game!");
        JOIN_PART = withMini("<gray>joined the game!");
        DEATH_PART = withMini("<yellow>was blown up by");
        SHOT_PART = withMini("<yellow> You were shot by");
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
    public static @NotNull Component getLobbyTime(int time) {
        return withMiniPrefix("<gold>Starting in... <red>" + time);
    }

    @Contract(value = "_ -> new", pure = true)
    public static @NotNull Component getJoinMessage(@NotNull Player player) {
        return PREFIX.append(Component.space()).append(withMini("<color:#249D9F>" + player.getUsername() + "</color>"))
                .append(Component.space()).append(JOIN_PART);
    }

    @Contract(value = "_ -> new", pure = true)
    public static @NotNull Component getLeaveMessage(@NotNull Player player) {
        return PREFIX.append(Component.space()).append(withMini("<color:#249D9F>" + player.getUsername() + "</color>"))
                .append(Component.space()).append(LEAVE_PART);
    }

    @Contract(value = "_, _ -> new", pure = true)
    public static @NotNull Component getDeathMessage(@NotNull Player target, @NotNull Player killer) {
        return withPrefix(target.getDisplayName().append(Component.space()).append(DEATH_PART).append(Component.space())
                .append(killer.getDisplayName()));
    }

    @Contract(value = "_ -> new", pure = true)
    public static @NotNull Component getShotMessage(@NotNull Player killer) {
        return withPrefix(SHOT_PART.append(Component.space()).append(killer.getDisplayName()));
    }

    @Contract
    public static @NotNull Component buildChatLayout(@NotNull Player player, @NotNull Component message) {
        return player.getDisplayName().append(SEPARATOR).append(message);
    }
}

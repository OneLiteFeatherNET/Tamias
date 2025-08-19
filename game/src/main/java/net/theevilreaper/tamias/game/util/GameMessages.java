package net.theevilreaper.tamias.game.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.minestom.server.entity.Player;
import net.theevilreaper.tamias.common.config.GameConfig;
import net.theevilreaper.tamias.common.util.Messages;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 **/
public final class GameMessages extends Messages {

    public static final Component TNT_MESSAGE;
    private static final Component SEPARATOR;
    private static final Component LEAVE_PART;
    private static final Component JOIN_PART;
    private static final Component DEATH_PART;
    private static final Component SHOT_PART;
    private static final Component TITLE_TIME;

    public static final Component CHOOSING_NEW_TNT;
    public static final Component GAME_END_KICK_MESSAGE;

    public static final Component MAP_READY;
    public static final Component MAP_BUILDING;

    public static final Component ALREADY_FORCE_STARTED;
    public static final Component PHASE_NOT_RUNNING;
    public static final Component PHASE_FORCE_STARTED;

    static {
        TNT_MESSAGE = PREFIX.append(Component.space()).append(MINI_MESSAGE.deserialize("<red>You are now a TNT! <yellow>Right click <red>your TNT near players to blow them up!"));
        SEPARATOR = Component.space().append(Component.text(">>")).append(Component.space());
        LEAVE_PART = withMini("<gray>left the game!");
        JOIN_PART = withMini("<gray>joined the game!");
        DEATH_PART = withMini("<yellow>was blown up by");
        SHOT_PART = withMini("<yellow> You were shot by");
        CHOOSING_NEW_TNT = withMiniPrefix("<red>Choosing new tnt<gold>....");
        TITLE_TIME = Component.text("Time:", NamedTextColor.GOLD).append(Component.space());
        GAME_END_KICK_MESSAGE = Component.text("The game is over. Thanks for playing it. <3", NamedTextColor.RED);
        MAP_READY = Component.text("Map is ready!", NamedTextColor.GREEN);
        MAP_BUILDING = Component.text("Map is building up...", NamedTextColor.GREEN);
        int forceStartTime = GameConfig.FORCE_START_TIME;
        ALREADY_FORCE_STARTED = withMiniPrefix("<red>The game has already been force started!");
        PHASE_NOT_RUNNING = withMiniPrefix("<red>The lobby countdown is not running!");
        PHASE_FORCE_STARTED = withMiniPrefix("<gray>The timer has been set to <color:#09ff00><seconds></color> seconds!",
                TagResolver.builder().tag("seconds", (argumentQueue, context) -> Tag.preProcessParsed(String.valueOf(forceStartTime))).build());
    }

    private GameMessages() {
        super();
    }

    /**
     * Returns a time component for the lobby countdown.
     *
     * @param time the time in seconds to display
     * @return a component with the lobby time message
     */
    @Contract(value = "_ -> new", pure = true)
    public static @NotNull Component getLobbyTime(int time) {
        return withMiniPrefix("<gold>Starting in... <red>" + time);
    }

    /**
     * Creates a component for the title time.
     *
     * @param time the time to display
     * @return a component with the title time
     */
    @Contract(value = "_ -> new", pure = true)
    public static @NotNull Component getTitleTime(int time) {
        return TITLE_TIME.append(Component.text(time, NamedTextColor.YELLOW));
    }

    /**
     * Returns a component that indicates the restart time.
     *
     * @param time the time in seconds until the restart
     * @return a component with the restart time message
     */
    @Contract(value = "_ -> new", pure = true)
    public static @NotNull Component getRestartTime(int time) {
        if (time == 1) {
            return withMiniPrefix("<gray>Restarting in <red>" + time + " <gray>second!");
        }
        return withMiniPrefix("<gray>Restarting in <red>" + time + " <gray>seconds!");
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

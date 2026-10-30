package net.theevilreaper.tamias.game.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.minestom.server.entity.Player;
import net.theevilreaper.aves.util.Strings;
import net.theevilreaper.aves.util.TimeFormat;
import net.theevilreaper.tamias.common.config.GameConfig;
import net.theevilreaper.tamias.common.util.Messages;
import net.theevilreaper.xerus.api.component.team.ColorComponent;
import net.theevilreaper.xerus.api.team.Team;
import org.jetbrains.annotations.Contract;

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
    private static final Component SPECTATOR_PART;
    private static final Component NEW_BOMBER_PART;
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
        SPECTATOR_PART = withMini("<gray>has no TNT left and is now spectating!");
        NEW_BOMBER_PART = withMini("<yellow>replaces the bomber who left the game!");
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
    public static Component getLobbyTime(int time) {
        return withMiniPrefix("<gold>Starting in... <red>" + time);
    }

    /**
     * Returns a component for the countdown before a round is played.
     *
     * @param time the time in seconds until the round starts
     * @return a component with the round start message
     */
    @Contract(value = "_ -> new", pure = true)
    public static Component getRoundStartTime(int time) {
        if (time == 1) {
            return withMiniPrefix("<gray>The round starts in <red>" + time + " <gray>second!");
        }
        return withMiniPrefix("<gray>The round starts in <red>" + time + " <gray>seconds!");
    }

    /**
     * Creates a component for the title time.
     *
     * @param time the time in seconds to display
     * @return a component with the title time in the format {@code MM:SS}
     */
    @Contract(value = "_ -> new", pure = true)
    public static Component getTitleTime(int time) {
        return TITLE_TIME.append(Component.text(Strings.getTimeString(TimeFormat.MM_SS, time), NamedTextColor.YELLOW));
    }

    /**
     * Returns a component that indicates the restart time.
     *
     * @param time the time in seconds until the restart
     * @return a component with the restart time message
     */
    @Contract(value = "_ -> new", pure = true)
    public static Component getRestartTime(int time) {
        if (time == 1) {
            return withMiniPrefix("<gray>Restarting in <red>" + time + " <gray>second!");
        }
        return withMiniPrefix("<gray>Restarting in <red>" + time + " <gray>seconds!");
    }

    /**
     * Creates the message which shows a player its own stats at the end of the game.
     *
     * @param wins   the amount of rounds the player has won
     * @param rounds the amount of rounds which were played
     * @param kills  the amount of survivors the player has converted as a bomber
     * @param hits   the amount of bombers the player has hit as a survivor
     * @return a component with the personal result
     */
    @Contract(value = "_, _, _, _ -> new", pure = true)
    public static Component getPersonalResult(int wins, int rounds, int kills, int hits) {
        return withMiniPrefix("<gray>You won <green>" + wins + " <gray>/ <aqua>" + rounds + " <gray>rounds · <red>"
                + kills + " <gray>kills · <yellow>" + hits + " <gray>hits");
    }

    @Contract(value = "_ -> new", pure = true)
    public static Component getJoinMessage(Player player) {
        return PREFIX.append(Component.space()).append(withMini("<color:#249D9F>" + player.getUsername() + "</color>"))
                .append(Component.space()).append(JOIN_PART);
    }

    @Contract(value = "_ -> new", pure = true)
    public static Component getLeaveMessage(Player player) {
        return PREFIX.append(Component.space()).append(withMini("<color:#249D9F>" + player.getUsername() + "</color>"))
                .append(Component.space()).append(LEAVE_PART);
    }

    /**
     * Creates the message which announces the survivor who replaces the bomber that left the game.
     *
     * @param player the new bomber
     * @return a component with the new bomber message
     */
    @Contract(value = "_ -> new", pure = true)
    public static Component getNewBomberMessage(Player player) {
        return withPrefix(getName(player).append(Component.space()).append(NEW_BOMBER_PART));
    }

    @Contract(value = "_ -> new", pure = true)
    public static Component getSpectatorMessage(Player player) {
        return PREFIX.append(Component.space()).append(withMini("<color:#249D9F>" + player.getUsername() + "</color>"))
                .append(Component.space()).append(SPECTATOR_PART);
    }

    /**
     * Creates the message which announces the team that won the round.
     *
     * @param winner the team which won the round
     * @return a component with the win message
     */
    @Contract(value = "_ -> new", pure = true)
    public static Component getWinMessage(Team winner) {
        String teamName = GameConfig.BOMBER_KEY.equals(winner.key()) ? "Bombers" : "Survivors";
        ColorComponent colorComponent = winner.get(ColorComponent.class);
        TextColor teamColor = colorComponent != null ? colorComponent.colorData().getChatColor() : NamedTextColor.GOLD;
        return withPrefix(Component.text("The ", NamedTextColor.GRAY)
                .append(Component.text(teamName, teamColor))
                .append(Component.text(" won the round!", NamedTextColor.GRAY)));
    }

    @Contract(value = "_, _ -> new", pure = true)
    public static Component getDeathMessage(Player target, Player killer) {
        return withPrefix(getName(target).append(Component.space()).append(DEATH_PART).append(Component.space())
                .append(getName(killer)));
    }

    @Contract(value = "_ -> new", pure = true)
    public static Component getShotMessage(Player killer) {
        return withPrefix(SHOT_PART.append(Component.space()).append(getName(killer)));
    }

    @Contract
    public static Component buildChatLayout(Player player, Component message) {
        return getName(player).append(SEPARATOR).append(message);
    }

    /**
     * Returns the display name of the player, which shows the team color, or the username if none is set.
     *
     * @param player the player to get the name from
     * @return the name of the player
     */
    private static Component getName(Player player) {
        Component displayName = player.getDisplayName();
        return displayName != null ? displayName : Component.text(player.getUsername());
    }
}

package net.theevilreaper.tamias.setup.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.tag.PreProcess;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.minestom.server.utils.Direction;
import net.theevilreaper.tamias.common.util.Messages;
import net.theevilreaper.tamias.setup.map.MapDataCategory;
import org.jetbrains.annotations.Contract;

public class SetupMessages extends Messages {

    public static final Component SELECT_MAP_FIRST;
    public static final Component NO_SPACE_SEPARATOR;
    public static final Component TELEPORT_CLICK;
    public static final Component DELETE_CLICK;
    public static final Component CYCLE_CLICK;
    public static final Component OPEN_CLICK;
    public static final Component EDIT_CLICK;
    public static final Component CHOOSE_CLICK;
    public static final Component SELECT_CLICK;
    public static final Component ADD_CLICK;
    public static final Component REMOVE_CLICK;
    public static final Component CLEAR_CLICK;
    public static final Component GROUND_BLOCK_REQUIRED;
    public static final Component GROUND_PRESET_LIMIT;

    static {
        SELECT_MAP_FIRST = Messages.withPrefix(Component.text("Please select a map first before executing this function", NamedTextColor.RED));
        NO_SPACE_SEPARATOR = Component.text("»", NamedTextColor.GRAY);

        TELEPORT_CLICK = NO_SPACE_SEPARATOR
                .append(Component.space())
                .append(Component.text("Left", NamedTextColor.GREEN))
                .append(Component.space())
                .append(Component.text("click", NamedTextColor.GRAY))
                .append(Component.space())
                .append(Component.text("->", NamedTextColor.GRAY))
                .append(Component.space())
                .append(Component.text("teleport", NamedTextColor.GREEN));
        DELETE_CLICK = NO_SPACE_SEPARATOR
                .append(Component.space())
                .append(Component.text("Right", NamedTextColor.RED))
                .append(Component.space())
                .append(Component.text("click", NamedTextColor.GRAY))
                .append(Component.space())
                .append(Component.text("->", NamedTextColor.GRAY))
                .append(Component.space())
                .append(Component.text("delete", NamedTextColor.RED));
        CYCLE_CLICK = NO_SPACE_SEPARATOR
                .append(Component.space())
                .append(Component.text("Left", NamedTextColor.GREEN))
                .append(Component.space())
                .append(Component.text("click", NamedTextColor.GRAY))
                .append(Component.space())
                .append(Component.text("->", NamedTextColor.GRAY))
                .append(Component.space())
                .append(Component.text("next direction", NamedTextColor.GREEN));
        OPEN_CLICK = clickHint("Left", NamedTextColor.GREEN, "open");
        EDIT_CLICK = clickHint("Left", NamedTextColor.GREEN, "edit");
        CHOOSE_CLICK = clickHint("Left", NamedTextColor.GREEN, "choose");
        SELECT_CLICK = clickHint("Left", NamedTextColor.GREEN, "select");
        ADD_CLICK = clickHint("Left", NamedTextColor.GREEN, "add");
        REMOVE_CLICK = clickHint("Left", NamedTextColor.RED, "remove");
        CLEAR_CLICK = clickHint("Right", NamedTextColor.RED, "clear");
        GROUND_BLOCK_REQUIRED = Messages.withPrefix(Component.text("Choose a ground block before saving the preset", NamedTextColor.RED));
        GROUND_PRESET_LIMIT = Messages.withPrefix(Component.text("The map already has the maximum amount of ground presets", NamedTextColor.RED));
    }

    /**
     * Returns the message shown when a preset already has the maximum amount of decoration blocks.
     *
     * @param maxBlocks the maximum amount of decoration blocks
     * @return the error message
     */
    @Contract(value = "_ -> new", pure = true)
    public static Component getDecorationLimit(int maxBlocks) {
        return Messages.withPrefix(Component.text("A preset can have at most " + maxBlocks + " decoration blocks", NamedTextColor.RED));
    }

    /**
     * Creates a lore line with a gray label and a colored value, for example {@code » Ground: Red Sand}.
     *
     * @param label the label of the line
     * @param value the value of the line
     * @param color the color of the value
     * @return the created line
     */
    @Contract(value = "_, _, _ -> new", pure = true)
    public static Component loreValue(String label, String value, NamedTextColor color) {
        return NO_SPACE_SEPARATOR
                .append(Component.space())
                .append(Component.text(label + ": ", NamedTextColor.GRAY))
                .append(Component.text(value, color));
    }

    private static Component clickHint(String side, NamedTextColor color, String action) {
        return NO_SPACE_SEPARATOR
                .append(Component.space())
                .append(Component.text(side, color))
                .append(Component.space())
                .append(Component.text("click", NamedTextColor.GRAY))
                .append(Component.space())
                .append(Component.text("->", NamedTextColor.GRAY))
                .append(Component.space())
                .append(Component.text(action, color));
    }

    @Contract(value = "_ -> new", pure = true)
    public static Component getInvalidFace(String face) {
        PreProcess facePreProcess = Tag.preProcessParsed(face);
        TagResolver faceTag = TagResolver.builder().tag("face", (argumentQueue, context) -> facePreProcess).build();
        return Messages.withMini("<red>You are looking in an invalid direction! <gray>(<gold><face><gray>)", faceTag);
    }

    /**
     * Returns the confirmation message shown after a position was captured for a category.
     *
     * @param category the category the position was captured for
     * @return the confirmation message
     */
    @Contract(value = "_ -> new", pure = true)
    public static Component getPositionSet(MapDataCategory category) {
        return Messages.withPrefix(Component.text(category.getName() + " position set.", NamedTextColor.GREEN));
    }

    /**
     * Returns the confirmation message shown after a direction was set for a category.
     *
     * @param category  the category the direction was set for
     * @param direction the direction that was set
     * @return the confirmation message
     */
    @Contract(value = "_, _ -> new", pure = true)
    public static Component getDirectionSet(MapDataCategory category, Direction direction) {
        return Messages.withPrefix(Component.text(category.getName() + " direction set to " + direction.name() + ".", NamedTextColor.GREEN));
    }
}

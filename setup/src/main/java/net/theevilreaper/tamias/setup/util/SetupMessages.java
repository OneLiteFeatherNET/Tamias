package net.theevilreaper.tamias.setup.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.tag.PreProcess;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.minestom.server.item.Material;
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
    public static final Component SET_BLOCK_CLICK;
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
        SET_BLOCK_CLICK = NO_SPACE_SEPARATOR
                .append(Component.space())
                .append(Component.text("Click with a block on the cursor", NamedTextColor.GRAY));
        GROUND_BLOCK_REQUIRED = Messages.withPrefix(Component.text("A ground preset requires a ground block", NamedTextColor.RED));
        GROUND_PRESET_LIMIT = Messages.withPrefix(Component.text("The map can't have more ground presets", NamedTextColor.RED));
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

    /**
     * Returns the message shown when an item can't be used as a ground block.
     *
     * @param material the material of the rejected item
     * @return the error message
     */
    @Contract(value = "_ -> new", pure = true)
    public static Component getInvalidGroundBlock(Material material) {
        return Messages.withPrefix(Component.text(material.key().value() + " can't be used as a ground block", NamedTextColor.RED));
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

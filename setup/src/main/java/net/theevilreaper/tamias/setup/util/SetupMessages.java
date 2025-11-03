package net.theevilreaper.tamias.setup.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.tag.PreProcess;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.theevilreaper.tamias.common.util.Messages;
import org.jetbrains.annotations.Contract;

public class SetupMessages extends Messages {

    public static final Component SELECT_MAP_FIRST;

    static {
        SELECT_MAP_FIRST = Messages.withPrefix(Component.text("Please select a map first before executing this function", NamedTextColor.RED));
    }

    @Contract(value = "_ -> new", pure = true)
    public static Component getInvalidFace(String face) {
        PreProcess facePreProcess = Tag.preProcessParsed(face);
        TagResolver faceTag = TagResolver.builder().tag("face", (argumentQueue, context) -> facePreProcess).build();
        return Messages.withMini("<red>You are looking in an invalid direction! <gray>(<gold><face><gray>)", faceTag);
    }
}

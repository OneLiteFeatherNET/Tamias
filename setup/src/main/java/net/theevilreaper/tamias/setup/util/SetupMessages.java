package net.theevilreaper.tamias.setup.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.PreProcess;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.theevilreaper.tamias.common.util.Messages;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class SetupMessages extends Messages {

    @Contract(value = "_ -> new", pure = true)
    public static @NotNull Component getInvalidFace(@NotNull String face) {
        PreProcess facePreProcess = Tag.preProcessParsed(face);
        TagResolver faceTag = TagResolver.builder().tag("face", (argumentQueue, context) -> facePreProcess).build();
        return Messages.withMini("<red>You are looking in an invalid direction! <gray>(<gold><face><gray>)", faceTag);
    }
}

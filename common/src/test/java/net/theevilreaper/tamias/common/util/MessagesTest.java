package net.theevilreaper.tamias.common.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MessagesTest {

    @Test
    void withPrefixStringUsesSingleSpacedPrefix() {
        Component message = Messages.withPrefix("Hello world");
        assertEquals("SuicideTNT » Hello world", PlainTextComponentSerializer.plainText().serialize(message));
    }

    @Test
    void withPrefixComponentUsesSingleSpacedPrefix() {
        Component message = Messages.withPrefix(Component.text("Hello world", NamedTextColor.RED));
        assertEquals("SuicideTNT » Hello world", PlainTextComponentSerializer.plainText().serialize(message));
    }

    @Test
    void withMiniPrefixStringUsesSingleSpacedPrefix() {
        Component message = Messages.withMiniPrefix("<red>Hello world");
        assertEquals("SuicideTNT » Hello world", PlainTextComponentSerializer.plainText().serialize(message));
    }
}

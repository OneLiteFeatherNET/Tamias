/*package net.theevilreaper.tamias.game.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.translation.GlobalTranslator;
import net.theevilreaper.tamias.common.util.Translations;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Locale;

import static net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameMessagesTest {

    @BeforeAll
    static void setUp() {
        Translations.register();
    }

    @ParameterizedTest
    @CsvSource({
            "en, 0, The round starts in 0 seconds!",
            "en, 1, The round starts in 1 second!",
            "en, 5, The round starts in 5 seconds!",
            "de, 1, Die Runde startet in 1 Sekunde!",
            "de, 5, Die Runde startet in 5 Sekunden!"
    })
    void testRoundStartTime(String language, int time, String expected) {
        String text = render(GameMessages.getRoundStartTime(time), Locale.of(language));
        assertTrue(text.endsWith(expected), text);
    }

    @ParameterizedTest
    @CsvSource({
            "en, 2, Restarting in 2 seconds!",
            "de, 1, Neustart in 1 Sekunde!"
    })
    void testRestartTime(String language, int time, String expected) {
        String text = render(GameMessages.getRestartTime(time), Locale.of(language));
        assertTrue(text.endsWith(expected), text);
    }

    @ParameterizedTest
    @CsvSource({
            "en, You won 2 / 3 rounds · 4 kills · 7 hits",
            "de, Du hast 2 / 3 Runden gewonnen · 4 Kills · 7 Treffer"
    })
    void testPersonalResult(String language, String expected) {
        String text = render(GameMessages.getPersonalResult(2, 3, 4, 7), Locale.of(language));
        assertTrue(text.endsWith(expected), text);
    }

    @ParameterizedTest
    @CsvSource({ "en, Time: 04:59", "de, Zeit: 04:59" })
    void testTitleTime(String language, String expected) {
        String text = render(GameMessages.getTitleTime("04:59"), Locale.of(language));
        assertTrue(text.equals(expected), text);
    }

    private static String render(Component component, Locale locale) {
        return plainText().serialize(GlobalTranslator.render(component, locale));
    }
}*/

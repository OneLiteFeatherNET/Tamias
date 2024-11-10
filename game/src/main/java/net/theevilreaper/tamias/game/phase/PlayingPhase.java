package net.theevilreaper.tamias.game.phase;

import de.icevizion.aves.util.Strings;
import de.icevizion.aves.util.TimeFormat;
import de.icevizion.xerus.api.phase.TimedPhase;
import net.kyori.adventure.text.Component;
import net.theevilreaper.tamias.game.util.GameMessages;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.time.temporal.ChronoUnit;
import java.util.function.Consumer;

/**
 * @author theEvilReaper
 * @version 1.0.0
 * @since
 **/

public final class PlayingPhase extends TimedPhase {

    private final Consumer<@NotNull Component> scoreboardTitle;


    public PlayingPhase(@NotNull Consumer<@NotNull Component> scoreboardTitle) {
        super("GamePhase", ChronoUnit.SECONDS, 1);
        this.scoreboardTitle = scoreboardTitle;
    }

    @Override
    protected void onFinish() {

    }

    @Override
    public void onUpdate() {
        this.scoreboardTitle.accept(getTimeDisplay());
    }

    @Contract(pure = true)
    private @NotNull Component getTimeDisplay() {
        return GameMessages.withMini("<gold>Time: " + Strings.getTimeString(TimeFormat.MM_SS, getCurrentTicks()));
    }
}

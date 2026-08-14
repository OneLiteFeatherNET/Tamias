package net.theevilreaper.tamias.setup.util;

import net.kyori.adventure.sound.Sound;
import net.minestom.server.sound.SoundEvent;

/**
 * Holds sound references that are used as feedback during the setup process.
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 */
public final class SetupSounds {

    public static final Sound DATA_SET = Sound.sound(SoundEvent.ENTITY_EXPERIENCE_ORB_PICKUP, Sound.Source.MASTER, 1f, 1.2f);

    private SetupSounds() {
        // Nothing to do here
    }
}

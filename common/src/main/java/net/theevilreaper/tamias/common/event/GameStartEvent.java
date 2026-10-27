package net.theevilreaper.tamias.common.event;

import net.minestom.server.event.Event;

/**
 * The event is called once per game at the moment the lobby phase finishes.
 * <p>
 * It is a pure signal for components outside the game module, for example the CloudNet bridge
 * extension, which must not depend on the game module itself.
 *
 * @author TheMeinerLP
 * @version 1.0.0
 * @since 1.0.0
 */
public final class GameStartEvent implements Event {
}

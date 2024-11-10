package net.theevilreaper.tamias.common.round.event;

import net.minestom.server.event.Event;

public record RoundEndEvent(byte winnerTeam) implements Event {
}

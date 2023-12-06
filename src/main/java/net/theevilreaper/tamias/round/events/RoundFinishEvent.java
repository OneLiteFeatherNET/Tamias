package net.theevilreaper.tamias.round.events;

import net.minestom.server.event.Event;

public record RoundFinishEvent(byte winnerTeam) implements Event { }
package net.theevilreaper.tamias.game.round.events;

import net.minestom.server.event.Event;

public record RoundFinishEvent(byte winnerTeam) implements Event { }
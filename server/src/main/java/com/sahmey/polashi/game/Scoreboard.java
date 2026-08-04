package com.sahmey.polashi.game;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

public final class Scoreboard {
    private static final int CHAPTERS_TO_WIN = 3;

    private final Map<Faction, Integer> wins = new EnumMap<>(Faction.class);

    public synchronized void recordWin(Faction faction) {
        wins.merge(faction, 1, Integer::sum);
    }

    public synchronized int winsFor(Faction faction) {
        return wins.getOrDefault(faction, 0);
    }

    public synchronized Optional<Faction> winner() {
        return wins.entrySet().stream()
            .filter(entry -> entry.getValue() >= CHAPTERS_TO_WIN)
            .map(Map.Entry::getKey)
            .findFirst();
    }
}

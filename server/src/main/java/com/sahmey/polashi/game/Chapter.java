package com.sahmey.polashi.game;

public final class Chapter {
    public static Faction resolve(int redsPlayed, int threshold) {
        return redsPlayed >= threshold ? Faction.EIC : Faction.NAWAB;
    }

    private Chapter() {}
}

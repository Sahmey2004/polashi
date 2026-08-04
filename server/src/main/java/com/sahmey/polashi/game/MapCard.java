package com.sahmey.polashi.game;

import java.util.Map;

public final class MapCard {
    // players -> team size per chapter (index 0 = chapter 1 ... index 4 = chapter 5)
    private static final Map<Integer, int[]> TEAM_SIZES = Map.of(
        5,  new int[] {2, 3, 2, 3, 3},
        6,  new int[] {2, 3, 4, 3, 4},
        7,  new int[] {2, 3, 3, 4, 4},
        8,  new int[] {3, 4, 4, 5, 5},
        9,  new int[] {3, 4, 4, 5, 5},
        10, new int[] {3, 4, 4, 5, 5}
    );

    public static int teamSize(int players, int chapter) {
        int[] chapters = TEAM_SIZES.get(players);
        if (chapters == null) throw new IllegalArgumentException("players must be 5-10");
        if (chapter < 1 || chapter > chapters.length) {
            throw new IllegalArgumentException("chapter must be 1-" + chapters.length);
        }
        return chapters[chapter - 1];
    }

    public static int redsToWin(int players, int chapter) {
        return (chapter == 4 && players >= 7) ? 2 : 1;
    }

    private MapCard() {}
}

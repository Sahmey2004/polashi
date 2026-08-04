package com.sahmey.polashi.game;

import org.junit.jupiter.api.Test;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScoreboardTest {
    private Scoreboard scored(int eicWins, int nawabWins) {
        Scoreboard board = new Scoreboard();
        for (int i = 0; i < eicWins; i++) board.recordWin(Faction.EIC);
        for (int i = 0; i < nawabWins; i++) board.recordWin(Faction.NAWAB);
        return board;
    }

    @Test
    void threeZeroEndsForEic() {
        assertEquals(Optional.of(Faction.EIC), scored(3, 0).winner());
    }

    @Test
    void threeOneEndsForEic() {
        assertEquals(Optional.of(Faction.EIC), scored(3, 1).winner());
    }

    @Test
    void threeTwoEndsForEic() {
        assertEquals(Optional.of(Faction.EIC), scored(3, 2).winner());
    }

    @Test
    void twoTwoDoesNotEnd() {
        assertTrue(scored(2, 2).winner().isEmpty());
    }

    @Test
    void nawabCanAlsoReachThree() {
        assertEquals(Optional.of(Faction.NAWAB), scored(1, 3).winner());
    }

    @Test
    void winsForReportsPerFactionTally() {
        Scoreboard board = scored(2, 1);
        assertEquals(2, board.winsFor(Faction.EIC));
        assertEquals(1, board.winsFor(Faction.NAWAB));
    }

    @Test
    void winsForIsZeroWhenFactionHasNotWonYet() {
        assertEquals(0, new Scoreboard().winsFor(Faction.EIC));
    }
}

package com.sahmey.polashi.game;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ChapterTest {
    @Test
    void eicWinsWhenRedsPlayedMeetsThreshold() {
        assertEquals(Faction.EIC, Chapter.resolve(1, 1));
        assertEquals(Faction.EIC, Chapter.resolve(2, 2));
        assertEquals(Faction.EIC, Chapter.resolve(3, 2));
    }

    @Test
    void nawabWinsWhenRedsPlayedBelowThreshold() {
        assertEquals(Faction.NAWAB, Chapter.resolve(0, 1));
        assertEquals(Faction.NAWAB, Chapter.resolve(1, 2));
    }
}

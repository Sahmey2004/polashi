package com.sahmey.polashi.game;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class MapCardThresholdTest {
    @Test
    void defaultThresholdIsOne() {
        assertEquals(1, MapCard.redsToWin(5, 1));
        assertEquals(1, MapCard.redsToWin(7, 1));
        assertEquals(1, MapCard.redsToWin(7, 2));
        assertEquals(1, MapCard.redsToWin(7, 3));
        assertEquals(1, MapCard.redsToWin(7, 5));
        assertEquals(1, MapCard.redsToWin(10, 5));
    }

    @Test
    void chapterFourNeedsTwoWhenSevenOrMorePlayers() {
        assertEquals(2, MapCard.redsToWin(7, 4));
        assertEquals(2, MapCard.redsToWin(8, 4));
        assertEquals(2, MapCard.redsToWin(10, 4));
    }

    @Test
    void chapterFourStillOneUnderSevenPlayers() {
        assertEquals(1, MapCard.redsToWin(5, 4));
        assertEquals(1, MapCard.redsToWin(6, 4));
    }
}

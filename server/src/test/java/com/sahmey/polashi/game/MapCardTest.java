package com.sahmey.polashi.game;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class MapCardTest {
    @Test
    void teamSizesMatchSpecFor5Players() {
        assertEquals(2, MapCard.teamSize(5, 1));
        assertEquals(3, MapCard.teamSize(5, 2));
        assertEquals(2, MapCard.teamSize(5, 3));
        assertEquals(3, MapCard.teamSize(5, 4));
        assertEquals(3, MapCard.teamSize(5, 5));
    }

    @Test
    void teamSizesMatchSpecFor6Players() {
        assertEquals(2, MapCard.teamSize(6, 1));
        assertEquals(3, MapCard.teamSize(6, 2));
        assertEquals(4, MapCard.teamSize(6, 3));
        assertEquals(3, MapCard.teamSize(6, 4));
        assertEquals(4, MapCard.teamSize(6, 5));
    }

    @Test
    void teamSizesMatchSpecFor7Players() {
        assertEquals(2, MapCard.teamSize(7, 1));
        assertEquals(3, MapCard.teamSize(7, 2));
        assertEquals(3, MapCard.teamSize(7, 3));
        assertEquals(4, MapCard.teamSize(7, 4));
        assertEquals(4, MapCard.teamSize(7, 5));
    }

    @Test
    void teamSizesMatchSpecFor8Players() {
        assertEquals(3, MapCard.teamSize(8, 1));
        assertEquals(4, MapCard.teamSize(8, 2));
        assertEquals(4, MapCard.teamSize(8, 3));
        assertEquals(5, MapCard.teamSize(8, 4));
        assertEquals(5, MapCard.teamSize(8, 5));
    }

    @Test
    void teamSizesMatchSpecFor9Players() {
        assertEquals(3, MapCard.teamSize(9, 1));
        assertEquals(4, MapCard.teamSize(9, 2));
        assertEquals(4, MapCard.teamSize(9, 3));
        assertEquals(5, MapCard.teamSize(9, 4));
        assertEquals(5, MapCard.teamSize(9, 5));
    }

    @Test
    void teamSizesMatchSpecFor10Players() {
        assertEquals(3, MapCard.teamSize(10, 1));
        assertEquals(4, MapCard.teamSize(10, 2));
        assertEquals(4, MapCard.teamSize(10, 3));
        assertEquals(5, MapCard.teamSize(10, 4));
        assertEquals(5, MapCard.teamSize(10, 5));
    }

    @Test
    void rejectsPlayersOutOfRange() {
        try {
            MapCard.teamSize(4, 1);
        } catch (IllegalArgumentException expected) {
            return;
        }
        throw new AssertionError("expected IllegalArgumentException for players=4");
    }

    @Test
    void rejectsChapterOutOfRange() {
        try {
            MapCard.teamSize(5, 6);
        } catch (IllegalArgumentException expected) {
            return;
        }
        throw new AssertionError("expected IllegalArgumentException for chapter=6");
    }
}

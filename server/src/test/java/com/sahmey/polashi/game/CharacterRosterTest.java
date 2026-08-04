package com.sahmey.polashi.game;

import org.junit.jupiter.api.Test;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CharacterRosterTest {

    @Test
    void eicRosterSizeMatchesFactionTableRedCount() {
        for (int players = 5; players <= 10; players++) {
            assertEquals(FactionTable.redCount(players), CharacterRoster.eicRoster(players).size());
        }
    }

    @Test
    void eicRosterForTwoRed() {
        assertEquals(Set.of(Character.MIR_JAFAR, Character.GHOSETI_BEGUM),
            new HashSet<>(CharacterRoster.eicRoster(5)));
        assertEquals(Set.of(Character.MIR_JAFAR, Character.GHOSETI_BEGUM),
            new HashSet<>(CharacterRoster.eicRoster(6)));
    }

    @Test
    void eicRosterForThreeRedAddsOmichand() {
        Set<Character> expected = Set.of(Character.MIR_JAFAR, Character.GHOSETI_BEGUM, Character.OMICHAND);
        assertEquals(expected, new HashSet<>(CharacterRoster.eicRoster(7)));
        assertEquals(expected, new HashSet<>(CharacterRoster.eicRoster(8)));
        assertEquals(expected, new HashSet<>(CharacterRoster.eicRoster(9)));
    }

    @Test
    void eicRosterForFourRedAddsRayDurlabh() {
        assertEquals(
            Set.of(Character.MIR_JAFAR, Character.GHOSETI_BEGUM, Character.OMICHAND, Character.RAY_DURLABH),
            new HashSet<>(CharacterRoster.eicRoster(10)));
    }

    @Test
    void nawabRosterSizeMatchesRemainingPlayers() {
        for (int players = 5; players <= 10; players++) {
            int expectedGreen = players - FactionTable.redCount(players);
            assertEquals(expectedGreen, CharacterRoster.nawabRoster(players).size());
        }
    }

    @Test
    void nawabRosterAlwaysIncludesModonAndMohanLal() {
        for (int players = 5; players <= 10; players++) {
            List<Character> roster = CharacterRoster.nawabRoster(players);
            assertTrue(roster.contains(Character.MIR_MODON), "players=" + players);
            assertTrue(roster.contains(Character.MOHAN_LAL), "players=" + players);
        }
    }

    @Test
    void nawabRosterNeverIncludesEicCharacters() {
        for (int players = 5; players <= 10; players++) {
            for (Character c : CharacterRoster.nawabRoster(players)) {
                assertEquals(Faction.NAWAB, c.getFaction(), c + " at players=" + players);
            }
        }
    }

    @Test
    void nawabRosterForEightPlayersIncludesAllFiveNamedCharacters() {
        Set<Character> expected = Set.of(Character.MIR_MODON, Character.MOHAN_LAL,
            Character.SIRAJ_UD_DAULAH, Character.LUTFUNNISA_BEGUM, Character.ST_FRAIS);
        assertEquals(expected, new HashSet<>(CharacterRoster.nawabRoster(8)));
    }

    @Test
    void nawabRosterForNineAndTenPlayersAddsGenericFiller() {
        for (int players : new int[] {9, 10}) {
            List<Character> roster = CharacterRoster.nawabRoster(players);
            assertTrue(roster.contains(Character.BENGALI_NOBLEMAN), "players=" + players);
            assertEquals(6, roster.size());
        }
    }

    @Test
    void smallGamesPickVariedFillersAcrossManyTrials() {
        Set<Character> seenFillers = new HashSet<>();
        for (int i = 0; i < 300; i++) {
            for (Character c : CharacterRoster.nawabRoster(5)) {
                if (c != Character.MIR_MODON && c != Character.MOHAN_LAL) {
                    seenFillers.add(c);
                }
            }
        }
        assertEquals(
            Set.of(Character.SIRAJ_UD_DAULAH, Character.LUTFUNNISA_BEGUM, Character.ST_FRAIS),
            seenFillers);
    }

    @Test
    void fullRosterSizeMatchesPlayerCountAndHasNoDuplicates() {
        for (int players = 5; players <= 10; players++) {
            List<Character> roster = CharacterRoster.fullRoster(players);
            assertEquals(players, roster.size(), "players=" + players);
            assertEquals(players, new HashSet<>(roster).size(), "duplicates at players=" + players);
        }
    }
}

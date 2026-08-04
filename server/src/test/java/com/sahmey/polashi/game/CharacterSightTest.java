package com.sahmey.polashi.game;

import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CharacterSightTest {

    private Player playerWith(Character character) {
        Player player = new Player(character.getDisplayName(), null);
        player.setCharacter(character);
        player.setRole(character.getFaction());
        return player;
    }

    @Test
    void tenPlayerRosterFullMutualCircleAndConfusedSight() {
        Player jafar = playerWith(Character.MIR_JAFAR);
        Player ghoseti = playerWith(Character.GHOSETI_BEGUM);
        Player durlabh = playerWith(Character.RAY_DURLABH);
        Player omichand = playerWith(Character.OMICHAND);
        Player modon = playerWith(Character.MIR_MODON);
        Player mohanLal = playerWith(Character.MOHAN_LAL);
        Player siraj = playerWith(Character.SIRAJ_UD_DAULAH);
        Player lutfunnisa = playerWith(Character.LUTFUNNISA_BEGUM);
        Player stFrais = playerWith(Character.ST_FRAIS);
        Player nobleman = playerWith(Character.BENGALI_NOBLEMAN);

        List<Player> players = List.of(
            jafar, ghoseti, durlabh, omichand, modon, mohanLal, siraj, lutfunnisa, stFrais, nobleman);

        Map<java.util.UUID, CharacterSight.SightResult> sight = CharacterSight.resolveAll(players);

        assertEquals(Set.of(Character.GHOSETI_BEGUM, Character.RAY_DURLABH), clearCharacters(sight, jafar));
        assertEquals(Set.of(Character.MIR_JAFAR, Character.RAY_DURLABH), clearCharacters(sight, ghoseti));
        assertEquals(Set.of(Character.MIR_JAFAR, Character.GHOSETI_BEGUM), clearCharacters(sight, durlabh));

        assertEquals(Set.of(Character.GHOSETI_BEGUM), clearCharacters(sight, modon));
        assertTrue(sight.get(modon.getId()).confused().isEmpty());

        assertTrue(sight.get(mohanLal.getId()).clear().isEmpty());
        assertEquals(Set.of(modon, ghoseti), Set.copyOf(sight.get(mohanLal.getId()).confused()));

        for (Player filler : List.of(omichand, siraj, lutfunnisa, stFrais, nobleman)) {
            assertTrue(sight.get(filler.getId()).clear().isEmpty(), filler.getNickname() + " should have no clear sight");
            assertTrue(sight.get(filler.getId()).confused().isEmpty(), filler.getNickname() + " should have no confused sight");
        }
    }

    @Test
    void fivePlayerRosterWithoutRayDurlabhOrOmichand() {
        // 5-player game: only Jafar + Ghoseti on the EIC side, Ray Durlabh isn't in play.
        Player jafar = playerWith(Character.MIR_JAFAR);
        Player ghoseti = playerWith(Character.GHOSETI_BEGUM);
        Player modon = playerWith(Character.MIR_MODON);
        Player mohanLal = playerWith(Character.MOHAN_LAL);
        Player filler = playerWith(Character.ST_FRAIS);

        List<Player> players = List.of(jafar, ghoseti, modon, mohanLal, filler);
        Map<java.util.UUID, CharacterSight.SightResult> sight = CharacterSight.resolveAll(players);

        assertEquals(Set.of(Character.GHOSETI_BEGUM), clearCharacters(sight, jafar));
        assertEquals(Set.of(Character.MIR_JAFAR), clearCharacters(sight, ghoseti));
        assertEquals(Set.of(Character.GHOSETI_BEGUM), clearCharacters(sight, modon));
        assertEquals(Set.of(modon, ghoseti), Set.copyOf(sight.get(mohanLal.getId()).confused()));
        assertTrue(sight.get(filler.getId()).clear().isEmpty());
        assertTrue(sight.get(filler.getId()).confused().isEmpty());
    }

    private Set<Character> clearCharacters(Map<java.util.UUID, CharacterSight.SightResult> sight, Player viewer) {
        return sight.get(viewer.getId()).clear().stream()
            .map(CharacterSight.SightEntry::character)
            .collect(Collectors.toSet());
    }
}

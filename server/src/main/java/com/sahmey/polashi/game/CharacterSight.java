package com.sahmey.polashi.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class CharacterSight {

    public record SightEntry(Character character, Player player) {}

    public record SightResult(List<SightEntry> clear, List<Player> confused) {}

    private static final Map<Character, Set<Character>> CLEAR_SIGHT = new EnumMap<>(Map.of(
        Character.MIR_JAFAR, EnumSet.of(Character.GHOSETI_BEGUM, Character.RAY_DURLABH),
        Character.GHOSETI_BEGUM, EnumSet.of(Character.MIR_JAFAR, Character.RAY_DURLABH),
        Character.RAY_DURLABH, EnumSet.of(Character.MIR_JAFAR, Character.GHOSETI_BEGUM),
        Character.MIR_MODON, EnumSet.of(Character.GHOSETI_BEGUM)
    ));

    private static final Map<Character, Set<Character>> CONFUSED_SIGHT = new EnumMap<>(Map.of(
        Character.MOHAN_LAL, EnumSet.of(Character.MIR_MODON, Character.GHOSETI_BEGUM)
    ));

    public static Map<UUID, SightResult> resolveAll(List<Player> players) {
        Map<Character, Player> playerByCharacter = new EnumMap<>(Character.class);
        for (Player player : players) {
            playerByCharacter.put(player.getCharacter(), player);
        }

        Map<UUID, SightResult> results = new HashMap<>();
        for (Player viewer : players) {
            Character viewerCharacter = viewer.getCharacter();

            List<SightEntry> clear = new ArrayList<>();
            for (Character target : CLEAR_SIGHT.getOrDefault(viewerCharacter, Set.of())) {
                Player targetPlayer = playerByCharacter.get(target);
                if (targetPlayer != null) {
                    clear.add(new SightEntry(target, targetPlayer));
                }
            }

            List<Player> confused = new ArrayList<>();
            for (Character target : CONFUSED_SIGHT.getOrDefault(viewerCharacter, Set.of())) {
                Player targetPlayer = playerByCharacter.get(target);
                if (targetPlayer != null) {
                    confused.add(targetPlayer);
                }
            }
            Collections.shuffle(confused);

            results.put(viewer.getId(), new SightResult(clear, confused));
        }
        return results;
    }

    private CharacterSight() {}
}

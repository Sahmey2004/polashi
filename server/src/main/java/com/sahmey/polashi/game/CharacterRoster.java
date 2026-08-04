package com.sahmey.polashi.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class CharacterRoster {
    private static final List<Character> EIC_IN_JOIN_ORDER =
        List.of(Character.MIR_JAFAR, Character.GHOSETI_BEGUM, Character.OMICHAND, Character.RAY_DURLABH);

    private static final List<Character> NAWAB_OPTIONAL_FILLERS =
        List.of(Character.SIRAJ_UD_DAULAH, Character.LUTFUNNISA_BEGUM, Character.ST_FRAIS);

    public static List<Character> eicRoster(int players) {
        int redCount = FactionTable.redCount(players);
        return new ArrayList<>(EIC_IN_JOIN_ORDER.subList(0, redCount));
    }

    public static List<Character> nawabRoster(int players) {
        int needed = players - FactionTable.redCount(players);

        List<Character> roster = new ArrayList<>();
        roster.add(Character.MIR_MODON);
        roster.add(Character.MOHAN_LAL);

        int extraNeeded = needed - roster.size();
        if (extraNeeded <= NAWAB_OPTIONAL_FILLERS.size()) {
            List<Character> shuffledFillers = new ArrayList<>(NAWAB_OPTIONAL_FILLERS);
            Collections.shuffle(shuffledFillers);
            roster.addAll(shuffledFillers.subList(0, extraNeeded));
        } else {
            roster.addAll(NAWAB_OPTIONAL_FILLERS);
            roster.add(Character.BENGALI_NOBLEMAN);
        }

        return roster;
    }

    public static List<Character> fullRoster(int players) {
        List<Character> roster = new ArrayList<>(eicRoster(players));
        roster.addAll(nawabRoster(players));
        return roster;
    }

    private CharacterRoster() {}
}

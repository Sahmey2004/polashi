package com.sahmey.polashi.game;

import java.util.Map;



public final class FactionTable{
    private static final Map<Integer, Integer> REDS = Map.of(
        5, 2, 6, 2, 7, 3, 8 ,3, 9, 3, 10, 4
    );

    public static int redCount(int players){
        Integer r = REDS.get(players);
        if (r == null) throw new
        IllegalArgumentException("players must be 5-10");
        return r;


    }

    public static int greenCount(int players){
        return players - redCount(players);
    }

    private FactionTable(){}
}
package com.sahmey.polashi.game;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;



class FactionTableTest{
    @Test
    void redCountsMatchSpec(){
        assertEquals(2, FactionTable.redCount(5));
        assertEquals(2, FactionTable.redCount(6));
        assertEquals(3, FactionTable.redCount(7));
        assertEquals(3, FactionTable.redCount(8));
        assertEquals(3, FactionTable.redCount(9));
        assertEquals(4, FactionTable.redCount(10));
    }

    @Test 

    void greenIsTheRest(){
        assertEquals(3, FactionTable.greenCount(5));
        assertEquals(6, FactionTable.greenCount(10));
    }
}
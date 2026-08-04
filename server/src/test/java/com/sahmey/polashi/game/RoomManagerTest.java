package com.sahmey.polashi.game;

import org.junit.jupiter.api.Test;
import java.util.HashSet;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RoomManagerTest {

    @Test
    void createRoomReturnsAUsableRoomLinkedToAFreshGame() {
        RoomManager rooms = new RoomManager();

        String code = rooms.createRoom();

        assertNotNull(code);
        assertSame(rooms.getRoom(code), rooms.getRoom(code));
        assertEquals(Phase.LOBBY, rooms.getRoom(code).getPhase());
    }

    @Test
    void createRoomGeneratesDistinctCodes() {
        RoomManager rooms = new RoomManager();
        Set<String> codes = new HashSet<>();

        for (int i = 0; i < 500; i++) {
            codes.add(rooms.createRoom());
        }

        assertEquals(500, codes.size());
    }

    @Test
    void gettingAnUnknownRoomThrows() {
        RoomManager rooms = new RoomManager();

        assertThrows(IllegalArgumentException.class, () -> rooms.getRoom("NOPE"));
    }
}

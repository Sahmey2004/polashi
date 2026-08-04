package com.sahmey.polashi.game;

import org.springframework.stereotype.Component;
import java.security.SecureRandom;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public final class RoomManager {
    // I/O/0/1 excluded to avoid ambiguous codes when read aloud or typed
    private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int CODE_LENGTH = 5;

    private final SecureRandom random = new SecureRandom();
    private final Map<String, Game> rooms = new ConcurrentHashMap<>();

    public String createRoom() {
        String code;
        do {
            code = generateCode();
        } while (rooms.putIfAbsent(code, new Game()) != null);
        return code;
    }

    public Game getRoom(String roomCode) {
        Game game = rooms.get(roomCode);
        if (game == null) {
            throw new IllegalArgumentException("no such room: " + roomCode);
        }
        return game;
    }

    private String generateCode() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }
}

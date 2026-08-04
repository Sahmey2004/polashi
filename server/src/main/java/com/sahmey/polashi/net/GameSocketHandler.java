package com.sahmey.polashi.net;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;
import com.sahmey.polashi.game.CharacterSight;
import com.sahmey.polashi.game.Faction;
import com.sahmey.polashi.game.Game;
import com.sahmey.polashi.game.MapCard;
import com.sahmey.polashi.game.Phase;
import com.sahmey.polashi.game.Player;
import com.sahmey.polashi.game.RoomManager;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class GameSocketHandler extends TextWebSocketHandler {
    private static final String ROOM_CODE_ATTR = "roomCode";
    private static final String PLAYER_ID_ATTR = "playerId";

    private final RoomManager roomManager;
    private final ObjectMapper mapper = new ObjectMapper();

    public GameSocketHandler(RoomManager roomManager) {
        this.roomManager = roomManager;
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        JsonNode node;
        try {
            node = mapper.readTree(message.getPayload());
        } catch (Exception e) {
            sendError(session, "invalid JSON");
            return;
        }

        String type = node.path("type").asString(null);
        if (type == null) {
            sendError(session, "missing 'type'");
            return;
        }

        try {
            switch (type) {
                case "createRoom" -> handleCreateRoom(session, node);
                case "joinRoom" -> handleJoinRoom(session, node);
                case "startGame" -> handleStartGame(session);
                case "proposeTeam" -> handleProposeTeam(session, node);
                case "castVote" -> handleCastVote(session, node);
                case "playWarCard" -> handlePlayWarCard(session, node);
                case "nextChapter" -> handleNextChapter(session);
                default -> sendError(session, "unknown type: " + type);
            }
        } catch (IllegalArgumentException e) {
            sendError(session, e.getMessage());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        // Players keep their seat on disconnect for this demo; a real deployment
        // would mark them offline and allow session re-attachment on reconnect.
    }

    // ---- message handlers -------------------------------------------------

    private void handleCreateRoom(WebSocketSession session, JsonNode node) throws IOException {
        String nickname = node.path("nickname").asString("player");
        String roomCode = roomManager.createRoom();
        Game game = roomManager.getRoom(roomCode);
        joinExistingGame(session, game, roomCode, nickname);
    }

    private void handleJoinRoom(WebSocketSession session, JsonNode node) throws IOException {
        String roomCode = node.path("roomCode").asString("").trim().toUpperCase();
        Game game = roomManager.getRoom(roomCode);
        String nickname = node.path("nickname").asString("player");
        joinExistingGame(session, game, roomCode, nickname);
    }

    private void joinExistingGame(WebSocketSession session, Game game, String roomCode, String nickname)
            throws IOException {
        Player player = game.addPlayer(nickname, session);
        session.getAttributes().put(ROOM_CODE_ATTR, roomCode);
        session.getAttributes().put(PLAYER_ID_ATTR, player.getId());

        ObjectNode joined = mapper.createObjectNode();
        joined.put("type", "joined");
        joined.put("roomCode", roomCode);
        joined.put("playerId", player.getId().toString());
        send(session, joined);

        broadcastRoomState(game, roomCode);
    }

    private void handleStartGame(WebSocketSession session) throws IOException {
        Game game = currentGame(session);
        game.start();

        for (Player player : game.getPlayers()) {
            sendPrivateRole(game, player);
        }
        broadcastChapterState(game);
    }

    private void handleProposeTeam(WebSocketSession session, JsonNode node) throws IOException {
        Game game = currentGame(session);
        UUID captainId = currentPlayerId(session);

        List<UUID> teamIds = new ArrayList<>();
        for (JsonNode idNode : node.path("playerIds")) {
            teamIds.add(UUID.fromString(idNode.asString()));
        }

        game.proposeTeam(captainId, teamIds);
        broadcastChapterState(game);
    }

    private void handleCastVote(WebSocketSession session, JsonNode node) throws IOException {
        Game game = currentGame(session);
        UUID playerId = currentPlayerId(session);
        boolean approve = node.path("approve").asBoolean();

        game.castVote(playerId, approve);

        if (game.getPhase() == Phase.VOTING) {
            broadcastVoteProgress(game);
            return;
        }

        broadcastVoteResult(game);
        if (game.getPhase() == Phase.GAME_OVER) {
            broadcastGameOver(game);
        } else if (game.getPhase() == Phase.TEAM_PROPOSAL) {
            broadcastChapterState(game);
        }
    }

    private void handlePlayWarCard(WebSocketSession session, JsonNode node) throws IOException {
        Game game = currentGame(session);
        UUID playerId = currentPlayerId(session);
        boolean red = node.path("red").asBoolean();

        game.playWarCard(playerId, red);

        if (game.getPhase() == Phase.WAR_CARDS) {
            broadcastWarProgress(game);
            return;
        }

        broadcastWarResult(game);
        if (game.getPhase() == Phase.GAME_OVER) {
            broadcastGameOver(game);
        } else if (game.getPhase() == Phase.TEAM_PROPOSAL) {
            broadcastChapterState(game);
        }
    }

    private void handleNextChapter(WebSocketSession session) throws IOException {
        broadcastChapterState(currentGame(session));
    }

    // ---- session helpers ----------------------------------------------------

    private Game currentGame(WebSocketSession session) {
        Object roomCode = session.getAttributes().get(ROOM_CODE_ATTR);
        if (roomCode == null) {
            throw new IllegalArgumentException("join a room first");
        }
        return roomManager.getRoom((String) roomCode);
    }

    private UUID currentPlayerId(WebSocketSession session) {
        Object playerId = session.getAttributes().get(PLAYER_ID_ATTR);
        if (playerId == null) {
            throw new IllegalArgumentException("join a room first");
        }
        return (UUID) playerId;
    }

    // ---- outgoing payloads --------------------------------------------------

    private void broadcastRoomState(Game game, String roomCode) throws IOException {
        ObjectNode payload = mapper.createObjectNode();
        payload.put("type", "roomState");
        payload.put("roomCode", roomCode);
        ArrayNode playersNode = payload.putArray("players");
        for (Player p : game.getPlayers()) {
            ObjectNode playerNode = playersNode.addObject();
            playerNode.put("id", p.getId().toString());
            playerNode.put("nickname", p.getNickname());
        }
        broadcast(game, payload);
    }

    private void broadcastChapterState(Game game) throws IOException {
        ObjectNode payload = mapper.createObjectNode();
        payload.put("type", "chapterState");
        payload.put("phase", game.getPhase().name());
        payload.put("chapter", game.getCurrentChapter());
        payload.put("captainId", game.getCurrentCaptain().getId().toString());
        payload.put("teamSize", MapCard.teamSize(game.getPlayers().size(), game.getCurrentChapter()));
        ArrayNode proposedTeamNode = payload.putArray("proposedTeam");
        for (Player p : game.getProposedTeam()) {
            proposedTeamNode.add(p.getId().toString());
        }
        broadcast(game, payload);
    }

    private void broadcastVoteProgress(Game game) throws IOException {
        ObjectNode payload = mapper.createObjectNode();
        payload.put("type", "voteProgress");
        payload.put("votesCast", game.getVotesCastCount());
        payload.put("totalPlayers", game.getPlayers().size());
        broadcast(game, payload);
    }

    private void broadcastVoteResult(Game game) throws IOException {
        ObjectNode payload = mapper.createObjectNode();
        payload.put("type", "voteResult");
        payload.put("phase", game.getPhase().name());
        payload.put("captainId", game.getCurrentCaptain().getId().toString());
        payload.put("approveCount", game.getLastVoteApproveCount());
        payload.put("totalVotes", game.getPlayers().size());
        broadcast(game, payload);
    }

    private void broadcastWarProgress(Game game) throws IOException {
        ObjectNode payload = mapper.createObjectNode();
        payload.put("type", "warProgress");
        payload.put("cardsPlayed", game.getWarCardsPlayedCount());
        payload.put("teamSize", game.getProposedTeam().size());
        broadcast(game, payload);
    }

    private void broadcastWarResult(Game game) throws IOException {
        ObjectNode payload = mapper.createObjectNode();
        payload.put("type", "warResult");
        payload.put("phase", game.getPhase().name());
        payload.put("chapter", game.getLastChapterNumber());
        payload.put("teamSize", game.getLastChapterTeamSize());
        payload.put("redsPlayed", game.getLastChapterRedsPlayed());
        payload.put("chapterWinner", game.getLastChapterWinner().name());
        broadcast(game, payload);
    }

    private void broadcastGameOver(Game game) throws IOException {
        ObjectNode payload = mapper.createObjectNode();
        payload.put("type", "gameOver");
        Optional<Faction> winner = game.getScoreboard().winner();
        payload.put("winner", winner.map(Enum::name).orElse(null));
        broadcast(game, payload);
    }

    private void sendPrivateRole(Game game, Player player) throws IOException {
        ObjectNode payload = mapper.createObjectNode();
        payload.put("type", "privateRole");
        payload.put("role", player.getRole().name());
        payload.put("character", player.getCharacter().getDisplayName());

        ArrayNode clearSight = payload.putArray("clearSight");
        for (CharacterSight.SightEntry entry : game.getClearSight(player.getId())) {
            ObjectNode entryNode = clearSight.addObject();
            entryNode.put("character", entry.character().getDisplayName());
            entryNode.put("playerId", entry.player().getId().toString());
            entryNode.put("nickname", entry.player().getNickname());
        }

        ArrayNode confusedSight = payload.putArray("confusedSight");
        for (Player suspect : game.getConfusedSight(player.getId())) {
            ObjectNode suspectNode = confusedSight.addObject();
            suspectNode.put("playerId", suspect.getId().toString());
            suspectNode.put("nickname", suspect.getNickname());
        }

        send(player.getSession(), payload);
    }

    private void sendError(WebSocketSession session, String message) throws IOException {
        ObjectNode payload = mapper.createObjectNode();
        payload.put("type", "error");
        payload.put("message", message);
        send(session, payload);
    }

    private void broadcast(Game game, ObjectNode payload) throws IOException {
        for (Player p : game.getPlayers()) {
            send(p.getSession(), payload);
        }
    }

    private void send(WebSocketSession session, ObjectNode payload) throws IOException {
        if (session == null || !session.isOpen()) {
            return;
        }
        String json = mapper.writeValueAsString(payload);
        synchronized (session) {
            session.sendMessage(new TextMessage(json));
        }
    }
}

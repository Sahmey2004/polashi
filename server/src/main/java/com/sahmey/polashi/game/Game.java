package com.sahmey.polashi.game;


import org.springframework.web.socket.WebSocketSession;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


public final class Game{
    private final List<Player> players = new ArrayList<>();
    private final List<Player> proposedTeam = new ArrayList<>();
    private final Map<UUID, Boolean> votes = new HashMap<>();
    private final Map<UUID, Boolean> warCards = new HashMap<>();
    private Map<UUID, CharacterSight.SightResult> sightByPlayer = Map.of();
    private final ChapterProgress rejectTracking = new ChapterProgress();
    private final Scoreboard scoreboard = new Scoreboard();
    private Phase phase = Phase.LOBBY;
    private int captainIndex = 0;
    private int currentChapter = 1;
    private int lastChapterRedsPlayed = 0;
    private int lastChapterTeamSize = 0;
    private int lastChapterNumber = 0;
    private Faction lastChapterWinner = null;
    private int lastVoteApproveCount = 0;

    public synchronized Player addPlayer(String nickname, WebSocketSession session){
        Player player = new Player(nickname, session);
        players.add(player);
        return player;
    }

    public synchronized void start(){
        List<Character> characters = CharacterRoster.fullRoster(players.size());
        Collections.shuffle(characters);

        for (int i = 0; i < players.size(); i++){
            Player player = players.get(i);
            Character character = characters.get(i);
            player.setCharacter(character);
            player.setRole(character.getFaction());
        }

        sightByPlayer = CharacterSight.resolveAll(players);

        captainIndex = 0;
        phase = Phase.TEAM_PROPOSAL;
    }

    public synchronized Player getCurrentCaptain(){
        return players.get(captainIndex);

    }

    public synchronized List<Player> getPlayers(){
        return List.copyOf(players);
    }

    public synchronized Phase getPhase(){
        return phase;
    }

    public synchronized int getCurrentChapter(){
        return currentChapter;
    }

    public synchronized List<Player> getProposedTeam(){
        return List.copyOf(proposedTeam);
    }

    public synchronized void proposeTeam(UUID captainId, List<UUID> playerIds){
        if (phase != Phase.TEAM_PROPOSAL) {
            throw new IllegalArgumentException("can only propose a team during TEAM_PROPOSAL");
        }
        if (!getCurrentCaptain().getId().equals(captainId)) {
            throw new IllegalArgumentException("only the current captain can propose a team");
        }

        int expectedSize = MapCard.teamSize(players.size(), currentChapter);
        if (playerIds.size() != expectedSize) {
            throw new IllegalArgumentException(
                "team must have " + expectedSize + " players, got " + playerIds.size());
        }

        proposedTeam.clear();
        for (UUID id : playerIds) {
            proposedTeam.add(findPlayer(id));
        }

        phase = Phase.VOTING;
    }

    public synchronized void castVote(UUID playerId, boolean approve){
        if (phase != Phase.VOTING) {
            throw new IllegalArgumentException("can only vote during VOTING");
        }
        if (votes.containsKey(playerId)) {
            throw new IllegalArgumentException("player already voted this round");
        }
        votes.put(playerId, approve);

        if (votes.size() < players.size()) {
            return;
        }

        long approveCount = votes.values().stream().filter(v -> v).count();
        lastVoteApproveCount = (int) approveCount;
        votes.clear();

        if (approveCount > players.size() / 2) {
            phase = Phase.WAR_CARDS;
        } else {
            rejectTracking.reject();
            if (rejectTracking.eicWinsByRejection()) {
                phase = Phase.GAME_OVER;
            } else {
                captainIndex = (captainIndex + 1) % players.size();
                phase = Phase.TEAM_PROPOSAL;
            }
        }
    }

    public synchronized Scoreboard getScoreboard(){
        return scoreboard;
    }

    public synchronized int getLastVoteApproveCount(){
        return lastVoteApproveCount;
    }

    public synchronized List<CharacterSight.SightEntry> getClearSight(UUID playerId){
        CharacterSight.SightResult result = sightByPlayer.get(playerId);
        return result == null ? List.of() : result.clear();
    }

    public synchronized List<Player> getConfusedSight(UUID playerId){
        CharacterSight.SightResult result = sightByPlayer.get(playerId);
        return result == null ? List.of() : result.confused();
    }

    public synchronized int getVotesCastCount(){
        return votes.size();
    }

    public synchronized int getWarCardsPlayedCount(){
        return warCards.size();
    }

    public synchronized int getLastChapterRedsPlayed(){
        return lastChapterRedsPlayed;
    }

    public synchronized int getLastChapterTeamSize(){
        return lastChapterTeamSize;
    }

    public synchronized int getLastChapterNumber(){
        return lastChapterNumber;
    }

    public synchronized Faction getLastChapterWinner(){
        return lastChapterWinner;
    }

    public synchronized void playWarCard(UUID playerId, boolean red){
        if (phase != Phase.WAR_CARDS) {
            throw new IllegalArgumentException("can only play a war card during WAR_CARDS");
        }
        boolean onTeam = proposedTeam.stream().anyMatch(p -> p.getId().equals(playerId));
        if (!onTeam) {
            throw new IllegalArgumentException("only proposed team members can play a war card");
        }
        if (warCards.containsKey(playerId)) {
            throw new IllegalArgumentException("player already played a card this chapter");
        }
        if (red && findPlayer(playerId).getRole() != Faction.EIC) {
            throw new IllegalArgumentException("only EIC players can play a red card");
        }

        warCards.put(playerId, red);

        if (warCards.size() < proposedTeam.size()) {
            return;
        }

        resolveChapter();
    }

    private void resolveChapter(){
        long redsPlayed = warCards.values().stream().filter(v -> v).count();
        int threshold = MapCard.redsToWin(players.size(), currentChapter);
        Faction chapterWinner = Chapter.resolve((int) redsPlayed, threshold);
        lastChapterRedsPlayed = (int) redsPlayed;
        lastChapterTeamSize = proposedTeam.size();
        lastChapterNumber = currentChapter;
        lastChapterWinner = chapterWinner;
        scoreboard.recordWin(chapterWinner);

        warCards.clear();
        proposedTeam.clear();

        if (scoreboard.winner().isPresent()) {
            phase = Phase.GAME_OVER;
        } else {
            currentChapter++;
            captainIndex = (captainIndex + 1) % players.size();
            phase = Phase.TEAM_PROPOSAL;
        }
    }

    private Player findPlayer(UUID id){
        for (Player player : players) {
            if (player.getId().equals(id)) {
                return player;
            }
        }
        throw new IllegalArgumentException("no such player: " + id);
    }
}
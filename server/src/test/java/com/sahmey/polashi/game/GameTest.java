package com.sahmey.polashi.game;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameTest {

    private Game gameWithPlayers(int count) {
        Game game = new Game();
        for (int i = 0; i < count; i++) {
            game.addPlayer("player" + i, null);
        }
        return game;
    }

    @Test
    void startAssignsACharacterToEveryPlayerMatchingTheirFaction() {
        Game game = gameWithPlayers(7);
        game.start();

        for (Player player : game.getPlayers()) {
            assertNotNull(player.getCharacter(), player.getNickname() + " should have a character");
            assertEquals(player.getRole(), player.getCharacter().getFaction());
        }
    }

    @Test
    void startAssignsCharactersMatchingTheRosterForPlayerCount() {
        for (int players = 5; players <= 10; players++) {
            Game game = gameWithPlayers(players);
            game.start();

            Set<Character> assigned = game.getPlayers().stream()
                .map(Player::getCharacter)
                .collect(Collectors.toSet());

            assertEquals(players, assigned.size(), "players=" + players);

            // Legal set, not a second randomized nawabRoster() call (which could draw a
            // different subset than start() actually used and flake this assertion).
            Set<Character> legal = new HashSet<>(CharacterRoster.eicRoster(players));
            legal.add(Character.MIR_MODON);
            legal.add(Character.MOHAN_LAL);
            legal.add(Character.SIRAJ_UD_DAULAH);
            legal.add(Character.LUTFUNNISA_BEGUM);
            legal.add(Character.ST_FRAIS);
            int nawabNeeded = players - FactionTable.redCount(players);
            if (nawabNeeded == 6) {
                legal.add(Character.BENGALI_NOBLEMAN);
            }

            assertTrue(legal.containsAll(assigned), "players=" + players + " assigned=" + assigned);
        }
    }

    @Test
    void mirJafarAndGhosetiBegumSeeEachOtherClearly() {
        Game game = gameWithPlayers(10);
        game.start();

        Player jafar = findByCharacter(game, Character.MIR_JAFAR);
        Player ghoseti = findByCharacter(game, Character.GHOSETI_BEGUM);
        Player durlabh = findByCharacter(game, Character.RAY_DURLABH);

        Set<Player> jafarSees = game.getClearSight(jafar.getId()).stream()
            .map(CharacterSight.SightEntry::player)
            .collect(Collectors.toSet());
        assertEquals(Set.of(ghoseti, durlabh), jafarSees);
    }

    @Test
    void mohanLalSeesModonAndGhosetiButNotWhichIsWhich() {
        Game game = gameWithPlayers(10);
        game.start();

        Player mohanLal = findByCharacter(game, Character.MOHAN_LAL);
        Player modon = findByCharacter(game, Character.MIR_MODON);
        Player ghoseti = findByCharacter(game, Character.GHOSETI_BEGUM);

        assertTrue(game.getClearSight(mohanLal.getId()).isEmpty());
        assertEquals(Set.of(modon, ghoseti), Set.copyOf(game.getConfusedSight(mohanLal.getId())));
    }

    private Player findByCharacter(Game game, Character character) {
        return game.getPlayers().stream()
            .filter(p -> p.getCharacter() == character)
            .findFirst()
            .orElseThrow();
    }

    @Test
    void startAssignsARoleToEveryPlayer() {
        Game game = gameWithPlayers(5);
        game.start();

        for (Player player : game.getPlayers()) {
            assertNotNull(player.getRole(), player.getNickname() + " should have a role after start()");
        }
    }

    @Test
    void startAssignsCorrectNumberOfRedRoles() {
        Game game = gameWithPlayers(7);
        game.start();

        long redCount = game.getPlayers().stream()
            .filter(p -> p.getRole() == Faction.EIC)
            .count();

        assertEquals(FactionTable.redCount(7), redCount);
    }

    @Test
    void startShufflesRoleAssignment() {
        // Run many fresh 5-player games and confirm the *first* player added
        // isn't always assigned the same faction -- proves start() actually
        // shuffles instead of always assigning roles in player order.
        Set<Faction> firstPlayerRolesSeen = new HashSet<>();

        for (int i = 0; i < 200; i++) {
            Game game = gameWithPlayers(5);
            game.start();
            firstPlayerRolesSeen.add(game.getPlayers().get(0).getRole());
        }

        assertEquals(Set.of(Faction.EIC, Faction.NAWAB), firstPlayerRolesSeen);
    }

    @Test
    void firstCaptainIsFirstPlayerAdded() {
        Game game = gameWithPlayers(5);
        Player first = game.getPlayers().get(0);
        game.start();

        assertEquals(first.getId(), game.getCurrentCaptain().getId());
    }

    @Test
    void startMovesPhaseToTeamProposal() {
        Game game = gameWithPlayers(5);
        game.start();

        assertTrue(game.getPhase() == Phase.TEAM_PROPOSAL);
    }

    @Test
    void captainCanProposeAValidSizedTeam() {
        Game game = gameWithPlayers(7);
        game.start();
        Player captain = game.getCurrentCaptain();

        int size = MapCard.teamSize(7, game.getCurrentChapter());
        List<UUID> teamIds = game.getPlayers().stream()
            .limit(size)
            .map(Player::getId)
            .collect(Collectors.toList());

        game.proposeTeam(captain.getId(), teamIds);

        assertEquals(Phase.VOTING, game.getPhase());
        assertEquals(size, game.getProposedTeam().size());
    }

    @Test
    void nonCaptainCannotProposeATeam() {
        Game game = gameWithPlayers(7);
        game.start();
        Player captain = game.getCurrentCaptain();
        Player notCaptain = game.getPlayers().stream()
            .filter(p -> !p.getId().equals(captain.getId()))
            .findFirst()
            .orElseThrow();

        int size = MapCard.teamSize(7, game.getCurrentChapter());
        List<UUID> teamIds = game.getPlayers().stream()
            .limit(size)
            .map(Player::getId)
            .collect(Collectors.toList());

        assertThrows(IllegalArgumentException.class,
            () -> game.proposeTeam(notCaptain.getId(), teamIds));
    }

    @Test
    void wrongSizedTeamIsRejected() {
        Game game = gameWithPlayers(7);
        game.start();
        Player captain = game.getCurrentCaptain();

        int wrongSize = MapCard.teamSize(7, game.getCurrentChapter()) + 1;
        List<UUID> teamIds = game.getPlayers().stream()
            .limit(wrongSize)
            .map(Player::getId)
            .collect(Collectors.toList());

        assertThrows(IllegalArgumentException.class,
            () -> game.proposeTeam(captain.getId(), teamIds));
    }

    private void proposeValidTeam(Game game) {
        Player captain = game.getCurrentCaptain();
        int size = MapCard.teamSize(game.getPlayers().size(), game.getCurrentChapter());
        List<UUID> teamIds = game.getPlayers().stream()
            .limit(size)
            .map(Player::getId)
            .collect(Collectors.toList());
        game.proposeTeam(captain.getId(), teamIds);
    }

    @Test
    void majorityApproveMovesPhaseToWarCards() {
        Game game = gameWithPlayers(7);
        game.start();
        proposeValidTeam(game);

        for (Player p : game.getPlayers()) {
            game.castVote(p.getId(), true);
        }

        assertEquals(Phase.WAR_CARDS, game.getPhase());
        assertEquals(7, game.getLastVoteApproveCount());
    }

    @Test
    void majorityRejectReturnsToTeamProposalAndRotatesCaptain() {
        Game game = gameWithPlayers(7);
        game.start();
        Player firstCaptain = game.getCurrentCaptain();
        proposeValidTeam(game);

        for (Player p : game.getPlayers()) {
            game.castVote(p.getId(), false);
        }

        assertEquals(Phase.TEAM_PROPOSAL, game.getPhase());
        assertNotEquals(firstCaptain.getId(), game.getCurrentCaptain().getId());
        assertEquals(0, game.getLastVoteApproveCount());
    }

    @Test
    void thirdConsecutiveRejectionEndsTheGame() {
        Game game = gameWithPlayers(7);
        game.start();

        for (int i = 0; i < 3; i++) {
            proposeValidTeam(game);
            for (Player p : game.getPlayers()) {
                game.castVote(p.getId(), false);
            }
        }

        assertEquals(Phase.GAME_OVER, game.getPhase());
    }

    @Test
    void nawabCannotPlayRed() {
        Game game = gameWithPlayers(7);
        game.start();
        proposeValidTeam(game);
        for (Player p : game.getPlayers()) game.castVote(p.getId(), true);

        Player teamMember = game.getProposedTeam().get(0);
        teamMember.setRole(Faction.NAWAB);

        assertThrows(IllegalArgumentException.class,
            () -> game.playWarCard(teamMember.getId(), true));
        assertEquals(Phase.WAR_CARDS, game.getPhase());
    }

    @Test
    void nonTeamMemberCannotPlayWarCard() {
        Game game = gameWithPlayers(7);
        game.start();
        proposeValidTeam(game);
        for (Player p : game.getPlayers()) game.castVote(p.getId(), true);

        Player notOnTeam = game.getPlayers().stream()
            .filter(p -> game.getProposedTeam().stream().noneMatch(t -> t.getId().equals(p.getId())))
            .findFirst()
            .orElseThrow();

        assertThrows(IllegalArgumentException.class,
            () -> game.playWarCard(notOnTeam.getId(), false));
    }

    @Test
    void chapterResolutionAdvancesToNextChapterWhenGameContinues() {
        Game game = gameWithPlayers(7);
        game.start();
        proposeValidTeam(game);
        for (Player p : game.getPlayers()) game.castVote(p.getId(), true);

        List<Player> team = game.getProposedTeam();
        int threshold = MapCard.redsToWin(7, game.getCurrentChapter());
        for (int i = 0; i < team.size(); i++) {
            team.get(i).setRole(i < threshold ? Faction.EIC : Faction.NAWAB);
        }
        for (int i = 0; i < team.size(); i++) {
            game.playWarCard(team.get(i).getId(), i < threshold);
        }

        assertEquals(2, game.getCurrentChapter());
        assertEquals(Phase.TEAM_PROPOSAL, game.getPhase());
        assertEquals(Faction.EIC, game.getLastChapterWinner());
        assertEquals(threshold, game.getLastChapterRedsPlayed());
        assertEquals(team.size(), game.getLastChapterTeamSize());
        assertEquals(1, game.getLastChapterNumber());
    }

    @Test
    void eicWinsGameAfterThreeChaptersWon() {
        Game game = gameWithPlayers(7);
        game.start();

        for (int chapter = 0; chapter < 3; chapter++) {
            proposeValidTeam(game);
            for (Player p : game.getPlayers()) game.castVote(p.getId(), true);

            List<Player> team = game.getProposedTeam();
            int threshold = MapCard.redsToWin(7, game.getCurrentChapter());
            for (int i = 0; i < team.size(); i++) {
                team.get(i).setRole(i < threshold ? Faction.EIC : Faction.NAWAB);
            }
            for (int i = 0; i < team.size(); i++) {
                game.playWarCard(team.get(i).getId(), i < threshold);
            }
        }

        assertEquals(Phase.GAME_OVER, game.getPhase());
        assertEquals(Optional.of(Faction.EIC), game.getScoreboard().winner());
    }

    @Test
    void concurrentWarCardsResolveTheChapterExactlyOnce() throws Exception {
        // Regression test: real WebSocket connections each run on their own Tomcat
        // thread, so team members' playWarCard calls can race. Without
        // synchronization in Game, multiple threads can observe "team complete"
        // and all call resolveChapter(), double-advancing currentChapter/scoreboard.
        // A single race attempt is timing-dependent, so this repeats the race many
        // times -- a low per-attempt failure probability compounds close to certainty.
        for (int attempt = 0; attempt < 50; attempt++) {
            Game game = gameWithPlayers(10);
            game.start();
            proposeValidTeam(game);
            for (Player p : game.getPlayers()) game.castVote(p.getId(), true);

            List<Player> team = game.getProposedTeam();
            int chapterBefore = game.getCurrentChapter();

            ExecutorService pool = Executors.newFixedThreadPool(team.size());
            CountDownLatch ready = new CountDownLatch(team.size());
            CountDownLatch go = new CountDownLatch(1);
            for (Player p : team) {
                pool.submit(() -> {
                    ready.countDown();
                    try {
                        go.await();
                    } catch (InterruptedException ignored) {
                    }
                    game.playWarCard(p.getId(), false);
                });
            }
            ready.await();
            go.countDown();
            pool.shutdown();
            assertTrue(pool.awaitTermination(5, TimeUnit.SECONDS));

            assertEquals(chapterBefore + 1, game.getCurrentChapter(),
                "attempt " + attempt + ": chapter should advance by exactly one");
            assertEquals(1, game.getScoreboard().winsFor(Faction.NAWAB),
                "attempt " + attempt + ": chapter should resolve exactly once");
        }
    }
}

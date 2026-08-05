import { useEffect, useRef, useState } from "react";
import { connect } from "./socket";
import Home from "./screens/Home";
import Lobby from "./screens/Lobby";
import RoleReveal from "./screens/RoleReveal";
import ChapterBoard from "./screens/ChapterBoard";
import Vote from "./screens/Vote";
import VoteReveal from "./screens/VoteReveal";
import War from "./screens/War";
import WarReveal from "./screens/WarReveal";
import GameOver from "./screens/GameOver";
import Roadmap from "./screens/Roadmap";
import RulesButton from "./screens/RulesButton";
import RulesModal from "./screens/RulesModal";
import backgroundImage from "./assets/background.jpeg";
import "./App.css";

export default function App() {
  const connRef = useRef(null);
  const [view, setView] = useState("home");
  const [error, setError] = useState(null);
  const [rulesOpen, setRulesOpen] = useState(false);

  const [roomCode, setRoomCode] = useState(null);
  const [myPlayerId, setMyPlayerId] = useState(null);
  const [isHost, setIsHost] = useState(false);
  const [players, setPlayers] = useState([]);

  const [myRole, setMyRole] = useState(null);
  const [chapterState, setChapterState] = useState(null);
  const [currentPhase, setCurrentPhase] = useState(null);
  const [voteProgress, setVoteProgress] = useState(null);
  const [voteResult, setVoteResult] = useState(null);
  const [warProgress, setWarProgress] = useState(null);
  const [warResult, setWarResult] = useState(null);
  const [tally, setTally] = useState({});
  const [chapterResults, setChapterResults] = useState([]);
  const [gameOver, setGameOver] = useState(null);

  // Role reveal / vote reveal / war reveal are dramatic-pacing gates: the actual
  // game keeps moving server-side regardless of when a given player dismisses
  // them, so a slow player's underlying state (chapterState/voteResult/warResult)
  // can go stale relative to what's shown. REVEAL_VIEWS are "sticky" -- no
  // incoming event is allowed to change the view while pinned on one, only the
  // Continue button may leave it, and it routes off currentPhase/gameOver (the
  // single latest-known truth) rather than whatever phase happened to be current
  // when the screen was first entered.
  const REVEAL_VIEWS = ["roleReveal", "voteReveal", "warReveal"];

  function nextViewFromCurrentState() {
    if (gameOver) return "gameOver";
    if (currentPhase === "VOTING") return "vote";
    if (currentPhase === "WAR_CARDS") return "war";
    return "chapterBoard";
  }

  useEffect(() => {
    if (!error) return;
    const t = setTimeout(() => setError(null), 4000);
    return () => clearTimeout(t);
  }, [error]);

  function ensureConnection() {
    if (connRef.current) return connRef.current;
    const conn = connect(handleEvent);
    connRef.current = conn;
    return conn;
  }

  function handleEvent(msg) {
    switch (msg.type) {
      case "error":
        setError(msg.message);
        break;
      case "joined":
        setRoomCode(msg.roomCode);
        setMyPlayerId(msg.playerId);
        break;
      case "roomState":
        setPlayers(msg.players);
        setView((v) => (v === "home" ? "lobby" : v));
        break;
      case "privateRole":
        setMyRole(msg);
        setView("roleReveal");
        break;
      case "chapterState":
        setChapterState(msg);
        setCurrentPhase(msg.phase);
        setView((v) => (REVEAL_VIEWS.includes(v) ? v : (msg.phase === "VOTING" ? "vote" : "chapterBoard")));
        break;
      case "voteProgress":
        setVoteProgress(msg);
        break;
      case "voteResult":
        setVoteResult(msg);
        setCurrentPhase(msg.phase);
        setView((v) => (REVEAL_VIEWS.includes(v) ? v : "voteReveal"));
        break;
      case "warProgress":
        setWarProgress(msg);
        break;
      case "warResult":
        setWarResult(msg);
        setCurrentPhase(msg.phase);
        setTally((t) => ({ ...t, [msg.chapterWinner]: (t[msg.chapterWinner] ?? 0) + 1 }));
        setChapterResults((r) => [...r, { chapter: msg.chapter, winner: msg.chapterWinner }]);
        setView((v) => (REVEAL_VIEWS.includes(v) ? v : "warReveal"));
        break;
      case "gameOver":
        setGameOver(msg);
        setView((v) => (REVEAL_VIEWS.includes(v) ? v : "gameOver"));
        break;
      default:
        break;
    }
  }

  function handleCreate(nickname) {
    const conn = ensureConnection();
    setIsHost(true);
    conn.send("createRoom", { nickname });
  }

  function handleJoin(nickname, code) {
    const conn = ensureConnection();
    setIsHost(false);
    conn.send("joinRoom", { nickname, roomCode: code });
  }

  function handleStart() {
    connRef.current.send("startGame");
  }

  function handlePropose(playerIds) {
    connRef.current.send("proposeTeam", { playerIds });
  }

  function handleVote(approve) {
    connRef.current.send("castVote", { approve });
  }

  function handlePlayCard(red) {
    connRef.current.send("playWarCard", { red });
  }

  function handleRevealContinue() {
    setView(nextViewFromCurrentState());
  }

  function renderView() {
    switch (view) {
      case "home":
        return <Home onCreate={handleCreate} onJoin={handleJoin} error={error} />;

      case "lobby":
        return (
          <Lobby
            roomCode={roomCode}
            players={players}
            myPlayerId={myPlayerId}
            isHost={isHost}
            onStart={handleStart}
          />
        );

      case "roleReveal":
        return <RoleReveal role={myRole} onContinue={handleRevealContinue} />;

      case "chapterBoard":
        return (
          <ChapterBoard
            chapterState={chapterState}
            players={players}
            myPlayerId={myPlayerId}
            tally={tally}
            onPropose={handlePropose}
            roadmap={<Roadmap chapterResults={chapterResults} animateLatest />}
          />
        );

      case "vote":
        return (
          <Vote
            key={`${chapterState.chapter}-${chapterState.proposedTeam.join(",")}`}
            chapterState={chapterState}
            players={players}
            voteProgress={voteProgress}
            onVote={handleVote}
            roadmap={<Roadmap chapterResults={chapterResults} />}
          />
        );

      case "war":
        return (
          <War
            key={`${chapterState.chapter}-${chapterState.proposedTeam.join(",")}`}
            chapterState={chapterState}
            myPlayerId={myPlayerId}
            myRole={myRole}
            warProgress={warProgress}
            onPlay={handlePlayCard}
            roadmap={<Roadmap chapterResults={chapterResults} />}
          />
        );

      case "voteReveal":
        return <VoteReveal voteResult={voteResult} onContinue={handleRevealContinue} />;

      case "warReveal":
        return <WarReveal warResult={warResult} onContinue={handleRevealContinue} />;

      case "gameOver":
        return (
          <GameOver
            winner={gameOver.winner}
            tally={tally}
            roadmap={<Roadmap chapterResults={chapterResults} />}
          />
        );

      default:
        return null;
    }
  }

  return (
    <>
      <div className="app-backdrop" style={{ backgroundImage: `url(${backgroundImage})` }} />
      <div className="app-content">
        <RulesButton onClick={() => setRulesOpen(true)} />
        {renderView()}
        {rulesOpen && <RulesModal onClose={() => setRulesOpen(false)} />}
      </div>
    </>
  );
}

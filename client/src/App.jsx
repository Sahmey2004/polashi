import { useEffect, useRef, useState } from "react";
import { connect } from "./socket";
import Home from "./screens/Home";
import Lobby from "./screens/Lobby";
import RoleReveal from "./screens/RoleReveal";
import ChapterBoard from "./screens/ChapterBoard";
import Vote from "./screens/Vote";
import War from "./screens/War";
import GameOver from "./screens/GameOver";
import "./App.css";

export default function App() {
  const connRef = useRef(null);
  const [view, setView] = useState("home");
  const [error, setError] = useState(null);

  const [roomCode, setRoomCode] = useState(null);
  const [myPlayerId, setMyPlayerId] = useState(null);
  const [isHost, setIsHost] = useState(false);
  const [players, setPlayers] = useState([]);

  const [myRole, setMyRole] = useState(null);
  const [chapterState, setChapterState] = useState(null);
  const [voteProgress, setVoteProgress] = useState(null);
  const [warProgress, setWarProgress] = useState(null);
  const [tally, setTally] = useState({});
  const [gameOver, setGameOver] = useState(null);

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
        setView((v) => {
          if (v === "roleReveal") return v;
          return msg.phase === "VOTING" ? "vote" : "chapterBoard";
        });
        break;
      case "voteProgress":
        setVoteProgress(msg);
        break;
      case "voteResult":
        if (msg.phase === "WAR_CARDS") setView("war");
        break;
      case "warProgress":
        setWarProgress(msg);
        break;
      case "warResult":
        setTally((t) => ({ ...t, [msg.chapterWinner]: (t[msg.chapterWinner] ?? 0) + 1 }));
        break;
      case "gameOver":
        setGameOver(msg);
        setView("gameOver");
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
      return (
        <RoleReveal role={myRole} onContinue={() => setView("chapterBoard")} />
      );

    case "chapterBoard":
      return (
        <ChapterBoard
          chapterState={chapterState}
          players={players}
          myPlayerId={myPlayerId}
          tally={tally}
          onPropose={handlePropose}
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
        />
      );

    case "gameOver":
      return <GameOver winner={gameOver.winner} tally={tally} />;

    default:
      return null;
  }
}

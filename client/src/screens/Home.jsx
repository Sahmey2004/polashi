import { useState } from "react";

export default function Home({ onCreate, onJoin, error }) {
  const [nickname, setNickname] = useState("");
  const [roomCode, setRoomCode] = useState("");

  return (
    <div className="card">
      <h1>Polashi</h1>
      <p className="subtitle">A hidden-role game of trade and treachery.</p>

      <label className="field">
        <span>Your name</span>
        <input
          value={nickname}
          onChange={(e) => setNickname(e.target.value)}
          placeholder="Sam"
          maxLength={20}
        />
      </label>

      <div className="actions">
        <button
          disabled={!nickname.trim()}
          onClick={() => onCreate(nickname.trim())}
        >
          Create room
        </button>
      </div>

      <div className="divider">or</div>

      <label className="field">
        <span>Room code</span>
        <input
          value={roomCode}
          onChange={(e) => setRoomCode(e.target.value.toUpperCase())}
          placeholder="Q8J9B"
          maxLength={5}
        />
      </label>
      <div className="actions">
        <button
          disabled={!nickname.trim() || !roomCode.trim()}
          onClick={() => onJoin(nickname.trim(), roomCode.trim())}
        >
          Join room
        </button>
      </div>

      {error && <p className="error">{error}</p>}
    </div>
  );
}

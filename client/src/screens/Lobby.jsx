const MIN_PLAYERS = 5;

export default function Lobby({ roomCode, players, myPlayerId, isHost, onStart }) {
  return (
    <div className="card">
      <h1>Lobby</h1>
      <p className="subtitle">
        Room code <strong>{roomCode}</strong> — share it so others can join.
      </p>

      <ul className="player-list">
        {players.map((p) => (
          <li key={p.id}>
            {p.nickname}
            {p.id === myPlayerId && <span className="you-tag">you</span>}
          </li>
        ))}
      </ul>

      {isHost ? (
        <>
          <button disabled={players.length < MIN_PLAYERS} onClick={onStart}>
            Start game
          </button>
          {players.length < MIN_PLAYERS && (
            <p className="hint">
              Need at least {MIN_PLAYERS} players ({players.length}/{MIN_PLAYERS})
            </p>
          )}
        </>
      ) : (
        <p className="hint">Waiting for the host to start the game…</p>
      )}
    </div>
  );
}

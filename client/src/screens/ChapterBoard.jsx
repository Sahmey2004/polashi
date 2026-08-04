import { useState } from "react";

export default function ChapterBoard({ chapterState, players, myPlayerId, tally, onPropose }) {
  const { chapter, captainId, teamSize } = chapterState;
  const captain = players.find((p) => p.id === captainId);
  const isCaptain = captainId === myPlayerId;
  const [selected, setSelected] = useState([]);

  function toggle(id) {
    setSelected((prev) => {
      if (prev.includes(id)) return prev.filter((x) => x !== id);
      if (prev.length >= teamSize) return prev;
      return [...prev, id];
    });
  }

  return (
    <div className="card">
      <div className="scoreboard">
        <span>EIC {tally.EIC ?? 0}</span>
        <span className="scoreboard-divider">–</span>
        <span>{tally.NAWAB ?? 0} Nawab</span>
      </div>

      <h1>Chapter {chapter}</h1>
      <p className="subtitle">
        Captain: <strong>{captain?.nickname ?? "?"}</strong> is choosing {teamSize} player
        {teamSize === 1 ? "" : "s"} for this chapter's team.
      </p>

      {isCaptain ? (
        <>
          <ul className="player-list selectable">
            {players.map((p) => (
              <li
                key={p.id}
                className={selected.includes(p.id) ? "selected" : ""}
                onClick={() => toggle(p.id)}
              >
                {p.nickname}
              </li>
            ))}
          </ul>
          <button
            disabled={selected.length !== teamSize}
            onClick={() => onPropose(selected)}
          >
            Propose team ({selected.length}/{teamSize})
          </button>
        </>
      ) : (
        <p className="hint">Waiting for {captain?.nickname ?? "the captain"} to propose a team…</p>
      )}
    </div>
  );
}

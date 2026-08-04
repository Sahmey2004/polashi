import { useState } from "react";

export default function War({ chapterState, myPlayerId, myRole, warProgress, onPlay }) {
  const [played, setPlayed] = useState(false);
  const onTeam = chapterState.proposedTeam.includes(myPlayerId);
  const isEic = myRole.role === "EIC";

  function play(red) {
    setPlayed(true);
    onPlay(red);
  }

  if (!onTeam) {
    return (
      <div className="card">
        <h1>War phase</h1>
        <p className="hint">
          You're not on this chapter's team. Waiting for it to resolve
          {warProgress ? ` (${warProgress.cardsPlayed}/${warProgress.teamSize} played)` : ""}…
        </p>
      </div>
    );
  }

  return (
    <div className="card">
      <h1>Play your war card</h1>
      <p className="subtitle">
        {isEic
          ? "As EIC, you may sabotage this chapter with a red card."
          : "As Nawab, you can only play green — you have no red card to play."}
      </p>

      {played ? (
        <p className="hint">Card played. Waiting on the rest of the team…</p>
      ) : (
        <div className="actions">
          <button className="green" onClick={() => play(false)}>
            Play green
          </button>
          {isEic && (
            <button className="red" onClick={() => play(true)}>
              Play red
            </button>
          )}
        </div>
      )}
    </div>
  );
}

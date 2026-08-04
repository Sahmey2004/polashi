import { useState } from "react";

export default function Vote({ chapterState, players, voteProgress, onVote, roadmap }) {
  const [hasVoted, setHasVoted] = useState(false);
  const team = chapterState.proposedTeam
    .map((id) => players.find((p) => p.id === id)?.nickname)
    .filter(Boolean);

  function vote(approve) {
    setHasVoted(true);
    onVote(approve);
  }

  return (
    <div className="card">
      {roadmap}
      <h1>Proposed team</h1>
      <p className="subtitle">{team.join(", ")}</p>

      {hasVoted ? (
        <p className="hint">
          Vote cast. Waiting on the rest of the table
          {voteProgress ? ` (${voteProgress.votesCast}/${voteProgress.totalPlayers})` : ""}…
        </p>
      ) : (
        <div className="actions">
          <button className="approve" onClick={() => vote(true)}>
            Approve
          </button>
          <button className="reject" onClick={() => vote(false)}>
            Reject
          </button>
        </div>
      )}
    </div>
  );
}

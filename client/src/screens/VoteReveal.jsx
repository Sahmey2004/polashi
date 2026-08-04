import { useEffect, useState } from "react";

export default function VoteReveal({ voteResult, onContinue }) {
  const approved = voteResult.phase === "WAR_CARDS";
  const [count, setCount] = useState(0);
  const [settled, setSettled] = useState(false);

  useEffect(() => {
    const target = voteResult.approveCount;
    if (target === 0) {
      setSettled(true);
      return;
    }
    let current = 0;
    const step = setInterval(() => {
      current += 1;
      setCount(current);
      if (current >= target) {
        clearInterval(step);
        setTimeout(() => setSettled(true), 200);
      }
    }, 220);
    return () => clearInterval(step);
  }, [voteResult.approveCount]);

  return (
    <div className={`card role-card ${approved ? "role-nawab" : "role-eic"}`}>
      <p className="dossier-label" style={{ textAlign: "center", marginBottom: 18 }}>
        The table has voted
      </p>

      <div className="stamp-row">
        {Array.from({ length: voteResult.totalVotes }).map((_, i) => (
          <span
            key={i}
            className={`stamp ${i < count ? (approved ? "stamp-approve" : "stamp-reject") : "stamp-pending"}`}
            style={{ animationDelay: `${i * 0.03}s` }}
          />
        ))}
      </div>

      <p className="stamp-tally">
        {count} / {voteResult.totalVotes} approve
      </p>

      {settled && (
        <div className="reveal-in">
          <h1 style={{ textAlign: "center", marginTop: 18 }}>
            {approved ? "Team Approved" : "Team Rejected"}
          </h1>
          <p className="subtitle" style={{ textAlign: "center" }}>
            {approved
              ? "The chosen team proceeds to the chapter."
              : "The captaincy passes to the next player."}
          </p>
          <button onClick={onContinue} style={{ display: "block", margin: "18px auto 0" }}>
            Continue
          </button>
        </div>
      )}
    </div>
  );
}

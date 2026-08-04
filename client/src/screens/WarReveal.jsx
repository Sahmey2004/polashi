import { useMemo, useState, useEffect } from "react";

export default function WarReveal({ warResult, onContinue }) {
  const teamSize = warResult.teamSize;
  const eicWon = warResult.chapterWinner === "EIC";

  // Which face-down cards turn out red is only known in aggregate (redsPlayed) --
  // never tied to a specific player, to preserve who-played-what secrecy. Shuffle
  // position once per reveal so the same seat isn't always "the red one" visually.
  const cardIsRed = useMemo(() => {
    const cards = Array.from({ length: teamSize }, (_, i) => i < warResult.redsPlayed);
    for (let i = cards.length - 1; i > 0; i--) {
      const j = Math.floor(Math.random() * (i + 1));
      [cards[i], cards[j]] = [cards[j], cards[i]];
    }
    return cards;
  }, [teamSize, warResult.redsPlayed]);

  const [flipped, setFlipped] = useState(0);
  const [settled, setSettled] = useState(false);

  useEffect(() => {
    if (flipped >= teamSize) {
      const t = setTimeout(() => setSettled(true), 300);
      return () => clearTimeout(t);
    }
    const t = setTimeout(() => setFlipped((f) => f + 1), 500);
    return () => clearTimeout(t);
  }, [flipped, teamSize]);

  return (
    <div className={`card role-card ${eicWon ? "role-eic" : "role-nawab"}`}>
      <p className="dossier-label" style={{ textAlign: "center", marginBottom: 18 }}>
        Chapter {warResult.chapter} war cards
      </p>

      <div className="war-card-row">
        {cardIsRed.map((isRed, i) => (
          <div key={i} className={`war-card ${i < flipped ? "flipped" : ""}`}>
            <div className="war-card-face war-card-back" />
            <div className={`war-card-face war-card-front ${isRed ? "war-card-red" : "war-card-green"}`} />
          </div>
        ))}
      </div>

      {settled && (
        <div className="reveal-in">
          <h1 style={{ textAlign: "center", marginTop: 22 }}>
            {eicWon ? "The Chapter Falls" : "The Chapter Holds"}
          </h1>
          <p className="subtitle" style={{ textAlign: "center" }}>
            {warResult.redsPlayed} red card{warResult.redsPlayed === 1 ? "" : "s"} played &mdash; the
            chapter goes to {eicWon ? "the East India Company" : "the Nawab"}.
          </p>
          <button onClick={onContinue} style={{ display: "block", margin: "18px auto 0" }}>
            Continue
          </button>
        </div>
      )}
    </div>
  );
}

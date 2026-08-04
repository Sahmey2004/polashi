export default function GameOver({ winner, tally, roadmap }) {
  const eicWon = winner === "EIC";
  return (
    <>
      <div className={`finale-wash ${eicWon ? "finale-wash-eic" : "finale-wash-nawab"}`} />
      <div className={`card role-card finale-card ${eicWon ? "role-eic" : "role-nawab"}`}>
        {roadmap}
        <div className="finale-flourish" />
        <h1 className="finale-title">
          {eicWon ? "The Company Prevails" : "The Nawab's Rule Endures"}
        </h1>
        <p className="subtitle" style={{ textAlign: "center" }}>
          {eicWon
            ? "Betrayal from within has delivered Bengal to the East India Company."
            : "Loyalty has held — the Nawab's court survives the conspiracy."}
        </p>
        <p className="scoreboard finale-tally">
          EIC {tally.EIC ?? 0} &ndash; {tally.NAWAB ?? 0} Nawab
        </p>
        <button onClick={() => window.location.reload()} style={{ display: "block", margin: "20px auto 0" }}>
          Play again
        </button>
      </div>
    </>
  );
}

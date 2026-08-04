export default function GameOver({ winner, tally }) {
  return (
    <div className={`card role-card ${winner === "EIC" ? "role-eic" : "role-nawab"}`}>
      <h1>{winner === "EIC" ? "EIC wins" : "Nawab wins"}</h1>
      <p className="subtitle">
        Final chapters: EIC {tally.EIC ?? 0} – {tally.NAWAB ?? 0} Nawab
      </p>
      <button onClick={() => window.location.reload()}>Play again</button>
    </div>
  );
}

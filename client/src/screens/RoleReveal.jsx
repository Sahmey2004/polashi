import { useState } from "react";

export default function RoleReveal({ role, onContinue }) {
  const [broken, setBroken] = useState(false);
  const isEic = role.role === "EIC";

  return (
    <div className={`card role-card ${broken ? (isEic ? "role-eic" : "role-nawab") : ""}`}>
      {!broken ? (
        <div className="seal-stage" onClick={() => setBroken(true)}>
          <div className="wax-seal wax-neutral">
            <span className="wax-seal-glyph">&#10070;</span>
          </div>
          <p className="hint seal-prompt">Break the seal to learn your role</p>
        </div>
      ) : (
        <div className="reveal-stage">
          <div className={`wax-seal-shard ${isEic ? "wax-eic" : "wax-nawab"}`} />

          <h1 className="reveal-in">{role.character}</h1>
          <p className="subtitle reveal-in reveal-in-2">
            {isEic
              ? "You serve the East India Company. Sabotage chapters by secretly playing red war cards."
              : "You serve the Nawab. Vote wisely and hope your teams stay honest."}
          </p>

          {(role.clearSight.length > 0 || role.confusedSight.length > 0) && (
            <div className="dossier reveal-in reveal-in-3">
              <p className="dossier-label">Your knowledge</p>
              {role.clearSight.map((s) => (
                <p key={s.playerId} className="dossier-line">
                  <strong>{s.nickname}</strong> is {s.character}
                </p>
              ))}
              {role.confusedSight.length > 0 && (
                <p className="dossier-line">
                  Two of these are notable, but you cannot tell who is who:{" "}
                  <strong>{role.confusedSight.map((s) => s.nickname).join(", ")}</strong>
                </p>
              )}
            </div>
          )}

          <button className="reveal-in reveal-in-4" onClick={onContinue}>
            Continue
          </button>
        </div>
      )}
    </div>
  );
}

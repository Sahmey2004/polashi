export default function RoleReveal({ role, onContinue }) {
  const isEic = role.role === "EIC";
  return (
    <div className={`card role-card ${isEic ? "role-eic" : "role-nawab"}`}>
      <h1>{isEic ? "You are EIC" : "You are Nawab"}</h1>
      <p className="subtitle">
        {isEic
          ? "You're a traitor. Sabotage chapters by secretly playing red war cards."
          : "You're loyal. Vote wisely and hope your teams stay honest."}
      </p>

      {isEic && (
        <div className="teammates">
          <p>Your fellow EIC:</p>
          {role.teammates.length > 0 ? (
            <ul>
              {role.teammates.map((name) => (
                <li key={name}>{name}</li>
              ))}
            </ul>
          ) : (
            <p className="hint">You're the only EIC this game.</p>
          )}
        </div>
      )}

      <button onClick={onContinue}>Continue</button>
    </div>
  );
}

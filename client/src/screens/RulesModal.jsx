import { useState } from "react";

const PLAYER_COUNTS = [5, 6, 7, 8, 9, 10];

const RED_COUNTS = { 5: 2, 6: 2, 7: 3, 8: 3, 9: 3, 10: 4 };

const TEAM_SIZES = {
  5: [2, 3, 2, 3, 3],
  6: [2, 3, 4, 3, 4],
  7: [2, 3, 3, 4, 4],
  8: [3, 4, 4, 5, 5],
  9: [3, 4, 4, 5, 5],
  10: [3, 4, 4, 5, 5],
};

const TABS = ["Who Sees Who", "Voting", "Team Composition"];

export default function RulesModal({ onClose }) {
  const [tab, setTab] = useState(0);

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-card" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <h2>Rules</h2>
          <button className="modal-close" onClick={onClose} aria-label="Close">
            &times;
          </button>
        </div>

        <div className="modal-tabs">
          {TABS.map((label, i) => (
            <button
              key={label}
              className={`modal-tab ${tab === i ? "modal-tab-active" : ""}`}
              onClick={() => setTab(i)}
            >
              {label}
            </button>
          ))}
        </div>

        <div className="modal-body">
          {tab === 0 && <SightChart />}
          {tab === 1 && <VotingRules />}
          {tab === 2 && <TeamComposition />}
        </div>
      </div>
    </div>
  );
}

function SightChart() {
  return (
    <div>
      <p className="rules-intro">
        Most characters know nothing beyond their own faction. A handful carry
        secret knowledge of specific others:
      </p>
      <svg viewBox="0 0 580 290" className="sight-chart">
        <text x="130" y="20" textAnchor="middle" className="sight-faction-label sight-nawab-label">
          NAWAB PAKSHA
        </text>
        <text x="450" y="20" textAnchor="middle" className="sight-faction-label sight-eic-label">
          EIC PAKSHA
        </text>

        {/* Nawab side */}
        <rect x="20" y="32" width="180" height="30" rx="5" className="sight-node sight-node-filler" />
        <text x="110" y="51" textAnchor="middle" className="sight-node-text">Siraj-ud-Daulah</text>

        <rect x="20" y="68" width="180" height="30" rx="5" className="sight-node sight-node-filler" />
        <text x="110" y="87" textAnchor="middle" className="sight-node-text">Lutfunnisa Begum</text>

        <rect x="20" y="112" width="180" height="32" rx="5" className="sight-node sight-node-power sight-node-nawab" />
        <text x="110" y="132" textAnchor="middle" className="sight-node-text sight-node-text-bold">Mir Modon</text>

        <rect x="20" y="156" width="180" height="32" rx="5" className="sight-node sight-node-power sight-node-nawab" />
        <text x="110" y="176" textAnchor="middle" className="sight-node-text sight-node-text-bold">Mohan Lal</text>

        <rect x="20" y="200" width="180" height="30" rx="5" className="sight-node sight-node-filler" />
        <text x="110" y="219" textAnchor="middle" className="sight-node-text">St. Frais</text>

        <rect x="20" y="238" width="180" height="28" rx="5" className="sight-node sight-node-filler" />
        <text x="110" y="256" textAnchor="middle" className="sight-node-text sight-node-text-italic">
          Bengali Nobleman (9-10p)
        </text>

        {/* EIC side */}
        <rect x="380" y="80" width="180" height="32" rx="5" className="sight-node sight-node-power sight-node-eic" />
        <text x="470" y="100" textAnchor="middle" className="sight-node-text sight-node-text-bold">Mir Jafar</text>

        <rect x="380" y="124" width="180" height="32" rx="5" className="sight-node sight-node-power sight-node-eic" />
        <text x="470" y="144" textAnchor="middle" className="sight-node-text sight-node-text-bold">Ghoseti Begum</text>

        <rect x="380" y="168" width="180" height="30" rx="5" className="sight-node sight-node-filler" />
        <text x="470" y="187" textAnchor="middle" className="sight-node-text sight-node-text-italic">Ray Durlabh (10p only)</text>

        <rect x="380" y="204" width="180" height="30" rx="5" className="sight-node sight-node-filler" />
        <text x="470" y="223" textAnchor="middle" className="sight-node-text">Omichand</text>

        <defs>
          <marker id="rules-arrow-clear" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="6" markerHeight="6" orient="auto-start-reverse">
            <path d="M0,0 L10,5 L0,10 z" className="sight-arrow-clear-fill" />
          </marker>
          <marker id="rules-arrow-confused" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="6" markerHeight="6" orient="auto-start-reverse">
            <path d="M0,0 L10,5 L0,10 z" className="sight-arrow-confused-fill" />
          </marker>
        </defs>

        {/* Mir Jafar <-> Ghoseti Begum, mutual */}
        <line x1="470" y1="112" x2="470" y2="124" className="sight-line-clear" markerEnd="url(#rules-arrow-clear)" markerStart="url(#rules-arrow-clear)" />

        {/* Mir Modon -> Ghoseti Begum */}
        <line x1="200" y1="128" x2="378" y2="138" className="sight-line-clear" markerEnd="url(#rules-arrow-clear)" />

        {/* Mohan Lal -> Mir Modon, -> Ghoseti Begum (confused) */}
        <line x1="110" y1="156" x2="110" y2="146" className="sight-line-confused" markerEnd="url(#rules-arrow-confused)" />
        <line x1="200" y1="182" x2="378" y2="146" className="sight-line-confused" markerEnd="url(#rules-arrow-confused)" />
      </svg>

      <div className="sight-legend">
        <div><span className="sight-legend-swatch sight-legend-clear" /> knows exact identity</div>
        <div><span className="sight-legend-swatch sight-legend-confused" /> sees, but can't tell who's who</div>
      </div>

      <ul className="rules-list">
        <li>Mir Jafar and Ghoseti Begum always know each other on sight.</li>
        <li>When Ray Durlabh is in play (10-player games), he and Ghoseti Begum and Mir Jafar all see each other too.</li>
        <li>Mir Modon knows Ghoseti Begum's identity — but she does not know his.</li>
        <li>Mohan Lal is shown which two players are Mir Modon and Ghoseti Begum, but not which is which.</li>
        <li>Every other role sees no one, and is seen by no one.</li>
      </ul>
    </div>
  );
}

function VotingRules() {
  return (
    <div>
      <ol className="rules-list rules-list-numbered">
        <li>The current captain proposes a team of the size required for that chapter.</li>
        <li>Every player votes to <strong>Approve</strong> or <strong>Reject</strong> the proposed team.</li>
        <li>A strict majority must approve for the team to go forward — a tie counts as rejected.</li>
        <li>If approved, the proposed team moves on to play war cards for the chapter.</li>
        <li>If rejected, the captaincy passes to the next player and a new team is proposed for the same chapter.</li>
        <li>
          <strong>Three rejected proposals in a row ends the game immediately</strong> — a stalled
          table hands victory straight to the East India Company.
        </li>
      </ol>
    </div>
  );
}

function TeamComposition() {
  return (
    <div>
      <h3 className="rules-table-title">Faction split by player count</h3>
      <div className="rules-table-scroll">
        <table className="rules-table">
          <thead>
            <tr>
              <th>Total players</th>
              {PLAYER_COUNTS.map((p) => (
                <th key={p}>{p}</th>
              ))}
            </tr>
          </thead>
          <tbody>
            <tr>
              <td className="rules-table-row-label rules-table-row-eic">EIC (red)</td>
              {PLAYER_COUNTS.map((p) => (
                <td key={p}>{RED_COUNTS[p]}</td>
              ))}
            </tr>
            <tr>
              <td className="rules-table-row-label rules-table-row-nawab">Nawab (green)</td>
              {PLAYER_COUNTS.map((p) => (
                <td key={p}>{p - RED_COUNTS[p]}</td>
              ))}
            </tr>
          </tbody>
        </table>
      </div>

      <h3 className="rules-table-title">Team size per round</h3>
      <div className="rules-table-scroll">
        <table className="rules-table">
          <thead>
            <tr>
              <th>Total players</th>
              {PLAYER_COUNTS.map((p) => (
                <th key={p}>{p}</th>
              ))}
            </tr>
          </thead>
          <tbody>
            {[0, 1, 2, 3, 4].map((roundIdx) => (
              <tr key={roundIdx}>
                <td className="rules-table-row-label">{roundIdx + 1}{ordinalSuffix(roundIdx + 1)} round</td>
                {PLAYER_COUNTS.map((p) => (
                  <td key={p}>{TEAM_SIZES[p][roundIdx]}</td>
                ))}
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}

function ordinalSuffix(n) {
  if (n === 1) return "st";
  if (n === 2) return "nd";
  if (n === 3) return "rd";
  return "th";
}

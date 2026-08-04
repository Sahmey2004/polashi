# Polashi — Step-by-Step Build Guide (Learn by Doing)

This guide walks you through building Polashi from nothing to a deployed,
publicly playable web game. Follow it in order. Each phase has:

- **Goal** — what you'll have working at the end.
- **Steps** — exact commands / code.
- **Why** — the reasoning, so you learn and not just copy.
- **Checkpoint** — how to prove it works before moving on.
- **Your turn** — a small exercise to cement the learning.

The design this follows: `docs/superpowers/specs/2026-08-04-polashi-design.md`.

Architecture recap: **Java (Spring Boot) authoritative server** holds all game
state and talks JSON over **WebSockets** to a **React browser client**. Server
deploys to Render/Railway; client deploys to Vercel and is proxied at
`/polashi`.

---

## Phase 0 — Prerequisites

**Goal:** the right tools installed and verified.

### Steps

1. Install JDK 21 (your current Java 8 is too old for Spring Boot 3):

   ```bash
   brew install openjdk@21
   ```

2. Make it your active Java. Homebrew prints the exact line; on Apple Silicon
   it's usually:

   ```bash
   sudo ln -sfn /opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk \
     /Library/Java/JavaVirtualMachines/openjdk-21.jdk
   ```

   Then add to `~/.zshrc`:

   ```bash
   export JAVA_HOME=$(/usr/libexec/java_home -v 21)
   export PATH="$JAVA_HOME/bin:$PATH"
   ```

   Reload: `source ~/.zshrc`

3. Verify everything:

   ```bash
   java -version   # should say 21
   node -v         # 25.x — good
   git --version   # good
   ```

### Why

Spring Boot 3 dropped support for Java 8/11 and requires Java 17+. JDK 21 is the
current long-term-support release, so tutorials and hosting platforms all
support it. `JAVA_HOME` tells build tools which JDK to use.

### Checkpoint

`java -version` prints `21.x`. If it still prints `1.8`, your `PATH`/`JAVA_HOME`
didn't take — reopen the terminal.

---

## Phase 1 — Repository & Structure

**Goal:** a monorepo with clear server/client separation, under git.

### Steps

You already have the `polashi/` folder. Inside it:

```bash
cd "polashi"
git init          # only if this folder isn't already tracked
mkdir -p server client
```

Create `polashi/.gitignore`:

```gitignore
# Java / Gradle
server/.gradle/
server/build/
server/bin/
# Node
client/node_modules/
client/dist/
# Env / IDE
.env
.DS_Store
.idea/
*.iml
```

### Why

A **monorepo** keeps the two halves of one game together so they version and
review as a unit. `.gitignore` stops build artifacts and secrets from being
committed.

### Checkpoint

`ls polashi` shows `server/`, `client/`, `.gitignore`.

---

## Phase 2 — Spring Boot Server (Hello World)

**Goal:** a running Java web server you can hit in the browser.

### Steps

1. Generate the project from Spring Initializr straight into `server/`
   (run from inside `polashi/`):

   ```bash
   curl https://start.spring.io/starter.tgz \
     -d type=gradle-project \
     -d language=java \
     -d bootVersion=3.3.5 \
     -d javaVersion=21 \
     -d groupId=com.sahmey \
     -d artifactId=polashi \
     -d name=polashi \
     -d packageName=com.sahmey.polashi \
     -d dependencies=web,websocket \
     | tar -xzvf - -C server
   ```

2. Run it:

   ```bash
   cd server
   ./gradlew bootRun
   ```

3. Add a tiny health endpoint. Create
   `server/src/main/java/com/sahmey/polashi/HealthController.java`:

   ```java
   package com.sahmey.polashi;

   import org.springframework.web.bind.annotation.GetMapping;
   import org.springframework.web.bind.annotation.RestController;

   @RestController
   public class HealthController {
       @GetMapping("/health")
       public String health() {
           return "polashi ok";
       }
   }
   ```

   Stop the server (Ctrl-C) and `./gradlew bootRun` again.

### Why

Spring Initializr scaffolds a standard project with the Gradle **wrapper**
(`./gradlew`), so nobody needs Gradle installed globally. The `web` dependency
gives you HTTP; `websocket` gives you real-time later. A health endpoint is the
simplest proof the server runs, and hosts like Render use it for health checks.

### Checkpoint

Open <http://localhost:8080/health> → you see `polashi ok`.

### Your turn

Add a `/version` endpoint returning any string. Confirm it in the browser. This
proves you understand the controller pattern.

---

## Phase 3 — The Rules Engine (Test-Driven, No Networking)

**Goal:** the whole game's *logic* in plain Java, proven by tests — before any
networking. This is the heart of the game and the best place to learn.

You'll write the **test first**, watch it fail, then implement until it passes
(TDD). Do these one at a time.

### 3.1 Faction distribution

Create the test
`server/src/test/java/com/sahmey/polashi/game/FactionTableTest.java`:

```java
package com.sahmey.polashi.game;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class FactionTableTest {
    @Test
    void redCountsMatchSpec() {
        assertEquals(2, FactionTable.redCount(5));
        assertEquals(2, FactionTable.redCount(6));
        assertEquals(3, FactionTable.redCount(7));
        assertEquals(3, FactionTable.redCount(8));
        assertEquals(3, FactionTable.redCount(9));
        assertEquals(4, FactionTable.redCount(10));
    }

    @Test
    void greenIsTheRest() {
        assertEquals(3, FactionTable.greenCount(5));
        assertEquals(6, FactionTable.greenCount(10));
    }
}
```

Run `./gradlew test` → it fails to compile (no `FactionTable` yet). Now create
`server/src/main/java/com/sahmey/polashi/game/FactionTable.java`:

```java
package com.sahmey.polashi.game;

import java.util.Map;

public final class FactionTable {
    // players -> number of Red (EIC) traitors
    private static final Map<Integer, Integer> REDS = Map.of(
        5, 2, 6, 2, 7, 3, 8, 3, 9, 3, 10, 4
    );

    public static int redCount(int players) {
        Integer r = REDS.get(players);
        if (r == null) throw new IllegalArgumentException("players must be 5-10");
        return r;
    }

    public static int greenCount(int players) {
        return players - redCount(players);
    }

    private FactionTable() {}
}
```

Run `./gradlew test` → green. **You just did your first TDD cycle.**

### 3.2 Team size per chapter

Write `MapCardTest` asserting the table from the spec (e.g.
`teamSize(players=7, chapter=3) == 3`, `teamSize(8,4) == 5`), then implement a
`MapCard.teamSize(int players, int chapter)`. Encode the grid:

| Players | Ch1 | Ch2 | Ch3 | Ch4 | Ch5 |
|--------:|:--:|:--:|:--:|:--:|:--:|
| 5 | 2 | 3 | 2 | 3 | 3 |
| 6 | 2 | 3 | 4 | 3 | 4 |
| 7 | 2 | 3 | 3 | 4 | 4 |
| 8 | 3 | 4 | 4 | 5 | 5 |
| 9 | 3 | 4 | 4 | 5 | 5 |
| 10 | 3 | 4 | 4 | 5 | 5 |

### 3.3 Red threshold

Write `thresholdTest`, then implement
`MapCard.redsToWin(int players, int chapter)` → returns `1`, except **`2` when
`chapter == 4 && players >= 7`**.

### 3.4 Chapter resolution

`Chapter.resolve(int redsPlayed, int threshold)` → returns `EIC` if
`redsPlayed >= threshold`, else `NAWAB`. Test both sides of the boundary.

### 3.5 Reject tracking

A small `ChapterProgress` that counts consecutive rejects and reports
`eicWinsByRejection()` true once the count hits **3**. Test that the 3rd reject
trips it and an approval resets the counter.

### 3.6 Overall game end

`Scoreboard` tracking chapters won per faction; `winner()` returns a faction
once it reaches **3**, else none. Test that 3–0, 3–1, 3–2 all end and 2–2 does
not.

### Why

Testing pure logic first means the trickiest, most bug-prone part of the game is
correct and regression-proof *before* you add the complexity of sockets and UI.
Each class does one job, which is exactly what makes it easy to test and reason
about.

### Checkpoint

`./gradlew test` is green with tests covering all six pieces above.

### Your turn

Add a test for an invalid input (e.g. `teamSize(4, 1)` or `chapter = 6`) and
make the code throw a clear exception. Handling boundaries is a real engineering
habit.

---

## Phase 4 — Rooms & WebSocket (Real-Time Server)

**Goal:** players connect by room code and the server drives the game over a
socket. This is where the rules engine gets a "conductor."

### Concepts you'll build

- **`Player`** — id, nickname, socket session, secret role (never serialized to
  others).
- **`Game`** — one room: players, phase, current captain, proposed team, votes,
  war cards, scoreboard. Holds a `Scoreboard` and uses `FactionTable`/`MapCard`.
- **`RoomManager`** — `Map<String, Game>` keyed by room code; creates codes,
  routes joins.
- **`GameSocketHandler`** — a Spring `TextWebSocketHandler` that parses incoming
  JSON intents and calls into `Game`, then pushes events back out.

### Steps (outline — implement guided by the spec's protocol)

1. Register a WebSocket endpoint. Create a config:

   ```java
   package com.sahmey.polashi.net;

   import org.springframework.context.annotation.Configuration;
   import org.springframework.web.socket.config.annotation.*;

   @Configuration
   @EnableWebSocket
   public class WsConfig implements WebSocketConfigurer {
       private final GameSocketHandler handler;
       public WsConfig(GameSocketHandler handler) { this.handler = handler; }

       @Override
       public void registerWebSocketHandlers(WebSocketHandlerRegistry r) {
           r.addHandler(handler, "/ws").setAllowedOrigins("*");
       }
   }
   ```

2. In `GameSocketHandler.handleTextMessage`, parse `{ "type": "...", ... }` with
   Jackson (`ObjectMapper`) and switch on `type`: `createRoom`, `joinRoom`,
   `startGame`, `proposeTeam`, `castVote`, `playWarCard`, `nextChapter`.

3. **Enforce secrecy on the server:** build a *per-recipient* payload. When you
   send `privateRole`, send each socket only its own role; EIC sockets also get
   the list of fellow EIC. Never broadcast raw roles or individual votes.

4. Broadcast public state (`roomState`, `chapterState`, `voteResult`,
   `warResult`, `gameOver`) to everyone in the room.

### Why

The server is **authoritative**: clients send *intentions*, the server decides
outcomes. That's what stops a player from opening dev tools and reading who the
traitors are — the data simply never leaves the server. Building per-recipient
payloads is the core security idea of hidden-role games.

### Checkpoint

Use a WebSocket tester (browser console or an extension). Connect two tabs, send
`createRoom` then `joinRoom`, and confirm both receive `roomState` with two
players. Start with 5 fake players and confirm each socket gets a `privateRole`
that only contains its own role.

### Your turn

Add server-side rejection of an illegal move: a Green (Nawab) player sending
`playWarCard: red` must get an `error` and not affect the result. Prove it with
a test on `Game`.

---

## Phase 5 — React Client (Connect & Lobby)

**Goal:** a browser UI that creates/joins a room and shows the lobby.

### Steps

1. Scaffold (run from inside `polashi/`):

   ```bash
   npm create vite@latest client -- --template react
   cd client
   npm install
   npm run dev
   ```

2. Add a tiny socket helper `client/src/socket.js`:

   ```js
   const URL = import.meta.env.VITE_WS_URL ?? "ws://localhost:8080/ws";

   export function connect(onEvent) {
     const ws = new WebSocket(URL);
     ws.onmessage = (e) => onEvent(JSON.parse(e.data));
     const send = (type, payload = {}) =>
       ws.send(JSON.stringify({ type, ...payload }));
     return { ws, send };
   }
   ```

3. Build a **Home** screen: nickname input + "Create room" and "Join room (code)"
   buttons that call `send("createRoom", {name})` / `send("joinRoom", {code,
   name})`. Store incoming `roomState` in React state and render the player list
   = your **Lobby**.

### Why

The client is deliberately thin: it sends intents and renders whatever the
server reports. Reading the WebSocket URL from `import.meta.env.VITE_WS_URL`
lets the same build point at localhost in dev and your Render server in
production.

### Checkpoint

Two browser tabs, same room code, both show each other in the lobby.

### Your turn

Disable the host's **Start** button until the lobby has 5+ players (the game's
minimum). UI reflecting server rules is a recurring pattern.

---

## Phase 6 — Game Screens

**Goal:** play a full game end-to-end in the browser.

Build one screen per phase, each driven by a server event:

1. **Role reveal** (`privateRole`) — show your faction; if EIC, list teammates.
2. **Chapter board** (`chapterState`) — scoreboard, captain, required team size;
   if you're captain, a team-picker that sends `proposeTeam`.
3. **Vote** (`votePhase`) — approve/reject buttons → `castVote`; then show the
   `voteResult` tally (never individual votes). Show the 1-minute discussion
   timer on a reject.
4. **War** (`warPhase`) — only the chosen team sees green/red buttons →
   `playWarCard`; then `warResult` shows red count + chapter winner.
5. **Game over** (`gameOver`) — winning faction + play again.

### Why

Mapping one screen to one server event keeps the client a pure function of
server state, which is easy to reason about and debug: if the screen is wrong,
inspect the last event.

### Checkpoint

Play a whole 5-person game (open 5 tabs) and reach a 3-chapter victory.

### Your turn

Add a visible "consecutive rejects: n/3" indicator so players feel the
anti-stall pressure.

---

## Phase 7 — Deploy (Make It Public)

**Goal:** anyone can play at `yourportfolio.com/polashi`.

### Steps

1. **Server → Render** (or Railway/Fly):
   - Add a `Dockerfile` to `server/` (build with Gradle, run the jar), or use
     Render's native Java build.
   - Create a Web Service from your repo's `server/` dir. Note the public URL,
     e.g. `https://polashi.onrender.com`. Your WebSocket URL is then
     `wss://polashi.onrender.com/ws`.

2. **Client → Vercel:**
   - Set env var `VITE_WS_URL=wss://polashi.onrender.com/ws`.
   - Deploy the `client/` dir. Note its Vercel URL.

3. **Proxy at `/polashi`** in the Portfolio's `vercel.json` rewrites (mirroring
   the existing `internship-tracker` entry):

   ```json
   { "source": "/polashi", "destination": "https://<your-client>.vercel.app/polashi" },
   { "source": "/polashi/:path*", "destination": "https://<your-client>.vercel.app/polashi/:path*" }
   ```

   Set Vite's `base` to `/polashi/` in `client/vite.config.js` so asset paths
   resolve under the sub-path.

### Why

Vercel can't host a long-lived WebSocket server, so the Java server lives on a
process host (Render) and the static client lives on Vercel — proxied into your
portfolio exactly like your other projects. `wss://` is the secure WebSocket
scheme required on HTTPS pages.

### Checkpoint

Visit `yourportfolio.com/polashi` from two devices on different networks and
play a game.

### Your turn

Add a friendly "server waking up…" state — free Render instances sleep when
idle and take a few seconds to cold-start the first connection.

---

## Suggested order & git hygiene

Commit at the end of each phase with a clear message (e.g.
`feat(server): faction table + tests`). Small, working commits are easier to
learn from and to roll back.

## When you get stuck

- **Rules bug?** Write a failing JUnit test that reproduces it, then fix.
- **Socket bug?** Log the raw JSON on both ends; the payload usually tells you.
- **UI bug?** Console-log the last server event that screen received.

Build the engine (Phase 3) really solidly — everything else hangs off it.

const URL = import.meta.env.VITE_WS_URL ?? "ws://localhost:8080/ws";

export function connect(onEvent) {
  const ws = new WebSocket(URL);
  const queue = [];

  ws.onopen = () => {
    for (const msg of queue) ws.send(msg);
    queue.length = 0;
  };
  ws.onmessage = (e) => onEvent(JSON.parse(e.data));

  const send = (type, payload = {}) => {
    const msg = JSON.stringify({ type, ...payload });
    if (ws.readyState === WebSocket.OPEN) {
      ws.send(msg);
    } else {
      queue.push(msg);
    }
  };

  return { ws, send };
}

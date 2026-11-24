const STORAGE_KEY = "plottwist.game-state";
const API_BASE_URL = "/api/v1";

const initialState = {
  sessionId: "demo-0241",
  seed: "NIGHT-8842",
  discoveredClues: ["clue-1"],
  selectedSuspect: "s1",
  selectedRoom: "r1",
  activeTab: "overview",
  interviewSuspect: "s2",
  interviewQuestion: "",
  accusation: null,
};

function readState() {
  const saved = localStorage.getItem(STORAGE_KEY);
  if (!saved) return initialState;
  try {
    return { ...initialState, ...JSON.parse(saved) };
  } catch (error) {
    console.error("Impossible de lire la sauvegarde PlotTwist.", error);
    return initialState;
  }
}

let state = readState();
const listeners = new Set();

function setState(patch) {
  state = { ...state, ...patch };
  localStorage.setItem(STORAGE_KEY, JSON.stringify(state));
  listeners.forEach((listener) => listener(state));
  return state;
}

async function request(path, options = {}) {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...options,
    headers: {
      Accept: "application/json",
      ...(options.body ? { "Content-Type": "application/json" } : {}),
      ...options.headers,
    },
  });
  if (!response.ok) {
    const body = await response.text();
    throw new Error(`Erreur API ${response.status}${body ? ` : ${body}` : ""}`);
  }
  if (response.status === 204) return null;
  return response.json();
}

export const api = {
  getState: () => state,
  subscribe(listener) {
    listeners.add(listener);
    return () => listeners.delete(listener);
  },
  update(patch) {
    return setState(patch);
  },
  get(path) {
    return request(path);
  },
  post(path, body) {
    return request(path, { method: "POST", body: JSON.stringify(body) });
  },
  async startSession(seed) {
    const session = await request("/sessions", {
      method: "POST",
      body: JSON.stringify({ seed }),
    });
    return setState({
      sessionId: session.sessionId,
      seed: session.seed,
      discoveredClues: [],
      accusation: null,
    });
  },
};

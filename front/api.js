import { investigation } from "./data.js?v=f4-validation-1";

const STORAGE_KEY = "plottwist.game-state";
const API_BASE_URL = "/api/v1";
const USE_MOCK = false;

const initialState = {
  sessionId: "demo-0241",
  seed: "#AF42",
  discoveredClues: ["clue-1"],
  selectedSuspect: "s1",
  selectedRoom: "r1",
  activeTab: "overview",
  interviewSuspect: "s2",
  interviewQuestion: "",
  accusation: null,
  f4Accusation: null,
};

function readState() {
  const saved = localStorage.getItem(STORAGE_KEY);
  if (!saved) return initialState;
  try {
    const savedState = JSON.parse(saved);
    const storedState = { ...savedState };
    delete storedState.f4DiscoveriesBySeed;
    if (storedState.f4Accusation?.conflictType !== undefined) {
      storedState.f4Accusation = null;
    }
    return {
      ...initialState,
      ...storedState,
      seed: !storedState.seed
        || storedState.seed === "NIGHT-8842"
        || storedState.seed === "#B731"
        || storedState.seed.startsWith("#AUTO-")
          ? initialState.seed
          : storedState.seed,
    };
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
  async getF4Board() {
    if (USE_MOCK) {
      const suspectsById = new Map(investigation.suspects.map((suspect) => [suspect.id, suspect]));
      const placesById = new Map(investigation.rooms.map((place) => [place.id, place]));
      return {
        declarations: investigation.declarations.map((declaration) => ({
          ...declaration,
          suspectName: suspectsById.get(declaration.suspectId).name,
          placeName: placesById.get(declaration.placeId).name,
        })),
      };
    }

    const declarations = await request("/f4/declarations");
    return { declarations };
  },
  async submitF4Accusation(accusation) {
    if (USE_MOCK) {
      const [firstId, secondId] = [
        accusation.firstDeclarationId,
        accusation.secondDeclarationId,
      ].sort();
      const key = `${firstId}|${secondId}|${accusation.designatedLiarDeclarationId}`;
      const correct = investigation.f4ExpectedAccusations[key];
      if (typeof correct !== "boolean") {
        throw new Error(`Aucune réponse de démo F4 pour l'accusation ${key}.`);
      }
      return { correct };
    }

    const result = await request("/f4/accusations", {
      method: "POST",
      body: JSON.stringify(accusation),
    });
    if (!result || typeof result.correct !== "boolean") {
      throw new Error("La réponse du serveur F4 doit contenir un verdict booléen.");
    }
    return result;
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

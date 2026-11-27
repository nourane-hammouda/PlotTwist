import { investigation } from "./data.js?v=f4-discovery-1";

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
  f4DiscoveriesBySeed: {},
};

function readState() {
  const saved = localStorage.getItem(STORAGE_KEY);
  if (!saved) return initialState;
  try {
    const savedState = JSON.parse(saved);
    return {
      ...initialState,
      ...savedState,
      seed: !savedState.seed || savedState.seed === "NIGHT-8842"
        ? initialState.seed
        : savedState.seed,
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
  async getF4Seeds() {
    if (USE_MOCK) return [investigation.seed];

    const seeds = await request("/f4/seeds");
    if (
      !Array.isArray(seeds)
      || seeds.length === 0
      || seeds.some((seed) => typeof seed !== "string" || !seed)
    ) {
      throw new Error("La liste des graines F4 renvoyée par le serveur est invalide.");
    }
    return seeds;
  },
  async getF4Board(seed = state.seed) {
    if (USE_MOCK) {
      if (seed !== investigation.seed) {
        throw new Error(`Aucune fixture F4 de démonstration pour la graine ${seed}.`);
      }
      const suspectsById = new Map(investigation.suspects.map((suspect) => [suspect.id, suspect]));
      const placesById = new Map(investigation.rooms.map((place) => [place.id, place]));
      return {
        declarations: investigation.declarations.map((declaration) => ({
          ...declaration,
          suspectName: suspectsById.get(declaration.suspectId).name,
          placeName: placesById.get(declaration.placeId).name,
        })),
        conflicts: [],
        case: {
          seed,
          title: investigation.title,
          introduction: "Reliez les versions incompatibles et identifiez les déclarations mensongères.",
          discoveryCount: investigation.f4Conflicts.length,
        },
      };
    }

    const [declarations, caseDetails] = await Promise.all([
      request(`/f4/declarations?seed=${encodeURIComponent(seed)}`),
      request(`/f4/case?seed=${encodeURIComponent(seed)}`),
    ]);
    return { declarations, conflicts: [], case: caseDetails };
  },
  async generateF4Investigation() {
    if (USE_MOCK) {
      throw new Error("La génération d’enquête nécessite le backend F4.");
    }
    return request("/f4/generate", { method: "POST" });
  },
  async submitF4Discovery(discovery) {
    if (USE_MOCK) {
      throw new Error("La validation progressive nécessite le backend F4.");
    }
    return request("/f4/discoveries", {
      method: "POST",
      body: JSON.stringify({ ...discovery, seed: discovery.seed ?? state.seed }),
    });
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
      body: JSON.stringify({ ...accusation, seed: accusation.seed ?? state.seed }),
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

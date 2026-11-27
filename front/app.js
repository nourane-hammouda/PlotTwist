import { api } from "./api.js?v=f4-discovery-1";
import { investigation } from "./data.js?v=f4-discovery-1";
import { renderSuspects } from "./suspects-view.js";
import { renderTimeline } from "./timeline-view.js";
import { renderClues } from "./clues-view.js";
import { bindF4BoardInteractions, renderVerdict } from "./verdict-view.js?v=f4-discovery-1";

const root = document.querySelector("#view-root");
const toast = document.querySelector("#toast");
let f4BoardData = null;
let f4Seeds = [];
let f4BoardRequestId = 0;
let toastTimer;

function showToast(message) {
  toast.textContent = message;
  toast.classList.add("visible");
  clearTimeout(toastTimer);
  toastTimer = setTimeout(() => toast.classList.remove("visible"), 2600);
}

function renderOverview(state) {
  const count = state.discoveredClues.length;
  const faces = investigation.suspects.map((suspect) => `<span class="face" title="${suspect.name}">${suspect.initial.slice(0, 1)}</span>`).join("");
  const discoveries = state.f4DiscoveriesBySeed?.[state.seed] ?? [];
  const discoveryCount = f4BoardData?.case?.discoveryCount ?? 0;
  const discoveryList = discoveries.length > 0
    ? `<ul class="f4-discovery-list">${discoveries.map((discovery) => `<li>${discovery.discoveryText}</li>`).join("")}</ul>`
    : `<p class="f4-discovery-empty">Les incohérences seront révélées au fur et à mesure de vos déductions.</p>`;
  const f4Preview = f4BoardData
    ? discoveryList
    : `<p class="f4-board-loading" role="status">Chargement des données F4…</p>`;
  root.innerHTML = `
    <div class="overview-grid">
      <div class="left-column"><section class="panel"><div class="panel-heading"><div><span class="section-overline">L'AFFAIRE EN BREF</span><h2>Une mort en pleine lumière.</h2></div><button class="small-link" data-tab="timeline">Voir la chronologie ↗</button></div>
        <div class="case-note"><span class="note-pin">✳</span><span class="section-overline">NOTE DU COMMISSAIRE</span><p>Victor Delatour, metteur en scène adulé et redouté, a été retrouvé sans vie sur la scène après la première. Cinq personnes étaient encore dans le théâtre. Toutes ont une bonne raison de mentir.</p></div>
        <div class="overview-lower"><div><div class="mini-label">PERSONNES DANS LE DOSSIER <strong>05 SUSPECTS</strong></div><div class="suspect-mini-list"><div class="face-stack">${faces}</div><span class="face-more">+ détails</span></div></div><div><div class="mini-label">INDICES RÉPERTORIÉS <strong>${count} / 8</strong></div><div class="clue-stat"><strong>${count}</strong><span>indice${count === 1 ? "" : "s"} découvert${count === 1 ? "" : "s"}</span></div><div class="clue-meter"><span style="width:${Math.max(12.5, count * 12.5)}%"></span></div></div></div>
      </section>
      <section class="panel f4-overview"><div class="panel-heading"><div><span class="section-overline">F4 / INCOHÉRENCES</span><h2>Des alibis se contredisent.</h2><p>${f4BoardData?.case?.title ?? "Les déductions débloquent les découvertes de l’enquête."}</p></div>${f4BoardData ? `<span class="stat-badge">${discoveries.length} / ${discoveryCount} DÉCOUVERTES</span>` : ""}</div>
        ${f4Preview}
        <button class="primary-button f4-overview-button" type="button" data-tab="verdict">Ouvrir la validation F4 <span>→</span></button>
      </section></div>
      <aside class="right-column"><section class="quote-card"><span class="quote-mark">“</span><blockquote>Le rideau tombe toujours. La question, c'est : qui est encore dans la salle ?</blockquote><cite>— Note anonyme, loge 3</cite></section><section class="next-step"><span class="section-overline">VOTRE PROCHAINE PISTE</span><h3>Fouillez les coulisses.</h3><p>Une trace a été signalée derrière la scène. Elle n'apparaît dans aucun rapport officiel.</p><button class="primary-button" data-tab="clues">Ouvrir le carnet <span>→</span></button></section></aside>
    </div>`;
}

function render() {
  const state = api.getState();
  document.querySelectorAll("[data-tab]").forEach((button) => {
    const active = button.dataset.tab === state.activeTab;
    button.classList.toggle("active", active);
    if (button.matches('[role="tab"]')) button.setAttribute("aria-selected", String(active));
  });
  document.querySelector("#clue-count").textContent = String(state.discoveredClues.length).padStart(2, "0");
  document.querySelector("#tab-clue-count").textContent = String(state.discoveredClues.length).padStart(2, "0");
  document.querySelector("#progress-value").textContent = `${state.discoveredClues.length} / 8`;
  document.querySelector("#progress-bar").style.width = `${Math.max(12.5, state.discoveredClues.length * 12.5)}%`;
  document.querySelector("#seed-display").textContent = state.seed;

  if (state.activeTab === "suspects") renderSuspects(root, state);
  else if (state.activeTab === "timeline") renderTimeline(root, state);
  else if (state.activeTab === "clues") renderClues(root, { ...state, onSearch: (clueQuery) => api.update({ clueQuery }) });
  else if (state.activeTab === "verdict") {
    const discoveries = state.f4DiscoveriesBySeed?.[state.seed] ?? [];
    const board = f4BoardData
      ? { ...f4BoardData, conflicts: discoveries.map((discovery) => discovery.conflict).filter(Boolean) }
      : null;
    renderVerdict(root, state, board, f4Seeds);
  }
  else renderOverview(state);
}

function connectF4Declarations(firstDeclarationId, secondDeclarationId) {
  api.update({
    f4Accusation: {
      firstDeclarationId,
      secondDeclarationId,
      designatedLiarDeclarationId: null,
      designatedLiarName: null,
      conflictType: "",
      correct: null,
      discovery: null,
      status: "linked",
    },
  });
}

async function accuseF4Declaration(designatedLiarDeclarationId) {
  const current = api.getState().f4Accusation;
  if (!current?.firstDeclarationId || !current?.secondDeclarationId) return;
  if (!current.conflictType) {
    showToast("Choisissez d’abord le type d’incohérence.");
    return;
  }
  const declaration = f4BoardData?.declarations.find((item) => item.id === designatedLiarDeclarationId);
  if (!declaration) {
    showToast("Cette déclaration n’est pas disponible dans les données F4.");
    return;
  }

  api.update({
    f4Accusation: {
      ...current,
      designatedLiarDeclarationId,
      designatedLiarName: declaration.suspectName,
      correct: null,
      status: "checking",
    },
  });

  try {
    const seed = api.getState().seed;
    const result = await api.submitF4Discovery({
      seed,
      firstDeclarationId: current.firstDeclarationId,
      secondDeclarationId: current.secondDeclarationId,
      conflictType: current.conflictType,
      designatedLiarDeclarationId,
    });
    if (typeof result.correct !== "boolean") {
      throw new Error("La réponse F4 ne contient pas le résultat de la découverte.");
    }

    const latestState = api.getState();
    const discoveriesBySeed = { ...(latestState.f4DiscoveriesBySeed ?? {}) };
    const discoveries = discoveriesBySeed[seed] ?? [];
    if (result.correct && !discoveries.some((item) => item.discoveryId === result.discoveryId)) {
      discoveriesBySeed[seed] = [...discoveries, result];
    }
    api.update({
      f4Accusation: {
        ...api.getState().f4Accusation,
        correct: result.correct,
        status: result.correct ? "complete" : "wrong",
        discovery: result.correct ? result : null,
      },
      f4DiscoveriesBySeed: discoveriesBySeed,
    });
  } catch (error) {
    console.error("Impossible de vérifier la découverte F4.", error);
    api.update({
      f4Accusation: {
        ...api.getState().f4Accusation,
        correct: null,
        status: "error",
        error: "La vérification a échoué. Réessayez.",
      },
    });
    showToast("La vérification de la découverte a échoué.");
  }
}

document.addEventListener("click", (event) => {
  const target = event.target.closest("[data-tab], [data-action]");
  if (!target) return;
  if (target.dataset.tab) {
    api.update({ activeTab: target.dataset.tab });
    return;
  }
  const { action, id } = target.dataset;
  if (action === "select-suspect") api.update({ selectedSuspect: id });
  if (action === "select-room") api.update({ selectedRoom: id });
  if (action === "collect-clue") {
    const clue = investigation.clues.find((item) => item.id === id);
    if (!api.getState().discoveredClues.includes(id)) {
      api.update({ discoveredClues: [...api.getState().discoveredClues, id] });
      showToast(`Indice ajouté au carnet : ${clue.title}`);
    }
  }
  if (action === "ask-question") api.update({ interviewQuestion: id });
  if (action === "new-case") {
    generateF4Investigation();
  }
});

document.addEventListener("change", (event) => {
  if (event.target.id === "f4-seed-select") {
    const seed = event.target.value;
    if (!f4Seeds.includes(seed)) {
      showToast("Cette graine F4 n’est pas disponible.");
      return;
    }
    if (seed === api.getState().seed) return;

    f4BoardData = null;
    api.update({ seed, f4Accusation: null });
    loadF4Board(seed);
    return;
  }
  if (event.target.id === "f4-conflict-type") {
    const current = api.getState().f4Accusation;
    if (!current?.firstDeclarationId) return;
    api.update({
      f4Accusation: {
        ...current,
        conflictType: event.target.value,
        correct: null,
        discovery: null,
        status: "linked",
      },
    });
    return;
  }
  if (event.target.id === "interview-suspect") api.update({ interviewSuspect: event.target.value });
});

document.addEventListener("submit", (event) => {
  if (!event.target.matches('[data-action="accuse"]')) return;
  event.preventDefault();
  const form = new FormData(event.target);
  const suspectId = form.get("suspectId");
  const motive = form.get("motive");
  const clueIds = form.getAll("proof");
  if (!suspectId || !motive || clueIds.length === 0) {
    showToast("Choisissez un suspect, un mobile et au moins une preuve.");
    return;
  }
  const solution = investigation.solution;
  const checks = {
    suspect: suspectId === solution.suspectId,
    motive: motive === solution.motive,
    proofs: clueIds.length === solution.clueIds.length && solution.clueIds.every((clueId) => clueIds.includes(clueId)),
  };
  const correct = Object.values(checks).every(Boolean);
  const explanation = correct
    ? "Les indices du bouton et des traces convergent vers le secret que Mina cherchait à préserver."
    : "Reprenez les indices et les alibis : une piste reste à confirmer.";
  api.update({ accusation: { suspectId, motive, clueIds, correct, checks, explanation } });
});

bindF4BoardInteractions(root, {
  onConnect: connectF4Declarations,
  onPin: accuseF4Declaration,
});

api.subscribe(render);
render();
async function loadF4Board(seed) {
  const requestId = ++f4BoardRequestId;
  try {
    const board = await api.getF4Board(seed);
    if (requestId !== f4BoardRequestId) return false;
    f4BoardData = board;
    render();
    return true;
  } catch (error) {
    if (requestId !== f4BoardRequestId) return false;
    console.error("Impossible de charger les données F4.", error);
    showToast("Impossible de charger les déclarations F4.");
    return false;
  }
}

async function generateF4Investigation() {
  try {
    const generated = await api.generateF4Investigation();
    f4Seeds = [generated.seed, ...f4Seeds.filter((seed) => seed !== generated.seed)];
    f4BoardData = null;
    api.update({
      seed: generated.seed,
      activeTab: "verdict",
      f4Accusation: null,
    });
    if (await loadF4Board(generated.seed)) {
      showToast(`Nouvelle enquête créée : ${generated.title}`);
    }
  } catch (error) {
    console.error("Impossible de générer une enquête F4.", error);
    showToast("La génération de l’enquête a échoué.");
  }
}

async function initializeF4() {
  try {
    f4Seeds = await api.getF4Seeds();
    const currentSeed = api.getState().seed;
    if (currentSeed.startsWith("#AUTO-")) {
      f4Seeds = [currentSeed, ...f4Seeds.filter((seed) => seed !== currentSeed)];
    } else if (!f4Seeds.includes(currentSeed)) {
      api.update({ seed: f4Seeds[0], f4Accusation: null });
    }
    await loadF4Board(api.getState().seed);
  } catch (error) {
    console.error("Impossible de charger les graines F4.", error);
    showToast("Impossible de charger les enquêtes F4.");
  }
}

initializeF4();

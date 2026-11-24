import { api } from "./api.js";

const SEED = "#AF42";
let loadStatus = "idle";
let loadResult;
let loadError;

function escapeHtml(value) {
  return String(value ?? "").replace(/[&<>"']/g, (character) => ({
    "&": "&amp;",
    "<": "&lt;",
    ">": "&gt;",
    '"': "&quot;",
    "'": "&#39;",
  })[character]);
}

function formatTime(value) {
  if (!value) return "—";
  const date = new Date(value);
  return Number.isNaN(date.getTime())
    ? "—"
    : date.toLocaleTimeString("fr-FR", { hour: "2-digit", minute: "2-digit" });
}

function renderLoadedView(root, state) {
  const { profiles, events, graph } = loadResult;
  const profileById = new Map(profiles.map((profile) => [profile.id, profile]));
  const selected = profiles.find((profile) => profile.id === state.selectedSuspect) ?? profiles[0];

  document.querySelectorAll('[data-tab="suspects"] .nav-count, [role="tab"][data-tab="suspects"] span')
    .forEach((counter) => {
      counter.textContent = String(graph.suspectCount).padStart(2, "0");
    });

  const profileCards = profiles.length
    ? profiles.map((profile) => `
        <button class="suspect-card f1-suspect-card ${selected.id === profile.id ? "selected" : ""}"
                type="button" data-action="select-suspect" data-id="${escapeHtml(profile.id)}"
                aria-pressed="${selected.id === profile.id}">
          <span class="suspect-avatar" aria-hidden="true">${escapeHtml(profile.name.slice(0, 1))}</span>
          <span><strong>${escapeHtml(profile.name)}</strong><small>${escapeHtml(profile.role)}</small></span>
          <span class="suspect-status">PROFIL</span>
        </button>`).join("")
    : '<p class="f1-empty">Aucun profil n’est disponible pour cette enquête.</p>';

  const declarationRows = events.length
    ? events.map((event) => {
        const declarant = profileById.get(event.declaredBy)?.name ?? "Déclarant inconnu";
        const participants = (event.participants ?? [])
          .map((id) => profileById.get(id)?.name ?? "Inconnu")
          .join(", ");
        return `
          <li class="f1-declaration">
            <time>${escapeHtml(formatTime(event.occurredAt))}</time>
            <div>
              <h3>${escapeHtml(event.title)}</h3>
              <p>${escapeHtml(event.description || "Aucun détail fourni.")}</p>
              <div class="f1-declaration-meta">
                <span>${escapeHtml(event.subjectType || "RÉCIT")}</span>
                <span>Déclaré par ${escapeHtml(declarant)}</span>
                <span>Participants : ${escapeHtml(participants || "non précisés")}</span>
              </div>
            </div>
          </li>`;
      }).join("")
    : '<li class="f1-empty">Aucun récit déclaré pour cette enquête.</li>';

  root.innerHTML = `
    <header class="view-header">
      <div>
        <span class="section-overline">01 / F1 · RÉCITS DÉCLARÉS</span>
        <h2>Profils et déclarations</h2>
        <p>Les témoignages et les personnes qu’ils impliquent.</p>
      </div>
      <span class="stat-badge">${escapeHtml(graph.suspectCount)} PROFILS · ${escapeHtml(graph.eventCount)} RÉCITS · ${escapeHtml(graph.relationCount)} LIENS</span>
    </header>
    <div class="f1-suspect-layout">
      <section class="panel">
        <div class="panel-heading">
          <div><h2>Profils des suspects</h2><p>Sélectionnez une personne pour consulter sa fiche.</p></div>
          <span class="section-overline">ENQUÊTE</span>
        </div>
        <div class="f1-suspect-list">${profileCards}</div>
        ${selected ? `
          <section class="detail-card f1-profile-detail" aria-live="polite">
            <span class="section-overline">FICHE DU SUSPECT</span>
            <h3>${escapeHtml(selected.name)}</h3>
            <p>${escapeHtml(selected.biography)}</p>
          </section>` : ""}
      </section>
      <section class="panel">
        <div class="panel-heading">
          <div><h2>Frise des déclarations</h2><p>Récits associés à cette enquête.</p></div>
          <span class="section-overline">CHRONOLOGIE</span>
        </div>
        <ol class="f1-declaration-list">${declarationRows}</ol>
      </section>
    </div>`;
}

export function renderSuspects(root, state, retry = false) {
  if (retry) {
    loadStatus = "idle";
    loadResult = undefined;
    loadError = undefined;
  }

  if (loadStatus === "ready") {
    renderLoadedView(root, state);
    return;
  }
  if (loadStatus === "error") {
    root.innerHTML = `
      <section class="panel f1-status error">
        <p>Impossible de charger les profils et récits : ${escapeHtml(loadError?.message || "erreur inconnue")}</p>
        <button class="secondary-button" type="button" data-action="retry-f1">Réessayer</button>
      </section>`;
    return;
  }
  if (loadStatus === "loading") {
    root.innerHTML = '<p class="panel f1-status">Chargement des profils et récits…</p>';
    return;
  }

  loadStatus = "loading";
  root.innerHTML = '<p class="panel f1-status">Chargement des profils et récits…</p>';
  const query = `?seed=${encodeURIComponent(SEED)}`;
  Promise.all([
    api.get(`/f1/profiles${query}`),
    api.get(`/f1/events${query}`),
    api.get(`/f1/graph${query}`),
  ]).then(([profiles, events, graph]) => {
    loadResult = { profiles, events, graph };
    loadStatus = "ready";
  }).catch((error) => {
    loadError = error;
    loadStatus = "error";
  }).then(() => {
    const currentState = api.getState();
    if (currentState.activeTab === "suspects") {
      renderSuspects(root, currentState);
    }
  });
}

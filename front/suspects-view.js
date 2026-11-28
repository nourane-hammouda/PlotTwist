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

function truncate(value, limit) {
  const text = String(value ?? "");
  return text.length > limit ? `${text.slice(0, limit - 1)}…` : text;
}

function formatTime(value) {
  if (!value) return "—";
  const date = new Date(value);
  return Number.isNaN(date.getTime())
    ? "—"
    : date.toLocaleTimeString("fr-FR", { hour: "2-digit", minute: "2-digit" });
}

function renderNarrativeGraph(profiles, events) {
  if (!profiles.length || !events.length) {
    return '<p class="f1-empty">Le graphe apparaîtra dès que des profils et des récits seront disponibles.</p>';
  }

  const width = 900;
  const height = Math.max(280, Math.max(profiles.length, events.length) * 92 + 48);
  const profileX = 24;
  const profileWidth = 224;
  const eventX = 652;
  const eventWidth = 224;
  const nodeHeight = 58;
  const rowY = (index, count) => 24 + ((height - 48) / count) * (index + 0.5);
  const profileById = new Map(profiles.map((profile) => [profile.id, profile]));
  const profilePositions = new Map(
    profiles.map((profile, index) => [profile.id, rowY(index, profiles.length)]),
  );
  const eventPositions = new Map(
    events.map((event, index) => [event.id, rowY(index, events.length)]),
  );

  const edges = events.flatMap((event) => {
    const participants = (event.participants ?? []).filter((id) => profilePositions.has(id));
    const eventY = eventPositions.get(event.id);
    return participants.map((participantId, index) => {
      const profile = profileById.get(participantId);
      const startY = profilePositions.get(participantId);
      const endY = eventY + (index - (participants.length - 1) / 2) * 10;
      const declarer = participantId === event.declaredBy;
      const description = `${profile.name} — ${event.title}${declarer ? " (déclarant)" : ""}`;
      return `
        <path class="f1-graph-edge ${declarer ? "declarer" : ""}"
              d="M ${profileX + profileWidth} ${startY} C 390 ${startY}, 510 ${endY}, ${eventX} ${endY}">
          <title>${escapeHtml(description)}</title>
        </path>`;
    }).join("");
  }).join("");

  const profileNodes = profiles.map((profile) => {
    const centerY = profilePositions.get(profile.id);
    return `
      <g class="f1-graph-profile">
        <rect x="${profileX}" y="${centerY - nodeHeight / 2}" width="${profileWidth}" height="${nodeHeight}" rx="16" />
        <circle cx="${profileX + 27}" cy="${centerY}" r="16" />
        <text class="f1-graph-initial" x="${profileX + 27}" y="${centerY + 4}" text-anchor="middle">
          ${escapeHtml(profile.name.slice(0, 1))}
        </text>
        <text class="f1-graph-name" x="${profileX + 52}" y="${centerY - 2}">
          ${escapeHtml(truncate(profile.name, 22))}
        </text>
        <text class="f1-graph-role" x="${profileX + 52}" y="${centerY + 15}">
          ${escapeHtml(truncate(profile.role, 25))}
        </text>
      </g>`;
  }).join("");

  const eventNodes = events.map((event) => {
    const centerY = eventPositions.get(event.id);
    return `
      <g class="f1-graph-event">
        <rect x="${eventX}" y="${centerY - nodeHeight / 2}" width="${eventWidth}" height="${nodeHeight}" rx="16" />
        <circle cx="${eventX + 24}" cy="${centerY}" r="6" />
        <text class="f1-graph-type" x="${eventX + 38}" y="${centerY - 3}">
          ${escapeHtml(event.subjectType || "RÉCIT")}
        </text>
        <text class="f1-graph-title" x="${eventX + 16}" y="${centerY + 17}">
          ${escapeHtml(truncate(event.title, 27))}
        </text>
      </g>`;
  }).join("");

  const mobileEvents = events.map((event) => {
    const participants = (event.participants ?? [])
      .filter((id) => profileById.has(id))
      .map((id) => {
        const profile = profileById.get(id);
        const declarer = id === event.declaredBy;
        return `<span class="f1-mobile-participant ${declarer ? "declarer" : ""}">
          ${escapeHtml(profile.name)}${declarer ? " · déclarant" : ""}
        </span>`;
      }).join("");
    return `
      <li class="f1-mobile-event">
        <strong>${escapeHtml(event.title)}</strong>
        <div>${participants}</div>
      </li>`;
  }).join("");

  return `
    <div class="f1-graph-scroll" tabindex="0" aria-label="Graphe des récits, défilement horizontal possible">
      <svg class="f1-narrative-graph" viewBox="0 0 ${width} ${height}" role="img"
           aria-labelledby="f1-graph-title f1-graph-description">
        <title id="f1-graph-title">Relations entre les suspects et les récits</title>
        <desc id="f1-graph-description">Chaque ligne relie un participant à un récit. Les lignes roses désignent l'auteur de la déclaration.</desc>
        ${edges}
        ${profileNodes}
        ${eventNodes}
      </svg>
    </div>
    <ul class="f1-graph-mobile" aria-label="Récits et participants">${mobileEvents}</ul>`;
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
    </div>
    <section class="panel f1-graph-panel">
      <div class="panel-heading">
        <div><h2>Graphe des récits</h2><p>Qui intervient dans chaque déclaration ?</p></div>
        <span class="section-overline">RELATIONS</span>
      </div>
      <div class="f1-graph-legend" aria-label="Légende du graphe">
        <span><i class="f1-legend-line declarer"></i>Déclarant</span>
        <span><i class="f1-legend-line"></i>Autre participant</span>
      </div>
      ${renderNarrativeGraph(profiles, events)}
    </section>`;
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

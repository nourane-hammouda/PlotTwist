import { investigation as data } from "./data.js";

const nodePositions = {
  s1: [50, 15], s2: [23, 43], s3: [77, 43], s4: [32, 79], s5: [69, 79],
};

export function renderSuspects(root, state) {
  const selected = data.suspects.find((suspect) => suspect.id === state.selectedSuspect) ?? data.suspects[0];
  const edges = data.relations.map((relation) => {
    const [x1, y1] = nodePositions[relation.from];
    const [x2, y2] = nodePositions[relation.to];
    return `<line class="network-edge ${relation.type === "dashed" ? "dashed" : ""}" x1="${x1}%" y1="${y1}%" x2="${x2}%" y2="${y2}%"><title>${relation.kind}</title></line>`;
  }).join("");
  const nodes = data.suspects.map((suspect) => {
    const [x, y] = nodePositions[suspect.id];
    return `<button class="node node-${suspect.id.slice(1)} ${state.selectedSuspect === suspect.id ? "selected" : ""}" style="left:${x}%;top:${y}%" data-action="select-suspect" data-id="${suspect.id}" aria-pressed="${state.selectedSuspect === suspect.id}"><span class="node-icon">${suspect.initial.slice(0, 1)}</span><strong>${suspect.name}</strong><small>${suspect.role}</small></button>`;
  }).join("");
  const cards = data.suspects.map((suspect) => `<button class="suspect-card ${state.selectedSuspect === suspect.id ? "selected" : ""}" data-action="select-suspect" data-id="${suspect.id}"><span class="suspect-avatar">${suspect.initial}</span><span><strong>${suspect.name}</strong><small>${suspect.role}</small></span><span class="suspect-status">À SUIVRE</span></button>`).join("");

  root.innerHTML = `
    <div class="view-header"><div><span class="section-overline">01 / RÉSEAU HUMAIN</span><h2>Tout le monde se connaît.</h2><p>Et chacun a quelque chose à cacher.</p></div><span class="stat-badge">05 PERSONNES · 07 LIENS</span></div>
    <div class="suspect-layout">
      <section class="panel network-panel"><div class="panel-heading"><div><h2>Le cercle des suspects</h2><p>Sélectionnez une fiche pour consulter son profil.</p></div><span class="section-overline">SOCIOGRAMME</span></div>
        <div class="network-canvas"><svg viewBox="0 0 100 100" preserveAspectRatio="none" aria-label="Graphe des relations entre suspects">${edges}</svg>${nodes}</div>
        <div class="legend"><span><i></i> Relation connue</span><span><i></i> Tension / lien à vérifier</span></div>
      </section>
      <div><div class="suspect-list">${cards}</div><section class="detail-card" aria-live="polite"><span class="section-overline">PERSONNE D'INTÉRÊT</span><h3>${selected.name}</h3><p>${selected.note}</p></section></div>
    </div>`;
}

import { investigation as data } from "./data.js";

export function renderClues(root, state) {
  const query = state.clueQuery?.trim().toLocaleLowerCase("fr") ?? "";
  const clues = data.clues.filter((clue) => `${clue.title} ${clue.category} ${clue.place} ${clue.description}`.toLocaleLowerCase("fr").includes(query));
  const cards = clues.length ? clues.map((clue) => {
    const collected = state.discoveredClues.includes(clue.id);
    return `<article class="clue-card ${collected ? "collected" : ""}"><span class="tag">${clue.category}</span><h3>${clue.title}</h3><p>${clue.description}</p><footer><span>⌖ ${clue.place}</span>${collected ? "<span class=\"tag\">AU DOSSIER</span>" : `<button data-action="collect-clue" data-id="${clue.id}">+ Ajouter</button>`}</footer></article>`;
  }).join("") : `<div class="empty-state">Aucun élément ne correspond à cette recherche.</div>`;
  const inventory = data.clues.filter((clue) => state.discoveredClues.includes(clue.id)).map((clue) => `<div class="inventory-entry"><i>✳</i><span><strong>${clue.title}</strong><small>${clue.place}</small></span></div>`).join("");
  const count = state.discoveredClues.length;
  const percent = Math.round((count / data.clues.length) * 100);
  root.innerHTML = `
    <div class="view-header"><div><span class="section-overline">03 / ÉLÉMENTS MATÉRIELS</span><h2>Les détails racontent une autre histoire.</h2><p>Fouillez les lieux. Gardez une trace de ce qui ne colle pas.</p></div><span class="stat-badge">${count} / ${data.clues.length} INDICES</span></div>
    <div class="clue-layout">
      <section class="panel"><div class="panel-heading"><div><h2>À examiner</h2><p>Les objets trouvés dans le théâtre.</p></div><span class="section-overline">Fouille</span></div><div class="clue-toolbar"><input class="search-input" type="search" placeholder="Rechercher un indice, un lieu…" aria-label="Rechercher un indice" value="${query.replaceAll("&", "&amp;").replaceAll('"', "&quot;")}" data-action="search-clues" /></div><div class="clue-list">${cards}</div></section>
      <aside class="panel inventory-panel"><span class="section-overline">TABLEAU DE DÉDUCTION</span><div class="suspicion-score"><div><strong>${Math.min(96, 18 + count * 20)}%</strong><span> niveau de suspicion</span></div><span>EN CONSTRUCTION</span></div><div class="suspicion-meter"><span style="width:${Math.min(96, 18 + count * 20)}%"></span></div><p>Les indices collectés font évoluer la piste. Une preuve n'est jamais une certitude seule.</p><div class="inventory-divider"></div><div class="mini-label">VOTRE CARNET <strong>${count} PIÈCE${count > 1 ? "S" : ""}</strong></div>${inventory || "<p>Aucun indice consigné pour le moment.</p>"}</aside>
    </div>`;
  const input = root.querySelector('[data-action="search-clues"]');
  if (input) {
    input.addEventListener("input", (event) => {
      const cursor = event.target.selectionStart;
      state.onSearch?.(event.target.value);
      const next = root.querySelector('[data-action="search-clues"]');
      next?.focus();
      next?.setSelectionRange(cursor, cursor);
    });
  }
}

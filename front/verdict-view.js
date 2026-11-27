import { investigation as data } from "./data.js";

function f4DeclarationCard(declaration, state) {
  const accusation = state.f4Accusation?.firstDeclarationId ? state.f4Accusation : null;
  const connected = accusation?.firstDeclarationId === declaration.id
    || accusation?.secondDeclarationId === declaration.id;
  const accused = accusation?.designatedLiarDeclarationId === declaration.id;
  const pin = connected ? `
    <button
      class="f4-lie-pin ${accused ? "f4-lie-pin--selected" : ""}"
      type="button"
      data-f4-pin="${declaration.id}"
      aria-pressed="${accused}"
      aria-label="Épingler MENSONGE sur la déclaration de ${declaration.suspectName}"
      ${accusation.status === "checking" ? "disabled" : ""}
    >${accused ? "⚑ MENSONGE ÉPINGLÉ" : "⚑ ÉPINGLER MENSONGE"}</button>` : "";
  const alone = declaration.claimsAlone ? "<span class=\"f4-alone-claim\">AFFIRME ÊTRE SEUL·E</span>" : "";

  return `
    <article
      class="f4-declaration-card ${connected ? "f4-declaration-card--connected" : ""} ${accused ? "f4-declaration-card--accused" : ""}"
      data-f4-card
      data-declaration-id="${declaration.id}"
      tabindex="0"
      role="group"
      aria-label="${declaration.suspectName}, ${declaration.placeName}, de ${declaration.startTime} à ${declaration.endTime}. Utilisez Entrée pour relier cette fiche."
      aria-describedby="f4-board-instructions"
    >
      <span class="f4-card-pin" aria-hidden="true">●</span>
      <span class="f4-card-suspect">${declaration.suspectName}</span>
      <span class="f4-card-place">${declaration.placeName}</span>
      <span class="f4-card-time">${declaration.startTime} – ${declaration.endTime}</span>
      ${alone}
      ${pin}
    </article>`;
}

function f4BoardMarkup(state, board) {
  if (!board) {
    return `
      <section class="f4-board-shell" aria-labelledby="f4-board-title">
        <h2 class="f4-board-title" id="f4-board-title">Tableau des déclarations</h2>
        <p class="f4-board-status" role="status" aria-live="polite">Chargement des fiches F4…</p>
      </section>`;
  }

  const accusation = state.f4Accusation?.firstDeclarationId ? state.f4Accusation : null;
  const firstId = accusation?.firstDeclarationId;
  const secondId = accusation?.secondDeclarationId;
  const linked = firstId && secondId;
  const threadClass = accusation?.status === "checking"
    ? "f4-thread--pending"
    : accusation?.correct === true
      ? "f4-thread--true"
      : accusation?.correct === false
        ? "f4-thread--broken"
        : "f4-thread--red";
  const status = accusation?.status === "error"
    ? accusation.error
    : accusation?.status === "checking"
      ? "Vérification de l’accusation…"
      : accusation?.correct === true
        ? `VRAI — la déclaration de ${accusation.designatedLiarName} est reconnue mensongère.`
        : accusation?.correct === false
          ? "FAUX — le fil casse : l’accusation ne correspond pas à la vérité terrain."
          : linked
            ? "Fil posé. Épinglez MENSONGE sur l’une des deux déclarations reliées."
            : "Reliez deux fiches en les faisant glisser l’une vers l’autre, ou sélectionnez-les avec Entrée.";
  const cards = board.declarations.map((declaration) => f4DeclarationCard(declaration, state)).join("");
  const thread = linked
    ? `<line class="f4-thread ${threadClass}" data-f4-thread data-first-id="${firstId}" data-second-id="${secondId}" />`
    : "";

  return `
    <section class="f4-board-shell" aria-labelledby="f4-board-title">
      <div class="f4-board-heading">
        <div>
          <span class="f4-board-kicker">F4 / DOSSIER DES TÉMOIGNAGES</span>
          <h2 class="f4-board-title" id="f4-board-title">Reliez les versions contradictoires</h2>
          <p class="f4-board-instructions" id="f4-board-instructions">
            Faites glisser une fiche vers une autre. Au clavier : Entrée sur la première, puis sur la seconde.
          </p>
        </div>
        <span class="f4-board-count">${board.declarations.length} FICHES</span>
      </div>
      <div class="f4-board-stage" data-f4-stage>
        <svg class="f4-thread-layer" data-f4-svg aria-hidden="true">
          ${thread}
          <line class="f4-thread f4-thread--drawing" data-f4-drag-thread hidden />
        </svg>
        <div class="f4-declaration-grid">${cards}</div>
      </div>
      <p class="f4-board-status" role="status" aria-live="polite" aria-atomic="true">${status}</p>
    </section>`;
}

function positionThread(root, line, firstId, secondId, endPoint) {
  const stage = root.querySelector("[data-f4-stage]");
  const first = root.querySelector(`[data-f4-card][data-declaration-id="${firstId}"]`);
  if (!stage || !first || !line) return;

  const stageRect = stage.getBoundingClientRect();
  const firstRect = first.getBoundingClientRect();
  const startX = firstRect.left - stageRect.left + firstRect.width / 2;
  const startY = firstRect.top - stageRect.top + firstRect.height / 2;
  let endX = endPoint?.x;
  let endY = endPoint?.y;

  if (secondId) {
    const second = root.querySelector(`[data-f4-card][data-declaration-id="${secondId}"]`);
    if (!second) return;
    const secondRect = second.getBoundingClientRect();
    endX = secondRect.left - stageRect.left + secondRect.width / 2;
    endY = secondRect.top - stageRect.top + secondRect.height / 2;
  }

  const svg = line.ownerSVGElement;
  svg.setAttribute("viewBox", `0 0 ${stageRect.width} ${stageRect.height}`);
  line.setAttribute("x1", String(startX));
  line.setAttribute("y1", String(startY));
  line.setAttribute("x2", String(endX));
  line.setAttribute("y2", String(endY));
}

export function bindF4BoardInteractions(root, handlers) {
  if (root.dataset.f4BoardInteractionsBound) return;
  root.dataset.f4BoardInteractionsBound = "true";

  let pointerSourceId = null;
  let keyboardSourceId = null;

  root.addEventListener("pointerdown", (event) => {
    const card = event.target.closest("[data-f4-card]");
    if (!card || event.target.closest("button") || event.button !== 0) return;

    pointerSourceId = card.dataset.declarationId;
    const line = root.querySelector("[data-f4-drag-thread]");
    const stage = root.querySelector("[data-f4-stage]");
    if (line && stage) {
      line.hidden = false;
      positionThread(root, line, pointerSourceId, null, {
        x: event.clientX - stage.getBoundingClientRect().left,
        y: event.clientY - stage.getBoundingClientRect().top,
      });
      card.setPointerCapture(event.pointerId);
    }
  });

  root.addEventListener("pointermove", (event) => {
    if (!pointerSourceId) return;
    const line = root.querySelector("[data-f4-drag-thread]");
    const stage = root.querySelector("[data-f4-stage]");
    if (!line || !stage) return;
    const stageRect = stage.getBoundingClientRect();
    positionThread(root, line, pointerSourceId, null, {
      x: event.clientX - stageRect.left,
      y: event.clientY - stageRect.top,
    });
  });

  root.addEventListener("pointerup", (event) => {
    if (!pointerSourceId) return;
    const sourceId = pointerSourceId;
    pointerSourceId = null;
    keyboardSourceId = null;
    root.querySelectorAll(".f4-declaration-card--keyboard-source")
      .forEach((card) => card.classList.remove("f4-declaration-card--keyboard-source"));
    const line = root.querySelector("[data-f4-drag-thread]");
    if (line) line.hidden = true;

    const target = document.elementFromPoint(event.clientX, event.clientY)?.closest("[data-f4-card]");
    const targetId = target?.dataset.declarationId;
    if (targetId && targetId !== sourceId) handlers.onConnect(sourceId, targetId);
  });

  root.addEventListener("pointercancel", () => {
    pointerSourceId = null;
    const line = root.querySelector("[data-f4-drag-thread]");
    if (line) line.hidden = true;
  });

  root.addEventListener("keydown", (event) => {
    if (event.key !== "Enter" && event.key !== " ") return;
    const card = event.target.closest("[data-f4-card]");
    if (!card || event.target.closest("button")) return;
    event.preventDefault();

    const declarationId = card.dataset.declarationId;
    if (!keyboardSourceId) {
      keyboardSourceId = declarationId;
      card.classList.add("f4-declaration-card--keyboard-source");
      const status = root.querySelector(".f4-board-status");
      if (status) status.textContent = `Première fiche sélectionnée : ${card.querySelector(".f4-card-suspect").textContent}. Placez le focus sur la seconde et appuyez sur Entrée.`;
      return;
    }

    const firstId = keyboardSourceId;
    root.querySelector(`[data-f4-card][data-declaration-id="${firstId}"]`)
      ?.classList.remove("f4-declaration-card--keyboard-source");
    keyboardSourceId = null;
    if (firstId !== declarationId) {
      handlers.onConnect(firstId, declarationId);
    } else {
      const status = root.querySelector(".f4-board-status");
      if (status) status.textContent = "Choisissez deux fiches différentes.";
    }
  });

  root.addEventListener("click", (event) => {
    const pin = event.target.closest("[data-f4-pin]");
    if (pin && !pin.disabled) handlers.onPin(pin.dataset.f4Pin);
  });

  root.addEventListener("submit", (event) => {
    if (!event.target.matches("[data-f4-form]")) return;
    event.preventDefault();
  });

  const drawSavedThread = () => {
    const line = root.querySelector("[data-f4-thread]");
    if (!line) return;
    positionThread(root, line, line.dataset.firstId, line.dataset.secondId);
  };
  root.addEventListener("f4-board-rendered", drawSavedThread);
}

export function renderVerdict(root, state, f4Board) {
  const selected = data.suspects.find((suspect) => suspect.id === state.interviewSuspect) ?? data.suspects[1];
  const activeQuestion = data.questions.find((question) => question.id === state.interviewQuestion);
  const checks = state.accusation?.checks ?? {};
  const suspects = data.suspects.map((suspect) => `<option value="${suspect.id}" ${state.accusation?.suspectId === suspect.id ? "selected" : ""}>${suspect.name}</option>`).join("");
  const proofs = data.clues.map((clue) => `<label class="proof-choice"><input type="checkbox" name="proof" value="${clue.id}" ${state.accusation?.clueIds?.includes(clue.id) ? "checked" : ""} /> ${clue.title}</label>`).join("");
  const result = state.accusation ? `<div class="verdict-result ${state.accusation.correct ? "" : "wrong"}" role="status"><strong>${state.accusation.correct ? "✓ Votre déduction est juste." : "La piste reste à vérifier."}</strong><div class="verdict-checks"><span class="${checks.suspect ? "match" : ""}">Coupable ${checks.suspect ? "✓" : "×"}</span><span class="${checks.motive ? "match" : ""}">Mobile ${checks.motive ? "✓" : "×"}</span><span class="${checks.proofs ? "match" : ""}">Preuves ${checks.proofs ? "✓" : "×"}</span></div>${state.accusation.explanation}</div>` : "";
  const f4BoardMarkupHtml = f4BoardMarkup(state, f4Board);
  root.innerHTML = `
    <div class="view-header"><div><span class="section-overline">04 / CONFRONTATION & DÉDUCTION</span><h2>La vérité résiste aux questions.</h2><p>Interrogez, confrontez les faits, puis assumez votre théorie.</p></div><span class="stat-badge">DÉNOUEMENT À ÉCRIRE</span></div>
    ${f4BoardMarkupHtml}
    <div class="interview-layout">
      <section class="panel interview-panel"><div class="panel-heading"><div><h2>Salle d'interrogatoire</h2><p>Choisissez un sujet pour orienter l'échange.</p></div><span class="section-overline">DIALOGUE STRUCTURÉ</span></div>
        <div class="dialogue"><div class="dialogue-meta"><span class="suspect-avatar">${selected.initial}</span><span><strong>${selected.name}</strong><small>${selected.role}</small></span><label class="interview-select-label" for="interview-suspect">INTERROGER</label><select class="field-select interview-select" id="interview-suspect">${data.suspects.map((suspect) => `<option value="${suspect.id}" ${selected.id === suspect.id ? "selected" : ""}>${suspect.name}</option>`).join("")}</select></div>
          <div class="speech">Vous étiez encore dans le théâtre au moment de la coupure de lumière.<cite>VOUS · 23:16</cite></div>
          <div class="speech reply">${activeQuestion?.answer ?? "Je vous ai déjà tout dit. Cette nuit-là, chacun avait une raison de rester après le dernier salut."}<cite>${selected.name.toUpperCase()} · 23:17</cite></div>
          <div class="question-list">${data.questions.map((question) => `<button class="question-button" data-action="ask-question" data-id="${question.id}">${question.label} →</button>`).join("")}</div>
        </div>
      </section>
      <form class="panel verdict-panel" data-action="accuse"><span class="section-overline">VOTRE THÉORIE</span><h3>Formuler une accusation</h3><p>Une accusation se construit. Le verdict examine séparément le coupable, le mobile et les preuves.</p>
        <label class="field-label" for="accused">Qui est responsable ?</label><select class="field-select" id="accused" name="suspectId"><option value="">Choisir une personne…</option>${suspects}</select>
        <label class="field-label" for="motive">Quel mobile ?</label><select class="field-select" id="motive" name="motive"><option value="">Choisir un mobile…</option><option value="Protéger un secret" ${state.accusation?.motive === "Protéger un secret" ? "selected" : ""}>Protéger un secret</option><option value="Jalousie artistique" ${state.accusation?.motive === "Jalousie artistique" ? "selected" : ""}>Jalousie artistique</option><option value="Intérêt financier" ${state.accusation?.motive === "Intérêt financier" ? "selected" : ""}>Intérêt financier</option></select>
        <span class="field-label">Quelles preuves lient votre suspect au crime ?</span><div class="proof-choice-list">${proofs}</div><button class="primary-button" type="submit">Rendre mon verdict <span>↗</span></button>${result}
      </form>
    </div>`;
  root.dispatchEvent(new Event("f4-board-rendered"));
}

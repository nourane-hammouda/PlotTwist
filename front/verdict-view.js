import { investigation as data } from "./data.js?v=f4-discovery-1";

const f4VhsPlayback = {
  start: 0,
  end: 0,
  current: 0,
  speed: 1,
  playing: false,
  interval: null,
  lastTick: 0,
  boardKey: "",
  conflictWindows: [],
};

function timeToMinutes(time) {
  const [hours, minutes] = time.split(":").map(Number);
  return hours * 60 + minutes;
}

function minutesToTime(minutes) {
  const wholeMinutes = Math.floor(minutes);
  const hours = Math.floor(wholeMinutes / 60);
  const remainder = wholeMinutes % 60;
  return `${String(hours).padStart(2, "0")}:${String(remainder).padStart(2, "0")}`;
}

function stopF4VhsPlayback() {
  if (f4VhsPlayback.interval !== null) {
    clearInterval(f4VhsPlayback.interval);
    f4VhsPlayback.interval = null;
  }
  f4VhsPlayback.playing = false;
}

function prepareF4Vhs(board) {
  const declarations = board?.declarations ?? [];
  if (declarations.length === 0) return null;

  const start = Math.min(...declarations.map((declaration) => timeToMinutes(declaration.startTime)));
  const end = Math.max(...declarations.map((declaration) => timeToMinutes(declaration.endTime)));
  const boardKey = `${board.case?.seed ?? ""}|${declarations
    .map((declaration) => `${declaration.id}:${declaration.startTime}:${declaration.endTime}`)
    .join("|")}`;

  if (boardKey !== f4VhsPlayback.boardKey) {
    stopF4VhsPlayback();
    f4VhsPlayback.boardKey = boardKey;
    f4VhsPlayback.start = start;
    f4VhsPlayback.end = end;
    f4VhsPlayback.current = start;
    f4VhsPlayback.speed = 1;
  }

  const declarationsById = new Map(declarations.map((declaration) => [declaration.id, declaration]));
  f4VhsPlayback.conflictWindows = (board.conflicts ?? []).flatMap((conflict) => {
    const first = declarationsById.get(conflict.firstDeclarationId);
    const second = declarationsById.get(conflict.secondDeclarationId);
    if (!first || !second) return [];

    const conflictStart = Math.max(timeToMinutes(first.startTime), timeToMinutes(second.startTime));
    const conflictEnd = Math.min(timeToMinutes(first.endTime), timeToMinutes(second.endTime));
    if (conflictStart >= conflictEnd) return [];

    return [{
      start: conflictStart,
      end: conflictEnd,
      declarationIds: [first.id, second.id],
    }];
  });

  const declarationsBySuspect = new Map();
  declarations.forEach((declaration) => {
    if (!declarationsBySuspect.has(declaration.suspectId)) {
      declarationsBySuspect.set(declaration.suspectId, {
        name: declaration.suspectName,
        declarations: [],
      });
    }
    declarationsBySuspect.get(declaration.suspectId).declarations.push(declaration);
  });

  const rows = [...declarationsBySuspect.values()].map((suspect) => {
    const tracks = [];
    const blocks = suspect.declarations
      .slice()
      .sort((first, second) => timeToMinutes(first.startTime) - timeToMinutes(second.startTime))
      .map((declaration) => {
        const declarationStart = timeToMinutes(declaration.startTime);
        const declarationEnd = timeToMinutes(declaration.endTime);
        let trackIndex = tracks.findIndex((trackEnd) => trackEnd <= declarationStart);
        if (trackIndex === -1) trackIndex = tracks.length;
        tracks[trackIndex] = declarationEnd;
        return { declaration, trackIndex };
      });
    return { name: suspect.name, blocks, trackCount: Math.max(1, tracks.length) };
  });

  return { declarations, start, end, rows };
}

function f4VhsMarkup(board) {
  if (!board) {
    return `
      <section class="f4-vhs" aria-labelledby="f4-vhs-title">
        <h2 class="f4-vhs-title" id="f4-vhs-title">Magnétoscope des témoignages</h2>
        <p class="f4-vhs-loading" role="status">Chargement de la frise…</p>
      </section>`;
  }

  const model = prepareF4Vhs(board);
  if (!model) {
    return `
      <section class="f4-vhs" aria-labelledby="f4-vhs-title">
        <h2 class="f4-vhs-title" id="f4-vhs-title">Magnétoscope des témoignages</h2>
        <p class="f4-vhs-loading" role="status">Aucune déclaration à afficher.</p>
      </section>`;
  }

  const duration = model.end - model.start;
  const positionFor = (minute) => `${((minute - model.start) / duration) * 100}%`;
  const ticks = [];
  for (let minute = model.start; minute < model.end; minute += 10) ticks.push(minute);
  if (ticks[ticks.length - 1] !== model.end) ticks.push(model.end);

  const tickMarkup = ticks.map((minute, index) => {
    const edge = index === 0 ? "f4-vhs-tick--start" : index === ticks.length - 1 ? "f4-vhs-tick--end" : "";
    return `
      <span class="f4-vhs-tick ${edge}" style="left:${positionFor(minute)}">${minutesToTime(minute)}</span>`;
  }).join("");
  const rows = model.rows.map((row) => {
    const laneHeight = row.trackCount * 30 + 4;
    const blocks = row.blocks.map(({ declaration, trackIndex }) => {
      const declarationStart = timeToMinutes(declaration.startTime);
      const declarationEnd = timeToMinutes(declaration.endTime);
      return `
        <span
          class="f4-vhs-block"
          data-f4-vhs-block="${declaration.id}"
          role="img"
          aria-label="${declaration.placeName}, ${declaration.startTime} à ${declaration.endTime}"
          title="${declaration.placeName} · ${declaration.startTime}–${declaration.endTime}"
          style="left:${positionFor(declarationStart)};width:${((declarationEnd - declarationStart) / duration) * 100}%;top:${trackIndex * 30 + 3}px"
        >${declaration.placeName}</span>`;
    }).join("");

    return `
      <div class="f4-vhs-row" role="group" aria-label="${row.name}">
        <span class="f4-vhs-suspect">${row.name}</span>
        <div class="f4-vhs-lane" style="height:${laneHeight}px">
          ${blocks}
          <span class="f4-vhs-playhead" data-f4-vhs-playhead aria-hidden="true"></span>
        </div>
      </div>`;
  }).join("");

  return `
    <section class="f4-vhs" aria-labelledby="f4-vhs-title" data-f4-vhs>
      <div class="f4-vhs-heading">
        <div>
          <span class="f4-vhs-kicker">F4 / ARCHIVES VIDÉO</span>
          <h2 class="f4-vhs-title" id="f4-vhs-title">Magnétoscope des témoignages</h2>
        </div>
        <div class="f4-vhs-timecode">
          <span class="f4-vhs-rec">● REC</span>
          <time data-f4-vhs-timecode aria-live="off">${minutesToTime(f4VhsPlayback.current)}</time>
        </div>
      </div>
      <div class="f4-vhs-controls" role="group" aria-label="Commandes du magnétoscope">
        <button type="button" data-f4-vhs-control="rewind" aria-label="Revenir au début de la frise">⏮</button>
        <button type="button" data-f4-vhs-control="toggle" aria-label="${f4VhsPlayback.playing ? "Mettre en pause" : "Lancer la lecture"}" aria-pressed="${f4VhsPlayback.playing}">${f4VhsPlayback.playing ? "⏸" : "▶"}</button>
        <button type="button" data-f4-vhs-control="forward" aria-label="Avancer à la fin de la frise">⏭</button>
        <button type="button" class="f4-vhs-speed" data-f4-vhs-control="speed" aria-label="Vitesse de lecture ×${f4VhsPlayback.speed}" aria-pressed="${f4VhsPlayback.speed === 2}">×${f4VhsPlayback.speed}</button>
        <span class="f4-vhs-announcement" data-f4-vhs-announcement role="status" aria-live="polite">${f4VhsPlayback.playing ? "Lecture en cours." : "Lecture en pause."}</span>
      </div>
      <div class="f4-vhs-screen" data-f4-vhs-screen>
        <div class="f4-vhs-track-scroll" role="region" aria-label="Frise des déclarations, défilement horizontal possible" tabindex="0">
          <div class="f4-vhs-track-grid">
            <div class="f4-vhs-ruler">
              <span class="f4-vhs-suspect-label">HEURE</span>
              <div class="f4-vhs-axis">
                ${tickMarkup}
                <span class="f4-vhs-playhead f4-vhs-playhead--marker" data-f4-vhs-playhead aria-hidden="true"></span>
              </div>
            </div>
            ${rows}
          </div>
        </div>
      </div>
    </section>`;
}

function updateF4Vhs(root, announcement) {
  const screen = root.querySelector("[data-f4-vhs-screen]");
  if (!screen) return;

  const duration = f4VhsPlayback.end - f4VhsPlayback.start;
  const progress = duration > 0
    ? Math.max(0, Math.min(100, ((f4VhsPlayback.current - f4VhsPlayback.start) / duration) * 100))
    : 0;
  root.querySelectorAll("[data-f4-vhs-playhead]").forEach((playhead) => {
    playhead.style.left = `${progress}%`;
  });

  const timecode = root.querySelector("[data-f4-vhs-timecode]");
  if (timecode) timecode.textContent = minutesToTime(f4VhsPlayback.current);

  const activeDeclarations = new Set();
  f4VhsPlayback.conflictWindows.forEach((window) => {
    if (f4VhsPlayback.current >= window.start && f4VhsPlayback.current < window.end) {
      window.declarationIds.forEach((id) => activeDeclarations.add(id));
    }
  });
  screen.classList.toggle("f4-vhs-screen--glitch", activeDeclarations.size > 0);
  root.querySelectorAll("[data-f4-vhs-block]").forEach((block) => {
    block.classList.toggle("f4-vhs-block--active", activeDeclarations.has(block.dataset.f4VhsBlock));
  });

  const toggle = root.querySelector('[data-f4-vhs-control="toggle"]');
  if (toggle) {
    toggle.textContent = f4VhsPlayback.playing ? "⏸" : "▶";
    toggle.setAttribute("aria-label", f4VhsPlayback.playing ? "Mettre en pause" : "Lancer la lecture");
    toggle.setAttribute("aria-pressed", String(f4VhsPlayback.playing));
  }
  const speed = root.querySelector('[data-f4-vhs-control="speed"]');
  if (speed) {
    speed.textContent = `×${f4VhsPlayback.speed}`;
    speed.setAttribute("aria-label", `Vitesse de lecture ×${f4VhsPlayback.speed}`);
    speed.setAttribute("aria-pressed", String(f4VhsPlayback.speed === 2));
  }

  const status = root.querySelector("[data-f4-vhs-announcement]");
  if (status && announcement) status.textContent = announcement;
}

function startF4VhsPlayback(root) {
  if (f4VhsPlayback.current >= f4VhsPlayback.end) {
    f4VhsPlayback.current = f4VhsPlayback.start;
  }
  f4VhsPlayback.playing = true;
  f4VhsPlayback.lastTick = performance.now();
  f4VhsPlayback.interval = setInterval(() => {
    if (!root.querySelector("[data-f4-vhs-screen]")) {
      stopF4VhsPlayback();
      return;
    }

    const now = performance.now();
    f4VhsPlayback.current += ((now - f4VhsPlayback.lastTick) / 1000) * f4VhsPlayback.speed;
    f4VhsPlayback.lastTick = now;
    if (f4VhsPlayback.current >= f4VhsPlayback.end) {
      f4VhsPlayback.current = f4VhsPlayback.end;
      stopF4VhsPlayback();
      updateF4Vhs(root, "Fin de l’enregistrement.");
      return;
    }
    updateF4Vhs(root);
  }, 1000 / 30);
}

function f4DeclarationCard(declaration, state) {
  const accusation = state.f4Accusation?.firstDeclarationId ? state.f4Accusation : null;
  const connected = accusation?.firstDeclarationId === declaration.id
    || accusation?.secondDeclarationId === declaration.id;
  const accused = accusation?.designatedLiarDeclarationId === declaration.id;
  const pin = connected && accusation.conflictType ? `
    <button
      class="f4-lie-pin ${accused ? "f4-lie-pin--selected" : ""}"
      type="button"
      data-f4-pin="${declaration.id}"
      aria-pressed="${accused}"
      aria-label="Désigner ${declaration.suspectName} comme menteur"
      ${accusation.status === "checking" || accusation.status === "complete" ? "disabled" : ""}
    >${accused ? "⚑ MENSONGE DÉSIGNÉ" : "⚑ DÉSIGNER LE MENSONGE"}</button>` : "";
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

function f4ConflictExplanationMarkup(state, allDiscoveriesFound) {
  const accusation = state.f4Accusation;
  if (accusation?.correct === true && accusation.discovery) {
    return `
      <div class="f4-conflict-explanation-panel f4-discovery-result" role="status" aria-live="polite">
        <strong>${allDiscoveriesFound
          ? "Toutes les incohérences ont été élucidées"
          : `Découverte ${accusation.discovery.discoveryOrder} / ${accusation.discovery.discoveryCount}`}</strong>
        <p>${accusation.discovery.discoveryText}</p>
      </div>`;
  }
  const selectedType = accusation?.conflictType ?? "";
  return `
    <div class="f4-conflict-explanation-panel" role="status" aria-live="polite">
      <strong>Quel type d’incohérence avez-vous repéré ?</strong>
      <label class="f4-conflict-type-picker" for="f4-conflict-type">
        <span>Choisissez une déduction avant de désigner le menteur.</span>
        <select class="field-select" id="f4-conflict-type" aria-label="Type d’incohérence">
          <option value="">Choisir un type…</option>
          <option value="UBIQUITY" ${selectedType === "UBIQUITY" ? "selected" : ""}>Ubiquité — une personne, deux lieux</option>
          <option value="SOLITUDE_CONTREDITE" ${selectedType === "SOLITUDE_CONTREDITE" ? "selected" : ""}>Solitude contredite — un autre témoin est présent</option>
        </select>
      </label>
    </div>`;
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
  const discoveries = state.f4DiscoveriesBySeed?.[state.seed] ?? [];
  const discoveryCount = board.case?.discoveryCount ?? 0;
  const allDiscoveriesFound = discoveryCount > 0 && discoveries.length >= discoveryCount;
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
      ? "Vérification de votre déduction…"
      : accusation?.correct === true
        ? allDiscoveriesFound
          ? "Toutes les incohérences ont été élucidées. Les déclarations mensongères sont maintenant consignées dans le carnet."
          : "Découverte enregistrée dans le carnet. Reliez d’autres fiches pour poursuivre l’enquête."
        : accusation?.correct === false
          ? "Cette déduction ne correspond pas aux témoignages. Vous pouvez réessayer."
          : linked
            ? accusation.conflictType
              ? "Type choisi. Désignez la déclaration qui ment."
              : "Choisissez le type d’incohérence, puis désignez la déclaration qui ment."
            : "Reliez deux fiches. Choisissez le type d’incohérence et le témoignage mensonger.";
  const cards = board.declarations.map((declaration) => f4DeclarationCard(declaration, state)).join("");
  const thread = linked
    ? `<line class="f4-thread ${threadClass}" data-f4-thread data-first-id="${firstId}" data-second-id="${secondId}" />`
    : "";
  const conflictExplanation = linked
    ? f4ConflictExplanationMarkup(state, allDiscoveriesFound)
    : "";

  return `
    <section class="f4-board-shell" aria-labelledby="f4-board-title">
      <div class="f4-board-heading">
        <div>
          <span class="f4-board-kicker">F4 / DOSSIER DES TÉMOIGNAGES</span>
          <h2 class="f4-board-title" id="f4-board-title">Reliez les versions contradictoires</h2>
          <p class="f4-board-instructions" id="f4-board-instructions">
            Reliez deux fiches, choisissez le type d’incohérence, puis désignez le menteur. Au clavier : Entrée sur la première fiche, puis sur la seconde.
          </p>
        </div>
        <span class="f4-board-count">${discoveries.length} / ${discoveryCount} DÉCOUVERTES · ${board.declarations.length} FICHES</span>
      </div>
      <div class="f4-board-stage" data-f4-stage>
        <svg class="f4-thread-layer" data-f4-svg aria-hidden="true">
          ${thread}
          <line class="f4-thread f4-thread--drawing" data-f4-drag-thread hidden />
        </svg>
        <div class="f4-declaration-grid">${cards}</div>
      </div>
      ${conflictExplanation}
      <p class="f4-board-status" role="status" aria-live="polite" aria-atomic="true">${status}</p>
    </section>`;
}

function f4CaseMarkup(board) {
  if (!board?.case) return "";
  const caseType = board.case.seed.startsWith("#AUTO-")
    ? "ENQUÊTE GÉNÉRÉE"
    : "ENQUÊTE DE DÉMONSTRATION";
  return `
    <section class="f4-case-brief" aria-labelledby="f4-case-title">
      <span class="f4-board-kicker">${caseType} · ${board.case.seed}</span>
      <h3 id="f4-case-title">${board.case.title}</h3>
      <p>${board.case.introduction}</p>
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
    const vhsControl = event.target.closest("[data-f4-vhs-control]");
    if (vhsControl) {
      const { f4VhsControl } = vhsControl.dataset;
      if (f4VhsControl === "toggle") {
        if (f4VhsPlayback.playing) {
          stopF4VhsPlayback();
          updateF4Vhs(root, "Lecture mise en pause.");
        } else {
          startF4VhsPlayback(root);
          updateF4Vhs(root, "Lecture en cours.");
        }
      } else if (f4VhsControl === "rewind") {
        stopF4VhsPlayback();
        f4VhsPlayback.current = f4VhsPlayback.start;
        updateF4Vhs(root, "Retour au début de la frise.");
      } else if (f4VhsControl === "forward") {
        stopF4VhsPlayback();
        f4VhsPlayback.current = f4VhsPlayback.end;
        updateF4Vhs(root, "Fin de l’enregistrement.");
      } else if (f4VhsControl === "speed") {
        f4VhsPlayback.speed = f4VhsPlayback.speed === 1 ? 2 : 1;
        updateF4Vhs(root, `Vitesse ×${f4VhsPlayback.speed}.`);
      }
      return;
    }

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

export function renderVerdict(root, state, f4Board, f4Seeds = []) {
  const selected = data.suspects.find((suspect) => suspect.id === state.interviewSuspect) ?? data.suspects[1];
  const activeQuestion = data.questions.find((question) => question.id === state.interviewQuestion);
  const checks = state.accusation?.checks ?? {};
  const suspects = data.suspects.map((suspect) => `<option value="${suspect.id}" ${state.accusation?.suspectId === suspect.id ? "selected" : ""}>${suspect.name}</option>`).join("");
  const proofs = data.clues.map((clue) => `<label class="proof-choice"><input type="checkbox" name="proof" value="${clue.id}" ${state.accusation?.clueIds?.includes(clue.id) ? "checked" : ""} /> ${clue.title}</label>`).join("");
  const result = state.accusation ? `<div class="verdict-result ${state.accusation.correct ? "" : "wrong"}" role="status"><strong>${state.accusation.correct ? "✓ Votre déduction est juste." : "La piste reste à vérifier."}</strong><div class="verdict-checks"><span class="${checks.suspect ? "match" : ""}">Coupable ${checks.suspect ? "✓" : "×"}</span><span class="${checks.motive ? "match" : ""}">Mobile ${checks.motive ? "✓" : "×"}</span><span class="${checks.proofs ? "match" : ""}">Preuves ${checks.proofs ? "✓" : "×"}</span></div>${state.accusation.explanation}</div>` : "";
  const f4VhsMarkupHtml = f4VhsMarkup(f4Board);
  const f4BoardMarkupHtml = f4BoardMarkup(state, f4Board);
  const seedOptions = f4Seeds.length > 0
    ? f4Seeds.map((seed) => `<option value="${seed}" ${state.seed === seed ? "selected" : ""}>${seed}</option>`).join("")
    : `<option value="">Chargement…</option>`;
  root.innerHTML = `
    <div class="view-header"><div><span class="section-overline">04 / CONFRONTATION & DÉDUCTION</span><h2>La vérité résiste aux questions.</h2><p>Interrogez, confrontez les faits, puis assumez votre théorie.</p></div><div class="f4-view-header-controls"><span class="stat-badge">DÉNOUEMENT À ÉCRIRE</span><label class="f4-seed-picker" for="f4-seed-select"><span class="f4-seed-label">GRAINE D'ENQUÊTE</span><select class="field-select f4-seed-select" id="f4-seed-select" aria-label="Choisir la graine de l'enquête" ${f4Seeds.length === 0 ? "disabled" : ""}>${seedOptions}</select></label></div></div>
    ${f4CaseMarkup(f4Board)}
    ${f4VhsMarkupHtml}
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

import { investigation as data } from "./data.js";

export function renderVerdict(root, state) {
  const selected = data.suspects.find((suspect) => suspect.id === state.interviewSuspect) ?? data.suspects[1];
  const activeQuestion = data.questions.find((question) => question.id === state.interviewQuestion);
  const checks = state.accusation?.checks ?? {};
  const suspects = data.suspects.map((suspect) => `<option value="${suspect.id}" ${state.accusation?.suspectId === suspect.id ? "selected" : ""}>${suspect.name}</option>`).join("");
  const proofs = data.clues.map((clue) => `<label class="proof-choice"><input type="checkbox" name="proof" value="${clue.id}" ${state.accusation?.clueIds?.includes(clue.id) ? "checked" : ""} /> ${clue.title}</label>`).join("");
  const result = state.accusation ? `<div class="verdict-result ${state.accusation.correct ? "" : "wrong"}" role="status"><strong>${state.accusation.correct ? "✓ Votre déduction est juste." : "La piste reste à vérifier."}</strong><div class="verdict-checks"><span class="${checks.suspect ? "match" : ""}">Coupable ${checks.suspect ? "✓" : "×"}</span><span class="${checks.motive ? "match" : ""}">Mobile ${checks.motive ? "✓" : "×"}</span><span class="${checks.proofs ? "match" : ""}">Preuves ${checks.proofs ? "✓" : "×"}</span></div>${state.accusation.explanation}</div>` : "";
  root.innerHTML = `
    <div class="view-header"><div><span class="section-overline">04 / CONFRONTATION & DÉDUCTION</span><h2>La vérité résiste aux questions.</h2><p>Interrogez, confrontez les faits, puis assumez votre théorie.</p></div><span class="stat-badge">DÉNOUEMENT À ÉCRIRE</span></div>
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
}

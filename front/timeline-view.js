import { investigation as data } from "./data.js";

export function renderTimeline(root, state) {
  const selectedRoom = data.rooms.find((room) => room.id === state.selectedRoom) ?? data.rooms[0];
  const rooms = data.rooms.map((room) => `<button class="room ${state.selectedRoom === room.id ? "selected" : ""}" data-action="select-room" data-id="${room.id}" aria-pressed="${state.selectedRoom === room.id}"><span class="room-name">${room.name}</span><span class="room-type">${room.type}</span></button>`).join("");
  const events = data.events.map((event) => `<article class="timeline-event ${event.flagged ? "flagged" : ""}"><span class="timeline-time">${event.time}</span><div><strong>${event.title}</strong><p>${event.description}</p></div></article>`).join("");
  root.innerHTML = `
    <div class="view-header"><div><span class="section-overline">02 / ESPACE & TEMPS</span><h2>Chaque minute a son importance.</h2><p>Reconstituez les déplacements et confrontez les alibis.</p></div><span class="stat-badge">5 ÉVÉNEMENTS</span></div>
    <div class="timeline-layout">
      <section class="panel map-panel"><div class="panel-heading"><div><h2>Plan du théâtre</h2><p>Choisissez une pièce pour inspecter les traces.</p></div><span class="section-overline">NIVEAU 1</span></div><div class="map-grid">${rooms}</div>
      <div class="room-route"><i class="route-dot"></i><span>Pièce sélectionnée : <strong>${selectedRoom.name}</strong> · dernier signal ${selectedRoom.time}</span></div></section>
      <section class="panel timeline-panel"><div class="panel-heading"><div><h2>La nuit du drame</h2><p>Vendredi 14 octobre · chronologie reconstituée</p></div><span class="section-overline">21:40 — 22:31</span></div><div class="timeline">${events}</div><div class="alibi-banner">⚑ <strong>Zone d'ombre :</strong> trois minutes sans témoin pendant la coupure de lumière.</div></section>
    </div>`;
}

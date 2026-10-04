// Converteix les missions i la configuració del joc en SQL per a les taules oficials del servidor
// (descobreix.missions_oficials i descobreix.configuracio). Ho fa servir generar-sql-missions.js.

function text(v) {
  return `'${String(v).replace(/'/g, "''")}'`;
}

function nombre(v) {
  if (v === null || v === undefined) return 'null';
  if (typeof v !== 'number' || !Number.isFinite(v)) throw new Error(`Nombre no vàlid: ${v}`);
  return String(v);
}

function filaMissio(m) {
  if (!/^[0-9]{5}$/.test(m.municipi)) throw new Error(`Codi INE no vàlid a ${m.id}: ${m.municipi}`);
  if (m.prova !== 'GPS' && m.prova !== 'FOTO') throw new Error(`Prova desconeguda a ${m.id}: ${m.prova}`);
  if (!Number.isInteger(m.punts) || m.punts < 0) throw new Error(`Punts no vàlids a ${m.id}: ${m.punts}`);
  return `(${text(m.id)}, ${text(m.municipi)}, ${text(m.prova)}, ${nombre(m.lat)}, ${nombre(m.lon)}, ${m.punts})`;
}

/** SQL que substitueix totes les missions oficials i la configuració, en blocs de `mida` files. */
function sqlMissions(missions, config, { mida = 1000 } = {}) {
  const ids = new Set();
  for (const m of missions) {
    if (ids.has(m.id)) throw new Error(`Missió repetida: ${m.id}`);
    ids.add(m.id);
  }
  const c = {
    radi: nombre(config.gps.radiMissioMetres),
    precisio: nombre(config.gps.precisioMaximaMetres),
    bonus: nombre(config.punts.bonusTotesLesMissions),
  };
  const linies = [
    '-- Generat per scripts/generar-sql-missions.js. No l\'editis a mà.',
    `-- ${missions.length} missions oficials i la configuració del joc (versió ${config.versio}).`,
    '',
    'insert into descobreix.configuracio (id, radi_missio_metres, precisio_maxima_metres, bonus_totes_les_missions)',
    `values (1, ${c.radi}, ${c.precisio}, ${c.bonus})`,
    'on conflict (id) do update set',
    '    radi_missio_metres = excluded.radi_missio_metres,',
    '    precisio_maxima_metres = excluded.precisio_maxima_metres,',
    '    bonus_totes_les_missions = excluded.bonus_totes_les_missions;',
    '',
    'delete from descobreix.missions_oficials;',
  ];
  for (let i = 0; i < missions.length; i += mida) {
    const bloc = missions.slice(i, i + mida).map(filaMissio);
    linies.push('', 'insert into descobreix.missions_oficials (id, codi_ine, prova, lat, lon, punts) values');
    linies.push(bloc.map((f) => `    ${f}`).join(',\n') + ';');
  }
  return linies.join('\n') + '\n';
}

module.exports = { sqlMissions };

// Espai de col·laboració: entra amb el codi i envia propostes. Parla amb Supabase per les funcions de
// supabase/migrations/20261010120000_collaboradors.sql (mai directament amb les taules).
'use strict';

const CONFIG = window.CONFIG || {};
const $ = (id) => document.getElementById(id);
let codi = null;
let municipis = null;

async function rpc(funcio, args) {
  if (!CONFIG.supabaseUrl || !CONFIG.anonKey) throw new Error('La web encara no està connectada al servidor.');
  const r = await fetch(`${CONFIG.supabaseUrl}/rest/v1/rpc/${funcio}`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      apikey: CONFIG.anonKey,
      Authorization: `Bearer ${CONFIG.anonKey}`,
      'Content-Profile': 'descobreix',
      'Accept-Profile': 'descobreix',
    },
    body: JSON.stringify(args),
  });
  const text = await r.text();
  const dades = text ? JSON.parse(text) : null;
  if (!r.ok) {
    const e = new Error((dades && dades.message) || `Error ${r.status}`);
    e.codi = dades && dades.code;
    throw e;
  }
  return dades;
}

function missatge(id, text, tipus) {
  const m = $(id);
  m.textContent = text;
  m.className = `missatge ${tipus || ''}`;
}

function textError(e) {
  if (e.codi === '42501') return 'Aquest codi no és vàlid. Reviseu-lo o demaneu-ne un de nou.';
  if (e.codi === '23514') return 'Hi ha alguna dada que no quadra (per exemple, les dates). Reviseu-ho.';
  if (e.codi === '54000') return e.message;
  return 'No s\'ha pogut desar. Torneu-ho a provar d\'aquí a una estona.';
}

async function carregaMunicipis() {
  if (municipis) return municipis;
  const r = await fetch('dades/municipis.json');
  municipis = await r.json();
  return municipis;
}

function escapa(s) {
  return String(s ?? '').replace(/[&<>"]/g, (c) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;' })[c]);
}

async function pinta(espai) {
  $('entrada').hidden = true;
  $('espai').hidden = false;
  $('titol').textContent = espai.nom;
  $('subtitol').textContent = espai.tipus === 'CONSELL_COMARCAL'
    ? 'Podeu proposar la medalla de la comarca i missions de tots els seus municipis.'
    : 'Podeu proposar missions del municipi i idees per a la medalla de la comarca.';

  const d = await carregaMunicipis();
  const nom = Object.fromEntries(d.municipis.map((m) => [m.codi, m.nom]));
  const llista = espai.tipus === 'CONSELL_COMARCAL'
    ? d.municipis.filter((m) => m.comarca === espai.codi).sort((a, b) => a.nom.localeCompare(b.nom, 'ca'))
    : d.municipis.filter((m) => m.codi === espai.codi);
  const sel = $('municipi');
  const triat = sel.value;
  sel.innerHTML = llista.map((m) => `<option value="${m.codi}">${escapa(m.nom)}</option>`).join('');
  if (triat) sel.value = triat;

  const c = espai.contacte;
  if (c) {
    const f = $('contacte');
    f.nom.value = c.nom || '';
    f.carrec.value = c.carrec || '';
    f.correu.value = c.correu || '';
    f.consentiment.checked = true;
  }

  $('llista-medalla').innerHTML = espai.medalla.map((p) => `<li><div><b>${escapa(p.que_hi_surt)}</b>${p.per_que ? `<p>${escapa(p.per_que)}</p>` : ''}</div>
    <button type="button" data-retira="${p.id}">Retira</button></li>`).join('');
  $('llista-missions').innerHTML = espai.missions.map((p) => `<li><div><b>${escapa(p.titol)}</b><p>${escapa(nom[p.codi_ine] || p.codi_ine)}${p.data_inici ? ` · del ${p.data_inici} al ${p.data_fi}` : ''}</p></div>
    <button type="button" data-retira="${p.id}">Retira</button></li>`).join('');
}

async function actualitza() {
  await pinta(await rpc('espai_collaborador', { codi_acces: codi }));
}

function coordenades(text) {
  const t = (text || '').trim();
  if (!t) return [null, null];
  const m = t.match(/^(-?\d+(?:[.,]\d+)?)\s*[,;\s]\s*(-?\d+(?:[.,]\d+)?)$/);
  if (!m) throw Object.assign(new Error('coordenades'), { codi: 'coordenades' });
  return [parseFloat(m[1].replace(',', '.')), parseFloat(m[2].replace(',', '.'))];
}

$('entrada').addEventListener('submit', async (e) => {
  e.preventDefault();
  missatge('msg-entrada', 'Comprovant el codi…');
  try {
    codi = $('codi').value.trim();
    await actualitza();
    try { sessionStorage.setItem('codi', codi); } catch (_) { /* sense emmagatzematge: cal tornar a entrar */ }
  } catch (err) {
    codi = null;
    missatge('msg-entrada', err.codi ? textError(err) : err.message, 'error');
  }
});

$('surt').addEventListener('click', () => {
  codi = null;
  try { sessionStorage.removeItem('codi'); } catch (_) { /* res */ }
  location.reload();
});

$('contacte').addEventListener('submit', async (e) => {
  e.preventDefault();
  const f = e.target;
  try {
    await rpc('desa_contacte', { codi_acces: codi, nom: f.nom.value, carrec: f.carrec.value, correu: f.correu.value, consentiment: f.consentiment.checked });
    missatge('msg-contacte', 'Contacte desat. Gràcies!', 'be');
  } catch (err) { missatge('msg-contacte', textError(err), 'error'); }
});

$('medalla').addEventListener('submit', async (e) => {
  e.preventDefault();
  const f = e.target;
  try {
    await rpc('proposa_medalla', { codi_acces: codi, que_hi_surt: f.que_hi_surt.value, per_que: f.per_que.value, referencia: f.referencia.value });
    f.reset();
    missatge('msg-medalla', 'Proposta enviada. Gràcies!', 'be');
    await actualitza();
  } catch (err) { missatge('msg-medalla', textError(err), 'error'); }
});

$('missio').addEventListener('submit', async (e) => {
  e.preventDefault();
  const f = e.target;
  try {
    const [lat, lon] = coordenades(f.coordenades.value);
    const inici = f.data_inici.value || null;
    const fi = f.data_fi.value || inici;
    await rpc('proposa_missio', {
      codi_acces: codi, codi_ine: f.codi_ine.value, titol: f.titol.value, descripcio: f.descripcio.value, lloc: f.lloc.value,
      lat, lon, prova: f.prova.value, data_inici: inici, data_fi: inici ? fi : null,
    });
    const municipi = f.codi_ine.value;
    f.reset();
    f.codi_ine.value = municipi;
    missatge('msg-missio', 'Missió enviada. Gràcies!', 'be');
    await actualitza();
  } catch (err) {
    missatge('msg-missio', err.codi === 'coordenades' ? 'Les coordenades han de ser dos números: latitud i longitud.' : textError(err), 'error');
  }
});

$('espai').addEventListener('click', async (e) => {
  const id = e.target.dataset && e.target.dataset.retira;
  if (!id || !confirm('Voleu retirar aquesta proposta?')) return;
  try {
    await rpc('retira_proposta', { codi_acces: codi, proposta: id });
    await actualitza();
  } catch (err) { alert(textError(err)); }
});

// Si ja s'havia entrat en aquesta pestanya, no cal tornar a escriure el codi.
(async () => {
  let desat = null;
  try { desat = sessionStorage.getItem('codi'); } catch (_) { /* res */ }
  if (!desat) return;
  codi = desat;
  try { await actualitza(); } catch (_) { codi = null; }
})();

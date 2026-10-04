// Genera les missions de tots els municipis:
//   app/src/main/assets/dades/missions.json   missions que van dins de l'app
//   dades/informe_missions.txt                quants municipis només tenen missions genèriques
//
// Origen de les missions:
//   - Genèriques: les de dades/configuracio_joc.json, a tots els municipis.
//   - De lloc: Wikidata (castells, monestirs, museus, esglésies, monuments i béns culturals
//     d'interès nacional) i OpenStreetMap (historic=*, tourism=museum|viewpoint|attraction,
//     llocs de culte cristians). Només llocs amb nom i coordenades que cauen dins del municipi.
//
// Ús:
//   cd scripts && node generar-missions.js ../font/dts_municipis_cat_2025.json
//   node generar-missions.js ../font/dts_municipis_cat_2025.json --nomes-generiques
//
// Les respostes de Wikidata i d'OpenStreetMap es guarden a scripts/cache/ perquè es pugui tornar
// a executar sense consultar els servidors. Esborra la carpeta per refrescar-les.
// Si cal passar per un proxy, executa-ho amb NODE_USE_ENV_PROXY=1 (Node 22.21 o posterior).

const fs = require('fs');
const path = require('path');
const tc = require('topojson-client');
const geo = require('./lib/geometria');

const args = process.argv.slice(2);
const src = args.find((a) => !a.startsWith('--'));
const nomesGeneriques = args.includes('--nomes-generiques');
if (!src) { console.error('Falta la ruta al fitxer dts_municipis_cat_2025.json'); process.exit(1); }

const root = path.join(__dirname, '..');
const cache = path.join(__dirname, 'cache');
const config = JSON.parse(fs.readFileSync(path.join(root, 'dades/configuracio_joc.json'), 'utf8'));
const municipisApp = JSON.parse(fs.readFileSync(path.join(root, 'app/src/main/assets/dades/municipis.json'), 'utf8')).municipis;

const WIKIDATA = 'https://query.wikidata.org/sparql';
// Overpass té diversos servidors públics; si un està saturat es prova el següent.
const OVERPASS = [
  'https://overpass-api.de/api/interpreter',
  'https://overpass.private.coffee/api/interpreter',
  'https://overpass.kumi.systems/api/interpreter'
];
const AGENT = 'DescobreixCatalunya/1.0 (generador de missions; https://github.com/martifarranc/municipis)';

// Classes de Wikidata (P31) que es converteixen en missions.
const CLASSES_WIKIDATA = {
  Q23413: 'castell', // castell
  Q44613: 'monestir', // monestir
  Q33506: 'museu', // museu
  Q16970: 'esglesia', // edifici d'església
  Q2977: 'esglesia', // catedral
  Q4989906: 'monument' // monument
};

// ---------------------------------------------------------------------------
// Geometria dels municipis (resolució completa) per assignar cada lloc al seu municipi
// ---------------------------------------------------------------------------
const topo = JSON.parse(fs.readFileSync(src, 'utf8'));
const key = Object.keys(topo.objects)[0];
const vistos = new Set();
topo.objects[key].geometries = topo.objects[key].geometries.filter((g) => {
  const c = g.properties.codi_municipi_5;
  if (vistos.has(c)) return false;
  vistos.add(c);
  return true;
});
const formes = tc.feature(topo, topo.objects[key]).features.map((f) => {
  const pols = f.geometry.type === 'Polygon' ? [f.geometry.coordinates] : f.geometry.coordinates;
  return { codi: f.properties.codi_municipi_5, pols, b: geo.requadre(pols) };
});
function municipiDe(lon, lat) {
  for (const f of formes) {
    if (lon < f.b[0] || lon > f.b[2] || lat < f.b[1] || lat > f.b[3]) continue;
    if (f.pols.some((p) => geo.dinsPoligon(lon, lat, p))) return f.codi;
  }
  return null;
}

// ---------------------------------------------------------------------------
// Consultes
// ---------------------------------------------------------------------------
async function consulta(nom, urls, opcions) {
  fs.mkdirSync(cache, { recursive: true });
  const fitxer = path.join(cache, `${nom}.json`);
  if (fs.existsSync(fitxer)) {
    console.log(`${nom}: faig servir la còpia de scripts/cache/`);
    return JSON.parse(fs.readFileSync(fitxer, 'utf8'));
  }
  const llista = Array.isArray(urls) ? urls : [urls];
  let darrerError;
  // Tres intents per servidor, amb espera creixent: els servidors públics a vegades responen 429 o 504.
  for (let intent = 0; intent < 3; intent++) {
    for (const url of llista) {
      try {
        console.log(`${nom}: consultant ${new URL(url).host}...`);
        const r = await fetch(url, { ...opcions, headers: { 'User-Agent': AGENT, Accept: 'application/json', ...(opcions.headers || {}) } });
        if (!r.ok) throw new Error(`el servidor ha respost ${r.status}`);
        const json = await r.json();
        fs.writeFileSync(fitxer, JSON.stringify(json));
        return json;
      } catch (e) {
        darrerError = e;
        console.log(`${nom}: ${new URL(url).host} ha fallat (${e.message})`);
      }
    }
    await new Promise((resolt) => setTimeout(resolt, 30000 * (intent + 1)));
  }
  throw new Error(`${nom}: ${darrerError.message}`);
}

async function llocsWikidata() {
  const valors = Object.keys(CLASSES_WIKIDATA).map((q) => `wd:${q}`).join(' ');
  const sparql = `
SELECT ?item ?nom ?coord ?classe ?patrimoni WHERE {
  SERVICE wikibase:box {
    ?item wdt:P625 ?coord .
    bd:serviceParam wikibase:cornerSouthWest "Point(0.15 40.5)"^^geo:wktLiteral .
    bd:serviceParam wikibase:cornerNorthEast "Point(3.35 42.9)"^^geo:wktLiteral .
  }
  ?item rdfs:label ?nom . FILTER(LANG(?nom) = "ca")
  {
    VALUES ?classe { ${valors} }
    ?item wdt:P31 ?classe .
  } UNION {
    ?item wdt:P1435 ?designacio .
    ?designacio rdfs:label ?patrimoni . FILTER(LANG(?patrimoni) = "ca")
    FILTER(CONTAINS(LCASE(?patrimoni), "interès nacional"))
  }
}`;
  const json = await consulta('wikidata', WIKIDATA, {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded', Accept: 'application/sparql-results+json' },
    body: new URLSearchParams({ query: sparql }).toString()
  });
  const llocs = new Map();
  for (const b of json.results.bindings) {
    const qid = b.item.value.split('/').pop();
    const m = /Point\(([-\d.]+) ([-\d.]+)\)/.exec(b.coord.value);
    if (!m) continue;
    const categoria = b.patrimoni ? 'patrimoni' : CLASSES_WIKIDATA[b.classe.value.split('/').pop()];
    const anterior = llocs.get(qid);
    if (anterior && prioritat(anterior.categoria) <= prioritat(categoria)) continue;
    llocs.set(qid, {
      id: `wd-${qid}`, nom: b.nom.value, lon: +m[1], lat: +m[2], categoria,
      font: { tipus: 'wikidata', id: qid }, wikidata: qid
    });
  }
  return [...llocs.values()];
}

async function llocsOsm() {
  const ql = `
[out:json][timeout:600];
area["ISO3166-2"="ES-CT"]["admin_level"="4"]->.cat;
(
  nwr["historic"~"^(castle|monastery|church|monument|memorial|archaeological_site|ruins|tower|city_gate)$"]["name"](area.cat);
  nwr["tourism"~"^(museum|viewpoint|attraction)$"]["name"](area.cat);
  nwr["amenity"="place_of_worship"]["religion"="christian"]["name"](area.cat);
);
out center tags;`;
  const json = await consulta('osm', OVERPASS, {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body: new URLSearchParams({ data: ql }).toString()
  });
  const llocs = [];
  for (const e of json.elements) {
    const t = e.tags || {};
    const lat = e.lat ?? e.center?.lat;
    const lon = e.lon ?? e.center?.lon;
    const nom = t['name:ca'] || t.name;
    if (lat === undefined || lon === undefined || !nom) continue;
    const categoria = categoriaOsm(t);
    if (!categoria) continue;
    const tipus = { node: 'n', way: 'w', relation: 'r' }[e.type];
    llocs.push({
      id: `osm-${tipus}${e.id}`, nom, lon, lat, categoria,
      font: { tipus: 'osm', id: `${e.type}/${e.id}` }, wikidata: t.wikidata || null
    });
  }
  return llocs;
}

function categoriaOsm(t) {
  if (t.historic === 'castle') return 'castell';
  if (t.historic === 'monastery' || t.amenity === 'monastery') return 'monestir';
  if (t.tourism === 'museum') return 'museu';
  if (t.historic === 'church' || t.amenity === 'place_of_worship') return 'esglesia';
  if (t.tourism === 'viewpoint') return 'mirador';
  if (t.historic) return 'monument';
  if (t.tourism === 'attraction') return 'atraccio';
  return null;
}

const prioritat = (categoria) => config.categoriesLloc[categoria].prioritat;
function distanciaMetres(a, b) {
  const R = 6371000; const rad = Math.PI / 180;
  const dLat = (b.lat - a.lat) * rad; const dLon = (b.lon - a.lon) * rad;
  const h = Math.sin(dLat / 2) ** 2 + Math.cos(a.lat * rad) * Math.cos(b.lat * rad) * Math.sin(dLon / 2) ** 2;
  return 2 * R * Math.asin(Math.sqrt(h));
}

// Uneix els llocs repetits: mateix element de Wikidata, o a menys del radi de la prova GPS
// (una sola visita els completaria tots dos; sovint són el mateix lloc amb noms diferents).
// Es queda el de més prioritat.
function desduplica(llocs) {
  const radi = config.gps.radiMissioMetres;
  const ordenats = llocs.slice().sort((a, b) => prioritat(a.categoria) - prioritat(b.categoria) || a.id.localeCompare(b.id));
  const resultat = [];
  const perWikidata = new Set();
  for (const l of ordenats) {
    if (l.wikidata && perWikidata.has(l.wikidata)) continue;
    if (resultat.some((r) => distanciaMetres(r, l) < radi)) continue;
    resultat.push(l);
    if (l.wikidata) perWikidata.add(l.wikidata);
  }
  return resultat;
}

// Reparteix els punts dels llocs d'un municipi sense passar del pressupost.
function puntua(llocs, pressupost) {
  const p = config.punts;
  let triats = llocs.slice(0, p.maximMissionsLlocPerMunicipi).map((l) => ({ ...l, punts: config.categoriesLloc[l.categoria].punts }));
  const total = triats.reduce((s, l) => s + l.punts, 0);
  if (total > pressupost) {
    triats = triats.map((l) => ({ ...l, punts: Math.max(p.minimPerMissioLloc, Math.floor((l.punts * pressupost) / total)) }));
    while (triats.reduce((s, l) => s + l.punts, 0) > pressupost) triats.pop();
  }
  return triats;
}

async function main() {
  let llocs = [];
  if (!nomesGeneriques) {
    try {
      llocs = [...await llocsWikidata(), ...await llocsOsm()];
    } catch (e) {
      console.error(`No s'han pogut baixar les dades de Wikidata o d'OpenStreetMap: ${e.message}`);
      console.error('Torna-ho a provar amb connexió, o fes servir --nomes-generiques per generar només les missions genèriques.');
      process.exit(1);
    }
  }

  const perMunicipi = new Map(municipisApp.map((m) => [m.codi, []]));
  let fora = 0;
  for (const l of desduplica(llocs)) {
    const codi = municipiDe(l.lon, l.lat);
    if (!codi) { fora++; continue; }
    perMunicipi.get(codi).push(l);
  }

  const generiques = config.missionsGeneriques;
  const puntsGenerics = generiques.reduce((s, g) => s + g.punts, 0);
  if (puntsGenerics < config.punts.minimGarantitPerMunicipi) {
    throw new Error(`Les missions genèriques sumen ${puntsGenerics} punts, menys del mínim garantit (${config.punts.minimGarantitPerMunicipi})`);
  }
  const pressupost = config.punts.maximMissionsPerMunicipi - puntsGenerics;

  const missions = [];
  const nomesAmbGeneriques = [];
  for (const m of municipisApp) {
    for (const g of generiques) {
      missions.push({
        id: `gen-${m.codi}-${g.clau}`, municipi: m.codi, titol: g.titol, tipus: 'GENERICA', clau: g.clau,
        lat: null, lon: null, prova: g.prova, punts: g.punts, font: { tipus: 'generica' }
      });
    }
    const llocsM = perMunicipi.get(m.codi)
      .sort((a, b) => prioritat(a.categoria) - prioritat(b.categoria) || a.nom.localeCompare(b.nom, 'ca'));
    const triats = puntua(llocsM, pressupost);
    if (!triats.length) nomesAmbGeneriques.push(m.nom);
    for (const l of triats) {
      missions.push({
        id: l.id, municipi: m.codi, titol: l.nom, tipus: 'LLOC', categoria: l.categoria,
        lat: +l.lat.toFixed(6), lon: +l.lon.toFixed(6), prova: config.categoriesLloc[l.categoria].prova,
        punts: l.punts, font: l.font
      });
    }
  }

  const ids = new Set();
  for (const m of missions) {
    if (ids.has(m.id)) throw new Error(`Identificador de missió repetit: ${m.id}`);
    ids.add(m.id);
  }

  const fonts = nomesGeneriques
    ? ['Missions genèriques']
    : ['Missions genèriques', 'Wikidata (CC0)', 'OpenStreetMap (ODbL, © col·laboradors d\'OpenStreetMap)'];
  fs.writeFileSync(path.join(root, 'app/src/main/assets/dades/missions.json'), JSON.stringify({ versio: 1, fonts, missions }));

  const informe = [
    `Informe de missions (${new Date().toISOString().slice(0, 10)})`,
    '',
    `Mode: ${nomesGeneriques ? 'només genèriques (sense consultar Wikidata ni OpenStreetMap)' : 'Wikidata + OpenStreetMap + genèriques'}`,
    `Missions totals: ${missions.length}`,
    `Missions de lloc: ${missions.filter((m) => m.tipus === 'LLOC').length}`,
    `Llocs descartats perquè queden fora de Catalunya: ${fora}`,
    `Municipis només amb missions genèriques: ${nomesAmbGeneriques.length} de ${municipisApp.length}`,
    '',
    ...nomesAmbGeneriques.map((n) => `  ${n}`)
  ].join('\n');
  fs.writeFileSync(path.join(root, 'dades/informe_missions.txt'), `${informe}\n`);
  console.log(informe.split('\n').slice(0, 8).join('\n'));
}

main();

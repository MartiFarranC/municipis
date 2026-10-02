// Genera, a partir dels límits municipals reals (IGN, desembre 2024):
//   dades/municipis_catalunya.geojson      polígons dels 947 municipis (codi INE + nom), simplificats
//   dades/municipis_veins.json             per a cada municipi, la comarca i els codis INE dels veïns
//                                          (frontera + connexions especials de dades/connexions_especials.json)
//   dades/comarques.json                   les 43 comarques, amb les capitals i els municipis
//   prototip/mapa-interactiu.html          el prototip amb les dades incrustades
//   app/src/main/assets/dades/municipis.json   metadades per a l'app (noms, comarques, veïns)
//   app/src/main/assets/dades/mapa.bin         geometria projectada (Mercator) i simplificada, per dibuixar
//   app/src/main/assets/dades/limits.bin       geometria a resolució completa (lon/lat), per saber on és l'usuari
//   app/src/main/assets/dades/configuracio_joc.json   còpia de dades/configuracio_joc.json
//
// Ús:
//   git clone --depth 1 https://github.com/ArnauInes/geometries_cat_bcn_2024.git font
//   cd scripts && npm install && node generar-dades.js ../font/dts_municipis_cat_2025.json
//
// Les comarques es treuen de la capa de comarques de la mateixa font (dts_comarques_cat_2025.json,
// 43 comarques, el Lluçanès inclòs), assignant cada municipi a la comarca que en conté el punt interior.
// No es fa servir el camp de comarca dels municipis, perquè no està al dia.

const fs = require('fs');
const path = require('path');
const tc = require('topojson-client');
const ts = require('topojson-simplify');
const d3 = require('d3-geo');
const bin = require('./lib/binari');
const geo = require('./lib/geometria');

const src = process.argv[2];
if (!src) { console.error('Falta la ruta al fitxer dts_municipis_cat_2025.json'); process.exit(1); }
const srcComarques = path.join(path.dirname(src), 'dts_comarques_cat_2025.json');
const root = path.join(__dirname, '..');
const assets = path.join(root, 'app/src/main/assets/dades');
fs.mkdirSync(assets, { recursive: true });

const AMPLADA_MAPA = 20000;
// Dos nivells de detall: el general per a quan es veu tota Catalunya i el detallat per al zoom.
const SIMPLIFICACIO_APP = 1e-7;
const SIMPLIFICACIO_APP_GENERAL = 2e-6;
const SIMPLIFICACIO_GEOJSON = 3e-7;
const SIMPLIFICACIO_PROTOTIP = 2e-7;

// ---------------------------------------------------------------------------
// Municipis
// ---------------------------------------------------------------------------
const topo = JSON.parse(fs.readFileSync(src, 'utf8'));
const key = Object.keys(topo.objects)[0];

// La font repeteix cada municipi dues vegades: ens quedem amb un per codi INE.
const seen = new Set();
topo.objects[key].geometries = topo.objects[key].geometries.filter((g) => {
  const c = g.properties.codi_municipi_5;
  if (seen.has(c)) return false;
  seen.add(c);
  return true;
});
const geoms = topo.objects[key].geometries;
const n = geoms.length;
console.log('Municipis:', n);
const codiDe = (i) => geoms[i].properties.codi_municipi_5;
const nomDe = (i) => geoms[i].properties.nom_municipi;
const indexDe = new Map(geoms.map((g, i) => [g.properties.codi_municipi_5, i]));

// ---------------------------------------------------------------------------
// Veïns: frontera + connexions especials
// ---------------------------------------------------------------------------
const frontera = tc.neighbors(geoms).map((l) => l.map(codiDe));
const especials = JSON.parse(fs.readFileSync(path.join(root, 'dades/connexions_especials.json'), 'utf8')).connexions;
const especialsDe = new Map();
for (const c of especials) {
  for (const codi of [c.a, c.b]) {
    if (!indexDe.has(codi)) throw new Error(`Connexió especial amb un codi INE que no existeix: ${codi}`);
  }
  if (!especialsDe.has(c.a)) especialsDe.set(c.a, []);
  if (!especialsDe.has(c.b)) especialsDe.set(c.b, []);
  especialsDe.get(c.a).push(c.b);
  especialsDe.get(c.b).push(c.a);
}
const veinsDe = (i) => [...new Set([...frontera[i], ...(especialsDe.get(codiDe(i)) || [])])].sort();

// Comprovació: amb les connexions especials, el graf ha de ser connex.
{
  const visitats = new Set([codiDe(0)]);
  const cua = [codiDe(0)];
  while (cua.length) {
    for (const v of veinsDe(indexDe.get(cua.shift()))) if (!visitats.has(v)) { visitats.add(v); cua.push(v); }
  }
  if (visitats.size !== n) {
    const aillats = geoms.map((_, i) => i).filter((i) => !visitats.has(codiDe(i))).map(nomDe);
    throw new Error(`El graf de veïns no és connex. No s'arriba a: ${aillats.join(', ')}`);
  }
  console.log('Graf de veïns connex: des de qualsevol municipi s\'arriba als', n);
}

// ---------------------------------------------------------------------------
// Geometries: completa (lon/lat), simplificada per a l'app (projectada)
// ---------------------------------------------------------------------------
const completa = tc.feature(topo, topo.objects[key]).features;
const pre = ts.presimplify(topo);
const app = ts.simplify(pre, SIMPLIFICACIO_APP);
const appFeatures = tc.feature(app, app.objects[key]).features;
const proj = d3.geoMercator().fitWidth(AMPLADA_MAPA, { type: 'FeatureCollection', features: appFeatures });
const [tx, ty] = proj.translate();
const k = proj.scale();

const poligonsDe = (g) => (g.type === 'Polygon' ? [g.coordinates] : g.type === 'MultiPolygon' ? g.coordinates : []);
const projecta = (p) => { const [x, y] = proj(p); return [Math.round(x), Math.round(y)]; };
const netejaAnell = (anell) => {
  const r = [];
  for (const p of anell) {
    const q = projecta(p);
    if (!r.length || r[r.length - 1][0] !== q[0] || r[r.length - 1][1] !== q[1]) r.push(q);
  }
  if (r.length > 1 && r[0][0] === r[r.length - 1][0] && r[0][1] === r[r.length - 1][1]) r.pop();
  return r;
};
const geometriaApp = (features) => {
  const g = features.map((f) => poligonsDe(f.geometry)
    .map((pol) => pol.map(netejaAnell).filter((a) => a.length >= 3))
    .filter((pol) => pol.length > 0 && pol[0].length >= 3));
  g.forEach((pols, i) => { if (!pols.length) throw new Error(`${nomDe(i)} s'ha quedat sense geometria en simplificar`); });
  return g;
};
const mapa = geometriaApp(appFeatures);
const general = ts.simplify(pre, SIMPLIFICACIO_APP_GENERAL);
const mapaGeneral = geometriaApp(tc.feature(general, general.objects[key]).features);

// Punt per a l'etiqueta: dins del polígon més gran, tan lluny de les vores com es pugui.
const etiquetes = mapa.map((pols) => geo.puntInterior(pols));

// ---------------------------------------------------------------------------
// Comarques
// ---------------------------------------------------------------------------
const topoC = JSON.parse(fs.readFileSync(srcComarques, 'utf8'));
const keyC = Object.keys(topoC.objects)[0];
const vistesC = new Set();
topoC.objects[keyC].geometries = topoC.objects[keyC].geometries.filter((g) => {
  if (vistesC.has(g.properties.codi_comarca)) return false;
  vistesC.add(g.properties.codi_comarca);
  return true;
});
const comarquesF = tc.feature(topoC, topoC.objects[keyC]).features;
const requadresC = comarquesF.map((c) => d3.geoBounds(c));
const comarcaDe = etiquetes.map(([x, y], i) => {
  const lonlat = proj.invert([x, y]);
  const trobades = comarquesF.filter((c, j) => {
    const [[x0, y0], [x1, y1]] = requadresC[j];
    return lonlat[0] >= x0 && lonlat[0] <= x1 && lonlat[1] >= y0 && lonlat[1] <= y1 && d3.geoContains(c, lonlat);
  });
  if (trobades.length !== 1) throw new Error(`${nomDe(i)}: ${trobades.length} comarques contenen el punt interior`);
  return trobades[0].properties.codi_comarca;
});
const comarques = comarquesF.map((c) => {
  const codi = c.properties.codi_comarca;
  const municipis = geoms.map((_, i) => i).filter((i) => comarcaDe[i] === codi);
  const capitals = c.properties.nom_capital_comarca.split(' / ').map((nom) => {
    // La capital és un nucli: normalment té el mateix nom que el municipi (Vielha -> Vielha e Mijaran).
    const nomMin = nom.toLowerCase();
    const i = municipis.find((j) => nomDe(j).toLowerCase() === nomMin) ??
      municipis.find((j) => nomDe(j).toLowerCase().startsWith(`${nomMin} `));
    if (i === undefined) throw new Error(`No trobo la capital ${nom} a la comarca ${c.properties.nom_comarca}`);
    return codiDe(i);
  });
  return { codi, nom: c.properties.nom_comarca, capitals, municipis: municipis.map(codiDe).sort() };
}).sort((a, b) => a.codi.localeCompare(b.codi));
console.log('Comarques:', comarques.length);
for (const c of comarques) if (!c.municipis.length) throw new Error(`La comarca ${c.nom} no té cap municipi`);
const canvis = geoms.map((g, i) => [g, i]).filter(([g, i]) => g.properties.codi_comarca !== comarcaDe[i]);
console.log(`Municipis amb una comarca diferent de la del camp de l'IGN: ${canvis.length}`);
for (const [g, i] of canvis) {
  const nova = comarques.find((c) => c.codi === comarcaDe[i]).nom;
  console.log(`  ${nomDe(i)}: ${g.properties.nom_comarca} -> ${nova}`);
}

// ---------------------------------------------------------------------------
// Ordre: alfabètic en català. Aquest ordre és l'índex de tots els fitxers de l'app.
// ---------------------------------------------------------------------------
const ordre = geoms.map((_, i) => i).sort((a, b) => nomDe(a).localeCompare(nomDe(b), 'ca'));

// ---------------------------------------------------------------------------
// Fitxers de dades/
// ---------------------------------------------------------------------------
const veins = ordre.map((i) => ({
  codi_ine: codiDe(i),
  nom: nomDe(i),
  provincia: geoms[i].properties.nom_provincia,
  comarca: comarcaDe[i],
  veins: veinsDe(i),
  ...(especialsDe.has(codiDe(i)) ? { connexions_especials: especialsDe.get(codiDe(i)).slice().sort() } : {})
}));
fs.writeFileSync(path.join(root, 'dades/municipis_veins.json'), JSON.stringify(veins, null, 1));
fs.writeFileSync(path.join(root, 'dades/comarques.json'), JSON.stringify(comarques, null, 1));

const simple = ts.simplify(pre, SIMPLIFICACIO_GEOJSON);
const fc = tc.feature(simple, simple.objects[key]);
const round = (c) => (Array.isArray(c[0]) ? c.map(round) : [+c[0].toFixed(5), +c[1].toFixed(5)]);
fs.writeFileSync(path.join(root, 'dades/municipis_catalunya.geojson'), JSON.stringify({
  type: 'FeatureCollection',
  features: fc.features.map((f) => ({
    type: 'Feature',
    properties: { codi_ine: f.properties.codi_municipi_5, nom: f.properties.nom_municipi },
    geometry: { type: f.geometry.type, coordinates: round(f.geometry.coordinates) }
  }))
}));

// ---------------------------------------------------------------------------
// Assets de l'app
// ---------------------------------------------------------------------------
fs.writeFileSync(path.join(assets, 'municipis.json'), JSON.stringify({
  versio: 1,
  font: 'Instituto Geográfico Nacional (IGN), límits municipals de desembre de 2024, CC BY 4.0',
  comarques: comarques.map((c) => ({ codi: c.codi, nom: c.nom, capitals: c.capitals })),
  municipis: ordre.map((i) => ({ codi: codiDe(i), nom: nomDe(i), comarca: comarcaDe[i], veins: veinsDe(i) }))
}));
fs.copyFileSync(path.join(root, 'dades/configuracio_joc.json'), path.join(assets, 'configuracio_joc.json'));

// mapa.bin (big-endian). Format descrit a joc/src/main/kotlin/cat/descobreix/joc/dades/FormatMapa.kt
{
  const w = new bin.Escriptor();
  w.magic('DCM1');
  w.int(1);
  w.int(n);
  w.double(k); w.double(tx); w.double(ty);
  let maxX = 0; let maxY = 0;
  for (const pols of mapa) for (const pol of pols) for (const a of pol) for (const [x, y] of a) { maxX = Math.max(maxX, x); maxY = Math.max(maxY, y); }
  w.int(maxX + 1); w.int(maxY + 1);
  for (const i of ordre) {
    const [lx, ly] = etiquetes[i];
    w.int(lx); w.int(ly);
    const b = geo.requadre(mapa[i]);
    w.int(b[0]); w.int(b[1]); w.int(b[2]); w.int(b[3]);
  }
  let punts = 0;
  for (const nivell of [mapaGeneral, mapa]) {
    for (const i of ordre) {
      const pols = nivell[i];
      w.int(pols.length);
      for (const pol of pols) {
        w.int(pol.length);
        for (const a of pol) {
          w.int(a.length);
          punts += a.length;
          for (const [x, y] of a) { w.int(x); w.int(y); }
        }
      }
    }
  }
  fs.writeFileSync(path.join(assets, 'mapa.bin'), w.buffer());
  console.log(`mapa.bin: ${punts} punts, ${(w.mida() / 1024).toFixed(0)} kB`);
}

// limits.bin: capçalera amb índex i blocs per municipi, amb varints. Format a FormatLimits.kt
{
  const micro = (v) => Math.round(v * 1e6);
  const blocs = ordre.map((i) => {
    const w = new bin.Escriptor();
    const pols = poligonsDe(completa[i].geometry);
    w.varint(pols.length);
    for (const pol of pols) {
      w.varint(pol.length);
      for (const anell of pol) {
        const a = anell.slice(0, anell.length - 1); // GeoJSON repeteix el primer punt al final
        w.varint(a.length);
        let px = 0; let py = 0;
        for (const [lon, lat] of a) {
          const x = micro(lon); const y = micro(lat);
          w.zigzag(x - px); w.zigzag(y - py);
          px = x; py = y;
        }
      }
    }
    const tots = pols.flat(2);
    const b = [
      micro(Math.min(...tots.map((p) => p[0]))), micro(Math.min(...tots.map((p) => p[1]))),
      micro(Math.max(...tots.map((p) => p[0]))), micro(Math.max(...tots.map((p) => p[1])))
    ];
    return { dades: w.buffer(), b };
  });
  const capcalera = 4 + 4 + 4 + n * 5 * 4;
  const w = new bin.Escriptor();
  w.magic('DCL1');
  w.int(1);
  w.int(n);
  let offset = capcalera;
  for (const bl of blocs) {
    w.int(offset);
    for (const v of bl.b) w.int(v);
    offset += bl.dades.length;
  }
  for (const bl of blocs) w.bytes(bl.dades);
  fs.writeFileSync(path.join(assets, 'limits.bin'), w.buffer());
  console.log(`limits.bin: ${(w.mida() / 1024).toFixed(0)} kB`);
}

// ---------------------------------------------------------------------------
// Prototip web (Mercator, 4000 px d'amplada)
// ---------------------------------------------------------------------------
{
  const game = ts.simplify(pre, SIMPLIFICACIO_PROTOTIP);
  const gfc = tc.feature(game, game.objects[key]);
  const W = 4000;
  const p = d3.geoMercator().fitWidth(W, gfc);
  const gpath = d3.geoPath(p).digits(0);
  const b = gpath.bounds(gfc);
  const data = {
    ps: p.scale(), pt: p.translate(), w: Math.ceil(b[1][0]), h: Math.ceil(b[1][1]),
    m: gfc.features.map((f, i) => {
      const c = gpath.centroid(f);
      return { c: codiDe(i), n: nomDe(i), d: gpath(f), x: Math.round(c[0]), y: Math.round(c[1]), v: veinsDe(i).map((v) => indexDe.get(v)) };
    })
  };
  const tpl = fs.readFileSync(path.join(root, 'prototip/plantilla.html'), 'utf8');
  fs.writeFileSync(path.join(root, 'prototip/mapa-interactiu.html'), tpl.replace('__DATA__', JSON.stringify(data)));
}
console.log('Fet.');

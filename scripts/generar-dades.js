// Genera, a partir dels límits municipals reals (IGN, desembre 2024):
//   dades/municipis_catalunya.geojson  polígons dels 947 municipis (codi INE + nom)
//   dades/municipis_veins.json         per a cada municipi, els codis INE dels que hi fan frontera
//   prototip/mapa-interactiu.html      el prototip amb les dades incrustades
//
// Ús:
//   git clone --depth 1 https://github.com/ArnauInes/geometries_cat_bcn_2024.git font
//   cd scripts && npm install && node generar-dades.js ../font/dts_municipis_cat_2025.json

const fs = require('fs');
const path = require('path');
const tc = require('topojson-client');
const ts = require('topojson-simplify');
const d3 = require('d3-geo');

const src = process.argv[2];
if (!src) { console.error('Falta la ruta al fitxer dts_municipis_cat_2025.json'); process.exit(1); }
const root = path.join(__dirname, '..');
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
const neighbors = tc.neighbors(geoms);
console.log('Municipis:', geoms.length);

// 1. Taula de veïns
const veins = geoms.map((g, i) => ({
  codi_ine: g.properties.codi_municipi_5,
  nom: g.properties.nom_municipi,
  provincia: g.properties.nom_provincia,
  veins: neighbors[i].map((j) => geoms[j].properties.codi_municipi_5).sort()
})).sort((a, b) => a.nom.localeCompare(b.nom, 'ca'));
fs.writeFileSync(path.join(root, 'dades/municipis_veins.json'), JSON.stringify(veins, null, 1));

// 2. GeoJSON simplificat (el detall eliminat no es nota a la pantalla d'un mòbil)
const pre = ts.presimplify(topo);
const simple = ts.simplify(pre, 3e-7);
const fc = tc.feature(simple, simple.objects[key]);
const round = (c) => (Array.isArray(c[0]) ? c.map(round) : [+c[0].toFixed(5), +c[1].toFixed(5)]);
const geo = {
  type: 'FeatureCollection',
  features: fc.features.map((f) => ({
    type: 'Feature',
    properties: { codi_ine: f.properties.codi_municipi_5, nom: f.properties.nom_municipi },
    geometry: { type: f.geometry.type, coordinates: round(f.geometry.coordinates) }
  }))
};
fs.writeFileSync(path.join(root, 'dades/municipis_catalunya.geojson'), JSON.stringify(geo));

// 3. Dades projectades per al prototip (Mercator, 4000 px d'amplada)
const game = ts.simplify(pre, 2e-7);
const gfc = tc.feature(game, game.objects[key]);
const W = 4000;
const proj = d3.geoMercator().fitWidth(W, gfc);
const gpath = d3.geoPath(proj).digits(0);
const b = gpath.bounds(gfc);
const data = {
  ps: proj.scale(), pt: proj.translate(), w: Math.ceil(b[1][0]), h: Math.ceil(b[1][1]),
  m: gfc.features.map((f, i) => {
    const c = gpath.centroid(f);
    return { c: geoms[i].properties.codi_municipi_5, n: geoms[i].properties.nom_municipi, d: gpath(f), x: Math.round(c[0]), y: Math.round(c[1]), v: neighbors[i] };
  })
};
const tpl = fs.readFileSync(path.join(root, 'prototip/plantilla.html'), 'utf8');
fs.writeFileSync(path.join(root, 'prototip/mapa-interactiu.html'), tpl.replace('__DATA__', JSON.stringify(data)));
console.log('Fet.');

// Genera una migració de Supabase amb les missions oficials i la configuració del joc, a partir
// dels mateixos fitxers que van dins de l'app. El rànquing es calcula amb aquestes taules, mai amb
// els punts que envia el client (requisits.md, secció 9.3).
//
// Ús, després de generar-missions.js:
//   cd scripts && node generar-sql-missions.js
//   cd .. && npx supabase db push
//
// Cada execució crea una migració nova que substitueix totes les missions oficials.

const fs = require('fs');
const path = require('path');
const { sqlMissions } = require('./lib/sql-missions');

const root = path.join(__dirname, '..');
const missions = JSON.parse(fs.readFileSync(path.join(root, 'app/src/main/assets/dades/missions.json'), 'utf8')).missions;
const config = JSON.parse(fs.readFileSync(path.join(root, 'dades/configuracio_joc.json'), 'utf8'));

const ara = new Date().toISOString().replace(/[-:T]/g, '').slice(0, 14);
const desti = path.join(root, 'supabase/migrations', `${ara}_missions_oficials.sql`);
fs.writeFileSync(desti, sqlMissions(missions, config));
console.log(`${missions.length} missions → ${path.relative(root, desti)}`);

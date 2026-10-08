// Crea un codi de col·laboració per a la web dels col·laboradors (docs/web-collaboradors.md).
//
//   node codi-collaborador.js comarca <codi de comarca>   (per al consell comarcal)
//   node codi-collaborador.js municipi <codi INE>         (per a un ajuntament)
//
// Mostra el codi (per enviar-lo per correu) i l'SQL per donar-lo d'alta. Al servidor només hi va el resum: si es
// perd el codi, se'n fa un de nou i es dona de baixa l'antic.
'use strict';

const crypto = require('node:crypto');
const fs = require('node:fs');
const path = require('node:path');
const { resum, sql } = require('./lib/ajuntaments');

/** Un codi fàcil de dictar: 4 grups de 4 lletres i xifres, sense les que es confonen (0/O, 1/I/L). */
function codiNou() {
  const lletres = 'ABCDEFGHJKMNPQRSTUVWXYZ23456789';
  return Array.from({ length: 16 }, () => lletres[crypto.randomInt(lletres.length)]).join('').match(/.{4}/g).join('-');
}

function main() {
  const [tipus, codi] = process.argv.slice(2);
  const dades = JSON.parse(fs.readFileSync(path.join(__dirname, '..', 'app', 'src', 'main', 'assets', 'dades', 'municipis.json'), 'utf8'));
  let fila;
  if (tipus === 'comarca') {
    const c = dades.comarques.find((x) => x.codi === codi);
    if (!c) throw new Error(`No hi ha cap comarca amb el codi ${codi}`);
    fila = { tipus: 'CONSELL_COMARCAL', codi, nom: `Comarca: ${c.nom}` };
  } else if (tipus === 'municipi') {
    const m = dades.municipis.find((x) => x.codi === codi);
    if (!m) throw new Error(`No hi ha cap municipi amb el codi ${codi}`);
    fila = { tipus: 'AJUNTAMENT', codi, nom: `Municipi: ${m.nom}` };
  } else {
    throw new Error('Ús: node codi-collaborador.js comarca <codi> | municipi <codi INE>');
  }
  const nou = codiNou();
  console.log(`${fila.nom}\nCodi (envia'l per correu i no el guardis enlloc més): ${nou}\n`);
  console.log('SQL (al SQL Editor de Supabase):\n');
  console.log(
    'insert into descobreix.collaboradors (tipus, codi, nom, resum_codi_acces)\n' +
      `values (${sql(fila.tipus)}, ${sql(fila.codi)}, ${sql(fila.nom)}, ${sql(resum(nou))});`,
  );
}

try {
  main();
} catch (e) {
  console.error(e.message);
  process.exit(1);
}

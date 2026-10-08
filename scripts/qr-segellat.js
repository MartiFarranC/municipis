// Crea el codi QR d'un punt de segellat d'un ajuntament (docs/ajuntaments.md).
//
//   node qr-segellat.js <codi INE> "<títol de la missió>"
//
// Escriu la imatge del QR (SVG, per imprimir) a la carpeta `sortida/` i mostra l'SQL per entrar la missió al servidor.
// El codi del QR no es guarda enlloc més: si es perd la imatge, se'n fa un de nou i es canvia la missió.
'use strict';

const fs = require('node:fs');
const path = require('node:path');
const QRCode = require('qrcode');
const { textQrNou, resum, sql, municipi } = require('./lib/ajuntaments');

async function main() {
  const [codi, titol] = process.argv.slice(2);
  if (!codi || !titol) {
    console.error('Ús: node qr-segellat.js <codi INE> "<títol de la missió>"');
    process.exit(1);
  }
  const m = municipi(codi);
  const text = textQrNou();
  const carpeta = path.join(__dirname, 'sortida');
  fs.mkdirSync(carpeta, { recursive: true });
  const fitxer = path.join(carpeta, `qr-segellat-${codi}-${Date.now()}.svg`);
  fs.writeFileSync(fitxer, await QRCode.toString(text, { type: 'svg', errorCorrectionLevel: 'M', margin: 4 }));

  console.log(`Punt de segellat de ${m.nom} (${codi})`);
  console.log(`QR: ${fitxer}`);
  console.log('\nSQL (al SQL Editor de Supabase):\n');
  console.log(
    'insert into descobreix.missions_ajuntament (codi_ine, titol, prova, resum_qr)\n' +
      `values (${sql(codi)}, ${sql(titol)}, 'QR', ${sql(resum(text))});`,
  );
}

main().catch((e) => {
  console.error(e.message);
  process.exit(1);
});

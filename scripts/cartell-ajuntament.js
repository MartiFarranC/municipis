// Fa el cartell per promocionar l'app d'un ajuntament que col·labora (docs/ajuntaments.md).
//
//   node cartell-ajuntament.js <codi INE>
//
// Escriu `sortida/cartell-<codi>.html`, una pàgina A4 per obrir al navegador i imprimir (o desar en PDF).
// Porta un QR a la pàgina per instal·lar l'app.
'use strict';

const fs = require('node:fs');
const path = require('node:path');
const QRCode = require('qrcode');
const { URL_INSTALLACIO, municipi } = require('./lib/ajuntaments');

const escapa = (s) => String(s).replace(/[&<>"]/g, (c) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;' })[c]);

async function main() {
  const codi = process.argv[2];
  if (!codi) {
    console.error('Ús: node cartell-ajuntament.js <codi INE>');
    process.exit(1);
  }
  const m = municipi(codi);
  const qr = await QRCode.toString(URL_INSTALLACIO, { type: 'svg', errorCorrectionLevel: 'M', margin: 2, color: { dark: '#0B0E13', light: '#FFFFFF' } });
  const html = `<!doctype html>
<html lang="ca">
<meta charset="utf-8">
<title>Cartell · ${escapa(m.nom)}</title>
<link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Chakra+Petch:wght@600;700&family=Source+Sans+3:wght@400;600&display=swap">
<style>
  @page { size: A4; margin: 0 }
  * { box-sizing: border-box }
  html, body { margin: 0; background: #0B0E13; -webkit-print-color-adjust: exact; print-color-adjust: exact }
  .pagina { width: 210mm; height: 297mm; margin: 0 auto; padding: 22mm 20mm; display: flex; flex-direction: column; gap: 10mm;
    color: #EEF1F5; font-family: "Source Sans 3", system-ui, sans-serif; background: radial-gradient(circle at 70% 18%, rgba(242,181,68,.22), transparent 55%), #0B0E13 }
  .eti { font: 700 14pt "Chakra Petch", sans-serif; letter-spacing: .14em; color: #F2B544; text-transform: uppercase }
  h1 { margin: 0; font: 700 44pt/1.05 "Chakra Petch", sans-serif; text-wrap: balance }
  h1 span { color: #F2B544 }
  p { margin: 0; font-size: 17pt; line-height: 1.4; color: #C9D1DC; max-width: 150mm }
  .distintiu { align-self: flex-start; border: 1.5pt solid #F2B544; border-radius: 99px; padding: 2mm 6mm; font: 700 13pt "Chakra Petch", sans-serif; color: #F2B544 }
  .peu { margin-top: auto; display: flex; gap: 10mm; align-items: center }
  .qr { width: 62mm; height: 62mm; background: #fff; border-radius: 4mm; padding: 2mm; flex: none }
  .qr svg { width: 100%; height: 100% }
  .peu p { font-size: 15pt }
  .peu b { color: #EEF1F5 }
</style>
<div class="pagina">
  <div class="eti">Bocins de Catalunya</div>
  <h1>Descobreix <span>${escapa(m.nom)}</span> jugant</h1>
  <p>Una app per recórrer els 947 municipis de Catalunya com si fos un joc: desbloqueja municipis, fes-hi missions i omple el passaport de segells.</p>
  <div class="distintiu">★ Municipi col·laborador</div>
  <div class="peu">
    <div class="qr">${qr}</div>
    <p><b>Escaneja el codi</b> amb el mòbil per instal·lar l'app. És gratuïta, sense anuncis, i de moment només per a Android.</p>
  </div>
</div>
</html>
`;
  const carpeta = path.join(__dirname, 'sortida');
  fs.mkdirSync(carpeta, { recursive: true });
  const fitxer = path.join(carpeta, `cartell-${codi}.html`);
  fs.writeFileSync(fitxer, html);
  console.log(`Cartell de ${m.nom}: ${fitxer}`);
}

main().catch((e) => {
  console.error(e.message);
  process.exit(1);
});

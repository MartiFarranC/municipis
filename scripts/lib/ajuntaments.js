// Utilitats per als ajuntaments que col·laboren (docs/requisits.md, secció 9.6; guia a docs/ajuntaments.md).
'use strict';

const crypto = require('node:crypto');
const fs = require('node:fs');
const path = require('node:path');

/** On porta el QR d'un punt de segellat a qui no té l'app: la pàgina del projecte. */
const URL_PROJECTE = 'https://github.com/MartiFarranC/municipis';

/** Pàgina d'on s'instal·la l'app (Obtainium o l'APK de l'última versió). */
const URL_INSTALLACIO = `${URL_PROJECTE}/blob/main/docs/obtainium.md`;

/**
 * El text d'un codi QR nou per a un punt de segellat: un enllaç al projecte amb un codi a l'atzar.
 * El codi no es guarda enlloc: al servidor només hi va el resum (vegeu `resum`).
 */
function textQrNou() {
  return `${URL_PROJECTE}?segell=${crypto.randomBytes(16).toString('base64url')}`;
}

/** SHA-256 en hexadecimal del text, sense espais al principi ni al final. El mateix que fa l'app (ReglesAjuntaments). */
function resum(text) {
  return crypto.createHash('sha256').update(text.trim(), 'utf8').digest('hex');
}

/** Escapa un text per posar-lo entre cometes simples en SQL. */
function sql(text) {
  return `'${String(text).replace(/'/g, "''")}'`;
}

/** El municipi d'un codi INE, llegit de les dades generades de l'app. */
function municipi(codi) {
  const fitxer = path.join(__dirname, '..', '..', 'app', 'src', 'main', 'assets', 'dades', 'municipis.json');
  const dades = JSON.parse(fs.readFileSync(fitxer, 'utf8'));
  const m = dades.municipis.find((x) => x.codi === codi);
  if (!m) throw new Error(`No hi ha cap municipi amb el codi ${codi}`);
  return m;
}

module.exports = { URL_PROJECTE, URL_INSTALLACIO, textQrNou, resum, sql, municipi };

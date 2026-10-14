'use strict';

const test = require('node:test');
const assert = require('node:assert');
const { textQrNou, resum, sql, municipi, URL_PROJECTE } = require('../lib/ajuntaments');

test('el resum és el SHA-256 en hexadecimal, el mateix que fa l\'app', () => {
  // ReglesAjuntaments.resumQr("prova") i `encode(sha256('prova'), 'hex')` a Postgres.
  assert.strictEqual(resum('prova'), '6258a5e0eb772911d4f92be5b5db0e14511edbe01d1d0ddd1d5a2cb9db9a56ba');
  assert.strictEqual(resum('  prova\n'), resum('prova'));
});

test('cada QR nou és diferent i porta al projecte', () => {
  const a = textQrNou();
  const b = textQrNou();
  assert.notStrictEqual(a, b);
  assert.ok(a.startsWith(`${URL_PROJECTE}?segell=`));
});

test('l\'SQL escapa les cometes', () => {
  assert.strictEqual(sql("l'oficina"), "'l''oficina'");
});

test('el municipi surt de les dades de l\'app', () => {
  assert.strictEqual(municipi('08298').nom, 'Vic');
  assert.throws(() => municipi('99999'));
});

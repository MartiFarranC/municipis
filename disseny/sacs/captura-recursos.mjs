// Fa les imatges WebP dels recursos de recursos.json (generat per gen-recursos.py) i les desa a res/drawable-nodpi.
// Cal playwright-core (npm i playwright-core) i el Chromium de Playwright; ImageMagick passa el PNG a WebP.
// Ús: node captura-recursos.mjs [camí del chromium]
import { chromium } from 'playwright-core';
import { readFileSync, mkdirSync } from 'node:fs';
import { execFileSync } from 'node:child_process';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const aqui = path.dirname(fileURLToPath(import.meta.url));
const desti = path.join(aqui, '../../app/src/main/res/drawable-nodpi');
mkdirSync(desti, { recursive: true });
const { css, captures } = JSON.parse(readFileSync(path.join(aqui, 'recursos.json'), 'utf8'));
const navegador = await chromium.launch({ executablePath: process.argv[2] || '/opt/pw-browsers/chromium-1194/chrome-linux/chrome' });
const pagina = await navegador.newPage();
for (const c of captures) {
  const w = c.mida, h = c.alcada || c.mida;
  await pagina.setContent(`<html><head><style>html,body{margin:0;background:transparent}${css} svg{display:block;width:${w}px;height:${h}px;overflow:hidden}</style></head><body>${c.svg}</body></html>`);
  // Avisa si el dibuix surt de la caixa (es retallaria).
  const caixa = await pagina.evaluate(() => { const b = document.querySelector('svg').getBBox(); return [b.x, b.y, b.x + b.width, b.y + b.height]; });
  const vb = c.alcada ? [0, 0, 100, 140] : [-12, -12, 112, 112];
  if (caixa[0] < vb[0] - 1 || caixa[1] < vb[1] - 1 || caixa[2] > vb[2] + 1 || caixa[3] > vb[3] + 1) console.warn(`${c.nom} surt de la caixa`, caixa.map((v) => v.toFixed(1)).join(' '));
  const png = await pagina.locator('svg').screenshot({ omitBackground: true });
  execFileSync('convert', ['png:-', '-quality', '90', '-define', 'webp:alpha-quality=100', `webp:${path.join(desti, c.nom + '.webp')}`], { input: png });
}
await navegador.close();
console.log(`${captures.length} imatges`);

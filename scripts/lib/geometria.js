// Geometria plana per als scripts (coordenades ja projectades).

function requadre(poligons) {
  let x0 = Infinity; let y0 = Infinity; let x1 = -Infinity; let y1 = -Infinity;
  for (const pol of poligons) for (const a of pol) for (const [x, y] of a) {
    if (x < x0) x0 = x; if (y < y0) y0 = y; if (x > x1) x1 = x; if (y > y1) y1 = y;
  }
  return [x0, y0, x1, y1];
}

function area(anell) {
  let s = 0;
  for (let i = 0, j = anell.length - 1; i < anell.length; j = i++) s += (anell[j][0] - anell[i][0]) * (anell[j][1] + anell[i][1]);
  return Math.abs(s / 2);
}

function dinsAnell(x, y, anell) {
  let dins = false;
  for (let i = 0, j = anell.length - 1; i < anell.length; j = i++) {
    const [xi, yi] = anell[i]; const [xj, yj] = anell[j];
    if ((yi > y) !== (yj > y) && x < ((xj - xi) * (y - yi)) / (yj - yi) + xi) dins = !dins;
  }
  return dins;
}

// Dins del polígon: dins de l'anell exterior i fora dels forats.
function dinsPoligon(x, y, pol) {
  if (!dinsAnell(x, y, pol[0])) return false;
  for (let r = 1; r < pol.length; r++) if (dinsAnell(x, y, pol[r])) return false;
  return true;
}

function distSegment(x, y, [ax, ay], [bx, by]) {
  const dx = bx - ax; const dy = by - ay;
  const l = dx * dx + dy * dy;
  let t = l ? ((x - ax) * dx + (y - ay) * dy) / l : 0;
  t = Math.max(0, Math.min(1, t));
  return Math.hypot(x - (ax + t * dx), y - (ay + t * dy));
}

function distVora(x, y, pol) {
  let d = Infinity;
  for (const a of pol) for (let i = 0, j = a.length - 1; i < a.length; j = i++) d = Math.min(d, distSegment(x, y, a[j], a[i]));
  return d;
}

// Un punt dins del polígon més gran, tan lluny de les vores com es pugui
// (cerca en graella, refinada al voltant del millor punt).
function puntInterior(poligons) {
  const pol = poligons.reduce((m, p) => (area(p[0]) > area(m[0]) ? p : m));
  let [x0, y0, x1, y1] = requadre([pol]);
  let millor = null; let dMillor = -1;
  for (let pas = 0; pas < 3; pas++) {
    const N = 16;
    for (let i = 0; i <= N; i++) for (let j = 0; j <= N; j++) {
      const x = x0 + ((x1 - x0) * i) / N; const y = y0 + ((y1 - y0) * j) / N;
      if (!dinsPoligon(x, y, pol)) continue;
      const d = distVora(x, y, pol);
      if (d > dMillor) { dMillor = d; millor = [x, y]; }
    }
    if (!millor) break;
    const rx = (x1 - x0) / 8; const ry = (y1 - y0) / 8;
    [x0, y0, x1, y1] = [millor[0] - rx, millor[1] - ry, millor[0] + rx, millor[1] + ry];
  }
  if (!millor) millor = pol[0][0];
  return [Math.round(millor[0]), Math.round(millor[1])];
}

module.exports = { requadre, area, dinsAnell, dinsPoligon, distVora, puntInterior };

const test = require('node:test');
const assert = require('node:assert');
const bin = require('../lib/binari');
const geo = require('../lib/geometria');

test('enters big-endian i varints', () => {
  const w = new bin.Escriptor();
  w.magic('DCM1');
  w.int(-2);
  w.varint(300);
  w.zigzag(-1);
  w.zigzag(1);
  assert.deepStrictEqual([...w.buffer()], [0x44, 0x43, 0x4d, 0x31, 0xff, 0xff, 0xff, 0xfe, 0xac, 0x02, 0x01, 0x02]);
});

test('enters fora de rang', () => {
  assert.throws(() => new bin.Escriptor().int(2 ** 31));
});

test('punt dins de polígon amb forat', () => {
  const quadrat = [[0, 0], [10, 0], [10, 10], [0, 10]];
  const forat = [[4, 4], [6, 4], [6, 6], [4, 6]];
  assert.ok(geo.dinsPoligon(2, 2, [quadrat, forat]));
  assert.ok(!geo.dinsPoligon(5, 5, [quadrat, forat]));
  assert.ok(!geo.dinsPoligon(11, 5, [quadrat]));
});

test('punt interior lluny de les vores', () => {
  const [x, y] = geo.puntInterior([[[[0, 0], [100, 0], [100, 100], [0, 100]]]]);
  assert.ok(Math.abs(x - 50) <= 5 && Math.abs(y - 50) <= 5);
});

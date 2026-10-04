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

test('SQL de les missions oficials', () => {
  const { sqlMissions } = require('../lib/sql-missions');
  const config = { versio: 1, gps: { radiMissioMetres: 75, precisioMaximaMetres: 50 }, punts: { bonusTotesLesMissions: 50 } };
  const missions = [
    { id: 'gen-08001-checkin', municipi: '08001', prova: 'GPS', lat: null, lon: null, punts: 100 },
    { id: "wd-Q1", municipi: '08001', prova: 'GPS', lat: 41.5, lon: 2.1, punts: 18, titol: "L'església" },
    { id: 'osm-2', municipi: '08002', prova: 'FOTO', lat: 41.6, lon: 2.2, punts: 5 },
  ];
  const sql = sqlMissions(missions, config, { mida: 2 });
  assert.match(sql, /values \(1, 75, 50, 50\)/);
  assert.match(sql, /\('gen-08001-checkin', '08001', 'GPS', null, null, 100\)/);
  assert.match(sql, /\('osm-2', '08002', 'FOTO', 41.6, 2.2, 5\);/);
  assert.strictEqual(sql.match(/insert into descobreix.missions_oficials/g).length, 2, 'en blocs de dues files');
  assert.ok(!sql.includes('església'), 'el títol no va al servidor');

  assert.throws(() => sqlMissions([missions[0], missions[0]], config), /repetida/);
  assert.throws(() => sqlMissions([{ ...missions[0], municipi: '8001' }], config), /INE/);
  assert.throws(() => sqlMissions([{ ...missions[0], punts: -1 }], config), /Punts/);
  assert.throws(() => sqlMissions([{ ...missions[0], id: "x'); drop table y; --", prova: 'RES' }], config), /Prova/);
});

test('SQL escapa les cometes dels identificadors', () => {
  const { sqlMissions } = require('../lib/sql-missions');
  const config = { versio: 1, gps: { radiMissioMetres: 75, precisioMaximaMetres: 50 }, punts: { bonusTotesLesMissions: 50 } };
  const sql = sqlMissions([{ id: "a'b", municipi: '08001', prova: 'FOTO', lat: null, lon: null, punts: 1 }], config);
  assert.match(sql, /\('a''b', '08001'/);
});

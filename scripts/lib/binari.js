// Escriptor binari big-endian, compatible amb java.io.DataInputStream,
// amb varints (LEB128) i zigzag per als fitxers comprimits.

class Escriptor {
  constructor() { this.parts = []; this.n = 0; }

  afegeix(buf) { this.parts.push(buf); this.n += buf.length; }

  magic(text) { this.afegeix(Buffer.from(text, 'ascii')); }

  int(v) {
    if (!Number.isInteger(v) || v < -2147483648 || v > 2147483647) throw new Error(`Enter fora de rang: ${v}`);
    const b = Buffer.alloc(4); b.writeInt32BE(v); this.afegeix(b);
  }

  double(v) { const b = Buffer.alloc(8); b.writeDoubleBE(v); this.afegeix(b); }

  varint(v) {
    if (!Number.isInteger(v) || v < 0) throw new Error(`Varint invàlid: ${v}`);
    const bytes = [];
    while (v >= 0x80) { bytes.push((v & 0x7f) | 0x80); v = Math.floor(v / 128); }
    bytes.push(v);
    this.afegeix(Buffer.from(bytes));
  }

  zigzag(v) { this.varint(v >= 0 ? v * 2 : -v * 2 - 1); }

  bytes(buf) { this.afegeix(buf); }

  mida() { return this.n; }

  buffer() { return Buffer.concat(this.parts, this.n); }
}

module.exports = { Escriptor };

# Prepara els recursos de l'app que surten dels sacs: els emojis i les portades del passaport, com a SVG estàtics
# (recursos.json, per a captura-recursos.mjs, que en fa imatges WebP), i el catàleg de l'app (Kotlin i strings.xml).
# Ús: python3 gen-recursos.py && node captura-recursos.mjs
import json, os, re, unicodedata
AQUI = os.path.dirname(os.path.abspath(__file__))
ARREL = os.path.normpath(os.path.join(AQUI, '..', '..'))
EMO = os.path.join(AQUI, '..', 'emojis')
config = json.load(open(os.path.join(ARREL, 'dades', 'configuracio_joc.json')))['sacs']['objectes']

# Emojis: s'executa el generador (que també torna a escriure emojis.html, igual que abans).
g = {'__file__': os.path.join(EMO, 'gen-emojis.py')}
os.chdir(EMO)
exec(open(g['__file__']).read(), g)
os.chdir(AQUI)
def slug(t): return re.sub(r'[^a-z0-9]+', '-', unicodedata.normalize('NFD', t.lower()).encode('ascii', 'ignore').decode()).strip('-')
def sense_accents(t): return unicodedata.normalize('NFD', t).encode('ascii', 'ignore').decode()
emojis = {f'{slug(gr)}--{sense_accents(i)}': (nom, frase, s) for gr, i, nom, frase, s, _ in g['E']}
defs = re.search(r'<defs>.*?</defs>', open(os.path.join(EMO, 'emojis-plantilla.html')).read(), re.S)
DEFS = defs.group(0) if defs else ''

# Portades: es torna a executar gen-extres.py amb una tapa que només apunta els colors, i una altra que només fa el fons.
font = open(os.path.join(AQUI, 'gen-extres.py')).read().split('# Colors secundaris')[0]
colors_tapa = {}
def tapa_colors(fons, daurat, patro='', sil=True, text=None):
    return json.dumps({'fons': fons, 'daurat': daurat, 'text': text or daurat, 'silueta': sil, 'patro': patro})
ge = {'__file__': os.path.join(AQUI, 'gen-extres.py')}
exec(font.replace('PORTADES=[', 'tapa=__tapa\nPORTADES=['), dict(ge, __tapa=tapa_colors)) if False else None
ge['__tapa'] = tapa_colors
exec(font.replace('PORTADES=[', 'tapa=__tapa\nPORTADES=['), ge)
portades = {i: json.loads(s) | {'nom': n} for i, n, s in ge['PORTADES']}
font_c = open(os.path.join(AQUI, 'gen-extres.py')).read()
colors = {i: (n, h) for i, n, h in re.findall(r"\('([a-z-]+)','([^']+)','(#[0-9A-Fa-f]{6})'\)", font_c.split('COLORS=')[1].split(']')[0])}
anims = dict(re.findall(r"^ \('([a-z-]+)','((?:[^'\\]|\\')+)'", font_c.split('ANIMACIONS=[')[1].split('\n]')[0], re.M))

res = lambda t, i: f"{t}_{i.replace('--', '__').replace('-', '_')}"
captures = []
items = []
for o in config:
    t, i = o['tipus'], o['id']
    if t == 'emoji':
        nom, frase, s = emojis[i]
        # Amb marge: alguns emojis (els globus, la cascada…) surten una mica de la caixa de 100 × 100.
        captures.append({'nom': res('emoji', i), 'mida': 192, 'svg': f'<svg viewBox="-12 -12 124 124" xmlns="http://www.w3.org/2000/svg">{DEFS}{s}</svg>'})
        items.append((t, i, nom, frase))
    elif t == 'portada':
        p = portades[i]
        captures.append({'nom': res('portada', i), 'mida': 300, 'alcada': 420,
                         'svg': f'<svg viewBox="0 0 100 140" xmlns="http://www.w3.org/2000/svg"><rect width="100" height="140" fill="{p["fons"]}"/>{p["patro"]}</svg>'})
        items.append((t, i, p['nom'], None))
    elif t == 'animacio':
        items.append((t, i, anims[i].replace("\\'", "'"), None))
    else:
        items.append((t, i, colors[i][0], None))
json.dump({'css': g['CSS'], 'captures': captures}, open(os.path.join(AQUI, 'recursos.json'), 'w'), ensure_ascii=False)

# strings.xml (els noms, en català)
def x(s): return s.replace('&', '&amp;').replace('<', '&lt;').replace("'", "\\'").replace('"', '\\"')
linies = [f'    <string name="{res(t, i)}">{x(nom)}</string>' for t, i, nom, _ in items]
linies += [f'    <string name="{res(t, i)}_frase">{x(frase)}</string>' for t, i, _, frase in items if frase]
open(os.path.join(ARREL, 'app/src/main/res/values/sacs.xml'), 'w').write(
    '<?xml version="1.0" encoding="utf-8"?>\n<!-- Generat per disseny/sacs/gen-recursos.py: no l\'editis a mà. -->\n<resources>\n' + '\n'.join(linies) + '\n</resources>\n')

# Catàleg en Kotlin: d'on surt el dibuix i el nom de cada cosa.
def kc(h): return '0xFF' + h.lstrip('#').upper()
def arg(t, i): return f'"{i}"'
kt = ['package cat.descobreix.sacs', '', 'import androidx.compose.ui.graphics.Color', 'import cat.descobreix.R', '',
      '// Generat per disseny/sacs/gen-recursos.py a partir de la configuració del joc i dels dissenys: no l\'editis a mà.', '',
      '/** El nom de cada cosa de la col·lecció (strings.xml). */',
      'internal val NOMS_OBJECTES: Map<String, Int> = mapOf(']
kt += [f'    "{t}:{i}" to R.string.{res(t, i)},' for t, i, _, _ in items] + [')', '',
      '/** La frase de cada emoji, la que es fa servir com a reacció. */', 'internal val FRASES_EMOJIS: Map<String, Int> = mapOf(']
kt += [f'    "{i}" to R.string.{res(t, i)}_frase,' for t, i, _, f in items if f] + [')', '',
      '/** El dibuix de cada emoji. */', 'internal val DIBUIXOS_EMOJIS: Map<String, Int> = mapOf(']
kt += [f'    "{i}" to R.drawable.{res(t, i)},' for t, i, _, _ in items if t == 'emoji'] + [')', '',
      '/** Les portades del passaport dels sacs: el fons (amb el dibuix), el daurat, el text i si porta la silueta de Catalunya. */',
      'internal val PORTADES: Map<String, Portada> = mapOf(']
for t, i, _, _ in items:
    if t == 'portada':
        p = portades[i]
        kt.append(f'    "{i}" to Portada(R.drawable.{res(t, i)}, Color({kc(p["fons"])}), Color({kc(p["daurat"])}), Color({kc(p["text"])}), {str(p["silueta"]).lower()}),')
kt += [')', '', '/** Els colors secundaris dels sacs. */', 'internal val COLORS: Map<String, Color> = mapOf(']
kt += [f'    "{i}" to Color({kc(colors[i][1])}),' for t, i, _, _ in items if t == 'color'] + [')', '']
open(os.path.join(ARREL, 'app/src/main/kotlin/cat/descobreix/sacs/CatalegGenerat.kt'), 'w').write('\n'.join(kt))
print(len(items), 'objectes,', len(captures), 'imatges')

# Genera captures.html amb les captures de l'app que fa el CI (CapturesTest, al job «ui») i les notes de revisió.
# Les imatges van a dins de la pàgina (base64) perquè es pugui obrir des de qualsevol lloc.
import base64, glob, html, os

AQUI = os.path.dirname(os.path.abspath(__file__))
NOMS = {
    '01-inici': "Inici: d'on ets?", '02-tria': 'Tria del municipi', '03-mapa': 'Mapa', '04-missions': 'Missions',
    '05-fitxa': 'Fitxa de Vic', '06-fitxa-missions': 'Les missions de Vic', '07-perfil': 'Perfil',
    '08-perfil-opcions': 'Perfil: so i vibració', '09-sacs': 'Sacs i col·lecció',
}
VA_BE = [
    'El tema clar i el fosc segueixen el mòbil, i els dos es llegeixen bé.',
    'Les lletres noves (Archivo Black als títols, Instrument Sans al text) es veuen a totes les pantalles.',
    "Els botons tenen la vora de tinta i l'ombra dura, i les cantonades són rectes.",
    'Els teus municipis surten en groc al mapa, i la resta, sota la boira.',
    "L'opció «So i vibració» és al perfil.",
]
CAL_ARREGLAR = [
    "Mapa: el comptador de dalt («1 de 947 municipis descoberts») surt partit en una columna estreta, paraula per paraula.",
    "Mapa: les etiquetes dels noms encara són les d'abans (caixetes arrodonides), i surten noms de pobles que són a la boira.",
    'Mapa: en clar, els núvols gairebé no es veuen; cal més contrast.',
    'Barra de baix: la pestanya triada té una píndola arrodonida darrere la icona (és la de Material). Cal treure-la.',
    "Inici: encara diu «Descobreix Catalunya» amb el logo antic. Falta decidir si l'app es diu «Bocins de Catalunya».",
    "Missions: la targeta de cada poble és un requadre gris; li falta la vora de tinta com la resta.",
]
NO_ES_VEU = [
    "Les animacions (els núvols que s'obren, el gargot, el segell, el sac que tremola i el cromo que es gira): l'emulador del CI les té apagades i a les captures només surt el final.",
    "El desbloqueig amb la frase feta i el sac obert: a la partida de prova no hi ha barretines ni sacs per obrir.",
    'El so i la vibració.',
]

def img(nom):
    return 'data:image/jpeg;base64,' + base64.b64encode(open(os.path.join(AQUI, nom), 'rb').read()).decode()

def llista(xs):
    return '<ul>' + ''.join(f'<li>{html.escape(x)}</li>' for x in xs) + '</ul>'

fitxers = sorted(os.path.basename(f) for f in glob.glob(os.path.join(AQUI, '*.jpg')))
claus = sorted({f.split('-', 1)[1][:-4] for f in fitxers})
files = ''
for k in claus:
    cel = ''
    for tema in ('clar', 'fosc'):
        f = f'{tema}-{k}.jpg'
        cel += (f'<figure><img src="{img(f)}" alt="{html.escape(NOMS.get(k, k))}, tema {tema}" loading="lazy"><figcaption>{tema}</figcaption></figure>'
                if f in fitxers else f'<figure class="buida"><div>No hi ha captura en {tema}</div><figcaption>{tema}</figcaption></figure>')
    files += f'<section><h2>{html.escape(NOMS.get(k, k))}</h2><div class="parella">{cel}</div></section>'

pagina = f'''<!doctype html><html lang="ca"><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<title>Captures de l'app</title>
<style>
:root{{--fons:#fff;--tinta:#111;--suau:#666;--linia:#e4e4e4;--groc:#FFD000}}
@media (prefers-color-scheme:dark){{:root:not([data-theme="light"]){{--fons:#0f0f0f;--tinta:#f2f2f2;--suau:#9a9a9a;--linia:#2a2a2a}}}}
:root[data-theme="dark"]{{--fons:#0f0f0f;--tinta:#f2f2f2;--suau:#9a9a9a;--linia:#2a2a2a}}
*{{box-sizing:border-box}}body{{margin:0;background:var(--fons);color:var(--tinta);font:16px/1.5 system-ui,sans-serif}}
.pagina{{max-width:980px;margin:0 auto;padding:28px 16px 80px}}
small{{font-weight:700;font-size:12px;letter-spacing:.12em;text-transform:uppercase;color:var(--suau)}}
h1{{font-size:32px;line-height:1.1;margin:6px 0 12px}}
.notes{{display:grid;grid-template-columns:repeat(auto-fit,minmax(280px,1fr));gap:12px;margin:20px 0 10px}}
.notes div{{border:2px solid var(--tinta);padding:12px 14px}}
.notes h3{{margin:0 0 6px;font-size:17px}}.notes ul{{margin:0;padding-left:18px}}.notes li{{margin-bottom:6px}}
.notes .mal{{box-shadow:5px 5px 0 var(--groc)}}
section{{border-top:2px solid var(--tinta);padding:16px 0 22px}}
h2{{margin:0 0 10px;font-size:20px}}
.parella{{display:flex;gap:14px;flex-wrap:wrap}}
figure{{margin:0;flex:1 1 220px;max-width:300px}}
figure img{{width:100%;display:block;border:2px solid var(--tinta)}}
figcaption{{font-size:13px;color:var(--suau);margin-top:4px}}
.buida div{{border:2px dashed var(--linia);aspect-ratio:9/19;display:flex;align-items:center;justify-content:center;color:var(--suau);font-size:14px;padding:12px;text-align:center}}
</style>
<div class="pagina">
<small>Bocins de Catalunya · l'app de debò</small>
<h1>Com ha quedat l'estètica nova</h1>
<p>Aquestes captures són de l'app real, no d'un prototip: les fa un test automàtic que obre l'app en un emulador i hi passa dues vegades, una en clar i una en fosc. La segona vegada la partida ja hi és, per això en fosc no surten les pantalles d'inici.</p>
<div class="notes">
<div><h3>Va bé</h3>{llista(VA_BE)}</div>
<div class="mal"><h3>Cal arreglar</h3>{llista(CAL_ARREGLAR)}</div>
<div><h3>No es veu a les captures</h3>{llista(NO_ES_VEU)}</div>
</div>
{files}
</div></html>'''
open(os.path.join(AQUI, 'captures.html'), 'w').write(pagina)
print('captures.html', len(pagina))

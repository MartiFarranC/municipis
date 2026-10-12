# Genera sacs-estil.html (com és el sac i com s'obre) a partir de la plantilla i dels emojis de debò de recursos.json.
import json, os, re

AQUI = os.path.dirname(os.path.abspath(__file__))
r = json.load(open(os.path.join(AQUI, 'recursos.json')))
captures = {c['nom']: c['svg'] for c in r['captures']}
# Les frases són les de sacs.xml (emoji_*_frase).
xml = open(os.path.join(AQUI, '..', '..', 'app', 'src', 'main', 'res', 'values', 'sacs.xml')).read()
emojis = {}
for k, ident in enumerate(['cares--festa', 'anims--castell', 'celebrar--sardana']):
    nom = 'emoji_' + ident.replace('--', '__')
    svg = captures[nom]
    # Cada SVG té els seus id (p. ex. «halo»): s'hi afegeix un sufix perquè no xoquin.
    svg = re.sub(r'id="([^"]+)"', lambda m: f'id="{m.group(1)}-{k}"', svg)
    svg = re.sub(r'url\(#([^)]+)\)', lambda m: f'url(#{m.group(1)}-{k})', svg)
    frase = re.search(rf'<string name="{nom}_frase">([^<]+)</string>', xml).group(1)
    emojis[ident] = {'svg': svg, 'frase': frase}
import sys
sys.path.insert(0, os.path.join(AQUI, '..', 'estil'))
import fonts  # noqa: E402  (posa les fonts a dins de la pàgina)
for plantilla, sortida, families in [
    ('sacs-estil-plantilla.html', 'sacs-estil.html', None),
    ('sacs-estil2-plantilla.html', 'sacs-estil2.html', 'Archivo+Black&family=Instrument+Sans:wght@400;600;700&family=Caveat:wght@600'),
]:
    s = open(os.path.join(AQUI, plantilla)).read()
    s = s.replace('/*EMOJIS*/', json.dumps(emojis, ensure_ascii=False)).replace('/*CSS-EMOJIS*/', r['css'])
    if families:
        s = s.replace('/*FONTS*/', fonts.incrustades(families))
    open(os.path.join(AQUI, sortida), 'w').write(s)
    print(sortida)

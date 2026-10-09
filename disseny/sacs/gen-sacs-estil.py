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
s = open(os.path.join(AQUI, 'sacs-estil-plantilla.html')).read()
s = s.replace('/*EMOJIS*/', json.dumps(emojis, ensure_ascii=False)).replace('/*CSS-EMOJIS*/', r['css'])
open(os.path.join(AQUI, 'sacs-estil.html'), 'w').write(s)
print('sacs-estil.html')

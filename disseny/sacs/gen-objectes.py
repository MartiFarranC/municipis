# Escriu el contingut dels sacs a dades/configuracio_joc.json (bloc "sacs"), a partir de les decisions de
# docs/decisions-pendents.md: els emojis de disseny/emojis/sacs.json i els extres que van quedar al retall.
# Raresa "inicial": la té tothom des del principi.
import json, os, re
AQUI = os.path.dirname(os.path.abspath(__file__))
ARREL = os.path.join(AQUI, '..', '..')
CONFIG = os.path.join(ARREL, 'dades', 'configuracio_joc.json')

emojis = json.load(open(os.path.join(AQUI, '..', 'emojis', 'sacs.json')))
EXTRES = {
    'portada': {
        'inicial': ['cuir', 'mar', 'negre-plata', 'nit', 'pirineu', 'senyera', 'verd-bosc'],
        'comuna': ['castellers', 'costa-brava', 'crema', 'pedra-seca', 'roses'],
        'rara': ['barretina', 'bolets', 'rajola', 'riu', 'terrats'],
        'llegendaria': [],
    },
    'animacio': {
        'inicial': ['bici', 'bruixola', 'gegants', 'ocell', 'onades', 'punts'],
        'comuna': ['bastons', 'globus', 'segell'],
        'rara': ['campana', 'caragol', 'castell', 'rotllana', 'sol-lluna'],
        'llegendaria': ['cotxe', 'espiga'],
    },
    'color': {
        'inicial': ['blau-mar', 'llimona', 'rosa', 'verd-bosc', 'vermell'],
        'comuna': ['aigua', 'cel', 'lila', 'turquesa'],
        'rara': ['cirera', 'lavanda', 'pressec', 'salvia'],
        'llegendaria': ['plata'],
    },
}
objectes = [('emoji', id, 'inicial' if r == 'base' else r) for id, r in sorted(emojis.items())]
for tipus, per in EXTRES.items():
    for raresa, ids in per.items():
        objectes += [(tipus, id, raresa) for id in ids]

linies = ',\n'.join(f'      {{ "tipus": "{t}", "id": "{i}", "raresa": "{r}" }}' for t, i, r in objectes)
bloc = f'''"sacs": {{
    "probabilitats": {{ "comuna": 70, "rara": 25, "llegendaria": 5 }},
    "puntsSiJaTensTot": 50,
    "objectes": [
{linies}
    ]
  }}'''
text = open(CONFIG).read()
if '"sacs"' in text:
    text = re.sub(r'"sacs": \{.*?\n    \]\n  \}', lambda _: bloc, text, flags=re.S)
else:
    text = re.sub(r'\n\}\s*$', ',\n  ' + bloc.replace('\\', '\\\\') + '\n}\n', text)
open(CONFIG, 'w').write(text)
print(len(objectes), 'objectes')

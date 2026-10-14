# Baixa fonts de Google Fonts (llicència OFL) i en fa @font-face amb les dades a dins (base64), perquè les pàgines de
# disseny es vegin igual a qualsevol mòbil, encara que no pugui carregar fonts de fora. Només els subconjunts latin i
# latin-ext. Es desen a fonts-cache/ per no tornar-les a baixar.
import base64, os, re, sys, urllib.request

AQUI = os.path.dirname(os.path.abspath(__file__))
CACHE = os.path.join(AQUI, 'fonts-cache')
UA = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120 Safari/537.36'

def _baixa(url):
    os.makedirs(CACHE, exist_ok=True)
    nom = os.path.join(CACHE, re.sub(r'[^A-Za-z0-9._-]', '_', url)[-150:])
    if not os.path.exists(nom):
        with urllib.request.urlopen(urllib.request.Request(url, headers={'User-Agent': UA}), timeout=30) as r:
            open(nom, 'wb').write(r.read())
    return open(nom, 'rb').read()

def incrustades(families):
    """families: la part 'family=...' de l'URL css2, p. ex. 'Caveat:wght@600&family=Archivo+Black'."""
    css = _baixa('https://fonts.googleapis.com/css2?family=' + families + '&display=swap').decode()
    sortida = []
    for comentari, cos in re.findall(r'/\* ([\w-]+) \*/\s*(@font-face \{.*?\})', css, re.S):
        if comentari not in ('latin', 'latin-ext'):
            continue
        url = re.search(r'url\((https://[^)]+)\)', cos).group(1)
        dades = base64.b64encode(_baixa(url)).decode()
        sortida.append(cos.replace(url, 'data:font/woff2;base64,' + dades))
    return '\n'.join(sortida)

if __name__ == '__main__':
    print(incrustades(sys.argv[1])[:300])

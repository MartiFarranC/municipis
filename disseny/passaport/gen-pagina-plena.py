# Com queda la pàgina de la comarca amb més municipis (Alt Empordà, 68) amb tots els segells posats.
# Els llocs dels segells s'escullen a l'atzar evitant trepitjar-se tant com es pot, com faria algú que els va posant.
import json, os, math, random
AQUI=os.path.dirname(os.path.abspath(__file__))
exec(open(f'{AQUI}/gen-prototip.py').read().split('COMARCA=')[0])  # carrega les dades i camí()
COMARCA='Alt Empordà'
codis=sorted(COM[COMARCA]['municipis'],key=lambda c:PER_CODI[c]['properties']['nom'])
TINTES=['#2F5FA8','#B0303A','#2E7D4F','#6A3FA0','#1F7A8C']
W,H=720,1000  # pàgina amb la proporció d'un passaport
FILTRE='''<filter id="tinta" x="-5%" y="-5%" width="110%" height="110%"><feTurbulence type="fractalNoise" baseFrequency=".9" numOctaves="2" seed="4" result="s"/>
<feDisplacementMap in="SourceGraphic" in2="s" scale="1.6" result="m"/><feComponentTransfer in="s" result="c"><feFuncA type="discrete" tableValues="1 1 1 .55 1 1"/></feComponentTransfer><feComposite in="m" in2="c" operator="in"/></filter>'''

def segell(c,tinta,data):
    n=PER_CODI[c]['properties']['nom'].upper(); mida=min(8,78/len(n))
    return (f'<g filter="url(#tinta)" fill="none" stroke="{tinta}"><rect x="3" y="3" width="94" height="54" rx="5" stroke-width="3"/><rect x="8" y="8" width="84" height="44" rx="3" stroke-width="1"/>'
      f'<path d="{camí([c],20,30,26)}" fill="{tinta}" stroke-width=".4"/>'
      f'<text x="64" y="28" text-anchor="middle" font-family="Chakra Petch,system-ui,sans-serif" font-weight="700" font-size="{mida:.1f}" fill="{tinta}" stroke="none">{n}</text>'
      f'<text x="64" y="41" text-anchor="middle" font-family="Chakra Petch,system-ui,sans-serif" font-weight="700" font-size="7" fill="{tinta}" stroke="none">{data}</text></g>')

def llocs(n,amplada,rnd,marge_dalt=90,marge=20):
    # Per a cada segell prova 60 llocs i es queda el que queda més lluny dels que ja hi ha.
    h=amplada*.6; fets=[]
    for _ in range(n):
        millor=None
        for _ in range(60):
            x=rnd.uniform(marge+amplada/2,W-marge-amplada/2); y=rnd.uniform(marge_dalt+h/2,H-50-h/2)
            d=min([math.hypot((x-a)/amplada,(y-b)/h) for a,b in fets] or [9])
            if millor is None or d>millor[0]: millor=(d,x,y)
        fets.append(millor[1:])
    return fets

def pagina(codis_pag,amplada,llavor,titol,num):
    rnd=random.Random(llavor); o=''
    for c,(x,y) in zip(codis_pag,llocs(len(codis_pag),amplada,rnd)):
        rot=rnd.uniform(-12,12); t=rnd.choice(TINTES); s=amplada/100
        o+=f'<g transform="translate({x:.0f} {y:.0f}) rotate({rot:.0f}) scale({s:.2f}) translate(-50 -30)" opacity=".88">{segell(c,t,f"{rnd.randint(1,28):02d}·{rnd.randint(1,12):02d}·2026")}</g>'
    sil=camí(COM[COMARCA]['municipis'],50,50,92,pas=.5)
    return (f'<svg viewBox="0 0 {W} {H}" class="pag" role="img" aria-label="{titol}"><rect width="{W}" height="{H}" rx="18" fill="#F3EBD8"/>'
      f'<rect x="14" y="14" width="{W-28}" height="{H-28}" rx="8" fill="none" stroke="#D8CCB0" stroke-width="2"/>'
      f'<g transform="translate(60 140) scale(6)" opacity=".07"><path d="{sil}" fill="#4A3E28"/></g>'
      f'<text x="36" y="58" font-family="Chakra Petch,system-ui,sans-serif" font-weight="700" font-size="26" letter-spacing="3" fill="#4A3E28">{COMARCA.upper()}</text>'
      f'<text x="{W-36}" y="58" text-anchor="end" font-family="Chakra Petch,system-ui,sans-serif" font-weight="500" font-size="22" fill="#8A7A5A">{len(codis)} de {len(codis)}</text>'
      +o+f'<text x="{W/2}" y="{H-26}" text-anchor="middle" font-family="Chakra Petch,system-ui,sans-serif" font-size="16" letter-spacing="2" fill="#8A7A5A">{num}</text></svg>')

rnd=random.Random(7); ordre=codis[:]; rnd.shuffle(ordre)
A=pagina(ordre,245,1,'Mida de segell actual','PÀGINA 2')
B=pagina(ordre,150,2,'Segells més petits','PÀGINA 2')
pags=[ordre[i:i+20] for i in range(0,len(ordre),20)]
C=''.join(pagina(p,245,10+i,f'Pàgina {i+1}',f'PÀGINA {2+i}') for i,p in enumerate(pags))
css=('body{background:#0B0E13;color:#EEF1F5;font-family:system-ui,sans-serif;margin:0;padding:16px}h1{font-size:22px;margin:0 0 6px}'
     'h2{font-size:17px;margin:26px 0 4px}p{color:#9AA4B2;max-width:62ch;margin:4px 0 10px}.pag{width:100%;max-width:420px;height:auto;display:block}'
     '.fila{display:flex;gap:12px;flex-wrap:wrap}.fila .pag{max-width:300px}')
html=(f'<!doctype html><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>Passaport ple</title><style>{css}</style>'
  f'<svg width="0" height="0" style="position:absolute" aria-hidden="true"><defs>{FILTRE}</defs></svg>'
  f'<h1>L\'Alt Empordà, ple</h1><p>La comarca amb més municipis (68), amb tots els segells posats. Els noms i les siluetes són reals; els llocs, els colors i les dates són a l\'atzar.</p>'
  f'<h2>1 · Una sola pàgina, segells de la mida d\'ara</h2><p>Els 68 segells s\'han de trepitjar molt: com un passaport ben gastat, però molts noms no es llegeixen.</p>{A}'
  f'<h2>2 · Una sola pàgina, segells més petits</h2><p>Tots hi caben sense trepitjar-se gaire. Es llegeixen pitjor, però la pàgina queda plena i ordenada.</p>{B}'
  f'<h2>3 · Diverses pàgines, segells de la mida d\'ara</h2><p>Quan una pàgina té 20 segells, en comença una altra de la mateixa comarca. L\'Alt Empordà en tindria {len(pags)}.</p><div class="fila">{C}</div>')
open(f'{AQUI}/pagina-plena.html','w').write(html); print(len(html),len(pags))

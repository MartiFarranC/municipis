# Propostes de disseny de les medalles. La silueta de cada comarca surt de dades/municipis_catalunya.geojson
# (unió visual dels polígons dels seus municipis), sense inventar res.
import json, math, os
ARREL=os.path.join(os.path.dirname(os.path.abspath(__file__)),'..','..')
GEO=json.load(open(f'{ARREL}/dades/municipis_catalunya.geojson'))
COM={c['nom']:c for c in json.load(open(f'{ARREL}/dades/comarques.json'))}
PER_CODI={f['properties']['codi_ine']:f['geometry'] for f in GEO['features']}
K=math.cos(math.radians(41.7))

def anells(codi):
    g=PER_CODI[codi]
    polis=g['coordinates'] if g['type']=='MultiPolygon' else [g['coordinates']]
    return [p[0] for p in polis]

def silueta(nom,cx,cy,mida,color,vora=None,gruix=0):
    """Silueta de la comarca centrada a (cx,cy), que cap en un quadrat de costat «mida»."""
    rings=[r for codi in COM[nom]['municipis'] for r in anells(codi)]
    xs=[p[0]*K for r in rings for p in r]; ys=[-p[1] for r in rings for p in r]
    x0,x1,y0,y1=min(xs),max(xs),min(ys),max(ys); s=mida/max(x1-x0,y1-y0)
    ox=cx-(x0+x1)/2*s; oy=cy-(y0+y1)/2*s
    ds=[]
    for r in rings:
        pts=[];ult=None
        for lon,lat in r:
            x,y=lon*K*s+ox,-lat*s+oy
            if ult is None or abs(x-ult[0])+abs(y-ult[1])>.35: pts.append((x,y)); ult=(x,y)
        if len(pts)>2: ds.append('M'+'L'.join(f'{x:.1f},{y:.1f}' for x,y in pts)+'Z')
    d=''.join(ds)
    o=''
    if vora: o+=f'<path d="{d}" fill="none" stroke="{vora}" stroke-width="{gruix}" stroke-linejoin="round"/>'
    return o+f'<path d="{d}" fill="{color}" stroke="{color}" stroke-width=".5" stroke-linejoin="round"/>'

# Metalls: color base, fosc, clar i el de la silueta en relleu.
METALLS={
  'bronze':dict(b='#C98A4E',f='#8A5A2E',c='#EDBB8A',t='#5E3A1A'),
  'plata': dict(b='#C3CCD6',f='#7E8A9A',c='#F2F5F8',t='#4A5361'),
  'or':    dict(b='#F2B544',f='#B8862F',c='#FFE3A3',t='#6A4A10'),
  'gris':  dict(b='#2A313D',f='#1C2230',c='#3A4558',t='#4A5568'),
}
TXT='font-family="Chakra Petch,system-ui,sans-serif" font-weight="700"'
VERM='#D7262B'; GROC='#F2C230'

def cinta_senyera(x,y,w,h):
    # Cinta de penjar amb les quatre barres (vermell sobre groc).
    o=f'<path d="M{x},{y}h{w}l{-w*.18},{h}h{-w*.64}Z" fill="{GROC}"/>'
    for i in range(4):
        bx=x+w*(.14+i*.2); o+=f'<path d="M{bx:.1f},{y}h{w*.09:.1f}l{-w*.03:.1f},{h}h{-w*.06:.1f}Z" fill="{VERM}"/>'
    return o

def nom_curt(nom): return nom.upper()

# --- Estils. Cada funció dibuixa una medalla en un quadrat de 100 × 100.
def A_classica(nom,m,etiqueta=None):
    M=METALLS[m]; g=m=='gris'
    o='' if g else cinta_senyera(36,0,28,26)
    o+=f'<circle cx="50" cy="58" r="34" fill="{M["f"]}"/><circle cx="50" cy="58" r="30" fill="{M["b"]}"/>'
    o+=f'<circle cx="50" cy="58" r="25" fill="none" stroke="{M["c"]}" stroke-width="1.4" opacity=".7"/>'
    o+=icona(nom,50,57,38,M['t'] if not g else M['c'])
    o+=f'<path d="M14,86h72l-6,6l6,6H14l6,-6Z" fill="{M["f"] if not g else M["b"]}"/>'
    o+=f'<text x="50" y="95" text-anchor="middle" font-size="7.5" fill="{"#fff" if not g else M["c"]}" {TXT}>{etiqueta or nom_curt(nom)}</text>'
    return o
def B_escut(nom,m,etiqueta=None):
    M=METALLS[m]
    o=f'<path d="M50,4L88,16V48C88,72 70,88 50,96C30,88 12,72 12,48V16Z" fill="{M["f"]}"/>'
    o+=f'<path d="M50,10L82,20V48C82,68 67,82 50,89C33,82 18,68 18,48V20Z" fill="{M["b"]}"/>'
    o+=f'<path d="M50,10L82,20V30H18V20Z" fill="{M["c"]}" opacity=".55"/>'
    o+=icona(nom,50,56,40,M['t'] if m!='gris' else M['c'])
    o+=f'<text x="50" y="27" text-anchor="middle" font-size="7" fill="{M["t"] if m!="gris" else M["c"]}" {TXT}>{etiqueta or nom_curt(nom)}</text>'
    return o
def C_segell(nom,m,etiqueta=None):
    # Segell de passaport: el nom fa la volta per la vora.
    M=METALLS[m]; ide='arc'+str(abs(hash((nom,m,etiqueta)))%99999)
    dents=''.join(f'<circle cx="{50+44*math.cos(math.radians(a)):.1f}" cy="{50+44*math.sin(math.radians(a)):.1f}" r="4" fill="{M["f"]}"/>' for a in range(0,360,15))
    o=dents+f'<circle cx="50" cy="50" r="43" fill="{M["f"]}"/><circle cx="50" cy="50" r="40" fill="{M["b"]}"/>'
    o+=f'<circle cx="50" cy="50" r="27" fill="none" stroke="{M["t"] if m!="gris" else M["c"]}" stroke-width="1.2" stroke-dasharray="2 2"/>'
    o+=f'<defs><path id="{ide}" d="M18,50A32,32 0 1 1 82,50"/></defs><text font-size="8" fill="{M["t"] if m!="gris" else M["c"]}" letter-spacing="1" {TXT}><textPath href="#{ide}" startOffset="50%" text-anchor="middle">{etiqueta or nom_curt(nom)}</textPath></text>'
    o+=icona(nom,50,54,36,M['t'] if m!='gris' else M['c'])
    return o
def D_hexagon(nom,m,etiqueta=None):
    M=METALLS[m]
    hexa=lambda r: 'M'+'L'.join(f'{50+r*math.cos(math.radians(a)):.1f},{48+r*math.sin(math.radians(a)):.1f}' for a in range(-90,270,60))+'Z'
    o=f'<path d="{hexa(42)}" fill="{M["f"]}"/><path d="{hexa(37)}" fill="{M["b"]}"/>'
    o+=f'<path d="M50,11L82,29.5L50,48Z" fill="{M["c"]}" opacity=".35"/><path d="M18,66.5L50,85L50,48Z" fill="{M["f"]}" opacity=".3"/>'
    o+=icona(nom,50,48,44,M['t'] if m!='gris' else M['c'])
    o+=f'<rect x="16" y="84" width="68" height="13" rx="6.5" fill="{M["f"]}"/><text x="50" y="93.5" text-anchor="middle" font-size="7.5" fill="{"#fff" if m!="gris" else M["c"]}" {TXT}>{etiqueta or nom_curt(nom)}</text>'
    return o
def E_fanal(nom,m,etiqueta=None):
    # Amb la llum del fanal de l'app: raigs darrere la medalla.
    M=METALLS[m]; g=m=='gris'
    raigs='' if g else ''.join(f'<path d="M50,50L{50+48*math.cos(math.radians(a-5)):.1f},{50+48*math.sin(math.radians(a-5)):.1f}L{50+48*math.cos(math.radians(a+5)):.1f},{50+48*math.sin(math.radians(a+5)):.1f}Z" fill="{M["c"]}" opacity=".35"/>' for a in range(0,360,30))
    o=raigs+f'<circle cx="50" cy="50" r="34" fill="{M["f"]}"/><circle cx="50" cy="50" r="31" fill="{M["b"]}"/>'
    o+=f'<path d="M24,40A28,28 0 0 1 72,26" fill="none" stroke="{M["c"]}" stroke-width="3" stroke-linecap="round" opacity=".8"/>'
    o+=icona(nom,50,45,36,M['t'] if not g else M['c'])
    o+=f'<text x="50" y="72" text-anchor="middle" font-size="6.5" fill="{M["t"] if not g else M["c"]}" {TXT}>{etiqueta or nom_curt(nom)}</text>'
    return o
def F_cartell(nom,m,etiqueta=None):
    # Com un cartell d'entrada de poble, en placa de metall.
    M=METALLS[m]
    o=f'<rect x="6" y="16" width="88" height="68" rx="8" fill="{M["f"]}"/><rect x="10" y="20" width="80" height="60" rx="5" fill="{M["b"]}"/>'
    o+=f'<rect x="14" y="24" width="72" height="52" rx="3" fill="none" stroke="{M["t"] if m!="gris" else M["c"]}" stroke-width="1.6"/>'
    o+=icona(nom,50,45,30,M['t'] if m!='gris' else M['c'])
    o+=f'<text x="50" y="70" text-anchor="middle" font-size="7.5" fill="{M["t"] if m!="gris" else M["c"]}" {TXT}>{etiqueta or nom_curt(nom)}</text>'
    return o

def icona(nom,cx,cy,mida,color):
    if nom in COM: return silueta(nom,cx,cy,mida,color)
    t=f'font-size="{mida*.55:.0f}" fill="{color}" {TXT}'
    if nom.isdigit(): return f'<text x="{cx}" y="{cy+mida*.2:.1f}" text-anchor="middle" {t}>{nom}</text>'
    if nom=='CAPITALS': return f'<path d="M{cx},{cy-mida*.4}l{mida*.12},{mida*.25}l{mida*.28},{mida*.04}l{-mida*.2},{mida*.2}l{mida*.06},{mida*.28}l{-mida*.26},{-mida*.14}l{-mida*.26},{mida*.14}l{mida*.06},{-mida*.28}l{-mida*.2},{-mida*.2}l{mida*.28},{-mida*.04}Z" fill="{color}"/>'
    if nom=='CARTELLS': return f'<rect x="{cx-mida*.38}" y="{cy-mida*.28}" width="{mida*.76}" height="{mida*.4}" rx="2" fill="none" stroke="{color}" stroke-width="2.4"/><path d="M{cx-mida*.2},{cy+mida*.12}v{mida*.3}M{cx+mida*.2},{cy+mida*.12}v{mida*.3}" stroke="{color}" stroke-width="2.4"/>'
    return ''

ESTILS=[('A','Clàssica amb la cinta de la senyera',A_classica),('B','Escut',B_escut),('C','Segell de passaport',C_segell),
        ('D','Hexàgon',D_hexagon),('E','Amb la llum del fanal',E_fanal),('F','Placa com un cartell d\'entrada',F_cartell)]

def quatre(f,nom):
    # Bronze, plata, or i encara no aconseguida (gris), en una graella de 2 × 2.
    cel=lambda x,y,m: f'<g transform="translate({x} {y}) scale(.48)" style="transform-box:view-box;transform-origin:0 0">{f(nom,m)}</g>'
    return cel(1,1,'bronze')+cel(51,1,'plata')+cel(1,51,'or')+cel(51,51,'gris')

if __name__=='__main__':
    files=''
    for k,desc,f in ESTILS:
        mostres=''.join(f'<figure><svg viewBox="0 0 100 100" width="150" height="150">{f(n,m,e)}</svg><figcaption>{c}</figcaption></figure>'
          for n,m,e,c in [('Osona','bronze',None,'Osona · bronze'),('Garrotxa','plata',None,'Garrotxa · plata'),('Alt Empordà','or',None,'Alt Empordà · or'),('Segrià','gris',None,'Segrià · encara no'),
                          ('100','or','100 MUNICIPIS','Fita: 100 municipis'),('CAPITALS','or','CAPITALS','Totes les capitals'),('CARTELLS','plata','25 CARTELLS','Fita: 25 cartells')])
        files+=f'<section><h2>{k} · {desc}</h2><div class="fila">{mostres}</div></section>'
    open(os.path.join(os.path.dirname(os.path.abspath(__file__)),'medalles.html'),'w').write(
      '<!doctype html><meta charset="utf-8"><title>Propostes de medalles</title><style>body{background:#0B0E13;color:#EEF1F5;font-family:system-ui;margin:16px}'
      'h1{font-size:22px}.fila{display:flex;flex-wrap:wrap;gap:8px}figure{margin:0;text-align:center;font-size:12px;color:#9AA4B2}h2{font-size:16px}</style>'+'<h1>Propostes de medalles</h1><p style="color:#9AA4B2">Sis estils. A cada un: tres comarques (bronze, plata i or), una que encara no tens (en gris) i les medalles de fites, capitals i cartells. Les siluetes són les reals de cada comarca.</p>'+files)

# Propostes del passaport: la tapa i la pàgina d'una comarca amb els segells dels municipis.
# Les siluetes i els noms surten de dades/; les dates i quins municipis estan segellats són només un exemple.
import json, math, os, random
AQUI=os.path.dirname(os.path.abspath(__file__)); ARREL=os.path.join(AQUI,'..','..')
GEO=json.load(open(f'{ARREL}/dades/municipis_catalunya.geojson'))
COM={c['nom']:c for c in json.load(open(f'{ARREL}/dades/comarques.json'))}
PER_CODI={f['properties']['codi_ine']:f for f in GEO['features']}
K=math.cos(math.radians(41.7))
TXT='font-family="Chakra Petch,system-ui,sans-serif" font-weight="700"'

def silueta(codis,cx,cy,mida,color,pas=.3):
    rings=[]
    for c in codis:
        g=PER_CODI[c]['geometry']; rings+=[p[0] for p in (g['coordinates'] if g['type']=='MultiPolygon' else [g['coordinates']])]
    xs=[p[0]*K for r in rings for p in r]; ys=[-p[1] for r in rings for p in r]
    x0,x1,y0,y1=min(xs),max(xs),min(ys),max(ys); s=mida/max(x1-x0,y1-y0); ox=cx-(x0+x1)/2*s; oy=cy-(y0+y1)/2*s
    d=''
    for r in rings:
        pts=[];u=None
        for lon,lat in r:
            x,y=lon*K*s+ox,-lat*s+oy
            if u is None or abs(x-u[0])+abs(y-u[1])>pas: pts.append((x,y)); u=(x,y)
        if len(pts)>2: d+='M'+'L'.join(f'{x:.1f},{y:.1f}' for x,y in pts)+'Z'
    return f'<path d="{d}" fill="{color}" stroke="{color}" stroke-width=".4" stroke-linejoin="round"/>'

def nom(c): return PER_CODI[c]['properties']['nom']
TINTES=['#2F5FA8','#B0303A','#2E7D4F','#6A3FA0','#1F7A8C']

def segell_rodo(c,tinta,data,ide):
    n=nom(c).upper()
    return (f'<circle cx="50" cy="50" r="44" fill="none" stroke="{tinta}" stroke-width="3"/><circle cx="50" cy="50" r="37" fill="none" stroke="{tinta}" stroke-width="1.2"/>'
      +f'<defs><path id="{ide}" d="M16,50A34,34 0 1 1 84,50"/></defs><text font-size="{min(9,190/len(n)):.1f}" fill="{tinta}" letter-spacing=".5" {TXT}><textPath href="#{ide}" startOffset="50%" text-anchor="middle">{n}</textPath></text>'
      +silueta([c],50,52,30,tinta)+f'<text x="50" y="80" text-anchor="middle" font-size="7" fill="{tinta}" {TXT}>{data}</text>')
def segell_rect(c,tinta,data,ide):
    n=nom(c).upper()
    return (f'<rect x="6" y="20" width="88" height="60" rx="6" fill="none" stroke="{tinta}" stroke-width="3"/><rect x="11" y="25" width="78" height="50" rx="3" fill="none" stroke="{tinta}" stroke-width="1"/>'
      +silueta([c],26,50,22,tinta)+f'<text x="63" y="46" text-anchor="middle" font-size="{min(8,78/len(n)):.1f}" fill="{tinta}" {TXT}>{n}</text>'
      +f'<text x="63" y="60" text-anchor="middle" font-size="7" fill="{tinta}" {TXT}>{data}</text>')
def buit(c,ide,amb_nom):
    o='<circle cx="50" cy="50" r="40" fill="none" stroke="#B9AC90" stroke-width="2" stroke-dasharray="4 4"/>'
    if amb_nom: o+=silueta([c],50,46,30,'#D9CCAE')+f'<text x="50" y="82" text-anchor="middle" font-size="{min(7,110/len(nom(c))):.1f}" fill="#A89B80" {TXT}>{nom(c).upper()}</text>'
    else: o+=f'<text x="50" y="58" text-anchor="middle" font-size="26" fill="#C9BC9E" {TXT}>?</text>'
    return o

def pagina(comarca,segell,amb_nom,llavor):
    rnd=random.Random(llavor); codis=sorted(COM[comarca]['municipis'],key=nom)
    fets=set(rnd.sample(codis,len(codis)*3//5))
    cel=''
    for i,c in enumerate(codis):
        ide=f'p{llavor}_{i}'
        if c in fets:
            rot=rnd.uniform(-14,14); t=rnd.choice(TINTES); dia=rnd.randint(1,28); mes=rnd.randint(1,10)
            cos=f'<g transform="rotate({rot:.0f} 50 50)" style="transform-box:view-box;transform-origin:0 0" opacity=".88">{segell(c,t,f"{dia:02d}·{mes:02d}·2026",ide)}</g>'
        else: cos=buit(c,ide,amb_nom)
        cel+=f'<svg viewBox="0 0 100 100" class="seg">{cos}</svg>'
    cap=(f'<div class="cap"><svg viewBox="0 0 100 100" width="56" height="56">{silueta(COM[comarca]["municipis"],50,50,80,"#6A5A3A")}</svg>'
         f'<div><b>{comarca.upper()}</b><span>{len(fets)} de {len(codis)} municipis</span></div></div>')
    return f'<div class="pag">{cap}<div class="graella">{cel}</div></div>'

def tapa(fons,daurat,titol2):
    tots=[c for v in COM.values() for c in v['municipis']]
    return (f'<svg viewBox="0 0 100 140" class="tapa"><rect width="100" height="140" rx="6" fill="{fons}"/>'
      +f'<rect x="5" y="5" width="90" height="130" rx="4" fill="none" stroke="{daurat}" stroke-width=".8" opacity=".7"/>'
      +f'<text x="50" y="24" text-anchor="middle" font-size="9" fill="{daurat}" letter-spacing="2" {TXT}>PASSAPORT</text>'
      +silueta(tots,50,68,58,daurat,pas=.6)
      +f'<text x="50" y="112" text-anchor="middle" font-size="5" fill="{daurat}" letter-spacing=".6" {TXT}>{titol2}</text>'
      +f'<text x="50" y="122" text-anchor="middle" font-size="4.5" fill="{daurat}" opacity=".8" {TXT}>947 MUNICIPIS</text></svg>')

if __name__=='__main__':
    css=('body{background:#0B0E13;color:#EEF1F5;font-family:system-ui,sans-serif;margin:0;padding:16px}h1{font-size:22px;margin:0 0 6px}h2{font-size:16px;margin:22px 0 8px}'
         'p{color:#9AA4B2;max-width:62ch;margin:4px 0}.fila{display:flex;flex-wrap:wrap;gap:16px}.tapa{width:170px;height:238px}'
         '.pag{background:#F3EBD8;border-radius:10px;padding:14px;max-width:420px;box-shadow:0 2px 0 #D8CCB0 inset}'
         '.cap{display:flex;gap:10px;align-items:center;color:#4A3E28;margin-bottom:8px}.cap b{display:block;font-size:16px;letter-spacing:1px}.cap span{font-size:13px;color:#7A6A4A}'
         '.graella{display:grid;grid-template-columns:repeat(3,1fr);gap:4px}.seg{width:100%;height:auto}')
    html=(f'<!doctype html><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>Propostes de passaport</title><style>{css}</style>'
      '<h1>Propostes de passaport</h1><p>Cada municipi descobert posa el seu segell a la pàgina de la seva comarca. Les siluetes i els noms són reals; quins municipis estan segellats i les dates són només un exemple.</p>'
      '<h2>1 · Tapa</h2><div class="fila">'+tapa('#6E1B22','#E8C36A','DESCOBREIX CATALUNYA')+tapa('#16243F','#E8C36A','DESCOBREIX CATALUNYA')+tapa('#1C2330','#F2B544','DESCOBREIX CATALUNYA')+'</div>'
      '<p>Granat, blau fosc o els colors de l\'app (fosc amb ambre).</p>'
      '<h2>2 · Segell rodó · els que falten amb la silueta i el nom</h2>'+pagina('Garrotxa',segell_rodo,True,1)
      +'<h2>3 · Segell rodó · els que falten són sorpresa</h2>'+pagina('Pla de l\'Estany',segell_rodo,False,2)
      +'<h2>4 · Segell rectangular</h2>'+pagina('Garrotxa',segell_rect,True,3))
    open(f'{AQUI}/passaport.html','w').write(html)

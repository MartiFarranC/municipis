# Prototip interactiu de segellar el passaport: l'usuari tria on posa el segell a la pàgina de la comarca.
# Les siluetes i els noms surten de dades/; els segells que ja hi ha i les seves dates són només un exemple.
import json, os, math
AQUI=os.path.dirname(os.path.abspath(__file__)); ARREL=os.path.join(AQUI,'..','..')
GEO=json.load(open(f'{ARREL}/dades/municipis_catalunya.geojson'))
COM={c['nom']:c for c in json.load(open(f'{ARREL}/dades/comarques.json'))}
PER_CODI={f['properties']['codi_ine']:f for f in GEO['features']}
K=math.cos(math.radians(41.7))

def camí(codis,cx,cy,mida,pas=.3):
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
    return d

COMARCA='Garrotxa'
codis=sorted(COM[COMARCA]['municipis'],key=lambda c:PER_CODI[c]['properties']['nom'])
munis=[{'codi':c,'nom':PER_CODI[c]['properties']['nom'],'d':camí([c],20,30,26)} for c in codis]
# Exemple: municipis que ja tenen segell, amb posició (en % de la pàgina), gir, tinta i data d'exemple.
exemple=[('Olot',18,24,-8,0,'02·05·2026'),('Santa Pau',62,20,6,1,'02·05·2026'),('Castellfollit de la Roca',30,46,4,2,'14·06·2026'),
         ('Sant Joan les Fonts',70,44,-5,3,'14·06·2026'),('Besalú',22,70,-3,4,'21·07·2026'),('Mieres',66,76,9,0,'03·08·2026')]
per_nom={m['nom']:m for m in munis}
ja=[{'nom':n,'x':x,'y':y,'rot':r,'tinta':t,'data':d} for n,x,y,r,t,d in exemple if n in per_nom]
pendents=[m['nom'] for m in munis if m['nom'] not in {e['nom'] for e in ja}]
dades={'comarca':COMARCA,'total':len(munis),'munis':{m['nom']:m['d'] for m in munis},'ja':ja,'pendents':pendents,
       'silueta':camí(COM[COMARCA]['municipis'],50,50,92,pas=.5)}
html=open(f'{AQUI}/prototip-plantilla.html').read().replace('%%DADES%%',json.dumps(dades,ensure_ascii=False).replace('</','<\\/'))
open(f'{AQUI}/prototip-segellar.html','w').write(html); print(len(html), len(ja), len(pendents))

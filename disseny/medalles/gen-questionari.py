# Qüestionari per triar el dibuix de la medalla de cada comarca (punt 7). El Martí diu què és el més
# representatiu de cada comarca, perquè no s'inventi res. Les siluetes surten de dades/ (vegeu gen-medalles.py).
import json, math, os
AQUI=os.path.dirname(os.path.abspath(__file__))
ARREL=os.path.join(AQUI,'..','..')
GEO=json.load(open(f'{ARREL}/dades/municipis_catalunya.geojson'))
COMARQUES=json.load(open(f'{ARREL}/dades/comarques.json'))
PER_CODI={f['properties']['codi_ine']:f['geometry'] for f in GEO['features']}
K=math.cos(math.radians(41.7))

def anells(codi):
    g=PER_CODI[codi]
    polis=g['coordinates'] if g['type']=='MultiPolygon' else [g['coordinates']]
    return [p[0] for p in polis]

def cami(com,cx,cy,mida):
    rings=[r for codi in com['municipis'] for r in anells(codi)]
    xs=[p[0]*K for r in rings for p in r]; ys=[-p[1] for r in rings for p in r]
    x0,x1,y0,y1=min(xs),max(xs),min(ys),max(ys); s=mida/max(x1-x0,y1-y0)
    ox=cx-(x0+x1)/2*s; oy=cy-(y0+y1)/2*s
    ds=[]
    for r in rings:
        pts=[];ult=None
        for lon,lat in r:
            x,y=lon*K*s+ox,-lat*s+oy
            if ult is None or abs(x-ult[0])+abs(y-ult[1])>.6: pts.append((x,y)); ult=(x,y)
        if len(pts)>2: ds.append('M'+'L'.join(f'{x:.1f},{y:.1f}' for x,y in pts)+'Z')
    return ''.join(ds)

coms=[{'codi':c['codi'],'nom':c['nom'],'n':len(c['municipis']),'d':cami(c,50,50,84)} for c in sorted(COMARQUES,key=lambda c:c['nom'])]
plantilla=open(f'{AQUI}/questionari-plantilla.html').read()
open(f'{AQUI}/questionari.html','w').write(plantilla.replace('/*COMARQUES*/[]',json.dumps(coms,ensure_ascii=False)))
print(len(coms),'comarques')

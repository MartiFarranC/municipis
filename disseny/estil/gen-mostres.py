# Pantalles de mostra amb l'estètica de Nothing OS (proposta per canviar l'estètica de l'app i la web).
# El mapa és un tros real d'Osona (dades/municipis_catalunya.geojson) i les missions són les reals de Vic.
import json, math, os
AQUI=os.path.dirname(os.path.abspath(__file__)); ARREL=os.path.join(AQUI,'..','..')
GEO=json.load(open(f'{ARREL}/dades/municipis_catalunya.geojson'))
MU=json.load(open(f'{ARREL}/app/src/main/assets/dades/municipis.json'))
MI=json.load(open(f'{ARREL}/app/src/main/assets/dades/missions.json'))
K=math.cos(math.radians(41.7))
noms={m['codi']:m['nom'] for m in MU['municipis']}
veins={m['codi']:m['veins'] for m in MU['municipis']}
descoberts={'08298','08100'}  # Vic i Gurb
disponibles={v for d in descoberts for v in veins[d]}-descoberts
# Finestra al voltant de Vic.
lon0,lon1=2.10,2.40
# L'alçada surt de l'amplada, perquè el mapa no es deformi (els graus de longitud són més curts).
lat_mig=41.93; lat_span=(lon1-lon0)*K*330/300; lat0,lat1=lat_mig-lat_span/2,lat_mig+lat_span/2
W,H=300,330
def proj(lon,lat): return ((lon-lon0)/(lon1-lon0)*W, (lat1-lat)/(lat1-lat0)*H)
K=math.cos(math.radians(41.93))
peces=[]
for f in GEO['features']:
    codi=f['properties']['codi_ine']; g=f['geometry']
    polis=g['coordinates'] if g['type']=='MultiPolygon' else [g['coordinates']]
    ds=[]; dins=False
    for p in polis:
        r=p[0]; pts=[proj(lo,la) for lo,la in r]
        if any(0<=x<=W and 0<=y<=H for x,y in pts): dins=True
        ds.append('M'+'L'.join(f'{x:.1f},{y:.1f}' for x,y in pts[::2])+'Z')
    if not dins: continue
    estat='D' if codi in descoberts else 'V' if codi in disponibles else 'B'
    xs=[proj(lo,la)[0] for p in polis for lo,la in p[0]]; ys=[proj(lo,la)[1] for p in polis for lo,la in p[0]]
    peces.append({'c':codi,'n':noms.get(codi,''),'e':estat,'d':''.join(ds),'x':round(sum(xs)/len(xs),1),'y':round(sum(ys)/len(ys),1)})
vic=[{'t':m['titol'],'p':m['punts'],'g':m['prova']} for m in MI['missions'] if m['municipi']=='08298'][:6]
totes=[m['punts'] for m in MI['missions'] if m['municipi']=='08298']
dades={'mapa':peces,'vic':vic,'total':sum(totes)+50,'nmissions':len(totes)}
t=open(f'{AQUI}/mostres-plantilla.html').read()
open(f'{AQUI}/mostres-nothing.html','w').write(t.replace('/*DADES*/{}',json.dumps(dades,ensure_ascii=False)))
print(len(peces),'municipis al mapa')

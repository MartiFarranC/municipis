# Prototip interactiu de totes les pantalles de l'app amb l'estètica d'ara («nit i fanals»), en fosc i en clar.
# Dades reals: el mapa d'un tros d'Osona, les missions de cada municipi i les regles de la configuració.
import json, math, os
AQUI=os.path.dirname(os.path.abspath(__file__)); ARREL=os.path.join(AQUI,'..','..')
GEO=json.load(open(f'{ARREL}/dades/municipis_catalunya.geojson'))
MU=json.load(open(f'{ARREL}/app/src/main/assets/dades/municipis.json'))
MI=json.load(open(f'{ARREL}/app/src/main/assets/dades/missions.json'))
CFG=json.load(open(f'{ARREL}/dades/configuracio_joc.json'))
noms={m['codi']:m['nom'] for m in MU['municipis']}
comarca={m['codi']:m['comarca'] for m in MU['municipis']}
veins={m['codi']:m['veins'] for m in MU['municipis']}
nomcom={c['codi']:c['nom'] for c in MU['comarques']}
K=math.cos(math.radians(41.93))
lon0,lon1=2.06,2.44; W,H=300,330
lat_span=(lon1-lon0)*K*H/W; lat0,lat1=41.93-lat_span/2,41.93+lat_span/2
def proj(lo,la): return ((lo-lon0)/(lon1-lon0)*W,(lat1-la)/(lat1-lat0)*H)
peces=[]
for f in GEO['features']:
    codi=f['properties']['codi_ine']; g=f['geometry']
    polis=g['coordinates'] if g['type']=='MultiPolygon' else [g['coordinates']]
    pts_all=[proj(lo,la) for p in polis for lo,la in p[0]]
    if not any(0<=x<=W and 0<=y<=H for x,y in pts_all): continue
    d=''.join('M'+'L'.join(f'{x:.1f},{y:.1f}' for x,y in [proj(lo,la) for lo,la in p[0]][::2])+'Z' for p in polis)
    xs=[p[0] for p in pts_all]; ys=[p[1] for p in pts_all]
    peces.append({'c':codi,'n':noms[codi],'k':nomcom[comarca[codi]],'d':d,'x':round(sum(xs)/len(xs),1),'y':round(sum(ys)/len(ys),1),
                  'v':[v for v in veins[codi]]})
dins={p['c'] for p in peces}
missions={}
for m in MI['missions']:
    if m['municipi'] in dins: missions.setdefault(m['municipi'],[]).append({'id':m['id'],'t':m['titol'],'p':m['punts'],'g':m['prova']})
osona=[m for m in MU['municipis'] if nomcom[m['comarca']]=='Osona']
dades={'mapa':peces,'missions':missions,'cfg':{'base':CFG['desbloqueig']['costBase'],'inc':CFG['desbloqueig']['costIncrementPerMunicipi'],
       'max':CFG['desbloqueig']['costMaxim'],'bonus':CFG['punts']['bonusTotesLesMissions'],'nivell1':CFG['nivells']['puntsPrimerNivell'],'nivellInc':CFG['nivells']['increment']},
       'osona':sorted(m['nom'] for m in osona),'nOsona':len(osona),'total':len(MU['municipis']),'comarques':len(MU['comarques']),'finestra':{'lon0':lon0,'lon1':lon1,'lat0':lat0,'lat1':lat1}}
for plantilla,sortida in [('prototip-plantilla.html','prototip-app.html'),('estils-plantilla.html','estils-app.html'),('paletes-plantilla.html','paletes-app.html'),('conceptes-plantilla.html','conceptes.html'),('conceptes2-plantilla.html','conceptes2.html'),('conceptes3-plantilla.html','conceptes3.html'),('conceptes4-plantilla.html','conceptes4.html'),('conceptes5-plantilla.html','conceptes5.html'),('conceptes6-plantilla.html','conceptes6.html')]:
    t=open(f'{AQUI}/{plantilla}').read()
    open(f'{AQUI}/{sortida}','w').write(t.replace('/*DADES*/{}',json.dumps(dades,ensure_ascii=False)))
print(len(peces),'municipis;',sum(len(v) for v in missions.values()),'missions')

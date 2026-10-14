# Construeix la pàgina de la ronda per retallar els extres a partir de la plantilla i de la primera valoració.
import json,glob,os,sys
AQUI=os.path.dirname(os.path.abspath(__file__))
valoracio=sys.argv[1]  # carpeta amb els JSON de la col·lecció extres2
retall=sys.argv[2] if len(sys.argv)>2 else None  # opcional: la col·lecció extres3, per a les que s'han tornat a arreglar
t=open(f'{AQUI}/retalla-plantilla.html').read()
d=json.load(open(f'{AQUI}/extres.json'))
vals={}
for f in glob.glob(f'{valoracio}/*.json'):
    x=json.load(open(f)); vals[(x['tipus'],x['id'])]=x
D={tp:[dict(p,**({'arreglat':vals[(tp,p['id'])].get('nota','')} if vals[(tp,p['id'])]['valor']=='arreglar' else {})) for p in d[tp] if (tp,p['id']) in vals and vals[(tp,p['id'])]['valor'] in ('si','arreglar')] for tp in ['portades','animacions','colors']}
if retall:
    for tp in D:
        for p in D[tp]: p.pop('arreglat',None)
    for f in glob.glob(f'{retall}/*.json'):
        x=json.load(open(f))
        if x['valor']=='arreglar':
            for p in D[x['tipus']]:
                if p['id']==x['id']: p['arreglat']=x['nota']
open(f'{AQUI}/retalla-extres.html','w').write(t.replace('%%CSS%%',d['css']).replace('%%DADES%%',json.dumps(D,ensure_ascii=False).replace('</','<\\/')))

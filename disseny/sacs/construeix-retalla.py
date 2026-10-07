# Construeix la pàgina de la ronda per retallar els extres a partir de la plantilla i de la primera valoració.
import json,glob,os,sys
AQUI=os.path.dirname(os.path.abspath(__file__))
valoracio=sys.argv[1]  # carpeta amb els JSON de la col·lecció extres2
t=open(f'{AQUI}/retalla-plantilla.html').read()
d=json.load(open(f'{AQUI}/extres.json'))
vals={}
for f in glob.glob(f'{valoracio}/*.json'):
    x=json.load(open(f)); vals[(x['tipus'],x['id'])]=x
D={tp:[dict(p,**({'arreglat':vals[(tp,p['id'])].get('nota','')} if vals[(tp,p['id'])]['valor']=='arreglar' else {})) for p in d[tp] if (tp,p['id']) in vals and vals[(tp,p['id'])]['valor'] in ('si','arreglar')] for tp in ['portades','animacions','colors']}
open(f'{AQUI}/retalla-extres.html','w').write(t.replace('%%CSS%%',d['css']).replace('%%DADES%%',json.dumps(D,ensure_ascii=False).replace('</','<\\/')))

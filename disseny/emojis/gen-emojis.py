import json, math
import os
S=os.path.dirname(os.path.abspath(__file__))
LOW=json.load(open(f'{S}/logo2.json'))['low']
A='#F2B544'; AF='#B8862F'; AC='#FFE3A3'; R='#D7262B'; RF='#9E1C20'; FE='#8A96AA'; FEF='#4A525F'
BL='#EEF1F5'; PELL='#E8B98A'; VERD='#5E9E5A'; VERDF='#3E6E3C'; MARRO='#8A5A34'; MARROC='#B07A4A'; BLAU='#5AB8E8'; GRIS='#9AA4B2'
def P(d,f,x=''): return f'<path d="{d}" fill="{f}" {x}/>'
def L(d,c,w,x=''): return f'<path d="{d}" fill="none" stroke="{c}" stroke-width="{w}" stroke-linecap="round" stroke-linejoin="round" {x}/>'
def C(x,y,r,f,x2=''): return f'<circle cx="{x}" cy="{y}" r="{r}" fill="{f}" {x2}/>'
def G(cls,inner,x=''): return f'<g class="{cls}" {x}>{inner}</g>'
HALO=lambda x,y,r,cls='': f'<circle class="{cls}" cx="{x}" cy="{y}" r="{r}" fill="url(#halo)"/>'
E=[]
exec(open(f'{S}/barretina_nova.py').read())
def barretina(x,y,r): return barr_svg(x,y,r)
def em(grup,id_,nom,frase,svg,anim): E.append((grup,id_,nom,frase,svg,anim))


# ---- Cares amb barretina
TINTA='#3A2410'
BARR=barr_svg(50,58,33)
def cara(trets,fons=A,extra='',cap=True):
    return (f'<circle cx="50" cy="58" r="33" fill="{fons}"/>'
            +trets+(BARR if cap else '')+extra)
def ma(x,y,rot=0,c=A,e=1):
    return (f'<g transform="translate({x} {y}) rotate({rot}) scale({e})" style="transform-box:view-box;transform-origin:0 0">'
            +P('M-9,-2c0,-8 3,-12 9,-12s9,4 9,12v6c0,6 -4,10 -9,10s-9,-4 -9,-10Z',c,f'stroke="{TINTA}" stroke-width="2"')
            +L('M-3,-13v-5M2,-14v-6M7,-12v-5',TINTA,1.6)+'</g>')
ulls=lambda: C(39,54,3.6,TINTA)+C(61,54,3.6,TINTA)
em('Cares','riure','Rient','Em pixo de riure',
  G('e-riu',cara(L('M32,52l7,-4l7,4M54,52l7,-4l7,4',TINTA,3)+P('M30,62h40c0,12 -9,20 -20,20s-20,-8 -20,-20Z',TINTA)+P('M40,76c3,-4 17,-4 20,0c-3,4 -17,4 -20,0Z',R)
   +G('e-llagrima',P('M24,52c-4,6 -6,9 -6,12a5,5 0 0 0 10,0c0,-3 -2,-6 -4,-12Z',BLAU)+P('M76,52c4,6 6,9 6,12a5,5 0 0 1 -10,0c0,-3 2,-6 4,-12Z',BLAU)))),'riu')
em('Cares','content','Content','Que bé!',
  G('e-bota',cara(G('e-parpella',ulls())+L('M36,68q14,12 28,0',TINTA,3.4)+C(31,64,4,R,'opacity=".25"')+C(69,64,4,R,'opacity=".25"'))),'parpella')
cor=lambda x,y,s: P(f'M{x},{y+6*s}c{-6*s},{-4*s} {-9*s},{-7*s} {-9*s},{-10*s}c0,{-3*s} {2*s},{-5*s} {4.5*s},{-5*s}c{2*s},0 {3.5*s},{1*s} {4.5*s},{2.5*s}c{1*s},{-1.5*s} {2.5*s},{-2.5*s} {4.5*s},{-2.5*s}c{2.5*s},0 {4.5*s},{2*s} {4.5*s},{5*s}c0,{3*s} {-3*s},{6*s} {-9*s},{10*s}Z',R)
em('Cares','enamorat','Enamorat','M\'encanta',
  G('e-flota',cara(G('e-cors',cor(39,52,1)+cor(61,52,1))+L('M34,68q16,14 32,0',TINTA,3.4))),'cors')
em('Cares','sorpres','Sorprès','Ostres!',
  G('e-salt',cara(C(39,54,4.6,BL)+C(61,54,4.6,BL)+C(39,54,2.4,TINTA)+C(61,54,2.4,TINTA)+L('M32,44q7,-5 13,-1M55,43q6,-4 13,1',TINTA,2.6)+'<ellipse cx="50" cy="74" rx="6" ry="8" fill="'+TINTA+'"/>')),'salt')
em('Cares','trist','Plorant','Quina pena',
  G('e-sanglot',cara(L('M33,56q6,-4 12,0M55,56q6,-4 12,0',TINTA,3)+L('M38,76q12,-9 24,0',TINTA,3.4)+L('M32,47l10,-3M68,47l-10,-3',TINTA,2.4)
   +G('e-gota',P('M64,60c-3,5 -5,8 -5,10a5,5 0 0 0 10,0c0,-2 -2,-5 -5,-10Z',BLAU)))),'gota')
em('Cares','enfadat','Enfadat','Quina ràbia',
  G('e-sacseja',cara(L('M31,46l14,6M69,46l-14,6',TINTA,3.4)+ulls()+L('M38,76q12,-8 24,0',TINTA,3.4),'#E8743A')),'sacseja')
em('Cares','ulleres','Amb ulleres','Crack',
  cara(P('M26,48h20c2,0 3,1 3,3c0,6 -4,10 -10,10h-4c-6,0 -10,-4 -10,-10c0,-2 1,-3 1,-3ZM54,48h20c1,0 1,1 1,3c0,6 -4,10 -10,10h-4c-6,0 -10,-4 -10,-10c0,-2 1,-3 3,-3Z','#14171C')
   +L('M46,50h8',"#14171C",3)+L('M40,71q12,6 22,-2',TINTA,3.4)+G('e-brill',L('M30,52l5,-3',BL,2))),'brill')
em('Cares','pensant','Pensant','Mmm…',
  cara(G('e-mirada',C(42,54,3.6,TINTA)+C(64,54,3.6,TINTA))+L('M33,45l11,-3M56,40q7,-3 13,2',TINTA,2.6)+L('M40,74h18',TINTA,3.4)
   +G('e-punts',C(16,44,2.5,AC,'style="--i:0"')+C(9,36,3,AC,'style="--i:1"'))),'punts')
em('Cares','ullet','Picant l\'ullet','Ja m\'entens',
  G('e-inclina',cara(C(39,54,3.6,TINTA)+G('e-ullet',L('M55,54q6,-5 12,0',TINTA,3.2))+L('M36,68q14,12 28,0',TINTA,3.4)+P('M55,74c2,6 10,6 10,-1Z',R))),'ullet')
em('Cares','festa','De festa','Festa major!',
  G('e-balla',cara(L('M32,54q7,-6 14,0M54,54q7,-6 14,0',TINTA,3)+P('M32,64h36c0,10 -8,16 -18,16s-18,-6 -18,-16Z',TINTA)+P('M42,74c3,-3 13,-3 16,0c-3,4 -13,4 -16,0Z',R),
   extra=''.join(f'<rect class="k-confeti2" style="--i:{i}" x="{x}" y="4" width="3" height="5" rx=".6" fill="{c}"/>' for i,(x,c) in enumerate([(10,AC),(22,BLAU),(84,AC),(92,R),(6,R),(94,BLAU)])))),'confeti')
estrella=lambda x,y,r: P(f'M{x},{y-r}L{x+r*.3},{y-r*.3}L{x+r},{y}L{x+r*.3},{y+r*.3}L{x},{y+r}L{x-r*.3},{y+r*.3}L{x-r},{y}L{x-r*.3},{y-r*.3}Z',AC)
em('Cares','flipo','Flipant','Flipo',
  cara(G('e-estels2',estrella(39,54,8)+estrella(61,54,8))+P('M38,68h24c0,8 -5,12 -12,12s-12,-4 -12,-12Z',TINTA)),'estels2')
em('Cares','dormint','Dormint','Estic rebentat',
  G('e-respira',cara(L('M33,56q6,4 12,0M55,56q6,4 12,0',TINTA,3)+'<ellipse cx="50" cy="73" rx="5" ry="4" fill="'+TINTA+'"/>',
   extra=''.join(f'<text class="e-z" style="--i:{i}" x="{70+i*7}" y="{36-i*9}" font-size="{10+i*3}" fill="{AC}" font-family="Chakra Petch,sans-serif" font-weight="700">z</text>' for i in range(3)))),'z')
em('Cares','suant','Suant','Uf, quina pujada',
  cara(L('M33,52l12,3M67,52l-12,3',TINTA,3)+C(39,58,3,TINTA)+C(61,58,3,TINTA)+L('M38,74q6,-4 12,0t12,0',TINTA,3)
   +G('e-suor',P('M80,40c-4,6 -6,9 -6,12a6,6 0 0 0 12,0c0,-3 -2,-6 -6,-12Z',BLAU))),'suor')
em('Cares','peto','Fent un petó','Petonets',
  cara(C(39,54,3.6,TINTA)+L('M55,54q6,-5 12,0',TINTA,3.2)+L('M47,66q6,2 2,5q6,2 1,6',TINTA,3)
   +G('e-cor-vola',cor(70,70,.8))),'cor-vola')
em('Cares','vergonya','Avergonyit','Ai, que vergonya',
  G('e-encongeix',cara(L('M33,57q6,3 12,0M55,57q6,3 12,0',TINTA,3)+L('M42,72q8,4 16,0',TINTA,3)+G('e-galtes','<ellipse cx="30" cy="66" rx="7" ry="4" fill="'+R+'" opacity=".5"/><ellipse cx="70" cy="66" rx="7" ry="4" fill="'+R+'" opacity=".5"/>'))),'galtes')

em('Cares','rofl','Rodolant','M\'escanyo de riure',
  G('e-rodola',cara(L('M32,52l7,-4l7,4M54,52l7,-4l7,4',TINTA,3)+P('M30,62h40c0,12 -9,20 -20,20s-20,-8 -20,-20Z',TINTA)+P('M40,76c3,-4 17,-4 20,0c-3,4 -17,4 -20,0Z',R)
   +P('M22,56c-5,4 -8,7 -8,10a5,5 0 0 0 10,0Z',BLAU)+P('M78,56c5,4 8,7 8,10a5,5 0 0 1 -10,0Z',BLAU))),'rodola')
em('Cares','peta','Petant el cap','Em peta el cap',
  cara(C(39,56,5,BL)+C(61,56,5,BL)+C(39,56,2.2,TINTA)+C(61,56,2.2,TINTA)+'<ellipse cx="50" cy="76" rx="7" ry="6" fill="'+TINTA+'"/>',cap=False,
   extra=G('e-fum',''.join(C(x,y,r,'#C9CED6') for x,y,r in [(36,26,8),(50,20,10),(64,26,8),(44,14,6),(58,12,7)]))+G('e-barr-vola',BARR)),'peta')
em('Cares','bravo','Aplaudint','Bravo!',
  G('e-bota',cara(L('M32,54q7,-6 14,0M54,54q7,-6 14,0',TINTA,3)+P('M36,62h28c0,8 -6,12 -14,12s-14,-4 -14,-12Z',TINTA)))
  +G('e-ma-esq',ma(24,88,30,e=.85))+G('e-ma-dre',ma(76,88,-30,e=.85))+G('e-clap',L('M44,90l-4,-6M50,92v-8M56,90l4,-6',AC,2.6)),'bravo')
em('Cares','ei','Saludant','Ei!',
  cara(G('e-parpella',ulls())+L('M36,68q14,12 28,0',TINTA,3.4))+G('e-saluda',ma(86,62,15)),'saluda')
nota=lambda x,y,c: C(x,y,3,c)+L(f'M{x+3},{y}V{y-11}l5,2',c,1.8)
em('Cares','balla','Ballant','A ballar!',
  G('e-balla',cara(L('M32,54q7,-6 14,0M54,54q7,-6 14,0',TINTA,3)+L('M36,68q14,12 28,0',TINTA,3.4)))
  +G('e-nota',nota(10,40,AC),'style="--i:0"')+G('e-nota',nota(86,56,A),'style="--i:1"'),'balla')
em('Cares','fastic','Fastiguejat','Quin fàstic',
  G('e-arcada',cara(L('M33,54l12,3M67,54l-12,3',TINTA,3)+L('M36,70q7,-5 14,0t14,0',TINTA,3.4)+P('M48,73c0,8 8,10 10,2Z','#E26A8A'),'#9BC26A')),'arcada')
em('Cares','fred','Glaçat','Quin fred!',
  G('e-tremola',cara(C(39,54,3.6,TINTA)+C(61,54,3.6,TINTA)+f'<rect x="34" y="66" width="32" height="12" rx="4" fill="{BL}" stroke="{TINTA}" stroke-width="2.4"/>'+L('M42,66v12M50,66v12M58,66v12',TINTA,1.6)
   +P('M30,88l3,8l3,-8ZM62,90l3,7l3,-7Z','#CFE6F2'),'#8CC4E8')),'tremola')
em('Cares','fonc','Fonent-se','Em fonc',
  G('e-fon',cara(L('M33,56q6,-3 12,0M55,56q6,-3 12,0',TINTA,3)+L('M38,72q12,6 24,0',TINTA,3.4)
   +P('M30,84c0,8 4,12 4,12M70,84c0,6 -3,10 -3,10',"none",f'stroke="{A}" stroke-width="6" stroke-linecap="round"'),'#F2A544')),'fon')
espiral=lambda x,y: L(f'M{x},{y}m-1,0a1,1 0 1,1 2,0a3,3 0 1,1 -5,0a5,5 0 1,1 9,0',TINTA,2)
em('Cares','mareig','Marejat','Estic marejat',
  G('e-trontolla',cara(G('e-espiral',espiral(39,54),'style="transform-origin:39px 54px"')+G('e-espiral',espiral(61,54),'style="transform-origin:61px 54px"')+L('M38,74q6,-4 12,0t12,0',TINTA,3),'#D9C25A')),'trontolla')
em('Cares','visca','Cridant','Visca!',
  G('e-crida',cara(L('M31,48l12,4M69,48l-12,4',TINTA,3)+L('M33,56q6,-5 12,0M55,56q6,-5 12,0',TINTA,3)+'<ellipse cx="50" cy="74" rx="11" ry="10" fill="'+TINTA+'"/>'+P('M42,79c3,-3 13,-3 16,0c-3,3 -13,3 -16,0Z',R)))
  +G('e-crit',L('M8,60h8M10,46l7,4M10,74l7,-4M92,60h-8M90,46l-7,4M90,74l-7,-4',AC,3)),'visca')
em('Cares','salut','Bevent amb porró','Salut!',
  G('e-enrere',cara(L('M33,54q6,-5 12,0M55,54q6,-5 12,0',TINTA,3)+'<ellipse cx="48" cy="74" rx="8" ry="7" fill="'+TINTA+'"/>'))
  +G('e-porro2','<g transform="translate(106 18) scale(-.52 .52)" style="transform-box:view-box;transform-origin:0 0">'
     +P('M38,52c-12,4 -18,14 -16,24c2,10 14,14 28,14s26,-4 28,-14c2,-10 -4,-20 -16,-24V18c0,-3 -2,-5 -4,-5h-8c-2,0 -4,2 -4,5Z','#E8F4FA')
     +P('M23,72c1,10 12,15 27,15s26,-5 27,-15c-7,3 -16,5 -27,5s-20,-2 -27,-5Z',R)
     +L('M38,52c-12,4 -18,14 -16,24c2,10 14,14 28,14s26,-4 28,-14c2,-10 -4,-20 -16,-24V18c0,-3 -2,-5 -4,-5h-8c-2,0 -4,2 -4,5Z',TINTA,4.4)
     +P('M64,58L88,32L92,36L70,64Z','#E8F4FA',f'stroke="{TINTA}" stroke-width="4" stroke-linejoin="round"')+'</g>')
  +P('M58,36C52,40 50,54 49,67','none',f'class="e-raig2" stroke="{R}" stroke-width="3.2" stroke-linecap="round" stroke-dasharray="3 3"'),'salut')
em('Cares','trapella','Trapella','Quina trapelleria',
  cara(L('M31,50l13,4M69,50l-13,4',TINTA,3.2)+C(40,57,3.2,TINTA)+C(60,57,3.2,TINTA)+P('M34,68q16,14 32,0q-16,6 -32,0Z',TINTA),'#B06AD8',
   extra=G('e-banyes',P('M20,34c-6,-10 -4,-20 2,-24c0,8 4,14 10,18Z',R)+P('M80,34c6,-10 4,-20 -2,-24c0,8 -4,14 -10,18Z',R))),'banyes')
em('Cares','sant','Sant','Sóc un sant',
  cara(L('M33,56q6,3 12,0M55,56q6,3 12,0',TINTA,3)+L('M38,70q12,8 24,0',TINTA,3.4),
   extra=G('e-aureola','<ellipse cx="50" cy="8" rx="20" ry="5" fill="none" stroke="'+AC+'" stroke-width="3.4"/>')),'aureola')
em('Cares','atxim','Esternudant','Atxim!',
  G('e-esternut',cara(L('M33,54l12,2M67,54l-12,2',TINTA,3)+'<ellipse cx="50" cy="74" rx="9" ry="6" fill="'+TINTA+'"/>'))
  +G('e-gotes',''.join(C(x,y,r,'#BFE3F5') for x,y,r in [(50,90,3),(40,94,2.4),(60,94,2.4),(30,90,2),(70,90,2)])),'atxim')
em('Cares','corre','Corrent','Que arribo tard!',
  G('e-corre',cara(L('M33,52l12,3M67,52l-12,3',TINTA,3)+C(41,57,3.4,TINTA)+C(63,57,3.4,TINTA)+P('M40,70h20c0,6 -4,9 -10,9s-10,-3 -10,-9Z',TINTA)
   +P('M80,42c-3,5 -5,8 -5,10a5,5 0 0 0 10,0c0,-2 -2,-5 -5,-10Z',BLAU)))
  +G('e-velocitat',L('M4,46h12M2,58h10M6,70h12',GRIS,3)),'corre')

em('Cares','no','Negant','Ni de conya',
  G('e-nega',cara(L('M32,48l12,3M68,48l-12,3',TINTA,3)+ulls()+L('M38,74h24',TINTA,3.6))),'nega')
em('Cares','si','Assentint','Sí, sí!',
  G('e-assent',cara(L('M32,54q7,-6 14,0M54,54q7,-6 14,0',TINTA,3)+L('M36,68q14,12 28,0',TINTA,3.4))),'assent')
em('Cares','shh','Fent silenci','Xxxt!',
  cara(L('M33,52l12,2M67,52l-12,2',TINTA,3)+C(39,58,3,TINTA)+C(61,58,3,TINTA)+L('M42,74q8,-3 16,0',TINTA,3))
  +G('e-xxt','<rect x="46" y="58" width="8" height="26" rx="4" fill="'+A+'" stroke="'+TINTA+'" stroke-width="2"/>'
     +'<rect x="40" y="78" width="20" height="16" rx="7" fill="'+A+'" stroke="'+TINTA+'" stroke-width="2"/>')
  +G('e-ones',L('M80,62q4,4 0,8M86,58q6,8 0,16',AC,2.4)),'xxt')
em('Cares','alucino','Al·lucinant','Al·lucino',
  cara(C(39,52,6,BL)+C(61,52,6,BL)+C(39,52,2.6,TINTA)+C(61,52,2.6,TINTA)+L('M30,40q8,-6 16,-2M54,38q8,-4 16,2',TINTA,2.6)
   +G('e-mandibula','<ellipse cx="50" cy="76" rx="8" ry="10" fill="'+TINTA+'"/>'+P('M44,82c3,-2 9,-2 12,0c-2,3 -10,3 -12,0Z',R))),'mandibula')
em('Cares','mars','Plorant a mars','Ploro a mars',
  G('e-sanglot',cara(L('M32,54q7,-5 14,0M54,54q7,-5 14,0',TINTA,3)+P('M36,78q14,-12 28,0q-14,-4 -28,0Z',TINTA)
   +G('e-cascada',P('M36,56h6v40h-6ZM58,56h6v40h-6Z',BLAU,'opacity=".85"')))),'cascada')
em('Cares','llengua','Fent llengotes','Mec!',
  G('e-inclina',cara(C(39,54,3.6,TINTA)+L('M55,54q6,-5 12,0',TINTA,3.2)+L('M38,68q12,6 24,0',TINTA,3.4)
   +G('e-llengua',P('M44,70h12v8c0,5 -3,8 -6,8s-6,-3 -6,-8Z','#E26A8A')+L('M50,72v8','#B84868',1.4)))),'llengua')
em('Cares','niidea','Arronsant les espatlles','Ni idea',
  G('e-arronsa',cara(L('M33,48l11,-2M56,46l11,2',TINTA,2.6)+ulls()+L('M40,74q5,-3 10,0t10,0',TINTA,3))
   +ma(14,66,-60,e=.8)+ma(86,66,60,e=.8)),'arronsa')
em('Cares','ullsalcel','Ulls al cel','Quina paciència',
  cara(C(39,54,5.6,BL)+C(61,54,5.6,BL)+G('e-giraull',C(39,50,2.6,TINTA)+C(61,50,2.6,TINTA))+L('M40,74h20',TINTA,3.4)),'giraull')
em('Cares','sospitos','Sospitós','Mmm, sospitós',
  cara(L('M31,52h16M53,52h16',TINTA,3)+G('e-reull',C(42,56,2.6,TINTA)+C(64,56,2.6,TINTA))+L('M40,74q10,-3 20,1',TINTA,3)),'reull')
em('Cares','malalt','Malalt','Estic fotut',
  G('e-tremola-lent',cara(L('M33,56q6,3 12,0M55,56q6,3 12,0',TINTA,3)+L('M40,74q10,-5 20,0',TINTA,3)
   +L('M50,68l20,12',BL,4)+C(71,81,3,R),'#B7CF7A')
   +G('e-suor',P('M24,44c-3,5 -5,8 -5,10a5,5 0 0 0 10,0c0,-2 -2,-5 -5,-10Z',BLAU))),'malalt')
em('Cares','xalant','Xalant','Que bé m\'ho passo',
  G('e-bota',cara(L('M32,54q7,-7 14,0M54,54q7,-7 14,0',TINTA,3.2)+P('M30,62h40c0,12 -9,20 -20,20s-20,-8 -20,-20Z',TINTA)+P('M40,76c3,-4 17,-4 20,0c-3,4 -17,4 -20,0Z',R)
   +C(28,66,5,R,'opacity=".3"')+C(72,66,5,R,'opacity=".3"')))
  +''.join(G('e-espurneig',estrella(x,y,5),f'style="--i:{i}"') for i,(x,y) in enumerate([(10,40),(90,46),(14,82),(88,84)])),'xalant')
em('Cares','bull','Bullint','Em bull la sang',
  G('e-sacseja',cara(L('M30,46l15,7M70,46l-15,7',TINTA,3.6)+ulls()+P('M38,76h24v-4c-8,-4 -16,-4 -24,0Z',TINTA),'#D9442C'))
  +G('e-vapor',''.join(C(x,y,r,'#C9CED6') for x,y,r in [(10,56,5),(6,46,6),(90,56,5),(94,46,6)])),'vapor')
em('Cares','por','Esglaiat','Quina por!',
  G('e-tremola',cara(C(39,52,5,BL)+C(61,52,5,BL)+C(39,52,2.2,TINTA)+C(61,52,2.2,TINTA)+'<ellipse cx="50" cy="76" rx="6" ry="10" fill="'+TINTA+'"/>','#C8D8E8')
   +ma(20,72,-15,c='#C8D8E8',e=.8)+ma(80,72,15,c='#C8D8E8',e=.8)),'por')
em('Cares','son','Badallant','Quina son',
  G('e-badall',cara(L('M33,54q6,3 12,0M55,54q6,3 12,0',TINTA,3)+'<ellipse class="e-boca-badall" cx="50" cy="74" rx="8" ry="9" fill="'+TINTA+'"/>'))
  +G('e-ma-badall',ma(66,82,-30,e=.75)),'badall')
em('Cares','gana','Afamat','Tinc gana',
  G('e-flota',cara(L('M32,54q7,-6 14,0M54,54q7,-6 14,0',TINTA,3)+L('M36,70q14,8 28,0',TINTA,3.4)
   +G('e-llepa',P('M54,72c4,0 8,2 8,6c0,3 -3,4 -5,3c-2,-1 -3,-4 -3,-9Z','#E26A8A'))
   +G('e-baba',P('M40,74c-2,6 -3,10 -3,12a3,3 0 0 0 6,0c0,-2 -1,-6 -3,-12Z',BLAU)))),'gana')

em('Cares','emociono','Emocionat','M\'emociono',
  G('e-sanglot',cara(L('M32,54q7,-6 14,0M54,54q7,-6 14,0',TINTA,3)+L('M36,68q14,10 28,0',TINTA,3.4)
   +G('e-gota',P('M30,58c-3,5 -5,8 -5,10a5,5 0 0 0 10,0c0,-2 -2,-5 -5,-10Z',BLAU)))),'emociono')
em('Cares','burleta','Burleta','Je, je',
  G('e-inclina',cara(L('M32,52h12M56,50l12,-2',TINTA,3)+C(39,57,3,TINTA)+C(61,55,3,TINTA)+L('M40,72q14,4 22,-6',TINTA,3.4))),'burleta')
em('Cares','mut','Sense paraules','Sense paraules',
  G('e-reull',cara(ulls())),'mut')
em('Cares','decepcio','Decebut','Quina decepció',
  G('e-esbufega',cara(L('M33,57h12M55,57h12',TINTA,3)+L('M40,76q10,-4 20,0',TINTA,3.2)))
  +G('e-buf',C(64,82,4,'#C9CED6')+C(72,86,3,'#C9CED6')),'decepcio')
em('Cares','selfie','Fent-se una foto','Foto!',
  cara(L('M32,54q7,-6 14,0',TINTA,3)+L('M55,54q6,-5 12,0',TINTA,3.2)+P('M36,66h28c0,8 -6,12 -14,12s-14,-4 -14,-12Z',TINTA))
  +'<rect x="70" y="56" width="20" height="30" rx="4" fill="#2A2E35" stroke="'+FE+'" stroke-width="2"/>'+C(80,64,3,BLAU)
  +G('e-flash',C(80,64,14,'#fff','opacity=".9"')),'flash')
em('Cares','idea','Amb una idea','Quina idea!',
  G('e-bota',cara(C(39,54,3.6,TINTA)+C(61,54,3.6,TINTA)+L('M30,44q8,-6 16,-2M54,42q8,-4 16,2',TINTA,2.6)+P('M38,66h24c0,8 -5,12 -12,12s-12,-4 -12,-12Z',TINTA)))
  +G('e-bombeta',C(84,14,10,AC)+f'<rect x="80" y="23" width="8" height="6" rx="1.5" fill="{FE}"/>'+L('M84,0v-4M70,6l-4,-3M98,6l4,-3',AC,2.4)),'bombeta')
em('Cares','senyor','Molt senyor','Molt senyor',
  G('e-inclina',cara(C(39,54,3.4,TINTA)+C(61,54,8,'none',f'stroke="{A}" stroke-width="2.6"')+C(61,54,3.4,TINTA)+L('M69,58l6,16',A,1.6)
   +P('M30,68c6,-6 14,-6 20,-2c6,-4 14,-4 20,2c-6,2 -12,2 -20,-1c-8,3 -14,3 -20,1Z','#3A2410')+L('M42,76h16',TINTA,2.6))),'senyor')
em('Cares','musica','Escoltant música','Quin temazo',
  G('e-balla',cara(L('M33,56q6,3 12,0M55,56q6,3 12,0',TINTA,3)+L('M38,70q12,8 24,0',TINTA,3.4),
   extra=P('M16,58c0,-26 14,-42 34,-42s34,16 34,42','none',f'stroke="{FEF}" stroke-width="5"')+f'<rect x="10" y="52" width="12" height="20" rx="5" fill="{R}"/><rect x="78" y="52" width="12" height="20" rx="5" fill="{R}"/>'))
  +G('e-nota',nota(8,30,AC),'style="--i:0"')+G('e-nota',nota(92,26,A),'style="--i:1"'),'musica')
em('Cares','ressaca','Amb ressaca','Quina ressaca',
  G('e-trontolla',cara(L('M33,56l12,0M55,56l12,0',TINTA,3)+L('M38,74q6,-4 12,0t12,0',TINTA,3),'#E2C470'))
  +G('e-gel',P('M24,24h28c4,0 6,4 4,8l-6,10c-2,3 -6,4 -10,4h-6c-6,0 -10,-4 -10,-10v-4c0,-4 2,-8 0,-8Z','#8CC4E8')+P('M30,20h16l-2,6h-12Z','#5AB8E8')),'ressaca')
em('Cares','aimare','Mà a la cara','Ai, mare!',
  G('e-sacseja-lent',cara(C(61,55,3.4,TINTA)+L('M40,74q10,-5 20,0',TINTA,3.2))+ma(38,58,10,e=1.1)),'aimare')
em('Cares','mentida','Mentider','Mentida!',
  cara(C(39,54,3.4,TINTA)+C(61,54,3.4,TINTA)+L('M40,76q10,4 20,0',TINTA,3))+G('e-nas',f'<rect x="48" y="58" width="12" height="8" rx="4" fill="{AF}"/>','style="transform-box:view-box;transform-origin:48px 62px"'),'nas')
em('Cares','revés','Del revés','Tot del revés',
  G('e-capgira',cara(L('M32,54q7,-6 14,0M54,54q7,-6 14,0',TINTA,3)+L('M36,68q14,10 28,0',TINTA,3.4))),'capgira')
em('Cares','vinga','Impacient','Vinga, va!',
  G('e-tamboreja',cara(L('M32,50l12,4M68,50l-12,4',TINTA,3)+C(40,57,3,TINTA)+C(60,57,3,TINTA)+L('M38,74h24',TINTA,3.4)))
  +G('e-agulla-rapida',C(84,20,10,'#E9E2D0')+L('M84,20V13',TINTA,2)+L('M84,20h5',R,2),'style="transform-box:view-box;transform-origin:84px 20px"'),'vinga')
em('Cares','atope','A tope','A tope!',
  G('e-crida',cara(L('M31,48l12,4M69,48l-12,4',TINTA,3)+ulls()+P('M36,66h28v4c0,6 -6,10 -14,10s-14,-4 -14,-10Z',TINTA)+L('M38,68h24',BL,2.4)))
  +G('e-biceps',P('M70,84c4,-14 8,-20 14,-22c6,-2 10,2 8,8c-2,4 -6,4 -8,2c-2,6 -4,10 -6,14Z',A,f'stroke="{TINTA}" stroke-width="2"'),'style="transform-box:view-box;transform-origin:72px 84px"'),'atope')
em('Cares','calla','Rient tapant-se la boca','Calla, calla!',
  G('e-riu',cara(L('M32,52l7,-4l7,4M54,52l7,-4l7,4',TINTA,3)+C(28,62,5,R,'opacity=".3"')+C(72,62,5,R,'opacity=".3"'))+ma(50,78,0,e=1)),'calla')

# ---- Ànims
em('Ànims','fanal','Fanal','Ets un far',
  HALO(50,58,40,'e-halo')+C(50,13,4,'none','stroke="'+FE+'" stroke-width="3"')+L('M50,17V24',FE,3)
  +P('M26,40L44,24h12L74,40Z',FE)+f'<rect x="22" y="39" width="56" height="5" rx="2" fill="{FE}"/>'
  +P('M30,44h40l-8,32h-24Z',A,'class="e-vidre"')+L('M30,44h40l-8,32h-24Z',FE,3.5)
  +P('M36,76h28l-4,6h-20Z',FE)+C(50,86,2.6,FE),'flama')
cos=lambda x,y,c,s=1: (C(x,y-14*s,6*s,PELL)+barretina(x,y-14*s,6*s)+P(f'M{x-8*s},{y-6*s}h{16*s}l{3*s},{24*s}h{-22*s}Z',c)+f'<rect x="{x-8.5*s}" y="{y+2*s}" width="{17*s}" height="{3.4*s}" fill="#14171C"/>')
em('Ànims','castell','Castell','Força!',
  cos(26,74,R)+cos(50,74,R)+cos(74,74,R)+cos(38,50,R,.9)+cos(62,50,R,.9)
  +G('e-enxaneta',C(50,18,4.4,PELL)+barretina(50,18,4.4)+P('M45,23h10l2,14h-14Z',A)+L('M55,25l7,-9',A,3.2,'class="e-braç"')),'enxaneta')
em('Ànims','cim','Muntanya','Al cim!',
  P('M8,90C10,62 16,48 22,48S30,64 32,58C33,40 38,32 44,32S52,50 54,42C56,24 62,18 68,22S74,46 78,44C82,40 86,52 92,90Z',FEF)
  +P('M22,48C26,48 28,58 30,62M44,32C48,32 50,44 52,48M68,22C72,24 72,40 76,46','none','stroke="#6A7484" stroke-width="2" stroke-linecap="round"')
  +L('M68,22V6',BL,2.4)+P('M68,6h16l-4,4.5l4,4.5h-16Z',A,'class="e-bandera"'),'bandera')
em('Ànims','bota','Bota','Bona caminada',
  G('e-pas',P('M30,18h26v44c10,2 26,6 32,14c3,4 2,10 -4,10H24c-4,0 -6,-3 -6,-7V30c0,-7 5,-12 12,-12Z',MARRO)
  +P('M18,80h70v6c0,3 -2,5 -5,5H23c-3,0 -5,-2 -5,-5Z','#2A2E35')+L('M34,30h18M34,40h18M34,50h18',A,3)
  +P('M30,18h26v8H30Z',MARROC)),'pas')
em('Ànims','foc','Foc','En ratxa',
  G('e-flama',P('M50,10C58,28 76,36 76,60c0,16 -12,28 -26,28S24,76 24,60c0,-12 8,-18 12,-28c4,8 8,10 10,10C46,30 44,20 50,10Z',R)
  +P('M50,40c6,10 14,14 14,26c0,9 -6,16 -14,16s-14,-7 -14,-16c0,-6 4,-10 6,-14c2,4 4,5 5,5C47,52 46,46 50,40Z',A)
  +P('M50,62c3,5 6,7 6,12c0,4 -3,7 -6,7s-6,-3 -6,-7c0,-4 3,-7 6,-12Z',AC)),'flama2')

# ---- Celebrar
dansaire=lambda x,c: C(x,34,6,PELL)+barretina(x,34,6)+P(f'M{x-8},42h16l4,30h-24Z',c)+L(f'M{x-7},45l-8,-14M{x+7},45l8,-14',c,4)
em('Celebrar','sardana','Sardana','Ballem!',
  ''.join(G('e-bot',dansaire(x,c),f'style="--i:{i}"') for i,(x,c) in enumerate([(20,AF),(50,A),(80,AF)]))
  +L('M8,88h84',FEF,3),'bot')
em('Celebrar','traca','Traca','Quina festa!',
  ''.join(G('e-espurna',L(f'M{50+10*math.cos(math.radians(a))},{48+10*math.sin(math.radians(a))}L{50+36*math.cos(math.radians(a))},{48+36*math.sin(math.radians(a))}',[A,R,AC][i%3],4),f'style="--i:{i}"') for i,a in enumerate(range(0,360,30)))
  +C(50,48,7,AC,'class="e-nucli"'),'traca')
em('Fora','porro','Porró','Salut!',
  G('e-porro',P('M38,52c-12,4 -18,14 -16,24c2,10 14,14 28,14s26,-4 28,-14c2,-10 -4,-20 -16,-24Z','#CFE6F2','opacity=".35"')
  +P('M25,74c1,8 12,12 25,12s24,-4 25,-12c-6,2 -14,4 -25,4s-19,-2 -25,-4Z',R)
  +P('M42,52V18c0,-3 2,-5 4,-5h8c2,0 4,2 4,5v34','#CFE6F2','opacity=".35"')+L('M38,52c-12,4 -18,14 -16,24c2,10 14,14 28,14s26,-4 28,-14c2,-10 -4,-20 -16,-24V18c0,-3 -2,-5 -4,-5h-8c-2,0 -4,2 -4,5V52',BL,2.4)
  +L('M66,60L88,36',BL,3.4)+C(89,35,2.6,BL))+P('M90,40c4,8 4,14 2,22','none',f'class="e-raig" stroke="{R}" stroke-width="2.6" stroke-linecap="round" stroke-dasharray="4 4"'),'raig')
em('Celebrar','brindis','Brindis','Brindem!',
  G('e-esq',P('M30,58c-9,3 -14,11 -12,19c2,8 11,11 22,11s19,-3 20,-11c1,-6 -2,-12 -8,-16V34c0,-2 -1,-4 -3,-4h-6c-2,0 -3,2 -3,4Z','#CFE6F2','opacity=".35"')
   +P('M19,76c1,7 9,10 21,10s19,-3 20,-10c-5,2 -11,3 -20,3s-16,-1 -21,-3Z',R)
   +L('M30,58c-9,3 -14,11 -12,19c2,8 11,11 22,11s19,-3 20,-11c1,-6 -2,-12 -8,-16V34c0,-2 -1,-4 -3,-4h-6c-2,0 -3,2 -3,4V58',BL,2.2))
  +G('e-dre',P('M66,90h16c2,0 3,-1 3,-3V52c0,-6 -4,-10 -5,-14V22h-8v16c-1,4 -5,8 -5,14v35c0,2 1,3 3,3Z','#3E6B3A')
   +f'<rect x="72" y="16" width="8" height="7" rx="1.5" fill="{A}"/>'+f'<rect x="63" y="58" width="22" height="18" rx="2" fill="{BL}"/>'
   +P('M74,60l2.2,4.6l5,.7l-3.6,3.5l.9,5l-4.5,-2.4l-4.5,2.4l.9,-5l-3.6,-3.5l5,-.7Z',R))
  +G('e-xoc',''.join(L(f'M{56+9*math.cos(math.radians(a))},{22+9*math.sin(math.radians(a))}L{56+15*math.cos(math.radians(a))},{22+15*math.sin(math.radians(a))}',AC,3) for a in (200,240,270,300,340))),'brindis')
em('Celebrar','gegant','Gegant','Ets un gegant!',
  G('e-gegant',P('M28,94L36,44h28l8,50Z','#2F5FA8')+P('M28,94L36,44h28l8,50Z','none','stroke="#21467F" stroke-width="1"')
  +L('M38,48l-14,20M62,48l14,20','#2F5FA8',6)+C(24,69,3.6,PELL)+C(76,69,3.6,PELL)
  +P('M36,44h28l-4,8h-20Z',BL)+C(50,32,11,PELL)+P('M39,30a11,11 0 0 1 22,0c-2,-4 -6,-6 -11,-6s-9,2 -11,6Z','#3B2A1E')
  +barretina(50,32,11)+C(46,33,1.3,'#14171C')+C(54,33,1.3,'#14171C')+L('M46,38q4,3 8,0','#8A3A2A',1.4)),'gegant')
em('Celebrar','cor','Cor','Amb tot el cor',
  G('e-cor',P('M50,88C24,70 10,56 10,38c0,-12 9,-22 21,-22c8,0 15,5 19,12c4,-7 11,-12 19,-12c12,0 21,10 21,22c0,18 -14,32 -40,50Z',R)
  +P('M28,26c-6,1 -10,6 -10,12',"none",f'stroke="{AC}" stroke-width="4" stroke-linecap="round" opacity=".7"')),'cor')

# ---- Comentar
em('Comentar','lloc','Lloc','Quin lloc!',
  f'<g transform="translate(6 20) scale(.88)">{P(LOW,"#2A3344")}</g>'+G('e-pin',P('M58,52c0,0 -16,-16 -16,-28a16,16 0 0 1 32,0c0,12 -16,28 -16,28Z',R)+C(58,24,6,BL))
  +f'<ellipse cx="58" cy="56" rx="6" ry="2" fill="#000" opacity=".4"/>','pin')
em('Comentar','bolet','Rovelló','Quina troballa!',
  G('e-bolet',P('M42,58c-2,10 -2,22 0,30h16c2,-8 2,-20 0,-30Z','#F1DCC0')
  +P('M12,58c0,-22 18,-38 38,-38s38,16 38,38c0,3 -2,4 -5,4H17c-3,0 -5,-1 -5,-4Z','#E07A2C')
  +P('M24,50c4,-12 14,-20 26,-20M76,50c-3,-9 -10,-15 -18,-18','none','stroke="#B85A1A" stroke-width="2.6" stroke-linecap="round"'))
  +P('M20,90c10,-6 20,-6 30,0','none',f'stroke="{VERDF}" stroke-width="3" stroke-linecap="round"')+P('M54,90c8,-5 16,-5 24,0','none',f'stroke="{VERD}" stroke-width="3" stroke-linecap="round"'),'pop')
em('Comentar','prismatics','Prismàtics','T\'estic seguint',
  G('e-mira',P('M18,40h24v40c0,4 -3,7 -7,7h-10c-4,0 -7,-3 -7,-7Z','#2A2E35')+P('M58,40h24v40c0,4 -3,7 -7,7h-10c-4,0 -7,-3 -7,-7Z','#2A2E35')
  +f'<rect x="40" y="46" width="20" height="14" rx="3" fill="{FEF}"/>'+P('M22,26h16v14H22ZM62,26h16v14H62Z',FE)
  +C(30,78,7,BLAU)+C(70,78,7,BLAU)+C(28,76,2.2,BL)+C(68,76,2.2,BL)),'mira')
em('Comentar','rellotge','Rellotge','Ja era hora!',
  C(50,52,38,'#E9E2D0')+C(50,52,38,'none',f'stroke="{FE}" stroke-width="5"')
  +''.join(L(f'M{50+30*math.cos(math.radians(a))},{52+30*math.sin(math.radians(a))}L{50+34*math.cos(math.radians(a))},{52+34*math.sin(math.radians(a))}','#3A4558',3) for a in range(0,360,30))
  +L('M50,52V32','#14171C',4.5)+G('e-minutera',L('M50,52H72',R,3))+C(50,52,3.5,'#14171C'),'agulla')
em('Comentar','caganer','Caganer','Ups!',
  G('e-caganer',P('M34,54c0,-10 8,-16 16,-16s16,6 16,16v10H34Z','#3B6E8A')+P('M30,64h40l-4,16H34Z','#2A2E35')
  +C(50,30,11,PELL)+P('M38,26c0,-10 6,-16 14,-16c6,0 12,4 16,12c-4,0 -6,2 -8,4c-4,-2 -12,-2 -22,0Z',R)
  +C(46,31,1.4,'#14171C')+C(54,31,1.4,'#14171C')+L('M46,36q4,2 8,0','#8A3A2A',1.4))
  +P('M62,88c0,-4 3,-6 6,-6c1,-3 3,-4 5,-4c3,0 5,2 5,5c3,0 5,2 5,5Z',MARRO,'class="e-pila"'),'caganer')

# ---- Del dia
em('Del dia','sol','Sol','Bon dia',
  G('e-sol',C(50,62,18,A)+''.join(L(f'M{50+24*math.cos(math.radians(a))},{62+24*math.sin(math.radians(a))}L{50+32*math.cos(math.radians(a))},{62+32*math.sin(math.radians(a))}',A,4) for a in range(180,361,30)))
  +P('M0,72C20,62 34,64 50,70S82,62 100,68V100H0Z','#2A3344')+P('M0,82C24,74 44,78 60,84S88,78 100,80V100H0Z',FEF),'sol')
em('Del dia','lluna','Lluna','Bona nit',
  P('M60,18a32,32 0 1 0 22,56a26,26 0 1 1 -22,-56Z',AC)
  +''.join(G('e-estel',P(f'M{x},{y-5}L{x+1.5},{y-1.5}L{x+5},{y}L{x+1.5},{y+1.5}L{x},{y+5}L{x-1.5},{y+1.5}L{x-5},{y}L{x-1.5},{y-1.5}Z',A),f'style="--i:{i}"') for i,(x,y) in enumerate([(78,22),(86,44),(68,40)])),'estels')
em('Del dia','pa','Pa amb tomàquet','Bon profit',
  P('M14,44c0,-14 14,-22 36,-22s36,8 36,22c0,4 -2,6 -4,8v28c0,4 -3,6 -6,6H24c-3,0 -6,-2 -6,-6V52c-2,-2 -4,-4 -4,-8Z','#C98A4B')
  +P('M22,46c0,-10 12,-16 28,-16s28,6 28,16c0,3 -2,5 -4,6v24c0,3 -2,4 -4,4H30c-2,0 -4,-1 -4,-4V52c-2,-1 -4,-3 -4,-6Z','#F0D8A8')
  +P('M30,48c6,-6 14,2 20,-2s14,-4 18,2c3,5 -2,10 1,16c2,5 -4,9 -10,6c-6,-3 -12,4 -18,0c-6,-4 -12,2 -14,-4c-2,-6 4,-9 3,-18Z',R,'opacity=".85"')
  +G('e-oli',P('M74,14c3,5 5,8 5,11a5,5 0 0 1 -10,0c0,-3 2,-6 5,-11Z','#C8B53A')),'oli')
em('Del dia','calcot','Calçot','Calçotada!',
  P('M18,78h64c0,10 -14,16 -32,16s-32,-6 -32,-16Z','#7A3E22')+P('M22,78c2,-4 14,-6 28,-6s26,2 28,6Z','#D85A2A')
  +G('e-calcot',L('M70,10L44,72',VERD,7)+L('M64,24L46,66',BL,5)+L('M44,72L41,79','#2A2E35',7)+L('M70,10L76,4M70,10L66,2',VERDF,3)),'calcot')
em('Del dia','barretina','Barretina','Molt nostre',
  G('e-barretina',P('M20,76c0,-26 14,-50 40,-56c8,-2 16,4 14,12c-2,8 -10,8 -12,16c-2,10 10,18 10,28Z',R)
  +P('M20,72h54v10c0,3 -2,5 -5,5H25c-3,0 -5,-2 -5,-5Z',RF)+P('M58,20c6,-2 14,0 16,6',"none",f'stroke="{AC}" stroke-width="3" stroke-linecap="round" opacity=".5"')),'barretina')

# ---- Festes
em('Festes','santjordi','Rosa i llibre','Feliç Sant Jordi',
  P('M14,70l40,-10l34,10l-40,12Z','#8E2A2A')+P('M14,70v8l34,12v-8Z','#6E1E1E')+P('M48,82v8l40,-12v-8Z','#EDE3CF')
  +G('e-rosa',L('M50,72C50,58 54,46 58,34',VERDF,3)+P('M54,54c-8,-2 -12,-8 -10,-12c6,0 10,4 10,12Z',VERD)
   +C(58,28,10,R)+C(52,24,6,'#B81E24')+C(64,24,6,'#B81E24')+C(58,20,6,'#E8343A')+L('M54,28q4,4 8,0',RF,2)),'rosa')
em('Festes','drac','Drac','Quin drac!',
  G('e-drac',P('M16,62c0,-20 14,-34 34,-34c14,0 24,8 30,18l14,4c2,6 0,12 -6,14l-16,2c-4,10 -14,16 -26,16c-18,0 -30,-8 -30,-20Z',VERD)
   +P('M24,36l4,-12l6,10l4,-12l6,10l4,-10l4,12Z',VERDF)+C(56,46,5,BL)+C(57,46,2.6,'#14171C')
   +L('M80,62l12,-2',VERDF,2.4)+C(88,52,1.8,'#14171C')+P('M30,70c6,4 14,4 20,0',"none",f'stroke="{VERDF}" stroke-width="2.4"'))
  +G('e-foc-drac',P('M96,62c-6,-6 -2,-12 4,-12c-2,4 0,6 2,6c0,-4 4,-6 6,-6c-2,4 2,8 -2,12Z',A,'transform="translate(-8 4)"')),'drac')
em('Festes','tio','Tió','Caga tió!',
  G('e-tio',P('M22,50h50c8,0 12,8 12,16s-4,16 -12,16H22Z',MARRO)+C(22,66,16,MARROC)+C(22,66,10,'#D9B48A','opacity=".6"')
   +C(17,62,2,'#14171C')+C(27,62,2,'#14171C')+L('M16,70q6,5 12,0','#14171C',2)
   +L('M34,82v10M64,82v10',MARRO,4)+L('M40,56q10,-3 20,0M50,72q10,-3 20,0','#6A4426',2)
   +P('M8,52C6,38 16,30 26,32C34,33 40,38 38,46C37,50 33,50 31,48Z',R)+f'<rect x="7" y="49" width="30" height="5" rx="2" fill="{RF}"/>'
   +P('M70,50c0,-6 -6,-10 -10,-8c4,2 4,6 4,8Z',VERD)),'tio')
em('Festes','mona','Mona','Bona Pasqua',
  P('M14,64h72v16c0,4 -4,8 -8,8H22c-4,0 -8,-4 -8,-8Z','#C98A4B')+P('M14,64c0,-6 16,-10 36,-10s36,4 36,10s-16,8 -36,8s-36,-2 -36,-8Z','#F5D06A')
  +G('e-ou','<ellipse cx="50" cy="44" rx="13" ry="17" fill="#5A3420"/>'+P('M40,38q10,-6 20,0','none',f'stroke="{A}" stroke-width="2.4"')+C(44,50,2,R)+C(56,50,2,BLAU))
  +P('M22,58l-6,-14l10,10ZM78,58l6,-14l-10,10Z','#F07AA0'),'ou')
em('Festes','revetlla','Revetlla','Bona revetlla',
  L('M24,88L76,72M24,72L76,88',MARRO,7)
  +G('e-flama',P('M50,14C58,30 74,38 74,58c0,12 -10,20 -24,20S26,70 26,58c0,-10 6,-16 10,-24c4,6 6,8 8,8C44,30 44,22 50,14Z',R)
   +P('M50,40c6,8 12,12 12,22c0,8 -6,12 -12,12s-12,-4 -12,-12c0,-6 4,-8 6,-12c2,3 3,4 4,4C48,50 47,46 50,40Z',A))
  +''.join(G('e-espurna-puja',C(x,30,1.8,AC),f'style="--i:{i}"') for i,x in enumerate((28,40,62,74))),'flama')
em('Festes','correfoc','Correfoc','Correfoc!',
  L('M50,94V40',MARRO,5)+L('M40,44V30M50,40V24M60,44V30M40,44h20',FEF,4)
  +G('e-roda',''.join(L(f'M50,24L{50+22*math.cos(math.radians(a))},{24+22*math.sin(math.radians(a))}',[A,AC,R][i%3],3) for i,a in enumerate(range(0,360,30))),'style="transform-box:view-box;transform-origin:50px 24px"')
  +C(50,24,4,AC),'roda')
em('Festes','bastons','Ball de bastons','Toc, toc, toc!',
  G('e-baston-esq',L('M20,90L60,20',MARROC,6)+f'<rect x="54" y="16" width="8" height="8" rx="2" fill="{R}" transform="rotate(30 58 20)"/>')
  +G('e-baston-dre',L('M80,90L40,20',MARROC,6)+f'<rect x="38" y="16" width="8" height="8" rx="2" fill="{R}" transform="rotate(-30 42 20)"/>')
  +G('e-clap2',L('M50,8v-6M40,10l-4,-5M60,10l4,-5',AC,2.6)),'bastons')
em('Festes','gralla','Gralla','Toca la gralla!',
  G('e-gralla',P('M66,16l6,6L40,70l-6,-6Z','#7A4A28')+P('M34,64l6,6l-14,18c-4,4 -12,-4 -8,-8Z','#5A3420')
   +L('M60,26l3,3M54,34l3,3M48,42l3,3M42,50l3,3',AC,2.4)+L('M70,14l6,-6',A,3))
  +G('e-nota',nota(80,46,AC),'style="--i:0"')+G('e-nota',nota(88,30,A),'style="--i:1"'),'gralla')
em('Festes','oucombal','L\'ou com balla','Quin ou més ballador!',
  P('M18,70h64l-8,16H26Z',FE)+P('M18,70c0,-4 14,-6 32,-6s32,2 32,6Z','#A9C8DA')
  +C(24,66,5,R)+C(36,62,4,'#F07AA0')+C(64,62,4,A)+C(76,66,5,R)
  +L('M50,64V40',BLAU,4,'class="e-raig-aigua"')+G('e-ou-balla','<ellipse cx="50" cy="32" rx="7" ry="9" fill="#F4EDE0" stroke="#C9B89A" stroke-width="1.4"/>'),'oucombal')
em('Festes','cava','Cava','Que salti el tap!',
  P('M40,94h20c3,0 5,-2 5,-5V56c0,-8 -6,-12 -7,-18V24h-14v14c-1,6 -7,10 -7,18v33c0,3 2,5 3,5Z','#2F4A2C')
  +f'<rect x="43" y="60" width="22" height="16" rx="2" fill="{A}"/>'+P('M43,24h14v8H43Z',A)
  +G('e-tap',f'<rect x="45" y="12" width="10" height="12" rx="3" fill="#C9A06A"/>')
  +''.join(G('e-bombolla',C(x,y,2.2,AC),f'style="--i:{i}"') for i,(x,y) in enumerate([(40,10),(58,6),(50,2)])),'cava')
# ---- Taula
em('Taula','crema','Crema catalana','Boníssim!',
  '<ellipse cx="50" cy="66" rx="40" ry="16" fill="#8A4A26"/>'+'<ellipse cx="50" cy="60" rx="36" ry="12" fill="#F3D27A"/>'
  +G('e-cremat','<ellipse cx="50" cy="60" rx="30" ry="9" fill="#C4782A" opacity=".85"/>')+L('M30,54l26,-10',MARRO,4)
  +G('e-brill2',L('M40,58l6,-2M58,62l6,-2',AC,2.4)),'cremat')
em('Taula','botifarra','Botifarra','Botifarra!',
  G('e-botifarra',P('M14,62C20,36 52,26 80,38c6,3 8,10 4,14C62,42 36,48 26,70c-3,6 -10,4 -12,-8Z','#B5684A')
   +L('M32,52l6,6M46,44l6,6M60,40l6,6','#6A3422',2.6)+C(84,40,2.6,'#E8D4C4')+C(15,64,2.6,'#E8D4C4'))
  +G('e-xiula',L('M30,26q4,-6 0,-12M46,22q4,-6 0,-12M62,22q4,-6 0,-12',GRIS,2.4)),'botifarra')
em('Taula','caragol','Caragol','A poc a poc',
  G('e-caragol',P('M10,80c0,-6 6,-8 14,-8h44c10,0 18,-14 22,-14c4,0 6,6 2,10c-6,6 -8,18 -20,18H16c-4,0 -6,-2 -6,-6Z','#C9B08A')
   +L('M84,58l-2,-12M90,60l2,-12','#C9B08A',2.4)+C(82,45,2.4,'#14171C')+C(92,47,2.4,'#14171C')
   +C(42,54,22,'#9A5A34')+L('M42,54m-4,0a4,4 0 1,1 8,0a10,10 0 1,1 -18,0a16,16 0 1,1 30,0','#5A3420',3)),'caragol')
em('Taula','castanyes','Castanyes','Bona castanyada',
  P('M24,40h52L56,92h-12Z','#EDE3CF')+P('M24,40h52l-4,8H28Z','#C9B89A')
  +C(38,36,9,'#7A3E22')+C(56,34,10,'#6A3420')+C(48,28,8,'#8A4A26')+P('M50,22c4,0 6,2 6,4','none','stroke="#C9A06A" stroke-width="2" stroke-linecap="round"')
  +G('e-vapor-puja',L('M36,20q4,-6 0,-12M54,18q4,-6 0,-12',GRIS,2.4)),'vapor')
em('Taula','tortell','Tortell de Reis','Qui té la fava?',
  '<ellipse cx="50" cy="70" rx="40" ry="18" fill="#C98A4B"/>'+'<ellipse cx="50" cy="66" rx="40" ry="16" fill="#E0A85C"/>'+'<ellipse cx="50" cy="66" rx="18" ry="7" fill="#0B0E13"/>'
  +''.join(C(x,y,3.4,c) for x,y,c in [(20,62,R),(32,56,VERD),(68,56,R),(80,62,A),(30,76,A),(70,76,VERD),(50,80,R)])
  +G('e-corona',P('M34,40l4,-20l8,10l4,-14l4,14l8,-10l4,20Z',A)+C(50,16,2.4,R)),'corona')
# ---- Terra
em('Terra','cala','Cala','A la platja!',
  P('M0,40C18,36 26,48 30,62H0Z',FEF)+P('M100,30C82,30 72,44 70,62H100Z',FEF)
  +f'<rect x="0" y="60" width="100" height="40" fill="#2E6E9A"/>'
  +G('e-onada',L('M-10,70q10,-5 20,0t20,0t20,0t20,0t20,0t20,0',BLAU,3)+L('M-10,82q10,-5 20,0t20,0t20,0t20,0t20,0t20,0','#8CC4E8',3))
  +L('M84,30V16',MARRO,3)+C(84,14,8,VERDF)+C(78,18,6,VERDF)+C(90,18,6,VERDF)+C(70,20,6,A,'opacity=".9"'),'onada')
em('Terra','llaut','Llaüt','Bona pesca',
  f'<rect x="0" y="76" width="100" height="24" fill="#2E6E9A"/>'
  +G('e-balanceig',P('M14,70h72c-4,10 -14,14 -34,14S18,80 14,70Z',BL)+P('M14,70h72v-3H14Z',R)
   +L('M50,68V14',MARRO,3)+P('M50,16L20,62h30Z',"#EDE3CF")+P('M18,64L74,10',"none",f'stroke="{MARRO}" stroke-width="2.4"'))
  +G('e-onada',L('M-10,80q10,-4 20,0t20,0t20,0t20,0t20,0t20,0','#8CC4E8',2.6)),'balanceig')
em('Terra','ruc','Ruc català','Tossut com un ruc',
  G('e-orella-esq',P('M30,40c-8,-16 -8,-30 -2,-34c6,4 10,18 10,30Z','#4A3A30'),'style="transform-box:view-box;transform-origin:34px 40px"')
  +G('e-orella-dre',P('M62,38c4,-16 12,-28 18,-30c4,6 -2,20 -10,32Z','#4A3A30'),'style="transform-box:view-box;transform-origin:64px 38px"')
  +P('M28,40c0,-6 8,-10 20,-10s20,4 20,10v24c0,12 -6,24 -20,24s-20,-12 -20,-24Z','#4A3A30')
  +'<ellipse cx="48" cy="76" rx="16" ry="12" fill="#D8CBB8"/>'+C(42,76,2,'#14171C')+C(54,76,2,'#14171C')
  +C(38,52,3,BL)+C(58,52,3,BL)+C(38,52,1.6,'#14171C')+C(58,52,1.6,'#14171C'),'orella')
em('Terra','campana','Campana','Toquen a festa!',
  L('M30,14h40',MARRO,5)
  +G('e-campana',P('M38,18h24v6c10,4 14,20 16,40h-56c2,-20 6,-36 16,-40Z','#C99A3A')+f'<rect x="20" y="62" width="60" height="6" rx="3" fill="#A97E2A"/>'+C(50,74,5,'#7A5A1C'),'style="transform-box:view-box;transform-origin:50px 16px"')
  +G('e-so',L('M10,40q-4,8 0,16M90,40q4,8 0,16',AC,2.6)),'campana')
em('Terra','espardenya','Espardenya','A ballar sardanes',
  G('e-pas',P('M10,70c0,-10 10,-14 30,-14c20,0 40,-4 50,4c6,4 4,14 -4,16c-20,6 -50,6 -66,4c-6,-1 -10,-4 -10,-10Z','#EDE3CF')
   +P('M10,76c0,4 4,8 12,8h56c8,0 14,-4 12,-10c-10,6 -60,8 -80,2Z','#C9A06A')
   +L('M36,58C34,44 30,34 26,28M44,56C46,42 52,34 58,30',"#8A96AA",2.6)+L('M26,28C40,24 52,26 58,30',"#8A96AA",2.6)),'pas')

# ---- Festes (més)
em('Festes','capgros','Capgròs','Quin cap més gros!',
  G('e-balla',P('M38,74h24l6,22H32Z','#2F5FA8')+C(50,46,30,PELL)+C(40,44,4,'#14171C')+C(60,44,4,'#14171C')+P('M38,58q12,10 24,0','none','stroke="#8A3A2A" stroke-width="3" stroke-linecap="round"')
   +C(30,54,5,R,'opacity=".3"')+C(70,54,5,R,'opacity=".3"')
   +barr_svg(50,46,30)),'capgros')
em('Festes','fira','Fira','Anem a la fira!',
  L('M30,94L50,50L70,94',FE,4)+G('e-sinia',C(50,50,32,'none',f'stroke="{FE}" stroke-width="3"')
   +''.join(L(f'M50,50L{50+32*math.cos(math.radians(a))},{50+32*math.sin(math.radians(a))}',FEF,2) for a in range(0,360,45))
   +''.join(C(50+32*math.cos(math.radians(a)),50+32*math.sin(math.radians(a)),5,[R,A,BLAU,AC][i%4]) for i,a in enumerate(range(0,360,45))),'style="transform-box:view-box;transform-origin:50px 50px"')
  +C(50,50,4,A),'sinia')
em('Festes','pessebre','Pessebre','Bon Nadal',
  P('M14,60L50,34L86,60Z',MARRO)+f'<rect x="22" y="58" width="56" height="34" fill="#5A3A24"/>'+f'<rect x="38" y="68" width="24" height="24" fill="#2A1A10"/>'
  +P('M40,92c2,-8 18,-8 20,0Z',A,'opacity=".7"')
  +G('e-estrella-nadal',P('M50,4L54,14L64,14L56,20L59,30L50,24L41,30L44,20L36,14L46,14Z',AC)),'nadal')
em('Festes','carnestoltes','Carnestoltes','Visca el carnaval!',
  G('e-mascara',P('M10,40c10,-6 24,-6 40,4c16,-10 30,-10 40,-4c2,16 -6,30 -20,30c-8,0 -14,-6 -20,-12c-6,6 -12,12 -20,12c-14,0 -22,-14 -20,-30Z','#9A3AB8')
   +P('M24,48c4,-4 10,-4 14,0c-4,4 -10,4 -14,0ZM62,48c4,-4 10,-4 14,0c-4,4 -10,4 -14,0Z','#0B0E13')
   +P('M88,38c6,-12 4,-24 -2,-30c0,10 -4,18 -10,24Z',A)+P('M80,36c2,-12 -2,-22 -8,-26c2,10 0,18 -4,22Z',R))
  +L('M14,70L6,96',MARROC,3),'mascara')
em('Festes','estel','Estel','Amunt l\'estel!',
  G('e-estel-vola',P('M60,6L84,30L60,62L36,30Z',R)+P('M60,6L60,62M36,30L84,30','none',f'stroke="{RF}" stroke-width="2"')
   +P('M60,62C56,72 64,78 58,86C54,92 60,96 56,100','none',f'stroke="{AC}" stroke-width="2"')
   +P('M58,74l-6,-3l2,6ZM58,86l6,-3l-2,6Z',A))
  +L('M10,96C24,80 40,72 58,62',GRIS,1.4,'stroke-dasharray="3 3"'),'estel')
# ---- Taula (més)
em('Taula','fuet','Fuet','Un fuet i cap a la muntanya',
  G('e-fuet',P('M14,70C30,40 64,30 88,40c4,2 4,8 0,10C64,42 36,52 22,78c-3,5 -10,2 -8,-8Z','#6A3A2A')
   +''.join(C(x,y,1.6,'#F4EDE0') for x,y in [(30,58),(44,48),(58,44),(72,42),(24,66),(52,52),(66,48)])
   +P('M88,40c4,-4 8,-2 8,2',"none",'stroke="#C9B89A" stroke-width="2"')),'fuet')
em('Taula','xocolata','Xocolata desfeta','Xocolata amb melindros',
  P('M20,50h46v26c0,10 -8,16 -23,16S20,86 20,76Z','#EDE3CF')+P('M66,56c10,0 12,14 0,16','none','stroke="#EDE3CF" stroke-width="5"')
  +'<ellipse cx="43" cy="50" rx="23" ry="6" fill="#4A2A1A"/>'
  +G('e-melindro',f'<rect x="58" y="18" width="12" height="34" rx="6" fill="#E8C47A" transform="rotate(20 64 35)"/>')
  +G('e-vapor-puja',L('M32,40q4,-6 0,-12M44,38q4,-6 0,-12',GRIS,2.4)),'xocolata')
em('Taula','panellets','Panellets','Bons panellets!',
  '<ellipse cx="50" cy="80" rx="42" ry="10" fill="#EDE3CF"/>'
  +''.join(G('e-panellet',C(x,y,11,'#E8C47A')+''.join(C(x+dx,y+dy,1.8,'#B07A4A') for dx,dy in [(-4,-3),(3,-5),(5,2),(-2,4)]),f'style="--i:{i}"') for i,(x,y) in enumerate([(30,68),(52,66),(74,68),(41,52),(63,52)])),'panellet')
em('Taula','escudella','Escudella','Escudella i carn d\'olla',
  P('M14,46h72c0,26 -14,40 -36,40S14,72 14,46Z','#8A4A26')+'<ellipse cx="50" cy="46" rx="36" ry="9" fill="#D9A24A"/>'
  +C(36,46,5,'#B5684A')+P('M52,40c6,-2 12,0 14,4c-4,2 -10,2 -14,-4Z',VERD)+C(62,48,3,'#F4EDE0')
  +L('M10,46h8M82,46h8',FEF,5)+G('e-vapor-puja',L('M36,30q4,-6 0,-12M50,28q4,-6 0,-12M64,30q4,-6 0,-12',GRIS,2.4)),'vapor')
em('Taula','allioli','Allioli','Allioli!',
  P('M18,50h64c0,22 -14,36 -32,36S18,72 18,50Z','#C9B89A')+'<ellipse cx="50" cy="50" rx="32" ry="8" fill="#F6EFD6"/>'
  +G('e-ma-morter',L('M64,10L50,48',MARROC,8)+C(49,50,5,MARROC),'style="transform-box:view-box;transform-origin:64px 10px"')
  +C(26,86,5,'#F4EDE0')+C(32,90,4,'#F4EDE0'),'morter')
# ---- Ruta
em('Ruta','bruixola','Brúixola','Cap al nord!',
  C(50,50,40,'#C99A3A')+C(50,50,33,'#F4EDE0')+L('M50,20v6M50,74v6M20,50h6M74,50h6','#3A4558',3)
  +G('e-agulla-brui',P('M50,22L56,50L50,50Z',R)+P('M50,22L44,50L50,50Z','#B81E24')+P('M50,78L56,50L50,50Z',FE)+P('M50,78L44,50L50,50Z','#6A7484'),'style="transform-box:view-box;transform-origin:50px 50px"')
  +C(50,50,3.4,'#14171C'),'bruixola')
em('Ruta','motxilla','Motxilla','Ja tinc la motxilla feta',
  G('e-motxilla',P('M26,30c0,-10 10,-18 24,-18s24,8 24,18v54c0,4 -3,8 -8,8H34c-5,0 -8,-4 -8,-8Z','#4E7A3A')
   +P('M26,36c0,-8 10,-14 24,-14s24,6 24,14v6H26Z','#3E6230')+f'<rect x="34" y="58" width="32" height="22" rx="4" fill="#3E6230"/>'
   +L('M40,64h20',A,3)+P('M40,12c0,-6 4,-8 10,-8s10,2 10,8','none','stroke="#3E6230" stroke-width="4"')),'motxilla')
em('Ruta','rodalies','Tren','El tren torna a anar tard',
  G('e-tren',f'<rect x="8" y="34" width="70" height="40" rx="8" fill="#E8E2D6"/>'+f'<rect x="8" y="58" width="70" height="6" fill="{R}"/>'
   +''.join(f'<rect x="{x}" y="40" width="12" height="12" rx="2" fill="#2E6E9A"/>' for x in (16,34,52))+P('M78,38h8c4,0 6,4 6,8v24c0,2 -2,4 -4,4H78Z','#D8D0C0')
   +C(24,78,5,'#3A4558')+C(62,78,5,'#3A4558'))+L('M0,86h100',FE,3)
  +G('e-rellotget',C(84,20,10,'#E9E2D0')+L('M84,20V13',TINTA,2)+L('M84,20h5',R,2),'style="transform-box:view-box;transform-origin:84px 20px"'),'tren')
em('Ruta','cotxe','Cotxe','Carretera i manta',
  G('e-cotxe',P('M10,66c0,-6 4,-10 10,-10l8,-14c2,-4 6,-6 10,-6h22c4,0 8,2 10,6l8,14h4c6,0 8,4 8,10v6H10Z',R)
   +P('M32,40h12v14H26ZM50,40h12l6,14H50Z','#8CC4E8')+C(28,74,8,'#2A2E35')+C(72,74,8,'#2A2E35')+C(28,74,3,FE)+C(72,74,3,FE)+C(86,62,3,A))
  +G('e-velocitat',L('M0,52h6M0,62h4',GRIS,3))+L('M0,84h100','#3A4558',3),'cotxe')
em('Ruta','bici','Bicicleta','Anem pedalant',
  G('e-roda-bici',C(24,66,16,'none',f'stroke="#2A2E35" stroke-width="4"')+L('M24,52v28M10,66h28',FE,1.4),'style="transform-box:view-box;transform-origin:24px 66px"')
  +G('e-roda-bici',C(76,66,16,'none',f'stroke="#2A2E35" stroke-width="4"')+L('M76,52v28M62,66h28',FE,1.4),'style="transform-box:view-box;transform-origin:76px 66px"')
  +L('M24,66L42,40L66,40L76,66M42,40L50,66L66,40M50,66H24',R,3.6)+L('M40,34h8M66,40l-2,-10h8',"#2A2E35",3.4),'bici')
em('Ruta','tenda','Tenda','Acampada!',
  P('M10,84L50,20L90,84Z','#E07A2C')+P('M50,20L36,84h28Z','#B85A1A')+P('M50,40L42,84h16Z','#2A1A10')
  +L('M50,20V12',FE,2.4)+G('e-bandereta',P('M50,12h10l-3,3l3,3h-10Z',A))+L('M4,86h92',VERDF,3)
  +''.join(G('e-estel',estrella(x,y,4),f'style="--i:{i}"') for i,(x,y) in enumerate([(16,20),(84,16),(76,40)])),'tenda')
em('Ruta','camera','Càmera','Quina foto!',
  f'<rect x="10" y="30" width="80" height="54" rx="8" fill="#2A2E35"/>'+P('M34,30l6,-10h20l6,10Z','#2A2E35')
  +C(50,57,18,FE)+C(50,57,13,'#14171C')+C(50,57,8,'#2E6E9A')+C(46,53,3,BL,'opacity=".7"')+f'<rect x="72" y="36" width="10" height="6" rx="2" fill="{A}"/>'
  +G('e-flash',C(77,39,16,'#fff','opacity=".9"')),'flash')
em('Ruta','cartell','Cartell d\'entrada','Ja hi som!',
  L('M30,94V50M70,94V50',FE,4)+G('e-cartell',f'<rect x="12" y="22" width="76" height="34" rx="4" fill="#F4EDE0" stroke="{R}" stroke-width="4"/>'
   +f'<rect x="22" y="34" width="56" height="5" rx="2" fill="#2A2E35"/><rect x="30" y="43" width="40" height="4" rx="2" fill="#6A7484"/>','style="transform-box:view-box;transform-origin:50px 56px"'),'cartell')
em('Ruta','gr','Marca de GR','Pel bon camí',
  P('M6,94C10,60 20,40 40,34C60,30 80,40 94,94Z','#6A7484')+P('M40,34C60,30 80,40 94,94H70C66,60 56,44 40,34Z','#56606F')
  +G('e-marca',f'<rect x="36" y="52" width="30" height="8" fill="#F4EDE0"/><rect x="36" y="60" width="30" height="8" fill="{R}"/>'),'gr')
em('Ruta','esquis','Esquís','A esquiar!',
  P('M0,60L60,30L100,52V100H0Z','#E8F1F8')+P('M60,30L46,38L54,40L62,34L70,40L76,36Z','#FFFFFF')
  +G('e-esquiador',L('M10,82L80,62',R,4)+L('M14,90L84,70',R,4)+L('M40,72L36,46M58,66L56,40',FE,2.4)),'esquis')

# ---- Ànims (més)
em('Ànims','tupots','Puny','Tu pots!',
  G('e-puny',P('M30,50c0,-10 6,-14 12,-14h22c6,0 10,4 10,10v18c0,14 -8,24 -22,24c-12,0 -22,-8 -22,-20Z',A,f'stroke="{TINTA}" stroke-width="2.4"')
   +L('M42,36v14M53,36v14M64,38v12',TINTA,2)+P('M30,52c-6,0 -10,4 -10,10s6,8 12,4',A,f'stroke="{TINTA}" stroke-width="2.4"')
   +f'<rect x="34" y="84" width="34" height="14" rx="3" fill="{R}"/>'),'puny')
em('Ànims','endavant','Senyal','Endavant!',
  L('M50,96V54',FE,5)+G('e-fletxa',P('M14,22h52l20,16l-20,16H14Z','#3E8A4A')+P('M24,38h34m-10,-10l10,10l-10,10','none',f'stroke="{BL}" stroke-width="5" stroke-linecap="round" stroke-linejoin="round"')),'fletxa')
em('Ànims','gairebe','Barra de progrés','Gairebé hi ets!',
  f'<rect x="8" y="40" width="84" height="22" rx="11" fill="#2A3344"/>'+G('e-progres',f'<rect x="12" y="44" width="76" height="14" rx="7" fill="{A}"/>','style="transform-box:view-box;transform-origin:12px 51px"')
  +f'<text x="50" y="84" text-anchor="middle" font-family="Chakra Petch,sans-serif" font-weight="700" font-size="16" fill="{AC}">90%</text>','progres')
em('Ànims','bateria','Bateria','Recarregant piles',
  f'<rect x="16" y="28" width="62" height="44" rx="6" fill="none" stroke="{BL}" stroke-width="5"/><rect x="80" y="40" width="8" height="20" rx="2" fill="{BL}"/>'
  +G('e-carrega',f'<rect x="22" y="34" width="50" height="32" rx="3" fill="#5EC26A"/>','style="transform-box:view-box;transform-origin:22px 50px"')
  +P('M50,36l-10,16h8l-4,12l12,-18h-8l4,-10Z',AC),'bateria')
em('Ànims','pasapas','Petjades','Pas a pas',
  ''.join(G('e-petjada',P(f'M{x},{y}c-4,0 -6,-6 -6,-12s3,-10 6,-10s6,4 6,10s-2,12 -6,12Z',MARROC)+''.join(C(x+dx,y-25+dy,2,MARROC) for dx,dy in [(-4,0),(0,-2),(4,0)]),f'style="--i:{i}"') for i,(x,y) in enumerate([(24,92),(44,74),(62,90),(80,70)])),'petjades')
# ---- Celebrar (més)
em('Celebrar','trofeu','Trofeu','Campió!',
  G('e-trofeu',P('M30,16h40v18c0,14 -8,24 -20,24S30,48 30,34Z',A)+P('M30,22h-10c0,12 6,18 12,18M70,22h10c0,12 -6,18 -12,18','none',f'stroke="{A}" stroke-width="4"')
   +f'<rect x="44" y="56" width="12" height="14" fill="{AF}"/><rect x="32" y="70" width="36" height="10" rx="2" fill="{AF}"/><rect x="28" y="80" width="44" height="10" rx="2" fill="#5A3420"/>'
   +estrella(50,34,8))+G('e-espurneig',estrella(18,10,4)+estrella(84,12,4)),'trofeu')
em('Celebrar','medalla','Medalla','Medalla d\'or',
  P('M32,4h14l10,30h-14ZM68,4h-14l-10,30h14Z',R)+G('e-medalla',C(50,60,24,A)+C(50,60,18,'none',f'stroke="{AF}" stroke-width="3"')+estrella(50,60,10),'style="transform-box:view-box;transform-origin:50px 30px"'),'medalla')
em('Celebrar','pastis','Pastís','Per molts anys!',
  P('M18,58h64v28c0,4 -4,6 -8,6H26c-4,0 -8,-2 -8,-6Z','#F07AA0')+P('M18,58c0,-4 14,-8 32,-8s32,4 32,8c-6,6 -10,0 -16,4c-6,4 -10,-2 -16,2c-6,4 -10,-2 -16,2c-6,4 -10,-2 -16,-8Z','#F4EDE0')
  +''.join(f'<rect x="{x}" y="34" width="5" height="18" rx="2" fill="{c}"/>' for x,c in [(32,BLAU),(48,A),(64,VERD)])
  +''.join(G('e-flameta',P(f'M{x+2.5},{22}c3,4 4,6 4,8a4,4 0 0 1 -8,0c0,-2 1,-4 4,-8Z',A),f'style="--i:{i}"') for i,x in enumerate((32,48,64))),'pastis')
em('Celebrar','globus','Globus','Que bonic!',
  ''.join(G('e-globus',P(f'M{x},{y+20}c-10,0 -16,-10 -16,-20c0,-10 7,-18 16,-18s16,8 16,18c0,10 -6,20 -16,20Z',c)+P(f'M{x},{y+20}l-3,4h6Z',c)+L(f'M{x},{y+24}C{x-4},{y+40} {x+6},{y+50} 50,96',GRIS,1.2),f'style="--i:{i}"') for i,(x,y,c) in enumerate([(30,20,R),(70,22,BLAU),(50,6,A)])),'globus')
em('Celebrar','petard','Canó de confeti','Visca, visca!',
  G('e-cano',P('M12,92L30,52L52,74Z','#9A3AB8')+P('M30,52L52,74','none',f'stroke="{A}" stroke-width="4"'))
  +''.join(G('e-confeti-surt',f'<rect x="38" y="60" width="4" height="7" rx="1" fill="{c}"/>',f'style="--i:{i};--dx:{dx}px;--dy:{dy}px"') for i,(dx,dy,c) in enumerate([(10,-40,A),(30,-34,R),(44,-14,BLAU),(20,-50,AC),(40,-44,VERD),(0,-48,R)])),'petard')
# ---- Comentar (més)
em('Comentar','lupa','Lupa','A veure, a veure…',
  G('e-lupa',C(42,42,24,'#8CC4E8','opacity=".35"')+C(42,42,24,'none',f'stroke="{FE}" stroke-width="6"')+L('M60,60L86,86',MARRO,9)+L('M30,32a14,14 0 0 1 10,-6',"#fff",3)),'lupa')
em('Comentar','llibreta','Llibreta','M\'ho apunto',
  f'<rect x="20" y="10" width="54" height="78" rx="4" fill="#F4EDE0"/>'+''.join(L(f'M28,{y}h38','#C9B89A',2) for y in (30,42,54,66,78))+L('M20,10v78',R,4)
  +G('e-llapis',L('M70,70L90,20',A,7)+P('M66,80l4,-10l6,3Z','#E8C47A')+L('M89,22l2,-5',R,7)),'llapis')
em('Comentar','alerta','Alerta','Compte!',
  G('e-alerta',P('M50,10L92,86H8Z',A)+P('M50,18L84,80H16Z','none',f'stroke="{AF}" stroke-width="2"')+f'<rect x="46" y="36" width="8" height="26" rx="4" fill="#14171C"/>'+C(50,72,4.6,'#14171C')),'alerta')
em('Comentar','cobertura','Sense cobertura','Aquí no hi ha cobertura',
  ''.join(f'<rect x="{18+i*16}" y="{70-i*14}" width="10" height="{16+i*14}" rx="2" fill="{FEF}"/>' for i in range(4))
  +G('e-barra-cob',f'<rect x="18" y="70" width="10" height="16" rx="2" fill="{A}"/>')+G('e-creu',L('M66,16l20,20M86,16l-20,20',R,5)),'cobertura')
em('Comentar','onets','Mapa','On ets?',
  P('M10,24l26,-8l28,8l26,-8v62l-26,8l-28,-8l-26,8Z','#E9E2D0')+P('M36,16v62M64,24v62','none','stroke="#C9B89A" stroke-width="2"')
  +L('M18,60C30,50 40,66 52,50S76,40 82,30',R,2.6,'stroke-dasharray="4 4"')
  +G('e-interrogant',f'<text x="70" y="64" font-family="Chakra Petch,sans-serif" font-weight="700" font-size="34" fill="{A}" stroke="#14171C" stroke-width="1.4">?</text>'),'onets')
# ---- Del dia (més)
em('Del dia','cafe','Cafè','Primer, un cafè',
  P('M22,44h44v26c0,10 -8,18 -22,18S22,80 22,70Z','#F4EDE0')+P('M66,50c10,0 12,14 0,16','none','stroke="#F4EDE0" stroke-width="5"')
  +'<ellipse cx="44" cy="44" rx="22" ry="5" fill="#5A3420"/>'+'<ellipse cx="46" cy="90" rx="34" ry="5" fill="#C9B89A"/>'
  +G('e-vapor-puja',L('M36,34q4,-6 0,-12M48,32q4,-6 0,-12',GRIS,2.4)),'cafe')
em('Del dia','pluja','Pluja','Plou i fa sol',
  C(70,30,14,A)+G('e-nuvol',C(36,40,16,'#C9CED6')+C(54,34,18,'#C9CED6')+C(68,44,12,'#C9CED6')+f'<rect x="22" y="42" width="58" height="14" rx="7" fill="#C9CED6"/>')
  +''.join(G('e-gota-pluja',L(f'M{x},64l-3,8',BLAU,3),f'style="--i:{i}"') for i,x in enumerate((30,42,54,66))),'pluja')
em('Del dia','calor','Termòmetre','Quina calor!',
  f'<rect x="42" y="10" width="16" height="60" rx="8" fill="#F4EDE0"/>'+C(50,78,14,R)
  +G('e-mercuri',f'<rect x="46" y="20" width="8" height="58" rx="4" fill="{R}"/>','style="transform-box:view-box;transform-origin:50px 78px"')
  +''.join(L(f'M60,{y}h6',FE,2) for y in (20,30,40,50))+C(84,20,8,A),'calor')
em('Del dia','ninot','Ninot de neu','Neva!',
  C(50,72,22,'#F4F8FB')+C(50,38,15,'#F4F8FB')+C(44,36,2,'#14171C')+C(56,36,2,'#14171C')+P('M50,40l10,3l-10,2Z','#E07A2C')
  +C(50,64,2.2,'#14171C')+C(50,74,2.2,'#14171C')+L('M36,48q14,6 28,0',R,4)
  +P('M34,28C32,12 48,4 62,6C72,8 80,12 80,20C80,26 76,28 72,26C71,24 70,23 68,22Z',R)+f'<rect x="33" y="24" width="36" height="6" rx="2.5" fill="{RF}"/>'
  +''.join(G('e-floc',C(x,y,2.2,BL),f'style="--i:{i}"') for i,(x,y) in enumerate([(12,10),(88,30),(18,50),(86,62),(10,80)])),'ninot')
em('Del dia','vermut','Vermut','Fem el vermut?',
  P('M30,30h40l-6,40h-28Z','#C9E2EE','opacity=".4"')+P('M33,46h34l-3,24h-28Z','#8E2A2A')+L('M30,30h40l-6,40h-28Z',BL,2)
  +f'<rect x="28" y="72" width="44" height="6" rx="3" fill="{BL}"/>'+C(44,56,4,'#E8743A')+L('M44,56L60,18',MARRO,2)+C(58,24,4,VERD)
  +G('e-oliva',C(82,76,6,VERD)+C(82,76,2,R)),'vermut')
# ---- Terra (més)
em('Terra','verema','Raïm','Verema!',
  G('e-raim',L('M50,20V8',MARRO,3)+P('M52,14c8,-8 18,-6 22,0c-8,4 -14,4 -22,0Z',VERD)
   +''.join(C(x,y,8,'#6A2A5A') for x,y in [(38,30),(54,30),(30,44),(46,44),(62,44),(38,58),(54,58),(46,72)])
   +''.join(C(x-2,y-3,2,'#B06AA8') for x,y in [(38,30),(54,30),(46,44),(38,58)])),'raim')
em('Terra','olivera','Olivera','Oli d\'aquí',
  P('M44,94c2,-14 0,-26 -6,-34c6,2 10,6 12,10c2,-6 6,-10 12,-12c-6,8 -8,22 -6,36Z','#6A5A40')
  +G('e-copa',C(34,40,16,'#7A8A5A')+C(54,30,20,'#8A9A6A')+C(70,44,14,'#7A8A5A')+''.join(C(x,y,2.6,'#3A4A2A') for x,y in [(40,34),(60,24),(66,42),(48,46),(30,46)])),'olivera')
em('Terra','ovella','Ovella','Beee!',
  G('e-ovella',''.join(C(x,y,12,'#F4F0E6') for x,y in [(36,50),(52,44),(66,52),(48,60),(62,62),(34,62)])
   +'<ellipse cx="20" cy="50" rx="9" ry="11" fill="#3A3430"/>'+C(17,48,1.8,'#fff')+L('M36,72v18M50,74v18M60,74v18',"#3A3430",4)),'ovella')
em('Terra','bosc','Bosc','Al bosc!',
  ''.join(G('e-pi',P(f'M{x},{y}l{-14*s},{30*s}h{28*s}Z','#2E6A3E')+P(f'M{x},{y+14*s}l{-17*s},{30*s}h{34*s}Z','#3E7A4A')+f'<rect x="{x-3*s}" y="{y+44*s}" width="{6*s}" height="{10*s}" fill="{MARRO}"/>',f'style="--i:{i}"') for i,(x,y,s) in enumerate([(30,26,1),(70,30,.9),(50,12,1.2)])),'bosc')
em('Terra','cascada','Salt d\'aigua','Quin salt d\'aigua!',
  P('M0,10h40v50H0ZM62,10h38v50H62Z',FEF)+G('e-aigua-cau',''.join(L(f'M{x},10V70',c,4) for x,c in [(44,BLAU),(50,'#8CC4E8'),(56,BLAU)]))
  +'<ellipse cx="50" cy="80" rx="44" ry="12" fill="#2E6E9A"/>'+G('e-esquitx',C(36,72,3,BL)+C(64,72,3,BL)+C(50,68,3.4,BL)),'cascada')
# ---- Vehicles
em('Vehicles','tractor','Tractor','Pagès de cor',
  G('e-tractor',f'<rect x="40" y="34" width="30" height="30" rx="3" fill="#3E8A4A"/>'+P('M44,38h20v14H44Z','#8CC4E8')+f'<rect x="10" y="50" width="34" height="20" rx="3" fill="#3E8A4A"/>'
   +L('M18,50V36',FEF,4)+C(62,72,18,'#2A2E35')+C(62,72,7,A)+C(20,76,10,'#2A2E35')+C(20,76,4,A)),'tractor')
em('Vehicles','moto','Moto','Brrrum!',
  G('e-moto',P('M30,70c0,-14 10,-20 24,-20h12l8,20Z','#5AB8E8')+P('M60,50l10,-14h8','none',f'stroke="{FE}" stroke-width="4" stroke-linecap="round"')
   +C(24,74,12,'#2A2E35')+C(24,74,5,FE)+C(78,74,12,'#2A2E35')+C(78,74,5,FE)+P('M30,52c4,-6 14,-6 20,0Z','#2A2E35'))
  +G('e-fum2',C(6,70,5,'#C9CED6')+C(10,62,4,'#C9CED6')),'moto')
em('Vehicles','bus','Autobús','Agafem el bus',
  G('e-bus',f'<rect x="10" y="22" width="80" height="54" rx="8" fill="#E8B23A"/>'+''.join(f'<rect x="{x}" y="30" width="14" height="16" rx="2" fill="#2E6E9A"/>' for x in (16,34,52,70))
   +f'<rect x="10" y="56" width="80" height="6" fill="{R}"/>'+C(26,78,7,'#2A2E35')+C(74,78,7,'#2A2E35')+C(86,68,3,AC)),'bus')
em('Vehicles','camper','Furgoneta','Ruta en furgo',
  G('e-cotxe',P('M8,72V40c0,-8 6,-14 14,-14h46c8,0 14,6 18,14l6,14v18Z','#5EA8A0')+f'<rect x="8" y="54" width="84" height="18" fill="#F4EDE0"/>'
   +P('M20,32h16v14H20ZM42,32h16v14H42ZM66,32h8l8,14H66Z','#8CC4E8')+C(26,74,8,'#2A2E35')+C(74,74,8,'#2A2E35')+C(26,74,3,FE)+C(74,74,3,FE)
   +f'<rect x="20" y="16" width="44" height="8" rx="3" fill="{MARROC}"/>'),'camper')
em('Vehicles','avio','Avió','Me\'n vaig de viatge',
  G('e-avio',P('M8,52c0,-4 4,-6 10,-6h56c8,0 18,2 20,6c-2,4 -12,6 -20,6H18c-6,0 -10,-2 -10,-6Z','#F4EDE0')
   +P('M44,46L30,18h10l22,28ZM44,58L30,86h10l22,-28Z','#C9CED6')+P('M16,46L8,30h8l10,16Z',R)+''.join(C(x,52,2.2,'#2E6E9A') for x in (54,62,70,78))),'avio')
em('Vehicles','cremallera','Cremallera','Amunt per la cremallera',
  P('M0,96L100,30V100H0Z','#56606F')+L('M0,90L100,24',FE,3)
  +G('e-cremallera','<g transform="rotate(-33 50 58)">'+f'<rect x="30" y="40" width="40" height="22" rx="4" fill="{R}"/>'+''.join(f'<rect x="{x}" y="44" width="8" height="8" rx="1" fill="#8CC4E8"/>' for x in (34,46,58))+'</g>'),'cremallera')
em('Vehicles','telefèric','Telefèric','Quines vistes!',
  L('M0,20L100,44',FE,2.4)+G('e-teleferic',L('M50,32V44',FE,3)+f'<rect x="32" y="44" width="36" height="30" rx="6" fill="{R}"/>'+P('M36,50h28v12H36Z','#8CC4E8'))
  +P('M0,96L30,70L50,84L76,58L100,80V100H0Z',FEF),'teleferic')
em('Vehicles','globusaer','Globus aerostàtic','Fins al cel!',
  G('e-flota',P('M50,8c-22,0 -34,16 -34,32c0,18 20,30 26,40h16c6,-10 26,-22 26,-40c0,-16 -12,-32 -34,-32Z',R)
   +P('M50,8c-8,0 -12,16 -12,32c0,18 4,30 6,40h12c2,-10 6,-22 6,-40c0,-16 -4,-32 -12,-32Z',A)
   +L('M42,80l2,8M58,80l-2,8',FE,1.6)+f'<rect x="42" y="86" width="16" height="10" rx="2" fill="{MARRO}"/>'),'globusaer')
em('Vehicles','patinet','Patinet','Zum, zum',
  G('e-patinet',L('M70,20L78,74',FE,4)+L('M62,20h16',"#2A2E35",4)+f'<rect x="18" y="72" width="60" height="6" rx="3" fill="#2A2E35"/>'+C(22,82,7,'#2A2E35')+C(78,82,7,'#2A2E35')+C(22,82,2.4,A)+C(78,82,2.4,A))
  +G('e-velocitat',L('M0,60h10M2,70h8',GRIS,3)),'patinet')
em('Vehicles','veler','Veler','Vent a favor',
  f'<rect x="0" y="76" width="100" height="24" fill="#2E6E9A"/>'+G('e-balanceig',P('M18,70h64c-4,8 -14,12 -32,12S22,78 18,70Z',BL)
   +L('M50,70V10',MARRO,3)+P('M54,12L84,64H54Z','#F4EDE0')+P('M46,20L22,64h24Z',BLAU))+G('e-onada',L('M-10,80q10,-4 20,0t20,0t20,0t20,0t20,0t20,0','#8CC4E8',2.6)),'veler')

CSS='''
.em *{transform-box:fill-box;transform-origin:center}
.anim .e-halo{animation:ehalo 2.4s ease-in-out infinite}@keyframes ehalo{0%,100%{opacity:.7}50%{opacity:1}}
.anim .e-vidre{animation:evidre 3s steps(1) infinite}@keyframes evidre{0%,100%{fill:#F2B544}62%{fill:#FFE3A3}66%{fill:#F2B544}70%{fill:#FFE3A3}}
.anim .e-braç{transform-origin:0 100%;animation:ebrac .5s ease-in-out infinite alternate}@keyframes ebrac{from{transform:rotate(-18deg)}to{transform:rotate(16deg)}}
.anim .e-enxaneta{animation:eenx 2s ease-in-out infinite}@keyframes eenx{0%,100%{transform:none}50%{transform:translateY(-2px)}}
.anim .e-bandera{transform-origin:0 50%;animation:eband .7s ease-in-out infinite alternate}@keyframes eband{from{transform:scaleX(1) skewY(0)}to{transform:scaleX(.82) skewY(6deg)}}
.anim .e-pas{transform-origin:20% 100%;animation:epas 1.2s ease-in-out infinite}@keyframes epas{0%,100%{transform:none}30%{transform:rotate(-8deg) translateY(-4px)}60%{transform:none}}
.anim .e-flama{transform-origin:50% 100%;animation:eflama .45s ease-in-out infinite alternate}@keyframes eflama{from{transform:scale(1,1)}to{transform:scale(.94,1.06)}}
.anim .e-bot{animation:ebot .5s ease-in-out infinite alternate;animation-delay:calc(var(--i)*.12s)}@keyframes ebot{from{transform:none}to{transform:translateY(-6px)}}
.anim .e-espurna{animation:eesp 1.4s ease-out infinite;animation-delay:calc(var(--i)*.03s)}@keyframes eesp{0%{transform:scale(.2);opacity:0}20%{opacity:1}70%{transform:scale(1);opacity:1}100%{transform:scale(1.1);opacity:0}}
.anim .e-nucli{animation:enuc 1.4s ease-out infinite}@keyframes enuc{0%{transform:scale(1.4)}30%{transform:scale(.6)}100%{transform:scale(1)}}
.anim .e-porro{transform-origin:40% 80%;animation:eporro 2.4s ease-in-out infinite}@keyframes eporro{0%,100%{transform:none}40%,70%{transform:rotate(-10deg)}}
.anim .e-raig{animation:eraig .4s linear infinite;stroke-dashoffset:0}@keyframes eraig{to{stroke-dashoffset:-8}}
.anim .e-gegant{transform-origin:50% 100%;animation:egeg 1.6s ease-in-out infinite}@keyframes egeg{0%,100%{transform:rotate(-5deg)}50%{transform:rotate(5deg)}}
.anim .e-cor{animation:ecor 1s ease-in-out infinite}@keyframes ecor{0%,100%{transform:scale(1)}15%{transform:scale(1.12)}30%{transform:scale(1)}45%{transform:scale(1.08)}}
.anim .e-pin{animation:epin 1.6s cubic-bezier(.3,1.6,.5,1) infinite}@keyframes epin{0%{transform:translateY(-18px);opacity:0}30%,85%{transform:none;opacity:1}100%{opacity:1}}
.anim .e-bolet{transform-origin:50% 100%;animation:ebolet 2s cubic-bezier(.3,1.6,.5,1) infinite}@keyframes ebolet{0%{transform:scaleY(.3)}25%,100%{transform:none}}
.anim .e-mira{animation:emira 2.4s ease-in-out infinite}@keyframes emira{0%,100%{transform:translateX(-4px)}50%{transform:translateX(4px)}}
.anim .e-minutera{transform-origin:0 50%;animation:emin 2s linear infinite}@keyframes emin{to{transform:rotate(360deg)}}
.anim .e-caganer{animation:ecag 1.6s ease-in-out infinite}@keyframes ecag{0%,100%{transform:none}50%{transform:translateY(2px) scaleY(.97)}}
.anim .e-pila{animation:epila 1.6s ease-out infinite}@keyframes epila{0%,40%{transform:scale(0);opacity:0}60%,100%{transform:none;opacity:1}}
.anim .e-sol{animation:esol 2.8s ease-out infinite}@keyframes esol{0%{transform:translateY(14px)}50%,100%{transform:none}}
.anim .e-estel{animation:eest 1.6s ease-in-out infinite;animation-delay:calc(var(--i)*.4s)}@keyframes eest{0%,100%{transform:scale(.5);opacity:.4}50%{transform:scale(1.1);opacity:1}}
.anim .e-oli{animation:eoli 1.8s ease-in infinite}@keyframes eoli{0%{transform:translateY(-6px);opacity:0}20%{opacity:1}80%{transform:translateY(26px);opacity:1}100%{transform:translateY(30px);opacity:0}}
.anim .e-calcot{transform-origin:100% 0;animation:ecal 2s ease-in-out infinite}@keyframes ecal{0%,100%{transform:translateY(-6px)}45%,60%{transform:translateY(4px)}}
.anim .e-barretina{transform-origin:30% 100%;animation:ebar 1.8s ease-in-out infinite}@keyframes ebar{0%,100%{transform:rotate(0)}50%{transform:rotate(-4deg)}}
.anim .e-esq{transform-origin:50% 100%;animation:eesq 1.6s ease-in-out infinite}@keyframes eesq{0%,100%{transform:rotate(-4deg)}40%,55%{transform:rotate(8deg) translateX(3px)}}
.anim .e-dre{transform-origin:50% 100%;animation:edre 1.6s ease-in-out infinite}@keyframes edre{0%,100%{transform:rotate(4deg)}40%,55%{transform:rotate(-8deg) translateX(-3px)}}
.anim .e-xoc{animation:exoc 1.6s ease-out infinite}@keyframes exoc{0%,40%{opacity:0;transform:scale(.4)}50%{opacity:1;transform:scale(1)}75%,100%{opacity:0;transform:scale(1.2)}}
.anim .e-riu{animation:eriu .35s ease-in-out infinite alternate}@keyframes eriu{from{transform:rotate(-4deg)}to{transform:rotate(4deg) translateY(-2px)}}
.anim .e-llagrima{animation:ellag 1s ease-in infinite}@keyframes ellag{0%{transform:translateY(-3px);opacity:0}30%{opacity:1}100%{transform:translateY(8px);opacity:0}}
.anim .e-parpella{animation:eparp 3s infinite}@keyframes eparp{0%,92%,100%{transform:none}95%{transform:scaleY(.1)}}
.anim .e-cors{animation:ecors .8s ease-in-out infinite}@keyframes ecors{0%,100%{transform:scale(1)}40%{transform:scale(1.18)}}
.anim .e-salt{animation:esalt 1.6s cubic-bezier(.3,1.6,.5,1) infinite}@keyframes esalt{0%,60%,100%{transform:none}20%{transform:translateY(-8px) scale(1.04)}}
.anim .e-gota{animation:egota 1.4s ease-in infinite}@keyframes egota{0%{transform:translateY(-2px);opacity:0}25%{opacity:1}100%{transform:translateY(14px);opacity:0}}
.anim .e-sacseja{animation:esac .12s linear infinite alternate}@keyframes esac{from{transform:translateX(-1.5px)}to{transform:translateX(1.5px)}}
.anim .e-brill{animation:ebrill 2.2s ease-in-out infinite}@keyframes ebrill{0%,70%,100%{opacity:0;transform:translateX(0)}80%{opacity:1}90%{opacity:0;transform:translateX(30px)}}
.anim .e-mirada{animation:emir 2.4s ease-in-out infinite}@keyframes emir{0%,100%{transform:none}40%,60%{transform:translate(4px,-3px)}}
.anim .e-punts circle{animation:epun 1.4s ease-in-out infinite;animation-delay:calc(var(--i)*.3s)}@keyframes epun{0%,100%{opacity:.3}50%{opacity:1}}
.anim .e-ullet{animation:eull 2s infinite}@keyframes eull{0%,60%,100%{transform:none}70%,85%{transform:scaleY(.2)}}
.anim .k-confeti2{opacity:0;animation:kc2 2.4s ease-in infinite;animation-delay:calc(var(--i)*.25s)}@keyframes kc2{0%{opacity:0;transform:translateY(0)}10%{opacity:1}100%{opacity:0;transform:translateY(90px) rotate(200deg)}}
.anim .e-estels2{animation:eest2 1.2s ease-in-out infinite}@keyframes eest2{0%,100%{transform:scale(1) rotate(0)}50%{transform:scale(1.2) rotate(20deg)}}
.anim .e-z{opacity:0;animation:ez 2.4s ease-out infinite;animation-delay:calc(var(--i)*.5s)}@keyframes ez{0%{opacity:0;transform:translateY(6px)}30%{opacity:1}100%{opacity:0;transform:translateY(-8px)}}
.anim .e-suor{animation:egota 1.6s ease-in infinite}
.anim .e-cor-vola{animation:ecv 1.8s ease-out infinite}@keyframes ecv{0%{transform:translate(-8px,6px) scale(.4);opacity:0}25%{opacity:1}100%{transform:translate(8px,-28px) scale(1.1);opacity:0}}
.anim .e-galtes{animation:egal 2s ease-in-out infinite}@keyframes egal{0%,100%{opacity:.5}50%{opacity:1}}
.anim .e-bota{transform-origin:50% 100%;animation:ebota .7s cubic-bezier(.3,0,.5,1) infinite}@keyframes ebota{0%,100%{transform:translateY(0) scale(1.06,.94)}50%{transform:translateY(-8px) scale(.96,1.04)}}
.anim .e-flota{animation:eflota 1.6s ease-in-out infinite}@keyframes eflota{0%,100%{transform:translateY(0) rotate(-4deg)}50%{transform:translateY(-6px) rotate(4deg)}}
.anim .e-inclina{transform-origin:50% 90%;animation:einc 2s ease-in-out infinite}@keyframes einc{0%,55%,100%{transform:none}65%,85%{transform:rotate(-12deg)}}
.anim .e-balla{transform-origin:50% 100%;animation:eballa .8s ease-in-out infinite}@keyframes eballa{0%,100%{transform:rotate(-10deg) translateX(-4px)}50%{transform:rotate(10deg) translateX(4px)}}
.anim .e-respira{transform-origin:50% 100%;animation:eresp 2.4s ease-in-out infinite}@keyframes eresp{0%,100%{transform:scale(1)}50%{transform:scale(1.05,.97)}}
.anim .e-encongeix{transform-origin:50% 100%;animation:eenc 2s ease-in-out infinite}@keyframes eenc{0%,100%{transform:none}40%,70%{transform:scale(.9) translateY(4px) rotate(-6deg)}}
.anim .e-sanglot{transform-origin:50% 100%;animation:esang .5s ease-in-out infinite}@keyframes esang{0%,100%{transform:none}30%{transform:translateY(-3px) scale(1.02,.97)}}
.anim .e-rodola{animation:erod 1.4s cubic-bezier(.5,0,.5,1) infinite}@keyframes erod{0%{transform:translateX(-14px) rotate(-30deg)}50%{transform:translateX(14px) rotate(330deg)}100%{transform:translateX(-14px) rotate(-30deg)}}
.anim .e-barr-vola{animation:evola 1.8s cubic-bezier(.2,.8,.3,1) infinite}@keyframes evola{0%,15%{transform:none}35%{transform:translateY(-34px) rotate(-25deg)}60%{transform:translateY(-30px) rotate(10deg)}85%,100%{transform:none}}
.anim .e-fum{opacity:0;animation:efum 1.8s ease-out infinite}@keyframes efum{0%,15%{opacity:0;transform:scale(.3)}30%{opacity:1;transform:scale(1)}70%{opacity:0;transform:scale(1.4) translateY(-6px)}100%{opacity:0}}
.anim .e-ma-esq{animation:emesq .3s ease-in-out infinite alternate}@keyframes emesq{from{transform:translateX(-4px) rotate(-8deg)}to{transform:translateX(8px)}}
.anim .e-ma-dre{animation:emdre .3s ease-in-out infinite alternate}@keyframes emdre{from{transform:translateX(4px) rotate(8deg)}to{transform:translateX(-8px)}}
.anim .e-saluda{transform-origin:50% 100%;animation:esal .35s ease-in-out infinite alternate}@keyframes esal{from{transform:rotate(-22deg)}to{transform:rotate(18deg)}}
.anim .e-nota{opacity:0;animation:enota 1.6s ease-out infinite;animation-delay:calc(var(--i)*.8s)}@keyframes enota{0%{opacity:0;transform:translateY(8px)}30%{opacity:1}100%{opacity:0;transform:translateY(-16px) rotate(15deg)}}
.anim .e-arcada{transform-origin:50% 100%;animation:earc 1.2s ease-in-out infinite}@keyframes earc{0%,100%{transform:none}20%{transform:scale(1.04,.92)}35%{transform:scale(.96,1.06) translateY(-3px)}50%{transform:none}}
.anim .e-tremola{animation:etrem .08s linear infinite alternate}@keyframes etrem{from{transform:translate(-2px,1px)}to{transform:translate(2px,-1px)}}
.anim .e-fon{transform-origin:50% 100%;animation:efon 2.4s ease-in-out infinite}@keyframes efon{0%,100%{transform:none}50%,70%{transform:scale(1.12,.78)}}
.anim .e-trontolla{animation:etron 1.6s ease-in-out infinite}@keyframes etron{0%,100%{transform:rotate(-10deg) translateX(-3px)}50%{transform:rotate(10deg) translateX(3px)}}
.anim .e-espiral{transform-box:view-box;animation:eesp2 .9s linear infinite}@keyframes eesp2{to{transform:rotate(360deg)}}
.anim .e-crida{transform-origin:50% 100%;animation:ecrida .6s cubic-bezier(.3,1.5,.5,1) infinite}@keyframes ecrida{0%,100%{transform:none}40%{transform:translateY(-10px) scale(1.06)}}
.anim .e-crit{animation:ecrit .6s steps(2) infinite}@keyframes ecrit{0%{opacity:1}50%{opacity:.2}}
.anim .e-enrere{transform-origin:50% 100%;animation:eenr 2s ease-in-out infinite}@keyframes eenr{0%,100%{transform:none}30%,80%{transform:rotate(-14deg)}}
.anim .e-raig2{animation:eraig 0.3s linear infinite}
.anim .e-banyes{animation:eban .5s ease-in-out infinite alternate}@keyframes eban{from{transform:translateY(0)}to{transform:translateY(-3px)}}
.anim .e-aureola{animation:eaur 1.6s ease-in-out infinite}@keyframes eaur{0%,100%{transform:translateY(0)}50%{transform:translateY(-4px)}}
.anim .e-esternut{transform-origin:50% 100%;animation:ester 1.8s ease-in infinite}@keyframes ester{0%{transform:none}35%{transform:rotate(-10deg) translateY(-4px)}45%{transform:rotate(14deg) translateY(4px) scale(1.04)}60%,100%{transform:none}}
.anim .e-gotes{opacity:0;animation:egotes 1.8s ease-out infinite}@keyframes egotes{0%,42%{opacity:0;transform:scale(.3)}50%{opacity:1;transform:scale(1)}80%,100%{opacity:0;transform:scale(1.5) translateY(4px)}}
.anim .e-corre{transform-origin:50% 100%;animation:ecorre .25s ease-in-out infinite alternate}@keyframes ecorre{from{transform:rotate(8deg) translateY(0)}to{transform:rotate(8deg) translateY(-4px)}}
.anim .e-velocitat{animation:evel .3s linear infinite}@keyframes evel{from{transform:translateX(6px);opacity:.3}to{transform:translateX(-6px);opacity:1}}
.anim .e-porro2{transform-origin:80% 70%;animation:eporro2 2s ease-in-out infinite}@keyframes eporro2{0%,100%{transform:rotate(6deg)}30%,80%{transform:rotate(-4deg)}}
.anim .e-clap{animation:eclap .6s steps(1) infinite}@keyframes eclap{0%{opacity:0}50%{opacity:1}}
.anim .e-nega{animation:enega .35s ease-in-out infinite alternate}@keyframes enega{from{transform:translateX(-6px) rotate(-6deg)}to{transform:translateX(6px) rotate(6deg)}}
.anim .e-assent{transform-origin:50% 100%;animation:eass .4s ease-in-out infinite alternate}@keyframes eass{from{transform:translateY(-4px) rotate(0)}to{transform:translateY(4px) scale(1,.96)}}
.anim .e-xxt{animation:exxt 2s ease-in-out infinite}@keyframes exxt{0%,100%{transform:translateY(0)}50%{transform:translateY(-3px)}}
.anim .e-ones{animation:eones 1s ease-out infinite}@keyframes eones{0%{opacity:0;transform:translateX(-4px)}50%{opacity:1}100%{opacity:0;transform:translateX(4px)}}
.anim .e-mandibula{transform-origin:50% 0;animation:emand 1.6s cubic-bezier(.3,1.6,.5,1) infinite}@keyframes emand{0%,20%{transform:scaleY(.4)}50%,90%{transform:scaleY(1.5) translateY(4px)}100%{transform:scaleY(.4)}}
.anim .e-cascada{animation:ecasc .3s linear infinite alternate}@keyframes ecasc{from{opacity:.6}to{opacity:1}}
.anim .e-llengua{transform-origin:50% 0;animation:ellen .25s ease-in-out infinite alternate}@keyframes ellen{from{transform:rotate(-14deg)}to{transform:rotate(14deg)}}
.anim .e-arronsa{animation:earr 1.6s ease-in-out infinite}@keyframes earr{0%,100%{transform:none}30%,60%{transform:translateY(-6px) scale(1.03,.97)}}
.anim .e-giraull{transform-box:view-box;transform-origin:50px 54px;animation:egir 2.2s ease-in-out infinite}@keyframes egir{0%,100%{transform:translateY(0)}20%{transform:translate(-3px,-2px)}50%{transform:translate(0,-3px)}80%{transform:translate(3px,-2px)}}
.anim .e-reull{animation:ereull 2.4s ease-in-out infinite}@keyframes ereull{0%,100%{transform:translateX(-4px)}45%,55%{transform:translateX(5px)}}
.anim .e-tremola-lent{animation:etrem .18s linear infinite alternate}
.anim .e-espurneig{animation:eest 1.2s ease-in-out infinite;animation-delay:calc(var(--i)*.3s)}
.anim .e-vapor{opacity:0;animation:evap 1s ease-out infinite}@keyframes evap{0%{opacity:0;transform:scale(.4) translateY(4px)}40%{opacity:1}100%{opacity:0;transform:scale(1.3) translateY(-14px)}}
.anim .e-badall{transform-origin:50% 100%;animation:ebad 3s ease-in-out infinite}@keyframes ebad{0%,100%{transform:none}40%,60%{transform:scale(1.02,1.06) rotate(-4deg)}}
.anim .e-boca-badall{animation:eboca 3s ease-in-out infinite}@keyframes eboca{0%,100%{transform:scaleY(.5)}40%,60%{transform:scaleY(1.4)}}
.anim .e-ma-badall{animation:emab 3s ease-in-out infinite}@keyframes emab{0%,25%,75%,100%{transform:translate(10px,10px);opacity:0}40%,60%{transform:none;opacity:1}}
.anim .e-llepa{transform-origin:0 50%;animation:ellepa .5s ease-in-out infinite alternate}@keyframes ellepa{from{transform:rotate(-20deg)}to{transform:rotate(20deg)}}
.anim .e-baba{animation:ebaba 2s ease-in infinite}@keyframes ebaba{0%,40%{transform:scaleY(.4);opacity:.6}80%{transform:scaleY(1.3) translateY(3px);opacity:1}100%{opacity:0}}
.anim .e-rosa{transform-box:view-box;transform-origin:50px 72px;animation:erosa 2s ease-in-out infinite}@keyframes erosa{0%,100%{transform:rotate(-6deg)}50%{transform:rotate(6deg)}}
.anim .e-drac{transform-origin:20% 80%;animation:edrac 2s ease-in-out infinite}@keyframes edrac{0%,100%{transform:none}50%{transform:rotate(-4deg) translateY(-2px)}}
.anim .e-foc-drac{transform-origin:0 50%;animation:efocd 2s ease-out infinite}@keyframes efocd{0%,40%{opacity:0;transform:scale(.2)}55%{opacity:1;transform:scale(1.3)}85%,100%{opacity:0;transform:scale(1.6) translateX(4px)}}
.anim .e-tio{transform-origin:50% 100%;animation:etio .9s cubic-bezier(.3,1.6,.5,1) infinite}@keyframes etio{0%,100%{transform:none}30%{transform:translateY(-8px) rotate(-4deg)}50%{transform:translateY(0) scale(1.05,.92)}}
.anim .e-ou{transform-origin:50% 100%;animation:eou 1.2s ease-in-out infinite}@keyframes eou{0%,100%{transform:rotate(-8deg)}50%{transform:rotate(8deg)}}
.anim .e-espurna-puja{opacity:0;animation:epuja 1.6s ease-out infinite;animation-delay:calc(var(--i)*.35s)}@keyframes epuja{0%{opacity:0;transform:translateY(10px)}20%{opacity:1}100%{opacity:0;transform:translateY(-24px)}}
.anim .e-roda{animation:eroda .6s linear infinite}@keyframes eroda{to{transform:rotate(360deg)}}
.anim .e-baston-esq{transform-origin:0 100%;animation:ebesq .5s ease-in-out infinite alternate}@keyframes ebesq{from{transform:rotate(-8deg)}to{transform:rotate(2deg)}}
.anim .e-baston-dre{transform-origin:100% 100%;animation:ebdre .5s ease-in-out infinite alternate}@keyframes ebdre{from{transform:rotate(8deg)}to{transform:rotate(-2deg)}}
.anim .e-clap2{animation:eclap .5s steps(1) infinite}
.anim .e-gralla{transform-origin:30% 80%;animation:egra 1s ease-in-out infinite alternate}@keyframes egra{from{transform:rotate(-4deg)}to{transform:rotate(4deg)}}
.anim .e-ou-balla{animation:eoub .7s ease-in-out infinite alternate}@keyframes eoub{from{transform:translateY(0) rotate(-10deg)}to{transform:translateY(-8px) rotate(14deg)}}
.anim .e-raig-aigua{animation:eraigw .7s ease-in-out infinite alternate}@keyframes eraigw{from{opacity:.7}to{opacity:1}}
.anim .e-tap{animation:etap 1.8s cubic-bezier(.2,.8,.3,1) infinite}@keyframes etap{0%,30%{transform:none}50%{transform:translate(18px,-26px) rotate(80deg)}70%{opacity:0;transform:translate(26px,-36px) rotate(160deg)}71%,100%{opacity:0}}
.anim .e-bombolla{opacity:0;animation:ebomb 1.8s ease-out infinite;animation-delay:calc(.5s + var(--i)*.15s)}@keyframes ebomb{0%{opacity:0;transform:translateY(14px)}20%{opacity:1}100%{opacity:0;transform:translateY(-10px)}}
.anim .e-cremat{animation:ecrem 1.6s ease-in-out infinite}@keyframes ecrem{0%,100%{fill:#C4782A}50%{fill:#A85A1A}}
.anim .e-brill2{animation:ebrill3 1.6s ease-in-out infinite}@keyframes ebrill3{0%,100%{opacity:0}50%{opacity:1}}
.anim .e-botifarra{animation:ebot2 .2s linear infinite alternate}@keyframes ebot2{from{transform:translateY(0)}to{transform:translateY(-1.5px)}}
.anim .e-xiula{animation:evap 1.2s ease-out infinite}
.anim .e-caragol{animation:ecarg 4s linear infinite}@keyframes ecarg{0%{transform:translateX(-10px)}100%{transform:translateX(10px)}}
.anim .e-vapor-puja{animation:evap 1.6s ease-out infinite}
.anim .e-corona{animation:ecor2 1s cubic-bezier(.3,1.6,.5,1) infinite}@keyframes ecor2{0%,100%{transform:none}40%{transform:translateY(-8px) rotate(6deg)}}
.anim .e-onada{animation:eonada 1.4s linear infinite}@keyframes eonada{from{transform:translateX(0)}to{transform:translateX(-20px)}}
.anim .e-balanceig{transform-origin:50% 90%;animation:ebal 2s ease-in-out infinite}@keyframes ebal{0%,100%{transform:rotate(-5deg) translateY(0)}50%{transform:rotate(5deg) translateY(2px)}}
.anim .e-orella-esq{animation:eoesq 1.6s ease-in-out infinite}@keyframes eoesq{0%,70%,100%{transform:rotate(0)}80%{transform:rotate(-14deg)}90%{transform:rotate(4deg)}}
.anim .e-orella-dre{animation:eodre 1.6s ease-in-out infinite;animation-delay:.3s}@keyframes eodre{0%,70%,100%{transform:rotate(0)}80%{transform:rotate(14deg)}90%{transform:rotate(-4deg)}}
.anim .e-campana{animation:ecamp 1s ease-in-out infinite alternate}@keyframes ecamp{from{transform:rotate(-22deg)}to{transform:rotate(22deg)}}
.anim .e-so{animation:eclap 1s steps(1) infinite}
.anim .e-esbufega{transform-origin:50% 100%;animation:eesb 2s ease-in-out infinite}@keyframes eesb{0%,100%{transform:none}40%,60%{transform:scale(1.03,.95) translateY(3px)}}
.anim .e-buf{opacity:0;animation:ebuf 2s ease-out infinite}@keyframes ebuf{0%,40%{opacity:0;transform:translate(0,0) scale(.4)}55%{opacity:1}100%{opacity:0;transform:translate(12px,4px) scale(1.4)}}
.e-flash,.e-fum,.e-gotes,.e-buf{opacity:0}
.anim .e-flash{opacity:0;animation:eflash 1.8s steps(1) infinite}@keyframes eflash{0%,80%,100%{opacity:0}84%{opacity:.9}}
.anim .e-bombeta{animation:ebomb2 1.6s steps(1) infinite}@keyframes ebomb2{0%,30%{opacity:.25}35%,100%{opacity:1}}
.anim .e-gel{transform-origin:40% 100%;animation:egel 1.6s ease-in-out infinite}@keyframes egel{0%,100%{transform:rotate(-4deg)}50%{transform:rotate(4deg)}}
.anim .e-sacseja-lent{animation:enega 1s ease-in-out infinite alternate}
.anim .e-nas{animation:enas 2s ease-in-out infinite}@keyframes enas{0%,20%{transform:scaleX(1)}60%,85%{transform:scaleX(3.4)}100%{transform:scaleX(1)}}
.anim .e-capgira{animation:ecapg 2.4s cubic-bezier(.6,0,.4,1) infinite}@keyframes ecapg{0%,35%{transform:rotate(0)}50%,85%{transform:rotate(180deg)}100%{transform:rotate(360deg)}}
.anim .e-tamboreja{animation:etamb .3s ease-in-out infinite alternate}@keyframes etamb{from{transform:translateY(0)}to{transform:translateY(-3px)}}
.anim .e-agulla-rapida{animation:emin 1s linear infinite}
.anim .e-biceps{animation:ebic .5s ease-in-out infinite alternate}@keyframes ebic{from{transform:rotate(10deg)}to{transform:rotate(-12deg) scale(1.08)}}
.anim .e-sinia{animation:emin 6s linear infinite}
.anim .e-estrella-nadal{animation:eest2 1.4s ease-in-out infinite}
.anim .e-mascara{transform-origin:20% 90%;animation:emasc 1.4s ease-in-out infinite}@keyframes emasc{0%,100%{transform:rotate(-6deg)}50%{transform:rotate(6deg) translateY(-3px)}}
.anim .e-estel-vola{animation:eestv 2.4s ease-in-out infinite}@keyframes eestv{0%,100%{transform:translate(0,0) rotate(-6deg)}50%{transform:translate(4px,-6px) rotate(8deg)}}
.anim .e-fuet{transform-origin:50% 50%;animation:egel 2s ease-in-out infinite}
.anim .e-melindro{animation:emel 2s ease-in-out infinite}@keyframes emel{0%,100%{transform:translateY(0)}40%,60%{transform:translateY(10px)}}
.anim .e-panellet{animation:ebot .5s ease-in-out infinite alternate;animation-delay:calc(var(--i)*.1s)}
.anim .e-ma-morter{animation:emort .5s ease-in-out infinite alternate}@keyframes emort{from{transform:rotate(-10deg)}to{transform:rotate(10deg)}}
.anim .e-agulla-brui{animation:ebrui 2.4s cubic-bezier(.3,1.6,.5,1) infinite}@keyframes ebrui{0%{transform:rotate(-60deg)}30%{transform:rotate(20deg)}50%{transform:rotate(-8deg)}70%,100%{transform:rotate(0)}}
.anim .e-motxilla{transform-origin:50% 100%;animation:ebota 1s cubic-bezier(.3,0,.5,1) infinite}
.anim .e-tren{animation:etren 3s ease-in-out infinite}@keyframes etren{0%{transform:translateX(-6px)}40%{transform:translateX(6px)}45%,100%{transform:translateX(6px)}}
.anim .e-rellotget{animation:emin .8s linear infinite}
.anim .e-cotxe{animation:etamb .2s ease-in-out infinite alternate}
.anim .e-roda-bici{animation:emin .8s linear infinite}
.anim .e-bandereta{transform-origin:0 50%;animation:eband .5s ease-in-out infinite alternate}
.anim .e-cartell{animation:ecart 1.8s cubic-bezier(.3,1.6,.5,1) infinite}@keyframes ecart{0%{transform:scale(.6);opacity:0}25%,100%{transform:none;opacity:1}}
.anim .e-marca{animation:ebrill3 1.6s ease-in-out infinite}
.anim .e-esquiador{animation:eesq2 1.4s ease-in-out infinite}@keyframes eesq2{0%{transform:translate(-10px,4px)}100%{transform:translate(10px,-4px)}}
.anim .e-puny{transform-origin:50% 100%;animation:epuny .5s cubic-bezier(.3,1.6,.5,1) infinite alternate}@keyframes epuny{from{transform:translateY(6px)}to{transform:translateY(-6px) rotate(-4deg)}}
.anim .e-fletxa{animation:efl .8s ease-in-out infinite alternate}@keyframes efl{from{transform:translateX(-4px)}to{transform:translateX(4px)}}
.anim .e-progres{animation:eprog 2.4s ease-out infinite}@keyframes eprog{0%{transform:scaleX(.1)}70%,100%{transform:scaleX(.9)}}
.anim .e-carrega{animation:ecarr 2s steps(5) infinite}@keyframes ecarr{from{transform:scaleX(.1)}to{transform:scaleX(1)}}
.anim .e-petjada{opacity:0;animation:epetj 2s ease-out infinite;animation-delay:calc(var(--i)*.4s)}@keyframes epetj{0%{opacity:0}15%,60%{opacity:1}100%{opacity:0}}
.anim .e-trofeu{transform-origin:50% 100%;animation:ebota .8s cubic-bezier(.3,0,.5,1) infinite}
.anim .e-medalla{animation:emed 1.6s ease-in-out infinite}@keyframes emed{0%,100%{transform:rotate(-10deg)}50%{transform:rotate(10deg)}}
.anim .e-flameta{transform-origin:50% 100%;animation:eflama .3s ease-in-out infinite alternate;animation-delay:calc(var(--i)*.1s)}
.anim .e-globus{animation:eglob 2s ease-in-out infinite;animation-delay:calc(var(--i)*-.6s)}@keyframes eglob{0%,100%{transform:translateY(0) rotate(-3deg)}50%{transform:translateY(-6px) rotate(3deg)}}
.anim .e-cano{transform-origin:20% 90%;animation:ecano 1.6s ease-out infinite}@keyframes ecano{0%,10%{transform:none}15%{transform:scale(.92) rotate(-4deg)}30%,100%{transform:none}}
.anim .e-confeti-surt{opacity:0;animation:econf 1.6s ease-out infinite}@keyframes econf{0%,12%{opacity:0;transform:translate(0,0)}20%{opacity:1}100%{opacity:0;transform:translate(var(--dx),var(--dy)) rotate(220deg)}}
.anim .e-lupa{animation:elupa 2.4s ease-in-out infinite}@keyframes elupa{0%,100%{transform:translate(-4px,0)}25%{transform:translate(4px,-4px)}50%{transform:translate(4px,4px)}75%{transform:translate(-4px,4px)}}
.anim .e-llapis{animation:ellap .3s ease-in-out infinite alternate}@keyframes ellap{from{transform:translate(-3px,2px)}to{transform:translate(3px,-2px)}}
.anim .e-alerta{animation:eclap .8s steps(1) infinite}
.anim .e-barra-cob{animation:eclap 1s steps(1) infinite}
.anim .e-creu{animation:ecor 1s ease-in-out infinite}
.anim .e-interrogant{transform-origin:50% 100%;animation:ebota .7s cubic-bezier(.3,0,.5,1) infinite}
.anim .e-nuvol{animation:efl 2s ease-in-out infinite alternate}
.anim .e-gota-pluja{opacity:0;animation:egp .8s linear infinite;animation-delay:calc(var(--i)*.2s)}@keyframes egp{0%{opacity:0;transform:translateY(-4px)}20%{opacity:1}100%{opacity:0;transform:translateY(18px)}}
.anim .e-mercuri{animation:emerc 2s ease-in-out infinite}@keyframes emerc{0%,100%{transform:scaleY(.5)}60%{transform:scaleY(1)}}
.anim .e-floc{animation:efloc 2.4s linear infinite;animation-delay:calc(var(--i)*-.5s)}@keyframes efloc{0%{transform:translateY(-10px);opacity:0}20%{opacity:1}100%{transform:translateY(20px) translateX(4px);opacity:0}}
.anim .e-oliva{transform-origin:50% 50%;animation:ebota .6s cubic-bezier(.3,0,.5,1) infinite}
.anim .e-raim{transform-origin:50% 0;animation:erosa 2s ease-in-out infinite}
.anim .e-copa{transform-origin:50% 100%;animation:eresp 2.4s ease-in-out infinite}
.anim .e-ovella{transform-origin:50% 100%;animation:ebota .9s cubic-bezier(.3,0,.5,1) infinite}
.anim .e-pi{transform-origin:50% 100%;animation:egeg 2s ease-in-out infinite;animation-delay:calc(var(--i)*-.4s)}
.anim .e-aigua-cau{animation:ecasc .2s linear infinite alternate}
.anim .e-esquitx{animation:esalt .6s cubic-bezier(.3,1.6,.5,1) infinite}
.anim .e-tractor{animation:etamb .25s ease-in-out infinite alternate}
.anim .e-moto{transform-origin:20% 90%;animation:emoto 1.2s ease-in-out infinite}@keyframes emoto{0%,100%{transform:none}40%,60%{transform:rotate(-10deg)}}
.anim .e-fum2{animation:evap 1s ease-out infinite}
.anim .e-bus{animation:etamb .3s ease-in-out infinite alternate}
.anim .e-avio{animation:eavio 2s ease-in-out infinite}@keyframes eavio{0%,100%{transform:translate(-4px,4px) rotate(-6deg)}50%{transform:translate(4px,-4px) rotate(-10deg)}}
.anim .e-cremallera{animation:ecrem2 2.4s ease-in-out infinite alternate}@keyframes ecrem2{from{transform:translate(-14px,9px)}to{transform:translate(14px,-9px)}}
.anim .e-teleferic{animation:etel 3s ease-in-out infinite alternate}@keyframes etel{from{transform:translate(-20px,-5px)}to{transform:translate(20px,5px)}}
.anim .e-patinet{animation:etamb .2s ease-in-out infinite alternate}
'''
def svg(inner,cls,label=None):
    a=f'role="img" aria-label="{label}"' if label else 'aria-hidden="true"'
    return f'<svg class="em {cls}" viewBox="0 0 100 100" {a}>{inner}</svg>'
exec(open(f'{S}/emojis_v2.py').read())
exec(open(f'{S}/emojis_v3.py').read())
exec(open(f'{S}/emojis_v4.py').read())
exec(open(f'{S}/emojis_v5.py').read())
exec(open(f'{S}/emojis_v6.py').read())
exec(open(f'{S}/emojis_v7.py').read())
exec(open(f'{S}/emojis_v8.py').read())
grups=[]
E[:]=[e for e in E if e[0]!='Fora']
for g in ['Cares','Ànims','Celebrar','Comentar','Del dia','Festes','Taula','Terra','Ruta','Vehicles']:
    items=[e for e in E if e[0]==g]
    cards=''.join(f'''<article class="emo"><div class="gran">{svg(s,'anim',f'{nom}: {frase}')}</div>
<div class="txt"><b>{frase}</b><span>{nom}</span></div><div class="petits">{svg(s,'m32')}{svg(s,'m20')}</div></article>''' for _,i,nom,frase,s,_a in items)
    grups.append(f'<section class="grup"><h2>{g}</h2><div class="graella">{cards}</div></section>')
# Barra de reaccions d'exemple
barra=''.join(f'<button class="reac" aria-label="{frase}">{svg(s,"m28")}</button>' for _,i,nom,frase,s,_a in [E[k] for k in (0,2,3,8,9,10)])
html=open(f'{S}/emojis-plantilla.html').read().replace('%%CSS%%',CSS).replace('%%GRUPS%%','\n'.join(grups)).replace('%%BARRA%%',barra).replace('%%N%%',str(len(E)))
open(f'{S}/emojis.html','w').write(html); print(len(E),len(html))

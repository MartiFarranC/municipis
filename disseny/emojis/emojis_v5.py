# Quarta ronda: correccions de la tercera valoració.
NOU5=set()
def s5(id_,svg,anim=None): subst(id_,svg,anim); NOU5.add(id_)
def cara_parts(trets,fons=A,cls='',extra=''):
    # Cara amb els trets en un grup a part, perquè es puguin moure sols (girar o assentir) sense moure la barretina.
    return f'<circle cx="50" cy="58" r="33" fill="{fons}"/>'+G(cls,trets)+BARR+extra
enc=lambda svg: svg

s5('no',enc(cara_parts(L('M32,48l12,3M68,48l-12,3',TINTA,3)+ulls()+L('M40,75q10,-5 20,0',TINTA,3.4),cls='e-gira-no')
  +G('e-moviment',L('M10,52q-4,8 0,16M90,52q4,8 0,16',GRIS,2.6))))
s5('si',enc(cara_parts(L('M32,54q7,-6 14,0M54,54q7,-6 14,0',TINTA,3)+P('M36,66h28c0,8 -6,12 -14,12s-14,-4 -14,-12Z',TINTA),cls='e-gira-si')
  +G('e-moviment',L('M40,98q10,3 20,0M44,103q6,2 12,0',GRIS,2.6))))
s5('atope',enc(cara(L('M32,47l12,3M68,47l-12,3',TINTA,3)+C(40,56,3.4,TINTA)+C(60,56,3.4,TINTA)
  +P('M34,66h32c0,9 -7,14 -16,14s-16,-5 -16,-14Z',TINTA)+P('M36,66h28v4h-28Z',BL)))
  +G('e-llamp',llamp(14,18,1),'style="--i:0"')+G('e-llamp',llamp(92,14,1),'style="--i:1"')+G('e-llamp',llamp(10,72,.8),'style="--i:2"')+G('e-llamp',llamp(94,68,.8),'style="--i:3"')
  +G('e-espurneig',estrella(22,40,4)+estrella(80,88,4)))
s5('peto',enc(cara(L('M32,55q7,-5 14,0M54,55q7,-5 14,0',TINTA,3)+'<ellipse cx="54" cy="72" rx="4.5" ry="5" fill="none" stroke="'+TINTA+'" stroke-width="3"/>'
  +'<ellipse cx="30" cy="66" rx="6" ry="3.6" fill="#F05A6A" opacity=".55"/><ellipse cx="72" cy="66" rx="6" ry="3.6" fill="#F05A6A" opacity=".55"/>'))
  +G('e-cor-vola',cor(68,62,.9)))
# Caganer de cara: ajupit, camisa blanca, faixa, calces fosques i la seva obra a sota.
s5('caganer',G('e-caganer',
   o('M30,74c-4,6 -4,14 2,16h12l2,-14Z','#2A2E35',1.8)+o('M70,74c4,6 4,14 -2,16H56l-2,-14Z','#2A2E35',1.8)
  +o('M32,90h12v5H30Z','#5A3A24',1.4)+o('M56,90h12l2,5H56Z','#5A3A24',1.4)
  +o('M34,48c-4,8 -4,18 0,26h32c4,-8 4,-18 0,-26Z','#F4EDE0',1.8)+o('M33,64h34v6H33Z','#14171C',1.2)
  +oc(30,66,5,PELL,1.4)+oc(70,66,5,PELL,1.4)
  +oc(50,38,11,PELL,1.8)+C(46,37,1.6,K)+C(54,37,1.6,K)+L('M46,42q4,3 8,0','#8A3A2A',1.6)+C(42,41,2,'#F05A6A','opacity=".5"')+C(58,41,2,'#F05A6A','opacity=".5"')
  +barr_svg(50,38,11,True))
  +o('M42,98c0,-4 3,-6 6,-6c1,-3 3,-4 5,-4c3,0 5,2 5,5c3,0 5,2 5,5Z',MARRO,1.4,'class="e-pila"'),'caganer')
# Traca: focs artificials clàssics, amb raigs que surten del centre.
def esclat2(x,y,r,c,i):
    raigs=''.join(L(f'M{x+r*.25*math.cos(math.radians(a)):.1f},{y+r*.25*math.sin(math.radians(a)):.1f}L{x+r*math.cos(math.radians(a)):.1f},{y+r*math.sin(math.radians(a)):.1f}',c,2.4)+C(x+r*1.12*math.cos(math.radians(a)),y+r*1.12*math.sin(math.radians(a)),1.6,AC) for a in range(0,360,30))
    return G('e-esclat',raigs,f'style="--i:{i};transform-box:view-box;transform-origin:{x}px {y}px"')
s5('traca',f'<rect x="0" y="0" width="100" height="100" rx="18" fill="#141A2E"/>'
  +esclat2(32,34,20,R,0)+esclat2(70,28,17,A,1)+esclat2(58,64,15,BLAU,2)+esclat2(26,70,11,'#F07AA0',3)
  +G('e-coet',L('M86,96L82,62',AC,2,'stroke-dasharray="2 3"')+o('M80,58l3,-8l3,8v5h-6Z',R,1.2)),'traca')
# Ball de bastons: els bastons es creuen ben amunt, entre els dos dansaires.
def dansaire_b(x,s):
    return persona(x,70,1.05,'#F4EDE0')+o(f'M{x+s*6},54L{x+s*34},22','none',4.6)+L(f'M{x+s*6},54L{x+s*34},22',MARROC,3.2)+C(x+s*34,22,2.6,R)
s5('bastons',G('e-baston-esq',dansaire_b(22,1),'style="transform-box:view-box;transform-origin:28px 54px"')+G('e-baston-dre',dansaire_b(78,-1),'style="transform-box:view-box;transform-origin:72px 54px"')
  +G('e-clap2',L('M50,26v-8M42,28l-5,-6M58,28l5,-6',AC,2.6))+L('M6,96h88','#4A525F',2),'bastons')
# Correfoc: banyes enganxades a la barretina del diable.
s5('correfoc',G('e-balla',o('M34,98L40,58h20l6,40Z','#2A2E35')+o('M42,66l-14,-12','none',2)+L('M42,66l-14,-12','#2A2E35',6)+o('M58,66l14,-22','none',2)+L('M58,66l14,-22','#2A2E35',6)
  +barr_svg(50,46,11,True)
  +o('M40,38c-6,-4 -9,-10 -8,-16c3,5 7,7 11,8Z','#7A1418',1.4)+o('M60,38c6,-4 9,-10 8,-16c-3,5 -7,7 -11,8Z','#7A1418',1.4)
  +oc(50,46,11,PELL,2)+C(46,45,1.8,K)+C(54,45,1.8,K)+P('M45,51q5,4 10,0','#8A3A2A'))
  +L('M72,44L84,12',MARRO,3.6)+G('e-roda',''.join(L(f'M84,12L{84+16*math.cos(math.radians(a))},{12+16*math.sin(math.radians(a))}',[A,AC,R][i%3],2.6) for i,a in enumerate(range(0,360,30))),'style="transform-box:view-box;transform-origin:84px 12px"')
  +''.join(G('e-espurna-cau',C(x,24,2,AC),f'style="--i:{i}"') for i,x in enumerate((72,80,90,66,96))),'correfoc')
# Autobús: llarg, amb moltes finestres, la porta, el parabrisa davant i el rètol de destinació.
s5('bus',G('e-bus',o('M4,30c0,-4 3,-6 6,-6h72c6,0 10,4 12,10l2,8v32c0,3 -2,4 -4,4H8c-2,0 -4,-1 -4,-4Z','#E8B23A')
  +o('M80,30h8c3,0 5,3 6,6l1,10h-15Z','#8CC4E8',1.6)+''.join(o(f'M{x},32h12v14h-12Z','#8CC4E8',1.4) for x in (8,23,38,53))
  +o('M68,32h10v38h-10Z','#5A8AA8',1.4)+L('M73,32v38',K,1)+f'<rect x="4" y="54" width="64" height="5" fill="{R}"/>'
  +o('M60,18h30v6h-30Z','#2A2E35',1.2)+L('M64,21h22',AC,1.6)
  +oc(20,78,8,'#2A2E35')+oc(84,78,8,'#2A2E35')+C(20,78,3,FE)+C(84,78,3,FE)+oc(96,64,2.4,AC,1)),'bus')
# Moto: dues rodes grosses, dipòsit, seient, manillar i tub d'escapament.
s5('moto',G('e-moto',oc(22,72,15,'#2A2E35')+C(22,72,6,FE)+oc(80,72,15,'#2A2E35')+C(80,72,6,FE)
  +o('M22,72L44,52H66L80,72','none',2.4)+L('M22,72L44,52H66L80,72',FE,3)
  +o('M42,56c2,-8 10,-12 22,-12c6,0 8,4 6,10l-4,4H44Z',R,2)+o('M24,50h20l2,6H26Z','#2A2E35',1.6)
  +o('M46,60h16v12H46Z','#6A7484',1.6)+o('M44,74h-22l-2,4h24Z','#C9CED6',1.4)
  +L('M70,46L80,72',FE,3)+L('M64,40l8,6l8,-4',K,3)+oc(82,44,3,AC,1.2))
  +G('e-fum2',C(8,80,4,'#C9CED6')+C(6,72,3,'#C9CED6')),'moto')

FORA5={'calla','emociono','shh','gegant','vermut','rodalies','llaut','avio'}
E[:]=[e for e in E if e[1] not in FORA5]
CSS+=r'''
.anim .e-gira-no{animation:egirano .5s ease-in-out infinite alternate}@keyframes egirano{from{transform:translateX(-8px)}to{transform:translateX(8px)}}
.anim .e-gira-si{animation:egirasi .45s ease-in-out infinite alternate}@keyframes egirasi{from{transform:translateY(-6px) scaleY(1.04)}to{transform:translateY(6px) scaleY(.94)}}
'''

# La barretina calcada del dibuix és més alta: les cares (i el capgròs) s'encongeixen una mica perquè hi càpiga.
def _encongeix_final(svg): return f'<g transform="translate(5,7) scale(.9)" style="transform-box:view-box;transform-origin:0 0">{svg}</g>'
E[:]=[(g,i,nom,fr,_encongeix_final(svg) if (g=='Cares' or i=='capgros') else svg,an) for g,i,nom,fr,svg,an in E]

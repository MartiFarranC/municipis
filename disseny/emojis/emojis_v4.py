# Tercera ronda: barretina nova (a gen-emojis), mans noves, i correccions de la segona valoració.
MA_COL='#F7C25A'; MA_VORA='#B8862F'
MA_PATH='M-9,14L-11,-8C-11,-15 -6,-15 -6,-9L-6,-12C-6,-20 -1,-20 -1,-12L-1,-13C-1,-21 4,-21 4,-13L4,-11C4,-18 9,-18 9,-11L9,0C11,-4 17,-4 16,2C14,8 10,12 7,16Z'
DIT_PATH='M-8,14C-13,14 -14,7 -13,2C-12,-2 -9,-4 -5,-4L-5,-22C-5,-27 1,-27 1,-22L1,-5C5,-5 9,-3 10,1C11,6 9,12 4,14Z'
def _g(x,y,rot,e,inner,flip=False):
    sx=-e if flip else e
    return f'<g transform="translate({x} {y}) rotate({rot}) scale({sx} {e})" {ESTIL0}>{inner}</g>'
def _simple(formes,c):
    # Contorn primer i color a sobre: una sola silueta neta, sense ratlles per dins.
    return (''.join(f.replace('FILL','none').replace('/>',' stroke="#3A2410" stroke-width="3.2"/>') for f in formes)
            +''.join(f.replace('FILL',c) for f in formes))
def ma3(x,y,rot=0,e=1,flip=False,c=A):
    # Mà sense dits: només una forma rodona.
    f=['<ellipse cx="0" cy="-2" rx="9.5" ry="11" fill="FILL"/>']
    return _g(x,y,rot,e,_simple(f,c),flip)
def dit(x,y,rot=0,e=1,flip=False,c=A):
    # Mà sense dits, allargada i dreta (per tapar la boca o aguantar la barbeta).
    f=['<rect x="-7" y="-20" width="14" height="30" rx="7" fill="FILL"/>']
    return _g(x,y,rot,e,_simple(f,c),flip)
NOU4=set()
def s4(id_,svg,anim=None): subst(id_,svg,anim); NOU4.add(id_)

# --- Mans noves
s4('bravo',G('e-bota',cara(L('M32,54q7,-6 14,0M54,54q7,-6 14,0',TINTA,3)+P('M36,62h28c0,8 -6,12 -14,12s-14,-4 -14,-12Z',TINTA)))
  +G('e-ma-esq',ma3(34,90,25,1.05,True))+G('e-ma-dre',ma3(66,90,-25,1.05))+G('e-clap',L('M44,94l-4,-6M50,96v-8M56,94l4,-6',AC,2.6)))
s4('ei',cara(G('e-parpella',ulls())+L('M36,68q14,12 28,0',TINTA,3.4))+G('e-saluda',ma3(86,68,10,1.15),'style="transform-box:view-box;transform-origin:86px 84px"'))
s4('aimare',G('e-sacseja-lent',cara(C(64,56,3.4,TINTA)+L('M58,47l11,-2',TINTA,2.6)+L('M42,76q10,-5 20,0',TINTA,3.2))+ma3(38,54,-10,1.35)))
s4('calla',G('e-riu',cara(L('M32,52l7,-4l7,4M54,52l7,-4l7,4',TINTA,3)+C(28,64,5,R,'opacity=".35"')+C(72,64,5,R,'opacity=".35"'))+ma3(50,84,0,1.2)))
s4('niidea',G('e-arronsa',cara(L('M33,46l11,-2M56,44l11,2',TINTA,2.6)+ulls()+L('M40,74q5,-3 10,0t10,0',TINTA,3))+ma3(12,76,-60,.95,True)+ma3(88,76,60,.95)))
s4('shh',cara(L('M33,54q6,3 12,0M55,54q6,3 12,0',TINTA,3)+'<ellipse cx="50" cy="74" rx="5" ry="3.4" fill="'+TINTA+'"/>')+G('e-xxt',dit(52,94,0,1.05))
  +G('e-ones',L('M80,62q4,4 0,8M86,58q6,8 0,16',AC,2.4)))
# Pensant: mà sota la barbeta amb el dit cap al costat (com 🤔), mai cap amunt.
s4('pensant',G('e-inclina',cara(C(42,53,3.6,TINTA)+C(63,53,3.6,TINTA)+L('M33,44l11,-3M56,39q7,-4 13,1',TINTA,2.6)+L('M44,73q8,-4 16,-2',TINTA,3.2))
  +dit(40,90,-70,.85,True))+G('e-punts',C(84,42,2.5,AC,'style="--i:0"')+C(91,34,3.2,AC,'style="--i:1"')+C(97,24,4,AC,'style="--i:2"')))
s4('atope',G('e-crida','<g transform="translate(-4 -2) scale(.82)" style="transform-box:view-box;transform-origin:0 0">'+cara(L('M31,48l12,4M69,48l-12,4',TINTA,3)+ulls()+P('M36,66h28v4c0,6 -6,10 -14,10s-14,-4 -14,-10Z',TINTA)+L('M38,68h24',BL,2.4)))
  +G('e-biceps',P('M60,100C60,88 64,82 72,80C76,74 80,68 82,62','none',f'stroke="{TINTA}" stroke-width="15.2" stroke-linecap="round"')
     +P('M60,100C60,88 64,82 72,80C76,74 80,68 82,62','none',f'stroke="{A}" stroke-width="12" stroke-linecap="round"')
     +C(68,86,9.6,TINTA)+C(68,86,8,A)+_g(84,58,0,.8,_simple(['<ellipse cx="0" cy="0" rx="9" ry="8" fill="FILL"/>'],A)),
     'style="transform-box:view-box;transform-origin:60px 100px"'))

# --- Cares
s4('sorpres',G('e-salt',cara(G('e-ull-obre',ull_gran(39,53,7.5)+ull_gran(61,53,7.5))+G('e-celles',L('M30,40q8,-6 16,-2M54,38q8,-4 16,2',TINTA,2.6))
  +G('e-boca-o','<ellipse cx="50" cy="76" rx="7" ry="9" fill="'+TINTA+'"/>'))))
s4('vergonya',G('e-encongeix',cara(ull_gran(40,54,5.6,-1.5,2)+ull_gran(62,54,5.6,-1.5,2)+L('M33,45q6,-3 12,-1M55,44q6,-2 12,1',TINTA,2.4)
  +G('e-galtes2','<ellipse cx="28" cy="66" rx="9" ry="5" fill="#F05A6A"/><ellipse cx="72" cy="66" rx="9" ry="5" fill="#F05A6A"/>')
  +L('M44,76q3,-2 6,0t6,0',TINTA,2.6))+G('e-suor',P('M82,42c-3,5 -5,8 -5,10a5,5 0 0 0 10,0c0,-2 -2,-5 -5,-10Z',BLAU))))
s4('emociono',G('e-tremola-lent',cara(L('M30,44q7,-4 14,-1M56,43q7,-3 14,1',TINTA,2.6)
  +ull_gran(39,55,8,0,1)+ull_gran(61,55,8,0,1)+'<ellipse cx="39" cy="61" rx="7" ry="3" fill="#8CC4E8" opacity=".85"/><ellipse cx="61" cy="61" rx="7" ry="3" fill="#8CC4E8" opacity=".85"/>'
  +P('M40,72q5,5 10,0q5,5 10,0','none',f'stroke="{TINTA}" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"'))
  +G('e-gota',P('M31,64c-3,5 -5,8 -5,10a5,5 0 0 0 10,0c0,-2 -2,-5 -5,-10Z',BLAU))+G('e-gota',P('M69,64c-3,5 -5,8 -5,10a5,5 0 0 0 10,0c0,-2 -2,-5 -5,-10Z',BLAU),'style="animation-delay:-.6s"')))
s4('fastic',G('e-arcada',cara(L('M32,52l9,4l-9,4M68,52l-9,4l9,4',TINTA,3)+'<ellipse cx="50" cy="73" rx="10" ry="6" fill="'+TINTA+'"/>'
  +G('e-llengua2',P('M43,74h14v12c0,6 -3,9 -7,9s-7,-3 -7,-9Z','#E26A8A')+L('M50,77v12','#B84868',1.4),'style="transform-box:view-box;transform-origin:50px 74px"')
  +C(28,66,4,'#7AA84A','opacity=".6"')+C(72,66,4,'#7AA84A','opacity=".6"'),'#A8CC74')))
s4('no',G('e-nega2',cara(L('M32,48l12,3M68,48l-12,3',TINTA,3)+G('e-ulls-nega',ulls())+L('M38,75q12,-6 24,0',TINTA,3.6)))
  +G('e-moviment',L('M6,44q-5,12 0,24M13,38q-7,18 0,34M94,44q5,12 0,24M87,38q7,18 0,34',GRIS,2.6)))
s4('si',G('e-assent2',cara(L('M32,54q7,-6 14,0M54,54q7,-6 14,0',TINTA,3)+P('M36,66h28c0,8 -6,12 -14,12s-14,-4 -14,-12Z',TINTA)))
  +G('e-moviment',L('M30,98q20,6 40,0M34,92q16,4 32,0',GRIS,2.6)))
s4('peto',cara(C(39,55,3.8,TINTA)+L('M55,56q6,-6 12,0',TINTA,3.4)+L('M52,64q8,1 4,5q8,1 2,7',TINTA,3.2)+'<ellipse cx="30" cy="66" rx="6" ry="3.6" fill="#F05A6A" opacity=".6"/><ellipse cx="72" cy="66" rx="6" ry="3.6" fill="#F05A6A" opacity=".6"/>')
  +G('e-cor-vola',cor(66,64,.9)))
s4('trapella',cara(L('M31,50l13,4M69,50l-13,4',TINTA,3.2)+C(40,57,3.2,TINTA)+C(60,57,3.2,TINTA)+P('M34,68q16,14 32,0q-16,6 -32,0Z',TINTA),'#B06AD8',
  extra=G('e-banyes',P('M24,18c-8,-6 -10,-14 -8,-20c4,6 12,8 18,10c-2,4 -6,8 -10,10Z','#3A1250',f'stroke="{AC}" stroke-width="1.8"')
                     +P('M60,10c4,-8 10,-12 18,-14c-2,8 -6,14 -12,20c-2,-2 -4,-4 -6,-6Z','#3A1250',f'stroke="{AC}" stroke-width="1.8"'))))

# --- Coses
s4('bota',G('e-pas-esq',f'<g transform="translate(-2 30) scale(.62)">{bota_p()}</g>')+G('e-pas-dre',f'<g transform="translate(40 30) scale(.62)">{bota_p()}</g>')
  +L('M4,87h92','#6A5A4A',3)+G('e-pols',C(30,85,4,'#8A7A6A')+C(36,86,3,'#8A7A6A')+C(74,85,4,'#8A7A6A')))
# Gegant: alt, amb el cap gran, faldilla fins a terra i el geganter sota; barretina nova.
s4('gegant',G('e-gegant',o('M18,98L34,40h32l16,58Z','#2F5FA8')+o('M18,98h64l-2,-8H20Z',A,1.6)+L('M28,74h44','#21467F',2)
  +o('M34,42c-8,6 -12,18 -14,30','none',2.2)+L('M34,42c-8,6 -12,18 -14,30','#2F5FA8',6)+o('M66,42c8,6 12,18 14,30','none',2.2)+L('M66,42c8,6 12,18 14,30','#2F5FA8',6)
  +oc(20,74,4,PELL,1.4)+oc(80,74,4,PELL,1.4)+o('M34,40h32l-5,8h-22Z',BL,1.6)
  +oc(50,25,14,PELL,2)+C(45,24,1.8,K)+C(55,24,1.8,K)+L('M44,31q6,4 12,0','#8A3A2A',1.8)+C(41,29,2.4,'#F05A6A','opacity=".5"')+C(59,29,2.4,'#F05A6A','opacity=".5"')
  +barr_svg(50,25,14,True))+G('e-cames',L('M44,98v2M56,98v2','#2A2E35',4)),'gegant')
# Caganer: pagès ajupit de perfil, amb barretina, faixa i calces abaixades.
s4('caganer',G('e-caganer',
   o('M30,86h40','none',2)+o('M34,68c-2,8 -2,14 2,18h10l2,-14Z','#2A2E35',1.8)+o('M66,68c2,8 2,14 -2,18H54l-2,-14Z','#2A2E35',1.8)
  +o('M38,86h10v6H36Z','#5A3A24',1.4)+o('M56,86h10l2,6H56Z','#5A3A24',1.4)
  +o('M36,46c-6,8 -6,18 -2,24h32c4,-8 2,-18 -6,-24Z','#F4EDE0',1.8)+o('M34,62h32v6H34Z','#14171C',1.4)
  +o('M24,62c-8,0 -12,6 -12,10s4,8 12,8s10,-4 10,-8s-2,-10 -10,-10Z',PELL,1.8)+L('M24,66v10','#C99A7A',1.4)
  +o('M60,52c6,2 10,8 10,14','none',2)+L('M60,52c6,2 10,8 10,14','#F4EDE0',5)+oc(70,68,4,PELL,1.4)
  +oc(50,32,12,PELL,2)+C(55,30,1.6,K)+L('M54,37q3,2 6,0','#8A3A2A',1.4)+barr_svg(50,32,12,True))
  +o('M8,96c0,-4 3,-6 6,-6c1,-3 3,-4 5,-4c3,0 5,2 5,5c3,0 5,2 5,5Z',MARRO,1.4,'class="e-pila"'),'caganer')
# Barretina sola: penjada d'un clau, que es veu sencera.
s4('barretina',G('e-barretina',barr_svg(54,98,44,True)),'barretina')
# Correfoc: un diable (amb barretina i banyes) i la forca amb la carretilla que gira i escup espurnes.
s4('correfoc',G('e-balla',o('M34,98L40,54h20l6,44Z','#2A2E35')+o('M42,62l-14,-12','none',2)+L('M42,62l-14,-12','#2A2E35',6)+o('M58,62l14,-22','none',2)+L('M58,62l14,-22','#2A2E35',6)
  +oc(50,42,11,PELL,2)+C(46,41,1.8,K)+C(54,41,1.8,K)+P('M45,47q5,4 10,0','#8A3A2A')+barr_svg(50,42,11,True)
  +o('M38,26c-5,-6 -5,-13 -3,-17c2,6 6,8 9,10Z','#7A1418',1.4)+o('M58,22c2,-6 6,-10 11,-12c0,6 -2,10 -6,14Z','#7A1418',1.4))
  +L('M72,40L84,10',MARRO,3.6)+G('e-roda',''.join(L(f'M84,10L{84+16*math.cos(math.radians(a))},{10+16*math.sin(math.radians(a))}',[A,AC,R][i%3],2.6) for i,a in enumerate(range(0,360,30))),'style="transform-box:view-box;transform-origin:84px 10px"')
  +''.join(G('e-espurna-cau',C(x,22,2,AC),f'style="--i:{i}"') for i,x in enumerate((72,80,90,66,96))),'correfoc')
# Ja hi som: el cartell d'un poble amb una bandereta que s'hi planta.
s4('cartell',L('M30,96V52M70,96V52',FE,4)+o('M10,22h80v32h-80Z','#F4EDE0',2)+P('M14,26h72v24h-72Z','none',f'stroke="{R}" stroke-width="3"')
  +f'<rect x="24" y="33" width="52" height="5" rx="2" fill="#2A2E35"/><rect x="32" y="42" width="36" height="4" rx="2" fill="#6A7484"/>'
  +G('e-bandereta-planta',L('M84,58V16',MARRO,2.6)+o('M84,16h14l-4,5l4,5h-14Z',A,1.4)),'cartell')

s4('por',G('e-tremola',cara(ull_gran(39,52,6.5)+ull_gran(61,52,6.5)+'<ellipse cx="50" cy="78" rx="6" ry="10" fill="'+TINTA+'"/>','#C8D8E8')
  +ma3(22,74,-12,1,False,'#C8D8E8')+ma3(78,74,12,1,True,'#C8D8E8')))
s4('son',G('e-badall',cara(L('M33,54q6,3 12,0M55,54q6,3 12,0',TINTA,3)+'<ellipse class="e-boca-badall" cx="50" cy="74" rx="8" ry="9" fill="'+TINTA+'"/>'))
  +G('e-ma-badall',ma3(62,80,-20,.95)))
FORA4={'tupots','fonc','ullet'}
E[:]=[e for e in E if e[1] not in FORA4]

CSS+=r'''
.anim .e-ull-obre{animation:eullob 2.4s ease-in-out infinite}@keyframes eullob{0%,40%,100%{transform:scale(1)}50%,70%{transform:scale(1.18)}}
.anim .e-celles{animation:ecelles 2.4s ease-in-out infinite}@keyframes ecelles{0%,40%,100%{transform:none}50%,70%{transform:translateY(-4px)}}
.anim .e-boca-o{animation:ebocao 2.4s ease-in-out infinite}@keyframes ebocao{0%,40%,100%{transform:scale(1)}50%,70%{transform:scale(1.2,1.3)}}
.anim .e-galtes2{animation:egal2 1.4s ease-in-out infinite}@keyframes egal2{0%,100%{opacity:.35;transform:scale(.85)}50%{opacity:.95;transform:scale(1.15)}}
.e-galtes2{opacity:.7}
.anim .e-llengua2{animation:ellen2 .5s ease-in-out infinite alternate}@keyframes ellen2{from{transform:scaleY(.85)}to{transform:scaleY(1.1)}}
.anim .e-ulls-nega{animation:eullnega .25s ease-in-out infinite alternate}@keyframes eullnega{from{transform:translateX(-2px)}to{transform:translateX(2px)}}
.anim .e-bandereta-planta{animation:eplanta 2s cubic-bezier(.3,1.6,.5,1) infinite}@keyframes eplanta{0%,15%{transform:translateY(-30px);opacity:0}35%,100%{transform:none;opacity:1}}
'''

# --- Ai, mare! i A tope!, refets perquè s'entenguin sense dits
s4('aimare',G('e-sacseja-lent',cara(L('M31,54l9,4l-9,4M69,54l-9,4l9,4',TINTA,3)+L('M38,77q6,-5 12,0t12,0',TINTA,3.2)
  +L('M30,44l10,-3M70,44l-10,-3',TINTA,2.4))
  +ma3(15,52,-25,1.05)+ma3(85,52,25,1.05,True)
  +G('e-suor',P('M78,34c-3,5 -5,8 -5,10a5,5 0 0 0 10,0c0,-2 -2,-5 -5,-10Z',BLAU))),'aimare')
# Braç de fer força (com l'emoji 💪): espatlla a baix, colze a la dreta, avantbraç pujant en diagonal cap al puny i un bíceps ben inflat.
def _braç(c,extra=0):
    w=lambda v: v+extra
    return (f'<path d="M60,95L92,90" fill="none" stroke="{c}" stroke-width="{w(17)}" stroke-linecap="round"/>'
            f'<path d="M92,90L80,60" fill="none" stroke="{c}" stroke-width="{w(14)}" stroke-linecap="round"/>'
            f'<ellipse cx="75" cy="85" rx="{w(25)/2:.1f}" ry="{w(19)/2:.1f}" fill="{c}" transform="rotate(-10 75 85)"/>'
            f'<circle cx="79" cy="55" r="{w(20)/2:.1f}" fill="{c}"/>')
bras=_braç(TINTA,3.2)+_braç(A)+L('M73,54q6,-4 12,0',"#C9932A",1.6)
s4('atope',G('e-crida','<g transform="translate(-4 -2) scale(.82)" style="transform-box:view-box;transform-origin:0 0">'+cara(L('M31,48l12,4M69,48l-12,4',TINTA,3)+ulls()+P('M36,66h28v4c0,6 -6,10 -14,10s-14,-4 -14,-10Z',TINTA)+L('M38,68h24',BL,2.4))+'</g>')
  +G('e-flexio',bras,'style="transform-box:view-box;transform-origin:92px 90px"')
  +G('e-espurneig',estrella(95,48,4)+estrella(66,70,3)),'atope')
CSS+=r'''
.anim .e-bicep{animation:ebicep .6s ease-in-out infinite alternate}@keyframes ebicep{from{transform:scale(.85)}to{transform:scale(1.15)}}
.anim .e-flexio{animation:eflex .6s ease-in-out infinite alternate}@keyframes eflex{from{transform:rotate(4deg)}to{transform:rotate(-4deg)}}
'''

# --- Brindem: dos botellins que xoquen (vidre verd, etiqueta crema amb estrella vermella, xapa daurada). Sense cap marca ni nom.
def botelli():
    # Botellí de cervesa ambre amb la xapa i les etiquetes vermelles i una estrella; sense cap nom ni logotip.
    estel=lambda x,y,r: f'M{x},{y-r}L{x+r*.29},{y-r*.4}L{x+r*.95},{y-r*.31}L{x+r*.47},{y+r*.15}L{x+r*.59},{y+r*.81}L{x},{y+r*.5}L{x-r*.59},{y+r*.81}L{x-r*.47},{y+r*.15}L{x-r*.95},{y-r*.31}L{x-r*.29},{y-r*.4}Z'
    return (o('M-9,34h18c3,0 5,-2 5,-5V0c0,-6 -5,-9 -6,-15V-26h-12V-15c-1,6 -6,9 -6,15v29c0,3 2,5 5,5Z','#F2B83A',2.2)
      +L('M-8,2v24','#FFE08A',2.6,'opacity=".8"')
      +o('M-6,-30h12v5h-12Z',R,1.4)
      +o('M-6,-25h12v11h-12Z',R,1.4)+P(estel(0,-19.5,3.6),'#F4E4C8')
      +o('M-12,6h24v18h-24Z',R,1.6)+P(estel(0,12,4.6),'#F4E4C8')+L('M-8,19h16M-6,22h12','#F4E4C8',1,'opacity=".7"'))
s4('brindis',G('e-esq',f'<g transform="translate(32 58) rotate(16)" {ESTIL0}>{botelli()}</g>')+G('e-dre',f'<g transform="translate(68 58) rotate(-16)" {ESTIL0}>{botelli()}</g>')
  +G('e-xoc',''.join(L(f'M{50+7*math.cos(math.radians(a))},{24+7*math.sin(math.radians(a))}L{50+13*math.cos(math.radians(a))},{24+13*math.sin(math.radians(a))}',AC,3) for a in (200,240,270,300,340)))
  +G('e-escuma',C(44,22,2.6,'#fff')+C(56,20,2.2,'#fff')+C(50,16,2,'#fff')),'brindis')

# --- A tope!: sense braç. Dents serrades, celles de decisió, vapor pel cap i llamps d'energia.
llamp=lambda x,y,s: P(f'M{x},{y}l{-6*s},{12*s}h{5*s}l{-3*s},{10*s}l{9*s},{-14*s}h{-5*s}l{3*s},{-8*s}Z',AC,f'stroke="{AF}" stroke-width="1.2" stroke-linejoin="round"')
s4('atope',G('e-crida',cara(L('M30,47l14,6M70,47l-14,6',TINTA,3.4)+C(40,57,3.4,TINTA)+C(60,57,3.4,TINTA)
   +f'<rect x="34" y="66" width="32" height="12" rx="4" fill="{BL}" stroke="{TINTA}" stroke-width="2.6"/>'+L('M42,66v12M50,66v12M58,66v12M34,72h32',TINTA,1.6),'#F2A030'))
  +G('e-vapor',''.join(C(x,y,r,'#C9CED6') for x,y,r in [(10,56,5),(6,46,6),(90,56,5),(94,46,6)]))
  +G('e-llamp',llamp(14,14,1),'style="--i:0"')+G('e-llamp',llamp(92,10,1),'style="--i:1"')+G('e-llamp',llamp(10,74,.8),'style="--i:2"')+G('e-llamp',llamp(94,70,.8),'style="--i:3"'),'atope')
CSS+=r'''
.anim .e-llamp{animation:ellamp .9s steps(1) infinite;animation-delay:calc(var(--i)*.22s)}@keyframes ellamp{0%{opacity:1;transform:scale(1)}50%{opacity:.15;transform:scale(.8)}}
'''


# Les cares (i el capgròs) es fan una mica més petites i baixen, perquè la barretina, ara més alta, hi càpiga.
def _encongeix(svg): return svg  # la barretina ja hi cap sense encongir la cara
E[:]=[(g,i,nom,fr,_encongeix(svg) if (g=='Cares' or i=='capgros') else svg,an) for g,i,nom,fr,svg,an in E]

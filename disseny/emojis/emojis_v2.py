# Segona ronda: correccions segons la valoració del Martí.
ESTIL0='style="transform-box:view-box;transform-origin:0 0"'
def contorn(formes,c,gruix=3.6):
    # Primer el contorn de totes les formes i després l'ompliment: la silueta queda neta, sense línies per dins.
    return ''.join(f.replace('FILL','none').replace('/>',f' stroke="{TINTA}" stroke-width="{gruix}" stroke-linejoin="round"/>') for f in formes)+''.join(f.replace('FILL',c) for f in formes)
def ma2(x,y,rot=0,e=1,c=A):
    f=[f'<rect x="-10" y="-6" width="20" height="17" rx="7" fill="FILL"/>']
    f+=[f'<rect x="{dx-2.6}" y="-17" width="5.2" height="15" rx="2.6" fill="FILL"/>' for dx in (-7.4,-2.5,2.5,7.4)]
    f+=['<rect x="-17" y="-1" width="11" height="5.6" rx="2.8" fill="FILL" transform="rotate(-35 -9 2)"/>']
    return f'<g transform="translate({x} {y}) rotate({rot}) scale({e})" {ESTIL0}>'+contorn(f,c)+'</g>'
def puny(x,y,rot=0,e=1,c=A,dit=False):
    f=['<rect x="-14" y="-10" width="28" height="22" rx="8" fill="FILL"/>']+[f'<circle cx="{dx}" cy="-9" r="4.6" fill="FILL"/>' for dx in (-10,-3.4,3.4,10)]
    if dit: f=['<rect x="-6.8" y="-34" width="7" height="28" rx="3.5" fill="FILL"/>']+f
    f+=['<rect x="-15" y="1" width="21" height="7.5" rx="3.75" fill="FILL"/>']
    extra=L('M-6.7,-9v6M0,-9v6M6.7,-9v6',TINTA,1.4)+(f'<rect x="-5.8" y="-33" width="5" height="5" rx="2" fill="{AC}" opacity=".7"/>' if dit else '')
    return f'<g transform="translate({x} {y}) rotate({rot}) scale({e})" {ESTIL0}>'+contorn(f,c)+extra+'</g>'
def subst(id_,svg,anim=None,frase=None):
    for k,e in enumerate(E):
        if e[1]==id_:
            g,i,nom,fr,_,an=e; E[k]=(g,i,nom,frase or fr,svg,anim or an); return
    raise KeyError(id_)
NOU=set()
def s2(id_,svg,anim=None,frase=None): subst(id_,svg,anim,frase); NOU.add(id_)
ull_gran=lambda x,y,r=7,px=0,py=0: C(x,y,r,BL)+C(x+px,y+py,r*.48,TINTA)+C(x+px-r*.2,y+py-r*.25,r*.16,'#fff')

# --- Mans
s2('bravo',G('e-bota',cara(L('M32,54q7,-6 14,0M54,54q7,-6 14,0',TINTA,3)+P('M36,62h28c0,8 -6,12 -14,12s-14,-4 -14,-12Z',TINTA)))
  +G('e-ma-esq',ma2(30,88,30,.95))+G('e-ma-dre',ma2(70,88,-30,.95))+G('e-clap',L('M44,92l-4,-6M50,94v-8M56,92l4,-6',AC,2.6)))
s2('ei',cara(G('e-parpella',ulls())+L('M36,68q14,12 28,0',TINTA,3.4))+G('e-saluda',ma2(86,66,12,1.05),'style="transform-box:view-box;transform-origin:86px 80px"'))
s2('aimare',G('e-sacseja-lent',cara(C(64,56,3.4,TINTA)+L('M58,47l11,-2',TINTA,2.6)+L('M42,76q10,-5 20,0',TINTA,3.2))+ma2(38,52,-15,1.25)))
s2('calla',G('e-riu',cara(L('M32,52l7,-4l7,4M54,52l7,-4l7,4',TINTA,3)+C(28,64,5,R,'opacity=".35"')+C(72,64,5,R,'opacity=".35"'))+ma2(50,82,0,1.15)))
s2('niidea',G('e-arronsa',cara(L('M33,46l11,-2M56,44l11,2',TINTA,2.6)+ulls()+L('M40,74q5,-3 10,0t10,0',TINTA,3))+ma2(12,74,-70,.9)+ma2(88,74,70,.9)))
s2('por',G('e-tremola',cara(ull_gran(39,52,6.5)+ull_gran(61,52,6.5)+'<ellipse cx="50" cy="78" rx="6" ry="10" fill="'+TINTA+'"/>','#C8D8E8')
  +ma2(22,72,-12,.95,'#C8D8E8')+ma2(78,72,12,.95,'#C8D8E8')))
s2('son',G('e-badall',cara(L('M33,54q6,3 12,0M55,54q6,3 12,0',TINTA,3)+'<ellipse class="e-boca-badall" cx="50" cy="74" rx="8" ry="9" fill="'+TINTA+'"/>'))
  +G('e-ma-badall',ma2(64,82,-25,.85)))
s2('shh',cara(L('M33,54q6,3 12,0M55,54q6,3 12,0',TINTA,3)+'<ellipse cx="50" cy="74" rx="5" ry="3.4" fill="'+TINTA+'"/>')+G('e-xxt',puny(53,100,0,.95,dit=True))
  +G('e-ones',L('M80,62q4,4 0,8M86,58q6,8 0,16',AC,2.4)))
s2('pensant',G('e-inclina',cara(C(42,52,3.6,TINTA)+C(63,52,3.6,TINTA)+L('M33,43l11,-3M56,38q7,-4 13,1',TINTA,2.6)+L('M42,72l16,-3',TINTA,3.4))
  +puny(36,96,-28,.75,dit=True))+G('e-punts',C(84,40,2.5,AC,'style="--i:0"')+C(91,32,3.2,AC,'style="--i:1"')+C(96,22,4,AC,'style="--i:2"')))
s2('atope',G('e-crida',cara(L('M31,48l12,4M69,48l-12,4',TINTA,3)+ulls()+P('M36,66h28v4c0,6 -6,10 -14,10s-14,-4 -14,-10Z',TINTA)+L('M38,68h24',BL,2.4)))
  +G('e-biceps',P('M62,100C62,88 66,82 74,80C76,72 80,66 84,62','none',f'stroke="{TINTA}" stroke-width="17" stroke-linecap="round"')
     +P('M62,100C62,88 66,82 74,80C76,72 80,66 84,62','none',f'stroke="{A}" stroke-width="12" stroke-linecap="round"')
     +C(70,86,9,TINTA)+C(70,86,7.4,A)+puny(86,56,-15,.6),'style="transform-box:view-box;transform-origin:62px 100px"'))
s2('tupots',G('e-puny',puny(50,48,0,2.1)+f'<rect x="32" y="74" width="36" height="16" rx="4" fill="{R}" stroke="{TINTA}" stroke-width="2.4"/>'))

# --- Cares
s2('emociono',G('e-tremola-lent',cara(L('M30,44q7,-4 14,-1M56,43q7,-3 14,1',TINTA,2.6)
  +ull_gran(39,54,7.5,0,1)+ull_gran(61,54,7.5,0,1)+P('M31,60q8,5 16,0','none',f'stroke="{BLAU}" stroke-width="3" stroke-linecap="round"')+P('M53,60q8,5 16,0','none',f'stroke="{BLAU}" stroke-width="3" stroke-linecap="round"')
  +L('M42,74q4,-3 8,0t8,0',TINTA,3))+G('e-gota',P('M33,62c-3,5 -5,8 -5,10a5,5 0 0 0 10,0c0,-2 -2,-5 -5,-10Z',BLAU))))
s2('enamorat',G('e-flota',cara(G('e-cors',cor(38,50,1.3)+cor(62,50,1.3))+L('M34,70q16,13 32,0',TINTA,3.4))))
s2('fastic',G('e-arcada',cara(L('M32,52l9,4l-9,4M68,52l-9,4l9,4',TINTA,3)+P('M36,72q7,-6 14,0t14,0v2q-7,4 -14,0t-14,0Z',TINTA)
  +P('M44,74h12v10c0,5 -3,8 -6,8s-6,-3 -6,-8Z','#E26A8A')+L('M50,76v10','#B84868',1.4),'#9BC26A')))
s2('flipo',cara(G('e-estels2',estrella(38,53,12)+estrella(62,53,12))+P('M38,70h24c0,8 -5,12 -12,12s-12,-4 -12,-12Z',TINTA)))
FONC_FORMA='M17,60C17,40 32,25 50,25S83,40 83,60C83,70 86,76 92,82C96,86 94,92 86,92H14C6,92 4,86 8,82C14,76 17,70 17,60Z'
s2('fonc',G('e-fon',P(FONC_FORMA,'#F2A544')+L('M33,58q6,-3 12,0M55,58q6,-3 12,0',TINTA,3)+L('M38,74q12,6 24,0',TINTA,3.4)+BARR)
  +G('e-degota',P('M30,92c0,6 2,10 2,12a2,2 0 0 1 -4,0c0,-2 2,-6 2,-12Z','#F2A544')+P('M70,92c0,4 2,8 2,10a2,2 0 0 1 -4,0c0,-2 2,-6 2,-10Z','#F2A544')))
s2('gana',G('e-flota',cara(L('M32,54q7,-6 14,0M54,54q7,-6 14,0',TINTA,3)+L('M36,70q14,8 28,0',TINTA,3.4)
  +G('e-llepa',P('M56,72c4,0 8,2 8,6c0,3 -3,4 -5,3c-2,-1 -3,-4 -3,-9Z','#E26A8A'))))
  +G('e-baba2',L('M38,74V84',BLAU,2.4)+P('M38,82c-3,5 -5,8 -5,10a5,5 0 0 0 10,0c0,-2 -2,-5 -5,-10Z',BLAU)))
s2('malalt',G('e-tremola-lent',cara(L('M33,55q6,4 12,0M55,55q6,4 12,0',TINTA,3)+'<ellipse cx="39" cy="61" rx="6" ry="2" fill="#7A9A4A" opacity=".6"/><ellipse cx="61" cy="61" rx="6" ry="2" fill="#7A9A4A" opacity=".6"/>'
  +C(50,64,5,'#E25A4A')+L('M40,76q5,-3 10,0t10,0',TINTA,3)+G('e-moc',P('M52,68c0,4 1,7 1,8a2,2 0 0 1 -4,0c0,-1 1,-4 3,-8Z',BLAU)),'#B7CF7A',
  extra=P('M20,84c10,6 50,6 60,0l4,10c-14,6 -54,6 -68,0Z','#3E7AB8')+L('M30,88l4,6M40,89l3,6M60,89l-3,6M70,88l-4,6','#2E5A8A',2))))
s2('no',G('e-nega2',cara(L('M32,48l12,3M68,48l-12,3',TINTA,3)+ulls()+L('M38,75q12,-6 24,0',TINTA,3.6)))
  +G('e-moviment',L('M8,46q-5,10 0,20M14,40q-6,16 0,28M92,46q5,10 0,20M86,40q6,16 0,28',GRIS,2.6)))
s2('si',G('e-assent2',cara(L('M32,54q7,-6 14,0M54,54q7,-6 14,0',TINTA,3)+P('M36,66h28c0,8 -6,12 -14,12s-14,-4 -14,-12Z',TINTA)))
  +G('e-moviment',L('M40,98q10,4 20,0M36,96q14,6 28,0',GRIS,2.6)))
s2('peta',cara(ull_gran(39,56,8.5)+ull_gran(61,56,8.5)+'<ellipse cx="50" cy="78" rx="7" ry="6" fill="'+TINTA+'"/>',cap=False,
  extra=G('e-fum',''.join(C(x,y,r,'#C9CED6') for x,y,r in [(36,26,8),(50,20,10),(64,26,8),(44,14,6),(58,12,7)]))+G('e-barr-vola',BARR)))
s2('peto',cara(C(39,55,3.6,TINTA)+L('M55,55q6,-5 12,0',TINTA,3.2)+L('M50,66q7,1 3,5q7,1 2,6',TINTA,3)+C(30,66,4.5,R,'opacity=".35"')+C(70,66,4.5,R,'opacity=".35"'))
  +G('e-cor-vola',cor(62,64,.8)))
s2('riure',G('e-riu',cara(L('M31,52l8,-5l8,5M53,52l8,-5l8,5',TINTA,3.2)+P('M30,62h40c0,12 -9,20 -20,20s-20,-8 -20,-20Z',TINTA)+P('M40,76c3,-4 17,-4 20,0c-3,4 -17,4 -20,0Z',R)
  +G('e-llagrima2',P('M30,50C22,50 16,56 14,64','none',f'stroke="{BLAU}" stroke-width="5" stroke-linecap="round"')+P('M14,62c-3,5 -5,9 -5,11a5,5 0 0 0 10,0c0,-2 -2,-6 -5,-11Z',BLAU)
   +P('M70,50C78,50 84,56 86,64','none',f'stroke="{BLAU}" stroke-width="5" stroke-linecap="round"')+P('M86,62c-3,5 -5,9 -5,11a5,5 0 0 0 10,0c0,-2 -2,-6 -5,-11Z',BLAU)))))
s2('sorpres',G('e-salt',cara(ull_gran(39,53,7.5)+ull_gran(61,53,7.5)+L('M30,40q8,-6 16,-2M54,38q8,-4 16,2',TINTA,2.6)+'<ellipse cx="50" cy="76" rx="7" ry="9" fill="'+TINTA+'"/>')))
s2('trapella',cara(L('M31,50l13,4M69,50l-13,4',TINTA,3.2)+C(40,57,3.2,TINTA)+C(60,57,3.2,TINTA)+P('M34,68q16,14 32,0q-16,6 -32,0Z',TINTA),'#B06AD8',
  extra=G('e-banyes',P('M30,14c-6,-6 -8,-14 -6,-20c4,6 10,8 16,10c-2,4 -6,8 -10,10Z','#4A1A6A',f'stroke="{AC}" stroke-width="1.6"')+P('M66,12c4,-8 10,-14 18,-16c-2,8 -4,14 -10,20c-2,-2 -6,-4 -8,-4Z','#4A1A6A',f'stroke="{AC}" stroke-width="1.6"'))))
s2('ulleres',cara(P('M18,46h30c2,0 3,2 3,4c0,8 -6,14 -14,14h-4c-8,0 -15,-6 -15,-14c0,-2 0,-4 0,-4ZM52,46h30c0,0 0,2 0,4c0,8 -7,14 -15,14h-4c-8,0 -14,-6 -14,-14c0,-2 1,-4 3,-4Z','#14171C')
  +L('M48,49h4',"#14171C",3.4)+L('M40,74q12,6 22,-2',TINTA,3.4)+G('e-brill',L('M24,52l7,-4',BL,2.4))))
s2('ullet',G('e-inclina',cara(C(39,54,3.8,TINTA)+L('M55,55q6,-6 12,0',TINTA,3.4)+L('M54,46l12,-3',TINTA,2.4)+L('M36,68q14,12 28,0',TINTA,3.4))))
s2('ullsalcel',cara(C(39,55,8.5,BL)+C(61,55,8.5,BL)+G('e-giraull2',C(39,49,3.6,TINTA)+C(61,49,3.6,TINTA))+L('M42,76h18',TINTA,3.4)))
s2('vergonya',G('e-encongeix',cara(ull_gran(40,54,5.6,-1.5,2)+ull_gran(62,54,5.6,-1.5,2)+L('M33,45q6,-3 12,-1M55,44q6,-2 12,1',TINTA,2.4)
  +'<ellipse cx="28" cy="66" rx="9" ry="5" fill="#F05A6A" opacity=".75"/><ellipse cx="72" cy="66" rx="9" ry="5" fill="#F05A6A" opacity=".75"/>'
  +L('M44,76q3,-2 6,0t6,0',TINTA,2.6))+G('e-suor',P('M82,42c-3,5 -5,8 -5,10a5,5 0 0 0 10,0c0,-2 -2,-5 -5,-10Z',BLAU))))

# --- Coses
bota_p=lambda: (P('M30,18h26v44c10,2 26,6 32,14c3,4 2,10 -4,10H24c-4,0 -6,-3 -6,-7V30c0,-7 5,-12 12,-12Z',MARRO)
  +P('M18,80h70v6c0,3 -2,5 -5,5H23c-3,0 -5,-2 -5,-5Z','#2A2E35')+L('M34,30h18M34,40h18M34,50h18',A,3)+P('M30,18h26v8H30Z',MARROC))
s2('bota',G('e-pas-esq',f'<g transform="translate(-2 30) scale(.62)">{bota_p()}</g>')+G('e-pas-dre',f'<g transform="translate(40 30) scale(.62)">{bota_p()}</g>')
  +G('e-pols',C(30,92,4,'#8A7A6A')+C(36,94,3,'#8A7A6A')+C(74,92,4,'#8A7A6A'))+L('M4,94h92','#4A525F',2))
gerra=lambda: (P('M-14,-22h28v36c0,5 -4,8 -9,8h-10c-5,0 -9,-3 -9,-8Z','#C9E2EE','opacity=".35"')
  +P('M-12,-12h24v26c0,4 -3,6 -7,6h-10c-4,0 -7,-2 -7,-6Z','#E8A03A')+P('M14,-14c10,0 12,18 0,18','none','stroke="#E8F4FA" stroke-width="4.4"')
  +L('M-14,-22v36c0,5 4,8 9,8h10c5,0 9,-3 9,-8v-36',"#E8F4FA",2.4)
  +''.join(C(x,-22+dy,r,'#FFFFFF') for x,dy,r in [(-10,0,6),(-2,-3,7),(7,-1,6),(12,1,4)])+L('M-6,-4v12M2,-4v12',AC,1.6,'opacity=".6"'))
s2('brindis',G('e-esq',f'<g transform="translate(30 58) rotate(14)" {ESTIL0}>{gerra()}</g>')+G('e-dre',f'<g transform="translate(70 58) rotate(-14) scale(-1 1)" {ESTIL0}>{gerra()}</g>')
  +G('e-xoc',''.join(L(f'M{50+8*math.cos(math.radians(a))},{24+8*math.sin(math.radians(a))}L{50+14*math.cos(math.radians(a))},{24+14*math.sin(math.radians(a))}',AC,3) for a in (200,240,270,300,340)))
  +G('e-escuma',C(44,32,2.4,'#fff')+C(56,30,2,'#fff')+C(50,26,1.8,'#fff')),'brindis')
s2('gegant',G('e-gegant',P('M22,96L36,40h28l14,56Z','#2F5FA8')+P('M22,96h56l-2,-6H24Z',A)+P('M30,70h40','none','stroke="#21467F" stroke-width="2"')
  +P('M36,40c-6,4 -10,14 -12,26','none','stroke="#2F5FA8" stroke-width="7" stroke-linecap="round"')+P('M64,40c6,4 10,14 12,26','none','stroke="#2F5FA8" stroke-width="7" stroke-linecap="round"')
  +C(24,68,4,PELL)+C(76,68,4,PELL)+P('M36,40h28l-4,6h-20Z',BL)
  +C(50,26,13,PELL)+C(45,25,1.6,'#14171C')+C(55,25,1.6,'#14171C')+L('M45,31q5,3 10,0','#8A3A2A',1.6)
  +P('M37,22C36,8 46,0 56,2C64,3 70,7 69,14C69,18 66,19 63,18C62,16 61,15 60,14Z',R)+f'<rect x="36" y="19" width="28" height="5" rx="2" fill="{RF}"/>')
  +G('e-cames',L('M44,96v4M56,96v4','#2A2E35',4)),'gegant')
s2('petard',G('e-cano',P('M12,92L30,52L52,74Z','#9A3AB8')+P('M30,52L52,74','none',f'stroke="{A}" stroke-width="4"'))
  +''.join(G('e-confeti-surt',f'<rect x="38" y="60" width="4" height="7" rx="1" fill="{c}"/>',f'style="--i:{i};--dx:{dx}px;--dy:{dy}px"')
     for i,(dx,dy,c) in enumerate([(10,-40,A),(30,-34,R),(44,-14,BLAU),(20,-50,AC),(40,-44,VERD),(0,-48,R),(50,-30,A),(26,-20,BLAU),(16,-30,'#F07AA0'),(54,-46,AC),
                                   (36,-56,R),(6,-36,VERD),(46,-4,A),(58,-22,'#F07AA0'),(24,-44,BLAU),(34,-26,AC),(12,-56,A),(52,-52,R),(42,-38,VERD),(60,-8,BLAU)])))
s2('caganer',G('e-caganer',P('M40,44c-8,6 -12,16 -10,26h28c2,-10 -2,-20 -10,-26Z','#3B6E8A')
  +P('M30,70c-4,4 -6,10 -4,16h12l2,-12Z','#2A2E35')+P('M58,70c4,4 6,10 4,16H50l-2,-12Z','#2A2E35')
  +P('M26,66c-6,0 -10,4 -10,8s4,8 10,8s10,-4 10,-8s-4,-8 -10,-8Z',PELL)+L('M26,70v8',"#C99A7A",1.4)
  +P('M36,86h12M52,86h12','none','stroke="#5A3A24" stroke-width="5" stroke-linecap="round"')
  +P('M58,54c6,2 10,8 10,14','none',f'stroke="#3B6E8A" stroke-width="6" stroke-linecap="round"')+C(68,70,4,PELL)
  +C(50,32,11,PELL)+C(54,30,1.5,'#14171C')+L('M53,36q3,2 6,0','#8A3A2A',1.4)
  +P('M38,28C36,14 46,6 58,8C66,10 72,14 71,22C71,26 67,27 64,26C63,24 62,23 60,22Z',R)+f'<rect x="37" y="25" width="28" height="5" rx="2" fill="{RF}"/>')
  +P('M8,94c0,-4 3,-6 6,-6c1,-3 3,-4 5,-4c3,0 5,2 5,5c3,0 5,2 5,5Z',MARRO,'class="e-pila"'),'caganer')
s2('llibreta',f'<rect x="16" y="10" width="54" height="80" rx="4" fill="#F4EDE0"/>'+''.join(L(f'M24,{y}h38','#C9B89A',2) for y in (30,42,54,66,78))+L('M16,10v80',R,4)
  +G('e-escrit',L('M24,30h30',"#3A4558",2.6),'style="transform-box:view-box;transform-origin:24px 30px"')
  +G('e-llapis',f'<g transform="translate(64 50) rotate(35)" {ESTIL0}>'+f'<rect x="-5" y="-38" width="10" height="44" fill="{A}"/><rect x="-5" y="-38" width="3.4" height="44" fill="#D89A2A"/>'
     +f'<rect x="-5" y="-46" width="10" height="7" rx="2" fill="#F07AA0"/><rect x="-5.6" y="-40" width="11.2" height="4" fill="{FE}"/>'
     +P('M-5,6h10l-5,12Z','#E8C49A')+P('M-2,12h4l-2,6Z','#3A4558')+'</g>'),'llapis')
s2('barretina',G('e-barretina',P('M20,74C18,46 30,22 54,18C70,15 80,22 78,32C76,40 66,40 62,44C58,48 62,56 66,60C70,64 70,72 64,74Z',R)
  +P('M62,44C58,48 62,56 66,60C70,64 70,72 64,74','none',f'stroke="{RF}" stroke-width="2.4"')
  +f'<rect x="18" y="70" width="52" height="12" rx="5" fill="{RF}"/>'+L('M24,74h40',"#7A1418",1.4)
  +''.join(L(f'M{x},68C{x+2},50 {x+8},34 {x+18},26','none' and R,1) for x in ()) +P('M30,62C32,44 40,30 54,26','none','stroke="#E8343A" stroke-width="2.4" stroke-linecap="round" opacity=".6"')))
s2('correfoc',G('e-balla',P('M36,96L42,50h16l6,46Z','#2A2E35')+L('M44,58l-12,-14M56,58l14,-20','#2A2E35',6)
  +C(50,40,10,PELL)+C(46,40,1.6,'#14171C')+C(54,40,1.6,'#14171C')+P('M45,45q5,4 10,0','#8A3A2A')
  +P('M38,36C36,22 46,14 58,16C66,18 72,22 71,30C71,34 67,35 64,34C63,32 62,31 60,30Z',R)+f'<rect x="37" y="33" width="28" height="5" rx="2" fill="{RF}"/>'
  +P('M40,26c-4,-6 -4,-12 -2,-16c2,6 6,8 8,10Z','#7A1418')+P('M60,22c2,-6 6,-10 10,-12c0,6 -2,10 -6,14Z','#7A1418'))
  +L('M70,38L82,8',MARRO,3.4)+G('e-roda',''.join(L(f'M82,8L{82+18*math.cos(math.radians(a))},{8+18*math.sin(math.radians(a))}',[A,AC,R][i%3],2.6) for i,a in enumerate(range(0,360,30))),'style="transform-box:view-box;transform-origin:82px 8px"')
  +''.join(G('e-espurna-cau',C(x,20,1.8,AC),f'style="--i:{i}"') for i,x in enumerate((70,80,90,64,94))),'correfoc')
s2('cartell',L('M30,94V50M70,94V50',FE,4)+f'<rect x="12" y="22" width="76" height="34" rx="4" fill="#F4EDE0" stroke="{R}" stroke-width="4"/>'
  +f'<rect x="22" y="34" width="56" height="5" rx="2" fill="#2A2E35"/><rect x="30" y="43" width="40" height="4" rx="2" fill="#6A7484"/>'
  +G('e-segell',C(80,24,14,'#3E9A4A')+L('M73,24l5,5l9,-10',BL,4)),'segell')
cama=lambda: (P('M-6,-40h12v34h-12Z',PELL)+P('M-8,-6h22c6,0 10,4 10,8v4h-32Z','#F4EDE0')+P('M-8,6h32v4h-32Z','#C9A06A')
  +L('M-6,-4L6,-16M6,-4L-6,-16M-6,-16L6,-28M6,-16L-6,-28','#14171C',2))
s2('espardenya',G('e-sardana-esq',f'<g transform="translate(34 74)" {ESTIL0}>{cama()}</g>')+G('e-sardana-dre',f'<g transform="translate(62 74)" {ESTIL0}>{cama()}</g>')
  +L('M10,88h80','#4A525F',2)+G('e-nota',nota(86,30,AC),'style="--i:0"'),'espardenya')

# --- Fora els que no t'agraden
FORA={'endavant','pasapas','burleta','ressaca','prismatics','rellotge','carnestoltes','gralla','mona','oucombal','pessebre','esquis','allioli','botifarra','castanyes','crema','escudella','fuet','panellets','tortell','xocolata','olivera','verema','cremallera'}
E[:]=[e for e in E if e[1] not in FORA]
SENSE_NOTA={'castell','cim','fred','traca','cafe','calcot','ninot','pa','vermut','bastons','cava','drac','tio','cotxe','rodalies','cascada','llaut','avio','bus','moto','tractor'}

CSS+=r'''
.anim .e-nega2{animation:enega2 .25s ease-in-out infinite alternate}@keyframes enega2{from{transform:translateX(-9px) rotate(-10deg)}to{transform:translateX(9px) rotate(10deg)}}
.anim .e-moviment{animation:eclap .5s steps(1) infinite}
.anim .e-assent2{transform-origin:50% 100%;animation:eass2 .3s ease-in-out infinite alternate}@keyframes eass2{from{transform:translateY(-7px) scaleY(1.03)}to{transform:translateY(5px) scaleY(.9)}}
.anim .e-llagrima2{animation:ellag2 .5s ease-in-out infinite alternate}@keyframes ellag2{from{transform:scale(.9)}to{transform:scale(1.1) translateY(2px)}}
.anim .e-giraull2{transform-box:view-box;animation:egir2 2s ease-in-out infinite}@keyframes egir2{0%,100%{transform:translateX(-3px)}50%{transform:translateX(3px)}}
.anim .e-degota{animation:edeg 1.6s ease-in infinite}@keyframes edeg{0%{transform:translateY(-4px) scaleY(.5)}80%{transform:translateY(4px) scaleY(1.2)}100%{opacity:0}}
.anim .e-baba2{transform-box:view-box;transform-origin:38px 74px;animation:ebaba2 2s ease-in-out infinite}@keyframes ebaba2{0%,100%{transform:scaleY(.6)}60%{transform:scaleY(1.15)}}
.anim .e-moc{animation:egota 1.8s ease-in infinite}
.anim .e-pas-esq{animation:epasx 1s ease-in-out infinite}.anim .e-pas-dre{animation:epasx 1s ease-in-out infinite;animation-delay:-.5s}
@keyframes epasx{0%,100%{transform:translate(0,0) rotate(0)}25%{transform:translate(4px,-10px) rotate(-10deg)}50%{transform:translate(8px,0) rotate(0)}}
.anim .e-pols{opacity:0;animation:evap 1s ease-out infinite}
.anim .e-escuma{opacity:0;animation:econf2 1.6s ease-out infinite}@keyframes econf2{0%,40%{opacity:0;transform:translateY(4px)}50%{opacity:1}100%{opacity:0;transform:translateY(-12px)}}
.anim .e-cames{animation:etamb .3s ease-in-out infinite alternate}
.anim .e-escrit{animation:eescrit 1.6s ease-out infinite}@keyframes eescrit{0%{transform:scaleX(0)}70%,100%{transform:scaleX(1)}}
.anim .e-espurna-cau{opacity:0;animation:ecau 1s ease-in infinite;animation-delay:calc(var(--i)*.2s)}@keyframes ecau{0%{opacity:0;transform:translateY(0)}20%{opacity:1}100%{opacity:0;transform:translateY(40px)}}
.anim .e-segell{animation:eseg 2s cubic-bezier(.3,1.6,.5,1) infinite}@keyframes eseg{0%,20%{transform:scale(2.2);opacity:0}35%,100%{transform:scale(1);opacity:1}}
.e-segell{transform-origin:center}
.anim .e-sardana-esq{animation:esard 1.2s ease-in-out infinite}.anim .e-sardana-dre{animation:esard 1.2s ease-in-out infinite;animation-delay:-.6s}
@keyframes esard{0%,100%{transform:translateY(0)}20%{transform:translateY(-10px)}40%{transform:translateY(0)}}
'''

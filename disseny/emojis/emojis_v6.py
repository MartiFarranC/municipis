# Cinquena ronda: correccions de la quarta valoració.
NOU6=set()
def s6(id_,svg,anim=None): subst(id_,svg,anim); NOU6.add(id_)
cf=_encongeix_final  # les cares s'encongeixen igual que les altres perquè hi càpiga la barretina
def cara_sota(sota,trets,fons=A,extra=''):
    # Cara amb coses darrere del cap (com les banyes), que han de quedar tapades per la cara.
    return sota+f'<circle cx="50" cy="58" r="33" fill="{fons}"/>'+trets+BARR+extra
gota=lambda x,y,s=1: P(f'M{x},{y}c{-2*s},{3.2*s} {-3.2*s},{5*s} {-3.2*s},{6.6*s}a{3.2*s},{3.2*s} 0 0 0 {6.4*s},0c0,{-1.6*s} {-1.2*s},{-3.4*s} {-3.2*s},{-6.6*s}Z',BLAU)

# M'encanta: els cors baixen perquè la barretina no els tapi.
s6('enamorat',cf(G('e-flota',cara(G('e-cors',cor(39,58,1.05)+cor(61,58,1.05))+L('M37,72q13,11 26,0',TINTA,3.4)))))
# Em pixo de riure: llàgrimes petites a la punta dels ulls.
s6('riure',cf(G('e-riu',cara(L('M31,55l8,-5l8,5M53,55l8,-5l8,5',TINTA,3.2)+P('M31,64h38c0,11 -8,18 -19,18s-19,-7 -19,-18Z',TINTA)+P('M41,77c3,-3 15,-3 18,0c-3,3 -15,3 -18,0Z',R)
  +G('e-llagrima2',gota(27,55,.8)+gota(73,55,.8))))))
# Quin fred!: cara blavosa però simpàtica, que tremola i bat les dents; bufanda i flocs de neu.
s6('fred',cf(G('e-tremola',cara(L('M33,48q6,-3 11,0M56,48q5,-3 11,0',TINTA,2.6)+ulls()+C(50,63,3.2,'#E8707A')
  +C(31,66,4.5,'#F2A0B0','opacity=".6"')+C(69,66,4.5,'#F2A0B0','opacity=".6"')
  +L('M38,74l3,-3l3,3l3,-3l3,3l3,-3l3,3l3,-3l3,3',TINTA,2.6),'#BFE3F5',
  extra=o('M22,86c10,6 46,6 56,0l2,8c-12,6 -48,6 -60,0Z',R,2)+o('M64,90l5,10h-9Z',R,1.6)+L('M30,90v4M40,92v4M50,92v4M60,92v4',RF,1.4)))
  +''.join(G('e-floc',P(f'M{x},{y-4}v8M{x-3.5},{y-2}l7,4M{x-3.5},{y+2}l7,-4','none',f'stroke="{BL}" stroke-width="1.6" stroke-linecap="round"'),f'style="--i:{i}"') for i,(x,y) in enumerate([(6,22),(94,30),(6,62),(94,72)]))),'fred')
# Quina trapelleria: dues banyes iguals i simètriques que surten dels costats del cap.
BANYA='M6,-8Q-14,-4 -10,-24Q6,-20 6,8Z'
banyes=(f'<g transform="translate(22 52) scale(1.1)" {ESTIL0}>'+o(BANYA,'#7A2A9A',1.4)+L('M1,-4Q-6,-8 -7,-18','#C08AE0',1.4)+'</g>'
        +f'<g transform="translate(78 52) scale(-1.1 1.1)" {ESTIL0}>'+o(BANYA,'#7A2A9A',1.4)+L('M1,-4Q-6,-8 -7,-18','#C08AE0',1.4)+'</g>')
s6('trapella',cf(G('e-banyes',cara_sota(banyes,L('M31,51l13,4M69,51l-13,4',TINTA,3.2)+C(40,58,3.2,TINTA)+C(60,58,3.2,TINTA)+P('M35,69q15,13 30,0q-15,6 -30,0Z',TINTA),'#B06AD8'))))

# Brindem: botellins com els de la referència (vidre daurat, etiqueta del coll vermella fins a l'espatlla i etiqueta del cos a baix), sense cap nom.
def botelli():
    estel=lambda x,y,r: f'M{x},{y-r}L{x+r*.29},{y-r*.4}L{x+r*.95},{y-r*.31}L{x+r*.47},{y+r*.15}L{x+r*.59},{y+r*.81}L{x},{y+r*.5}L{x-r*.59},{y+r*.81}L{x-r*.47},{y+r*.15}L{x-r*.95},{y-r*.31}L{x-r*.29},{y-r*.4}Z'
    cos='M-4.5,-36H4.5V-17C4.5,-11 11,-9 11,-1V32C11,35 9,37 6,37H-6C-9,37 -11,35 -11,32V-1C-11,-9 -4.5,-11 -4.5,-17Z'
    return (o(cos,'#E2A12E',2.2)
      +P('M5,-4C8,-3 9,0 9,3V32C9,34 8,35 6,35H4Z','#C4841E')
      +L('M-7,0V30','#F6D27A',2.4,'opacity=".9"')
      +o('M-4.5,-35H4.5V-17C4.5,-13 6.5,-11 8.5,-8H-8.5C-6.5,-11 -4.5,-13 -4.5,-17Z',R,1.4)+P(estel(0,-22,3.2),'#F4E4C8')
      +o('M-5.5,-40H5.5V-35H-5.5Z','#B81A22',1.4)
      +o('M-11,8H11V28H-11Z',R,1.6)+f'<rect x="-9" y="10" width="18" height="16" fill="none" stroke="#F4E4C8" stroke-width=".7"/>'
      +P(estel(0,14.5,3.4),'#F4E4C8')+L('M-6,20.5h12M-5,23h10','#F4E4C8',1,'opacity=".8"'))
s6('brindis',G('e-esq',f'<g transform="translate(33 58) rotate(19)" {ESTIL0}>{botelli()}</g>')+G('e-dre',f'<g transform="translate(67 58) rotate(-19)" {ESTIL0}>{botelli()}</g>')
  +G('e-xoc',''.join(L(f'M{50+7*math.cos(math.radians(a)):.1f},{17+7*math.sin(math.radians(a)):.1f}L{50+12*math.cos(math.radians(a)):.1f},{17+12*math.sin(math.radians(a)):.1f}',AC,2.6) for a in (200,240,270,300,340)))
  +G('e-escuma',C(45,14,2.4,'#fff')+C(55,12,2,'#fff')+C(50,8,1.8,'#fff')),'brindis')

# Quina festa!: focs artificials de debò. Cada coet puja deixant una estela i esclata en raigs que cauen corbats, sobre les teulades del poble.
def palmera(x,y,r,c,i,n=14):
    raigs=''
    for k in range(n):
        a=math.radians(k*360/n+8)
        x0,y0=x+r*.2*math.cos(a),y+r*.2*math.sin(a)
        x1,y1=x+r*math.cos(a),y+r*math.sin(a)+r*.28
        cx,cy=x+r*.75*math.cos(a),y+r*.75*math.sin(a)-r*.05
        raigs+=L(f'M{x0:.1f},{y0:.1f}Q{cx:.1f},{cy:.1f} {x1:.1f},{y1:.1f}',c,1.8)+C(x1,y1,1.5,AC)
    return G('e-esclat',C(x,y,r*.22,AC,'opacity=".9"')+raigs,f'style="--i:{i};transform-box:view-box;transform-origin:{x}px {y}px"')
estela=lambda x0,y0,x1,y1: L(f'M{x0},{y0}Q{(x0+x1)/2+3},{(y0+y1)/2} {x1},{y1}',AC,1.4,'stroke-dasharray="1.5 3" opacity=".7"')
s6('traca',f'<rect x="0" y="0" width="100" height="100" rx="18" fill="#141A2E"/>'
  +''.join(C(x,y,.8,'#fff','opacity=".6"') for x,y in [(12,10),(48,8),(88,12),(8,44),(92,46),(46,48)])
  +estela(30,86,30,44)+estela(70,86,72,38)+estela(52,86,50,62)
  +palmera(30,34,19,R,0)+palmera(72,28,17,BLAU,1)+palmera(50,56,13,'#F07AA0',2,12)
  +P('M0,100V86h8v-6l6,-5l6,5v6h8v-8l7,-6l7,6v8h6v-4h10v4h8v-10l6,-5l6,5v10h8v-6l6,-4l6,4v6h8V100Z','#0A0E1A')
  +''.join(f'<rect x="{x}" y="{y}" width="2" height="2.4" fill="{A}" opacity=".7"/>' for x,y in [(12,88),(36,86),(66,84),(84,90)]),'traca')

# Neva!: ninot amb dues boles ben rodones, la barretina al cap, bufanda, nas de pastanaga i braços de branca.
s6('ninot','<ellipse cx="50" cy="93" rx="40" ry="6" fill="#E8F1F8" opacity=".9"/>'
  +L('M32,66L14,52M18,55l-6,-1M18,55l-2,-6M68,66L86,52M82,55l6,-1M82,55l2,-6','#6A4426',2.6)
  +oc(50,72,21,'#F4F8FB')+oc(50,40,14,'#F4F8FB')
  +C(45,40,1.8,K)+C(55,40,1.8,K)+o('M50,44l11,2.5l-11,2.5Z','#E07A2C',1.2)
  +''.join(C(x,y,1.1,K) for x,y in [(44,48),(47,49.5),(50,50),(53,49.5),(56,48)])
  +''.join(C(50,y,2,K) for y in (64,72,80))
  +o('M37,52c4,5 22,5 26,0l1,5c-6,5 -22,5 -28,0Z',R,1.6)+o('M56,56l5,11h-7Z',R,1.4)
  +barr_mini(50,40,14)
  +''.join(G('e-floc',C(x,y,1.8,BL),f'style="--i:{i}"') for i,(x,y) in enumerate([(12,14),(88,22),(14,38),(88,44),(8,72),(92,76)])),'ninot')

# Bon profit: llesca de pa de pagès fregada amb tomàquet (vermell clar, amb llavors), oli i sal, i mig tomàquet al costat.
s6('pa',f'<g transform="rotate(-8 46 52)" {ESTIL0}>'
  +o('M12,46C12,26 28,16 48,16S84,26 84,46C84,52 80,56 78,58V78C78,84 74,86 68,86H26C20,86 16,84 16,78V58C14,56 12,52 12,46Z','#9A5B2A')
  +P('M19,46C19,32 32,23 48,23S77,32 77,46C77,50 74,53 72,55V77C72,79 70,80 68,80H26C24,80 22,79 22,77V55C20,53 19,50 19,46Z','#EBC98E')
  +''.join(f'<ellipse cx="{x}" cy="{y}" rx="{rx}" ry="{ry}" fill="#C99A5A"/>' for x,y,rx,ry in [(34,38,2.4,1.6),(58,34,2,1.4),(66,52,2.6,1.8),(30,66,2,1.4),(52,72,2.4,1.6),(44,54,1.6,1.1)])
  +P('M24,44C30,32 46,28 56,30S74,36 72,48C70,58 72,66 66,74S44,78 34,74S22,62 24,52Z','#E0452A','opacity=".62"')
  +''.join(P(f'M{x},{y}c2,-1 4,0 4,1.4c-1,1.4 -3,1.4 -4,-1.4Z','#F2C14E') for x,y in [(36,44),(52,40),(60,58),(42,64),(30,56)])
  +''.join(C(x,y,1.6,'#B5281A','opacity=".7"') for x,y in [(46,48),(56,66),(38,58),(64,44)])
  +L('M30,40q10,-6 22,-4M60,68q4,-4 4,-10','#FFF6D8',1.8,'opacity=".55"')
  +''.join(f'<rect x="{x}" y="{y}" width="1.6" height="1.6" fill="#fff" transform="rotate(30 {x} {y})"/>' for x,y in [(40,36),(48,58),(58,50),(34,70),(66,62)])
  +'</g>'
  +oc(82,82,13,'#D7262B',2)+C(82,82,9.5,'#F06A50')+''.join(f'<ellipse cx="{82+5.5*math.cos(math.radians(a)):.1f}" cy="{82+5.5*math.sin(math.radians(a)):.1f}" rx="2.6" ry="1.6" fill="#F7C35A" transform="rotate({a} {82+5.5*math.cos(math.radians(a)):.1f} {82+5.5*math.sin(math.radians(a)):.1f})"/>' for a in (0,90,180,270))
  +C(82,82,2.2,'#F7A090')+o('M80,70l2,-5l3,5Z',VERD,1)
  +G('e-degota',P('M50,4c-2,3 -3,5 -3,6.4a3,3 0 0 0 6,0c0,-1.4 -1,-3.4 -3,-6.4Z','#D9B73A')),'pa')

# Quin drac!: drac de Sant Jordi de perfil, amb ales de ratpenat, banyes, crestes a l'esquena, cua de punta de fletxa i foc per la boca.
VD='#4E9A4E'; VDF='#2F6A34'; PANXA='#E8D27A'
s6('drac',G('e-drac',
  o('M30,64C16,66 8,74 10,86C14,78 22,74 32,74Z',VD,2)+o('M5,82l5,11l7,-8Z',VDF,1.6)
  +o('M54,54L44,28L16,22Q26,30 22,40Q32,42 32,50Q42,48 54,54Z',VDF,2)+L('M44,28L22,40M44,28L32,50','#7FC27A',1.4)+L('M54,54L44,28L16,22',VD,3)
  +o('M34,80C24,78 22,68 28,60C34,52 50,50 58,56L60,74C56,80 46,82 34,80Z',VD,2)
  +o('M54,60C56,50 60,42 66,34L76,40C70,46 66,54 64,64Z',VD,2)
  +P('M36,78C46,80 56,76 60,70L62,64C58,72 48,76 36,74Z',PANXA)+P('M58,64L66,44L70,46L62,66Z',PANXA)
  +o('M62,30C66,22 76,22 82,26L92,30C95,32 94,37 90,38L78,40C72,42 64,40 62,34Z',VD,2)
  +o('M66,25L58,14L70,22Z',AC,1.4)+o('M72,23L68,11L77,22Z',AC,1.4)
  +''.join(o(f'M{x},{y}l{-2},-6l6,3Z',AC,1) for x,y in [(60,44),(56,52),(50,52),(42,52)])
  +oc(73,29,3.6,BL,1.4)+C(74,29,1.9,K)+C(74.6,28.4,.6,'#fff')+C(89,32,1,K)+L('M80,37l8,-1',K,1.2)
  +o('M34,78v10h6v-8M50,78v10h6v-10',VD,1.8)+L('M33,90h8M49,90h8',K,1.6))
  +G('e-foc-drac',o('M93,34c3,-4 6,-4 7,-6c0,4 -1,7 -3,9c2,0 3,1 3,3c-3,2 -6,1 -8,-1Z',A,1.2)),'drac')

# Bona caminada: una sola bota de muntanya ben dibuixada, que fa passes; sense cap línia que la travessi.
bota6=(o('M34,20H58C60,20 61,22 61,24V44C70,48 84,52 88,60C90,64 89,70 84,70H30C27,70 25,68 25,64V29C25,24 29,20 34,20Z',MARRO,2.2)
  +o('M62,46C70,50 82,53 86,60C87,63 86,66 84,66H70C66,60 64,52 62,46Z',MARROC,1.4)
  +o('M31,16H59C61,16 62,18 62,20V25H28V20C28,18 29,16 31,16Z','#6A4426',1.8)
  +o('M22,68H91V75C91,78 89,80 86,80H27C24,80 22,78 22,75Z','#2A2E35',2)+L('M30,80v-3M40,80v-3M50,80v-3M60,80v-3M70,80v-3M80,80v-3','#5A5E66',1.6)
  +L('M50,30l10,4M50,36l10,-4M50,38l11,4M50,44l11,-4M54,46l10,4',A,1.8)+''.join(C(x,y,1.2,'#C9CED6') for x,y in [(50,30),(50,36),(50,38),(50,44),(61,34),(61,42)]))
s6('bota','<ellipse cx="56" cy="88" rx="34" ry="4" fill="#000" opacity=".25"/>'
  +G('e-pas',f'<g transform="translate(-4 6)" {ESTIL0}>{bota6}</g>')
  +G('e-pols',C(16,82,4,'#8A7A6A')+C(10,78,3,'#8A7A6A')+C(20,76,2.4,'#8A7A6A')),'bota')

# Primer, un cafè: no agrada, fora.
E[:]=[e for e in E if e[1]!='cafe']

# --- Correccions de la cinquena valoració
# Quina trapelleria: banyes de dimoni, primes i punxegudes, que pugen rectes i tomben la punta cap endins (no semblen orelles).
BANYA2='M-4.5,0C-4.5,-9 -2,-18 5,-24C2,-16 3,-8 4.5,0Z'
banya2=lambda x,s: (f'<g transform="translate({x} 46) scale({s} 1)" {ESTIL0}>'+o(BANYA2,'#8E3BB5',1.6)+L('M-1.5,-3C-1.5,-10 0,-15 3,-19','#D3A6EC',1.2)+'</g>')
s6('trapella',cf(G('e-banyes',f'<circle cx="50" cy="58" r="33" fill="#B06AD8"/>'
  +L('M31,51l13,4M69,51l-13,4',TINTA,3.2)+C(40,58,3.2,TINTA)+C(60,58,3.2,TINTA)+P('M35,69q15,13 30,0q-15,6 -30,0Z',TINTA)
  +BARR+banya2(19,1)+banya2(81,-1))))
# Bon profit: només la llesca de pa de pagès (rodona i irregular, crosta fosca enfarinada, molla amb forats grossos) fregada amb tomàquet.
forat=lambda x,y,rx,ry,a: f'<ellipse cx="{x}" cy="{y}" rx="{rx}" ry="{ry}" fill="#C9984E" transform="rotate({a} {x} {y})" {ESTIL0}/>'
s6('pa',f'<g transform="rotate(-8 50 52)" {ESTIL0}>'
  +o('M6,54C5,38 20,26 38,25C44,22 56,22 62,25C82,26 96,38 95,54C94,70 80,80 60,81C54,83 44,83 38,81C18,80 6,70 6,54Z','#7E461E',2.4)
  +''.join(C(x,y,1.1,'#EADCC0','opacity=".75"') for x,y in [(12,44),(22,33),(36,28),(50,25),(66,28),(80,33),(90,46),(89,62),(78,74),(62,79),(46,80),(30,77),(16,68),(10,56)])
  +P('M14,54C14,41 26,32 40,32C46,29 56,29 62,32C78,33 88,42 88,54C88,66 76,74 60,74C54,76 46,76 40,74C24,74 14,66 14,54Z','#EED3A0')
  +''.join(forat(*f) for f in [(28,46,4,2.2,15),(46,40,3,1.8,-10),(66,44,4.6,2.4,20),(76,56,3,1.8,-15),(24,60,3,1.8,-20),(40,64,4.2,2.2,10),(58,62,2.6,1.6,-25),(52,50,2.2,1.4,0),(34,54,1.8,1.2,0),(68,68,2.2,1.4,0),(82,48,1.8,1.2,0)])
  +P('M20,50C24,42 34,38 44,40C52,36 64,36 72,42C80,46 82,54 78,60C74,66 64,64 58,68C50,72 38,70 30,66C22,62 18,56 20,50Z','#E2553A','opacity=".42"')
  +P('M30,46C38,42 48,44 56,42C62,46 70,48 72,54C66,58 56,56 48,60C40,62 32,58 30,52Z','#D8402A','opacity=".35"')
  +''.join(P(f'M{x},{y}c1.6,-.8 3.2,0 3.2,1.1c-.8,1.1 -2.4,1.1 -3.2,-1.1Z','#F2C14E','opacity=".9"') for x,y in [(36,48),(54,44),(64,56),(44,60),(28,54)])
  +'</g>','pa')

# --- Correccions de la sisena valoració
# Quin fred!: la cara groga de sempre (res de blau), ulls tancats arrufats, galtes i nas vermells, i la bufanda fins a la barbeta.
s6('fred',cf(G('e-tremola',cara(L('M33,54l8,3l-8,3M67,54l-8,3l8,3',TINTA,3)+C(50,64,3.4,'#E8505A')
  +C(30,66,5,'#F07080','opacity=".55"')+C(70,66,5,'#F07080','opacity=".55"')
  +L('M43,75q2.3,-2.5 4.6,0t4.6,0t4.6,0',TINTA,2.6),
  extra=o('M22,82c10,7 46,7 56,0l2,9c-12,7 -48,7 -60,0Z',R,2)+o('M64,88l5,11h-9Z',R,1.6)+L('M30,88v4M40,90v4M50,90v4M60,90v4',RF,1.4)))
  +''.join(G('e-floc',P(f'M{x},{y-4}v8M{x-3.5},{y-2}l7,4M{x-3.5},{y+2}l7,-4','none',f'stroke="{BL}" stroke-width="1.6" stroke-linecap="round"'),f'style="--i:{i}"') for i,(x,y) in enumerate([(6,22),(94,30),(6,62),(94,72)]))),'fred')
# Quina trapelleria: les banyes surten del cap seguint-ne la corba (en la direcció del radi), i travessen la barretina.
def banya3(ang,s):
    x=50+33*math.cos(math.radians(ang)); y=58+33*math.sin(math.radians(ang)); rot=ang+90
    return f'<g transform="translate({x:.1f} {y:.1f}) rotate({rot}) scale({s} 1)" {ESTIL0}>'+o(BANYA2,'#8E3BB5',1.6)+L('M-1.5,-3C-1.5,-10 0,-15 3,-19','#D3A6EC',1.2)+'</g>'
s6('trapella',cf(G('e-banyes',f'<circle cx="50" cy="58" r="33" fill="#B06AD8"/>'
  +L('M31,51l13,4M69,51l-13,4',TINTA,3.2)+C(40,58,3.2,TINTA)+C(60,58,3.2,TINTA)+P('M35,69q15,13 30,0q-15,6 -30,0Z',TINTA)
  +BARR+banya3(-128,1)+banya3(-52,-1))))
# Bon profit: el pa de pagès sencer al darrere (rodó, enfarinat, amb el tall en creu) i una llesca fregada amb tomàquet al davant.
s6('pa',o('M44,36C44,20 56,10 70,10S96,20 96,36C96,50 84,58 70,58S44,50 44,36Z','#A0602A',2.4)
  +P('M50,26C54,18 62,14 70,14','none','stroke="#C8884A" stroke-width="3" stroke-linecap="round" opacity=".8"')
  +L('M60,24L80,44M80,24L60,44','#E6C48A',3)+L('M60,24L80,44M80,24L60,44','#7A4418',1,'opacity=".5"')
  +''.join(C(x,y,1.2,'#F4ECDA','opacity=".9"') for x,y in [(54,20),(64,14),(80,16),(88,26),(50,34),(92,36),(66,32),(76,50),(58,48),(86,46)])
  +f'<g transform="rotate(-12 40 66)" {ESTIL0}>'
  +o('M6,66C6,50 20,40 40,40S76,50 76,66S62,90 40,90S6,82 6,66Z','#7E461E',2.4)
  +P('M12,66C12,53 24,46 40,46S70,53 70,66S58,84 40,84S12,79 12,66Z','#EED3A0')
  +''.join(forat(*f) for f in [(26,58,3.4,2,15),(42,54,2.6,1.6,-10),(56,60,3.6,2,20),(30,72,2.8,1.6,-20),(46,76,3.4,1.8,10),(60,72,2.2,1.4,-25),(40,66,1.8,1.2,0)])
  +P('M18,64C20,54 32,50 42,51S62,54 64,64C64,74 54,80 42,80S18,74 18,64Z','#D63A26','opacity=".62"')
  +''.join(P(f'M{x},{y}c1.6,-.8 3.2,0 3.2,1.1c-.8,1.1 -2.4,1.1 -3.2,-1.1Z','#F2C14E','opacity=".9"') for x,y in [(30,62),(46,58),(54,68),(36,74)])
  +'</g>','pa')

# --- Correcció de la setena valoració
# Bon profit: una sola llesca grossa de pa de pagès (tall d'un pa rodó: base plana i dalt en cúpula), torrada amb les ratlles
# de la graella, i ben fregada de tomàquet vermell amb polpa i llavors, amb una mica de brillantor d'oli.
s6('pa',f'<g transform="rotate(-6 50 56)" {ESTIL0}>'
  +o('M6,74C4,46 22,22 50,22S96,46 94,74C94,80 90,84 84,84H16C10,84 6,80 6,74Z','#8A4A1C',2.6)
  +P('M8,78C9,82 12,84 16,84H84C88,84 91,82 92,78Z','#B8783E')
  +P('M14,72C13,50 28,30 50,30S87,50 86,72C86,75 84,77 81,77H19C16,77 14,75 14,72Z','#E8C88E')
  +''.join(L(f'M{x},34L{x-14},76','#B98A4C',3,'opacity=".55"') for x in (40,58,76))
  +''.join(forat(*f) for f in [(30,52,3,1.8,10),(50,44,2.4,1.4,-10),(66,54,3.2,1.8,20),(36,66,2.6,1.6,-15),(58,68,3,1.6,10),(76,66,2,1.3,0)])
  +P('M18,68C17,52 30,36 50,36S83,52 82,68C82,72 78,73 74,72C66,74 60,70 52,73C44,76 36,72 28,73C22,74 18,72 18,68Z','#D7301E','opacity=".78"')
  +''.join(P(f'M{x},{y}c2,-3 6,-3 8,0c-2,3 -6,3 -8,0Z','#A81E14','opacity=".7"') for x,y in [(30,56),(48,48),(62,60),(40,66),(70,50)])
  +''.join(P(f'M{x},{y}c1.6,-.8 3.2,0 3.2,1.1c-.8,1.1 -2.4,1.1 -3.2,-1.1Z','#F6D46A') for x,y in [(36,52),(54,44),(64,54),(46,62),(28,64),(72,62),(56,66)])
  +L('M30,44q12,-8 26,-6',"#FFF4D6",2.4,'opacity=".55"')+L('M68,46q6,4 8,10',"#FFF4D6",1.8,'opacity=".45"')
  +'</g>','pa')

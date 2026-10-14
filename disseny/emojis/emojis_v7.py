# Revisió final: correccions de la revisió de tots els emojis aprovats.
NOU7=set()
def s7(id_,svg,anim=None): subst(id_,svg,anim); NOU7.add(id_)
ORIG=lambda x,y: f'style="transform-box:view-box;transform-origin:{x}px {y}px"'

# Flipo: els ulls d'estrella baixen perquè la barretina no els tapi.
s7('flipo',cf(cara(G('e-estels2',estrella(38,60,11)+estrella(62,60,11))+P('M39,73h22c0,7 -5,11 -11,11s-11,-4 -11,-11Z',TINTA))))
# Quina idea!: les celles baixen perquè es vegin sota la barretina.
s7('idea',cf(G('e-bota',cara(C(39,58,3.6,TINTA)+C(61,58,3.6,TINTA)+L('M31,50q7,-5 14,-2M55,48q7,-4 14,1',TINTA,2.8)+P('M38,68h24c0,8 -5,12 -12,12s-12,-4 -12,-12Z',TINTA))))
  +G('e-bombeta',C(84,18,9,AC)+f'<rect x="80.5" y="26" width="7" height="5" rx="1.5" fill="{FE}"/>'+L('M84,6v-4M73,10l-4,-3M95,10l4,-3',AC,2.2)))
# Petonets: el petó es veu: els llavis fan el morret i en surten cors que volen, un darrere l'altre.
s7('peto',cf(cara(L('M32,57q7,-5 14,0M54,57q7,-5 14,0',TINTA,3)
  +'<ellipse cx="30" cy="68" rx="6" ry="3.6" fill="#F05A6A" opacity=".55"/><ellipse cx="72" cy="68" rx="6" ry="3.6" fill="#F05A6A" opacity=".55"/>'
  +G('e-morret',P('M48,70c3,-3 8,-1 6,3c3,1 2,6 -2,6c-2,1 -5,0 -5,-2c0,-2 1,-4 1,-7Z',TINTA),ORIG(51,74))))
  +''.join(G('e-petons',cor(66,64,.75),f'style="--i:{i}"') for i in range(3)))
# Em pixo de riure: les llàgrimes surten disparades cap als costats des de la punta dels ulls, com dues fonts.
def raig(s):
    return G('e-raig-ll',L(f'M{50+s*20},53Q{50+s*30},44 {50+s*38},50',BLAU,3.6)+gota(50+s*38,49,.9),ORIG(50+s*20,53))
s7('riure',cf(G('e-riu',cara(L('M31,55l8,-5l8,5M53,55l8,-5l8,5',TINTA,3.2)+P('M31,64h38c0,11 -8,18 -19,18s-19,-7 -19,-18Z',TINTA)+P('M41,77c3,-3 15,-3 18,0c-3,3 -15,3 -18,0Z',R)
  +raig(-1)+raig(1)))))
# Crack: les ulleres de sol fan el gest de baixar i pujar, hi passa una lluentor i fan una espurna.
LENTS='M18,48h30c2,0 3,2 3,4c0,8 -6,14 -14,14h-4c-8,0 -15,-6 -15,-14c0,-2 0,-4 0,-4ZM52,48h30c0,0 0,2 0,4c0,8 -7,14 -15,14h-4c-8,0 -14,-6 -14,-14c0,-2 1,-4 3,-4Z'
s7('ulleres',cf(cara(L('M40,76q12,6 22,-2',TINTA,3.4)
  +G('e-ulleres',P(LENTS,'#14171C')+L('M48,51h4',"#14171C",3.4)+G('e-llum-lent',L('M22,62l10,-12M56,62l10,-12',BL,3,'opacity=".8"')))
  +G('e-brill2',estrella(80,46,6)))))

# Al cim!: la muntanya petita queda al darrere de la gran, i cada pic té la seva neu.
s7('cim',C(82,20,10,A,'opacity=".9"')
  +o('M42,94L72,42L100,94Z','#6E7A8C')+o('M72,42L65,54l5,-2l3,4l5,-5Z','#F4F8FB',1.4)
  +o('M0,94L38,24L78,94Z','#56606F')+o('M38,24L29,40l6,-3l4,5l5,-6Z','#F4F8FB',1.6)
  +P('M38,24L48,42L78,94H40Z','#000','opacity=".14"')
  +L('M38,24V6',BL,2.6)+G('e-bandera',o('M38,6h18l-5,5l5,5h-18Z',R,1.6),ORIG(38,11))
  +G('e-espurneig',estrella(30,18,4)+estrella(48,14,3)),'cim')

# A la platja!: una cala de la costa refeta de zero: penya-segats amb pins, aigua turquesa, sorra i l'onada que va i ve.
pi=lambda x,y,s: L(f'M{x},{y}l{-1*s},{10*s}',MARRO,2.2*s)+f'<ellipse cx="{x}" cy="{y}" rx="{9*s}" ry="{3.6*s}" fill="#2F6A34"/>'+f'<ellipse cx="{x+2*s}" cy="{y-2.4*s}" rx="{6*s}" ry="{2.6*s}" fill="#3E8A44"/>'
s7('cala','<rect x="0" y="0" width="100" height="100" rx="18" fill="#8FD3F4"/>'
  +C(52,16,7,'#FFD45A')
  +'<path d="M0,44H100V82C100,92 92,100 82,100H18C8,100 0,92 0,82Z" fill="#1FA3B8"/><path d="M0,44H100V50H0Z" fill="#167F99"/>'
  +G('e-balanceig',P('M50,56h14c-1,3 -4,4 -7,4s-6,-1 -7,-4Z',BL)+L('M57,55V44',MARRO,1.2)+P('M57,44L63,54H57Z','#F4F8FB'))
  +P('M0,28C12,26 22,34 27,48C31,60 29,72 22,82H0Z','#B7865A')+P('M0,40C8,40 14,48 16,58C18,68 14,76 10,82H0Z','#9A6C44')
  +P('M100,24C86,24 76,34 72,48C68,60 71,72 78,82H100Z','#B7865A')+P('M100,38C92,38 86,46 85,56C84,66 88,76 92,82H100Z','#9A6C44')
  +pi(12,22,1)+pi(24,30,.7)+pi(86,18,1)+pi(76,28,.7)
  +P('M8,100C18,86 36,80 50,80S82,86 92,100Z','#F2D59A')
  +G('e-espuma',L('M14,94C24,84 38,82 50,82S76,84 86,94',BL,2.6,'opacity=".9"')),'cala')

# Toquen a festa!: una campana de bronze de debò, penjada del jou, que fa un bon vaivé amb el batall i ones de so.
s7('campana',o('M14,12H86',"none",3)+L('M14,12H86',MARRO,5)+L('M22,12V4M78,12V4',MARRO,4)
  +G('e-campana2',o('M45,13h10v6h-10Z','#8A6420',1.4)
     +G('e-batall',L('M50,68V82','#5A4012',2.4)+oc(50,84,4.6,'#5A4012',1.4),ORIG(50,66))
     +o('M38,24C38,18 62,18 62,24L64,44C66,58 72,66 80,70H20C28,66 34,58 36,44Z','#C99A3A',2.2)
     +P('M42,26C42,22 46,21 48,21L46,44C45,56 40,64 34,68H28C34,62 39,54 40,44Z','#E8C266','opacity=".7"')
     +o('M17,70H83C85,70 86,72 86,73V75C86,77 85,78 83,78H17C15,78 14,77 14,75V73C14,72 15,70 17,70Z','#A97E2A',2)
     +L('M30,52H70',"#A97E2A",1.6,'opacity=".6"'),ORIG(50,12))
  +''.join(G('e-ona-so',L(f'M{x},40q{d*6},10 0,20',AC,2.6),f'style="--i:{i};transform-box:view-box;transform-origin:{50+d*6}px 50px"') for i,(x,d) in enumerate([(12,-1),(88,1),(5,-1),(95,1)])),'campana')

# Agafem el bus: autobús de ciutat vist de cara, vermell, amb el rètol de la línia, el parabrisa gran, els retrovisors i els fars.
s7('bus',G('e-bus',
  L('M20,30C12,28 8,32 8,40',K,2.4)+orr(4,38,8,14,2,'#2A2E35',1.6)+L('M80,30C88,28 92,32 92,40',K,2.4)+orr(88,38,8,14,2,'#2A2E35',1.6)
  +orr(22,84,12,10,3,'#14171C',1.6)+orr(66,84,12,10,3,'#14171C',1.6)
  +orr(18,12,64,76,10,'#D7262B',2.4)
  +orr(25,17,50,10,2,'#14171C',1.4)+G('e-retol',L('M30,22h6M40,22h14M58,22h12',A,2.6))
  +orr(23,31,54,32,5,'#9ED3F0',2)+P('M28,58L44,34h8L36,58Z','#fff','opacity=".35"')+L('M50,31V63',K,1.6)
  +L('M34,62l8,-8M62,62l8,-8',K,1.6)
  +orr(32,68,36,8,2,'#9E1C20',1.2)+L('M36,72h28',"#5A0E12",1.2)
  +G('e-fars',oc(27,74,4,'#FFF2C2',1.4)+oc(73,74,4,'#FFF2C2',1.4))
  +orr(16,80,68,7,3,'#3A3F48',1.6)),'bus')

# Uf, quina pujada: no agrada, fora.
E[:]=[e for e in E if e[1]!='suant']

CSS+=r'''
.anim .e-morret{animation:emorret 1.6s ease-in-out infinite}@keyframes emorret{0%,100%{transform:scale(1)}40%{transform:scale(1.35)}55%{transform:scale(.9)}}
.anim .e-petons{opacity:0;animation:epetons 2.4s ease-out infinite;animation-delay:calc(var(--i)*.8s)}@keyframes epetons{0%{opacity:0;transform:translate(-14px,10px) scale(.3)}15%{opacity:1}100%{opacity:0;transform:translate(14px,-38px) scale(1.2)}}
.e-petons{opacity:0}.e-petons[style*="--i:0"]{opacity:1}
.anim .e-raig-ll{animation:eraigll .35s ease-in-out infinite alternate}@keyframes eraigll{from{transform:scale(.8)}to{transform:scale(1.1)}}
.anim .e-ulleres{animation:eull 2.4s ease-in-out infinite}@keyframes eull{0%,55%,100%{transform:none}65%,80%{transform:translateY(5px)}}
.anim .e-llum-lent{animation:ellum 2.4s ease-in-out infinite}@keyframes ellum{0%,15%{opacity:0;transform:translateX(-8px)}30%{opacity:1}45%,100%{opacity:0;transform:translateX(10px)}}
.anim .e-espuma{animation:eespuma 2.2s ease-in-out infinite}@keyframes eespuma{0%,100%{transform:translateY(0);opacity:.9}50%{transform:translateY(-4px);opacity:.5}}
.anim .e-campana2{animation:ecamp2 1.2s ease-in-out infinite alternate}@keyframes ecamp2{from{transform:rotate(-26deg)}to{transform:rotate(26deg)}}
.anim .e-batall{animation:ebat 1.2s ease-in-out infinite alternate}@keyframes ebat{from{transform:rotate(18deg)}to{transform:rotate(-18deg)}}
.anim .e-ona-so{opacity:0;animation:eonaso 1.2s ease-out infinite;animation-delay:calc(var(--i)*.3s)}@keyframes eonaso{0%{opacity:0;transform:scale(.6)}30%{opacity:1}100%{opacity:0;transform:scale(1.3)}}
.anim .e-bus{animation:ebus .5s ease-in-out infinite alternate}@keyframes ebus{from{transform:translateY(0)}to{transform:translateY(-2px)}}
.anim .e-fars{animation:efars 1.6s steps(1) infinite}@keyframes efars{0%,60%{opacity:1}70%{opacity:.35}80%,100%{opacity:1}}
.anim .e-retol{animation:eretol 2s steps(1) infinite}@keyframes eretol{0%,50%{opacity:1}55%,100%{opacity:.6}}
'''

# --- Segona passada de la revisió
# Flipo: els ulls d'estrella més grossos i ben separats de la boca, que és petita i rodona.
s7('flipo',cf(cara(G('e-estels2',estrella(37,59,12.5)+estrella(63,59,12.5))+'<ellipse cx="50" cy="79" rx="6" ry="5" fill="'+TINTA+'"/>')))
# Petonets: la boca és el morret clàssic (com un 3) i en surten cors volant.
s7('peto',cf(cara(L('M32,57q7,-5 14,0M54,57q7,-5 14,0',TINTA,3)
  +'<ellipse cx="30" cy="68" rx="6" ry="3.6" fill="#F05A6A" opacity=".55"/><ellipse cx="72" cy="68" rx="6" ry="3.6" fill="#F05A6A" opacity=".55"/>'
  +G('e-morret',L('M47,68q6,1.5 1,4.5q5,1.5 -1,5',TINTA,3.2),ORIG(49,73))))
  +''.join(G('e-petons',cor(64,66,.7),f'style="--i:{i}"') for i in range(3)))
# Em pixo de riure: les llàgrimes més petites i fines.
def raig(s):
    return G('e-raig-ll',L(f'M{50+s*21},54Q{50+s*28},48 {50+s*33},52',BLAU,2.4)+gota(50+s*33,51.5,.55),ORIG(50+s*21,54))
s7('riure',cf(G('e-riu',cara(L('M31,55l8,-5l8,5M53,55l8,-5l8,5',TINTA,3.2)+P('M31,64h38c0,11 -8,18 -19,18s-19,-7 -19,-18Z',TINTA)+P('M41,77c3,-3 15,-3 18,0c-3,3 -15,3 -18,0Z',R)
  +raig(-1)+raig(1)))))
# Crack: les ulleres baixen pel nas i deixen veure els ulls que fan l'ullet, i tornen a pujar amb una lluentor.
s7('ulleres',cf(cara(G('e-ulls-darrere',C(37,55,3.2,TINTA)+L('M58,55h7',TINTA,3))+L('M40,76q12,6 22,-2',TINTA,3.4)
  +G('e-ulleres',P(LENTS,'#14171C')+L('M48,51h4',"#14171C",3.4)+G('e-llum-lent',L('M22,62l10,-12M56,62l10,-12',BL,3,'opacity=".8"')))
  +G('e-brill2',estrella(80,46,6)))))
# A la platja!: platja de debò: molta sorra, para-sol de ratlles, tovallola, el mar amb onades que arriben a la sorra i el sol.
s7('cala','<rect x="0" y="0" width="100" height="100" rx="18" fill="#8FD3F4"/>'
  +G('e-sol-gira',C(78,18,9,'#FFD45A')+L('M78,4v-3M92,18h3M88,8l2,-2M68,8l-2,-2',"#FFD45A",2.2),ORIG(78,18))
  +P('M0,40H100V62H0Z','#1FA3B8')+P('M0,40H100V44H0Z','#167F99')
  +G('e-onada',L('M-20,52q5,-3 10,0t10,0t10,0t10,0t10,0t10,0t10,0t10,0t10,0t10,0t10,0t10,0',BL,1.8,'opacity=".7"'))
  +'<path d="M0,62C30,56 70,58 100,62V82C100,92 92,100 82,100H18C8,100 0,92 0,82Z" fill="#F2D59A"/>'
  +G('e-espuma',L('M0,62C30,56 70,58 100,62',BL,3))
  +f'<g transform="rotate(-8 30 80)" {ESTIL0}>'+o('M14,74h32v14h-32Z','#5AB8E8',1.6)+L('M14,79h32M14,84h32',BL,2)+'</g>'
  +L('M70,92L64,52',K,2.6)
  +o('M44,56C48,40 80,36 90,50Z',R,2)+P('M55,45C58,48 61,51 64,53L70,46C67,45 61,44 55,45Z',BL)+P('M75,44C79,46 83,48 86,51L90,50C86,46 81,44 75,44Z',BL)
  +o('M80,92c0,-5 4,-8 8,-8s8,3 8,8Z',"#F07A4A",1.4),'cala')
# Toquen a festa!: campana senzilla (sense jou ni detalls), que es gronxa molt, fa ding-dong i treu ones de so.
s7('campana',L('M50,6V14',K,3)+C(50,6,3,'none',f'stroke="{K}" stroke-width="2.4"')
  +G('e-campana3',G('e-batall',L('M50,64V78',K,2.6)+oc(50,80,5,'#7A5A1C',1.6),ORIG(50,62))
     +o('M36,22C36,14 64,14 64,22L66,44C68,58 74,64 82,68H18C26,64 32,58 34,44Z','#E0A93A',2.4)
     +P('M42,24C42,20 45,19 47,19L45,44C44,54 40,60 34,64H28C34,58 38,52 39,44Z','#F6D27A','opacity=".8"')
     +o('M16,68H84V73C84,75 83,76 81,76H19C17,76 16,75 16,73Z','#C48A24',2.2),ORIG(50,14))
  +''.join(G('e-ona-so',L(f'M{x},34q{d*7},12 0,24',AC,2.8),f'style="--i:{i};transform-box:view-box;transform-origin:{50+d*8}px 46px"') for i,(x,d) in enumerate([(12,-1),(88,1),(4,-1),(96,1)])),'campana')

CSS+=r'''
.anim .e-campana3{animation:ecamp3 .9s ease-in-out infinite alternate}@keyframes ecamp3{from{transform:rotate(-32deg)}to{transform:rotate(32deg)}}
.anim .e-campana3 .e-batall{animation:ebat .9s ease-in-out infinite alternate}
.anim .e-ulls-darrere{animation:eullsd 2.4s ease-in-out infinite}@keyframes eullsd{0%,55%,100%{opacity:0}62%,82%{opacity:1}}
.e-ulls-darrere{opacity:0}
.anim .e-ulleres{animation:eull2 2.4s ease-in-out infinite}@keyframes eull2{0%,55%,100%{transform:none}62%,82%{transform:translateY(8px) rotate(-4deg)}}
.anim .e-sol-gira{animation:esolg 6s linear infinite}@keyframes esolg{to{transform:rotate(360deg)}}
'''

# --- Tercera passada de la revisió
# Flipo: els ulls d'estrella es dibuixen per sobre de la barretina, perquè no hi quedin mai amagats a sota.
s7('flipo',cf(cara('<ellipse cx="50" cy="79" rx="6" ry="5" fill="'+TINTA+'"/>',extra=G('e-estels2',estrella(37,58,12.5)+estrella(63,58,12.5)))))
# Petonets: la boca són uns llavis vermells que fan el petó (sense la ratlla en forma de 3).
s7('peto',cf(cara(L('M32,57q7,-5 14,0M54,57q7,-5 14,0',TINTA,3)
  +'<ellipse cx="30" cy="68" rx="6" ry="3.6" fill="#F05A6A" opacity=".55"/><ellipse cx="72" cy="68" rx="6" ry="3.6" fill="#F05A6A" opacity=".55"/>'
  +G('e-morret',o('M43,73c2,-4 5,-4 7,-1.5c2,-2.5 5,-2.5 7,1.5c-2,4.5 -12,4.5 -14,0Z','#D7303A',1.6)+L('M44.5,73.2h11','#8E1A22',1.2),ORIG(50,73))))
  +''.join(G('e-petons',cor(64,66,.7),f'style="--i:{i}"') for i in range(3)))
# Em pixo de riure: tornen les gotes petites de la punta dels ulls, que ja t'agradaven.
s7('riure',cf(G('e-riu',cara(L('M31,55l8,-5l8,5M53,55l8,-5l8,5',TINTA,3.2)+P('M31,64h38c0,11 -8,18 -19,18s-19,-7 -19,-18Z',TINTA)+P('M41,77c3,-3 15,-3 18,0c-3,3 -15,3 -18,0Z',R)
  +G('e-llagrima2',gota(27,55,.8)+gota(73,55,.8))))))
# A la platja!: no agrada, fora.
E[:]=[e for e in E if e[1]!='cala']

# --- Quarta passada: Petonets amb la boqueta petita i plena (opció B triada).
s7('peto',cf(cara(L('M32,57q7,-5 14,0M54,57q7,-5 14,0',TINTA,3)
  +'<ellipse cx="30" cy="68" rx="6" ry="3.6" fill="#F05A6A" opacity=".55"/><ellipse cx="72" cy="68" rx="6" ry="3.6" fill="#F05A6A" opacity=".55"/>'
  +G('e-morret','<ellipse cx="52" cy="73" rx="3.6" ry="4.2" fill="'+TINTA+'"/>',ORIG(52,73))))
  +''.join(G('e-petons',cor(64,66,.7),f'style="--i:{i}"') for i in range(3)))

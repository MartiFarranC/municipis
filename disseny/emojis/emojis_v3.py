# Redisseny dels 21 que tenien bona idea però un dibuix que no agradava.
K='#14171C'
def o(d,f,w=2.4,x=''): return f'<path d="{d}" fill="{f}" stroke="{K}" stroke-width="{w}" stroke-linejoin="round" stroke-linecap="round" {x}/>'
def oc(x,y,r,f,w=2.4,x2=''): return f'<circle cx="{x}" cy="{y}" r="{r}" fill="{f}" stroke="{K}" stroke-width="{w}" {x2}/>'
def orr(x,y,w,h,rx,f,sw=2.4,x2=''): return f'<rect x="{x}" y="{y}" width="{w}" height="{h}" rx="{rx}" fill="{f}" stroke="{K}" stroke-width="{sw}" {x2}/>'
def barr_mini(x,y,r): return barr_svg(x,y,r,True)
NOU3=set()
def s3(id_,svg,anim=None): subst(id_,svg,anim); NOU3.add(id_)
def persona(x,y,s=1,camisa=R,cls=''):
    h=C(x,y-15*s,6*s,PELL,f'stroke="{K}" stroke-width="1.6"')
    cos=o(f'M{x-8*s},{y-8*s}h{16*s}l{2*s},{14*s}h{-20*s}Z',camisa,1.6)+f'<rect x="{x-9*s}" y="{y+2*s}" width="{18*s}" height="{3.6*s}" fill="{K}"/>'
    cames=o(f'M{x-8*s},{y+6*s}h{16*s}l-1,{12*s}h{-14*s}Z','#F4EDE0',1.6)
    return cames+cos+h+barr_mini(x,y-15*s,6*s)

# 1 Castell
s3('castell',G('e-castell',
   ''.join(persona(x,88,.85) for x in (14,32,50,68,86))
  +''.join(persona(x,64,.85) for x in (32,50,68))
  +''.join(persona(x,42,.8) for x in (41,59))
  +G('e-enxaneta',persona(50,22,.65,A)+L('M54,10l7,-8',A,3,'class="e-braç"'))),'castell')
# 2 Cim
s3('cim',C(80,22,10,A,'opacity=".9"')+o('M2,94L36,30L52,54L66,36L98,94Z','#56606F')
  +o('M36,30L28,46l6,-3l4,5l5,-6Z','#F4F8FB',1.6)+o('M66,36L60,46l5,-2l4,4l4,-5Z','#F4F8FB',1.6)
  +P('M36,30L46,48L52,54L36,94H2Z','#000','opacity=".14"')
  +L('M36,30V8',BL,2.6)+G('e-bandera',o('M36,8h18l-5,5l5,5h-18Z',R,1.6),'style="transform-box:view-box;transform-origin:36px 13px"')
  +G('e-espurneig',estrella(30,24,4)+estrella(44,20,3)),'cim')
# 3 Quin fred! (cara)
s3('fred',G('e-tremola',cara(L('M32,50l12,3M68,50l-12,3',TINTA,2.6)+C(39,56,3.6,TINTA)+C(61,56,3.6,TINTA)
   +f'<rect x="34" y="66" width="32" height="12" rx="4" fill="{BL}" stroke="{TINTA}" stroke-width="2.4"/>'+L('M42,66v12M50,66v12M58,66v12',TINTA,1.6)
   +C(50,62,4,'#E8606A'),'#9CCDEB',
   extra=o('M18,84c10,6 54,6 64,0l3,10c-14,6 -56,6 -70,0Z',R,2)+o('M66,88l6,12h-10Z',R,1.6)
   +''.join(o(f'M{x},38l3,10l3,-10Z','#DDF1FF',1.2) for x in (24,40,56,72))))
  +''.join(G('e-floc',P(f'M{x},{y-5}v10M{x-4.3},{y-2.5}l8.6,5M{x-4.3},{y+2.5}l8.6,-5','none',f'stroke="{BL}" stroke-width="1.8" stroke-linecap="round"'),f'style="--i:{i}"') for i,(x,y) in enumerate([(8,20),(92,26),(6,60),(94,70)])),'fred')
# 4 Traca
def esclat(x,y,r,c,i):
    raigs=''.join(C(x+r*math.cos(math.radians(a))*f,y+r*math.sin(math.radians(a))*f,1.6+f*.6,c) for a in range(0,360,30) for f in (.55,1))
    return G('e-esclat',raigs+C(x,y,2.6,AC),f'style="--i:{i};transform-box:view-box;transform-origin:{x}px {y}px"')
s3('traca',f'<rect x="0" y="0" width="100" height="100" rx="18" fill="#141A2E"/>'
  +esclat(30,32,20,R,0)+esclat(70,26,18,A,1)+esclat(56,62,16,BLAU,2)+esclat(24,70,12,AC,3)
  +G('e-coet',L('M84,96L80,60',AC,2,'stroke-dasharray="2 3"')+o('M78,56l4,-8l4,8v6h-8Z',R,1.4)),'traca')
# 5 Cafè
s3('cafe',o('M8,80c0,-4 18,-8 42,-8s42,4 42,8s-18,8 -42,8s-42,-4 -42,-8Z','#E9E2D0')
  +o('M68,48c14,-2 18,14 4,20','none',3.4)+L('M68,48c14,-2 18,14 4,20','#F4EDE0',2)
  +o('M22,40h48l-4,30c-1,8 -9,12 -20,12s-19,-4 -20,-12Z','#F4EDE0')
  +'<ellipse cx="46" cy="41" rx="24" ry="6" fill="#6A3A20" stroke="'+K+'" stroke-width="2.4"/>'
  +P('M46,45c-3,-3 -9,-3 -9,-1c0,3 6,5 9,7c3,-2 9,-4 9,-7c0,-2 -6,-2 -9,1Z','#E8C49A')
  +G('e-vapor-puja',P('M36,30q5,-6 0,-12t0,-12M48,28q5,-6 0,-12t0,-12','none',f'stroke="{GRIS}" stroke-width="2.6" stroke-linecap="round"')),'cafe')
# 6 Calçot
calcot1=lambda: (L('M0,0L0,-44',VERDF,5)+L('M0,-44L-6,-60M0,-44L5,-62',VERD,4)+L('M0,0L0,-30','#F4F0E0',6)+L('M0,0V10','#2A2420',7)+L('M-3,-4l-2,10M3,-6l2,12','#4A3A2A',1.6))
jaient=lambda y: L(f'M10,{y}H26','#2A2420',6)+L(f'M26,{y}H46','#F4F0E0',6)+L(f'M46,{y}H58',VERDF,5)+L(f'M58,{y}l8,-4M58,{y}l8,3',VERD,3)+L(f'M12,{y-2}h10',"#4A3A2A",1.2)
s3('calcot',o('M4,62C16,56 60,56 70,62L68,82C58,78 16,78 6,82Z','#C0663A')+jaient(64)+jaient(71)
  +o('M62,74h32c0,10 -7,16 -16,16s-16,-6 -16,-16Z','#8A4A26')+'<ellipse cx="78" cy="74" rx="16" ry="4" fill="#D85A2A" stroke="'+K+'" stroke-width="2"/>'
  +G('e-calcot2',L('M78,70V52','#2A2420',6)+L('M78,52V30','#F4F0E0',6)+L('M78,30V16',VERDF,5)+L('M78,16l-5,-12M78,16l5,-12',VERD,3)),'calcot')
# 7 Ninot de neu
s3('ninot',o('M0,90C20,84 80,84 100,90V100H0Z','#E8F1F8',2)
  +oc(50,70,20,'#F4F8FB')+oc(50,40,14,'#F4F8FB')+C(45,38,1.8,K)+C(55,38,1.8,K)+o('M50,42l12,3l-12,3Z','#E07A2C',1.4)
  +L('M44,46q6,3 12,0',K,1.6)+''.join(C(50,y,2.2,K) for y in (62,70,78))
  +L('M31,64L14,52M14,52l-4,-4M14,52l-5,2M69,64L86,50M86,50l3,-5M86,50l5,1','#6A4426',2.6)
  +o('M36,52c4,6 24,6 28,0l2,6c-6,6 -26,6 -32,0Z',R,1.6)+o('M58,56l6,12h-8Z',R,1.4)
  +barr_mini(50,40,14)
  +''.join(G('e-floc',C(x,y,2,BL),f'style="--i:{i}"') for i,(x,y) in enumerate([(12,14),(88,24),(16,40),(84,64),(8,70)])),'ninot')
# 8 Pa amb tomàquet
s3('pa',o('M10,50C10,30 26,20 50,20S90,30 90,50C90,56 86,60 84,62V80C84,86 80,88 74,88H26C20,88 16,86 16,80V62C14,60 10,56 10,50Z','#B5733A')
  +P('M18,50C18,36 32,28 50,28S82,36 82,50C82,54 80,56 78,58V78C78,80 76,82 74,82H26C24,82 22,80 22,78V58C20,56 18,54 18,50Z','#F0D8A8')
  +P('M26,46C32,38 44,40 50,36S70,36 74,44C78,52 72,56 74,64S66,76 58,72S40,78 34,70S22,58 26,46Z','#D7262B','opacity=".88"')
  +''.join('<ellipse cx="%d" cy="%d" rx="3" ry="2" fill="#B5733A" opacity=".5"/>'%(x,y) for x,y in [(36,52),(56,48),(64,64),(42,66)])
  +G('e-oli',P('M60,6c3,5 5,8 5,11a5,5 0 0 1 -10,0c0,-3 2,-6 5,-11Z','#C8B53A',f'stroke="{K}" stroke-width="1.4"'))
  +o('M80,82c0,-8 6,-12 12,-12c0,8 -4,12 -12,12Z','#E8343A',1.6),'oli')
# 9 Vermut
s3('vermut',o('M14,34h40l-4,52c0,3 -2,4 -5,4H23c-3,0 -5,-1 -5,-4Z','#E8F4FA','opacity=".55"')
  +P('M17,52h34l-3,32c0,2 -1,3 -3,3H23c-2,0 -3,-1 -3,-3Z','#8E2A2A')
  +o('M22,52h10v9H22ZM36,56h10v9H36Z','#DDF1FF',1.2,'opacity=".8"')
  +o('M30,44a10,10 0 0 1 20,0Z','#F2A030',1.6)+L('M24,70L46,28',MARRO,2)+oc(44,32,4,VERD,1.4)
  +o('M66,40h16v48c0,3 -2,4 -4,4h-8c-2,0 -4,-1 -4,-4Z','#5AB8E8','opacity=".55"')
  +o('M64,30h20v10H64Z','#8A96AA',1.8)+o('M84,32h10l-2,4h-8Z','#8A96AA',1.4)+L('M74,30V22',FE,2.6)
  +G('e-sifo',P('M94,34c4,6 2,14 -6,20','none',f'stroke="{BLAU}" stroke-width="2" stroke-dasharray="2 3"')),'vermut')
# 10 Ball de bastons
balla_b=lambda x,s,rot: persona(x,70,1.05,'#F4EDE0')+o(f'M{x-2},{52}L{x+s*24},{30}','none',4)+L(f'M{x-2},{52}L{x+s*24},{30}',MARROC,3)+C(x+s*24,30,2.4,R)
s3('bastons',G('e-baston-esq',balla_b(26,1,0),'style="transform-box:view-box;transform-origin:26px 70px"')+G('e-baston-dre',balla_b(74,-1,0),'style="transform-box:view-box;transform-origin:74px 70px"')
  +G('e-clap2',L('M50,22v-8M42,24l-5,-6M58,24l5,-6',AC,2.6))+L('M6,96h88','#4A525F',2),'bastons')
# 11 Cava
s3('cava',o('M38,94h20c3,0 5,-2 5,-5V54c0,-8 -6,-12 -7,-18V20h-10v16c-1,6 -7,10 -7,18v35c0,3 2,5 -1,5Z','#2F4A2C')
  +o('M45,20h12v14H45Z',A,1.6)+o('M37,62h26v18H37Z','#F4EDE0',1.6)+L('M42,68h16M44,74h12','#C9B89A',2)
  +G('e-tap',o('M46,8h10v12H46Z','#C9A06A',1.6))
  +G('e-escuma',C(44,16,3,'#fff')+C(56,14,2.6,'#fff')+C(50,10,2.2,'#fff'))
  +o('M72,46h16l-2,16c0,4 -3,6 -6,6s-6,-2 -6,-6Z','#F6E7A0','opacity=".85"')+L('M80,68V86M74,88h12',K,2.4)
  +''.join(G('e-bombolla',C(x,y,1.4,'#fff'),f'style="--i:{i}"') for i,(x,y) in enumerate([(78,58),(82,54),(80,50)])),'cava')
# 12 Drac
s3('drac',G('e-drac',o('M20,80c-12,-4 -14,-16 -6,-22c-2,8 4,14 12,12Z',VERD,2)
  +o('M22,74c0,-18 12,-28 28,-28c10,0 16,4 20,10l14,-2c6,0 8,6 6,10l-4,8c-2,4 -6,6 -10,4l-6,-2c-6,10 -16,14 -28,12c-8,-2 -20,-4 -20,-12Z',VERD)
  +o('M30,46l2,-12l6,8l4,-12l6,10l4,-10l4,12','#3E7A4A',1.8)
  +P('M36,66c6,8 18,8 24,2','none',f'stroke="#B8E0A0" stroke-width="2.4" stroke-linecap="round"')
  +oc(66,52,5.6,BL,1.8)+C(67,52,3,K)+C(68,51,1,'#fff')+C(86,60,1.6,K)
  +o('M40,58c4,-8 14,-10 20,-4c-6,0 -12,2 -16,8Z','#7AC08A',1.6)
  +o('M36,80l-2,10h8l2,-8M56,82l2,8h8l-2,-10',VERD,1.8))
  +G('e-foc-drac',o('M92,64c6,-4 6,-12 2,-16c0,4 -2,6 -4,6c0,-4 -2,-8 -6,-8c2,4 0,8 -2,10c-2,-2 -4,-2 -6,0c2,4 8,10 16,8Z',A,1.6)),'drac')
# 13 Tió
s3('tio',G('e-tio',o('M16,50h54c8,0 12,8 12,16s-4,16 -12,16H16Z',MARRO)
  +L('M30,56q12,-3 24,0M38,72q12,-3 24,0','#6A4426',2)
  +o('M34,46c10,-6 30,-6 40,2v20c-10,6 -30,6 -40,0Z',R,2)+L('M38,52h32M38,60h32',RF,1.6)
  +oc(16,66,16,MARROC)+'<circle cx="16" cy="66" r="10" fill="#D9B48A" opacity=".55"/>'
  +C(11,62,2,K)+C(21,62,2,K)+P('M10,70q6,6 12,0','none',f'stroke="{K}" stroke-width="2" stroke-linecap="round"')+C(8,68,2.4,'#E8606A','opacity=".6"')+C(24,68,2.4,'#E8606A','opacity=".6"')
  +o('M28,82v10M62,82v10',MARRO,3)+barr_mini(16,58,12))
  +G('e-regal',o('M78,84h14v12H78Z',BLAU,1.6)+L('M85,84v12M78,90h14',A,2)),'tio')
# 14 Cotxe
s3('cotxe',G('e-cotxe',o('M8,72c0,-8 4,-12 12,-12l10,-16c2,-4 6,-6 10,-6h22c5,0 9,2 12,6l10,16c8,0 10,4 10,12v4c0,2 -2,4 -4,4H12c-2,0 -4,-2 -4,-4Z',R)
  +o('M36,44h14v16H28ZM56,44h12l8,16H56Z','#8CC4E8',1.8)+o('M30,32h40l-2,6H32Z',FEF,1.6)+o('M36,24h28v8H36Z','#C9A06A',1.6)
  +oc(28,78,9,'#2A2E35')+oc(76,78,9,'#2A2E35')+C(28,78,3.4,FE)+C(76,78,3.4,FE)+oc(90,66,3,AC,1.4))
  +G('e-velocitat',L('M0,52h8M0,62h5M2,72h6',GRIS,3))+L('M0,90h100','#3A4558',3),'cotxe')
# 15 Tren (Rodalies)
s3('rodalies',G('e-tren',o('M14,36c0,-10 8,-16 18,-16h36c10,0 18,6 18,16v38c0,4 -4,8 -8,8H22c-4,0 -8,-4 -8,-8Z','#F4EDE0')
  +o('M22,30h56v22H22Z','#2E6E9A',1.8)+f'<rect x="14" y="58" width="72" height="6" fill="#E8742A"/>'
  +oc(28,72,4,AC,1.6)+oc(72,72,4,AC,1.6)+o('M40,74h20',"none",2)+L('M30,86l-6,8M70,86l6,8',K,3))+L('M4,96h92',FE,3)
  +G('e-rellotget',oc(88,16,11,'#E9E2D0')+L('M88,16V9',K,2)+L('M88,16h5',R,2),'style="transform-box:view-box;transform-origin:88px 16px"')
  +f'<text x="66" y="12" font-family="Chakra Petch,sans-serif" font-weight="700" font-size="10" fill="{R}">+15</text>','tren')
# 16 Cascada
s3('cascada',o('M0,6H38c4,10 2,22 -2,34H0Z','#56606F')+o('M62,6H100V40H64C60,28 58,16 62,6Z','#56606F')
  +C(10,12,8,'#3E7A4A')+C(22,8,7,'#3E7A4A')+C(88,10,8,'#3E7A4A')
  +G('e-aigua-cau',''.join(L(f'M{x},6V80',c,5) for x,c in [(42,BLAU),(48,'#8CC4E8'),(54,BLAU),(59,'#8CC4E8')]))
  +o('M2,80c12,-8 84,-8 96,0c-4,12 -88,12 -96,0Z','#2E6E9A')+G('e-esquitx',C(36,74,3,BL)+C(66,74,3,BL)+C(50,70,3.4,BL)+C(44,66,2,BL))
  +G('e-onada',L('M-10,84q10,-3 20,0t20,0t20,0t20,0t20,0t20,0','#8CC4E8',2)),'cascada')
# 17 Llaüt
s3('llaut',f'<rect x="0" y="76" width="100" height="24" fill="#2E6E9A"/>'
  +G('e-balanceig',o('M8,66h84c-4,12 -16,18 -42,18S12,78 8,66Z','#F4EDE0')+P('M10,72h80c-1,3 -3,5 -5,7H15c-2,-2 -4,-4 -5,-7Z','#2F7A5A')
   +L('M46,66V22',MARRO,3)+o('M44,14L84,60H44Z','#F4EDE0',2)+L('M30,30L86,4',MARRO,2.6)+oc(18,70,2.4,K,0))
  +G('e-onada',L('M-10,82q10,-4 20,0t20,0t20,0t20,0t20,0t20,0','#8CC4E8',2.6)),'balanceig')
# 18 Avió
s3('avio',G('e-nuvol2',C(16,78,8,'#C9CED6')+C(26,74,10,'#C9CED6')+C(36,80,7,'#C9CED6')+C(76,24,6,'#C9CED6')+C(86,22,8,'#C9CED6'))
  +G('e-avio',o('M10,52c0,-5 5,-8 12,-8h54c10,0 18,4 20,8c-2,4 -10,8 -20,8H22c-7,0 -12,-3 -12,-8Z','#F4EDE0')
   +o('M44,46L30,18h12l22,28ZM44,58L30,86h12l22,-28Z','#C9CED6',2)+o('M14,46L6,28h9l11,18Z',R,2)
   +''.join(C(x,52,2.4,'#2E6E9A') for x in (50,58,66,74))+o('M82,46c4,0 9,2 12,6h-12Z','#2E6E9A',1.4)),'avio')
# 19 Bus
cap_fin=lambda x: C(x,34,4,PELL)+barr_mini(x,34,4)
s3('bus',G('e-bus',o('M8,30c0,-8 6,-12 12,-12h60c8,0 12,6 12,12v44c0,4 -2,6 -6,6H14c-4,0 -6,-2 -6,-6Z','#E8B23A')
  +''.join(o(f'M{x},26h14v18h-14Z','#8CC4E8',1.6) for x in (14,32,50))+''.join(cap_fin(x) for x in (21,39,57))
  +o('M70,26h14v40h-14Z','#5A8AA8',1.6)+L('M77,26v40',K,1.2)+f'<rect x="8" y="52" width="62" height="6" fill="{R}"/>'
  +oc(24,82,8,'#2A2E35')+oc(72,82,8,'#2A2E35')+C(24,82,3,FE)+C(72,82,3,FE)+oc(90,68,2.6,AC,1.2)),'bus')
# 20 Moto (escúter)
s3('moto',G('e-moto',o('M26,72c-2,-14 8,-22 22,-22h10l4,-12h10l-4,16c8,4 12,10 12,18Z','#5AB8E8')
  +o('M40,50c0,-6 4,-8 10,-8h10v8Z','#2A2E35',1.8)+o('M62,38l4,-14h6','none',3)+oc(74,26,4,AC,1.6)
  +o('M20,58h14v8H20Z','#C9A06A',1.6)
  +oc(24,78,11,'#2A2E35')+C(24,78,4,FE)+oc(80,78,11,'#2A2E35')+C(80,78,4,FE))
  +G('e-fum2',C(6,74,5,'#C9CED6')+C(10,66,4,'#C9CED6')),'moto')
# 21 Tractor
s3('tractor',G('e-tractor',o('M38,30h28v32H38Z',R)+o('M42,34h20v14H42Z','#8CC4E8',1.6)+C(52,42,4.4,PELL)+barr_mini(52,42,4.4)
  +o('M10,48h32v20H10Z',R)+o('M16,48V30h6v18','#4A525F',1.8)
  +oc(66,72,20,'#2A2E35')+oc(66,72,8,A,1.8)+''.join(L(f'M{66+12*math.cos(math.radians(a))},{72+12*math.sin(math.radians(a))}L{66+19*math.cos(math.radians(a))},{72+19*math.sin(math.radians(a))}','#4A525F',3) for a in range(0,360,40))
  +oc(20,78,11,'#2A2E35')+oc(20,78,4.6,A,1.6))
  +G('e-fum2',C(19,22,4,'#C9CED6')+C(24,14,5,'#C9CED6')),'tractor')

CSS+=r'''
.anim .e-castell{transform-origin:50% 100%;animation:egel 3s ease-in-out infinite}
.anim .e-esclat{opacity:0;animation:eescl 2.4s ease-out infinite;animation-delay:calc(var(--i)*.55s)}@keyframes eescl{0%{opacity:0;transform:scale(.1)}12%{opacity:1}55%{opacity:1;transform:scale(1)}80%,100%{opacity:0;transform:scale(1.15) translateY(4px)}}
.anim .e-coet{animation:ecoet 2.4s ease-in infinite}@keyframes ecoet{0%{transform:translateY(20px);opacity:0}10%{opacity:1}50%{transform:translateY(-40px);opacity:1}55%,100%{opacity:0}}
.anim .e-calcot2{animation:ecal2 2.2s ease-in-out infinite}@keyframes ecal2{0%,100%{transform:translateY(-8px)}45%,60%{transform:translateY(6px)}}
.anim .e-sifo{animation:eraig .3s linear infinite}
.anim .e-regal{animation:eregal 1.8s cubic-bezier(.3,1.6,.5,1) infinite}@keyframes eregal{0%,40%{transform:translate(-14px,0) scale(.2);opacity:0}60%,100%{transform:none;opacity:1}}
.anim .e-nuvol2{animation:enuv 3s linear infinite}@keyframes enuv{from{transform:translateX(16px)}to{transform:translateX(-16px)}}
'''

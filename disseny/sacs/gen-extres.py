# Propostes de les coses dels sacs que no són emojis: portades del passaport, colors de l'app i animacions de càrrega.
# Genera extres.json (dades per a la pàgina de tria). La silueta de Catalunya surt de dades/ (geojson dels municipis).
import json, math, os, random
AQUI=os.path.dirname(os.path.abspath(__file__))
g={'__file__':os.path.join(AQUI,'..','passaport','gen-passaport.py')}
exec(open(g['__file__']).read().split("if __name__=='__main__':")[0],g)
silueta,COM,TXT=g['silueta'],g['COM'],g['TXT']
TOTS=[c for v in COM.values() for c in v['municipis']]
CAT=lambda color,cx=50,cy=66,mida=52: silueta(TOTS,cx,cy,mida,color,pas=1.2)

def tapa(fons,daurat,patro='',sil=True,text=None):
    t=text or daurat
    return (f'<svg viewBox="0 0 100 140"><defs><clipPath id="CLIP"><rect width="100" height="140" rx="6"/></clipPath></defs>'
      f'<g clip-path="url(#CLIP)"><rect width="100" height="140" fill="{fons}"/>{patro}</g>'
      f'<rect x="5" y="5" width="90" height="130" rx="4" fill="none" stroke="{daurat}" stroke-width=".8" opacity=".7"/>'
      f'<text x="50" y="24" text-anchor="middle" font-size="9" fill="{t}" letter-spacing="2" {TXT}>PASSAPORT</text>'
      +(CAT(daurat) if sil else '')+
      f'<text x="50" y="114" text-anchor="middle" font-size="5" fill="{t}" letter-spacing=".6" {TXT}>DESCOBREIX CATALUNYA</text></svg>')

rnd=random.Random(3)
def ones(c,op=.25): return ''.join(f'<path d="M-10,{y} q10,-5 20,0 t20,0 t20,0 t20,0 t20,0 t20,0" fill="none" stroke="{c}" stroke-width="1.4" opacity="{op}"/>' for y in range(32,140,8))
def rajola(c1,c2):
    o=''
    for i in range(0,100,20):
        for j in range(0,140,20):
            o+=f'<rect x="{i}" y="{j}" width="20" height="20" fill="{c1 if (i+j)//20%2 else c2}"/><circle cx="{i+10}" cy="{j+10}" r="6" fill="none" stroke="{c2 if (i+j)//20%2 else c1}" stroke-width="1.2"/><path d="M{i},{j+10}h20M{i+10},{j}v20" stroke="{c2 if (i+j)//20%2 else c1}" stroke-width=".5" opacity=".6"/>'
    return f'<g opacity=".35">{o}</g>'
def barres(): return ''.join(f'<rect x="{x}" y="0" width="7" height="140" fill="#D7262B"/>' for x in (15,35,55,75))+'<rect x="10" y="14" width="80" height="14" rx="3" fill="#F2C230"/><rect x="8" y="106" width="84" height="12" rx="3" fill="#F2C230"/>'
def estrelles(): return ''.join(f'<circle cx="{rnd.uniform(4,96):.1f}" cy="{rnd.uniform(4,136):.1f}" r="{rnd.choice([.4,.6,.8,1.1])}" fill="#FFE3A3" opacity="{rnd.uniform(.4,1):.2f}"/>' for _ in range(70))
def muntanyes(c): return f'<path d="M0,140V112L14,96L24,106L40,84L54,102L66,90L80,104L100,86V140Z" fill="{c}" opacity=".55"/><path d="M40,84L45,91L49,88M80,104" fill="none" stroke="#fff" stroke-width="1.2" opacity=".5"/>'
def trencadis():
    o=''
    cols=['#2F8F9D','#E8B04A','#D9573B','#3E6FB0','#F1E7D0','#6BAA4F']
    for _ in range(140):
        x,y=rnd.uniform(-5,100),rnd.uniform(-5,140); r=rnd.uniform(3,7)
        pts=' '.join(f'{x+r*math.cos(a+rnd.uniform(-.4,.4)):.1f},{y+r*math.sin(a+rnd.uniform(-.4,.4)):.1f}' for a in [k*2*math.pi/5 for k in range(5)])
        o+=f'<polygon points="{pts}" fill="{rnd.choice(cols)}" stroke="#F4EEDD" stroke-width=".8"/>'
    return f'<g opacity=".55">{o}</g>'
def corbes(c): return ''.join(f'<path d="M-5,{y} C20,{y-10} 35,{y+12} 55,{y} S90,{y-8} 105,{y+4}" fill="none" stroke="{c}" stroke-width=".8" opacity=".35"/>' for y in range(10,140,7))
def vinya(): return ''.join(f'<circle cx="{x}" cy="{y}" r="2.6" fill="#7A3B6E" opacity=".5"/>' for x in range(8,100,10) for y in range(8,140,10) if (x+y)%3) 
def cuir(): return '<rect x="9" y="9" width="82" height="122" rx="3" fill="none" stroke="#E9C9A0" stroke-width=".8" stroke-dasharray="2 2" opacity=".7"/>'
def albada(): return '<defs><linearGradient id="alb" x1="0" y1="0" x2="0" y2="1"><stop offset="0" stop-color="#2B2457"/><stop offset=".55" stop-color="#C2456A"/><stop offset="1" stop-color="#F2A65A"/></linearGradient></defs><rect width="100" height="140" fill="url(#alb)"/>'
def neu(): return ''.join(f'<path d="M{x},{y-2.5}v5M{x-2.2},{y-1.2}l4.4,2.4M{x-2.2},{y+1.2}l4.4,-2.4" stroke="#FFFFFF" stroke-width=".6" opacity=".55"/>' for x,y in [(rnd.uniform(5,95),rnd.uniform(5,135)) for _ in range(40)])
def castellers(c): return ''.join(f'<g opacity=".18" fill="{c}"><circle cx="{x}" cy="{y}" r="2.2"/><rect x="{x-2.6}" y="{y+2.6}" width="5.2" height="6" rx="1.5"/></g>' for x in range(10,100,12) for y in range(12,140,14))
def barretines(): return ''.join(f'<path d="M{x-5},{y+3}c0,-6 2,-9 6,-9c3,0 4,2 3,4c-1,2 -3,1 -3,3h4v2h-10z" fill="#D7262B" opacity=".3"/>' for x in range(10,100,16) for y in range(14,140,18))

PORTADES=[
 ('verd-bosc','Verd bosc',tapa('#1F4A33','#E8C36A')),
 ('negre-plata','Negre i plata',tapa('#121418','#C3CCD6')),
 ('mar','Mar Mediterrani',tapa('#11506E','#E8C36A',ones('#8FD3F4'))),
 ('rajola','Rajola hidràulica',tapa('#7A3324','#F4E4C8',rajola('#B85C3C','#F4E4C8'))),
 ('senyera','Quatre barres',tapa('#F2C230','#7A1418',barres(),sil=False,text='#7A1418')),
 ('cuir','Cuir de viatge',tapa('#6B4224','#E9C9A0',cuir())),
 ('nit','Nit estrellada',tapa('#0E1430','#FFE3A3',estrelles())),
 ('pirineu','Pirineu',tapa('#2E4A6B','#F2F5F8',muntanyes('#1A2B40'))),
 ('trencadis','Trencadís',tapa('#F4EEDD','#7A3324',trencadis(),text='#7A3324')),
 ('vinya','Vinya',tapa('#3E1C3A','#E8C36A',vinya())),
 ('crema','Crema i or',tapa('#EFE6D2','#A87A2A',text='#7A5410')),
 ('topografic','Mapa topogràfic',tapa('#24302A','#D9C27A',corbes('#D9C27A'))),
 ('castellers','Castellers',tapa('#5A1A20','#E8C36A',castellers('#E8C36A'))),
 ('barretina','Barretina',tapa('#1E1E22','#D7262B',barretines(),text='#E8C36A')),
 ('neu','Neu',tapa('#5C86A8','#F2F5F8',neu())),
 ('albada','Albada',tapa('#2B2457','#FFE3A3',albada())),
]

# Colors secundaris: substitueixen l'ambre (botons, municipis descoberts, xifres). Fons i textos no canvien.
COLORS=[('vermell','Vermell','#E5484D'),('blau-mar','Blau mar','#4FA3E0'),('verd-bosc','Verd bosc','#4CB782'),('lila','Lila','#A27BEA'),
 ('rosa','Rosa','#F07AAA'),('turquesa','Turquesa','#2EC4B6'),('taronja','Taronja','#F28C38'),('llimona','Llimona','#E8D44D'),
 ('coure','Coure','#D08A5C'),('plata','Plata','#C3CCD6'),('menta','Menta','#7FD1A0'),('corall','Corall','#FF7F6B'),('cirera','Cirera','#C2445A'),('cel','Blau cel','#8FD3F4')]
def mostra_color(c):
    # Un tros de mapa amb municipis descoberts, un botó i una xifra, com es veurien a l'app.
    hexes=''.join(f'<path d="M{x},{y}l9,-5l9,5v10l-9,5l-9,-5z" fill="{f}" stroke="#0B0E13" stroke-width="1"/>'
      for x,y,f in [(6,14,c),(24,14,c+'99'),(42,14,'#1C2330'),(15,29,c),(33,29,'#2A2618'),(51,29,'#1C2330'),(6,44,'#1C2330'),(24,44,c+'99'),(42,44,'#161B23')])
    return (f'<svg viewBox="0 0 100 100"><rect width="100" height="100" rx="12" fill="#0B0E13"/>{hexes}'
      f'<text x="66" y="30" font-size="16" fill="{c}" {TXT}>+50</text>'
      f'<rect x="8" y="70" width="84" height="18" rx="9" fill="{c}"/><text x="50" y="82.5" text-anchor="middle" font-size="8" fill="#1A1305" {TXT}>DESBLOQUEJA</text></svg>')

# Animacions de càrrega: dibuixos d'una línia, en ambre, que es mouen sols. Classes amb el prefix c-.
A='#F2B544'; AF='#B8862F'
def L(d,w=2.4,c=A,x=''): return f'<path d="{d}" fill="none" stroke="{c}" stroke-width="{w}" stroke-linecap="round" stroke-linejoin="round" {x}/>'
def C(x,y,r,c=A,x2=''): return f'<circle cx="{x}" cy="{y}" r="{r}" fill="{c}" {x2}/>'
def G(cls,inner,orig=None,x=''): return f'<g class="{cls}" style="transform-box:view-box;transform-origin:{orig or "50px 50px"}" {x}>{inner}</g>'
persona=lambda x,y,s=1,c=A: C(x,y-7*s,2.4*s,c)+f'<path d="M{x-3*s},{y-3.5*s}h{6*s}l{1*s},{9*s}h{-8*s}z" fill="{c}"/>'
ANIMACIONS=[
 ('castell','Castell que es fa',''.join(G('c-pis',''.join(persona(50+dx,86-14*i,1) for dx in ([-12,0,12] if i==0 else [-6,6] if i==1 else [0])),x=f'style="--i:{i};transform-box:view-box"') for i in range(3))+L('M20,90h60',1.4,AF)),
 ('fanal','Fanal que s\'encén',G('c-halo',C(50,52,26,A,'opacity=".18"'))+L('M50,14v10M38,26h24l-4,8H42z',2.2)+G('c-vidre',f'<path d="M42,34h16l-3,26H45z" fill="{A}"/>')+L('M40,60h20M50,60v26',2.2)),
 ('porro','Porró que aboca',G('c-porro',L('M34,62c-6,-14 6,-24 16,-24s22,10 16,24z',2.2)+L('M50,38v-14M66,52l18,-14',2.2),'50px 60px')+G('c-raig',L('M84,38q8,10 2,30',1.6,A,'stroke-dasharray="2 3"'))),
 ('tio','El tió es mou',G('c-tio',f'<rect x="22" y="56" width="50" height="22" rx="11" fill="{AF}"/><circle cx="72" cy="67" r="11" fill="{A}"/>'+C(76,64,1.6,'#1A1305')+L('M64,76q4,3 8,0',1.4,'#1A1305'),'50px 78px')+G('c-bastonet',L('M20,30l22,20',3),'20px 30px')),
 ('bastons','Ball de bastons',G('c-bas-e',L('M30,76L58,28',3.4),'30px 76px')+G('c-bas-d',L('M70,76L42,28',3.4),'70px 76px')+G('c-xoc',L('M50,22v-8M42,24l-5,-6M58,24l5,-6',2,AF))),
 ('gegants','Gegants ballant',G('c-gegant',persona(34,80,2.6),'34px 84px')+G('c-gegant2',persona(66,80,2.6,AF),'66px 84px')),
 ('drac','El drac treu foc',L('M14,70c6,-14 20,-20 34,-16l12,-8c6,-2 10,2 10,6l-6,6c-6,8 -20,14 -34,14',2.4)+C(62,50,1.8)+''.join(G('c-foc',C(80+i*6,52-i*2,3-i*.6,A),x=f'style="--i:{i};transform-box:view-box"') for i in range(3))),
 ('correfoc','Espurnes de correfoc',L('M50,86V60',2.6,AF)+''.join(G('c-espurna',C(50,58,1.8),x=f'style="--i:{i};--a:{a}deg;transform-box:view-box;transform-origin:50px 58px"') for i,a in enumerate(range(-80,81,20)))),
 ('bici','Pedalant',G('c-roda',C(30,64,13,'none',f'stroke="{A}" stroke-width="2.4"')+L('M30,51v26M17,64h26',1,AF),'30px 64px')+G('c-roda',C(72,64,13,'none',f'stroke="{A}" stroke-width="2.4"')+L('M72,51v26M59,64h26',1,AF),'72px 64px')+L('M30,64l14,-22h18l10,22M44,42l8,22l10,-22M58,36h8',2.2)),
 ('segell','El segell pica',G('c-segell',f'<rect x="26" y="34" width="48" height="30" rx="4" fill="none" stroke="{A}" stroke-width="2.6"/>'+L('M34,49h32M38,56h24',1.6),'50px 49px')+G('c-onada',f'<rect x="22" y="30" width="56" height="38" rx="6" fill="none" stroke="{AF}" stroke-width="1.6"/>','50px 49px')),
 ('bruixola','Brúixola',C(50,52,30,'none',f'stroke="{A}" stroke-width="2.4"')+L('M50,26v4M50,74v4M24,52h4M72,52h4',2,AF)+G('c-agulla',f'<path d="M50,30l5,22h-10z" fill="{A}"/><path d="M50,74l5,-22h-10z" fill="{AF}"/>','50px 52px')),
 ('sol-lluna','Sol i lluna',G('c-cel',C(50,22,8)+''.join(L(f'M{50+11*math.cos(math.radians(a)):.1f},{22+11*math.sin(math.radians(a)):.1f}L{50+14*math.cos(math.radians(a)):.1f},{22+14*math.sin(math.radians(a)):.1f}',1.6) for a in range(0,360,45))
     +f'<path d="M46,74a9,9 0 1 0 10,10a7,7 0 1 1 -10,-10z" fill="{AF}"/>','50px 52px')+L('M10,52h80',1,AF,'opacity=".4"')),
 ('onades','Onades',''.join(G('c-ona',L(f'M-20,{y}q10,-7 20,0t20,0t20,0t20,0t20,0t20,0t20,0',2.2,A if i%2==0 else AF),x=f'style="--i:{i};transform-box:view-box"') for i,y in enumerate((42,54,66)))),
 ('estel','L\'estel vola',G('c-estel',f'<path d="M50,14l14,20l-14,24l-14,-24z" fill="{A}"/>'+L('M50,58q-8,10 0,18t0,16',1.6,AF),'50px 58px')),
 ('caragol','A poc a poc',G('c-caragol',f'<path d="M24,76h46c4,0 6,-3 6,-6l4,-8" fill="none" stroke="{A}" stroke-width="2.4" stroke-linecap="round"/>'+C(48,60,14,'none',f'stroke="{A}" stroke-width="2.4"')+L('M48,60m-6,0a6,6 0 1 1 6,6',2,AF)+L('M80,62l2,-8M76,62l-2,-8',1.4),'50px 70px')+L('M14,80h72',1,AF,'opacity=".4"')),
 ('campana','La campana toca',L('M50,12v6',2)+G('c-campana',f'<path d="M36,30c0,-8 28,-8 28,0l2,20c2,10 6,14 12,16H22c6,-2 10,-6 12,-16z" fill="{A}"/>'+C(50,72,4,AF),'50px 18px')+''.join(G('c-so',L(f'M{x},40q{d*5},10 0,20',2,AF),x=f'style="--i:{i};transform-box:view-box"') for i,(x,d) in enumerate([(14,-1),(86,1)]))),
]
CSS_ANIM='''
.c-pis{animation:cpis 2.4s ease-in-out infinite;animation-delay:calc(var(--i)*.3s)}@keyframes cpis{0%,10%{opacity:0;transform:translateY(-8px)}25%,75%{opacity:1;transform:none}90%,100%{opacity:0}}
.c-halo{animation:chalo 1.6s ease-in-out infinite}@keyframes chalo{0%,100%{opacity:.3;transform:scale(.85)}50%{opacity:1;transform:scale(1.1)}}
.c-vidre{animation:cvidre 1.6s steps(1) infinite}@keyframes cvidre{0%,100%{opacity:1}55%{opacity:.35}62%{opacity:1}70%{opacity:.5}}
.c-porro{animation:cporro 2s ease-in-out infinite}@keyframes cporro{0%,100%{transform:rotate(0)}40%,70%{transform:rotate(-14deg)}}
.c-raig{animation:craig 2s ease-in-out infinite}@keyframes craig{0%,30%,85%,100%{opacity:0}45%,70%{opacity:1}}
.c-tio{animation:ctio .8s ease-in-out infinite alternate}@keyframes ctio{from{transform:rotate(-3deg)}to{transform:rotate(3deg)}}
.c-bastonet{animation:cbast .8s ease-in-out infinite alternate}@keyframes cbast{from{transform:rotate(-20deg)}to{transform:rotate(10deg)}}
.c-bas-e{animation:cbase .6s ease-in-out infinite alternate}@keyframes cbase{from{transform:rotate(-14deg)}to{transform:rotate(4deg)}}
.c-bas-d{animation:cbasd .6s ease-in-out infinite alternate}@keyframes cbasd{from{transform:rotate(14deg)}to{transform:rotate(-4deg)}}
.c-xoc{animation:cxoc .6s ease-in-out infinite alternate}@keyframes cxoc{from{opacity:0}to{opacity:1}}
.c-gegant{animation:cgeg 1.6s ease-in-out infinite}.c-gegant2{animation:cgeg 1.6s ease-in-out infinite reverse}@keyframes cgeg{0%,100%{transform:rotate(-7deg)}50%{transform:rotate(7deg)}}
.c-foc{animation:cfoc 1s ease-out infinite;animation-delay:calc(var(--i)*.15s)}@keyframes cfoc{0%{opacity:0;transform:translateX(-6px) scale(.4)}40%{opacity:1}100%{opacity:0;transform:translateX(8px) scale(1.4)}}
.c-espurna{animation:cesp 1.2s ease-out infinite;animation-delay:calc(var(--i)*.09s)}@keyframes cesp{0%{opacity:1;transform:rotate(var(--a)) translateY(0)}100%{opacity:0;transform:rotate(var(--a)) translateY(-34px)}}
.c-roda{animation:croda 1s linear infinite}@keyframes croda{to{transform:rotate(360deg)}}
.c-segell{animation:cseg 1.6s cubic-bezier(.5,0,.75,0) infinite}@keyframes cseg{0%{opacity:0;transform:scale(1.8) rotate(-8deg)}35%,85%{opacity:1;transform:scale(1) rotate(0)}100%{opacity:0}}
.c-onada{animation:conada 1.6s ease-out infinite}@keyframes conada{0%,34%{opacity:0;transform:scale(.9)}40%{opacity:.8}70%,100%{opacity:0;transform:scale(1.25)}}
.c-agulla{animation:cagu 2.4s ease-in-out infinite}@keyframes cagu{0%,100%{transform:rotate(-30deg)}50%{transform:rotate(40deg)}}
.c-cel{animation:ccel 4s linear infinite}@keyframes ccel{to{transform:rotate(360deg)}}
.c-ona{animation:cona 1.4s linear infinite;animation-delay:calc(var(--i)*-.4s)}@keyframes cona{to{transform:translateX(-20px)}}
.c-estel{animation:cestel 1.8s ease-in-out infinite}@keyframes cestel{0%,100%{transform:translate(-6px,2px) rotate(-8deg)}50%{transform:translate(6px,-4px) rotate(8deg)}}
.c-caragol{animation:ccar 4s linear infinite}@keyframes ccar{from{transform:translateX(-22px)}to{transform:translateX(18px)}}
.c-campana{animation:ccamp .8s ease-in-out infinite alternate}@keyframes ccamp{from{transform:rotate(-26deg)}to{transform:rotate(26deg)}}
.c-so{animation:cso .8s ease-out infinite;animation-delay:calc(var(--i)*.4s)}@keyframes cso{0%{opacity:0}40%{opacity:1}100%{opacity:0}}
@media (prefers-reduced-motion:reduce){[class^="c-"],[class*=" c-"]{animation:none!important}}
'''
dades={
 'portades':[{'id':i,'nom':n,'svg':s.replace('CLIP','clip_'+i)} for i,n,s in PORTADES],
 'colors':[{'id':i,'nom':n,'hex':h,'svg':mostra_color(h)} for i,n,h in COLORS],
 'animacions':[{'id':i,'nom':n,'svg':f'<svg viewBox="0 0 100 100">{s}</svg>'} for i,n,s in ANIMACIONS],
 'css':CSS_ANIM,
}
json.dump(dades,open(os.path.join(AQUI,'extres.json'),'w'),ensure_ascii=False)
print(len(PORTADES),len(COLORS),len(ANIMACIONS))

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
def muntanyes(c): return f'<path d="M0,140V112L14,96L24,106L40,84L54,102L66,90L80,104L100,86V140Z" fill="{c}" opacity=".55"/><path d="M35,90L40,84L45,90L42,89L40,92L38,89Z" fill="#fff" opacity=".6"/><path d="M76,92L80,88L84,92L82,91L80,93L78,91Z" fill="#fff" opacity=".6"/>'
def trencadis():
    o=''
    cols=['#2F8F9D','#E8B04A','#D9573B','#3E6FB0','#F1E7D0','#6BAA4F']
    for _ in range(140):
        x,y=rnd.uniform(-5,100),rnd.uniform(-5,140); r=rnd.uniform(3,7)
        pts=' '.join(f'{x+r*math.cos(a+rnd.uniform(-.4,.4)):.1f},{y+r*math.sin(a+rnd.uniform(-.4,.4)):.1f}' for a in [k*2*math.pi/5 for k in range(5)])
        o+=f'<polygon points="{pts}" fill="{rnd.choice(cols)}" stroke="#F4EEDD" stroke-width=".8"/>'
    return f'<g opacity=".55">{o}</g>'
def corbes(c):
    # Mapa topogràfic: corbes de nivell tancades al voltant de dos cims, amb el triangle del vèrtex i la quadrícula.
    o=''.join(f'<ellipse cx="34" cy="48" rx="{6+i*6}" ry="{4+i*5}" transform="rotate(-15 34 48)"/>' for i in range(6))
    o+=''.join(f'<ellipse cx="72" cy="104" rx="{5+i*6}" ry="{4+i*4.5}" transform="rotate(20 72 104)"/>' for i in range(5))
    grid=''.join(f'<path d="M{x},0V140"/>' for x in range(0,101,20))+''.join(f'<path d="M0,{y}H100"/>' for y in range(0,141,20))
    return (f'<g fill="none" stroke="{c}" stroke-width=".8" opacity=".45">{o}</g><g stroke="{c}" stroke-width=".4" opacity=".2">{grid}</g>'
        f'<path d="M34,44l3,5h-6z" fill="{c}" opacity=".8"/><path d="M72,100l3,5h-6z" fill="{c}" opacity=".8"/>')
def vinya():
    # Vinya: rengs de ceps amb fulles i raïms.
    def raim(x,y): return ''.join(f'<circle cx="{x+dx}" cy="{y+dy}" r="1.7" fill="#6A2A5E"/>' for dx,dy in [(-3,0),(0,0),(3,0),(-1.5,2.6),(1.5,2.6),(0,5.2)])
    fulla=lambda x,y: f'<path d="M{x},{y}l-4,-2l1,-3l3,1l0,-3l3,2l1,3l-2,2z" fill="#4E7A3A"/>'
    rengs=''.join(f'<path d="M0,{y}q25,-4 50,0t50,0" fill="none" stroke="#8A5A34" stroke-width="1.4"/>' for y in range(34,140,26))
    return '<g opacity=".7">'+rengs+''.join(fulla(x,y-3)+raim(x+4,y) for x in range(10,100,22) for y in range(36,140,26))+'</g>'
def cuir(): return '<rect x="9" y="9" width="82" height="122" rx="3" fill="none" stroke="#E9C9A0" stroke-width=".8" stroke-dasharray="2 2" opacity=".7"/>'
def albada(): return '<defs><linearGradient id="alb" x1="0" y1="0" x2="0" y2="1"><stop offset="0" stop-color="#2B2457"/><stop offset=".55" stop-color="#C2456A"/><stop offset="1" stop-color="#F2A65A"/></linearGradient></defs><rect width="100" height="140" fill="url(#alb)"/>'
def neu(): return ''.join(f'<path d="M{x},{y-2.5}v5M{x-2.2},{y-1.2}l4.4,2.4M{x-2.2},{y+1.2}l4.4,-2.4" stroke="#FFFFFF" stroke-width=".6" opacity=".55"/>' for x,y in [(rnd.uniform(5,95),rnd.uniform(5,135)) for _ in range(40)])
def castellers(c):
    # Castells: pisos de persones que s'enfilen, amb l'enxaneta a dalt.
    p=lambda x,y,k=1: f'<circle cx="{x}" cy="{y-5*k}" r="{1.6*k}"/><path d="M{x-2*k},{y-3*k}h{4*k}l{.6*k},{6*k}h{-5.2*k}z"/>'
    def castell(cx,base,k):
        o=''
        for i,n in enumerate([4,3,2,1]):
            for j in range(n): o+=p(cx+(j-(n-1)/2)*5.2*k, base-i*8.5*k, k)
        return o
    return f'<g fill="{c}" opacity=".3">'+castell(20,136,1)+castell(80,136,1)+castell(50,136,1.2)+'</g>'
BARR_COS='M50,290 L48,222 C30,226 6,214 4,180 C2,140 28,92 70,78 C120,62 200,66 245,90 C284,112 298,160 300,232 L301,260 Z'
BARR_VORA='M38,271 L312,233 C318,232 321,235 322,241 L325,270 C326,276 323,280 317,281 L45,318 C39,319 36,316 35,310 L32,281 C31,275 33,272 38,271 Z'
def barretines():
    # La mateixa barretina dels emojis (calcada del dibuix de referència), petita i repetida.
    una=f'<path d="{BARR_COS}" fill="#C8262C"/><path d="M188,104 C210,110 232,124 246,150" fill="none" stroke="#A01E25" stroke-width="14" stroke-linecap="round"/><path d="{BARR_VORA}" fill="#0E0E10"/>'
    return ''.join(f'<g transform="translate({x} {y}) scale(.075) translate(-180 -190)" opacity=".85">{una}</g>' for x,y in [(16,34),(84,30),(14,92),(86,96),(30,128),(70,128)])


def montserrat(c):
    # Les agulles arrodonides de Montserrat, una al costat de l'altra.
    o='M0,140V112'
    x=0
    for h,w in [(10,6),(18,7),(14,6),(24,8),(30,7),(22,6),(34,8),(26,7),(16,6),(28,8),(20,7),(12,6)]:
        o+=f'C{x},{112-h} {x+w},{112-h} {x+w},112'
        x+=w
    o+=f'H100V140Z'
    return f'<path d="{o}" fill="{c}" opacity=".7"/><path d="{o}" fill="none" stroke="#E8C36A" stroke-width=".6" opacity=".4"/>'
def delta(): return ''.join(f'<path d="M0,{y}h100" stroke="#9CC27A" stroke-width=".8" opacity=".35"/>' for y in range(40,140,5))+''.join(f'<path d="M{x},{y}q3,-3 6,0q3,-3 6,0" fill="none" stroke="#F4EEDD" stroke-width=".8" opacity=".6"/>' for x,y in [(14,40),(70,48),(40,32),(80,30)])
def costa(): return '<path d="M0,96C20,90 30,104 50,98S80,86 100,92V140H0Z" fill="#E8D2A0" opacity=".5"/><path d="M0,100C20,94 30,108 50,102S80,90 100,96" fill="none" stroke="#FFFFFF" stroke-width="1" opacity=".6"/>'
def terrats(c): return f'<path d="M0,140V112h10v-8h8v12h10v-18h6v-6h4v24h12v-10h10v-14h8v20h10v-8h8v-6h6V140Z" fill="{c}" opacity=".6"/>'
def roses():
    # Roses de Sant Jordi: capoll de pètals en espiral, tija i fulles.
    def rosa(x,y): return (f'<g transform="translate({x} {y})"><path d="M0,4v12" stroke="#3E8A44" stroke-width="1.2"/><path d="M0,10q5,-3 6,1q-4,2 -6,-1z" fill="#3E8A44"/><path d="M0,12q-5,-3 -6,1q4,2 6,-1z" fill="#3E8A44"/>'
        f'<path d="M-5,0c0,-5 3,-7 5,-7s5,2 5,7c0,4 -2,5 -5,5s-5,-1 -5,-5z" fill="#C8202A"/><path d="M-2,-2c1,-2 4,-2 4,1c0,2 -3,2 -3,0" fill="none" stroke="#7A0E14" stroke-width=".8"/><path d="M-4,1c2,2 6,2 8,-1" fill="none" stroke="#7A0E14" stroke-width=".7"/></g>')
    return '<g opacity=".55">'+''.join(rosa(x,y) for x in range(14,100,24) for y in range(18,140,30))+'</g>'
def sardana_p(c): return ''.join(f'<g fill="{c}" opacity=".3"><circle cx="{50+38*math.cos(math.radians(a)):.1f}" cy="{70+44*math.sin(math.radians(a)):.1f}" r="2.4"/></g>' for a in range(0,360,20))
def gotic(c):
    # Silueta de la Sagrada Família: les torres punxegudes de la façana i la torre central més alta.
    # Torres en forma de bala, molt punxegudes, acabades amb un pinacle rodó.
    torre=lambda x,b,h,w: f'<path d="M{x-w},{b}V{b-h*.45}C{x-w},{b-h*.8} {x-w*.3},{b-h*.95} {x},{b-h}C{x+w*.3},{b-h*.95} {x+w},{b-h*.8} {x+w},{b-h*.45}V{b}Z"/><circle cx="{x}" cy="{b-h-2}" r="{w*.5}"/>'
    o=''.join(torre(x,140,h,w) for x,h,w in [(30,58,3.4),(38,66,3.6),(62,66,3.6),(70,58,3.4),(50,84,4.2),(18,40,3),(82,40,3)])
    o+='<path d="M8,140V118h84V140Z"/>'
    return f'<g fill="{c}" opacity=".45">{o}</g>'
def oliveres(): return ''.join(f'<ellipse cx="{x}" cy="{y}" rx="3.4" ry="1.3" fill="#B8C48A" opacity=".45" transform="rotate({(x*7+y)%60-30} {x} {y})"/>' for x in range(6,100,9) for y in range(8,140,10))
def pedra(): return ''.join(f'<rect x="{x+(6 if (y//10)%2 else 0)}" y="{y}" width="{rnd.uniform(9,13):.1f}" height="8" rx="3" fill="#C9B89A" opacity=".35"/>' for x in range(-6,100,13) for y in range(0,140,10))
def riu(): return '<path d="M30,0C60,30 10,60 50,90S70,130 60,140" fill="none" stroke="#5AB8E8" stroke-width="7" opacity=".5"/>'
def bolets():
    # Bolets de tardor: barret vermell amb punts blancs i peu blanc.
    def bolet(x,y,k): return (f'<g transform="translate({x} {y}) scale({k})"><path d="M-3,0 q-1,7 1,9h4q2,-2 1,-9z" fill="#F4EEDD"/>'
        f'<path d="M-9,1c0,-7 4,-11 9,-11s9,4 9,11z" fill="#C8302A"/><circle cx="-4" cy="-4" r="1.3" fill="#fff"/><circle cx="2" cy="-6" r="1.1" fill="#fff"/><circle cx="5" cy="-2" r="1" fill="#fff"/></g>')
    return '<g opacity=".7">'+''.join(bolet(x,y,k) for x,y,k in [(14,40,.9),(84,34,1),(20,96,1.1),(80,100,.9),(12,128,.8),(88,128,1),(50,128,.7)])+'</g>'
def fanal_p(): return '<defs><radialGradient id="llf" cx=".5" cy=".45" r=".7"><stop offset="0" stop-color="#F2B544" stop-opacity=".55"/><stop offset="1" stop-color="#0B0E13" stop-opacity="0"/></radialGradient></defs><rect width="100" height="140" fill="url(#llf)"/>'

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
 ('montserrat','Montserrat',tapa('#3A3E5C','#E8C36A',montserrat('#1F2238'))),
 ('delta','Delta de l\'Ebre',tapa('#2E5E3A','#F4EEDD',delta())),
 ('costa-brava','Costa Brava',tapa('#1F7A8C','#F4EEDD',costa())),
 ('terrats','Terrats de ciutat',tapa('#2A2F3A','#E8C36A',terrats('#151820'))),
 ('roses','Roses de Sant Jordi',tapa('#F3E6D6','#9E1C20',roses(),text='#9E1C20')),
 ('rotllana','Rotllana de sardana',tapa('#14324A','#E8C36A',sardana_p('#E8C36A'))),
 ('fanal','Llum de fanal',tapa('#0B0E13','#F2B544',fanal_p())),
 ('gotic','Gòtic',tapa('#4A4038','#E8D7B0',gotic('#E8D7B0'))),
 ('blau-intens','Blau intens',tapa('#1A2FA0','#E8ECF0')),
 ('oliveres','Oliveres',tapa('#5C6B3A','#F4EEDD',oliveres())),
 ('pedra-seca','Pedra seca',tapa('#6E5E48','#F4EEDD',pedra())),
 ('riu','Riu',tapa('#2E4A3A','#E8C36A',riu())),
 ('bolets','Tardor de bolets',tapa('#5A3A22','#F2C230',bolets())),
 ('vermell','Vermell i or',tapa('#B01E28','#F2C230')),
]

# Colors secundaris: substitueixen l'ambre (botons, municipis descoberts, xifres). Fons i textos no canvien.
COLORS=[('vermell','Vermell','#E5484D'),('blau-mar','Blau mar','#4FA3E0'),('verd-bosc','Verd bosc','#4CB782'),('lila','Lila','#A27BEA'),
 ('rosa','Rosa','#F07AAA'),('turquesa','Turquesa','#2EC4B6'),('taronja','Taronja','#F28C38'),('llimona','Llimona','#E8D44D'),
 ('coure','Coure','#D08A5C'),('plata','Plata','#C3CCD6'),('menta','Menta','#7FD1A0'),('corall','Corall','#FF7F6B'),('cirera','Cirera','#C2445A'),('cel','Blau cel','#8FD3F4'),
 ('or-vell','Or vell','#D4A63A'),('salvia','Sàlvia','#9CB89A'),('lavanda','Lavanda','#B9A5E8'),('magenta','Magenta','#E05AB8'),('indi','Indi','#7C83F2'),
 ('oliva','Oliva','#A8B04A'),('maduixa','Maduixa','#F25C7A'),('ocre','Ocre','#D9963A'),('aigua','Aigua','#5ED6E0'),('pressec','Préssec','#FFB38A'),
 ('llima','Verd llima','#A6E05A'),('acer','Blau acer','#7AA2C8'),('sorra','Sorra','#E2C48F'),('gerd','Gerd','#E0457B')]
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
def G(cls,inner,orig=None,x=''):
    # Un sol atribut style: el que es passi a x (variables com --i) s'hi afegeix.
    extra=x[len('style="'):-1].replace('transform-box:view-box','').strip(';') if x.startswith('style="') else ''
    estil=f'transform-box:view-box;transform-origin:{orig or "50px 50px"}'+(';'+extra if extra else '')
    return f'<g class="{cls}" style="{estil}">{inner}</g>'
persona=lambda x,y,s=1,c=A: C(x,y-7*s,2.4*s,c)+f'<path d="M{x-3*s},{y-3.5*s}h{6*s}l{1*s},{9*s}h{-8*s}z" fill="{c}"/>'
ANIMACIONS=[
 ('castell','Castell que es fa',''.join(G('c-pis',''.join(persona(50+dx,86-14*i,1) for dx in ([-12,0,12] if i==0 else [-6,6] if i==1 else [0])),x=f'style="--i:{i};transform-box:view-box"') for i in range(3))+L('M20,90h60',1.4,AF)),
 ('fanal','Fanal que s\'encén',L('M18,22h18M22,22v-4',2.2,AF)+L('M36,22c8,0 12,4 12,10',2.2,AF)+G('c-halo',C(48,58,24,A,'opacity=".18"'))+L('M48,32v4M40,36h16l-2,4H42z',2)+G('c-vidre',f'<path d="M41,40h14l-2,24H43z" fill="{A}"/>')+L('M41,40h14l-2,24H43zM48,40v24',1.4,'#7A5410')+L('M42,64h12l-2,4H44z',2)),
 ('porro','Porró que aboca',G('c-porro',f'<path d="M36,72c-10,0 -14,-8 -10,-16c3,-6 10,-8 12,-14V30h8v12c2,6 9,8 12,14c4,8 0,16 -10,16z" fill="none" stroke="{A}" stroke-width="2.4" stroke-linejoin="round"/>'+L('M54,58l22,-20',3)+L('M38,30h8',3)+f'<path d="M29,62c3,4 14,4 18,0v6c-4,3 -15,3 -18,0z" fill="{AF}" opacity=".7"/>','42px 60px')+G('c-raig',L('M78,36q6,14 0,34',1.6,A,'stroke-dasharray="2 3"'))),
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

 ('rotllana','Rotllana de sardana',G('c-rotllana',''.join(persona(50+28*math.cos(math.radians(a)),58+10*math.sin(math.radians(a)),.9,A if math.sin(math.radians(a))>0 else AF) for a in range(0,360,45)),'50px 54px')+L('M18,74q32,12 64,0',1,AF,'opacity=".4"')),
 ('castanyes','Castanyes torrant',L('M24,66h52l-6,14H30z',2.2,AF)+''.join(G('c-bot',f'<path d="M{x-5},62c0,-7 4,-10 5,-10s5,3 5,10z" fill="{A}"/>',x=f'style="--i:{i};transform-box:view-box"') for i,x in enumerate((36,50,64)))+''.join(G('c-fum',L(f'M{x},44q-3,-6 0,-12t0,-12',1.6,AF),x=f'style="--i:{i};transform-box:view-box"') for i,x in enumerate((40,56)))),
 ('cremallera','Tren cremallera',L('M10,84L90,30',1.6,AF)+L('M14,90L94,36',1,AF,'stroke-dasharray="2 3"')+G('c-tren',f'<g transform="rotate(-34 50 57)"><rect x="34" y="46" width="32" height="16" rx="4" fill="{A}"/><rect x="38" y="49" width="7" height="6" fill="#0B0E13"/><rect x="48" y="49" width="7" height="6" fill="#0B0E13"/><rect x="58" y="49" width="5" height="6" fill="#0B0E13"/></g>')),
 ('globus','Globus que puja',G('c-globus',C(50,40,18)+L('M38,52l8,16M62,52l-8,16',1.4,AF)+f'<rect x="44" y="68" width="12" height="9" rx="2" fill="{AF}"/>')),
 ('ocell','Ocell volant',G('c-ocell',G('c-ala',L('M30,50q10,-12 20,0q10,-12 20,0',2.6),'50px 50px'))),
 ('flor','Flor que s\'obre',L('M50,88V52',2.2,AF)+L('M50,74q-10,-6 -14,-14',1.8,AF)+G('c-flor',''.join(f'<ellipse cx="50" cy="38" rx="5" ry="11" fill="{A}" transform="rotate({a} 50 50)"/>' for a in range(0,360,60))+C(50,50,5,AF),'50px 50px')),
 ('cor','Cor que batega',G('c-cor',f'<path d="M50,78C24,60 22,42 32,34c8,-6 16,-2 18,6c2,-8 10,-12 18,-6c10,8 8,26 -18,44z" fill="{A}"/>')),
 ('punts','Tres punts',''.join(G('c-punt',C(30+20*i,52,6),x=f'style="--i:{i};transform-box:view-box"') for i in range(3))),
 ('barretina','La barretina salta',G('c-salt',f'<path d="M26,66c0,-20 10,-34 26,-36c10,-1 16,4 14,12c-2,6 -10,6 -12,12c-1,4 2,8 6,12z" fill="{A}"/>'+f'<rect x="22" y="64" width="44" height="7" rx="2" fill="{AF}"/>','50px 72px')),
 ('gota','Gota que cau',G('c-gota',f'<path d="M50,20c-7,11 -11,18 -11,24a11,11 0 0 0 22,0c0,-6 -4,-13 -11,-24z" fill="{A}"/>')+G('c-esquitx',L('M36,82q14,-8 28,0',2,AF))),
 ('rellotge','Rellotge',C(50,52,30,'none',f'stroke="{A}" stroke-width="2.4"')+G('c-minuts',L('M50,52V30',2.4),'50px 52px')+G('c-hores',L('M50,52h12',3,AF),'50px 52px')),
 ('foguera','Foguera',L('M30,82l40,-8M30,74l40,8',3,AF)+G('c-flama',f'<path d="M50,74c-12,-4 -14,-18 -4,-30c0,8 6,10 8,4c8,8 10,22 -4,26z" fill="{A}"/>','50px 76px')),
 ('cotxe','Carretera i manta',''.join(G('c-ratlla',L(f'M{x},82h10',2,AF),x=f'style="--i:{i};transform-box:view-box"') for i,x in enumerate((10,40,70)))+G('c-cotxe',f'<path d="M22,64h56v-8l-10,-4l-8,-10H40l-8,10l-10,4z" fill="{A}"/>'+C(34,66,6,AF)+C(66,66,6,AF))),
 ('espiga','Espiga al vent',''.join(G('c-espiga',L(f'M{x},88V44',1.8,AF)+''.join(f'<ellipse cx="{x+(3 if k%2 else -3)}" cy="{44+k*5}" rx="2.4" ry="4" fill="{A}" transform="rotate({20 if k%2 else -20} {x+(3 if k%2 else -3)} {44+k*5})"/>' for k in range(5)),f'{x}px 88px',f'style="--i:{i};transform-box:view-box;transform-origin:{x}px 88px"') for i,x in enumerate((36,50,64)))),
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

.c-rotllana{animation:crot 6s linear infinite}@keyframes crot{0%,100%{transform:translateX(-3px)}50%{transform:translateX(3px)}}
.c-bot{animation:cbot .6s ease-in-out infinite alternate;animation-delay:calc(var(--i)*.15s)}@keyframes cbot{from{transform:none}to{transform:translateY(-4px)}}
.c-fum{animation:cfum 1.6s ease-out infinite;animation-delay:calc(var(--i)*.5s)}@keyframes cfum{0%{opacity:0;transform:translateY(6px)}40%{opacity:1}100%{opacity:0;transform:translateY(-8px)}}
.c-tren{animation:ctren 2.4s ease-in-out infinite}@keyframes ctren{0%{transform:translate(-20px,13px)}100%{transform:translate(20px,-13px)}}
.c-globus{animation:cglob 2.4s ease-in-out infinite alternate}@keyframes cglob{from{transform:translateY(10px)}to{transform:translateY(-8px)}}
.c-ocell{animation:cocell 3s ease-in-out infinite}@keyframes cocell{0%,100%{transform:translate(-8px,6px)}50%{transform:translate(8px,-6px)}}
.c-ala{animation:cala .4s ease-in-out infinite alternate}@keyframes cala{from{transform:scaleY(1)}to{transform:scaleY(-.6)}}
.c-flor{animation:cflor 2s ease-in-out infinite}@keyframes cflor{0%,100%{transform:scale(.6) rotate(0)}50%{transform:scale(1) rotate(30deg)}}
.c-cor{animation:ccor 1s ease-in-out infinite}@keyframes ccor{0%,100%{transform:scale(1)}15%{transform:scale(1.12)}30%{transform:scale(1)}45%{transform:scale(1.08)}}
.c-punt{animation:cpunt .9s ease-in-out infinite;animation-delay:calc(var(--i)*.15s)}@keyframes cpunt{0%,60%,100%{transform:none}30%{transform:translateY(-10px)}}
.c-salt{animation:csalt 1s cubic-bezier(.3,1.6,.5,1) infinite}@keyframes csalt{0%,100%{transform:none}40%{transform:translateY(-14px) rotate(-6deg)}}
.c-gota{animation:cgota 1.4s ease-in infinite}@keyframes cgota{0%{opacity:0;transform:translateY(-10px)}20%{opacity:1}80%{opacity:1;transform:translateY(30px)}100%{opacity:0;transform:translateY(30px)}}
.c-esquitx{animation:cesq 1.4s ease-out infinite}@keyframes cesq{0%,78%{opacity:0}85%{opacity:1}100%{opacity:0}}
.c-minuts{animation:cmin 2s linear infinite}@keyframes cmin{to{transform:rotate(360deg)}}.c-hores{animation:cmin 24s linear infinite}
.c-flama{animation:cflam .4s ease-in-out infinite alternate}@keyframes cflam{from{transform:scale(1,1)}to{transform:scale(.9,1.12)}}
.c-ratlla{animation:crat .8s linear infinite;animation-delay:calc(var(--i)*-.27s)}@keyframes crat{from{transform:translateX(20px)}to{transform:translateX(-20px)}}
.c-cotxe{animation:ccotxe .3s ease-in-out infinite alternate}@keyframes ccotxe{from{transform:none}to{transform:translateY(-1.5px)}}
.c-espiga{animation:cespi 2s ease-in-out infinite;animation-delay:calc(var(--i)*.2s)}@keyframes cespi{0%,100%{transform:rotate(-8deg)}50%{transform:rotate(8deg)}}
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

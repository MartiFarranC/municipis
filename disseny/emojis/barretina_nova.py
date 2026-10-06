import re as _re
# Barretina: calcada del dibuix de referència del Martí (l'adhesiu de «La barretina del tió»).
# Les formes es dibuixen en les coordenades del dibuix i es passen a les d'un cap de centre (50,58) i radi 33.
# Cos vermell alt que puja pel costat dret, la punta plegada en un lòbul ample que cau per l'esquerra
# fins a mitja alçada, la vora negra recta (una mica inclinada) i un reflex fosc a dalt a la dreta.
def _ref(d,K=.2):
    out=[];eix=0
    for t in _re.findall(r'[A-Za-z]|-?\d+\.?\d*',d):
        if t.isalpha(): out.append(t);eix=0;continue
        v=float(t); v=50+(v-180)*K if eix==0 else 43+(v-298)*K
        out.append(f'{v:.2f}'); eix^=1
    return ' '.join(out)
BARR_FORMES=[(_ref(d),c) for d,c in [
  ('M50,290 L48,222 C30,226 6,214 4,180 C2,140 28,92 70,78 C120,62 200,66 245,90 C284,112 298,160 300,232 L301,260 Z','#C8262C'),
]]
BARR_OMBRES=[(_ref('M50,266 L49,222 C64,218 79,212 91,205 C85,226 70,248 52,265 Z'),'#A01E25')]
BARR_VORA=[(_ref('M38,271 L312,233 C318,232 321,235 322,241 L325,270 C326,276 323,280 317,281 L45,318 C39,319 36,316 35,310 L32,281 C31,275 33,272 38,271 Z'),'#1E1E22')]
BARR_LINIES=[(_ref('M188,104 C210,110 232,124 246,150'),'#A01E25',14*.2)]
def _tr(d,cx,cy,k):
    out=[];eix=0
    for t in _re.findall(r'[A-Za-z]|-?\d+\.?\d*',d):
        if t.isalpha(): out.append(t);eix=0;continue
        v=float(t); v=cx+(v-50)*k if eix==0 else cy+(v-58)*k
        out.append(f'{v:.2f}'); eix^=1
    return ' '.join(out)
def barr_svg(cx,cy,r,contorn=False):
    k=r/33
    st=f' stroke="#14171C" stroke-width="{max(.9,2.2*k):.2f}" stroke-linejoin="round"' if contorn else ''
    p=lambda d,c,s='': f'<path d="{_tr(d,cx,cy,k)}" fill="{c}"{s}/>'
    o=''.join(p(d,c,st) for d,c in BARR_FORMES)
    if r>=8:
        o+=''.join(p(d,c) for d,c in BARR_OMBRES)
        o+=''.join(f'<path d="{_tr(d,cx,cy,k)}" fill="none" stroke="{c}" stroke-width="{w*k:.2f}" stroke-linecap="round"/>' for d,c,w in BARR_LINIES)
    o+=''.join(p(d,c,st) for d,c in BARR_VORA)
    return o

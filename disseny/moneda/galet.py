# Dibuixa un galet com un tub en espiral: una tira d el·lipses (les estries) que es fan grosses fins a la boca.
import math,sys
def galet(n=13,a0=200,a1=-70,R=5.2,r0=1.6,r1=4.6,k=.55,cx=12,cy=12.5,sw=.7):
    out=[]
    for i in range(n):
        t=i/(n-1); a=math.radians(a0+(a1-a0)*t)
        r=r0+(r1-r0)*t**1.3
        Rt=R+0.8*t
        x=cx+Rt*math.cos(a); y=cy-Rt*math.sin(a)
        # tangent direction angle (degrees) for ellipse rotation: major axis along normal (radial)
        rot=-math.degrees(a)
        last=i==n-1
        out.append(f'<ellipse cx="{x:.2f}" cy="{y:.2f}" rx="{r:.2f}" ry="{r*k:.2f}" transform="rotate({rot:.1f} {x:.2f} {y:.2f})" fill="${{c}}" stroke="${{d}}" stroke-width="{sw}"/>')
        if last:
            out.append(f'<ellipse cx="{x:.2f}" cy="{y:.2f}" rx="{r*.7:.2f}" ry="{r*k*.62:.2f}" transform="rotate({rot:.1f} {x:.2f} {y:.2f})" fill="${{d}}"/>')
    return ''.join(out)
print(galet(*map(float,sys.argv[1:])) if len(sys.argv)>1 else galet())

# Disseny

## Estètica nova: escollida (pendent d'aplicar)

Escollida el 12 d'octubre de 2026 a partir del qüestionari. Referència: `disseny/estil/proposta1.html` (pantalles, botons i animacions) i `disseny/estil/proposta2.html`, estil «Núvols» (el mapa). Encara no s'ha aplicat a l'app: fins aleshores mana l'estètica "nit i fanals" de sota.

| Element | Valor |
|---|---|
| Fons / tinta (clar) | `#FFFFFF` / `#111111` |
| Fons / tinta (fosc) | `#0F0F0F` / `#F2F2F2` |
| L'únic color fort | groc `#FFD000`, amb text `#111111` a sobre |
| Secundari, filets, boira | `#6B6B6B`, `#E4E4E4`, `#ECECEC` (clar) · `#9A9A9A`, `#2A2A2A`, `#1E1E1E` (fosc) |
| Títols i xifres | Archivo Black |
| Text | Instrument Sans |
| Frases fetes i pistes | Caveat (escrit a mà), només aquí |

- **Clar i fosc**, segons el mòbil.
- **Botons:** vora de 2 px i ombra dura de 4 px, de la tinta. En prémer, s'enfonsen fins a tocar l'ombra. Les accions grans contesten (el text canvia: «Fet!», «No t'arriba»). Res de píndoles ni degradats.
- **Mapa «Núvols»:** els teus pobles en groc amb vora de tinta; els veïns en blanc, mig visibles a través de la boira; la resta, tapada per núvols. En desbloquejar, els núvols s'obren i surt escrita a mà una frase feta catalana a l'atzar.
- **Missions:** fetes, es ratllen amb un gargot groc fet a mà; el check-in cau com un segell amb la data; els punts volen fins al comptador.
- **Canvi de pantalla:** zoom des del poble tocat; entre pestanyes, tall directe.
- **So i vibració** a les accions, amb opció d'apagar-ho.

- **El sac** (`disseny/sacs/sacs-estil2.html`, opcions «Sac de roba» i «Cromo»): un sac de roba amb trama de teixit, plecs al coll, vora una mica torta, corda groga i l'etiqueta «BOCINS de Catalunya» impresa. Abans d'obrir-se tremola una vegada si el que hi ha és comú, dues si és rar i tres si és llegendari (amb so i vibració a cada sacsejada). En obrir-se, la corda cau, el coll s'eixampla i el sac s'aixafa. El que en surt és un **cromo**: una carta amb ombra dura que puja i es gira, amb una banda groga amb la raresa i una, dues o tres estrelles; la llegendària té una lluentor que hi passa per sobre. A sota, un comentari escrit a mà per a cada raresa. Es continua dient «sac».

El que s'ha après de les rondes: fons blanc net (mai blanc trencat), colors apagats o madurs (mai cridaners ni infantils), res que sembli vell, i res de botons de píndola ni paletes genèriques. La festa major (programa d'actes, lletra de tipus de fusta, dues tintes) és el concepte que més ha agradat.

### Respostes del qüestionari (octubre de 2026)

Després de descartar els conceptes tancats, es va tornar a començar amb un qüestionari (`disseny/estil/questionari-estil.html`). La proposta que en surt és a `disseny/estil/proposta1.html`.

- Objectiu: cridanera per al jovent i professional per a la gent adulta; divertida i amb gràcia, sense passar-se.
- Personalitat: un presentador de la tele. Ni seriosa ni juganera, una mica atrevida, més aviat minimalista i molt feta a mà.
- Referències: Nothing, Any.do, Notion, Wispr Flow.
- Color: blanc i negre amb un sol color fort. Clar i fosc segons el mòbil.
- Botons: ombra dura; en prémer s'enfonsen, fan de segell o contesten (el text canvia).
- Desbloquejar: la boira s'esvaeix, amb una frase feta catalana a l'atzar (idea del Martí).
- Missió feta: es ratlla. Canvi de pantalla: zoom des del mapa, però no sempre la mateixa animació. Animació: bastant.
- Humor: textos, animacions, so i vibració. Sense mascota. Avís del GPS en to amable.
- Mai: degradats, pastels infantils, emojis per tot arreu, massa text, píndoles, aire d'app feta amb IA, il·lustracions de gent sense cara.
- Les fonts de les pàgines de disseny no es veien al mòbil del Martí: ara van a dins de la pàgina (`disseny/estil/fonts.py`).

### Rètols esmaltats (descartat)

Les plaques de llauna esmaltada de les botigues i dels carrers: plaques amb la vora blanca interior, les cantonades arrodonides i dos cargols. Fons blanc net (mai blanc trencat), colors apagats (mai cridaners) i res que sembli vell.

| Element | Color |
|---|---|
| Fons | `#FFFFFF` |
| Esmalt principal (plaques, barra de navegació, text) | `#28364F` (blau nit) |
| Accent (acció principal, el teu municipi, missions fetes) | `#A9503F` (terracota) |
| Disponible (veí) | `#C9D0DB` |
| A la boira | `#F3F4F5` |
| Filets / text secundari | `#E2E5EA` / `#9097A3` |
| Cargols | `#D5D7DA` amb vora `#8D9096` |

Tipografia: **Oswald**. Les llistes (missions, xifres del perfil) van dins d'una sola placa blanca amb filets, no una placa per línia. Referència: `disseny/estil/conceptes6.html`, paleta A.

## Estètica actual: "nit i fanals"

El mapa és de nit. Els municipis descoberts brillen en ambre, com pobles amb els llums encesos. Els que encara no has descobert són a la boira.

| Element | Color |
|---|---|
| Fons | `#0B0E13` |
| Superfícies | `#151A22`, `#1C2330` |
| Línies | `#273041` |
| Text / text secundari | `#EEF1F5` / `#9AA4B2` |
| Descobert (el teu municipi) | `#F2B544` |
| Descobert (altres) | `#B8862F` |
| Disponible (veí) | ratllat ambre fosc `#2A2618` / `#4A3E1E` |
| A la boira | `#161B23` |
| Selecció i accions secundàries | `#5AB8E8` |

Tipografia: **Chakra Petch** per als títols i els números, i **Atkinson Hyperlegible** per al text.

## Pantalles

1. **Tria el teu municipi.** Un cercador o la ubicació. El municipi triat queda desbloquejat.
2. **Mapa.** És la pantalla principal. Mostra els límits reals amb tres estats (descobert, disponible, a la boira), un minimapa de Catalunya, les barretines (la moneda: els punts del joc) i el comptador sobre 947.
3. **Fitxa del municipi.** Té la foto, les missions amb les barretines que donen i el bonus per completar-les totes.
4. **Municipi bloquejat.** Mostra el cost i quantes missions s'hi amaguen. Només es desbloqueja amb barretines.
5. **Rànquing.** Pestanya del perfil. Per punts o per municipis, global i amics, amb la teva posició sempre visible. Més endavant, per comarca i per setmana.
6. **Perfil i àlbum.** Foto de perfil, estadístiques, fotos per municipi i la vitrina de medalles.

Disseny visual de les pantalles: https://claude.ai/artifact/KoLHogxx6EceJqBkFAyXih

## Mecàniques

Les regles definitives són a [`requisits.md`](requisits.md). El prototip web (`prototip/`) encara fa servir unes regles antigues: desbloqueig gratis amb GPS i cost de 50 + 10 punts per municipi. L'app no les ha de copiar.

## Proposta tècnica per a Android

- Kotlin i Jetpack Compose.
- El mapa dibuixat amb Canvas a partir del GeoJSON, perquè sembli un joc i no Google Maps.
- Room per guardar les dades al mòbil, amb mode sense connexió.
- CameraX per a les fotos.
- Servidor: Supabase (Auth, Postgres, Storage). Vegeu la secció 9 de `requisits.md`.

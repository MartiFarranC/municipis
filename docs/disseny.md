# Disseny

## Estètica nova: "rètols esmaltats" (escollida, pendent d'aplicar)

Escollida l'octubre de 2026 després de diverses rondes de conceptes (`disseny/estil/conceptes*.html`). Encara no s'ha aplicat a l'app: fins aleshores mana l'estètica "nit i fanals" de sota.

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
5. **Rànquing.** Global i amics, amb la teva posició sempre visible. Més endavant, per comarca i per setmana.
6. **Perfil i àlbum.** Estadístiques, fotos per municipi i la vitrina de medalles.

Disseny visual de les pantalles: https://claude.ai/artifact/KoLHogxx6EceJqBkFAyXih

## Mecàniques

Les regles definitives són a [`requisits.md`](requisits.md). El prototip web (`prototip/`) encara fa servir unes regles antigues: desbloqueig gratis amb GPS i cost de 50 + 10 punts per municipi. L'app no les ha de copiar.

## Proposta tècnica per a Android

- Kotlin i Jetpack Compose.
- El mapa dibuixat amb Canvas a partir del GeoJSON, perquè sembli un joc i no Google Maps.
- Room per guardar les dades al mòbil, amb mode sense connexió.
- CameraX per a les fotos.
- Servidor: Supabase (Auth, Postgres, Storage). Vegeu la secció 9 de `requisits.md`.

# Disseny

## Estètica: "nit i fanals"

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
2. **Mapa.** És la pantalla principal. Mostra els límits reals amb tres estats (descobert, disponible, a la boira), un minimapa de Catalunya, els punts i el comptador sobre 947.
3. **Fitxa del municipi.** Té la foto, les missions amb punts i el bonus per completar-les totes.
4. **Municipi bloquejat.** Mostra el cost i quantes missions s'hi amaguen. Només es desbloqueja amb punts.
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

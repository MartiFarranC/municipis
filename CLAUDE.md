# CLAUDE.md

App Android per visitar els 947 municipis de Catalunya com si fos un videojoc: un mapa amb boira de guerra, missions per municipi, punts i desbloqueig de municipis veïns.

## Llegeix primer

- `docs/requisits.md`: què ha de fer l'app i com s'ha de construir. **És la font de veritat.**
- `docs/disseny.md`: estètica, colors, tipografia i pantalles.
- `prototip/mapa-interactiu.html`: prototip web del mapa. És una referència visual i d'interacció. **Les seves regles de joc estan obsoletes**: mana `requisits.md`.

## Normes

- **Textos:** tots els textos de l'app en **català**, sempre a `strings.xml`, mai dins del codi.
- **Dades:** no t'inventis cap dada geogràfica, ni cap monument, ni cap missió. Les dades surten dels scripts de `scripts/` a partir de fonts reals. Si falta una dada, deixa-ho indicat i pregunta.
- **Configuració del joc:** les regles i els valors (costos, punts, radis del GPS) han d'estar en un sol lloc de configuració i han de tenir tests.
- **Servidor:** només Supabase (`supabase-kt`), seguint la secció 9 de `requisits.md`. Room és la font principal i l'app ha de funcionar sense connexió. Mai la clau `service_role` a l'app ni al repositori. No afegeixis cap SDK d'analítica ni de publicitat.
- **Mapa:** el mapa es dibuixa amb Compose `Canvas` a partir dels assets generats. No facis servir Google Maps ni MapLibre.
- **Abans d'acabar una tasca:** compila, i passa els tests i el lint.
- **Commits:** petits i amb missatge en català.

## Estructura

```
app/            projecte Android (Compose, Hilt, Room, DataStore, CameraX)
joc/            regles del joc en Kotlin pur, amb tests (build composta, sense SDK d'Android)
dades/          dades generades, configuració del joc i connexions especials
supabase/       migracions de la base de dades (esquema, RLS i funcions)
docs/           requisits i disseny
prototip/       prototip web del mapa
scripts/        generació de dades i missions (Node.js)
```

- **Configuració del joc:** `dades/configuracio_joc.json` (l'script la copia als assets). La llegeix `joc/.../ConfiguracioJoc.kt`.
- **Lògica:** les regles van al mòdul `joc`; l'app només les aplica i en desa el resultat (`app/.../domain/Joc.kt`).

## Compilar i provar

```bash
./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
./gradlew -p joc test
cd scripts && npm test
npx supabase start && npx supabase test db   # Supabase local amb Docker; mai contra el projecte compartit
```

## Regenerar les dades

```bash
git clone --depth 1 https://github.com/ArnauInes/geometries_cat_bcn_2024.git font
cd scripts && npm install && node generar-dades.js ../font/dts_municipis_cat_2025.json
node generar-missions.js ../font/dts_municipis_cat_2025.json
node generar-sql-missions.js   # migració amb les missions oficials per al rànquing
```

Després de regenerar les missions, cal aplicar la migració nova al projecte de Supabase (`npx supabase db push`).

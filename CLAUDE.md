# CLAUDE.md

App Android per visitar els 947 municipis de Catalunya com si fos un videojoc: un mapa amb boira de guerra, missions per municipi, punts i desbloqueig de municipis veïns.

## Llegeix primer

- `docs/requisits.md`: què ha de fer la versió 1 i com s'ha de construir. **És la font de veritat.**
- `docs/disseny.md`: estètica, colors, tipografia i pantalles.
- `prototip/mapa-interactiu.html`: prototip web del mapa. És una referència visual i d'interacció. **Les seves regles de joc estan obsoletes**: mana `requisits.md`.

## Normes

- **Textos:** tots els textos de l'app en **català**, sempre a `strings.xml`, mai dins del codi.
- **Dades:** no t'inventis cap dada geogràfica, ni cap monument, ni cap missió. Les dades surten dels scripts de `scripts/` a partir de fonts reals. Si falta una dada, deixa-ho indicat i pregunta.
- **Configuració del joc:** les regles i els valors (costos, punts, radis del GPS) han d'estar en un sol lloc de configuració i han de tenir tests.
- **Sense servidor:** la versió 1 és local i funciona sense connexió. No afegeixis cap SDK de servidor ni d'analítica.
- **Mapa:** el mapa es dibuixa amb Compose `Canvas` a partir dels assets generats. No facis servir Google Maps ni MapLibre.
- **Abans d'acabar una tasca:** compila, i passa els tests i el lint.
- **Commits:** petits i amb missatge en català.

## Estructura

```
app/            projecte Android (pendent de crear)
dades/          dades generades (GeoJSON i veïns)
docs/           requisits i disseny
prototip/       prototip web del mapa
scripts/        generació de dades (Node.js)
```

## Regenerar les dades

```bash
git clone --depth 1 https://github.com/ArnauInes/geometries_cat_bcn_2024.git font
cd scripts && npm install && node generar-dades.js ../font/dts_municipis_cat_2025.json
```

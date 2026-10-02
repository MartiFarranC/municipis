# Descobreix Catalunya

App per visitar els 947 municipis de Catalunya com si fos un videojoc. Tot el mapa comença a la boira. El teu municipi és el primer que s'il·lumina, i a partir d'aquí vas desbloquejant els municipis veïns amb punts o visitant-los en persona.

## Què hi ha ara mateix

| Carpeta | Contingut |
|---|---|
| `prototip/mapa-interactiu.html` | Prototip web jugable del mapa. Obre'l amb qualsevol navegador. |
| `prototip/plantilla.html` | El mateix prototip sense les dades, que l'script omple. |
| `dades/municipis_catalunya.geojson` | Polígons reals dels 947 municipis (codi INE i nom). |
| `dades/municipis_veins.json` | Per a cada municipi, els codis INE dels municipis que hi fan frontera. |
| `scripts/generar-dades.js` | Regenera tots els fitxers anteriors a partir de la font original. |
| `docs/disseny.md` | Pantalles, estètica i mecàniques de joc. |

Encara no hi ha el projecte d'Android Studio: anirà a la carpeta `app/` quan es creï.

## Regenerar les dades

```bash
git clone --depth 1 https://github.com/ArnauInes/geometries_cat_bcn_2024.git font
cd scripts
npm install
node generar-dades.js ../font/dts_municipis_cat_2025.json
```

## Fonts de dades

Els límits municipals són de l'Instituto Geográfico Nacional (IGN), en la versió de desembre de 2024 publicada a [ArnauInes/geometries_cat_bcn_2024](https://github.com/ArnauInes/geometries_cat_bcn_2024). Les dades de l'IGN es distribueixen amb llicència CC BY 4.0, i cal citar-ne la font dins l'app.

Pendent de revisar:

- **Comarques.** El camp de comarca de la font no inclou el Lluçanès (creat el 2023), per això no s'ha fet servir. Cal treure-les de l'Idescat.
- **Precisió.** Per a la versió definitiva, la referència oficial és la base municipal de l'ICGC. Els codis INE coincideixen, així que el canvi és directe.
- **Missions.** Es generaran a partir de Wikidata i OpenStreetMap.

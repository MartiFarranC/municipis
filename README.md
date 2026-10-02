# Descobreix Catalunya

App per visitar els 947 municipis de Catalunya com si fos un videojoc. Tot el mapa comença a la boira. El teu municipi és el primer que s'il·lumina, i a partir d'aquí vas desbloquejant els municipis veïns amb els punts que guanyes completant missions.

Les regles i l'abast de la versió 1 són a [`docs/requisits.md`](docs/requisits.md).

## Estructura

| Carpeta | Contingut |
|---|---|
| `app/` | L'app Android (Kotlin, Jetpack Compose, Hilt, Room, DataStore, CameraX). |
| `joc/` | Les regles del joc en Kotlin pur: configuració, economia de punts, veïns, geometria, localització, proves, cercador, nivells i assoliments. Té els seus tests i no necessita l'SDK d'Android. |
| `dades/` | Dades generades (GeoJSON, veïns, comarques), la configuració del joc (`configuracio_joc.json`) i les connexions especials (`connexions_especials.json`). |
| `scripts/` | Scripts de Node.js que generen les dades i les missions. |
| `prototip/` | Prototip web del mapa. Les seves regles de joc estan obsoletes. |
| `docs/` | Requisits i disseny. |

## Compilar i provar

Cal Android Studio (o l'SDK d'Android amb la plataforma 36) i JDK 17 o posterior.

```bash
./gradlew :app:assembleDebug        # compila l'app
./gradlew :app:testDebugUnitTest    # tests unitaris de l'app (ViewModels i servei del joc)
./gradlew :app:lintDebug            # lint
./gradlew :app:connectedDebugAndroidTest   # tests de UI (cal un emulador o un mòbil)
./gradlew -p joc test               # tests de les regles del joc (no cal l'SDK d'Android)
cd scripts && npm test              # tests dels scripts
```

## Regenerar les dades

```bash
git clone --depth 1 https://github.com/ArnauInes/geometries_cat_bcn_2024.git font
cd scripts
npm install
node generar-dades.js ../font/dts_municipis_cat_2025.json
node generar-missions.js ../font/dts_municipis_cat_2025.json
```

`generar-dades.js` escriu els fitxers de `dades/`, el prototip i els assets de l'app (`app/src/main/assets/dades/`):

- `municipis.json`: noms, comarques i veïns.
- `mapa.bin`: geometria projectada (Mercator) i simplificada en dos nivells de detall, per dibuixar el mapa.
- `limits.bin`: límits a resolució completa, per saber en quin municipi és l'usuari. L'app el llegeix municipi a municipi.
- `configuracio_joc.json`: còpia de `dades/configuracio_joc.json`.

`generar-missions.js` consulta Wikidata i OpenStreetMap i escriu `missions.json` i `dades/informe_missions.txt`. Sense connexió, amb `--nomes-generiques` genera només les missions genèriques.

## Fonts de dades

- **Límits municipals.** Instituto Geográfico Nacional (IGN), versió de desembre de 2024 publicada a [ArnauInes/geometries_cat_bcn_2024](https://github.com/ArnauInes/geometries_cat_bcn_2024), amb llicència CC BY 4.0.
- **Comarques.** Les 43 comarques actuals (el Lluçanès inclòs), de la capa de comarques de la mateixa font. Cada municipi s'assigna a la comarca que en conté el punt interior.
- **Missions.** Wikidata (CC0) i OpenStreetMap (ODbL, © col·laboradors d'OpenStreetMap), més les missions genèriques de `dades/configuracio_joc.json`.
- **Tipografies.** Chakra Petch i Atkinson Hyperlegible (SIL Open Font License 1.1). Les llicències són a `app/src/main/assets/llicencies/`.

## Pendent

- **Missions de lloc.** Els `missions.json` actuals només tenen les missions genèriques, perquè es van generar sense accés a Wikidata ni a OpenStreetMap. Cal tornar a executar `generar-missions.js` amb connexió.
- **Base municipal de l'ICGC.** La referència oficial per als límits. Els codis INE coincideixen, així que el canvi és directe.
- **Comarques de l'Idescat.** Ara surten de la capa de comarques de l'IGN, que ja té les 43 comarques. Si cal la llista oficial de l'Idescat, s'ha de baixar i comparar.
- **Ajustar l'economia**, el nom definitiu de l'app i la icona (secció 11 dels requisits).

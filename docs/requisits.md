# Requisits — Descobreix Catalunya (versió 1)

Aquest document defineix què ha de fer la primera versió de l'app i com s'ha de construir. Si alguna cosa d'aquí contradiu `disseny.md` o el prototip web, mana aquest document.

## 1. Què és

Una app Android per visitar els 947 municipis de Catalunya com si fos un videojoc.

- El mapa de Catalunya comença tot a la boira, amb els límits municipals reals.
- L'usuari tria el seu municipi, que queda desbloquejat.
- Cada municipi té una llista de missions: llocs que cal visitar o coses que cal fer.
- Les missions donen punts, i amb els punts es desbloquegen municipis veïns.
- L'usuari pot afegir fotos a cada municipi.

## 2. Abast de la versió 1

**Inclou:**
- Funcionament **100% local i sense connexió**: sense comptes, sense servidor.
- L'app **només en català**.

**No inclou (versió 2, online):**
- Comptes d'usuari i rànquing.
- Amics.
- Compartir fotos o missions entre usuaris.
- Moderació de missions proposades.

Tot i això, el codi s'ha de preparar perquè la versió 2 es pugui afegir sense refer res (vegeu la secció 9).

## 3. Regles del joc

### 3.1 Estats d'un municipi

| Estat | Condició | Al mapa |
|---|---|---|
| **Descobert** | L'usuari l'ha desbloquejat, o és el seu municipi d'inici | Ambre brillant |
| **Disponible** | Fa frontera amb almenys un municipi descobert | Ratllat ambre fosc |
| **A la boira** | La resta | Fosc |

La relació de veïnatge surt de `dades/municipis_veins.json`, i s'ha de regenerar a partir de les dades de l'ICGC (secció 7).

**Cas especial: Llívia.** És un enclavament dins de França i no fa frontera amb cap municipi català. Si només es miren les fronteres, no es podria desbloquejar mai. Per això cal una **connexió especial Llívia ↔ Puigcerdà**, que correspon a la carretera que les uneix. Les connexions especials es defineixen en un fitxer de dades a part (per exemple `dades/connexions_especials.json`), que l'script afegeix al graf de veïns. Un test ha de comprovar que, amb aquestes connexions, des de qualsevol municipi es pot arribar a tots els altres.

### 3.2 Desbloquejar

- **Només amb punts.** No hi ha cap altra manera de desbloquejar un municipi: ni GPS, ni visitar-lo.
- Només es poden desbloquejar els municipis **disponibles**.
- **Municipi bloquejat i usuari a dins:** si l'usuari és físicament en un municipi que encara no ha desbloquejat, **no hi pot fer res**. No pot fer missions, ni afegir fotos, ni guardar res pendent. L'app li ho ha d'explicar i mostrar quants punts li falten per desbloquejar-lo.

### 3.3 Economia de punts

**Regla obligatòria: el joc no pot quedar mai encallat.** Ha de ser impossible arribar a un estat en què l'usuari no pugui guanyar prou punts per desbloquejar cap altre municipi. Això ha de quedar cobert per tests.

**Valors inicials.** Han de viure en un sol lloc de configuració, perquè s'hauran d'ajustar:

| Paràmetre | Valor inicial |
|---|---|
| Cost de desbloquejar | `min(60 + 5 × (municipis_descoberts − 1), 200)` |
| Punts mínims garantits per municipi | 200 |
| Punts màxims per municipi | 400 |
| Bonus per completar totes les missions d'un municipi | +50 |

**Com es garanteix el mínim de 200 punts.** Cada municipi ha de tenir missions genèriques que en qualsevol poble es poden fer, encara que no tingui monuments coneguts. Per exemple: fer check-in al nucli, fer una foto del cartell d'entrada, fer una foto de l'església o de l'ajuntament si en té.

**Per què hi ha un màxim de 400.** Perquè Barcelona no valgui deu vegades més que un poble petit.

### 3.4 Missions

**Origen**

1. **Automàtiques.** Es generen fora de l'app amb un script, a partir de Wikidata (patrimoni, monuments, esglésies, museus) i d'OpenStreetMap (`historic=*`, `tourism=attraction|museum|viewpoint`, etc.). Es guarden en un JSON que va dins de l'app.
2. **Proposades per l'usuari.** A la versió 1 només les veu qui les crea. **No donen punts**, perquè ningú es pugui inventar missions per sumar-ne. A la versió 2 es podran compartir després d'una moderació.

**Prova.** Cada missió té un tipus de prova, que surt de les dades:

| Tipus | Com es valida |
|---|---|
| **GPS** | La ubicació de l'usuari ha de ser a menys de **75 m** de les coordenades del lloc, i la precisió del GPS ha de ser de **≤ 50 m**. |
| **Foto** | La foto s'ha de fer **amb la càmera des de l'app**. No s'accepten fotos de la galeria. En fer-la, es guarda la ubicació, que ha de ser dins del municipi. |

Totes dues proves només funcionen si el municipi ja està **desbloquejat**.

**Dades no inventades.** Les missions automàtiques només poden fer referència a llocs que existeixen a les fonts. Si per a un municipi no hi ha prou dades, només tindrà missions genèriques.

### 3.5 "Ets dins del municipi"

- Es fan servir els límits de l'ICGC **a resolució completa** per comprovar-ho. La versió simplificada només serveix per dibuixar.
- **Marge a les fronteres:** si l'usuari és a menys de **50 m** d'una frontera, o si la precisió del GPS és pitjor que la distància a la frontera, l'app li pregunta a quin dels municipis és, en lloc de decidir-ho sola.

### 3.6 Fotos

- Hi pot haver diverses fotos per municipi, i una d'elles és la portada de la fitxa.
- Cada foto té una **visibilitat** que tria l'usuari: `PRIVADA` (per defecte), `AMICS` o `PUBLICA`. A la versió 1 totes són locals, però el camp s'ha de guardar ja i l'usuari l'ha de poder canviar, perquè la versió 2 el respecti.
- Les fotos es guarden a l'emmagatzematge intern de l'app, comprimides (màxim 2048 px pel costat llarg). També es guarda una miniatura per a l'àlbum.

### 3.7 Progressió

- Hi ha nivells calculats a partir dels punts totals guanyats. Els punts gastats en desbloquejar també compten.
- **Assoliments.** Exemples: primer municipi, primera comarca completa, 10 / 50 / 100 municipis, totes les capitals de comarca.
- **Comarques.** Les dades de comarques s'han de treure de l'Idescat, amb les 43 comarques actuals (el Lluçanès inclòs). No s'ha de fer servir el camp de comarca de la font de l'IGN, perquè no està al dia.

## 4. Pantalles

Disseny de referència: `docs/disseny.md` i el llenç https://claude.ai/artifact/KoLHogxx6EceJqBkFAyXih

1. **Tria el teu municipi.** Un cercador per nom, que no tingui en compte els accents ni els articles, i l'opció "fer servir la meva ubicació".
2. **Mapa.** Té:
   - el mapa interactiu amb pan, zoom i tocar un municipi;
   - un minimapa de tota Catalunya;
   - els punts, el comptador sobre 947 i un cercador;
   - els noms dels municipis segons el zoom.
3. **Fitxa d'un municipi descobert.** Té:
   - la foto de portada i el progrés de missions i punts;
   - la llista de missions amb el seu tipus de prova;
   - el botó per afegir una missió pròpia;
   - la llista de municipis veïns, que es pot tocar.
4. **Fitxa d'un municipi disponible o a la boira.** Mostra el cost, quantes missions té (sense dir quines són) i el botó de desbloquejar. Si és a la boira, diu a quants municipis de distància és del territori de l'usuari.
5. **Perfil i àlbum.** Té les estadístiques, les fotos per municipi, els assoliments i el progrés per comarques.

A la versió 1 **no hi ha** pantalla de rànquing.

## 5. Estètica

Es segueix el que diu `docs/disseny.md`:
- **Tema:** fosc, "nit i fanals".
- **Colors:** ambre `#F2B544` per al que està descobert i blau `#5AB8E8` per a la selecció.
- **Tipografia:** Chakra Petch per als títols i Atkinson Hyperlegible per al text.

Requisits d'accessibilitat:
- Mida mínima dels elements tàctils: 48 dp.
- Contrast de text de 4,5:1.
- Etiquetes de contingut per a TalkBack.
- Respectar l'opció del sistema de reduir les animacions.

## 6. Arquitectura

| Tema | Decisió |
|---|---|
| Llenguatge i UI | Kotlin i Jetpack Compose (Material 3, personalitzat amb els tokens del disseny) |
| Arquitectura | MVVM amb flux de dades unidireccional, `StateFlow`, corrutines |
| Injecció de dependències | Hilt |
| Persistència | Room (estat del joc, missions, fotos) i DataStore (preferències) |
| Càmera | CameraX |
| Ubicació | Fused Location Provider |
| Mòduls | Al principi un sol mòdul `:app`, organitzat per paquets de funcionalitat (`map`, `municipality`, `missions`, `photos`, `profile`, `onboarding`) i una capa `data` / `domain` |
| SDK | `minSdk 26`; `targetSdk` i `compileSdk` a l'última versió estable |
| Tests | Unitaris per a les regles del joc (secció 3), la geometria (punt dins de polígon, distància a frontera) i els ViewModels. Tests de UI de Compose per als fluxos principals. |

### 6.1 Mapa

- Es dibuixa amb **Compose `Canvas`**, sense proveïdor de mapes. No es fa servir Google Maps ni MapLibre.
- **Geometria:**
  - es precalcula fora de l'app, projectada (Mercator) i simplificada;
  - es carrega des d'`assets/` en un format compacte, binari o JSON;
  - els `Path` es construeixen una sola vegada i es guarden en memòria cau.
- **Gestos:** pan, pinch zoom i doble toc.
- **Tocar un municipi:** es detecta amb una comprovació ràpida de requadre i després de punt dins de polígon.
- **Rendiment:**
  - 60 fps fent pan i zoom en un mòbil de gamma mitjana;
  - no es pot reconstruir cap `Path` en cada fotograma;
  - els noms només apareixen a partir de cert zoom.
- **Dos jocs de geometria:**
  - **Per dibuixar:** simplificada.
  - **Per comprovar la ubicació:** resolució completa. Es carrega només quan cal, municipi a municipi.

## 7. Dades

Tot es genera amb scripts a `scripts/`, i el resultat va a `app/src/main/assets/`. **Cap dada s'escriu a mà dins del codi.**

| Dades | Font | Script |
|---|---|---|
| Límits municipals (2 resolucions) | **ICGC**, base municipal. Fins que no estigui baixada, la de l'IGN que ja hi ha a `dades/` | `generar-dades.js` (cal adaptar-lo a la font de l'ICGC) |
| Veïns | Derivats dels límits | ídem |
| Comarques | Idescat | nou |
| Missions | Wikidata + OpenStreetMap + genèriques | nou: `generar-missions` |

L'script de missions ha de produire, per a cada missió:
- un identificador estable;
- el codi INE del municipi;
- el títol en català;
- el tipus (`LLOC` o `GENERICA`);
- les coordenades, si en té;
- el tipus de prova (`GPS` o `FOTO`);
- els punts;
- la font, amb l'identificador Wikidata o OSM.

També ha de fer un informe de quants municipis queden només amb missions genèriques.

**Atribucions.** A la pantalla "Sobre l'app" s'han de citar l'IGN o l'ICGC, Wikidata (CC0) i OpenStreetMap (ODbL, © col·laboradors d'OpenStreetMap).

## 8. Privacitat i permisos

- **Ubicació:** només mentre s'utilitza l'app, mai en segon pla. Es demana en el moment en què cal, no en obrir l'app.
- **Càmera:** es demana en el moment de fer la primera foto.
- **Dades:** a la versió 1 no surt res del dispositiu. L'usuari pot exportar i esborrar tot el seu progrés.

## 9. Preparació per a la versió 2 (online)

- Totes les entitats de l'usuari (progrés, missions pròpies, fotos) tenen un identificador UUID i dates de creació i modificació.
- L'accés a dades es fa a través de repositoris amb interfícies, perquè es pugui afegir una font remota sense tocar la UI.
- El servidor (Firebase o Supabase) es decidirà a la versió 2. No s'ha d'afegir cap SDK de servidor a la versió 1.

## 10. Ordre de feina proposat

1. **Base del projecte.** Projecte Android, tema i tokens de disseny, navegació i Hilt.
2. **Dades.** Adaptar els scripts, generar els assets i carregar-los.
3. **Mapa.** Renderitzat, gestos, estats i tocar un municipi.
4. **Onboarding i regles de joc.** Desbloqueig, economia de punts, i tests que demostrin que el joc no es pot quedar encallat.
5. **Fitxa i missions.** Proves amb GPS i amb foto, i missions pròpies.
6. **Fotos i àlbum.** Amb la visibilitat de cada foto.
7. **Perfil.** Nivells, assoliments i comarques.
8. **Poliment.** Accessibilitat, rendiment, exportar i esborrar dades, atribucions.

## 11. Decisions pendents

- Baixar la base municipal de l'ICGC i decidir-ne el format d'entrada per a l'script.
- Ajustar els valors de l'economia (secció 3.3) després de provar-la.
- Quines categories de Wikidata i d'OSM es converteixen en missions, i quants punts val cada categoria.
- Nom definitiu de l'app i icona.

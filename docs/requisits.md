# Requisits — Descobreix Catalunya

Aquest document defineix què ha de fer l'app i com s'ha de construir. Si alguna cosa d'aquí contradiu `disseny.md` o el prototip web, mana aquest document.

## 1. Què és

Una app Android per visitar els 947 municipis de Catalunya com si fos un videojoc.

- El mapa de Catalunya comença tot a la boira, amb els límits municipals reals.
- L'usuari tria el seu municipi, que queda desbloquejat.
- Cada municipi té una llista de missions: llocs que cal visitar o coses que cal fer.
- Les missions donen punts, i amb els punts es desbloquegen municipis veïns.
- L'usuari pot afegir fotos a cada municipi.

## 2. Abast

**Inclou:**
- **Compte d'usuari obligatori** (Google, correu i contrasenya, o enllaç màgic), amb servidor **Supabase** (vegeu la secció 9).
- Funcionament **sense connexió** un cop iniciada la sessió: el joc es juga en local i se sincronitza quan hi ha connexió.
- Còpia i sincronització del progrés, les missions pròpies i les fotos entre dispositius.
- Rànquing de punts.
- Amics, i fotos visibles per als amics o per a tothom.
- L'app **només en català**.

**No inclou (versió posterior):**
- Compartir missions proposades entre usuaris.
- Moderació de missions proposades.

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

**La moneda es diu «barretines».** A l'app, els punts es mostren com a barretines, amb la icona de la barretina dels emojis (`IconaBarretina`). Als textos sempre amb plural correcte: «1 barretina», «2 barretines». Al codi, a la configuració i al servidor es continuen dient «punts»; en aquest document també.

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
2. **Proposades per l'usuari.** Només les veu qui les crea (se sincronitzen entre els seus dispositius). **No donen punts**, perquè ningú es pugui inventar missions per sumar-ne. En una versió posterior es podran compartir després d'una moderació.

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
- Cada foto té una **visibilitat** que tria l'usuari: `PRIVADA` (per defecte), `AMICS` o `PUBLICA`. El servidor l'ha de fer complir (secció 9.4): una foto privada només la pot llegir el seu propietari.
- Les fotos es guarden a l'emmagatzematge intern de l'app, comprimides (màxim 2048 px pel costat llarg). També es guarda una miniatura per a l'àlbum. Totes dues es pugen a Supabase Storage quan hi ha connexió.

### 3.7 Progressió

- Hi ha nivells calculats a partir dels punts totals guanyats. Els punts gastats en desbloquejar també compten.
- **Medalles.** Substitueixen els assoliments. Una per comarca amb tres nivells (bronze: tots els municipis descoberts; plata: a més, totes les missions automàtiques; or: a més, la foto del cartell de cada municipi), fites de 10 / 50 / 100 / 250 / 500 / 947 municipis, totes les capitals de comarca i fites de 1 / 25 / 100 / 500 / 947 cartells. Cada medalla (i cada nivell de comarca) dona uns quants punts una sola vegada. Els valors són a `configuracio_joc.json`. Detall a `docs/decisions-pendents.md`.
- **Catàleg de cartells.** Al perfil, un àlbum amb els 947 municipis (per comarques, amb el recompte de cada una, o per ordre alfabètic). Cada casella té la foto del cartell d'entrada quan es fa; els municipis descoberts sense foto surten amb el nom i el marc buit, i els altres, tapats. La foto del cartell es fa amb un requadre que l'usuari ajusta sobre la càmera; només se'n desa el retall, es comprova el GPS i que s'hi llegeixi el nom del municipi (Tesseract, al mòbil i sense connexió). Es pot repetir sense punts. Les fotos del cartell anteriors, sense requadre, no valen per al catàleg.
- **Sacs.** Es guanyen la primera vegada que es desbloqueja un municipi (a més del d'inici), que es fa una missió i que es fa una foto, i un per cada nivell de medalla de comarca (129). En obrir-los surt a l'atzar una cosa que encara no es té: un emoji, una tapa del passaport, una animació de càrrega o un color secundari de l'app. Rareses comuna, rara i llegendària (70 / 25 / 5 %). Mai no surten repetides; quan ja es té tot, el sac dona punts. El contingut i les probabilitats són a `configuracio_joc.json` (bloc `sacs`, generat per `disseny/sacs/gen-objectes.py`). Els dibuixos dels emojis i de les tapes surten de `disseny/sacs/gen-recursos.py` i `captura-recursos.mjs`.
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
5. **Perfil i àlbum.** Té les estadístiques, les fotos per municipi, la vitrina de medalles i el progrés per comarques. També el nom d'usuari, tancar la sessió i esborrar les dades del joc.
6. **Inici de sessió.** Abans de "Tria el teu municipi". Google, correu i contrasenya (amb recuperació de contrasenya) i enllaç màgic. Si no hi ha connexió, ho explica.
7. **Rànquing.** Classificació general i classificació entre amics.
8. **Amics.** Buscar per nom d'usuari, enviar, acceptar i rebutjar sol·licituds, eliminar amics, i veure el mapa i les fotos visibles d'un amic.

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
| Servidor | Supabase (Auth, Postgres amb RLS, Storage) amb el client `supabase-kt`. Inici de sessió amb Google via Credential Manager. Sincronització en segon pla amb WorkManager |
| Mòduls | Al principi un sol mòdul `:app`, organitzat per paquets de funcionalitat (`map`, `municipality`, `missions`, `photos`, `profile`, `onboarding`) i una capa `data` / `domain` |
| SDK | `minSdk 26`; `targetSdk` i `compileSdk` a l'última versió estable |
| Tests | Unitaris per a les regles del joc (secció 3), la geometria (punt dins de polígon, distància a frontera) i els ViewModels. Tests de UI de Compose per als fluxos principals. |
| Distribució | Obtainium, des de les releases de GitHub. La CI publica una release amb l'APK signat cada vegada que tot passa a `main` (vegeu [`obtainium.md`](obtainium.md)) |

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
- **Dades:** el progrés, les missions pròpies i les fotos es desen a Supabase, en un projecte de la **regió UE**. La ubicació només es puja com a part d'una missió completada o d'una foto, mai de manera contínua. L'usuari pot exportar les seves dades i **esborrar-les des de l'app**: s'esborren totes les dades d'aquesta app del servidor (taules i fotos), però no el compte d'usuari, perquè és compartit amb altres apps (secció 9.0). També cal una URL web per demanar l'esborrat. Cal comprovar si això compleix la política d'esborrat de comptes de Google Play (secció 11).
- **Política de privacitat** publicada i enllaçada des de l'app i des de Google Play.
- **Sense analítica:** no s'hi afegeix cap SDK d'analítica ni de publicitat.

## 9. Comptes, sincronització i funcions socials

### 9.0 Projecte de Supabase compartit

El projecte de Supabase (`https://mjdbqbcyensvyvhzegrc.supabase.co`, regió `eu-west-3`, París) és **compartit amb altres apps**. Per això:

- Totes les taules, vistes i funcions d'aquesta app van a l'esquema **`descobreix`**, mai a `public`. L'esquema s'ha d'afegir a *Settings → API → Exposed schemas*.
- Els buckets de Storage porten el prefix `descobreix-` (per exemple `descobreix-fotos`).
- Els comptes (`auth.users`) i la configuració d'autenticació (proveïdors, plantilles de correu, URL de redirecció) són compartits. Un usuari "és de l'app" quan té una fila a `descobreix.perfils`.
- Les migracions no poden tocar res de fora de l'esquema `descobreix` i els buckets `descobreix-*`, ni dependre de l'historial de migracions de les altres apps.
- Les migracions s'apliquen amb `supabase db push`. Quan es va adoptar, cap altra app del projecte feia servir migracions de la CLI (`supabase_migrations` no existia). Si una altra app en comença a fer servir, cal coordinar-ho.
- L'app **no pot esborrar mai** files d'`auth.users`.

### 9.1 Comptes

- El compte és **obligatori**. El primer cop cal connexió per iniciar la sessió. Després la sessió es guarda i l'app funciona sense connexió.
- Mètodes: Google, correu i contrasenya (amb verificació del correu i recuperació de contrasenya), i enllaç màgic per correu (obre l'app amb un deep link).
- **De moment l'app no envia cap correu.** Per crear el compte n'hi ha prou amb el correu i la contrasenya, que s'escriu dues vegades. No hi ha verificació del correu (*Confirm email* desactivat al projecte), ni enllaç màgic, ni recuperació de contrasenya. Es tornaran a activar quan el projecte tingui un SMTP propi i `cat.descobreix://login/**` sigui a les URL de redirecció.
- Cada usuari té un **perfil**: nom d'usuari únic (el que es mostra al rànquing i als amics) i data d'alta. No es mostra mai el correu a altres usuaris.
- **Dades d'abans dels comptes:** si en iniciar sessió hi ha progrés local sense compte, s'assigna a aquest compte.

### 9.2 Sincronització

- **Room continua sent la font principal.** La UI llegeix sempre de Room; el servidor és una còpia que se sincronitza.
- Cada canvi local queda marcat com a pendent i un treball de WorkManager el puja quan hi ha connexió. En iniciar sessió en un dispositiu nou, es baixa tot.
- Resolució de conflictes:
  - **Municipis descoberts, missions completades i moviments de punts** només s'afegeixen, mai es modifiquen: es fusionen per UUID. Si dos dispositius completen la mateixa missió, val la primera (`missioId` és únic per usuari).
  - **Missions pròpies i fotos:** guanya la modificació més recent (`modificatEl`).
  - Els esborrats són **lògics** (`esborratEl`) perquè es puguin sincronitzar.
- Les regles del joc s'han de continuar complint després de fusionar. Si una fusió dona un estat invàlid, cal un test que ho cobreixi i una regla clara per resoldre-ho.
- **Com està fet** (`app/.../data/sincronitzacio/`):
  - Uns triggers de SQLite apunten a la cua (`canvis_pendents`) cada fila que canvia; el `Sincronitzador` primer baixa el que ha canviat al servidor (per `sincronitzat_el`, des de l'últim cursor) i ho ajunta (`Fusio`, amb tests), i després puja la cua. Les fotos es pugen a Storage (`<usuari>/<fitxer>` i `<usuari>/miniatures/<fitxer>`).
  - Si dos mòbils han fet el mateix sense connexió (el mateix municipi, la mateixa missió, el mateix segell o el mateix sac), val la fila del servidor. D'un sac, es manté el que n'hagi sortit si només s'havia obert en un mòbil.
  - Limitació coneguda: en aquest cas, els moviments de punts dels dos mòbils es mantenen (es poden comptar dues vegades). Si el saldo queda negatiu, no es desfà res: cal guanyar punts abans de tornar a desbloquejar (test a `ReglesJocTest`).
  - WorkManager sincronitza cada 6 hores i, amb connexió, uns segons després de cada canvi.

### 9.3 Rànquing

- Es calcula **al servidor** a partir de les missions completades i dels punts que val cada missió segons la taula oficial de missions del servidor. **No es fia mai dels punts que envia el client.**
- La taula oficial de missions es carrega al servidor amb un script a partir del mateix JSON que va dins de l'app.
- Les missions pròpies no compten mai.
- Classificació general i entre amics.
- Limitació coneguda: el GPS es pot falsejar. Es valida com a mínim que la missió existeix, que el municipi està desbloquejat i que la ubicació enviada compleix el radi (secció 3.4).

### 9.4 Seguir i fotos

- Ja no hi ha amics: **es segueix** (docs/decisions-pendents.md). Cada perfil és d'**Explorador** (juga) o d'**Espectador** (només segueix), es tria en crear el compte i no es pot canviar.
- Cada compte és **públic** (qualsevol el segueix directament) o **privat** (el seguit accepta o rebutja la sol·licitud). Fins que no s'accepta, no es veu res del compte. Els perfils d'abans ho trien en entrar.
- Visibilitat de les fotos, aplicada amb **RLS** a Postgres i a Storage:
  - `PRIVADA`: només el propietari;
  - `SEGUIDORS`: el propietari i qui el segueix (acceptat);
  - `PUBLICA`: qualsevol usuari amb sessió, si el compte és públic; si és privat, només els seguidors.
- Un seguidor veu el mapa (municipis descoberts) de l'altre.
- **Pestanya «Gent»** (l'Espectador hi entra directament): el mur amb el que fa la gent que segueixes (municipis desbloquejats i fotos), buscar gent pel nom d'usuari i les sol·licituds per seguir-te. El perfil d'una persona mostra a quanta gent segueix i quants la segueixen, el botó de seguir i, si et deixa, el mapa, les fotos i a qui segueix.
- Al perfil: el compte públic o privat i la gent que segueixes.
- **Animar:** al mur, cada municipi desbloquejat i cada foto té el botó «Anima»: un emoji dels que tens (els 6 de tothom i els dels sacs), que s'envia amb la seva frase, o un missatge de 80 caràcters com a molt. Només es pot animar algú que segueixes. Les reaccions només les veuen qui les envia i qui les rep; a la pestanya «Gent» hi ha les que t'han enviat.
  - Que l'emoji sigui teu només ho comprova l'app (el servidor no sap quins emojis tens).
- **Bloquejar i denunciar:** des d'una reacció rebuda o des del perfil d'algú. Bloquejar fa que deixeu de seguir-vos i que no et pugui tornar a seguir ni escriure; el bloquejat no ho sap. Les denúncies es guarden a `descobreix.denuncies` (amb el text denunciat) i les revisa el Martí des del servidor.

### 9.6 Col·laboracions amb els ajuntaments

Decidit amb la pàgina `disseny/ajuntaments/tria-ajuntaments.html`. Es prepara ara, encara que no hi hagi cap ajuntament.

**Què hi posa un ajuntament** (tot és opcional i surt marcat «De l'ajuntament»):

- **Missions oficials.** Es fan com les altres: amb GPS (un punt i el radi de la configuració), amb una foto (dins del municipi) o llegint un **codi QR** (vegeu més avall).
  - Donen les barretines que diu la configuració (`ajuntaments.puntsMissio`), no les que posa l'ajuntament.
  - Com a molt `ajuntaments.maximMissionsPerMunicipi` missions per municipi: si n'hi ha més, l'app només fa servir les primeres (per `ordre`).
  - Van a part de les missions automàtiques: no compten per al bonus de completar-les totes, ni per a «municipi complet», ni per als punts màxims del municipi.
  - Si una missió es retira, qui ja l'havia feta no perd les barretines.
- **Festes i fires.** Una missió oficial pot tenir unes dates (de la festa major, d'una fira…). Només es pot fer aquells dies (hora de Catalunya, dates incloses) i dona un bonus (`ajuntaments.bonusFesta`). Abans, es veu amb les dates; quan han passat, només es veu si s'havia fet.
- **Punt de segellat (QR).** Un codi QR penjat a l'ajuntament o a l'oficina de turisme. La missió es fa llegint-lo amb la càmera de l'app des de dins del municipi.
  - El servidor només en guarda el resum (SHA-256), no el codi. L'app el comprova sense connexió.
  - El QR és un enllaç a la pàgina de l'app: qui no la té, hi va a parar.
- **Presentació del municipi:** un text curt (500 caràcters com a molt), el web i l'oficina de turisme, a la fitxa del municipi.
- **Segell propi al passaport:** un dibuix d'un sol color (PNG amb transparència, 512 × 512) a Storage (`descobreix-ajuntaments/<codi>/segell.png`). L'app el pinta amb la tinta del segell i el fa servir en lloc del genèric, també als segells que ja hi eren.
- **Avantatges fora de l'app** (entrada reduïda, regal…): l'app només els mostra a la fitxa, amb les condicions i fins quan valen. Els gestiona l'ajuntament.

**Què rep l'ajuntament:**

- **Xifres anònimes:** quanta gent ha desbloquejat el municipi i quantes missions s'hi han fet cada mes. Només totals, i cap xifra per sota de 10 persones. Les treu el Martí des del servidor (funció `descobreix.xifres_ajuntament`, només amb el rol de servei). No hi ha cap SDK d'analítica.
- **Distintiu «Municipi col·laborador»** a la fitxa i a la targeta del mapa.
- **Cartell per promocionar l'app**, amb un QR per instal·lar-la (`scripts/cartell-ajuntament.js`).

**Com es fa:**

- L'ajuntament escriu al Martí, que entra el contingut al servidor. No hi ha cap pas de revisió a part: el que s'entra **surt directament**. La guia és a [`ajuntaments.md`](ajuntaments.md).
- El contingut va a Supabase (taules `ajuntaments`, `missions_ajuntament` i `avantatges`) i l'app el baixa en sincronitzar. Qualsevol usuari amb sessió el pot llegir, i només el rol de servei el pot escriure. L'app el desa a Room per fer-lo servir sense connexió.

### 9.5 Seguretat

- **Totes les taules tenen RLS activat.** Un usuari només pot escriure les seves pròpies files.
- A l'app només hi va la URL del projecte i la clau pública (`anon`), llegides de `local.properties` a través de `BuildConfig`. **La clau `service_role` no pot sortir mai del servidor ni entrar al repositori.**
- L'esquema de la base de dades, les polítiques RLS i les funcions es versionen com a migracions a `supabase/migrations/` i tenen tests.

## 10. Ordre de feina proposat

1. **Base del projecte.** Projecte Android, tema i tokens de disseny, navegació i Hilt.
2. **Dades.** Adaptar els scripts, generar els assets i carregar-los.
3. **Mapa.** Renderitzat, gestos, estats i tocar un municipi.
4. **Onboarding i regles de joc.** Desbloqueig, economia de punts, i tests que demostrin que el joc no es pot quedar encallat.
5. **Fitxa i missions.** Proves amb GPS i amb foto, i missions pròpies.
6. **Fotos i àlbum.** Amb la visibilitat de cada foto.
7. **Perfil.** Nivells, medalles i comarques.
8. **Poliment.** Accessibilitat, rendiment, exportar i esborrar dades, atribucions.
9. **Servidor.** Projecte Supabase, esquema, RLS i migracions amb tests.
10. **Comptes.** Pantalla d'inici de sessió amb els tres mètodes, perfil, tancar sessió i esborrar les dades del joc.
11. **Sincronització.** Esborrats lògics, cua de pendents, pujada i baixada amb WorkManager i fusió amb tests.
12. **Fotos al núvol.** Pujar-les a Storage amb la visibilitat aplicada.
13. **Rànquing.** Taula oficial de missions al servidor, càlcul al servidor i pantalla.
14. **Seguir.** Explorador o Espectador, comptes públics i privats, la pestanya «Gent», el mur i el perfil dels altres.

## 11. Decisions pendents

- Baixar la base municipal de l'ICGC i decidir-ne el format d'entrada per a l'script.
- Ajustar els valors de l'economia (secció 3.3) després de provar-la.
- Quines categories de Wikidata i d'OSM es converteixen en missions, i quants punts val cada categoria.
- Nom definitiu de l'app i icona.
- Normes del nom d'usuari (llargada, caràcters permesos, paraules prohibides).
- Si el rànquing és només de punts o també de municipis descoberts.
- URL de la política de privacitat i de la pàgina per esborrar les dades.
- Si esborrar només les dades de l'app (i no el compte compartit) compleix la política d'esborrat de comptes de Google Play. Si no, caldrà un projecte de Supabase dedicat.

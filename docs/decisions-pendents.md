# Decisions preses, pendents d'implementar

Decisions que ha pres el Martí i que encara no són al codi. Quan s'implementin, passen a `requisits.md` i s'esborren d'aquí.

## Actualitzacions automàtiques al mòbil (Obtainium)

- L'app s'instal·la i s'actualitza amb [Obtainium](https://github.com/ImranR98/Obtainium), que vigila les publicacions (releases) de GitHub.
- **Quan:** cada vegada que la compilació passa a `main`, la CI publica una release amb l'APK.
- **Repositori:** públic. Obtainium hi accedeix sense token.
- **Cal fer:**
  - una clau de signatura fixa per a les versions de prova, perquè cada APK s'instal·li sobre l'anterior;
  - un número de versió (`versionCode`) que creixi a cada compilació, perquè Android accepti l'actualització;
  - un pas a la CI que publiqui la release;
  - les passes per configurar Obtainium al mòbil.

## Perfils: Explorador i Espectador

- En entrar, l'usuari tria entre dos perfils:
  - **Explorador:** recorre els municipis i juga;
  - **Espectador:** no explora, segueix virtualment altres persones per veure com van.
- Un Espectador pot seguir **diverses persones**, i tot el que veu és només de la gent que segueix:
  - **Mapa:** veu el mapa de cada persona, amb els municipis que ha descobert.
  - **Avisos:** rep una notificació quan l'altre desbloqueja un municipi o completa una missió.
  - **Animar:** pot enviar una reacció o un missatge curt quan l'altre aconsegueix alguna cosa.
  - **Fotos:** veu les fotos de les missions de l'altre.
- Un **Explorador també pot seguir** altres exploradors, i hi veu el mateix que un Espectador.
- Necessita els comptes (Supabase) per compartir les dades entre mòbils.

## Comptes públics i privats

- Cada usuari, sigui Explorador o Espectador, tria si el seu compte és **públic** o **privat**.
- **Públic:** qualsevol el pot seguir directament.
- **Privat:** seguir-lo és una sol·licitud, que l'usuari **accepta o rebutja**. Fins que no l'accepta, no es veu res del seu compte: ni el mapa, ni els avisos, ni les fotos.
- **Què mostra el perfil d'un Espectador**, que no té mapa ni fotos:
  - a tothom, el nom i a quanta gent segueix;
  - la llista de persones que segueix, només a qui ell ha acceptat (si el compte és privat) o a tothom (si és públic);
  - les reaccions que envia, només a la persona que les rep.
- **En crear el compte**, l'usuari tria si és públic o privat. No hi ha cap opció marcada per defecte.

## Avisos

- Els avisos arriben com a **notificacions del mòbil**, encara que l'app estigui tancada.
- Cal Firebase Cloud Messaging. És un SDK de Google, i quan s'afegeixi cal actualitzar la norma de `CLAUDE.md` que ho prohibeix a la versió 1.
- A Ajustos, l'usuari ha de poder apagar-les.

## Reaccions per animar

- **Emojis propis de l'app**, dibuixats amb l'estètica del logo: fanals, sardana, castellers, etc.
- **Frases fetes** en català.
- **Missatges lliures** curts.
- Com que hi ha text lliure, cal poder **bloquejar** i **denunciar** algú que molesta.

## Rànquings

- **Entre la gent que segueixo**, amb mi inclòs.
- **General de Catalunya**: només comptes públics.
- **Per comarca**: qui ha descobert més municipis d'una comarca. També només comptes públics.
- Es classifica per **municipis descoberts**.
- Cada rànquing té dues versions: **de sempre** (històric) i **del mes** en curs, que es reinicia l'1 de cada mes. Al del mes compten els municipis descoberts aquell mes.

## Medalles

Les medalles substitueixen els assoliments: al perfil hi ha una **vitrina de medalles** i els assoliments deixen d'existir com a cosa a part.

- **Medalla de comarca (43), amb tres nivells:**
  - **Bronze:** tots els municipis de la comarca descoberts.
  - **Plata:** a més, totes les missions automàtiques dels seus municipis fetes (les missions pròpies no compten, com que no donen punts).
  - **Or:** a més, la foto del cartell d'entrada de cada municipi de la comarca.
- **Fites de municipis:** 10, 50, 100, 250, 500 i 947 municipis descoberts (abans eren assoliments).
- **Capitals de comarca:** medalla per haver descobert totes les capitals de comarca.
- **Cartells:** fites de fotos del cartell d'entrada: 1, 25, 100, 500 i 947.
- **El repte** (947 municipis) i la **medalla física**: com s'explica a «Coses per fer».
- **Quan en guanyes una:** surt la celebració de la sardana amb la medalla, i els que et segueixen reben un avís per poder-te animar amb reaccions.
- **Punts (pocs):** comarca bronze 25, plata 50, or 100; fites de municipis 20; cartells 20; capitals de comarca 100. Com que desbloquejar un municipi costa entre 60 i 200 punts, ajuden una mica sense trencar el joc. Els valors aniran a `dades/configuracio_joc.json`, amb tests.
- **Vitrina:** es veuen totes les medalles; les que encara no tens, en gris i amb el progrés (per exemple, «12 de 23 municipis»).
- **Primer municipi, primera missió i primera foto:** no seran medalles. Seran una altra cosa, encara per decidir (segurament sacs).
- **Disseny:** estil **A, clàssica**: medalla rodona penjada d'una cinta amb la senyera, la silueta de la comarca (o una icona per a les altres) al mig i el nom en una banda a sota; el color segons el nivell (bronze, plata, or). Les que encara no tens, en gris. Generador: `disseny/medalles/`.

## Passaport

A més de les medalles, l'app té un **passaport**: cada municipi descobert hi posa el seu **segell**, com els segells d'un passaport de viatge (inspirat en l'estil C de les propostes de medalla). El passaport té una pàgina per comarca, amb la silueta i el recompte (per exemple, «12 de 21 municipis»).

- **Tapa:** l'usuari tria el color entre tres opcions: **granat**, **blau fosc** o **els colors de l'app** (fosc amb ambre). Totes amb la silueta de Catalunya en daurat.
- **Segell:** **rectangular**, amb la silueta del municipi, el nom i la data del descobriment. Cada segell surt amb un color de tinta i una mica girat, com en un passaport de veritat.
- **Pàgina lliure:** en lloc d'una graella, la pàgina és com la d'un passaport de veritat i **l'usuari tria on posa cada segell**. En completar un municipi: premis, el passaport s'obre amb una animació, l'usuari toca un lloc de la pàgina i el segell hi cau **sense tampó**: baixa des de dalt, pica sobre el paper, fa una onada i esquitxa unes gotes de tinta. Si li agrada on ha quedat, l'accepta; si no, el repeteix (el segell s'esborra i torna a triar el lloc). Prototip: `disseny/passaport/prototip-segellar.html`.
- **Pàgines per comarca:** com a màxim **20 segells per pàgina**. Quan una pàgina en té 20, en comença una altra de la mateixa comarca (l'Alt Empordà, amb 68 municipis, en tindria 4). Els segells mantenen la mida d'ara. El valor (20) ha d'anar a la configuració del joc.
- **Municipis que encara no tens:** de moment no es mostren; la pàgina només té els segells posats i el recompte (per exemple, «6 de 21»).
- **Quan es posa el segell:** en fer la missió de **check-in amb el GPS** al municipi (el municipi s'ha de desbloquejar abans amb punts, com sempre). En tancar la celebració del check-in s'obre el passaport. Els check-ins fets abans que hi hagués passaport surten a «Per segellar».
- **A l'app:** el passaport és al perfil («El meu passaport»): la tapa, el color que es tria, les pàgines de cada comarca i els municipis per segellar. Els segells es desen al mòbil (taula `segells`) i surten a l'exportació. Pendent: sincronitzar-los amb Supabase quan es faci la sincronització.
- Generador de propostes: `disseny/passaport/`.

## Coses per fer

- **Catàleg de municipis:**
  - Una pantalla amb els 947 municipis, que es pot ordenar **per comarques** o **per ordre alfabètic**.
  - Cada municipi té la seva casella. Quan es completa, la foto del seu **cartell d'entrada** s'hi col·loca, com un àlbum de cromos.
- **Càmera del cartell:**
  - A cada municipi hi ha l'opció «Fes la foto del cartell».
  - S'obre la càmera de l'app amb un **requadre rectangular** imposat a sobre: cal enquadrar-hi el cartell i fer la foto.
  - Només es guarda la part de dins del requadre (la foto queda retallada a la mida del cartell).
  - Es comprova amb el GPS que la foto s'ha fet al municipi, com les altres missions de foto.
  - Si és vàlida, completa la missió del cartell i la foto passa al catàleg. És també la prova per a la medalla física.
- **Medalles per comarca:**
  - Cada comarca completada (tots els seus municipis descoberts) dona una **medalla digital**, que es veu al perfil.
  - **El repte** és completar els **947 municipis**.
  - Qui completi el repte rebrà una **medalla física** si:
    - el projecte ha anat bé;
    - ha fet la **foto del cartell d'entrada de cada municipi** (la missió «Fes una foto del cartell d'entrada»).
  - Les fotos s'han de fer amb la càmera de l'app, que ja no deixa triar fotos de la galeria i valida la ubicació amb el GPS.
  - Ara les fotos només es guarden al mòbil. Per poder comprovar el repte caldrà pujar-les (o almenys la del cartell) al núvol, o guardar-ne una prova verificable.
  - Pendent de decidir com es fa arribar la medalla física (adreça d'enviament i revisió de les fotos). **Serà de les últimes coses que es faran**, perquè encara no se sap si hi haurà medalla física.
- **Medalles amb estil propi per comarca:** més endavant, cada comarca tindrà una medalla amb un dibuix propi. Per triar-lo, el Martí respondrà un **qüestionari per comarca** (què és el més representatiu de cada una), perquè no s'inventi res.
- **Col·laboracions amb els ajuntaments** dels municipis. Pendent de decidir què s'hi ofereix i com.
- **Sistema de sacs:** sacs que es guanyen jugant i que, en obrir-los, donen a l'atzar coses per personalitzar l'app:
  - emojis per a les reaccions;
  - colors secundaris;
  - animacions de càrrega;
  - etc.

  Pendent de decidir com es guanyen (punts, missions, nivells…) i si algunes coses són més rares que d'altres.
- **Emojis propis:** proposta a l'artifact «Emojis propis». Totes les persones porten barretina. El brindis és un porró i una ampolla amb estrella, sense cap marca comercial.

## Valoració dels emojis

El Martí ha valorat els 155 emojis proposats (artifact «Tria d'emojis»).

### M'agraden (71)

- Recarregant piles (Ànims · Bateria)
- Ets un far (Ànims · Fanal)
- En ratxa (Ànims · Foc)
- Gairebé hi ets! (Ànims · Barra de progrés)
- Al·lucino (Cares · Al·lucinant)
- Atxim! (Cares · Esternudant)
- A ballar! (Cares · Ballant)
- Em bull la sang (Cares · Bullint)
- Que bé! (Cares · Content)
- Que arribo tard! (Cares · Corrent)
- Quina decepció (Cares · Decebut)
- Estic rebentat (Cares · Dormint)
- Quina ràbia (Cares · Enfadat)
- Festa major! (Cares · De festa)
- Quina idea! (Cares · Amb una idea)
- Mec! (Cares · Fent llengotes)
- Estic marejat (Cares · Marejat)
- Ploro a mars (Cares · Plorant a mars)
- Mentida! (Cares · Mentider)
- Quin temazo (Cares · Escoltant música)
- Sense paraules (Cares · Sense paraules)
- Tot del revés (Cares · Del revés)
- M'escanyo de riure (Cares · Rodolant)
- Salut! (Cares · Bevent amb porró)
- Sóc un sant (Cares · Sant)
- Foto! (Cares · Fent-se una foto)
- Molt senyor (Cares · Molt senyor)
- Mmm, sospitós (Cares · Sospitós)
- Uf, quina pujada (Cares · Suant)
- Quina pena (Cares · Plorant)
- Vinga, va! (Cares · Impacient)
- Visca! (Cares · Cridant)
- Que bé m'ho passo (Cares · Xalant)
- Amb tot el cor (Celebrar · Cor)
- Que bonic! (Celebrar · Globus)
- Medalla d'or (Celebrar · Medalla)
- Per molts anys! (Celebrar · Pastís)
- Ballem! (Celebrar · Sardana)
- Campió! (Celebrar · Trofeu)
- Compte! (Comentar · Alerta)
- Quina troballa! (Comentar · Rovelló)
- Aquí no hi ha cobertura (Comentar · Sense cobertura)
- Quin lloc! (Comentar · Lloc)
- A veure, a veure… (Comentar · Lupa)
- On ets? (Comentar · Mapa)
- Quina calor! (Del dia · Termòmetre)
- Bona nit (Del dia · Lluna)
- Plou i fa sol (Del dia · Pluja)
- Bon dia (Del dia · Sol)
- Quin cap més gros! (Festes · Capgròs)
- Amunt l'estel! (Festes · Estel)
- Anem a la fira! (Festes · Fira)
- Bona revetlla (Festes · Revetlla)
- Feliç Sant Jordi (Festes · Rosa i llibre)
- Anem pedalant (Ruta · Bicicleta)
- Cap al nord! (Ruta · Brúixola)
- Quina foto! (Ruta · Càmera)
- Pel bon camí (Ruta · Marca de GR)
- Ja tinc la motxilla feta (Ruta · Motxilla)
- Acampada! (Ruta · Tenda)
- A poc a poc (Taula · Caragol)
- Al bosc! (Terra · Bosc)
- A la platja! (Terra · Cala)
- Toquen a festa! (Terra · Campana)
- Beee! (Terra · Ovella)
- Tossut com un ruc (Terra · Ruc català)
- Ruta en furgo (Vehicles · Furgoneta)
- Fins al cel! (Vehicles · Globus aerostàtic)
- Zum, zum (Vehicles · Patinet)
- Quines vistes! (Vehicles · Telefèric)
- Vent a favor (Vehicles · Veler)

### Cal millorar (60)

- Bona caminada (Ànims · Bota): No sembla que camini
- Força! (Ànims · Castell)
- Al cim! (Ànims · Muntanya)
- Tu pots! (Ànims · Puny): No sembla un puny
- Ai, mare! (Cares · Mà a la cara): La ma
- A tope! (Cares · A tope): La ma
- Bravo! (Cares · Aplaudint): Les mans sembla que tinguin urpes
- Calla, calla! (Cares · Rient tapant-se la boca): La ma
- Ei! (Cares · Saludant): Les mans sembla que tingui urpes
- M'emociono (Cares · Emocionat): No sembla que s'emocioni
- M'encanta (Cares · Enamorat): Els cors haurien de ser més grans
- Quin fàstic (Cares · Fastiguejat): La llengua no quadra
- Flipo (Cares · Flipant): Les estrelles han de ser més grans
- Em fonc (Cares · Fonent-se): No sembla que es fongui
- Quin fred! (Cares · Glaçat)
- Tinc gana (Cares · Afamat): La gota està tallada
- Estic fotut (Cares · Malalt): Sembla que estigui fumant
- Ni idea (Cares · Arronsant les espatlles): Les mans
- Ni de conya (Cares · Negant): No sembla que digui que no
- Mmm… (Cares · Pensant): No sembla que estigui pensat
- Em peta el cap (Cares · Petant el cap): Els ulls han de ser més grans
- Petonets (Cares · Fent un petó): La posició dels ulls i boca no m'agraden
- Quina por! (Cares · Esglaiat): Les mans
- Em pixo de riure (Cares · Rient): Sembla que estigui suant
- Xxxt! (Cares · Fent silenci): No sembla un dit
- Sí, sí! (Cares · Assentint): No sembla que digui que si
- Quina son (Cares · Badallant): La ma
- Ostres! (Cares · Sorprès): Els ulls han de ser més gran
- Quina trapelleria (Cares · Trapella): No es veuen les banyes
- Crack (Cares · Amb ulleres): Les ulleres han de ser més grans
- Ja m'entens (Cares · Picant l'ullet): La llengua no quadra
- Quina paciència (Cares · Ulls al cel): Els ulls
- Ai, que vergonya (Cares · Avergonyit): No sembla avergonyit
- Brindem! (Celebrar · Brindis): Haurien de ser dos cerveses, la cervesa sembla una ampolla de vi
- Ets un gegant! (Celebrar · Gegant): no sembla un gegant
- Visca, visca! (Celebrar · Canó de confeti): Més confeti
- Quina festa! (Celebrar · Traca)
- Ups! (Comentar · Caganer): No sembla el caganer
- M'ho apunto (Comentar · Llibreta): El llapis
- Molt nostre (Del dia · Barretina): No sembla
- Primer, un cafè (Del dia · Cafè)
- Calçotada! (Del dia · Calçot)
- Neva! (Del dia · Ninot de neu)
- Bon profit (Del dia · Pa amb tomàquet)
- Fem el vermut? (Del dia · Vermut)
- Toc, toc, toc! (Festes · Ball de bastons)
- Que salti el tap! (Festes · Cava)
- Correfoc! (Festes · Correfoc): No sembla
- Quin drac! (Festes · Drac)
- Caga tió! (Festes · Tió)
- Ja hi som! (Ruta · Cartell d'entrada): No entenc l'animació
- Carretera i manta (Ruta · Cotxe)
- El tren torna a anar tard (Ruta · Tren)
- Quin salt d'aigua! (Terra · Salt d'aigua)
- A ballar sardanes (Terra · Espardenya): No ho entenc
- Bona pesca (Terra · Llaüt)
- Me'n vaig de viatge (Vehicles · Avió)
- Agafem el bus (Vehicles · Autobús)
- Brrrum! (Vehicles · Moto)
- Pagès de cor (Vehicles · Tractor)

### No m'agraden (24)

- Endavant! (Ànims · Senyal)
- Pas a pas (Ànims · Petjades)
- Je, je (Cares · Burleta)
- Quina ressaca (Cares · Amb ressaca)
- T'estic seguint (Comentar · Prismàtics)
- Ja era hora! (Comentar · Rellotge)
- Visca el carnaval! (Festes · Carnestoltes)
- Toca la gralla! (Festes · Gralla)
- Bona Pasqua (Festes · Mona)
- Quin ou més ballador! (Festes · L'ou com balla)
- Bon Nadal (Festes · Pessebre)
- A esquiar! (Ruta · Esquís)
- Allioli! (Taula · Allioli)
- Botifarra! (Taula · Botifarra)
- Bona castanyada (Taula · Castanyes)
- Boníssim! (Taula · Crema catalana)
- Escudella i carn d'olla (Taula · Escudella)
- Un fuet i cap a la muntanya (Taula · Fuet)
- Bons panellets! (Taula · Panellets)
- Qui té la fava? (Taula · Tortell de Reis)
- Xocolata amb melindros (Taula · Xocolata desfeta)
- Oli d'aquí (Terra · Olivera)
- Verema! (Terra · Raïm)
- Amunt per la cremallera (Vehicles · Cremallera)


### Tercera ronda d'emojis

- **M'agraden (18):** Força!; Al cim!; Ai, mare!; Bravo!; Ei!; M'encanta; Ni idea; Mmm…; Ostres!; Crack; Ai, que vergonya; Calçotada!; Que salti el tap!; Caga tió!; Ja hi som!; Carretera i manta; Quin salt d'aigua!; Pagès de cor
- **Cal millorar (18):** Bona caminada; Quin fred!; Ni de conya (sembla una campana no una persona dient que no); Petonets (la cara està deforma); Em pixo de riure; Quina trapelleria; Brindem! (no semblen estrelles dam, si vols una foto de referencia avisa); Quina festa! (No semblen focs artificials); Ups! (està molt deforme); Molt nostre (Continua sense semblar una barretina com tota la resta); Primer, un cafè; Neva!; Bon profit; Toc, toc, toc! (els pals estan mal colocat); Correfoc! (les banyes estan volant i la banya dreta no sembla una banya); Quin drac!; Agafem el bus (no sembla un bus); Brrrum! (no sembla una moto)
- **No m'agraden (8):** Calla, calla!; M'emociono; Xxxt!; Ets un gegant!; Fem el vermut?; El tren torna a anar tard; Bona pesca; Me'n vaig de viatge
- **Sense valorar (amb nota) (2):** A tope! (sembla que estigui cabrejat); Sí, sí! (no sembla que digui que si, sembla que estigui saltant)

### Quarta ronda d'emojis

La barretina ja és la del dibuix de referència (l'adhesiu de «La barretina del tió»), a la mida del cap.

- **M'agraden (15):** A tope!; Bravo!; Ni de conya; Mmm…; Petonets; Sí, sí!; Ostres!; Força!; Ups!; Molt nostre; Toc, toc, toc!; Correfoc!; Caga tió!; Agafem el bus; Brrrum!
- **Cal millorar (10):** Bona caminada (hi ha una línia al mig); M'encanta (la barretina tapa els cors); Quin fred! (fa por); Em pixo de riure (les llàgrimes són massa grosses); Quina trapelleria (les banyes no quadren i la de la dreta no ho sembla); Brindem! (les etiquetes no estan al seu lloc i són massa grogues); Quina festa! (no semblen focs artificials); Neva! (està deforme); Bon profit (no sembla pa amb tomàquet); Quin drac! (no sembla un drac)
- **No m'agraden (1):** Primer, un cafè

### Cinquena ronda d'emojis

- **M'agraden (5):** Bona caminada; Brindem!; Quina festa!; Neva!; Quin drac!
- **Cal millorar (2):** Quina trapelleria (les banyes semblen orelles d'ase); Bon profit (la gota d'oli, la sal i el tomàquet del costat sobren, i ha de ser pa de pagès)
- **Sense valorar (3):** M'encanta; Quin fred!; Em pixo de riure

### Sisena ronda d'emojis

- **M'agraden (2):** M'encanta; Em pixo de riure
- **Cal millorar (3):** Quin fred! (continua fent por); Quina trapelleria (les banyes no segueixen l'angle del cap); Bon profit (no sembla un pa)

### Setena ronda d'emojis

- **M'agraden (2):** Quin fred!; Quina trapelleria
- **Cal millorar (1):** Bon profit (no sembla una llesca de pa amb tomàquet)

### Vuitena ronda d'emojis

- **M'agraden (1):** Bon profit

Amb aquesta ronda tots els emojis queden aprovats (119). El generador és a `disseny/emojis/`. Falta passar-los a l'app.

### Revisió final de tots els emojis aprovats

- **M'agraden (109)**
- **Cal millorar (9):** Al cim! (el pic de la dreta està mal col·locat); Flipo (els ulls queden sota la barretina); Quina idea! (no se li veuen les celles); Petonets (hi ha d'haver animació del petó); Em pixo de riure (no m'agrada l'orientació de les llàgrimes); Crack (les ulleres haurien de ser més animades); A la platja! (refer de zero); Toquen a festa! (més animada i més campana); Agafem el bus (no em recorda a un bus)
- **No m'agraden (1):** Uf, quina pujada

A partir d'ara, a les pàgines de valoració, escriure una nota marca l'emoji com a «Cal millorar», i «Cal millorar» sense nota vol dir refer-lo de zero.

### Revisió final, segona passada

- **M'agraden (3):** Al cim!; Quina idea!; Agafem el bus
- **Cal millorar (6):** Flipo (els ulls queden sota la boca i s'han de fer més grans); Petonets (no hi ha animació i la boca és estranya); Em pixo de riure (les llàgrimes són massa); Crack (ha de millorar l'animació de les ulleres); A la platja! (no sembla una platja); Toquen a festa! (massa detall i hi ha d'haver més animació)

Les pàgines de valoració no tenien el CSS de les animacions noves; des de la tercera passada s'hi afegeix sempre el CSS actual.

### Revisió final, tercera passada

- **M'agraden (2):** Crack; Toquen a festa!
- **Cal millorar (3):** Flipo (es continuen ficant els ulls sota la barretina, haurien de quedar a dalt); Petonets (no m'agrada gens la boca); Em pixo de riure (no m'agraden gens les llàgrimes, m'agradava més quan eren gotes)
- **No m'agraden (1):** A la platja!

### Revisió final, quarta passada

- **M'agraden (2):** Flipo; Em pixo de riure
- **Cal millorar (1):** Petonets (no m'agrada gens la boca). Es proposen tres boques per triar.

### Revisió final, cinquena passada

- Petonets: de les tres boques, la B (boqueta petita i plena).

Amb això tots els emojis queden aprovats: **117**. Falta passar-los a l'app.

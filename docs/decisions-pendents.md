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

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

## Perfils: Explorador i Copilot

- En entrar, l'usuari tria entre dos perfils:
  - **Explorador:** recorre els municipis i juga;
  - **Copilot:** no explora, segueix altres persones.
- Un Copilot pot seguir **diverses persones**, i tot el que veu és només de la gent que segueix:
  - **Mapa:** veu el mapa de cada persona, amb els municipis que ha descobert.
  - **Avisos:** rep una notificació quan l'altre desbloqueja un municipi o completa una missió.
  - **Animar:** pot enviar una reacció o un missatge curt quan l'altre aconsegueix alguna cosa.
  - **Fotos:** veu les fotos de les missions de l'altre.
- Necessita els comptes (Supabase) per compartir les dades entre mòbils.

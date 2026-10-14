# Provar l'app al mòbil amb l'Android Studio

Aquests passos serveixen per instal·lar l'app al teu mòbil des de l'ordinador i provar-la. No cal configurar cap servidor: si no hi ha Supabase configurat, l'app entra en **mode de prova**.

## 1. Instal·la l'Android Studio

Baixa'l de <https://developer.android.com/studio> i instal·la'l amb les opcions per defecte. Porta el JDK i l'SDK d'Android que calen. La primera vegada et demanarà acceptar les llicències de l'SDK: accepta-les.

## 2. Obre el projecte

1. A la pantalla d'inici: **Clone Repository** (o *File → New → Project from Version Control*).
2. URL: `https://github.com/MartiFarranC/municipis.git`. Tria una carpeta i prem **Clone**. Si et demana entrar a GitHub, fes-ho.
3. Quan s'obri, canvia a la branca on és tota la feina: a baix a la dreta (o a dalt a l'esquerra, segons la versió) hi ha el nom de la branca. Clica-hi i tria **origin/claude/municipis-projecte-9z9461 → Checkout**. Quan aquesta branca s'hagi fusionat a `main`, ja podràs fer servir `main`.
4. Espera que acabi la sincronització de Gradle (la barra de progrés de baix). La primera vegada baixa moltes coses i pot trigar una bona estona. Si et proposa instal·lar alguna plataforma o eina de l'SDK que falta (per exemple, *Android SDK Platform 36*), accepta.

Si dona un error de JDK: *Settings → Build, Execution, Deployment → Build Tools → Gradle → Gradle JDK* i tria el que porta l'Android Studio (*jbr-17* o posterior).

## 3. Prepara el mòbil

1. *Configuració → Informació del telèfon* i toca 7 vegades **Número de compilació** fins que digui que ja ets desenvolupador.
2. *Configuració → Sistema → Opcions per a desenvolupadors* i activa **Depuració USB**.
3. Connecta el mòbil a l'ordinador amb el cable. Al mòbil sortirà un avís per permetre la depuració des d'aquest ordinador: **Permet** (i marca *Permet sempre des d'aquest ordinador*).

També es pot fer sense cable (Android 11 o posterior): a l'Android Studio, *Device Manager → Pair Devices Using Wi-Fi*, i al mòbil, *Depuració sense fil → Vincula el dispositiu amb un codi QR*.

## 4. Instal·la i obre l'app

A la barra de dalt de l'Android Studio:

1. A la llista de dispositius, tria el teu mòbil.
2. A la llista de configuracions, ha de sortir **app**.
3. Prem el botó ▶ (**Run 'app'**).

Compila, instal·la l'app i l'obre al mòbil. Els canvis que facis després s'instal·len de la mateixa manera, i les dades del joc es mantenen.

## Mode de prova (sense servidor)

Si no hi ha `supabase.url` i `supabase.anonKey` al fitxer `local.properties`, l'app:

- entra directament amb un compte local que es diu «prova», sense demanar correu ni contrasenya;
- desa tota la partida al mòbil (mapa, missions, fotos, sacs, passaport), com sempre;
- no sincronitza res amb el servidor, i la pestanya **Gent** no funciona (diu que no hi ha connexió).

És el que cal per provar el joc, l'estètica, les animacions, el so i la vibració.

## Amb el servidor de Supabase

Per provar també el compte, la sincronització i la part social, afegeix aquestes dues línies al fitxer `local.properties` de l'arrel del projecte (l'Android Studio el crea sol; no es puja mai a GitHub):

```
supabase.url=https://EL-TEU-PROJECTE.supabase.co
supabase.anonKey=LA-CLAU-ANON
```

Les trobaràs al tauler de Supabase, a *Project Settings → API*: la **Project URL** i la clau **anon public**. **Mai la `service_role`.** La base de dades ha de tenir les migracions aplicades (`npx supabase db push`, docs/requisits.md, secció 9). Després, torna a prémer ▶.

## Si ja tens l'app d'Obtainium

L'app de l'Android Studio i la d'Obtainium tenen el mateix identificador però estan signades amb claus diferents, i Android no deixa instal·lar l'una sobre l'altra. Abans de prémer ▶, desinstal·la la que hi hagi al mòbil (docs/obtainium.md).

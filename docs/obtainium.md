# Instal·lar i actualitzar l'app amb Obtainium

L'app no és a Google Play. S'instal·la i s'actualitza amb [Obtainium](https://github.com/ImranR98/Obtainium), que vigila les releases d'aquest repositori de GitHub i avisa quan n'hi ha una de nova.

## Com es publica

- Cada vegada que es puja a `main` i passen tots els tests (compilació, tests, lint, base de dades i UI), la CI (`.github/workflows/ci.yml`, tasca `publica`) compila l'APK i publica una release.
- **Versió:** `1.0.<n>`, on `<n>` és el número de la compilació de GitHub Actions. El `versionCode` és aquest mateix número, i per això sempre creix, que és el que Android necessita per acceptar l'actualització.
- **Signatura:** l'APK sempre es signa amb la mateixa clau, perquè cada versió s'instal·li sobre l'anterior sense perdre les dades. La clau és als secrets del repositori i **mai** al codi.
- Si falten els secrets, la CI no publica res i ho avisa amb un *warning*, però no falla.

## Secrets del repositori

A GitHub: **Settings → Secrets and variables → Actions → New repository secret**.

| Secret | Què és |
|---|---|
| `SIGNATURA_CLAU` | El fitxer de la clau (`.jks`) en base64 |
| `SIGNATURA_CONTRASENYA` | La contrasenya de la clau |
| `SIGNATURA_ALIES` | L'àlies de la clau (`descobreix`) |
| `SUPABASE_URL` | La URL del projecte de Supabase (la mateixa de `local.properties`) |
| `SUPABASE_ANON_KEY` | La clau pública (`anon`) de Supabase. **Mai** la `service_role` |

### Crear la clau (una sola vegada)

A Windows, amb el `keytool` que porta l'Android Studio (PowerShell):

```powershell
& "C:\Program Files\Android\Android Studio\jbr\bin\keytool.exe" -genkeypair -v `
  -keystore descobreix.jks -alias descobreix -keyalg RSA -keysize 4096 -validity 36500 `
  -dname "CN=Descobreix Catalunya"
```

Demana una contrasenya: és la de `SIGNATURA_CONTRASENYA`. Després, per copiar el fitxer en base64 al porta-retalls:

```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("descobreix.jks")) | Set-Clipboard
```

i enganxa-ho al secret `SIGNATURA_CLAU`.

**Important:** guarda `descobreix.jks` i la contrasenya en un lloc segur, fora del repositori (per exemple, al gestor de contrasenyes). Si es perden, les noves versions no es podran instal·lar sobre les antigues: caldria desinstal·lar l'app i es perdrien les dades que no estiguin sincronitzades.

## Configurar Obtainium al mòbil

1. Instal·la Obtainium des de les seves [releases](https://github.com/ImranR98/Obtainium/releases) (l'APK `app-arm64-v8a-release.apk` serveix per a gairebé tots els mòbils) o des de F-Droid.
2. Obre Obtainium → **Add App**.
3. A **App Source URL**, posa `https://github.com/MartiFarranC/municipis` i toca **Add**.
4. Toca **Install**. La primera vegada, Android demana permís perquè Obtainium pugui instal·lar apps: accepta-ho.
5. A partir d'aquí, Obtainium comprova les releases periòdicament i avisa quan n'hi ha una de nova. Per actualitzar, toca **Update**.

## L'app de l'Android Studio i la d'Obtainium

Totes dues fan servir el mateix identificador (`cat.descobreix`), però estan signades amb claus diferents: la de l'Android Studio amb la clau de depuració del teu ordinador i la d'Obtainium amb la clau de publicació. Android no deixa instal·lar l'una sobre l'altra. Per passar de l'una a l'altra, cal desinstal·lar l'app abans, i es perden les dades que no estiguin sincronitzades amb el compte.

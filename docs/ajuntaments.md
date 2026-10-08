# Guia per als ajuntaments que col·laboren

Què pot posar un ajuntament i què en rep és a la secció 9.6 de [`requisits.md`](requisits.md). Aquí hi ha com es fa.

Als exemples, `08298` és el codi d'un municipi i el que va entre `<` i `>` s'ha de canviar per les dades de l'ajuntament.

Tot es fa des del **SQL Editor de Supabase** (que té permís per escriure) i, per al segell, des de **Storage**. El que s'entra surt directament a l'app: els mòbils ho baixen la propera vegada que sincronitzen (cada 6 hores, o uns segons després d'un canvi).

**Abans de res**, cal que el servidor tingui les taules dels ajuntaments: `npx supabase db push` (migració `20261009120000_ajuntaments.sql`).

## Què cal demanar a l'ajuntament

| Què | Com ha de ser |
|---|---|
| Presentació | Un text de 500 caràcters com a molt |
| Web | Una adreça que comenci per `https://` |
| Oficina de turisme | On és i quan obre, 200 caràcters com a molt |
| Missions | El títol (80 caràcters com a molt), una descripció si cal (300), i com es fa: **GPS** (cal el punt: latitud i longitud), **foto** (dins del municipi) o **QR** (un punt de segellat). Les de festes o fires, amb les dates |
| Segell propi | Un dibuix d'un sol color, en PNG amb el fons transparent, de 512 × 512 píxels. L'app el pinta amb la tinta del segell |
| Avantatges | Què és, les condicions i fins quan val |

Les barretines de cada missió no les tria l'ajuntament: surten de la configuració del joc (`ajuntaments` a `dades/configuracio_joc.json`). De cada municipi, l'app només fa servir les primeres missions (per `ordre`), fins al màxim que diu la configuració.

## Donar d'alta un ajuntament

```sql
insert into descobreix.ajuntaments (codi_ine, presentacio, web, oficina_turisme)
values ('08298', '<presentació>', '<https://web de l''ajuntament>', '<adreça i horari de l''oficina de turisme>');
```

El codi INE de cada municipi és a `app/src/main/assets/dades/municipis.json`. Només amb això, el municipi ja surt com a **col·laborador** a l'app.

## Missions

Amb GPS (cal ser a menys del radi de la configuració del punt):

```sql
insert into descobreix.missions_ajuntament (codi_ine, titol, descripcio, prova, lat, lon, ordre)
values ('08298', '<títol>', '<descripció>', 'GPS', <latitud>, <longitud>, 1);
```

Amb una foto (feta dins del municipi):

```sql
insert into descobreix.missions_ajuntament (codi_ine, titol, prova, ordre)
values ('08298', '<títol>', 'FOTO', 2);
```

D'una festa o una fira (només es pot fer aquests dies, i dona un bonus):

```sql
insert into descobreix.missions_ajuntament (codi_ine, titol, prova, data_inici, data_fi, ordre)
values ('08298', '<títol>', 'FOTO', '<primer dia, 2027-03-20>', '<últim dia, 2027-03-21>', 3);
```

### Punt de segellat amb QR

```bash
cd scripts && npm install
node qr-segellat.js 08298 "<títol de la missió>"
```

Fa la imatge del QR a `scripts/sortida/` (per imprimir i enviar a l'ajuntament) i escriu l'SQL per entrar la missió. Al servidor només hi va el resum del codi, no el codi. Si es perd la imatge, no es pot refer: se'n fa un de nou i es retira la missió antiga.

## Segell propi

1. A Supabase, **Storage → `descobreix-ajuntaments`**, crea la carpeta amb el codi del municipi (`08298`) i puja-hi el dibuix amb el nom `segell.png`.
2. Després:

```sql
update descobreix.ajuntaments set te_segell = true where codi_ine = '08298';
```

Per canviar-lo, substitueix el fitxer i torna a fer l'`update` (així els mòbils el tornen a baixar).

## Avantatges

```sql
insert into descobreix.avantatges (codi_ine, titol, descripcio, condicions, valid_fins)
values ('08298', '<títol>', '<descripció>', '<condicions>', '<fins quan val, 2027-12-31>');
```

## Canviar o retirar

Es canvia amb un `update` normal. **No s'esborra mai cap fila**: es retira, perquè els mòbils sàpiguen que l'han de treure.

```sql
update descobreix.missions_ajuntament
set esborrat_el = (extract(epoch from now()) * 1000)::bigint
where id = '…';
```

Igual per a `ajuntaments` (deixa de ser col·laborador) i `avantatges`. Qui ja havia fet una missió retirada no perd les barretines.

## Xifres per a l'ajuntament

```sql
select * from descobreix.xifres_ajuntament('08298');
```

- La fila sense mes diu **quanta gent ha desbloquejat el municipi**.
- Les altres, **quantes missions s'hi han fet cada mes** (dels últims 12) i quanta gent les ha fetes.
- Si hi ha menys de 10 persones, la xifra surt buida o el mes no hi surt, perquè no es pugui saber qui és qui.

## Cartell per promocionar l'app

```bash
cd scripts && node cartell-ajuntament.js 08298
```

Fa `scripts/sortida/cartell-08298.html`: s'obre al navegador i s'imprimeix (o es desa en PDF) en A4. Porta un QR a la pàgina per instal·lar l'app ([`obtainium.md`](obtainium.md)).

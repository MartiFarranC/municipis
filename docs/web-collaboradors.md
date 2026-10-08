# Web per als col·laboradors

Una web perquè els **consells comarcals** (un per comarca: 43 contactes en lloc de 947) i, si volen, els ajuntaments ens ajudin a decidir què surt a la medalla de cada comarca i quines missions hi ha amb els llocs emblemàtics dels municipis. Res d'això no s'inventa: ho proposen ells i ho revisa el Martí.

- **Codi:** `web/` (pàgina principal i espai de col·laboració), estàtica, en català.
- **Servidor:** Supabase, migració `20261010120000_collaboradors.sql` (amb tests a `supabase/tests/collaboradors.test.sql`). La web fa servir la clau pública i només pot cridar les funcions que comproven el codi d'accés; no pot llegir cap taula.
- **Publicació:** GitHub Pages, amb `.github/workflows/web.yml`, cada vegada que canvia `web/` a `main`. Després s'hi posarà el domini propi.

## Abans de parlar amb ningú

- [ ] Crear els comptes d'**Instagram** i **TikTok** del projecte (depenen del nom: vegeu la secció 11 de `requisits.md`).
- [ ] Escriure els textos personals de la pàgina principal (marcats amb `PENDENT` a `web/index.html`): **qui sóc** i **per què ho faig**. Només els pot escriure el Martí.
- [ ] Posar-hi el correu de contacte i els enllaços a les xarxes.
- [ ] Triar i comprar el **domini** (vegeu més avall).
- [ ] Aplicar la migració al servidor: `npx supabase db push`.

## Posar-la en marxa

1. A GitHub: **Settings → Pages → Build and deployment → Source: GitHub Actions**.
2. Els secrets `SUPABASE_URL` i `SUPABASE_ANON_KEY` (els mateixos que per a Obtainium, `docs/obtainium.md`). Sense aquests, la web es publica però no es pot entrar a l'espai de col·laboració.
3. Quan es fusioni a `main`, la web surt a `https://martifarranc.github.io/municipis/`.

### Domini propi

1. Comprar-lo (per exemple a un registrador de dominis `.cat`).
2. A GitHub: **Settings → Pages → Custom domain**, posar-hi el domini.
3. Al registrador, els registres DNS que diu GitHub (un `CNAME` cap a `martifarranc.github.io`, o els `A` de GitHub per al domini arrel). Marcar **Enforce HTTPS** quan estigui a punt.

## Donar un codi a un col·laborador

```bash
cd scripts
node codi-collaborador.js comarca 24        # el consell comarcal d'Osona
node codi-collaborador.js municipi 08298    # l'Ajuntament de Vic
```

L'script escriu el codi (per enviar-lo per correu) i l'SQL per donar-lo d'alta al servidor. El codi no es guarda enlloc més: si es perd, se'n fa un de nou i l'antic es dona de baixa:

```sql
update descobreix.collaboradors set actiu = false where codi = '24' and tipus = 'CONSELL_COMARCAL';
```

Els codis de les comarques són a `app/src/main/assets/dades/municipis.json` (`comarques`).

## Llegir les propostes

Al SQL Editor de Supabase:

```sql
-- Propostes de medalla, per comarca
select c.nom, p.que_hi_surt, p.per_que, p.referencia, p.creat_el
from descobreix.propostes_medalla p join descobreix.collaboradors c on c.id = p.collaborador_id
order by c.codi, p.creat_el;

-- Propostes de missions
select c.nom, p.codi_ine, p.titol, p.descripcio, p.lloc, p.lat, p.lon, p.prova, p.data_inici, p.data_fi
from descobreix.propostes_missio p join descobreix.collaboradors c on c.id = p.collaborador_id
order by p.codi_ine, p.creat_el;

-- Persones de contacte
select c.nom, k.nom, k.carrec, k.correu from descobreix.contactes_collaboradors k
join descobreix.collaboradors c on c.id = k.collaborador_id;
```

Les missions acceptades s'entren a l'app com a missions oficials, seguint `docs/ajuntaments.md`.

## Dades personals

Només es guarda el contacte que el col·laborador hi posa, amb el seu consentiment (la casella del formulari), i només per parlar del projecte. Si algú demana esborrar-lo:

```sql
delete from descobreix.contactes_collaboradors
where collaborador_id = (select id from descobreix.collaboradors where codi = '24');
```

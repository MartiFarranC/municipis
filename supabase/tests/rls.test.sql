-- Tests de l'esquema descobreix: RLS, seguiments, visibilitat de les fotos, el mur i esborrat de dades.
-- S'executen amb `npx supabase test db`.

begin;
create extension if not exists pgtap with schema extensions;

select plan(47);

-- Tres usuaris: en Biel seguirà l'Anna (compte privat); la Clara té el compte públic.
insert into auth.users (id, email) values
    ('00000000-0000-0000-0000-00000000000a', 'anna@example.com'),
    ('00000000-0000-0000-0000-00000000000b', 'biel@example.com'),
    ('00000000-0000-0000-0000-00000000000c', 'clara@example.com');

create function pg_temp.com_a(usuari uuid) returns void language sql as $$
    select set_config('request.jwt.claims', json_build_object('sub', usuari, 'role', 'authenticated')::text, true);
$$;
grant execute on function pg_temp.com_a(uuid) to authenticated;

-- Perfils ---------------------------------------------------------------------------------------

set local role authenticated;
select pg_temp.com_a('00000000-0000-0000-0000-00000000000a');

select lives_ok(
    $$ insert into descobreix.perfils (id, nom_usuari) values ('00000000-0000-0000-0000-00000000000a', 'anna') $$,
    'Un usuari pot crear el seu perfil'
);
select throws_ok(
    $$ insert into descobreix.perfils (id, nom_usuari) values ('00000000-0000-0000-0000-00000000000b', 'biel') $$,
    '42501', null,
    'Un usuari no pot crear el perfil d''un altre'
);

select pg_temp.com_a('00000000-0000-0000-0000-00000000000b');
select throws_ok(
    $$ insert into descobreix.perfils (id, nom_usuari) values ('00000000-0000-0000-0000-00000000000b', 'Biel!') $$,
    '23514', null,
    'El nom d''usuari només pot tenir minúscules, números i _'
);
select throws_ok(
    $$ insert into descobreix.perfils (id, nom_usuari) values ('00000000-0000-0000-0000-00000000000b', 'anna') $$,
    '23505', null,
    'El nom d''usuari és únic'
);
insert into descobreix.perfils (id, nom_usuari) values ('00000000-0000-0000-0000-00000000000b', 'biel');

select pg_temp.com_a('00000000-0000-0000-0000-00000000000c');
insert into descobreix.perfils (id, nom_usuari) values ('00000000-0000-0000-0000-00000000000c', 'clara');

select is((select count(*) from descobreix.perfils), 3::bigint, 'Tothom amb sessió veu tots els perfils');

-- Progrés de l'Anna -----------------------------------------------------------------------------

select pg_temp.com_a('00000000-0000-0000-0000-00000000000a');

select lives_ok(
    $$ insert into descobreix.municipis_descoberts (id, codi_ine, es_inici, cost, creat_el, modificat_el)
       values ('10000000-0000-0000-0000-000000000001', '08298', true, 0, 1, 1) $$,
    'Un usuari pot afegir un municipi descobert'
);
select is(
    (select usuari_id from descobreix.municipis_descoberts where id = '10000000-0000-0000-0000-000000000001'),
    '00000000-0000-0000-0000-00000000000a'::uuid,
    'Per defecte, el municipi és de l''usuari que l''afegeix'
);
select throws_ok(
    $$ insert into descobreix.municipis_descoberts (id, usuari_id, codi_ine, es_inici, cost, creat_el, modificat_el)
       values ('10000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-00000000000b', '08019', false, 60, 1, 1) $$,
    '42501', null,
    'Un usuari no pot afegir progrés a un altre'
);
select throws_ok(
    $$ insert into descobreix.municipis_descoberts (id, codi_ine, es_inici, cost, creat_el, modificat_el)
       values ('10000000-0000-0000-0000-000000000003', '8298', false, 60, 1, 1) $$,
    '23514', null,
    'El codi INE ha de tenir 5 xifres'
);
select throws_ok(
    $$ insert into descobreix.municipis_descoberts (id, codi_ine, es_inici, cost, creat_el, modificat_el)
       values ('10000000-0000-0000-0000-000000000004', '08298', false, 60, 1, 1) $$,
    '23505', null,
    'Un municipi només es pot descobrir una vegada'
);
select throws_ok(
    $$ update descobreix.municipis_descoberts set cost = 0 $$,
    '42501', null,
    'Els municipis descoberts no es poden modificar'
);
select throws_ok(
    $$ delete from descobreix.municipis_descoberts $$,
    '42501', null,
    'Els municipis descoberts no es poden esborrar'
);

insert into descobreix.missions_completades (id, missio_id, codi_ine, punts, bonus, creat_el, modificat_el)
values ('20000000-0000-0000-0000-000000000001', 'gen-08298-checkin', '08298', 100, 0, 1, 1);
select throws_ok(
    $$ insert into descobreix.missions_completades (id, missio_id, codi_ine, punts, bonus, creat_el, modificat_el)
       values ('20000000-0000-0000-0000-000000000002', 'gen-08298-checkin', '08298', 100, 0, 2, 2) $$,
    '23505', null,
    'Una missió només es pot completar una vegada'
);
select throws_ok(
    $$ update descobreix.missions_completades set punts = 1000 $$,
    '42501', null,
    'Les missions completades no es poden modificar'
);

insert into descobreix.moviments_punts (id, tipus, quantitat, motiu, referencia, creat_el, modificat_el)
values ('30000000-0000-0000-0000-000000000001', 'GUANY', 100, 'MISSIO', 'gen-08298-checkin', 1, 1);
select throws_ok(
    $$ update descobreix.moviments_punts set quantitat = 1000 $$,
    '42501', null,
    'Els moviments de punts no es poden modificar'
);

insert into descobreix.missions_propies (id, codi_ine, titol, descripcio, completada, creat_el, modificat_el)
values ('40000000-0000-0000-0000-000000000001', '08298', 'Pujar al Montseny', null, false, 1, 1);

create temp table abans as
    select sincronitzat_el from descobreix.missions_propies where id = '40000000-0000-0000-0000-000000000001';

select lives_ok(
    $$ update descobreix.missions_propies set esborrat_el = 2, modificat_el = 2
       where id = '40000000-0000-0000-0000-000000000001' $$,
    'Una missió pròpia es pot esborrar de manera lògica'
);
select ok(
    (select m.sincronitzat_el > a.sincronitzat_el
     from descobreix.missions_propies m, abans a
     where m.id = '40000000-0000-0000-0000-000000000001'),
    'Modificar una fila n''actualitza sincronitzat_el'
);

-- Fotos de l'Anna, una de cada visibilitat i una d'esborrada.
insert into descobreix.fotos (id, codi_ine, ruta, ruta_miniatura, visibilitat, es_portada, creat_el, modificat_el, esborrat_el)
values
    ('50000000-0000-0000-0000-000000000001', '08298', '00000000-0000-0000-0000-00000000000a/1.jpg', '00000000-0000-0000-0000-00000000000a/1_min.jpg', 'PRIVADA', true, 1, 1, null),
    ('50000000-0000-0000-0000-000000000002', '08298', '00000000-0000-0000-0000-00000000000a/2.jpg', '00000000-0000-0000-0000-00000000000a/2_min.jpg', 'SEGUIDORS', false, 1, 1, null),
    ('50000000-0000-0000-0000-000000000003', '08298', '00000000-0000-0000-0000-00000000000a/3.jpg', '00000000-0000-0000-0000-00000000000a/3_min.jpg', 'PUBLICA', false, 1, 1, null),
    ('50000000-0000-0000-0000-000000000004', '08298', '00000000-0000-0000-0000-00000000000a/4.jpg', '00000000-0000-0000-0000-00000000000a/4_min.jpg', 'PUBLICA', false, 1, 1, 2);

select throws_ok(
    $$ insert into descobreix.fotos (id, codi_ine, ruta, ruta_miniatura, es_portada, creat_el, modificat_el)
       values ('50000000-0000-0000-0000-000000000005', '08298', '00000000-0000-0000-0000-00000000000b/5.jpg', '00000000-0000-0000-0000-00000000000b/5_min.jpg', false, 1, 1) $$,
    '23514', null,
    'La ruta d''una foto ha de ser a la carpeta del seu propietari'
);

-- Fitxers de les fotos a Storage.
select lives_ok(
    $$ insert into storage.objects (bucket_id, name) values
       ('descobreix-fotos', '00000000-0000-0000-0000-00000000000a/1.jpg'),
       ('descobreix-fotos', '00000000-0000-0000-0000-00000000000a/2.jpg'),
       ('descobreix-fotos', '00000000-0000-0000-0000-00000000000a/3.jpg') $$,
    'Un usuari pot pujar fitxers a la seva carpeta'
);
select throws_ok(
    $$ insert into storage.objects (bucket_id, name) values ('descobreix-fotos', '00000000-0000-0000-0000-00000000000b/x.jpg') $$,
    '42501', null,
    'Un usuari no pot pujar fitxers a la carpeta d''un altre'
);

-- Comptes ------------------------------------------------------------------------------------------

select lives_ok(
    $$ update descobreix.perfils set public = false where id = '00000000-0000-0000-0000-00000000000a' $$,
    'Un usuari tria si el seu compte és públic o privat'
);
select throws_ok(
    $$ update descobreix.perfils set tipus = 'ESPECTADOR' where id = '00000000-0000-0000-0000-00000000000a' $$,
    'P0001', null,
    'Explorador o Espectador no es pot canviar'
);

-- Abans de seguir ---------------------------------------------------------------------------------

select pg_temp.com_a('00000000-0000-0000-0000-00000000000b');

select is((select count(*) from descobreix.municipis_descoberts), 0::bigint, 'Sense seguir-lo, no es veu el mapa de l''altre');
select is((select count(*) from descobreix.missions_completades), 0::bigint, 'No es veuen les missions completades d''un altre');
select is((select count(*) from descobreix.moviments_punts), 0::bigint, 'No es veuen els moviments de punts d''un altre');
select is((select count(*) from descobreix.missions_propies), 0::bigint, 'No es veuen les missions pròpies d''un altre');
select is((select count(*) from descobreix.fotos), 0::bigint, 'D''un compte privat que no segueixes no es veu cap foto, ni les públiques');

-- Seguir un compte privat -------------------------------------------------------------------------

select lives_ok(
    $$ insert into descobreix.seguiments (seguit_id, estat) values ('00000000-0000-0000-0000-00000000000a', 'ACCEPTAT') $$,
    'Es pot demanar de seguir algú'
);
select is((select estat from descobreix.seguiments), 'PENDENT', 'Seguir un compte privat queda pendent, digui el que digui qui ho demana');
update descobreix.seguiments set estat = 'ACCEPTAT';
select is((select estat from descobreix.seguiments), 'PENDENT', 'Qui demana de seguir no ho pot acceptar');
select throws_ok(
    $$ insert into descobreix.seguiments (seguit_id) values ('00000000-0000-0000-0000-00000000000a') $$,
    '23505', null,
    'No es pot demanar dues vegades'
);
select is((select count(*) from descobreix.municipis_descoberts), 0::bigint, 'Mentre és pendent, no es veu res');

select pg_temp.com_a('00000000-0000-0000-0000-00000000000a');
select lives_ok($$ update descobreix.seguiments set estat = 'ACCEPTAT' $$, 'Qui rep la sol·licitud l''accepta');
select throws_ok(
    $$ update descobreix.seguiments set estat = 'PENDENT' $$,
    'P0001', null,
    'Un seguiment acceptat no pot tornar a pendent'
);

select pg_temp.com_a('00000000-0000-0000-0000-00000000000b');
select is((select count(*) from descobreix.municipis_descoberts), 1::bigint, 'Un seguidor veu el mapa de l''altre');
select is((select count(*) from descobreix.missions_propies), 0::bigint, 'Un seguidor no veu les missions pròpies de l''altre');
select results_eq(
    $$ select id from descobreix.fotos order by id $$,
    $$ values ('50000000-0000-0000-0000-000000000002'::uuid), ('50000000-0000-0000-0000-000000000003'::uuid) $$,
    'Un seguidor veu les fotos per a seguidors i les públiques, però no les privades'
);
select results_eq(
    $$ select name from storage.objects where bucket_id = 'descobreix-fotos' order by name $$,
    $$ values ('00000000-0000-0000-0000-00000000000a/2.jpg'), ('00000000-0000-0000-0000-00000000000a/3.jpg') $$,
    'A Storage, un seguidor només pot llegir els fitxers de les fotos que pot veure'
);
select is(
    (select count(*) from descobreix.mur() where tipus = 'FOTO'),
    2::bigint,
    'Al mur hi ha les fotos que pot veure de la gent que segueix'
);
select is(
    (select count(*) from descobreix.mur() where tipus = 'MUNICIPI'),
    0::bigint,
    'El municipi d''inici no surt al mur'
);

-- Seguir un compte públic -------------------------------------------------------------------------

select pg_temp.com_a('00000000-0000-0000-0000-00000000000c');
update descobreix.perfils set public = true where id = '00000000-0000-0000-0000-00000000000c';

select pg_temp.com_a('00000000-0000-0000-0000-00000000000b');
insert into descobreix.seguiments (seguit_id) values ('00000000-0000-0000-0000-00000000000c');
select is(
    (select estat from descobreix.seguiments where seguit_id = '00000000-0000-0000-0000-00000000000c'),
    'ACCEPTAT',
    'Un compte públic es pot seguir directament'
);

select pg_temp.com_a('00000000-0000-0000-0000-00000000000c');
select is(
    (select count(*) from descobreix.seguiments where seguidor_id = '00000000-0000-0000-0000-00000000000b'),
    1::bigint,
    'D''un compte privat, a qui segueix només ho veuen els seus seguidors i la persona seguida'
);
select is((select count(*) from descobreix.fotos), 0::bigint, 'Qui no segueix un compte privat no en veu les fotos');
select is(
    (select seguidors from descobreix.perfil_de('00000000-0000-0000-0000-00000000000a')),
    1::bigint,
    'Del perfil de qualsevol es veu quanta gent el segueix'
);

-- Esborrar les dades ----------------------------------------------------------------------------

select pg_temp.com_a('00000000-0000-0000-0000-00000000000a');
select descobreix.esborra_dades();

reset role;
select is(
    (select count(*) from descobreix.municipis_descoberts where usuari_id = '00000000-0000-0000-0000-00000000000a')
  + (select count(*) from descobreix.fotos where usuari_id = '00000000-0000-0000-0000-00000000000a')
  + (select count(*) from descobreix.seguiments where '00000000-0000-0000-0000-00000000000a' in (seguidor_id, seguit_id)),
    0::bigint,
    'Esborrar les dades esborra el progrés, les fotos i els seguiments de l''usuari'
);
select is(
    (select count(*) from auth.users where id = '00000000-0000-0000-0000-00000000000a'),
    1::bigint,
    'Esborrar les dades no esborra el compte, que és compartit amb altres apps'
);

-- Sense sessió ----------------------------------------------------------------------------------

set local role anon;
select throws_ok(
    $$ select * from descobreix.perfils $$,
    '42501', null,
    'Sense sessió no es pot llegir res'
);

select * from finish();
rollback;

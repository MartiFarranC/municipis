-- Tests de la foto de perfil i del rànquing (requisits.md, seccions 9.1 i 9.3).
-- S'executen amb `npx supabase test db`.

begin;
create extension if not exists pgtap with schema extensions;

select plan(23);

-- Missions i configuració de prova, en lloc de les reals.
delete from descobreix.missions_oficials;
insert into descobreix.configuracio (id, radi_missio_metres, precisio_maxima_metres, bonus_totes_les_missions)
values (1, 75, 50, 50)
on conflict (id) do update set radi_missio_metres = 75, precisio_maxima_metres = 50, bonus_totes_les_missions = 50;
insert into descobreix.missions_oficials (id, codi_ine, prova, lat, lon, punts) values
    ('gen-08001-checkin', '08001', 'GPS', null, null, 100),
    ('gen-08001-cartell', '08001', 'FOTO', null, null, 50),
    ('wd-08001-castell', '08001', 'GPS', 41.5, 2.1, 20),
    ('gen-08002-checkin', '08002', 'GPS', null, null, 100),
    ('gen-08003-checkin', '08003', 'GPS', null, null, 100);

-- L'Anna i en Biel són amics; la Clara i en Dani no són amics de ningú.
insert into auth.users (id, email) values
    ('00000000-0000-0000-0000-00000000000a', 'anna@example.com'),
    ('00000000-0000-0000-0000-00000000000b', 'biel@example.com'),
    ('00000000-0000-0000-0000-00000000000c', 'clara@example.com'),
    ('00000000-0000-0000-0000-00000000000d', 'dani@example.com');
insert into descobreix.perfils (id, nom_usuari, creat_el) values
    ('00000000-0000-0000-0000-00000000000a', 'anna', '2026-01-01'),
    ('00000000-0000-0000-0000-00000000000b', 'biel', '2026-01-02'),
    ('00000000-0000-0000-0000-00000000000c', 'clara', '2026-01-03'),
    ('00000000-0000-0000-0000-00000000000d', 'dani', '2026-01-04');
insert into descobreix.amistats (sollicitant_id, destinatari_id, estat) values
    ('00000000-0000-0000-0000-00000000000a', '00000000-0000-0000-0000-00000000000b', 'ACCEPTADA');

create function pg_temp.com_a(usuari uuid) returns void language sql as $$
    select set_config('request.jwt.claims', json_build_object('sub', usuari, 'role', 'authenticated')::text, true);
$$;
grant execute on function pg_temp.com_a(uuid) to authenticated;

set local role authenticated;

-- Taules oficials ------------------------------------------------------------------------------

select pg_temp.com_a('00000000-0000-0000-0000-00000000000a');
select throws_ok($$ select * from descobreix.missions_oficials $$, '42501', null, 'El client no llegeix les missions oficials');
select throws_ok($$ select * from descobreix.puntuacions() $$, '42501', null, 'El client no crida les puntuacions directament');

-- Progrés de l'Anna: tot el municipi 08001, amb punts inflats, i coses que no han de comptar.
insert into descobreix.municipis_descoberts (id, codi_ine, es_inici, cost, creat_el, modificat_el) values
    (gen_random_uuid(), '08001', true, 0, 1, 1);
insert into descobreix.missions_completades (id, missio_id, codi_ine, punts, bonus, lat, lon, precisio, creat_el, modificat_el) values
    (gen_random_uuid(), 'gen-08001-checkin', '08001', 9999, 0, 41.3, 2.0, 10, 1, 1),
    (gen_random_uuid(), 'gen-08001-cartell', '08001', 50, 0, null, null, null, 1, 1),
    -- A 30 m del castell amb bona precisió: compta.
    (gen_random_uuid(), 'wd-08001-castell', '08001', 20, 50, 41.50027, 2.1, 10, 1, 1),
    -- Missió inventada: no compta.
    (gen_random_uuid(), 'inventada', '08001', 500, 0, null, null, null, 1, 1),
    -- Municipi no descobert: no compta.
    (gen_random_uuid(), 'gen-08002-checkin', '08002', 100, 0, null, null, null, 1, 1);

select is(
    (select punts from descobreix.ranquing('PUNTS', false) where soc_jo),
    (100 + 50 + 20 + 50)::bigint,
    'Els punts surten de la taula oficial, amb el bonus del municipi complet, i no dels que envia el client'
);
select is((select municipis from descobreix.ranquing('MUNICIPIS', false) where soc_jo), 1::bigint, 'Compta els municipis descoberts');

-- En Biel: el castell massa lluny, i el check-in amb mala precisió però sense coordenades de lloc.
select pg_temp.com_a('00000000-0000-0000-0000-00000000000b');
insert into descobreix.municipis_descoberts (id, codi_ine, es_inici, cost, creat_el, modificat_el) values
    (gen_random_uuid(), '08001', true, 0, 1, 1),
    (gen_random_uuid(), '08002', false, 60, 1, 1),
    (gen_random_uuid(), '08003', false, 65, 1, 1),
    -- Codi que no és cap municipi de l'app: no compta.
    (gen_random_uuid(), '99999', false, 0, 1, 1);
insert into descobreix.missions_completades (id, missio_id, codi_ine, punts, bonus, lat, lon, precisio, creat_el, modificat_el) values
    (gen_random_uuid(), 'wd-08001-castell', '08001', 20, 0, 41.502, 2.1, 10, 1, 1),
    (gen_random_uuid(), 'gen-08002-checkin', '08002', 100, 0, null, null, null, 1, 1);

select is((select punts from descobreix.ranquing('PUNTS', false) where soc_jo), (100 + 50)::bigint,
    'Una missió de GPS fora del radi no compta, i completar un municipi de només una missió dona el bonus');
select is((select municipis from descobreix.ranquing('MUNICIPIS', false) where soc_jo), 3::bigint,
    'Els codis que no són de cap municipi no compten');

-- Una missió de lloc amb mala precisió no compta.
select pg_temp.com_a('00000000-0000-0000-0000-00000000000c');
insert into descobreix.municipis_descoberts (id, codi_ine, es_inici, cost, creat_el, modificat_el) values
    (gen_random_uuid(), '08001', true, 0, 1, 1);
insert into descobreix.missions_completades (id, missio_id, codi_ine, punts, bonus, lat, lon, precisio, creat_el, modificat_el) values
    (gen_random_uuid(), 'wd-08001-castell', '08001', 20, 0, 41.5, 2.1, 80, 1, 1),
    (gen_random_uuid(), 'gen-08001-checkin', '08001', 100, 0, null, null, null, 1, 1);
select is((select punts from descobreix.ranquing('PUNTS', false) where soc_jo), 100::bigint,
    'Una missió de lloc amb una precisió pitjor que el màxim no compta');

-- Classificacions ----------------------------------------------------------------------------

-- Punts: Anna 220, Biel 150, Clara 100, Dani 0. Municipis: Biel 3, Anna 1, Clara 1, Dani 0.
select pg_temp.com_a('00000000-0000-0000-0000-00000000000d');
select results_eq(
    $$ select posicio, nom_usuari, punts, soc_jo from descobreix.ranquing('PUNTS', false) $$,
    $$ values (1::bigint, 'anna', 220::bigint, false), (2, 'biel', 150, false), (3, 'clara', 100, false), (4, 'dani', 0, true) $$,
    'Classificació general per punts; els usuaris sense progrés hi surten amb 0'
);
select results_eq(
    $$ select posicio, nom_usuari, municipis from descobreix.ranquing('MUNICIPIS', false) $$,
    $$ values (1::bigint, 'biel', 3::bigint), (2, 'anna', 1), (2, 'clara', 1), (4, 'dani', 0) $$,
    'Classificació per municipis: els empats comparteixen posició'
);
select results_eq(
    $$ select posicio, nom_usuari from descobreix.ranquing('PUNTS', false, 2) $$,
    $$ values (1::bigint, 'anna'), (2, 'biel'), (4, 'dani') $$,
    'Amb un límit, la fila de l''usuari hi és igualment'
);
select results_eq(
    $$ select nom_usuari from descobreix.ranquing('PUNTS', true) $$,
    $$ values ('dani') $$,
    'Sense amics, la classificació d''amics només té l''usuari'
);

select pg_temp.com_a('00000000-0000-0000-0000-00000000000a');
select results_eq(
    $$ select posicio, nom_usuari, soc_jo from descobreix.ranquing('PUNTS', true) $$,
    $$ values (1::bigint, 'anna', true), (2, 'biel', false) $$,
    'La classificació d''amics només té l''usuari i els seus amics'
);
select throws_ok($$ select * from descobreix.ranquing('RES', false) $$, '22023', null, 'Un criteri desconegut és un error');
select is(
    (select count(*) from descobreix.missions_completades),
    5::bigint,
    'El rànquing no obre les missions completades dels altres'
);

reset role;
set local role anon;
select throws_ok($$ select * from descobreix.ranquing('PUNTS', false) $$, '42501', null, 'Sense sessió no es pot veure el rànquing');
reset role;
set local role authenticated;

-- Foto de perfil -------------------------------------------------------------------------------

select pg_temp.com_a('00000000-0000-0000-0000-00000000000a');
select lives_ok(
    $$ insert into storage.objects (bucket_id, name) values ('descobreix-avatars', '00000000-0000-0000-0000-00000000000a/1.jpg') $$,
    'Un usuari pot pujar la seva foto de perfil'
);
select throws_ok(
    $$ insert into storage.objects (bucket_id, name) values ('descobreix-avatars', '00000000-0000-0000-0000-00000000000b/1.jpg') $$,
    '42501', null,
    'Un usuari no pot pujar fotos de perfil a la carpeta d''un altre'
);
select lives_ok(
    $$ update descobreix.perfils set foto = '00000000-0000-0000-0000-00000000000a/1.jpg' where id = '00000000-0000-0000-0000-00000000000a' $$,
    'Un usuari pot posar-se la foto de perfil'
);
select throws_ok(
    $$ update descobreix.perfils set foto = '00000000-0000-0000-0000-00000000000b/1.jpg' where id = '00000000-0000-0000-0000-00000000000a' $$,
    '23514', null,
    'La foto de perfil ha de ser de la carpeta de l''usuari'
);
select is(
    (select count(*) from descobreix.perfils where id = '00000000-0000-0000-0000-00000000000b' and foto is not null),
    0::bigint,
    'Ningú no canvia la foto d''un altre'
);
update descobreix.perfils set foto = 'x' where id = '00000000-0000-0000-0000-00000000000b';
select is(
    (select foto from descobreix.perfils where id = '00000000-0000-0000-0000-00000000000b'),
    null,
    'La RLS no deixa modificar el perfil d''un altre'
);

select pg_temp.com_a('00000000-0000-0000-0000-00000000000c');
select results_eq(
    $$ select name from storage.objects where bucket_id = 'descobreix-avatars' $$,
    $$ values ('00000000-0000-0000-0000-00000000000a/1.jpg') $$,
    'Qualsevol usuari amb sessió veu les fotos de perfil'
);
select is(
    (select foto from descobreix.ranquing('PUNTS', false) where nom_usuari = 'anna'),
    '00000000-0000-0000-0000-00000000000a/1.jpg',
    'El rànquing porta la foto de perfil'
);

select * from finish();
rollback;

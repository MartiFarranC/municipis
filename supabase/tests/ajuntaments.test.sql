-- Tests del contingut dels ajuntaments i de les xifres anònimes.
begin;
create extension if not exists pgtap with schema extensions;

select plan(13);

-- Dades per a les xifres: 12 persones han desbloquejat Vic, i 3 Gurb.
insert into auth.users (id, email)
select ('00000000-0000-0000-0000-0000000000' || lpad(i::text, 2, '0'))::uuid, 'u' || i || '@example.com'
from generate_series(1, 12) i;
insert into descobreix.perfils (id, nom_usuari)
select ('00000000-0000-0000-0000-0000000000' || lpad(i::text, 2, '0'))::uuid, 'usuari' || i from generate_series(1, 12) i;
insert into descobreix.municipis_descoberts (id, usuari_id, codi_ine, es_inici, cost, creat_el, modificat_el)
select gen_random_uuid(), ('00000000-0000-0000-0000-0000000000' || lpad(i::text, 2, '0'))::uuid, '08298', false, 60, 1, 1
from generate_series(1, 12) i;
insert into descobreix.municipis_descoberts (id, usuari_id, codi_ine, es_inici, cost, creat_el, modificat_el)
select gen_random_uuid(), ('00000000-0000-0000-0000-0000000000' || lpad(i::text, 2, '0'))::uuid, '08100', false, 60, 1, 1
from generate_series(1, 3) i;
insert into descobreix.missions_completades (id, usuari_id, missio_id, codi_ine, punts, bonus, creat_el, modificat_el)
select gen_random_uuid(), ('00000000-0000-0000-0000-0000000000' || lpad(i::text, 2, '0'))::uuid, 'checkin-08298', '08298', 100, 0,
       (extract(epoch from now()) * 1000)::bigint, 1
from generate_series(1, 12) i;

insert into auth.users (id, email) values ('00000000-0000-0000-0000-00000000000a', 'anna@example.com');

-- El Martí entra el contingut amb el rol de servei.
set local role service_role;
insert into descobreix.ajuntaments (codi_ine, presentacio, web, te_segell) values
    ('08298', 'Ciutat de mercat.', 'https://www.vic.cat', true);

select lives_ok(
    $$ insert into descobreix.missions_ajuntament (codi_ine, titol, prova, lat, lon)
       values ('08298', 'Puja al campanar', 'GPS', 41.93, 2.25) $$,
    'Una missió de GPS té un punt'
);
select throws_ok(
    $$ insert into descobreix.missions_ajuntament (codi_ine, titol, prova) values ('08298', 'Sense punt', 'GPS') $$,
    '23514', null,
    'Una missió de GPS sense punt no es pot entrar'
);
select lives_ok(
    $$ insert into descobreix.missions_ajuntament (codi_ine, titol, prova, resum_qr)
       values ('08298', 'Segella a l''oficina de turisme', 'QR', encode(sha256('prova'), 'hex')) $$,
    'Una missió de QR en guarda el resum'
);
select throws_ok(
    $$ insert into descobreix.missions_ajuntament (codi_ine, titol, prova, resum_qr) values ('08298', 'QR', 'QR', 'prova') $$,
    '23514', null,
    'Del QR només es guarda el resum SHA-256, no el codi'
);
select throws_ok(
    $$ insert into descobreix.missions_ajuntament (codi_ine, titol, prova, data_inici, data_fi)
       values ('08298', 'Fira', 'FOTO', '2026-04-06', '2026-04-05') $$,
    '23514', null,
    'Una festa acaba després de començar'
);
select throws_ok(
    $$ insert into descobreix.missions_ajuntament (codi_ine, titol, prova) values ('08019', 'Sense ajuntament', 'FOTO') $$,
    '23503', null,
    'Una missió és d''un ajuntament que col·labora'
);

select is(
    (select persones from descobreix.xifres_ajuntament('08298') where mes is null), 12::bigint,
    'Les xifres diuen quanta gent ha desbloquejat el municipi'
);
select is(
    (select missions from descobreix.xifres_ajuntament('08298') where mes is not null), 12::bigint,
    'I quantes missions s''hi han fet aquest mes'
);
select is(
    (select persones from descobreix.xifres_ajuntament('08100') where mes is null), null::bigint,
    'Amb menys de 10 persones no es dona cap xifra'
);

-- Els usuaris només llegeixen.
create function pg_temp.com_a(usuari uuid) returns void language sql as $$
    select set_config('request.jwt.claims', json_build_object('sub', usuari, 'role', 'authenticated')::text, true);
$$;
grant execute on function pg_temp.com_a(uuid) to authenticated;
set local role authenticated;
select pg_temp.com_a('00000000-0000-0000-0000-00000000000a');

select is((select count(*) from descobreix.missions_ajuntament), 2::bigint, 'Qualsevol usuari amb sessió veu les missions dels ajuntaments');
select throws_ok(
    $$ insert into descobreix.ajuntaments (codi_ine) values ('08019') $$,
    '42501', null,
    'Un usuari no pot entrar contingut d''un ajuntament'
);
select throws_ok(
    $$ update descobreix.ajuntaments set web = 'https://example.com' $$,
    '42501', null,
    'Ni canviar-lo'
);
select throws_ok(
    $$ select * from descobreix.xifres_ajuntament('08298') $$,
    '42501', null,
    'Ni veure les xifres'
);

select * from finish();
rollback;

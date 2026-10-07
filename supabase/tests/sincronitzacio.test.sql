-- Tests de les taules de la sincronització: segells, sacs i fotos del cartell.
begin;
create extension if not exists pgtap with schema extensions;

select plan(9);

insert into auth.users (id, email) values
    ('00000000-0000-0000-0000-00000000000a', 'anna@example.com'),
    ('00000000-0000-0000-0000-00000000000b', 'biel@example.com');

create function pg_temp.com_a(usuari uuid) returns void language sql as $$
    select set_config('request.jwt.claims', json_build_object('sub', usuari, 'role', 'authenticated')::text, true);
$$;
grant execute on function pg_temp.com_a(uuid) to authenticated;

set local role authenticated;
select pg_temp.com_a('00000000-0000-0000-0000-00000000000b');
insert into descobreix.perfils (id, nom_usuari) values ('00000000-0000-0000-0000-00000000000b', 'biel');
select pg_temp.com_a('00000000-0000-0000-0000-00000000000a');
insert into descobreix.perfils (id, nom_usuari) values ('00000000-0000-0000-0000-00000000000a', 'anna');

-- Segells ----------------------------------------------------------------------------------------

select lives_ok(
    $$ insert into descobreix.segells (id, codi_ine, comarca, pagina, x, y, gir, tinta, creat_el, modificat_el)
       values ('60000000-0000-0000-0000-000000000001', '08298', '24', 0, .5, .5, 3, 1, 1, 1) $$,
    'Un usuari pot desar un segell'
);
select throws_ok(
    $$ insert into descobreix.segells (id, codi_ine, comarca, pagina, x, y, gir, tinta, creat_el, modificat_el)
       values ('60000000-0000-0000-0000-000000000002', '08298', '24', 0, .2, .2, 0, 0, 2, 2) $$,
    '23505', null,
    'Un municipi només té un segell'
);
select throws_ok(
    $$ insert into descobreix.segells (id, codi_ine, comarca, pagina, x, y, gir, tinta, creat_el, modificat_el)
       values ('60000000-0000-0000-0000-000000000003', '08019', '13', 0, 1.5, .5, 0, 0, 2, 2) $$,
    '23514', null,
    'El segell és dins de la pàgina'
);

-- Sacs -------------------------------------------------------------------------------------------

select lives_ok(
    $$ insert into descobreix.sacs (id, origen, creat_el, modificat_el)
       values ('70000000-0000-0000-0000-000000000001', 'primera_foto', 1, 1) $$,
    'Un usuari pot desar un sac guanyat'
);
select lives_ok(
    $$ update descobreix.sacs set objecte_tipus = 'EMOJI', objecte_id = 'cares--content', obert_el = 2, modificat_el = 2
       where id = '70000000-0000-0000-0000-000000000001' $$,
    'I obrir-lo'
);
select throws_ok(
    $$ insert into descobreix.sacs (id, origen, objecte_tipus, creat_el, modificat_el)
       values ('70000000-0000-0000-0000-000000000002', 'primera_missio', 'COLOR', 1, 1) $$,
    '23514', null,
    'Si en surt una cosa, se''n sap el tipus i l''id'
);

-- Fotos del cartell ------------------------------------------------------------------------------

select lives_ok(
    $$ insert into descobreix.fotos (id, codi_ine, ruta, ruta_miniatura, es_portada, es_cromo, creat_el, modificat_el)
       values ('50000000-0000-0000-0000-000000000001', '08298', '00000000-0000-0000-0000-00000000000a/c.jpg',
               '00000000-0000-0000-0000-00000000000a/m/c.jpg', true, true, 1, 1) $$,
    'Una foto pot ser la del cartell (el cromo del catàleg)'
);

-- Un altre usuari no veu res -----------------------------------------------------------------------

select pg_temp.com_a('00000000-0000-0000-0000-00000000000b');
select is((select count(*) from descobreix.segells), 0::bigint, 'Els segells només els veu el propietari');
select is((select count(*) from descobreix.sacs), 0::bigint, 'Els sacs només els veu el propietari');

select * from finish();
rollback;

-- Tests de les reaccions, els bloquejos i les denúncies.
begin;
create extension if not exists pgtap with schema extensions;

select plan(15);

insert into auth.users (id, email) values
    ('00000000-0000-0000-0000-00000000000a', 'anna@example.com'),
    ('00000000-0000-0000-0000-00000000000b', 'biel@example.com'),
    ('00000000-0000-0000-0000-00000000000c', 'clara@example.com');

create function pg_temp.com_a(usuari uuid) returns void language sql as $$
    select set_config('request.jwt.claims', json_build_object('sub', usuari, 'role', 'authenticated')::text, true);
$$;
grant execute on function pg_temp.com_a(uuid) to authenticated;

set local role authenticated;
-- L'Anna té el compte públic; en Biel la segueix; la Clara no segueix ningú.
select pg_temp.com_a('00000000-0000-0000-0000-00000000000a');
insert into descobreix.perfils (id, nom_usuari, public) values ('00000000-0000-0000-0000-00000000000a', 'anna', true);
select pg_temp.com_a('00000000-0000-0000-0000-00000000000c');
insert into descobreix.perfils (id, nom_usuari, public) values ('00000000-0000-0000-0000-00000000000c', 'clara', true);
select pg_temp.com_a('00000000-0000-0000-0000-00000000000b');
insert into descobreix.perfils (id, nom_usuari, public) values ('00000000-0000-0000-0000-00000000000b', 'biel', true);
insert into descobreix.seguiments (seguit_id) values ('00000000-0000-0000-0000-00000000000a');

-- Reaccions --------------------------------------------------------------------------------------

select lives_ok(
    $$ insert into descobreix.reaccions (destinatari_id, objectiu_tipus, objectiu_id, emoji)
       values ('00000000-0000-0000-0000-00000000000a', 'MUNICIPI', '08298', 'anims--castell') $$,
    'Es pot animar amb un emoji algú que segueixes'
);
select lives_ok(
    $$ insert into descobreix.reaccions (destinatari_id, objectiu_tipus, objectiu_id, missatge)
       values ('00000000-0000-0000-0000-00000000000a', 'MUNICIPI', '08298', 'Som-hi, que ja quasi tens Osona!') $$,
    'O amb un missatge curt'
);
select throws_ok(
    $$ insert into descobreix.reaccions (destinatari_id, objectiu_tipus, objectiu_id, missatge)
       values ('00000000-0000-0000-0000-00000000000a', 'MUNICIPI', '08298', repeat('a', 81)) $$,
    '23514', null,
    'Un missatge té com a molt 80 caràcters'
);
select throws_ok(
    $$ insert into descobreix.reaccions (destinatari_id, objectiu_tipus, objectiu_id, emoji, missatge)
       values ('00000000-0000-0000-0000-00000000000a', 'FOTO', 'x', 'anims--castell', 'hola') $$,
    '23514', null,
    'Una reacció és un emoji o un missatge, no tots dos'
);
select throws_ok(
    $$ insert into descobreix.reaccions (destinatari_id, objectiu_tipus, objectiu_id, emoji)
       values ('00000000-0000-0000-0000-00000000000c', 'MUNICIPI', '08019', 'anims--castell') $$,
    '42501', null,
    'No es pot animar algú que no segueixes'
);

select pg_temp.com_a('00000000-0000-0000-0000-00000000000a');
select is((select count(*) from descobreix.reaccions), 2::bigint, 'Qui rep les reaccions les veu');
select pg_temp.com_a('00000000-0000-0000-0000-00000000000c');
select is((select count(*) from descobreix.reaccions), 0::bigint, 'Els altres no les veuen');

-- Denúncies --------------------------------------------------------------------------------------

select pg_temp.com_a('00000000-0000-0000-0000-00000000000a');
select lives_ok(
    $$ insert into descobreix.denuncies (denunciat_id, text_denunciat, motiu)
       values ('00000000-0000-0000-0000-00000000000b', 'Som-hi, que ja quasi tens Osona!', 'Prova') $$,
    'Es pot denunciar algú'
);
select throws_ok(
    $$ insert into descobreix.denuncies (denunciat_id, estat) values ('00000000-0000-0000-0000-00000000000b', 'REVISADA') $$,
    '42501', null,
    'Una denúncia no es pot crear ja revisada'
);
select pg_temp.com_a('00000000-0000-0000-0000-00000000000b');
select is((select count(*) from descobreix.denuncies), 0::bigint, 'El denunciat no veu les denúncies');

-- Bloquejos --------------------------------------------------------------------------------------

select pg_temp.com_a('00000000-0000-0000-0000-00000000000a');
select lives_ok(
    $$ insert into descobreix.bloquejos (bloquejat_id) values ('00000000-0000-0000-0000-00000000000b') $$,
    'Es pot bloquejar algú'
);
select pg_temp.com_a('00000000-0000-0000-0000-00000000000b');
select is((select count(*) from descobreix.seguiments), 0::bigint, 'En bloquejar-lo, deixa de seguir-te');
select is((select count(*) from descobreix.bloquejos), 0::bigint, 'El bloquejat no veu que l''han bloquejat');
select throws_ok(
    $$ insert into descobreix.seguiments (seguit_id) values ('00000000-0000-0000-0000-00000000000a') $$,
    '42501', null,
    'Un bloquejat no et pot tornar a seguir'
);
select throws_ok(
    $$ insert into descobreix.reaccions (destinatari_id, objectiu_tipus, objectiu_id, emoji)
       values ('00000000-0000-0000-0000-00000000000a', 'MUNICIPI', '08298', 'anims--castell') $$,
    '42501', null,
    'Ni animar-te'
);

select * from finish();
rollback;

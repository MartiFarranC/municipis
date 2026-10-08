-- Tests de la web per als col·laboradors: codis d'accés i propostes.
begin;
create extension if not exists pgtap with schema extensions;

select plan(14);

-- El Martí dona d'alta el Consell Comarcal d'Osona amb el codi «codi-osona» (només se'n guarda el resum).
insert into descobreix.collaboradors (tipus, codi, nom, resum_codi_acces)
values ('CONSELL_COMARCAL', '24', 'Consell Comarcal d''Osona', encode(sha256('codi-osona'), 'hex'));
insert into descobreix.collaboradors (tipus, codi, nom, resum_codi_acces, actiu)
values ('AJUNTAMENT', '08298', 'Ajuntament de prova', encode(sha256('codi-baixa'), 'hex'), false);

set local role anon;

select is(
    (descobreix.espai_collaborador('codi-osona'))->>'nom', 'Consell Comarcal d''Osona',
    'Amb el codi, la web sap qui és'
);
select is(
    (descobreix.espai_collaborador('  codi-osona '))->>'codi', '24',
    'Els espais del principi i del final no compten'
);
select throws_ok(
    $$ select descobreix.espai_collaborador('codi-dolent') $$,
    '42501', null,
    'Un codi que no existeix no entra'
);
select throws_ok(
    $$ select descobreix.espai_collaborador('codi-baixa') $$,
    '42501', null,
    'Ni el d''un col·laborador donat de baixa'
);

select isnt(
    descobreix.proposa_medalla('codi-osona', 'Una proposta de prova', null, null), null,
    'Es pot proposar què surt a la medalla'
);
select isnt(
    descobreix.proposa_missio('codi-osona', '08298', 'Una missió de prova', null, null, 41.93, 2.25, 'GPS', null, null), null,
    'I missions'
);
select throws_ok(
    $$ select descobreix.proposa_missio('codi-osona', '08298', 'Festa', null, null, null, null, 'FOTO', '2027-03-21', '2027-03-20') $$,
    '23514', null,
    'Les dates d''una festa han de tenir sentit'
);
select throws_ok(
    $$ select descobreix.proposa_medalla('codi-dolent', 'Res', null, null) $$,
    '42501', null,
    'Sense codi no es pot proposar res'
);

select lives_ok(
    $$ select descobreix.desa_contacte('codi-osona', 'Persona de prova', 'Turisme', 'prova@example.com', true) $$,
    'Es pot deixar un contacte'
);
select throws_ok(
    $$ select descobreix.desa_contacte('codi-osona', 'Persona de prova', null, 'prova@example.com', false) $$,
    '23514', null,
    'Però només amb el consentiment'
);

select is(
    jsonb_array_length((descobreix.espai_collaborador('codi-osona'))->'missions'), 1,
    'Cadascú veu les seves propostes'
);
select lives_ok(
    $$ select descobreix.retira_proposta('codi-osona', ((descobreix.espai_collaborador('codi-osona'))->'missions'->0->>'id')::uuid) $$,
    'I les pot retirar'
);
select is(
    jsonb_array_length((descobreix.espai_collaborador('codi-osona'))->'missions'), 0,
    'Retirada, ja no hi és'
);

select throws_ok(
    $$ select count(*) from descobreix.propostes_medalla $$,
    '42501', null,
    'La web no pot llegir les taules directament'
);

select * from finish();
rollback;

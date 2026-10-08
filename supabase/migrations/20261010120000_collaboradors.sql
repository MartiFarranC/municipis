-- Web per als col·laboradors (consells comarcals i ajuntaments): hi proposen la medalla de la comarca i missions
-- amb els llocs emblemàtics dels municipis (docs/web-collaboradors.md).
--   - Cada col·laborador entra amb un codi d'accés que li dona el Martí. Del codi només es guarda el resum SHA-256.
--   - La web fa servir la clau pública (anon) i no toca mai les taules: només les funcions d'aquí, que comproven el
--     codi. Les propostes només les llegeix el Martí, amb el rol de servei.
--   - Les dades de contacte només es guarden amb el consentiment de qui les dona.

grant usage on schema descobreix to anon;

create table descobreix.collaboradors (
    id uuid primary key default gen_random_uuid(),
    tipus text not null check (tipus in ('CONSELL_COMARCAL', 'AJUNTAMENT')),
    -- El codi de la comarca (dues xifres) o el codi INE del municipi.
    codi text not null,
    nom text not null check (char_length(btrim(nom)) between 1 and 120),
    resum_codi_acces text not null unique check (resum_codi_acces ~ '^[0-9a-f]{64}$'),
    actiu boolean not null default true,
    creat_el timestamptz not null default now(),
    check ((tipus = 'CONSELL_COMARCAL' and codi ~ '^[0-9]{2}$') or (tipus = 'AJUNTAMENT' and codi ~ '^[0-9]{5}$'))
);

create table descobreix.contactes_collaboradors (
    collaborador_id uuid primary key references descobreix.collaboradors (id) on delete cascade,
    nom text not null check (char_length(btrim(nom)) between 1 and 120),
    carrec text check (char_length(carrec) <= 120),
    correu text not null check (correu ~ '^[^@\s]+@[^@\s]+\.[^@\s]+$' and char_length(correu) <= 200),
    consentiment boolean not null check (consentiment),
    actualitzat_el timestamptz not null default now()
);

create table descobreix.propostes_medalla (
    id uuid primary key default gen_random_uuid(),
    collaborador_id uuid not null references descobreix.collaboradors (id) on delete cascade,
    que_hi_surt text not null check (char_length(btrim(que_hi_surt)) between 1 and 1000),
    per_que text check (char_length(per_que) <= 1000),
    referencia text check (char_length(referencia) <= 500),
    creat_el timestamptz not null default now()
);

create table descobreix.propostes_missio (
    id uuid primary key default gen_random_uuid(),
    collaborador_id uuid not null references descobreix.collaboradors (id) on delete cascade,
    codi_ine text not null check (codi_ine ~ '^[0-9]{5}$'),
    titol text not null check (char_length(btrim(titol)) between 1 and 120),
    descripcio text check (char_length(descripcio) <= 1000),
    -- On és: una adreça o una descripció. Les coordenades, si les saben.
    lloc text check (char_length(lloc) <= 300),
    lat double precision check (lat between 40 and 43.5),
    lon double precision check (lon between 0 and 3.5),
    prova text check (prova in ('GPS', 'FOTO', 'QR', 'ALTRE')),
    data_inici date,
    data_fi date,
    creat_el timestamptz not null default now(),
    check ((lat is null) = (lon is null)),
    check ((data_inici is null) = (data_fi is null)),
    check (data_fi >= data_inici)
);
create index propostes_medalla_collaborador on descobreix.propostes_medalla (collaborador_id);
create index propostes_missio_collaborador on descobreix.propostes_missio (collaborador_id);

-- Només el rol de servei hi té accés directe.
grant all on descobreix.collaboradors, descobreix.contactes_collaboradors, descobreix.propostes_medalla,
    descobreix.propostes_missio to service_role;
alter table descobreix.collaboradors enable row level security;
alter table descobreix.contactes_collaboradors enable row level security;
alter table descobreix.propostes_medalla enable row level security;
alter table descobreix.propostes_missio enable row level security;

-- El col·laborador d'un codi d'accés, si és actiu. Si no, error (el mateix error per a tots els casos).
create function descobreix.collaborador_del_codi(codi_acces text) returns uuid
language plpgsql
stable
security definer
set search_path = ''
as $$
declare
    trobat uuid;
begin
    select id into trobat from descobreix.collaboradors
    where actiu and resum_codi_acces = encode(pg_catalog.sha256(convert_to(btrim(coalesce(codi_acces, '')), 'UTF8')), 'hex');
    if trobat is null then
        raise exception 'Codi d''accés no vàlid' using errcode = '42501';
    end if;
    return trobat;
end;
$$;

-- Qui és i què ha proposat fins ara.
create function descobreix.espai_collaborador(codi_acces text) returns jsonb
language sql
stable
security definer
set search_path = ''
as $$
    select jsonb_build_object(
        'tipus', c.tipus,
        'codi', c.codi,
        'nom', c.nom,
        'contacte', (select jsonb_build_object('nom', k.nom, 'carrec', k.carrec, 'correu', k.correu)
                     from descobreix.contactes_collaboradors k where k.collaborador_id = c.id),
        'medalla', coalesce((select jsonb_agg(to_jsonb(p) - 'collaborador_id' order by p.creat_el)
                             from descobreix.propostes_medalla p where p.collaborador_id = c.id), '[]'::jsonb),
        'missions', coalesce((select jsonb_agg(to_jsonb(p) - 'collaborador_id' order by p.creat_el)
                              from descobreix.propostes_missio p where p.collaborador_id = c.id), '[]'::jsonb)
    )
    from descobreix.collaboradors c
    where c.id = descobreix.collaborador_del_codi(codi_acces);
$$;

create function descobreix.desa_contacte(codi_acces text, nom text, carrec text, correu text, consentiment boolean)
returns void
language sql
security definer
set search_path = ''
as $$
    insert into descobreix.contactes_collaboradors (collaborador_id, nom, carrec, correu, consentiment)
    values (descobreix.collaborador_del_codi(codi_acces), btrim(nom), nullif(btrim(carrec), ''), btrim(correu), consentiment)
    on conflict (collaborador_id) do update
    set nom = excluded.nom, carrec = excluded.carrec, correu = excluded.correu,
        consentiment = excluded.consentiment, actualitzat_el = now();
$$;

create function descobreix.proposa_medalla(codi_acces text, que_hi_surt text, per_que text, referencia text) returns uuid
language plpgsql
security definer
set search_path = ''
as $$
declare
    qui uuid := descobreix.collaborador_del_codi(codi_acces);
    nova uuid;
begin
    if (select count(*) from descobreix.propostes_medalla where collaborador_id = qui) >= 20 then
        raise exception 'Ja hi ha 20 propostes de medalla' using errcode = '54000';
    end if;
    insert into descobreix.propostes_medalla (collaborador_id, que_hi_surt, per_que, referencia)
    values (qui, btrim(que_hi_surt), nullif(btrim(per_que), ''), nullif(btrim(referencia), ''))
    returning id into nova;
    return nova;
end;
$$;

create function descobreix.proposa_missio(
    codi_acces text, codi_ine text, titol text, descripcio text, lloc text,
    lat double precision, lon double precision, prova text, data_inici date, data_fi date
) returns uuid
language plpgsql
security definer
set search_path = ''
as $$
declare
    qui uuid := descobreix.collaborador_del_codi(codi_acces);
    nova uuid;
begin
    if (select count(*) from descobreix.propostes_missio where collaborador_id = qui) >= 300 then
        raise exception 'Ja hi ha 300 propostes de missió' using errcode = '54000';
    end if;
    insert into descobreix.propostes_missio (collaborador_id, codi_ine, titol, descripcio, lloc, lat, lon, prova, data_inici, data_fi)
    values (qui, codi_ine, btrim(titol), nullif(btrim(descripcio), ''), nullif(btrim(lloc), ''), lat, lon, prova, data_inici, data_fi)
    returning id into nova;
    return nova;
end;
$$;

-- Treu una proposta pròpia (de medalla o de missió).
create function descobreix.retira_proposta(codi_acces text, proposta uuid) returns void
language plpgsql
security definer
set search_path = ''
as $$
declare
    qui uuid := descobreix.collaborador_del_codi(codi_acces);
begin
    delete from descobreix.propostes_medalla where id = proposta and collaborador_id = qui;
    delete from descobreix.propostes_missio where id = proposta and collaborador_id = qui;
end;
$$;

-- Ara anon té accés a l'esquema: cap funció no s'hi pot executar si no és de les d'aquí.
revoke execute on all functions in schema descobreix from public, anon;
grant execute on function descobreix.espai_collaborador(text) to anon, authenticated;
grant execute on function descobreix.desa_contacte(text, text, text, text, boolean) to anon, authenticated;
grant execute on function descobreix.proposa_medalla(text, text, text, text) to anon, authenticated;
grant execute on function descobreix.proposa_missio(text, text, text, text, text, double precision, double precision, text, date, date)
    to anon, authenticated;
grant execute on function descobreix.retira_proposta(text, uuid) to anon, authenticated;

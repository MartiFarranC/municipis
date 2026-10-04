-- Esquema de Descobreix Catalunya (requisits.md, secció 9).
--
-- El projecte de Supabase és compartit amb altres apps (secció 9.0): tot va a l'esquema
-- `descobreix` i al bucket `descobreix-fotos`. Aquesta migració no toca res més, a part de les
-- polítiques del bucket a `storage.objects`, que només s'apliquen a aquest bucket.
--
-- Les dates `creat_el`, `modificat_el` i `esborrat_el` són mil·lisegons des de l'època, com a Room.
-- `sincronitzat_el` la posa el servidor i és el cursor que fa servir l'app per baixar canvis.

create schema if not exists descobreix;

grant usage on schema descobreix to authenticated, service_role;

-- Utilitats -------------------------------------------------------------------------------------

create function descobreix.marca_sincronitzat() returns trigger
language plpgsql
set search_path = ''
as $$
begin
    new.sincronitzat_el := clock_timestamp();
    return new;
end;
$$;

-- Perfils ---------------------------------------------------------------------------------------

-- Un usuari "és de l'app" quan té un perfil. Els comptes (auth.users) són compartits.
create table descobreix.perfils (
    id uuid primary key references auth.users (id) on delete cascade,
    nom_usuari text not null unique check (nom_usuari ~ '^[a-z0-9_]{3,20}$'),
    creat_el timestamptz not null default now()
);

-- Amistats --------------------------------------------------------------------------------------

create table descobreix.amistats (
    id uuid primary key default gen_random_uuid(),
    sollicitant_id uuid not null references descobreix.perfils (id) on delete cascade,
    destinatari_id uuid not null references descobreix.perfils (id) on delete cascade,
    estat text not null default 'PENDENT' check (estat in ('PENDENT', 'ACCEPTADA')),
    creat_el timestamptz not null default now(),
    check (sollicitant_id <> destinatari_id)
);

-- Només una relació per parella, sigui qui sigui qui l'ha demanada.
create unique index amistats_parella on descobreix.amistats (
    least(sollicitant_id, destinatari_id),
    greatest(sollicitant_id, destinatari_id)
);
create index amistats_destinatari on descobreix.amistats (destinatari_id);

create function descobreix.son_amics(a uuid, b uuid) returns boolean
language sql
stable
security definer
set search_path = ''
as $$
    select exists (
        select 1
        from descobreix.amistats
        where estat = 'ACCEPTADA'
          and ((sollicitant_id = a and destinatari_id = b) or (sollicitant_id = b and destinatari_id = a))
    );
$$;

-- Una sol·licitud només pot passar de PENDENT a ACCEPTADA, i no se'n poden canviar els usuaris.
create function descobreix.protegeix_amistat() returns trigger
language plpgsql
set search_path = ''
as $$
begin
    if new.sollicitant_id <> old.sollicitant_id or new.destinatari_id <> old.destinatari_id then
        raise exception 'No es poden canviar els usuaris d''una amistat';
    end if;
    if not (old.estat = 'PENDENT' and new.estat = 'ACCEPTADA') then
        raise exception 'Només es pot acceptar una sol·licitud pendent';
    end if;
    return new;
end;
$$;

create trigger protegeix_amistat
before update on descobreix.amistats
for each row execute function descobreix.protegeix_amistat();

-- Progrés: només s'hi afegeixen files, mai es modifiquen (secció 9.2) ----------------------------

create table descobreix.municipis_descoberts (
    id uuid primary key,
    usuari_id uuid not null default auth.uid() references descobreix.perfils (id) on delete cascade,
    codi_ine text not null check (codi_ine ~ '^[0-9]{5}$'),
    es_inici boolean not null,
    cost integer not null check (cost >= 0),
    creat_el bigint not null,
    modificat_el bigint not null,
    sincronitzat_el timestamptz not null default clock_timestamp(),
    unique (usuari_id, codi_ine)
);
create index municipis_descoberts_sincronitzat on descobreix.municipis_descoberts (usuari_id, sincronitzat_el);

create table descobreix.missions_completades (
    id uuid primary key,
    usuari_id uuid not null default auth.uid() references descobreix.perfils (id) on delete cascade,
    missio_id text not null,
    codi_ine text not null check (codi_ine ~ '^[0-9]{5}$'),
    -- Punts que diu el client. El rànquing no se'n fia (secció 9.3).
    punts integer not null check (punts >= 0),
    bonus integer not null check (bonus >= 0),
    lat double precision,
    lon double precision,
    precisio double precision,
    foto_id uuid,
    creat_el bigint not null,
    modificat_el bigint not null,
    sincronitzat_el timestamptz not null default clock_timestamp(),
    unique (usuari_id, missio_id)
);
create index missions_completades_sincronitzat on descobreix.missions_completades (usuari_id, sincronitzat_el);

create table descobreix.moviments_punts (
    id uuid primary key,
    usuari_id uuid not null default auth.uid() references descobreix.perfils (id) on delete cascade,
    tipus text not null check (tipus in ('GUANY', 'DESPESA')),
    quantitat integer not null check (quantitat >= 0),
    motiu text not null check (motiu in ('MISSIO', 'BONUS', 'DESBLOQUEIG')),
    referencia text not null,
    creat_el bigint not null,
    modificat_el bigint not null,
    sincronitzat_el timestamptz not null default clock_timestamp()
);
create index moviments_punts_sincronitzat on descobreix.moviments_punts (usuari_id, sincronitzat_el);

-- Missions pròpies i fotos: guanya la modificació més recent i els esborrats són lògics ----------

create table descobreix.missions_propies (
    id uuid primary key,
    usuari_id uuid not null default auth.uid() references descobreix.perfils (id) on delete cascade,
    codi_ine text not null check (codi_ine ~ '^[0-9]{5}$'),
    titol text not null,
    descripcio text,
    completada boolean not null,
    creat_el bigint not null,
    modificat_el bigint not null,
    esborrat_el bigint,
    sincronitzat_el timestamptz not null default clock_timestamp()
);
create index missions_propies_sincronitzat on descobreix.missions_propies (usuari_id, sincronitzat_el);

create table descobreix.fotos (
    id uuid primary key,
    usuari_id uuid not null default auth.uid() references descobreix.perfils (id) on delete cascade,
    codi_ine text not null check (codi_ine ~ '^[0-9]{5}$'),
    -- Rutes dins del bucket descobreix-fotos: <usuari_id>/<fitxer>.
    ruta text not null,
    ruta_miniatura text not null,
    lat double precision,
    lon double precision,
    visibilitat text not null default 'PRIVADA' check (visibilitat in ('PRIVADA', 'AMICS', 'PUBLICA')),
    es_portada boolean not null,
    missio_id text,
    creat_el bigint not null,
    modificat_el bigint not null,
    esborrat_el bigint,
    sincronitzat_el timestamptz not null default clock_timestamp(),
    check (split_part(ruta, '/', 1) = usuari_id::text),
    check (split_part(ruta_miniatura, '/', 1) = usuari_id::text)
);
create index fotos_sincronitzat on descobreix.fotos (usuari_id, sincronitzat_el);
create index fotos_codi_ine on descobreix.fotos (codi_ine);

create trigger marca_sincronitzat before update on descobreix.missions_propies
for each row execute function descobreix.marca_sincronitzat();
create trigger marca_sincronitzat before update on descobreix.fotos
for each row execute function descobreix.marca_sincronitzat();

-- Permisos ---------------------------------------------------------------------------------------
-- Els permisos de taula limiten les operacions; la RLS limita les files.

grant select, insert, update, delete on descobreix.perfils to authenticated;
grant select, insert, update, delete on descobreix.amistats to authenticated;
grant select, insert on descobreix.municipis_descoberts to authenticated;
grant select, insert on descobreix.missions_completades to authenticated;
grant select, insert on descobreix.moviments_punts to authenticated;
grant select, insert, update on descobreix.missions_propies to authenticated;
grant select, insert, update on descobreix.fotos to authenticated;
grant all on all tables in schema descobreix to service_role;

revoke execute on all functions in schema descobreix from public, anon;
grant execute on function descobreix.son_amics(uuid, uuid) to authenticated;

-- RLS --------------------------------------------------------------------------------------------

alter table descobreix.perfils enable row level security;
alter table descobreix.amistats enable row level security;
alter table descobreix.municipis_descoberts enable row level security;
alter table descobreix.missions_completades enable row level security;
alter table descobreix.moviments_punts enable row level security;
alter table descobreix.missions_propies enable row level security;
alter table descobreix.fotos enable row level security;

-- Perfils: tothom amb sessió els pot veure (rànquing i cercar amics); cadascú gestiona el seu.
create policy perfils_llegeix on descobreix.perfils for select to authenticated using (true);
create policy perfils_crea on descobreix.perfils for insert to authenticated
    with check (id = (select auth.uid()));
create policy perfils_modifica on descobreix.perfils for update to authenticated
    using (id = (select auth.uid())) with check (id = (select auth.uid()));
create policy perfils_esborra on descobreix.perfils for delete to authenticated
    using (id = (select auth.uid()));

-- Amistats: les veuen els dos usuaris. Només el sol·licitant la crea, només el destinatari
-- l'accepta, i qualsevol dels dos la pot esborrar (rebutjar o eliminar l'amic).
create policy amistats_llegeix on descobreix.amistats for select to authenticated
    using ((select auth.uid()) in (sollicitant_id, destinatari_id));
create policy amistats_crea on descobreix.amistats for insert to authenticated
    with check (sollicitant_id = (select auth.uid()) and estat = 'PENDENT');
create policy amistats_accepta on descobreix.amistats for update to authenticated
    using (destinatari_id = (select auth.uid())) with check (destinatari_id = (select auth.uid()));
create policy amistats_esborra on descobreix.amistats for delete to authenticated
    using ((select auth.uid()) in (sollicitant_id, destinatari_id));

-- Municipis descoberts: el propietari i els seus amics (veure el mapa d'un amic, secció 9.4).
create policy municipis_descoberts_llegeix on descobreix.municipis_descoberts for select to authenticated
    using (usuari_id = (select auth.uid()) or descobreix.son_amics((select auth.uid()), usuari_id));
create policy municipis_descoberts_crea on descobreix.municipis_descoberts for insert to authenticated
    with check (usuari_id = (select auth.uid()));

-- Missions completades i moviments de punts: només el propietari.
create policy missions_completades_llegeix on descobreix.missions_completades for select to authenticated
    using (usuari_id = (select auth.uid()));
create policy missions_completades_crea on descobreix.missions_completades for insert to authenticated
    with check (usuari_id = (select auth.uid()));

create policy moviments_punts_llegeix on descobreix.moviments_punts for select to authenticated
    using (usuari_id = (select auth.uid()));
create policy moviments_punts_crea on descobreix.moviments_punts for insert to authenticated
    with check (usuari_id = (select auth.uid()));

-- Missions pròpies: només el propietari (secció 3.4).
create policy missions_propies_llegeix on descobreix.missions_propies for select to authenticated
    using (usuari_id = (select auth.uid()));
create policy missions_propies_crea on descobreix.missions_propies for insert to authenticated
    with check (usuari_id = (select auth.uid()));
create policy missions_propies_modifica on descobreix.missions_propies for update to authenticated
    using (usuari_id = (select auth.uid())) with check (usuari_id = (select auth.uid()));

-- Fotos: segons la visibilitat (secció 9.4). Les esborrades només les veu el propietari.
create policy fotos_llegeix on descobreix.fotos for select to authenticated
    using (
        usuari_id = (select auth.uid())
        or (esborrat_el is null and visibilitat = 'PUBLICA')
        or (esborrat_el is null and visibilitat = 'AMICS' and descobreix.son_amics((select auth.uid()), usuari_id))
    );
create policy fotos_crea on descobreix.fotos for insert to authenticated
    with check (usuari_id = (select auth.uid()));
create policy fotos_modifica on descobreix.fotos for update to authenticated
    using (usuari_id = (select auth.uid())) with check (usuari_id = (select auth.uid()));

-- Storage ----------------------------------------------------------------------------------------

insert into storage.buckets (id, name, public, file_size_limit, allowed_mime_types)
values ('descobreix-fotos', 'descobreix-fotos', false, 5242880, array['image/jpeg'])
on conflict (id) do nothing;

-- Cadascú escriu només a la seva carpeta (<usuari_id>/...). Es pot llegir un fitxer si és a la
-- pròpia carpeta o si és d'una foto que la RLS de descobreix.fotos deixa veure.
create policy descobreix_fotos_llegeix on storage.objects for select to authenticated
    using (
        bucket_id = 'descobreix-fotos'
        and (
            (storage.foldername(name))[1] = (select auth.uid())::text
            or exists (
                select 1 from descobreix.fotos f
                where f.ruta = storage.objects.name or f.ruta_miniatura = storage.objects.name
            )
        )
    );
create policy descobreix_fotos_crea on storage.objects for insert to authenticated
    with check (bucket_id = 'descobreix-fotos' and (storage.foldername(name))[1] = (select auth.uid())::text);
create policy descobreix_fotos_modifica on storage.objects for update to authenticated
    using (bucket_id = 'descobreix-fotos' and (storage.foldername(name))[1] = (select auth.uid())::text)
    with check (bucket_id = 'descobreix-fotos' and (storage.foldername(name))[1] = (select auth.uid())::text);
create policy descobreix_fotos_esborra on storage.objects for delete to authenticated
    using (bucket_id = 'descobreix-fotos' and (storage.foldername(name))[1] = (select auth.uid())::text);

-- Esborrar les dades -----------------------------------------------------------------------------

-- Esborra totes les dades d'aquesta app de l'usuari (secció 8). No toca auth.users, que és
-- compartit. Els fitxers de Storage els ha d'esborrar l'app abans, amb l'API de Storage.
create function descobreix.esborra_dades() returns void
language sql
security definer
set search_path = ''
as $$
    delete from descobreix.perfils where id = auth.uid();
$$;

revoke execute on function descobreix.esborra_dades() from public, anon;
grant execute on function descobreix.esborra_dades() to authenticated;

-- Foto de perfil i rànquing (requisits.md, seccions 9.1 i 9.3).
--
-- Com la primera migració, només toca l'esquema `descobreix` i els buckets `descobreix-*`.

-- Foto de perfil ---------------------------------------------------------------------------------

-- Ruta dins del bucket descobreix-avatars: <usuari_id>/<fitxer>. La veu qualsevol usuari amb sessió
-- (rànquing i amics).
alter table descobreix.perfils
    add column foto text check (foto is null or split_part(foto, '/', 1) = id::text);

insert into storage.buckets (id, name, public, file_size_limit, allowed_mime_types)
values ('descobreix-avatars', 'descobreix-avatars', false, 1048576, array['image/jpeg'])
on conflict (id) do nothing;

create policy descobreix_avatars_llegeix on storage.objects for select to authenticated
    using (bucket_id = 'descobreix-avatars');
create policy descobreix_avatars_crea on storage.objects for insert to authenticated
    with check (bucket_id = 'descobreix-avatars' and (storage.foldername(name))[1] = (select auth.uid())::text);
create policy descobreix_avatars_modifica on storage.objects for update to authenticated
    using (bucket_id = 'descobreix-avatars' and (storage.foldername(name))[1] = (select auth.uid())::text)
    with check (bucket_id = 'descobreix-avatars' and (storage.foldername(name))[1] = (select auth.uid())::text);
create policy descobreix_avatars_esborra on storage.objects for delete to authenticated
    using (bucket_id = 'descobreix-avatars' and (storage.foldername(name))[1] = (select auth.uid())::text);

-- Taules oficials --------------------------------------------------------------------------------
-- Les omple una migració generada per scripts/generar-sql-missions.js a partir dels mateixos
-- fitxers que van dins de l'app (missions.json i configuracio_joc.json). El client no hi té accés:
-- només les fan servir les funcions del rànquing.

create table descobreix.configuracio (
    id integer primary key default 1 check (id = 1),
    radi_missio_metres double precision not null,
    precisio_maxima_metres double precision not null,
    bonus_totes_les_missions integer not null
);

create table descobreix.missions_oficials (
    id text primary key,
    codi_ine text not null check (codi_ine ~ '^[0-9]{5}$'),
    prova text not null check (prova in ('GPS', 'FOTO')),
    lat double precision,
    lon double precision,
    punts integer not null check (punts >= 0)
);
create index missions_oficials_codi_ine on descobreix.missions_oficials (codi_ine);

alter table descobreix.configuracio enable row level security;
alter table descobreix.missions_oficials enable row level security;
grant all on descobreix.configuracio, descobreix.missions_oficials to service_role;

-- Puntuacions ------------------------------------------------------------------------------------

-- La mateixa fórmula que Geometria.distanciaMetres del mòdul joc.
create function descobreix.distancia_metres(lat1 double precision, lon1 double precision, lat2 double precision, lon2 double precision)
returns double precision
language sql
immutable
set search_path = ''
as $$
    select 2 * 6371000.0 * asin(sqrt(
        sin(radians(lat2 - lat1) / 2) ^ 2
        + cos(radians(lat1)) * cos(radians(lat2)) * sin(radians(lon2 - lon1) / 2) ^ 2
    ));
$$;

-- Punts i municipis de cada usuari, calculats al servidor (secció 9.3). No es fia dels punts que
-- envia el client: una missió només compta si
--   - és una missió oficial del mateix municipi,
--   - el municipi és entre els descoberts de l'usuari, i
--   - si és de GPS amb coordenades, la ubicació enviada és dins del radi amb prou precisió.
-- El bonus de completar un municipi es dona quan totes les seves missions oficials compten.
-- Limitació coneguda: el check-in i les fotos no es poden comprovar sense els límits municipals.
create function descobreix.puntuacions()
returns table (usuari_id uuid, punts bigint, municipis bigint)
language sql
stable
security definer
set search_path = ''
as $$
    with c as (
        select * from descobreix.configuracio where id = 1
    ),
    valides as (
        select mc.usuari_id, mc.codi_ine, mo.punts
        from descobreix.missions_completades mc
        join descobreix.missions_oficials mo on mo.id = mc.missio_id and mo.codi_ine = mc.codi_ine
        join descobreix.municipis_descoberts md on md.usuari_id = mc.usuari_id and md.codi_ine = mc.codi_ine
        cross join c
        where mo.prova <> 'GPS'
           or mo.lat is null
           or (
               mc.lat is not null and mc.lon is not null and mc.precisio is not null
               and mc.precisio <= c.precisio_maxima_metres
               and descobreix.distancia_metres(mc.lat, mc.lon, mo.lat, mo.lon) <= c.radi_missio_metres
           )
    ),
    per_municipi as (
        select v.usuari_id, v.codi_ine, sum(v.punts) as punts, count(*) as fetes
        from valides v
        group by v.usuari_id, v.codi_ine
    ),
    totals as (
        select codi_ine, count(*) as missions from descobreix.missions_oficials group by codi_ine
    ),
    punts as (
        select pm.usuari_id,
               sum(pm.punts + case when pm.fetes = t.missions then (select bonus_totes_les_missions from c) else 0 end) as punts
        from per_municipi pm
        join totals t on t.codi_ine = pm.codi_ine
        group by pm.usuari_id
    ),
    municipis as (
        select md.usuari_id, count(*) as municipis
        from descobreix.municipis_descoberts md
        where md.codi_ine in (select codi_ine from totals)
        group by md.usuari_id
    )
    select p.id, coalesce(pt.punts, 0)::bigint, coalesce(m.municipis, 0)::bigint
    from descobreix.perfils p
    left join punts pt on pt.usuari_id = p.id
    left join municipis m on m.usuari_id = p.id;
$$;

revoke execute on function descobreix.puntuacions() from public, anon, authenticated;

-- Classificació per punts o per municipis, general o entre amics. Torna les `limit_files` primeres
-- posicions i, si no hi és, també la fila de l'usuari, perquè sempre vegi on és. Els empats
-- comparteixen posició.
create function descobreix.ranquing(criteri text, nomes_amics boolean, limit_files integer default 50)
returns table (
    posicio bigint,
    usuari_id uuid,
    nom_usuari text,
    foto text,
    punts bigint,
    municipis bigint,
    soc_jo boolean
)
language plpgsql
stable
security definer
set search_path = ''
as $$
declare
    jo uuid := auth.uid();
begin
    if jo is null then
        raise exception 'Cal haver iniciat la sessió' using errcode = '42501';
    end if;
    if criteri not in ('PUNTS', 'MUNICIPIS') then
        raise exception 'Criteri desconegut: %', criteri using errcode = '22023';
    end if;
    return query
    with participants as (
        select p.id, p.nom_usuari, p.foto, p.creat_el, s.punts, s.municipis
        from descobreix.perfils p
        join descobreix.puntuacions() s on s.usuari_id = p.id
        where not nomes_amics or p.id = jo or descobreix.son_amics(jo, p.id)
    ),
    ordenats as (
        select pa.*,
               rank() over (order by case when criteri = 'PUNTS' then pa.punts else pa.municipis end desc) as pos,
               row_number() over (
                   order by case when criteri = 'PUNTS' then pa.punts else pa.municipis end desc, pa.creat_el, pa.id
               ) as fila
        from participants pa
    )
    select o.pos, o.id, o.nom_usuari, o.foto, o.punts, o.municipis, o.id = jo
    from ordenats o
    where o.fila <= limit_files or o.id = jo
    order by o.fila;
end;
$$;

revoke execute on function descobreix.ranquing(text, boolean, integer) from public, anon;
grant execute on function descobreix.ranquing(text, boolean, integer) to authenticated;

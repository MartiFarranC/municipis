-- Col·laboracions amb els ajuntaments (requisits.md, secció 9.6).
--   - El contingut l'entra el Martí amb el rol de servei i surt directament: no hi ha cap pas de revisió.
--   - Qualsevol usuari amb sessió el pot llegir; l'app el baixa per `sincronitzat_el` i el desa a Room.
--   - Els esborrats són lògics (`esborrat_el`, mil·lisegons), perquè l'app sàpiga què ha de treure.
--   - Dels codis QR dels punts de segellat només es guarda el resum SHA-256.
--   - Les xifres per als ajuntaments són anònimes i mai per sota de 10 persones.

create table descobreix.ajuntaments (
    codi_ine text primary key check (codi_ine ~ '^[0-9]{5}$'),
    presentacio text check (char_length(btrim(presentacio)) between 1 and 500),
    web text check (web ~ '^https://[^ ]+$'),
    oficina_turisme text check (char_length(btrim(oficina_turisme)) between 1 and 200),
    -- Si hi ha un segell propi a Storage (descobreix-ajuntaments/<codi_ine>/segell.png).
    te_segell boolean not null default false,
    esborrat_el bigint,
    sincronitzat_el timestamptz not null default clock_timestamp()
);
create index ajuntaments_sincronitzat on descobreix.ajuntaments (sincronitzat_el);

create table descobreix.missions_ajuntament (
    id uuid primary key default gen_random_uuid(),
    codi_ine text not null references descobreix.ajuntaments (codi_ine),
    titol text not null check (char_length(btrim(titol)) between 1 and 80),
    descripcio text check (char_length(btrim(descripcio)) between 1 and 300),
    -- GPS: un punt (lat, lon). FOTO: dins del municipi. QR: llegir el codi des de dins del municipi.
    prova text not null check (prova in ('GPS', 'FOTO', 'QR')),
    lat double precision check (lat between 40 and 43.5),
    lon double precision check (lon between 0 and 3.5),
    resum_qr text check (resum_qr ~ '^[0-9a-f]{64}$'),
    -- Festes i fires: només es pot fer aquests dies (hora de Catalunya, dates incloses).
    data_inici date,
    data_fi date,
    -- L'ordre a la fitxa. L'app només en fa servir les primeres (configuració del joc).
    ordre integer not null default 0,
    esborrat_el bigint,
    sincronitzat_el timestamptz not null default clock_timestamp(),
    check ((prova = 'GPS') = (lat is not null and lon is not null)),
    check ((lat is null) = (lon is null)),
    check ((prova = 'QR') = (resum_qr is not null)),
    check ((data_inici is null) = (data_fi is null)),
    check (data_fi >= data_inici)
);
create index missions_ajuntament_sincronitzat on descobreix.missions_ajuntament (sincronitzat_el);
create unique index missions_ajuntament_qr on descobreix.missions_ajuntament (resum_qr);

-- Avantatges fora de l'app (entrada reduïda, regal…). L'app només els mostra.
create table descobreix.avantatges (
    id uuid primary key default gen_random_uuid(),
    codi_ine text not null references descobreix.ajuntaments (codi_ine),
    titol text not null check (char_length(btrim(titol)) between 1 and 80),
    descripcio text check (char_length(btrim(descripcio)) between 1 and 300),
    condicions text check (char_length(btrim(condicions)) between 1 and 300),
    valid_fins date,
    esborrat_el bigint,
    sincronitzat_el timestamptz not null default clock_timestamp()
);
create index avantatges_sincronitzat on descobreix.avantatges (sincronitzat_el);

create trigger marca_sincronitzat before update on descobreix.ajuntaments
for each row execute function descobreix.marca_sincronitzat();
create trigger marca_sincronitzat before update on descobreix.missions_ajuntament
for each row execute function descobreix.marca_sincronitzat();
create trigger marca_sincronitzat before update on descobreix.avantatges
for each row execute function descobreix.marca_sincronitzat();

grant select on descobreix.ajuntaments, descobreix.missions_ajuntament, descobreix.avantatges to authenticated;
grant all on descobreix.ajuntaments, descobreix.missions_ajuntament, descobreix.avantatges to service_role;

alter table descobreix.ajuntaments enable row level security;
alter table descobreix.missions_ajuntament enable row level security;
alter table descobreix.avantatges enable row level security;

create policy ajuntaments_llegeix on descobreix.ajuntaments for select to authenticated using (true);
create policy missions_ajuntament_llegeix on descobreix.missions_ajuntament for select to authenticated using (true);
create policy avantatges_llegeix on descobreix.avantatges for select to authenticated using (true);

-- Segells propis: públics per llegir. Només el rol de servei hi escriu (no hi ha cap política d'escriptura).
insert into storage.buckets (id, name, public, file_size_limit, allowed_mime_types)
values ('descobreix-ajuntaments', 'descobreix-ajuntaments', true, 524288, array['image/png'])
on conflict (id) do nothing;

-- Xifres anònimes per a un ajuntament: quanta gent ha desbloquejat el municipi i quantes missions s'hi han fet
-- cada mes (dels últims 12). Si hi ha menys de 10 persones, no es dona cap xifra: el total surt buit i els mesos
-- amb menys de 10 persones no hi surten.
create function descobreix.xifres_ajuntament(codi text)
returns table (mes date, persones bigint, missions bigint)
language sql
stable
set search_path = ''
as $$
    with total as (
        select null::date as mes, count(*) as persones, null::bigint as missions
        from descobreix.municipis_descoberts where codi_ine = codi
    ),
    mesos as (
        select date_trunc('month', to_timestamp(creat_el / 1000.0) at time zone 'Europe/Madrid')::date as mes,
               count(distinct usuari_id) as persones,
               count(*) as missions
        from descobreix.missions_completades
        where codi_ine = codi and to_timestamp(creat_el / 1000.0) >= now() - interval '12 months'
        group by 1
    )
    select mes, case when persones >= 10 then persones end, missions from total
    union all
    select mes, persones, missions from mesos where persones >= 10
    order by mes nulls first;
$$;

revoke execute on function descobreix.xifres_ajuntament(text) from public, anon, authenticated;
grant execute on function descobreix.xifres_ajuntament(text) to service_role;

-- Sincronització (requisits.md, secció 9.2): les dades noves des de l'esquema inicial.
--   - Els segells del passaport: un per municipi; només s'hi afegeixen files.
--   - Els sacs: es guanyen (s'afegeixen) i s'obren (es modifiquen un sol cop); guanya la modificació més recent.
--   - Les fotos del cartell retallades amb el requadre, que van al catàleg (fotos.es_cromo).

alter table descobreix.fotos add column es_cromo boolean not null default false;

create table descobreix.segells (
    id uuid primary key,
    usuari_id uuid not null default auth.uid() references descobreix.perfils (id) on delete cascade,
    codi_ine text not null check (codi_ine ~ '^[0-9]{5}$'),
    comarca text not null,
    pagina integer not null check (pagina >= 0),
    x real not null check (x between 0 and 1),
    y real not null check (y between 0 and 1),
    gir real not null,
    tinta integer not null check (tinta >= 0),
    creat_el bigint not null,
    modificat_el bigint not null,
    sincronitzat_el timestamptz not null default clock_timestamp(),
    unique (usuari_id, codi_ine)
);
create index segells_sincronitzat on descobreix.segells (usuari_id, sincronitzat_el);

create table descobreix.sacs (
    id uuid primary key,
    usuari_id uuid not null default auth.uid() references descobreix.perfils (id) on delete cascade,
    -- Per què es va guanyar (primera_foto, medalla_comarca_24_bronze…): un sac per origen.
    origen text not null,
    objecte_tipus text check (objecte_tipus in ('EMOJI', 'PORTADA', 'ANIMACIO', 'COLOR')),
    objecte_id text,
    punts integer check (punts >= 0),
    obert_el bigint,
    creat_el bigint not null,
    modificat_el bigint not null,
    sincronitzat_el timestamptz not null default clock_timestamp(),
    unique (usuari_id, origen),
    check ((objecte_tipus is null) = (objecte_id is null))
);
create index sacs_sincronitzat on descobreix.sacs (usuari_id, sincronitzat_el);

create trigger marca_sincronitzat before update on descobreix.sacs
for each row execute function descobreix.marca_sincronitzat();

grant select, insert on descobreix.segells to authenticated;
grant select, insert, update on descobreix.sacs to authenticated;
grant all on descobreix.segells, descobreix.sacs to service_role;

alter table descobreix.segells enable row level security;
alter table descobreix.sacs enable row level security;

-- Segells i sacs: de moment només el propietari.
create policy segells_llegeix on descobreix.segells for select to authenticated
    using (usuari_id = (select auth.uid()));
create policy segells_crea on descobreix.segells for insert to authenticated
    with check (usuari_id = (select auth.uid()));

create policy sacs_llegeix on descobreix.sacs for select to authenticated
    using (usuari_id = (select auth.uid()));
create policy sacs_crea on descobreix.sacs for insert to authenticated
    with check (usuari_id = (select auth.uid()));
create policy sacs_modifica on descobreix.sacs for update to authenticated
    using (usuari_id = (select auth.uid())) with check (usuari_id = (select auth.uid()));

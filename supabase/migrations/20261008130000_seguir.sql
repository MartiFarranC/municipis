-- Seguir (docs/decisions-pendents.md, «Perfils», «Comptes públics i privats» i «Decisions de seguir i animar»).
--   - Els amics desapareixen: ara es segueix. Un compte públic es pot seguir directament; un de privat accepta o
--     rebutja les sol·licituds. Fins que no s'accepta, no es veu res del compte.
--   - Cada perfil és d'Explorador o d'Espectador, i no es pot canviar.
--   - La visibilitat AMICS de les fotos passa a ser SEGUIDORS.
--   - El mur: el que ha fet la gent que segueixes (municipis desbloquejats i fotos).

-- Perfils ---------------------------------------------------------------------------------------

alter table descobreix.perfils
    add column tipus text not null default 'EXPLORADOR' check (tipus in ('EXPLORADOR', 'ESPECTADOR'));
-- Null mentre l'usuari no ho ha triat (no hi ha cap opció per defecte).
alter table descobreix.perfils add column public boolean;

create function descobreix.protegeix_perfil() returns trigger
language plpgsql
set search_path = ''
as $$
begin
    if new.tipus <> old.tipus then
        raise exception 'No es pot canviar d''Explorador a Espectador ni al revés';
    end if;
    return new;
end;
$$;

create trigger protegeix_perfil
before update on descobreix.perfils
for each row execute function descobreix.protegeix_perfil();

-- Seguiments ------------------------------------------------------------------------------------

create table descobreix.seguiments (
    seguidor_id uuid not null default auth.uid() references descobreix.perfils (id) on delete cascade,
    seguit_id uuid not null references descobreix.perfils (id) on delete cascade,
    estat text not null default 'PENDENT' check (estat in ('PENDENT', 'ACCEPTAT')),
    creat_el timestamptz not null default now(),
    primary key (seguidor_id, seguit_id),
    check (seguidor_id <> seguit_id)
);
create index seguiments_seguit on descobreix.seguiments (seguit_id);

-- Si el compte que se segueix és públic, el seguiment queda acceptat de seguida; si no, queda pendent.
create function descobreix.estat_nou_seguiment() returns trigger
language plpgsql
security definer
set search_path = ''
as $$
begin
    new.estat := case
        when coalesce((select p.public from descobreix.perfils p where p.id = new.seguit_id), false) then 'ACCEPTAT'
        else 'PENDENT'
    end;
    return new;
end;
$$;

create trigger estat_nou_seguiment
before insert on descobreix.seguiments
for each row execute function descobreix.estat_nou_seguiment();

-- Un seguiment només pot passar de PENDENT a ACCEPTAT, i no se'n poden canviar els usuaris.
create function descobreix.protegeix_seguiment() returns trigger
language plpgsql
set search_path = ''
as $$
begin
    if new.seguidor_id <> old.seguidor_id or new.seguit_id <> old.seguit_id then
        raise exception 'No es poden canviar els usuaris d''un seguiment';
    end if;
    if not (old.estat = 'PENDENT' and new.estat = 'ACCEPTAT') then
        raise exception 'Només es pot acceptar una sol·licitud pendent';
    end if;
    return new;
end;
$$;

create trigger protegeix_seguiment
before update on descobreix.seguiments
for each row execute function descobreix.protegeix_seguiment();

-- Si [seguidor] segueix [seguit] (i [seguit] l'ha acceptat).
create function descobreix.el_segueix(seguidor uuid, seguit uuid) returns boolean
language sql
stable
security definer
set search_path = ''
as $$
    select exists (
        select 1 from descobreix.seguiments s
        where s.seguidor_id = seguidor and s.seguit_id = seguit and s.estat = 'ACCEPTAT'
    );
$$;

-- Si el compte és públic.
create function descobreix.es_public(usuari uuid) returns boolean
language sql
stable
security definer
set search_path = ''
as $$
    select coalesce((select p.public from descobreix.perfils p where p.id = usuari), false);
$$;

grant select, insert, update, delete on descobreix.seguiments to authenticated;
grant all on descobreix.seguiments to service_role;
alter table descobreix.seguiments enable row level security;

-- Els dos usuaris veuen el seguiment. A més, a qui segueix una persona ho veu tothom si el compte és públic, o
-- els seus seguidors si és privat (la llista de persones que segueix un Espectador).
create policy seguiments_llegeix on descobreix.seguiments for select to authenticated
    using (
        (select auth.uid()) in (seguidor_id, seguit_id)
        or (
            estat = 'ACCEPTAT'
            and (descobreix.es_public(seguidor_id) or descobreix.el_segueix((select auth.uid()), seguidor_id))
        )
    );
-- Només el seguidor el crea (l'estat el posa el servidor), només el seguit l'accepta i qualsevol dels dos
-- l'esborra (deixar de seguir, rebutjar o treure un seguidor).
create policy seguiments_crea on descobreix.seguiments for insert to authenticated
    with check (seguidor_id = (select auth.uid()));
create policy seguiments_accepta on descobreix.seguiments for update to authenticated
    using (seguit_id = (select auth.uid())) with check (seguit_id = (select auth.uid()));
create policy seguiments_esborra on descobreix.seguiments for delete to authenticated
    using ((select auth.uid()) in (seguidor_id, seguit_id));

-- Visibilitat: de les amistats als seguidors ----------------------------------------------------

drop policy municipis_descoberts_llegeix on descobreix.municipis_descoberts;
create policy municipis_descoberts_llegeix on descobreix.municipis_descoberts for select to authenticated
    using (usuari_id = (select auth.uid()) or descobreix.el_segueix((select auth.uid()), usuari_id));

alter table descobreix.fotos drop constraint fotos_visibilitat_check;
update descobreix.fotos set visibilitat = 'SEGUIDORS' where visibilitat = 'AMICS';
alter table descobreix.fotos
    add constraint fotos_visibilitat_check check (visibilitat in ('PRIVADA', 'SEGUIDORS', 'PUBLICA'));

-- Una foto pública d'un compte privat només la veuen els seus seguidors: d'un compte privat no es veu res.
drop policy fotos_llegeix on descobreix.fotos;
create policy fotos_llegeix on descobreix.fotos for select to authenticated
    using (
        usuari_id = (select auth.uid())
        or (
            esborrat_el is null
            and visibilitat <> 'PRIVADA'
            and (
                descobreix.el_segueix((select auth.uid()), usuari_id)
                or (visibilitat = 'PUBLICA' and descobreix.es_public(usuari_id))
            )
        )
    );

drop table descobreix.amistats;
drop function descobreix.son_amics(uuid, uuid);
drop function descobreix.protegeix_amistat();

-- Perfil d'una persona -----------------------------------------------------------------------------

-- El que es veu del perfil de qualsevol: el nom, el tipus, si és públic, quants el segueixen, a quants segueix i
-- com està el seguiment amb qui pregunta.
create function descobreix.perfil_de(usuari uuid)
returns table (
    id uuid,
    nom_usuari text,
    tipus text,
    public boolean,
    seguidors bigint,
    seguits bigint,
    el_segueixo text,
    em_segueix boolean
)
language sql
stable
security definer
set search_path = ''
as $$
    select
        p.id,
        p.nom_usuari,
        p.tipus,
        coalesce(p.public, false),
        (select count(*) from descobreix.seguiments s where s.seguit_id = p.id and s.estat = 'ACCEPTAT'),
        (select count(*) from descobreix.seguiments s where s.seguidor_id = p.id and s.estat = 'ACCEPTAT'),
        (select s.estat from descobreix.seguiments s where s.seguidor_id = auth.uid() and s.seguit_id = p.id),
        descobreix.el_segueix(p.id, auth.uid())
    from descobreix.perfils p
    where p.id = usuari;
$$;

-- El mur ------------------------------------------------------------------------------------------

-- El que ha fet la gent que segueixes, del més nou al més antic: municipis desbloquejats (no el d'inici) i fotos
-- que pots veure. Es pagina amb [abans] (creat_el en mil·lisegons). És security invoker: la RLS hi mana.
create function descobreix.mur(abans bigint default null, quants integer default 50)
returns table (
    tipus text,
    usuari_id uuid,
    nom_usuari text,
    codi_ine text,
    foto_id uuid,
    ruta_miniatura text,
    creat_el bigint
)
language sql
stable
set search_path = ''
as $$
    select e.*
    from (
        select 'MUNICIPI'::text, m.usuari_id, p.nom_usuari, m.codi_ine, null::uuid, null::text, m.creat_el
        from descobreix.municipis_descoberts m
        join descobreix.perfils p on p.id = m.usuari_id
        where m.usuari_id <> auth.uid() and not m.es_inici
        union all
        select 'FOTO'::text, f.usuari_id, p.nom_usuari, f.codi_ine, f.id, f.ruta_miniatura, f.creat_el
        from descobreix.fotos f
        join descobreix.perfils p on p.id = f.usuari_id
        where f.usuari_id <> auth.uid() and f.esborrat_el is null
          and descobreix.el_segueix(auth.uid(), f.usuari_id)
    ) e (tipus, usuari_id, nom_usuari, codi_ine, foto_id, ruta_miniatura, creat_el)
    where abans is null or e.creat_el < abans
    order by e.creat_el desc
    limit least(greatest(quants, 1), 200);
$$;

revoke execute on all functions in schema descobreix from public, anon;
grant execute on function descobreix.el_segueix(uuid, uuid) to authenticated;
grant execute on function descobreix.es_public(uuid) to authenticated;
grant execute on function descobreix.perfil_de(uuid) to authenticated;
grant execute on function descobreix.mur(bigint, integer) to authenticated;
grant execute on function descobreix.esborra_dades() to authenticated;

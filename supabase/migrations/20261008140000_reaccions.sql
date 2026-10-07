-- Reaccions per animar, bloquejar i denunciar (docs/decisions-pendents.md, «Reaccions per animar» i «Decisions de
-- seguir i animar»).
--   - Es reacciona a un municipi desbloquejat o a una foto d'algú que segueixes: amb un emoji (que s'envia amb la
--     seva frase) o amb un missatge lliure de 80 caràcters com a molt.
--   - Les reaccions només les veuen qui les envia i qui les rep.
--   - Es pot bloquejar algú: deixa de seguir-te (i tu a ell) i ja no et pot seguir ni escriure.
--   - Es pot denunciar algú (o una reacció). Les denúncies les revisa el Martí des del servidor.

-- Bloquejos -------------------------------------------------------------------------------------

create table descobreix.bloquejos (
    bloquejador_id uuid not null default auth.uid() references descobreix.perfils (id) on delete cascade,
    bloquejat_id uuid not null references descobreix.perfils (id) on delete cascade,
    creat_el timestamptz not null default now(),
    primary key (bloquejador_id, bloquejat_id),
    check (bloquejador_id <> bloquejat_id)
);
create index bloquejos_bloquejat on descobreix.bloquejos (bloquejat_id);

-- Si hi ha un bloqueig entre els dos, en qualsevol sentit.
create function descobreix.bloquejats(a uuid, b uuid) returns boolean
language sql
stable
security definer
set search_path = ''
as $$
    select exists (
        select 1 from descobreix.bloquejos
        where (bloquejador_id = a and bloquejat_id = b) or (bloquejador_id = b and bloquejat_id = a)
    );
$$;

-- En bloquejar algú, s'esborren els seguiments entre els dos.
create function descobreix.en_bloquejar() returns trigger
language plpgsql
security definer
set search_path = ''
as $$
begin
    delete from descobreix.seguiments
    where (seguidor_id = new.bloquejador_id and seguit_id = new.bloquejat_id)
       or (seguidor_id = new.bloquejat_id and seguit_id = new.bloquejador_id);
    return new;
end;
$$;

create trigger en_bloquejar
after insert on descobreix.bloquejos
for each row execute function descobreix.en_bloquejar();

grant select, insert, delete on descobreix.bloquejos to authenticated;
grant all on descobreix.bloquejos to service_role;
alter table descobreix.bloquejos enable row level security;

-- Cadascú veu i gestiona els seus bloquejos; el bloquejat no ho veu.
create policy bloquejos_llegeix on descobreix.bloquejos for select to authenticated
    using (bloquejador_id = (select auth.uid()));
create policy bloquejos_crea on descobreix.bloquejos for insert to authenticated
    with check (bloquejador_id = (select auth.uid()));
create policy bloquejos_esborra on descobreix.bloquejos for delete to authenticated
    using (bloquejador_id = (select auth.uid()));

-- Amb un bloqueig pel mig, no es pot demanar de seguir.
drop policy seguiments_crea on descobreix.seguiments;
create policy seguiments_crea on descobreix.seguiments for insert to authenticated
    with check (seguidor_id = (select auth.uid()) and not descobreix.bloquejats(seguidor_id, seguit_id));

-- Reaccions --------------------------------------------------------------------------------------

create table descobreix.reaccions (
    id uuid primary key default gen_random_uuid(),
    autor_id uuid not null default auth.uid() references descobreix.perfils (id) on delete cascade,
    destinatari_id uuid not null references descobreix.perfils (id) on delete cascade,
    -- A què es reacciona: un municipi desbloquejat (el codi INE) o una foto (el seu id).
    objectiu_tipus text not null check (objectiu_tipus in ('MUNICIPI', 'FOTO')),
    objectiu_id text not null,
    -- Un emoji (l'id de la col·lecció, que s'envia amb la seva frase) o un missatge lliure.
    emoji text,
    missatge text check (char_length(btrim(missatge)) between 1 and 80),
    creat_el timestamptz not null default now(),
    check ((emoji is null) <> (missatge is null)),
    check (autor_id <> destinatari_id)
);
create index reaccions_destinatari on descobreix.reaccions (destinatari_id, creat_el desc);
create index reaccions_autor on descobreix.reaccions (autor_id);

grant select, insert, delete on descobreix.reaccions to authenticated;
grant all on descobreix.reaccions to service_role;
alter table descobreix.reaccions enable row level security;

create policy reaccions_llegeix on descobreix.reaccions for select to authenticated
    using ((select auth.uid()) in (autor_id, destinatari_id));
-- Només a algú que segueixes (i que t'ha acceptat), i si no hi ha cap bloqueig entre els dos.
create policy reaccions_crea on descobreix.reaccions for insert to authenticated
    with check (
        autor_id = (select auth.uid())
        and descobreix.el_segueix(autor_id, destinatari_id)
        and not descobreix.bloquejats(autor_id, destinatari_id)
    );
-- Qui l'envia la pot retirar i qui la rep la pot treure.
create policy reaccions_esborra on descobreix.reaccions for delete to authenticated
    using ((select auth.uid()) in (autor_id, destinatari_id));

-- Denúncies --------------------------------------------------------------------------------------

create table descobreix.denuncies (
    id uuid primary key default gen_random_uuid(),
    denunciant_id uuid not null default auth.uid() references descobreix.perfils (id) on delete cascade,
    denunciat_id uuid not null references descobreix.perfils (id) on delete cascade,
    -- La reacció denunciada, si n'hi ha. Se'n copia el text, per si després s'esborra.
    reaccio_id uuid,
    text_denunciat text,
    motiu text check (char_length(motiu) <= 500),
    -- PENDENT fins que el Martí la revisa (des del servidor, amb el rol de servei).
    estat text not null default 'PENDENT' check (estat in ('PENDENT', 'REVISADA')),
    creat_el timestamptz not null default now(),
    check (denunciant_id <> denunciat_id)
);
create index denuncies_pendents on descobreix.denuncies (estat, creat_el);

grant select, insert on descobreix.denuncies to authenticated;
grant all on descobreix.denuncies to service_role;
alter table descobreix.denuncies enable row level security;

create policy denuncies_llegeix on descobreix.denuncies for select to authenticated
    using (denunciant_id = (select auth.uid()));
create policy denuncies_crea on descobreix.denuncies for insert to authenticated
    with check (denunciant_id = (select auth.uid()) and estat = 'PENDENT');

revoke execute on all functions in schema descobreix from public, anon;
grant execute on function descobreix.bloquejats(uuid, uuid) to authenticated;
grant execute on function descobreix.el_segueix(uuid, uuid) to authenticated;
grant execute on function descobreix.es_public(uuid) to authenticated;
grant execute on function descobreix.perfil_de(uuid) to authenticated;
grant execute on function descobreix.mur(bigint, integer) to authenticated;
grant execute on function descobreix.esborra_dades() to authenticated;

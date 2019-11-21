---- X1000 ----
create or replace view X1000 as
select generate_series as X
from generate_series(1,1000)
;


---- X1000000 ----
create or replace view X1000000 as
select generate_series as X
from generate_series(1,1000000)
;


---- EnsureNoTableOrViewMetaQuery ----
with N as ( select min(oid) as n_id
            from pg_catalog.pg_namespace
            where nspname = current_schema()
            limit 1 )
select 'drop ' || what || ' if exists ' || quote_ident(relname) || ' cascade' as cmd,
       C.oid::varchar::bigint as ord
from pg_catalog.pg_class C
     natural join
     (SELECT 'r'::"char", 'table' UNION
      SELECT 'v'::"char", 'view') as CX(relkind, what)
where relnamespace = (select n_id from N)
  and lower(relname) in (lower(?),lower(?),lower(?),lower(?))
;

---- ZapSchemaMetaQuery ----
with N as ( select min(oid) as n_id
            from pg_catalog.pg_namespace
            where nspname = current_schema()
            limit 1 )
--
select 'drop ' || what || ' if exists ' || quote_ident(typname) || ' cascade' as cmd,
       T.oid::varchar::bigint as ord
from pg_catalog.pg_type T
     natural join
     (SELECT 'e'::"char", 'type' UNION SELECT 'd'::"char", 'domain') as TX(typtype, what)
where typnamespace = (select n_id from N)
--
union all
--
select 'drop type if exists ' || quote_ident(ST.typname) || ' cascade' as cmd,
       ST.oid::varchar::bigint as ord
from pg_catalog.pg_type ST,
     pg_catalog.pg_class SC
where ST.typnamespace = (select n_id from N)
  and ST.typrelid = SC.oid
  and SC.relkind = 'c'::"char"
--
union all
--
select 'drop ' || what || ' if exists ' || quote_ident(relname) || ' cascade' as cmd,
       C.oid::varchar::bigint as ord
from pg_catalog.pg_class C
     natural join
     (SELECT 'r'::"char", 'table' UNION
      SELECT 'v'::"char", 'view') as CX(relkind, what)
where relnamespace = (select n_id from N)
--
union all
--
select 'drop ' || case when proisagg then 'aggregate if exists' else 'function' end
               || ' ' || quote_ident(proname) || '(' || oidvectortypes(proargtypes)::varchar || ') cascade' as cmd,
       oid::varchar::bigint as ord
from pg_catalog.pg_proc
where pronamespace = (select n_id from N)
--
union all
--
select 'drop operator if exists ' || oprname || '('
              || coalesce(format_type(nullif(oprleft,0),null),'none') || ','
              || coalesce(format_type(nullif(oprright,0),null),'none') || ') cascade' as cmd,
       oid::varchar::bigint as ord
from pg_catalog.pg_operator
where oprnamespace = (select n_id from N)
--
order by ord desc
;


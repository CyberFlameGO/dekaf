---- X1000 ----
create or replace view X1000 as
with rg2(X) as (select 1 union all select 1),
     rg8(X) as (select 1 from rg2 cross join rg2 cross join rg2),
     rg512(X) as (select 1 from rg8 cross join rg8 cross join rg8),
     rg1000(X) as (select 1 from rg512 cross join rg2 limit 1000)
select row_number() over(order by 1) X from rg1000
;


---- X1000000 ----
create or replace view X1000000 as
select row_number() over(order by 1) X from X1000 cross join X1000
;


---- EnsureNoTableOrViewMetaQuery ----
select case when object_type = 'VIEW' then 'drop view if exists "' || replace(object_name, '"', '""') || '" cascade'
            when object_type = 'TABLE' then 'drop table if exists "' || replace(object_name, '"', '""') || '" cascade constraints'
            else null end as cmd
from sys.exa_all_objects
where lower(object_name) in (lower(?),lower(?),lower(?),lower(?))
;


---- ZapSchemaMetaQuery ----
select case when object_type = 'VIEW' then 'drop view if exists "' || replace(object_name, '"', '""') || '" cascade'
            when object_type = 'TABLE' then 'drop table if exists "' || replace(object_name, '"', '""') || '" cascade constraints'
            when object_type = 'FUNCTION' then 'drop function "' || replace(object_name, '"', '""') || '"'
            when script_type = 'ADAPTER' then 'drop adapter script if exists "' || replace(object_name, '"', '""') || '"'
            when object_type = 'SCRIPT' then 'drop script if exists "' || replace(object_name, '"', '""') || '"'
            when object_type = 'CONNECTION' then 'drop connection if exists "' || replace(object_name, '"', '""') || '"'
            else null end as cmd
from sys.exa_all_objects left join sys.exa_all_scripts on object_id = script_object_id where root_name = current_schema
;

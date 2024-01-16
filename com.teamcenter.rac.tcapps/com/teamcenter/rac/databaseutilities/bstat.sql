Rem Modified version of $ORACLE_HOME/rdbms/admin/utlbstat.sql
Rem

grant select any table to infodba;

prompt ********************************************************************
prompt              Drop and Create all temporary tables
prompt ********************************************************************

drop table stats$begin_stats;
create table stats$begin_stats 
as select * from v$sysstat where 0 = 1;

drop table stats$end_stats;
create table stats$end_stats 
as select * from stats$begin_stats;

drop table stats$begin_latch;
create table stats$begin_latch 
as select * from v$latch where 0 = 1;

drop table stats$end_latch;
create table stats$end_latch 
as select * from stats$begin_latch;

drop table stats$begin_roll;
create table stats$begin_roll 
as select * from v$rollstat where 0 = 1;

drop table stats$end_roll;
create table stats$end_roll 
as select * from stats$begin_roll;

drop table stats$begin_lib;
create table stats$begin_lib 
as select * from v$librarycache where 0 = 1;

drop table stats$end_lib;
create table stats$end_lib 
as select * from stats$begin_lib;

drop table stats$begin_dc;
create table stats$begin_dc 
as select * from v$rowcache where 0 = 1;

drop table stats$end_dc;
create table stats$end_dc 
as select * from stats$begin_dc;

drop table stats$begin_event;
create table stats$begin_event 
as select * from v$system_event where 0 = 1;

drop table stats$end_event;
create table stats$end_event 
as select * from stats$begin_event;

drop table stats$begin_bck_event;
create table stats$begin_bck_event
  (event varchar2(200), total_waits number, time_waited number)
;

drop table stats$chained_rows;
create table stats$chained_rows
  (owner_name varchar2(30), table_name varchar2(30),
   cluster_name varchar2(30), head_rowid rowid,
   timestamp date)
;
Rem analyze table <table_name> list chained rows into stats$chained_rows;

drop table stats$end_bck_event;
create table stats$end_bck_event 
as select * from stats$begin_bck_event;

drop table stats$dates;
create table stats$dates (start_time date, end_time date)
;

drop view stats$file_view;
create view stats$file_view
as
  select ts.name    ts,
         i.name     name,
         x.phyrds pyr,
         x.phywrts pyw,
         x.readtim prt,
         x.writetim pwt,
         x.phyblkrd pbr,
         x.phyblkwrt pbw,
         round(i.bytes/1000000) megabytes_size
  from v$filestat x,
       sys.ts$ ts,
       v$datafile i,
       sys.file$ f
  where i.file#=f.file#
   and ts.ts#=f.ts#
   and x.file#=f.file#;

drop table stats$begin_file;
create table stats$begin_file 
as select * from stats$file_view where 0 = 1;

drop table stats$end_file;
create table stats$end_file 
as select * from stats$begin_file;

drop table stats$begin_waitstat;
create table stats$begin_waitstat 
as select * from v$waitstat where 1=0;

drop table stats$end_waitstat;
create table stats$end_waitstat 
as select * from stats$begin_waitstat;


prompt ********************************************************************
prompt                    Gather start statistics
prompt ********************************************************************

insert into stats$dates select sysdate, null from dual;

insert into stats$begin_waitstat select * from v$waitstat;

insert into stats$begin_bck_event 
  select event, sum(total_waits), sum(time_waited)
    from v$session s, v$session_event e
    where type = 'BACKGROUND' and s.sid = e.sid
    group by event;

insert into stats$begin_event select * from v$system_event;

insert into stats$begin_roll select * from v$rollstat;

insert into stats$begin_file select * from stats$file_view;

insert into stats$begin_dc select * from v$rowcache;

insert into stats$begin_stats select * from v$sysstat;

insert into stats$begin_lib select * from v$librarycache;

insert into stats$begin_latch select * from v$latch;

Rem Modified version of $ORACLE_HOME/rdbms/admin/utlestat.sql

Rem ********************************************************************
Rem                Gather Ending Statistics
Rem ********************************************************************


insert into stats$end_latch select * from v$latch;
insert into stats$end_stats select * from v$sysstat;
insert into stats$end_lib select * from v$librarycache;
update stats$dates set end_time = sysdate;
insert into stats$end_event select * from v$system_event;
insert into stats$end_bck_event
  select event, sum(total_waits), sum(time_waited)
    from v$session s, v$session_event e
    where type = 'BACKGROUND' and s.sid = e.sid
    group by event;
insert into stats$end_waitstat select * from v$waitstat;
insert into stats$end_roll select * from v$rollstat;
insert into stats$end_file select * from stats$file_view;
insert into stats$end_dc select * from v$rowcache;

Rem ********************************************************************
Rem                Create Summary Tables
Rem ********************************************************************

create table stats$stats  as
select  e.value-b.value change , n.name
   from v$statname n ,  stats$begin_stats b , stats$end_stats e
	where n.statistic# = b.statistic# and n.statistic# = e.statistic#;

create table stats$latches  as
select 	e.gets-b.gets gets,
	e.misses-b.misses misses,
	e.sleeps-b.sleeps sleeps,
	e.immediate_gets-b.immediate_gets immed_gets,
	e.immediate_misses-b.immediate_misses immed_miss,
	n.name
   from v$latchname n ,  stats$begin_latch b , stats$end_latch e
	where n.latch# = b.latch# and n.latch# = e.latch#;

create table stats$event  as
  select  e.total_waits-b.total_waits event_count,
          e.time_waited-b.time_waited time_waited,
          e.event
    from  stats$begin_event b , stats$end_event e
    where b.event = e.event
  union all
  select  e.total_waits event_count,
          e.time_waited time_waited,
          e.event
    from  stats$end_event e
    where e.event not in (select b.event from stats$begin_event b);

create table stats$bck_event  as
  select  e.total_waits-b.total_waits event_count,
          e.time_waited-b.time_waited time_waited,
          e.event
    from  stats$begin_bck_event b , stats$end_bck_event e
    where b.event = e.event
  union all
  select  e.total_waits event_count,
          e.time_waited time_waited,
          e.event
    from  stats$end_bck_event e
    where e.event not in (select b.event from stats$begin_bck_event b);

Rem subtract background events out of regular events
update stats$event e
  set (event_count, time_waited) =
	(select e.event_count - b.event_count,
	        e.time_waited - b.time_waited
	  from stats$bck_event b
         where e.event = b.event)
   where e.event in (select b.event from stats$bck_event b);

create table stats$waitstat  as
select  e.class,
        e.count - b.count count,
        e.time - b.time time
  from stats$begin_waitstat b, stats$end_waitstat e
   where e.class = b.class;

create table stats$roll  as
select  e.usn undo_segment,
        e.gets-b.gets trans_tbl_gets,
	e.waits-b.waits trans_tbl_waits,
	e.writes-b.writes undo_bytes_written,
	e.rssize segment_size_bytes,
        e.xacts-b.xacts xacts,
	e.shrinks-b.shrinks shrinks,
        e.wraps-b.wraps wraps
   from stats$begin_roll b, stats$end_roll e
        where e.usn = b.usn;

create table stats$files  as
select b.ts table_space,
       b.name file_name,
       e.pyr-b.pyr phys_reads,
       e.pbr-b.pbr phys_blks_rd,
       e.prt-b.prt phys_rd_time,
       e.pyw-b.pyw phys_writes,
       e.pbw-b.pbw phys_blks_wr,
       e.pwt-b.pwt phys_wrt_tim,
       e.megabytes_size
  from stats$begin_file b, stats$end_file e
       where b.name=e.name;

create table stats$dc  as
select b.parameter name,
       e.gets-b.gets get_reqs,
       e.getmisses-b.getmisses get_miss,
       e.scans-b.scans scan_reqs,
       e.scanmisses-b.scanmisses scan_miss,
       e.modifications-b.modifications mod_reqs,
       e.count count,
       e.usage cur_usage
  from stats$begin_dc b, stats$end_dc e
       where b.cache#=e.cache#
        and  nvl(b.subordinate#,-1) = nvl(e.subordinate#,-1);

create table stats$lib  as
select e.namespace,
       e.gets-b.gets gets,
       e.gethits-b.gethits gethits,
       e.pins-b.pins pins,
       e.pinhits-b.pinhits pinhits,
       e.reloads - b.reloads reloads,
       e.invalidations - b.invalidations invalidations
  from stats$begin_lib b, stats$end_lib e
       where b.namespace = e.namespace;


Rem *******************************************************************
Rem              Output statistics
Rem *******************************************************************

Rem parameterKey:Library cache statistics
prompt **************************************************************
prompt Select Library cache statistics:  The pin hit rate shoule be high.
prompt **************************************************************
select namespace library,
       gets,
       round(decode(gethits,0,1,gethits)/decode(gets,0,1,gets),3)
          gethitratio,
       pins,
       round(decode(pinhits,0,1,pinhits)/decode(pins,0,1,pins),3)
          pinhitratio,
       reloads, invalidations
  from stats$lib;

Rem parameterKey:Values changed
prompt **************************************************************
prompt All the changed values in stats$stats between bstat and estat...
prompt **************************************************************
select name "Statistic", change from stats$stats;

Rem parameterKey:Total statistics
prompt **************************************************************
prompt The total is the total value of the statistic between the time
prompt bstat was run and the time estat was run.  Note that the estat
prompt script logs on as "internal" so the per_logon statistics will
prompt always be based on at least one logon.
prompt **************************************************************
select n1.name "Statistic",
       n1.change "Total",
       round(n1.change/trans.change,2) "Per Transaction",
       round(n1.change/logs.change,2)  "Per Logon",
       round(n1.change/(to_number(to_char(end_time,   'J'))*60*60*24 -
                        to_number(to_char(start_time, 'J'))*60*60*24 +
			to_number(to_char(end_time,   'SSSSS')) -
			to_number(to_char(start_time, 'SSSSS')))
             , 2) "Per Second"
   from stats$stats n1, stats$stats trans, stats$stats logs, stats$dates
   where trans.name='user commits'
    and  logs.name='logons cumulative'
    and  trans.change != 0
    and  logs.change != 0
    and  n1.change != 0
   order by n1.name;

Rem parameterKey:Data block buffer cache efficiency
prompt **************************************************************
prompt Data block buffer cache efficiency -- Hit Ratio
prompt **************************************************************
select sum(decode(name, 'consistent gets', change, 0)) Consistent_Gets,
       sum(decode(name, 'db block gets', change, 0))   DB_Block_Gets,
       (sum(decode(name, 'consistent gets', change, 0)) +
        sum(decode(name, 'db block gets', change, 0))) Logical_Reads,
       sum(decode(name, 'physical reads', change, 0))  Physical_Reads,
       round(
	     (
	       ( sum(decode(name, 'consistent gets', change, 0)) +
                 sum(decode(name, 'db block gets', change, 0)) -
                 sum(decode(name, 'physical reads', change, 0)) ) /
               ( sum(decode(name, 'consistent gets', change, 0)) +
                 sum(decode(name, 'db block gets', change, 0)) )
	     ) * 100, 2
	    ) Hit_Ratio
   from stats$stats;


Rem parameterKey:Average length of the dirty buffer write queue
prompt **************************************************************
prompt Average length of the dirty buffer write queue.  If this is larger
prompt than the value of the db_block_write_batch init.ora parameter,
prompt then consider increasing the value of db_block_write_batch and
prompt check for disks that are doing many more IOs than other disks.
prompt **************************************************************
select queue.change/writes.change "Average Write Queue Length"
  from stats$stats queue, stats$stats writes
 where queue.name  = 'summed dirty queue length'
  and  writes.name = 'write requests'
  and  writes.change != 0;


Rem parameterKey:System wide wait events for non-background processes
prompt **************************************************************
prompt System wide wait events for non-background processes (PMON,
prompt SMON, etc).  Times are in hundreths of seconds.  Each one of
prompt these is a context switch which costs CPU time.  By looking at
prompt the Total Time you can often determine what is the bottleneck
prompt that processes are waiting for.  This shows the total time spent
prompt waiting for a specific event and the average time per wait on
prompt that event.
prompt **************************************************************
select 	n1.event "Event Name",
       	n1.event_count "Count",
	n1.time_waited "Total Time",
	round(n1.time_waited/n1.event_count, 2) "Avg Time"
   from stats$event n1
   where n1.event_count > 0
   order by n1.time_waited desc;

Rem parameterKey:System wide wait events for background processes
prompt **************************************************************
prompt System wide wait events for background processes (PMON, SMON, etc)
prompt **************************************************************
select 	n1.event "Event Name",
       	n1.event_count "Count",
	n1.time_waited "Total Time",
	round(n1.time_waited/n1.event_count, 2) "Avg Time"
   from stats$bck_event n1
   where n1.event_count > 0
   order by n1.time_waited desc;


Rem parameterKey:Latch statistics
prompt **************************************************************
prompt Latch statistics. Latch contention will show up as a large value for
prompt the 'latch free' event in the wait events above.
prompt Sleeps should be low.  The hit_ratio should be high.
prompt **************************************************************
select name latch_name, gets, misses,
    round(decode(gets-misses,0,1,gets-misses)/decode(gets,0,1,gets),3)
      hit_ratio,
    sleeps,
    round(sleeps/decode(misses,0,1,misses),3) "SLEEPS/MISS"
   from stats$latches
    where gets != 0
    order by name;

Rem parameterKey:Statistics on no_wait gets of latches
prompt **************************************************************
prompt Statistics on no_wait gets of latches.  A no_wait get does not
prompt wait for the latch to become free, it immediately times out.
prompt **************************************************************
select name latch_name,
    immed_gets nowait_gets,
    immed_miss nowait_misses,
    round(decode(immed_gets-immed_miss,0,1,immed_gets-immed_miss)/
           decode(immed_gets,0,1,immed_gets),
          3)
      nowait_hit_ratio
   from stats$latches
    where immed_gets != 0
    order by name;

Rem parameterKey:Buffer busy wait statistics
prompt **************************************************************
prompt Buffer busy wait statistics.  If the value for 'buffer busy wait' in
prompt the wait event statistics is high, then this table will identify
prompt which class of blocks is having high contention.  If there are high
prompt 'undo header' waits then add more rollback segments.  If there are
prompt high 'segment header' waits then adding freelists might help.  Check
prompt v$session_wait to get the addresses of the actual blocks having
prompt contention.
prompt **************************************************************
select * from stats$waitstat
  where count != 0
  order by count desc;

Rem parameterKey:Waits_for_trans_tbl
prompt **************************************************************
prompt Waits_for_trans_tbl high implies you should add rollback segments.
prompt **************************************************************
select * from stats$roll;

Rem parameterKey:The init.ora parameters
prompt **************************************************************
prompt The init.ora parameters currently in effect:
prompt **************************************************************
select name, value from v$parameter where isdefault = 'FALSE'
  order by name;

Rem parameterKey:get_miss and scan_miss
prompt **************************************************************
prompt get_miss and scan_miss should be very low compared to the requests.
prompt cur_usage is the number of entries in the cache that are being used.
prompt **************************************************************
select * from stats$dc
 where get_reqs != 0 or scan_reqs != 0 or mod_reqs != 0;

Rem parameterKey:IO per tablespaces
prompt **************************************************************
prompt IO per tablespaces.
prompt **************************************************************
select ts , sum(pyr) phys_rd, sum(pbr) phys_bl_rd, sum(prt) phys_rd_tm,
sum(pyw) phys_wt, sum(pbw) phys_bl_wt, sum(pwt) phys_wt_tm
from stats$file_view
group by ts order by ts;

Rem parameterKey:IO per data files
prompt **************************************************************
prompt IO per data files.
prompt **************************************************************
select name file_name, pyr phys_rd, pyw phys_wt
from stats$file_view
order by phys_rd desc, phys_wt desc;

Rem parameterKey:Sum IO operations over tablespaces
prompt **************************************************************
prompt Sum IO operations over tablespaces.
prompt **************************************************************
select
  table_space||'                                                 '
     table_space,
  sum(phys_reads) reads,  sum(phys_blks_rd) blks_read,
  sum(phys_rd_time) read_time,  sum(phys_writes) writes,
  sum(phys_blks_wr) blks_wrt,  sum(phys_wrt_tim) write_time,
  sum(megabytes_size) megabytes
 from stats$files
 group by table_space
 order by table_space;

Rem parameterKey:I/O should be spread evenly accross drives
prompt **************************************************************
prompt I/O should be spread evenly accross drives. A big difference between
prompt phys_reads and phys_blks_rd implies table scans are going on.
prompt **************************************************************
select table_space, file_name,
       phys_reads reads, phys_blks_rd blks_read, phys_rd_time read_time,
       phys_writes writes, phys_blks_wr blks_wrt, phys_wrt_tim write_time,
       megabytes_size megabytes
 from stats$files order by table_space, file_name;


Rem parameterKey:Frequency of the redo log switches
prompt **************************************************************
prompt Frequency of the redo log switches.  Log switch should
prompt occur no more often than every 25-30 minutes during peak time.
prompt If this is not the case, consider increasing redo log file size.
prompt Ex:  If redo log files switch every 5 minutes or so frequently
prompt during peak time, try to quintuple the redo log file size.
prompt **************************************************************
select sequence#, first_change#,
	 to_char(first_time, 'Mon DD, YYYY  hh24:mi:ss') time, next_change#
 from v$log_history;

prompt
prompt **************************************************************
prompt The times that bstat and estat were run.
prompt **************************************************************
prompt
select to_char(start_time, 'Mon DD, YYYY  hh24:mi:ss') start_time,
       to_char(end_time,   'Mon DD, YYYY  hh24:mi:ss') end_time
  from stats$dates;

prompt Versions
select * from v$version;


Rem ********************************************************************
Rem                 Drop Temporary Tables
Rem ********************************************************************

drop table stats$dates;

drop table stats$begin_stats;
drop table stats$end_stats;
drop table stats$stats;

drop table stats$begin_latch;
drop table stats$end_latch;
drop table stats$latches;

drop table stats$begin_roll;
drop table stats$end_roll;
drop table stats$roll;

drop table stats$begin_file;
drop table stats$end_file;
drop table stats$files;
drop view stats$file_view;

drop table stats$begin_dc;
drop table stats$end_dc;
drop table stats$dc;

drop table stats$begin_lib;
drop table stats$end_lib;
drop table stats$lib;

drop table stats$begin_event;
drop table stats$end_event;
drop table stats$event;

drop table stats$begin_bck_event;
drop table stats$end_bck_event;
drop table stats$bck_event;

drop table stats$begin_waitstat;
drop table stats$end_waitstat;
drop table stats$waitstat;

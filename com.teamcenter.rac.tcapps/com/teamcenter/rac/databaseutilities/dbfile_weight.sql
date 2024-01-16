Rem ==================================================================================================
Rem
Rem                    Copyright (c) 2000 Unigraphics Solutions Inc., An EDS Company
Rem                             Unpublished - All rights reserved
Rem
Rem ====================================================================================================
Rem
Rem file : Modified version of dbfile_weight.sql
Rem Location : com.teamcenter.rac.databaseutilities
Rem
Rem ====================================================================================================
Rem   Date      Name                    Description of Change
Rem07-Nov-2000  viswanat                Created
Rem08-Dec-2000  Lokesh Kallakrinda      Remove Control M's
Rem06-Feb-2006  Venkatesh Chitnis       de-imanization
Rem$HISTORY$
Rem==================================================================================================

prompt *************************************************************************
prompt Datafiles IO weights Information

prompt The results display the activity rate (e.g. number of reads and writes performed)
prompt of each of the data file in the database. This will help in understanding what databases files are hot.
prompt By isolating hot data file on separate disks or RAID devices, the databases performance can be improved.
prompt *************************************************************************

grant select any table to infodba;

drop view max_io_view;

create view max_io_view as
 select max(x.phyrds +x.phywrts) max_io
 from v$filestat x, sys.ts$ ts, v$datafile i, sys.file$ f
 where i.file#=f.file#
 and ts.ts#=f.ts#
 and x.file#=f.file#;

select i.name filename,
       x.phyrds + x.phywrts total_io,
       round(100*(x.phyrds+x.phywrts)/m.max_io,2) weight
 from v$filestat x,
     sys.ts$ ts,
     v$datafile i,
     sys.file$ f,
     max_io_view m
 where i.file#=f.file#
 and   ts.ts#=f.ts#
 and   x.file#=f.file#
 order by 1,3 desc;


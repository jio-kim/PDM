Rem ==================================================================================================
Rem
Rem                    Copyright (c) 2000 Unigraphics Solutions Inc., An EDS Company
Rem                             Unpublished - All rights reserved
Rem
Rem ====================================================================================================
Rem
Rem file : dbfile_details.sql
Rem Location : com.teamcenter.rac.databaseutilities
Rem
Rem ====================================================================================================
Rem   Date      Name                    Description of Change
Rem07-Nov-2000  viswanat                Created
Rem08-Dec-2000  Lokesh Kallakrinda      Remove Control M's
Rem06-Feb-2006  Venkatesh Chitnis       de-imanization
Rem$HISTORY$
Rem==================================================================================================

prompt Database Statistics

prompt The results will let the user know the percentage of free and used space
prompt in each of the data files that compose the databases tablespaces.
prompt For each data file of each tablespace in the database, the tablespace name,
prompt the data file name of this tablespace, the original size of the data file,
prompt the used space in this data file, the remaining free space in this data file,
prompt the percentage of used space, and the number of fragments in the data file are also displayed.

prompt *************************************************************************
prompt Details of data files

select name, bytes from v$datafile;

prompt *************************************************************************
prompt Details of control files

select name from v$controlfile;

prompt *************************************************************************
prompt Details of log files

select member, group# from v$logfile;

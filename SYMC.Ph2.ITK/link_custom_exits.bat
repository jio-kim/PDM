@rem = '
@echo off
setlocal
if "%MSDEV_HOME%"=="" goto nomsdev
set VCVARS_FILE="%MSDEV_HOME%\Auxiliary\Build\vcvarsall.bat"
if not exist %VCVARS_FILE% goto nomsdev

REM Set compiler bit
set COMPILER=x64

call %VCVARS_FILE% %COMPILER%
%TC_ROOT%\perl\bin\perl %0.bat %1 %2 %3 %4 %5 %6 %7 %8 %9
endlocal
goto :EOF
:nomsdev
echo MSDEV_HOME is either not defined or not defined to the correct location.
echo It must be defined to point to the root of the Visual Studio compiler
echo installation.  For example:
echo set MSDEV_HOME=c:\Program Files\Microsoft Visual Studio 15.0\VC
goto :EOF
rem ';

################################################################################
# REQUIRES the following be set:
#
# The root of the Microsoft Visual C++ directory must be set in the MSDEV_HOME
# environment variable.
#
# Example : set MSDEV_HOME=c:\Program Files\Microsoft Visual Studio 15.0\VC
#
################################################################################

sub Tell_Help
{
  print <<EOF

  This script will build the custom_exits shared library for Windows

  Usage: link_custom_exits [-h] for help

  Please notice:
  * The following environment variables should be set properly :
     TC_LIBRARY   library dir of TC.

  How to build my_custom.dll
  1. Create work directory.
     (ex: mkdir my_custom, and cd my_custom)
  2. Compile your objects or swap to your objects
  3. link dynamic link library
     (ex: link_custom_exits my_custom)
  4. Copy dll and PDB file to TC DLL directory.
     (ex: copy my_custom.dll %TC_ROOT%\\bin\\.
               my_custom.pdb %TC_ROOT%\\bin\\.
     )

  Once the new shareable library is built, it should be copied back to %TC_ROOT%\\bin
EOF
}

$errEnvVar = 0;

if (! $ENV{TC_LIBRARY}) {
    print "TC_LIBRARY variable must be set to run link_custom_exits.\n";
    print "Set the library directory of TC.\n";
    print "Example : set TC_LIBRARY=c:\\tc0500\\lib\\n";
    $errEnvVar = 1;
}

$ARGC=@ARGV;

################################
# Checking at least one argument
################################

if ($ARGV[0] =~ /^(-h|\/h)/ || $errEnvVar == 1) {
  &Tell_Help;
  exit 1;
}

if ($ARGV[0] eq "")
{
    print "The Custom Library to be built has to be specified.\n";
    print "Example : if my_custom has to be built, my_custom should be arg 1.\n";
    exit 1;
}

$LIBNAME = $ARGV[0];

$DLLFLAGS = " -debug" .
            " -nologo" .
            " -subsystem:console" .
            " -MACHINE:X64";

###############################################################################
# Platform Independent
###############################################################################
$LINK = "link";

$SYSLIBS = "wsock32.lib " .
           "advapi32.lib " .
           "msvcrt.lib " .
           "oldnames.lib " .
           "kernel32.lib " .
           "winmm.lib";

print "Creating the $LIBNAME dynamic link library\n";

@objectFiles = <*.obj>;
###############################################################################
# Link up
###############################################################################

$link_line="$LINK $DLLFLAGS ".
           "-dll ".
           "-out:$LIBNAME.dll ".
           join(" ",@objectFiles)." ".
           "$SYSLIBS /ignore:4199 delayimp.lib ".
           "$ENV{TC_LIBRARY}\\libuser_exits.lib /delayload:libuser_exits.dll ".
           "$ENV{TC_LIBRARY}\\libict.lib /delayload:libict.dll ".
           "$ENV{TC_LIBRARY}\\libsyss.lib ".
           "$ENV{TC_LIBRARY}\\libpom.lib ".
           "$ENV{TC_LIBRARY}\\libae.lib /delayload:libae.dll ".
           "$ENV{TC_LIBRARY}\\libappr.lib /delayload:libappr.dll ".
           "$ENV{TC_LIBRARY}\\libarchive.lib /delayload:libarchive.dll ".
           "$ENV{TC_LIBRARY}\\libbackup.lib /delayload:libbackup.dll ".
           "$ENV{TC_LIBRARY}\\libbom.lib /delayload:libbom.dll ".
           "$ENV{TC_LIBRARY}\\libcfm.lib /delayload:libcfm.dll ".
           "$ENV{TC_LIBRARY}\\libcxpom.lib ".
           "$ENV{TC_LIBRARY}\\libdmi.lib /delayload:libdmi.dll ".
           "$ENV{TC_LIBRARY}\\libecm.lib /delayload:libecm.dll ".
           "$ENV{TC_LIBRARY}\\libeint.lib /delayload:libeint.dll ".
           "$ENV{TC_LIBRARY}\\libepm.lib /delayload:libepm.dll ".
           "$ENV{TC_LIBRARY}\\libfclasses.lib ".
           "$ENV{TC_LIBRARY}\\libform.lib /delayload:libform.dll ".
           "$ENV{TC_LIBRARY}\\libgrm.lib /delayload:libgrm.dll ".
           "$ENV{TC_LIBRARY}\\libics.lib /delayload:libics.dll ".
           "$ENV{TC_LIBRARY}\\libtcinit.lib ".
           "$ENV{TC_LIBRARY}\\libtc.lib ".
           "$ENV{TC_LIBRARY}\\libtccore.lib /delayload:libtccore.dll ".
           "$ENV{TC_LIBRARY}\\libtctrushape.lib /delayload:libtctrushape.dll ".
           "$ENV{TC_LIBRARY}\\libinternal_exits.lib /delayload:libinternal_exits.dll ".
           "$ENV{TC_LIBRARY}\\libvalidation.lib /delayload:libvalidation.dll ".
           "$ENV{TC_LIBRARY}\\libcondvalidation.lib /delayload:libcondvalidation.dll ".
           "$ENV{TC_LIBRARY}\\libitk.lib /delayload:libitk.dll ".
           "$ENV{TC_LIBRARY}\\liblov.lib /delayload:liblov.dll ".
           "$ENV{TC_LIBRARY}\\libme.lib /delayload:libme.dll ".
           "$ENV{TC_LIBRARY}\\libmechatronics.lib /delayload:libmechatronics.dll ".
           "$ENV{TC_LIBRARY}\\libmgc.lib /delayload:libmgc.dll ".
           "$ENV{TC_LIBRARY}\\libobjio.lib /delayload:libobjio.dll ".
           "$ENV{TC_LIBRARY}\\libpie.lib /delayload:libpie.dll ".
           "$ENV{TC_LIBRARY}\\libai.lib /delayload:libai.dll ".
           "$ENV{TC_LIBRARY}\\libtrv.lib /delayload:libtrv.dll ".
           "$ENV{TC_LIBRARY}\\libtie.lib /delayload:libtie.dll ".
           "$ENV{TC_LIBRARY}\\libproperty.lib /delayload:libproperty.dll ".
           "$ENV{TC_LIBRARY}\\libwproxy.lib /delayload:libwproxy.dll ".
           "$ENV{TC_LIBRARY}\\libps.lib /delayload:libps.dll ".
           "$ENV{TC_LIBRARY}\\libpublication.lib /delayload:libpublication.dll ".
           "$ENV{TC_LIBRARY}\\libqry.lib /delayload:libqry.dll ".
           "$ENV{TC_LIBRARY}\\libres.lib /delayload:libres.dll ".
           "$ENV{TC_LIBRARY}\\librdv.lib ".
           "$ENV{TC_LIBRARY}\\libsa.lib ".
           "$ENV{TC_LIBRARY}\\libschmgt_bridge.lib ".
           "$ENV{TC_LIBRARY}\\libschmgt.lib ".
           "$ENV{TC_LIBRARY}\\libss.lib ".
           "$ENV{TC_LIBRARY}\\libsub_mgr.lib /delayload:libsub_mgr.dll ".
           "$ENV{TC_LIBRARY}\\libtccoreext.lib /delayload:libtccoreext.dll ".
           "$ENV{TC_LIBRARY}\\libtextsrv.lib ".
           "$ENV{TC_LIBRARY}\\libnxmgr_im.lib /delayload:libnxmgr_im.dll ".
           "$ENV{TC_LIBRARY}\\libfoundationbase.lib /delayload:libfoundationbase.dll ".
           "$ENV{TC_LIBRARY}\\libmld.lib /delayload:libmld.dll ".
           "$ENV{TC_LIBRARY}\\libbase_utils.lib ".
           "$ENV{TC_LIBRARY}\\xerces322_X_19.lib ";

system $link_line;

print "\n$link_line\n";


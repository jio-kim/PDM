rem 64bit로 컴파일해야 하는 경우에는 compile 옵션에 -64bit 를 추가해서 컴파일해야 한다.
rem 특히, 일반 도스 프롬프트가 아닌 visual studio의 64bit용 프롬프트를 실행하여 아래 compile과 link를 실행해야 정상적으로 컴파일이 된다.

rem 환경변수 설정
set DEV_HOME=C:\Siemens\ITK\SYMC.Ph2.ITK
set MSDEV_HOME=C:\Program Files (x86)\Microsoft Visual Studio\2019\Community\VC
set TC_ROOT=C:\Siemens\Teamcenter13
set TC_DATA=C:\Siemens\tcdata13
set USER_INCLUDE=C:\\Siemens\\ITK\SYMC.Ph2.ITK\\Source\\Include
call %TC_DATA%\tc_profilevars

rem 소스 컴파일
rem %TC_ROOT%\sample\compile -64bit -DIPLIB=libuser_exits %DEV_HOME%\Source\Common\symc_common.c %DEV_HOME%\Source\UserExits\symc_register_callbacks.c
%TC_ROOT%\sample\compile -DIPLIB=libuser_exits  %DEV_HOME%\Source\common\symc_common.c %DEV_HOME%\Source\UserExits\symc_register_callbacks.c

rem 실제 DLL 파일 생성
%TC_ROOT%\sample\link_custom_exits libsymcmbom

rem 64bit�� �������ؾ� �ϴ� ��쿡�� compile �ɼǿ� -64bit �� �߰��ؼ� �������ؾ� �Ѵ�.
rem Ư��, �Ϲ� ���� ������Ʈ�� �ƴ� visual studio�� 64bit�� ������Ʈ�� �����Ͽ� �Ʒ� compile�� link�� �����ؾ� ���������� �������� �ȴ�.

rem ȯ�溯�� ����
set DEV_HOME=C:\Siemens\ITK\SYMC.Ph2.ITK
set MSDEV_HOME=C:\Program Files (x86)\Microsoft Visual Studio\2019\Community\VC
set TC_ROOT=C:\Siemens\Teamcenter13
set TC_DATA=C:\Siemens\tcdata13
set USER_INCLUDE=C:\\Siemens\\ITK\SYMC.Ph2.ITK\\Source\\Include
call %TC_DATA%\tc_profilevars

rem �ҽ� ������
rem %TC_ROOT%\sample\compile -64bit -DIPLIB=libuser_exits %DEV_HOME%\Source\Common\symc_common.c %DEV_HOME%\Source\UserExits\symc_register_callbacks.c
%TC_ROOT%\sample\compile -DIPLIB=libuser_exits  %DEV_HOME%\Source\common\symc_common.c %DEV_HOME%\Source\UserExits\symc_register_callbacks.c

rem ���� DLL ���� ����
%TC_ROOT%\sample\link_custom_exits libsymcmbom

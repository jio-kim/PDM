/**
 * MRO Ref BOM �Ӽ� �ϰ� Upload Dialog
 * ����Ŭ����(BWXLSImpDialog)��  ������ ����� ����( ���� �߰� ���߽� ���� ����� Override�Ͽ� ���� �Ͽ��� ��)
 * �۾�Option�� dialogs_locale_ko_KR.properties�� ���� �Ǿ� ����
 */
package com.kgm.common.bundlework.imp;

import org.eclipse.swt.widgets.Shell;

import com.kgm.common.bundlework.BWXLSImpDialog;

public class BWCommonImpDialog extends BWXLSImpDialog
{
  
  public BWCommonImpDialog(Shell parent, int style)
  {
    super(parent, style, BWCommonImpDialog.class);
  }
  
  @Override
  public void dialogOpen()
  {
      super.dialogOpen();
      super.enableOptionButton();
  }

}

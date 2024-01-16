/**
 * MRO Ref BOM 속성 일괄 Upload Dialog
 * 상위클래스(BWXLSImpDialog)와  상이한 기능은 없음( 차후 추가 개발시 세부 기능을 Override하여 구현 하여야 함)
 * 작업Option은 dialogs_locale_ko_KR.properties에 정의 되어 있음
 */
package com.ssangyong.common.bundlework.imp;

import org.eclipse.swt.widgets.Shell;

import com.ssangyong.common.bundlework.BWXLSImpDialog;

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

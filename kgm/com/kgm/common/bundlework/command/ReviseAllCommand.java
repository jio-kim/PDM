package com.kgm.common.bundlework.command;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;

import com.kgm.common.bundlework.imp.BWReviseAllDialog;
import com.kgm.common.utils.CustomUtil;
import com.teamcenter.rac.aif.AbstractAIFCommand;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.util.MessageBox;

public class ReviseAllCommand extends AbstractAIFCommand
{

  public ReviseAllCommand() throws TCException
  {
    /** Dialog 호출. */
    Shell shell = AIFUtility.getActiveDesktop().getShell();
    final InterfaceAIFComponent[] coms = CustomUtil.getTargets();

    // PSE에서만 가능한 기능이며 Command Supression에서 설정해야함.
    // 현재 선택한 아이템은 Product Type이어야 한다.
    if (coms != null && coms.length > 0)
    {
      TCComponentItemRevision targetRevision = null;
      if( coms[0] instanceof TCComponentItemRevision  )
      {
        targetRevision = (TCComponentItemRevision) coms[0];
      }
      else if( coms[0] instanceof TCComponentItem)
      {
        targetRevision = ((TCComponentItem) coms[0]).getLatestItemRevision();
      }
      else if( coms[0] instanceof TCComponentBOMLine)
      {
        targetRevision = ((TCComponentBOMLine) coms[0]).getItemRevision();
      }      
      
      if( targetRevision != null )
      {
        BWReviseAllDialog dialog = new BWReviseAllDialog(shell, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL, targetRevision);
        dialog.dialogOpen();
      }

      else
      {
        MessageBox.post(AIFUtility.getActiveDesktop().getShell(), "Only Item/ItemRevision/BomLine Type is Available", "INFORMATION", MessageBox.WARNING);
        return;
      }

    }
  }
}

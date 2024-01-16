package com.ssangyong.commands.showon;

import org.eclipse.swt.widgets.Shell;

import com.ssangyong.common.utils.SYMTcUtil;
import com.teamcenter.rac.aif.AbstractAIFCommand;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.util.MessageBox;

public class ViewShowOnCommand extends AbstractAIFCommand {


	/**
	 * ShowOn Part의 2D Dataset 조회
	 * 
	 * Release 후에 수정 하므로 별도 Object로 관리
	 */
	public ViewShowOnCommand() {
		
		/** Dialog 호출. */
		Shell shell = AIFUtility.getActiveDesktop().getShell();
		
		
		try
		{
			// Release 후에 수정 하므로 별도 Object로 관리
			InterfaceAIFComponent comp = AIFUtility.getCurrentApplication().getTargetComponent();
			if (comp != null && comp instanceof TCComponentItemRevision)
			{
				TCComponentItemRevision targetRevision = (TCComponentItemRevision) comp;
				
				// Shown On No.(TypedReference)
				TCComponent showComp = targetRevision.getReferenceProperty("s7_SHOWN_PART_NO");
				if (showComp != null && showComp instanceof TCComponentItem )
				{
					TCComponentItem showOnItem = (TCComponentItem)showComp;
					TCComponentItemRevision showOnRev = SYMTcUtil.getLatestReleasedRevision(showOnItem);
					
					if( showOnRev == null )
					{
						MessageBox.post(AIFUtility.getActiveDesktop().getShell(), "ShowOn Part에 Release된 Revision이 존재하지 않습니다.", "INFORMATION", MessageBox.ERROR);
						return;
					}
					
					ViewShowOnDialog partMasterDialog = new ViewShowOnDialog(shell, showOnRev);
					partMasterDialog.open();
					
				}
				else
				{
					MessageBox.post(AIFUtility.getActiveDesktop().getShell(), "참조된 ShowOn Part가 없습니다.", "INFORMATION", MessageBox.ERROR);
					return;
				}
			
			
			}
			else
			{
				MessageBox.post(AIFUtility.getActiveDesktop().getShell(), "Item Revision 선택 후 실행해 주세요.", "INFORMATION", MessageBox.ERROR);
				return;
			}
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		
		

	}
}

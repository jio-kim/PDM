package com.kgm.commands.showon;

import org.eclipse.swt.widgets.Shell;

import com.kgm.common.utils.SYMTcUtil;
import com.teamcenter.rac.aif.AbstractAIFCommand;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.util.MessageBox;

public class ViewShowOnCommand extends AbstractAIFCommand {


	/**
	 * ShowOn Part�� 2D Dataset ��ȸ
	 * 
	 * Release �Ŀ� ���� �ϹǷ� ���� Object�� ����
	 */
	public ViewShowOnCommand() {
		
		/** Dialog ȣ��. */
		Shell shell = AIFUtility.getActiveDesktop().getShell();
		
		
		try
		{
			// Release �Ŀ� ���� �ϹǷ� ���� Object�� ����
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
						MessageBox.post(AIFUtility.getActiveDesktop().getShell(), "ShowOn Part�� Release�� Revision�� �������� �ʽ��ϴ�.", "INFORMATION", MessageBox.ERROR);
						return;
					}
					
					ViewShowOnDialog partMasterDialog = new ViewShowOnDialog(shell, showOnRev);
					partMasterDialog.open();
					
				}
				else
				{
					MessageBox.post(AIFUtility.getActiveDesktop().getShell(), "������ ShowOn Part�� �����ϴ�.", "INFORMATION", MessageBox.ERROR);
					return;
				}
			
			
			}
			else
			{
				MessageBox.post(AIFUtility.getActiveDesktop().getShell(), "Item Revision ���� �� ������ �ּ���.", "INFORMATION", MessageBox.ERROR);
				return;
			}
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		
		

	}
}

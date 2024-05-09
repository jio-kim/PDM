package com.kgm.commands.revise;

import com.kgm.common.SYMCClass;
import com.kgm.common.utils.CustomUtil;
import com.kgm.common.utils.SYMTcUtil;
import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.aif.AbstractAIFCommand;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentRevisionRule;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;

public class SYMCReviseCommand extends AbstractAIFCommand
{

	/**
	 * Revise Command
	 */
	public SYMCReviseCommand()
	{

		try
		{
			if (this.validationCheck())
			{
				AIFDesktop activeDesktop = AIFUtility.getActiveDesktop();
				SYMCReviseDialog dialog = new SYMCReviseDialog(activeDesktop);
				dialog.setModal(false);
				setRunnable(dialog);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			MessageBox.post(e.getMessage(), "Error", MessageBox.ERROR);
		}
	}

	/**
	 * Vehicle Part, Function Master �� Revise �����ϴ�
	 * 
	 * @return
	 * @throws Exception
	 */
	private boolean validationCheck() throws Exception
	{
		Registry registry = Registry.getRegistry(this);
		InterfaceAIFComponent[] targetComponents = AIFUtility.getCurrentApplication().getTargetComponents();
		TCComponentItemRevision itemRevision;

		if (targetComponents.length == 0)
		{
			MessageBox.post(registry.getString("ReviseDialog.MESSAGE.NoSelectedItem"), registry.getString("ReviseDialog.MESSAGE.Title.Warning"), MessageBox.INFORMATION); // Revise�� ����� ���õ��� �ʾҽ��ϴ�.
			return false;
		}

		/** PSE �� ��� */
		if (targetComponents[0] instanceof TCComponentBOMLine)
		{
			itemRevision = ((TCComponentBOMLine) targetComponents[0]).getItemRevision();
			// Vehicle Part, Function Master �� Revise �����մϴ�
			if (!(itemRevision.getType().equals(SYMCClass.S7_VEHPARTREVISIONTYPE) || itemRevision.getType().equals(SYMCClass.S7_FNCMASTPARTREVISIONTYPE) || itemRevision.getType().equals(SYMCClass.S7_PRODUCTPARTREVISIONTYPE)))
			{
				MessageBox.post("Revise can only Vehicle Part Revision, Function Master.", registry.getString("ReviseDialog.MESSAGE.Title.Warning"), MessageBox.INFORMATION);
				return false;
			}
			// Latest Relase Revision�� ������ �ּ���
			if (!itemRevision.getItem().getLatestItemRevision().equals(itemRevision))
			{
				MessageBox.post(registry.getString("ReviseDialog.MESSAGE.IsNotLastItemRevision"), registry.getString("ReviseDialog.MESSAGE.Title.Warning"), MessageBox.INFORMATION);
				return false;
			}

            // ����� ������ �������� Released ���°� �ƴմϴ�
            if (!CustomUtil.isReleased(itemRevision))
            {
                MessageBox.post("Selected Revision Is Not Release Status", registry.getString("ReviseDialog.MESSAGE.Title.Warning"), MessageBox.INFORMATION);
                return false;
            }
		}
		/** My Teamcenter �� ��� */
		else
		{
			itemRevision = ((TCComponentItemRevision) targetComponents[0]);
			// �ֽ� �������� �ƴϸ� return
			if (!itemRevision.getItem().getLatestItemRevision().equals(itemRevision))
			{
				MessageBox.post(registry.getString("ReviseDialog.MESSAGE.IsNotLastItemRevision"), registry.getString("ReviseDialog.MESSAGE.Title.Warning"), MessageBox.INFORMATION);
				return false;
			}

			if (!(targetComponents[0] instanceof TCComponentItemRevision))
			{
				MessageBox.post(registry.getString("ReviseDialog.MESSAGE.IsNotItemRevision"), registry.getString("ReviseDialog.MESSAGE.Title.Warning"), MessageBox.INFORMATION); //����� ������ �������� �ƴմϴ�.
				return false;
			}
			else if (targetComponents[0] instanceof TCComponentItemRevision)
			{
				if (!(targetComponents[0].getType().equals(SYMCClass.S7_VEHPARTREVISIONTYPE) || targetComponents[0].getType().equals(SYMCClass.S7_FNCMASTPARTREVISIONTYPE) || targetComponents[0].getType().equals(SYMCClass.S7_PRODUCTPARTREVISIONTYPE)))
				{
					MessageBox.post("REVISE of the selected revision is not supported.", registry.getString("ReviseDialog.MESSAGE.Title.Warning"), MessageBox.INFORMATION);
					return false;
				}

				// ����� ������ �������� Released ���°� �ƴմϴ�
				if (!CustomUtil.isReleased((TCComponentItemRevision) targetComponents[0]))
				{
					MessageBox.post("Selected Revision Is Not Release Status", registry.getString("ReviseDialog.MESSAGE.Title.Warning"), MessageBox.INFORMATION);
					return false;
				}
			}
		}
		
		//[SR190821-022][CSH]Function�� Item ID�� ����ġ�ϴ� FMP ���� ����
		if(itemRevision.getType().equals(SYMCClass.S7_FNCMASTPARTREVISIONTYPE)){
			String fmasterId = itemRevision.getProperty("item_id");
			String functionId = "";

			TCComponentRevisionRule revRule = SYMTcUtil.getRevisionRule(itemRevision.getSession(), "Latest Working");
			TCComponent[] imanComps = itemRevision.whereUsed(TCComponent.WHERE_USED_CONFIGURED, revRule);
			TCComponentItemRevision fncRev = null;
			for (int j = 0; j < imanComps.length; j++) {
				if( SYMCClass.S7_FNCPARTREVISIONTYPE.equals( imanComps[j].getType())){
					fncRev = (TCComponentItemRevision)imanComps[j];
					break;
				}
			}

			if(fncRev != null){
				functionId = fncRev.getProperty("item_id");
			}

			if(functionId.equals("")){
				MessageBox.post("Function�� �������Ͽ� ������ �Ұ��� FMP�Դϴ�.\n����������� �����ϼ���.", registry.getString("ReviseDialog.MESSAGE.Title.Warning"), MessageBox.INFORMATION);
				return false;
			}

			if(!fmasterId.substring(1,fmasterId.length()-1).equals(functionId.substring(1))) {
				MessageBox.post("Function�� Item ID�� ����ġ�Ͽ� Revise �Ұ��� FMP�Դϴ�.\n����������� �����ϼ���.", registry.getString("ReviseDialog.MESSAGE.Title.Warning"), MessageBox.INFORMATION);
				return false;
			}
		}

		return true;
	}

}

package com.symc.plm.rac.prebom.prebom.commands;

import org.eclipse.swt.widgets.Shell;

import com.kgm.common.utils.CustomUtil;
import com.symc.plm.rac.prebom.common.PropertyConstant;
import com.symc.plm.rac.prebom.prebom.dialog.updateweight.UpdateEstWeightDialog;
import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.aif.AbstractAIFCommand;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.util.MessageBox;

/**
 * [20180213][ljg] �ý��� �ڵ� ������ �������� bomline������ �̵�
 */
public class UpdateEstWeightCommand extends AbstractAIFCommand
{
	private TCComponentBOMLine targetBOMLine;

	/**
	 * Actual Weight Update Command
	 * 
	 * Release �Ŀ� ���� �ϹǷ� ���� Object�� ����
	 */
	public UpdateEstWeightCommand()
	{
		try
		{
			if (targetValidate())
			{
				/** Dialog ȣ��. */
				Shell shell = AIFUtility.getActiveDesktop().getShell();
				UpdateEstWeightDialog partMasterDialog = new UpdateEstWeightDialog(shell, targetBOMLine);
				partMasterDialog.open();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private boolean targetValidate() throws Exception
	{
		InterfaceAIFComponent targetCompoment = AIFUtility.getCurrentApplication().getTargetComponent();
		AIFDesktop curDesktop = AIFUtility.getActiveDesktop();
		String projectCode;
		String systemCode;

		//        if (targetCompoment instanceof TCComponentItemRevision)
		//        {
		//            if (! ((TCComponentItemRevision) targetCompoment).getItem().getLatestItemRevision().equals(targetCompoment))
		//            {
		//                MessageBox.post(curDesktop, "�߷������� ������ ������ �������� �����մϴ�.", "Ȯ��", MessageBox.INFORMATION);
		//                return false;
		//            }
		//            if (! CustomUtil.isReleased((TCComponentItemRevision) targetCompoment))
		//            {
		//                if (CustomUtil.isWorkingStatus((TCComponentItemRevision) targetCompoment))
		//                {
		//                    MessageBox.post(curDesktop, "�۾����� �׸��� ����â���� ���� �����Ͻñ� �ٶ��ϴ�.", "Ȯ��", MessageBox.INFORMATION);
		//                }
		//                else
		//                {
		//                    MessageBox.post(curDesktop, "�����Ϸ���� ���� �׸��� ������ �Ұ����մϴ�.", "Ȯ��", MessageBox.INFORMATION);
		//                }
		//                return false;
		//            }
		//
		//            targetRevision = (TCComponentItemRevision) targetCompoment;
		//            
		//        }
		//        else 
		if (targetCompoment instanceof TCComponentBOMLine){
			if (! CustomUtil.isReleased(((TCComponentBOMLine) targetCompoment).getItem().getLatestItemRevision()))
			{
				if (CustomUtil.isWorkingStatus(((TCComponentBOMLine) targetCompoment).getItem().getLatestItemRevision()))
				{
					MessageBox.post(curDesktop, "�۾����� �׸��� ����â���� ���� �����Ͻñ� �ٶ��ϴ�.", "Ȯ��", MessageBox.INFORMATION);
				}
				else
				{
					MessageBox.post(curDesktop, "�����Ϸ���� ���� �׸��� ������ �Ұ����մϴ�.", "Ȯ��", MessageBox.INFORMATION);
				}
				return false;
			}

			targetBOMLine = (TCComponentBOMLine)targetCompoment;
		}
//		else if (targetCompoment instanceof TCComponentItem)
//		{
//			if (! CustomUtil.isReleased(((TCComponentItem) targetCompoment).getLatestItemRevision()))
//			{
//				MessageBox.post(curDesktop, "�߷������� ������ ������ �������� ����Ϸ�� Pre Vehicle Part�� �����մϴ�.", "Ȯ��", MessageBox.INFORMATION);
//				return false;
//			}
//
//			targetBOMLine = ((TCComponentItem) targetCompoment).getLatestItemRevision();
//		}
		else
		{
			MessageBox.post(curDesktop, "�߷������� ������ Pre Vehicle Part(BOMLine)�� ������ �ּ���.", "Ȯ��", MessageBox.INFORMATION);
			return false;
		}

		projectCode = targetBOMLine.getProperty(PropertyConstant.ATTR_NAME_PROJCODE);
		systemCode = targetBOMLine.getProperty(PropertyConstant.ATTR_NAME_BL_BUDGETCODE);

		if (projectCode == null || projectCode.trim().length() == 0)
		{
			MessageBox.post(curDesktop, "Project Code ���� �����ϴ�. �����ڿ��� ������ �ּ���.", "Ȯ��", MessageBox.INFORMATION);
			return false;
		}
		if (systemCode == null || systemCode.trim().length() == 0)
		{
			MessageBox.post(curDesktop, "System Code ���� �����ϴ�. �����ڿ��� ������ �ּ���.", "Ȯ��", MessageBox.INFORMATION);
			return false;
		}

		return true;
	}
}

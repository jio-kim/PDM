package com.symc.plm.rac.prebom.prebom.commands;

import org.eclipse.swt.widgets.Shell;

import com.ssangyong.common.utils.CustomUtil;
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
 * [20180213][ljg] 시스템 코드 리비전 정보에서 bomline정보로 이동
 */
public class UpdateEstWeightCommand extends AbstractAIFCommand
{
	private TCComponentBOMLine targetBOMLine;

	/**
	 * Actual Weight Update Command
	 * 
	 * Release 후에 수정 하므로 별도 Object로 관리
	 */
	public UpdateEstWeightCommand()
	{
		try
		{
			if (targetValidate())
			{
				/** Dialog 호출. */
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
		//                MessageBox.post(curDesktop, "중량정보를 수정은 마지막 리비전만 가능합니다.", "확인", MessageBox.INFORMATION);
		//                return false;
		//            }
		//            if (! CustomUtil.isReleased((TCComponentItemRevision) targetCompoment))
		//            {
		//                if (CustomUtil.isWorkingStatus((TCComponentItemRevision) targetCompoment))
		//                {
		//                    MessageBox.post(curDesktop, "작업중인 항목은 정보창에서 직접 수정하시기 바랍니다.", "확인", MessageBox.INFORMATION);
		//                }
		//                else
		//                {
		//                    MessageBox.post(curDesktop, "결제완료되지 않은 항목은 수정이 불가능합니다.", "확인", MessageBox.INFORMATION);
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
					MessageBox.post(curDesktop, "작업중인 항목은 정보창에서 직접 수정하시기 바랍니다.", "확인", MessageBox.INFORMATION);
				}
				else
				{
					MessageBox.post(curDesktop, "결제완료되지 않은 항목은 수정이 불가능합니다.", "확인", MessageBox.INFORMATION);
				}
				return false;
			}

			targetBOMLine = (TCComponentBOMLine)targetCompoment;
		}
//		else if (targetCompoment instanceof TCComponentItem)
//		{
//			if (! CustomUtil.isReleased(((TCComponentItem) targetCompoment).getLatestItemRevision()))
//			{
//				MessageBox.post(curDesktop, "중량정보를 수정은 마지막 리비전이 결재완료된 Pre Vehicle Part만 가능합니다.", "확인", MessageBox.INFORMATION);
//				return false;
//			}
//
//			targetBOMLine = ((TCComponentItem) targetCompoment).getLatestItemRevision();
//		}
		else
		{
			MessageBox.post(curDesktop, "중량정보를 수정할 Pre Vehicle Part(BOMLine)를 선택해 주세요.", "확인", MessageBox.INFORMATION);
			return false;
		}

		projectCode = targetBOMLine.getProperty(PropertyConstant.ATTR_NAME_PROJCODE);
		systemCode = targetBOMLine.getProperty(PropertyConstant.ATTR_NAME_BL_BUDGETCODE);

		if (projectCode == null || projectCode.trim().length() == 0)
		{
			MessageBox.post(curDesktop, "Project Code 값이 없습니다. 관리자에게 문의해 주세요.", "확인", MessageBox.INFORMATION);
			return false;
		}
		if (systemCode == null || systemCode.trim().length() == 0)
		{
			MessageBox.post(curDesktop, "System Code 값이 없습니다. 관리자에게 문의해 주세요.", "확인", MessageBox.INFORMATION);
			return false;
		}

		return true;
	}
}

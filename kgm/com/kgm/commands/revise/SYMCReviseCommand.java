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
	 * Vehicle Part, Function Master 만 Revise 가능하다
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
			MessageBox.post(registry.getString("ReviseDialog.MESSAGE.NoSelectedItem"), registry.getString("ReviseDialog.MESSAGE.Title.Warning"), MessageBox.INFORMATION); // Revise할 대상이 선택되지 않았습니다.
			return false;
		}

		/** PSE 일 경우 */
		if (targetComponents[0] instanceof TCComponentBOMLine)
		{
			itemRevision = ((TCComponentBOMLine) targetComponents[0]).getItemRevision();
			// Vehicle Part, Function Master 만 Revise 가능합니다
			if (!(itemRevision.getType().equals(SYMCClass.S7_VEHPARTREVISIONTYPE) || itemRevision.getType().equals(SYMCClass.S7_FNCMASTPARTREVISIONTYPE) || itemRevision.getType().equals(SYMCClass.S7_PRODUCTPARTREVISIONTYPE)))
			{
				MessageBox.post("Revise can only Vehicle Part Revision, Function Master.", registry.getString("ReviseDialog.MESSAGE.Title.Warning"), MessageBox.INFORMATION);
				return false;
			}
			// Latest Relase Revision을 선택해 주세요
			if (!itemRevision.getItem().getLatestItemRevision().equals(itemRevision))
			{
				MessageBox.post(registry.getString("ReviseDialog.MESSAGE.IsNotLastItemRevision"), registry.getString("ReviseDialog.MESSAGE.Title.Warning"), MessageBox.INFORMATION);
				return false;
			}

            // 대상이 아이템 리비전이 Released 상태가 아닙니다
            if (!CustomUtil.isReleased(itemRevision))
            {
                MessageBox.post("Selected Revision Is Not Release Status", registry.getString("ReviseDialog.MESSAGE.Title.Warning"), MessageBox.INFORMATION);
                return false;
            }
		}
		/** My Teamcenter 일 경우 */
		else
		{
			itemRevision = ((TCComponentItemRevision) targetComponents[0]);
			// 최신 리비전이 아니면 return
			if (!itemRevision.getItem().getLatestItemRevision().equals(itemRevision))
			{
				MessageBox.post(registry.getString("ReviseDialog.MESSAGE.IsNotLastItemRevision"), registry.getString("ReviseDialog.MESSAGE.Title.Warning"), MessageBox.INFORMATION);
				return false;
			}

			if (!(targetComponents[0] instanceof TCComponentItemRevision))
			{
				MessageBox.post(registry.getString("ReviseDialog.MESSAGE.IsNotItemRevision"), registry.getString("ReviseDialog.MESSAGE.Title.Warning"), MessageBox.INFORMATION); //대상이 아이템 리비전이 아닙니다.
				return false;
			}
			else if (targetComponents[0] instanceof TCComponentItemRevision)
			{
				if (!(targetComponents[0].getType().equals(SYMCClass.S7_VEHPARTREVISIONTYPE) || targetComponents[0].getType().equals(SYMCClass.S7_FNCMASTPARTREVISIONTYPE) || targetComponents[0].getType().equals(SYMCClass.S7_PRODUCTPARTREVISIONTYPE)))
				{
					MessageBox.post("REVISE of the selected revision is not supported.", registry.getString("ReviseDialog.MESSAGE.Title.Warning"), MessageBox.INFORMATION);
					return false;
				}

				// 대상이 아이템 리비전이 Released 상태가 아닙니다
				if (!CustomUtil.isReleased((TCComponentItemRevision) targetComponents[0]))
				{
					MessageBox.post("Selected Revision Is Not Release Status", registry.getString("ReviseDialog.MESSAGE.Title.Warning"), MessageBox.INFORMATION);
					return false;
				}
			}
		}
		
		//[SR190821-022][CSH]Function과 Item ID가 불일치하는 FMP 개정 방지
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
				MessageBox.post("Function이 미존재하여 개정이 불가한 FMP입니다.\n기술관리팀에 문의하세요.", registry.getString("ReviseDialog.MESSAGE.Title.Warning"), MessageBox.INFORMATION);
				return false;
			}

			if(!fmasterId.substring(1,fmasterId.length()-1).equals(functionId.substring(1))) {
				MessageBox.post("Function과 Item ID가 불일치하여 Revise 불가한 FMP입니다.\n기술관리팀에 문의하세요.", registry.getString("ReviseDialog.MESSAGE.Title.Warning"), MessageBox.INFORMATION);
				return false;
			}
		}

		return true;
	}

}

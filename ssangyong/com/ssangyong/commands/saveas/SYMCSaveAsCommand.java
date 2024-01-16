package com.ssangyong.commands.saveas;

import org.eclipse.swt.widgets.Shell;

import com.ssangyong.commands.partmaster.SYMCPartManagerDialog;
import com.ssangyong.commands.partmaster.SYMCProductManagerDialog;
import com.ssangyong.common.SYMCClass;
import com.teamcenter.rac.aif.AbstractAIFCommand;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.util.MessageBox;

public class SYMCSaveAsCommand extends AbstractAIFCommand
{

	/**
	 * 생성자.
	 * 
	 * @copyright : S-PALM
	 * @author : 권상기
	 * @since : 2012. 12. 18.
	 */
	public SYMCSaveAsCommand()
	{
		InterfaceAIFComponent[] comps = AIFUtility.getCurrentApplication().getTargetComponents();

		if (comps == null || comps.length == 0)
		{
			MessageBox.post("다른 이름으로 저장 할 대상(Item, ItemRevision)을 하나 선택 한 후 실행 하십시오.", "알림", MessageBox.INFORMATION);
			return;
		}
		if (comps.length > 1)
		{
			MessageBox.post("다른 이름으로 저장 할 대상(Item, ItemRevision)을 하나만 선택 하신 후 실행 하십시오.", "알림.", MessageBox.INFORMATION);
			return;
		}

		TCComponent comp = (TCComponent) comps[0];
		//20230831 cf-4357seho 선택한 대상에서 아이템을 찾아냄.
		boolean isRev = false; //리비전 잘못 선택했을때 메시지 때문..
		TCComponentItem targetItem = null;
		if (comp instanceof TCComponentItem)
		{
			targetItem = (TCComponentItem) comp;
//			try
//			{
//				targetRev = ((TCComponentItem) comp).getLatestItemRevision();
//			} catch (TCException e)
//			{
//				e.printStackTrace();
//			}
		} else if (comp instanceof TCComponentItemRevision)
		{
			try
			{
				isRev = true;
				targetItem = ((TCComponentItemRevision)comp).getItem();
			} catch (TCException e)
			{
				e.printStackTrace();
				MessageBox.post(e);
				return;
			}
//			targetRev = (TCComponentItemRevision) comp;

			/** Dialog 호출. */
			// SYMCSaveAsDialog dialog = new SYMCSaveAsDialog(AIFUtility.getActiveDesktop(), comp);

			// Modal 상태 (true:Modal, false:NonModal)
			// dialog.setModal(true);
			// 스레드 단위로 Dialog 실행.
			// setRunnable(dialog);
		}

		//20230831 cf-4357 seho 선택된 대상의 최종 릴리즈 리비전을 가져오도록 함.
		TCComponentItemRevision targetRev = null;
		try
		{
			TCComponentItemRevision[] releasedItemRevisions = targetItem.getReleasedItemRevisions();
			if (releasedItemRevisions == null || releasedItemRevisions.length == 0)
			{
				MessageBox.post("선택된 " + (isRev?"리비전의 ":"") + "아이템에 릴리즈된 리비전이 존재하지 않습니다.", "최종 릴리즈 리비전 사용", MessageBox.INFORMATION);
				return;
			}
			TCComponentItemRevision releasedItemRevision = releasedItemRevisions[0];
			if(isRev && !comp.equals(releasedItemRevision))
			{
				MessageBox.post("선택한 리비전은 최종 릴리즈 리비전이 아닙니다.\n최종 릴리즈 리비전으로 자동 변경됩니다.\n " + comp + "\n ▶ " + releasedItemRevision, "최종 릴리즈 리비전 사용", MessageBox.INFORMATION);
			}
			targetRev = releasedItemRevision;
		} catch (TCException e)
		{
			e.printStackTrace();
			MessageBox.post(e);
			return;
		}

		if (targetRev == null)
		{
			MessageBox.post("Item 또는 ItemRevison을 선택 하신 후 실행 하십시오.", "알림.", MessageBox.INFORMATION);
			return;
		}

		String strRevType = targetRev.getType();
		Shell shell = AIFUtility.getActiveDesktop().getShell();
		if (SYMCClass.S7_PRODUCTPARTREVISIONTYPE.equals(strRevType) || SYMCClass.S7_VARIANTPARTREVISIONTYPE.equals(strRevType) || SYMCClass.S7_FNCPARTREVISIONTYPE.equals(strRevType)
				|| SYMCClass.S7_FNCMASTPARTREVISIONTYPE.equals(strRevType))
		{

			SYMCProductManagerDialog parDialog = new SYMCProductManagerDialog(shell, targetRev);
			// [SR140702-059][20140626] Save As 시 Project Code Blank 처리. 
			parDialog.setSaveAsMode(true);
			parDialog.setApplyButtonVisible(false);
			parDialog.open();
		} else if (SYMCClass.S7_VEHPARTREVISIONTYPE.equals(strRevType) || SYMCClass.S7_STDPARTREVISIONTYPE.equals(strRevType) )
		{
/*			
			String strMessage = CustomUtil.validateSaveAs(targetRev);
			if (!CustomUtil.isEmpty(strMessage))
			{
				MessageBox.post(shell, strMessage, "Warning", MessageBox.WARNING);
				return;
			}
*/			
			
			SYMCPartManagerDialog parDialog = new SYMCPartManagerDialog(shell, targetRev);
	         // [SR140702-059][20140626] Save As 시 Project Code Blank 처리.
			parDialog.setSaveAsMode(true);
			parDialog.setApplyButtonVisible(false);
			parDialog.open();
		}

	}

}

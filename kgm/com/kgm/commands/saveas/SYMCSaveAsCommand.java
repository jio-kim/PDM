package com.kgm.commands.saveas;

import org.eclipse.swt.widgets.Shell;

import com.kgm.commands.partmaster.SYMCPartManagerDialog;
import com.kgm.commands.partmaster.SYMCProductManagerDialog;
import com.kgm.common.SYMCClass;
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
	 * ������.
	 * 
	 * @copyright : S-PALM
	 * @author : �ǻ��
	 * @since : 2012. 12. 18.
	 */
	public SYMCSaveAsCommand()
	{
		InterfaceAIFComponent[] comps = AIFUtility.getCurrentApplication().getTargetComponents();

		if (comps == null || comps.length == 0)
		{
			MessageBox.post("�ٸ� �̸����� ���� �� ���(Item, ItemRevision)�� �ϳ� ���� �� �� ���� �Ͻʽÿ�.", "�˸�", MessageBox.INFORMATION);
			return;
		}
		if (comps.length > 1)
		{
			MessageBox.post("�ٸ� �̸����� ���� �� ���(Item, ItemRevision)�� �ϳ��� ���� �Ͻ� �� ���� �Ͻʽÿ�.", "�˸�.", MessageBox.INFORMATION);
			return;
		}

		TCComponent comp = (TCComponent) comps[0];
		//20230831 cf-4357seho ������ ��󿡼� �������� ã�Ƴ�.
		boolean isRev = false; //������ �߸� ���������� �޽��� ����..
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

			/** Dialog ȣ��. */
			// SYMCSaveAsDialog dialog = new SYMCSaveAsDialog(AIFUtility.getActiveDesktop(), comp);

			// Modal ���� (true:Modal, false:NonModal)
			// dialog.setModal(true);
			// ������ ������ Dialog ����.
			// setRunnable(dialog);
		}

		//20230831 cf-4357 seho ���õ� ����� ���� ������ �������� ���������� ��.
		TCComponentItemRevision targetRev = null;
		try
		{
			TCComponentItemRevision[] releasedItemRevisions = targetItem.getReleasedItemRevisions();
			if (releasedItemRevisions == null || releasedItemRevisions.length == 0)
			{
				MessageBox.post("���õ� " + (isRev?"�������� ":"") + "�����ۿ� ������� �������� �������� �ʽ��ϴ�.", "���� ������ ������ ���", MessageBox.INFORMATION);
				return;
			}
			TCComponentItemRevision releasedItemRevision = releasedItemRevisions[0];
			if(isRev && !comp.equals(releasedItemRevision))
			{
				MessageBox.post("������ �������� ���� ������ �������� �ƴմϴ�.\n���� ������ ���������� �ڵ� ����˴ϴ�.\n " + comp + "\n �� " + releasedItemRevision, "���� ������ ������ ���", MessageBox.INFORMATION);
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
			MessageBox.post("Item �Ǵ� ItemRevison�� ���� �Ͻ� �� ���� �Ͻʽÿ�.", "�˸�.", MessageBox.INFORMATION);
			return;
		}

		String strRevType = targetRev.getType();
		Shell shell = AIFUtility.getActiveDesktop().getShell();
		if (SYMCClass.S7_PRODUCTPARTREVISIONTYPE.equals(strRevType) || SYMCClass.S7_VARIANTPARTREVISIONTYPE.equals(strRevType) || SYMCClass.S7_FNCPARTREVISIONTYPE.equals(strRevType)
				|| SYMCClass.S7_FNCMASTPARTREVISIONTYPE.equals(strRevType))
		{

			SYMCProductManagerDialog parDialog = new SYMCProductManagerDialog(shell, targetRev);
			// [SR140702-059][20140626] Save As �� Project Code Blank ó��. 
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
	         // [SR140702-059][20140626] Save As �� Project Code Blank ó��.
			parDialog.setSaveAsMode(true);
			parDialog.setApplyButtonVisible(false);
			parDialog.open();
		}

	}

}

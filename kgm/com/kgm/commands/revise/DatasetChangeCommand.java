package com.kgm.commands.revise;

import com.kgm.commands.weight.PropertyConstant;
import com.kgm.common.SYMCClass;
import com.kgm.common.utils.CustomUtil;
import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.aif.AbstractAIFCommand;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentUser;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;

public class DatasetChangeCommand extends AbstractAIFCommand
{

	/**
	 * Revise Command
	 */
	public DatasetChangeCommand()
	{

		try
		{
			if (this.validationCheck())
			{
				AIFDesktop activeDesktop = AIFUtility.getActiveDesktop();
				ChangeDatasetDialog dialog = new ChangeDatasetDialog(activeDesktop);
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
	 * Function Master�� �����ϴ�. �ֽ� �������̰� Working ����
	 * 
	 * @return
	 * @throws Exception
	 */
	private boolean validationCheck() throws Exception {
		// 1. ��� ���� �Ǿ�����.
		// 2. ����� fmp. vehicle part revision ����
		// 3. working ��������
		// 4. latest revision ����
		// 5. revision�� owner ����
		
		Registry registry = Registry.getRegistry(this);
		InterfaceAIFComponent[] targetComponents = AIFUtility.getCurrentApplication().getTargetComponents();
		TCComponentItemRevision itemRevision;

		if (targetComponents.length == 0)
		{
			MessageBox.post(registry.getString("ReviseDialog.MESSAGE.NoSelectedItem"), registry.getString("ReviseDialog.MESSAGE.Title.Warning"), MessageBox.INFORMATION); // Revise�� ����� ���õ��� �ʾҽ��ϴ�.
			return false;
		}
		
		if (!(targetComponents[0] instanceof TCComponentItemRevision))
		{
			MessageBox.post("Please select a revision to revise the dataset.", registry.getString("ReviseDialog.MESSAGE.Title.Warning"), MessageBox.INFORMATION); //����� ������ �������� �ƴմϴ�.
			return false;
		} else if (targetComponents[0] instanceof TCComponentItemRevision) {
			itemRevision = ((TCComponentItemRevision) targetComponents[0]);
			
			if (!(itemRevision.getType().equals(SYMCClass.S7_VEHPARTREVISIONTYPE) || itemRevision.getType().equals(SYMCClass.S7_FNCMASTPARTREVISIONTYPE))){
				MessageBox.post("The revision you selected does not support dataset revisions.", registry.getString("ReviseDialog.MESSAGE.Title.Warning"), MessageBox.INFORMATION);
				return false;
			}

			// ����� ������ �������� Working ���°� �ƴմϴ�
			if (!CustomUtil.isWorkingStatus(itemRevision)) {
				MessageBox.post("Target revision is not in Working State", registry.getString("ReviseDialog.MESSAGE.Title.Warning"), MessageBox.INFORMATION);
				return false;
			}
			
			// �ֽ� �������� �ƴϸ� return
			if (!itemRevision.getItem().getLatestItemRevision().equals(itemRevision)) {
				MessageBox.post(registry.getString("ReviseDialog.MESSAGE.IsNotLastItemRevision"), registry.getString("ReviseDialog.MESSAGE.Title.Warning"), MessageBox.INFORMATION);
				return false;
			}
			
			//revision�� owner �� �ƴϸ� return
			TCSession session = (TCSession)AIFUtility.getCurrentApplication().getSession();
			TCComponentUser revOwner = (TCComponentUser)itemRevision.getReferenceProperty(PropertyConstant.ATTR_NAME_OWNINGUSER);
			if (session.getUser() != revOwner){
				MessageBox.post("You are not the owner of the target revision.", registry.getString("ReviseDialog.MESSAGE.Title.Warning"), MessageBox.INFORMATION);
				return false;
			}
		}

		return true;
	}

}

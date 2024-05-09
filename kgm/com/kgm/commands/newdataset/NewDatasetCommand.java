package com.kgm.commands.newdataset;

import com.kgm.common.utils.CustomUtil;
import com.teamcenter.rac.aif.AbstractAIFCommand;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;

public class NewDatasetCommand extends AbstractAIFCommand {

	/**
	 * ����Ʈ ������
	 * 
	 * @copyright : S-PALM
	 * @author : ������
	 * @since : 2012. 3. 29.
	 */
	public NewDatasetCommand(){
		InterfaceAIFComponent[] comps = AIFUtility.getCurrentApplication().getTargetComponents();
		Registry registry = Registry.getRegistry(this);
		
		if(comps == null || comps.length == 0){
			//MessageBox.post("������ ÷�� �ϰ��� �ϴ� �ϳ��� ����� ���� �� �ּ���","�˸�",MessageBox.INFORMATION);
			MessageBox.post(registry.getString("NewDataset.Command.Message.NoSelected"), registry.getString("NewDataset.MESSAGE.Title.Warning"), MessageBox.INFORMATION);
			return;
		}
		if(comps.length > 1){
//			MessageBox.post("������ ÷�� �ϰ��� �ϴ� �ϳ��� ����� ���� �� �ּ���","�˸�",MessageBox.INFORMATION);
			MessageBox.post(registry.getString("NewDataset.Command.Message.NoSelected"), registry.getString("NewDataset.MESSAGE.Title.Warning"), MessageBox.INFORMATION);
			return;
		}
		if(comps[0] instanceof TCComponent){
			try {
				
				String strUserName = comps[0].getSession().getUserName();
				
				if(!"infodba0".equals(strUserName) && !CustomUtil.isWorkingStatus((TCComponent)comps[0])){
//					MessageBox.post("���簡 �Ϸ� �Ǿ��ų� ���簡 ���� �� �Դϴ�. ������ ÷�� �Ͻ� �� �����ϴ�.","�˸�",MessageBox.INFORMATION);
					MessageBox.post(registry.getString("NewDataset.Command.Message.IsNotWorking"), registry.getString("NewDataset.MESSAGE.Title.Warning"), MessageBox.INFORMATION);
					return;
				}
			} catch (TCException e) {
				e.printStackTrace();
			}
			NewDatasetDialog dialog = new NewDatasetDialog(AIFUtility.getActiveDesktop());
			dialog.setModal(true);
			setRunnable(dialog);
		}
	}
}

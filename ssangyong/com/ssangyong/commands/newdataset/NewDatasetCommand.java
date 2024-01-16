package com.ssangyong.commands.newdataset;

import com.ssangyong.common.utils.CustomUtil;
import com.teamcenter.rac.aif.AbstractAIFCommand;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;

public class NewDatasetCommand extends AbstractAIFCommand {

	/**
	 * 디폴트 생성자
	 * 
	 * @copyright : S-PALM
	 * @author : 이정건
	 * @since : 2012. 3. 29.
	 */
	public NewDatasetCommand(){
		InterfaceAIFComponent[] comps = AIFUtility.getCurrentApplication().getTargetComponents();
		Registry registry = Registry.getRegistry(this);
		
		if(comps == null || comps.length == 0){
			//MessageBox.post("파일을 첨부 하고자 하는 하나의 대상을 선택 해 주세요","알림",MessageBox.INFORMATION);
			MessageBox.post(registry.getString("NewDataset.Command.Message.NoSelected"), registry.getString("NewDataset.MESSAGE.Title.Warning"), MessageBox.INFORMATION);
			return;
		}
		if(comps.length > 1){
//			MessageBox.post("파일을 첨부 하고자 하는 하나의 대상을 선택 해 주세요","알림",MessageBox.INFORMATION);
			MessageBox.post(registry.getString("NewDataset.Command.Message.NoSelected"), registry.getString("NewDataset.MESSAGE.Title.Warning"), MessageBox.INFORMATION);
			return;
		}
		if(comps[0] instanceof TCComponent){
			try {
				
				String strUserName = comps[0].getSession().getUserName();
				
				if(!"infodba0".equals(strUserName) && !CustomUtil.isWorkingStatus((TCComponent)comps[0])){
//					MessageBox.post("결재가 완료 되었거나 결재가 진행 중 입니다. 파일을 첨부 하실 수 없습니다.","알림",MessageBox.INFORMATION);
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

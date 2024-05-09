package com.kgm.commands.ec.report;

import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MessageBox;

import com.kgm.common.utils.CustomUtil;
import com.teamcenter.rac.aif.AbstractAIFCommand;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponentChangeItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;

public class ECOReportCommand extends AbstractAIFCommand {

	private InterfaceAIFComponent[] targetComponents;
	private TCSession session;
	private TCComponentChangeItemRevision changeRevision;
	@SuppressWarnings("unused")
    private File reportFile;
	
	public ECOReportCommand() {
		this.targetComponents = AIFUtility.getCurrentApplication().getTargetComponents();
		this.session = CustomUtil.getTCSession();
		String message = validation();
		if(message.equals("")){
			String defaultFilename = null;
			try {
				defaultFilename = changeRevision.getProperty("item_id")+"_Report.xls";
			} catch (TCException e) {
				e.printStackTrace();
				com.teamcenter.rac.util.MessageBox.post(AIFUtility.getActiveDesktop().getShell(), e.toString(), "Error", com.teamcenter.rac.util.MessageBox.ERROR);
			}
			
			//## FIXME
			if(!askSavePath(defaultFilename)) return;
			
	        ECOReportOperation operation = new ECOReportOperation(session, changeRevision, null);
	        session.queueOperation(operation);
			
		}else{
			com.teamcenter.rac.util.MessageBox.post(AIFUtility.getActiveDesktop().getShell(), message, "Error", com.teamcenter.rac.util.MessageBox.ERROR);
			return;
		}
	}

	private String validation(){
		StringBuffer message = new StringBuffer();
		
		/** targetComponents 갯수 확인 **/
		if(targetComponents == null || targetComponents.length == 0){
			message.append("ECO type Object is not selected.\n");
			return message.toString();
		}
		
		if(targetComponents.length > 1){
			message.append("Select just one ECO type object.\n");
			return message.toString();
		}

		/** targetComponents Type 확인 **/
		if(targetComponents[0] instanceof TCComponentChangeItemRevision){
			changeRevision = (TCComponentChangeItemRevision) targetComponents[0];
		}else{
			message.append("Select ECO type object.\n");
			return message.toString();
		}
		
		try{
//			/** 권한 체크 **/
//			if(!changeRevision.okToModify())
//				message.append("You have no power of request.\n");
//
//			/** 결재 여부 **/
//			String[] processProps = changeRevision.getProperties(new String[]{"date_released", "process_stage_list"});
//			if(!processProps[0].equals("") || !processProps[1].equals(""))
//				message.append("Already been payment in progress.\n");
//
//			/** infodba여부 확인 **/
//			if(session.getUserName().equals("infodba"))
//				message.append("infodba cannot request.\n");
		}catch(Exception e){
			e.printStackTrace();
			message.append("Error occurred in validation().\n");
			message.append(e.toString());
		}

		return message.toString();
	}
	
	private boolean askSavePath(String defaultFilename){
		//export 경로 정의
		FileDialog saveDialog = new FileDialog(AIFUtility.getActiveDesktop().getShell(), SWT.SAVE);
		saveDialog.setFilterExtensions(new String[] {"*.xls"});
		saveDialog.setFilterNames(new String[] {"Excel file (*.xls)"});
		saveDialog.setFileName(defaultFilename);
		if(saveDialog.open() != null){

			String name = saveDialog.getFileName();
			if(name.equals("")) return false;
			if(name.indexOf(".xls") != name.length() - 4) name += ".xls";

			File file = new File(saveDialog.getFilterPath(), name);
			if(file.exists()) {
				MessageBox box = new MessageBox(AIFUtility.getActiveDesktop().getShell(), SWT.ICON_WARNING | SWT.YES | SWT.NO);
				box.setText("Save");
				box.setMessage("File " + file.getName() + " already exists.\nDo you want to replace it?");
				if(box.open() != SWT.YES) {
					return false;
				}
			}
			this.reportFile = file;
		}else{
			return false;
		}
		return true;
	}
}

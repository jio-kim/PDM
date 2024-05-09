package com.kgm.commands.ec.report;

import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MessageBox;

import com.kgm.common.utils.CustomUtil;
import com.kgm.common.utils.DateUtil;
import com.teamcenter.rac.aif.AbstractAIFCommand;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponentChangeItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;

public class ReportEPLCommand extends AbstractAIFCommand {

	private InterfaceAIFComponent[] targetComponents;
	private TCSession session;
	private TCComponentChangeItemRevision changeRevision;
	private File reportFile;
	
	public ReportEPLCommand() {
		this(AIFUtility.getCurrentApplication().getTargetComponents());
	}

	/**
	 * [SR140905-008][jclee][20140911] Export Excel File Name 변경. [ECO No]_Report.xls -> [날짜]_ECO-[ECO No].xls
	 * [SR141106-036][2014.11.19][jclee] Monitoring ECO 팝업창 내 ECO Report Multi Download 기능 추가를 위해 생성자 Param 추가
	 */
	public ReportEPLCommand(InterfaceAIFComponent[] targetComponents) {
		this.targetComponents = targetComponents;
		this.session = CustomUtil.getTCSession();
		String message = validation();
		if(message.equals("")){
			String defaultFilename = null;
			try {
				// [SR140905-008][jclee][20140911] Export Excel File Name 변경. [ECO No]_Report.xls -> [날짜]_ECO-[ECO No].xls
//				defaultFilename = changeRevision.getProperty("item_id")+"_Report.xls";
				defaultFilename = DateUtil.getClientDay("yyyyMMdd") + "_ECO-" + changeRevision.getProperty("item_id") + ".xls";
				
			} catch (TCException e) {
				e.printStackTrace();
				com.teamcenter.rac.util.MessageBox.post(AIFUtility.getActiveDesktop().getShell(), e.toString(), "Error", com.teamcenter.rac.util.MessageBox.ERROR);
			}
			
			//## FIXME
			if(!askSavePath(defaultFilename)) return;
			
			ReportEPLOperation operation = new ReportEPLOperation(session, changeRevision, reportFile);
	        session.queueOperation(operation);
			
		}else{
			com.teamcenter.rac.util.MessageBox.post(AIFUtility.getActiveDesktop().getShell(), message, "Error", com.teamcenter.rac.util.MessageBox.ERROR);
			return;
		}
	}

	/**
	 * 검증
	 * @return
	 */
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
		return message.toString();
	}
	
	/**
	 * 저장 위치 지정
	 * @param defaultFilename
	 * @return
	 */
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

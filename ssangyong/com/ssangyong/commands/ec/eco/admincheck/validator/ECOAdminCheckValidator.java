package com.ssangyong.commands.ec.eco.admincheck.validator;

import java.util.ArrayList;

import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import com.ssangyong.commands.ec.eco.admincheck.common.ECOAdminCheckConstants;
import com.ssangyong.commands.ec.eco.admincheck.view.ECOAdminCheckBasicInfoPanel;
import com.ssangyong.commands.ec.eco.admincheck.view.ECOAdminCheckChangeCauseTablePanel;
import com.ssangyong.commands.ec.eco.admincheck.view.ECOAdminCheckEndItemListTablePanel;
import com.ssangyong.common.lov.SYMCLOVLoader;
import com.teamcenter.rac.kernel.TCComponentListOfValues;
import com.teamcenter.rac.util.MessageBox;

public class ECOAdminCheckValidator {
	private ECOAdminCheckChangeCauseTablePanel pnlECOAdminCheckChangeCauseTable;
	private ECOAdminCheckBasicInfoPanel pnlECOAdminCheckBasicInfo;
	private ECOAdminCheckEndItemListTablePanel pnlECOAdminCheckEndItemListTable;
	
	public ECOAdminCheckValidator(ECOAdminCheckChangeCauseTablePanel pnlECOAdminCheckChangeCauseTable, ECOAdminCheckBasicInfoPanel pnlECOAdminCheckBasicInfo, ECOAdminCheckEndItemListTablePanel pnlECOAdminCheckEndItemListTable) {
		this.pnlECOAdminCheckChangeCauseTable = pnlECOAdminCheckChangeCauseTable;
		this.pnlECOAdminCheckBasicInfo = pnlECOAdminCheckBasicInfo;
		this.pnlECOAdminCheckEndItemListTable = pnlECOAdminCheckEndItemListTable;
	}
	
	/**
	 * Change Cause Validation Check
	 * @return
	 */
	public boolean validationCheckChangeCause() throws Exception {
		int iTotalCount = pnlECOAdminCheckChangeCauseTable.getEndItemTotalCount();
		StringBuffer sbMessage = new StringBuffer();
		
		int iTotalEndItemCountA = 0;

		if (iTotalCount < 0) {
			sbMessage.append("End Item Total Count Error! Contact to Administrator.\n");
		}
		
		Table table = pnlECOAdminCheckChangeCauseTable.getTable();
		
		// Project Code
		TCComponentListOfValues lovProjectCode = SYMCLOVLoader.getLOV(ECOAdminCheckConstants.LOV_PROJECT_CODE);
		if (lovProjectCode == null) {
			sbMessage.append("Fail to load the Change Cause LOV.\n");
		}
		String[] saProjectCodeLOVValues = lovProjectCode.getListOfValues().getStringListOfValues();
		
		for (int inx = 0; inx < table.getItemCount(); inx++) {
			TableItem item = table.getItem(inx);
			boolean isContain = false;
			
			String sProjectCode = item.getText(2);
			
			for (int jnx = 0; jnx < saProjectCodeLOVValues.length; jnx++) {
				if (saProjectCodeLOVValues[jnx].equals(sProjectCode)) {
					isContain = true;
					break;
				}
			}
			
			if (!isContain) {
				sbMessage.append("Check the Project Code value. (Do not append with comma) -> Index : " + (inx + 1) + "\n");
			}
		}
		
		// Change Cause
		TCComponentListOfValues lovChangeCause = SYMCLOVLoader.getLOV(ECOAdminCheckConstants.LOV_CHANGE_CAUSE);
		if (lovChangeCause == null) {
			sbMessage.append("Fail to load the Change Cause LOV.\n");
		}
		String[] saChangeCauseLOVValues = lovChangeCause.getListOfValues().getStringListOfValues();
		
		for (int inx = 0; inx < table.getItemCount(); inx++) {
			TableItem item = table.getItem(inx);
			boolean isContain = false;
			
			String sChangeCause = item.getText(3);
			
			for (int jnx = 0; jnx < saChangeCauseLOVValues.length; jnx++) {
				if (saChangeCauseLOVValues[jnx].equals(sChangeCause)) {
					isContain = true;
					break;
				}
			}
			
			if (!isContain) {
				sbMessage.append("Check the Change Cause value.(01, 02, 03) -> Index : " + (inx + 1) + "\n");
			}
		}
		
		// End Item Count
		for (int inx = 0; inx < table.getItemCount(); inx++) {
			TableItem item = table.getItem(inx);
			
			String sEndItemCountA = item.getText(4);
			String sEndItemCountM = item.getText(5);
			
			if (sEndItemCountA == null || sEndItemCountA.equals("") || sEndItemCountA.length() == 0) {
				sEndItemCountA = "0";
			}
			
			if (sEndItemCountM == null || sEndItemCountM.equals("") || sEndItemCountM.length() == 0) {
				sEndItemCountM = "0";
			}
			
			if (sEndItemCountA.equals("0") && sEndItemCountM.equals("0")) {
				sbMessage.append("Check a End Item Count. -> Index : " + (inx + 1) + "\n");
			}
			
			iTotalEndItemCountA += Integer.valueOf(sEndItemCountA);
		}
		
		if (iTotalEndItemCountA != iTotalCount) {
			sbMessage.append("Check a Total Count.\n");
		}
		
		if (sbMessage.length() != 0) {
			MessageBox.post(sbMessage.toString(), "Validation Check", MessageBox.ERROR);
			return false;
		}
		
		return true;
	}

	/**
	 * Property Validation Check
	 * @return
	 */
	public boolean validationCheckProperties() {
		ArrayList<Control> controls = pnlECOAdminCheckBasicInfo.getControls();
		StringBuffer sbMessage = new StringBuffer();
		
		for (int inx = 0; inx < controls.size(); inx++) {
			Control control = controls.get(inx);
			Object oProp = control.getData(ECOAdminCheckConstants.PROP);
			
			if (oProp != null) {
				String sProp = (String) oProp;
				
				if (sProp.equals(ECOAdminCheckConstants.PROP_ADMIN_CHECK) || sProp.equals(ECOAdminCheckConstants.PROP_NOTE)) {
					Text txt = (Text) control;
					if (txt.getText().length() > 2000) {
						sbMessage.append("Check the Admin Check or Note Text length (MAX : 2000).\n");
					}
				} else {
					continue;
				}
			}
		}
		
		if (sbMessage.length() != 0) {
			MessageBox.post(sbMessage.toString(), "Validation Check", MessageBox.ERROR);
			return false;
		}
		
		return true;
	}

	/**
	 * End Item List Validation Check
	 * @return
	 */
	public boolean validationCheckEndItemList() {
		StringBuffer sbMessage = new StringBuffer();
		Table tblEndItem = pnlECOAdminCheckEndItemListTable.getTable();
		
		for (int inx = 0; inx < tblEndItem.getItemCount(); inx++) {
			TableItem item = tblEndItem.getItem(inx);
			String sRowNo = item.getText(0);
			String sPartName = item.getText(1);
			String sCT = item.getText(2);
			String sSMode = item.getText(3);
			String sEditable = item.getText(4);
			
			if ("FALSE".equals(sEditable)) {
				continue;
			}

			if (sPartName == null || sPartName.equals("") || sPartName.length() == 0) {
				sbMessage.append(sRowNo + " : " + "Part name can't input null.\n");
			} else {
				if (sPartName.length() > 128) {
					sbMessage.append(sRowNo + " : " + "Part name is too long.(128byte)\n");
				}
			}
			
			if (sCT == null || sCT.equals("") || sCT.length() == 0) {
				sbMessage.append(sRowNo + " : " + "Change Type can't input null.\n");
			} else {
				if (sCT.length() > 2) {
					sbMessage.append(sRowNo + " : " + "Change Type is too long.(2byte)\n");
				}
			}
			
			if (sSMode == null || sSMode.equals("") || sSMode.length() == 0) {
				sbMessage.append(sRowNo + " : " + "S/Mode can't input null.\n");
			} else {
				if (sSMode.length() > 8) {
					sbMessage.append(sRowNo + " : " + "S/Mode is too long.(8byte)\n");
				}
			}
		}
		
		if (sbMessage.length() != 0) {
			MessageBox.post(sbMessage.toString(), "Validation Check", MessageBox.ERROR);
			return false;
		}
		
		return true;
	}
}

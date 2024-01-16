package com.ssangyong.commands.ec.eco.admincheck.operation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import com.ssangyong.commands.ec.dao.CustomECODao;
import com.ssangyong.commands.ec.eco.admincheck.common.ECOAdminCheckConstants;
import com.ssangyong.commands.ec.eco.admincheck.view.ECOAdminCheckBasicInfoPanel;
import com.ssangyong.commands.ec.eco.admincheck.view.ECOAdminCheckChangeCauseTablePanel;
import com.ssangyong.commands.ec.eco.admincheck.view.ECOAdminCheckEndItemListTablePanel;
import com.ssangyong.common.utils.CustomUtil;
import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.kernel.Markpoint;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.controls.SWTComboBox;

public class ECOAdminCheckOperation extends AbstractAIFOperation {
	private ECOAdminCheckChangeCauseTablePanel pnlECOAdminCheckChangeCauseTable;
	private ECOAdminCheckBasicInfoPanel pnlECOAdminCheckBasicInfo;
	private ECOAdminCheckEndItemListTablePanel pnlECOAdminCheckEndItemListTable;
	private TCComponentItemRevision ecoRevision;
	private TCSession session;
	
	public ECOAdminCheckOperation(TCComponentItemRevision ecoRevision, ECOAdminCheckChangeCauseTablePanel pnlECOAdminCheckChangeCauseTable, ECOAdminCheckBasicInfoPanel pnlECOAdminCheckBasicInfo, ECOAdminCheckEndItemListTablePanel pnlECOAdminCheckEndItemListTable) {
		this.pnlECOAdminCheckChangeCauseTable = pnlECOAdminCheckChangeCauseTable;
		this.pnlECOAdminCheckBasicInfo = pnlECOAdminCheckBasicInfo;
		this.pnlECOAdminCheckEndItemListTable = pnlECOAdminCheckEndItemListTable;
		this.ecoRevision = ecoRevision;
		this.session = CustomUtil.getTCSession();
	}

	@Override
	public void executeOperation() throws Exception {
		saveChangeCause();
		saveProperties();
		saveEndItemList();
	}
	
	/**
	 * Save Change Cause
	 * (수정여부와 관계없이 무조건 작업 수행)
	 */
	public void saveChangeCause() throws Exception {
		CustomECODao dao = new CustomECODao();
		Markpoint mp = new Markpoint(session);
		
		try {
			String sECONo = ecoRevision.getProperty("item_id");
			ArrayList<HashMap<String, Object>> alChangeCause = getChangeCause();
			
			// 1. 기존 리스트 삭제
			dao.deleteECOChangeCause(sECONo);
			
			// 2. 새로운 리스트 생성
			for (int inx = 0; inx < alChangeCause.size(); inx++) {
				dao.insertECOChangeCause(alChangeCause.get(inx));
			}
			
			mp.forget();
		} catch (Exception e) {
			e.printStackTrace();
			MessageBox.post(e);
			mp.rollBack();
		}
	}
	
	/**
	 * Save Properties
	 */
	public void saveProperties() throws Exception {
		HashMap<String, String> hmDataPropertiesNew = new HashMap<String, String>();
		ArrayList<Control> alControls = pnlECOAdminCheckBasicInfo.getControls();
		
		for (int inx = 0; inx < alControls.size(); inx++) {
			String sValue = "";
			Control cProp = alControls.get(inx);
			String sProp = cProp.getData(ECOAdminCheckConstants.PROP).toString();
			
			if (cProp instanceof SWTComboBox) {
				SWTComboBox cmb = (SWTComboBox)cProp;
				sValue = cmb.getSelectedItem() == null ? "" : cmb.getSelectedItem().toString(); 
			} else if (cProp instanceof Text) {
				Text txt = (Text)cProp;
				sValue = txt.getText() == null ? "" : txt.getText();
			}
			
			hmDataPropertiesNew.put(sProp, sValue);
		}

		// 값이 변경되었을 경우에만 Save작업 수행
		if (isModifiedProperties(hmDataPropertiesNew)) {
			TCComponent trECORevision = ecoRevision.getReferenceProperty("s7_ECO_TypedReference");
			trECORevision.setProperties(hmDataPropertiesNew);
		}
	}

	/**
	 * Property 값이 변경되었는지 여부 확인
	 * @param hmDataPropertiesNew
	 * @return
	 */
	private boolean isModifiedProperties(HashMap<String, String> hmDataPropertiesNew) {
		HashMap<String, String> hmDataPropertiesOld = pnlECOAdminCheckBasicInfo.getDataPropertiesOld();
		Set<String> keySet = hmDataPropertiesOld.keySet();
		Iterator<String> iterator = keySet.iterator();
		
		if (hmDataPropertiesOld.size() == 0) {
			return true;
		}
		
		while (iterator.hasNext()) {
			String sProp = iterator.next();
			String sOldValue = hmDataPropertiesOld.get(sProp);
			String sNewValue = hmDataPropertiesNew.get(sProp);
			
			// Old Value 가 Null일 경우
			if (sOldValue == null || sOldValue.equals("") || sOldValue.length() == 0) {
				if (sNewValue != null) {
					return true;
				}
			}
			
			// New Value 가 Null일 경우
			if (sNewValue == null || sNewValue.equals("") || sNewValue.length() == 0) {
				if (sOldValue != null) {
					return true;
				}
			}
			
			// 값이 서로 다를 경우
			if (!sOldValue.equals(sNewValue)) {
				return true;
			}
		}
		
		return false;
	}

	/**
	 * Save End Item List
	 * (수정여부와 관계없이 무조건 작업 수행)
	 */
	public void saveEndItemList() throws Exception {
		CustomECODao dao = new CustomECODao();
		Markpoint mp = new Markpoint(session);
		
		try {
			String sECONo = ecoRevision.getProperty("item_id");
			ArrayList<HashMap<String, Object>> alEndItemList = getEndItemList();
			
			// 1. 기존 리스트 삭제
			dao.deleteECOEplEndItemList(sECONo);
			
			// 2. 새로운 리스트 생성
			for (int inx = 0; inx < alEndItemList.size(); inx++) {
				dao.insertECOEplEndItemList(alEndItemList.get(inx));
			}
			
			mp.forget();
		} catch (Exception e) {
			e.printStackTrace();
			MessageBox.post(e);
			mp.rollBack();
		}
	}
	
	/**
	 * 
	 * @return
	 */
	private ArrayList<HashMap<String, Object>> getChangeCause() {
		try {
			ArrayList<HashMap<String, Object>> result = new ArrayList<HashMap<String,Object>>();
			Table tblChangeCause = pnlECOAdminCheckChangeCauseTable.getTable();
			
			int iCount = tblChangeCause.getItemCount();
			
			for (int inx = 0; inx < iCount; inx++) {
				TableItem item = tblChangeCause.getItem(inx);
				HashMap<String, Object> hmResult = new HashMap<String, Object>();
				
				String sECONo = item.getText(0);
				String sSEQNo = item.getText(1);
				String sProjectCode = item.getText(2);
				String sChangeCause = item.getText(3);
				String sEndItemCountA = item.getText(4);
				String sEndItemCountM = item.getText(5);
				
				hmResult.put(ECOAdminCheckConstants.PROP_ECO_NO, sECONo);
				hmResult.put(ECOAdminCheckConstants.PROP_SEQ_NO, sSEQNo);
				hmResult.put(ECOAdminCheckConstants.PROP_PROJECT_CODE, sProjectCode);
				hmResult.put(ECOAdminCheckConstants.PROP_CHANGE_CAUSE, sChangeCause);
				hmResult.put(ECOAdminCheckConstants.PROP_END_ITEM_COUNT_A, sEndItemCountA);
				hmResult.put(ECOAdminCheckConstants.PROP_END_ITEM_COUNT_M, sEndItemCountM);
				
				result.add(hmResult);
			}
			
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			MessageBox.post(e);
		}
		
		return null;
	}

	/**
	 * 
	 * @return
	 */
	private ArrayList<HashMap<String, Object>> getEndItemList() {
		try {
			ArrayList<HashMap<String, Object>> result = new ArrayList<HashMap<String,Object>>();
			Table tblEndItem = pnlECOAdminCheckEndItemListTable.getTable();
			
			int iCount = tblEndItem.getItemCount();
			String sECONo = ecoRevision.getProperty("item_id");
			
			for (int inx = 0; inx < iCount; inx++) {
				TableItem item = tblEndItem.getItem(inx);
				HashMap<String, Object> hmResult = new HashMap<String, Object>();
				
				String sPartName = item.getText(1);
				String sCT = item.getText(2);
				String sSMode = item.getText(3);
				String sChangeCause = item.getText(4);
				String sEditable = item.getText(5);
				
//				if ("FALSE".equals(sEditable)) {
//					continue;
//				}
				
				hmResult.put(ECOAdminCheckConstants.PROP_ECO_NO, sECONo);
				hmResult.put(ECOAdminCheckConstants.PROP_PART_NAME, sPartName);
				hmResult.put(ECOAdminCheckConstants.PROP_CT, sCT);
				hmResult.put(ECOAdminCheckConstants.PROP_SMODE, sSMode);
				hmResult.put(ECOAdminCheckConstants.PROP_CHANGE_CAUSE, sChangeCause);
				hmResult.put(ECOAdminCheckConstants.PROP_EDITABLE, sEditable);
				
				result.add(hmResult);
			}
			
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			MessageBox.post(e);
		}
		
		return null;
	}
}

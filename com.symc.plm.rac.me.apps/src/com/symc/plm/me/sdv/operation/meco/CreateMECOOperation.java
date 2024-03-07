package com.symc.plm.me.sdv.operation.meco;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.widgets.TableItem;

import com.ssangyong.common.SYMCClass;
import com.ssangyong.dto.ApprovalLineData;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.sdv.command.meco.dao.CustomMECODao;
import com.symc.plm.me.sdv.dialog.meco.MECOSWTDialog;
import com.symc.plm.me.sdv.operation.AbstractTCSDVOperation;
import com.symc.plm.me.sdv.view.meco.MECOSWTRenderingView;
import com.symc.plm.me.utils.CustomUtil;
import com.symc.plm.me.utils.SYMTcUtil;
import com.teamcenter.rac.kernel.Markpoint;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentChangeItemRevision;
import com.teamcenter.rac.kernel.TCComponentFolder;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemType;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCPreferenceService;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.kernel.TCPreferenceService.TCPreferenceLocation;
import com.teamcenter.rac.ui.services.NavigatorOpenService;

/**
 * [SR140828-015][20140829] shcho, Migration 후 MECO ID 채번을 601 부터 할 수 있도록 변경. (2015년에는 다시 001부터 채번 할 수 있도록 Preference를 이용한 초기값 설정 적용)
 * [20240227][UPGRADE] 업그레이드 이후 생성기능 오류 수정		
 * @author 
 *
 */
public class CreateMECOOperation extends AbstractTCSDVOperation {

	private TCSession session;
	private HashMap<String, String> mecoInfoMap;
	private TCComponentItem mecoItem =null;
	private TCComponentChangeItemRevision mecoItemRevision = null;
	private MECOSWTDialog mecoDialog;
	private TCComponentFolder targetFolder;
	private CustomMECODao dao;
	private MECOSWTRenderingView mecoinfoPanel;
	
	public CreateMECOOperation(HashMap<String, String> mecoInfoMap, MECOSWTDialog mecoDialog, TCComponentFolder targetFolder, MECOSWTRenderingView mecoinfoPanel) {
		this.mecoInfoMap = mecoInfoMap;
		this.mecoDialog = mecoDialog;
		this.targetFolder = targetFolder;
		this.mecoinfoPanel = mecoinfoPanel;
	}

	@Override
	public void startOperation(String commandId) {

	}

	@Override
	public void endOperation() {

	}

	@Override
	public void executeOperation() throws Exception {
		session = (TCSession) getSession();
		createMECO();

	}
	
	// [SR140828-015][20140829] shcho, Migration 후 MECO ID 채번을 601 부터 할 수 있도록 변경. (2015년에는 다시 001부터 채번 할 수 있도록 Preference를 이용한 초기값 설정 적용)
	public void createMECO() throws TCException, Exception {
		TCComponentItemType itemType;
		dao = new CustomMECODao();
		Markpoint mp = null;
			mp = new Markpoint(session);
			String mecoID = null; 
			String year_YY = CustomUtil.getYear();
			String mecoType = (String)mecoInfoMap.get(SDVPropertyConstant.MECO_TYPE);
			String mecoPrefix = mecoType+(String)mecoInfoMap.get(SDVPropertyConstant.MECO_ORG_CODE)+year_YY;
			String preferenceName =  "SYMC_MECO_Init_NO_" + mecoType; 
			TCPreferenceService preferenceService = session.getPreferenceService();
//	        String init_no = preferenceService.getString(TCPreferenceService.TC_preference_site, preferenceName);
	        String init_no = preferenceService.getStringValueAtLocation(preferenceName, TCPreferenceLocation.OVERLAY_LOCATION);
	        if(init_no.equals("")) {
	            throw new Exception("There is no '" + preferenceName + "'" + " preference Value.");
	        }
			        
			mecoID = dao.getNextMECOSerial(mecoPrefix, init_no);
			
			//[20240227][UPGRADE] 업그레이드 이후 생성기능 오류 수정		
			//itemType = (TCComponentItemType)session.getTypeComponent("EngChange");
			String description = mecoInfoMap.get(SDVPropertyConstant.ITEM_OBJECT_DESC);
			description = description == null || description.isEmpty() ?mecoID:description;
			//mecoItem = itemType.create(mecoID, SYMCClass.ITEM_REV_ID, SDVTypeConstant.MECO_ITEM, mecoID, description, null);
			
			//Item Property 속성 입력
			Map<String, String> itemPropMap = new HashMap<>();
			Map<String, String> itemRevsionPropMap = new HashMap<>();
			itemPropMap.put(SDVPropertyConstant.ITEM_ITEM_ID, mecoID);
			itemPropMap.put(SDVPropertyConstant.ITEM_OBJECT_NAME, mecoID);
			itemPropMap.put(SDVPropertyConstant.ITEM_OBJECT_DESC, description);
			
			//Item Revision 속성 입력
			itemRevsionPropMap.put(SDVPropertyConstant.ITEM_REVISION_ID, SYMCClass.ITEM_REV_ID);
			//MCO 생성
			mecoItem = (TCComponentItem)SYMTcUtil.createItemObject(session, SDVTypeConstant.MECO_ITEM, itemPropMap, itemRevsionPropMap);
            mecoItemRevision = (TCComponentChangeItemRevision) mecoItem.getLatestItemRevision();
            mecoDialog.setMECORevison(mecoItemRevision);
			
			TCComponent	refComp = SYMTcUtil.createApplicationObject(mecoItemRevision.getSession(), SDVTypeConstant.MECO_TYPED_REFERECE, new String[] { "m7_EFFECT_DATE", "m7_EFFECT_EVENT" }, new String[] {
				(String) mecoInfoMap.get("m7_EFFECT_DATE"), (String) mecoInfoMap.get("m7_EFFECT_EVENT") });

			mecoItemRevision.setReferenceProperty(SDVPropertyConstant.MECO_TYPED_REFERENCE, refComp);
	
			mecoInfoMap.remove("m7_EFFECT_DATE");
			mecoInfoMap.remove("m7_EFFECT_EVENT");
			
			mecoItemRevision.setProperties(mecoInfoMap);
			saveApprovalLine(mecoID);

			if(targetFolder == null){
				session.getUser().getNewStuffFolder().add("contents", mecoItem);
			}else{
				targetFolder.add("contents", mecoItem);
				targetFolder.refresh();
			}
			mecoItemRevision.lock();
			mecoItemRevision.save();
			mecoItemRevision.refresh();
//			mecoDialog.close();
		    NavigatorOpenService openService = new NavigatorOpenService();
		    openService.open(mecoItemRevision);
			mp.forget();
	}
	
	private void saveApprovalLine(String meco_no) {
		ArrayList<ApprovalLineData> paramList = new ArrayList<ApprovalLineData>();
		
		TableItem[] itemList = mecoinfoPanel.getApprovalLineTable().getItems();
		
		try {
			ApprovalLineData delLine = new ApprovalLineData();
			delLine.setEco_no(meco_no);
			
			dao.removeApprovalLine(delLine);

			if(itemList != null && itemList.length > 0){
				int i = 0;
				for(TableItem item : itemList){
					ApprovalLineData theLine = new ApprovalLineData();
					theLine.setEco_no(meco_no);
					theLine.setSort(i+"");
					theLine.setTask(item.getText(0));
					theLine.setTeam_name(item.getText(1));
					theLine.setUser_name(item.getText(2));
					theLine.setTc_member_puid((String) item.getData("puid"));

					paramList.add(theLine);
					i++;
				}
			}
			dao.saveApprovalLine(paramList);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}	
}

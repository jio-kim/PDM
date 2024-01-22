package com.teamcenter.ets.translator.ugs.weldpointexport;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Vector;

import com.ssangyong.common.remote.DataSet;
import com.teamcenter.ets.soa.SoaHelper;
import com.teamcenter.services.loose.bom._2008_06.StructureManagement;
import com.teamcenter.services.loose.bom._2008_06.StructureManagement.AddOrUpdateChildrenToParentLineInfo;
import com.teamcenter.services.loose.bom._2008_06.StructureManagement.RemoveChildrenFromParentLineResponse;
import com.teamcenter.soa.client.Connection;
import com.teamcenter.soa.client.model.ModelObject;
import com.teamcenter.soa.client.model.strong.BOMLine;
import com.teamcenter.soa.client.model.strong.BOMWindow;
import com.teamcenter.soa.client.model.strong.ItemRevision;
import com.teamcenter.soa.client.model.strong.PSOccurrence;
import com.teamcenter.soa.client.model.strong.PSOccurrenceThread;
import com.teamcenter.soa.client.model.strong.RevisionRule;
import com.teamcenter.tstk.util.log.ITaskLogger;

/**
 * Weld Gropup의 BOM Structure를 구성하는 역활을 수행하는 Class
 * 
 * lib에 TcSoaBOMLoose_10000.1.0.jar, TcSoaBOMTypes_10000.1.0.jar를 추가해 줘야 한다.
 * 
 * [NON-SR][20160503] Taeku.Jeong
 * @author Taeku
 *
 */
public class WeldGroupStructureBuilder {

	ITaskLogger m_zTaskLogger;
	StringBuffer buffer;
	boolean isDebug = false;
	private String servletUrlStr = null;
	private String serviceClassName = "com.ssangyong.service.WeldPoint2ndService";
	
	String partItemId  = null;
	String partRevisionId = null; 
	ItemRevision newWeldGroupRevision = null;
	Connection connection = null;
	
	String weldGroupItemId = null;
	String weldGroupItemRevId = null;
	
	XFormMatrixUtil xFormMatrixUtil = null;
	
	private int currentNewSeqNo = -1;
	
	/**
	 * Clsss 생성자
	 * @param m_zTaskLogger
	 * @param buffer
	 * @param isDebug
	 * @param servletUrlStr
	 */
	public WeldGroupStructureBuilder(ITaskLogger m_zTaskLogger, StringBuffer buffer, boolean isDebug, String servletUrlStr){
		this.m_zTaskLogger = m_zTaskLogger;
		this.buffer = buffer;
		this.isDebug = isDebug;
		this.servletUrlStr = servletUrlStr;
		
		this.connection = SoaHelper.getSoaConnection();
		this.xFormMatrixUtil = new XFormMatrixUtil(m_zTaskLogger, buffer, isDebug);
	}
	
	/**
	 * CSV 파일과 Weld Group Item에서 이전 Revision의 Structure Data를 비교해서 만든 Data를 이용해 새로운 Weld Group Item Revision의
	 * BOM Structure를 생성하는 기능을 수행하는 함수
	 *  
	 * @param partItemId
	 * @param partRevisionId
	 * @param newWeldGroupRevision
	 * @throws Exception
	 */
	public void makeOrChangeNewWeldGroupRevisionStructure(String partItemId, String partRevisionId, ItemRevision newWeldGroupRevision) throws Exception{
		
		this.partItemId  = partItemId;
		this.partRevisionId = partRevisionId; 
		this.newWeldGroupRevision = newWeldGroupRevision;
		
		if(newWeldGroupRevision==null){
			return;
		}
		
		try {
			SoaHelper.getProperties(newWeldGroupRevision, new String[]{"item_id", "item_revision_id"});
			this.weldGroupItemId = newWeldGroupRevision.get_item_id();
			this.weldGroupItemRevId = newWeldGroupRevision.get_item_revision_id();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		Date date = new Date();
		RevisionRule rule = null;
		BOMWindow window = null;
		BOMLine weldGroupBOMLine = null;
		
		try {
			rule = Util.getRevisionRule(connection, "Latest Released");
			window = Util.createTopLineBOMWindow(connection, newWeldGroupRevision, rule, date);
			SoaHelper.getProperties(window, "top_line");
			weldGroupBOMLine = (BOMLine)window.get_top_line();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if(window==null){
			return;
		}
		
		initSeqNo();
		
		cutDeleteTargetBOMLines(weldGroupBOMLine);
		
		addNewBOMLines(weldGroupBOMLine);

		// 삭제 추가된 내용이 있으므로 Window와 BOMLine을 Refresh 한다.
		try {
			SoaHelper.refresh(weldGroupBOMLine);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// 혹여 사용자 Operation으로 인한 변경으로 본의 아니게 삭제된 BOMLine이 있으면 추가해주고
		// Feature Name, End Point 변경된 것이 있으면 반영해줘야 한다.
		updateChangedFeatureNameAndPositionMatrix(weldGroupBOMLine);
		
		// 삭제 추가된 내용이 있으므로 Window와 BOMLine을 Refresh 한다.
		try {
			SoaHelper.refresh(weldGroupBOMLine);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// WELD_POINT_SAVED Table에 기록된 Delete 를 제외한 Row의 수와 Weld Group Revision을 구성하는 Child Node의 수가 일치하지 않음. 
		boolean isEquipollence = checkBOMLineCountEquipollence(weldGroupBOMLine);
		
		//변경된 BOM의 수가 다르므로 Exception을 발생 시키고 Interface에 오류가 있음을 알린다.
		if( window != null){
			// BOMWindow에 변경 내용을 저장한다.
			com.teamcenter.services.strong.cad.StructureManagementService aStructureManagementService = com.teamcenter.services.strong.cad.StructureManagementService.getService( connection );  
			aStructureManagementService.saveBOMWindows(new BOMWindow[]{window});
			com.teamcenter.services.strong.cad.StructureManagementService.getService( connection ).closeBOMWindows(new BOMWindow[]{window});
		}			

		if(isEquipollence==false){
			String errorMessage = this.weldGroupItemId+"/"+this.weldGroupItemRevId + " BOM line count is mismatched";
			throw new Exception( errorMessage );
		}

	}
	
	/**
	 * Occurrence Name에 등록된 순서에 따라 Sequence No를 부여하는데 이것을 위해
	 * 이미 등록된 최종 Seq No 부터 수를 증가시켜 가면서 사용할 수 있도록 이미 등록된 최대의 Seq No를 찾는다.
	 */
	private void initSeqNo(){
		WeldPointDataManager weldPointDataManager = new WeldPointDataManager(m_zTaskLogger, buffer, isDebug);
		
		// occurrence Name을 정할때 ex) 5060036000-WP-298
		// 마지막의 순번을 정해야하는데 이때 사용하기 위해 이미등록된  
		if(this.currentNewSeqNo<0){
			int maxSeqNo = weldPointDataManager.getMaxOccSeqNo(partItemId, partRevisionId);
			this.currentNewSeqNo = maxSeqNo+1;
		}
	}

	/**
	 * 삭제되는 용접점의 BOMLine을 제거 한다.
	 * @param parentBOMLine
	 */
	private void cutDeleteTargetBOMLines(BOMLine parentBOMLine){
		
		ArrayList<BOMLine> deleteTargetBOMLineList = new ArrayList<BOMLine>();
		
		// WeldGroup을 전개해서 삭제 대상인 BOMLine을 제거 하기위한 Data를 준비한다.
		ModelObject[] currentStructuredChildBOMLineModels = null;
		try {
			SoaHelper.getProperties(parentBOMLine, new String[]{"bl_child_lines"});
			currentStructuredChildBOMLineModels = parentBOMLine.get_bl_child_lines();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// 삭제 대상인 Data를 WELD_POINT_SAVED Table에 생성된 변경 내역중에 읽어 온다.
		Vector<String> deleteTargetBOMLineThreadIdV = getDeleteTargetBOMLineThreadUids();
		
		for (int i = 0;currentStructuredChildBOMLineModels!=null && i < currentStructuredChildBOMLineModels.length; i++) {

			if(currentStructuredChildBOMLineModels[i]==null){
				continue;
			}
			
			String occurrenceName = null;
			String occurrenceUid = null;
			String occurrenceThreadUid = null;

			BOMLine weldPointLine = (BOMLine)currentStructuredChildBOMLineModels[i];
			ModelObject realOccurrenceModelObject = null;
			try {
				SoaHelper.getProperties(weldPointLine, new String[]{"bl_real_occurrence"});
				realOccurrenceModelObject = weldPointLine.get_bl_real_occurrence();
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			if(realOccurrenceModelObject!=null){
				PSOccurrence realOccurrence = (PSOccurrence)realOccurrenceModelObject;
				try {
					SoaHelper.getProperties(realOccurrence, new String[]{"occurrence_name", "occ_thread"});
					occurrenceName = realOccurrence.get_occurrence_name();
					occurrenceUid = realOccurrence.getUid();
					
					// 삭제 대상으로 등록된 Data가 있는지 확인 한다.
					boolean isDeleteTarget = false;
					PSOccurrenceThread occurrenceThread = realOccurrence.get_occ_thread();
					if(occurrenceThread!=null){
						occurrenceThreadUid = occurrenceThread.getUid();
						if(deleteTargetBOMLineThreadIdV!=null &&deleteTargetBOMLineThreadIdV.contains(occurrenceThreadUid.trim())==true){
							deleteTargetBOMLineList.add(weldPointLine);
							addLog("** BOMLine Del : "+occurrenceName+" ["+occurrenceThreadUid+"]");
							isDeleteTarget = true;
						}
					}
					
					// 삭제대상인 경우 궂이 아래의 과정을 수행할 필요가 없다.
					if(isDeleteTarget==true){
						continue;
					}

					// Dispatcher가 중복 수행되면서 생성된 Data가 있으면 같이 삭제한다.
					int currentSeqNo = -1;
					String occSeq = null;
					int lastIndexOfString = occurrenceName.lastIndexOf("-");
					if(lastIndexOfString>1){
						occSeq = occurrenceName.substring((lastIndexOfString+1));
						if(occSeq!=null && occSeq.trim().length()>0){
							try {
								currentSeqNo = Integer.parseInt(occSeq);
							} catch (Exception e) {
							}
						}
					}
					
					if(currentSeqNo >= this.currentNewSeqNo){
						deleteTargetBOMLineList.add(weldPointLine);
					}
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
		}
		
		if(deleteTargetBOMLineList!=null){
			
			ModelObject[] deleteTargetBOMLineModels = new ModelObject[deleteTargetBOMLineList.size()];
			for (int i = 0; i < deleteTargetBOMLineModels.length; i++) {
				deleteTargetBOMLineModels[i] = deleteTargetBOMLineList.get(i);
			}

			addLog("==============================");
			addLog("= BOM Line Cut Summary");
			addLog("==============================");
			addLog("BOM Line cut target count : "+deleteTargetBOMLineList.size());
			
			com.teamcenter.services.loose.bom.StructureManagementService aStructureManagementService =  
					com.teamcenter.services.loose.bom.StructureManagementService.getService(connection);
			RemoveChildrenFromParentLineResponse aRemoveChildrenFromParentLineResponse = 
					aStructureManagementService.removeChildrenFromParentLine(deleteTargetBOMLineModels);
	
			if(aRemoveChildrenFromParentLineResponse!=null){
				int errorCount = aRemoveChildrenFromParentLineResponse.serviceData.sizeOfPartialErrors();
			}
		}
			
	}
	
	/**
	 * 추가되는 용접점 BOMLine을 생성 한다.
	 * @param parentBOMLine
	 */
	private void addNewBOMLines(BOMLine parentBOMLine){
		
		WeldPointDataManager weldPointDataManager = new WeldPointDataManager(m_zTaskLogger, buffer, isDebug);
		
		// occurrence Name을 정할때 ex) 5060036000-WP-298
		// 마지막의 순번을 정해야하는데 이때 사용하기 위해 이미등록된  
		//int maxSeqNo = weldPointDataManager.getMaxOccSeqNo(partItemId, partRevisionId);
		WeldInformation[] addTargetWeldInformationList = weldPointDataManager.getAddTargetWeldPointData(
				partItemId, partRevisionId);
		

		if(addTargetWeldInformationList==null || 
				(addTargetWeldInformationList!=null && addTargetWeldInformationList.length<1) ){
			return;
		}
		
		StructureManagement.ItemLineInfo[] childNodeItemLineInfoList = 
				new StructureManagement.ItemLineInfo[addTargetWeldInformationList.length];
		
		for (int i = 0; i < addTargetWeldInformationList.length; i++) {
			
			WeldInformation aWeldInformation = addTargetWeldInformationList[i];
			ItemRevision weldItemRevision = getTypedWeldItemRevision( aWeldInformation);
			
			
			String seqStr = String.format("%03d", this.currentNewSeqNo );
			String occName = partItemId.trim()  + "-WP-" + seqStr;
			String positionDesc = aWeldInformation.getEndPointX()+", "+aWeldInformation.getEndPointY()+", "+aWeldInformation.getEndPointZ()+", "+aWeldInformation.getWeldLength();
			
			// -----------------------------------------------
			// 추가될 용접점의 Properties를 정의하고
			// BOMLine으로 추가되도록 childNodeItemLineInfoList에 추가한다.
			// -----------------------------------------------
			HashMap itemLineProperties = new HashMap();
			// weld Type별로 계산된 position matrix를 가져온다.
			String madePositionMatrixString = getMatrixString(aWeldInformation);
			
			itemLineProperties.put("bl_plmxml_occ_xform", madePositionMatrixString);
			itemLineProperties.put("bl_occurrence_name", occName);
			itemLineProperties.put("M7_FEATURE_NAME",  aWeldInformation.featureName);
			itemLineProperties.put("S7_POSITION_DESC",  positionDesc);
			
			//addLog("@@ bl_plmxml_occ_xform = " + itemLineProperties.get("bl_plmxml_occ_xform") );
			//addLog("@@ bl_occurrence_name = " + itemLineProperties.get("bl_occurrence_name") );
			//addLog("@@ M7_FEATURE_NAME = " + itemLineProperties.get("M7_FEATURE_NAME") );
			//addLog("@@ S7_POSITION_DESC = " + itemLineProperties.get("S7_POSITION_DESC") );
			
			//if(weldItemRevision==null){
			//	addLog("@@ weldItemRevision = null");
			//}else{
			//	addLog("@@ weldItemRevision = "+weldItemRevision);
			//}
			
			StructureManagement.ItemLineInfo aItemLineInfo = new StructureManagement.ItemLineInfo();
			aItemLineInfo.bomline = null;
			aItemLineInfo.clientId = "";		// 생성이므로 비워 놓으면 될것 같음, 수정인 경우에는 ??
			aItemLineInfo.itemRev = weldItemRevision;
			aItemLineInfo.item = null;
			aItemLineInfo.itemLineProperties = itemLineProperties;
			
			addLog("** BOMLine Add : "+aWeldInformation.weldType+"-"+aWeldInformation.getSheets()+"-"+aWeldInformation.getSecureType()+", Occ Name = "+occName+",  Feature Name = "+aWeldInformation.featureName);
			
			childNodeItemLineInfoList[i] = aItemLineInfo;
			
			this.currentNewSeqNo++;
		}
		
		if(childNodeItemLineInfoList!=null && parentBOMLine!=null){
			
			AddOrUpdateChildrenToParentLineInfo aAddOrUpdateChildrenToParentLineInfo = new AddOrUpdateChildrenToParentLineInfo();
			aAddOrUpdateChildrenToParentLineInfo.parentLine = parentBOMLine;
			aAddOrUpdateChildrenToParentLineInfo.viewType = "view";
			aAddOrUpdateChildrenToParentLineInfo.items = childNodeItemLineInfoList;
			
			addLog("==============================");
			addLog("= BOM Line Add Summary");
			addLog("==============================");
			addLog("Quantity to add = "+childNodeItemLineInfoList.length);
			addLog("Parent BOMLine : "+this.weldGroupItemId+"/"+this.weldGroupItemRevId);

			com.teamcenter.services.loose.bom.StructureManagementService structureManagementService =  
					com.teamcenter.services.loose.bom.StructureManagementService.getService(connection);
			if(structureManagementService!=null){
				structureManagementService.addOrUpdateChildrenToParentLine(new AddOrUpdateChildrenToParentLineInfo[]{ aAddOrUpdateChildrenToParentLineInfo });
			}else{
				addLog("Exception : StructureManagementService is null");
			}
		}

		
	}

	/**
	 * Feature Name이 변경되거나 종료점의 위치가 변경된 경우 이것을 반영해주는 함수임.
	 * 혹시나 있을지 모를 누락된 용접점 정보가 있으면 추가로 생성 해준다.
	 * 이것은 Dispatcher 수행과정에 발생된 용접점 생성오류 Data의 보정 기능임.
	 * @param weldGroupBOMLine
	 */
	private void updateChangedFeatureNameAndPositionMatrix(BOMLine weldGroupBOMLine){
		
		WeldPointDataManager weldPointDataManager = new WeldPointDataManager(m_zTaskLogger, buffer, isDebug);
		HashMap<String, WeldInformation> updateTargetWeldInformationHash = 
				weldPointDataManager.getUpdateTargetBOMLineData(partItemId, partRevisionId);
		
		if(updateTargetWeldInformationHash==null || (updateTargetWeldInformationHash!=null && updateTargetWeldInformationHash.size()<1)){
			return;
		}

		//변경대상 Data Properties를 만들어야 한다.
		Vector<StructureManagement.ItemLineInfo> updateTargetDataV = new Vector<StructureManagement.ItemLineInfo>();
		
		if(weldGroupBOMLine!=null){

			// Child Node를 전개한다.
			ModelObject[] childNodeObjects = null;
			try {
				SoaHelper.getProperties(weldGroupBOMLine, new String[]{"bl_child_lines"});
				childNodeObjects = weldGroupBOMLine.get_bl_child_lines();
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			// 존재하는 BOM Line 구성중 변경할 Data를 찾아서 변경한다.
			for( ModelObject childObj : childNodeObjects){
				
				BOMLine childBOMLine = (BOMLine)childObj;
				String bl_occ_xform = null;
				String feature_name = null;
				
				String occurrenceName = null;
				String occurrenceUid = null;
				String occurrenceThreadUid = null;
				
				try {
					SoaHelper.getProperties(childBOMLine, new String[]{
							"bl_real_occurrence", "bl_plmxml_occ_xform",
							"M7_FEATURE_NAME", "S7_POSITION_DESC" 
					});
					
					bl_occ_xform = childBOMLine.get_bl_plmxml_occ_xform();
					feature_name = childBOMLine.getPropertyObject("M7_FEATURE_NAME").getStringValue();
					
					ModelObject realOccurrenceModelObject = childBOMLine.get_bl_real_occurrence();
					if(realOccurrenceModelObject!=null){
								
						PSOccurrence realOccurrence = (PSOccurrence)realOccurrenceModelObject;
						SoaHelper.getProperties(realOccurrence, new String[]{"occurrence_name", "occ_thread"});
						occurrenceName = realOccurrence.get_occurrence_name();
						occurrenceUid = realOccurrence.getUid();
						PSOccurrenceThread occurrenceThread = realOccurrence.get_occ_thread();
						if(occurrenceThread!=null){
							occurrenceThreadUid = occurrenceThread.getUid();
						}

					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				if(occurrenceThreadUid!=null){
					WeldInformation weldInformation = updateTargetWeldInformationHash.get(occurrenceThreadUid);
					if(weldInformation==null){
						continue;
					}
					
					boolean isFeatureNameChange = false;
					boolean isEndPointChange = false;
					
					String oldFeatureName = weldInformation.oldFeatureName;
					String newFeatureName = weldInformation.featureName;
					if(oldFeatureName!=null && feature_name!=null && feature_name.trim().equalsIgnoreCase(oldFeatureName.trim())==true){
						if(newFeatureName!=null && newFeatureName.trim().equalsIgnoreCase(oldFeatureName.trim())==false){
							// Feature Name이 변경된 경우임
							isFeatureNameChange = true;
						}
					}

					if(weldInformation.changeMemo!=null && weldInformation.changeMemo.trim().equalsIgnoreCase("END_DIFF")==true){
						// 시작점과 종점중 종점이 변경된 경우임.
						isEndPointChange = true;
					}
					
					//updateTargetDataV에 Data 추가하고 updateTargetWeldInformationHash에서 Data 제거
					if(isFeatureNameChange==true || isEndPointChange==true){
						StructureManagement.ItemLineInfo aItemLineInfo = new StructureManagement.ItemLineInfo();
						aItemLineInfo.bomline = childBOMLine;
						aItemLineInfo.clientId = "";		// 생성이므로 비워 놓았는데, 수정인 경우에는 ??
						aItemLineInfo.itemRev = null;
						aItemLineInfo.item = null;
						
						// -----------------------------------------------
						// 변경될될 용접점의 Properties를 정의하고
						// BOMLine으로 추가되도록 childNodeItemLineInfoList에 추가한다.
						// -----------------------------------------------
						HashMap itemLineProperties = new HashMap();

						// Feature name이 변경된 것을 반영한다.
						if(isFeatureNameChange==true){
							itemLineProperties.put("M7_FEATURE_NAME",  weldInformation.featureName);
						}
						
						// Co2 용접의 종료점에 변경이 있는것을 반영한다.
						if(isEndPointChange==true){
							// weld Type별로 계산된 position matrix를 가져온다.
							String madePositionMatrixString = getMatrixString(weldInformation);
							String positionDesc = weldInformation.getEndPointX()+", "+weldInformation.getEndPointY()+", "+weldInformation.getEndPointZ()+", "+weldInformation.getWeldLength();
							itemLineProperties.put("bl_plmxml_occ_xform", madePositionMatrixString);
							itemLineProperties.put("S7_POSITION_DESC",  positionDesc);
						}
						
						aItemLineInfo.itemLineProperties = itemLineProperties;
						
						// 변경정보를 저장할 Vector 에는 Data를 추가하고 HashsTable에서는 해당 Data 제거한다.
						updateTargetDataV.add(aItemLineInfo);
						updateTargetWeldInformationHash.remove(occurrenceThreadUid);
						
						addLog("** BOMLine Update : "+occurrenceName+" ["+occurrenceThreadUid+"] "+oldFeatureName+"->"+newFeatureName+" End point change="+isEndPointChange);
					}
				}
				
			}
		}
		
		// 만약에 BOM Line 구성에는 없는데 변경된 Data가 존재하는 경우 해당 내용을 새로 생성 해주기위한
		// Data를 생성한다. -> 필요없는 기능으로 판단해서 우선 Remark 함.
		/*
		if(updateTargetWeldInformationHash!=null && updateTargetWeldInformationHash.size()>0){
			
			Set<String> keySet = updateTargetWeldInformationHash.keySet();
			Iterator<String> keyNameIterator = keySet.iterator();
			
			while (keyNameIterator.hasNext()) {
				String occurrenceThreadUid = (String) keyNameIterator.next();

				WeldInformation aWeldInformation = updateTargetWeldInformationHash.get(occurrenceThreadUid);
				ItemRevision weldItemRevision = getTypedWeldItemRevision( aWeldInformation);
				
				String seqStr = String.format("%03d", this.currentNewSeqNo );
				String occName = partItemId.trim()  + "-WP-" + seqStr;
				String positionDesc = aWeldInformation.getEndPointX()+", "+aWeldInformation.getEndPointY()+", "+aWeldInformation.getEndPointZ()+", "+aWeldInformation.getWeldLength();
				
				// -----------------------------------------------
				// 추가될 용접점의 Properties를 정의하고
				// BOMLine으로 추가되도록 childNodeItemLineInfoList에 추가한다.
				// -----------------------------------------------
				HashMap itemLineProperties = new HashMap();
				// weld Type별로 계산된 position matrix를 가져온다.
				String madePositionMatrixString = getMatrixString(aWeldInformation);
				itemLineProperties.put("bl_plmxml_occ_xform", madePositionMatrixString);
				itemLineProperties.put("bl_occurrence_name", occName);
				itemLineProperties.put("M7_FEATURE_NAME",  aWeldInformation.featureName);
				itemLineProperties.put("S7_POSITION_DESC",  positionDesc);
				
				StructureManagement.ItemLineInfo aItemLineInfo = new StructureManagement.ItemLineInfo();
				aItemLineInfo.bomline = null;
				aItemLineInfo.clientId = "";		// 생성이므로 비워 놓으면 될것 같음, 수정인 경우에는 ??
				aItemLineInfo.itemRev = weldItemRevision;
				aItemLineInfo.item = null;
				aItemLineInfo.itemLineProperties = itemLineProperties;
				
				updateTargetDataV.add(aItemLineInfo);
				
				this.currentNewSeqNo++;
			}

		}
		*/

		if(updateTargetDataV!=null && updateTargetDataV.size()>0){
			
			StructureManagement.ItemLineInfo[] itemLineInfoList = new StructureManagement.ItemLineInfo[updateTargetDataV.size()];
			for (int i = 0;updateTargetDataV!=null && i < updateTargetDataV.size(); i++) {
				itemLineInfoList[i] = updateTargetDataV.get(i);
			}
			
			addLog("==============================");
			addLog("= BOM Line Update Summary");
			addLog("==============================");
			addLog("BOM Line update target count : "+updateTargetDataV.size());
			
			AddOrUpdateChildrenToParentLineInfo aAddOrUpdateChildrenToParentLineInfo = new AddOrUpdateChildrenToParentLineInfo();
			aAddOrUpdateChildrenToParentLineInfo.parentLine = weldGroupBOMLine;
			aAddOrUpdateChildrenToParentLineInfo.viewType = "view";
			aAddOrUpdateChildrenToParentLineInfo.items = itemLineInfoList;
			
			com.teamcenter.services.loose.bom.StructureManagementService structureManagementService =  
					com.teamcenter.services.loose.bom.StructureManagementService.getService(connection);
			if(structureManagementService!=null){
				structureManagementService.addOrUpdateChildrenToParentLine(new AddOrUpdateChildrenToParentLineInfo[]{ aAddOrUpdateChildrenToParentLineInfo });
			}
		}
		
	}
	
	private String getMatrixString(WeldInformation aWeldInformation){
		
		// 모델링된 Co2, Brazing 용접의 지름이8mm 길이가 10mm 임.
		// 참고로 다른 구를 기반한 용접점의 외경은 10mm 임.
		double orignDiameter= 8.0d;
		double orignLength = 10.0d;
		double tragetDiameter = 8.0d;
		// targetLength를 0으로 설정하면 계산된 길이를 Return 해준다.
		double targetLength = 0.0d;
		double unitScale = 1.0d;
		
		String defaultMatrixString = "1 0 0 0 0 1 0 0 0 0 1 0 0 0 0 1";
		
		String madePositionMatrixString = null;
		if(aWeldInformation.weldType==null || (aWeldInformation.weldType!=null && aWeldInformation.weldType.trim().length()<1)){
			madePositionMatrixString = defaultMatrixString;
		}
		
		if(aWeldInformation.weldType.trim().equalsIgnoreCase("CO2") || aWeldInformation.weldType.trim().equalsIgnoreCase("BRAZING")){
			
			targetLength = aWeldInformation.getWeldLength();
			
			// 계산된 Position Matrix를 가져온다.
			madePositionMatrixString = xFormMatrixUtil.getFormMatrix(
					aWeldInformation.getStartPointX(), aWeldInformation.getStartPointY(), aWeldInformation.getStartPointZ(), 
					aWeldInformation.getEndPointX(),  aWeldInformation.getEndPointY(), aWeldInformation.getEndPointZ(), 
					orignDiameter, orignLength, 
					tragetDiameter, targetLength, unitScale);
			
		}else{
			// 구형태의 용접점 이므로 시작점의 위치만 지정하면 된다.
			madePositionMatrixString = 
					  "1 0 0 0 "
					+ "0 1 0 0 "
					+ "0 0 1 0 " 
					+ aWeldInformation.getStartPointX() + " " + aWeldInformation.getStartPointY() + " " + aWeldInformation.getStartPointZ() + " 1";
		}
		
		return madePositionMatrixString;
	}
	
	/**
	 * Feature Name이 변경 되거나 Position의 End Point가 변경된 BOMLine의 정보를 Update 한다.
	 * @param msg
	 */
	private void addLog(String msg){
		if( isDebug ){
			m_zTaskLogger.info(msg);
			buffer.append(msg);
			buffer.append("\r\n");
		}
	}
	
	/**
	 * CSV 파일을 읽어 만든 Data와 기존 Weld Group의 BOM Structure를 읽어 만든 Data를 비교해서 만든
	 *  WELD_POINT_SAVED Table에서 삭제 대상 Data의 OCC_THREAD_PUID 값을 읽어 Vector에 담아 Return 한다.
	 *  
	 * @return
	 */
	private Vector<String> getDeleteTargetBOMLineThreadUids(){
		
		Vector<String> deleteTargetBOMLineThreadIdV = new Vector<String>();
		
		DataSet ds = new DataSet();
	    ds.put("part_item_id", partItemId);
	    ds.put("part_rev_id", partRevisionId);
        
        HashMap<String, String> weldGroupDataHashMap = null;
        
        ArrayList<HashMap> resultList = null;
		try {
			resultList =  (ArrayList<HashMap>) Util.execute(
					servletUrlStr, 
					serviceClassName,
					"getDeleteTargetBOMLineData",
					ds,
					true);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if(resultList==null || (resultList!=null && resultList.size()<1)){
			deleteTargetBOMLineThreadIdV = null;
		}
		
		for (int i = 0;resultList!=null && i < resultList.size(); i++) {
			
			HashMap rowDataHash = resultList.get(i);
			if(rowDataHash != null){

//				Object weldTypeObject = rowDataHash.get("WELD_TYPE");
//				Object secureTypeObject = rowDataHash.get("SECURE_TYPE");
//				Object sheetsObject = rowDataHash.get("SHEETS");
//				Object freatureNameObject = rowDataHash.get("FEATURE_NAME");
//				Object occNameObject = rowDataHash.get("OCC_NAME");
				Object occThreadUidObject = rowDataHash.get("THREAD_UID");
				
				String occThreadUid = null;

				if(occThreadUidObject!=null){
					occThreadUid = occThreadUidObject.toString();
					if(occThreadUid!=null && occThreadUid.trim().length()>0){
						if(deleteTargetBOMLineThreadIdV.contains(occThreadUid.trim())==false){
							deleteTargetBOMLineThreadIdV.add(occThreadUid.trim());
						}
					}
				}
			}
		}
		
		return deleteTargetBOMLineThreadIdV;
	}
	

	
	private ItemRevision getTypedWeldItemRevision(WeldInformation weldInformation){
		
		ItemRevision weldItemRevision = null;
		
		String secureType = weldInformation.getSecureType();
		String weldType = weldInformation.weldType;
		int sheets = weldInformation.getSheets();
		
		if(secureType==null || (secureType!=null && secureType.trim().length()<1)){
			secureType = "CO";
		}
		
		String weldPointItemId = null;
		weldPointItemId = "WELDPOINT-"+weldType.trim().toUpperCase()+"-"+sheets+"-"+secureType.trim().toUpperCase();
		
		//addLog("@@@@@@ weldTypeItemId = "+weldPointItemId);
		
		try {
			weldItemRevision = Util.getLatestRevision(connection, weldPointItemId);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return weldItemRevision;
	}
	
	private boolean checkBOMLineCountEquipollence(BOMLine weldGroupBOMLine){
		boolean isEquipollence = false;
		
		WeldPointDataManager weldPointDataManager = new WeldPointDataManager(m_zTaskLogger, buffer, isDebug);
		int dbBOMLineCount = weldPointDataManager.getAllNewBOMLineCount(partItemId, partRevisionId);
		
		// Child Node의 수를 확인 한다.
		ModelObject[] currentStructuredChildBOMLineModels = null;
		try {
			SoaHelper.getProperties(weldGroupBOMLine, new String[]{"bl_child_lines"});
			currentStructuredChildBOMLineModels = weldGroupBOMLine.get_bl_child_lines();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		int bomLineCount = 0;
		if(currentStructuredChildBOMLineModels!=null && currentStructuredChildBOMLineModels.length>0){
			bomLineCount = currentStructuredChildBOMLineModels.length;
		}
		addLog("BOM Line Count : " + bomLineCount );
		addLog("DB BOMLineCount : " + dbBOMLineCount);
		addLog("DB BOMLine Target : " + partItemId + "/" + partRevisionId);
		if(bomLineCount==dbBOMLineCount){
			isEquipollence = true;
		}
		
		return isEquipollence;
	}
	
}

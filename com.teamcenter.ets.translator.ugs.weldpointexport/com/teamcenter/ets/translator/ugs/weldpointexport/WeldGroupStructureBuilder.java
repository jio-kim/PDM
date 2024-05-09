package com.teamcenter.ets.translator.ugs.weldpointexport;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Vector;

import com.kgm.common.remote.DataSet;
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
 * Weld Gropup�� BOM Structure�� �����ϴ� ��Ȱ�� �����ϴ� Class
 * 
 * lib�� TcSoaBOMLoose_10000.1.0.jar, TcSoaBOMTypes_10000.1.0.jar�� �߰��� ��� �Ѵ�.
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
	private String serviceClassName = "com.kgm.service.WeldPoint2ndService";
	
	String partItemId  = null;
	String partRevisionId = null; 
	ItemRevision newWeldGroupRevision = null;
	Connection connection = null;
	
	String weldGroupItemId = null;
	String weldGroupItemRevId = null;
	
	XFormMatrixUtil xFormMatrixUtil = null;
	
	private int currentNewSeqNo = -1;
	
	/**
	 * Clsss ������
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
	 * CSV ���ϰ� Weld Group Item���� ���� Revision�� Structure Data�� ���ؼ� ���� Data�� �̿��� ���ο� Weld Group Item Revision��
	 * BOM Structure�� �����ϴ� ����� �����ϴ� �Լ�
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

		// ���� �߰��� ������ �����Ƿ� Window�� BOMLine�� Refresh �Ѵ�.
		try {
			SoaHelper.refresh(weldGroupBOMLine);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// Ȥ�� ����� Operation���� ���� �������� ���� �ƴϰ� ������ BOMLine�� ������ �߰����ְ�
		// Feature Name, End Point ����� ���� ������ �ݿ������ �Ѵ�.
		updateChangedFeatureNameAndPositionMatrix(weldGroupBOMLine);
		
		// ���� �߰��� ������ �����Ƿ� Window�� BOMLine�� Refresh �Ѵ�.
		try {
			SoaHelper.refresh(weldGroupBOMLine);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// WELD_POINT_SAVED Table�� ��ϵ� Delete �� ������ Row�� ���� Weld Group Revision�� �����ϴ� Child Node�� ���� ��ġ���� ����. 
		boolean isEquipollence = checkBOMLineCountEquipollence(weldGroupBOMLine);
		
		//����� BOM�� ���� �ٸ��Ƿ� Exception�� �߻� ��Ű�� Interface�� ������ ������ �˸���.
		if( window != null){
			// BOMWindow�� ���� ������ �����Ѵ�.
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
	 * Occurrence Name�� ��ϵ� ������ ���� Sequence No�� �ο��ϴµ� �̰��� ����
	 * �̹� ��ϵ� ���� Seq No ���� ���� �������� ���鼭 ����� �� �ֵ��� �̹� ��ϵ� �ִ��� Seq No�� ã�´�.
	 */
	private void initSeqNo(){
		WeldPointDataManager weldPointDataManager = new WeldPointDataManager(m_zTaskLogger, buffer, isDebug);
		
		// occurrence Name�� ���Ҷ� ex) 5060036000-WP-298
		// �������� ������ ���ؾ��ϴµ� �̶� ����ϱ� ���� �̵̹�ϵ�  
		if(this.currentNewSeqNo<0){
			int maxSeqNo = weldPointDataManager.getMaxOccSeqNo(partItemId, partRevisionId);
			this.currentNewSeqNo = maxSeqNo+1;
		}
	}

	/**
	 * �����Ǵ� �������� BOMLine�� ���� �Ѵ�.
	 * @param parentBOMLine
	 */
	private void cutDeleteTargetBOMLines(BOMLine parentBOMLine){
		
		ArrayList<BOMLine> deleteTargetBOMLineList = new ArrayList<BOMLine>();
		
		// WeldGroup�� �����ؼ� ���� ����� BOMLine�� ���� �ϱ����� Data�� �غ��Ѵ�.
		ModelObject[] currentStructuredChildBOMLineModels = null;
		try {
			SoaHelper.getProperties(parentBOMLine, new String[]{"bl_child_lines"});
			currentStructuredChildBOMLineModels = parentBOMLine.get_bl_child_lines();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// ���� ����� Data�� WELD_POINT_SAVED Table�� ������ ���� �����߿� �о� �´�.
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
					
					// ���� ������� ��ϵ� Data�� �ִ��� Ȯ�� �Ѵ�.
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
					
					// ��������� ��� ���� �Ʒ��� ������ ������ �ʿ䰡 ����.
					if(isDeleteTarget==true){
						continue;
					}

					// Dispatcher�� �ߺ� ����Ǹ鼭 ������ Data�� ������ ���� �����Ѵ�.
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
	 * �߰��Ǵ� ������ BOMLine�� ���� �Ѵ�.
	 * @param parentBOMLine
	 */
	private void addNewBOMLines(BOMLine parentBOMLine){
		
		WeldPointDataManager weldPointDataManager = new WeldPointDataManager(m_zTaskLogger, buffer, isDebug);
		
		// occurrence Name�� ���Ҷ� ex) 5060036000-WP-298
		// �������� ������ ���ؾ��ϴµ� �̶� ����ϱ� ���� �̵̹�ϵ�  
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
			// �߰��� �������� Properties�� �����ϰ�
			// BOMLine���� �߰��ǵ��� childNodeItemLineInfoList�� �߰��Ѵ�.
			// -----------------------------------------------
			HashMap itemLineProperties = new HashMap();
			// weld Type���� ���� position matrix�� �����´�.
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
			aItemLineInfo.clientId = "";		// �����̹Ƿ� ��� ������ �ɰ� ����, ������ ��쿡�� ??
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
	 * Feature Name�� ����ǰų� �������� ��ġ�� ����� ��� �̰��� �ݿ����ִ� �Լ���.
	 * Ȥ�ó� ������ �� ������ ������ ������ ������ �߰��� ���� ���ش�.
	 * �̰��� Dispatcher ��������� �߻��� ������ �������� Data�� ���� �����.
	 * @param weldGroupBOMLine
	 */
	private void updateChangedFeatureNameAndPositionMatrix(BOMLine weldGroupBOMLine){
		
		WeldPointDataManager weldPointDataManager = new WeldPointDataManager(m_zTaskLogger, buffer, isDebug);
		HashMap<String, WeldInformation> updateTargetWeldInformationHash = 
				weldPointDataManager.getUpdateTargetBOMLineData(partItemId, partRevisionId);
		
		if(updateTargetWeldInformationHash==null || (updateTargetWeldInformationHash!=null && updateTargetWeldInformationHash.size()<1)){
			return;
		}

		//������ Data Properties�� ������ �Ѵ�.
		Vector<StructureManagement.ItemLineInfo> updateTargetDataV = new Vector<StructureManagement.ItemLineInfo>();
		
		if(weldGroupBOMLine!=null){

			// Child Node�� �����Ѵ�.
			ModelObject[] childNodeObjects = null;
			try {
				SoaHelper.getProperties(weldGroupBOMLine, new String[]{"bl_child_lines"});
				childNodeObjects = weldGroupBOMLine.get_bl_child_lines();
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			// �����ϴ� BOM Line ������ ������ Data�� ã�Ƽ� �����Ѵ�.
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
							// Feature Name�� ����� �����
							isFeatureNameChange = true;
						}
					}

					if(weldInformation.changeMemo!=null && weldInformation.changeMemo.trim().equalsIgnoreCase("END_DIFF")==true){
						// �������� ������ ������ ����� �����.
						isEndPointChange = true;
					}
					
					//updateTargetDataV�� Data �߰��ϰ� updateTargetWeldInformationHash���� Data ����
					if(isFeatureNameChange==true || isEndPointChange==true){
						StructureManagement.ItemLineInfo aItemLineInfo = new StructureManagement.ItemLineInfo();
						aItemLineInfo.bomline = childBOMLine;
						aItemLineInfo.clientId = "";		// �����̹Ƿ� ��� ���Ҵµ�, ������ ��쿡�� ??
						aItemLineInfo.itemRev = null;
						aItemLineInfo.item = null;
						
						// -----------------------------------------------
						// ����ɵ� �������� Properties�� �����ϰ�
						// BOMLine���� �߰��ǵ��� childNodeItemLineInfoList�� �߰��Ѵ�.
						// -----------------------------------------------
						HashMap itemLineProperties = new HashMap();

						// Feature name�� ����� ���� �ݿ��Ѵ�.
						if(isFeatureNameChange==true){
							itemLineProperties.put("M7_FEATURE_NAME",  weldInformation.featureName);
						}
						
						// Co2 ������ �������� ������ �ִ°��� �ݿ��Ѵ�.
						if(isEndPointChange==true){
							// weld Type���� ���� position matrix�� �����´�.
							String madePositionMatrixString = getMatrixString(weldInformation);
							String positionDesc = weldInformation.getEndPointX()+", "+weldInformation.getEndPointY()+", "+weldInformation.getEndPointZ()+", "+weldInformation.getWeldLength();
							itemLineProperties.put("bl_plmxml_occ_xform", madePositionMatrixString);
							itemLineProperties.put("S7_POSITION_DESC",  positionDesc);
						}
						
						aItemLineInfo.itemLineProperties = itemLineProperties;
						
						// ���������� ������ Vector ���� Data�� �߰��ϰ� HashsTable������ �ش� Data �����Ѵ�.
						updateTargetDataV.add(aItemLineInfo);
						updateTargetWeldInformationHash.remove(occurrenceThreadUid);
						
						addLog("** BOMLine Update : "+occurrenceName+" ["+occurrenceThreadUid+"] "+oldFeatureName+"->"+newFeatureName+" End point change="+isEndPointChange);
					}
				}
				
			}
		}
		
		// ���࿡ BOM Line �������� ���µ� ����� Data�� �����ϴ� ��� �ش� ������ ���� ���� ���ֱ�����
		// Data�� �����Ѵ�. -> �ʿ���� ������� �Ǵ��ؼ� �켱 Remark ��.
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
				// �߰��� �������� Properties�� �����ϰ�
				// BOMLine���� �߰��ǵ��� childNodeItemLineInfoList�� �߰��Ѵ�.
				// -----------------------------------------------
				HashMap itemLineProperties = new HashMap();
				// weld Type���� ���� position matrix�� �����´�.
				String madePositionMatrixString = getMatrixString(aWeldInformation);
				itemLineProperties.put("bl_plmxml_occ_xform", madePositionMatrixString);
				itemLineProperties.put("bl_occurrence_name", occName);
				itemLineProperties.put("M7_FEATURE_NAME",  aWeldInformation.featureName);
				itemLineProperties.put("S7_POSITION_DESC",  positionDesc);
				
				StructureManagement.ItemLineInfo aItemLineInfo = new StructureManagement.ItemLineInfo();
				aItemLineInfo.bomline = null;
				aItemLineInfo.clientId = "";		// �����̹Ƿ� ��� ������ �ɰ� ����, ������ ��쿡�� ??
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
		
		// �𵨸��� Co2, Brazing ������ ������8mm ���̰� 10mm ��.
		// ����� �ٸ� ���� ����� �������� �ܰ��� 10mm ��.
		double orignDiameter= 8.0d;
		double orignLength = 10.0d;
		double tragetDiameter = 8.0d;
		// targetLength�� 0���� �����ϸ� ���� ���̸� Return ���ش�.
		double targetLength = 0.0d;
		double unitScale = 1.0d;
		
		String defaultMatrixString = "1 0 0 0 0 1 0 0 0 0 1 0 0 0 0 1";
		
		String madePositionMatrixString = null;
		if(aWeldInformation.weldType==null || (aWeldInformation.weldType!=null && aWeldInformation.weldType.trim().length()<1)){
			madePositionMatrixString = defaultMatrixString;
		}
		
		if(aWeldInformation.weldType.trim().equalsIgnoreCase("CO2") || aWeldInformation.weldType.trim().equalsIgnoreCase("BRAZING")){
			
			targetLength = aWeldInformation.getWeldLength();
			
			// ���� Position Matrix�� �����´�.
			madePositionMatrixString = xFormMatrixUtil.getFormMatrix(
					aWeldInformation.getStartPointX(), aWeldInformation.getStartPointY(), aWeldInformation.getStartPointZ(), 
					aWeldInformation.getEndPointX(),  aWeldInformation.getEndPointY(), aWeldInformation.getEndPointZ(), 
					orignDiameter, orignLength, 
					tragetDiameter, targetLength, unitScale);
			
		}else{
			// �������� ������ �̹Ƿ� �������� ��ġ�� �����ϸ� �ȴ�.
			madePositionMatrixString = 
					  "1 0 0 0 "
					+ "0 1 0 0 "
					+ "0 0 1 0 " 
					+ aWeldInformation.getStartPointX() + " " + aWeldInformation.getStartPointY() + " " + aWeldInformation.getStartPointZ() + " 1";
		}
		
		return madePositionMatrixString;
	}
	
	/**
	 * Feature Name�� ���� �ǰų� Position�� End Point�� ����� BOMLine�� ������ Update �Ѵ�.
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
	 * CSV ������ �о� ���� Data�� ���� Weld Group�� BOM Structure�� �о� ���� Data�� ���ؼ� ����
	 *  WELD_POINT_SAVED Table���� ���� ��� Data�� OCC_THREAD_PUID ���� �о� Vector�� ��� Return �Ѵ�.
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
		
		// Child Node�� ���� Ȯ�� �Ѵ�.
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

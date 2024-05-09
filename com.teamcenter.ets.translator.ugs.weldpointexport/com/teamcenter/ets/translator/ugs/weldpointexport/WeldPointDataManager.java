package com.teamcenter.ets.translator.ugs.weldpointexport;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.Vector;

import com.kgm.common.remote.DataSet;
import com.teamcenter.ets.soa.SoaHelper;
import com.teamcenter.soa.client.Connection;
import com.teamcenter.soa.client.model.ModelObject;
import com.teamcenter.soa.client.model.Property;
import com.teamcenter.soa.client.model.strong.Item;
import com.teamcenter.soa.client.model.strong.ItemRevision;
import com.teamcenter.tstk.util.log.ITaskLogger;

/**
 * �������� �����ϴµ� ���Ǵ� ��κ��� Query���� �����ϰ� �� ����� Ȯ�� �ϴµ� ���Ǵ� Query�� ��� ���� Classs
 * [NON-SR][20160503] Taeku.Jeong
 * @author Taeku
 *
 */
public class WeldPointDataManager {
	
	private Properties prop;
	private String serviceClassName = "com.kgm.service.WeldPoint2ndService";
	private String servletUrlStr;
	private double allowanceDistance = 0.004;
	
	ITaskLogger m_zTaskLogger;
	StringBuffer buffer;
	boolean isDebug = false;
	
	/**
	 * ������
	 * @param m_zTaskLogger
	 * @param buffer
	 * @param isDebug
	 */
	public WeldPointDataManager(ITaskLogger m_zTaskLogger, StringBuffer buffer, boolean isDebug){
		this.m_zTaskLogger = m_zTaskLogger;
		this.buffer = buffer;
		this.isDebug = isDebug;
		
		// ���� ���� ȣ��Ǵ� Method
		try {
			prop = Util.getDefaultProperties("weldpointexport");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		servletUrlStr = prop.getProperty("servlet.url");

	}
	
	public void clearWeldPointDataRow(String partItemId, String parRevisionId) throws Exception{
		
		DataSet ds = new DataSet();
	    ds.put("part_item_id", partItemId);
	    ds.put("part_rev_id", parRevisionId);
		
		try {
			Util.execute(servletUrlStr, 
					serviceClassName, 
					"deleteWeldPointRawData",
					ds,
					false);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			Util.execute(servletUrlStr, 
					serviceClassName,
					"deleteWeldPointRaw2Data",
					ds,
					false);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			Util.execute(servletUrlStr, 
					serviceClassName,
					"deleteBOMWeldPointData",
					ds,
					false);
		} catch (Exception e) {
			e.printStackTrace();
		}
			
		try {
			Util.execute(servletUrlStr, 
					serviceClassName,
					"deleteBOMWeldPoint2Data",
					ds,
					false);
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			Util.execute(
					servletUrlStr, 
					serviceClassName, 
					"deleteCurrentInboundData",
					ds,
					false);
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			Util.execute(
					servletUrlStr, 
					serviceClassName, 
					"deleteCurrentSavedData",
					ds,
					false);
		} catch (Exception e) {
			e.printStackTrace();
		}
	
	}

	public void saveReadCSVFileWeldDataRow(WeldInformation rowOfWeldData, String ecoId) throws Exception{
		
		DataSet ds = new DataSet();
	    ds.put("part_item_id", rowOfWeldData.partItemId);
	    ds.put("part_rev_id", rowOfWeldData.partItemRevId);
	    ds.put("eco_id", ecoId);
	    ds.put("feature_name", rowOfWeldData.featureName);
	    ds.put("weld_type", rowOfWeldData.weldType);
	    ds.put("secure_type", rowOfWeldData.getSecureType());
	    ds.put("start_point_x", new BigDecimal(rowOfWeldData.getStartPointX()) );
	    ds.put("start_point_y", new BigDecimal(rowOfWeldData.getStartPointY()) );
	    ds.put("start_point_z", new BigDecimal(rowOfWeldData.getStartPointZ()) );
	    ds.put("end_point_x", new BigDecimal(rowOfWeldData.getEndPointX()) );
	    ds.put("end_point_y", new BigDecimal(rowOfWeldData.getEndPointY()) );
	    ds.put("end_point_z", new BigDecimal(rowOfWeldData.getEndPointZ()) );
	    ds.put("weld_length", new BigDecimal(rowOfWeldData.getWeldLength()) );
	    ds.put("sheets", new BigDecimal(rowOfWeldData.getSheets()) );
	    
		try {
			Util.execute(servletUrlStr, 
					serviceClassName,
					"insertWeldPointRawDataRow",
					ds,
					false);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	public void translateWeldPointDataRaw2(String partItemId, String parRevisionId) throws Exception{
		
		DataSet ds = new DataSet();
	    ds.put("part_item_id", partItemId);
	    ds.put("part_rev_id", parRevisionId);
		
		try {
			
			Util.execute(servletUrlStr, 
					serviceClassName,
					"makeArrangedStartPointData",
					ds,
					false);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	
	}
	
	public void updateArrangedStartPointDataScaling(String partItemId, String parRevisionId, double scale) throws Exception{
		
		DataSet ds = new DataSet();
	    ds.put("part_item_id", partItemId);
	    ds.put("part_rev_id", parRevisionId);
	    ds.put("scale", scale);
		
		try {
			
			Util.execute(servletUrlStr, 
					serviceClassName,
					"updateArrangedStartPointDataScaling",
					ds,
					false);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	
	}

	public void savePreRevisionBOMStructureData(Vector<WeldInformation> weldInformationV,
			String partItemId, String partItemRevId, String currentEcoNo,
			ItemRevision weldGroupRevision){

		String currentWeldGroupItemId = null;
		String currentWeldGroupItemRevisionId = null;
		Property ecoNoProperty = null;
		ItemRevision ecoRevision = null;
		String currentWeldGroupItemRevisionEcoNo = null;
		try {
			SoaHelper.getProperties(weldGroupRevision, new String[]{"item_id", "item_revision_id", "s7_ECO_NO"});
			currentWeldGroupItemId = weldGroupRevision.get_item_id();
			currentWeldGroupItemRevisionId = weldGroupRevision.get_item_revision_id();
			
//			addLog("currentWeldGroupItemId = "+currentWeldGroupItemId);
//			addLog("currentWeldGroupItemRevisionId = "+currentWeldGroupItemRevisionId);
			
			ecoNoProperty = weldGroupRevision.getPropertyObject("s7_ECO_NO");
			if(ecoNoProperty!=null){
				ecoRevision = (ItemRevision) ecoNoProperty.getModelObjectValue();
				if(ecoRevision!=null){
					SoaHelper.getProperties(ecoRevision, new String[]{"item_id"});
					currentWeldGroupItemRevisionEcoNo = ecoRevision.get_item_id();
					addLog("ecoNoStr = "+currentWeldGroupItemRevisionEcoNo);
				}else{
					addLog("ecoRevision is null");
				}
			}else{
				addLog("ecoNoProperty is null");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		DataSet dsWeldGroup = new DataSet();
		dsWeldGroup.put("part_item_id", partItemId);
		dsWeldGroup.put("part_rev_id", partItemRevId);
		
		try {
			Util.execute(servletUrlStr, 
					serviceClassName,
					"deleteBOMWeldPointData",
					dsWeldGroup,
					false);
			
			Util.execute(servletUrlStr, 
					serviceClassName,
					"deleteBOMWeldPoint2Data",
					dsWeldGroup,
					false);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		for (int i = 0; weldInformationV!=null && i < weldInformationV.size(); i++) {
			WeldInformation aWeldInformation = weldInformationV.get(i);
			
			aWeldInformation.partItemId = partItemId;
			aWeldInformation.partItemRevId = partItemRevId;
			
			// Dispatcher ��� Item Id�� Revision Id�� �����Ѵ�.
			// ���� Revision�� �� �Ǵ� �� History�� �Ѽ� ��Ű�� �ʰ� ���������
			DataSet ds = new DataSet();
			ds.clear();
	        ds.put("part_item_id", aWeldInformation.partItemId);
	        ds.put("part_rev_id", aWeldInformation.partItemRevId);
	        ds.put("old_part_rev_id", currentWeldGroupItemRevisionId);
	        ds.put("old_eco_id", currentWeldGroupItemRevisionEcoNo);
	        ds.put("feature_name", aWeldInformation.featureName);
	        ds.put("weld_type", aWeldInformation.weldType);
	        ds.put("secure_type", aWeldInformation.getSecureType());
	        
	        double sx = aWeldInformation.getStartPointX();
	        double sy = aWeldInformation.getStartPointY();
	        double sz = aWeldInformation.getStartPointZ();
	        
		    ds.setDouble("start_point_x", sx);
	        ds.setDouble("start_point_y", sy);
	        ds.setDouble("start_point_z", sz);
	        
	        double ex = aWeldInformation.getEndPointX();
	        double ey = aWeldInformation.getEndPointY();
	        double ez = aWeldInformation.getEndPointZ();
	        double wl = aWeldInformation.getWeldLength();
	        
	        ds.setDouble("end_point_x", ex);
	        ds.setDouble("end_point_y", ey);
	        ds.setDouble("end_point_z", ez);
	        ds.setDouble("weld_length", wl);
	        
	        ds.setInt("sheets", aWeldInformation.getSheets());
	        
	        ds.put("occurrence_name", aWeldInformation.occurrenceName);
	        ds.put("occurrence_uid", aWeldInformation.occurrenceUid);
	        ds.put("occurrence_thread_uid", aWeldInformation.occurrenceThreadUid);
	        
			try {
				Util.execute(servletUrlStr, 
						serviceClassName,
						"insertBOMWeldPointDataRow",
						ds,
						false);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		try {
			Util.execute(servletUrlStr, 
					serviceClassName,
					"makeBOMArrangedStartPointData",
					dsWeldGroup,
					false);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public void makeSaveDataForInBound(String partItemId, String partItemRevisionId) throws Exception {

		DataSet ds = new DataSet();
        ds.put("part_item_id", partItemId);
        ds.put("part_rev_id", partItemRevisionId);
        
		try {

			Util.execute(servletUrlStr, 
					serviceClassName,
					"deleteCurrentSavedData",
					ds,
					false);
			
			Util.execute(servletUrlStr, 
					serviceClassName,
					"makeSaveDataForDelete",
					ds,
					false);
			
			Util.execute(servletUrlStr, 
					serviceClassName,
					"makeSaveDataForAdd",
					ds,
					false);
			
			Util.execute(servletUrlStr, 
					serviceClassName,
					"makeSaveDataForInBound",
					ds,
					false);
			
			Util.execute(servletUrlStr, 
					serviceClassName,
					"makeSaveDataForEndDiff",
					ds,
					false);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

	
	public void makeInBoundData(String partItemId, String targetPartItemRevisionId) throws Exception {
		
		DataSet ds = new DataSet();
        ds.put("part_item_id", partItemId);
        ds.put("part_rev_id", targetPartItemRevisionId);
        // ��� �������� ���� ���������� ���� ����.
        ds.setDouble("allowance", this.allowanceDistance);
        
		try {
			Util.execute(
					servletUrlStr, 
					serviceClassName,
					"makeInBoundData",
					ds,
					false);
			
			Util.execute(
					servletUrlStr, 
					serviceClassName,
					"makeEndDiffData",
					ds,
					false);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public HashMap<String, String> findHaveSameEcoWeldGroupRevisionData(String partItemId, String ecoNo) throws Exception {
		String beforRevId = null;
		
		DataSet ds = new DataSet();
        ds.put("part_item_id", partItemId.trim());
        ds.put("eco_no", ecoNo.trim());
        
        addLog("findHaveSameEcoWeldGroupRevisionData (S)");
        addLog("partItemId = "+partItemId);
        addLog("ecoNo = "+ecoNo);
        
        HashMap<String, String> weldGroupDataHashMap = null;
        
        ArrayList<HashMap> resultList = null;
		try {
			resultList =  (ArrayList<HashMap>) Util.execute(
					servletUrlStr, 
					serviceClassName,
					"findHaveSameEcoWeldGroupRevisionData",
					ds,
					true);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		for (int i = 0;resultList!=null && i < resultList.size(); i++) {
			HashMap rowDataHash = resultList.get(i);
			
			if(rowDataHash != null){

				Object weldGroupItemIdObject = rowDataHash.get("WELDGROUP_ITEM_ID");
				Object weldGroupItemRevIdObject = rowDataHash.get("WELDGROUP_ITEM_REV_ID");
				Object weldGroupEcoNoObject = rowDataHash.get("ECO_NO");
				
				String weldGroupItemId = null;
				String weldGroupItemRevId = null;
				String weldGroupEcoNo = null;

				if(weldGroupItemIdObject!=null){
					weldGroupItemId = weldGroupItemIdObject.toString();
				}
				if(weldGroupItemRevIdObject!=null){
					weldGroupItemRevId = weldGroupItemRevIdObject.toString();
				}
				if(weldGroupEcoNoObject!=null){
					weldGroupEcoNo = weldGroupEcoNoObject.toString();
				}
				
				if(weldGroupItemId!=null && weldGroupItemId.trim().length()>0){
					if(weldGroupItemRevId!=null && weldGroupItemRevId.trim().length()>0){
						
						weldGroupDataHashMap = new HashMap<String, String>();
						weldGroupDataHashMap.put("item_id", weldGroupItemId.trim());
						weldGroupDataHashMap.put("item_rev_id", weldGroupItemRevId.trim());
						if(weldGroupEcoNo!=null && weldGroupEcoNo.trim().length()>0){
							weldGroupDataHashMap.put("eco_no", weldGroupEcoNo.trim());
						}else{
							weldGroupDataHashMap.put("eco_no", (String)null);
						}
						
						addLog("@ weldGroupItemId = "+weldGroupItemId);
						addLog("@ weldGroupItemRevId = "+weldGroupItemRevId);
						addLog("@ weldGroupEcoNo = "+weldGroupEcoNo);
						break;
					}
				}
				
			}
		}
		
		addLog("findHaveSameEcoWeldGroupRevisionData (E)");
		
		return weldGroupDataHashMap;
	}
	
	public String getECOMatchedFMPRevisionId(String fmpItemId, String ecoNo) throws Exception {
		String fmpRevisionId = null;
		
		DataSet ds = new DataSet();
        ds.put("fmp_item_id", fmpItemId.trim());
        ds.put("eco_no", ecoNo.trim());
        
        ArrayList<HashMap> resultList = null;
		try {
			resultList =  (ArrayList<HashMap>) Util.execute(
					servletUrlStr, 
					serviceClassName,
					"getECOMatchedFMPRevision",
					ds,
					true);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		for (int i = 0;resultList!=null && i < resultList.size(); i++) {
			
			HashMap rowDataHash = resultList.get(i);
			if(rowDataHash != null){

				//Object tempFmpItemIdObj = rowDataHash.get("FMP_ITEM_ID");
				Object tempFmpItemRevIdObj = rowDataHash.get("FMP_ITEM_REV_ID");
				//Object tempEcoNoStrObj = rowDataHash.get("ECO_NO");

				String tempFmpItemRevId = null;
				if(tempFmpItemRevIdObj!=null && tempFmpItemRevIdObj instanceof String){
					tempFmpItemRevId = tempFmpItemRevIdObj.toString();
				}

				if(tempFmpItemRevId!=null && tempFmpItemRevId.trim().length()>0){
					fmpRevisionId = tempFmpItemRevId.trim();
				}
			}
		}
		
		return fmpRevisionId;
	}
	
	/**
	 * DB �˻��� ���� �̵̹�ϵ� Weld Group Item Revision�� �ִٸ� �׵��߿� ������ ����� Revision�� ���� Revision�� �ִ��� Ȯ���ϰ�
	 * ������ Revision�� ã�Ƽ� Revurn �Ѵ�.
	 *
	 * ���� Revision������ �Է����ǿ� �´� Revision�� �ִ°�� �ش� Revision�� Return�Ѵ�.
	 * �׷��� ���� ��� ���� Revision�� Return�Ѵ�.
	 * 
	 * @param connection
	 * @param weldGroupDataHashMap DB�˻��� ���� ������ Weld Group�� ���� Revision�� �ִ°�� �ش� Revision�� ã�� ��������
	 * @return ������ Revision�߿� ���� Revision�� ���ǿ� �´� Revision�Ǵ� ���� Item�� ���� Revision �� Return �Ѵ�.
	 */
	public ItemRevision getWeldGroupPreRevision(Connection connection, HashMap<String, String> weldGroupDataHashMap){
		
    	String latestRevisionId = null;
    	ItemRevision latestRevision = null; 
    	
    	
    	String weldGroupItemId = weldGroupDataHashMap.get("item_id");
    	String weldGroupItemRevId = weldGroupDataHashMap.get("item_rev_id");
    	
		//WeldGroup�� �����ϴ��� Ȯ�� ��, ������ ������.
		try {
			Item weldGroupItem = Util.getItem(connection, weldGroupItemId);
			
			SoaHelper.getProperties(weldGroupItem, new String[]{"revision_list"});
	    	ModelObject[] weldGroupItemRevisionList = weldGroupItem.get_revision_list();
	    	
	    	for (int i = 0; i < weldGroupItemRevisionList.length; i++) {
	    		ItemRevision tempRevision = (ItemRevision)weldGroupItemRevisionList[i];
				SoaHelper.getProperties(tempRevision, new String[]{"item_revision_id"});
				String tempRevisionId = tempRevision.get_item_revision_id();
				
				if(tempRevisionId!=null && weldGroupItemRevId!=null && tempRevisionId.trim().equalsIgnoreCase(weldGroupItemRevId.trim())==true){
					latestRevision = tempRevision;
					break;
				}
				
				if(latestRevisionId==null){
					latestRevisionId = tempRevisionId;
					latestRevision = tempRevision;
				}else if(latestRevisionId.compareToIgnoreCase(tempRevisionId) < 0){
					latestRevisionId = tempRevisionId;
					latestRevision = tempRevision;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return latestRevision;
	}
	
	public WeldInformation[] getAddTargetWeldPointData(String partItemId, String partItemRevId){
		
		WeldInformation[] addTargetWeldInformationList = null;
		
		DataSet ds = new DataSet();
	    ds.put("part_item_id", partItemId);
	    ds.put("part_rev_id", partItemRevId);
        
        HashMap<String, String> weldGroupDataHashMap = null;
        
        ArrayList<HashMap> resultList = null;
		try {
			resultList =  (ArrayList<HashMap>) Util.execute(
					servletUrlStr, 
					serviceClassName,
					"getAddTargetWeldPointData",
					ds,
					true);
		} catch (Exception e) {
			e.printStackTrace();
		}

		if(resultList!=null && resultList.size() > 0){
			addTargetWeldInformationList = new WeldInformation[resultList.size()]; 
		}
		
		for (int i = 0;resultList!=null && i < resultList.size(); i++) {
			
			HashMap rowDataHash = resultList.get(i);
			if(rowDataHash != null){
				
				WeldInformation aWeldInformation = new WeldInformation( m_zTaskLogger, buffer, isDebug, servletUrlStr, partItemId, partItemRevId, rowDataHash);
				addTargetWeldInformationList[i] = aWeldInformation;
			
			}
		}
		
		return addTargetWeldInformationList;
		
	}
	
	public int getMaxOccSeqNo(String partItemId, String partItemRevId){
		
		int maxSeqNo = -1;
		
		DataSet ds = new DataSet();
	    ds.put("part_item_id", partItemId);
	    ds.put("part_rev_id", partItemRevId);
        
        ArrayList<HashMap> resultList = null;
		try {
			resultList =  (ArrayList<HashMap>) Util.execute(
					servletUrlStr, 
					serviceClassName,
					"getMaxOccSeqNo",
					ds,
					true);
		} catch (Exception e) {
			e.printStackTrace();
		}

		for (int i = 0;resultList!=null && i < resultList.size(); i++) {
			
			HashMap rowDataHash = resultList.get(i);
			if(rowDataHash != null){
				Object maxSeqNoObj = rowDataHash.get("MAX_SEQ_NO");
				if(maxSeqNoObj!=null && maxSeqNoObj instanceof BigDecimal) {
					maxSeqNo = ((BigDecimal) maxSeqNoObj ).intValue();
				}else{
					maxSeqNo = 0;
				}
			}
		}
		
		return maxSeqNo;
		
	}
	
	public int getAllNewBOMLineCount(String partItemId, String partItemRevId){
		
		int allBOMLineCount = 0;
		
		DataSet ds = new DataSet();
	    ds.put("part_item_id", partItemId);
	    ds.put("part_rev_id", partItemRevId);
        
        ArrayList<HashMap> resultList = null;
		try {
			resultList =  (ArrayList<HashMap>) Util.execute(
					servletUrlStr, 
					serviceClassName,
					"getAllNewBOMLineCount",
					ds,
					true);
		} catch (Exception e) {
			e.printStackTrace();
		}

		for (int i = 0;resultList!=null && i < resultList.size(); i++) {
			
			HashMap rowDataHash = resultList.get(i);
			if(rowDataHash != null){
				Object maxSeqNoObj = rowDataHash.get("ALL_BOMLINE_COUNT");
				if(maxSeqNoObj!=null && maxSeqNoObj instanceof BigDecimal) {
					allBOMLineCount = ((BigDecimal) maxSeqNoObj ).intValue();
				}
			}
		}
		
		return allBOMLineCount;
		
	}
	
	
	public HashMap<String, WeldInformation> getUpdateTargetBOMLineData(String partItemId, String partItemRevId){
		
		HashMap<String, WeldInformation> updateTargetWeldInformationHash = null;
		
		DataSet ds = new DataSet();
	    ds.put("part_item_id", partItemId);
	    ds.put("part_rev_id", partItemRevId);
        
        HashMap<String, String> weldGroupDataHashMap = null;
        
        ArrayList<HashMap> resultList = null;
		try {
			resultList =  (ArrayList<HashMap>) Util.execute(
					servletUrlStr, 
					serviceClassName,
					"getUpdateTargetBOMLineData",
					ds,
					true);
		} catch (Exception e) {
			e.printStackTrace();
		}

		if(resultList!=null && resultList.size() > 0){
			updateTargetWeldInformationHash = new HashMap<String, WeldInformation>(); 
		}
		
		for (int i = 0;resultList!=null && i < resultList.size(); i++) {
			
			HashMap rowDataHash = resultList.get(i);
			if(rowDataHash != null){
				WeldInformation aWeldInformation = new WeldInformation( m_zTaskLogger, buffer, isDebug, servletUrlStr, partItemId, partItemRevId, rowDataHash);
				if(aWeldInformation!=null && aWeldInformation.occThreadUid!=null){
					String occThreadUid = aWeldInformation.occThreadUid;
					updateTargetWeldInformationHash.put(occThreadUid, aWeldInformation);
				}
			}
		}
		
		return updateTargetWeldInformationHash;
		
	}	
	
	
	private void addLog(String msg){
		if( isDebug ){
			m_zTaskLogger.info(msg);
			buffer.append(msg);
			buffer.append("\r\n");
		}
	}
}

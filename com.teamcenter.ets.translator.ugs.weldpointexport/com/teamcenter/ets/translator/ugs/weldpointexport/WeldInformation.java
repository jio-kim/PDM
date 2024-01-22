package com.teamcenter.ets.translator.ugs.weldpointexport;

import java.math.BigDecimal;
import java.util.HashMap;

import com.teamcenter.ets.soa.SoaHelper;
import com.teamcenter.soa.client.model.ModelObject;
import com.teamcenter.soa.client.model.strong.BOMLine;
import com.teamcenter.soa.client.model.strong.ItemRevision;
import com.teamcenter.soa.client.model.strong.PSOccurrence;
import com.teamcenter.soa.client.model.strong.PSOccurrenceThread;
import com.teamcenter.tstk.util.log.ITaskLogger;

/**
 * 용접점 정보를 읽어 CSV 파일에 저장할때 필요한 정보를 저장하는 Class
 * [NON-SR][20160503] Taeku.Jeong
 * @author Taeku
 *
 */
public class WeldInformation {

	// 용접 Type
	// 'Spot', 'BRAZING', 'PLUG', 'Co2' 
	public String weldType = null;
	
	// 용접개체 이름
	public String weldPointItemName = null;
	
	// 용접 시작점
	private double startPointX = 0.0;
	private double startPointY = 0.0;
	private double startPointZ = 0.0;
	
	// 용접 끝나는점 (Co2)
	private double endPointX = 0.0;
	private double endPointY = 0.0;
	private double endPointZ = 0.0;

	// 겹수
	private int sheets = 0;
	// 보안용접 여부 
	private String secureType  = "CO";
	// 용접길이 (Co2)
	private double weldLength = 0.0;
	
	// Weld Point Name을 FeatureName으로 정의 한다.
	public String featureName = null;
	public String partItemId = null;
	public String partItemRevId = null;
	
	public String occurrenceName = null;
	public String occurrenceUid = null;
	public String occurrenceThreadUid = null;
	
	public String serverURLStr = null;
	
	ITaskLogger m_zTaskLogger;
	StringBuffer buffer;
	boolean isDebug = false;
	
	String step = null;
	
	public String oldFeatureName = null;
	public String changeMemo = null;
	public String occThreadUid = null;
	
	/**
	 * Dispatcher 에서 기존의 WeldGroup에 등록된 Data를 읽어 DB에 등록하는 과정에 호출되는 생성자
	 * 
	 * @param m_zTaskLogger
	 * @param buffer
	 * @param isDebug
	 * @param serverURLStr
	 * @param partItemId
	 * @param partItemRevId
	 * @param weldPointBOMline
	 */
	public WeldInformation(ITaskLogger m_zTaskLogger, StringBuffer buffer, boolean isDebug, String serverURLStr, String partItemId, String partItemRevId, BOMLine weldPointBOMline){
		this.serverURLStr = serverURLStr;
		this.m_zTaskLogger = m_zTaskLogger;
		this.buffer = buffer;
		this.isDebug = isDebug;
		
		String weldPointId = null; 
		String weldPointRevId = null;
		String weldPointRevName = null;
		
		String feature_name = null;
		String bl_occ_xform = null;
		String positionDesc = null;
		
		try {
			SoaHelper.getProperties(weldPointBOMline, new String[]{
					"bl_revision",
					"bl_plmxml_occ_xform", 
					"bl_occurrence_name", 
					"M7_FEATURE_NAME",
					"S7_POSITION_DESC",
					"bl_real_occurrence"
					});
			
			String occurrenceName = null;
			String occurrenceUid = null;
			String occurrenceThreadUid = null;
			
			ModelObject realOccurrenceModelObject = weldPointBOMline.get_bl_real_occurrence();
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
			
			this.occurrenceName = occurrenceName;
			this.occurrenceUid = occurrenceUid;
			this.occurrenceThreadUid = occurrenceThreadUid;

			ItemRevision itemRevision = (ItemRevision)weldPointBOMline.get_bl_revision();
			SoaHelper.getProperties(itemRevision, new String[]{
					"item_id", "item_revision_id", "object_name"
				});
			weldPointId = itemRevision.get_item_id();
			weldPointRevId = itemRevision.get_item_revision_id();
			weldPointRevName = itemRevision.get_object_name();
			
			feature_name = weldPointBOMline.getPropertyObject("M7_FEATURE_NAME").getStringValue();
			bl_occ_xform = weldPointBOMline.get_bl_plmxml_occ_xform();
			positionDesc = weldPointBOMline.getPropertyObject("S7_POSITION_DESC").getStringValue();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		this.partItemId = partItemId;
		this.partItemRevId = partItemRevId;
		
		if(weldPointId!=null){
			String [] weldPointIDS =  weldPointId.split("-");
			
			this.weldType = weldPointIDS[1].trim().toUpperCase();
			if(weldPointIDS[2]!=null){
				this.sheets = Integer.parseInt(weldPointIDS[2].trim());
			}else{
				this.sheets = 0;
			}
			if(weldPointIDS[3]!=null){
				secureType = weldPointIDS[3].trim().toUpperCase();
			}
		}
		
		this.featureName	 = feature_name;
		
		if(bl_occ_xform!=null){
			String [] matrix =  bl_occ_xform.trim().split(" ");
			if(matrix!=null && matrix.length>15){
				this.startPointX = Double.parseDouble(matrix[12]);
				this.startPointY = Double.parseDouble(matrix[13]);
				this.startPointZ = Double.parseDouble(matrix[14]);
			}
		}
		
		if(positionDesc!=null){
			String [] endPoirntDataString =  positionDesc.trim().split(",");
			if(endPoirntDataString!=null && endPoirntDataString.length>=3){
				this.endPointX =Double.parseDouble(endPoirntDataString[0]);
				this.endPointY =Double.parseDouble(endPoirntDataString[1]);
				this.endPointZ = Double.parseDouble(endPoirntDataString[2]);
			}
	
			if(endPoirntDataString!=null && endPoirntDataString.length==4){
				this.weldLength = Double.parseDouble(endPoirntDataString[3]);			
			}
		}else{
			this.endPointX = 0.0d;
			this.endPointY = 0.0d;
			this.endPointZ = 0.0d;
			this.weldLength = 0.0d;
		}
		
		if(this.weldType!=null && this.sheets>-1 && this.secureType!=null){
			this.weldPointItemName = "WELDPOINT-"+this.weldType+"-"+this.sheets+"-"+this.secureType;
		}
		
		this.step = "BOM Read"; 
		
	}
	
	/**
	 * BOMLine을 추가하는 단계에서 추가될 용접점을 등록할때 사용하는 생성자
	 * 
	 * @param m_zTaskLogger
	 * @param buffer
	 * @param isDebug
	 * @param serverURLStr
	 * @param itemId
	 * @param itemRevId
	 * @param rowDataHash
	 */
	public WeldInformation(ITaskLogger m_zTaskLogger, StringBuffer buffer, boolean isDebug, String serverURLStr, String itemId, String itemRevId, HashMap rowDataHash){
		
		this.serverURLStr = serverURLStr;
		this.partItemId = itemId;
		this.partItemRevId = itemRevId;
	
		this.m_zTaskLogger = m_zTaskLogger;
		this.buffer = buffer;
		this.isDebug = isDebug;
		
		String ecoId = null;
		
		Object ecoIdObject =  rowDataHash.get("ECO_ID");
		Object featureNameObject =  rowDataHash.get("FEATURE_NAME");
		Object weldTypeObject =  rowDataHash.get("WELD_TYPE");
		Object secureTypeObject =  rowDataHash.get("SECURE_TYPE");
		Object sheetsObject =  rowDataHash.get("SHEETS");
		Object startPxObject =  rowDataHash.get("START_PX");
		Object startPyObject =  rowDataHash.get("START_PY");
		Object startPzObject =  rowDataHash.get("START_PZ");
		Object endPxObject =  rowDataHash.get("END_PX");
		Object endPyObject =  rowDataHash.get("END_PY");
		Object endPzObject =  rowDataHash.get("END_PZ");
		Object weldLengthObject =  rowDataHash.get("WELD_LENGTH");
		
		Object oldFeatureNameObject =  rowDataHash.get("OLD_FEATURE_NAME");
		Object changeMemoObject =  rowDataHash.get("CHANGE_MEMO");
		Object occThreadUidObject =  rowDataHash.get("OCC_THREAD_UID");
		
	
		if(ecoIdObject!=null && ecoIdObject instanceof String){
			ecoId = ecoIdObject.toString();
		}
		
		if(featureNameObject!=null && featureNameObject instanceof String){
			this.featureName = featureNameObject.toString();
		}
		if(weldTypeObject!=null && weldTypeObject instanceof String){
			this.weldType = weldTypeObject.toString();
		}
		if(secureTypeObject!=null && secureTypeObject instanceof String){
			this.secureType = secureTypeObject.toString();
		}
		
		if(sheetsObject!=null && sheetsObject instanceof BigDecimal){
			this.sheets = ((BigDecimal)sheetsObject).intValue();
		}
		if(startPxObject!=null && startPxObject instanceof BigDecimal){
			this.startPointX = ((BigDecimal)startPxObject).doubleValue();
		}
		if(startPyObject!=null && startPyObject instanceof BigDecimal){
			this.startPointY = ((BigDecimal)startPyObject).doubleValue();
		}
		if(startPzObject!=null && startPzObject instanceof BigDecimal){
			this.startPointZ = ((BigDecimal)startPzObject).doubleValue();
		}
		
		if(endPxObject!=null && endPxObject instanceof BigDecimal){
			this.endPointX = ((BigDecimal)endPxObject).doubleValue();
		}
		if(endPyObject!=null && endPyObject instanceof BigDecimal){
			this.endPointY = ((BigDecimal)endPyObject).doubleValue();
		}
		if(endPzObject!=null && endPzObject instanceof BigDecimal){
			this.endPointZ = ((BigDecimal)endPzObject).doubleValue();
		}
		if(weldLengthObject!=null && weldLengthObject instanceof BigDecimal){
			this.weldLength = ((BigDecimal)weldLengthObject).doubleValue();
		}
	
		
		if(oldFeatureNameObject!=null && oldFeatureNameObject instanceof String){
			this.oldFeatureName = oldFeatureNameObject.toString();
		}
		if(changeMemoObject!=null && changeMemoObject instanceof String){
			this.changeMemo = changeMemoObject.toString();
		}
		if(occThreadUidObject!=null && occThreadUidObject instanceof String){
			this.occThreadUid = occThreadUidObject.toString();
		}
		
		if(this.weldType!=null && this.sheets>-1 && this.secureType!=null){
			this.weldPointItemName = "WELDPOINT-"+this.weldType+"-"+this.sheets+"-"+this.secureType;
		}
		
	}

	/**
	 * 용접점 정보를 가진 CSV 파일에서 Data를 한줄 읽은 문자열에서 용접점 정보를 생성하고 초기화 한다. 
	 * @param serverURLStr
	 * @param itemId
	 * @param itemRevId
	 * @param csvLineStr
	 */
	public WeldInformation(ITaskLogger m_zTaskLogger, StringBuffer buffer, boolean isDebug, String serverURLStr, String itemId, String itemRevId, String csvLineStr){
		
		this.serverURLStr = serverURLStr;
		this.partItemId = itemId;
		this.partItemRevId = itemRevId;

		this.m_zTaskLogger = m_zTaskLogger;
		this.buffer = buffer;
		this.isDebug = isDebug;
		
		String[] lineSplitStrings = csvLineStr.split(",");
		
		if(lineSplitStrings.length<6){
			// 용접 Type을 알 수 없으므로 Data를 확인 할 수 없음.
		}
		
		String tempWeldType = lineSplitStrings[5];
		if(tempWeldType!=null && tempWeldType.trim().length()>0){
			this.weldType = tempWeldType.trim();
		}

		boolean isSpot = true;
		if(this.weldType.equalsIgnoreCase("SPOT")==true){
			isSpot = true;
		}else{
			isSpot = false;
		}
		
		for (int i = 0; i < lineSplitStrings.length; i++) {
			String tempString = lineSplitStrings[i];
			
			if(isSpot==true){
				initSpotData(i, tempString);
			}else{
				initCo2Data(i, tempString);
			}
		}
		
		this.step = "CSV Read";
	}
	
	private void addLog(String msg){
		if( isDebug ){
			m_zTaskLogger.info(msg);
			buffer.append(msg);
			buffer.append("\r\n");
		}
	}
	
	/**
	 * 점 용접인경우 용접점 정보 문자열을 읽어 Data를  초기화 한다.
	 * @param index
	 * @param valueStr
	 */
	private void initSpotData(int index, String valueStr){
		switch (index) {
		case 0:
			if(valueStr!=null){
				this.featureName = valueStr.trim();
			}
			break;
		case 1:
			if(valueStr!=null){
				try {
					this.startPointX = Double.parseDouble(valueStr);	
				} catch (Exception e) {
				}
			}
			break;
		case 2:
			if(valueStr!=null){
				try {
					this.startPointY = Double.parseDouble(valueStr);	
				} catch (Exception e) {
				}
			}
			break;
		case 3:
			if(valueStr!=null){
				try {
					this.startPointZ = Double.parseDouble(valueStr);	
				} catch (Exception e) {
				}
			}
			break;
		case 4:
			if(valueStr!=null){
				try {
					this.sheets = Integer.parseInt(valueStr);	
				} catch (Exception e) {
				}
			}
			break;
		case 5:
			if(valueStr!=null){
				this.weldType = valueStr.trim();
			}
			break;
		case 6:
			if(valueStr!=null){
				if(valueStr.trim().equalsIgnoreCase("DR")==true){
					this.secureType="DR";
				}else{
					this.secureType="CO";
				}
			}else{
				this.secureType="CO";
			}
			break;

		default:
			break;
		}
		
		if(this.weldType!=null && this.sheets>-1 && this.secureType!=null){
			this.weldPointItemName = "WELDPOINT-"+this.weldType+"-"+this.sheets+"-"+this.secureType;
		}
	}

	/**
	 * Co2 용접인경우 용접점 정보 문자열을 읽어 Data를  초기화 한다.
	 * @param index
	 * @param valueStr
	 */
	private void initCo2Data(int index, String valueStr){
		switch (index) {
		case 0:
			if(valueStr!=null){
				this.featureName = valueStr.trim();
			}
			break;
		case 1:
			if(valueStr!=null){
				try {
					this.startPointX = Double.parseDouble(valueStr);	
				} catch (Exception e) {
				}
			}
			break;
		case 2:
			if(valueStr!=null){
				try {
					this.startPointY = Double.parseDouble(valueStr);	
				} catch (Exception e) {
				}
			}
			break;
		case 3:
			if(valueStr!=null){
				try {
					this.startPointZ = Double.parseDouble(valueStr);	
				} catch (Exception e) {
				}
			}
			break;
		case 4:
			if(valueStr!=null){
				try {
					this.sheets = Integer.parseInt(valueStr);	
				} catch (Exception e) {
				}
			}
			break;
		case 5:
			if(valueStr!=null){
				this.weldType = valueStr.trim();
			}
			break;
		case 6:
			if(valueStr!=null){
				if(valueStr.trim().equalsIgnoreCase("DR")==true){
					this.secureType="DR";
				}else{
					this.secureType="CO";
				}
			}else{
				this.secureType="CO";
			}
			break;
		case 7:
			if(valueStr!=null){
				try {
					this.endPointX = Double.parseDouble(valueStr);	
				} catch (Exception e) {
				}
			}
			break;
		case 8:
			if(valueStr!=null){
				try {
					this.endPointY = Double.parseDouble(valueStr);	
				} catch (Exception e) {
				}
			}
			break;
		case 9:
			if(valueStr!=null){
				try {
					this.endPointZ = Double.parseDouble(valueStr);	
				} catch (Exception e) {
				}
			}
			break;
		case 10:
			if(valueStr!=null){
				try {
					this.weldLength = Double.parseDouble(valueStr);	
				} catch (Exception e) {
				}
			}
			break;
		default:
			break;
		}
		
		if(this.weldType!=null && this.sheets>-1 && this.secureType!=null){
			this.weldPointItemName = "WELDPOINT-"+this.weldType+"-"+this.sheets+"-"+this.secureType;
		}
	}	
	
	public double getStartPointX() {
		return this.startPointX;
	}
	public void setStartPointX(double startPointX) {
		this.startPointX = startPointX;
	}
	
	public double getStartPointY() {
		return this.startPointY;
	}
	public void setStartPointY(double startPointY) {
		this.startPointY = startPointY;
	}
	
	public double getStartPointZ() {
		return this.startPointZ;
	}
	public void setStartPointZ(double startPointZ) {
		this.startPointZ = startPointZ;
	}
	
	public double getEndPointX() {
		return this.endPointX;
	}
	public void setEndPointX(double endPointX) {
		this.endPointX = endPointX;
	}
	
	public double getEndPointY() {
		return this.endPointY;
	}
	public void setEndPointY(double endPointY) {
		this.endPointY = endPointY;
	}
	
	public double getEndPointZ() {
		return this.endPointZ;
	}
	public void setEndPointZ(double endPointZ) {
		this.endPointZ = endPointZ;
	}
	
	public int getSheets() {
		return sheets;
	}
	public void setSheets(int sheets) {
		this.sheets = sheets;
	}
	public String getSecureType() {
		return secureType;
	}
	public void setSecureType(String secureType) {
		this.secureType = secureType;
	}
	public double getWeldLength() {
		return weldLength;
	}
	public void setWeldLength(double weldLength) {
		this.weldLength = weldLength;
	}
	
	public String toString(){
		String tempString = null;
		
		if(step!=null && step.trim().equalsIgnoreCase("BOM Read")==true){
			tempString = step +" --> "+partItemId +"/"+partItemRevId+" ["+weldPointItemName +"] "+
					"Start : "+startPointX +","+startPointY +","+startPointZ +"   "+
					"End : "+endPointX +","+endPointY +","+endPointZ +"   "+
					"weldLength : "+weldLength;
		}else if(step!=null && step.trim().equalsIgnoreCase("CSV Read")==true){
			tempString = step +" --> "+partItemId +"/"+partItemRevId+" ["+weldPointItemName +"] "+
					"Start : "+startPointX +","+startPointY +","+startPointZ +"   "+
					"End : "+endPointX +","+endPointY +","+endPointZ +"   "+
					"weldLength : "+weldLength;
		}else{
			tempString = step +" "+"["+weldType+"]"+
					"sheets:"+sheets +","+
					"isSecured:"+secureType +","+
					partItemId +"/"+partItemRevId+", "+
					"Start : "+startPointX +","+startPointY +","+startPointZ +"   "+
					"End : "+endPointX +","+endPointY +","+endPointZ +"   "+
					"weldLength : "+weldLength;
		}
		
		return tempString;
	}
	

}

package com.symc.plm.me.sdv.operation.meco;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

import com.kgm.common.remote.DataSet;
import com.kgm.common.remote.SYMCRemoteUtil;
import com.kgm.rac.kernel.SYMCBOPEditData;
import com.symc.plm.me.common.SDVBOPUtilities;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.utils.CustomUtil;
import com.symc.plm.me.utils.SYMTcUtil;
import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.cme.application.MFGLegacyApplication;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMViewRevision;
import com.teamcenter.rac.kernel.TCComponentBOPLine;
import com.teamcenter.rac.kernel.TCComponentBOPWindow;
import com.teamcenter.rac.kernel.TCComponentChangeItemRevision;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentProcess;
import com.teamcenter.rac.kernel.TCComponentUser;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;

/**
 * 조립작업 표준서, 용접조건표 Preview Open할때 MECO EPL을 자동 Update 하는 형태로 MECO EPL 생성방법을 변경 하는데 필요한
 * Function들을 모아서 처리 한다. 
 * @author Taeku
 *
 */
public class MECOCreationUtil {
	
	private TCComponentItemRevision changeRevision;
	public static SimpleDateFormat simpleDateFormat= new SimpleDateFormat("yyyy-MM-dd HH:mm");
	public static final String MEPL_SERVICE_CLASS = "com.kgm.service.SYMCMEPLService";
	public TCSession session;
	
	public static void updateMEPLForReview(){
		
	}
	
	public MECOCreationUtil(TCComponentItemRevision changeRevision){
		this.changeRevision = changeRevision;
		if(this.changeRevision!=null){
			session = this.changeRevision.getSession();
		}
	}
	
	/**
	 * EPL 데이터를 만들어냄.
	 * @param oldParentRevId 모의 이전 리비전 (이전 이후 차이를 찾아내기 위함.)
	 * @param newParentRevId 모의 이후 리비전 (만약 모가 개정되어 있지 않다면 동일한 값을 넣으면 됨)
	 * @param childItemId 자 파트를 입력하면 특정 자 파트만 나오도록 함.
	 **/
	public static ArrayList<HashMap> getMEPLResultList(String mecoId, String parentItemId, String oldParentRevId, String newParentRevId, String childItemId)
	{
		SYMCRemoteUtil remoteQuery = new SYMCRemoteUtil();
		DataSet ds = new DataSet();
		ds.put("mecoId", mecoId);
		ds.put("parentItemId", parentItemId);
		ds.put("oldParentRevId", oldParentRevId);
		ds.put("newParentRevId", newParentRevId);
		ds.put("childItemId", childItemId);

		ArrayList<HashMap> resultList = null;
		try
		{
			resultList = (ArrayList<HashMap>) remoteQuery.execute(MEPL_SERVICE_CLASS, "getMEPLResultList", ds);
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		return resultList;
	}

	/**
	 * EPL 데이터를 만들어냄.
	 * @param oldParentRevId 모의 이전 리비전 (이전 이후 차이를 찾아내기 위함.)
	 * @param newParentRevId 모의 이후 리비전 (만약 모가 개정되어 있지 않다면 동일한 값을 넣으면 됨)
	 * @param childItemId 자 파트를 입력하면 특정 자 파트만 나오도록 함.
	 **/
	public static ArrayList<HashMap> getBOPChildErrorList(String mecoId, String parentItemId, String parentRevId)
	{
		SYMCRemoteUtil remoteQuery = new SYMCRemoteUtil();
		DataSet ds = new DataSet();
		ds.put("mecoId", mecoId);
		ds.put("parentItemId", parentItemId);
		ds.put("parentRevId", parentRevId);

		ArrayList<HashMap> resultList = null;
		try
		{
			resultList = (ArrayList<HashMap>) remoteQuery.execute(MEPL_SERVICE_CLASS, "getBOPChildErrorList", ds);
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		return resultList;
	}

	public boolean isValideOperationMEPL(String mecoItemId, TCComponentItemRevision operationItemRevision){
		boolean valideMepl = false;
		
		TCComponentItemRevision baseOnRevision = null;
		//Date lastModifyDate = null;
		Date eplCreationDate = null;
		String currentRevId = null;
		
		String operationItemId = null;
		try {
			operationItemId = operationItemRevision.getItem().getProperty(SDVPropertyConstant.ITEM_ITEM_ID);
		} catch (TCException e1) {
			e1.printStackTrace();
		}
		
		eplCreationDate = getOperationEPLCreateionDate(mecoItemId, operationItemId);
		
//		try {
//			lastModifyDate = operationItemRevision.getDateProperty(SDVPropertyConstant.ITEM_LAST_MODIFY_DATE);
//		} catch (TCException e) {
//			e.printStackTrace();
//		}
//		
//		System.out.println("# lastModifyDate = "+lastModifyDate);
		
		String targetViewType = "View";
		Date bomViewLastModifyDate = null;
		try {
			TCComponentBOMViewRevision bomviewRevision = SDVBOPUtilities.getBOMViewRevision(operationItemRevision, targetViewType);
			if(bomviewRevision != null) {					
				bomViewLastModifyDate = bomviewRevision.getDateProperty(SDVPropertyConstant.ITEM_LAST_MODIFY_DATE);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//----------------------------------------------------------------------------------------------------
		// MECO-EPL에 기록된 시간이 분단위 까지만 기록 되어 있으므로 분 단위까지만 확인 한다.
		//----------------------------------------------------------------------------------------------------
		SimpleDateFormat simpleDateFormat= new SimpleDateFormat("yyyy-MM-dd HH:mm");
		String eplCreationDateString = null;
		if(eplCreationDate!=null){
			eplCreationDateString = simpleDateFormat.format(eplCreationDate);
			try {
				eplCreationDate = simpleDateFormat.parse(eplCreationDateString);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		
		String bomViewLastModifyDateString = null;
		if(bomViewLastModifyDate!=null){
			bomViewLastModifyDateString = simpleDateFormat.format(bomViewLastModifyDate);
			try {
				bomViewLastModifyDate = simpleDateFormat.parse(bomViewLastModifyDateString);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		
		System.out.println("# MECO-EPL SavedLastModifyDate = "+eplCreationDate);
		System.out.println("# BOM View LastModifyDate = "+bomViewLastModifyDate);
		
		//if(eplCreationDate != null && lastModifyDate != null && bomViewLastModifyDate != null){
		if(eplCreationDate != null && bomViewLastModifyDate != null){
			//if(lastModifyDate.before(eplCreationDate)==true && bomViewLastModifyDate.before(eplCreationDate)==true){
			// if(eplCreationDate.before(bomViewLastModifyDate)==true){
			if(eplCreationDate.equals(bomViewLastModifyDate) || bomViewLastModifyDate.before(eplCreationDate)){
				valideMepl = true;
			}
		}
		
		return valideMepl;
	}
	
	/**
	 * MECO의 Solution Items중 누락된 MECO EPL이 있는지 List 한다.
	 * @param operationItemRevision
	 * @return
	 */
	public ArrayList<HashMap> getMissingMEPLObjectList(String mecoId){
			Date eplCreationDate = null;
			
			SYMCRemoteUtil remoteQuery = new SYMCRemoteUtil();
			DataSet ds = new DataSet();
			ds.put("mecoItemId", mecoId);
			
			ArrayList<HashMap> resultList = null;
			try {
				resultList = (ArrayList<HashMap>) remoteQuery.execute(MEPL_SERVICE_CLASS, "getMissingMEPLObjectList", ds);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			return resultList;
	}
	
	/**
	 * Operation의 EPL 생성 날짜와 시간을 찾아 Return 한다.
	 * @param operationItemRevision
	 * @return
	 */
	public Date getOperationEPLCreateionDate(String mecoId, String operationItemId){
			Date eplCreationDate = null;
			
			SYMCRemoteUtil remoteQuery = new SYMCRemoteUtil();
			DataSet ds = new DataSet();
			ds.put("mecoItemId", mecoId);
			ds.put("operationItemId", operationItemId);
			
			try {
				ArrayList<HashMap> resultList = (ArrayList<HashMap>) remoteQuery.execute(MEPL_SERVICE_CLASS, "getOperationEPLCrationDate", ds);
				
				for (int i = 0;resultList!=null && i < resultList.size(); i++) {
					HashMap aHash = resultList.get(i);
					
					if(aHash.get("PARENT_MOD_DATE")!=null){
						Object modDateObj = aHash.get("PARENT_MOD_DATE");
						if(modDateObj!=null && modDateObj instanceof java.sql.Timestamp){
							eplCreationDate = ((Date)modDateObj);
							break;
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			return eplCreationDate;
	}
	
	/**
	 * 이건 Test 해봐야 한다.
	 * @param mecoId
	 * @param operationItemId
	 */
	public void deleteOperationMEPL(String mecoId, String operationItemId){
		
		SYMCRemoteUtil remoteQuery = new SYMCRemoteUtil();
		DataSet ds = new DataSet();
		ds.put("mecoItemId", mecoId);
		ds.put("operationItemId", operationItemId);
		
		try {
			remoteQuery.execute(MEPL_SERVICE_CLASS, "deleteOperationEPL", ds);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public int getOperationDiffCount(TCComponentItemRevision mecoRevision, TCComponentItemRevision operationItemRevision){

		String currentItemId = null;
		String currentRevId = null;
		String oldRevId = null;
		
		TCComponentItemRevision baseOnRevision = null;
		String baseOnItemId = null;
		String baseOnRevId = null;
		try {
			currentItemId = operationItemRevision.getItem().getProperty(SDVPropertyConstant.ITEM_ITEM_ID);
			currentRevId = operationItemRevision.getProperty(SDVPropertyConstant.ITEM_REVISION_ID);
			baseOnRevision = operationItemRevision.basedOn();
			if(baseOnRevision!=null){
				baseOnItemId = baseOnRevision.getItem().getProperty(SDVPropertyConstant.ITEM_ITEM_ID);
				baseOnRevId = baseOnRevision.getProperty(SDVPropertyConstant.ITEM_REVISION_ID);
			}
		} catch (TCException e) {
			e.printStackTrace();
		}
		
		if(baseOnItemId!=null && baseOnItemId.trim().equalsIgnoreCase(currentItemId.trim())){
			oldRevId = baseOnRevId;
		}else{
			oldRevId = "";
		}
		
		int diffCount = 0;
		
		ArrayList<CompResultDiffInfo> diffListArray = getDifrentList(currentItemId, oldRevId, currentRevId);
		if(diffListArray!=null && diffListArray.size()>0){
			diffCount = diffListArray.size();
		}
			
		return diffCount;

	}

	private String getVcValue(String rawValue){
		if(rawValue==null || (rawValue!=null && rawValue.trim().length()<1)){
			return null;
		}

		HashMap<String, Object> mapData = SDVBOPUtilities.getVariant(rawValue);

		//ArrayList<String> values = (ArrayList<String>)mapData.get("values");
		//HashMap<String, String> descriptions = (HashMap<String, String>)mapData.get("descriptions");
		//String printValues = (String)mapData.get("printValues");
		String printDescriptions = (String)mapData.get("printDescriptions");
		
		return printDescriptions;
	}
	
	private TCComponentItemRevision findItemRevisionUsingItemPuid(String itemPuid){
		TCComponentItemRevision findedRevision = null;
		
		if(this.session==null || (this.session!=null && this.session.isLoggedIn()==false)){
			return findedRevision;
		}
		
		TCComponent tempComponent = null;
		try {
			tempComponent = session.stringToComponent(itemPuid);
		} catch (TCException e) {
			e.printStackTrace();
		}
		
		if(tempComponent==null || (tempComponent!=null && (tempComponent instanceof TCComponentItem)==false)){
			return findedRevision;
		}
		
		TCComponentItem tempItem = (TCComponentItem)tempComponent;
		if(tempItem!=null){
			try {
				findedRevision = SYMTcUtil.getLatestReleasedRevision(tempItem);
				if(findedRevision==null){
					findedRevision = tempItem.getLatestItemRevision();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return findedRevision;
	}

	/**
	 * BOP 비교결과로 받은 Data를 이용해 수량 합산이 가능한 것들은 수량 합산을 실시한다.
	 * @param diffListArray
	 * @return
	 */
	private ArrayList<CompResultDiffInfo> summationCompResultDiffInfoList(ArrayList<CompResultDiffInfo> diffListArray){
		ArrayList<CompResultDiffInfo> resultDiffListArray = null;
		
		if(diffListArray==null || (diffListArray!=null && diffListArray.size()<2)){
			return diffListArray;
		}
		
		Vector<String> compKeyVector = new Vector<String>();
		Hashtable<String, ArrayList<CompResultDiffInfo>> valueHash = new Hashtable<String, ArrayList<CompResultDiffInfo>>();
		
		for (int i = 0; diffListArray!=null && i < diffListArray.size(); i++) {
			CompResultDiffInfo tempDiffInfoData = diffListArray.get(i);

			// ----------------------------------------------------------------------------------------
			// 같은 ItemId와 Order No를 가진 것은 Qty 합산 처리를 해야 한다.
			// ----------------------------------------------------------------------------------------
			// 1) Type이 같은것끼리
			// 2) Old Or New 의 Item ID가 같은것
			// 3) Old Or New의 Orderr No 같은것
			// 4) Old Or New의 Variant 같은것
			// ----------------------------------------------------------------------------------------

			String key = ""+
					tempDiffInfoData.changeType +"_" +
					tempDiffInfoData.oldChildItemu +"_" +
					tempDiffInfoData.oldSeq +"_" +
					tempDiffInfoData.oldVariantValue +"_" +
					tempDiffInfoData.newChildItemu +"_" +
					tempDiffInfoData.newSeq +"_" +
					tempDiffInfoData.newVariantValue;
			
			System.out.println("Sum key["+i+"] = "+key);

			key = key.trim();
			
			if(compKeyVector.contains(key)==false){
				// 없는경우
				ArrayList<CompResultDiffInfo> tempArrayList = new ArrayList<CompResultDiffInfo>();
				tempArrayList.add(tempDiffInfoData);
				compKeyVector.add(key);
				valueHash.put(key, tempArrayList);
			}else{
				// 있는경우
				ArrayList<CompResultDiffInfo> tempArrayList = valueHash.get(key);
				tempArrayList.add(tempDiffInfoData);
				// Hash Table Update (remove() 없이 그냥 put() 해도 되지만 혹시나 해서...)
				valueHash.remove(key);
				valueHash.put(key, tempArrayList);
			}
		}
		
		if(compKeyVector!=null && compKeyVector.size()==diffListArray.size()){
			return diffListArray;
		}
		
		resultDiffListArray = new ArrayList<CompResultDiffInfo>();
		
		for (int i = 0; compKeyVector!=null && i < compKeyVector.size(); i++) {
			
			String key = compKeyVector.get(i);
			
			ArrayList<CompResultDiffInfo> tempArrayList = valueHash.get(key);
			int multipleCount = tempArrayList.size();
			
			CompResultDiffInfo tempDiffInfo = tempArrayList.get(0);
			
			if(multipleCount == 1){
				resultDiffListArray.add(tempDiffInfo);
			}else{
				if(tempDiffInfo.oldQty>0){
					tempDiffInfo.oldQty = tempDiffInfo.oldQty *  multipleCount;
				}
				if(tempDiffInfo.newQty > 0){
					tempDiffInfo.newQty = tempDiffInfo.newQty *  multipleCount;
				}
				resultDiffListArray.add(tempDiffInfo);
			}
			
		}
		
		return resultDiffListArray;
	}
	
	
	public ArrayList<CompResultDiffInfo> getDifrentList(String itemId, String oldRevId, String newRevId){
		
		ArrayList<CompResultDiffInfo> diffListArray = null;
		
		Date eplCreationDate = null;
		
		SYMCRemoteUtil remoteQuery = new SYMCRemoteUtil();
		DataSet ds = new DataSet();
		ds.put("operationItemId", itemId);
		ds.put("oldRevisionId", oldRevId);
		ds.put("newRevisionId", newRevId);

		System.out.println("operationItemId = "+itemId);
		System.out.println("oldRevisionId = "+oldRevId);
		System.out.println("newRevisionId = "+newRevId);
		
		try {
			ArrayList<HashMap> resultList = (ArrayList<HashMap>) remoteQuery.execute(MEPL_SERVICE_CLASS, "getChangedStructureCompareResultList", ds);
			
			if(resultList!=null && resultList.size()>0){
				diffListArray = new ArrayList<CompResultDiffInfo>();
			}
			
			System.out.println("resultList = "+resultList);
			
			for (int i = 0;resultList!=null && i < resultList.size(); i++) {
				HashMap aHash = resultList.get(i);
				
				CompResultDiffInfo tempDiffInfoData = new CompResultDiffInfo(aHash);
				diffListArray.add(tempDiffInfoData);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return diffListArray;
	}
	
}

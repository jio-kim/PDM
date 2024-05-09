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
 * �����۾� ǥ�ؼ�, ��������ǥ Preview Open�Ҷ� MECO EPL�� �ڵ� Update �ϴ� ���·� MECO EPL ��������� ���� �ϴµ� �ʿ���
 * Function���� ��Ƽ� ó�� �Ѵ�. 
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
	 * EPL �����͸� ����.
	 * @param oldParentRevId ���� ���� ������ (���� ���� ���̸� ã�Ƴ��� ����.)
	 * @param newParentRevId ���� ���� ������ (���� �� �����Ǿ� ���� �ʴٸ� ������ ���� ������ ��)
	 * @param childItemId �� ��Ʈ�� �Է��ϸ� Ư�� �� ��Ʈ�� �������� ��.
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
	 * EPL �����͸� ����.
	 * @param oldParentRevId ���� ���� ������ (���� ���� ���̸� ã�Ƴ��� ����.)
	 * @param newParentRevId ���� ���� ������ (���� �� �����Ǿ� ���� �ʴٸ� ������ ���� ������ ��)
	 * @param childItemId �� ��Ʈ�� �Է��ϸ� Ư�� �� ��Ʈ�� �������� ��.
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
		// MECO-EPL�� ��ϵ� �ð��� �д��� ������ ��� �Ǿ� �����Ƿ� �� ���������� Ȯ�� �Ѵ�.
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
	 * MECO�� Solution Items�� ������ MECO EPL�� �ִ��� List �Ѵ�.
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
	 * Operation�� EPL ���� ��¥�� �ð��� ã�� Return �Ѵ�.
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
	 * �̰� Test �غ��� �Ѵ�.
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
	 * BOP �񱳰���� ���� Data�� �̿��� ���� �ջ��� ������ �͵��� ���� �ջ��� �ǽ��Ѵ�.
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
			// ���� ItemId�� Order No�� ���� ���� Qty �ջ� ó���� �ؾ� �Ѵ�.
			// ----------------------------------------------------------------------------------------
			// 1) Type�� �����ͳ���
			// 2) Old Or New �� Item ID�� ������
			// 3) Old Or New�� Orderr No ������
			// 4) Old Or New�� Variant ������
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
				// ���°��
				ArrayList<CompResultDiffInfo> tempArrayList = new ArrayList<CompResultDiffInfo>();
				tempArrayList.add(tempDiffInfoData);
				compKeyVector.add(key);
				valueHash.put(key, tempArrayList);
			}else{
				// �ִ°��
				ArrayList<CompResultDiffInfo> tempArrayList = valueHash.get(key);
				tempArrayList.add(tempDiffInfoData);
				// Hash Table Update (remove() ���� �׳� put() �ص� ������ Ȥ�ó� �ؼ�...)
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

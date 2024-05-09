package com.symc.plm.rac.prebom.masterlist.operation;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import com.kgm.commands.ospec.op.OSpec;
import com.kgm.commands.ospec.op.OpTrim;
import com.kgm.commands.ospec.op.OpUtil;
import com.kgm.common.WaitProgressBar;
import com.kgm.common.utils.CustomUtil;
import com.kgm.common.utils.SYMTcUtil;
import com.symc.plm.rac.prebom.common.PropertyConstant;
import com.symc.plm.rac.prebom.common.TypeConstant;
import com.symc.plm.rac.prebom.common.util.BomUtil;
import com.symc.plm.rac.prebom.masterlist.model.MasterListDataMapper;
import com.symc.plm.rac.prebom.masterlist.model.StoredOptionSet;
import com.symc.plm.rac.prebom.masterlist.view.MasterListTablePanel;
import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.kernel.TCComponentGroup;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentPerson;
import com.teamcenter.rac.kernel.TCComponentRole;
import com.teamcenter.rac.kernel.TCComponentUser;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;

public class ComparePreBOMWithDateOperation extends AbstractAIFOperation {

	private TCComponentItem selectedItem = null;
	private OSpec oldOspec = null, newOspec = null;
	private ArrayList<String> essentialNames = null;
	private HashMap<String, StoredOptionSet> oldStoredOptionSetMap = null, newStoredOptionSetMap = null;
	private Date oldDate = null, newDate = null;
	private WaitProgressBar waitBar = null;
	private HashMap<String, Object> resultData = new HashMap();
	
	public static String DATA_OLD_STORED_OPTION_SET = "OLD_STORED_OPTION_SET";
	public static String DATA_NEW_STORED_OPTION_SET = "NEW_STORED_OPTION_SET";
	public static String DATA_ERROR = "ERROR";
	public static String DATA_ITEM_ID = "ITEM_ID";
	public static String DATA_OLD_DATE = "OLD_DATE";
	public static String DATA_NEW_DATE = "NEW_DATE";
	public static String DATA_OLD_OSPEC = "OLD_OSPEC";
	public static String DATA_NEW_OSPEC = "NEW_OSPEC";
	public static String DATA_ESSENTIAL_NAMES = "ESSENTIAL_NAMES";
	public static String DATA_OLD_DATA = "OLD_DATA";
	public static String DATA_NEW_DATA = "NEW_DATA";
	private Vector oldData = null, newData = null;
	private ScriptEngineManager manager = new ScriptEngineManager();
	private ScriptEngine engine = manager.getEngineByName("js"); 
	private int totFmpCount = 0, finishedCount = 0;
//	private String currentUserName;
//	private String currentGroup;
	private boolean isCordinator;
	private TCSession session = null;
	
	public ComparePreBOMWithDateOperation(TCComponentItem selectedItem, 
			Date oldDate, Date newDate, WaitProgressBar waitBar){
		this.selectedItem = selectedItem;
		this.oldDate = oldDate;
		this.newDate = newDate;
		this.waitBar = waitBar;
		this.session = selectedItem.getSession();
	}
	
	private synchronized void setMsg(String msg){
		if( waitBar != null){
			waitBar.setStatus(msg);
		}
	}
	@Override
	public void executeOperation() throws Exception {
		// TODO Auto-generated method stub
		
		Calendar cal = Calendar.getInstance();
		Date startDate = cal.getTime();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");
		System.out.println("Start Time: " + sdf.format(startDate));
		storeOperationResult(resultData);
		
		SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd");
		String dateStr = sdf2.format(oldDate);
		oldDate = sdf.parse(dateStr + " 23:59:59");
		
		dateStr = sdf2.format(newDate);
		newDate = sdf.parse(dateStr + " 23:59:59");
		
		String itemId = selectedItem.getProperty(PropertyConstant.ATTR_NAME_ITEMID);
		resultData.put(DATA_ITEM_ID, itemId);
		
		String typeStr = selectedItem.getType();
		
		oldData = new ClonableVector();
		newData = new ClonableVector();
		String product_project_code = "";
		
		TCComponentItemRevision oldPreProductRevision = null, newPreProductRevision = null;
		if( typeStr.equals(TypeConstant.S7_PREPRODUCTTYPE)){
			setMsg("Loading Pre-Product...");
			oldPreProductRevision = BomUtil.getItemRevisionOnReleaseDate(selectedItem, oldDate);
			newPreProductRevision = BomUtil.getItemRevisionOnReleaseDate(selectedItem, newDate);
			product_project_code = selectedItem.getLatestItemRevision().getStringProperty("s7_PROJECT_CODE");
		}else{
			if( typeStr.equals(TypeConstant.S7_PREFUNCTIONTYPE)){
				setMsg("Loading Pre-Function...");
			}else{
				setMsg("Loading Pre-FMP...");
			}
			TCComponentItemRevision productRevision = (TCComponentItemRevision)BomUtil.getParent(selectedItem.getLatestItemRevision(), TypeConstant.S7_PREPRODUCTREVISIONTYPE);
			if( productRevision == null){
				resultData.put(DATA_ERROR, new Exception("Could not find PreProduct."));
				return;
			}
			oldPreProductRevision = BomUtil.getItemRevisionOnReleaseDate(productRevision.getItem(), oldDate);
			newPreProductRevision = BomUtil.getItemRevisionOnReleaseDate(productRevision.getItem(), newDate);
			product_project_code = productRevision.getStringProperty("s7_PROJECT_CODE");
		}
		
		if( oldPreProductRevision == null || newPreProductRevision == null){
			resultData.put(DATA_ERROR, new Exception("Could not find PreProduct."));
			return;
		}
		
		String oldOspecNo = oldPreProductRevision.getProperty(PropertyConstant.ATTR_NAME_OSPECNO);
		if( oldOspecNo == null || oldOspecNo.equals("")){
			resultData.put(DATA_ERROR, new Exception("Could not found Old OSpec."));
			return;
		}
		
		String newOspecNo = newPreProductRevision.getProperty(PropertyConstant.ATTR_NAME_OSPECNO);
		if( newOspecNo == null || newOspecNo.equals("")){
			resultData.put(DATA_ERROR, new Exception("Could not found New OSpec."));
			return;
		}
		
		setCurrentUserInfo();
		
		//Old Ospec 정보 셋.
		int t = oldOspecNo.lastIndexOf("-");
		String ospecId = oldOspecNo.substring(0, t);
		String ver = oldOspecNo.substring(t + 1);
		TCComponentItemRevision ospecRevision = SYMTcUtil.findItemRevision(session, ospecId, ver);
		oldOspec = BomUtil.getOSpec(ospecRevision);
		resultData.put(DATA_OLD_OSPEC, oldOspec);
		
		//New Ospec 정보 셋
		t = newOspecNo.lastIndexOf("-");
		ospecId = newOspecNo.substring(0, t);
		ver = newOspecNo.substring(t + 1);
		ospecRevision = SYMTcUtil.findItemRevision(session, ospecId, ver);
		newOspec = BomUtil.getOSpec(ospecRevision);
		resultData.put(DATA_NEW_OSPEC, newOspec);
		
		//oldOspec과 newOspec에 서로 없는 Trim을 넣어주어, Vector형식의 데이타를 추출할때 size가 동일하도록 맞춰준다.
		HashMap<String, OpTrim> oldTrimMap = oldOspec.getTrims();
		HashMap<String, OpTrim> newTrimMap = newOspec.getTrims();
		
		ArrayList<String> oldTrimKeyList = BomUtil.getKeyList(oldTrimMap);
		ArrayList<String> newTrimKeyList = BomUtil.getKeyList(newTrimMap);
		
		ArrayList<String> onlyOldList = (ArrayList<String>)oldTrimKeyList.clone();
		ArrayList<String> onlyNewList = (ArrayList<String>)newTrimKeyList.clone();
		onlyNewList.removeAll(oldTrimKeyList);
		onlyOldList.removeAll(newTrimKeyList);
		
		// [SR160316-025][20160325][jclee] MLM Compare BUG Fix
		for( int i = 0; i < onlyOldList.size(); i++){
			OpTrim dtDummy = new OpTrim();
			dtDummy.setTrim(onlyOldList.get(i));
			dtDummy.setColOrder(-1);
			newTrimMap.put(onlyOldList.get(i), dtDummy);
//			newTrimMap.put(onlyOldList.get(i), null);
		}
		
		for( int i = 0; i < onlyNewList.size(); i++){
			OpTrim dtDummy = new OpTrim();
			dtDummy.setTrim(onlyNewList.get(i));
			dtDummy.setColOrder(-1);
			oldTrimMap.put(onlyNewList.get(i), dtDummy);
//			oldTrimMap.put(onlyNewList.get(i), null);
		}
		
		// Trim이 새롭게 추가가 되거나 삭제가 됐을 경우 각 Old OSpec, New OSpec 에 Merge된 Trim에 Column Order를 다시 정렬하여 입력.
		// Trim이 변화가 없을 경우 별도로 Trim의 Order를 변경할 필요가 없음.
		if (onlyOldList.size() > 0 || onlyNewList.size() > 0) {
			OpUtil.resetColOrder(oldOspec, newOspec);
		}
		
		oldStoredOptionSetMap = BomUtil.getOptionSet(oldOspec);
		resultData.put(DATA_OLD_STORED_OPTION_SET, oldStoredOptionSetMap);
		
		newStoredOptionSetMap = BomUtil.getOptionSet(newOspec);
		resultData.put(DATA_NEW_STORED_OPTION_SET, newStoredOptionSetMap);
		
		essentialNames = BomUtil.getEssentialName();
		resultData.put(DATA_ESSENTIAL_NAMES, essentialNames);
		
		resultData.put(DATA_OLD_DATE, oldDate);
		resultData.put(DATA_NEW_DATE, newDate);
		
		resultData.put(DATA_OLD_DATA, oldData);
		resultData.put(DATA_NEW_DATA, newData);
		
		ExecutorService executor = Executors.newFixedThreadPool(2);
		BOMExpanderWithDate oldThread = new BOMExpanderWithDate(oldOspec, oldStoredOptionSetMap, oldDate, typeStr, oldData, product_project_code);
		BOMExpanderWithDate newThread = new BOMExpanderWithDate(newOspec, newStoredOptionSetMap, newDate, typeStr, newData, product_project_code);
		
		executor.execute(oldThread);
		executor.execute(newThread);
		
		executor.shutdown();
		while (!executor.isTerminated()) {
		}
		
        cal = Calendar.getInstance();
		Date endDate = cal.getTime();
		System.out.println("Finish Time: " + sdf.format(endDate));
		storeOperationResult(resultData);
	}
	
	private void setCurrentUserInfo() throws TCException{
		TCSession session = CustomUtil.getTCSession();
		TCComponentUser user = session.getUser();
		TCComponentGroup tGroup = session.getGroup();
		TCComponentPerson person = (TCComponentPerson)user.getRelatedComponent("person");
		String currentUserName = person.getProperty("user_name");
		String currentGroup = tGroup.getGroupName();
		String currentPa6Group = person.getProperty("PA6");
		
		resultData.put("USER_ID", user.getUserId());
		resultData.put("USER_NAME", currentUserName);
		resultData.put("USER_GROUP", currentGroup);
		resultData.put("USER_PA6_GROUP", currentPa6Group);
		
		ArrayList<String> roles = new ArrayList();
		Map<TCComponentGroup, List<TCComponentRole>> roleTable = user.getGroupRolesTable();
		for( TCComponentGroup group : roleTable.keySet()){
			List<TCComponentRole> roleList = roleTable.get(group);
			for( TCComponentRole role : roleList){
				String roleName = role.getProperty("role_name");
				roles.add(roleName);
			}
		}
		
		if( roles.contains("CORDINATOR")){
			isCordinator = true;
		}
		resultData.put("IS_CORDINATOR", isCordinator);
	}

	private synchronized void displayCnt(String fmpId){
		finishedCount++;
		setMsg( fmpId + " Finished : " + finishedCount + "/" +  totFmpCount);
	}
	
	/**
	 * FMP 순서에 맞게 저장.
	 * @param fmpData
	 */
	synchronized void addData(Vector<Vector> data, Vector<Vector> fmpData){
		
		if( data.isEmpty()){
			data.addAll(fmpData);
			return;
		}
		
		if( fmpData == null || fmpData.isEmpty()){
			return;
		}
		
		Vector firstRow = fmpData.get(0);
		String fmpFuncNo = firstRow.get(MasterListTablePanel.MASTER_LIST_FUNCTION_IDX + 1).toString();
		int idxToInsert = -1;
		
		for( int i = 0; i < data.size();i++){
			Vector row = data.get(i);
			String funcNo = row.get(MasterListTablePanel.MASTER_LIST_FUNCTION_IDX + 1).toString();
			if( funcNo.compareTo(fmpFuncNo) > 0){
				idxToInsert = i;
				break;
			}
		}
		
		if( idxToInsert < 0 ){
			data.addAll(fmpData);
			return;
		}
		
		for( int i = fmpData.size() - 1; i >= 0; i--){
			data.insertElementAt(fmpData.get(i), idxToInsert);
		}
		
	}
	
	class BOMLoader implements Runnable{
		
		private TCComponentBOMLine fmpLine;
		private OSpec ospec;
		private HashMap<String, StoredOptionSet> storedOptionSetMap;
		private Vector<Vector> data = null;
		private String product_project_code;
		
		public BOMLoader(TCComponentBOMLine fmpLine, OSpec ospec, 
				HashMap<String, StoredOptionSet> storedOptionSetMap, Vector<Vector> data, String product_project_code){
			this.fmpLine = fmpLine;
			this.ospec = ospec;
			this.storedOptionSetMap = storedOptionSetMap;
			this.data = data;
			this.product_project_code = product_project_code;
		}

		@Override
		public void run() {
			MasterListDataMapper releaseDataMapper;
			Vector<Vector> fmpData = null;
			String itemId = null;
			try {
				itemId = fmpLine.getItem().getProperty(PropertyConstant.ATTR_NAME_ITEMID);
				setMsg( "Loading " + itemId + " BOM Info.");
				releaseDataMapper = new MasterListDataMapper(fmpLine, ospec, essentialNames, false);
				BOMLoadOperation.loadChildMap(releaseDataMapper, fmpLine, storedOptionSetMap, product_project_code);
				
				HashMap<String, Vector> keyRowMapper = new HashMap();
				fmpData = releaseDataMapper.createMasterListData(keyRowMapper, storedOptionSetMap, engine);
				
				addData(data, fmpData);
//				data.addAll(fmpData);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}finally{
				releaseDataMapper = null;
				fmpData = null;
				displayCnt(itemId);
			}
		}
	}
	
	class ClonableVector extends Vector{
		
		public synchronized Object clone() {
			// TODO Auto-generated method stub
			Vector<Vector> newData = new Vector();
			for (int i = 0; i < this.elementCount; i++) {
				Vector row = (Vector) elementData[i];
				Vector newRow = new Vector();
				newRow.addAll(row);

				newData.add(newRow);
			}
			return newData;
		}
	}
	
	private class BOMExpanderWithDate implements Runnable{
		
		private OSpec ospec = null;
		private HashMap<String, StoredOptionSet> storedOptionSetMap = null;
		private Date date = null;
		private String typeStr = null;
		private Vector data = null;
		private String product_project_code;
		
		public  BOMExpanderWithDate(OSpec ospec, 
				HashMap<String, StoredOptionSet> storedOptionSetMap,
				Date date, String typeStr, Vector data, String product_project_code){
			this.ospec = ospec;
			this.storedOptionSetMap = storedOptionSetMap;
			this.date = date;
			this.typeStr = typeStr;
			this.data = data;
			this.product_project_code = product_project_code;
		}
		
		public void run(){
			//Release기준의 BOM Load
	        TCComponentBOMWindow bomWindow = null;
	        try{
	        	TCComponentBOMLine topLine = BomUtil.getBomLine(selectedItem, date);
	        	if( topLine != null){
	        		bomWindow = topLine.window();
	        	}
	        	setMsg("Loading Released BOM Info.");
	        	
	        	ExecutorService executor = Executors.newFixedThreadPool(20);
	        	ArrayList<BOMLoader> loaderList = new ArrayList();
	        	//Product일 경우.
	        	if( typeStr.equals(TypeConstant.S7_PREPRODUCTTYPE)){
		        	AIFComponentContext[] funcContext = topLine.getChildren();
		        	for( int i = 0; funcContext != null && i < funcContext.length; i++){
		        		TCComponentBOMLine funcLine = (TCComponentBOMLine)funcContext[i].getComponent();
		        		AIFComponentContext[] fmpContext = funcLine.getChildren();
		        		for( int j = 0; fmpContext != null && j < fmpContext.length; j++){
		        			TCComponentBOMLine fmpLine = (TCComponentBOMLine)fmpContext[j].getComponent();
		        			
		        			BOMLoader loader = new BOMLoader(fmpLine, ospec, storedOptionSetMap, data, product_project_code);
		        			loaderList.add(loader);
		        			
		        			totFmpCount++;
		        		}
		        	}
	        	}else if(typeStr.equals(TypeConstant.S7_PREFUNCTIONTYPE)){
	        		//Function일 경우
	        		AIFComponentContext[] fmpContext = topLine.getChildren();
	        		for( int j = 0; fmpContext != null && j < fmpContext.length; j++){
	        			TCComponentBOMLine fmpLine = (TCComponentBOMLine)fmpContext[j].getComponent();
	        			BOMLoader loader = new BOMLoader(fmpLine, ospec, storedOptionSetMap, data, product_project_code);
	        			loaderList.add(loader);
	        			totFmpCount++;
	        		}
	        	}else{
	        		TCComponentBOMLine fmpLine = topLine;
        			BOMLoader loader = new BOMLoader(fmpLine, ospec, storedOptionSetMap, data, product_project_code);
        			loaderList.add(loader);
        			totFmpCount++;
	        	}
	        	
	        	for( BOMLoader loader : loaderList){
	        		executor.execute(loader);
	        	}
	        	
	        	executor.shutdown();
	    		while (!executor.isTerminated()) {
	    		}
	    		
	        }catch(Exception e){
	        	resultData.put(DATA_ERROR, e);
	        }finally{
	        	if( bomWindow != null){
	        		try {
						bomWindow.close();
					} catch (TCException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	        	}
	        }
		}
	}
	
	
}

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

import com.ssangyong.commands.ospec.op.OSpec;
import com.ssangyong.common.WaitProgressBar;
import com.ssangyong.common.remote.DataSet;
import com.ssangyong.common.remote.SYMCRemoteUtil;
import com.ssangyong.common.utils.CustomUtil;
import com.ssangyong.common.utils.SYMTcUtil;
import com.symc.plm.rac.prebom.common.PropertyConstant;
import com.symc.plm.rac.prebom.common.TypeConstant;
import com.symc.plm.rac.prebom.common.util.BomUtil;
import com.symc.plm.rac.prebom.masterlist.model.MasterListDataMapper;
import com.symc.plm.rac.prebom.masterlist.model.StoredOptionSet;
import com.symc.plm.rac.prebom.masterlist.view.MasterListTablePanel;
import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.kernel.TCComponentBOMWindowType;
import com.teamcenter.rac.kernel.TCComponentGroup;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentPerson;
import com.teamcenter.rac.kernel.TCComponentRole;
import com.teamcenter.rac.kernel.TCComponentUser;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;

public class PreBOMLoadWithDateOperation extends AbstractAIFOperation {

	private TCComponentItemRevision selectedRevision = null;
	private OSpec ospec = null;
	private ArrayList<String> essentialNames = null;
	private HashMap<String, StoredOptionSet> storedOptionSetMap = null;
	private Date date = null;
	private ArrayList<String> alSelectedFMP = new ArrayList<String>();
	private WaitProgressBar waitBar = null;
	private HashMap<String, Object> resultData = new HashMap();
	
	public static String DATA_STORED_OPTION_SET = "STORED_OPTION_SET";
	public static String DATA_ERROR = "ERROR";
	public static String DATA_MAPPER = "DATA_MAPPER";
	public static String DATA_DATE = "DATE";
	public static String DATA_OSPEC = "OSPEC";
	public static String DATA_ESSENTIAL_NAMES = "ESSENTIAL_NAMES";
	public static String DATA_DATA = "DATA";
	private Vector<Vector> data = null;
	private ScriptEngineManager manager = new ScriptEngineManager();
	private ScriptEngine engine = manager.getEngineByName("js"); 
	private int totFmpCount = 0, finishedCount = 0;
	private String currentUserId;
	private String currentUserName;
	private String currentGroup;
	private boolean isCordinator;
	
	public PreBOMLoadWithDateOperation(TCComponentItemRevision selectedRevision, 
			Date date, ArrayList<String> alSelectedFMP, WaitProgressBar waitBar){
		this.selectedRevision = selectedRevision;
		this.date = date;
		this.alSelectedFMP = alSelectedFMP;
		this.waitBar = waitBar;
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
		SimpleDateFormat sdformat = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");
		System.out.println("Start Time: " + sdformat.format(startDate));
		storeOperationResult(resultData);
		
		String typeStr = selectedRevision.getItem().getType();
		if( !typeStr.equals(TypeConstant.S7_PREPRODUCTTYPE) && !typeStr.equals(TypeConstant.S7_PREFUNCTIONTYPE)){
			resultData.put(DATA_ERROR, new Exception("Invalid Item Type..."));
			return;
		}
		
		data = new Vector() {

			@Override
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

		};
		
		TCComponentItemRevision preProductRevision = null;
		if( typeStr.equals(TypeConstant.S7_PREPRODUCTTYPE)){
			setMsg("Loading Pre-Product...");
			preProductRevision = BomUtil.getItemRevisionOnReleaseDate(selectedRevision.getItem(), date);
		}else{
			setMsg("Loading Pre-Function...");
			preProductRevision = (TCComponentItemRevision)BomUtil.getParent(selectedRevision, TypeConstant.S7_PREPRODUCTREVISIONTYPE);
			if( preProductRevision == null){
				resultData.put(DATA_ERROR, new Exception("Could not find PreProduct."));
				return;
			}
			preProductRevision = BomUtil.getItemRevisionOnReleaseDate(preProductRevision.getItem(), date);
		}
		
		if( preProductRevision == null){
			resultData.put(DATA_ERROR, new Exception("Could not find PreProduct."));
			return;
		}
		
		String product_project_code = preProductRevision.getStringProperty("s7_PROJECT_CODE");
		
		preProductRevision = BomUtil.getItemRevisionOnReleaseDate(preProductRevision.getItem(), date);
		if( preProductRevision == null){
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			resultData.put(DATA_ERROR, new Exception("Could not found the Pre-Product[" + sdf.format(date) + "]"));
			return;
		}
		String ospecNo = preProductRevision.getProperty(PropertyConstant.ATTR_NAME_OSPECNO);
		if( ospecNo == null || ospecNo.equals("")){
			resultData.put(DATA_ERROR, new Exception("Could not found OSpec."));
			return;
		}
		
		setCurrentUserInfo();
		
		TCSession session = selectedRevision.getSession();
		int t = ospecNo.lastIndexOf("-");
		String ospecId = ospecNo.substring(0, t);
		String ver = ospecNo.substring(t + 1);
		TCComponentItemRevision ospecRevision = SYMTcUtil.findItemRevision(session, ospecId, ver);
		ospec = BomUtil.getOSpec(ospecRevision);
		resultData.put(DATA_OSPEC, ospec);
		
		storedOptionSetMap = BomUtil.getOptionSet(ospec);
		resultData.put(DATA_STORED_OPTION_SET, storedOptionSetMap);
		
		essentialNames = BomUtil.getEssentialName();
		resultData.put(DATA_ESSENTIAL_NAMES, essentialNames);
		
		resultData.put(DATA_DATE, date);
		
		//Release기준의 BOM Load
		TCComponentBOMWindowType windowType = (TCComponentBOMWindowType) session.getTypeComponent("BOMWindow");
        TCComponentBOMWindow bomWindow = null;
        TCComponentItemRevision latestReleasedRevision = null;
        try{
        	TCComponentBOMLine topLine = BomUtil.getBomLine(selectedRevision.getItem(), date);
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
	        			String sFMPNo = fmpLine.getItem().getProperty("item_id");
	        			for (int k = 0; k < alSelectedFMP.size(); k++) {
							if (alSelectedFMP.get(k).equals(sFMPNo)) {
								BOMLoader loader = new BOMLoader(fmpLine,product_project_code);
								loaderList.add(loader);
								
								totFmpCount++;
							}
						}
	        		}
	        	}
        	}else{
        		//Function일 경우
        		AIFComponentContext[] fmpContext = topLine.getChildren();
        		for( int j = 0; fmpContext != null && j < fmpContext.length; j++){
        			TCComponentBOMLine fmpLine = (TCComponentBOMLine)fmpContext[j].getComponent();
        			String sFMPNo = fmpLine.getItem().getProperty("item_id");
        			for (int k = 0; k < alSelectedFMP.size(); k++) {
						if (alSelectedFMP.get(k).equals(sFMPNo)) {
							BOMLoader loader = new BOMLoader(fmpLine,product_project_code);
							loaderList.add(loader);
							
							totFmpCount++;
						}
					}
        		}
        	}
        	
        	for( BOMLoader loader : loaderList){
        		executor.execute(loader);
        	}
        	
        	executor.shutdown();
    		while (!executor.isTerminated()) {
    		}
    		
    		resultData.put(DATA_DATA, data);
        	
        }catch(Exception e){
        	resultData.put(DATA_ERROR, e);
        }finally{
        	if( bomWindow != null){
        		bomWindow.close();
        	}
        }
        
        cal = Calendar.getInstance();
		Date endDate = cal.getTime();
		System.out.println("Finish Time: " + sdformat.format(endDate));
		storeOperationResult(resultData);
	}
	
	/**
	 * FMP 순서에 맞게 저장.
	 * @param fmpData
	 */
	synchronized void addData(Vector<Vector> fmpData){
		
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
	
	private void setCurrentUserInfo() throws TCException{
		TCSession session = CustomUtil.getTCSession();
		TCComponentUser user = session.getUser();
		TCComponentGroup tGroup = session.getGroup();
		TCComponentPerson person = (TCComponentPerson)user.getRelatedComponent("person");
		currentUserId = user.getUserId();
		currentUserName = person.getProperty("user_name");
		currentGroup = tGroup.getGroupName();
		String currentPa6Group = person.getProperty("PA6");
		
		resultData.put("USER_ID", currentUserId);
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
	
	class BOMLoader implements Runnable{
		
		private TCComponentBOMLine fmpLine;
		private String product_project_code;
		
		public BOMLoader(TCComponentBOMLine fmpLine, String product_project_code){
			this.fmpLine = fmpLine;
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
				
				addData(fmpData);
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
	
	
}

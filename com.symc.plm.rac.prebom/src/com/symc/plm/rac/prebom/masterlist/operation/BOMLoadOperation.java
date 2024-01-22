package com.symc.plm.rac.prebom.masterlist.operation;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ssangyong.commands.ospec.op.OSpec;
import com.ssangyong.common.WaitProgressBar;
import com.ssangyong.common.utils.CustomUtil;
import com.ssangyong.common.utils.variant.VariantOption;
import com.symc.plm.rac.prebom.common.PropertyConstant;
import com.symc.plm.rac.prebom.common.TypeConstant;
import com.symc.plm.rac.prebom.common.util.BomUtil;
import com.symc.plm.rac.prebom.common.util.OptionManager;
import com.symc.plm.rac.prebom.masterlist.model.MasterListDataMapper;
import com.symc.plm.rac.prebom.masterlist.model.StoredOptionSet;
import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.kernel.TCComponentBOMWindowType;
import com.teamcenter.rac.kernel.TCComponentGroup;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentPerson;
import com.teamcenter.rac.kernel.TCComponentRole;
import com.teamcenter.rac.kernel.TCComponentUser;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCProperty;
import com.teamcenter.rac.kernel.TCSession;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class BOMLoadOperation extends AbstractAIFOperation {

	private TCComponentItemRevision preProductRevision = null;
	private TCComponentBOMLine targetLine = null;
	private OSpec ospec = null;
	private WaitProgressBar waitBar = null;
	private MasterListDataMapper dataMapper = null;
	private MasterListDataMapper releaseDataMapper = null;
	private TCComponentItemRevision ospecRevision = null;
	private ArrayList<VariantOption> fmpOptionList = null;
	private OptionManager optionManager = null;
	private HashMap<String, StoredOptionSet> storedOptionSetMap = new HashMap();
	private ArrayList<String> essentialNames = null;
	private HashMap<String, Object> resultData = new HashMap();
	private String currentUserId = "";
	private String currentGroup = "";
	private String currentUserName = "";
	private String currentPa6Group = "";
	private boolean isCordinator = false;
	
	public static String DATA_STORED_OPTION_SET = "STORED_OPTION_SET";
	public static String DATA_ERROR = "ERROR";
	public static String DATA_MAPPER = "DATA_MAPPER";
	public static String DATA_CHILD_ROW_KEY = "DATA_CHILD_ROW_KEY";
	public static String DATA_DATE = "DATE";
	public static String DATA_OSPEC = "OSPEC";
	
	public BOMLoadOperation(TCComponentItemRevision preProductRevision, TCComponentBOMLine targetLine, TCComponentItemRevision ospecRevision, WaitProgressBar waitBar){
		this.preProductRevision = preProductRevision;
		this.targetLine = targetLine;	//옵션을 가져오기위해 현재 Structure manager상의 FMP
		this.ospecRevision = ospecRevision;
		this.waitBar = waitBar;
	}
	
	@Override
	public void executeOperation() throws Exception {
		
		storeOperationResult(resultData);
		
		waitBar.setStatus("Loading User Info.");
		setCurrentUserInfo();
		
		waitBar.setStatus("Loading Contents Info.");
		TCProperty tcProp = preProductRevision.getTCProperty(PropertyConstant.ATTR_NAME_CONTENTS);
		resultData.put("CONTENTS", tcProp.getStringArrayValue());
		String product_project_code = preProductRevision.getStringProperty("s7_PROJECT_CODE");
		
		waitBar.setStatus("Loading Fmp Option Info.");
		
		// FMP에서 옵션 리스트를 가져와야 함.
		optionManager = new OptionManager(targetLine, false);
		fmpOptionList = optionManager.getOptionSet(targetLine,null, null, null, false, false);
		
		waitBar.setStatus("Loading OSpec Info.");
		ospec = BomUtil.getOSpec(ospecRevision);
		
		waitBar.setStatus("Loading Stored Option Set.");
		storedOptionSetMap = BomUtil.getOptionSet(ospec);
		
		//필수 파트 명 가져오기.
		waitBar.setStatus("Loading Essential Part Name.");
		essentialNames = BomUtil.getEssentialName();
		
		//현재 Structure BOM Window기준의 BOM Load
		waitBar.setStatus("Loading BOM Info.");
		
		Date from = new Date();
		System.out.println("Load Start : " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(from));
		dataMapper = new MasterListDataMapper(targetLine, ospec, essentialNames, true);
		try{
			loadChildMap(dataMapper, targetLine, storedOptionSetMap, product_project_code);
		}catch(Exception e){
			resultData.put(BOMLoadWithDateOperation.DATA_ERROR, e);
        	throw e;
		}
		
		//Release기준의 BOM Load		
        TCComponentBOMWindow bomWindow = null;
        try{
        	
        	waitBar.setStatus("Loading Released BOM Info.");
        	TCComponentBOMLine topLine = BomUtil.getBomLine(targetLine.getItem(), Calendar.getInstance().getTime());
        	if( topLine != null){
	        	bomWindow = topLine.window();
	        	releaseDataMapper = new MasterListDataMapper(topLine, ospec, essentialNames, false);
	            loadChildMap(releaseDataMapper, topLine, storedOptionSetMap, product_project_code);
        	}
            
        }catch(Exception e){
        	resultData.put(BOMLoadWithDateOperation.DATA_ERROR, e);
        	throw e;
        }finally{
        	if( bomWindow != null){
        		bomWindow.close();
        	}
        }
        
		Date to = new Date();
		System.out.println("Load End : " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(to) + " : " + String.valueOf((to.getTime() - from.getTime())/1000) );
	}
	
	private void setCurrentUserInfo() throws TCException{
		TCSession session = CustomUtil.getTCSession();
		TCComponentUser user = session.getUser();
		TCComponentGroup tGroup = session.getGroup();
		TCComponentPerson person = (TCComponentPerson)user.getRelatedComponent("person");
		currentPa6Group = person.getProperty("PA6");
		currentUserId = user.getUserId();
		currentUserName = person.getProperty("user_name");
		currentGroup = tGroup.getGroupName();
		
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
	
	public ArrayList<String> getEssentialNames() {
		return essentialNames;
	}
	
	public HashMap<String, StoredOptionSet> getStoredOptionSetMap() {
		return storedOptionSetMap;
	}
	public OptionManager getOptionManager() {
		return optionManager;
	}
	public ArrayList<VariantOption> getFmpOptionList() {
		return fmpOptionList;
	}
	
	public OSpec getOspec(){
		return ospec;
	}
	
	public MasterListDataMapper getDataMapper() {
		return dataMapper;
	}
	
	public MasterListDataMapper getReleaseDataMapper() {
		return releaseDataMapper;
	}
	
	public static void loadChildMap(MasterListDataMapper dataMapper, TCComponentBOMLine parent, HashMap<String, StoredOptionSet> storedOptionSetMap, String product_project_code) throws Exception{
		
		String type = parent.getItem().getType();
		if( !type.equals(TypeConstant.S7_PREFUNCMASTERTYPE)
				&&  !type.equals(TypeConstant.S7_PREVEHICLEPARTTYPE)){
			return;
		}
		
		if( !parent.hasChildren()){
			return;
		}
		
		AIFComponentContext[] context = parent.getChildren();
		if( context == null || context.length == 0){ 
			return;
		}
		
		// SEQ No 순으로 정렬
		// MLM내 2LV Part 정렬 시 Find No 반대 순으로 정렬되는 버그 수정
		ArrayList<AIFComponentContext> list = new ArrayList<AIFComponentContext>();
		for (int inx = 0; inx < context.length; inx++) {
			list.add(context[inx]);
		}
		
		// Sorting
		Collections.sort(list, new Comparator<AIFComponentContext>() {
			@Override
			public int compare(AIFComponentContext comp1, AIFComponentContext comp2)
			{
				try {
					TCComponentBOMLine childLine1 = (TCComponentBOMLine)comp1.getComponent();
					TCComponentBOMLine childLine2 = (TCComponentBOMLine)comp2.getComponent();
					
					String sSeqNo1 = childLine1.getProperty("bl_sequence_no");
					String sSeqNo2 = childLine2.getProperty("bl_sequence_no");
					
					return sSeqNo1.compareTo(sSeqNo2);
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				return 0;
			}
		});
		
		Object[] sortedContext = list.toArray();
		
		for( int i = 0; i < sortedContext.length; i++){
			TCComponentBOMLine childLine = (TCComponentBOMLine)((AIFComponentContext)sortedContext[i]).getComponent();
			
			dataMapper.addBomLine(childLine, storedOptionSetMap, product_project_code);
			loadChildMap(dataMapper, childLine, storedOptionSetMap, product_project_code);
		}

		return;
	}
	
//	/**
//	 * bUseOspec = True 일 경우  ospec에서 추출.
//	 * bUseOspec = False 일 경우  TC SOS 에서 추출.
//	 * @param optionSetMap
//	 * @param bUseOspec
//	 * @throws Exception
//	 */
//	private void setOptionSet(HashMap<String, StoredOptionSet> optionSetMap, boolean bUseOspec) throws Exception{
//		if( bUseOspec){
//			ArrayList<OpTrim> trimList = ospec.getTrimList();
//			HashMap<String, ArrayList<Option>> trimOptionMap = ospec.getOptions();
//			
//			for( OpTrim opTrim : trimList){
//				ArrayList<Option> options = trimOptionMap.get(opTrim.getTrim());
//				String stdName = opTrim.getTrim() + "_STD";
//				String optName = opTrim.getTrim() + "_OPT";
//				StoredOptionSet stdSos = new StoredOptionSet(stdName);
//				stdSos.add("TRIM", stdName);
//				StoredOptionSet optSos = new StoredOptionSet(optName);
//				optSos.add("TRIM", optName);
//				
//				for( Option option : options){
//					if( option.getValue().equalsIgnoreCase("S")){
//						stdSos.add(option.getOp(), option.getOpValue());
//						optSos.add(option.getOp(), option.getOpValue());
//					}else if( !option.getValue().equalsIgnoreCase("-") ){
//						optSos.add(option.getOp(), option.getOpValue());
//					}
//				}
//				
//				optionSetMap.put(stdName, stdSos);
//				optionSetMap.put(optName, optSos);
//			}
//		}else{
//			AIFComponentContext[] relatedContexts = preProductRevision.getRelated("IMAN_reference");
//			for( AIFComponentContext context : relatedContexts){
//				TCComponent com = (TCComponent)context.getComponent();
//				if( com.getType().equals("StoredOptionSet")){
//					String sosName = com.getProperty(PropertyConstant.ATTR_NAME_ITEMNAME);
//					StoredOptionSet sos = optionSetMap.get(sosName);
//					if( sos == null){
//						sos = new StoredOptionSet(sosName);
//						optionSetMap.put(sosName, sos);
//					}
//					
//					SYMCRemoteUtil remote = new SYMCRemoteUtil();
//					DataSet ds = new DataSet();
//					ds.put("PUID", com.getUid());
//					ArrayList<HashMap<String, String>> list = (ArrayList<HashMap<String, String>>)remote.execute("com.ssangyong.service.MasterListService", "getStoredOptionSet", ds);
//					for( int i = 0; list != null && i < list.size(); i++){
//						HashMap<String, String> resultMap = list.get(i);
//						sos.add(resultMap.get("POPTION"), resultMap.get("PSTRING_VALUE"));
//					}
//				}
//			}
//		}
//		
//	}
//	
//	private void setOptionSet(HashMap<String, StoredOptionSet> optionSetMap) throws Exception{
//		setOptionSet(optionSetMap, false);
//	}
	
}

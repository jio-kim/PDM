package com.symc.work.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.Clob;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ssangyong.common.remote.DataSet;
import com.symc.common.dao.TcCommonDao;
import com.symc.common.soa.biz.Session;
import com.symc.common.soa.biz.TcItemUtil;
import com.symc.common.soa.biz.TcSessionUtil;
import com.symc.common.soa.service.TcDataManagementService;
import com.symc.common.soa.service.TcServiceManager;
import com.symc.common.soa.service.TcStructureManagementService;
import com.symc.common.soa.util.TcConstants;
import com.symc.common.soa.util.TcUtil;
import com.symc.common.util.IFConstants;
import com.symc.work.model.WeldPointGroupVO;
import com.teamcenter.ets.soa.SoaHelper;
import com.teamcenter.schemas.soa._2006_03.exceptions.ServiceException;
import com.teamcenter.services.internal.loose.core.ICTService;
import com.teamcenter.services.internal.loose.core._2011_06.ICT.Arg;
import com.teamcenter.services.internal.loose.core._2011_06.ICT.InvokeICTMethodResponse;
import com.teamcenter.services.internal.strong.structuremanagement.VariantManagementService;
import com.teamcenter.services.internal.strong.structuremanagement._2011_06.VariantManagement;
import com.teamcenter.services.internal.strong.structuremanagement._2011_06.VariantManagement.ModularOptionsForBomResponse;
import com.teamcenter.services.internal.strong.structuremanagement._2011_06.VariantManagement.ModularOptionsInput;
import com.teamcenter.services.loose.core.DataManagementService;
import com.teamcenter.services.loose.core._2007_01.DataManagement.WhereReferencedResponse;
import com.teamcenter.services.strong.cad.StructureManagementRestBindingStub;
import com.teamcenter.services.strong.cad._2007_01.StructureManagement.AttributesInfo;
import com.teamcenter.services.strong.cad._2007_01.StructureManagement.CreateOrUpdateRelativeStructureResponse;
import com.teamcenter.services.strong.cad._2007_01.StructureManagement.RelativeStructureChildInfo;
import com.teamcenter.services.strong.cad._2007_12.StructureManagement.CreateOrUpdateRelativeStructureInfo2;
import com.teamcenter.services.strong.cad._2007_12.StructureManagement.CreateOrUpdateRelativeStructurePref2;
import com.teamcenter.services.strong.core.DispatcherManagementService;
import com.teamcenter.services.strong.core.ReservationService;
import com.teamcenter.services.strong.core.SessionService;
import com.teamcenter.services.strong.core._2006_03.DataManagement.CreateItemsOutput;
import com.teamcenter.services.strong.core._2008_06.DataManagement.ReviseInfo;
import com.teamcenter.services.strong.core._2008_06.DataManagement.ReviseResponse2;
import com.teamcenter.services.strong.core._2008_06.DispatcherManagement.KeyValueArguments;
import com.teamcenter.services.strong.query.SavedQueryService;
import com.teamcenter.services.strong.query._2006_03.SavedQuery.GetSavedQueriesResponse;
import com.teamcenter.services.strong.query._2007_09.SavedQuery.QueryResults;
import com.teamcenter.services.strong.query._2007_09.SavedQuery.SavedQueriesResponse;
import com.teamcenter.services.strong.query._2008_06.SavedQuery.QueryInput;
import com.teamcenter.services.strong.workflow.WorkflowService;
import com.teamcenter.services.strong.workflow._2008_06.Workflow.ContextData;
import com.teamcenter.services.strong.workflow._2008_06.Workflow.InstanceInfo;
import com.teamcenter.soa.client.Connection;
import com.teamcenter.soa.client.model.ModelObject;
import com.teamcenter.soa.client.model.ServiceData;
import com.teamcenter.soa.client.model.Type;
import com.teamcenter.soa.client.model.strong.BOMLine;
import com.teamcenter.soa.client.model.strong.BOMWindow;
import com.teamcenter.soa.client.model.strong.Dataset;
import com.teamcenter.soa.client.model.strong.DispatcherRequest;
import com.teamcenter.soa.client.model.strong.EPMTask;
import com.teamcenter.soa.client.model.strong.Effectivity;
import com.teamcenter.soa.client.model.strong.ImanQuery;
import com.teamcenter.soa.client.model.strong.Item;
import com.teamcenter.soa.client.model.strong.ItemRevision;
import com.teamcenter.soa.client.model.strong.PSBOMViewRevision;
import com.teamcenter.soa.client.model.strong.ReleaseStatus;
import com.teamcenter.soa.client.model.strong.RevisionRule;
import com.teamcenter.soa.exceptions.NotLoadedException;
import com.teamcenter.soaictstubs.ICCTException;
import com.teamcenter.soaictstubs.TcUtility;
//import com.teamcenter.services.internal.strong.core.DataManagementService;

/**
 * [차체 Function 용접점 관리 방안] [20150907][ymjang] X100 이외의 차종은 별도의 용접점 관리 Part를 생성하여 관리하고, X100 의 경우만, 기존의 용접점 대상 추출 로직을 그대로 적용한다.
 * [용점접 2차 개선안] [20150907][ymjang] 용접점 처리시 각 대상 Part 별로 상태를 기록하기 위해 상태 테이블에 데이터를 생성한다.
 * [차체 Function 용접점 관리 방안] [20150907][ymjang] X100 이외의 차종은 별도의 용접점 관리 Part를 생성하여 관리하고, X100 의 경우만, 기존의 용접점 대상 추출 로직을 그대로 적용한다.
 */
/**
 * @author slobbie
 *
 * Batch로 실행되는 서비스가 많고, 실행시작 시각이 중복될 경우, 해당 기회가 사라짐.
 * TC Login을 최대한 하지 않기 위해 DB쿼리를 이용함.
 * 
 * [SR번호][20141201] shcho, Old를 떼어내기만 하고 New를 붙여넣지 않는 경우에 대한 Null 처리 추가
 * [20151016][ymjang] newPartNo nullpointer excetpion 오류 수정
 * [20151016][ymjang] 해당 프로젝트에 적용될 대상 PART 만 찾아서 용접점을 생성해야 함.
 * [20151016][ymjang] X100 을 제외한 다른 차종들은 'W' Part 의 부모 Part 로 용접그룹을 생성함.
 * [20151016][ymjang] 용접점 그룹 아이템 생성시 용접 대상 Part 의 명칭을 부여한다.
 * [20151126][ymjang] Release 대상이 없을 경우, Skip (NullPointer Exception 오류)
 * [20151215][ymjang] Dispatcher Duplicate 오류 방지를 위한 Filtering 
 * [20151215][ymjang] TCSession 생성 방법 변경 (TcLoginService 이용)
 * [20170103][taeku.jeong] S201 의 경우 MECO No가 "CM"으로 시작하는데 이 경우 용접점 생성을 수행 하지 않도록 한다.
 */
public class WeldPointCreationService {

	public final static String FMP_ID = "FMP_ID";
	public final static String FMP_REV_ID = "FMP_REV_ID";
	public final static String ACTION_ADD = "ACTION_ADD";
	public final static String ACTION_REMOVE = "ACTION_REMOVE";
	
	public StringBuffer buffer = null;
	
	public boolean weldPointFlagFail = false;
	
	public TcItemUtil tcItemUtil  = null;
	
	boolean hasTransTarget = false;
	int countTranstarget = 0;
	
	
	
	/**
	 *  SM용 프로젝트 코드 변수 추가
	 *  미변환된 용접점을 재 변환 요청 하기 위해 추가 
	 *   
	 *  모든 프로젝트를 한번에 변환 요청 하기에는 다소 무리가 있다 판단 하여 프로젝트 별로 진행
	 */
	public final static String SM_PROJECT_CODE = "";
	public boolean notTransformPartList_boolean = false;
	
	TcLoginService tcLoginService = new TcLoginService();
	Session session = null;
	
	private String getLatestServiceTime(StringBuffer log) throws Exception {
		TcCommonDao commonDao = TcCommonDao.getTcCommonDao();

		//1. Weld pont Service의 마지막 실행 시간을 가져온다.
		String latestServiceTimeStr = (String)commonDao.selectOne("com.symc.weld.getLatestServiceTime", null);
		if( latestServiceTimeStr == null || latestServiceTimeStr.equals("")){

			StringBuffer errorBuffer = new StringBuffer();
			errorBuffer.append(IFConstants.TEXT_RETURN);
			errorBuffer.append("Weld Point Service Start Time not found.");
			errorBuffer.append(IFConstants.TEXT_RETURN);
			errorBuffer.append("See the CUSTOM_WEB_ENV table and insert a LATEST_WELD_POINT_SERVICE_START_TIME value.");

			throw new Exception(errorBuffer.toString());
		}

		return latestServiceTimeStr;
	}

	@SuppressWarnings("unchecked")
	private List<HashMap<String, String>> getEcoFromLatestServiceTime( String latestServiceTimeStr, StringBuffer log )throws Exception {
		TcCommonDao commonDao = TcCommonDao.getTcCommonDao();
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("latest_service_time", latestServiceTimeStr);
		List<HashMap<String, String>> list = (List<HashMap<String, String>>)commonDao.selectList("com.symc.weld.getEcoFromLatestServiceTime", map);
		if( list == null || list.isEmpty()){
			log.append(IFConstants.TEXT_RETURN);
			log.append("ECO not found.");
		}

		return list;
	}

	private String getCurrentTime() throws Exception{
		TcCommonDao commonDao = TcCommonDao.getTcCommonDao();
		String curTime = (String)commonDao.selectOne("com.symc.weld.getCurrentTime", null);

		return curTime;
	}

	@SuppressWarnings("unchecked")
	public String startWeldPointService() throws Exception {
		 hasTransTarget = false;
		 countTranstarget = 0;
		 notTransformPartList_boolean = false;
		StringBuffer log = new StringBuffer();
		buffer = new StringBuffer();

		try{
			addLog("=========================== Start Weld Point Service SYMC Web ==========================================");
			session = tcLoginService.getTcSession();
			tcItemUtil = new TcItemUtil(session);
			TcSessionUtil sessionUtil = new TcSessionUtil(session);
			sessionUtil.setByPass();
			TcCommonDao commonDao = TcCommonDao.getTcCommonDao();

			//0. DB에서 가져온 현재 시간.
			String curTime = getCurrentTime();

			//1. Weld pont Service의 마지막 실행 시간을 가져온다.
			String latestServiceTimeStr = getLatestServiceTime(log);

			//2. Weld Point Service 마지막 실행시간이후에 릴리즈 된 ECO를 가져온다.
			// Ex)
			// ECO_NO, AFFECTED_PROJECT
			// 34BG005, AS11
			List<HashMap<String, String>> list = getEcoFromLatestServiceTime(latestServiceTimeStr, log);

			//3. ECO와 연관있는 Product 추출.
			HashMap<String, String> tmpMap = new HashMap<String, String>();
			String productStr = "";
			HashMap<String, String> currentWeldFmpRevMap = new HashMap<String, String>();
			ArrayList<WeldPointGroupVO> targetList = new ArrayList<WeldPointGroupVO>();
			HashMap<String, String> productMap = new HashMap<String, String>();
			
			
			//			String newFmpRevID = "";
			//Test 를 위해 샘플 데이타 생성 시작
			//			list.clear();
			//			HashMap<String, String> sampleMap = new HashMap<String, String>();
			//			sampleMap.put("ECO_NO", "35BD105");
			//			sampleMap.put("AFFECTED_PROJECT", "X100");
			//			list.add(sampleMap);
			//			sampleMap = new HashMap<String, String>();
			//			sampleMap.put("ECO_NO", "35BD110");
			//			sampleMap.put("AFFECTED_PROJECT", "X100");
			//			list.add(sampleMap);
			//Test 를 위해 샘플 데이타 생성 끝
			
			//////////////////////////////////////////////////////////////////////////////////////////////
			/*
			 * 수정점 : 20200303 
			 * 수정 내용 : 누락및  오류가 있을경우 ECO_NO, AFFECTED_PROJECT 값만 넣고 재 생성시 사용
			 * 유의점 : 배포시 주석 처리 할것 
			 */
			
//			list = null;
//			if( list == null ) {
//				list = new ArrayList<HashMap<String, String>>();
//				HashMap<String, String> ecoMap = new HashMap<String, String>();
//				ecoMap.put("ECO_NO", "39BK027");
//				ecoMap.put("AFFECTED_PROJECT", "E100,C300");
//				list.add(ecoMap);
//			}
			//////////////////////////////////////////////////////////////////////////////////////////////
			
			for( HashMap<String, String> ecoMap : list){
				HashMap<String, String> eco = new HashMap<String, String>();
				HashMap<String, String> hasWeldPartEco = new HashMap<String,String>();
				String ecoNo = ecoMap.get("ECO_NO");
				eco.put("ECO_NO", ecoNo);

				String affectedProjects = null;
				boolean makeForcedPassFlag = false;
				// [20170103][taeku.jeong] S201 프로젝트의 경우 용접점 생성은 수행 하지 않고 이력은 "Pass"로 남도록 처리 하기위해
				// JOB_TRANS_LOG Table에는 Data를 등록한다.
				// S201 Project의 경우 ECO No가 "CMxxxxx"의 형식 인데 S201의 경우 용접접 생성을 수행 하지 않으므로 "Pass"로 처리 한다.
				if(ecoNo!=null && ecoNo.trim().toUpperCase().startsWith("CM")==true){
					makeForcedPassFlag = true;
					affectedProjects = null;
				}else{
					affectedProjects = ecoMap.get("AFFECTED_PROJECT");
				}

				addLog("=========================== 데몬 실행시 변환요청 항목(미변환 항목 아님) -시작- ==========================================");
				if( affectedProjects != null){
					String[] projectArray = affectedProjects.split(",");

					//Project code의 중복 방지
					for( String project : projectArray){
						project = project.trim();
						if( productMap.containsKey(project)){
							productStr = productMap.get(project);
						}else{

							if( project.equals("")) continue;

							tmpMap.clear();
							tmpMap.put("project_code", project);
							// Project Code에 해당하는 Product Id String을 가져온다.
							// ex) PVT2009
							productStr = (String)commonDao.selectOne("com.symc.weld.getProduct", tmpMap);
							if( productStr != null){
								productMap.put(project, productStr);
							}else{
								continue;
							}
						}
					}
					
					

					Collection<String> keys = productMap.keySet();
					Iterator<String> its = keys.iterator();
					addLog("PROJECT CODE         : " + affectedProjects );
					while(its.hasNext()){
						String projectCode = its.next();
						productStr = productMap.get(projectCode);
						//4. Product를 찾은 후 Product 아이디를 이용하여 용접점관리 FMP(latest Revision) 아이디를 가져온다.
						String targetFmpID = "";
						String targetFmpRevID = "";
						// Function Master Id Rule에 따라 Function Master Id를 조합한다.
						String weldFunctionMaster = "M605" +  productStr.substring(2) + "A";
						targetFmpID = weldFunctionMaster;
						if( currentWeldFmpRevMap.containsKey(weldFunctionMaster)){
							targetFmpRevID = currentWeldFmpRevMap.get(weldFunctionMaster);
						}else{
							tmpMap = getCurrentWeldFmpRevision(weldFunctionMaster);
							if( tmpMap == null ){
								continue;
							}
							targetFmpRevID = (String)tmpMap.get("ITEM_REV_ID");
							currentWeldFmpRevMap.put(weldFunctionMaster, targetFmpRevID);
						}

						// [20151016][ymjang] 해당 프로젝트에 적용될 대상 PART 만 찾아서 용접점을 생성해야 함. 
						eco.put("PROJECT_CODE", projectCode);
						List<HashMap<String,Object>> eplList = (List<HashMap<String,Object>>)commonDao.selectList("com.symc.weld.getEcoEplInfo", eco);
						for( int i = 0; eplList != null && i < eplList.size(); i++){
							setWeldGroupMapProject((HashMap<String,Object>)eplList.get(i), targetList, targetFmpID, projectCode);
						}
						if( targetList.size() > 0 ) {
							hasTransTarget = true ;
							countTranstarget = targetList.size();
						}
					}
					addLog("항목 개수         : " + countTranstarget ); // 중복 카운트 있음 targetList로 다시 카운트 수정 필요
					addLog("=========================== 데몬 실행시 변환요청 항목(미변환 항목 아님) -끝- ==========================================");
					
				}else{
					if(makeForcedPassFlag==false){
						continue;
					}
				} // End Of if( affectedProjects != null){

				// [20160115][ymjang] 각 ECO 별 처리 로그 생성 //변화 실패한 ECO 일 경우 해당 ECO 를 찾아 update 
				if( list.size() > 0 && !ecoNo.equals("")) {
				insertJobTransLog(log, ecoNo, "S", "Start");
				}

			} // End of For()
			
			/*********************************************************************************************************************************************/
			/*
			 *  용접점 미 변환 Part 재 요청을 위한 로직 변경
			 * 
			 */
			
			// 0. Weld_Point_Trans_Log 테이블에서 Trans_Flag 가 'I' 인 것들만 조회
			
			// 1. Part Revision 과 최신 결재 리비전이 같지 않는 경우 
			// 	  최신 결재 리비전으로 재 검색 하여 용접점 변환 유무를 재 확인
			
			// 2. 최신 결재 리비전이 용접점 변환을 완료 하지 못했다면 최신 결재 리비전으로 변환 요청(targetList 에 담아 변환 요청)
			//    구 리비전의 TRANS_FLAG 와 TRANS_MSG 의 값을 'S', 'Success' 로 업데이트 시킴
			
			// 3. 최신 결재 리비전과 Part 의 리비전이 같다면 targetList 에 담아 변환 요청
			
//			TcLoginService tcLoginService = new TcLoginService();
//			Session session = null;
			try {
				// 1. TC Session 생성
			
				
				
			HashMap<String, String> notTransformPartListProjectCode = new HashMap<String, String>();
			notTransformPartListProjectCode.put("PROJECT_CODE", SM_PROJECT_CODE);
			List<HashMap<String,Object>> notTransformPartList = (List<HashMap<String,Object>>)commonDao.selectList("com.symc.weld.notTransformPartList", notTransformPartListProjectCode);
			
			// 미변환 항목중 Flag는 'I'이지만 변환이 완료된 항목
			List<HashMap<String,Object>> flagIButTransComplete = new ArrayList<HashMap<String,Object>>();
			
			if( notTransformPartList.size() > 0 ) {
				notTransformPartList_boolean = true;
				addLog("=========================== 미변환 항목 조회 Flag 'I' 인 경우 -시작- ==========================================");
				for( HashMap<String, Object> tempHash : notTransformPartList) {
					ItemRevision weldGroupRevision = null;
					if (((String)tempHash.get("PROJECT_CODE")).startsWith("X")) {
						 weldGroupRevision = getItemRevision(session.getConnection(), tcItemUtil, (String)tempHash.get("NEW_PART_NO") + "-WeldGroup", (String)tempHash.get("NEW_PART_REV"));
					}  else {
						 weldGroupRevision = getItemRevision(session.getConnection(), tcItemUtil, (String)tempHash.get("PARENT_NO") + "-WeldGroup", (String)tempHash.get("PARENT_LATEST_REV"));
						
					}
					addLog("PROJECT CODE         : " + tempHash.get("PROJECT_CODE"));
					addLog("FMP ID               : " + tempHash.get("FMP_ID"));
					addLog("PARENT NO            : " + tempHash.get("PARENT_NO"));
					addLog("PARENT REVIION       : " + tempHash.get("PARENT_REV"));
					addLog("OLD PART NO          : " + tempHash.get("OLD_PART_NO"));
					addLog("OLD PART REVISION    : " + tempHash.get("OLD_PART_REV"));
					addLog("NEW PART NO          : " + tempHash.get("NEW_PART_NO"));
					addLog("NEW PART REVISION    : " + tempHash.get("NEW_PART_REV"));
					addLog("ECO NO               : " + tempHash.get("ECO_NO"));
					addLog("******************************************************************************************************************");
					if( weldGroupRevision == null ) {
						// 없으면 생성 
						
						if (((String)tempHash.get("PROJECT_CODE")).startsWith("X"))
							// X100 차종의 경우,
							addXWeldPointGroupInfo(tempHash, targetList, ((String)tempHash.get("FMP_ID")), ((String)tempHash.get("PROJECT_CODE")));
						else
							// 그 외 차종의 경우,
							addWeldPointGroupInfo(tempHash, targetList, ((String)tempHash.get("FMP_ID")), ((String)tempHash.get("PROJECT_CODE")));
						
					} else {
						TcDataManagementService dmService = new TcDataManagementService(session);
						dmService.getProperties(new ModelObject[]{weldGroupRevision}, new String[]{"view"} );
						ModelObject[] weldGroupRevisionView = weldGroupRevision.get_view();
						
						if( weldGroupRevisionView.length < 1 ) {
							ItemRevision hasCatPartRevsion =   null;
							if (((String)tempHash.get("PROJECT_CODE")).startsWith("X")) {
								// X100 차종의 경우,
								
								hasCatPartRevsion = getItemRevision(session.getConnection(), tcItemUtil, (String)tempHash.get("NEW_PART_NO") , (String)tempHash.get("NEW_PART_REV"));
								ModelObject[] hasDataset = getRelatedDataset(hasCatPartRevsion, session);
								
								ModelObject[] catpartDataset = null;
								if (hasDataset != null) {
									for (int i = 0; i < hasDataset.length; i++) {
										if (IFConstants.TYPE_DATASET_CATPART.equals(hasDataset[i].getTypeObject().getName())) {
											catpartDataset = new ModelObject[] { hasDataset[i] };
											break;
										}
									}
								} 
								
								if( catpartDataset == null) {
									 updateWeldPointTransLog(tempHash, "E", "Do not have CatPart");
									continue;
								} else {
									addXWeldPointGroupInfo(tempHash, targetList, ((String)tempHash.get("FMP_ID")), ((String)tempHash.get("PROJECT_CODE")));
								}
								
								
							}else {
								// 그 외 차종의 경우,
								
								addWeldPointGroupInfo(tempHash, targetList, ((String)tempHash.get("FMP_ID")), ((String)tempHash.get("PROJECT_CODE")));
							}
						}  else {
							
							// 이미 용접점이 생성된 Part 이므로 Flag를 바꿔서 다음 데몬 실행시에는 검색되지 않게 함
							 updateWeldPointTransLog(tempHash, "S", "Success"); // 배포시 주석 풀것
							// 
							flagIButTransComplete.add(tempHash);
				
						}
						
					}
				}
				addLog("항목 개수 			 : " + notTransformPartList.size());
				addLog("=========================== 미변환 항목 조회 Flag 'I' 인 경우 -끝- ==========================================");
				if( flagIButTransComplete.size() >0 ) {
					addLog("=========================== 로그 상태 Flag 는 'I' 이며 이미 변환 완료 된 Part 항목 -시작- ==========================================");
					for (HashMap<String,Object> transCompleteMap : flagIButTransComplete) {
						addLog("PROJECT CODE         : " + (String)transCompleteMap.get("PROJECT_CODE"));
						addLog("FMP ID               : " + (String)transCompleteMap.get("FMP_ID"));
						addLog("PARENT NO            : " + (String)transCompleteMap.get("PARENT_NO"));
						addLog("PARENT REVIION       : " + (String)transCompleteMap.get("PARENT_REV"));
						addLog("OLD PART NO          : " + (String)transCompleteMap.get("OLD_PART_NO"));
						addLog("OLD PART REVISION    : " + (String)transCompleteMap.get("OLD_PART_REV"));
						addLog("NEW PART NO          : " + (String)transCompleteMap.get("NEW_PART_NO"));
						addLog("NEW PART REVISION    : " + (String)transCompleteMap.get("NEW_PART_REV"));
						addLog("ECO NO               : " + (String)transCompleteMap.get("ECO_NO"));
						addLog("******************************************************************************************************************");
					}
					addLog("항목 개수 			 : " + flagIButTransComplete.size());
					addLog("=========================== 로그 상태 Flag 는 'I' 이며 이미 변환 완료 된 Part 항목 -끝- ==========================================");
				}
			}
			
			}catch(Exception e) {
				addLog("=========================== ERROR 발생 ==========================================");
				
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				PrintStream pinrtStream = new PrintStream(out);
				e.printStackTrace(pinrtStream);
				addLog(out.toString());
				addLog("=========================== ERROR 발생 ==========================================");
			}  
			
			/*********************************************************************************************************************************************/

			if( !targetList.isEmpty()){
				sendToExportWeldPointModule(targetList);
			}
			
			if( tmpMap == null ) {
				tmpMap = new HashMap<String, String> ();
			}
			tmpMap.clear();
			tmpMap.put("CURRENT_TIME", curTime);
			commonDao.insert("com.symc.weld.insertWeldPointServiceTime", tmpMap);

			// [20160115][ymjang] 각 ECO 별 처리 로그 갱신
			updateJobTransLog(log, list, targetList);
			
			addLog("=========================== End Weld Point Service SYMC Web ==========================================");

		}catch(Exception e){
			weldPointFlagFail = true;
			addLog("=========================== ERROR 발생 ==========================================");
			
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			PrintStream pinrtStream = new PrintStream(out);
			e.printStackTrace(pinrtStream);
			addLog(out.toString());
			addLog("=========================== ERROR 발생 ==========================================");
			throw e;
		}  finally {
			//로그 파일 남기기
			
			if( weldPointFlagFail ) {
				printLog("D:/IF_FOLDER/WELDPOINT_LOG", "ERROR_" + "변환요청", buffer.toString());
			} else {
				printLog("D:/IF_FOLDER/WELDPOINT_LOG", "변환요청", buffer.toString());
			}
		}


		return log.toString();
	}

	private String getNextRevisionID(String itemRevID){

		String revisionStr = "000";
		try{
			int revNumber = Integer.parseInt(itemRevID);
			revisionStr = String.format("%03d", revNumber + 1);
		}catch( NumberFormatException nfe){
			revisionStr = "000";
		}
		return revisionStr;
	}

	private String getNextRevisionID(String itemID, String itemRevID, HashMap<String, ArrayList<String>> revisionToReviseMap){

		if( revisionToReviseMap.containsKey(itemID)){
			ArrayList<String> list = revisionToReviseMap.get(itemID);
			String maxRevision = list.get(list.size() - 1);
			return getNextRevisionID( maxRevision );
		}else{
			String newRevID = getNextRevisionID( itemRevID );
			ArrayList<String> revList = new ArrayList<String>();
			revList.add(newRevID);
			revisionToReviseMap.put(itemID, revList);
			return newRevID;
		}
	}

	public static String clobToString(Clob clob) throws SQLException,
	IOException {
		if (clob == null) {
			return "";
		}
		StringBuffer strOut = new StringBuffer();
		String str = "";
		BufferedReader br = new BufferedReader(clob.getCharacterStream());
		while ((str = br.readLine()) != null) {
			strOut.append(str);
		}
		return strOut.toString();
	}


	private void sendToExportWeldPointModule(ArrayList<WeldPointGroupVO> targetList){

//		TcLoginService tcLoginService = new TcLoginService();
//		Session session = null;
		try {
			// 1. TC Session 생성
//			session = tcLoginService.getTcSession();
			SessionService.getService(session.getConnection()).refreshPOMCachePerRequest(true);
			TcItemUtil tcItemUtil = new TcItemUtil(session);
			
			TcDataManagementService dmService = new TcDataManagementService(session);
			HashMap<ItemRevision, ArrayList<ItemRevision>> targetToReleaseMap = new HashMap<ItemRevision, ArrayList<ItemRevision>>();
			HashMap<String, ItemRevision> ecoRevisionMap = new HashMap<String, ItemRevision>();
			HashMap<String, HashMap<String, Object>> baseFmpRevisionMap = new HashMap<String, HashMap<String, Object>>();
			HashMap<String, ItemRevision> functionRevisionMap = new HashMap<String, ItemRevision>();
			ItemRevision ecoRevision = null;
			ItemRevision baseFmpRevision = null;
			ItemRevision functionRevision = null;

			for(int i = 0; i < targetList.size(); i++ ){

				WeldPointGroupVO vo = targetList.get(i);
				try{

					if( ecoRevisionMap.containsKey(vo.getEcoNo())){
						ecoRevision = ecoRevisionMap.get(vo.getEcoNo());
					}else{
						ecoRevision = getItemRevision(session.getConnection(), tcItemUtil, vo.getEcoNo(), "000");
						dmService.getProperties(new ModelObject[]{ecoRevision}, new String[]{"date_released"});
						ecoRevisionMap.put(vo.getEcoNo(), ecoRevision);
					}

					//M605****A/000 FMP Revision이 릴리즈 되어 있지 않다면 릴리즈함.
					//            		baseFmpRevision = baseFmpRevisionMap.get(vo.getFmpId());
					if( !baseFmpRevisionMap.containsKey(vo.getFmpId()) ){
						baseFmpRevision = tcItemUtil.getRevisionInfo(vo.getFmpId(), "000");
						if( baseFmpRevision == null){
							continue;
						}

						HashMap<String, Object> map = new HashMap<String, Object>();
						map.put("ECO", ecoRevision);
						map.put("PROJECT_CODE", vo.getProjectCode());
						map.put("BASE_FMP", baseFmpRevision);
						baseFmpRevisionMap.put(vo.getFmpId(), map);

					}

					// [20151016][ymjang] X100 을 제외한 다른 차종들은 'W' Part 의 부모 Part 로 용접그룹을 생성함.
					// [20151016][ymjang] 용접점 그룹 아이템 생성시 용접 대상 Part 의 명칭을 부여한다.
					String weldGroupID = null;
					String weldGroupName = null;
					if (vo.getProjectCode().startsWith("X"))
					{
						weldGroupID = vo.getItemId() + "-WeldGroup";	
						weldGroupName = "WELD " + tcItemUtil.getItem(vo.getItemId()).get_object_name();
					} else
					{
						weldGroupID = vo.getParentId() + "-WeldGroup";	
						weldGroupName = "WELD " + tcItemUtil.getItem(vo.getParentId()).get_object_name();
					}
					ItemRevision fmpRevision = getFmpRevision(session.getConnection(), tcItemUtil, dmService, targetToReleaseMap, vo, ecoRevision);

					ItemRevision weldGroupRevision = getWeldGroupRevision(session, tcItemUtil, dmService, fmpRevision, ecoRevision, weldGroupID, weldGroupName, vo, targetToReleaseMap);

					String functionId = vo.getFmpId();
					functionId = "F" + functionId.substring(1, functionId.length() - 1);

					if( functionRevisionMap.containsKey(functionId)){
						functionRevision = functionRevisionMap.get(functionId);
					}else{
						functionRevision = getItemRevision(session.getConnection(), tcItemUtil, functionId, "000");
						functionRevisionMap.put(functionId, functionRevision);
					}

					ArrayList<HashMap<String, Object>> children = getChildren(fmpRevision.get_item_id(), fmpRevision.get_item_revision_id(), weldGroupID, null, null);
					if( vo.getChangeType().equals("D")){
						// 용접점 그룹이 삭제되는 경우는 거의 없으며,
						// Dispatcher Server에서 해당 용접점 그룹을 제거함.
					}else{
						if( children == null || children.isEmpty()){
							addWeldPointGroup(session.getConnection(), dmService, fmpRevision, new ItemRevision[]{weldGroupRevision}, false);
							//Latest Working Rule을 적용하여 Condition 적용함.
							setMvlCondition(session, dmService, functionRevision, weldGroupRevision, vo.getEplId());
						}
					}

				}catch(Exception e){
					addLog("=========================== sendToExportWeldPointModule 에서 요청전 에러 발생 ==========================================");
					
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					PrintStream pinrtStream = new PrintStream(out);
					e.printStackTrace(pinrtStream);
					addLog(out.toString());
					addLog("=========================== sendToExportWeldPointModule 에서 요청전 에러 발생 ==========================================");
				}
			}

			Set<String> set = baseFmpRevisionMap.keySet();
			Iterator<String> its = set.iterator();
			while( its.hasNext() ){
				HashMap<String, Object> map =  baseFmpRevisionMap.get(its.next());

				try{
					removeReleaseStatus(session, new ModelObject[]{(ItemRevision)map.get("BASE_FMP")});
					System.out.println("FMP/000의 PSR Release 제거완료.");
				}catch(Exception e){
					System.out.println("FMP/000의 PSR Release 제거실패!");
					System.out.println("이건의 오류에 대해서는 무시하고 진행함.");
					e.printStackTrace();
				}

				if(!isReleased(dmService, (ItemRevision)map.get("BASE_FMP"))){
					baseFmpRelease(session.getConnection(), tcItemUtil, dmService, (ItemRevision)map.get("ECO"), (ItemRevision)map.get("BASE_FMP"), (String)map.get("PROJECT_CODE"));
					refresh(session.getConnection(), (ItemRevision)map.get("BASE_FMP"), 0);
				}

			}
			release(session.getConnection(), tcItemUtil, dmService, ecoRevisionMap, targetToReleaseMap);

			// [용점접 2차 개선안] [20150907][ymjang] 용접점 처리시 각 대상 Part 별로 상태를 기록하기 위해 상태 테이블에 데이터를 생성한다.
				TcCommonDao commonDao = TcCommonDao.getTcCommonDao();
				HashMap<String, String> tmpMap = new HashMap<String, String>();
				for(WeldPointGroupVO vo : targetList) 
				{
					// [용접점 누락 수정] 추가 적인 미변환 Part 들이 아닌 것들만 Log에 기록하기 위해 추가
					if( !vo.isNotTransformFlag() ) {
						try {
							tmpMap.clear();
							tmpMap.put("ECO_NO", vo.getEcoNo());
							tmpMap.put("FMP_ID", vo.getFmpId());
							tmpMap.put("PROJECT_CODE", vo.getProjectCode());
							tmpMap.put("CHANGE_TYPE", vo.getChangeType());
							tmpMap.put("EPL_ID", vo.getEplId());
							tmpMap.put("PARENT_NO", vo.getParentId());
							tmpMap.put("PARENT_REVISION_ID", vo.getParentRevId());
							tmpMap.put("PART_NO", vo.getItemId());
							tmpMap.put("PART_REVISION_ID", vo.getItemRevId());
							// 이미 변환 실패 한 리스트는 DB에 있기 때문에 입력할 필요가 없을것 같음 (추가)
							commonDao.insert("com.symc.weld.insertWeldPointTransLog", tmpMap);
						} catch(Exception e) {
							ByteArrayOutputStream out = new ByteArrayOutputStream();
					        PrintStream pinrtStream = new PrintStream(out);
					        e.printStackTrace(pinrtStream);
							addLog("=========================== 변환 용접점 리스트 로그 입력시 에러(미변환 아님) -시작- ==========================================");
							addLog("ECO_NO            : " + vo.getEcoNo());
							addLog("FMP_ID            : " + vo.getFmpId());
							addLog("PROJECT_CODE      : " + vo.getProjectCode());
							addLog("CHANGE_TYPE       : " + vo.getChangeType());
							addLog("EPL_ID            : " + vo.getEplId());
							addLog("PARENT_NO         : " + vo.getParentId());
							addLog("PARENT_REVISION_ID: " + vo.getParentRevId());
							addLog("PART_NO           : " + vo.getItemId());
							addLog("PART_REVISION_ID  : " + vo.getItemRevId());
							addLog("변환 용접점 리스트 로그 입력시 에러 	: " + out.toString());
							addLog("=========================== 변환 용접점 리스트 로그 입력시 에러(미변환 아님) -끝- ==========================================");
							throw e;
						}
					}
				}

				
			HashMap<String,Object> targetPartMap = new HashMap<String,Object>();
			// 미변환 항목중 CatPart Dataset이 없는 항목 리스트
		    ArrayList<WeldPointGroupVO> notHaveCatPartDatasetList = new  ArrayList<WeldPointGroupVO>();
		    ArrayList<WeldPointGroupVO> transFailList = new  ArrayList<WeldPointGroupVO>();
		    ArrayList<WeldPointGroupVO> duplicatePartList = new  ArrayList<WeldPointGroupVO>();
			addLog("=========================== 미변환 용접점 변환 요청 성공 항목 -시작- ==========================================");
			for(WeldPointGroupVO vo : targetList){
				try{
					// [20151215][ymjang] Dispatcher Duplicate 오류 방지를 위한 Filtering
					if (!targetPartMap.containsKey(vo.getItemId()+vo.getItemRevId()))
					{
						targetPartMap.put(vo.getItemId()+vo.getItemRevId(), vo);
						createDispatcherRequest(vo, session, notHaveCatPartDatasetList, transFailList) ;
						
					} else {
						duplicatePartList.add(vo);
					}
				}catch(Exception e){
					addLog("=========================== 용접점 변환 요청 에러 발생 시작==========================================");
					addLog("ECO_NO              : " + vo.getEcoNo());
					addLog("FMP_ID              : " + vo.getFmpId());
					addLog("PROJECT_CODE        : " + vo.getProjectCode());
					addLog("CHANGE_TYPE         : " + vo.getChangeType());
					addLog("EPL_ID              : " + vo.getEplId());
					addLog("PARENT_NO           : " + vo.getParentId());
					addLog("PARENT_REVISION_ID  : " + vo.getParentRevId());
					addLog("PART_NO             : " + vo.getItemId());
					addLog("PART_REVISION_ID    : " + vo.getItemRevId());
					addLog("=========================== 용접점 변환 요청 에러 발생 끝==========================================");
					throw e;
				}
			}
		
			addLog("=========================== 미변환 용접점 변환 요청 성공 항목 -끝- ==========================================");
			
			addLog("=========================== 미변환 항목중 CatPart Datset을 가지지 못한 항목리스트 ==========================================");
			for( WeldPointGroupVO notHaveCatPart : notHaveCatPartDatasetList) {
				addLog("PROJECT CODE         : " + notHaveCatPart.getProjectCode());
				addLog("FMP ID               : " + notHaveCatPart.getFmpId());
				addLog("PARENT NO            : " + notHaveCatPart.getParentId());
				addLog("PARENT REVIION       : " + notHaveCatPart.getParentRevId());
				addLog("PART NO              : " + notHaveCatPart.getItemId());
				addLog("PART REVISION        : " + notHaveCatPart.getItemRevId());
				addLog("ECO NO               : " + notHaveCatPart.getEcoNo());
				addLog("******************************************************************************************************************");
			}
			addLog("항목개수		     : " + notHaveCatPartDatasetList.size());
			addLog("=========================== 미변환 항목중 CatPart Datset을 가지지 못한 항목리스트 ==========================================");
			
			
			addLog("=========================== 미변환 항목중 변환요청 실패 항목  ==========================================");
			for( WeldPointGroupVO transFail : transFailList) {
				addLog("PROJECT CODE         : " + transFail.getProjectCode());
				addLog("FMP ID               : " + transFail.getFmpId());
				addLog("PARENT NO            : " + transFail.getParentId());
				addLog("PARENT REVIION       : " + transFail.getParentRevId());
				addLog("PART NO              : " + transFail.getItemId());
				addLog("PART REVISION        : " + transFail.getItemRevId());
				addLog("ECO NO               : " + transFail.getEcoNo());
				addLog("변환 요청 실패 로그  : " + transFail.getTransFailReason());
				addLog("******************************************************************************************************************");
			}
			addLog("항목개수		     : " + transFailList.size());
			
			addLog("=========================== 미변환 항목중 Part No 중복 리스트 ==========================================");
			for( WeldPointGroupVO duplicate : duplicatePartList) {
				addLog("PROJECT CODE         : " + duplicate.getProjectCode());
				addLog("FMP ID               : " + duplicate.getFmpId());
				addLog("PARENT NO            : " + duplicate.getParentId());
				addLog("PARENT REVIION       : " + duplicate.getParentRevId());
				addLog("PART NO              : " + duplicate.getItemId());
				addLog("PART REVISION        : " + duplicate.getItemRevId());
				addLog("ECO NO               : " + duplicate.getEcoNo());
				addLog("******************************************************************************************************************");
			}
			addLog("항목개수		     : " + duplicatePartList.size());
			addLog("=========================== 미변환 항목중 Part No 중복 리스트 ==========================================");

		}catch(Exception e){
			ByteArrayOutputStream out = new ByteArrayOutputStream();
	        PrintStream pinrtStream = new PrintStream(out);
	        e.printStackTrace(pinrtStream);
	        addLog("******************************************************************************************************************");
			addLog("sendToExportWeldPointModule  에러 발생    : " + out.toString());
			addLog("******************************************************************************************************************");
		}finally{
			if (session != null) {
				session.logout();
			}
		}
	}

	private void removeRelated(Connection connection, ModelObject source, ModelObject target, String relatedContextName) throws ServiceException, ICCTException{

		com.teamcenter.services.internal.loose.core.ICTService service
		= com.teamcenter.services.internal.loose.core.ICTService.getService(connection);

		Type type = source.getTypeObject();

		Arg[] args_ = new Arg[5];
		args_[0] = TcUtility.createArg(type.getName());
		args_[1] = TcUtility.createArg(type.getUid());
		args_[2] = TcUtility.createArgStringUnion(source.getUid());
		args_[3] = TcUtility.createArg(relatedContextName);
		args_[4] = TcUtility.createArg(new String[]{target.getUid()});
		InvokeICTMethodResponse response = service.invokeICTMethod("ICCT", "removeRelated", args_);
		if( response.serviceData.sizeOfPartialErrors() > 0)
		{
			throw new ICCTException( response.serviceData);
		}

		args_ = new Arg[4];
		args_[0] = TcUtility.createArg( type.getName() );
		args_[1] = TcUtility.createArg( type.getUid() );
		args_[2] = TcUtility.createArg( source.getUid() );
		args_[3] = TcUtility.createArg(0);
		response = service.invokeICTMethod("ICCT", "refresh", args_);
		if( response.serviceData.sizeOfPartialErrors() > 0)
		{
			throw new ICCTException( response.serviceData);
		}
	}

	private ModelObject[] whereReferencedInfo(Connection connection, ReleaseStatus rs) throws ICCTException, ServiceException{

		ModelObject[] result = null;

		com.teamcenter.services.internal.loose.core.ICTService service
		= com.teamcenter.services.internal.loose.core.ICTService.getService(connection);

		Type type = rs.getTypeObject();

		Arg[] args_ = new Arg[4];
		args_[0] = TcUtility.createArg(type.getName());
		args_[1] = TcUtility.createArg(type.getUid());
		args_[2] = TcUtility.createArg(rs.getUid());
		args_[3] = TcUtility.createArg(true);
		InvokeICTMethodResponse response = service.invokeICTMethod("ICCT", "whereReferencedInfo", args_);
		if( response.serviceData.sizeOfPartialErrors() > 0)
		{
			throw new ICCTException( response.serviceData);
		}

		int size = response.serviceData.sizeOfPlainObjects();
		if ( size > 0 ) {
			result = new ModelObject[size];
			for( int i = 0; i < size; i++){
				result[i] = response.serviceData.getPlainObject(i);
			}
		}

		return result;
	}

	private void removeReleaseStatus(Session session, ModelObject[] modelObject) throws Exception{

		DataManagementService dm = DataManagementService.getService(session.getConnection());
		//    	TcQueryService tcQueryService = new TcQueryService(session);
		//    	String queryName = TcConstants.SEARCH_ITEM_REVISION;
		//        String[] entries = new String[]{ "Type", "Item ID", "Revision"};
		//        String[] values = new String[]{ "ItemRevision", "TEST000002", "000"};
		//        String[] properties = new String[]{TcConstants.PROP_ITEM_ID, "item_revision_id"};
		//
		//		com.teamcenter.services.internal.loose.core.ICTService service
		//					= com.teamcenter.services.internal.loose.core.ICTService.getService(session.getConnection());
		//
		//        ModelObject[] modelObject = tcQueryService.searchTcObject(queryName, entries, values, properties);
		for( int i = 0; modelObject != null && i < modelObject.length; i++){

			WhereReferencedResponse whereResponse = dm.whereReferenced(modelObject, 1);
			ServiceData data = whereResponse.serviceData;
			for( int j = 0; j < data.sizeOfPlainObjects(); j++){
				if( data.getPlainObject(j) instanceof EPMTask){

					ItemRevision revision = (ItemRevision)modelObject[i];
					dm.getProperties(new ModelObject[]{revision}, new String[]{"release_status_list"});
					ReleaseStatus[] releaseStatus = revision.get_release_status_list();
					for(ReleaseStatus rs : releaseStatus){

						ModelObject[] rsReferenced = whereReferencedInfo(session.getConnection(), rs);
						if ( rsReferenced != null ){
							for(ModelObject ref : rsReferenced){
								if( ref instanceof EPMTask){
									EPMTask task = (EPMTask)ref;
									dm.getProperties(new ModelObject[]{task}, new String[]{"object_name"});
									String objectName = task.get_object_name();
									if( objectName.equalsIgnoreCase("PSR")){
										removeRelated(session.getConnection(), modelObject[i], rs, "release_status_list");
									}
								}
							}
						}
					}

					EPMTask task = (EPMTask)data.getPlainObject(j);
					dm.getProperties(new ModelObject[]{task}, new String[]{"object_name"});
					String objectName = task.get_object_name();
					if( objectName.equalsIgnoreCase("PSR")){
						removeRelated(session.getConnection(), task, modelObject[i], "target_attachments");
					}

				}

			}
		}
	}

	protected RevisionRule getRevisionRule(Connection connection, String revisionRuleName) throws Exception {
		String queryName = "General...";
		String[] entries = { "Type", "Name" };
		String[] values = { "RevisionRule", revisionRuleName };
		QueryInput[] queryInput = new QueryInput[1];
		queryInput[0] = new QueryInput();
		queryInput[0].clientId= "user";
		queryInput[0].query = getQueryObject(connection, queryName);
		queryInput[0].resultsType=2;
		queryInput[0].entries = entries;
		queryInput[0].values = values;

		SavedQueriesResponse executeSavedQueries = SavedQueryService.getService(connection).executeSavedQueries(queryInput);
		if(executeSavedQueries.serviceData.sizeOfPartialErrors() < 1)
		{
			QueryResults[] queryresults = executeSavedQueries.arrayOfResults ;
			String[] uids = queryresults[0].objectUIDS;
			if (uids == null || uids.length == 0) {
				throw new Exception("Could not find RevisionRule[" + revisionRuleName + "]");
			}
			return (RevisionRule)SoaHelper.getModelObject(uids)[0];
		}

		return null;
	}

	@SuppressWarnings("unchecked")
	protected void setMvlCondition(Session session,  TcDataManagementService dmService, ItemRevision functionRevision
			,ItemRevision weldGroupRevision, String eplId) throws Exception {

		// 1. ECO_BOM_LIST에서 Condition 가져오기
		//		Properties prop = getDefaultProperties("weldpointexport");
		//		String servletUrlStr = prop.getProperty("servlet.url");

		HashMap<String, String> param = new HashMap<String, String>();
		TcCommonDao commonDao = TcCommonDao.getTcCommonDao();
		param.put("EPL_ID", eplId);
		HashMap<String, Object> map = (HashMap<String, Object>)commonDao.selectOne("com.symc.weld.getEcoEplInfo", param);
		String conditionStr = clobToString((Clob)map.get("NEW_VC"));
		if( conditionStr != null){
			conditionStr = conditionStr.trim();
		}
		System.out.println("Condition : " + conditionStr);

		if( conditionStr != null && !conditionStr.equals("")){

			dmService.getProperties(new ModelObject[]{weldGroupRevision}, new String[]{"item_id"});
			String weldGroupID = weldGroupRevision.get_item_id();

			dmService.getProperties(new ModelObject[]{functionRevision}, new String[]{"item_id"});
			String functionId = functionRevision.get_item_id();

			BOMLineService bomlineService = new BOMLineService(session);

			//2.Function을 최상위로 하여 BOM Window생성.
			boolean bFound = false;
			BOMWindow window = null;
			try{

				window = bomlineService.getCreateBOMWindow(functionRevision, null, null);
				dmService.getProperties(new ModelObject[]{window}, new String[]{"top_line"});
				BOMLine functionLine = (BOMLine)window.get_top_line();
				dmService.getProperties(new ModelObject[]{functionLine}, new String[]{"bl_child_lines"});
				ModelObject[] fmpObjs = functionLine.get_bl_child_lines();

				for( ModelObject fmpObj : fmpObjs){

					BOMLine fmpLine = (BOMLine)fmpObj;
					dmService.getProperties(new ModelObject[]{fmpLine}, new String[]{"bl_child_lines"});
					ModelObject[] weldGroupObjs = fmpLine.get_bl_child_lines();

					for( ModelObject weldGroupObj : weldGroupObjs){
						BOMLine weldGroupLine = (BOMLine)weldGroupObj;
						dmService.getProperties(new ModelObject[]{weldGroupLine}, new String[]{"bl_item"});
						Item tmpItem = (Item)weldGroupLine.get_bl_item();
						dmService.getProperties(new ModelObject[]{tmpItem}, new String[]{"item_id"});
						String tmpItemID = tmpItem.get_item_id();
						if( weldGroupID.equals(tmpItemID)){
							dmService.getProperties(new ModelObject[]{fmpLine}, new String[]{"bl_revision"});
							ItemRevision fmpRevision = (ItemRevision)fmpLine.get_bl_revision();
							dmService.getProperties(new ModelObject[]{fmpRevision}, new String[]{"structure_revisions"});

							PSBOMViewRevision[] bomViewRevisions = fmpRevision.get_structure_revisions();
							ReservationService rService = ReservationService.getService(session.getConnection());
							try{
								rService.checkout(bomViewRevisions, "", null);
								setMvlCondition(session, functionId, conditionStr, window, weldGroupLine);
							}catch(Exception e){
								throw e;
							}finally{
								rService.checkin(bomViewRevisions);
								for( PSBOMViewRevision bomView : bomViewRevisions){
									save(session.getConnection(), bomView);
								}
							}

							bFound = true;
							break;
						}
					}

					if( bFound ){
						break;
					}
				}

				save(session.getConnection(), window);

			}catch(Exception e){
				ByteArrayOutputStream out = new ByteArrayOutputStream();
		        PrintStream pinrtStream = new PrintStream(out);
		        e.printStackTrace(pinrtStream);
		        addLog("******************************************************************************************************************");
				addLog("setMvlCondition  에러 발생    : " + out.toString());
				addLog("******************************************************************************************************************");
				throw e;
			}finally{
				if( window != null){
					com.teamcenter.services.strong.cad.StructureManagementService.getService(session.getConnection()).closeBOMWindows(new BOMWindow[]{window});
				}
			}

		}

	}

	/**
	 * FMP에서 Project Code 속성을 이용해 Vehicle Code를 찾아서 Return 하는 Function
	 * FMP에 Attach할 WeldGroup을 제한 하기위해 동일한 자종을 구분하기위한 방법으로 만든 Function
	 * [NONE_SR] [20151013] taeku.jeong 윤순식 차장님 요청 
	 * @param connection
	 * @param dmService
	 * @param sourceItemRevision
	 * @return
	 * @throws Exception 
	 */
	private String getVehicleNo(TcDataManagementService dmService, Session session,  ItemRevision sourceItemRevision) throws Exception{

		String vehicleNo = null;
		String projectCode = null;

		if(sourceItemRevision!=null){
			dmService.getProperties(new ModelObject[]{sourceItemRevision}, new String[]{"s7_PROJECT_CODE"});

			//String projectCode = (String)fmpRevision.get_s7_project_code();
			try {
				projectCode = sourceItemRevision.getPropertyObject("s7_PROJECT_CODE").getStringValue();
			} catch (Exception e) {
				ByteArrayOutputStream out = new ByteArrayOutputStream();
		        PrintStream pinrtStream = new PrintStream(out);
		        e.printStackTrace(pinrtStream);
				addLog("******************************************************************************************************************");
				addLog("s7_PROJECT_CODE 속성 로딩시 에러    : " + out.toString());
				addLog("******************************************************************************************************************");
				
			}
		}

		if(projectCode==null || (projectCode!=null && projectCode.trim().length()<1)){
			return vehicleNo;
		}

		TcItemUtil tcItemUtil = null;
		if(session!=null){
			tcItemUtil = new TcItemUtil(session);
		}

		if(tcItemUtil==null){
			return vehicleNo;
		}

		try {
			ItemRevision projectItemRevision = null;
			if(tcItemUtil!=null && projectCode!=null){
				projectItemRevision = getItemRevision(session.getConnection(), tcItemUtil, projectCode, "000");
			}

			if(projectItemRevision!=null){
				dmService.getProperties(new ModelObject[]{projectItemRevision}, new String[]{"S7_VEHICLE_NO"});
				//vehicleNo = projectRevision.get_s7_vehicle_no();
				vehicleNo = sourceItemRevision.getPropertyObject("S7_VEHICLE_NO").getStringValue();
			}
		} catch (Exception e) {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
	        PrintStream pinrtStream = new PrintStream(out);
	        e.printStackTrace(pinrtStream);
			addLog("******************************************************************************************************************");
			addLog("S7_VEHICLE_NO 속성 로딩시 에러    : " + out.toString());
			addLog("******************************************************************************************************************");
			

		}

		return vehicleNo;
	}

	/**
	 * WeldGroup Item Revision 배열에 있는 WeldGroup Item Revision중에 Vehicle No가 동일한 FMP와 WeldGroup ItemRevision만 Attach 되도록하기위해
	 * 조건에 맞는 WeldGroup Item Revision만 Attach대상으로 List 해서 Return한다. 
	 * [NONE_SR] [20151013] taeku.jeong 윤순식 차장님 요청 
	 * @param dmService
	 * @param fmpRevision
	 * @param weldGroupRevisions
	 * @return
	 * @throws Exception 
	 */
	private ItemRevision[] getAttachableWeldGroupRevisions(TcDataManagementService dmService, ItemRevision fmpRevision, ItemRevision[] weldGroupRevisions) throws Exception{


		ItemRevision[] attachableRevision = null;

		ArrayList <ItemRevision> attachableRevisionList = null;

		TcLoginService tcLoginService = new TcLoginService();
		Session session = null;
		try {
			session = tcLoginService.getTcSession();
		} catch (Exception e) {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
	        PrintStream pinrtStream = new PrintStream(out);
	        e.printStackTrace(pinrtStream);
			addLog("******************************************************************************************************************");
			addLog("Session 연결시도 중 에러 발생    : " + out.toString());
			addLog("******************************************************************************************************************");
			
			throw e;
		}

		String fmpRevisionVehicleNo = getVehicleNo(dmService, session, fmpRevision);

		if(fmpRevisionVehicleNo==null || (fmpRevisionVehicleNo!=null && fmpRevisionVehicleNo.trim().length()<1)){
			return attachableRevision;
		}

		for (int i = 0; weldGroupRevisions!=null && i < weldGroupRevisions.length; i++) {
			ItemRevision weldGroupRevision = weldGroupRevisions[i];

			String weldGroupRevisionVehicleNo = getVehicleNo(dmService, session, weldGroupRevision);

			if(weldGroupRevisionVehicleNo!=null && weldGroupRevisionVehicleNo.trim().equalsIgnoreCase(fmpRevisionVehicleNo)){
				if(attachableRevisionList==null){
					attachableRevisionList = new ArrayList<ItemRevision>();
				}

				attachableRevisionList.add(weldGroupRevision);
			}
		}

		if(attachableRevisionList!=null && attachableRevisionList.size()>0){

			attachableRevision = new ItemRevision[attachableRevisionList.size()];

			for (int i = 0; i < attachableRevisionList.size(); i++) {
				attachableRevision[i] = attachableRevisionList.get(i);
			}
		}

		return attachableRevision;
	}

	protected Object addWeldPointGroup(Connection connection, TcDataManagementService dmService, ItemRevision fmpRevision
			, ItemRevision[] weldGroupRevisions, boolean isComplete) throws Exception {

		ServiceData serviceData = dmService.getProperties(new ModelObject[]{fmpRevision}, new String[]{"structure_revisions"});

		ReservationService rService = ReservationService.getService(connection);
		//		ReservationRestBindingStub rServiceStub = (ReservationRestBindingStub) rService;
		PSBOMViewRevision[] bomViewRevision = null;
		if( serviceData.sizeOfPartialErrors() < 1){
			bomViewRevision = fmpRevision.get_structure_revisions();
			if (bomViewRevision != null && bomViewRevision.length > 0) {
				rService.checkout(bomViewRevision, "", null);
			}
		}
		CreateOrUpdateRelativeStructureInfo2 createOrUpdateRelativeStructureInfo2 = new CreateOrUpdateRelativeStructureInfo2();
		createOrUpdateRelativeStructureInfo2.parent = fmpRevision;
		createOrUpdateRelativeStructureInfo2.precise = false;


		createOrUpdateRelativeStructureInfo2.childInfo = new RelativeStructureChildInfo[weldGroupRevisions.length];
		for( int i = 0; i < weldGroupRevisions.length ; i++){
			createOrUpdateRelativeStructureInfo2.childInfo[i] = new RelativeStructureChildInfo();
			createOrUpdateRelativeStructureInfo2.childInfo[i].child = weldGroupRevisions[i];
		}

		// [NONE_SR] [20151013] taeku.jeong 윤순식 차장님 요청 
		// attachAbleWeldGroupRevisions 의 Size가 0 이거나 null 인 경우 null을 Return하고 Function을 종료하는 것도 방법으로 보인다.
		/*
		ItemRevision[] attachAbleWeldGroupRevisions = getAttachableWeldGroupRevisions(dmService, fmpRevision, weldGroupRevisions);
		createOrUpdateRelativeStructureInfo2.childInfo = new RelativeStructureChildInfo[attachAbleWeldGroupRevisions.length];
		for( int i = 0;attachAbleWeldGroupRevisions!=null &&  i < attachAbleWeldGroupRevisions.length ; i++){
			createOrUpdateRelativeStructureInfo2.childInfo[i] = new RelativeStructureChildInfo();
			createOrUpdateRelativeStructureInfo2.childInfo[i].child = attachAbleWeldGroupRevisions[i];
		}
		 */

		CreateOrUpdateRelativeStructurePref2 pref2 = new CreateOrUpdateRelativeStructurePref2();

		StructureManagementRestBindingStub smrms = new StructureManagementRestBindingStub(connection);
		try{
			CreateOrUpdateRelativeStructureResponse response = smrms
					.createOrUpdateRelativeStructure(
							new CreateOrUpdateRelativeStructureInfo2[] { createOrUpdateRelativeStructureInfo2 },
							"view", isComplete, pref2);

			if( response.serviceData.sizeOfCreatedObjects() > 0){
				String occPuid = response.serviceData.getCreatedObject(0).getUid();
				return occPuid;
			}

		}catch(Exception e){
			ByteArrayOutputStream out = new ByteArrayOutputStream();
	        PrintStream pinrtStream = new PrintStream(out);
	        e.printStackTrace(pinrtStream);
	        addLog("******************************************************************************************************************");
			addLog("getCorpOptionTest  에러 발생    : " + out.toString());
			addLog("******************************************************************************************************************");
			e.printStackTrace();
		}finally{
			if (bomViewRevision != null && bomViewRevision.length > 0) {
				rService.checkin(bomViewRevision);
			}
		}

		return null;
	}

	@SuppressWarnings("unchecked")
	private  ArrayList<HashMap<String, Object>> getChildren(String parentID, String parentRevID, String childID, String childRevID, String eplID) throws Exception{

		String condition = null;
		TcCommonDao commonDao = TcCommonDao.getTcCommonDao();
		HashMap<String, String> ds = new HashMap<String, String>();
		if( eplID != null){
			ds.put("EPL_ID", eplID);
			HashMap<String, Object> map =  (HashMap<String, Object>)commonDao.selectOne("com.symc.weld.getEcoEplInfo", ds);
			condition = (String)map.get("NEW_VC");
		}
		ds.clear();
		ds.put("parent_id", parentID);
		ds.put("parent_rev_id", parentRevID);
		ds.put("child_id", childID);
		ds.put("child_rev_id", childRevID);
		ds.put("condition", (condition == null ? null:condition.trim()));
		ArrayList<HashMap<String, Object>> list = (ArrayList<HashMap<String, Object>>)commonDao.selectList("com.symc.weld.getChildren" ,ds);

		return list;
	}

	private ItemRevision getWeldGroupRevision(Session tcSession, TcItemUtil tcItemUtil, TcDataManagementService dmService, ItemRevision fmpRevision
			, ItemRevision ecoRevision, String weldGroupID, String weldGroupName, WeldPointGroupVO vo, HashMap<ItemRevision, ArrayList<ItemRevision>> targetToReleaseMap) throws Exception{

		ItemRevision weldGroupRevision = null;
		//WeldGroup이 존재하는지 확인 후, 없으면 생성함.
		Item item = tcItemUtil.getItem(weldGroupID);
		if( item == null){
			CreateItemsOutput[] output = tcItemUtil.createItems(weldGroupID, weldGroupName, "000", null, "S7_Vehpart", null, null, tcSession);
			weldGroupRevision = output[0].itemRev;

			com.teamcenter.services.loose.core._2010_09.DataManagement.PropInfo propInfo
			= new com.teamcenter.services.loose.core._2010_09.DataManagement.PropInfo();
			propInfo.timestamp = Calendar.getInstance();
			propInfo.object = weldGroupRevision;
			com.teamcenter.services.loose.core._2010_09.DataManagement.NameValueStruct1[] nameValueStruct = new com.teamcenter.services.loose.core._2010_09.DataManagement.NameValueStruct1[2];
			nameValueStruct[0] = new  com.teamcenter.services.loose.core._2010_09.DataManagement.NameValueStruct1();
			nameValueStruct[0].name = "s7_MATURITY";
			nameValueStruct[0].values = new String[]{"In Work"};
			nameValueStruct[1] = new  com.teamcenter.services.loose.core._2010_09.DataManagement.NameValueStruct1();
			nameValueStruct[1].name = "s7_PROJECT_CODE";
			nameValueStruct[1].values = new String[]{vo.getProjectCode()};
			propInfo.vecNameVal = nameValueStruct;
			lock(tcSession.getConnection(), weldGroupRevision);
			//			DataManagementService.getService(tcSession.getConnection()).setProperties(new com.teamcenter.services.loose.core._2010_09.DataManagement.PropInfo[]{propInfo}, new String[]{"ENABLE_PSE_BULLETIN_BOARD"});
			DataManagementService.getService(tcSession.getConnection()).setProperties(new com.teamcenter.services.loose.core._2010_09.DataManagement.PropInfo[]{propInfo}, new String[]{""});
			save(tcSession.getConnection(), weldGroupRevision);
			unlock(tcSession.getConnection(), weldGroupRevision);
			//ECO No 저장
			setReferenceProperties(tcSession.getConnection(), weldGroupRevision, ecoRevision);

			//Release항목에 추가
			if( targetToReleaseMap.containsKey(ecoRevision) ){
				ArrayList<ItemRevision> list = targetToReleaseMap.get(ecoRevision);
				list.add(weldGroupRevision);
			}else{
				ArrayList<ItemRevision> list = new ArrayList<ItemRevision>();
				list.add(weldGroupRevision);
				targetToReleaseMap.put(ecoRevision, list);
			}

		}else{

			dmService.getProperties(new ModelObject[]{item}, new String[]{"revision_list"} );
			ModelObject[] weldGroupRevisionList = item.get_revision_list();
			weldGroupRevision = (ItemRevision)weldGroupRevisionList[weldGroupRevisionList.length-1];

		}

		return weldGroupRevision;
	}

	private ImanQuery getQueryObject(Connection connection, String queryName) throws Exception {

		try {
			GetSavedQueriesResponse savedQueries = SavedQueryService.getService(connection).getSavedQueries();
			for (int i = 0; i < savedQueries.queries.length; i++) {

				if (savedQueries.queries[i].name.equals(queryName)){
					return savedQueries.queries[i].query;
				}
			}
		} catch (ServiceException e) {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
	        PrintStream pinrtStream = new PrintStream(out);
	        e.printStackTrace(pinrtStream);
			addLog("******************************************************************************************************************");
			addLog("Saved Query 로딩중 에러 발생    : " + out.toString());
			addLog("******************************************************************************************************************");
			
			throw e;
		}
		return null;
	}

	private ItemRevision getItemRevision(Connection connection, TcItemUtil tcItemUtil, String itemID, String itemRevisionID) throws Exception {
		String queryName = "Item Revision...";
		String[] entries = { "Item ID", "Revision" };
		String[] values = { itemID, itemRevisionID };
		QueryInput[] queryInput = new QueryInput[1];
		queryInput[0] = new QueryInput();
		queryInput[0].clientId= "site";
		queryInput[0].query = getQueryObject(connection, queryName);
		queryInput[0].resultsType=2;
		queryInput[0].entries = entries;
		queryInput[0].values = values;

		SavedQueriesResponse executeSavedQueries = SavedQueryService.getService(connection).executeSavedQueries(queryInput);
		if(executeSavedQueries.serviceData.sizeOfPartialErrors() < 1)
		{
			QueryResults[] queryresults = executeSavedQueries.arrayOfResults ;
			String[] uids = queryresults[0].objectUIDS;
			if (uids == null || uids.length == 0) {
				return null;
			}
			return tcItemUtil.getRevisionInfo(uids[0]);
		}

		return null;
	}


	/**
	 * baseFmpRevision이 릴리즈 되었는지 확인.
	 *
	 * @param connection
	 * @param baseFmpRevision
	 * @return
	 * @throws Exception
	 */
	private boolean isReleased(TcDataManagementService dmService, ItemRevision baseFmpRevision) throws Exception{
		dmService.refreshObjects(baseFmpRevision);
		dmService.getProperties(new ModelObject[]{baseFmpRevision}, new String[]{"release_status_list"});
		ReleaseStatus[] releaseStatus = baseFmpRevision.get_release_status_list();
		if( releaseStatus != null && releaseStatus.length > 0){
			return true;
		}

		return false;
	}

	private void baseFmpRelease(Connection connection, TcItemUtil tcItemUtil, TcDataManagementService dmService, ItemRevision ecoRevision, ItemRevision baseFmpRevision, String projectCode) throws Exception{

		DataManagementService dm = DataManagementService.getService(connection);
		TcCommonDao commonDao = TcCommonDao.getTcCommonDao();
		dmService.getProperties(new ModelObject[]{ecoRevision}, new String[]{"date_released"});
		Calendar ecoReleaseDate = ecoRevision.get_date_released();

		Calendar newReleaseDate = Calendar.getInstance();
		newReleaseDate.setTime(ecoReleaseDate.getTime());
		newReleaseDate.add(Calendar.DATE, -1);

		com.teamcenter.services.loose.core._2010_09.DataManagement.PropInfo propInfo
		= new com.teamcenter.services.loose.core._2010_09.DataManagement.PropInfo();
		propInfo.timestamp = Calendar.getInstance();
		propInfo.object = baseFmpRevision;
		com.teamcenter.services.loose.core._2010_09.DataManagement.NameValueStruct1[] nameValueStruct
		= new com.teamcenter.services.loose.core._2010_09.DataManagement.NameValueStruct1[2];
		nameValueStruct[0] = new  com.teamcenter.services.loose.core._2010_09.DataManagement.NameValueStruct1();
		nameValueStruct[0].name = "s7_MATURITY";
		nameValueStruct[0].values = new String[]{"Released"}; //"2013-05-10T15:40:28+09:00"
		nameValueStruct[1] = new  com.teamcenter.services.loose.core._2010_09.DataManagement.NameValueStruct1();
		nameValueStruct[1].name = "s7_PROJECT_CODE";
		nameValueStruct[1].values = new String[]{projectCode};
		propInfo.vecNameVal = nameValueStruct;
		lock(connection, baseFmpRevision);
		dm.setProperties(new com.teamcenter.services.loose.core._2010_09.DataManagement.PropInfo[]{propInfo}, new String[]{""});
		save(connection, baseFmpRevision);
		unlock(connection, baseFmpRevision);

		//		InvokeICTMethodResponse response = createNewProcessTemp(connection, new ModelObject[]{baseFmpRevision}, "Weld Point Auto Release", "WSR");
		//ReleaseStatus의 Release날짜와  Effectivity 날짜를 ECO 릴리즈 날짜로 수정.
		InstanceInfo instanceInfo = createNewProcess(connection, new ModelObject[]{baseFmpRevision}, "Weld Point Auto Release", "WSR");
		if( instanceInfo.serviceData.sizeOfUpdatedObjects() > 0){
			for( int i = 0; i < instanceInfo.serviceData.sizeOfUpdatedObjects(); i++ ){
				ModelObject updatedObject = instanceInfo.serviceData.getUpdatedObject(i);
				if( updatedObject instanceof EPMTask){
					EPMTask task = (EPMTask)updatedObject;
					dmService.getProperties(new ModelObject[]{task}, new String[]{"release_status_attachments","release_status_list","release_statuses"});
					ModelObject[] releaseStatusAtt = task.get_release_status_attachments();
					ReleaseStatus releaseStatus = (ReleaseStatus)releaseStatusAtt[0];
					changeEffectivityDate(releaseStatus, connection, newReleaseDate, tcItemUtil);
					changeReleaseDate(releaseStatus, connection, newReleaseDate);

					HashMap<String, String> map = new HashMap<String, String>();
					map.put("release_status_puid", releaseStatus.getUid());
					map.put("eco_rev_puid", "UPDATE_REF_COMS");
					try {
						commonDao.update("com.symc.weld.updateDateReleasedWithEco", map);
					} catch (Exception e) {
						ByteArrayOutputStream out = new ByteArrayOutputStream();
				        PrintStream pinrtStream = new PrintStream(out);
				        e.printStackTrace(pinrtStream);
						addLog("******************************************************************************************************************");
						addLog("Effectivity 날짜를 ECO 릴리즈 날짜로 수정중 에러발생    : " + out.toString());
						addLog("******************************************************************************************************************");
						
					}

					break;
				}
			}
		}

	}

	private void release(Connection connection, TcItemUtil tcItemUtil, TcDataManagementService dmService
			, HashMap<String, ItemRevision> ecoRevisionMap, HashMap<ItemRevision, ArrayList<ItemRevision>> targetToReleaseMap) throws Exception{

		DataManagementService dm = DataManagementService.getService(connection);

		//ReleaseStatus의 Release날짜와  Effectivity 날짜를 ECO 릴리즈 날짜로 수정.
		TcCommonDao commonDao = TcCommonDao.getTcCommonDao();
		Set<String> keySet = ecoRevisionMap.keySet();
		ArrayList<String> ecoList = new ArrayList<String>(keySet);
		Collections.sort(ecoList);

		for( String ecoNo : ecoList){
			ItemRevision ecoRevision = ecoRevisionMap.get(ecoNo);
			ArrayList<ItemRevision> list = targetToReleaseMap.get(ecoRevision);
			dmService.getProperties(new ModelObject[]{ecoRevision}, new String[]{"date_released"});
			Calendar ecoReleaseDate = ecoRevision.get_date_released();

			// [20151126][ymjang] Release 대상이 없을 경우, Skip
			if (list == null) continue;

			//			dm.unloadObjects(list.toArray(new ModelObject[list.size()]) );
			for( int i = 0; i < list.size(); i++){

				ItemRevision revision = list.get(i);
				//				ServiceData serviceData = dm.loadObjects(new String[]{revision.getUid()});
				//				revision = (ItemRevision)serviceData.getPlainObject(0);
				//				dm.getProperties(new ModelObject[]{revision}, new String[]{"release_status_list"});

				com.teamcenter.services.loose.core._2010_09.DataManagement.PropInfo propInfo
				= new com.teamcenter.services.loose.core._2010_09.DataManagement.PropInfo();
				propInfo.timestamp = Calendar.getInstance();
				propInfo.object = revision;
				com.teamcenter.services.loose.core._2010_09.DataManagement.NameValueStruct1[] nameValueStruct = new com.teamcenter.services.loose.core._2010_09.DataManagement.NameValueStruct1[1];
				nameValueStruct[0] = new com.teamcenter.services.loose.core._2010_09.DataManagement.NameValueStruct1();
				nameValueStruct[0].name = "s7_MATURITY";
				nameValueStruct[0].values = new String[] { "Released" };
				propInfo.vecNameVal = nameValueStruct;
				lock(connection, revision);
				//				dm.setProperties(new com.teamcenter.services.loose.core._2010_09.DataManagement.PropInfo[] { propInfo },
				//						new String[] { "ENABLE_PSE_BULLETIN_BOARD" });
				dm.setProperties(new com.teamcenter.services.loose.core._2010_09.DataManagement.PropInfo[] { propInfo }, new String[] { "" });
				save(connection, revision);
				unlock(connection, revision);
			}

			InstanceInfo instanceInfo = createNewProcess(connection, list.toArray(new ModelObject[list.size()]), "Weld Point Auto Release", "WSR");
			for( int i = 0; i < instanceInfo.serviceData.sizeOfUpdatedObjects(); i++ ){
				ModelObject updatedObject = instanceInfo.serviceData.getUpdatedObject(i);
				if( updatedObject instanceof EPMTask){
					EPMTask task = (EPMTask)updatedObject;
					dmService.getProperties(new ModelObject[]{task}, new String[]{"release_status_attachments","release_status_list","release_statuses"});
					ModelObject[] releaseStatusAtt = task.get_release_status_attachments();
					ReleaseStatus releaseStatus = (ReleaseStatus)releaseStatusAtt[0];

					changeEffectivityDate(releaseStatus, connection, ecoReleaseDate, tcItemUtil);
					changeReleaseDate(releaseStatus, connection, ecoReleaseDate);

					HashMap<String, String> map = new HashMap<String, String>();
					map.put("release_status_puid", releaseStatus.getUid());
					map.put("eco_rev_puid", ecoRevision.getUid());
					try {
						commonDao.update("com.symc.weld.updateDateReleasedWithEco", map);
					} catch (Exception e) {
						ByteArrayOutputStream out = new ByteArrayOutputStream();
				        PrintStream pinrtStream = new PrintStream(out);
				        e.printStackTrace(pinrtStream);
						addLog("******************************************************************************************************************");
						addLog("Weld Point Auto Release 중 에러 발생    : " + out.toString());
						addLog("******************************************************************************************************************");
					}

					break;
				}
			}

		}

	}


	protected void setReferenceProperties(Connection connection, ItemRevision targetRevision, ItemRevision ecoRevision) throws Exception {
		com.teamcenter.services.strong.core.DataManagementService dmService
		= com.teamcenter.services.strong.core.DataManagementService.getService(connection);

		//		HashMap<String, String> map = new HashMap<String, String>();
		//		map.put("s7_ECO_NO", ecoRevision.getUid());
		//		dmService.setProperties(new ModelObject[]{targetRevision}, map);
		com.teamcenter.services.loose.core._2010_09.DataManagement.PropInfo prop[] = new com.teamcenter.services.loose.core._2010_09.DataManagement.PropInfo[1];
		prop[0] = new com.teamcenter.services.loose.core._2010_09.DataManagement.PropInfo();
		prop[0].object = targetRevision;
		prop[0].timestamp = Calendar.getInstance();
		com.teamcenter.services.loose.core._2010_09.DataManagement.NameValueStruct1 nvs = new com.teamcenter.services.loose.core._2010_09.DataManagement.NameValueStruct1();
		nvs.name = "s7_ECO_NO";
		nvs.values = new String[]{ecoRevision.getUid()};
		prop[0].vecNameVal = new com.teamcenter.services.loose.core._2010_09.DataManagement.NameValueStruct1[]{nvs};
		lock(connection, targetRevision);
		//		com.teamcenter.services.loose.core._2010_09.DataManagement.SetPropertyResponse response = DataManagementService.getService(connection).setProperties(prop, new String[]{"ENABLE_PSE_BULLETIN_BOARD"});
		com.teamcenter.services.loose.core._2010_09.DataManagement.SetPropertyResponse response = DataManagementService.getService(connection).setProperties(prop, new String[]{""});
		save(connection, targetRevision);
		unlock(connection, targetRevision);
		if(response.data.sizeOfPartialErrors() > 0){
			throw new Exception("setProperties Fail!");
		}
	}

	private void lock(Connection connection, ModelObject model) throws ServiceException, ICCTException{

		ICTService service = ICTService.getService(connection);
		com.teamcenter.soa.client.model.Type type = model.getTypeObject();
		Arg[] args = new Arg[3];
		args[0] = TcUtility.createArg(type.getName());
		args[1] = TcUtility.createArg(type.getUid());
		args[2] = TcUtility.createArg(model.getUid());
		InvokeICTMethodResponse response = service.invokeICTMethod("ICCT", "lock", args);
		if( response.serviceData.sizeOfPartialErrors() > 0)
		{
			throw new ICCTException( response.serviceData);
		}
	}

	private void unlock(Connection connection, ModelObject model)
			throws Exception {

		ICTService service = ICTService.getService(connection);
		com.teamcenter.soa.client.model.Type type = model.getTypeObject();
		Arg[] args = new Arg[3];
		args[0] = TcUtility.createArg(type.getName());
		args[1] = TcUtility.createArg(type.getUid());
		args[2] = TcUtility.createArg(model.getUid());
		InvokeICTMethodResponse response = service.invokeICTMethod("ICCT",
				"unlock", args);
		if (response.serviceData.sizeOfPartialErrors() > 0) {
			throw new ICCTException(response.serviceData);
		}
	}

	private void save(Connection connection, ModelObject model) throws ServiceException{

		ICTService service = ICTService.getService(connection);
		com.teamcenter.soa.client.model.Type type = model.getTypeObject();
		Arg[] args = new Arg[3];
		args[0] = TcUtility.createArg(type.getName());
		args[1] = TcUtility.createArg(type.getUid());
		args[2] = TcUtility.createArg(model.getUid());
		service.invokeICTMethod("ICCT", "save", args);
	}

	private void refresh(Connection connection, ModelObject model, int lockFlag) throws ServiceException{

		ICTService service = ICTService.getService(connection);
		com.teamcenter.soa.client.model.Type type = model.getTypeObject();
		Arg[] args = new Arg[4];
		args[0] = TcUtility.createArg(type.getName());
		args[1] = TcUtility.createArg(type.getUid());
		args[2] = TcUtility.createArg(model.getUid());
		args[3] = TcUtility.createArg(lockFlag);
		service.invokeICTMethod("ICCT", "refresh", args);
	}

	private ItemRevision revise(Connection connection, ItemRevision itemRev, TcItemUtil tcItemUtil, TcDataManagementService dmService) throws Exception {

		dmService.getProperties(new ModelObject[]{itemRev}, new String[]{"item_id", "object_name", "item_revision_id", "object_desc"});
		ItemRevision newRevision = null;
		try{

			com.teamcenter.services.strong.core._2008_06.DataManagement.ReviseInfo[] revInfo
			= new com.teamcenter.services.strong.core._2008_06.DataManagement.ReviseInfo[1];

			revInfo[0] = new ReviseInfo();
			revInfo[0].clientId = itemRev.getUid() + "AAAAAAAAAAAAA";
			revInfo[0].baseItemRevision = itemRev;
			revInfo[0].name = itemRev.get_object_name();
			revInfo[0].newRevId = getNextRevisionID(itemRev.get_item_revision_id());
			revInfo[0].description = itemRev.get_object_desc();
			//		    ReviseResponse2 response = com.teamcenter.services.internal.strong.core.DataManagementService.getService(connection).reviseObject(revInfo, true);
			ReviseResponse2 response = dmService.revise2(revInfo);

			if(response.serviceData.sizeOfPartialErrors() > 0){
				throw new Exception("Revise Fail!");
			}

			int cretedCount = response.serviceData.sizeOfCreatedObjects();
			for(int i=0; i <cretedCount; i++) {
				ModelObject createdObject = response.serviceData.getCreatedObject(i);
				if(createdObject instanceof ItemRevision) {
					newRevision = tcItemUtil.getRevisionInfo(createdObject.getUid());
				}
			}

		}catch(Exception e){
			ByteArrayOutputStream out = new ByteArrayOutputStream();
	        PrintStream pinrtStream = new PrintStream(out);
	        e.printStackTrace(pinrtStream);
	        addLog("******************************************************************************************************************");
			addLog("아이템 Revise 시 에러 발생    : " + out.toString());
			addLog("******************************************************************************************************************");
			throw e;
		}finally{
		}

		//		HashMap<String, String> map = new HashMap<String, String>();
		//		map.put("s7_MATURITY", "In Work");
		//		dmService.setProperties(new ModelObject[]{newRevision}, map);

		com.teamcenter.services.loose.core._2010_09.DataManagement.PropInfo propInfo = new com.teamcenter.services.loose.core._2010_09.DataManagement.PropInfo();
		propInfo.timestamp = Calendar.getInstance();
		propInfo.object = newRevision;
		com.teamcenter.services.loose.core._2010_09.DataManagement.NameValueStruct1[] nameValueStruct = new com.teamcenter.services.loose.core._2010_09.DataManagement.NameValueStruct1[1];
		nameValueStruct[0] = new  com.teamcenter.services.loose.core._2010_09.DataManagement.NameValueStruct1();
		nameValueStruct[0].name = "s7_MATURITY";
		nameValueStruct[0].values = new String[]{"In Work"};
		propInfo.vecNameVal = nameValueStruct;
		lock(connection, newRevision);
		//		DataManagementService.getService(connection).setProperties(new com.teamcenter.services.loose.core._2010_09.DataManagement.PropInfo[]{propInfo}, new String[]{"ENABLE_PSE_BULLETIN_BOARD"});
		DataManagementService.getService(connection).setProperties(new com.teamcenter.services.loose.core._2010_09.DataManagement.PropInfo[]{propInfo}, new String[]{""});
		save(connection, newRevision);
		unlock(connection, newRevision);
		return newRevision;
	}

	private ItemRevision getFmpRevision(Connection connection, TcItemUtil tcItemUtil, TcDataManagementService dmService ,HashMap<ItemRevision, ArrayList<ItemRevision>> targetToReleaseMap
			, WeldPointGroupVO vo, ItemRevision ecoRevision) throws Exception{

		//ECO ID가 일치하는 FMP를 가져온다.
		ItemRevision fmpRevision = getFmpRevisionWithEco(tcItemUtil, dmService, vo.getFmpId(), vo.getEcoNo());

		//ECO ID와 일치하는 FMP가 존재하지 않을 경우, 최신 리비전을 가져온 후, Revis함.
		if( fmpRevision == null){
			fmpRevision = getLatestRevision(tcItemUtil, dmService, vo.getFmpId());
			dmService.getProperties(new ModelObject[]{fmpRevision}, new String[]{"item_id", "item_revision_id"});

			fmpRevision = revise(connection, fmpRevision, tcItemUtil, dmService);

			//			HashMap<String, String> map = new HashMap<String, String>();
			//			map.put("s7_MATURITY", "In Work");
			//			map.put("s7_PROJECT_CODE", vo.getProjectCode());
			//			dmService.setProperties(new ModelObject[]{fmpRevision}, map);
			com.teamcenter.services.loose.core._2010_09.DataManagement.PropInfo propInfo
			= new com.teamcenter.services.loose.core._2010_09.DataManagement.PropInfo();
			propInfo.timestamp = Calendar.getInstance();
			propInfo.object = fmpRevision;
			com.teamcenter.services.loose.core._2010_09.DataManagement.NameValueStruct1[] nameValueStruct = new com.teamcenter.services.loose.core._2010_09.DataManagement.NameValueStruct1[2];
			nameValueStruct[0] = new  com.teamcenter.services.loose.core._2010_09.DataManagement.NameValueStruct1();
			nameValueStruct[0].name = "s7_MATURITY";
			nameValueStruct[0].values = new String[]{"In Work"}; //"2013-05-10T15:40:28+09:00"
			nameValueStruct[1] = new  com.teamcenter.services.loose.core._2010_09.DataManagement.NameValueStruct1();
			nameValueStruct[1].name = "s7_PROJECT_CODE";
			nameValueStruct[1].values = new String[]{vo.getProjectCode()};
			propInfo.vecNameVal = nameValueStruct;
			lock(connection, fmpRevision);
			//			DataManagementService.getService(connection).setProperties(new com.teamcenter.services.loose.core._2010_09.DataManagement.PropInfo[]{propInfo}, new String[]{"ENABLE_PSE_BULLETIN_BOARD"});
			DataManagementService.getService(connection).setProperties(new com.teamcenter.services.loose.core._2010_09.DataManagement.PropInfo[]{propInfo}, new String[]{""});
			save(connection, fmpRevision);
			unlock(connection, fmpRevision);
			//ECO No 저장
			//			ItemRevision ecoRevision = getItemRevision(connection, tcItemUtil, vo.getEcoNo(), "000");
			setReferenceProperties(connection, fmpRevision, ecoRevision);

			if( targetToReleaseMap.containsKey(ecoRevision) ){
				ArrayList<ItemRevision> list = targetToReleaseMap.get(ecoRevision);
				list.add(fmpRevision);
			}else{
				ArrayList<ItemRevision> list = new ArrayList<ItemRevision>();
				list.add(fmpRevision);
				targetToReleaseMap.put(ecoRevision, list);
			}

		}
		return fmpRevision;
	}

	private ItemRevision getLatestRevision(TcItemUtil tcItemUtil, TcDataManagementService dmService, String itemID) throws Exception{
		ItemRevision revision = null;
		ItemRevision[] revisionList = getItemRevisionList(tcItemUtil, dmService, itemID);
		if( revisionList == null){
			throw new Exception("fmpLatestRevision == null");
		}else{
			revision = revisionList[revisionList.length-1];
		}

		return revision;
	}

	private ItemRevision getFmpRevisionWithEco(TcItemUtil tcItemUtil, TcDataManagementService dmService, String fmpItemID, String ecoID) throws Exception{

		ItemRevision resultFmpRevision = null;
		ArrayList<ItemRevision> fmpList = new ArrayList<ItemRevision>();
		ItemRevision[] fmpRevisionList = getItemRevisionList(tcItemUtil, dmService, fmpItemID);

		for(ItemRevision fmpRevision : fmpRevisionList){
			dmService.getProperties(new ModelObject[]{fmpRevision}, new String[]{"item_id","item_revision_id","s7_ECO_NO"});

			//Revision ID 가 000은 비교에서 제외.
			if( fmpRevision.get_item_revision_id().equals("000")){
				continue;
			}
			ItemRevision fmpEcoRevision = (ItemRevision) fmpRevision.getPropertyObject("s7_ECO_NO").getModelObjectValue();

			dmService.getProperties(new ModelObject[]{fmpEcoRevision}, new String[]{"item_id"});
			String foundEcoId = fmpEcoRevision.get_item_id();
			if( ecoID.equals(foundEcoId)){
				fmpList.add(fmpRevision);  
			}
		}

		if( fmpList.isEmpty()){
			return null;
		}

		//혹시 동일한 ECO ID를 가진 FMP가 다수 존재 할 경우, 가장 최신의 리비전을 리턴.
		resultFmpRevision = fmpList.get(fmpList.size() - 1);
		return resultFmpRevision;
	}

	private ItemRevision[] getItemRevisionList(TcItemUtil tcItemUtil, TcDataManagementService dmService, String itemID) throws Exception {

		Item item = tcItemUtil.getItem(itemID);
		if( item == null) return null;

		dmService.getProperties(new ModelObject[]{item}, new String[]{"revision_list"});
		ModelObject[] list = item.get_revision_list();
		ItemRevision[] revisionList = new ItemRevision[list.length];
		System.arraycopy(list, 0, revisionList, 0, list.length);

		return revisionList;
	}

	@SuppressWarnings("unchecked")
	private HashMap<String, String> getCurrentWeldFmpRevision(String weldFunctionMaster) throws Exception{
		HashMap<String, String> tmpMap = new HashMap<String, String>();
		tmpMap.put("item_id", weldFunctionMaster);
		TcCommonDao commonDao = TcCommonDao.getTcCommonDao();
		HashMap<String, String> fmpRevisionMap = (HashMap<String, String>)commonDao.selectOne("com.symc.weld.getLatestRevision", tmpMap);

		return fmpRevisionMap;
	}

	/**
	 * [차체 Function 용접점 관리 방안] [20150907][ymjang] 메스드 명 변경 addWeldPointGroupInfo --> addXWeldPointGroupInfo
	 * @param epl
	 * @param targetList
	 * @param fmpID
	 * @param projectCode
	 */
	private void addXWeldPointGroupInfo(HashMap<String, Object> epl, ArrayList<WeldPointGroupVO> targetList
			, String fmpID, String projectCode){

		String ecoNo = (String)epl.get("ECO_NO");
		String changeType = (String)epl.get("CT");
		String eplID = (String)epl.get("EPL_ID");

		String parentId = (String)epl.get("PARENT_NO");
		String parentRev = (String)epl.get("PARENT_REV");

		WeldPointGroupVO vo = new WeldPointGroupVO();
		vo.setChangeType(changeType);
		vo.setEcoNo(ecoNo);
		vo.setEplId(eplID);

		if( changeType.equals("D")){
			vo.setItemId((String)epl.get("OLD_PART_NO"));
			vo.setItemRevId((String)epl.get("OLD_PART_REV"));
		}else{
			vo.setItemId((String)epl.get("NEW_PART_NO"));
			vo.setItemRevId((String)epl.get("NEW_PART_REV"));
		}

		vo.setFmpId(fmpID);
		vo.setProjectCode(projectCode);
		vo.setParentId(parentId);
		vo.setParentRevId(parentRev);
		
		// 정식 용접점 변환 서비스가 아닌 추가 적인 미변환 Part 임을 알리는 Flag 필요
		if( notTransformPartList_boolean ) {
			vo.setNotTransformFlag(true);
			
			
			if( !targetList.contains(vo)){
				targetList.add(vo);
			}
			
			return;
		}
		
		
		if( !targetList.contains(vo)){
			targetList.add(vo);
			//데몬 실행시 변환요청 항목(미변환 항목 아님)
			addLog("******변환 항목(미변환 항목 아님)*********************************************************************************");
			addLog("FUNC                 : " + epl.get("FUNC"));
			addLog("PARENT NO            : " + epl.get("PARENT_NO"));
			addLog("PARENT REVIION       : " + epl.get("PARENT_REV"));
			addLog("OLD PART NO          : " + epl.get("OLD_PART_NO"));
			addLog("OLD PART REVISION    : " + epl.get("OLD_PART_REV"));
			addLog("NEW PART NO          : " + epl.get("NEW_PART_NO"));
			addLog("NEW PART REVISION    : " + epl.get("NEW_PART_REV"));
			addLog("ECO NO 			     : " + epl.get("ECO_NO"));
			addLog("NEW SMODE 			 : " + epl.get("NEW_SMODE"));
			addLog("******************************************************************************************************************");
			
		}

	}

	/**
	 * [차체 Function 용접점 관리 방안] [20150907][ymjang] 
	 * X100 이외의 차종은 별도의 용접점 관리 Part를 생성하여 관리하고, 
	 * X100 의 경우만, 기존의 용접점 대상 추출 로직을 그대로 적용한다. 
	 * @param epl
	 * @param targetList
	 * @param fmpID
	 * @param projectCode
	 * @throws Exception 
	 */
	private void addWeldPointGroupInfo(HashMap<String, Object> epl, ArrayList<WeldPointGroupVO> targetList, String fmpID, String projectCode ) throws Exception
	{

		TcCommonDao commonDao = TcCommonDao.getTcCommonDao();
		String ecoNo = (String)epl.get("ECO_NO");
		String changeType = (String)epl.get("CT");
		String eplID = (String)epl.get("EPL_ID");

		WeldPointGroupVO vo = null;

		// 용접점 대상 Part (W Part의 부모) 가 삭제되면 ECO EPL 상에 하위의 W 용점점 PART 는 보이지 않음
		// --> 용접점 대상 Part (W Part의 부모)를 Dispatcher 에게 전달해 모두 삭제하도록 한다.
		if( changeType.equals("D")){

			new WeldPointGroupVO();
			vo.setChangeType(changeType);
			vo.setEcoNo(ecoNo);
			vo.setEplId(eplID);

			vo.setFmpId(fmpID);
			vo.setProjectCode(projectCode);
			vo.setParentId((String)epl.get("OLD_PART_NO"));
			vo.setParentRevId((String)epl.get("OLD_PART_REV"));

			vo.setItemId((String)epl.get("OLD_PART_NO"));
			vo.setItemRevId((String)epl.get("OLD_PART_REV"));

			if( !targetList.contains(vo)){
				targetList.add(vo);
			}

			return;
		} 
		
		// 수정 용접점 미변환 대상 변환 로직 추가
		// 미변환 대상 속성값 추출 하여 Vo에 담아 변환 서버로 전송 준비
		if( notTransformPartList_boolean ) {
			
			String parent_no = (String)epl.get("PARENT_NO");
			String parent_revision_id = (String)epl.get("PARENT_REV");
			String part_no = null;
			String part_revision_id = null;			
			if( changeType.equals("D")){
				part_no = (String)epl.get("OLD_PART_NO");
				part_revision_id = (String)epl.get("OLD_PART_REV");
			}else{
				part_no = (String)epl.get("NEW_PART_NO");
				part_revision_id = (String)epl.get("NEW_PART_REV");
			}

			// 용접점 W PART 만
			//[SR170320-022][ljg] M으로 시작 하는 아이템도 추가
			if (part_no != null && (part_no.startsWith("W") || part_no.startsWith("M"))){
				
				ItemRevision hasCatPartRevsion = getItemRevision(session.getConnection(), tcItemUtil, part_no , part_revision_id);
				ModelObject[] hasDataset = getRelatedDataset(hasCatPartRevsion, session);
				
				ModelObject[] catpartDataset = null;
				if (hasDataset != null) {
					for (int i = 0; i < hasDataset.length; i++) {
						if (IFConstants.TYPE_DATASET_CATPART.equals(hasDataset[i].getTypeObject().getName())) {
							catpartDataset = new ModelObject[] { hasDataset[i] };
							break;
						}
					}
				} 
				
				if(catpartDataset == null) {
					
					updateWeldPointTransLog(epl, "E", "Do not have CatPart");
					return;
				}
				
				vo = new WeldPointGroupVO();
				vo.setChangeType(changeType);
				vo.setEcoNo(ecoNo);
				vo.setEplId(eplID);

				vo.setFmpId(fmpID);
				vo.setProjectCode(projectCode);
				vo.setParentId(parent_no);
				vo.setParentRevId(parent_revision_id);

				vo.setItemId(part_no);
				vo.setItemRevId(part_revision_id);
				
				// 정식 용접점 변환 서비스가 아닌 추가 적인 미변환 Part 임을 알리는 Flag 필요
				vo.setNotTransformFlag(true);
				
				
				if( !targetList.contains(vo)){
					targetList.add(vo);
				}
			}
				return;
		}
		
		
		String parentId = (String)epl.get("NEW_PART_NO");
		String parentRev = (String)epl.get("NEW_PART_REV");

		
		HashMap <String, String> paramMap = new HashMap <String, String>();
		paramMap.put("ECO_NO", ecoNo);
		paramMap.put("PARENT_NO", parentId);
		paramMap.put("PARENT_REV", parentRev);
		paramMap.put("PROJECT_CODE", projectCode);

		// 용접점 대상 PART 하위의 용접점 PART ('W') 를 EPL 에서 검색한다.
		List<HashMap<String,Object>> eplList = (List<HashMap<String,Object>>)commonDao.selectList("com.symc.weld.getEcoWeldParentEplInfo", paramMap);

		if (eplList.size() == 0) {
			////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			/*
			* 수정점    : 20200219 
			* 수정 내용 : Carry Over 관련된 용접점 생성 로직 추가
			* 1. Carry Over 추가된 용접 파트는 EPL 에서 Change Type 이 F2로 됨
			* 2. EPL 정보에서 
			*   ex) 용접점 그룹을 생성 하기 위해서는  ECO_BOM_LIST 테이블에 아래와 같이 데이터가 입력 되있어야함
			*   (1). Parent_No : M650BA2020A  Parent_REV : 004  Old_part_no : null        Old_part_rev : null  new_part_no :  5511037011  new_part_rev : 001
			*   (2). Parent_No : 5511037011   Parent_REV : 001  Old_part_no : W742037050  Old_part_rev : 001   new_part_no :  W742037050  new_part_rev : 002
			*   
			*   (1), (2) 번의 데이터가 연계적으로 입력이 되있어야 함 
			*   하지만 Chane Type 이 F2일 경우 (2) 번 줄의 데이터가 없는 경우가 있을 수 있어서 용접점 생성이 스킵되는 현상이 생김 
			*/
			// Change Type 이 F2 일경우 
			if( changeType.equals("F2") && ((String)epl.get("OLD_PART_NO")) == null) {
				TcDataManagementService dmService = new TcDataManagementService(session);
				String part_no = "";
				String part_revision_id = "";
//				ItemRevision fmpPartLatestRevision = getLatestRevision(tcItemUtil, dmService, fmpID);
//				List<String> fmpChildrenItemIdList = (List<String>)commonDao.selectList("com.symc.weld.getChildrenItemID", fmpID);
					
					List<String> childrenItemIdList = (List<String>)commonDao.selectList("com.symc.weld.getChildrenItemID", parentId);
						for( int i = 0; i < childrenItemIdList.size(); i ++ ) {
							// 용접점 을 포함 하는 파트 하위의 파트중 W,M 으로 시작 하는 용접점 파트 유무 확인
							String tempChildItemId = childrenItemIdList.get(i);
							if( tempChildItemId.startsWith("W") || tempChildItemId.startsWith("M")) {
								part_no = tempChildItemId;
								break;
							}
					}
					
					if( part_no != null || !part_no.equals("") ) {
						
						ItemRevision weldPartLatestRevision = getLatestRevision(tcItemUtil, dmService, part_no);
						dmService.getProperties(new ModelObject[]{weldPartLatestRevision}, new String[]{"item_revision_id"});
						part_revision_id = weldPartLatestRevision.get_item_revision_id();
						
						vo = new WeldPointGroupVO();
						vo.setChangeType(changeType);
						vo.setEcoNo(ecoNo);
						vo.setEplId(eplID);
						
						vo.setFmpId(fmpID);
						vo.setProjectCode(projectCode);
						vo.setParentId(parentId);
						vo.setParentRevId(parentRev);
						
						vo.setItemId(part_no);
						vo.setItemRevId(part_revision_id);
						
						
						if( !targetList.contains(vo)){
							targetList.add(vo);
							
							//데몬 실행시 변환요청 항목(미변환 항목 아님)
							addLog("******변환 항목(미변환 항목 아님)*********************************************************************************");
							addLog("FUNC                 : " + epl.get("FUNC"));
							addLog("CT                   : " + changeType);
							addLog("PARENT NO            : " + epl.get("PARENT_NO"));
							addLog("PARENT REVIION       : " + epl.get("PARENT_REV"));
							addLog("OLD PART NO          : " + epl.get("OLD_PART_NO"));
							addLog("OLD PART REVISION    : " + epl.get("OLD_PART_REV"));
							addLog("NEW PART NO          : " + epl.get("NEW_PART_NO"));
							addLog("NEW PART REVISION    : " + epl.get("NEW_PART_REV"));
							addLog("ECO NO               : " + epl.get("ECO_NO"));
							addLog("NEW SMODE            : " + epl.get("NEW_SMODE"));
							addLog("******************************************************************************************************************");
						}
					}
					
				return;
			//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			} else {
				return ;
			}
		}
		
		HashMap<String,Object> weldEpl = new HashMap<String,Object>();
		for (int i = 0; i < eplList.size(); i++) {

			weldEpl = (HashMap<String,Object>)eplList.get(i);

			changeType = (String)weldEpl.get("CT");
			eplID = (String)weldEpl.get("EPL_ID");

			String partNo = null;
			String partRev = null;			
			if( changeType.equals("D")){
				partNo = (String)weldEpl.get("OLD_PART_NO");
				partRev = (String)weldEpl.get("OLD_PART_REV");
			}else{
				partNo = (String)weldEpl.get("NEW_PART_NO");
				partRev = (String)weldEpl.get("NEW_PART_REV");
			}

			// 용접점 W PART 만
			//[SR170320-022][ljg] M으로 시작 하는 아이템도 추가
			if (partNo != null && (partNo.startsWith("W") || partNo.startsWith("M"))){
				vo = new WeldPointGroupVO();
				vo.setChangeType(changeType);
				vo.setEcoNo(ecoNo);
				vo.setEplId(eplID);

				vo.setFmpId(fmpID);
				vo.setProjectCode(projectCode);
				vo.setParentId(parentId);
				vo.setParentRevId(parentRev);

				vo.setItemId(partNo);
				vo.setItemRevId(partRev);
				
				
					if( !targetList.contains(vo)){
					targetList.add(vo);
					
					//데몬 실행시 변환요청 항목(미변환 항목 아님)
					addLog("******변환 항목(미변환 항목 아님)*********************************************************************************");
					addLog("FUNC                 : " + epl.get("FUNC"));
					addLog("CT                   : " + changeType);
					addLog("PARENT NO            : " + epl.get("PARENT_NO"));
					addLog("PARENT REVIION       : " + epl.get("PARENT_REV"));
					addLog("OLD PART NO          : " + epl.get("OLD_PART_NO"));
					addLog("OLD PART REVISION    : " + epl.get("OLD_PART_REV"));
					addLog("NEW PART NO          : " + epl.get("NEW_PART_NO"));
					addLog("NEW PART REVISION    : " + epl.get("NEW_PART_REV"));
					addLog("ECO NO               : " + epl.get("ECO_NO"));
					addLog("NEW SMODE            : " + epl.get("NEW_SMODE"));
					addLog("******************************************************************************************************************");
				}
				

			}
		}  // end For문
		
		

	}

	/**
	 * [차체 Function 용접점 관리 방안] [20150907][ymjang] 
	 * X100 이외의 차종은 별도의 용접점 관리 Part를 생성하여 관리하고, 
	 * X100 의 경우만, 기존의 용접점 대상 추출 로직을 그대로 적용한다.
	 * @param epl
	 * @param targetList
	 * @param fmpID
	 * @param projectCode
	 * @throws Exception 
	 */
	private void setWeldGroupMapProject(HashMap<String, Object> epl, ArrayList<WeldPointGroupVO> targetList, String fmpID, String projectCode) throws Exception
	{
		String funcs = (String)epl.get("FUNC");
		String newPartNo = (String)epl.get("NEW_PART_NO");
		String oldPartNo = (String)epl.get("OLD_PART_NO");
		// W 용접점 Part 는 WeldGroup 을 생성하지 않으므로 Skip
		// [20151016][ymjang] newPartNo nullpointer excetpion 오류 수정
		
		
		if (newPartNo != null && (newPartNo.startsWith("W") || newPartNo.startsWith("M"))) //[SR170320-022][ljg] M으로 시작 하는 아이템도 추가
			return;

		if( !((String)epl.get("CT")).equals("F2") ) {
			if (oldPartNo != null && (oldPartNo.startsWith("W") || oldPartNo.startsWith("M"))) //[SR170320-022][ljg] M으로 시작 하는 아이템도 추가
				return;
		}

		//      F006 function에 포함된 경우는 Supply Mode는 무의미함.
		//      50150, 52001, 52002, 50600, 57000로 시작되는 아이디에 한해서 용접점 생성.
		if( funcs != null && funcs.indexOf("F006") >= 0){
			//[SR번호][20141201] shcho, Old를 떼어내기만 하고 New를 붙여넣지 않는 경우에 대한 Null 처리 추가
			if(newPartNo != null && (newPartNo.indexOf("50150") >= 0 || newPartNo.indexOf("52001") >= 0
					|| newPartNo.indexOf("52002") >= 0 || newPartNo.indexOf("50600") >= 0 || newPartNo.indexOf("57000") >= 0 )){

				if (projectCode.startsWith("X"))
					// X100 차종의 경우,
					addXWeldPointGroupInfo(epl, targetList, fmpID, projectCode);
				else
					// 그 외 차종의 경우,
					addWeldPointGroupInfo(epl, targetList, fmpID, projectCode);
			}
		}else if(funcs != null && ( funcs.indexOf("F610") >= 0 || funcs.indexOf("F620") >= 0
				|| funcs.indexOf("F630") >= 0 || funcs.indexOf("F640") >= 0 || funcs.indexOf("F650") >= 0
				|| funcs.indexOf("F660") >= 0 || funcs.indexOf("F720") >= 0 || funcs.indexOf("F730") >= 0
				|| funcs.indexOf("F740") >= 0 || funcs.indexOf("F750") >= 0)){
			String supplyMode = (String)epl.get("NEW_SMODE");
			if( supplyMode != null && supplyMode.toUpperCase().equals("P0")){

				if (projectCode.startsWith("X"))
					// X100 차종의 경우,
					addXWeldPointGroupInfo(epl, targetList, fmpID, projectCode);
				else
					// 그 외 차종의 경우,
					addWeldPointGroupInfo(epl, targetList, fmpID, projectCode);
				
			}
		}  
	}

	private  void setMvlCondition(Session session,
			String functionId, String condition, BOMWindow window,
			BOMLine targetLine) throws Exception {

		HashMap<String, String> corpOptionMap = null;
		HashMap<String, String> optionDescMap = new HashMap<String, String>();

		Type windowType = window.getTypeObject();
		BOMLine topLine = (BOMLine) window.get_top_line();
		ICTService service = ICTService.getService(session.getConnection());
		String result = "";
		Pattern p = Pattern.compile("F\\w*:");
		Matcher m = p.matcher(condition);
		while (m.find()) {
			System.out.println(m.start() + " " + m.group());
			result = m.replaceAll(functionId + ":");
			System.out.println("result : " + result);
		}

		p = Pattern.compile(":\\w{3}\\s");
		m = p.matcher(condition);
		while (m.find()) {
			String optionName = m.group().substring(1).trim();
			System.out.println(m.start() + " " + optionName);
			if (!optionDescMap.containsKey(optionName)) {
				// Function Option 정의
				if (corpOptionMap == null) {
					corpOptionMap = getCorpOptionTest(session);
				}

				String uid = topLine.getUid();
				String desc = corpOptionMap.get(optionName);

				// Function에 옵션 정의
				Arg[] args = new Arg[2];
				args[0] = TcUtility.createArg(uid);
				args[1] = TcUtility.createArg("public " + optionName
						+ " uses  \"" + desc + "\" 'CorporateOption-001':"
						+ optionName);
				service.invokeICTMethod("ICCTVariantService",
						"lineDefineOption", args);
				System.out.println("Option Definition complete");
				// Window Save
				args = new Arg[3];
				args[0] = TcUtility.createArg( windowType.getName() );
				args[1] = TcUtility.createArg( windowType.getUid() );
				args[2] = TcUtility.createArg( window.getUid() );
				service.invokeICTMethod("ICCT", "save", args);
				System.out.println("Option Save");
				// Window Refresh
				args = new Arg[4];
				args[0] = TcUtility.createArg( windowType.getName() );
				args[1] = TcUtility.createArg( windowType.getUid() );
				args[2] = TcUtility.createArg( window.getUid() );
				args[3] = TcUtility.createArg(0);
				service.invokeICTMethod("ICCT", "refresh", args);
				System.out.println("BOMLine Refresh");
				optionDescMap.put(optionName, corpOptionMap.get(optionName));
			}else{
				System.out.println("optionDescMap.containsKey(" + optionName + ") : " + optionDescMap.containsKey(optionName));
			}
		}

		// Condition 셋팅.
		String uid = targetLine.getUid();
		Arg[] args = new Arg[2];
		args[0] = TcUtility.createArg(uid);
		args[1] = TcUtility.createArg(result);
		service.invokeICTMethod("ICCTVariantService", "setLineMvlCondition",
				args);

		System.out.println("setLineMvlCondition ");
	}

	private  HashMap<String, String> getCorpOptionTest(Session session) throws Exception{

		TcQueryService tcQueryService = new TcQueryService(session);
		BOMLineService bomlineService = new BOMLineService(session);
		TcItemUtil tcItemUtil = new TcItemUtil(session);
		BOMWindow window = null;
		HashMap<String, String> optionDescMap = new HashMap<String, String>();
		try{

			String queryName = TcConstants.SEARCH_ITEM_REVISION;
			String[] entries = { "Item ID", "Revision" };
			String[] values = { "CorporateOption-001", "000" };
			String[] properties = { TcConstants.PROP_ITEM_ID, "item_revision_id",
					"date_released", "object_desc", "object_type", "date_released"
			/*,"structure_revisions"*/ };

			ModelObject[] modelObject = tcQueryService.searchTcObject(queryName,
					entries, values, properties);

			ItemRevision corpOptionRevision = (ItemRevision)modelObject[0];

			window = bomlineService.getCreateBOMWindow(corpOptionRevision, null, null);
			//			window = createTopLineBOMWindow(session.getConnection(), corpOptionRevision, null, null);
			tcItemUtil.getProperties(new ModelObject[]{window}, new String[]{"top_line"});
			BOMLine topLine = (BOMLine)window.get_top_line();

			VariantManagementService vm = VariantManagementService.getService(session.getConnection());
			ModularOptionsInput input = new ModularOptionsInput();
			input.bomWindow = window;
			input.bomLines = new BOMLine[]{topLine};
			ModularOptionsForBomResponse response = vm.getModularOptionsForBom(new ModularOptionsInput[]{input});
			if( response.serviceData.sizeOfPartialErrors() < 1){

				VariantManagement.ModularOptionsInfo[] optionInfos =  response.optionsOutput[0].optionsInfo;
				for( VariantManagement.ModularOptionsInfo optionInfo : optionInfos){
					for( VariantManagement.ModularOption option : optionInfo.options.options){
						if( !optionDescMap.containsKey(option.optionName)){
							optionDescMap.put(option.optionName, option.optionDescription);
						}
					}
				}
			}
		}catch(Exception e){
			ByteArrayOutputStream out = new ByteArrayOutputStream();
	        PrintStream pinrtStream = new PrintStream(out);
	        e.printStackTrace(pinrtStream);
	        addLog("******************************************************************************************************************");
			addLog("getCorpOptionTest  에러 발생    : " + out.toString());
			addLog("******************************************************************************************************************");
			throw e;
		}finally{
			if( window != null){
				com.teamcenter.services.strong.cad.StructureManagementService.getService(session.getConnection()).closeBOMWindows(new BOMWindow[]{window});
			}
		}
		System.out.println("getCorpOption : " + optionDescMap.size());
		return optionDescMap;
	}

	private void changeReleaseDate(ModelObject modelObject, Connection connection, Calendar ecoReleaseDate) throws Exception{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
		String ecoDateStr = sdf.format(ecoReleaseDate.getTime());
		StringBuffer sb = new StringBuffer(ecoDateStr);
		sb.insert(22, ':');
		com.teamcenter.services.loose.core._2010_09.DataManagement.PropInfo propInfo
		= new com.teamcenter.services.loose.core._2010_09.DataManagement.PropInfo();
		propInfo.timestamp = Calendar.getInstance();
		propInfo.object = modelObject;
		com.teamcenter.services.loose.core._2010_09.DataManagement.NameValueStruct1 nameValueStruct = new  com.teamcenter.services.loose.core._2010_09.DataManagement.NameValueStruct1();
		nameValueStruct.name = "date_released";
		nameValueStruct.values = new String[]{sb.toString()};//"2013-05-10T15:40:28+09:00"
		propInfo.vecNameVal = new com.teamcenter.services.loose.core._2010_09.DataManagement.NameValueStruct1[]{nameValueStruct};

		lock(connection, modelObject);
		//		DataManagementService.getService(connection).setProperties(new com.teamcenter.services.loose.core._2010_09.DataManagement.PropInfo[]{propInfo},new String[]{"ENABLE_PSE_BULLETIN_BOARD"});
		DataManagementService.getService(connection).setProperties(new com.teamcenter.services.loose.core._2010_09.DataManagement.PropInfo[]{propInfo},new String[]{""});
		save(connection, modelObject);
		unlock(connection, modelObject);

	}

	private void changeEffectivityDate(ReleaseStatus releaseStatus, Connection connection, Calendar ecoReleaseDate, TcItemUtil tcItemUtil) throws Exception{

		tcItemUtil.getProperties(new ModelObject[]{releaseStatus}, new String[]{"effectivities"});
		ModelObject[] effectivities = releaseStatus.get_effectivities();
		for(ModelObject effectivityObj : effectivities){
			Effectivity effectivity = (Effectivity)effectivityObj;
			com.teamcenter.soa.client.model.Type type = effectivity.getTypeObject();
			SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy HH:mm", Locale.US);
			String ecoDateStr = sdf.format(ecoReleaseDate.getTime());

			ICTService service = ICTService.getService(connection);
			Arg[] args_ = new Arg[5];
			args_[0] = TcUtility.createArg(type.getName());
			args_[1] = TcUtility.createArg(type.getUid());
			args_[2] = TcUtility.createArg(releaseStatus.getUid());
			args_[3] = TcUtility.createArg(effectivity.getUid());
			args_[4] = TcUtility.createArgStringUnion(ecoDateStr + " to UP");
			InvokeICTMethodResponse response = null;
			try{
				lock(connection, effectivity);
				response = service.invokeICTMethod("ICCTEffectivity", "setDateRange", args_);
			}catch(Exception e){
				ByteArrayOutputStream out = new ByteArrayOutputStream();
		        PrintStream pinrtStream = new PrintStream(out);
		        e.printStackTrace(pinrtStream);
				addLog("******************************************************************************************************************");
				addLog("changeEffectivityDate 에러    : " + out.toString());
				addLog("******************************************************************************************************************");
				throw e;
			}finally{
				save(connection, effectivity);
				unlock(connection, effectivity);
			}

			if( response.serviceData.sizeOfPartialErrors() > 0)
			{
				throw new ICCTException( response.serviceData);
			}

		}
	}

	protected InvokeICTMethodResponse createNewProcessTemp(Connection connection, ModelObject[] revModels, String processTitle, String wfprocessName) throws NotLoadedException, Exception {
		ICTService service = ICTService.getService(connection);
		//		com.teamcenter.soa.client.model.Type type = model.getTypeObject();


		Arg[] args = new Arg[7];
		args[0] = TcUtility.createArg("Job");
		args[1] = TcUtility.createArg("TYPE::Job::EPMJob::EPMJob");
		args[2] = TcUtility.createArg(processTitle);
		args[3] = TcUtility.createArg("");
		args[4] = TcUtility.createArg("RWWJqzi94Fo0UD");
		args[5] = TcUtility.createArg(new int[]{1});

		String[] attachmentUids = new String[revModels.length];
		for(int i = 0; i < revModels.length; i++){
			ModelObject model = revModels[i];
			attachmentUids[i] = model.getUid();
		}
		args[6] = TcUtility.createArg(attachmentUids);
		InvokeICTMethodResponse response = service.invokeICTMethod("ICCTProcess", "create", args);
		if( response.serviceData.sizeOfPartialErrors() > 0)
		{
			throw new ICCTException( response.serviceData);
		}

		return response;
	}

	protected InstanceInfo createNewProcess (Connection connection, ModelObject[] revModels, String processTitle, String wfprocessName) throws NotLoadedException, Exception {

		ContextData contextData = new ContextData();
		String observerKey = "";
		String name = processTitle; //"My SOA Do Task";
		String subject = "";
		String description = "";

		//==============================================
		int[] attType = new int[revModels.length];
		for(int i=0; i < revModels.length; i++) {
			attType[i] = 1;
		}
		//=============================================
		String[] revUid = new String[revModels.length];
		for(int i=0; i<revModels.length; i++) {
			revUid[i] = revModels[i].getUid();
		}
		contextData.processTemplate = wfprocessName; //"SOA Do Task";
		contextData.subscribeToEvents = false;
		contextData.subscriptionEventCount = 0;
		contextData.attachmentCount = revModels.length;
		//==================================
		contextData.attachments = revUid;
		contextData.attachmentTypes = attType;
		//==================================

		InstanceInfo instanceInfo =  WorkflowService.getService(connection).createInstance(true, observerKey, name, subject, description, contextData);
		//wfService.createInstance(false, observerKey,name, subject, description, contextData);
		if( instanceInfo.serviceData.sizeOfPartialErrors() > 0){
			throw new Exception(TcUtil.makeMessageOfFail(instanceInfo.serviceData).get(TcConstants.TC_RETURN_FAIL_REASON).toString());
		}else{
			return instanceInfo;
		}
	}

	public ModelObject createDispatcherRequest(WeldPointGroupVO vo, Session session, ArrayList<WeldPointGroupVO> notHaveCatPartDatasetList, ArrayList<WeldPointGroupVO> transFailList) throws Exception {

		TcItemUtil tcItemUtil = new TcItemUtil(session);
		
		ItemRevision revision = tcItemUtil.getRevisionInfo(vo.getItemId(), vo.getItemRevId());
		
		if( revision == null ) {
			return null;
		}
		
		ModelObject[] dataset = getRelatedDataset(revision, session);
		ModelObject[] catpartDataset = null;
		if (dataset != null) {
			for (int i = 0; i < dataset.length; i++) {
				if (IFConstants.TYPE_DATASET_CATPART.equals(dataset[i].getTypeObject().getName())) {
					catpartDataset = new ModelObject[] { dataset[i] };
					break;
				}
			}
		} 
		
		if (catpartDataset != null) {

			System.out.println("ECO NO : " + vo.getEcoNo());

			TcServiceManager tcServiceManager = new TcServiceManager(session);
			DispatcherManagementService dispatcherMgmtService = DispatcherManagementService.getService(session.getConnection());
			com.teamcenter.services.strong.core._2008_06.DispatcherManagement.CreateDispatcherRequestArgs args[] = new com.teamcenter.services.strong.core._2008_06.DispatcherManagement.CreateDispatcherRequestArgs[1];
			args[0] = new com.teamcenter.services.strong.core._2008_06.DispatcherManagement.CreateDispatcherRequestArgs();
			args[0].providerName = "SIEMENS";
			args[0].serviceName = "weldpointexport";
			args[0].priority = 1;
			args[0].interval = -1;
			args[0].primaryObjects = catpartDataset;
			args[0].secondaryObjects = new ModelObject[] { revision };
			KeyValueArguments keyValueArgs[] = new KeyValueArguments[9];
			keyValueArgs[0] = new KeyValueArguments();
			keyValueArgs[0].key = "ECO_NO";
			keyValueArgs[0].value = vo.getEcoNo();
			keyValueArgs[1] = new KeyValueArguments();
			keyValueArgs[1].key = "FMP_ID";
			keyValueArgs[1].value = vo.getFmpId();
			keyValueArgs[2] = new KeyValueArguments();
			keyValueArgs[2].key = "PROJECT_CODE";
			keyValueArgs[2].value = vo.getProjectCode();
			keyValueArgs[3] = new KeyValueArguments();
			keyValueArgs[3].key = "CHANGE_TYPE";
			keyValueArgs[3].value = vo.getChangeType();
			keyValueArgs[4] = new KeyValueArguments();
			keyValueArgs[4].key = "EPL_ID";
			keyValueArgs[4].value = vo.getEplId();
			keyValueArgs[5] = new KeyValueArguments();
			keyValueArgs[5].key = "PARENT_NO";
			keyValueArgs[5].value = vo.getParentId();
			keyValueArgs[6] = new KeyValueArguments();
			keyValueArgs[6].key = "PARENT_REVISION_ID";
			keyValueArgs[6].value = vo.getParentRevId();
			keyValueArgs[7] = new KeyValueArguments();
			keyValueArgs[7].key = "PART_NO";
			keyValueArgs[7].value = vo.getItemId();
			keyValueArgs[8] = new KeyValueArguments();
			keyValueArgs[8].key = "PART_REVISION_ID";
			keyValueArgs[8].value = vo.getItemRevId();

			args[0].keyValueArgs = keyValueArgs;
			com.teamcenter.services.strong.core._2008_06.DispatcherManagement.CreateDispatcherRequestResponse responseObject = dispatcherMgmtService.createDispatcherRequest(args);
			if (!tcServiceManager.getDataService().ServiceDataError(responseObject.svcData)) {
				// 수정 log 추가
				
				addLog("PROJECT CODE         : " + vo.getProjectCode());
				addLog("FMP ID               : " + vo.getFmpId());
				addLog("PARENT NO            : " + vo.getParentId());
				addLog("PARENT REVIION       : " + vo.getParentRevId());
				addLog("PART NO              : " + vo.getItemId());
				addLog("PART REVISION        : " + vo.getItemRevId());
				addLog("ECO NO               : " + vo.getEcoNo());
				addLog("******************************************************************************************************************");
				return responseObject.requestsCreated[0];
			} else {
				vo.setTransFailReason(TcUtil.makeMessageOfFail(responseObject.svcData).get(TcConstants.TC_RETURN_FAIL_REASON).toString());
				transFailList.add(vo);
//				throw new Exception(TcUtil.makeMessageOfFail(responseObject.svcData).get(TcConstants.TC_RETURN_FAIL_REASON).toString());
			}
		}  else {
				// 미변환 리스트 중에 CatPart Dataset을 가지지 못한 항목
				notHaveCatPartDatasetList.add(vo);
		}
		return null;
	}

	public ModelObject[] getRelatedDataset(ItemRevision ir, Session session) throws Exception {
		ArrayList<ModelObject> datasets = new ArrayList<ModelObject>();
		// JT : TcConstants.RELATION_RENDERING ??조회?��? ?�으므�?추후 조회?�에??Properties??추�? ?�것.
		TcItemUtil tcItemUtil = new TcItemUtil(session);
		tcItemUtil.getProperties(new ModelObject[] { ir }, new String[] { TcConstants.RELATION_REFERENCES, TcConstants.RELATION_SPECIFICATION });
		// relatedObject =
		// modelobj[0].getProperty(TcConstants.RELATION_SPECIFICATION).getModelObjectArrayValue();
		ModelObject[] relatedReferenceObject = ir.getPropertyObject(TcConstants.RELATION_REFERENCES).getModelObjectArrayValue();
		for (int i = 0; i < relatedReferenceObject.length; i++) {
			if(relatedReferenceObject[i] instanceof Dataset) {
				datasets.add(relatedReferenceObject[i]);
			}
		}
		ModelObject[] relatedSpecificationObject = ir.getPropertyObject(TcConstants.RELATION_SPECIFICATION).getModelObjectArrayValue();
		for (int i = 0; i < relatedSpecificationObject.length; i++) {
			if(relatedSpecificationObject[i] instanceof Dataset) {
				datasets.add(relatedSpecificationObject[i]);
			}
		}
		return datasets.toArray(new ModelObject[datasets.size()]);
	}

	/**
	 * [20160115][ymjang] 각 ECO 별로 처리 로그 생성
	 *  Flag => S : Start 시작 , E : End 처리 완료( ECO 내에 변환용접점이 있고 변환 요청 완료 )
	 *  	 	P : Pass ( ECO 내에 변환 용접점이 없으며 변환요청도 하지 않음 )
	 *    		
	 * @param log
	 * @param eco_no
	 * @return
	 * @throws Exception
	 */
	private int insertJobTransLog(StringBuffer log, String eco_no, String trans_flag, String trans_msg) throws Exception {
		TcCommonDao commonDao = TcCommonDao.getTcCommonDao();

		//1. 각 ECO 별 처리 로그 생성을 위하여 JOB_PUID 를 구한다.
		String job_puid = (String)commonDao.selectOne("com.symc.weld.getJobPuid", null);
		String curTime = getCurrentTime();

		//1. 각 ECO 별 처리 로그 생성
		try {

			HashMap<String, String> tmpMap = new HashMap<String, String>();
			tmpMap.put("JOB_PUID", job_puid);
			tmpMap.put("WHO_USER_ID", "if_system");
			tmpMap.put("WHAT_JOB", "WELDPOINT");
			tmpMap.put("START_JOB_TIME", curTime);
			tmpMap.put("TRNAS_FLAG", trans_flag);
			tmpMap.put("TRNAS_MSG", trans_msg);
			tmpMap.put("TARGET_ID", eco_no);

			commonDao.insert("com.symc.weld.insertJobTransLog", tmpMap);

		} catch(Exception e) {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
	        PrintStream pinrtStream = new PrintStream(out);
	        e.printStackTrace(pinrtStream);
			addLog("******************************************************************************************************************");
			addLog("ECO 별로 처리 로그 생성 에러    : " + out.toString());
			addLog("******************************************************************************************************************");
			
			throw e;
		}

		return 0;
	}

	/**
	 * [20160115][ymjang] 각 ECO 별로 처리 로그에 처리 결과 갱신
	 * @param log
	 * @param session
	 * @param eco_no
	 * @return
	 * @throws Exception
	 */
	private int updateJobTransLog(StringBuffer log, List<HashMap<String, String>> ecoList, ArrayList<WeldPointGroupVO> targetList) throws Exception {
		TcCommonDao commonDao = TcCommonDao.getTcCommonDao();

		String curTime = getCurrentTime();

		String trans_flag = null;
		String trans_msg = null;
		boolean isFound = false;

		try {

			HashMap<String, String> tmpMap = new HashMap<String, String>();
			for( HashMap<String, String> ecoMap : ecoList){
				String ecoNo = ecoMap.get("ECO_NO");
				addLog("**********************************Job Trans Log 테이블에 ECO 정보 Update 시작 ********************************************************************************");
				addLog("Job Trans Log 테이블에 ECO 정보 Update    : " + ecoNo);
				isFound = false;
				for(int i = 0; i < targetList.size(); i++ ){
					WeldPointGroupVO vo = targetList.get(i);
					if (ecoNo.equals(vo.getEcoNo()))
						isFound = true;
				}

				trans_flag = (isFound ? "E" : "P");
				trans_msg = (isFound ? "End" : "Pass");
				
				if(!hasTransTarget) {
					trans_msg =  trans_msg + "    Trans Target Count : " +  countTranstarget;
				} 

				tmpMap.clear();
				tmpMap.put("END_JOB_TIME", curTime);
				tmpMap.put("TRNAS_FLAG", trans_flag);
				tmpMap.put("TRNAS_MSG", trans_msg);
				tmpMap.put("TARGET_ID", ecoNo);

				commonDao.update("com.symc.weld.updateJobTransLog", tmpMap);
				
				
			}

		} catch(Exception e) {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
	        PrintStream pinrtStream = new PrintStream(out);
	        e.printStackTrace(pinrtStream);
			addLog("******************************************************************************************************************");
			addLog("ECO 별로 처리 로그 갱신 에러    : " + out.toString() + "\n");
			addLog("******************************************************************************************************************");
			
			throw e;
		}

		return 0;
	}
	
	
	/**
	 * [용점접 2차 개선안] [20150907][ymjang] 용접점 처리시 각 대상 Part 별로 상태를 기록하기 위해 상태 테이블에 데이터를 생성한다.
	 * @throws Exception
	 */
	public void updateWeldPointTransLog(HashMap<String, Object> optionMap, String transFlag, String transMsg) throws Exception{
		
		try{			

//			addLog("======== updateWeldPointTransLog 실행 ========");
//			addLog("transFlag =" + transFlag );
			TcCommonDao commonDao = TcCommonDao.getTcCommonDao();
			
			String ecoNo = (String)optionMap.get("ECO_NO");
			String fmpId = (String)optionMap.get("FMP_ID");
			String projectCode = (String)optionMap.get("PROJECT_CODE");
			String changeType = (String)optionMap.get("CT");
			String eplId = (String)optionMap.get("EPL_ID");
			String parentNo = (String)optionMap.get("PARENT_NO");
			String parentRev = (String)optionMap.get("PARENT_REV");
			String partNo = (String)optionMap.get("NEW_PART_NO");
			String partRev = (String)optionMap.get("NEW_PART_REV");

			HashMap<String, String> ds = new DataSet();
			ds.put("ECO_NO", ecoNo);
			ds.put("FMP_ID", fmpId);
			ds.put("PROJECT_CODE", projectCode);
			ds.put("CHANGE_TYPE", changeType);
			ds.put("EPL_ID", eplId);
			ds.put("PARENT_NO", parentNo);
			ds.put("PARENT_REVISION_ID", parentRev);
			ds.put("PART_NO", partNo);
			ds.put("PART_REVISION_ID", partRev);
			ds.put("TRANS_FLAG", transFlag);
			ds.put("TRANS_MSG", transMsg);
			
//			String servletUrlStr = prop.getProperty("servlet.url");
			commonDao.update("com.symc.weld.updateWeldPointSuccessLog", ds);
		
		}catch(Exception ex){
			ByteArrayOutputStream out = new ByteArrayOutputStream();
	        PrintStream pinrtStream = new PrintStream(out);
	        ex.printStackTrace(pinrtStream);
	        addLog("******************************************************************************************************************");
			addLog("용접점 아이템별 로그 갱신시 에러 발생    : " + out.toString());
			addLog("******************************************************************************************************************");
			throw ex;
		}finally{
			
		}
		
	}
	
	
	public  void printLog(String dirPath, String fileName, String logContents) throws IOException {

	    File zLogFile = new File( dirPath + "\\"+ fileName +"_"+ getCreateLogFileTime() +".txt" );			
	    BufferedWriter out = new BufferedWriter(new FileWriter(zLogFile));                                    
	    out.write(logContents);	      
	    out.close();		
	}  
	
	
	/**
     * 현재 년월일시분초 값을 가져오는 함수
     */
    public String getCreateLogFileTime() {
        SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat ( "yyyyMMddHHmmss", Locale.KOREA );
        Date currentTime = new Date(); 
        String mTime = mSimpleDateFormat.format(currentTime);
        return mTime;
    }
    
    
    /**
	 * Log File을 Update 한다.
	 * @param msg
	 */
	public void addLog(String msg){
		
		buffer.append(msg);
		buffer.append("\r\n");
	}
	
	
	///////////////////////////////////////////////////Test 메서드 //////////////////////////////////////////////////////////////////////////////
	
//	@org.junit.Test
//	public void WeldPointTest() throws Exception {
//		Session session = null;
//		TcLoginService tcLoginService = new TcLoginService();
//		try {
//			try {
//
//				session = tcLoginService.getTcSession();
//
//				// BYPASS
//				TcSessionUtil sessionUtil = new TcSessionUtil(session);
//				sessionUtil.setByPass();
//				//				WeldPointGroupVO vo = new WeldPointGroupVO();
//				//				vo.setChangeType("R0");
//				//				vo.setFmpId("M605XA2018A");
//				//				vo.setFmpRevId("001");
//				//				vo.setItemId("5511035020");
//				//				vo.setItemRevId("003");
//				//				createDispatcherRequest(vo, session);
//			} catch (Exception e) {
//				e.printStackTrace();
//				throw e;
//			}
//			// session = tcLoginService.getTcSession();
//			BOMLineService bomLineService = new BOMLineService(session);
//			TcItemUtil tcItemUtil = new TcItemUtil(session);
//			TcStructureManagementService structureSVC = new TcStructureManagementService(
//					session);
//			TcQueryService tcQueryService = new TcQueryService(session);
//			TcDataManagementService dmService = new TcDataManagementService(
//					session);
//
//			createWeldPointGroupTest(session, bomLineService,
//					tcItemUtil, structureSVC, tcQueryService, dmService);
//		} catch (Exception e) {
//			throw e;
//		} finally {
//			if (session != null) {
//				session.logout();
//			}
//
//		}
//	}
	
//	@SuppressWarnings("unused")
//	private void setWeldGroupMap(HashMap<String, Object> epl
//			, HashMap<String, HashMap<String, Object>> changedWeldGroupMap
//			, HashMap<String, HashMap<String, Object>> removedWeldGroupMap){
//
//		String funcs = (String)epl.get("FUNC");
//		String newPartNo = (String)epl.get("NEW_PART_NO");
//		String changeType = (String)epl.get("CT");
//		String eplID = (String)epl.get("EPL_ID");
//		//      F006 function에 포함된 경우는 Supply Mode는 무의미함.
//		//      50150, 52001, 52002, 50600, 57000로 시작되는 아이디에 한해서 용접점 생성.
//		if( funcs != null && funcs.indexOf("F006") > -1){
//			if( newPartNo.indexOf("50150") > -1 || newPartNo.indexOf("52001") > -1
//					|| newPartNo.indexOf("52002") > -1 || newPartNo.indexOf("50600") > -1 || newPartNo.indexOf("57000") > -1 ){
//				//삭제된 WeldGroup은 상위FMP를 Revise 후 해당 WeldGroup을 제거하고 Release함.
//				if( changeType.equals("D")){
//					if( !removedWeldGroupMap.containsKey(eplID)){
//						removedWeldGroupMap.put(eplID, epl);
//					}
//				}else{
//					if( !changedWeldGroupMap.containsKey(eplID)){
//						changedWeldGroupMap.put(eplID, epl);
//					}
//				}
//			}
//
//		}else if(funcs != null && ( funcs.indexOf("F610") > -1 || funcs.indexOf("F620") > -1
//				|| funcs.indexOf("F630") > -1 || funcs.indexOf("F640") > -1 || funcs.indexOf("F650") > -1
//				|| funcs.indexOf("F660") > -1 || funcs.indexOf("F720") > -1 || funcs.indexOf("F730") > -1
//				|| funcs.indexOf("F740") > -1 || funcs.indexOf("F750") > -1)){
//			String supplyMode = (String)epl.get("NEW_SMODE");
//			if( supplyMode != null && supplyMode.toUpperCase().equals("P0")){
//				//삭제된 WeldGroup은 상위FMP를 Revise 후 해당 WeldGroup을 제거하고 Release함.
//				if( changeType.equals("D")){
//					if( !removedWeldGroupMap.containsKey(eplID)){
//						removedWeldGroupMap.put(eplID, epl);
//					}
//				}else{
//					if( !changedWeldGroupMap.containsKey(eplID)){
//						changedWeldGroupMap.put(eplID, epl);
//					}
//				}
//			}
//		}
//	}
//
//	private void createWeldPointGroupTest(Session session,
//			BOMLineService bomLineService, TcItemUtil tcItemUtil,
//			TcStructureManagementService structureSVC,
//			TcQueryService tcQueryService, TcDataManagementService dmService) throws Exception {
//
//		String queryName = TcConstants.SEARCH_ITEM_REVISION;
//		String[] entries = { "Item ID", "Revision" };
//		String[] values = { "F605XA2015", "000" };
//		String[] properties = { TcConstants.PROP_ITEM_ID, "item_revision_id",
//				"date_released", "object_desc", "object_type", "date_released"
//		/*,"structure_revisions"*/ };
//
//		ModelObject[] modelObject = tcQueryService.searchTcObject(queryName,
//				entries, values, properties);
//
//		ItemRevision parentRevision = null;
//		if (modelObject != null && modelObject.length > 0) {
//			parentRevision = (ItemRevision) modelObject[0];
//		}
//
//		Calendar cal = Calendar.getInstance();
//		cal.set(Calendar.YEAR, 2019);
//		cal.set(Calendar.MONTH, Calendar.APRIL);
//		cal.set(Calendar.DAY_OF_MONTH, 2);
//		BOMLineService bomlineService = new BOMLineService(session);
//		BOMWindow window = bomlineService.getCreateBOMWindow(parentRevision, "Latest Released", cal.getTime());
//		dmService.getProperties(new ModelObject[]{window}, new String[]{"top_line"});
//		BOMLine topLine = (BOMLine)window.get_top_line();
//		dmService.getProperties(new ModelObject[]{topLine}, new String[]{"bl_child_lines"});
//		ModelObject[] fmpObj = topLine.get_bl_child_lines();
//		BOMLine fmpLine = (BOMLine)fmpObj[0];
//		dmService.getProperties(new ModelObject[]{fmpLine}, new String[]{"bl_child_lines","bl_revision"});
//		ModelObject[] weldGroupObj = fmpLine.get_bl_child_lines();
//		ItemRevision fmpRevision = (ItemRevision)fmpLine.get_bl_revision();
//		tcItemUtil.getProperties(new ModelObject[]{fmpRevision}, new String[]{"structure_revisions"});
//		PSBOMViewRevision[] bomViewRevision = fmpRevision.get_structure_revisions();
//		if( bomViewRevision != null){
//
//			ReservationService rService = ReservationService.getService(session.getConnection());
//			try{
//				rService.checkout(bomViewRevision, "", null);
//
//				BOMLine[] bomlines = new BOMLine[weldGroupObj.length];
//				System.arraycopy(weldGroupObj, 0, bomlines, 0, weldGroupObj.length);
//
//				setMvlConditionTest(session, "F605XA2015", "F630XA2015:S13 = \"S13X\"", window, bomlines[0]);
//			}catch(Exception e){
//				throw e;
//			}finally{
//				rService.checkin(bomViewRevision);
//				save(session.getConnection(), bomViewRevision[0]);
//			}
//
//		}
//
//		System.out.println("ggggg");
//	}
//	
//	/**
//	 * 용접점 그룹이 추가 또는 삭제 된 경우,
//	 * Dispatcher에서 M605 FMP를 Revise해야 함.
//	 * ECO와 Product 단위로 한번만 실행되어야함.
//	 *
//	 * @param weldFmpId
//	 * @param weldFmpRevId
//	 * @param changedWeldGroupMap
//	 * @param removedWeldGroupMap
//	 * @return
//	 * @throws Exception
//	 */
//	@SuppressWarnings({ "unused", "unchecked" })
//	private String getFmpRevIdToApply(String weldFmpId, String weldFmpRevId
//			,  HashMap<String, HashMap<String, Object>> changedWeldGroupMap
//			,  HashMap<String, HashMap<String, Object>> removedWeldGroupMap
//			,  HashMap<String, ArrayList<String>> revisionToReviseMap) throws Exception{
//
//		String newFmpRevID = weldFmpRevId;
//
//		//삭제가 하나라도 존재할 경우 M605 FMP Revise Up.
//		if( !removedWeldGroupMap.isEmpty()){
//			return getNextRevisionID(weldFmpId, weldFmpRevId, revisionToReviseMap);
//		}
//
//		Collection<HashMap<String, Object>> col = changedWeldGroupMap.values();
//		Iterator<HashMap<String, Object>> its = col.iterator();
//
//		TcCommonDao commonDao = TcCommonDao.getTcCommonDao();
//
//		//추가된 용접점 그룹인지 확인
//		//Condition도 함께 비교.
//		while(its.hasNext()){
//
//			HashMap<String, Object> map = its.next();
//
//			HashMap<String, String> tmpMap = new HashMap<String, String>();
//			tmpMap.put("parent_id", weldFmpId);
//			tmpMap.put("parent_rev_id", weldFmpRevId);
//			tmpMap.put("child_id", (String)map.get("NEW_PART_NO") + "-WeldGroup");
//			//			tmpMap.put("child_rev_id", (String)map.get("NEW_PART_REV"));
//			String condition = clobToString((java.sql.Clob)map.get("NEW_VC"));
//			tmpMap.put("condition", (condition == null ? null:condition.trim()));
//			ArrayList<HashMap<String, String>> children = (ArrayList<HashMap<String, String>>)commonDao.selectList("com.symc.weld.getChildren", tmpMap);
//			if( children == null || children.isEmpty() ){
//				return getNextRevisionID(weldFmpId, weldFmpRevId, revisionToReviseMap);
//			}
//		}
//
//
//		return newFmpRevID;
//	}
//	
//	@SuppressWarnings("unused")
//	private void addTarget(ArrayList<WeldPointGroupVO> targetList, HashMap<String, HashMap<String, Object>> weldGroupMap
//			, String ecoNO, String weldFmpId, String projectCode) throws Exception{
//		Collection<HashMap<String, Object>> col = weldGroupMap.values();
//		Iterator<HashMap<String, Object>> its = col.iterator();
//		while(its.hasNext()){
//			HashMap<String, Object> map = its.next();
//			String changeType = (String)map.get("CT");
//			String eplId = (String)map.get("EPL_ID");
//			WeldPointGroupVO  vo = new WeldPointGroupVO();
//			vo.setEcoNo(ecoNO);
//			vo.setFmpId(weldFmpId);
//			vo.setProjectCode(projectCode);
//			vo.setChangeType(changeType);
//			vo.setEplId(eplId);
//
//			if( changeType.equals("D")){
//				vo.setItemId((String)map.get("OLD_PART_NO"));
//				vo.setItemRevId((String)map.get("OLD_PART_REV"));
//			}else{
//				vo.setItemId((String)map.get("NEW_PART_NO"));
//				vo.setItemRevId((String)map.get("NEW_PART_REV"));
//			}
//			targetList.add(vo);
//		}
//
//	}
//	
//	@SuppressWarnings("unused")
//	private Item getItem(Connection connection, String itemID) throws Exception {
//		String queryName = "Item...";
//		String[] entries = { "Item ID" };
//		String[] values = { itemID };
//		QueryInput[] queryInput = new QueryInput[1];
//		queryInput[0] = new QueryInput();
//		queryInput[0].clientId= "site";
//		queryInput[0].query = getQueryObject(connection, queryName);
//		queryInput[0].resultsType=2;
//		queryInput[0].entries = entries;
//		queryInput[0].values = values;
//
//		SavedQueriesResponse executeSavedQueries = SavedQueryService.getService(connection).executeSavedQueries(queryInput);
//		if(executeSavedQueries.serviceData.sizeOfPartialErrors() < 1)
//		{
//			QueryResults[] queryresults = executeSavedQueries.arrayOfResults ;
//			String[] uids = queryresults[0].objectUIDS;
//			if (uids == null || uids.length == 0) {
//				return null;
//			}
//			return (Item)SoaHelper.getModelObject(uids)[0];
//		}
//
//		return null;
//	}
	
//	@SuppressWarnings("unused")
//	private int setMarpoint(Connection connection) throws Exception{
//
//		ICTService service = ICTService.getService(connection);
//		Arg[] args_ = new Arg[1];
//		args_[0] = TcUtility.createArg(new String[0]);
//		InvokeICTMethodResponse response = service.invokeICTMethod("ICCTSession", "placeMarkpoint", args_);
//		if( response.serviceData.sizeOfPartialErrors() > 0)
//		{
//			throw new ICCTException( response.serviceData);
//		}
//		int argValue_ =0;
//		return (int) TcUtility.queryArg(response.output[0], argValue_);
//
//
//		//		ICCTSession icctSession = new ICCTSession(connection);
//		////		Arg[] args = new Arg[1];
//		////		args[0] = TcUtility.createArg(type.getName());
//		////		icctSession.invokeICTMethod("ICCT", "save", args);
//		//		int id = icctSession.placeMarkpoint(new String[0]);
//		//
//		//		icctSession.forgetMarkpoint(id)
//
//		//		return id;
//	}
//
//	@SuppressWarnings("unused")
//	private void forgotMarkpoint(Connection connection, int markpoint) throws ICCTException, ServiceException{
//
//		ICTService service = ICTService.getService(connection);
//		Arg[] args_ = new Arg[1];
//		args_[0] = TcUtility.createArg(markpoint);
//		InvokeICTMethodResponse response = service.invokeICTMethod("ICCTSession", "forgetMarkpoint", args_);
//		if( response.serviceData.sizeOfPartialErrors() > 0)
//		{
//			throw new ICCTException( response.serviceData);
//		}
//	}
//	
//	@SuppressWarnings("unused")
//	public boolean rollToMarkpoint(Connection connection, int markpoint) throws Exception {
//
//		ICTService service = ICTService.getService(connection);
//		Arg[] args_ = new Arg[1];
//		args_[0] = TcUtility.createArg(markpoint);
//		InvokeICTMethodResponse response = service.invokeICTMethod("ICCTSession", "rollToMarkpoint", args_);
//		if( response.serviceData.sizeOfPartialErrors() > 0)
//		{
//			throw new ICCTException( response.serviceData);
//		}
//		boolean argValue_ =false;
//		return (boolean) TcUtility.queryArg(response.output[0], argValue_);
//	}
	
//	private static void setMvlConditionTest(Session session,
//			String functionId, String condition, BOMWindow window,
//			BOMLine targetLine) throws Exception {
//
//		HashMap<String, String> corpOptionMap = null;
//		HashMap<String, String> optionDescMap = new HashMap<String, String>();
//
//		Type windowType = window.getTypeObject();
//		BOMLine topLine = (BOMLine) window.get_top_line();
//		ICTService service = ICTService.getService(session.getConnection());
//		String result = "";
//		Pattern p = Pattern.compile("F\\w*:");
//		Matcher m = p.matcher(condition);
//		while (m.find()) {
//			System.out.println(m.start() + " " + m.group());
//			result = m.replaceAll(functionId + ":");
//			System.out.println("result : " + result);
//		}
//
//		p = Pattern.compile(":\\w{3}\\s");
//		m = p.matcher(condition);
//		while (m.find()) {
//			String optionName = m.group().substring(1).trim();
//			System.out.println(m.start() + " " + optionName);
//			if (!optionDescMap.containsKey(optionName)) {
//				// Function Option 정의
//				if (corpOptionMap == null) {
//					corpOptionMap = getCorpOptionTest(session);
//				}
//
//				String uid = topLine.getUid();
//				String desc = corpOptionMap.get(optionName);
//
//				// Function에 옵션 정의
//				Arg[] args = new Arg[2];
//				args[0] = TcUtility.createArg(uid);
//				args[1] = TcUtility.createArg("public " + optionName
//						+ " uses  \"" + desc + "\" 'CorporateOption-001':"
//						+ optionName);
//				service.invokeICTMethod("ICCTVariantService",
//						"lineDefineOption", args);
//				System.out.println("Option Definition complete");
//				// Window Save
//				args = new Arg[3];
//				args[0] = TcUtility.createArg( windowType.getName() );
//				args[1] = TcUtility.createArg( windowType.getUid() );
//				args[2] = TcUtility.createArg( window.getUid() );
//				service.invokeICTMethod("ICCT", "save", args);
//				System.out.println("Option Save");
//				// Window Refresh
//				args = new Arg[4];
//				args[0] = TcUtility.createArg( windowType.getName() );
//				args[1] = TcUtility.createArg( windowType.getUid() );
//				args[2] = TcUtility.createArg( window.getUid() );
//				args[3] = TcUtility.createArg(0);
//				service.invokeICTMethod("ICCT", "refresh", args);
//				System.out.println("BOMLine Refresh");
//				optionDescMap.put(optionName, corpOptionMap.get(optionName));
//			}else{
//				System.out.println("optionDescMap.containsKey(" + optionName + ") : " + optionDescMap.containsKey(optionName));
//			}
//		}
//
//		// Condition 셋팅.
//		String uid = targetLine.getUid();
//		Arg[] args = new Arg[2];
//		args[0] = TcUtility.createArg(uid);
//		args[1] = TcUtility.createArg(result);
//		service.invokeICTMethod("ICCTVariantService", "setLineMvlCondition",
//				args);
//
//		System.out.println("setLineMvlCondition ");
//	}
//
//	@SuppressWarnings({ "unused", "rawtypes", "unchecked" })
//	private void setReferencePropertyTest(Session session,
//			BOMLineService bomLineService, TcItemUtil tcItemUtil,
//			TcStructureManagementService structureSVC,
//			TcQueryService tcQueryService, TcDataManagementService dmService) throws Exception{
//		String queryName = TcConstants.SEARCH_ITEM_REVISION;
//		String[] entries = { "Item ID", "Revision" };
//		String[] values = { "M605XA2015A", "000" };
//		String[] properties = { TcConstants.PROP_ITEM_ID, "item_revision_id",
//				"date_released", "object_desc", "object_type", "date_released"
//		/*,"structure_revisions"*/ };
//
//		ModelObject[] modelObject = tcQueryService.searchTcObject(queryName,
//				entries, values, properties);
//
//		ItemRevision fmpRevision = null;
//		ItemRevision ecoRevision = null;
//		if (modelObject != null && modelObject.length > 0) {
//			fmpRevision = (ItemRevision) modelObject[0];
//		}
//
//		values = new String[] { "35BD105", "000" };
//		modelObject = tcQueryService.searchTcObject(queryName,
//				entries, values, properties);
//		ecoRevision = (ItemRevision) modelObject[0];
//		Calendar ecoReleaseDate = ecoRevision.get_date_released();
//		HashMap tmpMap = new HashMap();
//		tmpMap.put("s7_ECO_NO", ecoRevision.getUid());
//		com.teamcenter.services.loose.core._2010_09.DataManagement.PropInfo prop[] = new com.teamcenter.services.loose.core._2010_09.DataManagement.PropInfo[1];
//		prop[0] = new com.teamcenter.services.loose.core._2010_09.DataManagement.PropInfo();
//		prop[0].object = fmpRevision;
//		prop[0].timestamp = Calendar.getInstance();
//		com.teamcenter.services.loose.core._2010_09.DataManagement.NameValueStruct1 nvs = new com.teamcenter.services.loose.core._2010_09.DataManagement.NameValueStruct1();
//		nvs.name = "s7_ECO_NO";
//		nvs.values = new String[]{ecoRevision.getUid()};
//		prop[0].vecNameVal = new com.teamcenter.services.loose.core._2010_09.DataManagement.NameValueStruct1[]{nvs};
//		DataManagementService.getService(session.getConnection()).setProperties(prop, new String[]{"ENABLE_PSE_BULLETIN_BOARD"});
//
//	}
//
//	@SuppressWarnings("unused")
//	private void createProcessTest(Connection connection, ArrayList<ModelObject> targetToReleaseList, Calendar ecoReleaseDate, TcItemUtil tcItemUtil) throws NotLoadedException, Exception{
//		InstanceInfo instanceInfo = createNewProcess(connection, targetToReleaseList.toArray(new ModelObject[targetToReleaseList.size()]), "Weld Point Auto Release", "WSR");
//		if( instanceInfo.serviceData.sizeOfUpdatedObjects() > 0){
//			for( int i = 0; i < instanceInfo.serviceData.sizeOfUpdatedObjects(); i++ ){
//				ModelObject updatedObject = instanceInfo.serviceData.getUpdatedObject(i);
//				if( updatedObject instanceof EPMTask){
//					EPMTask task = (EPMTask)updatedObject;
//					tcItemUtil.getProperties(new ModelObject[]{task}, new String[]{"release_status_attachments","release_status_list","release_statuses"});
//					ModelObject[] releaseStatusAtt = task.get_release_status_attachments();
//					ReleaseStatus releaseStatus = (ReleaseStatus)releaseStatusAtt[0];
//					changeEffectivityDate(releaseStatus, connection, ecoReleaseDate, tcItemUtil);
//					changeReleaseDate(releaseStatus, connection, ecoReleaseDate);
//
//					break;
//				}
//				System.out.println("ggggg");
//			}
//		}
//	}
//
//	@SuppressWarnings({ "unused", "unchecked", "rawtypes" })
//	private void weldPointAddTest(Connection connection, ItemRevision parentRevision, ItemRevision childRevision) throws NotLoadedException{
//		ReservationService rService = ReservationService.getService(connection);
//		PSBOMViewRevision[] bomViewRevision = parentRevision.get_structure_revisions();
//		if (bomViewRevision != null && bomViewRevision.length > 0) {
//			rService.checkout(bomViewRevision, "", null);
//		}
//
//		CreateOrUpdateRelativeStructureInfo2 createOrUpdateRelativeStructureInfo2 = new CreateOrUpdateRelativeStructureInfo2();
//		createOrUpdateRelativeStructureInfo2.parent = parentRevision;
//		createOrUpdateRelativeStructureInfo2.precise = false;
//
//		createOrUpdateRelativeStructureInfo2.childInfo = new RelativeStructureChildInfo[1];
//		createOrUpdateRelativeStructureInfo2.childInfo[0] = new RelativeStructureChildInfo();
//		createOrUpdateRelativeStructureInfo2.childInfo[0].child = childRevision;
//		CreateOrUpdateRelativeStructurePref2 pref2 = new CreateOrUpdateRelativeStructurePref2();
//		ArrayList<AttributesInfo> attrList = new ArrayList();
//		AttributesInfo attr = new AttributesInfo();
//		attr.name = "bl_plmxml_occ_xform";
//		attr.value = "1 0 0 0 0 1 0 0 0 0 1 0 " + "0.251" + " " + "0.321" + " " + "-0.478" + " 1";
//		attrList.add(attr);
//		attr = new AttributesInfo();
//		attr.name = "bl_occurrence_name";
//		attr.value = "TEST";
//		attrList.add(attr);
//		attr = new AttributesInfo();
//		//		attr.name = "Mfg0number_of_sheets_welded";
//		//		attr.value = "3";
//		//		attrList.add(attr);
//		createOrUpdateRelativeStructureInfo2.childInfo[0].occInfo.attrsToSet = attrList.toArray(new AttributesInfo[attrList.size()]);
//
//		StructureManagementRestBindingStub smrms = new StructureManagementRestBindingStub(connection);
//		try{
//			CreateOrUpdateRelativeStructureResponse response = smrms
//					.createOrUpdateRelativeStructure(
//							new CreateOrUpdateRelativeStructureInfo2[] { createOrUpdateRelativeStructureInfo2 },
//							"view", false, pref2);
//
//			if( response.serviceData.sizeOfCreatedObjects() > 0){
//				String occPuid = response.serviceData.getCreatedObject(0).getUid();
//				return ;
//			}
//
//		}catch(Exception e){
//			e.printStackTrace();
//		}finally{
//			if (bomViewRevision != null && bomViewRevision.length > 0) {
//				rService.checkin(bomViewRevision);
//			}
//		}
//	}
	
	///////////////////////////////////////////////////Test 메서드 //////////////////////////////////////////////////////////////////////////////

}

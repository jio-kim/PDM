package com.symc.work.service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import com.ssangyong.commands.ospec.op.OSpec;
import com.ssangyong.commands.ospec.op.OpTrim;
import com.ssangyong.commands.ospec.op.OpUtil;
import com.ssangyong.commands.ospec.op.OpValueName;
import com.ssangyong.commands.ospec.op.Option;
import com.ssangyong.commands.ospec.op.StoredOptionSet;
import com.ssangyong.common.remote.DataSet;
import com.symc.common.dao.TcCommonDao;
import com.symc.common.soa.biz.Session;
import com.symc.common.soa.biz.TcDatasetUtil;
import com.symc.common.soa.biz.TcFileUtil;
import com.symc.common.soa.biz.TcItemUtil;
import com.symc.common.soa.biz.TcSessionUtil;
import com.symc.common.soa.service.TcDataManagementService;
import com.symc.common.soa.service.TcPreferenceManagementService;
import com.symc.common.soa.service.TcServiceManager;
import com.symc.common.soa.service.TcStructureManagementService;
import com.symc.common.soa.util.TcConstants;
import com.symc.common.util.IFConstants;
import com.symc.work.model.KeyLOV;
import com.teamcenter.services.strong.administration._2012_09.PreferenceManagement.CompletePreference;
import com.teamcenter.services.strong.administration._2012_09.PreferenceManagement.GetPreferencesResponse;
import com.teamcenter.services.strong.cad._2007_01.StructureManagement.CreateBOMWindowsResponse;
import com.teamcenter.soa.client.model.ModelObject;
import com.teamcenter.soa.client.model.Property;
import com.teamcenter.soa.client.model.strong.BOMLine;
import com.teamcenter.soa.client.model.strong.BOMWindow;
import com.teamcenter.soa.client.model.strong.Dataset;
import com.teamcenter.soa.client.model.strong.ImanFile;
import com.teamcenter.soa.client.model.strong.Item;
import com.teamcenter.soa.client.model.strong.ItemRevision;
import com.teamcenter.soa.client.model.strong.ReleaseStatus;
import com.teamcenter.soa.client.model.strong.User;

/**
 * [20160414][ymjang] PA6 User 속성이 아닌 Person 속성 --> PA6 값 읽기 오류 개선 
 * [20160425][ymjang] S7_CHG_CD PreVehclePart Revision 속성 정의 오류 수정 --> bomline 속성으로 변경함.
 * [20160425][ymjang] 오류 발생시 관리자 메일 발송 기능 추가
 * [20160525][ymjang] Runnable --> Callable 방식으로 변경함.
 * [20160523][ymjang] 처리 로그 작성 기능 추가
 * [20160606][ymjang] 메일 발송 방식 개선 (through EAI)
 * [20160608][ymjang] usage 계산 오류 수정
 * [20160629][ymjang] FullBOM Master List 출력을 위하여 Level M, QTY, SORT_ORDER, VC 컬럼 추가 
 * [20160718][ymjang] 최종 Release 된 리비전 찾기 오류 수정
 * [20170517][chc] OSPEC 정보 중 E00, E10이 모두 존재하지 않을 경우 처리 로직 추가 : DCES 시스템과의 연계 부분 확인해야 함. --> C300, A200, G15DTF 만 선별적으로 처리하는 중
 * [SR170706-008][LJG] Proto Tooling 컬럼 추가
 * [20180213][ljg] 시스템 코드 리비전 정보에서 bomline정보로 이동
 * [20180307][csh] OSPEC NO 추가 및 chgtype에 다른 dcs info 연결로직 변경
 * [SR180713][20180716][CSH] Vehpart는 실중량을 우선으로.. 없으면 예상중량값을 예상중량값에 표시, stdpart는 실중량을 예상중량에 표시
 * [SR181206-041][20181206][KCH] SYSTEM_ROW_KEY 가져오는 값이 가끔 누락되는 현상이 있어 TC API -> DB Query 로 변경
 */
public class FullPreProductNotInterfaceService {
	private Session tcSession;
	private TcItemUtil tcItemUtil;
	private TcDatasetUtil datasetUtil;
	private TcFileUtil fileUtil;
	private TcStructureManagementService strService;
	private TcDataManagementService dataService;
	private TcQueryService queryService;
	private HashMap<String, String> systemCodeMap;
	private HashMap<String, OpValueName> wtMap;
	private HashMap<String, OpValueName> tmMap;
	private ScriptEngineManager manager = new ScriptEngineManager();
	private ScriptEngine engine = manager.getEngineByName("js");
	private SimpleDateFormat DATE_FORMAT_MM = new SimpleDateFormat("yyyyMMddhhmmss");

	public static String[] parentRevProperties = 
		{
		TcConstants.PROP_ITEM_ID,
		TcConstants.PROP_S7_DISPLAYPARTNO,
		TcConstants.PROP_OBJECT_TYPE,
		TcConstants.PROP_ITEM_REVISION_ID,
		TcConstants.PROP_OBJECT_NAME
		};
	public static String[] bomlineProperties =
		{
		TcConstants.PROP_BL_ITEM_ID,               //0
		TcConstants.PROP_BL_SEQUENCE_NO,       //1
		TcConstants.PROP_BL_OBJECT_TYPE,       //2
		TcConstants.PROP_BL_REV_OBJECT_TYPE,   //3
		TcConstants.PROP_BL_QUANTITY,          //4
		TcConstants.PROP_BL_VARIANT_CONDITION, //5
		TcConstants.PROP_BL_OCC_FND_OBJECT_ID, //6
		TcConstants.PROP_BL_ITEM_REVISION_ID,  //7
		TcConstants.PROP_BL_ABS_OCC_ID,        //8


		TcConstants.PROP_BL_MODULE_CODE,         //9
		TcConstants.PROP_BL_SUPPLY_MODE,         //10

		TcConstants.PROP_BL_REQ_OPT,             //11  REQ OPT
		TcConstants.PROP_BL_SPEC_DESC,           //12
		TcConstants.PROP_BL_CHG_CD,              //13     
		TcConstants.PROP_BL_PRE_ALTER_PART,          //14
		TcConstants.PROP_BL_LEV_M,                //15  LEV M
		//[180117][LJG] bom level 추가
		TcConstants.PROP_BL_LEVEL,
		TcConstants.PROP_BL_SYSTEM_ROW_KEY,		 // 16

		/* [SR없음][20150914][jclee] DVP Sample 속성 BOMLine으로 이동*/
		TcConstants.PROP_S7_BL_DVP_NEEDED_QTY,         // 17
		TcConstants.PROP_S7_BL_DVP_USE,                // 18
		TcConstants.PROP_S7_BL_DVP_REQ_DEPT,           // 19

		/* [SR없음][20160317][jclee] Design User, Dept 속성 BOMLine으로 이동 */
		TcConstants.PROP_S7_BL_ENG_DEPT_NM,	// 20
		TcConstants.PROP_S7_BL_ENG_RESPONSIBLITY,	// 21

		//[SR170706-008][LJG] Proto Tooling 컬럼 추가 /22
		TcConstants.PROP_BL_PROTO_TOOLING,
		
		 //[20180213][ljg] 시스템 코드 리비전 정보에서 bomline정보로 이동
        TcConstants.PROP_BL_BUDGET_CODE,   //23

        //20201021 seho EJS Column 추가.
        TcConstants.PROP_BL_EJS,
        
        //20210104 WEIGHT_MANAGEMENT Column 추가 by 전성용.
        TcConstants.PROP_BL_WEIGHT_MANAGEMENT
		};

	public static String[] revisionProperties =
		{
		TcConstants.PROP_S7_PROJCODE,               // 0
		//[20180213][ljg] 시스템 코드 리비전 정보에서 bomline정보로 이동
		//TcConstants.PROP_S7_BUDGETCODE,             // 1
		TcConstants.PROP_S7_COLORID,                // 2
		TcConstants.PROP_S7_ESTWEIGHT,              // 3     ESTIMATE WEIGHT
		TcConstants.PROP_S7_CALWEIGHT,              // 4
		TcConstants.PROP_S7_TARGET_WEIGHT,          // 5     TARGET WEIGHT
		TcConstants.PROP_S7_CONTENTS,               // 6
		TcConstants.PROP_S7_CHG_TYPE_NM,            // 7
		//            TcConstants.PROP_S7_ORIGIN_PROJECT,        // 8 번째
		TcConstants.PROP_S7_CON_DWG_PLAN,           // 8
		TcConstants.PROP_S7_CON_DWG_PERFORMANCE,    // 9
		TcConstants.PROP_S7_CON_DWG_TYPE,           // 10
		TcConstants.PROP_S7_DWG_DEPLOYABLE_DATE,    // 11
		TcConstants.PROP_S7_PRD_DWG_PERFORMANCE,    // 12
		TcConstants.PROP_S7_PRD_DWG_PLAN,           // 13

		/* [SR없음][20150914][jclee] DVP Sample 속성 BOMLine으로 이동*/
		//            TcConstants.PROP_S7_DVP_NEEDED_QTY,         // 14
		//            TcConstants.PROP_S7_DVP_USE,                // 15
		//            TcConstants.PROP_S7_DVP_REQ_DEPT,           // 16
		"",									          // 14
		"",                							  // 15
		"",           								  // 16

		/* [SR없음][20160317][jclee] Design User, Dept 속성 BOMLine으로 이동 */
		//            TcConstants.PROP_S7_ENG_DEPT_NM,            // 17
		//            TcConstants.PROP_S7_ENG_RESPONSIBLITY,      // 18
		"",		// 17
		"",		// 18

		//            TcConstants.PROP_S7_CIC_DEPT_NM,             // 19 번째
		TcConstants.PROP_S7_EST_COST_MATERIAL,      // 19
		TcConstants.PROP_S7_TARGET_COST_MATERIAL,   // 20
		TcConstants.PROP_S7_SELECTED_COMPANY,       // 21
		TcConstants.PROP_S7_PRT_TOOLG_INVESTMENT,   // 22
		TcConstants.PROP_S7_PRD_TOOL_COST,          // 23
		TcConstants.PROP_S7_PRD_SERVICE_COST,       // 24
		TcConstants.PROP_S7_PRD_SAMPLE_COST,        // 25
		TcConstants.PROP_S7_PUR_DEPT_NM,            // 26
		TcConstants.PROP_S7_PUR_RESPONSIBILITY,     // 27
		//            TcConstants.PROP_S7_EMPLOYEE_NO,            // 28
		TcConstants.PROP_S7_CHANGE_DESCRIPTION,     // 29
		TcConstants.PROP_S7_SELECTIVEPART,          // 30
		TcConstants.PROP_S7_DR,                     // 31
		TcConstants.PROP_S7_OLD_PART_NO,            // 32
		TcConstants.PROP_S7_BOX,                    // 33
		TcConstants.PROP_S7_REGULATION,             // 34
		TcConstants.PROP_S7_DISPLAYPARTNO,          // 35
		TcConstants.PROP_ITEM_ID,                 // 36
		TcConstants.PROP_S7_ECO_NO,                  // 37
		TcConstants.PROP_S7_PRD_PROJ_CODE,   // 38
		TcConstants.PROP_OBJECT_NAME,
		TcConstants.PROP_ITEM_REVISION_ID,
		TcConstants.PROP_OBJECT_TYPE,
		TcConstants.PROP_S7_CCN_NO,
		TcConstants.PROP_S7_PREVEH_TYPEDREFERENCE,
        TcConstants.PROP_S7_ACTWEIGHT
		};
	
	public static String[] refProperties =
        {
    		TcConstants.PROP_S7_EST_COST_MATERIAL, 
    		TcConstants.PROP_S7_TARGET_COST_MATERIAL, 
    		TcConstants.PROP_S7_PROTO_TOOLG,
    		TcConstants.PROP_S7_PRD_TOOL_COST,
    		TcConstants.PROP_S7_PRD_SERVICE_COST,
    		TcConstants.PROP_S7_PRD_SAMPLE_COST
        };

	public Object startPreProductInterfaceService() throws Exception
	{
		// 1. TC Session 생성
		if (tcSession == null)
		{
			TcLoginService tcLoginService = new TcLoginService();

			tcSession = tcLoginService.getTcSession();
		}

		tcItemUtil = new TcItemUtil(tcSession);
		datasetUtil = new TcDatasetUtil(tcSession);
		fileUtil = new TcFileUtil(tcSession);
		strService = new TcStructureManagementService(tcSession);
		dataService = new TcDataManagementService(tcSession);

		BOMWindow window = null;
		BOMLine prodBOMLine;
		HashMap<String, HashMap<String, String>> productAllChildPartsList = new HashMap<>();
		HashMap<String, HashMap<String, HashMap<String, String>>> productAllChildPartsUsageList = new HashMap<>();

		try
		{

			TcSessionUtil tcSessionUtil = new TcSessionUtil( tcSession ) ;

			/**
			 * D300 및 특정 PreProdcuet 에 대해 한시적으로 인터페이스 제외
			 * 
			 * 2017 12-20 by Been la ho 
			 * */

			CompletePreference[] preferences = tcSessionUtil.getPreference(TcConstants.TC_PREF_SCOPE_SITE, new String[]{"preproduct_interface_filter"});
			// 제외 대상
			String excludeProductValues = "";

			System.out.println( " FullPreProductNotInterfaceService ----------------  preferences " + preferences);

			if( preferences != null ){

				String[] filterPreProductIdList =preferences[0].values.values;

				for( int i =0; filterPreProductIdList != null && i < filterPreProductIdList.length ; i++){

					excludeProductValues = excludeProductValues + (i != 0 ?";":"") + filterPreProductIdList[i];

				}
			}
			System.out.println( " FullPreProductNotInterfaceService ----------------  excludeProductValues :" + excludeProductValues);
			String[] entries = null;
			String[] values = null;
			
			//특정 프로젝트만 스케쥴러 실행할 경우에는 아래에서 PRE-PRODUCT 지정.
			//excludeProductValues = "PE100PRE";//"PQ250PRE";
			excludeProductValues = "PKR10PRE";//"PO100PRE";//"PQ250PRE";

			if( excludeProductValues != null && !excludeProductValues.equals("") && excludeProductValues.length() > 0 ){ 

				entries = new String[]{"Gate", "ID"};
				values = new String[]{"6;7;8;9;10", excludeProductValues };

			}else{

				return "";

			}
			// END  2017 12-20 by Been la ho 

			queryService = new TcQueryService(tcSession);

			systemCodeMap = new HashMap<>();
			TcLOVService lovService = new TcLOVService();
			List<KeyLOV> systemCodeLovLists = lovService.getLOVValueList("S7_SYSTEM_CODE");
			//            List<KeyLOV> systemCodeLovLists = (List<KeyLOV>) TcCommonDao.getTcCommonDao().selectList("com.symc.tc.lov.getLOVValues", "S7_SYSTEM_CODE");
			for (KeyLOV value : systemCodeLovLists)
			{
				String key = value.getValue() == null ? "" : value.getValue().toString();
				String keyValue = value.getDescription();

				systemCodeMap.put(key, keyValue);
			}

//			ModelObject []findPreProducts = queryService.searchTcObject(TcConstants.SEARCH_PREPRODUCTREV, entries, values, new String[]{TcConstants.PROP_ITEM_ID, TcConstants.PROP_S7_OSPEC_NO, TcConstants.PROP_ITEMS_TAG});
			//필요한 프로젝트만 할때는 위에 프로젝트 지정하고 아래를 주석 풀어서 실행할 것.
			ModelObject []findPreProducts = queryService.searchTcObject(TcConstants.SEARCH_PREPRODUCTREV_INTERFACE, entries, values, new String[]{TcConstants.PROP_ITEM_ID, TcConstants.PROP_S7_OSPEC_NO, TcConstants.PROP_ITEMS_TAG});
			if (findPreProducts == null || findPreProducts.length == 0)
				return "";
 
			List<ModelObject> findProdRevList = Arrays.asList(findPreProducts);

			ArrayList<Item> itemList = new ArrayList<>();
			for (ModelObject preProduct : findPreProducts)
			{
				Item revItem = ((ItemRevision) preProduct).get_items_tag();
				tcItemUtil.getProperties(new ModelObject[]{revItem}, new String[]{TcConstants.PROP_ITEM_ID, "revision_list"});

				if (! itemList.contains(revItem))
					itemList.add(revItem);
			}
			
//			for(int i = 0; i < itemList.size(); i++) {
//				
//				System.out.println( "PRODUCT_ID : " + itemList.get(i).get_item_id() );
//			}

			//            dataService.getProperties(itemList.toArray(new ModelObject[0]), new String[]{"revision_list"});

			for (Item prodItem : itemList)
			{
				ItemRevision prodRevision = getLatestReleasedRevision(prodItem);

				if (prodRevision == null)
					continue;


				// 0. 마지막 결재된 리비전이 Gate에 해당하는 것만 처리하도록 한다.
				if (! findProdRevList.contains(prodRevision))
					continue;

				// Gate 값이 맞는지 체크
				if (! Arrays.asList(values[0].split(";")).contains(prodRevision.getPropertyObject("s7_GATE_NO").getStringValue()))
					continue;
				// 1. Ospec 을 읽어온다.
				//20190625 ospec 이 없으면 skip되도록 처리함.
				ModelObject ospecRev;
				try {
					ospecRev = getOSpecRevision(prodRevision);
				}catch(Exception ex) {
					continue;
				}
				
				OSpec oSpec = getOspec(ospecRev);
				HashMap<String, StoredOptionSet> revSOSList = getStoredOptionSets(oSpec);

				// Whell Type Option 및 Transmission Option 정보를 분류한다.
				tmMap = new HashMap<>();
				wtMap = new HashMap<>();
				for (OpValueName opValueName : oSpec.getOpNameList())
				{
					// TransMission
					if (opValueName.getCategory().equals("E00"))
					{
						tmMap.put(opValueName.getOption(), opValueName);
					}
					// Wheel Type
					if (opValueName.getCategory().equals("E10"))
					{
						wtMap.put(opValueName.getOption(), opValueName);
					}
				}
				//System.out.println( "------------------------- X2 --------------------" + prodRevision.get_item_id() );
				// BOMLine을 가져온다.
				CreateBOMWindowsResponse res = strService.createTopLineBOMWindow(prodRevision, strService.getRevisionRule("Latest Released_revision_rule"), null);
				try
				{
					window = res.output[0].bomWindow;
					prodBOMLine = res.output[0].bomLine;

					tcItemUtil.getProperties(new ModelObject[]{prodRevision}, new String[]{TcConstants.PROP_S7_PROJCODE});
					String proj_code = prodRevision.getPropertyObject(TcConstants.PROP_S7_PROJCODE).getStringValue();

					// BOMLine 하위를 가져온다.(재귀)
					ArrayList<BOMLineSearcher> bomLoaderList = new ArrayList<BOMLineSearcher>();

					tcItemUtil.getProperties(new ModelObject[]{prodBOMLine}, new String[]{"bl_child_lines"});
					//                BOMLine[] funcLines = strService.getExpandPSOneLevel(tcSession, prodBOMLine);
					ModelObject[] funcLines = prodBOMLine.get_bl_child_lines();
					tcItemUtil.getProperties(funcLines, new String[]{TcConstants.PROP_BL_ITEM_ID, "bl_item_revision", "bl_child_lines"});
					int func_idx = 0;

					//System.out.println( "------------------------- X3 --------------------" + funcLines.length );
					for (ModelObject funcLine : funcLines)
					{		
						if (((BOMLine) funcLine).get_bl_revision() != null)
						{
							func_idx++;

							Item funcItem = (Item) ((BOMLine) funcLine).get_bl_item();
							tcItemUtil.getProperties(new ModelObject[]{funcItem}, new String[]{TcConstants.PROP_ITEM_ID});
							String func_code = funcItem.get_item_id().substring(0, 4);

							//                        BOMLine[] fmpLines = strService.getExpandPSOneLevel(tcSession, funcLine);
							ModelObject[] fmpLines = ((BOMLine) funcLine).get_bl_child_lines();
							tcItemUtil.getProperties(fmpLines, new String[]{TcConstants.PROP_BL_ITEM_ID, "bl_item_revision", "bl_child_lines"});
							int fmp_idx = 0;
							for (ModelObject fmpLine : fmpLines)
							{
								fmp_idx ++;
//								테스트코드								
//								if(!funcItem.get_item_id().equals("F620D300PB")){
//									continue;
//								}
								//System.out.println( "------------------------- X3.1 --------------------" + funcLines.length );
								if (((BOMLine) fmpLine).get_bl_revision() != null && ((BOMLine) fmpLine).get_bl_child_lines().length > 0)
								{
									//System.out.println( "------------------------- X3.2 --------------------" + funcLines.length );
									BOMLineSearcher bomLoader = new BOMLineSearcher(proj_code, func_code, ((BOMLine) fmpLine), oSpec, productAllChildPartsList, productAllChildPartsUsageList, revSOSList, func_idx, fmp_idx);
									bomLoaderList.add(bomLoader);
								}
							}
						}
					}

					// [20160525][ymjang] Runnable --> Callable 방식으로 변경함.
					if (bomLoaderList.size() > 0)
					{
						ExecutorService executor = Executors.newFixedThreadPool(20);
						List<Future<String>> futureList = new ArrayList<Future<String>>();
						for (BOMLineSearcher loader : bomLoaderList)
						{
							Future<String> future = executor.submit(loader);
							futureList.add(future);
						}

						for (Future<String> future : futureList) {
							try {
								writeLog(future.get());
								//System.out.println((new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")).format(new Date()) + " :: " + future.get());
							} catch (Exception e) {
								e.printStackTrace();
							}	
						}

						executor.shutdown();
						while (!executor.isTerminated())
						{}
					}
					//System.out.println( "------------------------- X4 --------------------" + prodRevision.get_item_id() );
					
				}
				catch (Exception ex)
				{
					throw ex;
				}
				finally
				{
					if (window != null)
						strService.closeBOMWindow(window);
				}
			}

			System.out.println("-------------------------------  count " + productAllChildPartsList.size() );


			if (productAllChildPartsList.size() > 0)
			{
				IFMasterFullService dbService = new IFMasterFullService();

				if (dbService != null)
				{
					dbService.insertMasterFullList(productAllChildPartsList, productAllChildPartsUsageList);
				}
			}

			Calendar c = Calendar.getInstance();
			String  eai_create_time =  new SimpleDateFormat("yyyyMMddHHmmss").format(c.getTime());

			TcCommonDao commonDao = TcCommonDao.getTcCommonDao();
			DataSet ds = new DataSet();
			//EAI_FLAG가 "S" 인 경우 전송 상태값을 SUCCESS로 변경
			ds.put("EAI_FLAG", "X");
			ds.put("STAT", "SKIP");
			ds.put("EAI_CREATE_TIME", eai_create_time);
			System.out.println("----------------  EAI_CREATE_TIME : " + eai_create_time );
			commonDao.update("com.symc.interface.updateNotEaiPreMasterFullList", ds);
			commonDao.update("com.symc.interface.updateNotEaiPreUsageFullList", ds);
			commonDao.update("com.symc.interface.updateNotEaiOspecTrim", ds);
			
			return "Success";
		}
		catch (Exception ex)
		{
			throw ex;
		} finally {
			if (tcSession != null)
				tcSession.logout();
		}
	}

	public void setSession(Session session) {
		this.tcSession = session;
	}

	private ItemRevision getLatestReleasedRevision(Item prodItem) throws Exception
	{
		ModelObject[] revisions = prodItem.get_revision_list();

		dataService.getProperties(revisions, new String[]{TcConstants.PROP_RELEASE_STATUS_LIST, TcConstants.PROP_ITEM_REVISION_ID});
		for (int i = revisions.length - 1; i >= 0; i--)
		{
			//[20160718][ymjang] 최종 Release 된 리비전 찾기 오류 수정
			ReleaseStatus[] releaseStatusArray = ((ItemRevision) revisions[i]).get_release_status_list();
			if (releaseStatusArray != null && releaseStatusArray.length > 0)
				return (ItemRevision) revisions[i];
		}

		return null;
	}

	private HashMap<String, StoredOptionSet> getStoredOptionSets(OSpec ospec) throws Exception
	{
		try
		{
			HashMap<String, StoredOptionSet> optionSetMap = new HashMap<>();;

			ArrayList<OpTrim> trimList = ospec.getTrimList();
			HashMap<String, ArrayList<Option>> trimOptionMap = ospec.getOptions();

			for (OpTrim opTrim : trimList)
			{
				ArrayList<Option> options = trimOptionMap.get(opTrim.getTrim());
				String stdName = opTrim.getTrim() + "_STD";
				String optName = opTrim.getTrim() + "_OPT";
				StoredOptionSet stdSos = new StoredOptionSet(stdName);
				stdSos.add("TRIM", stdName);
				StoredOptionSet optSos = new StoredOptionSet(optName);
				optSos.add("TRIM", optName);

				for( Option option : options){
					if( option.getValue().equalsIgnoreCase("S")){
						stdSos.add(option.getOp(), option.getOpValue());
						optSos.add(option.getOp(), option.getOpValue());
					}else if( !option.getValue().equalsIgnoreCase("-") ){
						optSos.add(option.getOp(), option.getOpValue());
					}
				}

				optionSetMap.put(stdName, stdSos);
				optionSetMap.put(optName, optSos);
			}

			return optionSetMap;
		}
		catch (Exception ex)
		{
			throw ex;
		}
	}

	private OSpec getOspec(ModelObject ospecRev) throws Exception {
		try
		{
			OSpec ospec = null;
			String ospecStr = ((ItemRevision) ospecRev).get_item_id() + "-" + ((ItemRevision) ospecRev).get_item_revision_id();
			ModelObject[] datasets = ospecRev.getPropertyObject(TcConstants.RELATION_REFERENCES).getModelObjectArrayValue();

			tcItemUtil.getProperties(datasets, new String[]{TcConstants.PROP_OBJECT_NAME, TcConstants.PROP_REF_NAMES});
			for( int i = 0; datasets != null && i < datasets.length; i++){
				ModelObject ds = datasets[i];

				if( ospecStr.equals(((Dataset) ds).get_object_name())){
					Property refProperty = ds.getPropertyObject(TcConstants.PROP_REF_NAMES);
					ImanFile[] imanFiles = datasetUtil.getReferencedFileFromDataset(ds.getUid(), refProperty.getStringArrayValue());
					File[] files = fileUtil.getFiles(imanFiles);
					ospec = OpUtil.getOSpec(files[0]);
					break;
				};
			}

			return ospec;
		}
		catch (Exception ex)
		{
			throw ex;
		}
	}

	private ModelObject getOSpecRevision(ItemRevision preProduct) throws Exception
	{
		try
		{
			Property ospecNo = preProduct.getPropertyObject(TcConstants.PROP_S7_OSPEC_NO);

			if( ospecNo == null || ospecNo.equals("")){
				throw new Exception("Could not found OSPEC_NO.");
			}

			int idx = ospecNo.getStringValue().lastIndexOf("-");
			if( idx < 0){
				throw new Exception("Invalid OSPEC_NO.");
			}
			String ospecId = ospecNo.getStringValue().substring(0, idx);
			String ospecRevId = ospecNo.getStringValue().substring( idx + 1 );

			ModelObject []ospec = queryService.searchTcObject(TcConstants.SEARCH_ITEM_REVISION, new String[]{"Item ID", "Revision"}, new String[]{ospecId, ospecRevId}, new String[]{TcConstants.PROP_ITEM_ID, TcConstants.PROP_ITEM_REVISION_ID, TcConstants.RELATION_REFERENCES});

			if (ospec != null && ospec.length > 0)
				return ospec[0];

			return null;
		}
		catch (Exception ex)
		{
			throw ex;
		}
	}

	/**
	 * [20160523][ymjang] 처리 로그 작성 기능 추가
	 * @param projCode
	 * @param funcCode
	 * @param fmpLine
	 * @param ospec
	 * @param usageOptionSetList
	 * @param allPartsList
	 * @param allUsageList
	 * @param logBuff
	 * @throws Exception
	 */
	protected void getChildBOMLineWithSOS(String projCode, String funcCode, BOMLine fmpLine, OSpec ospec, 
			HashMap<String, StoredOptionSet> usageOptionSetList, HashMap<String, HashMap<String, String>> allPartsList, 
			HashMap<String, HashMap<String, HashMap<String, String>>> allUsageList,
			StringBuffer logBuff, int func_idx, int fmp_idx, int line_idx, int level_idx) throws Exception
			{
		try
		{
			if (fmpLine.get_bl_revision() == null)
				return;

			ModelObject []childLines = fmpLine.get_bl_child_lines();

			if (childLines != null && childLines.length > 0)
			{
				String fmpItemId = fmpLine.get_bl_item_item_id();
				//KCH 추가 - SYSTEM_ROW_KEY Query 변경
				IFMasterFullService dbService = new IFMasterFullService();
				getAllChildrenList(fmpItemId, childLines, projCode, funcCode, "", ospec, usageOptionSetList, allPartsList, allUsageList, logBuff, func_idx, fmp_idx, line_idx, level_idx, dbService);
			}
		}
		catch (Exception ex)
		{
			throw ex;
		}
			}

	/**
	 * [20160523][ymjang] 처리 로그 작성 기능 추가
	 * @param lineItemId
	 * @param children
	 * @param projCode
	 * @param funcCode
	 * @param parentCondition
	 * @param ospec
	 * @param usageOptionSetList
	 * @param allPartsList
	 * @param allUsageList
	 * @param logBuff
	 * @throws Exception
	 */
	private void getAllChildrenList(String lineItemId, ModelObject[] children, String projCode, String funcCode, String parentCondition, OSpec ospec, 
			HashMap<String,StoredOptionSet> usageOptionSetList, HashMap<String, HashMap<String, String>> allPartsList, 
			HashMap<String, HashMap<String, HashMap<String, String>>> allUsageList,
			StringBuffer logBuff, int func_idx, int fmp_idx, int line_idx, int level_idx, IFMasterFullService dbService) throws Exception
			{
		try
		{
			if (children == null)
				return;

			level_idx ++;
			tcItemUtil.getProperties(children, new String[]{TcConstants.PROP_BL_ITEM_ID, "bl_item_revision", "bl_child_lines", "bl_parent"});
			BOMLine childBOMLine = null;
			ModelObject bl_revision = null;
			ModelObject[] bl_child_lines = null;
			
			for (ModelObject child : children)
			{
				line_idx ++ ;

				childBOMLine = (BOMLine) child;
				bl_revision = childBOMLine.get_bl_revision();
				if (bl_revision == null)
					continue;

				ItemRevision childRevision = (ItemRevision) bl_revision;

				ItemRevision parentRevision = (ItemRevision) ((BOMLine) childBOMLine.get_bl_parent()).get_bl_revision();
				tcItemUtil.getProperties(new ModelObject[]{parentRevision}, parentRevProperties);
				tcItemUtil.getProperties(new ModelObject[]{childBOMLine}, bomlineProperties);
				tcItemUtil.getProperties(new ModelObject[]{childRevision}, revisionProperties);

				Property parentItemID = parentRevision.getPropertyObject(TcConstants.PROP_ITEM_ID);
				Property childItemID = childBOMLine.getPropertyObject(TcConstants.PROP_BL_ITEM_ID);
				Property findNoProp = childBOMLine.getPropertyObject(TcConstants.PROP_BL_SEQUENCE_NO);
				String curCondition = childBOMLine.getPropertyObject(TcConstants.PROP_BL_VARIANT_CONDITION).getStringValue(); // childBOMLine.get_bl_variant_condition();
				String carryOver = childBOMLine.getPropertyObject(TcConstants.PROP_BL_CHG_CD).getStringValue();
				String childType = childRevision.get_object_type();

				String parentNo = parentItemID.getStringValue();
				String findNo = findNoProp.getStringValue();
				String childNo = childItemID.getStringValue();
				//KCH 추가 - System_row_key 추가.
				String systemRowKey = dbService.selectSystemRowKey(childBOMLine.getPropertyObject(TcConstants.PROP_BL_OCC_FND_OBJECT_ID).getStringValue());
				//System.out.println("AAA:"+childBOMLine.getPropertyObject(TcConstants.PROP_BL_OCC_FND_OBJECT_ID).getStringValue());
				//System.out.println(systemRowKey);
				//KCH TEST MODE START
//				System.out.println("======================");
//				System.out.println("PARENT : " + parentNo +" : "+parentRevision.getUid());
//				System.out.println("CHILD : " + childNo +" : "+childRevision.getUid());
				
				//KCH TEST MODE END
//				테스트코드
//				if(childNo.equals("D300B131495") || childNo.equals("D300B130635") || childNo.equals("D300B130818") || childNo.equals("D300B130158")){
//					System.out.println("");
//				} else {
//					continue;
//				}
				String mapKey = funcCode + parentNo + findNo + childNo;
				String thisCondition;

				if (parentCondition == null || parentCondition.equals("")) {                	
					thisCondition = curCondition;
				}
				else {
					// [20160608][ymjang] usage 계산 오류 수정
					thisCondition = "(" + parentCondition + ") and (" + curCondition + ")";
					//thisCondition = "(" + parentCondition + ") AND (" + curCondition + ")";
				}
				String lineUniqNo = lineItemId + "^" + childNo;

				// [20160523][ymjang] 처리 로그 작성 기능 추가
				logBuff.append(IFConstants.TEXT_RETURN);
				logBuff.append(mapKey + " : " + lineUniqNo + " is reading.");

				if (! allPartsList.containsKey(mapKey))
				{
					// KCH 수정 - SYSTEM_ROW_KEY Query로 변경
					allPartsList.put(mapKey, getPartValue(lineUniqNo, projCode, funcCode, childBOMLine, parentRevision, childRevision, ospec, usageOptionSetList, curCondition, func_idx, fmp_idx, line_idx, level_idx, systemRowKey));
					allUsageList.put(mapKey, getUsageValue(lineUniqNo, projCode, childBOMLine, childRevision, thisCondition, ospec, usageOptionSetList, null, systemRowKey));
				}
				else
				{
					// KCH 수정 - SYSTEM_ROW_KEY Query로 변경
					allUsageList.put(mapKey, getUsageValue(lineUniqNo, projCode, childBOMLine, childRevision, thisCondition, ospec, usageOptionSetList, allUsageList.get(mapKey), systemRowKey));
				}

				// [20160523][ymjang] 처리 로그 작성 기능 추가
				logBuff.append(IFConstants.TEXT_RETURN);
				logBuff.append(mapKey + " : " + lineUniqNo + " is end.");

				//[20180830][CSH] S7_PreVehPartRevision이고 carryOver이면 로직 수행(기술관리 송대영책임 요청사항)
                if(childType.equals("S7_PreVehPartRevision") && carryOver.equals("C")){
                	carryOver = "";
                }
                bl_child_lines = childBOMLine.get_bl_child_lines();
				if (bl_child_lines != null && bl_child_lines.length > 0 && ! carryOver.equals("C") && ! carryOver.equals("D") && childType.equals("S7_PreVehPartRevision"))
				{
					// KCH 추가
					getAllChildrenList(lineUniqNo, bl_child_lines, projCode, funcCode, thisCondition, ospec, usageOptionSetList, allPartsList, allUsageList, logBuff, func_idx, fmp_idx, line_idx, level_idx, dbService);
				}
			}
		}
		catch (Exception ex)
		{
			throw ex;
		}
			}

	private String convertToSimpleCondition(String condition){
		ArrayList<String> foundOpValueList = new ArrayList<String>();

		if (condition == null || condition.equals("")) {
			return "" ;
		}

		Pattern p = Pattern.compile(" or | and |\"[a-zA-Z0-9]{4}\"|\"[a-zA-Z0-9]{5}_STD\"|\"[a-zA-Z0-9]{5}_OPT\"");
		Matcher m = p.matcher(condition);
		while (m.find()) {
			//          System.out.println(m.start() + " " + m.group());
			foundOpValueList.add(m.group().trim());
		}

		String conditionResult = null;
		for( String opValue : foundOpValueList){
			String con = opValue.replaceAll("\"", "");
			if( conditionResult == null){
				conditionResult = con;
			}else{
				conditionResult += " " + con;
			}
		}

		return conditionResult;
	}

	public String removeTrimOptionValue(String condition){

		String resultStr = "";

		if (condition == null || condition.equals("")) {
			return "" ;
		}

		String[] tmpArray = condition.split(" or ");
		for( int i = 0; tmpArray != null && i < tmpArray.length; i++){
			String str = tmpArray[i].replaceAll("[a-zA-Z0-9]{5}_STD and |[a-zA-Z0-9]{5}_OPT and ", "");
			str = str.replaceAll(" and [a-zA-Z0-9]{5}_STD| and [a-zA-Z0-9]{5}_OPT", "");
			str = str.replaceAll("[a-zA-Z0-9]{5}_STD|[a-zA-Z0-9]{5}_OPT", "").trim(); 
			if( resultStr.equals("")){
				resultStr = str;
			}else{
				resultStr += " or " + str;
			}
		}

		return resultStr;
	}

	public static HashMap<String, String> getUsageInfo(String uniqueId, String itemID, String projCode, String e0Code, String e0Desc, String e1Code, String e1Desc, OpTrim trim, String qty, String optionType, String sysRowKey)
	{
		HashMap<String, String> usageMap = new HashMap<>();

		usageMap.put("UNIQUE_ROW_KEY", uniqueId);
		usageMap.put("PART_UNIQUE_NO", itemID);
		if (projCode != null && projCode.trim().length() > 0)
			usageMap.put("PROJECT_CODE", projCode);
		usageMap.put("AREA", trim.getArea());
		usageMap.put("PASSENGER", trim.getPassenger());
		usageMap.put("ENGINE", trim.getEngine());
		usageMap.put("GRADE", trim.getGrade());
		usageMap.put("TRIM", trim.getTrim());
		usageMap.put("USAGE_QTY", qty);
		usageMap.put("USAGE_TYPE", optionType);
		usageMap.put("SYSTEM_ROW_KEY", sysRowKey);
		usageMap.put("OPT_E00", e0Code);
		usageMap.put("OPT_E00_DESC", e0Desc);
		usageMap.put("OPT_E10", e1Code);
		usageMap.put("OPT_E10_DESC", e1Desc);

		return usageMap;
	}

	private HashMap<String, HashMap<String, String>> getUsageValue(String uniqueId, String prodProjCode, BOMLine childBOMLine, ItemRevision revision, String thisCondition, OSpec ospec, HashMap<String, StoredOptionSet> usageOptionSetList, HashMap<String, HashMap<String, String>> usageMap, String systemRowKey) throws Exception
	{
		try
		{
			if (usageMap == null)
				usageMap = new HashMap<>();

				//            String variantCondition = childBOMLine.getPropertyObject(TcConstants.PROP_BL_VARIANT_CONDITION).getStringValue();
				String lineQty = childBOMLine.getPropertyObject(TcConstants.PROP_BL_QUANTITY).getDisplayableValue();
				//KCH 추가 - SYSTEM_ROW_KEY Query로 변경
				//String systemRowKey = childBOMLine.getPropertyObject(TcConstants.PROP_BL_SYSTEM_ROW_KEY).getStringValue();
				String itemID = revision.get_item_id();
				String projCode = prodProjCode;//revision.getPropertyObject(TcConstants.PROP_S7_PROJCODE).getStringValue();

				//EA는 Double형의 Quantity가 올 수 없다.
				//Integer Type이 아니면 그대로 표기함.
				if (lineQty == null || lineQty.trim().equals(""))
					lineQty = "1";
				try{
					double dNum = Double.parseDouble(lineQty);
					int iNum = (int)dNum;
					if( dNum == iNum){
						lineQty = "" + iNum;
					}
				}catch(NumberFormatException nfe){
					nfe.printStackTrace();
				}

				ArrayList<OpTrim> trimList = ospec.getTrimList();
				String simpleCondition = convertToSimpleCondition(thisCondition);

				if (usageOptionSetList != null)
				{
					for (OpTrim trim : trimList)
					{
						String sosStdName = trim.getTrim() + "_STD";
						String sosOptName = trim.getTrim() + "_OPT";
						StoredOptionSet sosStd = usageOptionSetList.get(sosStdName);
						StoredOptionSet sosOpt = usageOptionSetList.get(sosOptName);

						if (sosStd.isInclude(engine, simpleCondition))
						{
							if (usageMap.containsKey(sosStdName))
							{
								HashMap<String, String> valueMap = usageMap.get(sosStdName);

								String beforeQty = valueMap.get("USAGE_QTY").toString();
								double curQty = Double.valueOf(beforeQty) + Double.valueOf(lineQty);
								valueMap.put("USAGE_QTY", String.valueOf(curQty));
							}
							else
							{
								// 20170515 : C300, A200, G15DTF 의 경우 E00, E10 이 반드시 존재함.
								//			  E100의 경우 E00이 존재하지 않음(향후 존재할 수 있음.)	 
								//				> 없는 경우 공백 등록 (NULL은 허용하지 않음.)
								String tmCode = " ";
								String tmName = " ";
								String wtCode = " ";
								String wtName = " ";

								if( sosStd.getOptionSet().get("E00") != null ) {
									if (!"".equals(sosStd.getOptionSet().get("E00").get(0)) ) {
										tmCode = sosStd.getOptionSet().get("E00").get(0);
										tmName = tmMap.get(tmCode).getOptionName();
									}
								}

								if( sosStd.getOptionSet().get("E10") != null ) {
									if(!"".equals(sosStd.getOptionSet().get("E10").get(0)) ) {
										wtCode = sosStd.getOptionSet().get("E10").get(0);
										wtName = wtMap.get(wtCode).getOptionName();
									}
								}
								// ~ 20170515
								// 원본 (20170515)                        	
								//                            String tmCode = sosStd.getOptionSet().get("E00").get(0);
								//                            String tmName = tmMap.get(tmCode).getOptionName();
								//                            String wtCode = sosStd.getOptionSet().get("E10").get(0);
								//                            String wtName = wtMap.get(wtCode).getOptionName();

								HashMap<String, String> newValue = getUsageInfo(uniqueId, itemID, projCode, tmCode, tmName, wtCode, wtName, trim, lineQty, "STD", systemRowKey);

								usageMap.put(sosStdName, newValue);
							}
						}
						else if (sosOpt.isInclude(engine, simpleCondition))
						{
							if (usageMap.containsKey(sosOptName))
							{
								HashMap<String, String> valueMap = usageMap.get(sosOptName);

								String beforeQty = valueMap.get("USAGE_QTY").toString();
								double curQty = Double.valueOf(beforeQty) + Double.valueOf(lineQty);
								valueMap.put("USAGE_QTY", String.valueOf(curQty));
							}
							else
							{
								// 20170515 : C300, A200, G15DTF 의 경우 E00, E10 이 반드시 존재함.
								//			  E100의 경우 E00이 존재하지 않음(향후 존재할 수 있음.)	 
								//				> 없는 경우 공백 등록 (NULL은 허용하지 않음.)
								String tmCode = " ";
								String tmName = " ";
								String wtCode = " ";
								String wtName = " ";

								if( sosStd.getOptionSet().get("E00") != null ) {
									if(!"".equals(sosStd.getOptionSet().get("E00").get(0)) ) {
										tmCode = sosStd.getOptionSet().get("E00").get(0);
										tmName = tmMap.get(tmCode).getOptionName();
									}
								}

								if( sosStd.getOptionSet().get("E10") != null ) {
									if(!"".equals(sosStd.getOptionSet().get("E10").get(0)) ) {
										wtCode = sosStd.getOptionSet().get("E10").get(0);
										wtName = wtMap.get(wtCode).getOptionName();
									}
								}
								// ~ 20170515

								// 원본 (20170515)
								//                            String tmCode = sosStd.getOptionSet().get("E00").get(0);
								//                            String tmName = tmMap.get(tmCode).getOptionName();
								//                            String wtCode = sosStd.getOptionSet().get("E10").get(0);
								//                            String wtName = wtMap.get(wtCode).getOptionName();

								HashMap<String, String> newValue = getUsageInfo(uniqueId, itemID, projCode, tmCode, tmName, wtCode, wtName, trim, lineQty, "OPT", systemRowKey);

								usageMap.put(sosOptName, newValue);
							}
						}
					}
				}

				return usageMap;
		}
		catch (Exception ex)
		{
			throw ex;
		}
	}

	@SuppressWarnings("unchecked")
	private HashMap<String, String> getPartValue(String uniqueId, String prodProjectCode, String functionCode, BOMLine bomLine,
			ItemRevision parentRevision, ItemRevision currentRevision, OSpec ospec, 
			HashMap<String, StoredOptionSet> usageOptionSetList,
			String curCondition, int func_idx, int fmp_idx, int line_idx, int level_idx, String systemRowKey) throws Exception
			{
		try
		{
			HashMap<String, String> propMap = new HashMap<>();

			String childType = currentRevision.get_object_type();

			propMap.put("UNIQUE_ROW_KEY", uniqueId);
			propMap.put("TARGET_PROJECT", prodProjectCode);
//			if (childType.equals("S7_StdpartRevision"))[20180307][LJG]주석 - 표준품도 시스템코드를 BOMLine에서 가져오도록 변경
//			{
//				propMap.put("SYSTEM_CODE", "X00");
//				propMap.put("SYSTEM_NAME", "STANDARD HARD-WARES");
//			}
//			else
//			{
				propMap.put("SYSTEM_CODE", bomLine.getPropertyObject(TcConstants.PROP_BL_BUDGET_CODE).getStringValue());
				propMap.put("SYSTEM_NAME", systemCodeMap.get(bomLine.getPropertyObject(TcConstants.PROP_BL_BUDGET_CODE).getStringValue()));
//			}
			propMap.put("FUNCTION", functionCode);
			String parentNo = parentRevision.getPropertyObject(TcConstants.PROP_S7_DISPLAYPARTNO).getStringValue();
			if (parentNo == null || parentNo.trim().length() == 0)
				parentNo = parentRevision.get_item_id();
			propMap.put("PARENT_NO", parentNo);
			propMap.put("PARENT_UNIQUE_NO", parentRevision.get_item_id());
			propMap.put("PARENT_REV", parentRevision.get_item_revision_id());
			propMap.put("PARENT_NAME", parentRevision.get_object_name());
			String childNo = currentRevision.getPropertyObject(TcConstants.PROP_S7_DISPLAYPARTNO).getStringValue();
			if (childNo == null || childNo.trim().length() == 0)
				childNo = currentRevision.get_item_id();
			propMap.put("CHILD_NO", childNo);
			propMap.put("CHILD_UNIQUE_NO", currentRevision.get_item_id());
			propMap.put("CHILD_REV", currentRevision.get_item_revision_id());
			propMap.put("CHILD_NAME", currentRevision.get_object_name());
			propMap.put("SEQ", bomLine.getPropertyObject(TcConstants.PROP_BL_SEQUENCE_NO).getStringValue());
			propMap.put("PREBOM_UNIQUE_ID", parentRevision.get_item_id() + propMap.get("SEQ") + currentRevision.get_item_id());
			propMap.put("MANDATORY_OPT", bomLine.getPropertyObject(TcConstants.PROP_BL_REQ_OPT).getStringValue());
			propMap.put("SPECIFICATION", bomLine.getPropertyObject(TcConstants.PROP_BL_SPEC_DESC).getStringValue());
			propMap.put("MODULE", bomLine.getPropertyObject(TcConstants.PROP_BL_MODULE_CODE).getStringValue());
			propMap.put("SMODE", bomLine.getPropertyObject(TcConstants.PROP_BL_SUPPLY_MODE).getStringValue());
			//20201021 seho EJS column 추가
			propMap.put("EJS", bomLine.getPropertyObject(TcConstants.PROP_BL_EJS).getStringValue());
			//20210104 WEIGHT_MANAGEMENT column 추가 by 전성용
			propMap.put("WEIGHT_MANAGEMENT", bomLine.getPropertyObject(TcConstants.PROP_BL_WEIGHT_MANAGEMENT).getStringValue());			
			//[20180117][LJG]Bomline 속성 값으로 수정
			//propMap.put("LEV", String.valueOf(level_idx));
			propMap.put("LEV", String.valueOf(bomLine.getPropertyObject(TcConstants.PROP_BL_LEVEL).getIntValue() - 2));

			// [20160629][ymjang] FullBOM Master List 출력을 위하여 Level M, QTY, SORT_ORDER, VC 컬럼 추가 
			propMap.put("LEV_M", bomLine.getPropertyObject(TcConstants.PROP_BL_LEV_M).getStringValue());
			propMap.put("QTY", bomLine.getPropertyObject(TcConstants.PROP_BL_QUANTITY).getStringValue());
			propMap.put("SORT_ORDER", String.format("%05d", func_idx).trim() + "_" + String.format("%05d", fmp_idx).trim() + "_" + String.format("%05d", line_idx).trim());
			String simpleCondition = convertToSimpleCondition(curCondition);
			String tCondition = removeTrimOptionValue(simpleCondition);
			propMap.put("VC", tCondition);

			/* [SR없음][20150914][jclee] DVP Sample 속성 BOMLine으로 이동*/
			//          propMap.put("DVP_NEEDED_QTY", currentRevision.getPropertyObject(TcConstants.PROP_S7_DVP_NEEDED_QTY).getDisplayableValue());
			//          propMap.put("DVP_USE", currentRevision.getPropertyObject(TcConstants.PROP_S7_DVP_USE).getStringValue());
			//          propMap.put("DVP_REQ_DEPT", currentRevision.getPropertyObject(TcConstants.PROP_S7_DVP_REQ_DEPT).getStringValue());
			propMap.put("DVP_NEEDED_QTY", bomLine.getPropertyObject(TcConstants.PROP_S7_BL_DVP_NEEDED_QTY).getStringValue());
			propMap.put("DVP_USE", bomLine.getPropertyObject(TcConstants.PROP_S7_BL_DVP_USE).getStringValue());
			propMap.put("DVP_REQ_DEPT", bomLine.getPropertyObject(TcConstants.PROP_S7_BL_DVP_REQ_DEPT).getStringValue());

			/* [SR없음][20160317][jclee] Design User, Dept 속성 BOMLine으로 이동 */
			//          propMap.put("ENG_DEPT_NM", currentRevision.getPropertyObject(TcConstants.PROP_S7_ENG_DEPT_NM).getStringValue());
			//          propMap.put("ENG_RESPONSIBLITY", getUserIdForName(currentRevision.getPropertyObject(TcConstants.PROP_S7_ENG_RESPONSIBLITY).getStringValue(), currentRevision.getPropertyObject(TcConstants.PROP_S7_ENG_DEPT_NM).getStringValue()));
			propMap.put("ENG_DEPT_NM", bomLine.getPropertyObject(TcConstants.PROP_S7_BL_ENG_DEPT_NM).getStringValue());
			propMap.put("ENG_RESPONSIBLITY", getUserIdForName(bomLine.getPropertyObject(TcConstants.PROP_S7_BL_ENG_RESPONSIBLITY).getStringValue(), bomLine.getPropertyObject(TcConstants.PROP_S7_BL_ENG_DEPT_NM).getStringValue()));

			String chgType2 = bomLine.getPropertyObject(TcConstants.PROP_BL_CHG_CD).getStringValue(); // C, D
			
//			if( "A200P200079".equals( propMap.get("CHILD_UNIQUE_NO"))){
//				System.out.println("----------------------------------------------");
//				String chgType = currentRevision.getPropertyObject(TcConstants.PROP_S7_CHG_TYPE_NM).getStringValue();
//				System.out.println(chgType);
//				System.out.println(chgType2);
//				System.out.println("----------------------------------------------");
//				
//			}
			if (! childType.equals("S7_StdpartRevision")) {
				propMap.put("COLOR_ID", currentRevision.getPropertyObject(TcConstants.PROP_S7_COLORID).getStringValue());
				propMap.put("CATEGORY", currentRevision.getPropertyObject(TcConstants.PROP_S7_DR).getStringValue());
				//[SR180713][20180716][CSH] Vehpart는 실중량을 우선으로.. 없으면 예상중량값을 예상중량값에 표시
				if (childType.equals("S7_PreVehPartRevision")){
                	propMap.put("EST_WEIGHT", currentRevision.getPropertyObject(TcConstants.PROP_S7_ESTWEIGHT).getDisplayableValue());
                } else if(childType.equals("S7_VehpartRevision")){
                	String est_weight = currentRevision.getPropertyObject(TcConstants.PROP_S7_ACTWEIGHT).getDisplayableValue();
                	if(est_weight == null || est_weight.equals("") || est_weight.equals("0")){
                		est_weight = currentRevision.getPropertyObject(TcConstants.PROP_S7_ESTWEIGHT).getDisplayableValue();
                	}
                	propMap.put("EST_WEIGHT", est_weight);
                }
					
				String boxValue = currentRevision.getPropertyObject(TcConstants.PROP_S7_BOX).getStringValue();
				// KCH 2byte -> 저장된 값 그대로 Insert 하도록 변경 (아래 주석 처리 else 부분)
				if (boxValue == null || boxValue.trim().length() == 0)
					boxValue = "";
				else
					boxValue = boxValue.substring(0, 1) + "B";
				propMap.put("BOX", boxValue);
				propMap.put("PROJECT", currentRevision.getPropertyObject(TcConstants.PROP_S7_PROJCODE).getStringValue());

				if (childType.equals("S7_PreVehPartRevision")) {
					propMap.put("TGT_WEIGHT", currentRevision.getPropertyObject(TcConstants.PROP_S7_TARGET_WEIGHT).getDisplayableValue());
					propMap.put("CONTENTS", currentRevision.getPropertyObject(TcConstants.PROP_S7_CONTENTS).getStringValue());
					String chgType = currentRevision.getPropertyObject(TcConstants.PROP_S7_CHG_TYPE_NM).getStringValue();
					//[20160425][ymjang] S7_CHG_CD PreVehclePart Revision 속성 정의 오류 수정 --> bomline 속성으로 변경함.
					/**[20170524][ljg]cd 값이 null인지 아닌지를 판단하여 MLM에 보여주는로직으로 변경
                    if (chgType == null  || chgType.trim().equals(""))
                        propMap.put("CHG_TYPE_ENGCONCEPT", bomLine.getPropertyObject(TcConstants.PROP_BL_CHG_CD).getStringValue());
                    else
                        propMap.put("CHG_TYPE_ENGCONCEPT", chgType);
					 **/
					//[20170524][ljg]cd 값이 null인지 아닌지를 판단하여 MLM에 보여주는로직으로 변경
					if (chgType2 == null  || chgType2.trim().equals(""))
						propMap.put("CHG_TYPE_ENGCONCEPT", chgType);
					else
						propMap.put("CHG_TYPE_ENGCONCEPT", chgType2);

					if (chgType != null && (chgType2.equals("C") || chgType.equals("N") || chgType2.equals("D")))
					{
						propMap.put("PROJECT", currentRevision.getPropertyObject(TcConstants.PROP_S7_PROJCODE).getStringValue());
					}
					else if (chgType != null && (chgType.contains("M")))
					{
						propMap.put("PROJECT", currentRevision.getPropertyObject(TcConstants.PROP_S7_PRD_PROJ_CODE).getStringValue());
					}
					else
					{
						propMap.put("PROJECT", currentRevision.getPropertyObject(TcConstants.PROP_S7_PROJCODE).getStringValue());
					}
					
					//[CSH 20180404] DCS No 연계 로직 변경 
                    //N,M,C,D 중 “N”과“C”는 기존 Logic을 따르고, “M1,M2,M3＂인 경우 (System code + 해당차종의 Project)가 일치하는 설계 구상서를 연결
//					HashMap<String, String> dcsInfo = getDCSInfo(propMap.get("PROJECT"), propMap.get("SYSTEM_CODE"));
//                    HashMap<String, String> dcsInfo = null;
                    //[SR181211-009][CSH]External Table에서 DCS 정보 가져오기
                    HashMap<String, Object> dcsInfo = null;
                    if (chgType != null && (chgType.contains("M"))){
//                    	dcsInfo = getDCSInfo(propMap.get("TARGET_PROJECT"), propMap.get("SYSTEM_CODE"));
                    	dcsInfo = getNewDCSInfo(propMap.get("TARGET_PROJECT"), propMap.get("SYSTEM_CODE"));
                    } else {
//                    	dcsInfo = getDCSInfo(propMap.get("PROJECT"), propMap.get("SYSTEM_CODE"));
                    	dcsInfo = getNewDCSInfo(propMap.get("PROJECT"), propMap.get("SYSTEM_CODE"));
                    }

					if (dcsInfo != null && dcsInfo.size() > 0)
					{
//						propMap.put("DC_ID", dcsInfo.get("DC_ID"));
//						propMap.put("DC_REV", dcsInfo.get("DC_REV"));
//						propMap.put("RELEASED_DATE", dcsInfo.get("DC_RELEASE_DATE"));
						propMap.put("DC_ID", dcsInfo.get("DC_ID").toString());
                        propMap.put("DC_REV", dcsInfo.get("DC_REV").toString());
                        propMap.put("RELEASED_DATE", dcsInfo.get("DC_RELEASED_DATE").toString());
					}

					propMap.put("CON_DWG_PLAN", currentRevision.getPropertyObject(TcConstants.PROP_S7_CON_DWG_PLAN).getStringValue());
					propMap.put("CON_DWG_PERFORMANCE", currentRevision.getPropertyObject(TcConstants.PROP_S7_CON_DWG_PERFORMANCE).getStringValue());
					propMap.put("CON_DWG_TYPE", currentRevision.getPropertyObject(TcConstants.PROP_S7_CON_DWG_TYPE).getStringValue());

					propMap.put("PRD_DWG_PLAN", currentRevision.getPropertyObject(TcConstants.PROP_S7_PRD_DWG_PLAN).getStringValue());
					propMap.put("PRD_DWG_PERFORMANCE", currentRevision.getPropertyObject(TcConstants.PROP_S7_PRD_DWG_PERFORMANCE).getStringValue());

					//TODOS 여기서 이름 대신에 ID가 들어가도록 수정해 달라네.
					//[CSH 20180510] EST Cost Material & TGT Cost Material은 리비전 속성이아닌 s7_PreVeh_typedReference의 속성을 사용하고 있음.
					ModelObject s7_PreVeh_typedReference = currentRevision.getPropertyObject(TcConstants.PROP_S7_PREVEH_TYPEDREFERENCE).getModelObjectValue();
                    String est_cost_material = "";
                    String target_cost_material = "";
                    String proto_toolg = "";
                    String prd_tool_cost = "";
                    String prd_service_cost = "";
                    String prd_sample_cost = "";
                    int prd_total_i = 0;
     				if(s7_PreVeh_typedReference != null){
     					tcItemUtil.getProperties(new ModelObject[]{s7_PreVeh_typedReference}, refProperties);
     					est_cost_material = s7_PreVeh_typedReference.getPropertyObject(TcConstants.PROP_S7_EST_COST_MATERIAL).getDisplayableValue();
     					target_cost_material = s7_PreVeh_typedReference.getPropertyObject(TcConstants.PROP_S7_TARGET_COST_MATERIAL).getDisplayableValue();
     					proto_toolg = s7_PreVeh_typedReference.getPropertyObject(TcConstants.PROP_S7_PROTO_TOOLG).getDisplayableValue();
     					prd_tool_cost = s7_PreVeh_typedReference.getPropertyObject(TcConstants.PROP_S7_PRD_TOOL_COST).getDisplayableValue();
     					prd_service_cost = s7_PreVeh_typedReference.getPropertyObject(TcConstants.PROP_S7_PRD_SERVICE_COST).getDisplayableValue();
     					prd_sample_cost = s7_PreVeh_typedReference.getPropertyObject(TcConstants.PROP_S7_PRD_SAMPLE_COST).getDisplayableValue();
     					
     					if(prd_tool_cost != null && !prd_tool_cost.equals("")){
     						try{
     							prd_total_i = Integer.parseInt(prd_tool_cost);
     						} catch(Exception e){
     						}
     					}
     					if(prd_service_cost != null && !prd_service_cost.equals("")){
     						try{
     							prd_total_i += Integer.parseInt(prd_service_cost);
     						} catch(Exception e){
     						}
     					}
     					if(prd_sample_cost != null && !prd_sample_cost.equals("")){
     						try{
     							prd_total_i += Integer.parseInt(prd_sample_cost);
     						} catch(Exception e){
     						}
     					}
     					
     				}
                    propMap.put("EST_COST_MATERIAL", est_cost_material);
                    propMap.put("TGT_COST_MATERIAL", target_cost_material);
//					propMap.put("EST_COST_MATERIAL", currentRevision.getPropertyObject(TcConstants.PROP_S7_EST_COST_MATERIAL).getDisplayableValue());
//					propMap.put("TGT_COST_MATERIAL", currentRevision.getPropertyObject(TcConstants.PROP_S7_TARGET_COST_MATERIAL).getDisplayableValue());
                    propMap.put("SELECTED_COMPANY", currentRevision.getPropertyObject(TcConstants.PROP_S7_SELECTED_COMPANY).getStringValue());
//                    propMap.put("PRT_TOOLG_INVESTMENT", currentRevision.getPropertyObject(TcConstants.PROP_S7_PRT_TOOLG_INVESTMENT).getDisplayableValue());
                    propMap.put("PRT_TOOLG_INVESTMENT", proto_toolg);
                    
                    propMap.put("PRD_TOOL_COST", prd_tool_cost);
                    propMap.put("PRD_SERVICE_COST", prd_service_cost);
                    propMap.put("PRD_SAMPLE_COST", prd_sample_cost);
                    propMap.put("PRD_TOTAL", ""+prd_total_i);
				}
				else
				{
					propMap.put("CHG_TYPE_ENGCONCEPT", chgType2);
				}

				DataSet inputSet = new DataSet();
				inputSet.put("PUID", currentRevision.getUid());
				Object dwgDepDate = TcCommonDao.getTcCommonDao().selectOne("com.symc.masterfull.selectDwgDeployableDate", inputSet);

				if (dwgDepDate != null)
					propMap.put("DWG_DEPLOYABLE_DATE", dwgDepDate.toString());

				propMap.put("CHANGE_DESC", currentRevision.getPropertyObject(TcConstants.PROP_S7_CHANGE_DESCRIPTION).getStringValue());
				propMap.put("REGULATION", currentRevision.getPropertyObject(TcConstants.PROP_S7_REGULATION).getStringValue());
			} else {
				propMap.put("CHG_TYPE_ENGCONCEPT", chgType2);
				//[SR180713][20180716][CSH] stdpart는 실중량을 예상중량에 표시한다.
				propMap.put("EST_WEIGHT", currentRevision.getPropertyObject(TcConstants.PROP_S7_ACTWEIGHT).getDisplayableValue());
			}

			propMap.put("ALTER_PART", bomLine.getPropertyObject(TcConstants.PROP_BL_PRE_ALTER_PART).getStringValue());
			//KCH 추가 - SYSTEM_ROW_KEY Query 변경
			//propMap.put("SYSTEM_ROW_KEY", bomLine.getPropertyObject(TcConstants.PROP_BL_SYSTEM_ROW_KEY).getStringValue());
			propMap.put("SYSTEM_ROW_KEY", systemRowKey);
			//[SR170706-008][LJG] Proto Tooling 컬럼 추가
			propMap.put("IS_PROTO_TOOLING", bomLine.getPropertyObject(TcConstants.PROP_BL_PROTO_TOOLING).getStringValue());

			// [CSH 20180404] O-SPEC No 추가
 			if (currentRevision.get_object_type().equals("S7_PreVehPartRevision")) {
 				ModelObject ccnir = currentRevision.getPropertyObject(TcConstants.PROP_S7_CCN_NO).getModelObjectValue();
 				String ospec_no = "";
 				if(ccnir != null){
 					tcItemUtil.getProperties(new ModelObject[]{ccnir}, new String[]{TcConstants.PROP_S7_OSPEC_NO});
 					ospec_no = ccnir.getPropertyObject(TcConstants.PROP_S7_OSPEC_NO).getStringValue();
 				}
 				propMap.put("OSPEC_NO", ospec_no);
 			}
						
			return propMap;
		}
		catch (Exception ex)
		{
			throw ex;
		}
			}

	private String getUserIdForName(String userName, String groupNm) throws Exception {
		if (userName == null || userName.trim().length() == 0)
			return "";

		ModelObject[] users = queryService.searchTcObject("SYMC_Search_User", new String[]{"Person Name"}, new String[]{userName}, new String[]{TcConstants.PROP_USER_ID, TcConstants.PROP_DEPT_NAME});
		if (users == null || users.length == 0){
			return "";
		} else if (users.length > 1){

			tcItemUtil.getProperties(users, new String[]{"person"});
			User sameNameTwoUser = null; 
			String sameNameTwoUserId = null; 
			for (int i = 0; i < users.length; i++) {
				sameNameTwoUser = (User)users[i]; 
				ModelObject person = sameNameTwoUser.getPropertyObject("person").getModelObjectValue();
				tcItemUtil.getProperties(new ModelObject[]{person}, new String[]{TcConstants.PROP_DEPT_NAME});
				if (person.getPropertyObject(TcConstants.PROP_DEPT_NAME) != null) {
					if (person.getPropertyObject(TcConstants.PROP_DEPT_NAME).getStringValue().equalsIgnoreCase(groupNm)){
						sameNameTwoUserId = ((User) users[i]).get_user_id();
					}
				} 
			}

			// User Id 를 못찾을 경우, 마지막 User Id 를 Return 한다.
			if (sameNameTwoUserId != null) {
				return sameNameTwoUserId;
			} else {
				return sameNameTwoUser.get_user_id();
			}

			/* 
			 * [20160414][ymjang] PA6 User 속성이 아닌 Person 속성 --> PA6 값 읽기 오류 개선 
        	for (int i = 0; i < users.length; i++) {
                if (users[i].getPropertyObject(TcConstants.PROP_DEPT_NAME).getStringValue().equals(groupNm)){
                    return ((User) users[i]).get_user_id();
                }
            }
			 */
		} else {
			return ((User) users[0]).get_user_id();
		}
	}

	private HashMap<String, String> getDCSInfo(String projectCode, String sysCode) throws Exception
	{
		HashMap<String, String> dcsMapInfo = new HashMap<String, String>();
		ModelObject[] tccomps = queryService.searchTcObject("SYMC_Search_DesignConcept", new String[]{"Project Code", "System Code"}, new String[]{projectCode, sysCode}, new String[]{TcConstants.PROP_ITEM_ID, "revision_list"});
		if (null != tccomps && tccomps.length > 0) {
			ModelObject dcsItem = (ModelObject) tccomps[0];
			ItemRevision dcsItemRev = getLatestReleasedRevision((Item) dcsItem);
			if (null != dcsItemRev) {
				tcItemUtil.getProperties(new ModelObject[]{dcsItemRev}, new String[]{TcConstants.PROP_ITEM_ID, TcConstants.PROP_ITEM_REVISION_ID, TcConstants.PROP_DATE_RELEASED});

				if (dcsItemRev.get_date_released() != null)
				{
					dcsMapInfo.put("DC_ID", dcsItemRev.get_item_id());
					dcsMapInfo.put("DC_REV", dcsItemRev.get_item_revision_id());
					dcsMapInfo.put("DC_RELEASED_DATE", DATE_FORMAT_MM.format(dcsItemRev.get_date_released().getTime()));
				}
			}
		}
		return dcsMapInfo;
	}
	
	//[SR181211-009][CSH]External Table에서 DCS 정보 가져오기
    private HashMap<String, Object> getNewDCSInfo(String projectCode, String sysCode) throws Exception {
        HashMap<String, Object> dcsMapInfo = new HashMap<String, Object>();

    	TcCommonDao commonDao = TcCommonDao.getTcCommonDao();
    	DataSet ds = new DataSet();
		ds.put("PROJECT_CODE", projectCode);
		ds.put("SYSTEM_CODE", sysCode);

    	ArrayList<HashMap<String, Object>> list = (ArrayList<HashMap<String, Object>>)commonDao.selectList("com.symc.masterfull.getDCSInfo", ds);

	    if(list.size() > 0){
	    	dcsMapInfo = list.get(0);
	    }
    	
        return dcsMapInfo;
    }

	/**
	 * [20160525][ymjang] Runnable --> Callable 방식으로 변경함.
	 * [20160525][ymjang] Log 생성을 위한 StringBuffer 추가
	 */
	class BOMLineSearcher implements Callable
	//class BOMLineSearcher implements Runnable
	{
		private String projCode;
		private String functionCode;
		private BOMLine fmpLine;
		private OSpec ospec;
		private HashMap<String, StoredOptionSet> usageOptionSetList;
		private HashMap<String, HashMap<String, String>> allPartsList;
		private HashMap<String, HashMap<String, HashMap<String, String>>> allUsageList;
		private StringBuffer logBuff = null; 
		private int func_idx = 0;
		private int fmp_idx = 0;
		private int line_idx = 0;
		private int level_idx = 0;

		public BOMLineSearcher(String projCode, String funcCode, BOMLine fmpLine, OSpec ospec, 
				HashMap<String, HashMap<String, String>> partsList, HashMap<String, HashMap<String, HashMap<String, String>>> usageList, 
				HashMap<String, StoredOptionSet> sosList, int func_idx, int fmp_idx)
		{
			this.projCode = projCode;
			this.functionCode = funcCode;
			this.fmpLine = fmpLine;
			this.ospec = ospec;
			this.allPartsList = partsList;
			this.allUsageList = usageList;
			this.usageOptionSetList = sosList;
			this.logBuff = new StringBuffer();
			this.func_idx = func_idx;
			this.fmp_idx = fmp_idx;
			this.line_idx = 0;
			this.level_idx = 0;
		}

		// [20160525][ymjang] Runnable --> Callable 방식으로 변경함.
		// [20160523][ymjang] 처리 로그 작성 기능 추가
		@Override
		public Object call() throws Exception {

			try
			{
				logBuff.append(fmpLine.get_bl_item_item_id());
				logBuff.append((new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")).format(new Date()) + " :: " +
						"ProjCode - " + projCode + ", FMP - " + fmpLine.get_bl_item_item_id() + " is Started.");

				getChildBOMLineWithSOS(projCode, functionCode, fmpLine, ospec, usageOptionSetList, allPartsList, allUsageList, logBuff, func_idx, fmp_idx, line_idx, level_idx);

				logBuff.append(IFConstants.TEXT_RETURN);
				logBuff.append((new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")).format(new Date()) + " :: " +
						"ProjCode - " + projCode + ", FMP - " + fmpLine.get_bl_item_item_id() + " is Completed.");
				logBuff.append("===========================================================================");
			}
			catch (Exception e1)
			{	
				// [20160425][ymjang] 오류 발생시 관리자 메일 발송 기능 추가
				//sendMail(e1.getMessage());
				logBuff.append(IFConstants.TEXT_RETURN);
				logBuff.append(e1.getMessage());
				logBuff.append("===========================================================================");
				e1.printStackTrace();
			}

			return logBuff.toString();
		}

		/*
        public void run()
        {
            try
            {
                getChildBOMLineWithSOS(projCode, functionCode, fmpLine, ospec, usageOptionSetList, allPartsList, allUsageList);
            }
            catch (Exception e1)
            {	
            	// [20160425][ymjang] 오류 발생시 관리자 메일 발송 기능 추가
            	sendMail(e1.getMessage());
                e1.printStackTrace();
            }
        }
		 */
	}

	/**
	 * [20160425][ymjang] 오류 발생시 관리자 메일 발송 기능 추가
	 * @param exceptionStr
	 */
	@SuppressWarnings({ "unchecked" })
	private void sendMail(String exceptionStr){

		TcServiceManager manager = new TcServiceManager(tcSession);
		TcPreferenceManagementService prefManager = null;
		CompletePreference retPrefValue = null;
		try {
			prefManager = manager.getPreferenceService();
			GetPreferencesResponse ret = prefManager.getPreferences(new String[]{"PREBOM_IF_ADMIN"}, true);
			if (ret != null && ret.data.sizeOfPartialErrors() == 0)
			{
				for (CompletePreference pref : ret.response)
					if (pref.definition.protectionScope.toUpperCase().equals("site".toUpperCase()))
						retPrefValue = pref;
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		String[] list = retPrefValue.values.values;
		if( list != null && list.length > 0){

			String toUsers = "";
			String body = "";
			String title = "";
			for( int i = 0; i < list.length; i++ ){
				String toUser = list[i];
				if( i > 0){
					toUsers += "," + toUser;
				}else{
					toUsers = toUser;
				}
			}
			title = "New PLM : TC to DCES Pre-BOM Master List I/F Error 알림";
			body = "<PRE>";
			body += exceptionStr.toString();
			body += "</PRE>";

			DataSet ds = new DataSet();
			ds.put("the_sysid", "NPLM");
			ds.put("the_sabun", "NPLM");

			ds.put("the_title", title);
			ds.put("the_remark", body);
			ds.put("the_tsabun", toUsers);

			try {
				// [20160606][ymjang] 메일 발송 방식 개선 (through EAI)
				TcCommonDao.getTcCommonDao().update("com.symc.interface.sendMailEai", ds);
				//TcCommonDao.getTcCommonDao().update("com.symc.interface.sendMail", ds);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
	}

	/**
	 * [20160523][ymjang] 처리 로그 작성 기능 추가
	 * @param logStr
	 */
	private void writeLog(String logStr){
		try {

			if (logStr == null || logStr.length() <= 0) {
				return;
			}

			// 로그 폴더 생성
			String logFilePath = "D:" + File.separator + "FullPreProductInterfaceService";
			File logDir = new File(logFilePath);
			if (!logDir.exists()) {
				logDir.mkdir();
			}

			// 로그 파일 생성
			File logFile = new File(logFilePath + File.separator + (new SimpleDateFormat("yyyy-MM-dd")).format(new Date()) + "_" + logStr.substring(0, 11) + ".log");
			if(!logFile.exists()) { 
				logFile.createNewFile();
			}

			if (logFile != null) {        		
				// 파일이 기존 내용에 이어서 작성
				BufferedWriter fw = new BufferedWriter(new FileWriter(logFile, true));
				fw.flush();
				fw.write(logStr);
				fw.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}

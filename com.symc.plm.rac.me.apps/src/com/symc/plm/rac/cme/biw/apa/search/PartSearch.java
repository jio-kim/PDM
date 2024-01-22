// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3)

package com.symc.plm.rac.cme.biw.apa.search;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import com.ssangyong.common.remote.DataSet;
import com.ssangyong.common.remote.SYMCRemoteUtil;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.utils.BOPStructureDataUtility;
import com.symc.plm.me.utils.CustomUtil;
import com.symc.plm.rac.cme.biw.apa.APADialog;
import com.symc.plm.rac.cme.biw.apa.resulttable.datastructure.PartData;
import com.symc.plm.rac.cme.biw.apa.resulttable.datastructure.ResultData;
import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aif.kernel.AbstractAIFSession;
import com.teamcenter.rac.cme.framework.find.FindInStructureOperation;
import com.teamcenter.rac.cme.kernel.bvr.TCComponentMfgBvrWeldPoint;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.kernel.TCComponentBOPLine;
import com.teamcenter.rac.kernel.TCComponentBOPWindow;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentQuery;
import com.teamcenter.rac.kernel.TCComponentQueryType;
import com.teamcenter.rac.kernel.TCComponentRevisionRule;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.kernel.TCTextService;
import com.teamcenter.rac.pse.search.PSESearchInClassElement;
import com.teamcenter.rac.pse.search.PSESearchOperationParameters;
import com.teamcenter.rac.psebase.AbstractBOMLineViewerApplication;
import com.teamcenter.rac.util.Cookie;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;
import com.teamcenter.schemas.soa._2006_03.exceptions.ServiceException;
import com.teamcenter.services.rac.manufacturing._2010_09.Core;
import com.teamcenter.services.rac.structuremanagement.StructureSearchService;

/**
 * [SR140611-032][20140611] jwlee 결합판넬 검색 개수를 15개에서 25개로 변경.
 *  결합판넬 Dialog에 표시되는 개수를 Preference로 조절할 수 있도로 변경.
 *  결합판넬 Dialog에 표시되는 개수를 검색대상에서 Assay Item은 제외 (Part ID의 다섯번째 자리가 0이면 Assay Item이니 이것들은 검색대상에서 제외 처리함)
 * [SR140721-004][20140729] shcho, 결합판넬 Dialog에 표시되는 개수를 Dialog에서 사용자가 입력한 값으로 조절 할 수 있도로 변경.
 * [NON-SR][20150703] shcho, 검색시 BOP Line에서 VehiclePart를 찾아오던 것을, Accept시 찾도록 변경 (검색이 오래 걸리는 문제제기로 인하여 변경, 정윤재 수석 지시사항)
 * Part Search [Old]
 * [SR160901-010][20160902] taeku.jeong 용접점 검색 결과 누락되어 결과가 없는 용접점도 있음.
 *                                     1) 수기확인 결과 결과로 3개가 나와야하는 용접점의 결합파트 3개 찾음
 *                                     2) Pert에 있는지 확인 하는 절차까지 오지 않음
 *                                     3) ABS_OCC_ID 검색이 아닌 FindInStructureOperation 이용하는 방법으로 변경
 *                                     3) Item Revision이 같은것이 한번 Pert에 없다고 판단되면 다음부터는 Pert에 있는지 확인 하지 않음.
 *                                     4) 동일한 Item Revision이라도 Process에 할당되고 Pert에 있는지 확인 하도록 수정
 *                                     5) 결과는 정확하게 나옴
 *                                     6) 검색된 Connected Part를 Process Structure에서 찾는데 시간이 많이 소요됨.
 */

public class PartSearch
{
    private AbstractAIFSession session;
    protected TCSession tcsession;
    private TCComponentBOPWindow bopWindow;
    private TCComponentBOMLine bopTopBOMLine;
    Vector<String> preDessorStationIdV = null;
    
    protected AbstractBOMLineViewerApplication viewerApp;
    private int connectedPartsCount;
    private Registry registry;
    private ArrayList<TCComponentBOMLine> weldPointBOMLineList;
//    private static final Logger logger = Logger.getLogger(PartSearch.class);
    String msg;
    TCException tcExpt;
    private String oldTargetOp = "";
    private TCComponentBOMLine oldTargetOpBOMLine = null;
    private String oldTargetStation = "";
    String connectedItemType = "Vehicle Part";
    private final String serviceClassName = "com.ssangyong.service.BopPertService";
    private List<String> decessorsList;
    private List<String> bopVehPartRevPUIDList;
    
    /** MProduct에서 검색 가능한 수 제한 **/
    private int connectedMFGNumber = 0;
    
    /** Dialog에 표기 가능한 수 제한 **/
//    private int connectedNumber = 15;   //[SR140611-032][20140611] jwlee 결합판넬 Dialog에 표시되는 개수를 Preference 에서 못 가져올 경우 Default값 15

	
	private DateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss.SSS");
	private TCComponentBOMLine[] targetFunctionBOMLins = null;
	
	private Vector<TCComponentItemRevision> connectedPartRevisionV = null;

    public PartSearch(AbstractAIFSession abstractaifsession)
    {
    	// PartSearch [OLD Way]
        session = null;
        tcsession = null;
        bopWindow = null;
        viewerApp = null;
        connectedPartsCount = 0;
        registry = null;
        weldPointBOMLineList = null;
        msg = "";
        tcExpt = new TCException(msg);
        session = abstractaifsession;
        tcsession = (TCSession)session;
        registry = Registry.getRegistry(com.teamcenter.rac.cme.biw.apa.APADialog.class);
    }

    public ResultData[] searchAll(TCComponentBOMLine tccomponentbomline){
        if(tccomponentbomline == null)
            return null;
        
        weldPointBOMLineList = new ArrayList<TCComponentBOMLine>();
        identifyAllItems(tccomponentbomline);
        
        ResultData resultdata[] = null;
        if(weldPointBOMLineList.size() != 0){
            resultdata = searchConnectedParts(weldPointBOMLineList);
        }
        
        return resultdata;
    }

    public void identifyAllItems(TCComponentBOMLine tccomponentbomline){
        try {
            addMFGToList(tccomponentbomline);
            if(tccomponentbomline.isPacked()){
                TCComponentBOMLine atccomponentbomline[] = tccomponentbomline.getPackedLines();
                for(int i = 0; i < atccomponentbomline.length; i++)
                    addMFGToList(atccomponentbomline[i]);

                atccomponentbomline = null;
            }
            if(tccomponentbomline.hasChildren()){
                AIFComponentContext aaifcomponentcontext[] = tccomponentbomline.getChildren();
                for(int j = 0; j < aaifcomponentcontext.length; j++){
                    TCComponentBOMLine tccomponentbomline1 = (TCComponentBOMLine)aaifcomponentcontext[j].getComponent();
                    identifyAllItems(tccomponentbomline1);
                }

                aaifcomponentcontext = null;
            }
        }
        catch(Exception exception) {
            exception.printStackTrace();
        }
    }

    public void addMFGToList(TCComponentBOMLine tccomponentbomline) throws TCException
    {
        // jwlee 검색 or 조건 추가 WeldPoint
        if((tccomponentbomline != null && (tccomponentbomline instanceof TCComponentMfgBvrWeldPoint)) ||
            tccomponentbomline.getProperty(SDVPropertyConstant.BL_OBJECT_TYPE).equals("WeldPoint")){
            weldPointBOMLineList.add(tccomponentbomline);
        }
    }

    /**
     * Get Current ConnectedParts
     * 
     * @param weldPointBOMLineList
     * @return
     */
    public ResultData[] searchConnectedParts(ArrayList<TCComponentBOMLine> weldPointBOMLineList){
        ResultData[] arrayResultData = null;
        if(weldPointBOMLineList != null)
        {
        	
        	initPreStationIdList(weldPointBOMLineList.get(0));
        	
            arrayResultData = new ResultData[weldPointBOMLineList.size()];
            for(int i = 0; i < weldPointBOMLineList.size(); i++){
                try{
                    TCComponentBOMLine weldPointBOMLine = weldPointBOMLineList.get(i);

                    /** WeldPoint 겹수 조회 **/
                    TCComponent[] weldRevMasterForm = weldPointBOMLine.getItemRevision().getReferenceListProperty("TC_Feature_Form_Relation");
                    String sheetNumber = "0";
                    if (weldRevMasterForm.length > 0)
                    {
                        sheetNumber = weldRevMasterForm[0].getProperty("Number_of_sheets_welded");
                    }
                    
                    /** WeldPoint 정보 표기 **/
                    ResultData resultData = new ResultData();
                    resultData.addColumn("MFGName", weldPointBOMLine.toString(), weldPointBOMLine);
                    resultData.addColumn("MFG_Number_Of_Sheet", sheetNumber, null);
                    resultData.addColumn("MFGType", weldPointBOMLine.getItem().getType(), null);
                    // [SR151207-042][20151209] taeku.jeong Find No 추가
                    resultData.addColumn("MFGFindNo", weldPointBOMLine.getProperty("bl_sequence_no"), null);

                    /** Connected Part 표기 **/
                    String connectedPartsNoteValue = weldPointBOMLine.getStringProperty(SDVPropertyConstant.BL_CONNECTED_PARTS);
                    TCComponent[] arrVehPartRevision = getVehPartRevision(connectedPartsNoteValue);
                    if(arrVehPartRevision != null) {
                        int connectedPartCount = arrVehPartRevision.length;
                        if(connectedPartCount == 1 && arrVehPartRevision[0].equals(""))
                            connectedPartCount = 0;
                        //[SR140611-032][20140611] jwlee 결합판넬 검색 개수를 15개에서 25개로 변경.
                        for(int k = 0; k < connectedPartCount && k < 25; k++) {
                            String key = (new StringBuilder()).append("Part").append(k + 1).toString();
                            resultData.addColumn(key, new PartData(arrVehPartRevision[k], true, false), arrVehPartRevision[k]);                        
                        }
                    }

                    arrayResultData[i] = resultData;
                }
                catch(Exception tcexception){
                    tcexception.printStackTrace();
                }
            }
        }
        return arrayResultData;
    }

    private TCComponent[] getVehPartRevision(String connectedPartsNoteValue) throws Exception {
        if(connectedPartsNoteValue == null || connectedPartsNoteValue.length() == 0) {
            return null;
        }

        String[] arrVehPartDisplayValue = connectedPartsNoteValue.split(",");
        TCComponent[] arrVehPartRevision = new TCComponent[arrVehPartDisplayValue.length];
        
        int j = 0;
        for (String displayValue : arrVehPartDisplayValue) {
            displayValue = displayValue.trim();
            String endItemID = displayValue.substring(0, displayValue.indexOf("/")); 
            String endItemRev = displayValue.substring(displayValue.indexOf("/") + 1);
            TCComponentItemRevision vehPartRevision = CustomUtil.findItemRevision(SDVTypeConstant.EBOM_VEH_PART_REV, endItemID, endItemRev);
            arrVehPartRevision[j] = vehPartRevision;
            j++;
        }
        
        return arrVehPartRevision;
    }

    /**
     * Search Connected Part 
     * 
     * [SR140721-004][20140729] shcho, 결합판넬 Dialog에 표시되는 개수를 Dialog에서 사용자가 입력한 값으로 조절 할 수 있도로 변경.
     * 
     * @param weldPointBOMLines
     * @param abstractbomlineviewerapplication
     * @param revisionRule 
     * @param arrDisplayData
     * @param productBOMLine
     * @return
     */
    public ResultData[] searchClosestParts(TCComponent weldPointBOMLines[], AbstractBOMLineViewerApplication abstractbomlineviewerapplication, TCComponentRevisionRule revisionRule, ResultData arrDisplayData[], TCComponentBOMLine productBOMLine){
    	
    	// Connected Part Sear 기존의 M-Product에서 검색 하는 방법
    	
		System.out.println(
				"// --------------------------------\n"+
				"// searchClosestParts [Function Start] : "+df.format(new Date())+"\n"+
				"// --------------------------------\n");
    	
		initPreStationIdList((TCComponentBOMLine)weldPointBOMLines[0]);
		
        try {
            viewerApp = abstractbomlineviewerapplication;
            bopWindow = (TCComponentBOPWindow) ((TCComponentBOMLine)weldPointBOMLines[0]).window();
            if(bopWindow!=null){
            	bopTopBOMLine = bopWindow.getTopBOMLine();
            }
            TCComponentBOMWindow productWindow = productBOMLine.window();

            String searchType = tcsession.getPreferenceService().getStringValue("MEAPCSearchType"); //S7_Vehpart

            TCTextService text_service = ((TCSession) session).getTextService();
            String type = text_service.getTextValue("Type");
            String keyItemId = text_service.getTextValue("ItemID");
            String keyOwningGroup = text_service.getTextValue("OwningGroup");
            
            // jwlee 추가 부분 TC_Preference 에 'MEAPCConnectedType' 에서 저장된 값을 가져온다. 값 : Vehicle Part
            connectedItemType = tcsession.getPreferenceService().getStringValue("MEAPCConnectedType");
            // jwlee MProduct 에서 검색해서 가져올 판넬에 갯수 지정
            if (connectedMFGNumber == 0) {
                String mfgNumber = tcsession.getPreferenceService().getStringValue("MEAPCConnectedMFGNumber");
                connectedMFGNumber = Integer.parseInt(mfgNumber);
            }

            // 용접 대상인 차체 부품에 해당 하는 부품을 검색하기위한 Query의 조건을 정의 한다.
            //String[] as = {type, keyItemId, keyOwningGroup};
            //String[] as1 = {searchType, "5*;6*", "BODY DESIGN1.SYMC;BODY DESIGN2.SYMC"};
            
            // 검색 결과에 누락 되는 Data가 있어서 Owning Group은 제거....
            String[] as = {type, keyItemId};
            String[] as1 = {searchType, "5*;6*"};
            
            TCComponentBOMLine arrWeldPointBOMLine[] = new TCComponentBOPLine[1];

            if(weldPointBOMLines!=null && weldPointBOMLines[0]!=null && weldPointBOMLines[0] instanceof TCComponentBOMLine){
                initBOPVehPartRevPUIDList((TCComponentBOPLine)weldPointBOMLines[0]);
            }
            
            targetFunctionBOMLins = FindConnectedPartUtility.geConnectedPartSearchTargetBOMLine(productWindow);
            
            for(int i = 0; i < weldPointBOMLines.length; i++){
                // 중복된 PART 는 한개만 빼고 회색 처리 하기 위해 리스트에 담아둔다 (이미 검색된 결과값에 ID 저장)
                List<String> resultIDList = new ArrayList<String>();
            	connectedPartRevisionV = new Vector<TCComponentItemRevision>();
                resultIDList = setConnectedPartIDList(arrDisplayData[i]);

                arrWeldPointBOMLine[0] = (TCComponentBOPLine)weldPointBOMLines[i];
                 
        		System.out.println(
        				"// --------------------------------\n"+
        				"// "+weldPointBOMLines[i].toString()+" [Taget"+i+" Start] : "+df.format(new Date())+"\n"+
        				"// --------------------------------\n");
                
                String connectedPartNoteValue = arrWeldPointBOMLine[0].getStringProperty(SDVPropertyConstant.BL_CONNECTED_PARTS);
                connectedPartsCount =  connectedPartNoteValue.length() == 0 ? 0 :connectedPartNoteValue.split(",").length;
                
                System.out.println(weldPointBOMLines[i]+" Current connected parts count = "+connectedPartsCount);

                // [NON-SR][20150626] shcho, 17개에서 25개로 변경 (화면이 오래전에 25개 까지 보여주는 것으로 변경되었는데 이를 반영하지 않았음.)
                if(connectedPartsCount < 25){
                    TCComponentQueryType tccomponentquerytype = (TCComponentQueryType)tcsession.getTypeComponent("ImanQuery");
                    TCComponentQuery tccomponentquery = (TCComponentQuery)tccomponentquerytype.find("Item...");
                    
            		boolean enableTrushapeFiltering = true;
            		boolean isReturnScopedSubTreesHit = true;
            		boolean isIncludeChildBomLines = true;
            		boolean executeVOOFilter = false;
            		boolean performRemoteSearch = false;
            		
                    PSESearchOperationParameters psesearchoperationparameters = new PSESearchOperationParameters(revisionRule, 
                    		getProximityDistance(), arrWeldPointBOMLine, tccomponentquery, as, as1, 
                    		null, null, null, null, null, null, null, null, null, null, null, null, 
                    		enableTrushapeFiltering, 
                    		isReturnScopedSubTreesHit, isIncludeChildBomLines, 
                    		executeVOOFilter, performRemoteSearch,
                    		null);

                    /** Product Window 에서 결합판넬 대상 Vehicle BOMLine 검색 **/
                    getRequiredNumberofParts(psesearchoperationparameters, productWindow, arrDisplayData[i]);
                    
                    
                    // 검색결과를 확인
                    if(connectedPartRevisionV!=null && connectedPartRevisionV.size()>0){
               			List<TCComponent> vehPartRevisionList = new ArrayList<TCComponent>();
                    	for (int j = 0;connectedPartRevisionV!=null && j < connectedPartRevisionV.size(); j++) {
                    		TCComponentItemRevision searchedItemRevision = connectedPartRevisionV.get(j);
                    		vehPartRevisionList.add(searchedItemRevision);
						}
                        
                        int partIndex = connectedPartsCount;
                        for(int k = 0;vehPartRevisionList!=null && k < vehPartRevisionList.size(); k++){
                        	
                            // 중복을 체크하여 색을 준다 statusFlag 의 값이 True 면 검정, False 면 회색처리를 할수 있다
                            boolean statusFlag = true;
                            TCComponentItemRevision vehPartRevision = (TCComponentItemRevision) vehPartRevisionList.get(k);
                            if (resultIDList.contains(vehPartRevision.getStringProperty(SDVPropertyConstant.ITEM_ITEM_ID))) {
                                statusFlag = false;
                            }
                            arrDisplayData[i].addColumn((new StringBuilder()).append("Part").append(partIndex + 1).toString(), new PartData(vehPartRevision, statusFlag, true), vehPartRevision);
                            resultIDList.add(vehPartRevision.getStringProperty(SDVPropertyConstant.ITEM_ITEM_ID));
                            
                        	partIndex++;
                        }
                    }

                }
                
        		System.out.println(
        				"// --------------------------------\n"+
        				"// "+weldPointBOMLines[i].toString()+" [Taget"+i+" End] : "+df.format(new Date())+"\n"+
        				"// --------------------------------\n");
            }
        }
        catch(Exception exception) {
            exception.printStackTrace();
            MessageBox.post(AIFDesktop.getActiveDesktop().getShell(), exception.toString(), "ERROR", MessageBox.ERROR);
        }finally{
    		System.out.println(
    				"// --------------------------------\n"+
    				"// searchClosestParts [Function End] : "+df.format(new Date())+"\n"+
    				"// --------------------------------\n");

        }
        
        return arrDisplayData;
    }

    /**
     * 
     * @param resultdata
     * @return
     */
    protected List<String> setConnectedPartIDList(ResultData resultdata) {
        List<String> resultIDList = new ArrayList<String>();
        int columnSize = resultdata.getColumnSize();
        int partIndex = 1;
        if (columnSize > 3) {
            for (int k = 0; k < columnSize; k++) {
                String columnValue = resultdata.getColumnValue("Part" + (partIndex));
                if (columnValue != null) {
                    String connectedPartId = columnValue.substring(0, columnValue.indexOf("/"));
                    resultIDList.add(connectedPartId);
                }
                partIndex++;
            }
        }
        return resultIDList;
    }

    public boolean isDuplicate(TCComponentItemRevision itemRevision) {
    	
        int i = connectedPartRevisionV.size();
        String s = itemRevision.getUid();
        for (int j = 0; j < i; j++) {
            if (s.equals(((TCComponentItemRevision) connectedPartRevisionV.get(j)).getUid())) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings({ "rawtypes", "unused" })
    public void getRequiredNumberofParts(PSESearchOperationParameters psesearchoperationparameters, TCComponentBOMWindow tccomponentbomwindow, ResultData resultdata){
        Object obj = null;
        ArrayList<TCComponent> connectedPartRevisionList = new ArrayList<TCComponent>();
        PSESearchInClassElement apsesearchinclasselement[] = psesearchoperationparameters.getInClassViewNames();
        Hashtable hashtable = resultdata.getRowContext();
        for(int i = 1; i <= connectedPartsCount; i++){
            connectedPartRevisionList.add((TCComponentItemRevision)hashtable.get((new StringBuilder()).append("Part").append(i).toString()));
        }

        if(apsesearchinclasselement != null && apsesearchinclasselement.length > 0){
            String as[] = new String[apsesearchinclasselement.length];
            for(int j = 0; j < apsesearchinclasselement.length; j++){
                as[j] = psesearchoperationparameters.getInClassViewNames()[j].getClassId();
            }
        }

        com.teamcenter.services.rac.structuremanagement._2010_09.StructureSearch.SearchExpressionSet searchexpressionset = new com.teamcenter.services.rac.structuremanagement._2010_09.StructureSearch.SearchExpressionSet();
        com.teamcenter.services.rac.structuremanagement._2008_05.StructureSearch.AttributeExpression aattributeexpression[] = new com.teamcenter.services.rac.structuremanagement._2008_05.StructureSearch.AttributeExpression[0];
        com.teamcenter.services.rac.structuremanagement._2008_05.StructureSearch.AttributeExpression aattributeexpression1[] = psesearchoperationparameters.buildAttributeExpression();
        com.teamcenter.services.rac.structuremanagement._2008_05.StructureSearch.ProximityExpression aproximityexpression[] = psesearchoperationparameters.buildProximityExpressions();
        com.teamcenter.services.rac.structuremanagement._2008_05.StructureSearch.BoxZoneExpression aboxzoneexpression[] = psesearchoperationparameters.buildBoxZoneExpressions();
        com.teamcenter.services.rac.structuremanagement._2008_05.StructureSearch.SavedQueryExpression asavedqueryexpression[] = psesearchoperationparameters.buildSavedQueryExpressions();
        com.teamcenter.services.rac.structuremanagement._2008_05.StructureSearch.OccurrenceNoteExpression aoccurrencenoteexpression[] = psesearchoperationparameters.buildOccurrenceNoteExpressions();
        com.teamcenter.services.rac.structuremanagement._2008_05.StructureSearch.FormAttributeExpression aformattributeexpression[] = psesearchoperationparameters.buildFormAttributeExpressions();
        com.teamcenter.services.rac.structuremanagement._2010_04.StructureSearch.PlaneZoneExpression aplanezoneexpression[] = new com.teamcenter.services.rac.structuremanagement._2010_04.StructureSearch.PlaneZoneExpression[0];
        com.teamcenter.services.rac.structuremanagement._2008_05.StructureSearch.InClassExpression ainclassexpression[] = new com.teamcenter.services.rac.structuremanagement._2008_05.StructureSearch.InClassExpression[0];
        com.teamcenter.services.rac.structuremanagement._2008_05.StructureSearch.SearchScope searchscope = new com.teamcenter.services.rac.structuremanagement._2008_05.StructureSearch.SearchScope();
        searchscope.window = tccomponentbomwindow;
        TCComponentBOMLine atccomponentbomline[] = new TCComponentBOMLine[0];
        if(targetFunctionBOMLins!=null && targetFunctionBOMLins.length>0){
        	searchscope.scopeBomLines = targetFunctionBOMLins;
        }else{
        	searchscope.scopeBomLines = atccomponentbomline;
        }
        searchexpressionset.itemAndRevisionAttributeExpressions = aattributeexpression1;
        searchexpressionset.itemAndRevisionAttributeExpressions = aattributeexpression;
        searchexpressionset.occurrenceNoteExpressions = aoccurrencenoteexpression;
        searchexpressionset.formAttributeExpressions = aformattributeexpression;
        searchexpressionset.proximitySearchExpressions = aproximityexpression;
        searchexpressionset.boxZoneExpressions = aboxzoneexpression;
        searchexpressionset.planeZoneExpressions = aplanezoneexpression;
        searchexpressionset.savedQueryExpressions = asavedqueryexpression;
        searchexpressionset.inClassQueryExpressions = ainclassexpression;
        searchexpressionset.doTrushapeRefinement = psesearchoperationparameters.isTrushapeFilterEnabled();
        
        String startDateTime = df.format(new Date());
        
        StructureSearchService structuresearchservice = StructureSearchService.getService(tcsession);
        com.teamcenter.services.rac.structuremanagement._2008_05.StructureSearch.StructureSearchResultResponse structuresearchresultresponse = null;

        Vector<TCComponentBOMLine> findedBOMLine = new Vector<TCComponentBOMLine>(); 
        // --------------------------------------------------        
        // Structure Search 를 수행 한다.
        // --------------------------------------------------
        try {
            structuresearchresultresponse = structuresearchservice.startSearch(searchscope, searchexpressionset);
            
            // 검색결과 근접 Part의 BOMLine 정보를 확인하기위한 While Loop의 조건을 초기화 한다.
            boolean isInCondition = true;
            while (isInCondition == true) {
            	
            	structuresearchresultresponse = structuresearchservice.nextSearch(structuresearchresultresponse.searchCursor);
            	
            	// 검색결과에 포함된 근접 Part BOMLine을 배열에 담는다.
                 for (int i = 0;structuresearchresultresponse!=null && i < structuresearchresultresponse.bomLines.length; i++) {
                     TCComponentBOMLine searchedBOMLine = structuresearchresultresponse.bomLines[i];
                     if(searchedBOMLine!=null){
                    	 if(findedBOMLine.contains(searchedBOMLine)==false){
                    		 findedBOMLine.add(searchedBOMLine);
                    	 }
                     }
 				}
                 
             	// 검색 결과가 더이상 없거나 검색결과응답이 종료되는 경우 While Loop를 종료한다.
             	if(structuresearchresultresponse == null || structuresearchresultresponse.finished){
             		isInCondition = false;
             		continue;
             	}
				
			}

        } catch(ServiceException serviceexception){
            serviceexception.printStackTrace();
        } catch(Exception exception){
            exception.printStackTrace();
        }finally{
        	try {
        		structuresearchresultresponse = structuresearchservice.stopSearch(structuresearchresultresponse.searchCursor);
			} catch (ServiceException e) {
				e.printStackTrace();
			}
        	
        	// 검색결과에 포함된 근접 Part BOMLine을 배열에 담는다.
            for (int i = 0;structuresearchresultresponse!=null && i < structuresearchresultresponse.bomLines.length; i++) {
                TCComponentBOMLine searchedBOMLine = structuresearchresultresponse.bomLines[i];
                if(searchedBOMLine!=null){
               	 if(findedBOMLine.contains(searchedBOMLine)==false){
               		 findedBOMLine.add(searchedBOMLine);
               	 }
                }
			}

        }
        
        String endDateTime = df.format(new Date());
        
        System.out.println("Connected Part Search : "+startDateTime+" -> "+endDateTime);
        
        if(findedBOMLine!=null && findedBOMLine.size()>0){
        	System.out.println("findedBOMLine.size() ="+findedBOMLine.size());
        	getAssignedProductPartBOMLine(findedBOMLine);
        }

    }
    
    /**
     * [20160902] taeku.jeong 이 함수는 Product에서 찾은 Part를 Process에서 효율적으로 찾기위한 방법이 필요해서
     * Test 및 개발을 위해 만드는 함수임.
     * @param productBOMLines
     */
    private void getAssignedProductPartBOMLine(Vector<TCComponentBOMLine> findedBOMLineV){
    	
    	// 검색된 Connected Part를 Process에서 찾는 방법....
    	// 우선 Process에 할당된 Part들은 abs_occ_id 값을 가지고 있음
    	// abs_occ_id 값이 없는 경우는 검토대상이 아님을 할 수 있다.

    	Vector<TCComponentBOMLine> findedBOMLineV2 = new Vector<TCComponentBOMLine>(); 
    	for (int i = 0; findedBOMLineV!=null && i < findedBOMLineV.size(); i++) {
    		TCComponentBOMLine bomLine = findedBOMLineV.get(i);
    		
    		String partId = null;
			try {
				partId = bomLine.getProperty(SDVPropertyConstant.BL_ITEM_ID);
			} catch (TCException e1) {
				e1.printStackTrace();
			}
    		
            //[SR140611-032][20140611] jwlee 결합판넬 Dialog에 표시되는 개수를 검색대상에서 Assay Item은 제외 
			// (Part ID의 다섯번째 자리가 0이면 Assay Item이니 이것들은 검색대상에서 제외 처리함)
            if (partId.length() > 5) {
                if (partId.charAt(4) == '0') {
                    continue;
                }
            }
    		
    		System.out.println("Finded Connected Parts["+i+"] = "+bomLine);
    		findedBOMLineV2.add(bomLine);
    		
    	}
    	
    	System.out.println("connectedPartRevisionV.size() = "+connectedPartRevisionV.size()+" / connectedMFGNumber = "+connectedMFGNumber);

    	String startDateTime = df.format(new Date());
    	
    	Vector<String> inPertProcessIdV = new Vector<String>(); 
    	Vector<TCComponentBOMLine> inPertBOMLinesV = new Vector<TCComponentBOMLine>();
    	for (int i = 0; findedBOMLineV2!=null && i < findedBOMLineV2.size(); i++) {
    		
    		// 25개를 초과하면 더 이상 찾지 않는다.
            if(connectedPartRevisionV.size() == connectedMFGNumber){
                continue;
            }
    		
    		TCComponentBOMLine bomLine = findedBOMLineV2.get(i);

    		String partId = null;
    		TCComponentItemRevision tempItemRevision = null;
			try {
				tempItemRevision = bomLine.getItemRevision();
				partId = bomLine.getProperty(SDVPropertyConstant.BL_ITEM_ID);
			} catch (TCException e1) {
				e1.printStackTrace();
			}
			
            //중복 BOMLine 제외
            if(isDuplicate(tempItemRevision)==true){
                continue;
            }
    		
            // Product에서 Process에 할당한 Part는 System에 의해 absOccId가 입력된다.
            // ABS Occ Id가 없는 Part는 검색할 필요가 없다.
            // FindInStructureOperation을 사용하면 정확도는 높으나 abs occ id 값이 없는것 까지
            // Process에서 찾으므로 시간이 너무 많이 걸린다. (2016/09/05)
    		String absOccId = findAbsOccId(bomLine);
    		
    		if( absOccId!=null && absOccId.trim().length()>0 ){
    			TCComponentBOMLine[] finded = null;
    			try {
					finded = this.bopWindow.findConfigedBOMLinesForAbsOccID(absOccId, true, bopTopBOMLine);
				} catch (TCException e) {
					e.printStackTrace();
				}
    			
    			boolean isInPert = false;
    			
    			// Part가 할당된 Process의 ID를 이용해 Pert 구성에 있는 Process인지 확인 한다.
    			for (int j = 0; finded!=null && j < finded.length; j++) {
    				
    				// 할당된 Operation Id로 관련된 Operation에 포함되 었는지 확인 한다.
	    			String assignedProcessId = getOperationId(finded[j]);
	    			
	    			System.out.println("assignedProcessId = "+assignedProcessId);
	    			if(inPertProcessIdV.contains(assignedProcessId)==true){
	    				isInPert = true;
	    				break;
					}
	    			
	    			if(assignedProcessId!=null && assignedProcessId.trim().length()>0){
	    				if(oldTargetOp.trim().equalsIgnoreCase(assignedProcessId.trim()) ==true ){
	    					// 검토 대상 Operation에 포함된 Part 임.
	    					isInPert = true;
	    					if(inPertProcessIdV.contains(assignedProcessId)==false){
	    						inPertProcessIdV.add(assignedProcessId);
	    					}
	    				}
	    				
	    				if(isInPert==true){
	    					break;
	    				}
	    			}
	    			
	    			// 할당된 Station Id로 선행 Pert경로에 포함되어 있는지 확인
	    			assignedProcessId = getStationId(finded[j]);
	    			System.out.println("assignedProcessId = "+assignedProcessId);
	    			if(inPertProcessIdV.contains(assignedProcessId)==true){
	    				isInPert = true;
	    				break;
					}
	    			
	    			if(assignedProcessId!=null && assignedProcessId.trim().length()>0){
	    				if(this.preDessorStationIdV!=null && this.preDessorStationIdV.contains(assignedProcessId.trim())==true){
	    					// 검토 대상 Pert 경로에 포함된 Part 임.
	    					isInPert = true;
	    					if(inPertProcessIdV.contains(assignedProcessId)==false){
	    						inPertProcessIdV.add(assignedProcessId);
	    					}
	    				}

	    				if(isInPert==true){
	    					break;
	    				}
	    			}
	    			
				}
    			
    			if(isInPert==true){
    				inPertBOMLinesV.add(bomLine);
    				
    				if(connectedPartRevisionV!=null && connectedPartRevisionV.contains(tempItemRevision)==false){
    					connectedPartRevisionV.add(tempItemRevision);
    				}
    			}
    			
    		}
		}
    	
    	inPertProcessIdV.clear();
    	inPertProcessIdV = null;
    	
		String endDateTime = df.format(new Date());
    	
		int inpertCount = 0;
    	if(inPertBOMLinesV!=null && inPertBOMLinesV.size()>0){
    		inpertCount = inPertBOMLinesV.size();
    	}

    	String messageStr = "Finded in pert : "+inpertCount+"/"+findedBOMLineV.size() +" ["+startDateTime+" -> "+endDateTime+"]";
    	
    	System.out.println(messageStr);
    }
    
    private String findAbsOccId(TCComponentBOMLine bomLine){
    	
    	String absOccId = null;
		try {
			absOccId = bomLine.getProperty(SDVPropertyConstant.BL_ABS_OCC_ID);
		} catch (TCException e) {
			e.printStackTrace();
		}
		
		if(absOccId==null || (absOccId!=null && absOccId.trim().length()<1)){
			TCComponentBOMLine parentBOMLine = null;
			try {
				parentBOMLine = bomLine.parent();
			} catch (TCException e) {
				e.printStackTrace();
			}
			if(parentBOMLine!=null){
				String itemRevisionType = null;
				try {
					itemRevisionType = parentBOMLine.getItemRevision().getType();
				} catch (TCException e) {
					e.printStackTrace();
				}
				if(itemRevisionType!=null && itemRevisionType.trim().equalsIgnoreCase(SDVTypeConstant.EBOM_FUNCTION_MAST_REV)==true){
					return null;
				}
				return findAbsOccId(parentBOMLine);
			}
		}
		
		return absOccId;
		
    }

    public double getProximityDistance_OLD() {
        double d;
        try {
            double d1 = viewerApp.getViewableTreeTable().getAssemblyUnits();
            if(d1 <= 0.0D){
                d1 = 1.0D;
            }
            Cookie cookie = Cookie.getCookie("MEAPACookie", true);
            String s = cookie.getString((new StringBuilder()).append(APADialog.class.getName()).append(".MAX_PROXIMITY_DISTANCE").toString());
            try {
                cookie.close();
            }
            catch(IOException ioexception){
                ioexception.printStackTrace();
            }
            if(s == null || s == ""){
                s = registry.getString("APADialog.DefaultProximityDistance");
            }
            double d2 = Double.parseDouble(s);
            d = d2 * d1;
        }catch(Exception exception){
            d = 1.0D;
            exception.printStackTrace();
        }
        return d;
    }
    
    /**
     * [NONE-SR][20151216] taeku.jeong ConnectedPart찾을때 거리환산부분이 BOMStructure마다 다르므로
     * 적합한 환산이 되도록 수정함.
     * 사용자가 입력한 기준은 밀리미터 단위로 기준을 잡음.
     * @return
     */
    public double getProximityDistance() {
    	
        // 현재선택된 BOM Tree의 기준 단위 정보를 읽어온다.
    	double assemblyUnit = viewerApp.getViewableTreeTable().getAssemblyUnits();
    	
    	// 쿠키에 저장된 Connected Part 검색 기본 거리를 읽어 온다.
		double cookieSavedDoubleValue = 1; 
        try {
            Cookie cookie = Cookie.getCookie("MEAPACookie", true);
            
            // Override된 Class를 읽어오면 실제 저장된 Cookie 이름과 다르므로 원래 Class의 이름을 가져와야 한다.
            String cName = com.teamcenter.rac.cme.biw.apa.APADialog.class.getName();
            String cookieValueName = cName+ ".MAX_PROXIMITY_DISTANCE";

            String cookieSavedValueString = cookie.getString(cookieValueName);
            try {
                cookie.close();
            }
            catch(IOException ioexception){
                ioexception.printStackTrace();
            }
            
            if(cookieSavedValueString == null || cookieSavedValueString == ""){
                cookieSavedValueString = registry.getString("APADialog.DefaultProximityDistance");
            }
            
            cookieSavedDoubleValue = Double.parseDouble(cookieSavedValueString);
            
        }catch(Exception exception){
            exception.printStackTrace();
        }finally{
        	
        }
        
        // 단위 환산부분.
        // Structure Search의 검색에 사용되는 단위계가 m 인것으로 판단.
		if (assemblyUnit == 0.0254D){
			// inch
			cookieSavedDoubleValue = cookieSavedDoubleValue * assemblyUnit;
			System.out.println("Structure Unit : inch");
		}else if (assemblyUnit == 0.001D){
			// millimeter
			cookieSavedDoubleValue = cookieSavedDoubleValue * assemblyUnit;
			System.out.println("Structure Unit : millimeter");
		}else if (assemblyUnit == 1.0D){
			// meter
			// 사용자가 입력할때 mm 단위를 기준으로 입력 하므로
			// m 단위라도 mm로 환산한다.
			cookieSavedDoubleValue = cookieSavedDoubleValue * 0.001D;
			System.out.println("Structure Unit : meter");
		}else{
			cookieSavedDoubleValue = 0.001;
		}
        
        return cookieSavedDoubleValue;
    }
    
    public void initBOPVehPartRevPUIDList(TCComponent weldBOMLineComponent)  throws Exception{
    	
        String targetOp = "";
        String targetStation = "";

        // 1. 용접점 상위 용접 공법에 연결된 공법 확인
        TCComponentBOPLine weldOPBOMLine = (TCComponentBOPLine) ((TCComponentBOPLine)weldBOMLineComponent).parent();
        TCComponent targetComponent = weldOPBOMLine.getItemRevision().getReferenceProperty("m7_TARGET_OP");

        if (targetComponent != null) {
            targetOp = targetComponent.getProperty(SDVPropertyConstant.ITEM_ITEM_ID);
            targetStation = weldOPBOMLine.parent().getProperty(SDVPropertyConstant.BL_ITEM_ID);

            // 20140226 타겟공법이 같으면 E/I 추출을 하지 않는다
            if (!oldTargetOp.equals(targetOp)) {
                // 2. 해당 공정에 상위 공정 리스트를 가져온다
                if (!oldTargetStation.equals(targetStation)) {
                    TCComponentBOPLine topLine = (TCComponentBOPLine) bopWindow.getTopBOMLine();
                    decessorsList = getDecessorsList(weldOPBOMLine, topLine.getProperty(SDVPropertyConstant.BL_ITEM_ID), targetStation);
                }
            }
        }
        
        decessorsList.add(targetOp);
        
        oldTargetOp = targetOp;
        oldTargetStation = targetStation;
    }

    /**
     *  jwlee 용접공법과 연결된 공법에 E/I 과 비교하여 중복 및 다른 아이템을 제거한다
     *  1. 용접점 상위 용접 공법에 연결된 공법 확인
     *    - 용접공법의 속성값으로 추가된 M7_TARGET_OP 값을 가져온다
     *  2. 상위 공정 리스트를 가져온다
     *  3. 상위 공정과 TargetOP의 PART Revision PUID 리스트를 가져온다
     *  4. BOP에서 가져온 PUID 와 MProduct 에서 검색된 PART Revision PUID를 비교한다
     *  5. 중복제거
     * @throws Exception
     *
    **/
    public List<TCComponent> getConnectedPartRevision(List<TCComponent> serachedRevisionList, TCComponentBOPWindow bopWindow) throws Exception {

        List<TCComponent> vehPartRevisionList = new ArrayList<TCComponent>();
        
        // 3. 상위 공정과 TargetOP의 PART 리스트를 가져온다
        bopVehPartRevPUIDList = new ArrayList<String>();
        if(decessorsList!=null && decessorsList.size()>0){
        	bopVehPartRevPUIDList = getDBSelectList(serachedRevisionList, oldTargetOp, decessorsList);
        }
        
        // 4. 가져온 PART 와 MProduct 에서 검색된 PART 를 비교한다
        for (TCComponent tcComponent : serachedRevisionList) {     
            TCComponentItemRevision vehPartRevision = (TCComponentItemRevision) tcComponent;
            if (bopVehPartRevPUIDList.contains(vehPartRevision.getUid())) {
                vehPartRevisionList.add(vehPartRevision);
            }
        }
        
        for (int i = 0;vehPartRevisionList!=null && i < vehPartRevisionList.size(); i++) {
			System.out.println("[$$2] = "+vehPartRevisionList.get(i));
		}

        // 5. 중복제거 (중복 BOMLine 제거)
//        List<TCComponent> newList = new ArrayList<TCComponent>(new HashSet<TCComponent>(vehPartRevisionList));
        
        // 6. 중복제거
        List<TCComponent> uniqueRevisionList = removeDuplication(vehPartRevisionList);
        
        for (int i = 0;uniqueRevisionList!=null && i < uniqueRevisionList.size(); i++) {
			System.out.println("[$$3] = "+uniqueRevisionList.get(i));
		}

        return uniqueRevisionList;

    }

    /**
     * 검색된 결과에 중복을 제거한다
     *
     * @method duplicationCheck
     * @date 2014. 4. 29.
     * @param
     * @return List<TCComponent>
     * @throws TCException
     * @exception
     * @throws
     * @see
     */
    public List<TCComponent> removeDuplication(List<TCComponent> vehPartRevisionList) throws TCException {
        List<TCComponent> uniqueRevisionList = new ArrayList<TCComponent>();
        
        for (TCComponent tcComponent : vehPartRevisionList) {
            if (!uniqueRevisionList.contains(tcComponent)) {
                uniqueRevisionList.add(tcComponent);
            }
        }
        
        return uniqueRevisionList;
    }
        
    /**
     * 상위 공정 (Predecessor 공정)의 part item list 를 가져온다
     *
     * @method getDBSelectList
     * @date 2014. 4. 2.
     * @param
     * @return List<String>
     * @exception
     * @throws
     * @see
     */
    @SuppressWarnings("unchecked")
    public List<String> getDBSelectList(List<TCComponent> ItemRevisionList, String opID, List<String> decessorsList) throws Exception{
        List<String> connectedToPartList = new ArrayList<String>();
        List<String> connectedToPartResultList = new ArrayList<String>();
        SYMCRemoteUtil remoteUtil = new SYMCRemoteUtil();

        for (TCComponent itemRevision : ItemRevisionList) {
            connectedToPartList.add(((TCComponentItemRevision)itemRevision).getUid());
        }

        DataSet ds = new DataSet();
        if (connectedToPartList.size() > 0) {
            ds.put("REVPUID_LIST", connectedToPartList);
        }else{
            return null;
        }
        
        ds.put("STATION_LIST", decessorsList);
        
        for (int i = 0; i < decessorsList.size(); i++) {
			System.out.println("DecessorsList["+i+"] = "+decessorsList.get(i));
		}

        ArrayList<HashMap<String, Object>> results;

        results = (ArrayList<HashMap<String, Object>>) remoteUtil.execute(serviceClassName, "selectBopVehpartList", ds);
        if (results.size() > 0) {
            for (HashMap<String, Object> result : results) {
                connectedToPartResultList.add(result.get("CHILD_REVPUID").toString().trim());
            }
        }
        
        return connectedToPartResultList;
    }

    /**
     * 현재 선택된 용접점이 속한 공정의 상위 공정 리스트를 가져온다
     *
     * @method getDecessorsList
     * @date 2014. 3. 21.
     * @param
     * @return List<String>
     * @exception
     * @throws
     * @see
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public List<String> getDecessorsList(TCComponentBOMLine bomLine, String shopID, String stationID) throws Exception{

    	// [NON-SR][20160128] Taeku.jeong PERT 연결정보를 이용해서 decessorsList 찾는 부분의 Query 수행 속도가
    	// 너무 느려져서 검색 방법을 개선하는 일환으로 변경함.
    	// 함수의 TCComponentBOMLine bomLine Argument 추가 했음.

    	BOPStructureDataUtility aBOPStructureDataUtility = new BOPStructureDataUtility();
        aBOPStructureDataUtility.initBOMLineData(bomLine);
        String lineId = aBOPStructureDataUtility.getLineId();
        String findKeyCode = aBOPStructureDataUtility.getLatestKeyCodeForShop(shopID);
        
        List<String> decessorsList = new ArrayList<String>();
        List<HashMap> decessorsStationDataList  = aBOPStructureDataUtility.getPredecessorStationsAtAllLine(findKeyCode, lineId, stationID);
        for (int i = 0; decessorsStationDataList!=null && i < decessorsStationDataList.size(); i++) {
			//STATION_ID, APP_NODE_PUID
        	HashMap rowData = decessorsStationDataList.get(i);
        	String tempStatoinId = (String)rowData.get("STATION_ID");
        	String tempStatoinIdAppNodePuid = (String)rowData.get("APP_NODE_PUID");
        	decessorsList.add(tempStatoinId);
		}
        
        return decessorsList;
    }
    
    /**
     * 할당된 Part의 Parent 노드까지 역전개 하면서 Operation 또는 Sataion을 만날때 까지 연전개해서 찾은 Operation ID를 Return 한다.
     * @param bomLine
     * @return
     */
    private String getOperationId(TCComponentBOMLine bomLine){
    	
    	if(bomLine!=null){
    		
        	String currentItemType = null;
    		try {
    			currentItemType = bomLine.getItem().getType();
    			
    			if(currentItemType!=null && (
    					currentItemType.trim().equalsIgnoreCase(SDVTypeConstant.BOP_PROCESS_BODY_OPERATION_ITEM)==true
    					|| currentItemType.trim().equalsIgnoreCase(SDVTypeConstant.BOP_PROCESS_STATION_ITEM)==true
    					) ){
    				
    				String currentItemId = bomLine.getItem().getProperty("item_id");
    				return currentItemId;
    			}
    			
    		} catch (TCException e) {
    			e.printStackTrace();
    		}
    		
        	try {
    			TCComponentBOMLine parentBOMLine = bomLine.parent();
    			if(parentBOMLine!=null){
    				return getOperationId(parentBOMLine);	
    			}
    		} catch (TCException e) {
    			e.printStackTrace();
    		}
    	}
    	
    	return null;
    }
    
    private String getStationId(TCComponentBOMLine bomLine){
    	
    	if(bomLine!=null){
    		
        	String currentItemType = null;
    		try {
    			currentItemType = bomLine.getItem().getType();
    			
    			if(currentItemType!=null && 
    					currentItemType.trim().equalsIgnoreCase(SDVTypeConstant.BOP_PROCESS_STATION_ITEM)==true ){
    				
    				String currentItemId = bomLine.getItem().getProperty("item_id");
    				return currentItemId;
    			}
    			
    		} catch (TCException e) {
    			e.printStackTrace();
    		}
    		
        	try {
    			TCComponentBOMLine parentBOMLine = bomLine.parent();
    			if(parentBOMLine!=null){
    				return getStationId(parentBOMLine);	
    			}
    		} catch (TCException e) {
    			e.printStackTrace();
    		}
    	}
    	
    	return null;
    }
    
    /**
     * Pert Chart를 통해 구성한 Pert의 순서를 기준으로 현재 선택된 Station 보다 선행하는 공정의 Station 목록을
     * 가져온다.
     * @param aTargetWeldPointBOMLine
     * @return
     */
    private void initPreStationIdList(TCComponentBOMLine aTargetWeldPointBOMLine){
    	
    	preDessorStationIdV = null;
    	List<HashMap> preDessorStationList = null;
    	BOPStructureDataUtility aBOPStructureDataUtility = new BOPStructureDataUtility();
    	aBOPStructureDataUtility.initBOMLineData(aTargetWeldPointBOMLine);
    	String shopId = aBOPStructureDataUtility.getShopId();
    	String lineId = aBOPStructureDataUtility.getLineId();
    	String stationId = aBOPStructureDataUtility.getStationId();
    	String findKey = null;
    	if(shopId!=null){
    		findKey = aBOPStructureDataUtility.getLatestKeyCodeForShop(shopId);
    		if(findKey !=null && findKey.trim().length()>0){
    			preDessorStationList = aBOPStructureDataUtility.getPredecessorStationsAtAllLine(findKey, lineId, stationId);
    		}
    	}
    	
    	if(preDessorStationList!=null && preDessorStationList.size()>0){
    		preDessorStationIdV = new Vector<String>();
    		for (int i = 0;preDessorStationList!=null && i < preDessorStationList.size(); i++) {
	    		HashMap<String, Object> hash = preDessorStationList.get(i);
	    		String aStationId = (String) hash.get("STATION_ID");
	    		if(aStationId!=null && aStationId.trim().length()>0 && preDessorStationIdV.contains(aStationId)==false){
	    			preDessorStationIdV.add(aStationId);
	    		}
			}
    	}
    }

}

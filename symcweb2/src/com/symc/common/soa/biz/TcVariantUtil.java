package com.symc.common.soa.biz;

import java.util.ArrayList;
import java.util.HashMap;

import com.symc.common.soa.service.TcServiceManager;
import com.symc.common.soa.util.TcConstants;
import com.symc.common.soa.util.TcUtil;
import com.symc.common.util.StringUtil;
import com.teamcenter.services.internal.strong.structuremanagement.VariantManagementService;
import com.teamcenter.services.internal.strong.structuremanagement._2011_06.VariantManagement;
import com.teamcenter.services.internal.strong.structuremanagement._2011_06.VariantManagement.ModularOption;
import com.teamcenter.services.internal.strong.structuremanagement._2011_06.VariantManagement.ModularOptions;
import com.teamcenter.services.internal.strong.structuremanagement._2011_06.VariantManagement.ModularOptionsForBomResponse;
import com.teamcenter.services.internal.strong.structuremanagement._2011_06.VariantManagement.ModularOptionsInfo;
import com.teamcenter.services.internal.strong.structuremanagement._2011_06.VariantManagement.ModularOptionsOutput;
import com.teamcenter.services.strong.cad._2007_01.StructureManagement.CreateBOMWindowsInfo;
import com.teamcenter.services.strong.cad._2007_01.StructureManagement.CreateBOMWindowsResponse;
import com.teamcenter.services.strong.query._2006_03.SavedQuery.GetSavedQueriesResponse;
import com.teamcenter.services.strong.query._2007_09.SavedQuery.SavedQueriesResponse;
import com.teamcenter.services.strong.query._2008_06.SavedQuery.QueryInput;
import com.teamcenter.soa.client.model.ModelObject;
import com.teamcenter.soa.client.model.strong.BOMLine;
import com.teamcenter.soa.client.model.strong.BOMWindow;
import com.teamcenter.soa.client.model.strong.ImanQuery;
import com.teamcenter.soa.client.model.strong.Item;
import com.teamcenter.soa.client.model.strong.ItemRevision;

public class TcVariantUtil {
    private TcServiceManager tcServiceManager;
    private VariantManagementService variantManagementService;
    private Session tcSession;
    private TcItemUtil tcItemUtil;
    private TcQueryUtil tcQueryUtil;

    public TcVariantUtil(Session tcSession) throws Exception {
        this.tcSession = tcSession;
        this.tcItemUtil = new TcItemUtil(this.tcSession);
        this.tcQueryUtil = new TcQueryUtil(this.tcSession);
        tcServiceManager = new TcServiceManager(this.tcSession);
        this.variantManagementService = tcServiceManager.getTcVariantManagementService().getService();
    }

    /**
     * OptionMaster를 가져온다.
     *
     * @method getOptionMaster
     * @date 2013. 7. 5.
     * @param
     * @return ModularOption[]
     * @exception
     * @throws
     * @see
     */
    public ModularOption[] getOptionMaster() throws Exception {
        VariantManagement.ModularOptionsInput amodularoptionsinput[] = new VariantManagement.ModularOptionsInput[1];
        VariantManagement.ModularOptionsInput modularoptionsinput = new VariantManagement.ModularOptionsInput();
        BOMWindow coprpOptionBomWindow = null;
        try {
            coprpOptionBomWindow = this.getCoprpOptionBomWindow();
            modularoptionsinput.bomWindow = coprpOptionBomWindow;
            modularoptionsinput.bomLines = new BOMLine[] { (BOMLine) coprpOptionBomWindow.get_top_line() };
            amodularoptionsinput[0] = modularoptionsinput;
            ModularOptionsForBomResponse modularoptionsforbomresponse = variantManagementService.getModularOptionsForBom(amodularoptionsinput);
            if (!tcServiceManager.getDataService().ServiceDataError(modularoptionsforbomresponse.serviceData)) {
                ModularOptionsOutput[] optionsOutput = modularoptionsforbomresponse.optionsOutput;
                ModularOptionsInfo[] optionsInfo = optionsOutput[0].optionsInfo;
                ModularOptions mOptions = optionsInfo[0].options;
                ModularOption[] options = mOptions.options;
                return options;
            } else {
                throw new Exception(TcUtil.makeMessageOfFail(modularoptionsforbomresponse.serviceData).get(TcConstants.TC_RETURN_FAIL_REASON).toString());
            }
        } catch (Exception e) {
            throw e;
        } finally {
            if (coprpOptionBomWindow != null) {
                tcServiceManager.getStructureService().closeBOMWindow(coprpOptionBomWindow);
            }
        }
    }

    private BOMWindow getCoprpOptionBomWindow() throws Exception {
        CreateBOMWindowsResponse createBOMWindowsResponse = tcServiceManager.getStructureService().createTopLineBOMWindow(this.getCoprpOptionLatestItemRev(), null, null);
        if (!tcServiceManager.getDataService().ServiceDataError(createBOMWindowsResponse.serviceData)) {
            return createBOMWindowsResponse.output[0].bomWindow;
        } else {
            throw new Exception(TcUtil.makeMessageOfFail(createBOMWindowsResponse.serviceData).get(TcConstants.TC_RETURN_FAIL_REASON).toString());
        }
    }

    private ItemRevision getCoprpOptionLatestItemRev() throws Exception {
        String queryName = TcConstants.SEARCH_ITEM;
        String[] entries = { "Type" };
        String[] values = { "S7_CorpOption" };
        SavedQueriesResponse executeSavedQueries = tcServiceManager.getQueryService().executeSavedQueries(this.setQueryInputforSingle(queryName, entries, values, "user"));
        if (!tcServiceManager.getDataService().ServiceDataError(executeSavedQueries.serviceData)) {
            String[] uids = tcQueryUtil.executeQueryResult(executeSavedQueries)[0].objectUIDS;
            if (uids == null || uids.length == 0) {
                throw new Exception("CoprpOption Item이 없습니다.");
            }
            ModelObject[] itemModels = tcServiceManager.getDataService().loadModelObjects(uids);
            Item item = (Item) itemModels[0];
            tcItemUtil.getProperties(new ModelObject[] { item }, new String[] { TcConstants.PROP_ITEM_ID, "revision_list" });
            ModelObject[] itemRevisions = item.get_revision_list();
            if (itemRevisions == null || itemRevisions.length == 0) {
                throw new Exception(item.get_item_id() + " : This revision does not exist.");
            }
            // 현재 Rev List의 제일 마지막을 Latest Revision 으로 간주하고 등록한다.
            ItemRevision rev = tcItemUtil.getRevisionInfo(itemRevisions[itemRevisions.length - 1].getUid());
            return rev;
        } else {
            throw new Exception(TcUtil.makeMessageOfFail(executeSavedQueries.serviceData).get(TcConstants.TC_RETURN_FAIL_REASON).toString());
        }
    }

    protected QueryInput[] setQueryInputforSingle(String queryName, String[] entries, String[] values, String clientId) throws Exception {
        /*
         * The type of results expected from this operation: 0 (top-level
         * objects only), 1 (top-level objects plus children:
         * Hierarchical/Indented results), 2 (default value as specified on the
         * query object
         */
        QueryInput[] queryInput = new QueryInput[1];
        queryInput[0] = new QueryInput();
        queryInput[0].clientId = clientId;
        queryInput[0].query = getQueryObject(queryName);
        queryInput[0].resultsType = 2;
        queryInput[0].entries = entries;
        queryInput[0].values = values;

        return queryInput;
    }

    private ImanQuery getQueryObject(String queryName) throws Exception {
        GetSavedQueriesResponse savedQueries = tcServiceManager.getQueryService().getQueryObject();
        for (int i = 0; i < savedQueries.queries.length; i++) {
            if (savedQueries.queries[i].name.equals(queryName)) {
                return savedQueries.queries[i].query;
            }
        }
        return null;
    }

    /**
     * Populate BOMWindow Information
     */
    public static CreateBOMWindowsInfo[] populateBOMWindowInfo(ItemRevision itemRev) {
        CreateBOMWindowsInfo[] bomInfo = new CreateBOMWindowsInfo[1];
        bomInfo[0] = new CreateBOMWindowsInfo();
        bomInfo[0].itemRev = itemRev;
        return bomInfo;
    }

    /**
     * Product Item Revision PUID를 가지고 BOMWindow를 생성한다.
     *
     * @method getCreateProductBOMWindow
     * @date 2013. 7. 16.
     * @param
     * @return BOMWindow
     * @exception
     * @throws
     * @see
     */
    public BOMWindow getCreateProductBOMWindow(String productRevPuid) throws Exception {
        ItemRevision productItemRevision = (ItemRevision) tcServiceManager.getDataService().loadModelObject(productRevPuid);
        CreateBOMWindowsResponse createBOMWindowsResponse = tcServiceManager.getStructureService().createTopLineBOMWindow(productItemRevision, null, null);
        if (!tcServiceManager.getDataService().ServiceDataError(createBOMWindowsResponse.serviceData)) {
            return createBOMWindowsResponse.output[0].bomWindow;
        } else {
            throw new Exception(TcUtil.makeMessageOfFail(createBOMWindowsResponse.serviceData).get(TcConstants.TC_RETURN_FAIL_REASON).toString());
        }
    }

    /**
     * TEST 중...
     *
     * @deprecated
     * @method getVariantOptions
     * @date 2013. 7. 19.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    public ModularOption[] getVariantOptions(String productRevPuid) throws Exception {
        BOMWindow bomWindow = null;
        ModularOption[] options = null;
        try {
            bomWindow = this.getCreateProductBOMWindow(productRevPuid);
            BOMLine topLine = (BOMLine) bomWindow.get_top_line();
            tcItemUtil.getProperties(new ModelObject[] { topLine }, new String[] { "bl_child_lines" });
            ModelObject[] childLines = (ModelObject[]) topLine.get_bl_child_lines();
            BOMLine[] lines = new BOMLine[childLines.length];
            System.arraycopy(childLines, 0, lines, 0, childLines.length);
            VariantManagement.ModularOptionsInput amodularoptionsinput[] = new VariantManagement.ModularOptionsInput[1];
            VariantManagement.ModularOptionsInput modularoptionsinput = new VariantManagement.ModularOptionsInput();
            for (int i = 0; i < childLines.length; i++) {
                modularoptionsinput.bomWindow = bomWindow;
                modularoptionsinput.bomLines = new BOMLine[] { lines[i] };
                amodularoptionsinput[0] = modularoptionsinput;
                ModularOptionsForBomResponse modularoptionsforbomresponse = variantManagementService.getModularOptionsForBom(amodularoptionsinput);
                if (!tcServiceManager.getDataService().ServiceDataError(modularoptionsforbomresponse.serviceData)) {
                    ModularOptionsOutput[] optionsOutput = modularoptionsforbomresponse.optionsOutput;
                    ModularOptionsInfo[] optionsInfo = optionsOutput[0].optionsInfo;
                    ModularOptions mOptions = optionsInfo[0].options;
                    options = mOptions.options;
                } else {
                    throw new Exception(TcUtil.makeMessageOfFail(modularoptionsforbomresponse.serviceData).get(TcConstants.TC_RETURN_FAIL_REASON).toString());
                }
            }
        } catch (Exception e) {
            throw e;
        } finally {
            if (bomWindow != null) {
                tcServiceManager.getStructureService().closeBOMWindow(bomWindow);
            }
        }
        return options;
    }

    /**
     * Constrains Options을 가지고 String Value를 가지고온다.
     *
     * @method getConstrainsOptions
     * @date 2013. 7. 24.
     * @param
     * @return String
     * @exception
     * @throws
     * @see
     */
    /*
    module F350CASY14
    interface start
    public C00 uses  "ENGINE" 'CorporateOption-001':C00
    public E10 uses  "TRANSFER CASE" 'CorporateOption-001':E10
    public H10 uses  "SERVICE BRAKE" 'CorporateOption-001':H10
    interface end
    if F350CASY14:E10 = "E10A" or F350CASY14:E10 = "E102" or F350CASY14:E10 = "E10F" then
     inform "[TC_MESSAGE_NOT_DEFINE][E10A, E102, E10F]"
    endif
    if F350CASY14:H10 = "H10T" or F350CASY14:H10 = "H10E" or F350CASY14:H10 = "H10R" or F350CASY14:H10 = "H10D" or F350CASY14:H10 = "H10J" then
     inform "[TC_MESSAGE_NOT_DEFINE][H10T, H10E, H10R, H10D, H10J]"
    endif
    if F350CASY14:C00 = "C00F" or F350CASY14:C00 = "C00E" or F350CASY14:C00 = "C00D" or F350CASY14:C00 = "C00C" or F350CASY14:C00 = "C00H" or F350CASY14:C00 = "C00G" or F350CASY14:C00 = "C00N" or F350CASY14:C00 = "C00M" or F350CASY14:C00 = "C00L" or F350CASY14:C00 = "C00R" or F350CASY14:C00 = "C002" or F350CASY14:C00 = "C00P" or F350CASY14:C00 = "C001" or F350CASY14:C00 = "C00U" or F350CASY14:C00 = "C00S" or F350CASY14:C00 = "C00T" or F350CASY14:C00 = "C00Y" or F350CASY14:C00 = "C00Z" or F350CASY14:C00 = "C00W" or F350CASY14:C00 = "C00X" or F350CASY14:C00 = "C00A" then
     inform "[TC_MESSAGE_NOT_DEFINE][C00F, C00E, C00D, C00C, C00H, C00G, C00N, C00M, C00L, C00R, C002, C00P, C001, C00U, C00S, C00T, C00Y, C00Z, C00W, C00X, C00A]"
    endif
     */
    public String getConstrainsOptions(ModularOption[] modularOptions, BOMLine bomLine) throws Exception {
        if(bomLine == null) {
            return "";
        }
        // Parent의 제약조건을 가지고 설정한다.
        String mvlText = bomLine.get_bl_rev_mvl_text();
        StringBuffer strConstrainsOptions = new StringBuffer();
        if("".equals(StringUtil.nullToString(mvlText))) {
            return "";
        }
        HashMap<String, String[]> constrainsOptions = this.getConvertConstrainsOptions(mvlText);
        for (int i = 0; i < modularOptions.length; i++) {
            String[] allowedValues = modularOptions[i].allowedValues;
            if (constrainsOptions.containsKey(modularOptions[i].optionName)) {
                String[] constrainsValues = constrainsOptions.get(modularOptions[i].optionName);
                for (int j = 0; j < allowedValues.length; j++) {
                    // modularOptions Value와 Constrains Value가 같지 않으면 등록
                    if(!(this.isConstrainsValue(allowedValues[j], constrainsValues))) {
                        if(strConstrainsOptions.length() == 0) {
                            strConstrainsOptions.append(allowedValues[j]);
                        } else {
                            strConstrainsOptions.append(",");
                            strConstrainsOptions.append(allowedValues[j]);
                        }
                    }
                }
            }
        }
        return strConstrainsOptions.toString();
    }

    /**
     * modularOptions Value가 Constrains Value 배열에 존재하는지 확인
     *
     * @method isConstrainsValue
     * @date 2013. 7. 24.
     * @param
     * @return boolean
     * @exception
     * @throws
     * @see
     */
    private boolean isConstrainsValue(String modularValue, String[] constrainsValues) {
        for (int i = 0; i < constrainsValues.length; i++) {
            if(modularValue.equals(constrainsValues[i])) {
                return true;
            }
        }
        return false;
    }


    /**
     * 옵션제약조건을 Key와 Value로 가져온다.
     *
     * @method getConvertConstrainsOptions
     * @date 2013. 7. 24.
     * @param
     * @return HashMap<String,String[]>
     * @exception
     * @throws
     * @see
     */
    public HashMap<String, String[]> getConvertConstrainsOptions(String mvlText) throws Exception {
        if("".equals(StringUtil.nullToString(mvlText))) {
            return null;
        }
        HashMap<String, String[]> constrainsOptions = new HashMap<String, String[]>();
        String[] splitOptions = mvlText.split("interface end");
        if(splitOptions.length == 2) {
            // 사용중인 Option Code 설정
            /*
            interface start
            public C00 uses  "ENGINE" 'CorporateOption-001':C00            --> C00
            public E10 uses  "TRANSFER CASE" 'CorporateOption-001':E10     --> E10
            public H10 uses  "SERVICE BRAKE" 'CorporateOption-001':H10     --> H10
            interface end
            */
            String[] usesOptions = StringUtil.nullToString(splitOptions[0].split("interface start")[1]).split("\\n");
            for (int i = 0; i < usesOptions.length; i++) {
                String optionCode = StringUtil.nullToString(usesOptions[i].substring(usesOptions[i].indexOf(" "), usesOptions[i].indexOf("uses")));
                constrainsOptions.put(optionCode, new String[0]);
            }
            String[] splitValues = splitOptions[1].split("endif");
            // 1. if 조건이 없는 경우 (사용 가능 옵션만 정의 되어있고 제약조건이 없는 경우)
            //  - new String[0] 인 경우는 Option Master 코드에 걸리는 전체 Value값 등록

            if("".equals(StringUtil.nullToString(splitValues[0]))) {
                return constrainsOptions;
            }
            // 2. if 조건이 있는 경우 (사용 가능 옵션만 정의 + 제약조건 있는 경우)
            //  - 하위 Constrains Value로직에서 들어간 배열 데이터를 제외한 Option Master Value 등록
            /*
            if F350CASY14:E10 = "E10A" or F350CASY14:E10 = "E102" or F350CASY14:E10 = "E10F" then
                inform "[TC_MESSAGE_NOT_DEFINE][E10A, E102, E10F]"
            endif
            if F350CASY14:H10 = "H10T" or F350CASY14:H10 = "H10E" or F350CASY14:H10 = "H10R" or F350CASY14:H10 = "H10D" or F350CASY14:H10 = "H10J" then
                inform "[TC_MESSAGE_NOT_DEFINE][H10T, H10E, H10R, H10D, H10J]"
            endif
            if F350CASY14:C00 = "C00F" or F350CASY14:C00 = "C00E" or F350CASY14:C00 = "C00D" or F350CASY14:C00 = "C00C" or F350CASY14:C00 = "C00H" or F350CASY14:C00 = "C00G" or F350CASY14:C00 = "C00N" or F350CASY14:C00 = "C00M" or F350CASY14:C00 = "C00L" or F350CASY14:C00 = "C00R" or F350CASY14:C00 = "C002" or F350CASY14:C00 = "C00P" or F350CASY14:C00 = "C001" or F350CASY14:C00 = "C00U" or F350CASY14:C00 = "C00S" or F350CASY14:C00 = "C00T" or F350CASY14:C00 = "C00Y" or F350CASY14:C00 = "C00Z" or F350CASY14:C00 = "C00W" or F350CASY14:C00 = "C00X" or F350CASY14:C00 = "C00A" then
                inform "[TC_MESSAGE_NOT_DEFINE][C00F, C00E, C00D, C00C, C00H, C00G, C00N, C00M, C00L, C00R, C002, C00P, C001, C00U, C00S, C00T, C00Y, C00Z, C00W, C00X, C00A]"
            endif
            */
            for (int i = 0; i < splitValues.length; i++) {
                // if F350CASY14:E10 = "E10A" or F350CASY14:E10 = "E102" or F350CASY14:E10 = "E10F" then
                //  inform "[TC_MESSAGE_NOT_DEFINE][E10A, E102, E10F]"
                // endif

                //if F350CASY14:H10 = "H10T" --> 'H10' Option Code를 뽑기 위한 로직
                String optionCode = StringUtil.nullToString(splitValues[i].split("=")[0].substring(splitValues[i].split("=")[0].indexOf(":")+1));
                // // if F350CASY14:E10 = "E10A" or F350CASY14:E10 = "E102" or F350CASY14:E10 = "E10F" then --> 구문만 분리
                String[] values = this.getOptionValues(splitValues[i].split("then")[0]);
                constrainsOptions.put(optionCode, values);
            }
        } else {
            throw new Exception("mvlText Option 포맷이 정확하지 않습니다. 관리자에게 문의 하세요.");
        }
        return constrainsOptions;
    }
    /**
     * Constrains Value값을 가져온다.
     *
     * @method getOptionValues
     * @date 2013. 7. 24.
     * @param
     * @return String[]
     * @exception
     * @throws
     * @see
     */
    public String[] getOptionValues(String values) {
        ArrayList<String> findValues = new ArrayList<String>();
        boolean isStartFindCode = false;
        String findValue = "";
        for (int i = 0; i < values.length(); i++) {
            char checkChar = values.charAt(i);
            String setpStr = String.valueOf(checkChar);
            if (!isStartFindCode && "\"".equals(setpStr)) {
                isStartFindCode = true;
                continue;
            } else if (isStartFindCode && "\"".equals(setpStr)) {
                isStartFindCode = false;
                findValues.add(findValue);
                findValue = "";
                continue;
            }
            if (isStartFindCode) {
                findValue = findValue + setpStr;
            }
        }
        return findValues.toArray(new String[findValues.size()]);
    }
}

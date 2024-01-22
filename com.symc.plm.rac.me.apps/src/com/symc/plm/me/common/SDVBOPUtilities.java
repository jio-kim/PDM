/**
 *
 */
package com.symc.plm.me.common;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.Vector;

import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.springframework.util.StringUtils;

import com.ssangyong.common.remote.DataSet;
import com.ssangyong.common.remote.SYMCRemoteUtil;
import com.symc.plm.me.utils.BundleUtil;
import com.symc.plm.me.utils.CustomUtil;
import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.aifrcp.AifrcpPlugin;
import com.teamcenter.rac.cme.application.MFGLegacyApplication;
import com.teamcenter.rac.cme.bvr.connect.services.impl.MFGConnectSOAService;
import com.teamcenter.rac.cme.connect.MFGConnectData;
import com.teamcenter.rac.cme.framework.treetable.CMEBOMTreeTable;
import com.teamcenter.rac.kernel.NamedReferenceContext;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMViewRevision;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.kernel.TCComponentBOMWindowType;
import com.teamcenter.rac.kernel.TCComponentBOPWindow;
import com.teamcenter.rac.kernel.TCComponentBOPWindowType;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentDatasetDefinition;
import com.teamcenter.rac.kernel.TCComponentDatasetType;
import com.teamcenter.rac.kernel.TCComponentFolder;
import com.teamcenter.rac.kernel.TCComponentFolderType;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentItemType;
import com.teamcenter.rac.kernel.TCComponentRevisionRule;
import com.teamcenter.rac.kernel.TCComponentTcFile;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCPreferenceService;
import com.teamcenter.rac.kernel.TCPreferenceService.TCPreferenceLocation;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.kernel.TCVariantService;
import com.teamcenter.rac.pse.common.BOMTreeTable;
import com.teamcenter.rac.pse.operations.ExpandBelowOperation;
import com.teamcenter.rac.pse.variants.modularvariants.MVLLexer;
import com.teamcenter.rac.pse.variants.modularvariants.ModularOptionModel;
import com.teamcenter.rac.psebase.common.AbstractViewableTreeTable;
import com.teamcenter.rac.util.AdapterUtil;
import com.teamcenter.rac.util.PlatformHelper;
import com.teamcenter.rac.util.Registry;
import com.teamcenter.services.internal.rac.structuremanagement.VariantManagementService;
import com.teamcenter.services.internal.rac.structuremanagement._2011_06.VariantManagement;
import com.teamcenter.services.internal.rac.structuremanagement._2011_06.VariantManagement.ModularOption;
import com.teamcenter.services.internal.rac.structuremanagement._2011_06.VariantManagement.ModularOptions;
import com.teamcenter.services.internal.rac.structuremanagement._2011_06.VariantManagement.ModularOptionsInfo;
import com.teamcenter.services.internal.rac.structuremanagement._2011_06.VariantManagement.ModularOptionsOutput;
import com.teamcenter.services.rac.manufacturing.DataManagementService;
import com.teamcenter.services.rac.manufacturing._2012_02.DataManagement;
import com.teamcenter.soa.client.model.ErrorStack;

/**
 * Class Name : SDVBOPUtilities Class Description :
 * 
 * @date 2013. 10. 28.
 * 
 */
public class SDVBOPUtilities {

    private static SDVBOPUtilities service;
    private static TCComponentDatasetType datasetType;

    /**
     * 현재 TC Session을 가져옴
     * 
     * @method getTCSession
     * @date 2013. 10. 28.
     * @param
     * @return TCSession
     * @exception
     * @throws
     * @see
     */
    public static TCSession getTCSession() {
        return (TCSession) AIFUtility.getSessionManager().getDefaultSession();
    }

    /**
     * BOM Window 를 가져옴
     * 
     * @param session
     *            TCSession
     * @param itemRevision
     *            아이템 리비전
     * @param ruleName
     *            리비전 룰 Name
     * @param viewType
     *            뷰타입명
     * @return
     * @throws Exception
     */
    public static TCComponentBOMWindow getBOMWindow(TCComponentItemRevision itemRevision, String ruleName, String viewType) throws Exception {
        TCComponentRevisionRule revRule;
        TCSession session = getTCSession();
        TCComponentBOMViewRevision viewRevision = getBOMViewRevision(itemRevision, viewType);
        // 리비전 룰을 가져옴
        revRule = getRevisionRule(session, ruleName);
        // BOMWindow를 생성
        TCComponentBOMWindow bomWindow = null;
        TCComponentBOMWindowType windowType = (TCComponentBOMWindowType) session.getTypeComponent("BOMWindow");
        bomWindow = windowType.create(revRule);
        bomWindow.setWindowTopLine(itemRevision.getItem(), itemRevision, null, viewRevision);

        return bomWindow;
    }

    public static TCComponentBOPWindow getBOPWindow(TCComponentItemRevision itemRevision, String ruleName, String viewType) throws Exception {
        TCComponentRevisionRule revRule;
        TCSession session = getTCSession();
        TCComponentBOMViewRevision viewRevision = getBOMViewRevision(itemRevision, viewType);
        // 리비전 룰을 가져옴
        revRule = getRevisionRule(session, ruleName);
        // BOMWindow를 생성
        TCComponentBOPWindow bopWindow = null;
        TCComponentBOPWindowType windowType = (TCComponentBOPWindowType) session.getTypeComponent("BOPWindow");
        bopWindow = windowType.createBOPWindow(revRule);
        bopWindow.setWindowTopLine(itemRevision.getItem(), itemRevision, null, viewRevision);

        return bopWindow;
    }
    
    /**
     * [SR150122-027][20150210] shcho, Find automatically replaced end item (공법 할당 E/Item의 설계 DPV에 의한 자동 변경 오류 해결) (10버전에서 개발 된 소스 9으로 이식함)
     */
    public static TCComponentBOPWindow createBOPWindow(String ruleName) throws Exception {
        TCSession session = getTCSession();
        
        TCComponentRevisionRule revRule = getRevisionRule(session, ruleName);
        TCComponentBOPWindowType windowType = (TCComponentBOPWindowType) session.getTypeComponent("BOPWindow");
        
        return windowType.createBOPWindow(revRule);
    }

    /**
     * Revision Rule을 가져옴
     * 
     * @param session
     *            TCSession
     * @param ruleName
     *            Revision Rule 명
     * @return revRule TCComponentRevisionRule
     * @throws Exception
     */
    public static TCComponentRevisionRule getRevisionRule(TCSession session, String ruleName) throws Exception {
        TCComponentRevisionRule revRule = null;
        if (ruleName == null || ruleName.length() == 0)
            return null;

        TCComponentRevisionRule[] revRules = TCComponentRevisionRule.listAllRules(session);

        if (revRules == null)
            return null;

        for (TCComponentRevisionRule ruvRule : revRules) {
            String revRuleName = ruvRule.getProperty("object_name");
            if (ruleName.trim().equalsIgnoreCase(revRuleName))
                revRule = ruvRule;
        }

        return revRule;
    }

    /**
     * 리비전 하위에 view와 타입이 일치하는 BOMViewRevision 검색하여 반환한다.
     * 
     * @param revision
     *            ItemRevision TCComponent
     * @param viewType
     *            뷰타입 String
     * @return bomViewRevision TCComponentBOMViewRevision
     * @throws TCException
     */
    public static TCComponentBOMViewRevision getBOMViewRevision(TCComponent comp, String viewType) throws Exception {
        comp.refresh();

        TCComponentBOMViewRevision returnTarget = null;
        
        TCComponent[] arrayStructureRevision = comp.getRelatedComponents("structure_revisions");

        for (int i = 0;arrayStructureRevision!=null && i < arrayStructureRevision.length; i++) {
        	
            TCComponentBOMViewRevision bomViewRevision = (TCComponentBOMViewRevision) arrayStructureRevision[i];
            
            TCComponent tempComp = bomViewRevision.getReferenceProperty("bom_view");
            String viewTypeStr = null;
            if(tempComp!=null){
            	viewTypeStr = tempComp.getProperty("view_type");
            }

            if(viewTypeStr!=null && viewTypeStr.equals(viewType)==true){
            	returnTarget = bomViewRevision;
            	break;
            }
        }
        
        return returnTarget;
    }

    /**
     * 
     * BOM line에 설정된 ModularOption 리스트를 가져옴
     * [SR150610-004][20150610] shcho, 대상 BOM Viewpart가 MPP에 열려있지 않을 경우 null point exception 발생하는 오류 수정
     * 
     * @method getModularOptions
     * @date 2013. 10. 22.
     * @param
     * @return ModularOption[]
     * @exception
     * @throws
     * @see
     */
    public static ModularOption[] getModularOptions(TCComponentBOMLine targetBOMLine) throws TCException {
        BOMTreeTable treeTable = null;
        TCComponentBOMLine treeTableBOMLine = null;

        IViewReference[] viewReferences = PlatformHelper.getCurrentPage().getViewReferences();
        for (IViewReference viewRefrence : viewReferences) {
            IViewPart viewPart = viewRefrence.getView(false);
            if (viewPart == null) {
                continue;
            }

            CMEBOMTreeTable cmeBOMTreeTable = (CMEBOMTreeTable) AdapterUtil.getAdapter(viewPart, CMEBOMTreeTable.class);
            if (cmeBOMTreeTable == null) {
                continue;
            }

            TCComponentBOMLine bomRoot = cmeBOMTreeTable.getBOMRoot();
            String targetBOMLineId = targetBOMLine.getItemRevision().getProperty("item_id");
            String bomRootId = bomRoot.getItemRevision().getProperty("item_id");
            if (targetBOMLineId.equals(bomRootId)) {
                treeTable = cmeBOMTreeTable;
                treeTableBOMLine = bomRoot;

                break;
            }
        }

        ModularOptionModel moduleModel = null;
        try {
            moduleModel = new ModularOptionModel(treeTable, treeTableBOMLine, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        //[SR150610-004][20150610] shcho, 대상 BOM Viewpart가 MPP에 열려있지 않을 경우 null point exception 발생하는 오류 수정 
        if (treeTable == null || treeTableBOMLine == null ) {
            return null;
        }
        
        int[] optionNums = moduleModel.getOptionsForModule(treeTable.getNode(treeTableBOMLine));
        if (optionNums == null) {
            return null;
        }

        com.teamcenter.services.internal.rac.structuremanagement._2011_06.VariantManagement.ModularOptionsInput amodularoptionsinput[] = new com.teamcenter.services.internal.rac.structuremanagement._2011_06.VariantManagement.ModularOptionsInput[1];
        VariantManagementService variantmanagementservice = VariantManagementService.getService(targetBOMLine.getSession());
        com.teamcenter.services.internal.rac.structuremanagement._2011_06.VariantManagement.ModularOptionsInput modularoptionsinput = new com.teamcenter.services.internal.rac.structuremanagement._2011_06.VariantManagement.ModularOptionsInput();
        modularoptionsinput.bomWindow = targetBOMLine.window();
        modularoptionsinput.bomLines = new TCComponentBOMLine[] { targetBOMLine };
        amodularoptionsinput[0] = modularoptionsinput;

        com.teamcenter.services.internal.rac.structuremanagement._2011_06.VariantManagement.ModularOptionsForBomResponse modularoptionsforbomresponse = variantmanagementservice.getModularOptionsForBom(amodularoptionsinput);
        ModularOptionsOutput[] optionsOutput = modularoptionsforbomresponse.optionsOutput;
        ModularOptionsInfo[] optionsInfo = optionsOutput[0].optionsInfo;
        ModularOptions mOptions = optionsInfo[0].options;
        ModularOption[] options = mOptions.options;

        return options;
    }

    /**
     * 
     * 옵션의 MVL String을 가져옴
     * 
     * @method getOptionString
     * @date 2013. 10. 22.
     * @param optionId
     *            옵션 ID *
     * @return String
     * @exception
     * @throws
     * @see
     */
    public static String getOptionString(String optionId, String optionName, String optionDesc) {
        String name = MVLLexer.mvlQuoteId(optionName, false);

        if (optionDesc.length() > 0)
            optionDesc = (new StringBuilder()).append(" ").append(MVLLexer.mvlQuoteString(optionDesc)).append(" ").toString();
        else
            optionDesc = " ";
        String s = "public ";
        s += name;
        s += " uses " + optionDesc + MVLLexer.mvlQuoteId(optionId, true) + ":" + name;
        return s;
    }

    /**
     * bl_occ_mvl_condition 속성에서 value 값 가져오기
     * 
     * @method getVariant
     * @date 2013. 11. 6.
     * @param
     * @return HashMap<String,Object>
     * @exception
     * @throws
     * @see
     */
    public static HashMap<String, Object> getVariant(String condition) {
        HashMap<String, Object> data = new HashMap<String, Object>();
        ArrayList<String> values = new ArrayList<String>();
        String printValues = null;
        String printDescriptions = null;

        StringBuilder sb = new StringBuilder();
        String tmpStr = null;
        StringTokenizer stringTokenizer1 = null;
        StringTokenizer stringTokenizer2 = null;
        stringTokenizer1 = new StringTokenizer(condition, "or");

        while (stringTokenizer1.hasMoreElements()) {
            tmpStr = (String) stringTokenizer1.nextElement();
            stringTokenizer2 = new StringTokenizer(tmpStr, "and");

            while (stringTokenizer2.hasMoreElements()) {
                tmpStr = (String) stringTokenizer2.nextElement();
                tmpStr = tmpStr.substring(tmpStr.indexOf("=") + 1, tmpStr.length());

                sb.append(tmpStr.replaceAll("\"", ""));
                if (stringTokenizer2.hasMoreTokens()) {
                    sb.append("and");
                }
            }

            if (stringTokenizer1.hasMoreTokens()) {
                sb.append("or");
            }
        }

        String temp = sb.toString().replaceAll(" ", "");
        printValues = temp;
        printValues = printValues.replaceAll("or", "@\n");
        printValues = printValues.replaceAll("and", " AND ");

        stringTokenizer1 = new StringTokenizer(temp, "or");
        while (stringTokenizer1.hasMoreElements()) {
            temp = (String) stringTokenizer1.nextElement();
            stringTokenizer2 = new StringTokenizer(temp, "and");
            while (stringTokenizer2.hasMoreElements()) {
                temp = (String) stringTokenizer2.nextElement();
                values.add(temp);
            }
        }

        HashMap<String, String> descriptions = getDescriptionFromVariant(values);
        printDescriptions = printValues;
        for (String value : values) {
            if (!descriptions.containsKey(value))
                continue;
            printDescriptions = printDescriptions.replace(value, descriptions.get(value));
        }

        data.put("values", values);
        data.put("descriptions", descriptions);
        data.put("printValues", printValues);
        data.put("printDescriptions", printDescriptions);

        return data;
    }

    /**
     * values 값에 해당하는 Description 가져오기
     * 
     * @method getDescriptionFromVariant
     * @date 2013. 11. 5.
     * @param
     * @return HashMap<String,String>
     * @exception
     * @throws
     * @see
     */
    @SuppressWarnings("unchecked")
    public static HashMap<String, String> getDescriptionFromVariant(ArrayList<String> values) {
        HashMap<String, String> descriptions = new HashMap<String, String>();

        // 1차 WAS를 호출
        SYMCRemoteUtil remote = new SYMCRemoteUtil();

        DataSet ds = new DataSet();

        for (String value : values) {
            ds.put("code_name", value);

            try {
                ArrayList<HashMap<String, String>> list = (ArrayList<HashMap<String, String>>) remote.execute("com.ssangyong.service.VariantService", "getVariantValueDesc", ds);
                if (list != null) {
                    for (HashMap<String, String> map : list) {
                        descriptions.put(map.get("CODE_NAME"), map.get("CODE_DESC"));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return descriptions;
    }

    /**
     * Item 을 찾음
     * 
     * @method FindItem
     * @date 2013. 11. 7.
     * @param
     * @return TCComponentItem
     * @exception
     * @throws
     * @see
     */
    public static TCComponentItem FindItem(String itemId, String itemTypeName) throws Exception {
        TCComponentItemType itemType = (TCComponentItemType) getTCSession().getTypeComponent(itemTypeName);
        if (itemType == null)
            return null;
        TCComponentItem[] items = itemType.findItems(itemId);
        if (items.length == 0)
            return null;
        return items[0];
    }

    /**
     * 
     * Item을 생성함
     * 
     * @method createItem
     * @date 2013. 11. 8.
     * @param
     * @return TCComponentItem
     * @exception
     * @throws
     * @see
     */
    public static TCComponentItem createItem(String compType, String itemId, String revision, String itemName, String desc) throws TCException {
        TCSession session = getTCSession();
        TCComponentItem item = null;
        TCComponentItemType itemType = (TCComponentItemType) session.getTypeComponent(compType);
        item = itemType.create(itemId, revision, compType, itemName, desc, null);
        return item;
    }

    public static TCComponentFolder createFolder(String name, String desc) throws TCException {
        TCComponentFolderType type = (TCComponentFolderType) getTCSession().getTypeComponent("Folder");

        return type.create(name, desc, "Folder");
    }

    /**
     * 
     * BOM에서 설정된 옵션 Set을 가져옴
     * 
     * @method getBOMConfiguredVariantSet
     * @date 2013. 11. 15.
     * @param
     * @return HashMap<String,String>
     * @exception
     * @throws
     * @see
     */
    public static HashMap<String, String> getBOMConfiguredVariantSet(TCComponentBOMWindow bomWindow) throws TCException {
        HashMap<String, String> variantSet = new HashMap<String, String>();
        VariantManagementService vmService = VariantManagementService.getService(bomWindow.getSession());
        VariantManagement.BOMVariantConfigOptionResponse response = vmService.getBOMVariantConfigOptions(bomWindow, bomWindow.getTopBOMLine());
        VariantManagement.BOMVariantConfigOutput out = response.output;
        for (VariantManagement.BOMVariantConfigurationOption configuredOption : out.configuredOptions) {

            String optionValue = configuredOption.valueSet;
            VariantManagement.ModularOption modularOption = configuredOption.modularOption;
            String optionName = modularOption.optionName;

            int howSet = configuredOption.howSet;
            if (howSet != TCVariantService.BOM_OPTION_SET_BY_USER)
                continue;

            variantSet.put(optionName, optionValue);
        }
        return variantSet;
    }

    public static String getBOMConfiguredVariantSetToString(TCComponentBOMWindow bomWindow) throws TCException {
        String strVariantSet = "";
        HashMap<String, String> variantSet = getBOMConfiguredVariantSet(bomWindow);
        for (String key : variantSet.keySet()) {
            strVariantSet = strVariantSet + " " + key + "=" + variantSet.get(key);
        }

        if (strVariantSet.length() > 1) {
            strVariantSet = strVariantSet.substring(1);
        }

        return strVariantSet;
    }

    /** 
     * BOP TOP BOPLine(Shop)과 관계되는 M Product BOMWindow를 가져옴 - Line 해제시 사용
     * [SR150122-027][20150309]shcho, 공법 할당 E/Item의 설계 DPV에 의한 자동 변경 문제 해결 - Link해제된 MProduct를 찾을 수 있도록 추가
     *  
     * @param bopItemRevision
     * @return TCComponentBOMWindow
     * @throws Exception
     */
    public static TCComponentBOMWindow getConnectedMProductBOMWindow(TCComponentItemRevision bopItemRevision) throws Exception {
        TCComponentBOMWindow mProductBomWindow = null;
        TCComponentItemRevision mProductRevision = null;
        mProductRevision = getConnectedMProductItemRevision(bopItemRevision);
        
        if(mProductRevision != null) {
            mProductBomWindow = SDVBOPUtilities.getBOMWindow(mProductRevision, "Latest Working", "bom_view");
        }
        
        return mProductBomWindow;
    }

    /**
     * BOP TOP BOPLine(Shop)과 관계되는 M Product ItemRevision을 가져옴 - Line 해제시 사용
     * [SR150122-027][20150309]shcho, 공법 할당 E/Item의 설계 DPV에 의한 자동 변경 문제 해결 - Link해제된 MProduct를 찾을 수 있도록 추가
     * 
     * @param bopItemRevision
     * @return
     * @throws TCException
     */
    public static TCComponentItemRevision getConnectedMProductItemRevision(TCComponentItemRevision bopItemRevision) throws TCException {
        TCComponentItemRevision mProductRevision = null;
        String productCode = bopItemRevision.getProperty(SDVPropertyConstant.SHOP_REV_PRODUCT_CODE);
        if(productCode != null && !productCode.equals("")) {
            TCComponent[] qryResult = SDVQueryUtils.executeSavedQuery("Item Revision...", new String[] { "Item ID" }, new String[] { "M".concat(productCode.substring(1)) });
            if (qryResult != null && qryResult.length != 0) {
                mProductRevision = (TCComponentItemRevision) qryResult[0];
            }
        }
        
        return mProductRevision;
    }

    /**
     * 
     * BOP TOP BOPLine에 연결된 M Product BOMWindow를 가져옴 - Default Working
     * 
     * @method getConnecteMProductBOMWindow
     * @date 2013. 11. 15.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    /* [SR150122-027][20150309]shcho, 공법 할당 E/Item의 설계 DPV에 의한 자동 변경 문제 해결 - Link 해제로 더이상 사용안함.
    public static TCComponentBOMWindow getConnectedMProductBOMWindow(TCComponentBOMLine topBOMLine) throws Exception {
        return getConnectedMProductBOMWindow(topBOMLine, "Latest Working", "bom_view");
    }
     */
    
    /**
     * 
     * BOP TOP BOPLine에 연결된 M Product BOMWindow를 가져옴 FIXME: MProduct 타입 정의에 따라서 변경해야함
     * 
     * @method getConnecteMProductBOMWindow
     * @date 2013. 11. 15.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    /* [SR150122-027][20150309]shcho, 공법 할당 E/Item의 설계 DPV에 의한 자동 변경 문제 해결 - Link 해제로 더이상 사용안함.
    public static TCComponentBOMWindow getConnectedMProductBOMWindow(TCComponentBOMLine topBOMLine, String revisionRule, String bomView) throws Exception {
        // Product로 연결된 BOMWindow
        TCComponentBOMWindow mProductBomWindow = null;
        TCComponent[] meTargetComps = topBOMLine.getItemRevision().getRelatedComponents("IMAN_METarget");
        for (TCComponent meTargetComp : meTargetComps) {
            String type = meTargetComp.getType();
            if (type.equals(SDVTypeConstant.BOP_MPRODUCT_REVISION)) {
                mProductBomWindow = SDVBOPUtilities.getBOMWindow((TCComponentItemRevision) meTargetComp, revisionRule, bomView);
                return mProductBomWindow;
            }
        }
        return mProductBomWindow;
    }
     */
    
    /**
     * 할당된 BOMLine의 원본 BOMLine을 가져옴
     * 
     * @method getAssignSrcBomLine
     * @date 2013. 11. 15.
     * @param
     * @return TCComponentBOMLine
     * @exception
     * @throws
     * @see
     */
    public static TCComponentBOMLine getAssignSrcBomLine(TCComponentBOMWindow productBomWindow, TCComponentBOMLine assignedBOMLine) throws Exception {
        TCComponentBOMLine srcBOMLine = null;

        if(productBomWindow != null && assignedBOMLine != null) {
            String occId = assignedBOMLine.getProperty(SDVPropertyConstant.BL_ABS_OCC_ID);
            TCComponentBOMLine[] findedBomline = productBomWindow.findAppearance(occId);
            if (findedBomline != null && findedBomline.length > 0) {
                TCComponentBOMLine occChildBomline = findedBomline[0];
                srcBOMLine = occChildBomline;
            }
        }

        return srcBOMLine;
    }

    /**
     * 할당된 BOMLine의 원본 BOMLine 배열을 가져옴
     * 
     * @method getAssignSrcBomLineList
     * @date 2013. 12. 4.
     * @param
     * @return TCComponentBOMLine[]
     * @exception
     * @throws
     * @see
     */
    public static TCComponentBOMLine[] getAssignSrcBomLineList(TCComponentBOMWindow productBomWindow, TCComponentBOMLine assignedBOMLine) throws Exception {
        String occId = assignedBOMLine.getProperty(SDVPropertyConstant.BL_ABS_OCC_ID);
        if (StringUtils.isEmpty(occId)) {
            return new TCComponentBOMLine[0];
        }
        return productBomWindow.findAppearance(occId);
    }

    /**
     * 템플릿의 최종 Release 된 Dataset을 반환한다.
     * 
     * @method getTemplateDataset
     * @date 2013. 11. 27.
     * @param preferenceName
     *            : Template Preference Name, name : dataset name (null이면 기존 Dataset 이름과 동일하게 생성)
     * @return TCComponentDataset
     * @exception
     * @throws
     * @see
     */
    public static TCComponentDataset getTemplateDataset(String preferenceName, String name) throws Exception {
        return getTemplateDataset(preferenceName, name, null);
    }

    public static TCComponentDataset getTemplateDataset(String preferenceName, String datasetName, String fileName) throws Exception {
        TCPreferenceService prefService = ((TCSession) AIFUtility.getDefaultSession()).getPreferenceService();
        // String itemId = prefService.getString(TCPreferenceService.TC_preference_site, preferenceName);
        String itemId = prefService.getStringValueAtLocation(preferenceName, TCPreferenceLocation.OVERLAY_LOCATION);

        TCComponentItem item = SDVBOPUtilities.FindItem(itemId, "Document");
        TCComponentDataset newDataset = null;
        if (item != null) {
            TCComponentItemRevision[] revisions = item.getReleasedItemRevisions();
            if (revisions != null && revisions.length > 0) {
                TCComponentItemRevision revision = revisions[0];
                TCComponent[] comps = revision.getRelatedComponents("TC_Attaches");
                if (comps != null) {
                    for (TCComponent comp : comps) {
                        if (comp instanceof TCComponentDataset) {
                            if (datasetName == null) {
                                datasetName = comp.getProperty(SDVPropertyConstant.ITEM_OBJECT_NAME);
                            }

                            TCComponentTcFile[] tcFiles = ((TCComponentDataset) comp).getTcFiles();
                            if (tcFiles != null && tcFiles.length > 0) {
                                String orgFileName = tcFiles[0].getProperty("original_file_name");
                                String[] orgFileNameSplit = orgFileName.split("[.]");
                                String extFileName = orgFileNameSplit[orgFileNameSplit.length - 1];
                                File file = tcFiles[0].getFile(null, fileName == null ? null : fileName + "." + extFileName);
                                newDataset = createDataset(file.getAbsolutePath(), datasetName);
                            }

                            // File[] files = getFiles((TCComponentDataset) comp);
                            // if(files != null && files.length > 0) {
                            // String filePath = files[0].getAbsolutePath();
                            // if(fileName != null) {
                            // filePath = filePath.substring(0, filePath.lastIndexOf(File.separator) + 1) + fileName;
                            // }
                            // newDataset = createDataset(filePath);
                            // }

                            // Dataset을 saveAs 하면 Dataset에 첨부된 NamedReference가 참조로 따라감...
                            // newDataset = ((TCComponentDataset) comp).saveAs(name);
                        }
                    }
                }
            }
        }

        return newDataset;
    }

    /**
     * 첨부할 Dataset 파일의 확장자에 따라 object_type을 결정
     * 
     * @method createDataset
     * @date 2013. 12. 10.
     * @param
     * @return TCComponentDataset
     * @exception
     * @throws
     * @see
     */
    public static void createService(TCSession session) {
        if (service == null) {
            service = new SDVBOPUtilities(session);
        }
    }

    private SDVBOPUtilities(TCSession session) {
        try {
            datasetType = (TCComponentDatasetType) session.getTypeComponent("Dataset");
        } catch (TCException e) {
            e.printStackTrace();
        }
    }

    public static TCComponentDataset createDataset(String filePath, String datasetName) throws Exception {
        if (datasetType == null) {
            datasetType = (TCComponentDatasetType) ((TCSession) AIFUtility.getDefaultSession()).getTypeComponent("Dataset");
        }

        TCComponentDataset dataset = null;
        File file = new File(filePath);
        if (file != null) {
            String extension = getExtension(file);
            if (extension != null && !extension.equals("")) {
                if (datasetName == null) {
                    datasetName = getFileName(file);
                }

                if (extension.equals("xls")) {
                    dataset = datasetType.create(datasetName, "", "MSExcel");
                    dataset.setFiles(new String[] { file.getAbsolutePath() }, new String[] { "MSExcel" }, new String[] { "Plain" }, new String[] { "excel" });
                } else if (extension.equals("xlsx") || extension.equals("xlsm")) {
                	// [20170525] bck 수정 
                	// Excel Dataset 유형 추가 xlsm 
                    dataset = datasetType.create(datasetName, "", "MSExcelX");
                    dataset.setFiles(new String[] { file.getAbsolutePath() }, new String[] { "MSExcelX" }, new String[] { "Plain" }, new String[] { "excel" });
                } else if (extension.equals("doc")) {
                    dataset = datasetType.create(datasetName, "", "MSWord");
                    dataset.setFiles(new String[] { file.getAbsolutePath() }, new String[] { "MSWord" }, new String[] { "Plain" }, new String[] { "word" });
                } else if (extension.equals("docx")) {
                    dataset = datasetType.create(datasetName, "", "MSWordX");
                    dataset.setFiles(new String[] { file.getAbsolutePath() }, new String[] { "MSWordX" }, new String[] { "Plain" }, new String[] { "word" });
                } else if (extension.equals("ppt")) {
                    dataset = datasetType.create(datasetName, "", "MSPowerPoint");
                    dataset.setFiles(new String[] { file.getAbsolutePath() }, new String[] { "MSPowerPoint" }, new String[] { "Plain" }, new String[] { "powerpoint" });
                } else if (extension.equals("pptx")) {
                    dataset = datasetType.create(datasetName, "", "MSPowerPointX");
                    dataset.setFiles(new String[] { file.getAbsolutePath() }, new String[] { "MSPowerPointX" }, new String[] { "Plain" }, new String[] { "powerpoint" });
                } else if (extension.equals("txt")) {
                    dataset = datasetType.create(datasetName, "", "Text");
                    dataset.setFiles(new String[] { file.getAbsolutePath() }, new String[] { "Text" }, new String[] { "Plain" }, new String[] { "Text" });
                } else if (extension.equals("pdf")) {
                    dataset = datasetType.create(datasetName, "", "PDF");
                    dataset.setFiles(new String[] { file.getAbsolutePath() }, new String[] { "PDF" }, new String[] { "Plain" }, new String[] { "PDF_Reference" });
                } else if (extension.equals("jpg")) {
                    dataset = datasetType.create(datasetName, "", "JPEG");
                    dataset.setFiles(new String[] { file.getAbsolutePath() }, new String[] { "JPEG" }, new String[] { "Plain" }, new String[] { "JPEG_Reference" });
                } else if (extension.equals("gif")) {
                    dataset = datasetType.create(datasetName, "", "GIF");
                    dataset.setFiles(new String[] { file.getAbsolutePath() }, new String[] { "GIF" }, new String[] { "Plain" }, new String[] { "GIF_Reference" });
                } else if (extension.equals("jpeg") || extension.equals("png") || extension.equals("tif") || extension.equals("tiff") || extension.equals("bmp")) {
                    dataset = datasetType.create(datasetName, "", "Image");
                    dataset.setFiles(new String[] { file.getAbsolutePath() }, new String[] { "Image" }, new String[] { "Plain" }, new String[] { "Image" });
                } else if (extension.equals("dwg")) {
                    dataset = datasetType.create(datasetName, "", "M7_RESOURCEDRAWINGDWG");
                    dataset.setFiles(new String[] { file.getAbsolutePath() }, new String[] { "M7_RESOURCEDRAWINGDWG" }, new String[] { "Plain" }, new String[] { "M7_DWG" });
                } else if (extension.equals("zip")) {
                    dataset = datasetType.create(datasetName, "", "Zip");
                    dataset.setFiles(new String[] { file.getAbsolutePath() }, new String[] { "Zip" }, new String[] { "Plain" }, new String[] { "ZIPFILE" });
                } else if (extension.equals("htm") || extension.equals("html")) {
                    dataset = datasetType.create(datasetName, "", "HTML");
                    dataset.setFiles(new String[] { file.getAbsolutePath() }, new String[] { "Text" }, new String[] { "Plain" }, new String[] { "HTML" });
                } else if (extension.equals("eml")) {
                    dataset = datasetType.create(datasetName, "", "EML");
                    dataset.setFiles(new String[] { file.getAbsolutePath() }, new String[] { "EML" }, new String[] { "Plain" }, new String[] { "EML_Reference" });
                } else if (extension.equalsIgnoreCase("CATPart")) {
                    dataset = datasetType.create(datasetName, "", "CATPart");
                    dataset.setFiles(new String[] { file.getAbsolutePath() }, new String[] { "CATPart" }, new String[] { "Plain" }, new String[] { "catpart" });
                } else if (extension.equalsIgnoreCase("cgr")) {
                    dataset = datasetType.create(datasetName, "", "CATCache");
                    dataset.setFiles(new String[] { file.getAbsolutePath() }, new String[] { "CATCache" }, new String[] { "Plain" }, new String[] { "catcgr" });
                } else if (extension.equalsIgnoreCase("jt")) {
                    dataset = datasetType.create(datasetName, "", "DirectModel");
                    dataset.setFiles(new String[] { file.getAbsolutePath() }, new String[] { "DirectModel" }, new String[] { "Plain" }, new String[] { "JTPART" });
                } else {
                    dataset = datasetType.create(datasetName, "", "MISC");
                    dataset.setFiles(new String[] { file.getAbsolutePath() }, new String[] { "MISC" }, new String[] { "Plain" }, new String[] { "MISC_BINARY" });
                }
            }
        }
        return dataset;
    }

    public static TCComponentDataset createDataset(String path) throws Exception {
        return createDataset(path, null);
    }

    public static void createDataset(TCComponent component, String relation, Vector<String> vector) throws Exception {
        for (int i = 0; i < vector.size(); i++) {
            TCComponentDataset dataset = createDataset(vector.elementAt(i));
            component.add(relation, dataset);
        }
    }

    public static TCComponentDataset[] createDataset(Vector<String> vector) throws Exception {
        TCComponentDataset[] datasets = new TCComponentDataset[vector.size()];
        for (int i = 0; i < vector.size(); i++) {
            TCComponentDataset dataset = createDataset(vector.elementAt(i));
            datasets[i] = dataset;
        }
        return datasets;
    }

    public static void createDataset(TCComponent component, String relation, String path) throws Exception {
        TCComponentDataset dataset = createDataset(path);
        if (dataset != null) {
            component.add(relation, dataset);
        }
    }

    public static File[] getFiles(TCComponentDataset dataset) throws Exception {
        String type = dataset.getType();
        String namedRefType = null;
        if (type.equals("MSExcel") || type.equals("MSExcelX")) {
            namedRefType = new String("excel");
        } else if (type.equals("MSWord") || type.equals("MSWordX")) {
            namedRefType = new String("word");
        } else if (type.equals("MSPowerPoint") || type.equals("MSPowerPointX")) {
            namedRefType = new String("powerpoint");
        } else if (type.equals("Text")) {
            namedRefType = new String("Text");
        } else if (type.equals("PDF")) {
            namedRefType = new String("PDF_Reference");
        } else if (type.equals("Image")) {
            namedRefType = new String("Image");
        } else if (type.equals("ACADDWG")) {
            namedRefType = new String("DWG");
        } else if (type.equals("Zip")) {
            namedRefType = new String("ZIPFILE");
        } else if (type.equals("HTML")) {
            namedRefType = new String("HTML");
        } else if (type.equals("MISC")) {
            namedRefType = new String("MISC_TEXT");
        }
        Registry client_specific = Registry.getRegistry("client_specific");
        String exportDir = client_specific.getString("TCExportDir");
        File folder = new File(exportDir);
        if (!folder.exists()) {
            folder.mkdir();
        }
        File[] files = dataset.getFiles(namedRefType, exportDir);
        return files;
    }

    private static String getFileName(File file) throws Exception {
        if (file.isDirectory())
            return file.getName();
        else {
            String filename = file.getName();
            int i = filename.lastIndexOf(".");
            if (i > 0) {
                return filename.substring(0, i);
            }
        }
        return null;
    }

    private static String getExtension(File file) throws Exception {
        if (file.isDirectory())
            return null;
        String filename = file.getName();
        int i = filename.lastIndexOf(".");
        if (i > 0 && i < filename.length() - 1) {
            return filename.substring(i + 1).toLowerCase();
        }
        return null;
    }

    public static void datasetUpdate(File file, TCComponentDataset dataset) throws Exception {
        NamedReferenceContext[] namedRefContext = dataset.getDatasetDefinitionComponent().getNamedReferenceContexts();
        for (int i = 0; i < namedRefContext.length; i++) {
            dataset.removeNamedReference(namedRefContext[i].getNamedReference());
        }
        TCComponentDatasetDefinition datasetDefinition = dataset.getDatasetDefinitionComponent();
        NamedReferenceContext namedRefTypes[] = datasetDefinition.getNamedReferenceContexts();
        String as1[] = { file.getAbsolutePath() };
        String as2[] = { namedRefTypes[0].getFileFormat() };
        String as3[] = { namedRefTypes[0].getMimeType() };
        String as4[] = { namedRefTypes[0].getNamedReference() };
        dataset.setFiles(as1, as2, as3, as4);
    }

    /**
     * BOP BOMLine에 자원데이터를 할당 (InterfaceAIFComponent)
     * 
     * 
     * @method connectObject
     * @date 2013. 12. 4.
     * @param TCComponentBOMLine
     *            targetBOMLine(paste 대상 BOMLine), ArrayList<TCComponentBOMLine> sourceBOMLines(할당될 BOMLine List), String paramString(타입)
     * @return void
     * @exception
     * @throws
     * @see
     */
    public static TCComponent[] connectObject(TCComponentBOMLine targetBOMLine, ArrayList<InterfaceAIFComponent> sourceComponents, String paramString) throws Exception {
        ArrayList<MFGConnectData> pasteDataList = new ArrayList<MFGConnectData>();
        for (InterfaceAIFComponent sourceComponent : sourceComponents) {
            MFGConnectData localMFGConnectData = new MFGConnectData(targetBOMLine);
            localMFGConnectData.add(sourceComponent, paramString);
            pasteDataList.add(localMFGConnectData);
        }
        TCComponent[] tcComponents = connectObject(targetBOMLine, pasteDataList);
        // 수량 기본값 1 입력
        for (TCComponent tcComponent : tcComponents) {
            tcComponent.setProperty(SDVPropertyConstant.BL_QUANTITY, "1");
        }
        return tcComponents;
    }

    /**
     * BOP BOMLine에 자원데이터를 할당 (InterfaceAIFComponent)
     * 
     * 
     * @method connectObject
     * @date 2013. 12. 4.
     * @param TCComponentBOMLine
     *            targetBOMLine(paste 대상 BOMLine), ArrayList<TCComponentBOMLine> sourceBOMLines(할당될 BOMLine List), String paramString(타입)
     * @return void
     * @exception
     * @throws
     * @see
     */
    public static TCComponent[] connectObjects(TCComponentBOMLine targetBOMLine, ArrayList<InterfaceAIFComponent> sourceComponents, ArrayList<String> arrParamString) throws Exception {
        TCComponent[] arrTCComponents = new TCComponent[sourceComponents.size()];

        for (int i = 0; i < sourceComponents.size(); i++) {
            ArrayList<InterfaceAIFComponent> sourceComponent = new ArrayList<InterfaceAIFComponent>();
            sourceComponent.add(sourceComponents.get(i));

            arrTCComponents[i] = connectObject(targetBOMLine, sourceComponent, arrParamString.get(i))[0];
        }

        return arrTCComponents;
    }

    /**
     * BOP BOMLine에 자원데이터를 할당 (MFGConnectData)
     * 
     * 
     * @method connectObject
     * @date 2013. 12. 4.
     * @param TCComponentBOMLine
     *            targetBOMLine(paste 대상 BOMLine), ArrayList<MFGConnectData> pasteDataList(할당될 MFGConnectData List)
     * @return void
     * @exception
     * @throws
     * @see
     */
    public static TCComponent[] connectObject(TCComponentBOMLine targetBOMLine, ArrayList<MFGConnectData> pasteDataList) throws Exception {
        // Refresh
        bomLineRefresh(targetBOMLine);
        MFGConnectSOAService mfgSoaService = new MFGConnectSOAService();
        mfgSoaService.setSessionService(AifrcpPlugin.getSessionService());
        DataManagement.ConnectObjectResponse pasteResponse = mfgSoaService.paste(pasteDataList);
        if (pasteResponse.serviceData.sizeOfPartialErrors() > 0) {
            ErrorStack localErrorStack = pasteResponse.serviceData.getPartialError(0);
            String[] arrayOfString = localErrorStack.getMessages();
            String errorMsg = "";
            for (int i = 0; i < arrayOfString.length; ++i) {
                errorMsg = errorMsg + arrayOfString[i] + ". ";
            }
            throw new Exception(errorMsg);
        }
        // Refresh
        bomLineRefresh(targetBOMLine);
        return pasteResponse.newObjects;
    }

    /**
     * BOMLine Refresh.
     * 
     * @method bomLineRefresh
     * @date 2013. 12. 12.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    public static void bomLineRefresh(TCComponentBOMLine bomLine) throws Exception {
        bomLine.clearCache();
        bomLine.refresh();
        bomLine.window().fireComponentChangeEvent();
    }

    /**
     * 
     * 대상 BOMLine으로 부터 BOMLine을 제거함
     * 
     * @method disconnectObjects
     * @date 2013. 12. 12.
     * @param TCComponentBOMLine
     *            targetBOMLine 대상 BOMLine
     * @param ArrayList
     *            <TCComponentBOMLine> disconectBOMLineList 제거될 BOMLine 리스트
     * @return void
     * @exception
     * @throws
     * @see
     */
    public static void disconnectObjects(TCComponentBOMLine targetBOMLine, ArrayList<TCComponentBOMLine> disconectBOMLineList) {
        ArrayList<com.teamcenter.services.rac.manufacturing._2009_10.DataManagement.DisconnectInput> disConnectInputList = new ArrayList<com.teamcenter.services.rac.manufacturing._2009_10.DataManagement.DisconnectInput>();
        DataManagementService datamanagementservice = DataManagementService.getService(targetBOMLine.getSession());

        for (TCComponentBOMLine endItemBopLine : disconectBOMLineList) {
            com.teamcenter.services.rac.manufacturing._2009_10.DataManagement.DisconnectInput input = new com.teamcenter.services.rac.manufacturing._2009_10.DataManagement.DisconnectInput();
            input.clientId = "1";
            input.object = endItemBopLine;
            input.disconnectFrom = targetBOMLine;
            disConnectInputList.add(input);
        }
        com.teamcenter.services.rac.manufacturing._2009_10.DataManagement.DisconnectInput inputArray[] = new com.teamcenter.services.rac.manufacturing._2009_10.DataManagement.DisconnectInput[disConnectInputList.size()];
        disConnectInputList.toArray(inputArray);
        datamanagementservice.disconnectObjects(inputArray);
        disConnectInputList.clear();
    }

    /**
     * 하위 ComponentBOMLine List
     * 
     * @method getChildrenBOMLine
     * @date 2013. 11. 29.
     * @param
     * @return TCComponentBOMLine[]
     * @exception
     * @throws
     * @see
     */
    public static TCComponentBOMLine[] getChildrenBOMLine(TCComponentBOMLine parentBOMLine) throws Exception {
        AIFComponentContext contexts[] = parentBOMLine.getChildren();
        TCComponentBOMLine childLines[] = new TCComponentBOMLine[contexts.length];
        for (int i = 0; i < childLines.length; i++) {
            childLines[i] = (TCComponentBOMLine) contexts[i].getComponent();
        }
        return childLines;
    }

    /**
     * 하위 ComponentBOMLine List - unpack
     * [20150401] shcho, unpack 후 parentBOMLine을 refresh 하도록 수정
     * 
     * @method getUnpackChildrenBOMLine
     * @date 2014. 1. 7.
     * @param
     * @return TCComponentBOMLine[]
     * @exception
     * @throws
     * @see
     */
    public static TCComponentBOMLine[] getUnpackChildrenBOMLine(TCComponentBOMLine parentBOMLine) throws Exception {
        AIFComponentContext contexts[] = parentBOMLine.getChildren();
        TCComponentBOMLine childLines[] = new TCComponentBOMLine[contexts.length];
        for (int i = 0; i < childLines.length; i++) {
            TCComponentBOMLine childBOMLine = (TCComponentBOMLine) contexts[i].getComponent();
            if (childBOMLine.isPacked()) {
                childBOMLine.unpack();
            }
        }
        parentBOMLine.refresh();
        return getChildrenBOMLine(parentBOMLine);
    }

    /**
     * BOMLine이 pack이면 unpack BOMLine List를 가져온다.
     * 
     * @method getUnpackBOMLines
     * @date 2014. 1. 10.
     * @param
     * @return TCComponentBOMLine[]
     * @exception
     * @throws
     * @see
     */
    public static TCComponentBOMLine[] getUnpackBOMLines(TCComponentBOMLine packBOMLine) throws Exception {
        if (packBOMLine == null) {
            return null;
        }
        if (!packBOMLine.isPacked()) {
            return new TCComponentBOMLine[] { packBOMLine };
        }
        TCComponentBOMLine[] packedLines = packBOMLine.getPackedLines();
        TCComponentBOMLine[] unpackLines = new TCComponentBOMLine[packedLines.length + 1];
        System.arraycopy(packedLines, 0, unpackLines, 0, packedLines.length);
        packBOMLine.unpack();
        packBOMLine.refresh();
        packBOMLine.parent().refresh();
        unpackLines[unpackLines.length - 1] = packBOMLine;
        return unpackLines;
    }

    /**
     * OPTION CONDITION 등록 - 할당시 Condition이 없으면 상위(공법)BOMLine의 Condition을 등록한다.
     * 
     * - 부자재 할당 시 사용
     * 
     * @method updateAssiginOptionCondition
     * @date 2014. 1. 7.
     * @param
     * @return TCComponentBOMLine
     * @exception
     * @throws
     * @see
     */
    public static void updateAssiginOptionCondition(TCComponentBOMLine bomLine, String conversionOptionCondition) throws Exception {
        // Condition이 empty이면 상위에서 Condition을 얻어온다.
        if (StringUtils.isEmpty(conversionOptionCondition)) {
            conversionOptionCondition = BundleUtil.nullToString(bomLine.parent().getProperty(SDVPropertyConstant.BL_OCC_MVL_CONDITION)).trim();
        }
        if (!StringUtils.isEmpty(conversionOptionCondition)) {
            updateOptionCondition(bomLine, conversionOptionCondition);
        }
    }

    /**
     * OPTION CONDITION 등록
     * 
     * @method updateOptionCondition
     * @date 2013. 12. 10.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    public static void updateOptionCondition(TCComponentBOMLine bomLine, String conversionOptionCondition) throws Exception {
        if (!StringUtils.isEmpty(conversionOptionCondition)) {
            TCVariantService svc = bomLine.getSession().getVariantService();
            svc.setLineMvlCondition(bomLine, conversionOptionCondition);
        }
    }

    public static MFGLegacyApplication getMFGApplication() {
        return (MFGLegacyApplication) AIFDesktop.getActiveDesktop().getCurrentApplication();
    }

    /**
     * BOM라인에서 하위 라인을 확장하는 함수
     */
    public static void executeExpandOneLevel() {
        executeExpandOneLevel(getMFGApplication().getAbstractViewableTreeTable());
    }

    /**
     * BOM라인에서 하위 라인을 확장하는 함수
     */
    public static void executeExpandOneLevel(AbstractViewableTreeTable viewTreeTable) {
        if (viewTreeTable == null)
            return;
        ExpandBelowOperation expandOperation = new ExpandBelowOperation(viewTreeTable, 1, false);
        CustomUtil.getTCSession().queueOperation(expandOperation);
    }

    /**
     * Line BOMLine이 미할당 라인 인지 유무를 확인
     * 
     * @method isAssyTempLine
     * @date 2014. 2. 20.
     * @param
     * @return boolean
     * @exception
     * @throws
     * @see
     */
    public static boolean isAssyTempLine(TCComponentBOMLine lineBOMLine) throws Exception {
        String plantIPrefix = "PTP";
        if (!lineBOMLine.getItem().getType().equals(SDVTypeConstant.BOP_PROCESS_LINE_ITEM))
            return false;
        TCComponentBOMLine shopBOMLine = lineBOMLine.parent();
        if (shopBOMLine == null)
            return false;

        String shopCode = shopBOMLine.getItemRevision().getProperty(SDVPropertyConstant.SHOP_REV_SHOP_CODE);
        String lineId = lineBOMLine.getProperty(SDVPropertyConstant.BL_ITEM_ID);
        String tempLinePrefix = plantIPrefix.concat("-").concat(shopCode).concat("-").concat("TEMP");

        if (lineId.startsWith(tempLinePrefix))
            return true;

        return false;
    }

}

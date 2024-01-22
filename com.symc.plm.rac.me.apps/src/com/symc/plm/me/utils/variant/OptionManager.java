/**
 * 
 */
package com.symc.plm.me.utils.variant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import com.ssangyong.common.remote.DataSet;
import com.ssangyong.common.remote.SYMCRemoteUtil;
import com.ssangyong.common.utils.TcDefinition;
import com.symc.plm.me.common.SDVBOPUtilities;
import com.symc.plm.me.common.SDVTypeConstant;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.kernel.TCComponentBOMWindowType;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentQuery;
import com.teamcenter.rac.kernel.TCComponentQueryType;
import com.teamcenter.rac.kernel.TCComponentRevisionRuleType;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCPreferenceService;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.kernel.TCVariantService;
import com.teamcenter.rac.kernel.TCPreferenceService.TCPreferenceLocation;
import com.teamcenter.rac.pse.variants.modularvariants.ConditionElement;
import com.teamcenter.rac.pse.variants.modularvariants.ConstraintsModel;
import com.teamcenter.rac.pse.variants.modularvariants.CustomMVPanel;
import com.teamcenter.rac.pse.variants.modularvariants.MVLLexer;
import com.teamcenter.rac.pse.variants.modularvariants.OVEOption;
import com.teamcenter.services.internal.rac.structuremanagement.VariantManagementService;
import com.teamcenter.services.internal.rac.structuremanagement._2011_06.VariantManagement;
import com.teamcenter.services.internal.rac.structuremanagement._2011_06.VariantManagement.ModularOption;
import com.teamcenter.services.internal.rac.structuremanagement._2011_06.VariantManagement.ModularOptions;
import com.teamcenter.services.internal.rac.structuremanagement._2011_06.VariantManagement.ModularOptionsInfo;
import com.teamcenter.services.internal.rac.structuremanagement._2011_06.VariantManagement.ModularOptionsOutput;

/**
 * 옵션 관리 유틸
 * [SR150623-040][20150715] shcho, Cooporate Option에 없는 Option을 갖고 있더라도  Manage Option을 실행할 수 있도록 보완('도장'을 위해서)
 * 
 * @author slobbie
 * 
 */
public class OptionManager {

    private ArrayList<TCComponentBOMLine> corporateBOMLines;
    private HashMap<String, String> optionItemMap = new HashMap<String, String>(); // 아이템 명을 Corporate Option Item이름으로 변경하기 위한 맵.
    private ArrayList<VariantOption> corpOptionSet = new ArrayList<VariantOption>(); // Corp Option set을 넣어두기 위한 list;
    private HashMap<String, String> descMap = null;
    private TCComponentBOMLine bomline = null;
    private HashMap<String, VariantOption> corpOptionMap = new HashMap<String, VariantOption>(); // 옵션 명, 옵션
    private TCSession session = null;
    private HashMap<String, VariantValue> valueMap = new HashMap<String, VariantValue>();
    private String[] corporateOptionItemIds = null;
    private WaitProgressBar waitProgress = null;

    /**
     * 
     * @param bomline
     * @param bIncludeCorpItem
     *            Corp Option Item을 검색하여 옵션셋을 정의해 둘지.. True인 경우, 차후에 Clear()를 반드시 호출해야함.
     * @throws Exception
     */
    public OptionManager(TCComponentBOMLine bomline, boolean bIncludeCorpItem) throws Exception {
        this(bomline, bIncludeCorpItem, null);
    }

    public OptionManager(TCComponentBOMLine bomline, boolean bIncludeCorpItem, WaitProgressBar waitProgress) throws Exception {
        this.bomline = bomline;
        this.session = bomline.getSession();
        this.waitProgress = waitProgress;
        init(bIncludeCorpItem);
    }

    public void setStatus(String msg) {
        if (waitProgress != null) {
            waitProgress.setStatus(msg);
        }
    }

    /**
     * 1. Option Variant Value의 설명을 모두 가져온다.
     * 2. Corporate Option Item을 모두 검색하여, 설정해둔 Option을 가져온다.
     * 
     * @param bIncludeCorpItem
     * @throws Exception
     */
    private void init(boolean bIncludeCorpItem) throws Exception {
        descMap = getDesc();
        if (bIncludeCorpItem) {
            ArrayList<VariantOption> optionSet = null;
            TCPreferenceService preferenceService = session.getPreferenceService();
//            corporateOptionItemIds = preferenceService.getStringArray(TCPreferenceService.TC_preference_site, "PSM_global_option_item_ids");
            corporateOptionItemIds = preferenceService.getStringValuesAtLocation("PSM_global_option_item_ids", TCPreferenceLocation.OVERLAY_LOCATION);
            for (int i = 0; corporateOptionItemIds != null && i < corporateOptionItemIds.length; i++) {

                try {
                    TCComponent[] corpItems = queryComponent("Item...", new String[] { "Type", "ItemID" }, new String[] { "Corporate Option Item", corporateOptionItemIds[i] });
                    if (corpItems == null || corpItems.length < 1) {
                        continue;
                    }
                    corporateBOMLines = getBOMLines(corpItems);
                    for (TCComponentBOMLine line : corporateBOMLines) {
                        try {
                            optionSet = getCorporateOptionSet(line, false);
                            corpOptionSet.addAll(optionSet);
                        } catch (TCException tce) {
                            tce.printStackTrace();
                        }

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

        }
    }

    public String[] getCorporateOptionItemIds() {
        return corporateOptionItemIds;
    }

    /**
     * Corporate Option Item
     * 
     * @return
     */
    public ArrayList<VariantOption> getCorpOptionSet() {
        return corpOptionSet;
    }

    /**
     * Corporate Option Item은 1개만 사용되어 진다.
     * 만약 변경되면.... OTL...
     * 
     * @return
     */
    public ArrayList<TCComponentBOMLine> getCorporateBOMLines() {
        return corporateBOMLines;
    }

    public HashMap<String, String> getDescMap() {
        return descMap;
    }

    public HashMap<String, VariantOption> getCorpOptionMap() {
        return corpOptionMap;
    }

    public void usingCheck(TCComponentBOMLine parent, Vector<VariantValue> inUseValueList, HashMap<TCComponentBOMLine, List<String>> conditionMap, TCVariantService variantService, int curDept, int toDepth) throws TCException {
        setStatus("loading the Option Set of " + parent.getItemRevision());
        if (parent.hasChildren()) {
            AIFComponentContext[] contexts = parent.getChildren();
            String parentItemType = parent.getItem().getType();
            for (AIFComponentContext context : contexts) {
                TCComponentBOMLine childLine = (TCComponentBOMLine) context.getComponent();
               // String itemType = childLine.getItem().getType();

                if (parentItemType.equals(TcDefinition.FUNCTION_MASTER_ITEM_TYPE)) {
                    List<String> conditionList = conditionMap.get(childLine);
                    String itemId = bomline.getItem().getProperty("item_id");
                    if (inUseValueList != null && !inUseValueList.isEmpty()) {
                        for (VariantValue value : inUseValueList) {
                            String key = itemId + ":" + value.getOption().getOptionName() + "=" + value.getValueName();
                            if (conditionList.contains(key)) {
                                value.setUsing(true);
                            }
                        }
                    }
                }

                Vector<String[]> userDefineErrorList = new Vector<String[]>();// 사용자 정의 유효성 검사.
                Vector<VariantValue> list = null;

                if (curDept < toDepth) {

                    list = getVariantValues(childLine, inUseValueList, userDefineErrorList);
                    if (list != null && !list.isEmpty()) {
                        for (int j = 0; j < inUseValueList.size(); j++) {
                            VariantValue value = inUseValueList.get(j);
                            for (int i = 0; i < list.size(); i++) {
                                VariantValue val = list.get(i);

                                // 사용중이면 새로 추가한 Value 인식시키고 삭제 가능하도록 한다.
                                // 같으면 사용 중이므로, setNew ==> false;
                                if (value.equals(val)) {
                                    value.setUsing(true);
                                }

                            }
                        }
                    }

                    usingCheck(childLine, inUseValueList, conditionMap, variantService, curDept + 1, toDepth);
                }

            }
        }
    }

    /**
     * parent하위에서 condition으로 VariantValue를 사용중인 경우, 해당 VariantValue의 isUsing을 true로 변경.
     * Condition은 Function Master 바로 하위 1레벨에만 설정한다.
     * 
     * @param parent
     * @param inUseValueList
     * @param conditionMap
     * @param variantService
     * @throws TCException
     */
    public void usingCheck(TCComponentBOMLine parent, Vector<VariantValue> inUseValueList, HashMap<TCComponentBOMLine, List<String>> conditionMap, TCVariantService variantService) throws TCException {

        if (parent.hasChildren()) {
            AIFComponentContext[] contexts = parent.getChildren();
            String parentItemType = parent.getItem().getType();
            for (AIFComponentContext context : contexts) {
                TCComponentBOMLine childLine = (TCComponentBOMLine) context.getComponent();

                List<String> conditionList = conditionMap.get(childLine);
                if (conditionList != null) {
                    String itemId = bomline.getItem().getProperty("item_id");
                    if (inUseValueList != null && !inUseValueList.isEmpty()) {
                        for (VariantValue value : inUseValueList) {
                            String key = itemId + ":" + value.getOption().getOptionName() + "=" + value.getValueName();
                            if (conditionList.contains(key)) {
                                value.setUsing(true);
                            }
                        }
                    }
                }

                Vector<String[]> userDefineErrorList = new Vector<String[]>();// 사용자 정의 유효성 검사.
                Vector<VariantValue> list = null;

                String itemType = childLine.getItem().getType();
                if (itemType.equals(TcDefinition.PRODUCT_ITEM_TYPE) || itemType.equals(TcDefinition.VARIANT_ITEM_TYPE) || itemType.equals(TcDefinition.FUNCTION_ITEM_TYPE) || itemType.equals(TcDefinition.FUNCTION_MASTER_ITEM_TYPE) || parentItemType.equals(TcDefinition.FUNCTION_MASTER_ITEM_TYPE)) {

                    list = getVariantValues(childLine, inUseValueList, userDefineErrorList);
                    if (list != null && !list.isEmpty()) {
                        for (int j = 0; j < inUseValueList.size(); j++) {
                            VariantValue value = inUseValueList.get(j);
                            for (int i = 0; i < list.size(); i++) {
                                VariantValue val = list.get(i);

                                // 사용중이면 새로 추가한 Value 인식시키고 삭제 가능하도록 한다.
                                // 같으면 사용 중이므로, setNew ==> false;
                                if (value.equals(val)) {
                                    value.setUsing(true);
                                }

                            }
                        }
                    }

                    usingCheck(childLine, inUseValueList, conditionMap, variantService);
                }

            }
        }

    }

    /**
     * 
     * @param parent
     * @param unUsedValueList
     *            사용자에 의해 unchecked된 VariantValue
     * @param variantService
     * @throws TCException
     */
    public void applyToChild(TCComponentBOMLine parent, Vector<VariantValue> unUsedValueList) throws TCException {

        if (parent.hasChildren()) {
            AIFComponentContext[] contexts = parent.getChildren();
            for (AIFComponentContext context : contexts) {
                TCComponentBOMLine childLine = (TCComponentBOMLine) context.getComponent();

                String type = childLine.getItem().getType();
                if (type.equals(TcDefinition.PRODUCT_ITEM_TYPE) || type.equals(TcDefinition.VARIANT_ITEM_TYPE) || type.equals(TcDefinition.FUNCTION_ITEM_TYPE)) {
                    childLine.refresh();
                    Vector<String[]> userDefineErrorList = new Vector<String[]>();// 사용자 정의 유효성 검사.
                    Vector<VariantValue> list = getVariantValues(childLine, unUsedValueList, userDefineErrorList);
                    if (list != null && !list.isEmpty()) {
                        setStatus("Applying to " + childLine.toDisplayString());
                        TCVariantService variantService = childLine.getSession().getVariantService();
                        apply(list, userDefineErrorList, childLine, variantService);
                    }

                    if (!type.equals(TcDefinition.FUNCTION_ITEM_TYPE))
                        applyToChild(childLine, unUsedValueList);

                } else {
                    return;
                }

            }

        }

    }

    /**
     * bomLine에 설정된 Variant 설정을 VariantValue 형태의 List로 리턴.
     * 
     * @param bomLine
     * @param inUseValueList
     * @param userDefineErrorList
     * @param variantService
     * @return
     * @throws TCException
     */
    private Vector<VariantValue> getVariantValues(TCComponentBOMLine bomLine, Vector<VariantValue> inUseValueList, Vector<String[]> userDefineErrorList) throws TCException {

        String mvlStr = bomLine.getItemRevision().getProperty("mvl_text");
        if (mvlStr == null || mvlStr.equals("")) {
            return null;
        }

        TCVariantService variantService = bomLine.getSession().getVariantService();
        String lineMvl = variantService.askLineMvl(bomLine);

        List<String> equalErrorList = null;
        List<String> notUseList = new ArrayList<String>();
        List<String> notDefineList = new ArrayList<String>();
        Vector<VariantValue> list = new Vector<VariantValue>();

        // if( lineMvl == null || lineMvl.equals("")) {
        // return list;
        // }

        ConstraintsModel constraintsModel = new ConstraintsModel(bomLine.getItem().getProperty("item_id"), lineMvl, null, bomLine, variantService);

        if (!constraintsModel.parse()) {
            throw new TCException("Condition을 파싱 할 수 없습니다.");
        }

        // 에러 항목 체크 ==================================
        boolean isUserDefine = false;
        String[][] errors = constraintsModel.errorChecksTableData();
        for (String[] errorInfo : errors) {
            if (errorInfo[1] != null && !errorInfo[1].equals("")) {
                if (errorInfo[1].indexOf(VariantValue.TC_MESSAGE_NOT_DEFINE) > -1) {
                    equalErrorList = notDefineList;
                    isUserDefine = false;
                } else if (errorInfo[1].indexOf(VariantValue.TC_MESSAGE_NOT_USE) > -1) {
                    equalErrorList = notUseList;
                    isUserDefine = false;
                } else {
                    isUserDefine = true;
                }
            }

            if (!isUserDefine && errorInfo[5].equals("=")) {
                if (equalErrorList != null && !equalErrorList.contains(errorInfo[4] + "!" + errorInfo[6]))
                    equalErrorList.add(errorInfo[4] + "!" + errorInfo[6]);
            }

            if (isUserDefine) {

                boolean isContain = false;
                String key = "";
                for (String tmpStr : errorInfo) {
                    key += (tmpStr + ",");
                }
                for (String[] tmpInfo : userDefineErrorList) {

                    String tmpkey = "";
                    for (String tmpStr : tmpInfo) {
                        tmpkey += (tmpStr + ",");
                    }
                    if (key.equals(tmpkey)) {
                        isContain = true;
                    }
                }

                if (!isContain) {
                    userDefineErrorList.add(errorInfo);
                }
            }
        }

        ModularOption[] modularOptions = getModularOptions(bomLine);
        if (modularOptions != null && modularOptions.length > 0) {
            HashMap<Integer, OVEOption> options = new HashMap<Integer, OVEOption>();
            for (ModularOption mOption : modularOptions) {

                OVEOption option = CustomMVPanel.getOveOption(bomLine, options, mOption);
                // Corporate Option Item Option에 존재 한다면, 아이템 명을 Corporate Option Item으로 변경함.
                String itemId = optionItemMap.get(mOption.optionName);
                // optionItemMap에서 찾을 수 없으면, 현재 BOM line의 아이템 아이디를 적용.
                // 이런 경우는 실무에서 발생하면 안됨.
                if (itemId == null) {
                    itemId = bomLine.getItem().getProperty("item_id");
                    throw new TCException("Corporate Item Option 에서 " + option.option.name + "이(가) 적용된 아이템을 찾을 수 없습니다.");
                }
                option.option.item = itemId; // 000090

                VariantOption variantOption = new VariantOption(option, itemId, option.option.name, option.option.desc);
                String[] values = option.comboValues;
                for (String value : values) {

                    int valueStatus = -1;
                    if (notUseList.contains(variantOption.getOptionName() + "!" + value)) {
                        valueStatus = VariantValue.VALUE_NOT_USE;
                    } else if (notDefineList.contains(variantOption.getOptionName() + "!" + value)) {
                        valueStatus = VariantValue.VALUE_NOT_DEFINE;
                    } else {
                        valueStatus = VariantValue.VALUE_USE;
                    }
                    VariantValue variantValue = new VariantValue(variantOption, value, (String) descMap.get(value), valueStatus, false);
                    variantOption.addValue(variantValue);
                    list.add(variantValue);
                }
            }
        }

        for (VariantValue value : inUseValueList) {

            for (VariantValue val : list) {

                // 현재의 BOM line에서 사용중인 옵션값이다.
                if (value.equals(val)) {
                    val.setValueStatus(value.getValueStatus());
                }
            }
        }

        return list;
    }

    /**
     * bomline의 옵션 값을 변경한다.
     * 
     * @param data
     * @param userDefineErrorList
     * @param bomLine
     * @throws TCException
     */
    private void apply(Vector<VariantValue> data, Vector<String[]> userDefineErrorList, TCComponentBOMLine bomLine, TCVariantService variantService) throws TCException {

        InterfaceAIFComponent[] targets = AIFUtility.getCurrentApplication().getTargetComponents();
        if (targets[0] instanceof TCComponentBOMLine) {
            try {
                ArrayList<String> appliedOption = new ArrayList<String>();

                HashMap<String, VariantErrorCheck> notUseErrorMap = new HashMap<String, VariantErrorCheck>();
                HashMap<String, VariantErrorCheck> notDefineErrorMap = new HashMap<String, VariantErrorCheck>();

                int curNum = 0;
                for (VariantValue value : data) {
                    VariantOption option = value.getOption();

                    if (value.getValueStatus() == VariantValue.VALUE_USE) {

                        if (appliedOption.contains(option.getOptionName())) {
                            continue;
                        }

                        boolean isExist = false;
                        ModularOption[] modularOptions = getModularOptions(bomLine);
                        if (modularOptions != null && modularOptions.length > 0) {
                            HashMap<Integer, OVEOption> options = new HashMap<Integer, OVEOption>();
                            for (ModularOption mOption : modularOptions) {

                                // Corporate Option Item Option에 존재 한다면, 아이템 명을 Corporate Option Item으로 변경함.
                                OVEOption oveOption = CustomMVPanel.getOveOption(bomLine, options, mOption);
                                String itemId = optionItemMap.get(oveOption.option.name);
                                if (itemId == null) {
                                    itemId = bomLine.getItem().getProperty("item_id");
                                    throw new TCException("Corporate Item Option 에서 " + oveOption.option.name + "이(가) 적용된 아이템을 찾을 수 없습니다.");
                                }
                                if (option.getItemId().equals(itemId) && option.getOptionName().equals(oveOption.option.name)) {
                                    isExist = true;
                                    break;
                                }
                            }
                        }

                        // 이미 사용중이 아닌것만 추가하기 위해.
                        if (!isExist) {
                            String s = OptionManager.getOptionString(option);
                            try {
                                variantService.lineDefineOption(bomLine, s);
                            } catch (TCException tce) {
                                tce.printStackTrace();
                            } finally {
                                appliedOption.add(option.getOptionName());
                                curNum++;
                                if (curNum % 20 == 19) {
                                    bomLine.window().save();
                                }
                                bomLine.refresh();
                            }

                        }

                        // 체크박스를 해제 한 경우
                        // Not Use로 할지 Not Define으로 할지 미정.
                    } else if (value.getValueStatus() == VariantValue.VALUE_NOT_USE) {
                        // condition 추가

                        VariantErrorCheck notUseErrorcheck = notUseErrorMap.get(option.getOptionName());
                        if (notUseErrorcheck == null) {
                            notUseErrorcheck = new VariantErrorCheck();
                            notUseErrorcheck.type = "inform";
                            notUseErrorcheck.message = VariantValue.TC_MESSAGE_NOT_USE;
                        }

                        ConditionElement condition = new ConditionElement();
                        if (notUseErrorcheck.getConditionSize() == 0) {
                            condition.ifOrAnd = "if";
                        } else {
                            condition.ifOrAnd = "or";
                        }
                        condition.item = bomLine.getItem().getProperty("item_id");
                        condition.op = "=";
                        condition.option = option.getOptionName();
                        condition.value = value.getValueName();
                        condition.valueIsString = true;
                        condition.fullName = condition.item + ":" + MVLLexer.mvlQuoteId(condition.option, false);

                        notUseErrorcheck.addCondition(condition);
                        notUseErrorMap.put(option.getOptionName(), notUseErrorcheck);
                        // 옵션 트리에서 옵션테이블로 이동 조차 하지 않은 Value
                    } else {

                        VariantErrorCheck notDefineErrorcheck = notDefineErrorMap.get(option.getOptionName());
                        if (notDefineErrorcheck == null) {
                            notDefineErrorcheck = new VariantErrorCheck();
                            notDefineErrorcheck.type = "inform";
                            notDefineErrorcheck.message = VariantValue.TC_MESSAGE_NOT_DEFINE;
                        }

                        // condition 추가
                        ConditionElement condition = new ConditionElement();
                        if (notDefineErrorcheck.getConditionSize() == 0) {
                            condition.ifOrAnd = "if";
                        } else {
                            condition.ifOrAnd = "or";
                        }
                        condition.item = bomLine.getItem().getProperty("item_id");
                        condition.op = "=";
                        condition.option = option.getOptionName();
                        condition.value = value.getValueName();
                        condition.valueIsString = true;
                        condition.fullName = condition.item + ":" + MVLLexer.mvlQuoteId(condition.option, false);

                        notDefineErrorcheck.addCondition(condition);
                        notDefineErrorMap.put(option.getOptionName(), notDefineErrorcheck);
                    }
                }

                StringBuilder sb = new StringBuilder();
                Set<String> set = notUseErrorMap.keySet();
                Iterator<String> its = set.iterator();
                while (its.hasNext()) {
                    String key = its.next();
                    VariantErrorCheck notUseErrorcheck = notUseErrorMap.get(key);
                    String msg = VariantValue.TC_MESSAGE_NOT_USE;
                    ConditionElement[] elements = notUseErrorcheck.getCondition();
                    for (int i = 0; elements != null && i < elements.length; i++) {
                        if (i == 0) {
                            msg += "[";
                        }
                        msg += (i > 0 ? ", " : "") + elements[i].value;
                        if (i == elements.length - 1) {
                            msg += "]";
                        }
                    }
                    notUseErrorcheck.message = msg;
                    notUseErrorcheck.appendConstraints(sb);
                }

                set = notDefineErrorMap.keySet();
                its = set.iterator();
                while (its.hasNext()) {
                    String key = its.next();
                    VariantErrorCheck notDefineErrorcheck = notDefineErrorMap.get(key);
                    String msg = VariantValue.TC_MESSAGE_NOT_DEFINE;
                    ConditionElement[] elements = notDefineErrorcheck.getCondition();
                    for (int i = 0; elements != null && i < elements.length; i++) {
                        if (i == 0) {
                            msg += "[";
                        }
                        msg += (i > 0 ? ", " : "") + elements[i].value;
                        if (i == elements.length - 1) {
                            msg += "]";
                        }
                    }
                    notDefineErrorcheck.message = msg;
                    notDefineErrorcheck.appendConstraints(sb);
                }

                VariantErrorCheck userDefineErrorcheck = null;
                if (userDefineErrorList != null && !userDefineErrorList.isEmpty()) {
                    for (String[] errorInfo : userDefineErrorList) {
                        ConditionElement condition = new ConditionElement();

                        if (errorInfo[0] != null && !errorInfo[0].equals("")) {
                            if (userDefineErrorcheck != null) {
                                userDefineErrorcheck.appendConstraints(sb);
                            }
                            userDefineErrorcheck = new VariantErrorCheck();
                            userDefineErrorcheck.type = errorInfo[0];
                        }
                        if (errorInfo[1] != null && !errorInfo[1].equals("")) {
                            userDefineErrorcheck.message = errorInfo[1];

                        }
                        condition.ifOrAnd = errorInfo[2];
                        condition.item = errorInfo[3];
                        condition.op = errorInfo[5];
                        condition.option = errorInfo[4];
                        condition.value = errorInfo[6];
                        condition.valueIsString = true;
                        condition.fullName = errorInfo[3] + ":" + MVLLexer.mvlQuoteId(errorInfo[4], false);
                        userDefineErrorcheck.addCondition(condition);
                    }
                    userDefineErrorcheck.appendConstraints(sb);
                }

                if (notUseErrorMap.size() > 0 || notDefineErrorMap.size() > 0 || (userDefineErrorcheck != null && userDefineErrorcheck.getConditionSize() > 0)) {
                    variantService.setLineMvl(bomLine, sb.toString());
                } else {
                    variantService.setLineMvl(bomLine, "");
                }
                bomLine.window().save();
                bomLine.refresh();
            } catch (TCException e) {
                e.printStackTrace();
                throw e;
            }

        }
    }

    public boolean isUsingValue(TCComponentBOMLine parent, HashMap<TCComponentBOMLine, List<String>> conditionMap, String key) throws TCException {

        List<String> list = conditionMap.get(parent);
        if (list == null)
            return false;

        if (list.contains(key)) {
            return true;
        }

        boolean bInUse = false;
        if (parent.hasChildren()) {
            AIFComponentContext[] contexts = parent.getChildren();
            for (int i = 0; contexts != null && i < contexts.length && !bInUse; i++) {
                AIFComponentContext context = contexts[i];
                TCComponentBOMLine childLine = (TCComponentBOMLine) context.getComponent();
                bInUse = isUsingValue(childLine, conditionMap, key);
            }
        }

        return bInUse;
    }

    /**
     * 
     * BOM line(하위 BOM line 포함)에 설정된 Condition을 HashMap<TCComponentBOMLine, List<String>> 형태로 리턴한다.
     * 
     * @param parent
     * @param conditionMap
     * @return
     * @throws TCException
     */
    public HashMap<TCComponentBOMLine, List<String>> getConditionSetAll(TCComponentBOMLine parent, HashMap<TCComponentBOMLine, List<String>> conditionMap) throws TCException {

        if (conditionMap == null) {
            conditionMap = new HashMap<TCComponentBOMLine, List<String>>();
        }

        if (parent.hasChildren()) {
            String parentType = parent.getItem().getType();
            AIFComponentContext[] contexts = parent.getChildren();
            for (AIFComponentContext context : contexts) {
                TCComponentBOMLine childLine = (TCComponentBOMLine) context.getComponent();

                if (parentType.equals(TcDefinition.PRODUCT_ITEM_TYPE) || parentType.equals(TcDefinition.VARIANT_ITEM_TYPE) || parentType.equals(TcDefinition.FUNCTION_ITEM_TYPE) || parentType.equals(TcDefinition.FUNCTION_MASTER_ITEM_TYPE)) {
                    if (!conditionMap.containsKey(childLine)) {
                        if (parentType.equals(TcDefinition.FUNCTION_MASTER_ITEM_TYPE)) {
                            conditionMap.put(childLine, getSimpleConditionSet(childLine));
                            continue;
                        }
                    }

                    getConditionSetAll(childLine, conditionMap);
                } else {
                    return conditionMap;
                }

            }
        }

        return conditionMap;
    }

    /**
     * BOM line에 설정된 Condition List를 리턴한다.
     * 내부적인 key는 element.item + ":" + element.option + element.op + element.value이며,
     * custom tool을 이용한 경우 element.op 는 "=" 이다.
     * 
     * @param line
     * @return
     * @throws TCException
     */
    public List<String> getSimpleConditionSet(TCComponentBOMLine line) throws TCException {

        List<String> list = new ArrayList<String>();
        TCSession session = line.getSession();
        TCVariantService variantService = session.getVariantService();
        String lineMvl = variantService.askLineMvlCondition(line);
        ConditionElement[] elements = ConstraintsModel.parseACondition(lineMvl);
        //HashMap<String, VariantOption> map = new HashMap<String, VariantOption>();
        //ConditionVector vec = null;
        for (int i = 0; elements != null && i < elements.length; i++) {
            ConditionElement element = elements[i];
            // 사용자 정의가 아닌 경우 element.op 는 모두 "="이다.
            String key = element.item + ":" + element.option + element.op + element.value;
            if (!list.contains(key)) {
                list.add(key);
            }
        }
        return list;
    }

    /**
     * BOM line에 설정된 컨디션을 리턴.
     * 
     * @param line
     * @return
     * @throws TCException
     */
    public List<ConditionVector> getConditionSet(TCComponentBOMLine line) throws TCException {

        List<ConditionVector> list = new ArrayList<ConditionVector>();
        TCSession session = line.getSession();
        TCVariantService variantService = session.getVariantService();
        String lineMvl = variantService.askLineMvlCondition(line);
        ConditionElement[] elements = ConstraintsModel.parseACondition(lineMvl);
        //HashMap<String, VariantOption> map = new HashMap<String, VariantOption>();
        ConditionVector vec = null;
        for (int i = 0; elements != null && i < elements.length; i++) {
            ConditionElement element = elements[i];
            if (element.ifOrAnd.equalsIgnoreCase("if") || element.ifOrAnd.equalsIgnoreCase("or"))
                vec = new ConditionVector();
            vec.add(element);

            if (!list.contains(vec))
                list.add(vec);
        }
        return list;
    }

    /**
     * 테이블 모델 데이타에서 컨디션을 가져온다.
     * 
     * @param data
     *            모델 Data Vector
     * @return
     * @throws TCException
     */
    public ConditionVector getConditionSet(Vector<Vector<VariantValue>> data) throws TCException {

        if (data == null || data.isEmpty())
            return null;

        ConditionVector condition = new ConditionVector();
        for (int i = 0; i < data.size(); i++) {
            Vector<VariantValue> row = data.get(i);
            VariantValue value = row.get(0);
            VariantOption option = value.getOption();
            ConditionElement element = new ConditionElement();
            element.fullName = option.getItemId() + ":" + option.getOptionName();
            element.ifOrAnd = (i == 0 ? "" : "AND");
            element.item = option.getItemId();
            element.op = "=";
            element.option = option.getOptionName();
            element.value = value.getValueName();
            element.valueIsString = true;
            condition.add(element);
        }
        return condition;
    }

    /**
     * 
     * @param line
     *            현재 선택된 BOM line
     * @param userDefineErrorList
     * @return
     * @throws TCException
     */
    public ArrayList<VariantOption> getOptionSet(TCComponentBOMLine line, HashMap<String, VariantOption> optionMap, Vector<String[]> userDefineErrorList, Vector<String[]> moduleConstratintsList) throws TCException {
        final InterfaceAIFComponent[] coms = getTargets();
        return getOptionSet(line, optionMap, userDefineErrorList, moduleConstratintsList, line.equals(coms[0]));
    }

    public ArrayList<VariantOption> getOptionSet(TCComponentBOMLine line, Vector<String[]> userDefineErrorList, Vector<String[]> moduleConstratintsList) throws TCException {
        final InterfaceAIFComponent[] coms = getTargets();
        return getOptionSet(line, userDefineErrorList, moduleConstratintsList, line.equals(coms[0]));
    }

    public ArrayList<VariantOption> getOptionSet(TCComponentBOMLine line, Vector<String[]> userDefineErrorList, Vector<String[]> moduleConstraintList, boolean isTarget) throws TCException {
        return getOptionSet(line, null, userDefineErrorList, moduleConstraintList, isTarget, true);
    }

    public ArrayList<VariantOption> getOptionSet(TCComponentBOMLine line, HashMap<String, VariantOption> optionMap, Vector<String[]> userDefineErrorList, Vector<String[]> moduleConstraintList, boolean isTarget) throws TCException {
        return getOptionSet(line, optionMap, userDefineErrorList, moduleConstraintList, isTarget, true);
    }

    /**
     * 선택한 BOM Line에 설정된 옵션 및 유효성검사를 수집한다.
     * 
     * @param line
     *            현재 선택한 BOM line
     * @param userDefineErrorList
     *            사용자 정의 메시지(에러)를 담을 객체
     * @param moduleConstraintList
     *            모듈 옵션 구속 조건을 담을 객체
     * @param isTarget
     *            현재 선택한 라인이 맞는지 여부
     * @param bChangeToOrgId
     *            true : 옵션이 정의된 아이템아이디를 입력(true) (Variant OPtion 정의시 사용)
     *            false : 옵션을 외부 옵션으로 사용중인 해당 아이템 아이디를 내부적으로 사용(Condition 설정 시 사용)
     * @return
     * @throws TCException
     */
    public ArrayList<VariantOption> getOptionSet(TCComponentBOMLine line, HashMap<String, VariantOption> optionMap, Vector<String[]> userDefineErrorList, Vector<String[]> moduleConstraintList, boolean isTarget, boolean bChangeToOrgId) throws TCException {

        // line.refresh();
        String type = line.getItem().getType();
        ConstraintsModel constraintsModel = null;
        if (type.equals(SDVTypeConstant.BOP_PROCESS_SHOP_ITEM)) {
            String lineMvl = line.getSession().getVariantService().askLineMvl(line);
            String mvlStr = line.getItemRevision().getProperty("mvl_text");
            if (mvlStr == null || mvlStr.equals("")) {
                return new ArrayList<VariantOption>();
            }
            constraintsModel = new ConstraintsModel(line.getItem().getProperty("item_id"), lineMvl, null, line, line.getSession().getVariantService());
            // 여기서 오래 걸림 parse()
            if (!constraintsModel.parse()) {
                throw new TCException("Condition을 파싱 할 수 없습니다.");
            }
        }

        //HashMap equalErrorMap = new HashMap();
        List<String> equalErrorList = null;
        ArrayList<String> notUseList = new ArrayList<String>();
        ArrayList<String> notDefineList = new ArrayList<String>();

        if (constraintsModel != null) {
            if (moduleConstraintList != null) {
                String[][] moduleConstraints = constraintsModel.moduleConstraintsTableData();
                //OptionConstraint[] constraints = constraintsModel.moduleConstraints();
                for (String[] moduleConstraint : moduleConstraints) {
                    String[] tmpStr = new String[moduleConstraint.length];
                    tmpStr[0] = moduleConstraint[0];
                    tmpStr[1] = moduleConstraint[2];
                    tmpStr[2] = moduleConstraint[4];
                    tmpStr[3] = moduleConstraint[5];
                    tmpStr[4] = moduleConstraint[6];
                    tmpStr[5] = moduleConstraint[7];
                    tmpStr[6] = moduleConstraint[8];
                    tmpStr[7] = moduleConstraint[9];
                    moduleConstraintList.add(tmpStr);
                }
            }

            // Error체크 Constraint를 분리함.(TC_MESSAGE_NOT_USE, TC_MESSAGE_NOT_DEFINE, 사용자 정의)
            int latestIdx = -1;
            boolean isUserDefine = false;
            String[][] errors = constraintsModel.errorChecksTableData();
            for (int i = 0; errors != null && i < errors.length; i++) {
                String[] errorInfo = errors[i];

                if (errorInfo[1] != null && !errorInfo[1].trim().equals("")) {
                    if (errorInfo[1].indexOf(VariantValue.TC_MESSAGE_NOT_DEFINE) > -1) {
                        equalErrorList = notDefineList;
                        isUserDefine = false;
                    } else if (errorInfo[1].indexOf(VariantValue.TC_MESSAGE_NOT_USE) > -1) {
                        equalErrorList = notUseList;
                        isUserDefine = false;
                    } else {
                        isUserDefine = true;
                    }
                }

                // VariantValue의 사용유무에 따라 표기되는 방식이 바뀌며, 이를 구분하기위해 아래 로직 추가.
                if (!isUserDefine && errorInfo[5].equals("=")) {
                    if (equalErrorList != null && !equalErrorList.contains(errorInfo[4] + "!" + errorInfo[6]))
                        equalErrorList.add(errorInfo[4] + "!" + errorInfo[6]);
                }

                if (userDefineErrorList != null) {
                    if (errorInfo[1] != null && !errorInfo[1].trim().equals("")) {
                        if (errorInfo[1].indexOf(VariantValue.TC_MESSAGE_NOT_DEFINE) < 0 && errorInfo[1].indexOf(VariantValue.TC_MESSAGE_NOT_USE) < 0) {
                            userDefineErrorList.add(errorInfo);
                            latestIdx = i;
                        }
                    } else {
                        if (i == latestIdx + 1) {
                            userDefineErrorList.add(errorInfo);
                            latestIdx = i;
                        }
                    }
                }
            }
        }

        ArrayList<VariantOption> list = new ArrayList<VariantOption>();
        ModularOption[] modularOptions = getModularOptions(line);
        if (modularOptions != null && modularOptions.length > 0) {
            HashMap<Integer, OVEOption> options = new HashMap<Integer, OVEOption>();
            for (ModularOption mOption : modularOptions) {

                OVEOption option = CustomMVPanel.getOveOption(line, options, mOption);
                String itemId = option.option.item;
                // Corporate Option Item Option에 존재 한다면, 아이템 명을 Corporate Option Item으로 변경함.
                if (bChangeToOrgId && !type.equals(TcDefinition.CORPORATE_ITEM_TYPE)) {
                    itemId = optionItemMap.get(option.option.name);
                }

                // optionItemMap에서 찾을 수 없으면, 현재 BOM line의 아이템 아이디를 적용.
                // 이런 경우는 실무에서 발생하면 안됨.
                if (itemId == null) {
                    itemId = line.getItem().getProperty("item_id");
                    //[SR150623-040][20150715] shcho, Cooporate Option에 없는 Option을 갖고 있더라도  Manage Option을 실행할 수 있도록 보완('도장'을 위해서)
                    //throw new TCException("Corporate Item Option 에서 " + option.option.name + "이(가) 적용된 아이템을 찾을 수 없습니다.");
                }
                option.option.item = itemId; // 000090
                VariantOption variantOption = new VariantOption(isTarget ? option : null, itemId, option.option.name, option.option.desc);
                list.add(variantOption);

                if (optionMap != null) {
                    optionMap.put(option.option.name, variantOption);
                }

                String[] values = option.comboValues;
                for (String value : values) {

                    int valueStatus = -1;
                    if (notUseList.contains(variantOption.getOptionName() + "!" + value)) {
                        valueStatus = VariantValue.VALUE_NOT_USE;
                    } else if (notDefineList.contains(variantOption.getOptionName() + "!" + value)) {
                        valueStatus = VariantValue.VALUE_NOT_DEFINE;
                    } else {
                        valueStatus = VariantValue.VALUE_USE;
                    }

                    VariantValue variantValue = null;
                    String key = option.option.name + ":" + value;

                    if (isTarget) {
                        if (valueMap.containsKey(option.option.name + ":" + value)) {
                            variantValue = valueMap.get(key);
                            variantValue.setValueStatus(valueStatus);
                            variantValue.setOption(variantOption);
                            variantValue.setNew(!isTarget);
                        } else {
                            variantValue = new VariantValue(variantOption, value, (String) descMap.get(value), valueStatus, !isTarget);
                            valueMap.put(key, variantValue);
                        }
                    } else {

                        if (!bChangeToOrgId) {
                            variantValue = valueMap.get(key);
                            if (variantValue == null) {
                                variantValue = new VariantValue(variantOption, value, (String) descMap.get(value), valueStatus, !isTarget);
                                valueMap.put(key, variantValue);
                            }

                        } else {
                            variantValue = new VariantValue(variantOption, value, (String) descMap.get(value), valueStatus, !isTarget);
                        }

                    }

                    variantOption.addValue(variantValue);
                }
            }
        }

        return list;
    }

    /**
     * 
     * @param line
     *            Corporate 아이템을 BOM라인 변환한것.
     * @param isTarget
     * @return
     * @throws TCException
     */
    private ArrayList<VariantOption> getCorporateOptionSet(TCComponentBOMLine line, boolean isTarget) throws TCException {

        ArrayList<VariantOption> list = new ArrayList<VariantOption>();

        TCComponentBOMWindow tccomponentbomwindow = line.window();
        VariantManagementService variantmanagementservice = VariantManagementService.getService(line.getSession());
        VariantManagement.BOMVariantConfigOptionResponse bomvariantconfigoptionresponse = variantmanagementservice.getBOMVariantConfigOptions(tccomponentbomwindow, line);
        VariantManagement.BOMVariantConfigOutput bomvariantconfigoutput = bomvariantconfigoptionresponse.output;
        //ArrayList arraylist = new ArrayList();
        for (int i = 0; i < bomvariantconfigoutput.configuredOptions.length; i++) {
            String itemId = line.getItem().getProperty("item_id");
            VariantManagement.BOMVariantConfigurationOption bomvariantconfigurationoption = bomvariantconfigoutput.configuredOptions[i];
            int optionId = bomvariantconfigurationoption.modularOption.optionId;
            VariantOption option = new VariantOption(null, itemId, bomvariantconfigurationoption.modularOption.optionName, bomvariantconfigurationoption.modularOption.optionDescription, null, optionId);

            list.add(option);
            // 생성된 옵션 정보를 외부에서 참조 할 수있도록 맵에 저장함.
            if (corpOptionMap != null)
                corpOptionMap.put(bomvariantconfigurationoption.modularOption.optionName, option);

            optionItemMap.put(bomvariantconfigurationoption.modularOption.optionName, itemId);
            for (String valueStr : bomvariantconfigurationoption.modularOption.allowedValues) {
                int valueStatus = VariantValue.VALUE_USE;
                VariantValue value = new VariantValue(option, valueStr, (String) descMap.get(valueStr), valueStatus, !isTarget);
                option.addValue(value);
            }
        }

        return list;
    }

    /**
     * DB에서 모든 Option Value 설명을 가져온다.
     * 
     * @return
     */
    @SuppressWarnings("unchecked")
    private HashMap<String,String> getDesc() {

        HashMap<String,String> valueDescMap = new HashMap<String,String>();

        SYMCRemoteUtil remote = new SYMCRemoteUtil();
        try {
            DataSet ds = new DataSet();
            ds.put("code_name", null);
            List<HashMap<String, String>> list = (List<HashMap<String, String>>)remote.execute("com.ssangyong.service.VariantService", "getVariantValueDesc", ds);
            if (list != null) {
                for (HashMap<String, String> map : list) {
                    valueDescMap.put(map.get("CODE_NAME"), map.get("CODE_DESC"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return valueDescMap;
    }

    private ArrayList<TCComponentBOMLine> getBOMLines(TCComponent[] corpItems) throws TCException {

        if (corpItems == null)
            return null;

        ArrayList<TCComponentBOMLine> bomlineAL = new ArrayList<TCComponentBOMLine>();
        TCSession session = corpItems[0].getSession();
        TCComponentBOMWindowType winType = (TCComponentBOMWindowType) session.getTypeComponent("BOMWindow");
        TCComponentRevisionRuleType tccomponentrevisionruletype = (TCComponentRevisionRuleType) session.getTypeComponent("RevisionRule");
        for (TCComponent com : corpItems) {
            TCComponentItem item = (TCComponentItem) com;
            TCComponentItemRevision revision = item.getLatestItemRevision();
            TCComponentBOMWindow window = winType.create(tccomponentrevisionruletype.getDefaultRule());
            bomlineAL.add(window.setWindowTopLine(null, revision, null, null));
        }

        return bomlineAL;
    }

    /**
     * 생성한 BomWindow를 모두 Close한다.
     * 
     * @param bCurrentWindowClose
     */
    public void clear(boolean bCurrentWindowClose) {
        if (corporateBOMLines == null)
            return;

        int i = corporateBOMLines.size() - 1;
        while (!corporateBOMLines.isEmpty()) {
            TCComponentBOMLine line = corporateBOMLines.get(i);
            try {

                if (line.equals(bomline) && !bCurrentWindowClose)
                    continue;

                if (line.window() != null) {
                    line.window().close();
                    System.out.println("window Close");
                }
            } catch (TCException tce) {
                tce.printStackTrace();
            }
            corporateBOMLines.remove(i--);
        }
    }

    /**
     * Corporate Option Item Type를 제외한 Variant, Function Type에 옵션을 셋팅하기위한
     * MVL String을 생성하는 메서드
     * 
     * @param option
     * @return
     */
    public static String getOptionString(VariantOption option) {
        String name = MVLLexer.mvlQuoteId(option.getOptionName(), false);
        String desc = option.getOptionDesc();
        if (desc.length() > 0)
            desc = (new StringBuilder()).append(" ").append(MVLLexer.mvlQuoteString(desc)).append(" ").toString();
        else
            desc = " ";
        String s = "public ";
        s += name;
        // implements ==> uses(외부) 변경함.
        s += " uses " + desc + MVLLexer.mvlQuoteId(option.getItemId(), true) + ":" + name;
        return s;
    }

    /**
     * Corporate Option Item에 옵션을 추가또는 변경하기위한 문자열을 생성
     * 
     * @param option
     * @return
     */
    public static String getCorpOptionString(VariantOption option) {
        String name = MVLLexer.mvlQuoteId(option.getOptionName(), false);
        String desc = option.getOptionDesc();
        if (desc.length() > 0)
            desc = (new StringBuilder()).append(" ").append(MVLLexer.mvlQuoteString(desc)).append(" ").toString();
        else
            desc = " ";

        String valueNameAll = "";
        List<VariantValue> values = option.getValues();
        for (int i = 0; i < values.size(); i++) {

            VariantValue value = values.get(i);
            String valueName = value.getValueName();
            valueNameAll += (i == 0 ? "" : ", ") + MVLLexer.mvlQuoteString(valueName);
        }
        String s = "public ";
        s += name;
        s += " string " + desc + " = " + valueNameAll;

        return s;
    }

    /**
     * 
     * @param key
     * @return
     */
    public VariantValue getValue(String key) {
        return valueMap.get(key);
    }

    /**
     * 사용정의 에러 메시지 리턴
     * 
     * @param line
     * @param bNeedToParse
     * @return
     * @throws TCException
     */
    public static Vector<String[]> getUserDefineErrors(TCComponentBOMLine line, boolean bNeedToParse) throws TCException {
        String lineMvl = SDVBOPUtilities.getTCSession().getVariantService().askLineMvl(line);
        ConstraintsModel constraintsModel = new ConstraintsModel(line.getItem().getProperty("item_id"), lineMvl, null, line, SDVBOPUtilities.getTCSession().getVariantService());

        if (bNeedToParse) {
            if (!constraintsModel.parse()) {
                throw new TCException("Condition을 파싱 할 수 없습니다.");
            }
        }

        int latestIdx = -1;
        Vector<String[]> userDefineErrorList = new Vector<String[]>();
        String[][] errors = constraintsModel.errorChecksTableData();
        for (int i = 0; errors != null && i < errors.length; i++) {
            String[] errorInfo = errors[i];
            if (errorInfo[1] != null && !errorInfo[1].trim().equals("")) {
                if (errorInfo[1].indexOf(VariantValue.TC_MESSAGE_NOT_DEFINE) < 0 && errorInfo[1].indexOf(VariantValue.TC_MESSAGE_NOT_USE) < 0) {
                    userDefineErrorList.add(errorInfo);
                    latestIdx = i;
                }
            } else {
                if (i == latestIdx + 1) {
                    userDefineErrorList.add(errorInfo);
                    latestIdx = i;
                }
            }
        }

        return userDefineErrorList;
    }

    /**
     * 옵션간 제약조건을 리턴.
     * 
     * @param line
     * @param bNeedToParse
     * @return
     * @throws TCException
     */
    public static Vector<String[]> getModuleConstraints(TCComponentBOMLine line, boolean bNeedToParse) throws TCException {
        String lineMvl = SDVBOPUtilities.getTCSession().getVariantService().askLineMvl(line);
        ConstraintsModel constraintsModel = new ConstraintsModel(line.getItem().getProperty("item_id"), lineMvl, null, line, SDVBOPUtilities.getTCSession().getVariantService());

        if (bNeedToParse) {
            if (!constraintsModel.parse()) {
                throw new TCException("Condition을 파싱 할 수 없습니다.");
            }
        }

        Vector<String[]> moduleConstraintList = new Vector<String[]>();
        String[][] moduleConstraints = constraintsModel.moduleConstraintsTableData();
        //OptionConstraint[] constraints = constraintsModel.moduleConstraints();
        for (String[] moduleConstraint : moduleConstraints) {
            String[] tmpStr = new String[moduleConstraint.length];
            tmpStr[0] = moduleConstraint[0];
            tmpStr[1] = moduleConstraint[2];
            tmpStr[2] = moduleConstraint[4];
            tmpStr[3] = moduleConstraint[5];
            tmpStr[4] = moduleConstraint[6];
            tmpStr[5] = moduleConstraint[7];
            tmpStr[6] = moduleConstraint[8];
            tmpStr[7] = moduleConstraint[9];
            moduleConstraintList.add(tmpStr);
        }

        return moduleConstraintList;
    }

    /**
     * BOM line에 설정된 ModularOption들을 리턴.
     * 
     * @param line
     * @return
     * @throws TCException
     */
    public ModularOption[] getModularOptions(TCComponentBOMLine line) throws TCException {

        com.teamcenter.services.internal.rac.structuremanagement._2011_06.VariantManagement.ModularOptionsInput amodularoptionsinput[] = new com.teamcenter.services.internal.rac.structuremanagement._2011_06.VariantManagement.ModularOptionsInput[1];
        VariantManagementService variantmanagementservice = VariantManagementService.getService(line.getSession());
        com.teamcenter.services.internal.rac.structuremanagement._2011_06.VariantManagement.ModularOptionsInput modularoptionsinput = new com.teamcenter.services.internal.rac.structuremanagement._2011_06.VariantManagement.ModularOptionsInput();
        modularoptionsinput.bomWindow = line.window();
        modularoptionsinput.bomLines = new TCComponentBOMLine[] { line };
        amodularoptionsinput[0] = modularoptionsinput;
        com.teamcenter.services.internal.rac.structuremanagement._2011_06.VariantManagement.ModularOptionsForBomResponse modularoptionsforbomresponse = variantmanagementservice.getModularOptionsForBom(amodularoptionsinput);
        ModularOptionsOutput[] optionsOutput = modularoptionsforbomresponse.optionsOutput;
        ModularOptionsInfo[] optionsInfo = optionsOutput[0].optionsInfo;
        ModularOptions mOptions = optionsInfo[0].options;
        ModularOption[] options = mOptions.options;

        return options;
    }

    public InterfaceAIFComponent[] getTargets() {
        InterfaceAIFComponent[] targets = AIFUtility.getCurrentApplication().getTargetComponents();
        return targets;
    }

    /**
     * 이미 만들어져 있는 Saved query를 이용하여 imancomponent를 검색하는 method이다.
     * 
     * @param savedQueryName
     *            String 저장된 query name
     * @param entryName
     *            String[] 검색 조건 name(오리지날 name)
     * @param entryValue
     *            String[] 검색 조건 value
     * @return TCComponent[] 검색 결과
     * @throws Exception
     * 
     */
    public TCComponent[] queryComponent(String savedQueryName, String[] entryName, String[] entryValue) throws Exception {
//        session.getPreferenceService().setString(1, "QRY_dataset_display_option", "2");
        session.getPreferenceService().setStringValue("QRY_dataset_display_option", "2");
        TCComponentQueryType queryType = (TCComponentQueryType) session.getTypeComponent("ImanQuery");
        TCComponentQuery query = (TCComponentQuery) queryType.find(savedQueryName);
        String[] queryEntries = session.getTextService().getTextValues(entryName);
        for (int i = 0; queryEntries != null && i < queryEntries.length; i++) {
            if (queryEntries[i] == null || queryEntries[i].equals("")) {
                queryEntries[i] = entryName[i];
            }
        }
        return query.execute(queryEntries, entryValue);
    }

}

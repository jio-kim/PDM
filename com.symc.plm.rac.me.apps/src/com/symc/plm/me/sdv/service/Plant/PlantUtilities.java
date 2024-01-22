package com.symc.plm.me.sdv.service.Plant;

import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;

import com.symc.plm.me.common.SDVBOPUtilities;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVTypeConstant;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.cme.kernel.bvr.TCComponentMfgBvrWorkarea;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;

public class PlantUtilities {

    /**
     * Plant의 ItemRevision을 가져오는 함수
     * 
     * @param interfaceAIFComponent
     * @param itemRevision
     * @return
     * @throws TCException
     */
    public static TCComponentItemRevision getItemRevision(InterfaceAIFComponent interfaceAIFComponent) throws TCException {
        TCComponentItemRevision itemRevision = null;
        if (interfaceAIFComponent instanceof TCComponentMfgBvrWorkarea) {
            TCComponentMfgBvrWorkarea MfgBvrWorkareaComponent = (TCComponentMfgBvrWorkarea) interfaceAIFComponent;
            itemRevision = MfgBvrWorkareaComponent.getItemRevision();
        }
        return itemRevision;
    }

    /**
     * Alternative BOMLine 생성 함수
     * 
     * @param altPrefix
     * @param targetBOMLine
     * @throws Exception
     */
    public static void createAlternativeBOMLine(TCComponentBOMLine targetBOMLine, TCComponentBOMLine parentBOMLine, String altPrefix) throws Exception {
        if (targetBOMLine != null && parentBOMLine != null) {
            /* Item ID 생성 */
            String originItemID = targetBOMLine.getItem().getProperty(SDVPropertyConstant.ITEM_ITEM_ID);
            if (targetBOMLine.getItemRevision().getLogicalProperty(SDVPropertyConstant.PLANT_REV_IS_ALTBOP)) {
                originItemID = originItemID.substring(originItemID.indexOf("-") + 1);
            }
            String newItemID = altPrefix + "-" + originItemID;

            /* ALT Plant가 자기자신을 복제 할 경우 오류 처리 */
            if (newItemID.equals(targetBOMLine.getItem().getProperty(SDVPropertyConstant.ITEM_ITEM_ID))) {
                throw new Exception(newItemID + " is current selected Alternative BOMLine.");
            }

            /* 적용 대상 altStationBOMLine 찾기 */
            TCComponentBOMLine altStationBOMLine = findBOMLine(parentBOMLine, newItemID);

            /* 적용 대상 altStationBOMLine이 없을 경우 생성 */
            if (altStationBOMLine == null) {
                TCComponentItem altStationItem = createPlantItem(newItemID, targetBOMLine.getItem().getType(), altPrefix);

                if (altStationItem != null) {
                    // BOMLine에 추가
                    altStationBOMLine = parentBOMLine.add(null, altStationItem.getLatestItemRevision(), null, false);
                    altStationBOMLine.setProperty("bl_plmxml_occ_xform", targetBOMLine.getProperty("bl_plmxml_occ_xform"));
                } else {
                    throw new Exception(newItemID + " does not exist.");
                }
            }

            /* 붙여넣을 대상 BOMLine 하위 BOMLine 제거 */
            deleteChildBOMLine(altStationBOMLine);

            /* targetBOMLine 하위의 BOMLine들을 altStationBOMLine 하위에 붙여넣기 */
            addBOMLine(targetBOMLine, altStationBOMLine, altPrefix);
        }
    }

    /**
     * Plant Item 생성 함수
     * 
     * @throws TCException
     */
    public static TCComponentItem createPlantItem(String itemId, String itemType, String altPrefixProperty) throws TCException {
        // Plant Item 생성
        TCComponentItem tcComponentItem = SDVBOPUtilities.createItem(itemType, itemId, "000", "", "");
        TCComponentItemRevision newItemRevision = tcComponentItem.getLatestItemRevision();

        // Plant ItemRevision 속성 입력
        newItemRevision.setProperty(SDVPropertyConstant.PLANT_REV_ALT_PREFIX, altPrefixProperty);
        newItemRevision.setLogicalProperty(SDVPropertyConstant.PLANT_REV_IS_ALTBOP, (altPrefixProperty.length() > 0) ? true : false);

        return tcComponentItem;
    }

    /**
     * 자원 할당 함수
     * 
     * @param targetBOMLine
     * @param parentBOMLine
     * @throws TCException
     * @throws Exception
     */
    public static TCComponent assignResource(TCComponentBOMLine targetBOMLine, TCComponentBOMLine parentBOMLine) throws TCException, Exception {
        ArrayList<InterfaceAIFComponent> resourceComponentList = new ArrayList<InterfaceAIFComponent>();
        resourceComponentList.add(targetBOMLine.getItemRevision());

        boolean isEquip = targetBOMLine.getItemRevision().isTypeOf("M7_EquipmentRevision");
        boolean isTool = targetBOMLine.getItemRevision().isTypeOf(SDVTypeConstant.BOP_PROCESS_TOOL_ITEM_REV);
        boolean isSubidiary = targetBOMLine.getItemRevision().isTypeOf(SDVTypeConstant.BOP_PROCESS_SUBSIDIARY_ITEM_REV);

        String occurrenceType = null;
        if (isEquip) {
            occurrenceType = SDVTypeConstant.BOP_PROCESS_OCCURRENCE_RESOURCE;
        } else if (isTool) {
            occurrenceType = SDVTypeConstant.BOP_PROCESS_OCCURRENCE_TOOL;
        } else if (isSubidiary) {
            occurrenceType = SDVTypeConstant.BOP_PROCESS_OCCURRENCE_SUBSIDIARY;
        }

        TCComponent[] tcComponents = SDVBOPUtilities.connectObject(parentBOMLine, resourceComponentList, occurrenceType);
        return tcComponents[0];
    }

    /**
     * Alternative에서 Production으로 BOMLine 적용 (Station Only)
     * 
     * @param parentBOMLine
     * @param targetBOMLine
     * @throws TCException
     * @throws Exception
     */
    public static void applyProductionBOMLine(TCComponentBOMLine targetBOMLine, TCComponentBOMLine parentBOMLine) throws TCException, Exception {

        /* Item ID 생성 */
        String alternativeItemID = targetBOMLine.getItem().getProperty(SDVPropertyConstant.ITEM_ITEM_ID);
        String productionItemID = alternativeItemID.substring(alternativeItemID.indexOf("-") + 1);

        /* 적용 대상 productionBOMLine 찾기 */
        TCComponentBOMLine productionBOMLine = findBOMLine(parentBOMLine, productionItemID);

        /* 적용 대상 productionBOMLine이 없을 경우 생성 */
        if (productionBOMLine == null) {
            TCComponentItem productionItem = SDVBOPUtilities.createItem(targetBOMLine.getItem().getType(), productionItemID, "000", "", "");
            if (productionItem != null) {
                productionBOMLine = parentBOMLine.add(null, productionItem.getLatestItemRevision(), null, false);
                productionBOMLine.setProperty("bl_plmxml_occ_xform", targetBOMLine.getProperty("bl_plmxml_occ_xform"));
            } else {
                throw new Exception(productionItemID + " does not exist.");
            }
        }

        /* productionBOMLine 하위 BOMLine 제거 */
        deleteChildBOMLine(productionBOMLine);

        /* Alternative 하위의 BOMLine들을 productionBOMLine 하위에 붙여넣기 */
        addBOMLine(targetBOMLine, productionBOMLine, "");
    }

    /**
     * ItemID로 BOMLine 찾기
     * 
     * @param parentBOMLine
     * @param targetItemID
     * @return
     * @throws TCException
     */
    public static TCComponentBOMLine findBOMLine(TCComponentBOMLine parentBOMLine, String targetItemID) throws TCException {
        /* Item ID로 Apply 대상 productionBOMLine 찾기 */
        TCComponentBOMLine targetBOMLine = null;
        AIFComponentContext[] arrAIFComponentContexts = parentBOMLine.getChildren();
        if (arrAIFComponentContexts != null) {
            for (AIFComponentContext aifComponentContext : arrAIFComponentContexts) {
                TCComponentBOMLine tcComponentBOMLine = (TCComponentBOMLine) aifComponentContext.getComponent();
                if (targetItemID.equals(tcComponentBOMLine.getItem().getProperty(SDVPropertyConstant.ITEM_ITEM_ID))) {
                    targetBOMLine = tcComponentBOMLine;
                    break;
                }
            }
        }
        return targetBOMLine;
    }

    /**
     * Alternative 하위의 BOMLine들을 targetBOMLine 하위에 붙여넣는 함수
     * 
     * @param alternativeBOMLine
     * @param productionBOMLine
     * @param altPrefix
     * @throws TCException
     * @throws Exception
     */
    public static void addBOMLine(TCComponentBOMLine alternativeBOMLine, TCComponentBOMLine productionBOMLine, String altPrefix) throws TCException, Exception {
        AIFComponentContext[] arrAifComponentContexts = alternativeBOMLine.getChildren();
        if (arrAifComponentContexts != null) {
            for (AIFComponentContext aifComponentContext : arrAifComponentContexts) {
                TCComponentBOMLine altChildBOMLine = (TCComponentBOMLine) aifComponentContext.getComponent();
                String altChildItemId = altChildBOMLine.getItem().getProperty(SDVPropertyConstant.ITEM_ITEM_ID);
                String altChildItemType = altChildBOMLine.getItem().getType();

                // Workarea 인 경우 productionBOMLine Add
                if (altChildItemType.equals(SDVTypeConstant.PLANT_OPAREA_ITEM)) {
                    TCComponentItem productionChildItem = null;
                    String productionChildItemId = null;

                    // Alternative에 add인 경우 : ALT Prefix를 제거한 ItemID 생성
                    if (altPrefix.length() > 0) {
                        if (StringUtils.contains(altChildItemId, "ALT")) {
                            productionChildItemId = altPrefix + "-" + altChildItemId.substring(altChildItemId.indexOf("-") + 1);
                        } else {
                            productionChildItemId = altPrefix + "-" + altChildItemId;
                        }
                    }
                    // Production에 add인 경우 : ALT Prefix를 제거한 ItemID 생성
                    else {
                        if (StringUtils.contains(altChildItemId, "ALT")) {
                            productionChildItemId = altChildItemId.substring(altChildItemId.indexOf("-") + 1); // 예) "ALT1-"
                        } else {
                            productionChildItemId = altChildItemId;
                        }

                    }

                    // Workarea Item이 존재하면 찾아서 사용, 없으면 생성
                    productionChildItem = SDVBOPUtilities.FindItem(productionChildItemId, altChildItemType);
                    if (productionChildItem == null) {
                        productionChildItem = createPlantItem(productionChildItemId, altChildItemType, altPrefix);
                    }
                 
                    // productionBOMLine Add
                    TCComponentBOMLine newBOMLine = null;
                    AIFComponentContext[] arrAIFComponentContexts = productionBOMLine.getChildren();
                    if (arrAIFComponentContexts != null) {
                        for (AIFComponentContext aifComponentContext2 : arrAIFComponentContexts) {
                            TCComponentBOMLine productionChildBOMLine = (TCComponentBOMLine) aifComponentContext2.getComponent();
                            if (productionChildItemId.equals(productionChildBOMLine.getItem().getProperty(SDVPropertyConstant.ITEM_ITEM_ID))) {
                                newBOMLine = productionChildBOMLine;
                                break;
                            }
                        }
                    }

                    if (newBOMLine == null) {
                        newBOMLine = productionBOMLine.add(null, productionChildItem.getLatestItemRevision(), null, false);
                    }

                    // BOMLine 속성 복사
                    if (newBOMLine != null) {
                        newBOMLine.setProperty(SDVPropertyConstant.BL_SEQUENCE_NO, altChildBOMLine.getProperty(SDVPropertyConstant.BL_SEQUENCE_NO));
                        newBOMLine.setProperty("bl_plmxml_occ_xform", altChildBOMLine.getProperty("bl_plmxml_occ_xform"));
                    }

                    // BOMLine 하위 재귀호출
                    addBOMLine(altChildBOMLine, newBOMLine, altPrefix);
                }

                // 자원인 경우 productionBOMLine에 할당
                else {
                    TCComponentBOMLine resourceBOMLine = (TCComponentBOMLine) assignResource(altChildBOMLine, productionBOMLine);
                    resourceBOMLine.setProperty(SDVPropertyConstant.BL_SEQUENCE_NO, altChildBOMLine.getProperty(SDVPropertyConstant.BL_SEQUENCE_NO));
                    resourceBOMLine.setProperty("bl_plmxml_occ_xform", altChildBOMLine.getProperty("bl_plmxml_occ_xform"));
                }
            }
        }
    }

    /**
     * 하위 BOMLine을 제거
     * 
     * @param productionBOMLine
     * @throws TCException
     */
    public static void deleteChildBOMLine(TCComponentBOMLine targetBOMLine) throws TCException {
        if (targetBOMLine != null) {
            AIFComponentContext[] arrAIFComponentContexts2 = targetBOMLine.getChildren();
            if (arrAIFComponentContexts2 != null) {
                ArrayList<TCComponentBOMLine> childBOMLineList = new ArrayList<TCComponentBOMLine>();
                for (AIFComponentContext aifComponentContext : arrAIFComponentContexts2) {
                    TCComponentBOMLine childBOMLine = (TCComponentBOMLine) aifComponentContext.getComponent();

                    if (childBOMLine != null) {
                        // 재귀 호출하여 Child의 하위 BOMLine을 찾아서 제거
                        deleteChildBOMLine(childBOMLine);
                        childBOMLineList.add(childBOMLine);
                    }
                }

                if (childBOMLineList.size() > 0) {
                    SDVBOPUtilities.disconnectObjects(targetBOMLine, childBOMLineList);
                }
            }
        }
    }
}

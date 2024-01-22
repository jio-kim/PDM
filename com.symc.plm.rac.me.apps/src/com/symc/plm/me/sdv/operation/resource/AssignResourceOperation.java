/**
 * 
 */
package com.symc.plm.me.sdv.operation.resource;

import java.util.ArrayList;
import java.util.Arrays;

import com.symc.plm.me.common.SDVBOPUtilities;
import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.sdv.operation.AbstractTCSDVOperation;
import com.symc.plm.me.sdv.service.resource.ResourceUtilities;
import com.symc.plm.me.sdv.viewpart.resource.ResourceSearchViewPart;
import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.cme.framework.treetable.CMEBOMTreeTable;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.psebase.common.AbstractViewableTreeTable;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;
import common.Logger;

public class AssignResourceOperation extends AbstractTCSDVOperation {

    private static final Logger logger = Logger.getLogger(AssignResourceOperation.class);
    private Registry registry = null;
    private ArrayList<InterfaceAIFComponent> resourceItemsList = null;
    private TCComponentBOMLine targetComponent;
    private InterfaceAIFComponent[] ainterfaceaifcomponents;

    /*
     * (non-Javadoc)
     * TODO: Validation 기능 을 구현
     * 
     * @see org.sdv.core.common.ISDVOperation#preExecuteSDVOperation()
     */
    @Override
    public void startOperation(String commandId) {

        registry = Registry.getRegistry(this);

        // ResourceViewPart
        ResourceSearchViewPart resourceSearchViewPart = ResourceUtilities.getResourceSearchViewPart();
        if (resourceSearchViewPart != null) {
            // ResourceViewPart의 선택된 자원
            InterfaceAIFComponent[] targetResourceItems = resourceSearchViewPart.getCurrentTable().getSelectedItems();
            resourceItemsList = new ArrayList<InterfaceAIFComponent>(Arrays.asList(targetResourceItems));
        }

        // MPPAppication BOP의 선택된 Target
//        ainterfaceaifcomponents = AifrcpPlugin.getSelectionMediatorService().getTargetComponents();
        ainterfaceaifcomponents = AIFUtility.getCurrentApplication().getTargetComponents();

        // PSEApplicationPanel psePanel = null;
        // application.getApplicationPanel();
        // if(application.getApplicationPanel() instanceof PSEApplicationPanel) {
        // psePanel = (PSEApplicationPanel)application.getApplicationPanel();
        // }
    }

    @Override
    public void executeOperation() throws Exception {
        try {
            // 자원 선택 여부, target BOP 선택 여부 체크 Validation
            if (resourceItemsList == null || resourceItemsList.size() <= 0) {
                throw new Exception(registry.getString("ResourceAssign.Resource.MESSAGE"));
            }
            if (ainterfaceaifcomponents == null || ainterfaceaifcomponents.length <= 0) {
                throw new Exception(registry.getString("ResourceAssign.BOMLine.MESSAGE"));
            }

            targetComponent = (TCComponentBOMLine) ainterfaceaifcomponents[0];
            if (targetComponent == null) {
                throw new Exception(registry.getString("ResourceAssign.BOMLine.MESSAGE"));
            }

            // resourceItem 별 occurrenceType 정보를 담는 ArrayList
            ArrayList<String> occurrenceTypeList = new ArrayList<String>();

            // 할당할 자원 Type
            for (int i = 0; i < resourceItemsList.size(); i++) {
                InterfaceAIFComponent component = resourceItemsList.get(i);
                TCComponentItemRevision itemRevision = (TCComponentItemRevision) component;

                boolean isEquip = itemRevision.isTypeOf("M7_EquipmentRevision");
                boolean isTool = itemRevision.isTypeOf(SDVTypeConstant.BOP_PROCESS_TOOL_ITEM_REV);
                boolean isSubidiary = itemRevision.isTypeOf(SDVTypeConstant.BOP_PROCESS_SUBSIDIARY_ITEM_REV);

                if (isEquip) {
                    occurrenceTypeList.add(SDVTypeConstant.BOP_PROCESS_OCCURRENCE_RESOURCE);
                } else if (isTool) {
                    occurrenceTypeList.add(SDVTypeConstant.BOP_PROCESS_OCCURRENCE_TOOL);
                } else if (isSubidiary) {
                    occurrenceTypeList.add(SDVTypeConstant.BOP_PROCESS_OCCURRENCE_SUBSIDIARY);
                }
            }

            // 1. 할당
            TCComponent[] tcComponentsList = SDVBOPUtilities.connectObjects(targetComponent, resourceItemsList, occurrenceTypeList);

            // 2. 부자재 Option condition 등록 (공법의 옵션을 부자재 옵션에 복사한다)
            for (TCComponent tcComponent : tcComponentsList) {
                if (tcComponent instanceof TCComponentBOMLine) {
                    TCComponentBOMLine tcComponentBOMLine = (TCComponentBOMLine) tcComponent;
                    // 수정 : bc.kim
                    // 이종화 차장 요청 공법 하위에 부자재 추가시 Assembly 공법일 경우에만 공법의 Option 입력함 
                    if (tcComponentBOMLine.getItem().getType().equals(SDVTypeConstant.BOP_PROCESS_SUBSIDIARY_ITEM) && targetComponent.getItem().getType().equals(SDVTypeConstant.BOP_PROCESS_ASSY_OPERATION_ITEM)) {
                        SDVBOPUtilities.updateAssiginOptionCondition(tcComponentBOMLine, null);
                    }
                }
            }

            // 3. 붙여넣은 BOMLine 펼치기
            AbstractViewableTreeTable viewTreeTable = SDVBOPUtilities.getMFGApplication().getAbstractViewableTreeTable();
            if (viewTreeTable == null) {
                try {
                    AbstractViewableTreeTable[] treeTables = SDVBOPUtilities.getMFGApplication().getViewableTreeTables();
                    if (treeTables != null && treeTables.length > 0) {
                        for (AbstractViewableTreeTable viewableTreeTable : treeTables) {
                            CMEBOMTreeTable cmeTree = (CMEBOMTreeTable) viewableTreeTable;
                            TCComponentBOMLine[] selectedBOMLines = cmeTree.getSelectedBOMLines();
                            for (TCComponentBOMLine tcComponentBOMLine : selectedBOMLines) {
                                if (tcComponentBOMLine.getObjectString().equals(targetComponent.getObjectString())) {
                                    viewTreeTable = viewableTreeTable;
                                    continue;
                                }
                            }
                        }
                    }
                } catch (Exception ex) {
                    logger.error(ex);
                    return;
                }
            }
            SDVBOPUtilities.executeExpandOneLevel(viewTreeTable);

        } catch (Exception e) {
            MessageBox.post(AIFDesktop.getActiveDesktop().getShell(), e.getMessage(), "ERROR", MessageBox.ERROR);
        }

    }

    @Override
    public void endOperation() {
    }
}

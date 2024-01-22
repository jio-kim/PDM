/**
 * 
 */
package com.symc.plm.me.sdv.operation.assembly.option;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;

import org.sdv.core.common.data.IDataSet;
import org.sdv.core.ui.UIManager;
import org.sdv.core.ui.operation.AbstractSDVActionOperation;

import com.symc.plm.me.common.SDVBOPUtilities;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.cme.application.MFGLegacyApplication;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCPreferenceService;
import com.teamcenter.rac.kernel.TCPreferenceService.TCPreferenceLocation;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.kernel.TCVariantService;
import com.teamcenter.rac.pse.variants.modularvariants.CustomMVPanel;
import com.teamcenter.rac.pse.variants.modularvariants.MVLLexer;
import com.teamcenter.rac.pse.variants.modularvariants.OVEOption;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;
import com.teamcenter.services.internal.rac.structuremanagement._2011_06.VariantManagement.ModularOption;

/**
 * Class Name : SyncOptionsetVsBOMOperation
 * Class Description : BOMLine에 정의된 옵션을 Update 하는 Operation
 * 
 * @date 2013. 10. 28.
 * 
 */
public class SyncOptionsetVsBOMOperation extends AbstractSDVActionOperation {

    private TCSession tcSession = null;
    private TCComponentBOMLine srcTopBomLine = null; // 복제되는 옵션의 Top BomLine
    private TCComponentBOMLine targetTopBomLine = null; // 대상 Top Bomline
    // 복제되는 원본 옵션 리스트
    private ModularOption[] srcOptions;
    // 옵션 Service
    private TCVariantService tcVarServ = null;

    private Registry registry = null;

    private IDataSet dataSet = null;
    private boolean isValidOK = true;

    /**
     * @param actionId
     * @param ownerId
     * @param dataSet
     */
    public SyncOptionsetVsBOMOperation(int actionId, String ownerId, IDataSet dataSet) {
        super(actionId, ownerId, dataSet);
        registry = Registry.getRegistry(this);
    }

    /*
     * (non-Javadoc)
     * TODO: Validation , 화면에서 구현되어야 할 코드. 이동해야 함
     * 
     * @see org.sdv.core.common.ISDVOperation#preExecuteSDVOperation()
     */
    @Override
    public void startOperation(String commandId) {

        try {
            dataSet = getDataSet();
            // updateOption
            TCComponentItemRevision srcItemRevision = (TCComponentItemRevision) dataSet.getValue("updateOption", "SRC_PRODUCT_REV");
            // 복제되는 Top BOM Window
            TCComponentBOMWindow srcTopBomWindow = SDVBOPUtilities.getBOMWindow(srcItemRevision, "Latest Working", "bom_view");
            // 복제되는 옵션을 가진 Top BOMLINE
            srcTopBomLine = srcTopBomWindow.getTopBOMLine();
            // 복제되는 원본 옵션 리스트
            srcOptions = SDVBOPUtilities.getModularOptions(srcTopBomLine);

            if (srcOptions == null || srcOptions.length == 0) {
                MessageBox.post(registry.getString("NotOptionDefine.MSG"), registry.getString("Warning.NAME"), MessageBox.WARNING);
                isValidOK = false;
                return;
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            setAbortRequested(true);
        }

        isValidOK = true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sdv.core.common.ISDVOperation#executeSDVOperation()
     */
    @Override
    public void executeOperation() throws Exception {

        if (!isValidOK)
            return;
        try {
            tcSession = (TCSession) getSession();
            tcVarServ = tcSession.getVariantService();

            // MPPAppication
            MFGLegacyApplication mfgApp = (MFGLegacyApplication) AIFUtility.getCurrentApplication();
            // 현재 BOM WINDOW
            TCComponentBOMWindow bomWindow = mfgApp.getBOMWindow();

            // 옵션이 저장된 Corporate Option Id를 Preference에서 가져옴
            TCPreferenceService preferenceService = tcSession.getPreferenceService();
//            String[] corpIds = preferenceService.getStringArray(TCPreferenceService.TC_preference_site, "PSM_global_option_item_ids");
            String[] corpIds = preferenceService.getStringValuesAtLocation("PSM_global_option_item_ids", TCPreferenceLocation.OVERLAY_LOCATION);

            // 대상 BOMLine
            targetTopBomLine = bomWindow.getTopBOMLine();

            // 대상 BOP의 옵션 리스트
            ModularOption[] targetOptions = SDVBOPUtilities.getModularOptions(targetTopBomLine);

            // 대상 BOP에 옵션이 없으면 신규 등록임
            if (targetOptions == null || targetOptions.length == 0) {
                tcSession.setStatus(registry.getString("creatingOption.MSG"));
                // 원본 옵션 리스트
                for (ModularOption srcOption : srcOptions) {
                    HashMap<Integer, OVEOption> options = new HashMap<Integer, OVEOption>();
                    OVEOption oveOption = CustomMVPanel.getOveOption(srcTopBomLine, options, srcOption);
                    // 저장될 옵션 값
                    String optionValue = SDVBOPUtilities.getOptionString(corpIds[0], oveOption.option.name, oveOption.option.desc);

                    // (옵션 추가) 옵션을 대상 BOMLINE에 추가함
                    tcVarServ.lineDefineOption(targetTopBomLine, optionValue);
                }

                // 대상 BOMLINE에 옵션 유효성검사 조건을 생성함
                SetLineMvl(false);

                // 이미 옵션이 있을 경우
            } else {

                // 원본 옵션 값 리스트(Key:옵션 Name, Value:옵션 값)
                Hashtable<String, String> srcOptionsHash = new Hashtable<String, String>();
                // 대상 BOMLINE 옵션 값 리스트(Key:옵션 Name, Value:옵션 값)
                Hashtable<String, String> targetOptionsHash = new Hashtable<String, String>();

                // 대상 BOMLINE 옵션 메세지 초기화
                SetLineMvl(true);

                // 원본 옵션 리스트에서 옵션값을 추출함
                for (ModularOption srcOption : srcOptions) {
                    HashMap<Integer, OVEOption> options = new HashMap<Integer, OVEOption>();
                    OVEOption oveOption = CustomMVPanel.getOveOption(srcTopBomLine, options, srcOption);
                    // 저장될 옵션 값
                    String optionValue = SDVBOPUtilities.getOptionString(corpIds[0], oveOption.option.name, oveOption.option.desc);

                    if (!srcOptionsHash.containsKey(oveOption.option.name))
                        srcOptionsHash.put(oveOption.option.name, optionValue);
                }

                // 삭제Sample(CustomMVPanel.deleteOption(tcvariantservice, targetTopBomLine, oveOption);)

                // 대상 옵션 리스트에서 옵션값을 추출함
                for (ModularOption targetOption : targetOptions) {

                    HashMap<Integer, OVEOption> options = new HashMap<Integer, OVEOption>();
                    OVEOption oveOption = CustomMVPanel.getOveOption(targetTopBomLine, options, targetOption);
                    // 저장될 옵션 값
                    String optionValue = SDVBOPUtilities.getOptionString(corpIds[0], oveOption.option.name, oveOption.option.desc);

                    if (!targetOptionsHash.containsKey(oveOption.option.name))
                        targetOptionsHash.put(oveOption.option.name, optionValue);

                }

                // (변경사항 반영로직)
                // 1. 옵션변경 Update
                // (삭제X)E-BOM에서는 옵션이 삭제가 될 경우가 없어서 삭제되는 것은 반영 할 필요가 없다.
                // (옵션값 변경X) Corporate Option이 변경되면 자동변경되어서 반영 할 필요가 없다.
                // (옵션 추가 O) 추가되는 옵션에 대하여 UPDATE 를 함
                // 2. 옵션 유효성검사 조건 Update
                // Full 업데이트
                boolean isExistAddOption = false;
                // 대상 BOMLINE에 존재하지 않는 옵션을 추가함
                for (Enumeration<String> enm = srcOptionsHash.keys(); enm.hasMoreElements();) {
                    String option = (String) enm.nextElement();
                    String srcOptionValue = srcOptionsHash.get(option);
                    if (targetOptionsHash.containsKey(option))
                        continue;
                    if (!isExistAddOption) {
                        tcSession.setStatus(registry.getString("addingOption.MSG"));
                        isExistAddOption = true;
                    }
                    // (옵션 추가) 옵션을 대상 BOMLINE에 추가함
                    tcVarServ.lineDefineOption(targetTopBomLine, srcOptionValue);
                }

                // 대상 BOMLINE에 옵션 유효성검사 조건을 생성함
                SetLineMvl(false);
            }
        } catch (Exception ex) {
            setAbortRequested(true);
            isValidOK = false;
            MessageBox.post(UIManager.getCurrentDialog().getShell(), ex.getMessage(), registry.getString("Inform.NAME"), MessageBox.INFORMATION);
            throw ex;

        }

    }

    /**
     * 
     * 대상 BOMLINE에 옵션 유효성검사 조건을 생성함
     * 
     * @method SetLineMvl
     * @date 2013. 10. 23.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void SetLineMvl(boolean isClear) throws Exception {
        try {
            if (isClear) {
                tcVarServ.setLineMvl(targetTopBomLine, "");
            } else {
                String srcOptItemId = MVLLexer.mvlQuoteId(srcTopBomLine.getProperty(SDVPropertyConstant.BL_ITEM_ID), true);
                String targetOptItemId = MVLLexer.mvlQuoteId(targetTopBomLine.getProperty(SDVPropertyConstant.BL_ITEM_ID), true);
                // 원본 옵션 유효성검사 조건을 가져옴
                String lineMvl = tcVarServ.askLineMvl(srcTopBomLine).replace(srcOptItemId, targetOptItemId);
                tcSession.setStatus(registry.getString("creatingOptValidation.MSG"));
                // 대상 BOMLINE에 옵션 유효성검사 조건을 생성함
                tcVarServ.setLineMvl(targetTopBomLine, lineMvl);
            }
        } catch (Exception ex) {
            throw ex;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sdv.core.common.ISDVOperation#endOperation()
     */
    @Override
    public void endOperation() {
        if (!isValidOK)
            return;
        MessageBox.post(AIFDesktop.getActiveDesktop().getShell(), registry.getString("Complete.MSG"), registry.getString("Inform.NAME"), MessageBox.INFORMATION);
    }

}

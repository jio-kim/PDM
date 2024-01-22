/**
 *
 */
package com.symc.plm.me.sdv.operation.common;

import java.util.HashMap;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Shell;
import org.sdv.core.common.data.IDataSet;
import org.sdv.core.ui.UIManager;
import org.sdv.core.ui.operation.AbstractSDVActionOperation;

import com.symc.plm.me.common.SDVBOPUtilities;
import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.cme.application.MFGLegacyApplication;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.kernel.TCPreferenceService;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.kernel.TCVariantService;
import com.teamcenter.rac.kernel.TCPreferenceService.TCPreferenceLocation;
import com.teamcenter.rac.pse.variants.modularvariants.CustomMVPanel;
import com.teamcenter.rac.pse.variants.modularvariants.OVEOption;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;
import com.teamcenter.services.internal.rac.structuremanagement._2011_06.VariantManagement.ModularOption;

/**
 * Class Name : ChangeSyncOptionsetOperation
 * Class Description : M Prdoduct에 정의된 옵션을 Update 하는 Operation
 *
 * @date 2014. 2. 12.
 *
 */
public class ChangeSyncOptionsetOperation extends AbstractSDVActionOperation {

    private TCSession tcSession = null;
    private TCComponentBOMLine targetTopBomLine = null; // 대상 Top Bomline
    // 복제되는 원본 옵션 리스트
    //private ModularOption[] srcOptions;
    // 옵션 Service
    private TCVariantService tcVarServ = null;

    private Registry registry = null;

    @SuppressWarnings("unused")
    private IDataSet dataSet = null;
    private boolean isValidOK = true;

    /**
     * @param actionId
     * @param ownerId
     * @param dataSet
     */
    public ChangeSyncOptionsetOperation(int actionId, String ownerId, IDataSet dataSet) {
        super(actionId, ownerId, dataSet);
        registry = Registry.getRegistry("com.symc.plm.me.sdv.operation.assembly.option.option");
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
            //targetTopBomLine = (TCComponentBOMLine) dataSet.getData("ChangeOptionsetView");

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

            // 원본 옵션 값 리스트(Key:옵션 Name, Value:옵션 값)
            //Hashtable<String, String> targetOptionsHash = new Hashtable<String, String>();

            String[] firstOption = targetOptions[0].mvlDefinitions;
            int optionStatus = 0;
            if (firstOption[0] != null) {
                optionStatus = firstOption[0].indexOf("uses");
            }
            //SetLineMvl(true);

            // 원본 옵션 리스트에서 옵션값을 추출함
            for (ModularOption targetOption : targetOptions) {
                HashMap<Integer, OVEOption> options = new HashMap<Integer, OVEOption>();
                OVEOption oveOption = CustomMVPanel.getOveOption(targetTopBomLine, options, targetOption);
                // 저장될 옵션 값
                String optionValue = SDVBOPUtilities.getOptionString(corpIds[0], oveOption.option.name, oveOption.option.desc);

                // 기존에 옵션이 등록되어 있는 값을 읽어와서 "uses" 또는 "implements" 바꾸어서 등록한다
                String changeOptionValue = "";

                if (optionStatus > 0) {
                    changeOptionValue = optionValue.replaceAll("uses", "implements");
                }else{
                    changeOptionValue = optionValue.replaceAll("implements", "uses");
                }

                CustomMVPanel.changeOption(tcVarServ, targetTopBomLine, targetOption.optionId, changeOptionValue);

                //CustomMVPanel.changeOption(tcVarServ, targetTopBomLine, oveOption, changeOptionValue);
            }
            targetTopBomLine.window().refresh();
            targetTopBomLine.window().fireChangeEvent();

        } catch (Exception ex) {
            setAbortRequested(true);
            isValidOK = false;
            Dialog dialog = (Dialog) UIManager.getAvailableDialog("symc.me.bop.SyncOptionsetVsMProductDialog");
            Shell shell = dialog.getShell();
            MessageBox.post(shell, ex.getMessage(), registry.getString("Inform.NAME"), MessageBox.INFORMATION);
            throw ex;

        }

    }

//    private void SetLineMvl(boolean isClear) throws Exception {
//        try {
//            tcVarServ.setLineMvl(targetTopBomLine, "");
//        } catch (Exception ex) {
//            throw ex;
//        }
//    }

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

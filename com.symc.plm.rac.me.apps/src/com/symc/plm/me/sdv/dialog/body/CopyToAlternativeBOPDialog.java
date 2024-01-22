/**
 *
 */
package com.symc.plm.me.sdv.dialog.body;

import org.eclipse.swt.widgets.Shell;
import org.sdv.core.beans.DialogStubBean;
import org.sdv.core.common.data.IDataSet;
import org.sdv.core.ui.dialog.SimpleSDVDialog;

import com.symc.plm.me.common.SDVBOPUtilities;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.utils.SYMTcUtil;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOPLine;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.util.ConfirmDialog;
import com.teamcenter.rac.util.Registry;

/**
 *
 */
public class CopyToAlternativeBOPDialog extends SimpleSDVDialog {
    Registry registry = Registry.getRegistry(CreateBodyOPDialog.class);

    /**
     * @param shell
     * @param dialogStub
     */
    public CopyToAlternativeBOPDialog(Shell shell, DialogStubBean dialogStub) {
        super(shell, dialogStub);
    }

    /**
     * @param shell
     * @param dialogStub
     * @param configId
     */
    public CopyToAlternativeBOPDialog(Shell shell, DialogStubBean dialogStub, int configId) {
        super(shell, dialogStub, configId);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.sdv.core.ui.dialog.AbstractSDVSWTDialog#validationCheck()
     */
    @Override
    protected boolean validationCheck() {
        try {
            IDataSet dataSet = getSelectDataSetAll();
            Object target_shop = dataSet.getValue(SDVPropertyConstant.WELDOP_REV_TARGET_OP);
            Object alt_prefix = dataSet.getValue(SDVPropertyConstant.OPERATION_REV_ALT_PREFIX);

            if (target_shop == null || (!(target_shop instanceof TCComponentBOPLine))) {
                showErrorMessage("[" + registry.getString("CopyToAltTargetRequired.MESSAGE", "Target BOP") + "]" + registry.getString("RequiredField.MESSAGE", "is a required field."), null);
                return false;
            }

            if (alt_prefix == null || alt_prefix.toString().trim().length() == 0) {
                showErrorMessage("[" + SYMTcUtil.getPropertyDisplayName(SDVTypeConstant.BOP_PROCESS_OPERATION_ITEM_REV, SDVPropertyConstant.OPERATION_REV_ALT_PREFIX) + "]" + registry.getString("RequiredField.MESSAGE", "is a required field."), null);
                return false;
            }

            TCComponentItemRevision shop_rev;
            if (((TCComponentBOPLine) target_shop).getItem().getType().equals(SDVTypeConstant.BOP_PROCESS_SHOP_ITEM))
                shop_rev = ((TCComponentBOPLine) target_shop).getItemRevision();
            else
                shop_rev = ((TCComponentBOPLine) target_shop).parent().getItemRevision();

            boolean is_altbop = shop_rev.getLogicalProperty(SDVPropertyConstant.SHOP_REV_IS_ALTBOP);
            String target_alt_prefix = shop_rev.getProperty(SDVPropertyConstant.SHOP_REV_ALT_PREFIX);
            String shop_code = shop_rev.getItem().getProperty(SDVPropertyConstant.ITEM_ITEM_ID);
            String alt_shop_code = (is_altbop ? target_alt_prefix : alt_prefix) + "-" + shop_code;

            // if (is_altbop && target_alt_prefix.equals(alt_prefix))
            // {
            // showErrorMessage("Can not continue. Alternative Prefix is same.", null);
            // return false;
            // }

            TCComponentItem alt_shop_item = SDVBOPUtilities.FindItem(alt_shop_code, SDVTypeConstant.BOP_PROCESS_SHOP_ITEM);
            if ((((TCComponentBOPLine) target_shop).getItem().getType().equals(SDVTypeConstant.BOP_PROCESS_LINE_ITEM)) && alt_shop_item == null) {
                int confirmRet = ConfirmDialog.prompt(getShell(), registry.getString("Confirmation.TITLE", "Confirm"), registry.getString("AltBOPShopDoesNotExist.MESSAGE", "Continue with create Alternative BOP Shop?"));
                if (confirmRet != 2)
                    return false;
            } else if ((((TCComponentBOPLine) target_shop).getItem().getType().equals(SDVTypeConstant.BOP_PROCESS_SHOP_ITEM)) && alt_shop_item != null) {
                int confirmRet = ConfirmDialog.prompt(getShell(), registry.getString("Confirmation.TITLE", "Confirm"), registry.getString("AltBOPShopIsExist.MESSAGE", "Alternative BOP Shop is exist.\n\nContinue with update Alternative BOP?"));
                if (confirmRet != 2)
                    return false;
            }

            // PERT 를 구성하는 라인의 존재 유무 체크
            String pre_line = checkPreDecessorsLine((TCComponentBOPLine) target_shop, alt_prefix.toString());
            if (!pre_line.equals("")) {
                showErrorMessage(registry.getString("." + "AltBOPLineDoesNotExist.MESSAGE", "Sub Line Decessors does not exist in the Alternative BOP.\n\n" + "[" + pre_line + "]" + "\n\nPlease create Alternative BOP Line."), null);
                return false;
            }

            String ext_Station = checkExtDecessorsLine((TCComponentBOPLine) target_shop, alt_prefix.toString());
            if (!ext_Station.equals("")) {
                showErrorMessage(registry.getString("." + "AltBOPLineDoesNotExist.MESSAGE", "Sub Line Decessors does not exist in the Alternative BOP.\n\n" + "[" + ext_Station + "]" + "\n\nPlease create Alternative BOP Line."), null);
                return false;
            }

        } catch (Exception ex) {
            showErrorMessage(ex.getMessage(), ex);
            return false;
        }

        return true;
    }

    protected String checkExtDecessorsLine(TCComponentBOPLine bomLines, String alt_prefix) throws Exception {
        String extDecessors = "";
        for (AIFComponentContext bomLine : bomLines.getChildren()) {
            TCComponentBOMLine stationBomLine = (TCComponentBOMLine) bomLine.getComponent();
            TCComponent[] decessorsStations = stationBomLine.getItemRevision().getReferenceListProperty(SDVPropertyConstant.ME_EXT_DECESSORS);
            if (decessorsStations != null && decessorsStations.length > 0) {
                for (TCComponent decessorsStation : decessorsStations) {
                    TCComponentItem decessorsItem = SDVBOPUtilities.FindItem(alt_prefix + "-" + decessorsStation.getStringProperty(SDVPropertyConstant.ITEM_ITEM_ID), SDVTypeConstant.BOP_PROCESS_STATION_ITEM);
                    if (decessorsItem == null) {
                        extDecessors += alt_prefix + "-" + decessorsStation.getStringProperty(SDVPropertyConstant.ITEM_ITEM_ID) + ", ";
                    }
                }
            }
        }
        return extDecessors;
    }

    private String checkPreDecessorsLine(TCComponentBOPLine bomLines, String alt_prefix) throws Exception {
        String PreDecessors = "";
        TCComponent[] decessorsLines = bomLines.getReferenceListProperty(SDVPropertyConstant.ME_PREDECESSORS);
        if (decessorsLines != null && decessorsLines.length > 0) {
            for (TCComponent decessorsLine : decessorsLines) {
                TCComponentItem decessorsItem = SDVBOPUtilities.FindItem(alt_prefix + "-" + decessorsLine.getStringProperty(SDVPropertyConstant.BL_ITEM_ID), SDVTypeConstant.BOP_PROCESS_LINE_ITEM);
                if (decessorsItem == null) {
                    PreDecessors += alt_prefix + "-" + decessorsLine.getStringProperty(SDVPropertyConstant.BL_ITEM_ID) + ", ";
                }
            }
        }
        return PreDecessors;
    }

}

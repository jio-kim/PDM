/**
 * 
 */
package com.symc.plm.me.sdv.dialog.swm;

import org.eclipse.swt.widgets.Shell;
import org.sdv.core.beans.DialogStubBean;
import org.sdv.core.common.IDialog;
import org.sdv.core.common.data.IDataMap;
import org.sdv.core.common.data.IDataSet;
import org.sdv.core.ui.UIManager;
import org.sdv.core.ui.dialog.SimpleSDVDialog;

import com.symc.plm.me.common.SDVPropertyConstant;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;

/**
 * Class Name : RegisterSWMDocDialog
 * Class Description :
 * 
 * @date 2014. 1. 28.
 * 
 */
public class RegisterSWMDocDialog extends SimpleSDVDialog {
    Registry registry = Registry.getRegistry(RegisterSWMDocDialog.class);

    public RegisterSWMDocDialog(Shell shell, DialogStubBean dialogStub) {
        super(shell, dialogStub);
    }

    @Override
    protected boolean validationCheck() {
        IDataSet dataset = getDataSetAll();
        IDataMap dataMap = dataset.getDataMap("registerSWMDocView");

        String shopCode = dataMap.getStringValue(SDVPropertyConstant.SWM_SHOP_CODE);
        String category = dataMap.getStringValue(SDVPropertyConstant.SWM_CATEGORY);
        String vehicleNo = dataMap.getStringValue(SDVPropertyConstant.SWM_VEHICLE_CODE);
        String referenceItemId = dataMap.getStringValue(SDVPropertyConstant.ITEM_M7_REFERENCE_INFO);
        String referenceObjectName = dataMap.getStringValue(SDVPropertyConstant.ITEM_OBJECT_NAME);
        String group = dataMap.getStringValue(SDVPropertyConstant.SWM_GROUP);

        // 생성 조건에 샵이 필요 합니다
        if (shopCode == null || shopCode.trim().length() == 0) {
            IDialog dialog = UIManager.getCurrentDialog();
            if (dialog.getShell() == null) {
                dialog = UIManager.getAvailableDialog("symc.dialog.registerSWMDocDialog");
            }
            MessageBox.post(dialog.getShell(), registry.getString("CreateConditionIsRequiredShopCodeField.MESSAGE"), "Warning", MessageBox.WARNING);
            return false;
        }

        // 생성 조건에 구분이 필요 합니다
        if (category == null || category.trim().length() == 0) {
            IDialog dialog = UIManager.getCurrentDialog();
            if (dialog.getShell() == null) {
                dialog = UIManager.getAvailableDialog("symc.dialog.registerSWMDocDialog");
            }
            MessageBox.post(dialog.getShell(), registry.getString("CreateConditionIsRequiredCategoryField.MESSAGE"), "Warning", MessageBox.WARNING);
            return false;
        }

        // 생성 조건에 차종이 필요 합니다
        if (vehicleNo == null || vehicleNo.trim().length() == 0) {
            IDialog dialog = UIManager.getCurrentDialog();
            if (dialog.getShell() == null) {
                dialog = UIManager.getAvailableDialog("symc.dialog.registerSWMDocDialog");
            }
            MessageBox.post(dialog.getShell(), registry.getString("CreateConditionIsRequiredVehicleCodeField.MESSAGE"), "Warning", MessageBox.WARNING);
            return false;
        }

        // 생성 조건에 관련근거가 필요 합니다
        if (referenceItemId == null || referenceItemId.trim().length() == 0) {
            IDialog dialog = UIManager.getCurrentDialog();
            if (dialog.getShell() == null) {
                dialog = UIManager.getAvailableDialog("symc.dialog.registerSWMDocDialog");
            }
            MessageBox.post(dialog.getShell(), registry.getString("CreateConditionIsRequiredReferenceField.MESSAGE"), "Warning", MessageBox.WARNING);
            return false;
        }

        // 생성 조건에 작업명이 필요 합니다
        if (referenceObjectName == null || referenceObjectName.trim().length() == 0) {
            IDialog dialog = UIManager.getCurrentDialog();
            if (dialog.getShell() == null) {
                dialog = UIManager.getAvailableDialog("symc.dialog.registerSWMDocDialog");
            }
            MessageBox.post(dialog.getShell(), registry.getString("CreateConditionIsRequiredWorkNameField.MESSAGE"), "Warning", MessageBox.WARNING);
            return false;
        }

        // 생성 조건에 직이 필요 합니다
        if (group == null || group.trim().length() == 0) {
            IDialog dialog = UIManager.getCurrentDialog();
            if (dialog.getShell() == null) {
                dialog = UIManager.getAvailableDialog("symc.dialog.registerSWMDocDialog");
            }
            MessageBox.post(dialog.getShell(), registry.getString("CreateConditionIsRequiredGroupField.MESSAGE"), "Warning", MessageBox.WARNING);
            return false;
        }

        return true;
    }

}

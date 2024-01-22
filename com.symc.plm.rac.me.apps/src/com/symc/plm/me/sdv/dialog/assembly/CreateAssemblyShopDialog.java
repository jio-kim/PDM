/**
 * 
 */
package com.symc.plm.me.sdv.dialog.assembly;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Shell;
import org.sdv.core.beans.DialogStubBean;
import org.sdv.core.common.data.IDataSet;
import org.sdv.core.ui.dialog.SimpleSDVDialog;

import com.symc.plm.me.common.SDVBOPUtilities;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVTypeConstant;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;

/**
 * 조립 Shop 생성 Dialog
 * Class Name : CreateAssemblyShopDialog
 * Class Description :
 * [SR140723-010][20140717] shcho, m7_JPH 속성의 타입을 정수에서 부동 소수점으로 변경. 소수점포함5자리까지 입력가능.
 * 
 * @date 2013. 11. 6.
 * 
 */
public class CreateAssemblyShopDialog extends SimpleSDVDialog {
    // ActionButton
    protected Button editButton;
    // CommandButton
    protected Button searchButton;

    private Registry registry = null;
    private boolean isValidateOK = false; // Validation 성공유무

    /**
     * @param shell
     * @param dialogStub
     */
    public CreateAssemblyShopDialog(Shell shell, DialogStubBean dialogStub) {
        super(shell, dialogStub);
        registry = Registry.getRegistry("com.symc.plm.me.sdv.view.assembly.assembly");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sdv.core.ui.dialog.AbstractSDVUIREnderDialog#validationCheck()
     */
    @Override
    protected boolean validationCheck() {
        if (isValidateOK)
            return true;

        // String mecoNo = "";
        TCComponentItemRevision mecoRevision = null;
        StringBuffer errorMsg = new StringBuffer();

        IDataSet dataSetAll = this.getSelectDataSetAll();
        try {

            String lovShop = dataSetAll.getStringValue("shopInform", SDVPropertyConstant.SHOP_REV_SHOP_CODE);
            String txtProduct = dataSetAll.getStringValue("shopInform", SDVPropertyConstant.SHOP_REV_PRODUCT_CODE);
            String txtShopKorName = dataSetAll.getStringValue("shopInform", SDVPropertyConstant.ITEM_OBJECT_NAME);
            String txtShopEngName = dataSetAll.getStringValue("shopInform", SDVPropertyConstant.SHOP_ENG_NAME);
            String txtJph = dataSetAll.getStringValue("shopInform", SDVPropertyConstant.SHOP_REV_JPH);
            String txtAllowance = dataSetAll.getStringValue("shopInform", SDVPropertyConstant.SHOP_REV_ALLOWANCE);
            // String txtVehicleKorName = dataSetAll.getStringValue("shopInform", SDVPropertyConstant.SHOP_VEHICLE_KOR_NAME);
            // String txtVehicleEngName = dataSetAll.getStringValue("shopInform", SDVPropertyConstant.SHOP_VEHICLE_ENG_NAME);
            // String vehicleCode = dataSetAll.getStringValue("shopInform", SDVPropertyConstant.SHOP_REV_VEHICLE_CODE);

            // mecoNo = dataSetAll.getStringValue("mecoSelect", "mecoNo");
            Object mecoObj = dataSetAll.getValue("mecoSelect", "mecoRev");
            if (mecoObj != null)
                mecoRevision = (TCComponentItemRevision) mecoObj;

            /**
             * 필수 항목 체크
             */
            // MECO No체크
            // if (mecoNo == null || mecoNo.isEmpty())
            if (mecoRevision == null)
                errorMsg.append(registry.getString("RequiredCheck.MSG").replace("%0", "MECO NO").concat("\n"));

            if (lovShop.isEmpty())
                errorMsg.append(registry.getString("RequiredCheck.MSG").replace("%0", registry.getString("ShopCode.NAME")).concat("\n"));

            if (txtProduct.isEmpty()) {
                errorMsg.append(registry.getString("RequiredCheck.MSG").replace("%0", registry.getString("ProductCode.NAME")).concat("\n"));
            } else {
                TCComponentItem ebomProductItem = null;
                Object ebomProductObj = dataSetAll.getValue("shopInform", "S7_Product");
                if (ebomProductObj != null)
                    ebomProductItem = (TCComponentItem) ebomProductObj;

                if (ebomProductItem == null) {
                    errorMsg.append(registry.getString("WrongInputCheck.MSG").replace("%0", registry.getString("ProductCode.NAME")).concat("\n"));
                }
            }

            if (txtShopKorName.isEmpty())
                errorMsg.append(registry.getString("RequiredCheck.MSG").replace("%0", registry.getString("ShopKorName.NAME")).concat("\n"));

            if (txtShopEngName.isEmpty())
                errorMsg.append(registry.getString("RequiredCheck.MSG").replace("%0", registry.getString("ShopEngName.NAME")).concat("\n"));

            // [SR140723-010][20140717] shcho, m7_JPH 속성의 타입을 정수에서 부동 소수점으로 변경. 소수점포함5자리까지 입력가능.
            if (txtJph.isEmpty()) {
                errorMsg.append(registry.getString("RequiredCheck.MSG").replace("%0", registry.getString("JPH.NAME")).concat("\n"));
            } else {
                // try {
                // Integer.parseInt(txtJph);
                // } catch (NumberFormatException ex) {
                // errorMsg.append(registry.getString("ValidInputTypeCheck.MSG").replace("%0", registry.getString("JPH.NAME")).replace("%1", registry.getString("Int.MSG")).concat("\n"));
                // }
                try {
                    Double.parseDouble(txtJph);
                } catch (NumberFormatException ex) {
                    errorMsg.append(registry.getString("ValidInputTypeCheck.MSG").replace("%0", registry.getString("JPH.NAME")).replace("%1", registry.getString("Float.MSG")).concat("\n"));
                }
            }

            if (txtAllowance.isEmpty()) {
                errorMsg.append(registry.getString("RequiredCheck.MSG").replace("%0", registry.getString("Allowance.NAME")).concat("\n"));
            } else {
                try {
                    Double.parseDouble(txtAllowance);
                } catch (NumberFormatException ex) {
                    errorMsg.append(registry.getString("ValidInputTypeCheck.MSG").replace("%0", registry.getString("Allowance.NAME")).replace("%1", registry.getString("Float.MSG")).concat("\n"));
                }
            }

            if (errorMsg.length() > 0) {
                MessageBox.post(getShell(), errorMsg.toString(), registry.getString("Warning.NAME"), MessageBox.WARNING);
                return false;
            }

            String itemId = SDVPropertyConstant.ITEM_ID_PREFIX + "-" + lovShop + "-" + txtProduct;
            TCComponentItem item = SDVBOPUtilities.FindItem(itemId, SDVTypeConstant.BOP_PROCESS_SHOP_ITEM);
            if (item != null) {
                MessageBox.post(getShell(), registry.getString("ValidIdCheck.MSG").replace("%0", itemId), registry.getString("Warning.NAME"), MessageBox.WARNING);
                return false;
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        isValidateOK = true;
        return true;
    }

}

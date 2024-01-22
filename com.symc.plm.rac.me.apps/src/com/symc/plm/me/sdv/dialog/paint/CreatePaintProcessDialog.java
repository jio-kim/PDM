/**
 * 
 */
package com.symc.plm.me.sdv.dialog.paint;

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
 * Class Name : CreatePaintProcessDialog
 * Class Description :
 * 
 * @date 2013. 12. 3.
 * 
 */
public class CreatePaintProcessDialog extends SimpleSDVDialog {
    // PLANT PRE FIX
    private static String PLANT_PREFIX = "PTP-";
    private Registry registry = null;
    // Validation 성공유무
    //private boolean isValidateOK = false;

    public CreatePaintProcessDialog(Shell shell, DialogStubBean dialogStub) {
        super(shell, dialogStub);
        registry = Registry.getRegistry("com.symc.plm.me.sdv.view.paint.paint");
    }

    @Override
    protected boolean validationCheck() {

//        if (isValidateOK)
//            return true;

        //String mecoNo = "";
        TCComponentItemRevision mecoRevision = null;
        StringBuffer errorMsg = new StringBuffer();

        IDataSet dataSetAll = this.getSelectDataSetAll();

        /**
         * Station 정보
         */
        //mecoNo = dataSetAll.getStringValue("mecoSelect", "mecoNo");
        Object mecoObj = dataSetAll.getValue("mecoSelect", "mecoRev");
        if (mecoObj != null)
            mecoRevision = (TCComponentItemRevision) mecoObj;
                
        String txtShopCode = dataSetAll.getStringValue("processInform", SDVPropertyConstant.STATION_SHOP);
        String txtLineCode = dataSetAll.getStringValue("processInform", SDVPropertyConstant.STATION_LINE);
        String lovStationCode = dataSetAll.getStringValue("processInform", SDVPropertyConstant.STATION_STATION_CODE);
        String txtProductCode = dataSetAll.getStringValue("processInform", SDVPropertyConstant.STATION_PRODUCT_CODE);
        String lovParallelStationNo = dataSetAll.getStringValue("processInform", SDVPropertyConstant.STATION_PARALLEL_STATION_NO);
        String txtSationKorName = dataSetAll.getStringValue("processInform", SDVPropertyConstant.ITEM_OBJECT_NAME);
        String txtStationEngName = dataSetAll.getStringValue("processInform", SDVPropertyConstant.STATION_ENG_NAME);
        String vehicleCode = dataSetAll.getStringValue("processInform", SDVPropertyConstant.STATION_VEHICLE_CODE);


        // MECO No체크
        //if (mecoNo.isEmpty())
        if (mecoRevision == null)
            errorMsg.append(registry.getString("RequiredCheck.MSG").replace("%0", "MECO NO").concat("\n"));

        if (txtShopCode.isEmpty())
            errorMsg.append(registry.getString("RequiredCheck.MSG").replace("%0", registry.getString("ShopCode.NAME")).concat("\n"));

        if (txtLineCode.isEmpty())
            errorMsg.append(registry.getString("RequiredCheck.MSG").replace("%0", registry.getString("LineCode.NAME")).concat("\n"));
      
        if (lovStationCode.isEmpty())
            errorMsg.append(registry.getString("RequiredCheck.MSG").replace("%0", registry.getString("StationCode.NAME")).concat("\n"));

        if (txtProductCode.isEmpty())
            errorMsg.append(registry.getString("RequiredCheck.MSG").replace("%0", registry.getString("ProductCode.NAME")).concat("\n"));

        if (txtSationKorName.isEmpty())
            errorMsg.append(registry.getString("RequiredCheck.MSG").replace("%0", registry.getString("StationKorName.NAME")).concat("\n"));

        if (lovParallelStationNo.isEmpty())
            errorMsg.append(registry.getString("RequiredCheck.MSG").replace("%0", registry.getString("ParallelStationNo.NAME")).concat("\n"));        
        
        if (txtStationEngName.isEmpty())
            errorMsg.append(registry.getString("RequiredCheck.MSG").replace("%0", registry.getString("StationEngName.NAME")).concat("\n"));

        if (vehicleCode.isEmpty())
            errorMsg.append(registry.getString("RequiredCheck.MSG").replace("%0", registry.getString("VehicleCode.NAME")).concat("\n"));

        
        if (errorMsg.length() > 0) {
            MessageBox.post(getShell(), errorMsg.toString(), registry.getString("Warning.NAME"), MessageBox.WARNING);
            return false;
        }

        String itemId = PLANT_PREFIX + txtShopCode + "-" + txtLineCode + "-" + lovStationCode + "-" + txtProductCode + "-" + lovParallelStationNo;

        try {
            TCComponentItem item = SDVBOPUtilities.FindItem(itemId, SDVTypeConstant.BOP_PROCESS_STATION_ITEM);
            if (item != null) {
                MessageBox.post(getShell(), registry.getString("ValidIdCheck.MSG").replace("%0", itemId), registry.getString("Warning.NAME"), MessageBox.WARNING);
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
//        isValidateOK = true;

        return true;
    }

}

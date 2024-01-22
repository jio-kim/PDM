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
 * Class Name : CreatePaintOPDialog
 * Class Description :
 * 
 * @date 2013. 11. 15.
 * 
 */
public class CreatePaintOPDialog extends SimpleSDVDialog {

    private boolean isValidateOK = false; // Validation 성공유무
    private Registry registry = null;

    /**
     * @param shell
     * @param dialogStub
     */
    public CreatePaintOPDialog(Shell shell, DialogStubBean dialogStub) {
        super(shell, dialogStub);
        registry = Registry.getRegistry("com.symc.plm.me.sdv.view.paint.paint");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sdv.core.ui.dialog.AbstractSDVSWTDialog#validationCheck()
     */
    @Override
    protected boolean validationCheck() {
        if (isValidateOK)
            return true;

        //String mecoNo = "";
        TCComponentItemRevision mecoRevision = null;
        StringBuffer errorMsg = new StringBuffer();
        IDataSet dataSetAll = this.getSelectDataSetAll();

        /**
         * 공법 정보
         */
        String vechicleCode = dataSetAll.getStringValue("opInform", SDVPropertyConstant.OPERATION_REV_VEHICLE_CODE);
        String shopCode = dataSetAll.getStringValue("opInform", SDVPropertyConstant.OPERATION_REV_SHOP);
        String opCode = dataSetAll.getStringValue("opInform", SDVPropertyConstant.OPERATION_REV_OPERATION_CODE);

        String bopVersion = dataSetAll.getStringValue("opInform", SDVPropertyConstant.OPERATION_REV_BOP_VERSION);
        String opKorName = dataSetAll.getStringValue("opInform", SDVPropertyConstant.OPERATION_REV_KOR_NAME);
        String opEngName = dataSetAll.getStringValue("opInform", SDVPropertyConstant.OPERATION_REV_ENG_NAME);

        String line = dataSetAll.getStringValue("opInform", SDVPropertyConstant.STATION_LINE);
        String stationCode = dataSetAll.getStringValue("opInform", SDVPropertyConstant.STATION_STATION_CODE);

        //mecoNo = dataSetAll.getStringValue("mecoSelect", "mecoNo");
        Object mecoObj = dataSetAll.getValue("mecoSelect", "mecoRev");
        if (mecoObj != null)
            mecoRevision = (TCComponentItemRevision) mecoObj;

        // MECO No체크
        //if (mecoNo == null || mecoNo.isEmpty())
        if (mecoRevision == null)
            errorMsg.append(registry.getString("RequiredCheck.MSG").replace("%0", "MECO NO").concat("\n"));
        /**
         * 필수 항목 체크
         */
        if (vechicleCode.isEmpty())
            errorMsg.append(registry.getString("RequiredCheck.MSG").replace("%0", registry.getString("VehicleCode.NAME")).concat("\n"));

        if (opCode.isEmpty()) {
            errorMsg.append(registry.getString("RequiredCheck.MSG").replace("%0", registry.getString("OperationCode.NAME")).concat("\n"));
        } else {
            if (opCode.length() != 4)
                errorMsg.append(registry.getString("LimitCheck.MSG").replace("%0", registry.getString("OperationCode.NAME")).replace("%1", "4").concat("\n"));
            else {
                String mainCode = opCode.substring(0, 2);
                String subCode = opCode.substring(2);
                if (!mainCode.concat(subCode).equals(line.concat(stationCode)))
                    errorMsg.append(registry.getString("WrongMatchCheck.MSG").replace("%0", registry.getString("OperationCode.NAME")).replace("%1", line.concat(stationCode)).concat("\n"));
            }
        }

        if (bopVersion.isEmpty()) {
            errorMsg.append(registry.getString("RequiredCheck.MSG").replace("%0", registry.getString("ParallelStationNo.NAME")).concat("\n"));
        } else {
            if (bopVersion.length() != 2)
                errorMsg.append(registry.getString("LimitCheck.MSG").replace("%0", registry.getString("ParallelStationNo.NAME")).replace("%1", "2").concat("\n"));
        }

        if (opKorName.isEmpty())
            errorMsg.append(registry.getString("RequiredCheck.MSG").replace("%0", registry.getString("OpKorName.NAME")).concat("\n"));

        if (opEngName.isEmpty())
            errorMsg.append(registry.getString("RequiredCheck.MSG").replace("%0", registry.getString("OpEngName.NAME")).concat("\n"));

        if (errorMsg.length() > 0) {
            MessageBox.post(getShell(), errorMsg.toString(), registry.getString("Warning.NAME"), MessageBox.WARNING);
            return false;
        }
        /**
         * 중복된 Item Id 체크 유무
         */
        String itemId = vechicleCode + "-" + shopCode + "-" + opCode + "-" + bopVersion;
        try {
            TCComponentItem item = SDVBOPUtilities.FindItem(itemId, SDVTypeConstant.BOP_PROCESS_SHOP_ITEM);
            if (item != null) {
                MessageBox.post(getShell(), registry.getString("ValidIdCheck.MSG").replace("%0", itemId), registry.getString("Warning.NAME"), MessageBox.WARNING);
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        isValidateOK = true;

        return true;
    }
}

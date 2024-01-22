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
 * Class Name : CreateAssemblyOPDialog
 * Class Description :
 * 
 * @date 2013. 11. 15.
 * 
 */
public class ModifyAssemblyOPDialog extends SimpleSDVDialog {

    // CommandButton
    protected Button searchButton;
    private Registry registry = null;

    // private boolean isValidateOK = false; // Validation 성공유무

    /**
     * @param shell
     * @param dialogStub
     */
    public ModifyAssemblyOPDialog(Shell shell, DialogStubBean dialogStub) {
        super(shell, dialogStub);
        registry = Registry.getRegistry("com.symc.plm.me.sdv.view.assembly.assembly");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sdv.core.ui.dialog.AbstractSDVSWTDialog#validationCheck()
     */
    @Override
    protected boolean validationCheck() {
    	
    	

        // if (isValidateOK)
        // return true;
        // String mecoNo = "";
        TCComponentItemRevision mecoRevision = null;
        StringBuffer errorMsg = new StringBuffer();
        IDataSet dataSetAll = this.getSelectDataSetAll();

        /**
         * 공법 정보
         */
        String vechicleCode = dataSetAll.getStringValue("opInform", SDVPropertyConstant.OPERATION_REV_VEHICLE_CODE);
        String lineCode = dataSetAll.getStringValue("opInform", SDVPropertyConstant.OPERATION_REV_SHOP);
        String functionCode = dataSetAll.getStringValue("opInform", SDVPropertyConstant.OPERATION_REV_FUNCTION_CODE);
        String opCode = dataSetAll.getStringValue("opInform", SDVPropertyConstant.OPERATION_REV_OPERATION_CODE);
        // String ProductNo = dataSetAll.getStringValue("opInform", SDVPropertyConstant.OPERATION_REV_PRODUCT_CODE);
        String bopVersion = dataSetAll.getStringValue("opInform", SDVPropertyConstant.OPERATION_REV_BOP_VERSION);
        String opKorName = dataSetAll.getStringValue("opInform", SDVPropertyConstant.OPERATION_REV_KOR_NAME);
        // String opEngName = dataSetAll.getStringValue("opInform", SDVPropertyConstant.OPERATION_ENG_NAME);
        // String lovDR = dataSetAll.getStringValue("opInform", SDVPropertyConstant.OPERATION_REV_DR);

        /**
         * 공정 정보
         */
        String stationCode = dataSetAll.getStringValue("opInform", SDVPropertyConstant.OPERATION_REV_STATION_NO);
        String operaterCode = dataSetAll.getStringValue("opInform", SDVPropertyConstant.OPERATION_WORKER_CODE);
        String opSequence = dataSetAll.getStringValue("opInform", SDVPropertyConstant.OPERATION_PROCESS_SEQ);
        String workPosition = dataSetAll.getStringValue("opInform", SDVPropertyConstant.OPERATION_WORKAREA);

        /**
         * 기타 정보
         */
        String assySystem = dataSetAll.getStringValue("opInform", SDVPropertyConstant.OPERATION_REV_ASSY_SYSTEM);
        String workUbody = dataSetAll.getStringValue("opInform", SDVPropertyConstant.OPERATION_WORK_UBODY);

        // mecoNo = dataSetAll.getStringValue("mecoSelect", "mecoNo");
//        Object mecoObj = dataSetAll.getValue("mecoSelect", "mecoRev");
//        if (mecoObj != null)
//            mecoRevision = (TCComponentItemRevision) mecoObj;

        /**
         * 필수 항목 체크
         */
        
        // MECO No체크
        //if (mecoNo == null || mecoNo.isEmpty())
//        if (mecoRevision == null)
//            errorMsg.append(registry.getString("RequiredCheck.MSG").replace("%0", "MECO NO").concat("\n"));

        if (vechicleCode.isEmpty())
            errorMsg.append(registry.getString("RequiredCheck.MSG").replace("%0", registry.getString("VehicleCode.NAME")).concat("\n"));

        if (functionCode.isEmpty()) {
            errorMsg.append(registry.getString("RequiredCheck.MSG").replace("%0", registry.getString("FunctionCode.NAME")).concat("\n"));
        } else {
            if (functionCode.length() != 3)
                errorMsg.append(registry.getString("LimitCheck.MSG").replace("%0", registry.getString("FunctionCode.NAME")).replace("%1", "3").concat("\n"));
        }

        if (opCode.isEmpty()) {
            errorMsg.append(registry.getString("RequiredCheck.MSG").replace("%0", registry.getString("OperationCode.NAME")).concat("\n"));
        } else {
            if (opCode.length() != 4)
                errorMsg.append(registry.getString("LimitCheck.MSG").replace("%0", registry.getString("OperationCode.NAME")).replace("%1", "4").concat("\n"));
        }

        if (bopVersion.isEmpty()) {
            errorMsg.append(registry.getString("RequiredCheck.MSG").replace("%0", registry.getString("BopVersion.NAME")).concat("\n"));
        } else {
            if (bopVersion.length() != 2)
                errorMsg.append(registry.getString("LimitCheck.MSG").replace("%0", registry.getString("BopVersion.NAME")).replace("%1", "2").concat("\n"));
        }

        if (opKorName.isEmpty())
            errorMsg.append(registry.getString("RequiredCheck.MSG").replace("%0", registry.getString("OpKorName.NAME")).concat("\n"));

        // if (opEngName.isEmpty())
        // errorMsg.append(registry.getString("RequiredCheck.MSG").replace("%0", registry.getString("OpEngName.NAME")).concat("\n"));

        if (stationCode.isEmpty()) {
            errorMsg.append(registry.getString("RequiredCheck.MSG").replace("%0", registry.getString("OpStationNo.NAME")).concat("\n"));
        } else {
            if (stationCode.length() != 6)
                errorMsg.append(registry.getString("LimitCheck.MSG").replace("%0", registry.getString("OpStationNo.NAME")).replace("%1", "6").concat("\n"));
            else {
                String regEx = "\\p{Alnum}{2}(-\\p{Alnum}{3})?";
                boolean isMatches = stationCode.matches(regEx);
                if (!isMatches)
                    errorMsg.append(registry.getString("WrongMatchCheck.MSG").replace("%0", registry.getString("OpStationNo.NAME")).replace("%1", "##-###").concat("\n"));
            }
        }

        if (operaterCode.isEmpty()) {
            errorMsg.append(registry.getString("RequiredCheck.MSG").replace("%0", registry.getString("WorkerCode.NAME")).concat("\n"));
        } else {
            if (operaterCode.length() != 6)
                errorMsg.append(registry.getString("LimitCheck.MSG").replace("%0", registry.getString("WorkerCode.NAME")).replace("%1", "6").concat("\n"));
            else {
                String regEx = "\\p{Alnum}{2}(-\\p{Alnum}{3})?";
                boolean isMatches = operaterCode.matches(regEx);
                if (!isMatches)
                    errorMsg.append(registry.getString("WrongMatchCheck.MSG").replace("%0", registry.getString("WorkerCode.NAME")).replace("%1", "##-###").concat("\n"));
            }
        }

        if (opSequence.isEmpty()) {
            errorMsg.append(registry.getString("RequiredCheck.MSG").replace("%0", registry.getString("OpProcessSeq.NAME")).concat("\n"));
        } else {
            if (opSequence.length() < 2)
                errorMsg.append(registry.getString("MinLimitCheck.MSG").replace("%0", registry.getString("OpProcessSeq.NAME")).replace("%1", "2").concat("\n"));
        }

        if (workPosition.isEmpty())
            errorMsg.append(registry.getString("RequiredCheck.MSG").replace("%0", registry.getString("WorkArea.NAME")).concat("\n"));

        if (assySystem.isEmpty())
            errorMsg.append(registry.getString("RequiredCheck.MSG").replace("%0", registry.getString("AssySystem.NAME")).concat("\n"));

        if (workUbody.isEmpty())
            errorMsg.append(registry.getString("RequiredCheck.MSG").replace("%0", registry.getString("WorkUbody.NAME")).concat("\n"));

        if (errorMsg.length() > 0) {
            MessageBox.post(getShell(), errorMsg.toString(), registry.getString("Warning.NAME"), MessageBox.WARNING);
            return false;
        }

        /**
         * 중복된 Item Id 체크 유무
         */
//        String itemId = vechicleCode + "-" + lineCode + "-" + functionCode + "-" + opCode + "-" + bopVersion;
//        try {
//            TCComponentItem item = SDVBOPUtilities.FindItem(itemId, SDVTypeConstant.BOP_PROCESS_SHOP_ITEM);
//            if (item != null) {
//                MessageBox.post(getShell(), registry.getString("ValidIdCheck.MSG").replace("%0", itemId), registry.getString("Warning.NAME"), MessageBox.WARNING);
//                return false;
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        // isValidateOK = true;

        return true;
    }

}

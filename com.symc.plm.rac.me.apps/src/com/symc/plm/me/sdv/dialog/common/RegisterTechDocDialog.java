package com.symc.plm.me.sdv.dialog.common;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Shell;
import org.sdv.core.beans.DialogStubBean;
import org.sdv.core.common.data.IDataSet;
import org.sdv.core.ui.dialog.SimpleSDVDialog;

import com.symc.plm.me.common.SDVPropertyConstant;
import com.teamcenter.rac.util.Registry;

public class RegisterTechDocDialog extends SimpleSDVDialog {
    
 // CommandButton
    protected Button searchButton;
    @SuppressWarnings("unused")
    private Registry registry = null;
    private boolean isValidateOK = false; // Validation 성공유무

    public RegisterTechDocDialog(Shell shell, DialogStubBean dialogStub) {
        super(shell, dialogStub);
        registry = Registry.getRegistry("com.symc.plm.me.sdv.view.common");
    }

//    public RegisterTechDocDialog(Shell shell, DialogStubBean dialogStub, int configId) {
//        super(shell, dialogStub, configId);
//    }
    
    @Override
    protected boolean validationCheck() {
        if (isValidateOK)
            return true;
        
        StringBuffer errorMsg = new StringBuffer();
        IDataSet dataSetAll = this.getSelectDataSetAll();
        
        String docType = dataSetAll.getStringValue("registerTechDocView", SDVPropertyConstant.M7_TECH_DOC_TYPE);
        String ipClass = dataSetAll.getStringValue("registerTechDocView", SDVPropertyConstant.IP_CLASSIFICATION);
        String desc = dataSetAll.getStringValue("registerTechDocView", SDVPropertyConstant.ITEM_OBJECT_DESC);
        
          
        if (docType.isEmpty())
//            errorMsg.append(registry.getString("RequiredCheck.MSG").replace("%0", registry.getString("VehicleCode.NAME")).concat("\n"));
            errorMsg.append("fail");
        
        if (ipClass.isEmpty())
            errorMsg.append("fail");
//            errorMsg.append(registry.getString("RequiredCheck.MSG").replace("%0", registry.getString("VehicleCode.NAME")).concat("\n"));
//        
        if (desc.isEmpty() || desc.length() > 240)
            errorMsg.append("'Description' 속성은 240자 이하로 입력하셔야 합니다." + "\n");
//            errorMsg.append(registry.getString("RequiredCheck.MSG").replace("%0", registry.getString("VehicleCode.NAME")).concat("\n"));
        
        
        
        
        isValidateOK = true;
        
        return true;
    }

}

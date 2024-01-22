/**
 * 
 */
package com.symc.plm.me.sdv.dialog.paint;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
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
 * Class Name : CreatePaintLineDialog
 * Class Description : 
 * @date 2013. 12. 2.
 *
 */
public class CreatePaintLineDialog extends SimpleSDVDialog{
    // ActionButton
    protected Button editButton;
    // CommandButton
    protected Button searchButton;
    // PLANT PRE FIX
    private static String PLANT_PREFIX = "PTP-";
    private Registry registry = null;
    //private boolean isValidateOK = false; // Validation 성공유무

    /**
     * @param shell
     * @param dialogStub
     */
    public CreatePaintLineDialog(Shell shell, DialogStubBean dialogStub) {
        super(shell, dialogStub);
        registry = Registry.getRegistry("com.symc.plm.me.sdv.view.paint.paint");
    }

    /* (non-Javadoc)
     * @see org.sdv.core.ui.dialog.SimpleSDVDialog#validationCheck()
     */
    @Override
    protected boolean validationCheck() {
        
//        if (isValidateOK)
//            return true;

        //String mecoNo = "";
        TCComponentItemRevision mecoRevision = null;
        StringBuffer errorMsg = new StringBuffer();

        IDataSet dataSetAll = this.getSelectDataSetAll();

        /**
         * Line 정보
         */
        String txtShopCode = dataSetAll.getStringValue("lineInform", SDVPropertyConstant.LINE_REV_SHOP_CODE);
        String lovLine = dataSetAll.getStringValue("lineInform", SDVPropertyConstant.LINE_REV_CODE);
        String txtProductCode = dataSetAll.getStringValue("lineInform", SDVPropertyConstant.LINE_REV_PRODUCT_CODE);
        String lovParallelStationNo = dataSetAll.getStringValue("lineInform", SDVPropertyConstant.LINE_PARALLEL_LINE_NO);
        String txtLineKorName = dataSetAll.getStringValue("lineInform", SDVPropertyConstant.ITEM_OBJECT_NAME);
        String txtLineEngName = dataSetAll.getStringValue("lineInform", SDVPropertyConstant.LINE_REV_ENG_NAME);

        //mecoNo = dataSetAll.getStringValue("mecoSelect", "mecoNo");
        Object mecoObj = dataSetAll.getValue("mecoSelect", "mecoRev");
        if (mecoObj != null)
            mecoRevision = (TCComponentItemRevision) mecoObj;
        
        // MECO No체크
        if (mecoRevision == null)
            errorMsg.append(registry.getString("RequiredCheck.MSG").replace("%0", "MECO NO").concat("\n"));
        
        if (txtShopCode.isEmpty())
            errorMsg.append(registry.getString("RequiredCheck.MSG").replace("%0", registry.getString("ShopCode.NAME")).concat("\n"));
        
        if (lovLine.isEmpty())
            errorMsg.append(registry.getString("RequiredCheck.MSG").replace("%0", registry.getString("LineCode.NAME")).concat("\n"));
                
        if (txtProductCode.isEmpty())
            errorMsg.append(registry.getString("RequiredCheck.MSG").replace("%0", registry.getString("ProductCode.NAME")).concat("\n"));
        
        if (lovParallelStationNo.isEmpty())
            errorMsg.append(registry.getString("RequiredCheck.MSG").replace("%0", registry.getString("ParallelStationNo.NAME")).concat("\n"));
        
        if (txtLineKorName.isEmpty())
            errorMsg.append(registry.getString("RequiredCheck.MSG").replace("%0", registry.getString("LineKorName.NAME")).concat("\n"));
        
        if (txtLineEngName.isEmpty())
            errorMsg.append(registry.getString("RequiredCheck.MSG").replace("%0", registry.getString("LineEngName.NAME")).concat("\n"));
        
        if (errorMsg.length() > 0) {
            MessageBox.post(getShell(), errorMsg.toString(), registry.getString("Warning.NAME"), MessageBox.WARNING);
            return false;
        }
        
        /**
         * 중복된 Item Id 체크 유무
         */
        String itemId = PLANT_PREFIX + txtShopCode + "-" + lovLine + "-" + txtProductCode + "-" + lovParallelStationNo;
        try {
            TCComponentItem item = SDVBOPUtilities.FindItem(itemId, SDVTypeConstant.BOP_PROCESS_SHOP_ITEM);
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
    
    /* (non-Javadoc)
     * @see org.sdv.core.ui.dialog.AbstractSDVSWTDialog#getRootContext()
     */
    @Override
    public Composite getRootContext() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.sdv.core.ui.dialog.AbstractSDVSWTDialog#refresh()
     */
    @Override
    public void refresh() {
    }
    
}

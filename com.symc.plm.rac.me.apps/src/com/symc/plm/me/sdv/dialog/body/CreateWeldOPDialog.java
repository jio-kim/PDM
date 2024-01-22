/**
 *
 */
package com.symc.plm.me.sdv.dialog.body;

import org.eclipse.swt.widgets.Shell;
import org.sdv.core.beans.DialogStubBean;
import org.sdv.core.common.data.IDataSet;
import org.sdv.core.ui.UIManager;
import org.sdv.core.ui.dialog.AbstractSDVSWTDialog;
import org.sdv.core.ui.dialog.SimpleSDVDialog;

import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.sdv.view.body.CreateWeldOPView;
import com.symc.plm.me.utils.CustomUtil;
import com.symc.plm.me.utils.SYMTcUtil;
import com.teamcenter.rac.kernel.TCComponentBOPLine;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;

/**
 * Class Name : CreateBodyOPDialog
 * Class Description :
 * 
 * [SR140702-044][20140702] jwLee 용접공법 ID체계 변경 (1. LOV추가, 2. Serial No.체계 변경, 3. 용접공법 중복 검사 소스 이동)
 * 
 * @date 2013. 11. 20.
 *
 */
public class CreateWeldOPDialog extends SimpleSDVDialog {
	Registry registry = Registry.getRegistry(CreateWeldOPDialog.class);

	protected CreateWeldOPView createWeldView;

	//private IDataSet saveDataSet = null;



    /**
     * @param shell
     * @param dialogStub
     */
    public CreateWeldOPDialog(Shell shell, DialogStubBean dialogStub) {
        super(shell, dialogStub);
    }

    /**
     * @param shell
     * @param dialogStub
     * @param configId
     */
    public CreateWeldOPDialog(Shell shell, DialogStubBean dialogStub, int configId) {
        super(shell, dialogStub, configId);
    }



    @Override
    protected void applyPressed() {
        super.applyPressed();

        refresh();
    }



    @Override
    public void refresh() {
        super.refresh();
        AbstractSDVSWTDialog dialog = (AbstractSDVSWTDialog) UIManager.getCurrentDialog();
        createWeldView = (CreateWeldOPView) dialog.getView("createWeldOP");
        try {
            createWeldView.refreshInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* (non-Javadoc)
     * @see org.sdv.core.ui.dialog.AbstractSDVSWTDialog#showErrorMessage(java.lang.String, java.lang.Throwable)
     */
    @Override
    protected void showErrorMessage(String message, Throwable th) {
    	if (th != null && th instanceof Exception)
    		th.printStackTrace();

    	if (getShell() == null)
    		MessageBox.post(message, "ERROR", MessageBox.ERROR);
    	else
    		MessageBox.post(getShell(), message, "ERROR", MessageBox.ERROR);
    }


    /* (non-Javadoc)
     * @see org.sdv.core.ui.dialog.AbstractSDVSWTDialog#validationCheck()
     */
    @Override
    protected boolean validationCheck() {
    	try
    	{
    	    IDataSet dataSet = getDataSetAll();

    		// mecoView 체크
            Object mecoID = dataSet.getValue("searchMECO", SDVPropertyConstant.SHOP_REV_MECO_NO);

            if (!(Boolean) dataSet.getValue("createWeldOP", SDVPropertyConstant.OPERATION_REV_IS_ALTBOP))
            {
                if (mecoID == null || mecoID.toString().trim().length() == 0)
                {
                    showErrorMessage("[" + SYMTcUtil.getPropertyDisplayName(SDVTypeConstant.BOP_PROCESS_OPERATION_ITEM_REV, SDVPropertyConstant.OPERATION_REV_MECO_NO) + "]" + registry.getString("RequiredField.MESSAGE", "is a required field."), null);
                    return false;
                }
            }

            // createWeldOP 체크
            TCComponentBOPLine targetOP = (TCComponentBOPLine) dataSet.getValue("createWeldOP", "targetOP");
            //String gunID = dataSet.getValue("createWeldOP", "gunID").toString();
            String serialNO = dataSet.getValue("createWeldOP", "serialNO").toString();
            String weldOpOption = dataSet.getValue("createWeldOP", "weldOpOption").toString();
	    	if (targetOP == null || targetOP.toString().trim().length() == 0)
	    	{
	    		showErrorMessage("[" + "TARGET_OP" + "]" + registry.getString("RequiredField.MESSAGE", " is a required field."), null);
	    		return false;
	    	}

	    	if (dataSet.getValue("createWeldOP", "gunID") == null)
	    	{
	    		showErrorMessage("[" + "Gun" + "]" + registry.getString("RequiredField.MESSAGE", " is a required field."), null);
	    		return false;
	    	}

	    	if (serialNO == null || serialNO.trim().length() == 0)
	    	{
	    		showErrorMessage("[" + "Serial NO." + "]" + registry.getString("RequiredField.MESSAGE", " is a required field."), null);
	    		return false;
	    	}

	    	if (weldOpOption == null || weldOpOption.trim().length() == 0)
            {
                showErrorMessage("[" + "Option." + "]" + registry.getString("RequiredField.MESSAGE", " is a required field."), null);
                return false;
            }

	    	// WeldOP 를 생성하기전 동일한 이름에 ID가 있는지 검색한다
	    	String id = targetOP.getItem().getProperty(SDVPropertyConstant.ITEM_ITEM_ID) + "-WEOP-" + serialNO + weldOpOption;
	    	TCComponentItem findItem = CustomUtil.findItem(SDVTypeConstant.BOP_PROCESS_BODY_WELD_OPERATION_ITEM, id);
	    	if (null != findItem) {
	    	    showErrorMessage("[" + "WeldOP ID." + "] " + registry.getString("SameID.MESSAGE", " The same ID exists.") + "  " + id, null);
                return false;
            }
    	}
    	catch (Exception ex)
    	{
    		showErrorMessage(ex.getMessage(), ex);
    		return false;
    	}

    	return true;
    }

}

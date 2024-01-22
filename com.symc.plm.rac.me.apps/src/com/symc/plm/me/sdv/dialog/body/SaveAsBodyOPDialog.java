/**
 * 
 */
package com.symc.plm.me.sdv.dialog.body;

import org.eclipse.swt.widgets.Shell;
import org.sdv.core.beans.DialogStubBean;
import org.sdv.core.common.data.IDataSet;
import org.sdv.core.ui.dialog.SimpleSDVDialog;

import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.sdv.view.body.CreateBodyOPView;
import com.symc.plm.me.utils.SYMTcUtil;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.util.Registry;

/**
 *
 */
public class SaveAsBodyOPDialog extends SimpleSDVDialog {
	Registry registry = Registry.getRegistry(CreateBodyOPDialog.class);

	/**
	 * @param shell
	 * @param dialogStub
	 */
	public SaveAsBodyOPDialog(Shell shell, DialogStubBean dialogStub) {
		super(shell, dialogStub);
	}

	/**
	 * @param shell
	 * @param dialogStub
	 * @param configId
	 */
	public SaveAsBodyOPDialog(Shell shell, DialogStubBean dialogStub, int configId) {
		super(shell, dialogStub, configId);
	}

    /* (non-Javadoc)
     * @see org.sdv.core.ui.dialog.AbstractSDVSWTDialog#validationCheck()
     */
    @Override
    protected boolean validationCheck() {
    	try
    	{
    		IDataSet dataSet = getDataSetAll();
    		Object mecoID = dataSet.getValue("mecoView", SDVPropertyConstant.SHOP_REV_MECO_NO);
    		
    		String vehicle_code = dataSet.getStringValue("saveAsOPView", SDVPropertyConstant.OPERATION_REV_VEHICLE_CODE);
    		String shop = dataSet.getStringValue("saveAsOPView", SDVPropertyConstant.OPERATION_REV_SHOP);
    		String station_code = dataSet.getStringValue("saveAsOPView", SDVPropertyConstant.OPERATION_REV_STATION_CODE);
    		String op_code = dataSet.getStringValue("saveAsOPView", SDVPropertyConstant.OPERATION_REV_OPERATION_CODE);
    		String bop_version = dataSet.getStringValue("saveAsOPView", SDVPropertyConstant.OPERATION_REV_BOP_VERSION);
    		String kor_name = dataSet.getStringValue("saveAsOPView", SDVPropertyConstant.OPERATION_REV_KOR_NAME);
    		String eng_name = dataSet.getStringValue("saveAsOPView", SDVPropertyConstant.OPERATION_REV_ENG_NAME);
    		Object target_op_object = dataSet.getValue("saveAsOPView", CreateBodyOPView.BodyOPViewType);
    		Object is_alt = dataSet.getValue("createOPView", SDVPropertyConstant.OPERATION_REV_IS_ALTBOP);
//    		String alt_prefix = dataSet.getStringValue("createOPView", SDVPropertyConstant.OPERATION_REV_ALT_PREFIX);
//    		String dr = dataSet.getStringValue("saveAsOPView", SDVPropertyConstant.OPERATION_REV_DR);
//    		String kpc = dataSet.getStringValue("saveAsOPView", SDVPropertyConstant.OPERATION_REV_KPC);
//    		String worker_cnt = dataSet.getStringValue("saveAsOPView", SDVPropertyConstant.PAINT_OPERATION_REV_WORKER_COUNT);

    		if (is_alt != null && ! is_alt.toString().toUpperCase().equals("TRUE") && (mecoID == null || mecoID.toString().trim().length() == 0))
    		{
    			showErrorMessage("[" + SYMTcUtil.getPropertyDisplayName(SDVTypeConstant.BOP_PROCESS_OPERATION_ITEM_REV, SDVPropertyConstant.OPERATION_REV_MECO_NO) + "]" + registry.getString("RequiredField.MESSAGE", "is a required field."), null);
    			return false;
    		}

	    	if (vehicle_code == null || vehicle_code.trim().length() == 0)
	    	{
	    		showErrorMessage("[" + SYMTcUtil.getPropertyDisplayName(SDVTypeConstant.BOP_PROCESS_OPERATION_ITEM_REV, SDVPropertyConstant.OPERATION_REV_VEHICLE_CODE) + "]" + registry.getString("RequiredField.MESSAGE", "is a required field."), null);
	    		return false;
	    	}

	    	if (shop == null || shop.trim().length() == 0)
	    	{
	    		showErrorMessage("[" + SYMTcUtil.getPropertyDisplayName(SDVTypeConstant.BOP_PROCESS_OPERATION_ITEM_REV, SDVPropertyConstant.OPERATION_REV_SHOP) + "]" + registry.getString("RequiredField.MESSAGE", "is a required field."), null);
	    		return false;
	    	}

	    	if (station_code == null || station_code.trim().length() == 0)
	    	{
	    		showErrorMessage("[" + SYMTcUtil.getPropertyDisplayName(SDVTypeConstant.BOP_PROCESS_OPERATION_ITEM_REV, SDVPropertyConstant.OPERATION_REV_STATION_CODE) + "]" + registry.getString("RequiredField.MESSAGE", "is a required field."), null);
	    		return false;
	    	}

	    	if (target_op_object == null || target_op_object.toString().length() == 0 || !(target_op_object instanceof TCComponentBOMLine))
	    	{
	    		showErrorMessage("[" + registry.getString("CopyToAltTargetRequired.MESSAGE", "Target BOP") + "]" + registry.getString("RequiredField.MESSAGE", "is a required field."), null);
	    		return false;
	    	}
	    	//TODOS 여기에는 선택입력한 상위가 BOM에 존재하는지 확인하는 기능이 추가되어야 함.
//	    	ManufacturingSearchEngine aa = new ManufacturingSearchEngine();
//	    	MFGSearchOperationParameters params = new MFGSearchOperationParameters();
//	    	aa.

    		if (op_code == null || op_code.trim().length() == 0)
	    	{
	    		showErrorMessage("[" + SYMTcUtil.getPropertyDisplayName(SDVTypeConstant.BOP_PROCESS_OPERATION_ITEM_REV, SDVPropertyConstant.OPERATION_REV_OPERATION_CODE) + "]" + registry.getString("RequiredField.MESSAGE", "is a required field."), null);
	    		return false;
	    	}

	    	if (kor_name == null || kor_name.trim().length() == 0)
	    	{
	    		showErrorMessage("[" + SYMTcUtil.getPropertyDisplayName(SDVTypeConstant.BOP_PROCESS_OPERATION_ITEM_REV, SDVPropertyConstant.OPERATION_REV_KOR_NAME) + "]" + registry.getString("RequiredField.MESSAGE", "is a required field."), null);
	    		return false;
	    	}

    		if (eng_name == null || eng_name.trim().length() == 0)
	    	{
	    		showErrorMessage("[" + SYMTcUtil.getPropertyDisplayName(SDVTypeConstant.BOP_PROCESS_OPERATION_ITEM_REV, SDVPropertyConstant.OPERATION_REV_ENG_NAME) + "]" + registry.getString("RequiredField.MESSAGE", "is a required field."), null);
	    		return false;
	    	}

	    	if (bop_version == null || bop_version.trim().length() == 0)
	    	{
	    		showErrorMessage("[" + SYMTcUtil.getPropertyDisplayName(SDVTypeConstant.BOP_PROCESS_OPERATION_ITEM_REV, SDVPropertyConstant.OPERATION_REV_BOP_VERSION) + "]" + registry.getString("RequiredField.MESSAGE", "is a required field."), null);
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

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
import com.symc.plm.me.utils.SYMTcUtil;
import com.teamcenter.rac.util.Registry;

/**
 * Class Name : CreateBodyProcessDialog
 * Class Description :
 * 
 * @date 2013. 12. 11.
 * 
 */
public class CreateBodyProcessDialog extends SimpleSDVDialog {
    Registry registry = Registry.getRegistry(CreateBodyLineDialog.class);

    public CreateBodyProcessDialog(Shell shell, DialogStubBean dialogStub) {
        super(shell, dialogStub);
    }

    public CreateBodyProcessDialog(Shell shell, DialogStubBean dialogStub, int configId) {
        super(shell, dialogStub, configId);
    }

    @Override
    protected boolean validationCheck() {
        try {
            IDataSet dataSet = getDataSetAll();
    	    /* [CF-3537] [20230131] [개선과제]MECO 결재 거부 후 공법 개정 불가
    	     * 기존 검색 화면에서 반려된 MECO가 검색 안되는 문제가 있어서 아래 내용으로 수정 
    	    isWorkingStatus와 반려된 MECO도 나올 수 있게 수정 기존 SearchTypeItemView에서 MecoSearchView 검색창으로 변경  아래 getValue부분에 화면의 key값과 속성값 변경*/
//            Object mecoID = dataSet.getValue("mecoView", SDVPropertyConstant.STATION_MECO_NO);
            Object mecoID = dataSet.getValue(SDVPropertyConstant.MECO_SELECT, SDVPropertyConstant.MECO_NO);

            String shop_code = dataSet.getStringValue("createBodyProcessView", SDVPropertyConstant.STATION_SHOP);
            String line_code = dataSet.getStringValue("createBodyProcessView", SDVPropertyConstant.STATION_LINE);
            String station_code = dataSet.getStringValue("createBodyProcessView", SDVPropertyConstant.STATION_STATION_CODE);
            String product_code = dataSet.getStringValue("createBodyProcessView", SDVPropertyConstant.STATION_PRODUCT_CODE);
            // String bop_version = dataSet.getStringValue("createBodyProcessView", SDVPropertyConstant.STATION_BOP_VERSION);
            String stationKorName = dataSet.getStringValue("createBodyProcessView", SDVPropertyConstant.ITEM_OBJECT_NAME);
            String stationEngName = dataSet.getStringValue("createBodyProcessView", SDVPropertyConstant.STATION_ENG_NAME);
            String vehicle_code = dataSet.getStringValue("createBodyProcessView", SDVPropertyConstant.STATION_VEHICLE_CODE);
            Object isAlt = dataSet.getValue("createProcessView", SDVPropertyConstant.STATION_ALT_PREFIX);
            String altPrefix = dataSet.getStringValue("createBodyProcessView", SDVPropertyConstant.STATION_ALT_PREFIX);

            if (isAlt != null && ! isAlt.toString().toUpperCase().equals("TRUE") && (mecoID == null || mecoID.toString().trim().length() == 0)) {
                showErrorMessage("[" + SYMTcUtil.getPropertyDisplayName(SDVTypeConstant.BOP_PROCESS_STATION_ITEM_REV, SDVPropertyConstant.STATION_MECO_NO) + "]" + registry.getString("RequiredField.MESSAGE", "is a required field."), null);
                return false;
            }

            if (isAlt != null && ! isAlt.toString().toUpperCase().equals("TRUE") && (mecoID != null && mecoID.toString().startsWith("MEW")))
            {
            	showErrorMessage(registry.getString("MECOTypeOnlyWeldOP.MESSAGE", "MECO Type is only for Weld Operation."), null);
            	return false;
            }

            if (shop_code == null || shop_code.trim().length() == 0) {
                showErrorMessage("[" + SYMTcUtil.getPropertyDisplayName(SDVTypeConstant.BOP_PROCESS_STATION_ITEM_REV, SDVPropertyConstant.STATION_SHOP) + "]" + registry.getString("RequiredField.MESSAGE", "is a required field."), null);
                return false;
            }

            if (line_code == null || line_code.trim().length() == 0) {
                showErrorMessage("[" + SYMTcUtil.getPropertyDisplayName(SDVTypeConstant.BOP_PROCESS_STATION_ITEM_REV, SDVPropertyConstant.STATION_LINE) + "]" + registry.getString("RequiredField.MESSAGE", "is a required field."), null);
                return false;
            }

            if (station_code == null || station_code.trim().length() == 0) {
                showErrorMessage("[" + SYMTcUtil.getPropertyDisplayName(SDVTypeConstant.BOP_PROCESS_STATION_ITEM_REV, SDVPropertyConstant.STATION_STATION_CODE) + "]" + registry.getString("RequiredField.MESSAGE", "is a required field."), null);
                return false;
            }

            if (product_code == null || product_code.trim().length() == 0) {
                showErrorMessage("[" + SYMTcUtil.getPropertyDisplayName(SDVTypeConstant.BOP_PROCESS_STATION_ITEM_REV, SDVPropertyConstant.STATION_PRODUCT_CODE) + "]" + registry.getString("RequiredField.MESSAGE", "is a required field."), null);
                return false;
            }

            // if (bop_version == null || bop_version.trim().length() == 0) {
            // showErrorMessage("[" + SYMTcUtil.getPropertyDisplayName(SDVTypeConstant.BOP_PROCESS_STATION_ITEM_REV, SDVPropertyConstant.STATION_BOP_VERSION) + "]" + registry.getString("RequiredField.MESSAGE", "is a required field."), null);
            // return false;
            // }

            if (stationKorName == null || stationKorName.trim().length() == 0) {
                showErrorMessage("[" + SYMTcUtil.getPropertyDisplayName(SDVTypeConstant.BOP_PROCESS_STATION_ITEM_REV, SDVPropertyConstant.ITEM_OBJECT_NAME) + "]" + registry.getString("RequiredField.MESSAGE", "is a required field."), null);
                return false;
            }

            if (stationEngName == null || stationEngName.trim().length() == 0) {
                showErrorMessage("[" + SYMTcUtil.getPropertyDisplayName(SDVTypeConstant.BOP_PROCESS_STATION_ITEM_REV, SDVPropertyConstant.STATION_ENG_NAME) + "]" + registry.getString("RequiredField.MESSAGE", "is a required field."), null);
                return false;
            }

            if (vehicle_code == null || vehicle_code.trim().length() == 0) {
                showErrorMessage("[" + SYMTcUtil.getPropertyDisplayName(SDVTypeConstant.BOP_PROCESS_STATION_ITEM_REV, SDVPropertyConstant.STATION_VEHICLE_CODE) + "]" + registry.getString("RequiredField.MESSAGE", "is a required field."), null);
                return false;
            }

             if (isAlt != null && isAlt.toString().toUpperCase().equals("TRUE") && (altPrefix == null || altPrefix.trim().length() == 0)) {
	             showErrorMessage("[" + SYMTcUtil.getPropertyDisplayName(SDVTypeConstant.BOP_PROCESS_STATION_ITEM_REV, SDVPropertyConstant.STATION_ALT_PREFIX) + "]" + registry.getString("RequiredField.MESSAGE", "is a required field."), null);
	             return false;
             }
        } catch (Exception ex) {
            showErrorMessage(ex.getMessage(), ex);
            return false;
        }

        return true;
    }

}

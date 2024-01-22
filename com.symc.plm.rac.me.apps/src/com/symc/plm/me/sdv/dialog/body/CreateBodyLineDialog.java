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
 * Class Name : CreateBodyLineDialog
 * Class Description :
 * 
 * @date 2013. 12. 9.
 * 
 */
public class CreateBodyLineDialog extends SimpleSDVDialog {
    Registry registry = Registry.getRegistry(CreateBodyLineDialog.class);

    /**
     * @param shell
     * @param dialogStub
     */
    public CreateBodyLineDialog(Shell shell, DialogStubBean dialogStub) {
        super(shell, dialogStub);
    }
    
    /**
     * @param shell
     * @param dialogStub
     * @param configId
     */
    public CreateBodyLineDialog(Shell shell, DialogStubBean dialogStub, int configId) {
        super(shell, dialogStub, configId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sdv.core.ui.dialog.SimpleSDVDialog#validationCheck()
     */
    @Override
    protected boolean validationCheck() {
        try {
            IDataSet dataSet = getDataSetAll();
            /*[CF-3537] [20230131] 	[개선과제]MECO 결재 거부 후 공법 개정 불가
             * 기존 검색 화면에서 반려된 MECO가 검색 안되는 문제가 있어서 아래 내용으로 수정 
            isWorkingStatus와 반려된 MECO도 나올 수 있게 수정 기존 SearchTypeItemView에서 MecoSearchView 검색창으로 변경 아래 getValue부분에 화면의 key값과 속성값 변경*/ 
//            Object mecoID = dataSet.getValue("mecoView", SDVPropertyConstant.SHOP_REV_MECO_NO);
            Object mecoID = dataSet.getValue(SDVPropertyConstant.MECO_SELECT, SDVPropertyConstant.MECO_NO);
            
            String shop = dataSet.getStringValue("CreateBodyLineView", SDVPropertyConstant.LINE_REV_SHOP_CODE);
            String line = dataSet.getStringValue("CreateBodyLineView", SDVPropertyConstant.LINE_REV_CODE);
            String lineKorName = dataSet.getStringValue("CreateBodyLineView", SDVPropertyConstant.ITEM_OBJECT_NAME);
            String lineEngName = dataSet.getStringValue("CreateBodyLineView", SDVPropertyConstant.LINE_REV_ENG_NAME);
//            String jph = dataSet.getStringValue("CreateBodyLineView", SDVPropertyConstant.LINE_REV_JPH);
//            String allowance = dataSet.getStringValue("CreateBodyLineView", SDVPropertyConstant.LINE_REV_ALLOWANCE);
            Object isAlt = dataSet.getValue("CreateBodyLineView", SDVPropertyConstant.SHOP_REV_IS_ALTBOP);
            String altPrefix = dataSet.getStringValue("CreateBodyLineView", SDVPropertyConstant.LINE_REV_ALT_PREFIX);

            if (isAlt != null && ! isAlt.toString().toUpperCase().equals("TRUE") && (mecoID == null || mecoID.toString().trim().length() == 0)) {
                showErrorMessage("[" + SYMTcUtil.getPropertyDisplayName(SDVTypeConstant.BOP_PROCESS_LINE_ITEM_REV, SDVPropertyConstant.LINE_REV_MECO_NO) + "]" + registry.getString("RequiredField.MESSAGE", "is a required field."), null);
                return false;
            }

            if (isAlt != null && ! isAlt.toString().toUpperCase().equals("TRUE") && (mecoID != null && mecoID.toString().startsWith("MEW")))
            {
            	showErrorMessage(registry.getString("MECOTypeOnlyWeldOP.MESSAGE", "MECO Type is only for Weld Operation."), null);
            	return false;
            }

            if (shop == null || shop.trim().length() == 0) {
                showErrorMessage("[" + SYMTcUtil.getPropertyDisplayName(SDVTypeConstant.BOP_PROCESS_SHOP_ITEM_REV, SDVPropertyConstant.SHOP_REV_SHOP_CODE) + "]" + registry.getString("RequiredField.MESSAGE", "is a required field."), null);
                return false;
            }

            if (line == null || line.trim().length() == 0) {
                showErrorMessage("[" + SYMTcUtil.getPropertyDisplayName(SDVTypeConstant.BOP_PROCESS_LINE_ITEM_REV, SDVPropertyConstant.LINE_REV_CODE) + "]" + registry.getString("RequiredField.MESSAGE", "is a required field."), null);
                return false;
            }

            if (lineKorName == null || lineKorName.trim().length() == 0) {
                showErrorMessage("[" + SYMTcUtil.getPropertyDisplayName(SDVTypeConstant.BOP_PROCESS_LINE_ITEM_REV, SDVPropertyConstant.ITEM_OBJECT_NAME) + "]" + registry.getString("RequiredField.MESSAGE", "is a required field."), null);
                return false;
            }

            if (lineEngName == null || lineEngName.trim().length() == 0) {
                showErrorMessage("[" + SYMTcUtil.getPropertyDisplayName(SDVTypeConstant.BOP_PROCESS_LINE_ITEM_REV, SDVPropertyConstant.LINE_REV_ENG_NAME) + "]" + registry.getString("RequiredField.MESSAGE", "is a required field."), null);
                return false;
            }

//            if (jph == null || jph.trim().length() == 0) {
//                showErrorMessage("[" + SYMTcUtil.getPropertyDisplayName(SDVTypeConstant.BOP_PROCESS_LINE_ITEM_REV, SDVPropertyConstant.LINE_REV_JPH) + "]" + registry.getString("RequiredField.MESSAGE", "is a required field."), null);
//                return false;
//            }

            if (isAlt != null && isAlt.toString().toUpperCase().equals("TRUE") && (altPrefix == null || altPrefix.trim().length() == 0)) {
                showErrorMessage("[" + SYMTcUtil.getPropertyDisplayName(SDVTypeConstant.BOP_PROCESS_SHOP_ITEM_REV, SDVPropertyConstant.SHOP_REV_ALT_PREFIX) + "]" + registry.getString("RequiredField.MESSAGE", "is a required field."), null);
                return false;
            }
        } catch (Exception ex) {
            showErrorMessage(ex.getMessage(), ex);
            return false;
        }

        return true;
    }
}

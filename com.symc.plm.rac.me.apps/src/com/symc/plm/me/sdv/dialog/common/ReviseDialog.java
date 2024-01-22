/**
 * 
 */
package com.symc.plm.me.sdv.dialog.common;

import org.eclipse.swt.widgets.Shell;
import org.sdv.core.beans.DialogStubBean;
import org.sdv.core.ui.dialog.SimpleSDVDialog;

import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.sdv.view.common.ReviseView;
import com.symc.plm.me.utils.SYMTcUtil;
import com.teamcenter.rac.util.ConfirmDialog;
import com.teamcenter.rac.util.Registry;

/**
 * Class Name : ReviseDialog
 * Class Description : 
 * @date 2013. 11. 20.
 *
 */
public class ReviseDialog extends SimpleSDVDialog {
	Registry registry = Registry.getRegistry(ReviseDialog.class);

    /**
     * @param shell
     * @param dialogStub
     */
    public ReviseDialog(Shell shell, DialogStubBean dialogStub) {
        super(shell, dialogStub);
    }

    /* (non-Javadoc)
     * @see org.sdv.core.ui.dialog.AbstractSDVSWTDialog#validationCheck()
     */
    @Override
    protected boolean validationCheck()
    {
    	try
    	{
    	    /* [CF-3537] [20230131]	[개선과제]MECO 결재 거부 후 공법 개정 불가
    	     *  기존 검색 화면에서 반려된 MECO가 검색 안되는 문제가 있어서 아래 내용으로 수정 
    	    isWorkingStatus와 반려된 MECO도 나올 수 있게 수정 기존 SearchTypeItemView에서 MecoSearchView 검색창으로 변경   아래 getValue부분에 화면의 key값과 속성값 변경*/
//    		Object mecoID = getSelectDataSet("reviseMecoView").getValue(SDVPropertyConstant.SHOP_REV_MECO_NO);
    		Object mecoID = getSelectDataSet("reviseMecoView").getValue(SDVPropertyConstant.MECO_NO);
    		Object skipMECO = getSelectDataSet("reviseView").getValue("SkipMECO");
    		if (! ((Boolean) skipMECO) && mecoID == null)
    		{
    			showErrorMessage("[" + SYMTcUtil.getPropertyDisplayName(SDVTypeConstant.BOP_PROCESS_SHOP_ITEM_REV, SDVPropertyConstant.SHOP_REV_MECO_NO) + "]" + registry.getString("RequiredField.MESSAGE", "is a required field."), null);
    			return false;
    		}
    		

    		if (getSelectDataSet("reviseView").getValue("reviseView") == null || getSelectDataSet("reviseView").getListValue("reviseView", "reviseView").size() == 0)
    		{
    			showErrorMessage(registry.getString("ReviseTargetIsNull.MESSAGE", "Revise target list is null."), null);
    			return false;
    		}

    		ReviseView reviseView = (ReviseView) getView("reviseView");
    		
    		
			/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			// Non-SR 윤순식 부장 요청 
			// Plant 아이템을 결재 하기로 업무 프로세스를 바꿈
			// Plant 개정시에는 MECO 가 필요 없음 
			// MECO 번호가 없고 Skip MECO 가 True 일경우 개정 Validation 통과
			if (((Boolean) skipMECO) && mecoID == null )
			{
				return true;
			}
			
			//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////    
    		
    		// [SR141208-036] [20150122] ymjang 용접점의 추가 삭제에 의해 일반공법 변경 사유 발생 시 MEP와 MEW용 MECO를 별개로 발행해야만 함.
    		// MEW MECO 는 용접 공법만 가능하도록 수정함.
    		if (mecoID.toString().startsWith("MEW") && !reviseView.isWeldOPOnlyMECOMEW())
    		{
    			showErrorMessage(registry.getString("SelectMEWMECOType.MESSAGE", "MEW type MECO is only available to WeldPoint Operation."), null);
    			return false;
    		}
    		
    		// 모든 항목을 개정하시겠습니까?
    		int ret = ConfirmDialog.prompt(getShell(), registry.getString("ReviseAllConfirm.TITLE", "Confirmation"), registry.getString("ReviseAllConfirm.MESSAGE", "모든 항목을 개정하시겠습니까?"));

    		if (ret != 2)
    			return false;
    	}
    	catch (Exception ex)
    	{
    		showErrorMessage(ex.getMessage(), ex);
    		return false;
    	}

    	return true;
    }
}

/**
 * 
 */
package com.symc.plm.me.sdv.dialog.common;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;
import org.sdv.core.beans.DialogStubBean;
import org.sdv.core.ui.dialog.SimpleSDVDialog;

import com.symc.plm.me.sdv.view.common.SearchTypedItemView;
import com.teamcenter.rac.util.Registry;

/**
 * Class Name : SearchTypedItemDialog
 * Class Description : 
 * @date 2013. 11. 20.
 *
 */
public class SearchTypedItemDialog extends SimpleSDVDialog {

	Registry registry = Registry.getRegistry(SearchTypedItemDialog.class);

    /**
     * @param shell
     * @param dialogStub
     */
    public SearchTypedItemDialog(Shell shell, DialogStubBean dialogStub) {
        super(shell, dialogStub);
        setParentDialogCompositeSize(new Point(550, 400));
    }

    /* (non-Javadoc)
     * @see org.sdv.core.ui.dialog.AbstractSDVSWTDialog#validationCheck()
     */
    @Override
    protected boolean validationCheck()
    {
    	try
    	{
	    	Object selectedItem = getSelectDataSetAll().getValue(SearchTypedItemView.ReturnSelectedKey);

	    	if (selectedItem == null)
	    	{
	    		showErrorMessage(registry.getString("SelectSearchResult.MESSAGE", "please. item select"), null);
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

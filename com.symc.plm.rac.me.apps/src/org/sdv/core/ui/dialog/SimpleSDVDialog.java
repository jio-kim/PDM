/**
 *
 */
package org.sdv.core.ui.dialog;

import org.eclipse.swt.widgets.Shell;
import org.sdv.core.beans.DialogStubBean;
import org.sdv.core.common.IViewPane;
import org.sdv.core.common.data.IDataMap;
import org.sdv.core.common.data.IDataSet;
import org.sdv.core.ui.UIManager;

import com.teamcenter.rac.util.MessageBox;

/**
 * Bean기준 생성 기본 Simple Dialog
 *
 * Class Name : SimpleSDVDialog
 * Class Description :
 *
 * @date 2013. 10. 11.
 *
 */
public class SimpleSDVDialog extends AbstractSDVSWTDialog {
    protected UIManager uiManager;

    /**
     * Create the dialog.
     *
     * @param parentShell
     */
    public SimpleSDVDialog(Shell shell, DialogStubBean dialogStub) {
        super(shell, dialogStub);
    }

	/**
	 * @param shell
	 * @param dialogStub
	 * @param configId
	 */
	public SimpleSDVDialog(Shell shell, DialogStubBean dialogStub, int configId) {
		super(shell, dialogStub, configId);
	}

    /* (non-Javadoc)
     * @see org.sdv.core.ui.dialog.AbstractSDVSWTDialog#afterCreateContents()
     */
    @Override
    protected void afterCreateContents() {

    }

    /* (non-Javadoc)
     * @see org.sdv.core.ui.dialog.AbstractSDVSWTDialog#validationCheck()
     */
    @Override
    protected boolean validationCheck() {
        return true;
    }

    /* (non-Javadoc)
     * @see org.sdv.core.ui.dialog.AbstractSDVSWTDialog#apply()
     */
    @Override
    protected boolean apply() {
        return true;
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

    /**
     * Description :
     * @method :
     * @date : 2013. 11. 21.
     * @author : cspark
     * @param :
     * @return :
     * @see org.sdv.core.ui.dialog.AbstractSDVSWTDialog#setLocalDataMap(org.sdv.core.common.data.IDataMap)
     */
    @Override
    public void setLocalDataMap(IDataMap dataMap) {
    }


    /**
     * Description :
     * @method :
     * @date : 2013. 11. 21.
     * @author : cspark
     * @param :
     * @return :
     * @see org.sdv.core.ui.dialog.AbstractSDVSWTDialog#getLocalDataMap()
     */
    @Override
    public IDataMap getLocalDataMap() {
        return null;
    }


    /**
     * Description :
     * @method :
     * @date : 2013. 11. 21.
     * @author : cspark
     * @param :
     * @return :
     * @see org.sdv.core.ui.dialog.AbstractSDVSWTDialog#getLocalSelectDataMap()
     */
    @Override
    public IDataMap getLocalSelectDataMap() {
        return null;
    }

    @Override
    public void initalizeLocalData(int result, IViewPane owner, IDataSet dataset) {

    }

}

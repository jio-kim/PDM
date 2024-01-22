/**
 * 
 */
package org.sdv.core.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.sdv.core.util.SDVSpringContextUtil;


/**
 * Class Name : ForceContextRefreshHandler
 * Class Description : 
 * @date 	2013. 12. 10.
 * @author  CS.Park
 * 
 */
public class ForceContextRefreshHandler extends AbstractHandler {

	/**
	 * Description :
	 * @method :
	 * @date : 2013. 12. 10.
	 * @author : CS.Park
	 * @param :
	 * @return : 
	 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	@Override
	public Object execute(ExecutionEvent evt) throws ExecutionException{
		
		SDVSpringContextUtil.forceResourceFileRefresh = true;
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(evt);
		MessageDialog.openInformation(window.getShell(), "Config Setting", "SDV Spring Context set force refresh.");
		return null;
	}


}

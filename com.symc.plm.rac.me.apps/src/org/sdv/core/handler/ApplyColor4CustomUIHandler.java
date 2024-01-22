/**
 * 
 */
package org.sdv.core.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.sdv.core.util.UIUtil;


/**
 * Class Name : ApplyColor4CustomUIHandler
 * Class Description : 
 * @date 	2013. 12. 17.
 * @author  CS.Park
 * 
 */
public class ApplyColor4CustomUIHandler extends AbstractHandler {

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
		
		Command command = evt.getCommand();
	    boolean oldValue = HandlerUtil.toggleCommandState(command);
	    boolean newValue = !oldValue;
	    if(newValue != UIUtil.useRandomBackground){
	    	UIUtil.setUseRandomBackground(newValue);
	    	IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(evt);
			MessageDialog.openInformation(window.getShell(), "Config Setting", "Setting Using Custom UI Color : " + (newValue?"Enabled":"Disabled"));
	    }

		return null;
	}


}


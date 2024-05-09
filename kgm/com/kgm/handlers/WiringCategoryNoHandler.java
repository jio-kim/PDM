package com.kgm.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import com.kgm.commands.wiringcategoryno.WiringCategoryNoDialog;

public class WiringCategoryNoHandler extends AbstractHandler
{

	public WiringCategoryNoHandler()
	{
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		final Shell shell = HandlerUtil.getActiveShell(event);
		WiringCategoryNoDialog wiringOptionDialog = new WiringCategoryNoDialog(shell);
		wiringOptionDialog.open();
		return null;
	}

}

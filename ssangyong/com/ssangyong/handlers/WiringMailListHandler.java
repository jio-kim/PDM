package com.ssangyong.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import com.ssangyong.commands.wiringcategoryno.WiringMailListDialog;

public class WiringMailListHandler extends AbstractHandler
{

	public WiringMailListHandler()
	{
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		final Shell shell = HandlerUtil.getActiveShell(event);
		WiringMailListDialog wiringMailListDialog = new WiringMailListDialog(shell);
		wiringMailListDialog.open();
		return null;
	}

}

package com.ssangyong.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import com.ssangyong.admin.ecoadmincheck.CheckListDialog;

public class CheckListHandler extends AbstractHandler
{

	public CheckListHandler()
	{
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		final Shell shell = HandlerUtil.getActiveShell(event);
		CheckListDialog checkListDialog = new CheckListDialog(shell);
		checkListDialog.open();
		return null;
	}

}

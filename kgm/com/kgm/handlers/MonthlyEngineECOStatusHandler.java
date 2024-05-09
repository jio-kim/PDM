package com.kgm.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import com.kgm.admin.ecoadmincheck.MonthlyEngineECOStatusDialog;

public class MonthlyEngineECOStatusHandler extends AbstractHandler
{

	public MonthlyEngineECOStatusHandler()
	{
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		final Shell shell = HandlerUtil.getActiveShell(event);
		MonthlyEngineECOStatusDialog monthlyEngineECOStatusDialog = new MonthlyEngineECOStatusDialog(shell);
		monthlyEngineECOStatusDialog.open();
		return null;
	}

}

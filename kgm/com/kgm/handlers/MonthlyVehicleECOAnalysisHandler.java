package com.kgm.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import com.kgm.admin.ecoadmincheck.MonthlyVehicleECOAnalysisDialog;

public class MonthlyVehicleECOAnalysisHandler extends AbstractHandler
{

	public MonthlyVehicleECOAnalysisHandler()
	{
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		final Shell shell = HandlerUtil.getActiveShell(event);
		MonthlyVehicleECOAnalysisDialog monthlyVehicleECOAnalysisDialog = new MonthlyVehicleECOAnalysisDialog(shell);
		monthlyVehicleECOAnalysisDialog.open();
		return null;
	}

}

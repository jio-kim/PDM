package com.ssangyong.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import com.ssangyong.admin.ecoadmincheck.MonthlyAllVehicleECOStatusDialog;

public class MonthlyAllVehicleECOStatusHandler extends AbstractHandler
{

	public MonthlyAllVehicleECOStatusHandler()
	{
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		final Shell shell = HandlerUtil.getActiveShell(event);
		MonthlyAllVehicleECOStatusDialog monthlyAllVehicleECOStatusDialog = new MonthlyAllVehicleECOStatusDialog(shell);
		monthlyAllVehicleECOStatusDialog.open();
		return null;
	}

}

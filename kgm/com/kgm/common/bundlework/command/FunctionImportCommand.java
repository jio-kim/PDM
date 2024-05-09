/**
 * Function Excel Import
 * [SR140725-012][20141111][jclee] 상부 BOM Part Excel Upload 신규 추가
 * 2014.11.11
 * jclee
 */
package com.kgm.common.bundlework.command;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;

import com.kgm.common.bundlework.imp.BWFunctionImpDialog;
import com.teamcenter.rac.aifrcp.AIFUtility;

public class FunctionImportCommand {
	public FunctionImportCommand() {
		/** Dialog 호출. */
		Shell shell = AIFUtility.getActiveDesktop().getShell();
		BWFunctionImpDialog functionImpDialog = new BWFunctionImpDialog(shell, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		functionImpDialog.dialogOpen();
	}
}

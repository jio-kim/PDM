/**
 * FMP Excel Import
 * [SR140725-012][20141111][jclee] ��� BOM Part Excel Upload �ű� �߰�
 * 2014.11.11
 * jclee
 */
package com.kgm.common.bundlework.command;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;

import com.kgm.common.bundlework.imp.BWFunctionMastImpDialog;
import com.teamcenter.rac.aifrcp.AIFUtility;

public class FunctionMastImportCommand {
	public FunctionMastImportCommand() {
		/** Dialog ȣ��. */
		Shell shell = AIFUtility.getActiveDesktop().getShell();
		BWFunctionMastImpDialog functionMastImpDialog = new BWFunctionMastImpDialog(shell, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		functionMastImpDialog.dialogOpen();
	}
}

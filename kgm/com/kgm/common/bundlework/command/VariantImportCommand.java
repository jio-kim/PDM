/**
 * [SR140725-012][20141111][jclee] ��� BOM Part Excel Upload
 */
package com.kgm.common.bundlework.command;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;

import com.kgm.common.bundlework.imp.BWVariantImpDialog;
import com.teamcenter.rac.aifrcp.AIFUtility;

public class VariantImportCommand {
	public VariantImportCommand() {
		/** Dialog ȣ��. */
		Shell shell = AIFUtility.getActiveDesktop().getShell();
		BWVariantImpDialog variantImpDialog = new BWVariantImpDialog(shell, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		variantImpDialog.dialogOpen();
	}
}

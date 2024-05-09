/**
 * Product Excel Import
 * [SR140725-012][20141111][jclee] ��� BOM Part Excel Upload �ű� �߰�
 * [20170116][ymjang] Product �ϰ� ���� ���α׷� ���� ���� ����
 * ProductImportCommand
 */
package com.kgm.common.bundlework.command;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;

import com.kgm.common.bundlework.imp.BWProductImpDialog;
import com.teamcenter.rac.aif.AbstractAIFCommand;
import com.teamcenter.rac.aifrcp.AIFUtility;

public class ProductImportCommand extends AbstractAIFCommand {
	public ProductImportCommand() {
		/** Dialog ȣ��. */
		Shell shell = AIFUtility.getActiveDesktop().getShell();
		BWProductImpDialog productImpDialog = new BWProductImpDialog(shell, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		productImpDialog.dialogOpen();
	}
}

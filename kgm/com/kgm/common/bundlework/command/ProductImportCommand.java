/**
 * Product Excel Import
 * [SR140725-012][20141111][jclee] 상부 BOM Part Excel Upload 신규 추가
 * [20170116][ymjang] Product 일괄 생성 프로그램 실행 오류 수정
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
		/** Dialog 호출. */
		Shell shell = AIFUtility.getActiveDesktop().getShell();
		BWProductImpDialog productImpDialog = new BWProductImpDialog(shell, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		productImpDialog.dialogOpen();
	}
}

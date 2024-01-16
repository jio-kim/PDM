package com.ssangyong.commands.ec.report;

import java.util.HashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import com.ssangyong.common.dialog.SYMCAbstractDialog;

public class ReportEPLDialog extends SYMCAbstractDialog {

	public ReportEPLDialog(Shell paramShell, int paramInt) {
		super(paramShell, SWT.RESIZE | SWT.TITLE | SWT.PRIMARY_MODAL | SWT.DIALOG_TRIM | paramInt);
		System.out.println("test");
	}
	
	@Override
    protected void createButtonsForButtonBar(Composite parentScrolledComposite) {
		Composite composite = new Composite(parentScrolledComposite, SWT.NONE);
		composite.setLayout(new GridLayout());
		Button closeButton = new Button(composite, SWT.PUSH);
    	closeButton.setText("Close");
    	closeButton.addSelectionListener(new SelectionAdapter () {
			public void widgetSelected(SelectionEvent e) {
				getShell().close();
			}
		});
    }

	@Override
	protected Composite createDialogPanel(ScrolledComposite parentScrolledComposite) {
		getShell().setText("Report EPL");
		Composite composite = new Composite(parentScrolledComposite, SWT.NONE);
		composite.setLayout(new GridLayout());
		
		System.out.println("createDialogPanel");
		createSearchCondition();
		createResultTable();
		return composite;
	}

	@Override
	protected boolean validationCheck() {
		return false;
	}
	
	@Override
	protected boolean apply() {
		return false;
	}
	
	private void createSearchCondition() {
		System.out.println("createSearchCondition");
		
	}

	private void createResultTable() {
		
	}
	
	/** 하위 구조 */
	@SuppressWarnings("unused")
    private HashMap<String, String> getChildren(String itemRevisionPuid){
		HashMap<String, String> returnMap = null;
		return returnMap;
	}
	
	/** 검색 */
	@SuppressWarnings("unused")
    private void search() {
		
	}


}

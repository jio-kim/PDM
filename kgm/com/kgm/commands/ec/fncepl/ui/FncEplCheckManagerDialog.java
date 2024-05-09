package com.kgm.commands.ec.fncepl.ui;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wb.swt.SWTResourceManager;

public class FncEplCheckManagerDialog extends Dialog {

	private CTabFolder tabFolder;
	private FncEplListComposite fncEplListComposite = null;
	private FncEplStatusComposite fncEplStatusComposite = null;

	/**
	 * Create the dialog.
	 * 
	 * @param parentShell
	 */
	public FncEplCheckManagerDialog(Shell parentShell) {
		super(parentShell);
		setShellStyle(SWT.SHELL_TRIM | SWT.BORDER);
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Function EPL Check");
	}

	/**
	 * Create contents of the dialog.
	 * 
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		parent.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		Composite container = (Composite) super.createDialogArea(parent);

		container.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
		container.setBackgroundMode(SWT.INHERIT_FORCE);

		GridLayout gl_container = new GridLayout(1, false);
		gl_container.verticalSpacing = 0;
		container.setLayout(gl_container);

		tabFolder = new CTabFolder(container, SWT.BORDER);
		tabFolder.setTabHeight(20);
		tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		tabFolder.setSelectionBackground(Display.getCurrent().getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT));
		tabFolder.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				if (tabFolder.getSelectionIndex() == 0) {

					if (fncEplListComposite == null)
						return;
					if (!fncEplListComposite.isInitDataLoad())
						fncEplListComposite.initDataLoad();

				} else if (tabFolder.getSelectionIndex() == 1) {
					if (fncEplStatusComposite == null)
						return;
					if (!fncEplStatusComposite.isInitDataLoad())
						fncEplStatusComposite.initDataLoad();
				}
			};
		});

		CTabItem fncEplListTabItem = new CTabItem(tabFolder, SWT.NONE);
		fncEplListTabItem.setText("Function List 包府");

		CTabItem registerChgListTabItem = new CTabItem(tabFolder, SWT.NONE);
		registerChgListTabItem.setText("泅炔 包府");

		fncEplListComposite = new FncEplListComposite(tabFolder, SWT.NONE);
		fncEplListTabItem.setControl(fncEplListComposite);

		fncEplStatusComposite = new FncEplStatusComposite(tabFolder, SWT.NONE);
		registerChgListTabItem.setControl(fncEplStatusComposite);
		return container;
	}

	/**
	 * Create contents of the button bar.
	 * 
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		parent.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		createButton(parent, IDialogConstants.OK_ID, "Close", true);
	}

	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(1000, 600);
	}

}

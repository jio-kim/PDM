package com.kgm.commands.faq;

import java.util.HashMap;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wb.swt.ResourceManager;
import org.eclipse.wb.swt.SWTResourceManager;

import swing2swt.layout.BorderLayout;

public class OpenOrSaveDialog extends Dialog {

	private HashMap<String, Object> parentDataMap;
	private HashMap<String, Object> resultDataMap;

	private Shell shell;

	/**
	 * Create the dialog.
	 * 
	 * @param parent
	 * @param style
	 */
	public OpenOrSaveDialog(Shell parent, int style) {
		super(parent, style);

	}
	
	public OpenOrSaveDialog(Shell parent, int style, HashMap<String, Object> parentDataMap) {
		this(parent, style);
		this.parentDataMap = parentDataMap;
	}

	/**
	 * Open the dialog.
	 * 
	 * @return the resultDataMap
	 */
	public Object open() {
		createContents();
		setContents();
		centerToParent(getParent(), shell);
		openShell(getParent(), shell);

		return resultDataMap;
	}

	/**
	 * Create contents of the dialog.
	 */
	private void createContents() {
		shell = new Shell(getParent(), SWT.DIALOG_TRIM | SWT.RESIZE | SWT.PRIMARY_MODAL);
		shell.setImage(ResourceManager.getPluginImage(FaqConstant.SYMBOLICNAME, "icons/defaultapplication_16.png"));
		shell.setSize(400, 400);
		shell.setText("File Open or Save");
		shell.setLayout(new FillLayout(SWT.HORIZONTAL));
		shell.setFocus();

		Composite composite = new Composite(shell, SWT.NONE);
		composite.setLayout(new BorderLayout(0, 0));

		Composite northComposite = new Composite(composite, SWT.NONE);
		northComposite.setLayoutData(BorderLayout.NORTH);
		northComposite.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		northComposite.setLayout(new GridLayout(3, false));

		GridData gd_leftTitleComposite = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_leftTitleComposite.widthHint = 10;

		Composite leftTitleComposite = new Composite(northComposite, SWT.NONE);
		leftTitleComposite.setLayoutData(gd_leftTitleComposite);
		leftTitleComposite.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));

		Composite titleComposite = new Composite(northComposite, SWT.NONE);
		titleComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		titleComposite.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		titleComposite.setLayout(new GridLayout(1, false));

		Label titleLabel = new Label(titleComposite, SWT.NONE);
		titleLabel.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, true, 1, 1));
		titleLabel.setText("File Open or Save");
		titleLabel.setFont(SWTResourceManager.getFont("Malgun Gothic", 0, SWT.BOLD));
		titleLabel.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));

		Label northSeparatorLabel = new Label(titleComposite, SWT.SEPARATOR | SWT.HORIZONTAL);
		northSeparatorLabel.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, true, 1, 1));

		Label dataTypeLabel = new Label(northComposite, SWT.NONE);
		dataTypeLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		dataTypeLabel.setImage(ResourceManager.getPluginImage(FaqConstant.SYMBOLICNAME, "icons/datatype.png"));
		dataTypeLabel.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));

		Composite centerComposite = new Composite(composite, SWT.NONE);
		centerComposite.setLayoutData(BorderLayout.CENTER);
		centerComposite.setLayout(new GridLayout(1, false));

		Label descriptionLabel = new Label(centerComposite, SWT.NONE);
		descriptionLabel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true, 1, 1));
		descriptionLabel.setText("Open or Save File ?");

		Composite southComposite = new Composite(composite, SWT.NONE);
		southComposite.setLayoutData(BorderLayout.SOUTH);
		southComposite.setLayout(new GridLayout(1, false));

		Label southSeparatorLabel = new Label(southComposite, SWT.SEPARATOR | SWT.HORIZONTAL);
		southSeparatorLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		Composite southButtonComposite = new Composite(southComposite, SWT.NONE);
		southButtonComposite.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, true, true, 1, 1));
		southButtonComposite.setLayout(new GridLayout(3, true));

		Button openButton = new Button(southButtonComposite, SWT.NONE);
		openButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		openButton.setImage(ResourceManager.getPluginImage(FaqConstant.SYMBOLICNAME, "icons/open_16.png"));
		openButton.setText("Open");
		openButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				resultDataMap = new HashMap<String, Object>();
				resultDataMap.put("action", "open");

				shell.dispose();
			}
		});

		Button saveButton = new Button(southButtonComposite, SWT.NONE);
		saveButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		saveButton.setImage(ResourceManager.getPluginImage(FaqConstant.SYMBOLICNAME, "icons/save_16.png"));
		saveButton.setText("Save");
		saveButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				String fileName = (String) parentDataMap.get("fileName");
				String exportPath = openFileDialog(shell, fileName);
				if (exportPath != null) {
					resultDataMap = new HashMap<String, Object>();
					resultDataMap.put("action", "save");
					resultDataMap.put("exportPath", exportPath);

					shell.dispose();
				}
			}
		});

		GridData gd_closeButton = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_closeButton.widthHint = 100;

		Button closeButton = new Button(southButtonComposite, SWT.NONE);
		closeButton.setLayoutData(gd_closeButton);
		closeButton.setText("Close");
		closeButton.setImage(ResourceManager.getPluginImage(FaqConstant.SYMBOLICNAME, "icons/close_16.png"));
		closeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				shell.dispose();
			}
		});
	}

	public void setContents() {
		try {

		} catch (Exception e) {
			e.printStackTrace();

			MessageDialog.openError(getParent(), "Error", e.getMessage() == null ? e.toString() : e.getMessage());
		}
	}

	public void centerToParent(Shell parentShell, Shell childShell) {
		Rectangle parentRectangle = parentShell.getBounds();
		Rectangle childRectangle = childShell.getBounds();
		int x = parentShell.getLocation().x + (parentRectangle.width - childRectangle.width) / 2;
		int y = parentShell.getLocation().y + (parentRectangle.height - childRectangle.height) / 2;
		childShell.setLocation(x, y);
	}
	
	public void openShell(Shell parentShell, Shell childShell) {
		childShell.open();
		childShell.layout();
		Display display = parentShell.getDisplay();
		while (!childShell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}
	
	public String openFileDialog(Shell shell, String fileName) {
		FileDialog fileDialog = new FileDialog(shell, SWT.SAVE);
		fileDialog.setFileName(fileName);
		fileDialog.setOverwrite(true);

		return fileDialog.open();
	}
	
}

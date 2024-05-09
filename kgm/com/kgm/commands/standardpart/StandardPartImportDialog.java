package com.kgm.commands.standardpart;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.kgm.common.dialog.SYMCAbstractDialog;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;

public class StandardPartImportDialog extends SYMCAbstractDialog {
	private Registry registry;
	
	private Label catFolderLbl;
	private Text catFolderTxt;
	private Button catFolderBttn;
	private Label excelLbl;
	private Text excelTxt;
	private Button excelBttn;
	
	
	public StandardPartImportDialog(Shell paramShell) {
		super(paramShell);
		this.registry = Registry.getRegistry(this);
	}

	@Override
	protected Composite createDialogPanel(ScrolledComposite parentScrolledComposite) {
		setDialogTextAndImage(registry.getString("StandardPartsImportDialog.TITLE"), null);
		
		Composite composite = new Composite(parentScrolledComposite, SWT.None);		
		FormLayout formLayout = new FormLayout();
		composite.setLayout(formLayout);
		
		excelLbl = new Label(composite, SWT.None);
		excelLbl.setText(registry.getString("StandardPartsImpor.LABEL.excelLbl"));
		FormData formData = new FormData();
		formData.left = new FormAttachment(0, 5);
		formData.top = new FormAttachment(0, 10);
		excelLbl.setLayoutData(formData);
		
		excelBttn = new Button(composite, SWT.PUSH);
		excelBttn.setText(registry.getString("StandardPartsImpor.LABEL.Button"));
		formData = new FormData();
		formData.right = new FormAttachment(100, -5);
		formData.top = new FormAttachment(excelLbl, 5);
		excelBttn.setLayoutData(formData);
		excelBttn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				FileDialog dialog = new FileDialog(getShell());
				dialog.setFilterExtensions(new String[] {"*.xlsx", "*xls"});
				String fileName = dialog.open();
				if(fileName != null) {
					excelTxt.setText(fileName);
				}
			}
		});
		
		excelTxt = new Text(composite, SWT.SINGLE | SWT.BORDER);
		formData = new FormData();
		formData.left = new FormAttachment(excelLbl, 0, SWT.LEFT);
		formData.right = new FormAttachment(excelBttn, -5);
		formData.top = new FormAttachment(excelBttn, 0, SWT.CENTER);
		excelTxt.setLayoutData(formData);
		excelTxt.setEditable(false);	
		
		catFolderLbl = new Label(composite, SWT.NONE);
		catFolderLbl.setText(registry.getString("StandardPartsImpor.LABEL.catFolderLbl"));
		formData = new FormData();
		formData.left = new FormAttachment(0, 5);
		formData.top = new FormAttachment(excelTxt, 20);
		catFolderLbl.setLayoutData(formData);

		catFolderBttn = new Button(composite, SWT.PUSH);
		catFolderBttn.setText(registry.getString("StandardPartsImpor.LABEL.Button"));
		formData = new FormData();
		formData.right = new FormAttachment(100, -5);
		formData.top = new FormAttachment(catFolderLbl, 5);
		catFolderBttn.setLayoutData(formData);
		catFolderBttn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				DirectoryDialog dialog = new DirectoryDialog(getShell());
				String folderName = dialog.open();
				if(folderName != null) {
					catFolderTxt.setText(folderName);
				}
			}
		});

		catFolderTxt = new Text(composite, SWT.SINGLE | SWT.BORDER);
		formData = new FormData();
		formData.left = new FormAttachment(catFolderLbl, 0, SWT.LEFT);
		formData.right = new FormAttachment(catFolderBttn, -5);
		formData.top = new FormAttachment(catFolderBttn, 0, SWT.CENTER);
		catFolderTxt.setLayoutData(formData);
		catFolderTxt.setEditable(false);
		
		return composite;
	}

	@Override
	protected boolean validationCheck() {
		if(excelTxt.getText().length() == 0) {	
			MessageBox.post(AIFUtility.getActiveDesktop().getShell(), "Please choose the Excel File.", "WARNING", MessageBox.WARNING);
			return false;
		}
		
		if(catFolderTxt.getText().length() == 0) {
			MessageBox.post(getShell(), "Please Choose the CATFile Folder", "WARNING", MessageBox.WARNING);
			return false;
		}
		
		return true;
	}

	@Override
	protected boolean apply() {
		StandardPartImportOperation operation = new StandardPartImportOperation(excelTxt.getText(), catFolderTxt.getText());
		
		try {
			operation.executeOperation();
		} catch (Exception e) {
			e.printStackTrace();
			MessageBox.post(AIFUtility.getActiveDesktop().getShell(), e.toString(), "ERROR", MessageBox.ERROR);
		}
		return true;
	}
}

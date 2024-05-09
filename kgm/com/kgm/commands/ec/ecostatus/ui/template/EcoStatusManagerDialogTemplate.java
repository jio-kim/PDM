package com.kgm.commands.ec.ecostatus.ui.template;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wb.swt.SWTResourceManager;

public class EcoStatusManagerDialogTemplate extends Dialog {

	private CTabFolder tabFolder;

	/**
	 * Create the dialog.
	 * @param parentShell
	 */
	public EcoStatusManagerDialogTemplate(Shell parentShell) {
		super(parentShell);
		setShellStyle(SWT.SHELL_TRIM | SWT.BORDER);
	}
	
	@Override
	protected void configureShell(Shell newShell)
	{
		super.configureShell(newShell);
		newShell.setText("설계변경 현황관리 시스템");
	}

	/**
	 * Create contents of the dialog.
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
		
		//FormToolkit toolkit = new FormToolkit(container.getDisplay());
		CTabItem registerBasicInformTabItem = new CTabItem(tabFolder, SWT.NONE);
		registerBasicInformTabItem.setText("기준 정보 등록");
		
		CTabItem registerChgListTabItem = new CTabItem(tabFolder, SWT.NONE);
		registerChgListTabItem.setText("설계변경리스트 작성");
		
		CTabItem ecoChgListTabItem = new CTabItem(tabFolder, SWT.NONE);
		ecoChgListTabItem.setText("설계변경리스트 관리");
		
		CTabItem ecoChgStatusTabItem = new CTabItem(tabFolder, SWT.NONE);
		ecoChgStatusTabItem.setText("설게변경현황 관리");
		GridData gd_sectionBasic = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_sectionBasic.minimumWidth = 5;
		
		EcoChangeListMgrCompositeTemplate ecoChangeListMgrComposite = new EcoChangeListMgrCompositeTemplate(tabFolder, SWT.NONE);
		ecoChgListTabItem.setControl(ecoChangeListMgrComposite);


		return container;
	}

	/**
	 * Create contents of the button bar.
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
		return new Point(1200, 700);
	}

    public static void main(String[] args)
    {
    	EcoStatusManagerDialogTemplate dialog = new EcoStatusManagerDialogTemplate(null);
        dialog.open();
    }
}

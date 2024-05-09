package com.kgm.commands.ec.ecostatus.ui;

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
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wb.swt.SWTResourceManager;

import com.kgm.commands.ec.ecostatus.model.EcoStatusData;

public class EcoStatusManagerDialog extends Dialog {

	private CTabFolder tabFolder;
	private EcoStdInformListComposite ecoStdInformListComposite = null; // 기준 정보 등록
	private EcoChangeListMgrComposite ecoChangeListMgrComposite = null; // 설계변경 리스트 관리
	private EcoTotalSatusMgrComposite ecoTotalSatusMgrComposite = null; // 설계변경 현황 관리
	private EcoChangeListRegisterComposite ecoChangeListRegisterComposite = null; // 설계변경 리스트 작성

	/**
	 * Create the dialog.
	 * 
	 * @param parentShell
	 */
	public EcoStatusManagerDialog(Shell parentShell) {
		super(parentShell);
		setShellStyle(SWT.SHELL_TRIM | SWT.BORDER);
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("설계변경 현황관리 시스템");
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
		tabFolder.setFont(SWTResourceManager.getFont("맑은 고딕", 10, SWT.NORMAL));
		tabFolder.setTabHeight(25);
		tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		tabFolder.setSelectionBackground(Display.getCurrent().getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT));

		tabFolder.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				if (tabFolder.getSelectionIndex() == 0) {
					// FIXME: 1차분 배포 주석
					if (ecoStdInformListComposite == null)
						return;
					// 초기 데이터 Load
					if (!ecoStdInformListComposite.isInitDataLoad())
						ecoStdInformListComposite.loadInitData();
				} else if (tabFolder.getSelectionIndex() == 1) {
					// FIXME: 1차분 배포 주석
					EcoStatusData selectedRowData = ecoStdInformListComposite.getSelectedRowData();
					if (selectedRowData == null) {
						tabFolder.setSelection(0);
						return;
					}
					ecoChangeListRegisterComposite.loadInitData(selectedRowData);

				} else if (tabFolder.getSelectionIndex() == 2) {
					if (ecoChangeListMgrComposite == null)
						return;
					// 초기 데이터 Load
					if (!ecoChangeListMgrComposite.isInitDataLoad())
						ecoChangeListMgrComposite.loadInitData();
				} else if (tabFolder.getSelectionIndex() == 3) {
					if (ecoTotalSatusMgrComposite == null)
						return;
					// 초기 데이터 Load
					if (!ecoTotalSatusMgrComposite.isInitDataLoad())
						ecoTotalSatusMgrComposite.loadInitData();
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});

		// FormToolkit toolkit = new FormToolkit(container.getDisplay());
		CTabItem registerBasicInformTabItem = new CTabItem(tabFolder, SWT.NONE);
		registerBasicInformTabItem.setFont(SWTResourceManager.getFont("맑은 고딕", 9, SWT.NORMAL));
		registerBasicInformTabItem.setText("기준 정보 등록");

		CTabItem registerChgListTabItem = new CTabItem(tabFolder, SWT.NONE);
		registerChgListTabItem.setText("설계변경리스트 작성");

		CTabItem ecoChgListTabItem = new CTabItem(tabFolder, SWT.NONE);
		ecoChgListTabItem.setText("설계변경리스트 관리");

		CTabItem ecoChgStatusTabItem = new CTabItem(tabFolder, SWT.NONE);
		ecoChgStatusTabItem.setText("설계변경현황 관리");
		GridData gd_sectionBasic = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_sectionBasic.minimumWidth = 5;

		ecoStdInformListComposite = new EcoStdInformListComposite(tabFolder, SWT.NONE);
		// FIXME: 1차분 배포 주석
		registerBasicInformTabItem.setControl(ecoStdInformListComposite);

		ecoChangeListRegisterComposite = new EcoChangeListRegisterComposite(tabFolder, EcoStatusManagerDialog.this, SWT.NONE);
		// FIXME: 1차분 배포 주석
		registerChgListTabItem.setControl(ecoChangeListRegisterComposite);

		ecoChangeListMgrComposite = new EcoChangeListMgrComposite(tabFolder, SWT.NONE);
		ecoChgListTabItem.setControl(ecoChangeListMgrComposite);

		ecoTotalSatusMgrComposite = new EcoTotalSatusMgrComposite(tabFolder, SWT.NONE);
		ecoChgStatusTabItem.setControl(ecoTotalSatusMgrComposite);

		loadInitData();

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
		Button btnClose = createButton(parent, IDialogConstants.OK_ID, "Close", false);
		GridData gd_btnClose = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_btnClose.widthHint = 70;
		btnClose.setLayoutData(gd_btnClose);
		btnClose.setImage(SWTResourceManager.getImage(EcoChangeListMgrComposite.class, "/icons/cancel_16.png"));
	}

	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(1380, 750);
	}

	/**
	 * 초기 Data Load
	 */
	private void loadInitData() {
		// FIXME: 주석 필요
		// tabFolder.setSelection(2);

		Event event = new Event();
		event.item = tabFolder.getSelection();

		tabFolder.notifyListeners(SWT.Selection, event);
	}

	/**
	 * 변경관리 리스트 Composite 를 가져옴
	 * 
	 * @return
	 */
	public EcoChangeListMgrComposite getChangeListMgrComposite() {
		return ecoChangeListMgrComposite;
	}

	/**
	 * TabFolder 가져옴
	 * 
	 * @return
	 */
	public CTabFolder getTabFolder() {
		return tabFolder;
	}

	public static void main(String[] args) {
		EcoStatusManagerDialog dialog = new EcoStatusManagerDialog(null);
		dialog.open();
	}
}

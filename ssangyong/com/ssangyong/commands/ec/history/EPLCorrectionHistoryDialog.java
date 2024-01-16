package com.ssangyong.commands.ec.history;

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import com.ssangyong.commands.ec.dao.CustomECODao;
import com.ssangyong.common.SYMCDateTimeButton;
import com.ssangyong.common.SYMCLOVCombo;
import com.ssangyong.common.SortListenerFactory;
import com.ssangyong.common.dialog.SYMCAbstractDialog;
import com.ssangyong.common.utils.CustomUtil;
import com.ssangyong.dto.SYMCECOStatusData;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCSession;

public class EPLCorrectionHistoryDialog extends SYMCAbstractDialog{
	private TCSession session;
	private Button btnClose;
	private Button btnSearch;
	
	private Text txtECONo;
	private Text txtOwningDept;
	private Text txtOwningUser;
	private SYMCLOVCombo cmbStatus;
	private SYMCDateTimeButton dtCompletedDateFrom;
	private SYMCDateTimeButton dtCompletedDateTo;
	
	private Table resultTable;
	private String[] columnName = new String[] { "ECR No.", "ECI Approval No.", "ECO No.", "ECO Status", "Owning Dept.", "Owning User", "Creation Date", "Submit Date", "Realse Date" };
	private int[] columnSize = new int[] { 80, 80, 80, 80, 160, 120, 120, 100, 100, 100 };
	
	public EPLCorrectionHistoryDialog(Shell parent) {
		super(parent);
		
		setBlockOnOpen(false);
		
		this.session = CustomUtil.getTCSession();
	}

	@Override
	protected boolean apply() {
		return false;
	}

	/** 버튼 변경 */
	protected void createButtonsForButtonBar(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());
		btnClose = new Button(composite, SWT.PUSH);
		btnClose.setText("Close");
		btnClose.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				getShell().close();
			}
		});
	}

	/** Composiste 생성 */
	@Override
	protected Composite createDialogPanel(ScrolledComposite parentScrolledComposite) {
		getShell().setText("EPL Correction History");
		Composite composite = new Composite(parentScrolledComposite, SWT.NONE);
		composite.setLayout(new GridLayout());

		createSearchComposite(composite);
		createSearchResultTable(composite);
		return composite;
	}

	/**
	 * 검색조건 영역
	 * @param composite
	 */
	private void createSearchComposite(Composite composite) {
		Composite cpsSearch = new Composite(composite, SWT.NONE);
		cpsSearch.setLayout(new GridLayout(5, false));
		cpsSearch.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		GridData gdLabelRightAlignment = new GridData();
		gdLabelRightAlignment.horizontalAlignment = SWT.RIGHT;
		
		// ECO No
		Label lblECONo = new Label(cpsSearch, SWT.None);
		lblECONo.setText("ECO No.");
		lblECONo.setLayoutData(gdLabelRightAlignment);
		
		txtECONo = new Text(cpsSearch, SWT.BORDER);
		txtECONo.setLayoutData(new GridData(175, 17));
		
		// Status - Completed로 고정
		Label lblStatus = new Label(cpsSearch, SWT.None);
		lblStatus.setText("ECO Status");
		lblStatus.setLayoutData(gdLabelRightAlignment);
		
		cmbStatus = new SYMCLOVCombo(cpsSearch, "S7_ECO_MATURITY");
		cmbStatus.setLayoutData(new GridData(155, 17));
		cmbStatus.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				System.out.println(cmbStatus.getSelectionIndex());
			}
		});
		String[] saStatus = cmbStatus.getItems();
		for (int inx = 0; inx < saStatus.length; inx++) {
			if (saStatus[inx] != null && !saStatus[inx].equals("") && saStatus[inx].length() > 0 && saStatus[inx].equals("Completed (Completed)")) {
				cmbStatus.select(inx);
				cmbStatus.setEnabled(false);
				break;
			}
		}
		
		
		// Search button
		GridData gdSearchButton = new GridData();
		gdSearchButton.horizontalAlignment = SWT.RIGHT;
		gdSearchButton.widthHint = 80;
		gdSearchButton.heightHint = 23;
		
		btnSearch = new Button(cpsSearch, SWT.PUSH);
		btnSearch.setText("Search");
		btnSearch.setLayoutData(gdSearchButton);
		btnSearch.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				search();
			}
		});
		
		// Owning Dept
		Label lblOwningDept = new Label(cpsSearch, SWT.None);
		lblOwningDept.setText("Owning Dept");
		lblOwningDept.setLayoutData(gdLabelRightAlignment);
		
		txtOwningDept = new Text(cpsSearch, SWT.BORDER);
		txtOwningDept.setLayoutData(new GridData(175, 17));
		
		// Owning User
		Label lblOwningUser = new Label(cpsSearch, SWT.None);
		lblOwningUser.setText("Owning Dept");
		lblOwningUser.setLayoutData(gdLabelRightAlignment);
		
		txtOwningUser = new Text(cpsSearch, SWT.BORDER);
		txtOwningUser.setLayoutData(new GridData(175, 17));
		
		Label lblBlank = new Label(cpsSearch, SWT.None);
		lblBlank.setText("");
		
		// Complated Date
		Label lblCompletedDate = new Label(cpsSearch, SWT.None);
		lblCompletedDate.setText("Completed Date");
		lblCompletedDate.setLayoutData(gdLabelRightAlignment);
		
		dtCompletedDateFrom = new SYMCDateTimeButton(cpsSearch);
		
		Label lblWave = new Label(cpsSearch, SWT.None);
		lblWave.setText("~");
		GridData gdWave = new GridData();
		gdWave.horizontalAlignment = SWT.CENTER;
		lblWave.setLayoutData(gdWave);
		
		dtCompletedDateTo = new SYMCDateTimeButton(cpsSearch);
		
		// Separator
		GridData gdSeparator = new GridData();
		
		Label lblSeparator1 = new Label(cpsSearch, SWT.SEPARATOR | SWT.HORIZONTAL);
		gdSeparator = new GridData(SWT.FILL, SWT.FILL, true, false);
		gdSeparator.horizontalSpan = 5;
		lblSeparator1.setLayoutData(gdSeparator);

		Label lblSeparator2 = new Label(cpsSearch, SWT.SEPARATOR | SWT.HORIZONTAL);
		gdSeparator = new GridData(SWT.FILL, SWT.FILL, true, false);
		gdSeparator.horizontalSpan = 5;
		lblSeparator2.setLayoutData(gdSeparator);
	}

	/**
	 * 검색결과 영역
	 * @param composite
	 */
	private void createSearchResultTable(Composite composite) {
		Composite cpsSearchResultTable = new Composite(composite, SWT.NONE);
		cpsSearchResultTable.setLayout(new GridLayout());

		GridData layoutData = new GridData(GridData.FILL_BOTH);
		layoutData.minimumHeight = 400;
		layoutData.horizontalSpan = 3;
		resultTable = new Table(cpsSearchResultTable, SWT.MULTI | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		resultTable.setHeaderVisible(true);
		resultTable.setLinesVisible(true);
		resultTable.setLayoutData(layoutData);
		resultTable.addMouseListener(new MouseAdapter() {
			public void mouseDoubleClick(MouseEvent e) {
				AIFUtility.getActiveDesktop().getShell().getDisplay().syncExec(new Runnable() {
					public void run() {
						Shell shell = AIFUtility.getActiveDesktop().getShell();
						TableItem[] selectItems = resultTable.getSelection();
						EPLCorrectionHistoryDetailDialog dialog = new EPLCorrectionHistoryDetailDialog(shell, SWT.DIALOG_TRIM | SWT.MIN, (String) selectItems[0].getText(2));
						dialog.open();
					}
				});
			}
		});

		int i = 0;
		for (String value : columnName) {
			TableColumn column = new TableColumn(resultTable, SWT.NONE);
			column.setText(value);
			column.setWidth(columnSize[i]);
			column.addListener(SWT.Selection, SortListenerFactory.getListener(SortListenerFactory.STRING_COMPARATOR));
			i++;
		}
	}

	@Override
	protected boolean validationCheck() {
		return true;
	}
	
	private void search() {
		SYMCECOStatusData searchCondition = new SYMCECOStatusData();

		String sOwningDept = txtOwningDept.getText();
		if (sOwningDept != null && sOwningDept.length() > 0) {
			sOwningDept = sOwningDept.replace("*", "%");
			searchCondition.setOwningTeam(sOwningDept.toUpperCase() + '%');
		}

		String sOwningUser = txtOwningUser.getText();
		if (sOwningUser != null && sOwningUser.length() > 0) {
			sOwningUser = sOwningUser.replace("*", "%");
			searchCondition.setOwningUser(sOwningUser.toUpperCase() + '%');
		}

		String sStatus = cmbStatus.getText();
		if (sStatus != null && sStatus.length() > 0) {
			searchCondition.setEcoStatus(sStatus);
		}

		String sECONo = txtECONo.getText();
		if (sECONo != null && sECONo.length() > 0) {
			sECONo = sECONo.replace("*", "%");
			searchCondition.setEcoNo(sECONo.toUpperCase() + '%');
		}

		String releaseDateFromDate = dtCompletedDateFrom.getYear() + "-" + String.format("%1$02d", (dtCompletedDateFrom.getMonth() + 1)) + "-"
				+ String.format("%1$02d", dtCompletedDateFrom.getDay()) + "";
		searchCondition.setReleaseDateFrom(releaseDateFromDate);

		String releaseDateToDate = dtCompletedDateTo.getYear() + "-" + String.format("%1$02d", (dtCompletedDateTo.getMonth() + 1)) + "-"
				+ String.format("%1$02d", dtCompletedDateTo.getDay()) + "";
		searchCondition.setReleaseDateTo(releaseDateToDate);

		try {
			CustomECODao dao = new CustomECODao();
			ArrayList<SYMCECOStatusData> resultList = dao.searchECOCorrectionHistory(searchCondition);

			if (resultList != null) {
				resultTable.removeAll();
				for (SYMCECOStatusData data : resultList) {
					TableItem item = new TableItem(resultTable, SWT.NONE);
					if (data.getEcrNo() != null)
						item.setText(0, data.getEcrNo());
					if (data.getEciNo() != null)
						item.setText(1, data.getEciNo());
					if (data.getEcoNo() != null)
						item.setText(2, data.getEcoNo());
					if (data.getEcoStatus() != null)
						item.setText(3, data.getEcoStatus());
					if (data.getOwningTeam() != null)
						item.setText(4, data.getOwningTeam());
					if (data.getOwningUser() != null)
						item.setText(5, data.getOwningUser());
					if (data.getCreateDate() != null)
						item.setText(6, data.getCreateDate());
					if (data.getRequestDate() != null)
						item.setText(7, data.getRequestDate());
					if (data.getRealseDate() != null)
						item.setText(8, data.getRealseDate());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

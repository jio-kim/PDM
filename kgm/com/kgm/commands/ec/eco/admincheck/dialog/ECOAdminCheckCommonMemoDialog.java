package com.kgm.commands.ec.eco.admincheck.dialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.kgm.commands.ec.dao.CustomECODao;
import com.kgm.commands.ec.eco.admincheck.view.ECOAdminCheckBasicInfoPanel;
import com.kgm.common.dialog.SYMCAbstractDialog;
import com.kgm.common.utils.SWTUtilities;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;

public class ECOAdminCheckCommonMemoDialog extends SYMCAbstractDialog {
	private Composite cpsParent;
	private Text txtMemo;
	private Button btnSend;
	private ECOAdminCheckBasicInfoPanel pnlECOAdminCheckBasicInfo;
	
	/**
	 * Constructor
	 * 
	 * @param parent
	 * @param iSelectionMode
	 * @param sECONo
	 */
	public ECOAdminCheckCommonMemoDialog(Shell parent, int iSelectionMode, ECOAdminCheckBasicInfoPanel pnlECOAdminCheckBasicInfo) {
		super(parent, SWT.RESIZE | SWT.TITLE | SWT.MODELESS | SWT.DIALOG_TRIM | iSelectionMode);
		setShellStyle(SWT.RESIZE | SWT.TITLE | SWT.MODELESS | SWT.DIALOG_TRIM | iSelectionMode);

		this.pnlECOAdminCheckBasicInfo = pnlECOAdminCheckBasicInfo;
		
		setBlockOnOpen(false);
	}

	/**
	 * Create Dialog Panel
	 */
	@Override
	protected Composite createDialogPanel(ScrolledComposite parentScrolledComposite) {
		this.setDialogTextAndImage("Admin Check Common Memo", null);
		SWTUtilities.skipESCKeyEvent(getShell());
		
		initialize(parentScrolledComposite);
		
		return cpsParent;
	}

	/**
	 * Initialize
	 * 
	 * @param parentScrolledComposite
	 */
	private void initialize(ScrolledComposite parentScrolledComposite) {
		boolean isAdmin = false;
		try {
			String sessionId = ((TCSession)AIFUtility.getDefaultSession()).getUser().getUserId();
//			isAdmin = sessionId.equals("001887");	// 류강하 책임 계정일 경우
			// [SR190326-033][CSH]류강하 책임 or 이종민 팀장 계정일 경우
			if(sessionId.equals("001887") || sessionId.equals("951530")){
				isAdmin = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
			MessageBox.post(e);
		}
		
		setApplyButtonVisible(isAdmin);	// 류강하 책임 계정일 경우에만 Apply하여 Common Memo 수정 가능
		
		cpsParent = new Composite(parentScrolledComposite, SWT.NONE);
		cpsParent.setLayout(new GridLayout(1, true));
		cpsParent.setBackground(new Color(null, 255, 255, 255));
		cpsParent.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		GridData gdMemo = new GridData(GridData.FILL_HORIZONTAL);
		gdMemo.heightHint = 450;
		txtMemo = new Text(cpsParent, SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		txtMemo.setTextLimit(4000);
		txtMemo.setLayoutData(gdMemo);
		txtMemo.setEditable(isAdmin);
		
		GridData gdSend = new GridData(100, 30);
		gdSend.horizontalAlignment = SWT.LEFT;
		btnSend = new Button(cpsParent, SWT.PUSH);
		btnSend.setLayoutData(gdSend);
		btnSend.setText("Send To Memo");
		btnSend.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				sendToBasicInfo();
			}
		});

		setOKButtonVisible(false);
		setApplyButtonVisible(isAdmin);
		load();
	}

	/**
	 * Load
	 */
	private void load() {
		try {
			// 최종 일자로 입력한 Memo를 Load
			CustomECODao dao = new CustomECODao();
			String sMemo = dao.getECOAdminCheckCommonMemo();
			txtMemo.setText(sMemo);
		} catch (Exception e) {
			e.printStackTrace();
			MessageBox.post(e);
		}
	}
	
	/**
	 * 현재 Dialog에 로드된 Memo를 Basic Info의 Memo에 입력
	 */
	private void sendToBasicInfo() {
		String sMemo = txtMemo.getText();
		
		if (sMemo == null || sMemo.equals("") || sMemo.length() == 0) {
			return;
		}
		
		pnlECOAdminCheckBasicInfo.setMemo(sMemo);
	}
	
	/**
	 * Apply
	 */
	@Override
	protected boolean apply() {
		try {
			String sMemo = txtMemo.getText();
			if (sMemo == null || sMemo.equals("") || sMemo.length() == 0) {
				MessageBox.post("Save Fail. Value is null!", "Save", MessageBox.ERROR);
				return false;
			}
			CustomECODao dao = new CustomECODao();
			dao.insertECOAdminCheckCommonMemo(sMemo);
		} catch (Exception e) {
			e.printStackTrace();
			MessageBox.post(e);
			return false;
		}
		
		MessageBox.post("Save Successfully.", "Save", MessageBox.INFORMATION);
		return true;
	}

	/**
	 * Validation Check
	 */
	@Override
	protected boolean validationCheck() {
		return true;
	}
}
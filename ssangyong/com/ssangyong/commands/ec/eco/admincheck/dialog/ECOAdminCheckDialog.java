package com.ssangyong.commands.ec.eco.admincheck.dialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import com.ssangyong.commands.ec.eco.admincheck.operation.ECOAdminCheckOperation;
import com.ssangyong.commands.ec.eco.admincheck.validator.ECOAdminCheckValidator;
import com.ssangyong.commands.ec.eco.admincheck.view.ECOAdminCheckBasicInfoPanel;
import com.ssangyong.commands.ec.eco.admincheck.view.ECOAdminCheckChangeCauseTablePanel;
import com.ssangyong.commands.ec.eco.admincheck.view.ECOAdminCheckEndItemListTablePanel;
import com.ssangyong.common.dialog.SYMCAbstractDialog;
import com.ssangyong.common.utils.SWTUtilities;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.util.MessageBox;

public class ECOAdminCheckDialog extends SYMCAbstractDialog {
	private Composite cpsParent;
	private TCComponentItemRevision ecoRevision;
	
	private ECOAdminCheckChangeCauseTablePanel pnlECOAdminCheckChangeCauseTable;
	private ECOAdminCheckBasicInfoPanel pnlECOAdminCheckBasicInfo;
	private ECOAdminCheckEndItemListTablePanel pnlECOAdminCheckEndItemListTable;
	
	/**
	 * Constructor
	 * 
	 * @param parent
	 * @param iSelectionMode
	 * @param sECONo
	 */
	public ECOAdminCheckDialog(Shell parent, int iSelectionMode, TCComponentItemRevision ecoRevision) {
		super(parent, SWT.RESIZE | SWT.TITLE | SWT.MODELESS | SWT.DIALOG_TRIM | iSelectionMode);
		setShellStyle(SWT.RESIZE | SWT.TITLE | SWT.MODELESS | SWT.DIALOG_TRIM | iSelectionMode);

		setBlockOnOpen(false);

		this.ecoRevision = ecoRevision;
	}

	/**
	 * Create Dialog Panel
	 */
	@Override
	protected Composite createDialogPanel(ScrolledComposite parentScrolledComposite) {
		this.setDialogTextAndImage("Admin Check", null);
		SWTUtilities.skipESCKeyEvent(getShell());
		
		initialize(parentScrolledComposite);
		
		// [SR없음][20151020][jclee] Apply -> OK
		setOKButtonVisible(false);
//		setApplyButtonVisible(false);
		
		return cpsParent;
	}

	/**
	 * Initialize
	 * 
	 * @param parentScrolledComposite
	 */
	private void initialize(ScrolledComposite parentScrolledComposite) {
		cpsParent = new Composite(parentScrolledComposite, SWT.NONE);
		cpsParent.setLayout(new GridLayout(1, true));
		cpsParent.setBackground(new Color(null, 255, 255, 255));
		cpsParent.setLayoutData(new GridData(GridData.FILL_BOTH));

		pnlECOAdminCheckChangeCauseTable = new ECOAdminCheckChangeCauseTablePanel(cpsParent, ecoRevision);
		pnlECOAdminCheckBasicInfo = new ECOAdminCheckBasicInfoPanel(cpsParent, ecoRevision);
		pnlECOAdminCheckEndItemListTable = new ECOAdminCheckEndItemListTablePanel(cpsParent, ecoRevision);

		load();
	}

	/**
	 * Load
	 */
	private void load() {
		try {
			clear();

			pnlECOAdminCheckChangeCauseTable.load();
			pnlECOAdminCheckBasicInfo.load();
			pnlECOAdminCheckEndItemListTable.load();
		} catch (Exception e) {
			e.printStackTrace();
			MessageBox.post(e);
		}
	}

	/**
	 * Clear
	 */
	private void clear() {
		pnlECOAdminCheckChangeCauseTable.clear();
		pnlECOAdminCheckBasicInfo.clear();
		pnlECOAdminCheckEndItemListTable.clear();
	}
	
	/**
	 * Apply Button 클릭 시 Event
	 */
	@Override
	protected void applyPressed() {
		// E/Item List에서 아무것도 입력하지 않은 Row는 삭제
		clearEmptyRow();
		
		super.okPressed();
	}
	
//	/**
//	 * OK Button 클릭 시 Event
//	 */
//	@Override
//	protected void okPressed() {
//		// E/Item List에서 아무것도 입력하지 않은 Row는 삭제
//		clearEmptyRow();
//		
//		super.okPressed();
//	}

	/**
	 * 변경사유, E/Item List에서 아무것도 입력하지 않은 Row는 삭제
	 */
	private void clearEmptyRow() {
		pnlECOAdminCheckChangeCauseTable.clearEmptyRow();
		pnlECOAdminCheckEndItemListTable.clearEmptyRow();
	}

	/**
	 * Validation Check
	 */
	@Override
	protected boolean validationCheck() {
		boolean isValidated = false;
		ECOAdminCheckValidator validator = new ECOAdminCheckValidator(pnlECOAdminCheckChangeCauseTable, pnlECOAdminCheckBasicInfo, pnlECOAdminCheckEndItemListTable);
		
		try {
			// Change Cause Validation
			isValidated = validator.validationCheckChangeCause();
			
			// Property Validation
			isValidated = isValidated ? validator.validationCheckProperties() : false;
			
			// E/Item List Validation
			isValidated = isValidated ? validator.validationCheckEndItemList() : false;
		} catch (Exception e) {
			e.printStackTrace();
			MessageBox.post(e);
		}
		
		return isValidated;
		
	}
	
	/**
	 * Apply
	 */
	@Override
	protected boolean apply() {
		try {
			final ECOAdminCheckOperation operation = new ECOAdminCheckOperation(ecoRevision, pnlECOAdminCheckChangeCauseTable, pnlECOAdminCheckBasicInfo, pnlECOAdminCheckEndItemListTable);
			cpsParent.getDisplay().syncExec(new Runnable() {
				@Override
				public void run() {
					try {
						operation.executeOperation();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
			
			clear();
			load();
		} catch (Exception e) {
			e.printStackTrace();
			MessageBox.post(e);
			return false;
		}
		
		MessageBox.post("Save Successfully.", "Save", MessageBox.INFORMATION);
		return true;
	}
}
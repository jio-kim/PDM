package com.symc.plm.rac.prebom.preospec.dialog;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.kgm.common.remote.DataSet;
import com.kgm.common.remote.SYMCRemoteUtil;
import com.kgm.commands.ec.SYMCECConstant;
import com.kgm.commands.ospec.op.OSpec;
import com.kgm.commands.ospec.op.OpUtil;
import com.kgm.commands.ospec.op.Option;
import com.kgm.common.dialog.SYMCAbstractDialog;
import com.kgm.common.utils.DatasetService;
import com.kgm.common.utils.SWTUtilities;
import com.symc.plm.rac.prebom.preospec.ui.PreOSpecMandatoryTable;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.Markpoint;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;

public class PreOSpecMandatoryDlg extends SYMCAbstractDialog {
	private TCSession session;
	private Composite cpsParent;
	private OSpec ospec;
	private String sOSpecNo;
	private PreOSpecMandatoryTable table;
	private Button btnCopy;
	private Button btnAdd;
	private Button btnDelete;
	
	/**
	 * Constructor
	 * @param cOSpec
	 */
	public PreOSpecMandatoryDlg(Shell parent, TCComponent cOSpec) {
		super(parent);
		
		this.session = (TCSession) AIFUtility.getCurrentApplication().getSession();
		
		setBlockOnOpen(false);
		setShellStyle(SWT.CLOSE | SWT.RESIZE | SWT.MODELESS);
		setApplyButtonVisible(false);
		
		try {
			sOSpecNo = cOSpec.getProperty("item_id") + "-" + cOSpec.getProperty("item_revision_id");
			
			AIFComponentContext[] context = cOSpec.getChildren(SYMCECConstant.ITEM_DATASET_REL);
			TCComponentDataset ds = (TCComponentDataset)context[0].getComponent();
			File[] files = DatasetService.getFiles(ds);
			ospec = OpUtil.getOSpec(files[0]);
			sOSpecNo = ospec.getOspecNo();
		} catch (Exception e) {
			MessageBox.post(e);
		}
	}

	/**
	 * 
	 */
	@Override
	protected Composite createDialogPanel(ScrolledComposite parentScrolledComposite) {
		SWTUtilities.skipESCKeyEvent(getShell());
		getShell().setText("Pre OSpec Mandatory Options");
		
		cpsParent = new Composite(parentScrolledComposite, SWT.None);
        cpsParent.setLayout(new GridLayout());
        cpsParent.setLayoutData(new GridData(GridData.FILL_BOTH));
        
		initUI();
		initTable();
		initButtonsListener();
        
		table.load();
		
		cpsParent.pack();
        return cpsParent;
	}

	/**
	 * Initialize UI
	 */
	private void initUI() {
		// UI Main Composite
		Composite cpsUI = new Composite(cpsParent, SWT.NONE);
		GridLayout glUI = new GridLayout(4, false);
		cpsUI.setLayout(glUI);
		GridData gdUI = new GridData(GridData.FILL_HORIZONTAL);
		cpsUI.setLayoutData(gdUI);
		
		// OSpec Info
		GridData gdOSpecNo = new GridData();
		gdOSpecNo.horizontalAlignment = SWT.LEFT;
		Label lblOSpecNo = new Label(cpsUI, SWT.BOLD);
		lblOSpecNo.setText("OSpec No : " + sOSpecNo);
		lblOSpecNo.setLayoutData(gdOSpecNo);
		
		// Buttons
		GridData gdCopy = new GridData();
		gdCopy.horizontalAlignment = SWT.RIGHT;
		gdCopy.grabExcessHorizontalSpace = true;
		btnCopy = new Button(cpsUI, SWT.PUSH);
		btnCopy.setText("Copy");
		btnCopy.setLayoutData(gdCopy);
		
		GridData gdAdd = new GridData();
		gdAdd.horizontalAlignment = SWT.RIGHT;
		btnAdd = new Button(cpsUI, SWT.PUSH);
		btnAdd.setText("Add");
		btnAdd.setLayoutData(gdAdd);
		
		GridData gdDelete = new GridData();
		gdDelete.horizontalAlignment = SWT.RIGHT;
		btnDelete = new Button(cpsUI, SWT.PUSH);
		btnDelete.setText("Delete");
		btnDelete.setLayoutData(gdDelete);
	}

	/**
	 * Initialize Pre OSpec Table
	 */
	private void initTable() {
		Composite cpsTable = new Composite(cpsParent, SWT.NONE);
		cpsTable.setLayout(new GridLayout());
		cpsTable.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		table = new PreOSpecMandatoryTable(cpsTable, ospec, SWT.NONE);
		
		cpsTable.pack();
		cpsTable.layout();
	}

	/**
	 * 
	 */
	@Override
	protected boolean validationCheck() {
		return validation();
	}

	/**
	 * 
	 */
	@Override
	protected boolean apply() {
		save();
		return false;
	}
	
	/**
	 * 
	 */
	private void initButtonsListener() {
		btnCopy.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				PreOSpecCopyDlg dialog = new PreOSpecCopyDlg(getShell(), table);
				dialog.open();
			}
		});
		
		btnAdd.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				table.addRow();
			}
		});
		
		btnDelete.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				table.delRow();
			}
		});
	}
	
	/**
	 * Validation Check
	 * @return
	 */
	private boolean validation() {
		ArrayList<String> alOptionCategories = new ArrayList<String>();
		ArrayList<String> alOptionValues = new ArrayList<String>();

		ArrayList<Option> optionList = ospec.getOptionList();
		
		for (int inx = 0; inx < optionList.size(); inx++) {
			Option option = optionList.get(inx);
			alOptionCategories.add(option.getOp());
			alOptionValues.add(option.getOpValue());
		}
		
		for (int inx = 0; inx < table.getTable().getItemCount(); inx++) {
			HashMap<String, String> hmMandatoryInfo = table.getMandatoryInfo(inx, true);

			if (!alOptionCategories.contains(hmMandatoryInfo.get("OPTION_CATEGORY"))) {
				MessageBox.post("A invalid category has used.", "Check a Category", MessageBox.ERROR);
				return false;
			}
			
			if (!alOptionValues.contains(hmMandatoryInfo.get("OPTION_VALUE"))) {
				MessageBox.post("A invalid code has used.", "Check a Code", MessageBox.ERROR);
				return false;
			}
			
			String sRemark = hmMandatoryInfo.get("REMARK");
			if (sRemark.indexOf("Available IF") < 0) {
				MessageBox.post("Available IF Condition must be contain in Remark.", "Check a Remark", MessageBox.ERROR);
				return false;
			}
			
			if (sRemark.indexOf("(") < 0 || sRemark.indexOf(")") < 0 ) {
				MessageBox.post("Mandatory Condition must be wrapped by \"( )\" in Remark", "Check a Remark", MessageBox.ERROR);
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * Save
	 */
	private void save() {
		Markpoint mp = null;
		try {
			mp = new Markpoint(session);
			SYMCRemoteUtil remote = new SYMCRemoteUtil();
			int iRowCount = table.getTable().getItemCount();
			
			DataSet ds = new DataSet();
			ds.setString("OSPEC_NO", ospec.getOspecNo());
			
			remote.execute("com.kgm.service.PreOSpecService", "deletePreOSpecMandatoryTrim", ds);
			remote.execute("com.kgm.service.PreOSpecService", "deletePreOSpecMandatoryInfo", ds);
			
			for (int inx = 0; inx < iRowCount; inx++) {
				HashMap<String, String> hmMandatoryInfo = table.getMandatoryInfo(inx, false);
				
				DataSet dsMandatoryInfo = new DataSet();
				Set<String> keySetMandatoryInfo = hmMandatoryInfo.keySet();
				
				Iterator<String> iteratorMandatoryInfo = keySetMandatoryInfo.iterator();
				
				while (iteratorMandatoryInfo.hasNext()) {
					String sKey = iteratorMandatoryInfo.next();
					dsMandatoryInfo.setString(sKey, hmMandatoryInfo.get(sKey));
				}
				
				remote.execute("com.kgm.service.PreOSpecService", "insertPreOSpecMandatoryInfo", dsMandatoryInfo);
				
				ArrayList<HashMap<String, String>> alMandatoryTrim = table.getMandatoryTrim(inx);
				
				for (int jnx = 0; jnx < alMandatoryTrim.size(); jnx++) {
					HashMap<String, String> hmMandatoryTrim = alMandatoryTrim.get(jnx);
					
					DataSet dsMandatoryTrim = new DataSet();
					Set<String> keySetMandatoryTrim = hmMandatoryTrim.keySet();
					
					Iterator<String> iteratorMandatoryTrim = keySetMandatoryTrim.iterator();
					
					while (iteratorMandatoryTrim.hasNext()) {
						String sKey = iteratorMandatoryTrim.next();
						dsMandatoryTrim.setString(sKey, hmMandatoryTrim.get(sKey));
					}
					
					remote.execute("com.kgm.service.PreOSpecService", "insertPreOSpecMandatoryTrim", dsMandatoryTrim);
				}
			}
			
			mp.forget();
		} catch (Exception e) {
			try {
				if (mp != null) {
					mp.rollBack();
				}
			} catch (TCException e1) {
				MessageBox.post(e1);
				e1.printStackTrace();
			}
			e.printStackTrace();
			MessageBox.post(e);
		}
	}
}

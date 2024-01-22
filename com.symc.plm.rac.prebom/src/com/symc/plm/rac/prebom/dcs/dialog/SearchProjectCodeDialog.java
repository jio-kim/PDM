package com.symc.plm.rac.prebom.dcs.dialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.ResourceManager;
import org.eclipse.wb.swt.SWTResourceManager;

import swing2swt.layout.BorderLayout;

import com.symc.plm.rac.prebom.common.CommonConstant;
import com.symc.plm.rac.prebom.common.LOVConstant;
import com.symc.plm.rac.prebom.common.PropertyConstant;
import com.symc.plm.rac.prebom.common.util.SDVLOVUtils;
import com.symc.plm.rac.prebom.dcs.common.DCSCommonUtil;
import com.symc.plm.rac.prebom.dcs.common.DCSTableUtil;
import com.symc.plm.rac.prebom.dcs.common.DCSUIUtil;
import com.teamcenter.rac.kernel.TCComponentGroup;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentPerson;
import com.teamcenter.rac.kernel.TCComponentUser;
import com.teamcenter.rac.util.Registry;
import com.teamcenter.rac.util.controls.SWTComboBox;
import com.teamcenter.soa.client.model.LovValue;

public class SearchProjectCodeDialog extends Dialog {

	private Registry registry;

	private HashMap<String, Object> parentDataMap;
	private HashMap<String, Object> resultDataMap;

	private DCSTableUtil searchProjectCodeTableUtil;

	private Shell shell;

	private Table searchProjectCodeTable;

	private Text searchText;

	private SWTComboBox searchComboBox;

	/**
	 * Create the dialog.
	 * 
	 * @param parent
	 * @param style
	 */
	public SearchProjectCodeDialog(Shell parent, int style) {
		super(parent, style);

		this.registry = Registry.getRegistry(this);
	}

	public SearchProjectCodeDialog(Shell parent, int style, HashMap<String, Object> parentDataMap) {
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
		DCSUIUtil.centerToParent(getParent(), shell);
		DCSUIUtil.openShell(getParent(), shell);

		return resultDataMap;
	}

	/**
	 * Create contents of the dialog.
	 */
	private void createContents() {
		shell = new Shell(getParent(), SWT.DIALOG_TRIM | SWT.RESIZE | SWT.PRIMARY_MODAL);
		shell.setImage(ResourceManager.getPluginImage(CommonConstant.SYMBOLICNAME, "icons/defaultapplication_16.png"));
		shell.setSize(500, 600);
		shell.setText(registry.getString("SearchProjectCode.NAME", "Project Code 검색"));
		shell.setLayout(new FillLayout(SWT.HORIZONTAL));

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
		titleLabel.setText(registry.getString("SearchProjectCode.NAME", "Project Code 검색"));
		titleLabel.setFont(SWTResourceManager.getFont("Malgun Gothic", 0, SWT.BOLD));
		titleLabel.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));

		Label northSeparatorLabel = new Label(titleComposite, SWT.SEPARATOR | SWT.HORIZONTAL);
		northSeparatorLabel.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, true, 1, 1));

		Label dataTypeLabel = new Label(northComposite, SWT.NONE);
		dataTypeLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		dataTypeLabel.setImage(ResourceManager.getPluginImage(CommonConstant.SYMBOLICNAME, "icons/datatype.png"));
		dataTypeLabel.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));

		Composite centerComposite = new Composite(composite, SWT.NONE);
		centerComposite.setLayoutData(BorderLayout.CENTER);
		centerComposite.setLayout(new GridLayout(1, false));

		Group searchGroup = new Group(centerComposite, SWT.NONE);
		searchGroup.setLayout(new GridLayout(3, true));
		searchGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		searchGroup.setText(registry.getString("Search.BUTTON", "검색"));

		GridData gd_searchComboBox = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_searchComboBox.widthHint = 100;

		searchComboBox = new SWTComboBox(searchGroup, SWT.BORDER);
		searchComboBox.setToolTipText(registry.getString("SearchItem.NAME", "검색항목"));
		searchComboBox.getTextField().setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		searchComboBox.setLayoutData(gd_searchComboBox);
		searchComboBox.addItem("Code", "Code");
		searchComboBox.addItem("Name", "Name");
		searchComboBox.setSelectedIndex(0);

		searchText = new Text(searchGroup, SWT.BORDER);
		searchText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		searchText.setFocus();
		searchText.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent event) {
				if (event.keyCode == 13 || event.keyCode == 16777296) {
					searchProjectCodeTableUtil.setTableData(search());
				}
			}
		});

		GridData gd_searchButton = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_searchButton.widthHint = 100;

		Button searchButton = new Button(searchGroup, SWT.NONE);
		searchButton.setImage(ResourceManager.getPluginImage(CommonConstant.SYMBOLICNAME, "icons/executesearch_16.png"));
		searchButton.setLayoutData(gd_searchButton);
		searchButton.setText(registry.getString("Search.BUTTON", "검색"));
		searchButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				searchProjectCodeTableUtil.setTableData(search());
			}
		});

		Group searchResultGroup = new Group(centerComposite, SWT.NONE);
		searchResultGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		searchResultGroup.setText(registry.getString("SearchResult.NAME", "검색결과"));
		searchResultGroup.setLayout(new GridLayout(1, false));

		GridData gd_searchProjectCodeTable = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_searchProjectCodeTable.heightHint = 300;
		gd_searchProjectCodeTable.widthHint = 450;

		searchProjectCodeTable = new Table(searchResultGroup, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
		searchProjectCodeTable.setLayoutData(gd_searchProjectCodeTable);
		searchProjectCodeTable.setHeaderVisible(true);
		searchProjectCodeTable.setLinesVisible(true);
		searchProjectCodeTable.addMouseListener(new MouseAdapter() {
			@SuppressWarnings("unchecked")
			@Override
			public void mouseDoubleClick(MouseEvent event) {
				int selectionIndex = searchProjectCodeTable.getSelectionIndex();
				TableItem tableItem = searchProjectCodeTable.getItem(selectionIndex);
				resultDataMap = (HashMap<String, Object>) tableItem.getData();

				String parentDialogType = (String) parentDataMap.get("parentDialogType");
				if (parentDialogType.equals("create")) {
					String message = isExistItemRevision();
					if (message.isEmpty()) {
						shell.dispose();
					} else {
						resultDataMap = null;

						MessageDialog.openWarning(shell, "Warning", message);
					}
				} else {
					shell.dispose();
				}
			}
		});

		Composite southComposite = new Composite(composite, SWT.NONE);
		southComposite.setLayoutData(BorderLayout.SOUTH);
		southComposite.setLayout(new GridLayout(1, false));

		Label southSeparatorLabel = new Label(southComposite, SWT.SEPARATOR | SWT.HORIZONTAL);
		southSeparatorLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		Composite southButtonComposite = new Composite(southComposite, SWT.NONE);
		southButtonComposite.setLayout(new GridLayout(2, true));
		southButtonComposite.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, true, true, 1, 1));

		Button okButton = new Button(southButtonComposite, SWT.NONE);
		okButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		okButton.setImage(ResourceManager.getPluginImage(CommonConstant.SYMBOLICNAME, "icons/ok_16.png"));
		okButton.setText("Ok");
		okButton.addSelectionListener(new SelectionAdapter() {
			@SuppressWarnings("unchecked")
			@Override
			public void widgetSelected(SelectionEvent event) {
				String parentDialogType = (String) parentDataMap.get("parentDialogType");
				if (parentDialogType.equals("create")) {
					int[] selectionIndices = searchProjectCodeTable.getSelectionIndices();
					if (selectionIndices.length == 0) {
						MessageDialog.openWarning(shell, "Warning", registry.getString("SelectTableRow.MESSAGE", "테이블 행을 선택해주세요."));

						return;
					}

					if (selectionIndices.length > 1) {
						MessageDialog.openWarning(shell, "Warning", String.format(registry.getString("NotMultiSelect.MESSAGE"), "Project Code"));

						return;
					}

					TableItem tableItem = searchProjectCodeTable.getItem(selectionIndices[0]);
					resultDataMap = (HashMap<String, Object>) tableItem.getData();

					String message = isExistItemRevision();
					if (!message.isEmpty()) {
						resultDataMap = null;

						MessageDialog.openWarning(shell, "Warning", message);

						return;
					}
				} else if (parentDialogType.equals("search")) {
					List<HashMap<String, Object>> projectCodeList = new ArrayList<HashMap<String, Object>>();

					int[] selectionIndices = searchProjectCodeTable.getSelectionIndices();
					for (int selectionIndex : selectionIndices) {
						TableItem tableItem = searchProjectCodeTable.getItem(selectionIndex);
						HashMap<String, Object> dataMap = (HashMap<String, Object>) tableItem.getData();
						projectCodeList.add(dataMap);
					}

					resultDataMap = new HashMap<String, Object>();
					resultDataMap.put("projectCodeList", projectCodeList);
				}

				shell.dispose();
			}
		});

		GridData gd_closeButton = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_closeButton.widthHint = 100;

		Button closeButton = new Button(southButtonComposite, SWT.NONE);
		closeButton.setImage(ResourceManager.getPluginImage(CommonConstant.SYMBOLICNAME, "icons/close_16.png"));
		closeButton.setLayoutData(gd_closeButton);
		closeButton.setText("Close");
		closeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				shell.dispose();
			}
		});
	}

	public void setContents() {
		try {
			searchProjectCodeTableUtil = new DCSTableUtil(searchProjectCodeTable, "searchProjectCodeTable");
			searchProjectCodeTableUtil.createTableColumn();
		} catch (Exception e) {
			e.printStackTrace();

			MessageDialog.openError(getParent(), "Error", e.getMessage() == null ? e.toString() : e.getMessage());
		}

		shell.pack();
	}

	public List<HashMap<String, Object>> search() {
		List<HashMap<String, Object>> dataList = new ArrayList<HashMap<String, Object>>();

		try {
			String type = searchComboBox.getTextField().getText();
			String text = searchText.getText().toLowerCase();

			List<LovValue> lovValues = SDVLOVUtils.getLOVValues(LOVConstant.LOV_NAME_PROJECTCODE);
			if (lovValues != null) {
				for (int i = 0; i < lovValues.size(); i++) {
					LovValue lovValue = lovValues.get(i);

					String projectCode = lovValue.getStringValue();
					String projectName = lovValue.getDescription();

					String projectCodeToLowerCase = projectCode.toLowerCase();
					String projectNameToLowerCase = projectName.toLowerCase();

					if (type.equals("Code")) {
						if (!projectCodeToLowerCase.contains(text)) {
							continue;
						}
					} else if (type.equals("Name")) {
						if (!projectNameToLowerCase.contains(text)) {
							continue;
						}
					}

					HashMap<String, Object> dataMap = new HashMap<String, Object>();
					dataMap.put("projectCode", projectCode);
					dataMap.put("projectName", projectName);
					dataList.add(dataMap);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();

			MessageDialog.openError(getParent(), "Error", e.getMessage() == null ? e.toString() : e.getMessage());
		}

		return dataList;
	}

	public String isExistItemRevision() {
		String message = "";

		try {
			TCComponentItemRevision itemRevision = null;

			String documentNo = "";
			String revisionNo = (String) parentDataMap.get(PropertyConstant.ATTR_NAME_ITEMREVID);
			String projectCode = (String) resultDataMap.get("projectCode");
			String systemCode = (String) parentDataMap.get(PropertyConstant.ATTR_NAME_SYSTEMCODE);
			String itemType = (String) parentDataMap.get("itemType");
			if (itemType.equals("SC")) {
				documentNo = itemType + projectCode + systemCode;
				itemRevision = DCSCommonUtil.getItemRevision(documentNo, revisionNo);

			} else if (itemType.equals("DC")) {
				documentNo = itemType + projectCode + systemCode;
				itemRevision = DCSCommonUtil.getItemRevision(documentNo, revisionNo);
			}

			if (itemRevision != null) {
				TCComponentUser owningUser = (TCComponentUser) itemRevision.getReferenceProperty(PropertyConstant.ATTR_NAME_OWNINGUSER);
				TCComponentGroup owningGroup = (TCComponentGroup) itemRevision.getReferenceProperty(PropertyConstant.ATTR_NAME_OWNINGGROUP);
				TCComponentPerson person = (TCComponentPerson) owningUser.getReferenceProperty("person");
				String tel = person.getProperty("PA10");
				String owningUserName = owningUser.getProperty(PropertyConstant.ATTR_NAME_USERNAME);
				String displayGroupName = DCSCommonUtil.getVNetTeamName(owningGroup);

				message = String.format(registry.getString("isExistItem.MESSAGE"), documentNo + "_" + revisionNo, displayGroupName, owningUserName + "(" + tel + ")");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return message;
	}

}

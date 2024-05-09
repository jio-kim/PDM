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

import com.kgm.common.remote.DataSet;
import com.symc.plm.rac.prebom.common.CommonConstant;
import com.symc.plm.rac.prebom.common.PropertyConstant;
import com.symc.plm.rac.prebom.dcs.common.DCSCommonUtil;
import com.symc.plm.rac.prebom.dcs.common.DCSTableUtil;
import com.symc.plm.rac.prebom.dcs.common.DCSUIUtil;
import com.teamcenter.rac.kernel.TCComponentGroup;
import com.teamcenter.rac.kernel.TCComponentGroupMember;
import com.teamcenter.rac.kernel.TCComponentUser;
import com.teamcenter.rac.util.Registry;
import com.teamcenter.rac.util.controls.SWTComboBox;

/**
 * [20160303][ymjang] DCS 검색시 퇴사자도 사용자 검색이 될 수 있도록 개선.
 */
public class SearchUserDialog extends Dialog {

	private Registry registry;

	private HashMap<String, Object> parentDataMap;
	private HashMap<String, Object> resultDataMap;

	private DCSTableUtil searchUserTableUtil;
	private DCSTableUtil addedUserTableUtil;

	private Shell shell;

	private Table searchUserTable;
	private Table addedUserTable;

	private Text searchText;

	private SWTComboBox searchComboBox;

	private Button addButton;
	private Button removeButton;

	/**
	 * Create the dialog.
	 * 
	 * @param parent
	 * @param style
	 */
	public SearchUserDialog(Shell parent, int style) {
		super(parent, style);

		this.registry = Registry.getRegistry(this);
	}

	public SearchUserDialog(Shell parent, int style, HashMap<String, Object> parentDataMap) {
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
		shell.setSize(750, 750);
		shell.setText(registry.getString("SearchUser.NAME", "사용자 검색"));
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
		titleLabel.setText(registry.getString("SearchUser.NAME", "사용자 검색"));
		titleLabel.setFont(SWTResourceManager.getFont("Malgun Gothic", 0, SWT.BOLD));
		titleLabel.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));

		Label northSeparatorLabel = new Label(titleComposite, SWT.SEPARATOR | SWT.HORIZONTAL);
		northSeparatorLabel.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, true, 1, 1));

		Label lblNewLabel = new Label(northComposite, SWT.NONE);
		lblNewLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		lblNewLabel.setImage(ResourceManager.getPluginImage(CommonConstant.SYMBOLICNAME, "icons/datatype.png"));
		lblNewLabel.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));

		Composite centerComposite = new Composite(composite, SWT.NONE);
		centerComposite.setLayoutData(BorderLayout.CENTER);
		centerComposite.setLayout(new GridLayout(1, false));

		Group searchGroup = new Group(centerComposite, SWT.NONE);
		searchGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		searchGroup.setText(registry.getString("Search.BUTTON", "검색"));
		searchGroup.setLayout(new GridLayout(3, true));

		GridData gd_searchComboBox = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_searchComboBox.widthHint = 100;

		searchComboBox = new SWTComboBox(searchGroup, SWT.BORDER);
		searchComboBox.setToolTipText(registry.getString("SearchItem.NAME", "검색항목"));
		searchComboBox.getTextField().setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		searchComboBox.setLayoutData(gd_searchComboBox);
		searchComboBox.addItem(registry.getString("UserId.NAME", "사번"), registry.getString("UserId.NAME", "사번"));
		searchComboBox.addItem(registry.getString("Name.NAME", "이름"), registry.getString("Name.NAME", "이름"));
		searchComboBox.addItem(registry.getString("Department.NAME", "부서"), registry.getString("Department.NAME", "부서"));
		searchComboBox.setSelectedIndex(1);

		searchText = new Text(searchGroup, SWT.BORDER);
		searchText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		searchText.setFocus();
		searchText.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent event) {
				if (event.keyCode == 13 || event.keyCode == 16777296) {
					searchUserTableUtil.setTableData(search());
				}
			}
		});

		GridData gd_searchButton = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_searchButton.widthHint = 100;

		Button searchButton = new Button(searchGroup, SWT.NONE);
		searchButton.setLayoutData(gd_searchButton);
		searchButton.setImage(ResourceManager.getPluginImage(CommonConstant.SYMBOLICNAME, "icons/executesearch_16.png"));
		searchButton.setText(registry.getString("Search.BUTTON", "검색"));
		searchButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				searchUserTableUtil.setTableData(search());
			}
		});

		Group searchResultGroup = new Group(centerComposite, SWT.NONE);
		searchResultGroup.setLayout(new GridLayout(3, false));
		searchResultGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		searchResultGroup.setText(registry.getString("SearchResult.NAME", "검색결과"));

		GridData gd_searchUserTable = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_searchUserTable.heightHint = 400;
		gd_searchUserTable.widthHint = 550;

		searchUserTable = new Table(searchResultGroup, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
		searchUserTable.setLayoutData(gd_searchUserTable);
		searchUserTable.setHeaderVisible(true);
		searchUserTable.setLinesVisible(true);
		searchUserTable.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				addButton.setEnabled(true);
				removeButton.setEnabled(false);
			}
		});
		searchUserTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent event) {
				addItemToTable();
			}
		});

		Composite addRemoveComposite = new Composite(searchResultGroup, SWT.NONE);
		addRemoveComposite.setLayout(new GridLayout(1, false));
		addRemoveComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

		addButton = new Button(addRemoveComposite, SWT.NONE);
		addButton.setEnabled(false);
		addButton.setToolTipText(registry.getString("Add.BUTTON", "추가"));
		addButton.setImage(ResourceManager.getPluginImage(CommonConstant.SYMBOLICNAME, "icons/add_16.png"));
		addButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		addButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				addItemToTable();
			}
		});

		removeButton = new Button(addRemoveComposite, SWT.NONE);
		removeButton.setEnabled(false);
		removeButton.setToolTipText(registry.getString("Remove.BUTTON", "제거"));
		removeButton.setImage(ResourceManager.getPluginImage(CommonConstant.SYMBOLICNAME, "icons/remove_16.png"));
		removeButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		removeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				int[] selectionIndices = addedUserTable.getSelectionIndices();
				for (int i = selectionIndices.length - 1; i >= 0; i--) {
					TableItem tableItem = addedUserTable.getItem(selectionIndices[i]);
					tableItem.dispose();
				}

				refresh();
			}
		});

		GridData gd_addedUserTable = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_addedUserTable.heightHint = 400;
		gd_addedUserTable.widthHint = 80;

		addedUserTable = new Table(searchResultGroup, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
		addedUserTable.setLayoutData(gd_addedUserTable);
		addedUserTable.setHeaderVisible(true);
		addedUserTable.setLinesVisible(true);
		addedUserTable.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				addButton.setEnabled(false);
				removeButton.setEnabled(true);
			}
		});

		Composite southComposite = new Composite(composite, SWT.NONE);
		southComposite.setLayoutData(BorderLayout.SOUTH);
		southComposite.setLayout(new GridLayout(1, false));

		Label southSeparatorLabel = new Label(southComposite, SWT.SEPARATOR | SWT.HORIZONTAL);
		southSeparatorLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		Composite southButtonComposite = new Composite(southComposite, SWT.NONE);
		southButtonComposite.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, true, true, 1, 1));
		southButtonComposite.setLayout(new GridLayout(2, true));

		Button okButton = new Button(southButtonComposite, SWT.NONE);
		okButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		okButton.setText("Ok");
		okButton.setImage(ResourceManager.getPluginImage(CommonConstant.SYMBOLICNAME, "icons/ok_16.png"));
		okButton.addSelectionListener(new SelectionAdapter() {
			@SuppressWarnings("unchecked")
			@Override
			public void widgetSelected(SelectionEvent event) {
				try {
					StringBuilder displayBuilder = new StringBuilder();
					List<TCComponentGroupMember> groupMemberList = new ArrayList<TCComponentGroupMember>();
					List<HashMap<String, Object>> vNetDataList = new ArrayList<HashMap<String, Object>>();

					TableItem[] tableItems = addedUserTable.getItems();
					for (TableItem tableItem : tableItems) {
						HashMap<String, Object> dataMap = (HashMap<String, Object>) tableItem.getData();
						String vNetUserId = (String) dataMap.get("USER_ID");
						//20211119 seho CF-2613 CCN 오류.
						//팀센터 유저일 경우 groupmember를 찾는데 팀센터 유저가 아니면 오류 메시지가 계속 나온다.
						//짜증나니까 오류 메시지 나오기 전에 한번더 체크해서 막아버리자.
						TCComponentGroupMember groupMember = null;
						try
						{
							DCSCommonUtil.getUser(vNetUserId);//팀센터 유저 없으면 exception 발생.
							groupMember = DCSCommonUtil.getGroupMember(vNetUserId);//여기서 오류 발생하면 메시지 창이 뜸.
						} catch (Exception e)
						{
							//팀센터 유저가 없으면 exception이 발생해서 빠진다.
						}
						// [20160303][ymjang] DCS 검색시 퇴사자도 사용자 검색이 될 수 있도록 개선.
						String userName = null;
						if (groupMember != null) {
							TCComponentUser user = groupMember.getUser();
							userName = user.getProperty(PropertyConstant.ATTR_NAME_USERNAME);
						} else {
							userName = (String) dataMap.get("USER_NAME");;
						}
						// ========================================================================
						displayBuilder.append(userName + ", ");
						groupMemberList.add(groupMember);
						vNetDataList.add(dataMap);
					}

					String displayString = displayBuilder.toString();
					if (!displayString.isEmpty()) {
						displayString = displayString.substring(0, displayString.length() - 2);
					}

					resultDataMap = new HashMap<String, Object>();
					resultDataMap.put("displayString", displayString);
					resultDataMap.put("displayString_1", displayString.replaceAll(", ", ";"));
					resultDataMap.put("groupMemberList", groupMemberList);
					resultDataMap.put("vNetDataList", vNetDataList);

					shell.dispose();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		GridData gd_closeButton = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_closeButton.widthHint = 100;

		Button closeButton = new Button(southButtonComposite, SWT.NONE);
		closeButton.setLayoutData(gd_closeButton);
		closeButton.setText("Close");
		closeButton.setImage(ResourceManager.getPluginImage(CommonConstant.SYMBOLICNAME, "icons/close_16.png"));
		closeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				shell.dispose();
			}
		});
	}

	@SuppressWarnings("unchecked")
	public void setContents() {
		try {
			searchUserTableUtil = new DCSTableUtil(searchUserTable, "searchUserTable");
			searchUserTableUtil.createTableColumn();
			searchUserTableUtil.setTableData(search());

			addedUserTableUtil = new DCSTableUtil(addedUserTable, "addedUserTable");
			addedUserTableUtil.createTableColumn();

			if (parentDataMap != null) {
				List<HashMap<String, Object>> vNetDataList = (List<HashMap<String, Object>>) parentDataMap.get("vNetDataList");
				if (vNetDataList != null) {
					for (HashMap<String, Object> dataMap : vNetDataList) {
						//[csh 20180417 DCS 개정 작업중 기존 등록된 공동작성자 중 퇴사자 발생시 오류 발생 방지]
						if(dataMap != null){
							TableItem tableItem = new TableItem(addedUserTable, SWT.NONE);
							tableItem.setText(1, (String) dataMap.get("USER_NAME"));
							tableItem.setData(dataMap);
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();

			MessageDialog.openError(getParent(), "Error", e.getMessage() == null ? e.toString() : e.getMessage());
		}

		shell.pack();
	}

	public List<HashMap<String, Object>> search() {
		List<HashMap<String, Object>> dataList = null;

		try {
			TCComponentGroup loginGroup = DCSCommonUtil.getTCSession().getGroup();
			String vNetTeamCode = loginGroup.getProperty("description");
			String vNetTeamName = DCSCommonUtil.getVNetTeamName(vNetTeamCode);

			String type = searchComboBox.getTextField().getText();
			String text = searchText.getText();

			DataSet dataSet = new DataSet();

			if (type.equals(registry.getString("UserId.NAME", "사번"))) {
				dataSet.put("user_id", text);
			} else if (type.equals(registry.getString("Name.NAME", "이름"))) {
				dataSet.put("user_name", text);
			} else if (type.equals(registry.getString("Department.NAME", "부서"))) {
				dataSet.put("team_name", text);
			}

			if (text.isEmpty()) {
				if (vNetTeamName != null) {
					dataSet.put("team_name", vNetTeamName);
				}
			}

			// [20160303][ymjang] DCS 검색시 퇴사자도 사용자 검색이 될 수 있도록 개선.
			if (parentDataMap != null && parentDataMap.get("use_yn") != null)
				dataSet.put("use_yn", parentDataMap.get("use_yn"));
			else
				dataSet.put("use_yn", null);
				
			dataList = DCSCommonUtil.selectVNetUserList(dataSet);
		} catch (Exception e) {
			e.printStackTrace();

			MessageDialog.openError(getParent(), "Error", e.getMessage() == null ? e.toString() : e.getMessage());
		}

		return dataList;
	}

	@SuppressWarnings("unchecked")
	public void addItemToTable() {
		StringBuilder messageBuilder = new StringBuilder();

		int[] selectionIndices = searchUserTable.getSelectionIndices();
		for (int selectionIndex : selectionIndices) {
			TableItem newTableItem = searchUserTable.getItem(selectionIndex);
			HashMap<String, Object> newDataMap = (HashMap<String, Object>) newTableItem.getData();
			String newUserName = (String) newDataMap.get("USER_NAME");

			if (isAlreadyAdded(newTableItem, addedUserTable)) {
				messageBuilder.append(String.format(registry.getString("AlreadyAdded.MESSAGE") + "\n", newUserName));
			} else {
				TableItem tableItem = new TableItem(addedUserTable, SWT.NONE);
				tableItem.setText(1, newUserName);
				tableItem.setData(newDataMap);
			}
		}

		String message = messageBuilder.toString();
		if (!message.isEmpty()) {
			MessageDialog.openWarning(shell, "Warning", message);
		}

		refresh();
	}

	@SuppressWarnings("unchecked")
	public boolean isAlreadyAdded(TableItem newTableItem, Table oldTable) {
		boolean isAlreadyAdded = false;

		HashMap<String, Object> newDataMap = (HashMap<String, Object>) newTableItem.getData();
		String newUserId = (String) newDataMap.get("USER_ID");

		for (TableItem oldTableItem : oldTable.getItems()) {
			HashMap<String, Object> oldDataMap = (HashMap<String, Object>) oldTableItem.getData();
			String oldUserId = (String) oldDataMap.get("USER_ID");
			if (newUserId.equals(oldUserId)) {
				isAlreadyAdded = true;
			}
		}

		return isAlreadyAdded;
	}

	public void refresh() {
		searchUserTable.deselectAll();
		addedUserTable.deselectAll();

		addButton.setEnabled(false);
		removeButton.setEnabled(false);
	}

}

package com.ssangyong.commands.ec.search;

import java.util.ArrayList;
import java.util.Vector;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
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

import com.ssangyong.common.SortListenerFactory;
import com.ssangyong.common.dialog.SYMCAbstractDialog;
import com.ssangyong.common.interfaces.ResultActionListener;
import com.ssangyong.common.swtsearch.SWTSearchUser;
import com.ssangyong.common.ui.SimpleProgressBar;
import com.ssangyong.common.utils.CustomUtil;
import com.ssangyong.common.utils.SWTUtilities;
import com.ssangyong.common.utils.SYMDisplayUtil;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentUser;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCPreferenceService;
import com.teamcenter.rac.kernel.TCPreferenceService.TCPreferenceLocation;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;

public class ECOSearchDialog extends SYMCAbstractDialog implements SelectionListener {

	private Vector<ResultActionListener> actionVector = new Vector<ResultActionListener>();

	private Text tId;
	private Text tName;
	private SWTSearchUser cpsUser;
	private Button bOwned;
	private Button bWorking;
	private Button bInProcess;
	private Button bComplete;

	private String itemId;
	private String itemName;
	private String owner;
	private boolean ownerCheck;
	private boolean workingCheck;
	private boolean inProcessCheck;
	private boolean completedCheck;

	private Table table;

	private int selection;
	private boolean initSearch;
	private boolean isSearchCompleteECO = false;
	private boolean isStop = false;
	private TCComponentItemRevision[] selectedECO;

	private TCSession session;

	/**
	 * Create the dialog.
	 * 
	 * @param parent
	 * @param style
	 */
	public ECOSearchDialog(Shell parent, int _selection) {
		this(parent, _selection, true);
	}

	/**
	 * Create the dialog.
	 * 
	 * @param parent
	 * @param style
	 */
	public ECOSearchDialog(Shell parent, int _selection, boolean initSearch) {
		this(parent, _selection, initSearch, false);
	}

	/**
	 * Create the dialog. [SR141010-011][jclee][2014.10.24] Complete된 ECO 검색
	 * 가능하도록 수정.
	 * 
	 * @param parent
	 * @param _selection
	 * @param initSearch
	 * @param isSearchCompleteECO
	 *            // 완료된 ECO 까지 함께 검색
	 */
	public ECOSearchDialog(Shell parent, int _selection, boolean initSearch, boolean isSearchCompleteECO) {
		super(parent, SWT.RESIZE | SWT.TITLE | SWT.PRIMARY_MODAL | SWT.DIALOG_TRIM);
		session = CustomUtil.getTCSession();

		selection = _selection;
		this.initSearch = initSearch;
		this.isSearchCompleteECO = isSearchCompleteECO;
		setApplyButtonVisible(false);
		super.create();
	}

	/**
	 * Open the dialog.
	 * 
	 * @return the result
	 * 
	 *         public Object open() { createContents(); shell.open();
	 *         shell.layout(); Display display = getParent().getDisplay(); while
	 *         (!shell.isDisposed()) { if (!display.readAndDispatch()) {
	 *         display.sleep(); } } return result; }
	 */

	/**
	 * Create contents of the dialog.
	 */
	protected Composite createDialogPanel(ScrolledComposite scrolledComposite) {
		getShell().setText("Searching ECO");
		getShell().setImage(com.teamcenter.rac.common.Activator.getDefault().getImage("icons/search_16.png"));
		SWTUtilities.skipKeyEvent(getShell());
		Composite composite = new Composite(scrolledComposite, SWT.NONE);
		composite.setLayout(new GridLayout(5, false));
		
		GridData gdLabel = new GridData(SWT.END, SWT.CENTER, false, false);
		gdLabel.widthHint = 70;
		Label lId = new Label(composite, SWT.RIGHT);
		lId.setText("ECO No.");
		lId.setLayoutData(gdLabel);
		tId = new Text(composite, SWT.BORDER);
//		GridData gdText = new GridData(SWT.FILL, SWT.CENTER, false, false);
		GridData gdText = new GridData(GridData.FILL_HORIZONTAL);
		gdText.verticalAlignment = SWT.CENTER;
		gdText.grabExcessVerticalSpace = false;
		gdText.horizontalSpan = 3;
		gdText.widthHint = 240;
		
		tId.setLayoutData(gdText);
		Button searchButton = new Button(composite, SWT.NONE);
		searchButton.setText("Search");
		searchButton.setImage(com.teamcenter.rac.common.Activator.getDefault().getImage("icons/search_16.png"));
		searchButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				searchAction();
			}
		});
		Label lTitle = new Label(composite, SWT.RIGHT);
		lTitle.setText("ECO Title");
		lTitle.setLayoutData(gdLabel);
		tName = new Text(composite, SWT.BORDER);
		tName.setLayoutData(gdText);
		Label lBlank = new Label(composite, SWT.RIGHT);
		lBlank.setText("");

		/**
		 * [SR141105-021][2014.11.18][jclee] Owning User 검색조건 추가
		 */
		Label lOwningUser = new Label(composite, SWT.RIGHT);
		lOwningUser.setText("Owning User");
		lOwningUser.setLayoutData(gdLabel);

		TCComponentUser currentUser = session.getUser();
		cpsUser = new SWTSearchUser(composite, currentUser);
		cpsUser.setLayoutData(gdText);
		cpsUser.setButtonEnabled(false);
		cpsUser.setTextFieldEnabled(false);
		
		bOwned = new Button(composite, SWT.CHECK);
		bOwned.setSelection(true);
		bOwned.setText("Owned ECO");
		bOwned.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				cpsUser.setButtonEnabled(!bOwned.getSelection());
				cpsUser.setTextFieldEnabled(!bOwned.getSelection());
			}
		});

		Label lStatus = new Label(composite, SWT.RIGHT);
		lStatus.setText("Maturity");
		lStatus.setLayoutData(gdLabel);
		bWorking = new Button(composite, SWT.CHECK);
		bWorking.setSelection(true);
		bWorking.setText("Working");
		bInProcess = new Button(composite, SWT.CHECK);
		bInProcess.setSelection(true);
		bInProcess.setText("In Process");
		bComplete = new Button(composite, SWT.CHECK);

		/**
		 * [SR141010-011][jclee][2014.10.24] Complete된 ECO 검색 가능하도록 수정.
		 */
		// bComplete.setSelection(true);
		bComplete.setSelection(isSearchCompleteECO);
		bComplete.setText("Completed");

		// add key listener
		KeyAdapter keyListener = new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.CR || e.keyCode == SWT.KEYPAD_CR) {
					searchAction();
				}
			}
		};
		tId.addKeyListener(keyListener);
		tName.addKeyListener(keyListener);

		GridData gdSprator = new GridData(SWT.FILL, SWT.CENTER, true, false);
		gdSprator.horizontalSpan = 5;
		Label lSeparator = new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL);
		lSeparator.setLayoutData(gdSprator);

		table = new Table(composite, SWT.BORDER | SWT.FULL_SELECTION | selection);
		GridData gdTable = new GridData(SWT.FILL, SWT.FILL, true, true);
		gdTable.horizontalSpan = 5;
		table.setLayoutData(gdTable);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		TableColumn tblclmnNewColumn = new TableColumn(table, SWT.NONE);
		tblclmnNewColumn.setWidth(70);
		tblclmnNewColumn.setText("ECO No.");
		tblclmnNewColumn.addListener(SWT.Selection, SortListenerFactory.getListener(SortListenerFactory.STRING_COMPARATOR));
		tblclmnNewColumn = new TableColumn(table, SWT.NONE);
		tblclmnNewColumn.setWidth(250);
		tblclmnNewColumn.setText("ECO Title");
		tblclmnNewColumn.addListener(SWT.Selection, SortListenerFactory.getListener(SortListenerFactory.STRING_COMPARATOR));
		tblclmnNewColumn = new TableColumn(table, SWT.NONE);
		tblclmnNewColumn.setWidth(80);
		tblclmnNewColumn.setText("Maturity");
		tblclmnNewColumn.addListener(SWT.Selection, SortListenerFactory.getListener(SortListenerFactory.STRING_COMPARATOR));
		tblclmnNewColumn = new TableColumn(table, SWT.NONE);
		tblclmnNewColumn.setWidth(110);
		tblclmnNewColumn.setText("Owning User");
		tblclmnNewColumn.addListener(SWT.Selection, SortListenerFactory.getListener(SortListenerFactory.STRING_COMPARATOR));

		table.addMouseListener(new MouseAdapter() {
			public void mouseDoubleClick(MouseEvent e) {
				okPressed();
			}
		});

		SYMDisplayUtil.centerToParent(getShell().getDisplay().getActiveShell(), getShell());

		if (initSearch) {
			searchAction();
		}

		return composite;
	}

	/**
	 * [SR141105-021][2014.11.17][jclee] ECO 검색 시 검색조건을 하나라도 추가해야만 검색 가능하도록 수정.
	 * 
	 * @return
	 */
	private boolean validationForSearch() {
		boolean isEmpty = true;

		String sECONo = tId.getText();
		String sECOName = tName.getText();

		if (!(sECONo.length() == 0 || sECONo.isEmpty())) {
			isEmpty = false;
		}

		if (isEmpty && !(sECOName.length() == 0 || sECOName.isEmpty())) {
			isEmpty = false;
		}

		if (isEmpty && bOwned.getSelection()) {
			isEmpty = false;
		}
		
		if (isEmpty && !cpsUser.getUserID().equals("")) {
			isEmpty = false;
		}

		if (isEmpty && !bComplete.getSelection()) {
			if (isEmpty && bWorking.getSelection()) {
				isEmpty = false;
			}
			
			if (isEmpty && bInProcess.getSelection()) {
				isEmpty = false;
			}
		}

		if (isEmpty) {
			MessageBox.post("Input more Search Conditions.", "Search Conditions", MessageBox.WARNING);
		}

		return !isEmpty;
	}

	private void searchAction() {
		table.removeAll();
		isStop = false;

		if (!validationForSearch()) {
			return;
		}

		new Job("Search ECO...") {
			@Override
			protected IStatus run(IProgressMonitor arg0) {
				getShell().getDisplay().syncExec(new Runnable() {
					public void run() {
						itemId = tId.getText().trim();
						itemName = tName.getText().trim();
						owner = cpsUser.getUserID();
						ownerCheck = bOwned.getSelection();
						workingCheck = bWorking.getSelection();
						inProcessCheck = bInProcess.getSelection();
						completedCheck = bComplete.getSelection();
					}
				});
				if (isStop) {
					return new Status(IStatus.OK, "ssangyong", "검색 완료");
				}
				final SimpleProgressBar simpleProgressBar = new SimpleProgressBar();

				try {
					getShell().getDisplay().syncExec(new Runnable() {
						public void run() {
							simpleProgressBar.setLabel("검색중...");
							simpleProgressBar.centerToParent(getShell());
							simpleProgressBar.setAlwaysOnTop(true);
							getShell().setEnabled(false);
							simpleProgressBar.setVisible(true);
						}
					});
					TCComponent[] resultList = searchECO(itemId, itemName, owner, ownerCheck, workingCheck, inProcessCheck, completedCheck);
					if (resultList != null && resultList.length != 0) {
						getShell().getDisplay().syncExec(new Runnable() {
							public void run() {
								simpleProgressBar.setLabel("로딩중...");
							}
						});
						loadingData(resultList);
						getShell().getDisplay().syncExec(new Runnable() {
							public void run() {
								simpleProgressBar.setVisible(false);
							}
						});
					} else {
						getShell().getDisplay().syncExec(new Runnable() {
							public void run() {
								simpleProgressBar.setVisible(false);
								MessageBox.post(getShell(), "No Searching Result.", "ECO Search", MessageBox.INFORMATION);
							}
						});
					}
				} catch (final Exception e1) {
					e1.printStackTrace();
					getShell().getDisplay().syncExec(new Runnable() {
						public void run() {
							MessageBox.post(getShell(), e1, true);
						}
					});
				} finally {
					getShell().getDisplay().syncExec(new Runnable() {
						public void run() {
							if (simpleProgressBar != null) {
								simpleProgressBar.setVisible(false);
								simpleProgressBar.dispose();
							}
						}
					});
				}

				getShell().getDisplay().syncExec(new Runnable() {
					public void run() {
						getShell().setEnabled(true);
					}
				});
				return new Status(IStatus.OK, "ssangyong", "Completed");
			}
		}.schedule();
	}

	public TCComponent[] searchECO(String itemId, String itemName, String owner, boolean ownerCheck, boolean workingCheck, boolean inProcessCheck, boolean completeCheck) throws Exception {
		// itemId = itemId.toUpperCase().replaceAll("\\*", "");
		TCSession session = CustomUtil.getTCSession();
		ArrayList<String> entry = new ArrayList<String>();
		ArrayList<String> value = new ArrayList<String>();
		String queryName = "__SYMC_S7_ECO_Revision";
		if (!itemId.equals("")) {
			entry.add("item_id");
			value.add(itemId);
		}
		if (!itemName.equals("")) {
			entry.add("object_desc");
			value.add(itemName);
		}
		if (ownerCheck) {
			entry.add("userid");
			value.add(session.getUser().getUserId());
		} else {
			if (!owner.equals("")) {
				entry.add("userid");
				value.add(owner);
			}
		}
		if (workingCheck || inProcessCheck || completeCheck) {
			TCPreferenceService preferenceService = session.getPreferenceService();
			//String searchSeperator = preferenceService.getString(TCPreferenceService.TC_preference_all, "WSOM_find_list_separator");
//			String searchSeperator = preferenceService.getStringValueAtLocation("WSOM_find_list_separator", TCPreferenceLocation.convertLocationFromLegacy(TCPreferenceService.TC_preference_all));
			String searchSeperator = preferenceService.getStringValue("WSOM_find_list_separator");
			if (CustomUtil.isEmpty(searchSeperator)) {
				searchSeperator = ";";
			}
			entry.add("maturity");
			String strMaturityCondition = "";
			if (workingCheck) {
				strMaturityCondition += "In Work";
			}
			if (inProcessCheck) {
				/**
				 * [SR141010-011][jclee][2014.10.24] Complete된 ECO 검색 가능하도록 수정.
				 * [SR141105-021][jclee][2014.11.17] In Progress 선택 시 Approved
				 * 상태의 ECO도 검색하도록 수정.
				 */
				// String strProgressCondition =
				// "In Review1"+searchSeperator+"In Review2"+searchSeperator+"In Approval"+searchSeperator+"Completed";
				// String strProgressCondition =
				// "In Review1"+searchSeperator+"In Review2"+searchSeperator+"In Approval";
				String strProgressCondition = "In Review1" + searchSeperator + "In Review2" + searchSeperator + "In Approval" + searchSeperator + "Approved";
				strMaturityCondition += (!"".equals(strMaturityCondition)) ? searchSeperator + strProgressCondition : strProgressCondition;
			}
			if (completeCheck) {
				/**
				 * [SR141010-011][jclee][2014.10.24] Complete된 ECO 검색 가능하도록 수정.
				 */
				// strMaturityCondition +=
				// (!"".equals(strMaturityCondition))?searchSeperator+"Released":"Released";
				strMaturityCondition += (!"".equals(strMaturityCondition)) ? searchSeperator + "Completed" : "Completed";
			}
			value.add(strMaturityCondition);
		}

		/**
		 * [SR141105-021][jclee][2014.11.17] ECO No순으로 정렬
		 */
		TCComponent[] comps = CustomUtil.queryComponent(queryName, entry.toArray(new String[entry.size()]), value.toArray(new String[value.size()]));
		for(int j=0; j<comps.length;j++)
		{
			for (int i=j+1 ; i<comps.length; i++)
			{
		    	if(comps[i].getProperty("item_id").compareTo(comps[j].getProperty("item_id"))<0)
		        {
		        	TCComponent temp= comps[j];
					comps[j]= comps[i]; 
					comps[i]=temp;
		        }
			}
		}

		return comps;
	}

	private void loadingData(TCComponent[] resultList) throws TCException {
		getShell().getDisplay().syncExec(new Runnable() {
			public void run() {
				table.removeAll();
			}
		});
		for (int i = 0; resultList != null && i < resultList.length; i++) {
			final TCComponent ecoRev = resultList[i];
			final String id = ecoRev.getProperty("item_id");
			final String title = ecoRev.getProperty("object_desc");
			final String status = ecoRev.getProperty("s7_ECO_MATURITY");
			final String user = ecoRev.getProperty("owning_user");

			getShell().getDisplay().syncExec(new Runnable() {
				public void run() {
					TableItem rowItem = new TableItem(table, SWT.NONE);
					rowItem.setText(0, id);
					rowItem.setText(1, title);
					rowItem.setText(2, status);
					rowItem.setText(3, user);
					rowItem.setData(ecoRev);
				}
			});
		}
	}

	public void addActionResultListener(ResultActionListener resultActionListener) {
		actionVector.add(resultActionListener);
	}

	@Override
	public void widgetSelected(SelectionEvent e) {
		if (e.getSource() == okButton) {
			apply();
		}
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
	}

	@Override
	protected boolean validationCheck() {
		return true;
	}

	@Override
	protected boolean apply() {
		TableItem[] selectRows = table.getSelection();
		if (selectRows == null || selectRows.length == 0) {
			MessageBox.post(getShell(), "Select ECO.", "ECO Search", MessageBox.INFORMATION);
			return false;
		}
		for (int i = 0; i < actionVector.size(); i++) {
			ResultActionListener resultActionListener = actionVector.get(i);
			resultActionListener.ResultAction();
		}
		selectedECO = new TCComponentItemRevision[selectRows.length];
		int i = 0;
		for (TableItem rowItem : selectRows) {
			selectedECO[i++] = (TCComponentItemRevision) rowItem.getData();
		}

		return true;
	}

	public TCComponentItemRevision[] getSelectctedECO() {
		return selectedECO;
	}

	/**
	 * Maturity Check Buttons Enabled
	 * 
	 * @method setAllMaturityButtonsEnabled
	 * @date 2013. 3. 29.
	 * @param
	 * @return void
	 * @exception
	 * @throws
	 * @see
	 */
	public void setAllMaturityButtonsEnabled(boolean enabled) {
		bWorking.setEnabled(enabled);
		bInProcess.setEnabled(enabled);
		bComplete.setEnabled(enabled);
	}

	public void setBWorkingSelect(boolean enabled) {
		bWorking.setSelection(enabled);
	}

	public void setBInProcessSelect(boolean enabled) {
		bInProcess.setSelection(enabled);
	}

	public void setBCompleteSelect(boolean enabled) {
		bComplete.setSelection(enabled);
	}

	public void create() {
	}
	/**
	 * Owning User 정보 초기화
	 */
	public void clearOwningUser(){
		cpsUser.getTxtDisplay().setText("");
		cpsUser.setUserID("");
		cpsUser.setButtonEnabled(true);
		bOwned.setSelection(false);
	}
}
package com.symc.plm.rac.prebom.prebom.dialog.preccn;

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

import com.kgm.common.SortListenerFactory;
import com.kgm.common.dialog.SYMCAbstractDialog;
import com.kgm.common.interfaces.ResultActionListener;
import com.kgm.common.swtsearch.SWTSearchUser;
import com.kgm.common.ui.SimpleProgressBar;
import com.kgm.common.utils.CustomUtil;
import com.kgm.common.utils.SWTUtilities;
import com.kgm.common.utils.SYMDisplayUtil;
import com.symc.plm.rac.prebom.common.PropertyConstant;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentUser;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;

public class CCNSearchDialog extends SYMCAbstractDialog implements SelectionListener {

	private Vector<ResultActionListener> actionVector = new Vector<ResultActionListener>();

	private Text tId;
	private Text tName;
	private SWTSearchUser cpsUser;
	private Button bOwned;

	private String itemId;
	private String itemName;
	private String owner;
	private boolean ownerCheck;

	private Table table;

	private int selection;
	private boolean initSearch;
	private boolean isStop = false;
	private TCComponentItemRevision[] selectedCCN;

	private TCSession session;

	/**
	 * Create the dialog.
	 * 
	 * @param parent
	 * @param style
	 */
	public CCNSearchDialog(Shell parent, int _selection) {
		this(parent, _selection, true);
	}

	/**
	 * Create the dialog. [SR141010-011][jclee][2014.10.24] Complete된 ECO 검색
	 * 가능하도록 수정.
	 * 
	 * @param parent
	 * @param _selection
	 * @param initSearch
	 */
	public CCNSearchDialog(Shell parent, int _selection, boolean initSearch) {
		super(parent, SWT.RESIZE | SWT.TITLE | SWT.PRIMARY_MODAL | SWT.DIALOG_TRIM);
		session = CustomUtil.getTCSession();

		selection = _selection;
		this.initSearch = initSearch;
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
		getShell().setText("Searching CCN");
		getShell().setImage(com.teamcenter.rac.common.Activator.getDefault().getImage("icons/search_16.png"));
		SWTUtilities.skipKeyEvent(getShell());
		Composite composite = new Composite(scrolledComposite, SWT.NONE);
		composite.setLayout(new GridLayout(5, false));
		
		GridData gdLabel = new GridData(SWT.END, SWT.CENTER, false, false);
		gdLabel.widthHint = 70;
		Label lId = new Label(composite, SWT.RIGHT);
		lId.setText("CCN No.");
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
		lTitle.setText("CCN Title");
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
		bOwned.setText("Owned CCN");
		bOwned.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				cpsUser.setButtonEnabled(!bOwned.getSelection());
				cpsUser.setTextFieldEnabled(!bOwned.getSelection());
			}
		});

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
		tblclmnNewColumn.setWidth(140);
		tblclmnNewColumn.setText("CCN No.");
		tblclmnNewColumn.addListener(SWT.Selection, SortListenerFactory.getListener(SortListenerFactory.STRING_COMPARATOR));
		tblclmnNewColumn = new TableColumn(table, SWT.NONE);
		tblclmnNewColumn.setWidth(250);
		tblclmnNewColumn.setText("CCN Title");
		tblclmnNewColumn.addListener(SWT.Selection, SortListenerFactory.getListener(SortListenerFactory.STRING_COMPARATOR));
		tblclmnNewColumn = new TableColumn(table, SWT.NONE);
		tblclmnNewColumn.setWidth(80);
		tblclmnNewColumn.setText("Maturity");
		tblclmnNewColumn.addListener(SWT.Selection, SortListenerFactory.getListener(SortListenerFactory.STRING_COMPARATOR));
		tblclmnNewColumn = new TableColumn(table, SWT.NONE);
		tblclmnNewColumn.setWidth(130);
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

		new Job("Search CCN...") {
			@Override
			protected IStatus run(IProgressMonitor arg0) {
				getShell().getDisplay().syncExec(new Runnable() {
					public void run() {
						itemId = tId.getText().trim();
						itemName = tName.getText().trim();
						owner = cpsUser.getUserID();
						ownerCheck = bOwned.getSelection();
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
					TCComponent[] resultList = searchCCN(itemId, itemName, owner, ownerCheck);
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
								MessageBox.post(getShell(), "No Searching Result.", "CCN Search", MessageBox.INFORMATION);
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

	public TCComponent[] searchCCN(String itemId, String itemName, String owner, boolean ownerCheck) throws Exception {
		// itemId = itemId.toUpperCase().replaceAll("\\*", "");
		TCSession session = CustomUtil.getTCSession();
		ArrayList<String> entry = new ArrayList<String>();
		ArrayList<String> value = new ArrayList<String>();
		String queryName = "__SYMC_S7_PreCCN_Revision";
		if (!itemId.equals("")) {
			entry.add(PropertyConstant.ATTR_NAME_ITEMID);
			value.add(itemId);
		}
		if (!itemName.equals("")) {
			entry.add(PropertyConstant.ATTR_NAME_ITEMDESC);
			value.add(itemName);
		}
		if (ownerCheck) {
			entry.add(PropertyConstant.ATTR_NAME_USERID);
			value.add(session.getUser().getUserId());
		} else {
			if (!owner.equals("")) {
				entry.add(PropertyConstant.ATTR_NAME_USERID);
				value.add(owner);
			}
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
			final String id = ecoRev.getProperty(PropertyConstant.ATTR_NAME_ITEMID);
			final String title = ecoRev.getProperty(PropertyConstant.ATTR_NAME_ITEMDESC);
			final String status = ecoRev.getProperty("s7_ECO_MATURITY");
			final String user = ecoRev.getProperty(PropertyConstant.ATTR_NAME_OWNINGUSER);

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
			MessageBox.post(getShell(), "Select CCN.", "CCN Search", MessageBox.INFORMATION);
			return false;
		}
		for (int i = 0; i < actionVector.size(); i++) {
			ResultActionListener resultActionListener = actionVector.get(i);
			resultActionListener.ResultAction();
		}
		selectedCCN = new TCComponentItemRevision[selectRows.length];
		int i = 0;
		for (TableItem rowItem : selectRows) {
			selectedCCN[i++] = (TCComponentItemRevision) rowItem.getData();
		}

		return true;
	}

	public TCComponentItemRevision[] getSelectctedECO() {
		return selectedCCN;
	}

	public void create() {
	}
}
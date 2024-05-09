package com.kgm.common.swtsearch;

import java.util.HashMap;
import java.util.Vector;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import swing2swt.layout.BorderLayout;
import swing2swt.layout.FlowLayout;

import com.kgm.common.SortListenerFactory;
import com.kgm.common.interfaces.ResultActionListener;
import com.kgm.common.ui.SimpleProgressBar;
import com.kgm.common.utils.CustomUtil;
import com.kgm.common.utils.SYMDisplayUtil;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.util.MessageBox;

@SuppressWarnings("unused")
public class SearchItemDialog extends Dialog implements SelectionListener {
	
	private boolean action = false;
	private Vector<ResultActionListener> actionVector = new Vector<ResultActionListener>();
	private Object result;
	private Shell shell;
	private Text idText;
	private Table table;
	private int selection;
	private String idString = "";
	private String nameString = "";
	private String currentUserName = "";
	private boolean isStop = false;
	private Text nameText;
	private Button okButton;
	private Button ownCheckButton;
    private Button chk2DButton;
	private Button chk3DButton;
	
	
	private HashMap<String, TCComponent> resultMap;
	
	private String strItemType;

	/**
	 * Create the dialog.
	 * 
	 * @param parent
	 * @param style
	 */
	public SearchItemDialog(Shell parent, int _selection, String strItemType) {
		super(parent);
		selection = _selection;
		setText("Search Item("+strItemType+")");
		this.strItemType = strItemType;

	}

	/**
	 * Open the dialog.
	 * 
	 * @return the result
	 */
	public Object open() {
		createContents();
		shell.open();
		shell.layout();
		Display display = getParent().getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return result;
	}

	/**
	 * Create contents of the dialog.
	 */
	private void createContents() {
		shell = new Shell(getParent(), SWT.RESIZE | SWT.TITLE | SWT.PRIMARY_MODAL | SWT.DIALOG_TRIM);
		shell.setImage(com.teamcenter.rac.common.Activator.getDefault().getImage("icons/search_16.png"));
		shell.setSize(494, 338);
		SYMDisplayUtil.centerToParent(getParent().getShell(), shell);
		shell.setText(getText());
		shell.setLayout(new BorderLayout(0, 5));
		Composite composite = new Composite(shell, SWT.NONE);
		composite.setLayoutData(BorderLayout.NORTH);
		composite.setLayout(null);
		Label idLabel = new Label(composite, SWT.RIGHT);
		idLabel.setBounds(5, 10, 36, 12);
		idLabel.setText("ID");
		idText = new Text(composite, SWT.BORDER);
		idText.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.CR || e.keyCode == SWT.KEYPAD_CR) {
					searchAction();
				}
			}
		});
		idText.setBounds(46, 7, 163, 18);

		Label nameLabel = new Label(composite, SWT.RIGHT);
		nameLabel.setBounds(5, 30, 36, 12);
		nameLabel.setText("Name");

		nameText = new Text(composite, SWT.BORDER);
		nameText.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.CR || e.keyCode == SWT.KEYPAD_CR) {
					searchAction();
				}
			}
		});
		nameText.setBounds(46, 27, 163, 18);
		
		ownCheckButton = new Button(composite, SWT.CHECK );
		ownCheckButton.setSelection(false);
		ownCheckButton.setText("Owned Item");
		ownCheckButton.setBounds(220, 27, 100, 18);

		Button searchButton = new Button(composite, SWT.NONE);
		searchButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				searchAction();
			}
		});
		searchButton.setBounds(425, 27, 40, 25);
		searchButton.setText("검색");
		table = new Table(shell, SWT.BORDER | SWT.FULL_SELECTION | selection);
		table.setLayoutData(BorderLayout.CENTER);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		TableColumn tblclmnNewColumn = new TableColumn(table, SWT.NONE);
		tblclmnNewColumn.setWidth(106);
		tblclmnNewColumn.setText("ID");
		tblclmnNewColumn.addListener(SWT.Selection, SortListenerFactory.getListener(SortListenerFactory.STRING_COMPARATOR));
		TableColumn tblclmnNewColumn_1 = new TableColumn(table, SWT.NONE);
		tblclmnNewColumn_1.setWidth(75);
		tblclmnNewColumn_1.setText("소유자");
		tblclmnNewColumn_1.addListener(SWT.Selection, SortListenerFactory.getListener(SortListenerFactory.STRING_COMPARATOR));
		TableColumn tblclmnNewColumn_2 = new TableColumn(table, SWT.NONE);
		tblclmnNewColumn_2.setWidth(280);
		tblclmnNewColumn_2.setText("이름");
		tblclmnNewColumn_2.addListener(SWT.Selection, SortListenerFactory.getListener(SortListenerFactory.STRING_COMPARATOR));
		table.addMouseListener(new MouseAdapter() {
			public void mouseDoubleClick(MouseEvent e) {
				okProcess();
			}
		});
		Composite composite_1 = new Composite(shell, SWT.NONE);
		composite_1.setLayoutData(BorderLayout.SOUTH);
		composite_1.setLayout(new FlowLayout(FlowLayout.RIGHT, 5, 0));
		
//		Composite composite_3 = new Composite(composite_1, SWT.NONE);
//		
//		Label cadTypeLabel = new Label(composite_3, SWT.None);
//		cadTypeLabel.setText("Select Cad Type To Copy");
//		cadTypeLabel.setBounds(0, 3, 145, 22);
//		
//		chk2DButton = new Button(composite_3, SWT.CHECK );
//		chk2DButton.setSelection(false);
//		chk2DButton.setText("2D");
//		chk2DButton.setBounds(145, 0, 33, 22);
//		
//		chk3DButton = new Button(composite_3, SWT.CHECK );
//		chk3DButton.setSelection(false);
//		chk3DButton.setText("3D");
//		chk3DButton.setBounds(185, 0, 60, 22);
		
		
		Composite composite_2 = new Composite(composite_1, SWT.NONE);
		okButton = new Button(composite_2, SWT.NONE);
		okButton.addSelectionListener(this);
		okButton.setBounds(0, 0, 77, 24);
		okButton.setText("확인");
		Button closeButton = new Button(composite_2, SWT.NONE);
		closeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				action = false;
				shell.close();
			}
		});
		closeButton.setBounds(82, 0, 77, 24);
		closeButton.setText("닫기");
		Label blank = new Label(composite_2, SWT.None);
		blank.setBounds(0, 25, 77, 5);
		
	}

	private void searchAction() {
		resultMap = new HashMap<String, TCComponent>();
		isStop = false;
		
		idString = idText.getText().trim();
		nameString = nameText.getText().trim();
		
		currentUserName = "";
		if( this.ownCheckButton.getSelection() )
			currentUserName = CustomUtil.getTCSession().getUserName();
		
		if( CustomUtil.isEmpty(idString) && CustomUtil.isEmpty(nameString) && !ownCheckButton.getSelection() )
		{
			MessageBox.post(shell, "검색조건을 입력하세요.", "경고", MessageBox.WARNING);
			return;
		}
		
		if ( (("*".equals(idString) &&  "".equals(nameString) ) || ("".equals(idString) &&  "*".equals(nameString) ) || ("*".equals(idString) &&  "*".equals(nameString) )  )  && !ownCheckButton.getSelection())
		{
			MessageBox.post(shell, "'*' 검색값은 허용하지 않습니다.", "경고", MessageBox.WARNING);
			return;
		}
		
		
		new Job("검색") {
			@Override
			protected IStatus run(IProgressMonitor arg0) {


				if (isStop) {
					return new Status(IStatus.OK, "ssangyong", "검색 완료");
				}
				final SimpleProgressBar simpleProgressBar = new SimpleProgressBar();
				try {
					shell.getDisplay().syncExec(new Runnable() {
						public void run() {
							simpleProgressBar.setLabel("검색중...");
							simpleProgressBar.centerToParent(shell);
							simpleProgressBar.setAlwaysOnTop(true);
							shell.setEnabled(false);
							simpleProgressBar.setVisible(true);
						}
					});
					TCComponent[] resultList = searchId(idString, nameString, currentUserName);
					if (resultList != null && resultList.length != 0) {
						shell.getDisplay().syncExec(new Runnable() {
							public void run() {
								simpleProgressBar.setLabel("로딩중...");
							}
						});
						loadingData(resultList);
						shell.getDisplay().syncExec(new Runnable() {
							public void run() {
								simpleProgressBar.setVisible(false);
							}
						});
					} else {
						shell.getDisplay().syncExec(new Runnable() {
							public void run() {
								simpleProgressBar.setVisible(false);
								MessageBox.post(shell, "검색 결과가 없습니다.", "경고", MessageBox.WARNING);
							}
						});
					}
				} catch (final Exception e1) {
					e1.printStackTrace();
					shell.getDisplay().syncExec(new Runnable() {
						public void run() {
							MessageBox.post(shell, e1, true);
						}
					});
				} finally {
					shell.getDisplay().syncExec(new Runnable() {
						public void run() {
							if (simpleProgressBar != null) {
								simpleProgressBar.setVisible(false);
								simpleProgressBar.dispose();
							}
						}
					});
				}

				shell.getDisplay().syncExec(new Runnable() {
					public void run() {
						shell.setEnabled(true);
					}
				});
				return new Status(IStatus.OK, "ssangyong", "검색 완료");
			}

		}.schedule();
	}

	public TCComponent[] searchId(String itemId, String itemName, String currentUserName) throws Exception {

		if(CustomUtil.isEmpty(itemId))
			itemId = "*";
		
		if(CustomUtil.isEmpty(itemName))
			itemName = "*";
		
		if(CustomUtil.isEmpty(currentUserName))
			currentUserName = "*";

		TCComponent[] comps = CustomUtil.queryComponent("Item...", new String[] { "ItemID", "Name", "Type", "OwningUser"  }, new String[] {
				itemId, itemName, this.strItemType, currentUserName  });
		return comps;
	}

	private void loadingData(TCComponent[] resultList) throws TCException {
		shell.getDisplay().syncExec(new Runnable() {
			public void run() {
				table.removeAll();
			}
		});
		for (int i = 0; resultList != null && i < resultList.length; i++) {
			TCComponent rowMap = resultList[i];
			final String id = rowMap.getProperty("item_id");
			final String rev = rowMap.getProperty("owning_user");
			final String name = rowMap.getProperty("object_name");
			resultMap.put(id, rowMap);

			shell.getDisplay().syncExec(new Runnable() {
				public void run() {

					TableItem rowItem = new TableItem(table, SWT.NONE);
					rowItem.setText(0, id);
					rowItem.setText(1, rev);
					rowItem.setText(2, name);
				}
			});
		}
	}

	public boolean isOK() {
		return action;
	}

	public Button getOkButton() {
		return okButton;
	}

	public Table getTable() {
		return table;
	}

	public HashMap<String, TCComponent> getResultMap() {
		return resultMap;
	}

	public void addActionResultListener(ResultActionListener resultActionListener) {
		actionVector.add(resultActionListener);
	}

	@Override
	public void widgetSelected(SelectionEvent e) {
		if (e.getSource() == okButton) {
			okProcess();
		}
	}
	
	private void okProcess() {
		TableItem[] selectRows = table.getSelection();
		if (selectRows == null || selectRows.length == 0) {
			MessageBox.post(shell, "검색 결과를 선택하여 주십시오.", "경고", MessageBox.WARNING);
			return;
		}
		for (int i = 0; i < actionVector.size(); i++) {
			ResultActionListener resultActionListener = actionVector.get(i);
			resultActionListener.ResultAction();
		}
		TCComponentItem[] selectedItems = new TCComponentItem[selectRows.length];
		int i = 0;
		for(TableItem rowItem : selectRows) {
			selectedItems[i++] = (TCComponentItem)resultMap.get(rowItem.getText());
		}
		result = selectedItems;
		action = true;
		shell.close();

	}

	@Override
	public void widgetDefaultSelected(SelectionEvent e) {

	}
}

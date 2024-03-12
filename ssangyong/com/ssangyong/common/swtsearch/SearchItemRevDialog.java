package com.ssangyong.common.swtsearch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
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
import org.eclipse.wb.swt.SWTResourceManager;

import com.ssangyong.common.SYMCClass;
import com.ssangyong.common.SortListenerFactory;
import com.ssangyong.common.remote.DataSet;
import com.ssangyong.common.remote.SYMCRemoteUtil;
import com.ssangyong.common.ui.SimpleProgressBar;
import com.ssangyong.common.utils.CustomUtil;
import com.ssangyong.common.utils.SYMDisplayUtil;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.util.MessageBox;

import swing2swt.layout.BorderLayout;
import swing2swt.layout.FlowLayout;

/**
 * 20230831 cf-4357 seho
 * 1. 아이템 리비전 검색하는 부분을 SQL로 변경하여 속도를 개선
 * 2. owner 조건을 빼고 latest released revision을 조건에 추가함.
 * 3. 검색 결과에서 선택 시 최종 릴리즈리비전을 자동으로 선택되도록 함.
 * [20240312][UPGRADE] Table Data Map 으로 저장. Sorting 후 입력시 오류 발생
 */
public class SearchItemRevDialog extends Dialog implements SelectionListener
{

	private boolean action = false;
	private Object result;
	private Shell shell;
	private Text idText;
	private Table table;
	private int selection;
	private String idString = "";
	private String nameString = "";
	private String isOnlyLatestReleasedRevision = "";
	private boolean isStop = false;
	private Text nameText;
	private Button okButton;
	private Button latestOnlyCheckButton;

	private String strItemType;

	/**
	 * Create the dialog.
	 * 
	 * @param parent
	 * @param style
	 */
	public SearchItemRevDialog(Shell parent, int _selection, String strItemType)
	{
		super(parent);
		selection = _selection;
		setText("Search Item(" + strItemType + ")");
		this.strItemType = strItemType;
	}

	/**
	 * Open the dialog.
	 * 
	 * @return the result
	 */
	public Object open()
	{
		createContents();
		shell.open();
		shell.layout();
		Display display = getParent().getDisplay();
		while (!shell.isDisposed())
		{
			if (!display.readAndDispatch())
			{
				display.sleep();
			}
		}

		return result;
	}

	/**
	 * Create contents of the dialog.
	 */
	private void createContents()
	{
		shell = new Shell(getParent(), SWT.RESIZE | SWT.TITLE | SWT.PRIMARY_MODAL | SWT.DIALOG_TRIM);
		shell.setImage(com.teamcenter.rac.common.Activator.getDefault().getImage("icons/search_16.png"));
		shell.setSize(690, 382);
		SYMDisplayUtil.centerToParent(getParent().getShell(), shell);
		shell.setText(getText());
		shell.setLayout(new BorderLayout(0, 5));
		Composite composite = new Composite(shell, SWT.NONE);
		composite.setLayoutData(BorderLayout.NORTH);
		composite.setLayout(new GridLayout(4, false));
		Label idLabel = new Label(composite, SWT.RIGHT);
		idLabel.setText("ID");
		idText = new Text(composite, SWT.BORDER);
		GridData gd_idText = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_idText.widthHint = 169;
		idText.setLayoutData(gd_idText);
		idText.addKeyListener(new KeyAdapter()
		{
			@Override
			public void keyPressed(KeyEvent e)
			{
				if (e.keyCode == SWT.CR || e.keyCode == SWT.KEYPAD_CR)
				{
					searchAction();
				}
			}
		});
		new Label(composite, SWT.NONE);
		new Label(composite, SWT.NONE);

		Label nameLabel = new Label(composite, SWT.RIGHT);
		nameLabel.setText("Name");

		nameText = new Text(composite, SWT.BORDER);
		nameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		nameText.addKeyListener(new KeyAdapter()
		{
			@Override
			public void keyPressed(KeyEvent e)
			{
				if (e.keyCode == SWT.CR || e.keyCode == SWT.KEYPAD_CR)
				{
					searchAction();
				}
			}
		});

		latestOnlyCheckButton = new Button(composite, SWT.CHECK);
		latestOnlyCheckButton.setText("Only Latest Released Revision");
		latestOnlyCheckButton.setSelection(true);

		Button searchButton = new Button(composite, SWT.NONE);
		searchButton.setImage(SWTResourceManager.getImage(SearchItemRevDialog.class, "/com/teamcenter/rac/util/images/search_16.png"));
		searchButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
		searchButton.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				searchAction();
			}
		});
		searchButton.setText("검색");

		table = new Table(shell, SWT.BORDER | SWT.FULL_SELECTION | selection);
		table.setLayoutData(BorderLayout.CENTER);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		TableColumn tblclmnNewColumn = new TableColumn(table, SWT.NONE);
		tblclmnNewColumn.setWidth(100);
		tblclmnNewColumn.setText("ID");
		tblclmnNewColumn.addListener(SWT.Selection, SortListenerFactory.getListener(SortListenerFactory.STRING_COMPARATOR));
		TableColumn tblclmnNewColumn_0 = new TableColumn(table, SWT.NONE);
		tblclmnNewColumn_0.setWidth(35);
		tblclmnNewColumn_0.setText("Rev.");
		tblclmnNewColumn_0.addListener(SWT.Selection, SortListenerFactory.getListener(SortListenerFactory.STRING_COMPARATOR));
		TableColumn tblclmnNewColumn_2 = new TableColumn(table, SWT.NONE);
		tblclmnNewColumn_2.setWidth(240);
		tblclmnNewColumn_2.setText("이름");
		tblclmnNewColumn_2.addListener(SWT.Selection, SortListenerFactory.getListener(SortListenerFactory.STRING_COMPARATOR));
		TableColumn tblclmnNewColumn_1 = new TableColumn(table, SWT.NONE);
		tblclmnNewColumn_1.setWidth(106);
		tblclmnNewColumn_1.setText("소유자");
		tblclmnNewColumn_1.addListener(SWT.Selection, SortListenerFactory.getListener(SortListenerFactory.STRING_COMPARATOR));

		TableColumn release_statuses_Column = new TableColumn(table, SWT.NONE);
		release_statuses_Column.setWidth(80);
		release_statuses_Column.setText("\uACB0\uC7AC\uC0C1\uD0DC");

		TableColumn tblclmnNewColumn_3 = new TableColumn(table, SWT.NONE);
		tblclmnNewColumn_3.setWidth(80);
		tblclmnNewColumn_3.setText("Maturity");
		release_statuses_Column.addListener(SWT.Selection, SortListenerFactory.getListener(SortListenerFactory.STRING_COMPARATOR));

		table.addMouseListener(new MouseAdapter()
		{
			public void mouseDoubleClick(MouseEvent e)
			{
				okProcess();
			}
		});

		Composite composite_1 = new Composite(shell, SWT.NONE);
		composite_1.setLayoutData(BorderLayout.SOUTH);
		composite_1.setLayout(new FlowLayout(FlowLayout.RIGHT, 5, 0));
		Composite composite_2 = new Composite(composite_1, SWT.NONE);
		okButton = new Button(composite_2, SWT.NONE);
		okButton.addSelectionListener(this);
		okButton.setBounds(0, 0, 77, 24);
		okButton.setText("확인");
		Button closeButton = new Button(composite_2, SWT.NONE);
		closeButton.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				action = false;
				shell.close();
			}
		});
		closeButton.setBounds(82, 0, 77, 24);
		closeButton.setText("닫기");
		Label blank = new Label(composite_2, SWT.None);
		blank.setBounds(0, 25, 77, 5);

		if (SYMCClass.S7_PROJECTREVISIONTYPE.equals(strItemType))
		{
			latestOnlyCheckButton.setVisible(false);
		}
		//cf-4417 20230920 seho 재질일 경우 최종 리비전 유무에 이름을 변경해줌.
		if (SYMCClass.S7_MATPARTREVISIONTYPE.equals(strItemType))
		{
			latestOnlyCheckButton.setText("Only Latest Revision");
		}
	}

	private void searchAction()
	{
		isStop = false;

		idString = idText.getText().trim();
		nameString = nameText.getText().trim();

		isOnlyLatestReleasedRevision = latestOnlyCheckButton.getSelection() + "";

		if (CustomUtil.isEmpty(idString) && CustomUtil.isEmpty(nameString))
		{
			MessageBox.post(shell, "검색조건을 입력하세요.", "경고", MessageBox.WARNING);
			return;
		}
		//cf-4417 20230920 seho 재질의 경우 프로젝트와 같이 검색 글자수 제한을 없앰.
		if (!SYMCClass.S7_PROJECTREVISIONTYPE.equals(strItemType) && !SYMCClass.S7_MATPARTREVISIONTYPE.equals(strItemType))
		{
			if ((("*".equals(idString) && "".equals(nameString)) || ("".equals(idString) && "*".equals(nameString)) || ("*".equals(idString) && "*".equals(nameString))))
			{
				MessageBox.post(shell, "'*' 검색값은 허용하지 않습니다.", "경고", MessageBox.WARNING);
				return;
			}
			if (!idString.isEmpty() && idString.replaceAll("\\*", "").length() < 4)
			{
				MessageBox.post(shell, "ID는 최소 네자리 이상 입력해 주세요.", "경고", MessageBox.WARNING);
				return;
			}
			if (!nameString.isEmpty() && nameString.replaceAll("\\*", "").length() < 4)
			{
				MessageBox.post(shell, "NAME은 최소 네자리 이상 입력해 주세요.", "경고", MessageBox.WARNING);
				return;
			}
		}
		new Job("검색")
		{
			@Override
			protected IStatus run(IProgressMonitor arg0)
			{

				if (isStop)
				{
					return new Status(IStatus.OK, "ssangyong", "검색 완료");
				}
				final SimpleProgressBar simpleProgressBar = new SimpleProgressBar();
				try
				{
					shell.getDisplay().syncExec(new Runnable()
					{
						public void run()
						{
							table.removeAll();
							result = null;
							simpleProgressBar.setLabel("검색중...");
							simpleProgressBar.centerToParent(shell);
							simpleProgressBar.setAlwaysOnTop(true);
							shell.setEnabled(false);
							simpleProgressBar.setVisible(true);
						}
					});
					final int count = searchItemRevision(idString, nameString, isOnlyLatestReleasedRevision);
					shell.getDisplay().syncExec(new Runnable()
					{
						public void run()
						{
							simpleProgressBar.setVisible(false);
						}
					});
					if (count == 0)
					{
						shell.getDisplay().syncExec(new Runnable()
						{
							public void run()
							{
								MessageBox.post(shell, "검색 결과가 없습니다.", "경고", MessageBox.WARNING);
							}
						});
					}
				} catch (final Exception e1)
				{
					e1.printStackTrace();
					shell.getDisplay().syncExec(new Runnable()
					{
						public void run()
						{
							MessageBox.post(shell, e1, true);
						}
					});
				} finally
				{
					shell.getDisplay().syncExec(new Runnable()
					{
						public void run()
						{
							if (simpleProgressBar != null)
							{
								simpleProgressBar.setVisible(false);
								simpleProgressBar.dispose();
							}
						}
					});
				}

				shell.getDisplay().syncExec(new Runnable()
				{
					public void run()
					{
						shell.setEnabled(true);
					}
				});
				return new Status(IStatus.OK, "ssangyong", "검색 완료");
			}

		}.schedule();
	}

//	public TCComponent[] searchId(String itemId, String itemName, String currentUserName) throws Exception
//	{
//
//		if (CustomUtil.isEmpty(itemId))
//			itemId = "*";
//
//		if (CustomUtil.isEmpty(itemName))
//			itemName = "*";
//
//		if (CustomUtil.isEmpty(currentUserName))
//		    currentUserName = "*";
//
//		TCComponent[] comps = null;
// 		if ( SYMCClass.S7_PROJECTREVISIONTYPE.equals(this.strItemType))
//		{
//			comps = CustomUtil.queryComponent("__SYMC_S7_PROJECTRevision", new String[] { "item_id", "object_name", "user_name" }, new String[] { itemId, itemName, currentUserName });
//		} else if (SYMCClass.S7_MATPARTREVISIONTYPE.equals(this.strItemType))
//		{
////			comps = CustomUtil.queryComponent("__SYMC_S7_MaterialRevision", new String[] { "item_id", "object_name", "user_name", "s7_ACTIVATION" }, new String[] { itemId, itemName, currentUserName, "Y" });
//			comps = CustomUtil.queryComponent("__SYMC_S7_MaterialRevision", new String[] { "item_id", "object_name", "user_name", "Activation" }, new String[] { itemId, itemName, currentUserName, "Y" });
//		} else
//		{
//			comps = CustomUtil.queryComponent("Item Revision...", new String[] { "ItemID", "Name", "Type", "OwningUser" }, new String[] { itemId, itemName, this.strItemType, currentUserName });
//		}
//		return comps;
//
//	}

	/**
	 * 20230901 seho 검색 결과를 다르게 보여주기 위해 SQL로 변경함.
	 * searchId method는 제거함.
	 * 
	 * @param itemId
	 * @param itemName
	 * @param isOnlyLatestReleasedRevision
	 * @return
	 * @throws Exception
	 */
	public int searchItemRevision(String itemId, String itemName, String isOnlyLatestReleasedRevision) throws Exception
	{
		int count = 0;
		itemId = itemId.replaceAll("\\*", "%");
		itemName = itemName.replaceAll("\\*", "%");
		SYMCRemoteUtil remoteQuery = new SYMCRemoteUtil();
		DataSet ds = new DataSet();
		ds.put("ID", itemId);
		ds.put("NAME", itemName);
		ds.put("TYPE", strItemType);
		ds.put("IS_ONLY_LATEST_RELEASED_REVISION", isOnlyLatestReleasedRevision);
		ArrayList<HashMap> resultList = (ArrayList<HashMap>) remoteQuery.execute("com.ssangyong.service.SearchTeamcenterService", "searchItemRevision", ds);
		if (resultList == null)
		{
			return count;
		}
		count = resultList.size();
		for (HashMap<?, ?> resultRow : resultList)
		{
			final String id = (String) resultRow.get("ID");
			final String revision = (String) resultRow.get("REVISION");
			final String name = (String) resultRow.get("NAME");
			final String owner = (String) resultRow.get("OWNER");
			final String status = (String) resultRow.get("STATUS");
			final String maturity = (String) resultRow.get("MATURITY");
			final String revPuid = (String) resultRow.get("REV_PUID");
			final String latestReleasedRevisionPuid = (String) resultRow.get("LATEST_RELEASED_REVISION");
			//최종 릴리즈 리비전의 리비전과 maturity 값을 저장해둔다.
			//나중에 보여주고 체크하기 위해서.
			//결재 완료된게 하나도 없으면 
			String tlatestRev = "";
			String tlatestMaturity = "";
			for (HashMap<?, ?> resultRow1 : resultList)
			{
				String xlatestRev = (String) resultRow1.get("REVISION");
				String xlatestMaturity = (String) resultRow1.get("MATURITY");
				String xrevPuid = (String) resultRow1.get("REV_PUID");
				if (xrevPuid.equals(latestReleasedRevisionPuid))
				{
					tlatestRev = xlatestRev;
					tlatestMaturity = xlatestMaturity;
					break;
				}
			}
			final String latestRev = tlatestRev.isEmpty() ? revision : tlatestRev;
			final String latestMaturity = tlatestMaturity.isEmpty() ? maturity : tlatestMaturity;

			shell.getDisplay().syncExec(new Runnable()
			{
				public void run()
				{
					TableItem rowItem = new TableItem(table, SWT.NONE);
					rowItem.setText(0, id);
					rowItem.setText(1, revision);
					rowItem.setText(2, name);
					rowItem.setText(3, owner);
					rowItem.setText(4, status);
					rowItem.setText(5, maturity);
					//[20240312][UPGRADE] Table Data Map 으로 저장. Sorting 시 못가져옴
//					rowItem.setData("REV_PUID", revPuid);
//					rowItem.setData("LATEST_RELEASED_REVISION", latestReleasedRevisionPuid == null ? "" : latestReleasedRevisionPuid);
//					rowItem.setData("LATEST_REVISION", latestRev);
//					rowItem.setData("LATEST_MATURITY", latestMaturity);
					Map<String,String> tableData = new HashMap<>();
					tableData.put("REV_PUID", revPuid);
					tableData.put("LATEST_RELEASED_REVISION", latestReleasedRevisionPuid == null ? "" : latestReleasedRevisionPuid);
					tableData.put("LATEST_REVISION", latestRev);
					tableData.put("LATEST_MATURITY", latestMaturity);
					rowItem.setData(tableData);

				}
			});
		}
		return count;
	}

	public boolean isOK()
	{
		return action;
	}

	public Button getOkButton()
	{
		return okButton;
	}

	public Table getTable()
	{
		return table;
	}

	@Override
	public void widgetSelected(SelectionEvent e)
	{
		if (e.getSource() == okButton)
		{
			okProcess();
		}
	}

	private void okProcess()
	{
		TableItem[] selectRows = table.getSelection();
		if (selectRows == null || selectRows.length == 0)
		{
			MessageBox.post(shell, "검색 결과를 선택하여 주십시오.", "경고", MessageBox.WARNING);
			return;
		}
		ArrayList<String> puidList = new ArrayList<String>();
		for (int inx = 0; inx < selectRows.length; inx++)
		{
			TableItem ti = selectRows[inx];
			@SuppressWarnings("unchecked")
			Map<String,String>tableData = (HashMap<String, String>)ti.getData();
//			String revisionPuid = (String) ti.getData("REV_PUID");
//			String latestReleasedRevisionPuid = (String) ti.getData("LATEST_RELEASED_REVISION");
//			String latestRevision = (String) ti.getData("LATEST_REVISION");
//			String latestMaturity = (String) ti.getData("LATEST_MATURITY");
			String revisionPuid = tableData.get("REV_PUID");
			String latestReleasedRevisionPuid = tableData.get("LATEST_RELEASED_REVISION");
			String latestRevision = tableData.get("LATEST_REVISION");
			String latestMaturity = tableData.get("LATEST_MATURITY");
			String selectedRevisionString = ti.getText(0) + "/" + ti.getText(1) + "-" + ti.getText(2);
			String latestRevisionString = ti.getText(0) + "/" + latestRevision + "-" + ti.getText(2);

			String puid = latestReleasedRevisionPuid;
			//cf-4417 20230920 seho 최종 릴리즈 puid가 없는 경우는 펑션마스터나 비히클 파트의 경우이다.
			if (latestReleasedRevisionPuid.isEmpty())
			{
				MessageBox.post("선택된 리비전의 아이템에 릴리즈된 리비전이 존재하지 않습니다.", "최종 릴리즈 리비전 사용", MessageBox.INFORMATION);
				return;
			}
			//cf-4417 20230920 seho 재질의 경우에도 여러 리비전이 있다면 최종 리비전을 자동 선택하게 해준다. 최종리비전은 결재 유무에 상관없다.
			if (!revisionPuid.equals(latestReleasedRevisionPuid))
			{
				String releaseString = SYMCClass.S7_MATPARTREVISIONTYPE.equals(strItemType) ? "" : "릴리즈 ";
				MessageBox.post("선택한 리비전은 최종 " + releaseString + "리비전이 아닙니다.\n최종 " + releaseString + "리비전이 선택됩니다.\n" + selectedRevisionString + "\n ▶ " + latestRevisionString, "최종 " + releaseString + "리비전 사용", MessageBox.INFORMATION);
			}
			if (latestMaturity.equals("Obsolete"))
			{
				MessageBox.post(latestRevisionString + " is obsoleted.", "Error", MessageBox.ERROR);
				return;
			}
			if (!puid.isEmpty() && !puidList.contains(puid))
			{
				puidList.add(puid);
			}
		}
		String[] puidArray = puidList.toArray(new String[puidList.size()]);
		try
		{
			result = (TCComponent[]) CustomUtil.getTCSession().stringToComponent(puidArray);
		} catch (TCException e)
		{
			e.printStackTrace();
			MessageBox.post(shell, e, true);
			return;
		}
		action = true;
		shell.close();

	}

	@Override
	public void widgetDefaultSelected(SelectionEvent e)
	{

	}
}

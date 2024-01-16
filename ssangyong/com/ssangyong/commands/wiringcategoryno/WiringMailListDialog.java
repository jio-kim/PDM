package com.ssangyong.commands.wiringcategoryno;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.wb.swt.SWTResourceManager;

import com.ssangyong.common.lov.SYMCLOVLoader;
import com.ssangyong.common.remote.DataSet;
import com.ssangyong.common.remote.SYMCRemoteUtil;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponentUser;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.ArraySorter;
import com.teamcenter.rac.util.ConfirmDialog;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.SWTUIUtilities;

public class WiringMailListDialog extends Dialog
{

	protected Object result;
	protected Shell shell;
	private Group grpMailList;
	private Table table;
	private TableColumn tblclmnNewColumn;
	private TableColumn tblclmnNewColumn_1;
	private Combo combo;
	private Button addButton;
	private Button removeButton;
	private Button saveButton;
	private Button closeButton;
	private Label lblNewLabel;
	private TableColumn tblclmnNewColumn_2;
	private TCSession session;
	private Label lblNewLabel_1;
	private TableColumn tblclmnNewColumn_3;

	/**
	 * Create the dialog.
	 * 
	 * @param parent
	 * @param style
	 */
	public WiringMailListDialog(Shell parent)
	{
		super(parent);
		session = (TCSession) AIFUtility.getDefaultSession();
	}

	/**
	 * Open the dialog.
	 * 
	 * @return the result
	 */
	public Object open()
	{
		createContents();
		isWritable(false);
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				getUserList();
				loadAction();
			}
		}).start();
		SWTUIUtilities.centerInParent(getParent(), shell);
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
		shell = new Shell(getParent(), SWT.CLOSE | SWT.RESIZE);
		shell.setImage(SWTResourceManager.getImage(WiringMailListDialog.class, "/icons/mailboxfoldercollapsed_16.png"));
		shell.setBackgroundMode(SWT.INHERIT_FORCE);
		shell.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		shell.setSize(551, 540);
		shell.setText("Wiring Mail List \uAD00\uB9AC");
		GridLayout gl_shlWiringMailList = new GridLayout();
		gl_shlWiringMailList.numColumns = 2;
		shell.setLayout(gl_shlWiringMailList);

		grpMailList = new Group(shell, SWT.NONE);
		grpMailList.setText("Mail List");
		grpMailList.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		grpMailList.setLayout(new GridLayout(4, false));

		lblNewLabel = new Label(grpMailList, SWT.NONE);
		lblNewLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel.setText("User");

		combo = new Combo(grpMailList, SWT.NONE);
		combo.setToolTipText("\uC774\uB984\uC744 \uC4F0\uACE0 \uBC29\uD5A5\uD0A4(\uC0C1\uD558)\uB97C \uC6C0\uC9C1\uC774\uBA74 \uAC80\uC0C9\uC774 \uB429\uB2C8\uB2E4.");
		combo.addKeyListener(new KeyAdapter()
		{
			@Override
			public void keyPressed(KeyEvent e)
			{
				if (e.keyCode == SWT.CR)
				{
					addAction();
				}
			}
		});
		GridData gd_combo = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_combo.widthHint = 191;
		combo.setLayoutData(gd_combo);

		addButton = new Button(grpMailList, SWT.NONE);
		addButton.setImage(SWTResourceManager.getImage(WiringMailListDialog.class, "/icons/add_16.png"));
		addButton.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				addAction();
			}
		});
		addButton.setText("Add");

		removeButton = new Button(grpMailList, SWT.NONE);
		removeButton.setImage(SWTResourceManager.getImage(WiringMailListDialog.class, "/icons/remove_16.png"));
		removeButton.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				TableItem[] tableItems = table.getSelection();
				if (tableItems == null || tableItems.length == 0)
				{
					MessageBox.post(shell, "제거할 User를 아래 Table에서 선택하세요.", "선택", MessageBox.WARNING);
					return;
				}
				int r = ConfirmDialog.prompt(shell, "삭제", "선택한 대상을 삭제하시겠습니까?");
				if (r == 2)
				{
					for (TableItem tableItem : tableItems)
					{
						tableItem.dispose();
					}
				}
			}
		});
		removeButton.setText("Remove");

		table = new Table(grpMailList, SWT.BORDER | SWT.CHECK | SWT.FULL_SELECTION | SWT.MULTI);
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1));
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		tblclmnNewColumn_2 = new TableColumn(table, SWT.CENTER);
		tblclmnNewColumn_2.setWidth(48);
		tblclmnNewColumn_2.setText("\uAD00\uB9AC\uC790");

		tblclmnNewColumn = new TableColumn(table, SWT.CENTER);
		tblclmnNewColumn.setWidth(100);
		tblclmnNewColumn.setText("ID");

		tblclmnNewColumn_1 = new TableColumn(table, SWT.CENTER);
		tblclmnNewColumn_1.setWidth(150);
		tblclmnNewColumn_1.setText("Name");
		
		tblclmnNewColumn_3 = new TableColumn(table, SWT.CENTER);
		tblclmnNewColumn_3.setWidth(181);
		tblclmnNewColumn_3.setText("Team");

		lblNewLabel_1 = new Label(shell, SWT.WRAP);
		lblNewLabel_1.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));
		lblNewLabel_1.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		lblNewLabel_1.setText("\u203B \uAD00\uB9AC\uC790\uB294 Wiring Mail List\uC640 Wiring Category No \uC815\uBCF4\uB97C \uBCC0\uACBD\uD560 \uC218 \uC788\uC2B5\uB2C8\uB2E4.");

		saveButton = new Button(shell, SWT.NONE);
		saveButton.setImage(SWTResourceManager.getImage(WiringMailListDialog.class, "/icons_16/save_16.png"));
		saveButton.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				saveAction();
			}
		});
		saveButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1));
		saveButton.setText("\uC800\uC7A5");

		closeButton = new Button(shell, SWT.NONE);
		closeButton.setImage(SWTResourceManager.getImage(WiringMailListDialog.class, "/com/teamcenter/rac/util/images/close_16.png"));
		closeButton.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				shell.dispose();
			}
		});
		closeButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
		closeButton.setText("\uB2EB\uAE30");
	}

	protected void loadAction()
	{
		String loginUser = "";
		boolean isSystemAdmin = false;
		try
		{
			loginUser = session.getUser().getUserId();
			isSystemAdmin = session.isUserSystemAdmin();
		} catch (TCException e1)
		{
			e1.printStackTrace();
			MessageBox.post(shell, e1, true);
		}
		//sql로 데이터를 가져옴.
		ArrayList<HashMap> resultList = null;
		SYMCRemoteUtil remoteQuery = new SYMCRemoteUtil();
		//나중에 조건을 넣을까 싶어서 놔둠.
		try
		{
			DataSet ds = new DataSet();
			ds.put("USER_ID", null);
			resultList = (ArrayList<HashMap>) remoteQuery.execute("com.ssangyong.service.WiringCheckService", "getWiringMailList", ds);
			if (resultList == null)
			{
				return;
			}
		} catch (Exception e)
		{
			e.printStackTrace();
			MessageBox.post(shell, e, true);
			return;
		}

		for (HashMap resultRow : resultList)
		{
			final boolean idAdmin = ((String) resultRow.get("IS_ADMIN")).equals("Y") ? true : false;
			final String userId = (String) resultRow.get("ID");
			final String userName = (String) resultRow.get("NAME");
			final String team = (String) resultRow.get("TEAM");
			Display.getDefault().syncExec(new Runnable()
			{
				@Override
				public void run()
				{
					TableItem tableItem = new TableItem(table, SWT.NONE);
					tableItem.setChecked(idAdmin);
					tableItem.setText(1, userId);
					tableItem.setText(2, userName);
					tableItem.setText(3, team);
//					if(idAdmin)
//					{
//						tableItem.setBackground(SWTResourceManager.getColor(SWT.COLOR_YELLOW));
//					}
					try
					{
						if ((idAdmin && session.getUser().getUserId().equals(userId)) || session.isUserSystemAdmin())
						{
							isWritable(true);
						}
					} catch (TCException e)
					{
						e.printStackTrace();
						MessageBox.post(shell, e, true);
						return;
					}
				}
			});
		}
	}

	protected void saveAction()
	{
		TableItem[] tableItems = table.getItems();
		if (tableItems == null || tableItems.length == 0)
		{
			MessageBox.post(shell, "저장할 데이터가 없습니다.", "저장", MessageBox.WARNING);
			return;
		}
		boolean isExistAdmin = false;
		for (TableItem tableItem : tableItems)
		{
			if (tableItem.getChecked())
			{
				isExistAdmin = true;
				break;
			}
		}
		if (!isExistAdmin)
		{
			MessageBox.post(shell, "관리자는 최소 한명 이상이어야 합니다.", "관리자 지정", MessageBox.WARNING);
			return;
		}
		int r = ConfirmDialog.prompt(shell, "저장", "저장하시겠습니까?");
		if (r == 2)
		{
			SYMCRemoteUtil remoteQuery = new SYMCRemoteUtil();
			try
			{
				remoteQuery.execute("com.ssangyong.service.WiringCheckService", "deleteWiringMailList", null);
			} catch (Exception e1)
			{
				e1.printStackTrace();
				MessageBox.post(shell, e1, true);
				return;
			}
			ArrayList userList = new ArrayList();
			for (TableItem tableItem : tableItems)
			{
				boolean isAdmin = tableItem.getChecked();
				String userId = tableItem.getText(1);
				DataSet ds = new DataSet();
				ds.put("USER_ID", userId);
				ds.put("IS_ADMIN", isAdmin ? "Y" : "N");
				userList.add(ds);
			}
			try
			{
				DataSet ds = new DataSet();
				ds.put("USER_LIST", userList);
				remoteQuery.execute("com.ssangyong.service.WiringCheckService", "insertWiringMailList", ds);
			} catch (Exception e)
			{
				e.printStackTrace();
				MessageBox.post(shell, e, true);
				return;
			}
			MessageBox.post(shell, "저장이 완료되었습니다", "저장", MessageBox.INFORMATION);
		}
	}

	protected void addAction()
	{
		String user = combo.getText();
		int index = combo.indexOf(user);
		if (index == -1)
		{
			MessageBox.post(shell, "User를 선택하세요.", "선택", MessageBox.WARNING);
			return;
		}
		String userId = TCComponentUser.getUserIdFromFormattedString(user);
		String userName = TCComponentUser.getUserNameFromFormattedString(user);
		String team = user.substring(user.lastIndexOf(" "));
		TableItem[] items = table.getItems();
		for (TableItem item : items)
		{
			String tUserId = item.getText(1);
			if (userId.equals(tUserId))
			{
				MessageBox.post(shell, "선택한 User는 이미 추가되어 있습니다.", "중복", MessageBox.WARNING);
				return;
			}
		}
		TableItem tableItem = new TableItem(table, SWT.NONE);
		tableItem.setText(1, userId);
		tableItem.setText(2, userName);
		tableItem.setText(3, team);
	}

	private void getUserList()
	{
		Display.getDefault().syncExec(new Runnable()
		{
			@Override
			public void run()
			{
				combo.setEnabled(false);
				combo.setText("loading...");
			}
		});
		//sql로 데이터를 가져옴.
		ArrayList<HashMap> resultList = null;
		SYMCRemoteUtil remoteQuery = new SYMCRemoteUtil();
		try
		{
			DataSet ds = new DataSet();
			ds.put("USER_ID", null);
			resultList = (ArrayList<HashMap>) remoteQuery.execute("com.ssangyong.service.WiringCheckService", "findUserInVNet", ds);
			if (resultList == null)
			{
				return;
			}
		} catch (Exception e)
		{
			e.printStackTrace();
			MessageBox.post(shell, e, true);
			return;
		}
		for (final HashMap rowMap : resultList)
		{
			Display.getDefault().syncExec(new Runnable()
			{
				@Override
				public void run()
				{
					String userId = (String) rowMap.get("ID");
					String userName = (String) rowMap.get("NAME");
					String team = (String) rowMap.get("TEAM");
					String userString = userName + " (" + userId + ") - " + team;
					combo.add(userString);
				}
			});
		}
		Display.getDefault().syncExec(new Runnable()
		{
			@Override
			public void run()
			{
				combo.setEnabled(true);
				combo.setText("");
			}
		});
	}

	private void isWritable(boolean isWritable)
	{
		addButton.setEnabled(isWritable);
		removeButton.setEnabled(isWritable);
		table.setEnabled(isWritable);
		saveButton.setEnabled(isWritable);
	}
}

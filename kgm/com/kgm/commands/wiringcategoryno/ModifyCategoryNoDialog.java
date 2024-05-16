package com.kgm.commands.wiringcategoryno;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.wb.swt.SWTResourceManager;

import com.teamcenter.rac.util.ArraySorter;
import com.teamcenter.rac.util.ConfirmDialog;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.SWTUIUtilities;

public class ModifyCategoryNoDialog extends Dialog
{

	protected Object result;
	protected Shell shell;
	private String title;
	private Hashtable<String, String> categoryHash;
	private String categoryNo;
	private Composite grpCategoryNo;
	private Table categoryTable;
	private Composite composite;
	private Button addButton;
	private Button removeButton;
	private Table categoryListTable;
	private TableColumn tblclmnNewColumn;
	private Label titleLabel;
	private TableColumn tblclmnNewColumn_1;
	private TableColumn tblclmnNewColumn_2;
	private Composite composite_1;
	private Button btnNewButton;
	private Button cancelButton;
	private TableColumn tblclmnNewColumn_3;
	private Group grpCategoryNo_1;
	private Group grpProjectCategory;

	/**
	 * Create the dialog.
	 * 
	 * @param parent
	 * @param style
	 */
	public ModifyCategoryNoDialog(Shell parent, String _title, Hashtable<String, String> _categotyHash, String _categoryNo)
	{
		super(parent);
		title = _title;
		categoryHash = _categotyHash;
		categoryNo = _categoryNo;
	}

	/**
	 * Open the dialog.
	 * 
	 * @return the result
	 */
	public String open()
	{
		createContents();
		loadAction();
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
		return categoryNo;
	}

	/**
	 * Create contents of the dialog.
	 */
	private void createContents()
	{
		shell = new Shell(getParent(), SWT.CLOSE | SWT.RESIZE | SWT.PRIMARY_MODAL);
		shell.setBackgroundMode(SWT.INHERIT_FORCE);
		shell.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		shell.setSize(568, 649);
		shell.setText("Category No \uD3B8\uC9D1");
		shell.setLayout(new GridLayout());

		grpCategoryNo = new Composite(shell, SWT.NONE);
		grpCategoryNo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		grpCategoryNo.setLayout(new GridLayout(3, false));

		titleLabel = new Label(grpCategoryNo, SWT.NONE);
		titleLabel.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));
		titleLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 1));

		grpCategoryNo_1 = new Group(grpCategoryNo, SWT.NONE);
		grpCategoryNo_1.setText("\uC120\uD0DD\uB41C Category No");
		grpCategoryNo_1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		grpCategoryNo_1.setLayout(new GridLayout(1, false));

		categoryTable = new Table(grpCategoryNo_1, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
		categoryTable.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseDoubleClick(MouseEvent e)
			{
				TableItem[] selectedItems = categoryTable.getSelection();
				TableItem cliskedItem = categoryTable.getItem(new Point(e.x, e.y));
				if (selectedItems != null && selectedItems.length != 0 && selectedItems[0].equals(cliskedItem))
				{
					removeCategoryNo();
				}
			}
		});
		categoryTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		categoryTable.setHeaderVisible(true);
		categoryTable.setLinesVisible(true);

		tblclmnNewColumn_3 = new TableColumn(categoryTable, SWT.NONE);

		tblclmnNewColumn = new TableColumn(categoryTable, SWT.CENTER);
		tblclmnNewColumn.setText("Category No");
		tblclmnNewColumn.setWidth(100);

		Listener sortListener = new Listener()
		{
			public void handleEvent(Event e)
			{
				TableItem[] items = categoryTable.getItems();
				Collator collator = Collator.getInstance(Locale.getDefault());
				TableColumn column = (TableColumn) e.widget;
				int index = categoryTable.indexOf(tblclmnNewColumn);
				for (int i = 1; i < items.length; i++)
				{
					String value1 = items[i].getText(index);
					for (int j = 0; j < i; j++)
					{
						String value2 = items[j].getText(index);
						if (collator.compare(value1, value2) < 0)
						{
							String[] values = { items[i].getText(0), items[i].getText(1), items[i].getText(2), items[i].getText(3) };
							items[i].dispose();
							TableItem item = new TableItem(categoryTable, SWT.NONE, j);
							item.setText(values);
							items = categoryTable.getItems();
							break;
						}
					}
				}
				categoryTable.setSortColumn(column);
			}
		};
		tblclmnNewColumn.addListener(SWT.Selection, sortListener);
		categoryTable.setSortColumn(tblclmnNewColumn);
		categoryTable.setSortDirection(SWT.UP);

		composite = new Composite(grpCategoryNo, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));

		addButton = new Button(composite, SWT.NONE);
		addButton.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				addCategoryNo();
			}
		});
		addButton.setToolTipText("\uCD94\uAC00");
		addButton.setImage(SWTResourceManager.getImage(ModifyCategoryNoDialog.class, "/com/kgm/commands/variantoptioneditor/images/backarrow.png"));

		removeButton = new Button(composite, SWT.NONE);
		removeButton.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				removeCategoryNo();
			}
		});
		removeButton.setToolTipText("\uC81C\uAC70");
		removeButton.setImage(SWTResourceManager.getImage(ModifyCategoryNoDialog.class, "/com/kgm/commands/variantoptioneditor/images/forwardarrow.png"));

		grpProjectCategory = new Group(grpCategoryNo, SWT.NONE);
		grpProjectCategory.setText("Project \uC804\uCCB4 Category No \uB9AC\uC2A4\uD2B8");
		grpProjectCategory.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		grpProjectCategory.setLayout(new GridLayout(1, false));

		categoryListTable = new Table(grpProjectCategory, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
		categoryListTable.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseDoubleClick(MouseEvent e)
			{
				TableItem[] selectedItems = categoryListTable.getSelection();
				TableItem cliskedItem = categoryListTable.getItem(new Point(e.x, e.y));
				if (selectedItems != null && selectedItems.length != 0 && selectedItems[0].equals(cliskedItem))
				{
					addCategoryNo();
				}
			}
		});
		categoryListTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		categoryListTable.setHeaderVisible(true);
		categoryListTable.setLinesVisible(true);

		tblclmnNewColumn_1 = new TableColumn(categoryListTable, SWT.NONE);
		tblclmnNewColumn_1.setWidth(180);
		tblclmnNewColumn_1.setText("Category Name");

		tblclmnNewColumn_2 = new TableColumn(categoryListTable, SWT.CENTER);
		tblclmnNewColumn_2.setWidth(100);
		tblclmnNewColumn_2.setText("Category No");

		composite_1 = new Composite(shell, SWT.NONE);
		composite_1.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		composite_1.setLayout(new GridLayout(2, false));

		btnNewButton = new Button(composite_1, SWT.NONE);
		btnNewButton.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				saveAction();
			}
		});
		btnNewButton.setImage(SWTResourceManager.getImage(ModifyCategoryNoDialog.class, "/icons/edit_16.png"));
		btnNewButton.setText("\uD3B8\uC9D1 \uC644\uB8CC");

		cancelButton = new Button(composite_1, SWT.NONE);
		cancelButton.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				shell.dispose();
			}
		});
		cancelButton.setImage(SWTResourceManager.getImage(ModifyCategoryNoDialog.class, "/com/teamcenter/rac/util/images/close_16.png"));
		cancelButton.setText("\uCDE8\uC18C");
	}

	protected void addCategoryNo()
	{
		TableItem[] selectedItems = categoryListTable.getSelection();
		if (selectedItems == null || selectedItems.length == 0)
		{
			MessageBox.post(shell, "선택된 대상이 없습니다.", "선택", MessageBox.WARNING);
			return;
		}
		ArrayList<TableItem> selectedItemList = new ArrayList<TableItem>();
		ArrayList<String> currentCategoryList = getCurrentCategoryList();
		for (TableItem selectedItem : selectedItems)
		{
			String catNo = selectedItem.getText(1);
			if (!currentCategoryList.contains(catNo))
			{
				TableItem addItem = new TableItem(categoryTable, SWT.NONE);
				addItem.setText(1, catNo);
				selectedItemList.add(addItem);
			}
		}
		if (!selectedItemList.isEmpty())
		{
			categoryTable.setSelection(selectedItemList.toArray(new TableItem[selectedItemList.size()]));
		}
	}

	protected void removeCategoryNo()
	{
		TableItem[] selectedItems = categoryTable.getSelection();
		if (selectedItems == null || selectedItems.length == 0)
		{
			MessageBox.post(shell, "제거할 대상이 선택되지 않았습니다.", "선택", MessageBox.WARNING);
			return;
		}
		for (TableItem selectedItem : selectedItems)
		{
			selectedItem.dispose();
		}
	}

	protected void saveAction()
	{
		String categoryListString = "";
		ArrayList<String> currentCategoryList = getCurrentCategoryList();
		if (currentCategoryList.isEmpty())
		{
			MessageBox.post(shell, "선택된 Category No가 없습니다.", "선택", MessageBox.WARNING);
			return;
		}
		ArrayList<String> exceptCatList = new ArrayList<String>();
		for (String catString : currentCategoryList)
		{
			//카테고리 리스트에 없는 대상이 포함 된 경우 제외시킴.
			if (categoryHash.containsKey(catString))
			{
				categoryListString += (categoryListString.isEmpty() ? "" : " ") + catString;
			} else
			{
				exceptCatList.add(catString);
			}
		}
		if (!exceptCatList.isEmpty())
		{
			int r = ConfirmDialog.prompt(shell, "제외대상", "Catagory 리스트에 없는 Category No가 존재합니다.\n아래 Category No를 제외하고 적용하시겠습니까?\n" + exceptCatList.toString());
			if (r != 2)
			{
				TableItem[] tableItems = categoryTable.getItems();
				TableItem[] exceptItems = new TableItem[exceptCatList.size()];
				for (int i = 0; i < exceptCatList.size(); i++)
				{
					String exceptCatString = exceptCatList.get(i);
					for (TableItem tableItem : tableItems)
					{
						String catNoString = tableItem.getText(1);
						if (exceptCatString.equals(catNoString))
						{
							exceptItems[i] = tableItem;
							break;
						}
					}
				}
				categoryTable.setSelection(exceptItems);
				return;
			}
		}
		categoryNo = categoryListString;
		shell.dispose();
	}

	private void loadAction()
	{
		titleLabel.setText("■ " + title);
		String[] categoryNoArray = categoryNo.split(" ");
		if (categoryNoArray != null && categoryNoArray.length > 0)
		{
			for (String catNo : categoryNoArray)
			{
				// [20240313][UPGRADE] catNo 값이 있는경우만 추가하도록 수정
				if (catNo != null && !catNo.isEmpty()) {
					TableItem tableItem = new TableItem(categoryTable, SWT.NONE);
					tableItem.setText(1, catNo);
				}
			}
		}
		ArrayList<String> enumList = new ArrayList<String>();
		Enumeration<String> enum1 = categoryHash.keys();
		while (enum1.hasMoreElements())
		{
			enumList.add(enum1.nextElement());
		}
		ArraySorter.sort(enumList);
		for (String catNo : enumList)
		{
			String categoryName = categoryHash.get(catNo);
			TableItem tableItem = new TableItem(categoryListTable, SWT.NONE);
			tableItem.setText(0, categoryName);
			tableItem.setText(1, catNo);
		}
	}

	private ArrayList<String> getCurrentCategoryList()
	{
		ArrayList<String> currentCategoryList = new ArrayList<String>();
		TableItem[] rowItems = categoryTable.getItems();
		for (TableItem rowItem : rowItems)
		{
			String catString = rowItem.getText(1);
			currentCategoryList.add(catString);
		}
		ArraySorter.sort(currentCategoryList);
		return currentCategoryList;
	}
}

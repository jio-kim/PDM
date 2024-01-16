package com.ssangyong.commands.wiringcategoryno;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Set;

import org.eclipse.nebula.widgets.grid.Grid;
import org.eclipse.nebula.widgets.grid.GridColumn;
import org.eclipse.nebula.widgets.grid.GridItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;

import com.ssangyong.commands.ospec.op.OSpec;
import com.ssangyong.commands.ospec.op.OpCategory;
import com.ssangyong.commands.ospec.op.OpUtil;
import com.ssangyong.common.ExportPOIExcel;
import com.ssangyong.common.lov.SYMCLOVLoader;
import com.ssangyong.common.remote.DataSet;
import com.ssangyong.common.remote.SYMCRemoteUtil;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.ArraySorter;
import com.teamcenter.rac.util.ConfirmDialog;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.SWTUIUtilities;

public class WiringCategoryNoDialog extends Dialog
{

	protected Object result;
	protected Shell shell;
	private Hashtable<String, Hashtable<String, String>> ospecHash = new Hashtable<String, Hashtable<String, String>>();
	private Group allCategoryNoGroup;
	private Group modifyCategoryNoGroup;
	private Label lblNewLabel;
	private Combo projectCombo;
	private Text mainCategoryText;
	private Button addButton;
	private Button closeButton;
	private Button removeButton;
	private Grid grid;
	private GridColumn gridColumn;
	private GridColumn gridColumn_1;
	private GridColumn gridColumn_2;
	private TCSession session;
	private Button saveButton;
	private Label lblNewLabel_1;
	private Label lblNewLabel_2;
	private Label lblNewLabel_3;
	private Text engineCategoryText;
	private Text floorCategoryText;
	private Button modifyMainButton;
	private Button modifyEngineButton;
	private Button modifyFloorButton;
	private Composite composite;
	private Button exportExcelButton;
	private Label lblNewLabel_4;
	private Button reLoadButton;
	private Composite composite_1;

	/**
	 * Create the dialog.
	 * 
	 * @param parent
	 * @param style
	 */
	public WiringCategoryNoDialog(Shell parent)
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
		shell.setBackgroundMode(SWT.INHERIT_FORCE);
		shell.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		shell.setSize(897, 710);
		shell.setText("Wiring Category No Manager");
		GridLayout gl_shlWiringCategoryNo = new GridLayout();
		shell.setLayout(gl_shlWiringCategoryNo);

		allCategoryNoGroup = new Group(shell, SWT.NONE);
		allCategoryNoGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		allCategoryNoGroup.setText("Project\uBCC4 Category No");
		allCategoryNoGroup.setLayout(new GridLayout(1, false));

		lblNewLabel_4 = new Label(allCategoryNoGroup, SWT.NONE);
		lblNewLabel_4.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));
		lblNewLabel_4.setText("\u203B Table\uC758 \uB9AC\uC2A4\uD2B8\uB97C \uB354\uBE14 \uD074\uB9AD\uD558\uC5EC \uD654\uBA74 \uD558\uB2E8\uC5D0\uC11C \uD3B8\uC9D1\uD560 \uC218 \uC788\uC2B5\uB2C8\uB2E4. \uD3B8\uC9D1 \uAD8C\uD55C\uC740 Wiring Mail List \uBA54\uB274\uC5D0\uC11C \uC9C0\uC815\uB41C \uAD00\uB9AC\uC790\uB9CC \uAC00\uB2A5\uD569\uB2C8\uB2E4.");

		grid = new Grid(allCategoryNoGroup, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.SINGLE);
		grid.setToolTipText("\uB9C8\uC6B0\uC2A4 \uB354\uBE14 \uD074\uB9AD\uC73C\uB85C \uD3B8\uC9D1\uD560 \uC218 \uC788\uC2B5\uB2C8\uB2E4.");
		grid.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseDoubleClick(MouseEvent e)
			{
				GridItem[] gridItems = getSelectedGridItemSet(null);
				if (gridItems == null)
				{
					return;
				}
				for (GridItem rowItem : gridItems)
				{
					String projectCode = rowItem.getText(0);
					projectCombo.select(projectCombo.indexOf(projectCode));
					String wiringType = rowItem.getText(1);
					String cagegoryNo = rowItem.getText(2);
					if (wiringType.equals("1.MAIN"))
					{
						mainCategoryText.setText(cagegoryNo);
					} else if (wiringType.equals("2.ENGINE"))
					{
						engineCategoryText.setText(cagegoryNo);
					} else if (wiringType.equals("3.FLOOR"))
					{
						floorCategoryText.setText(cagegoryNo);
					}
				}
			}
		});
		grid.setHeaderVisible(true);
		grid.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		gridColumn = new GridColumn(grid, SWT.CENTER);
		gridColumn.setText("Project");
		gridColumn.setWidth(50);

		gridColumn_1 = new GridColumn(grid, SWT.NONE);
		gridColumn_1.setText("Wiring Type");
		gridColumn_1.setWidth(80);

		gridColumn_2 = new GridColumn(grid, SWT.NONE);
		gridColumn_2.setText("Category No");
		gridColumn_2.setWidth(700);

		composite_1 = new Composite(allCategoryNoGroup, SWT.NONE);
		composite_1.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		GridLayout gl_composite_1 = new GridLayout(2, false);
		gl_composite_1.marginHeight = 0;
		composite_1.setLayout(gl_composite_1);

		addButton = new Button(composite_1, SWT.NONE);
		addButton.setToolTipText("\uC544\uB798 \"Project Category No \uD3B8\uC9D1\"\uC758 \uB0B4\uC6A9\uC744 \uCD94\uAC00 \uB610\uB294 \uC5C5\uB370\uC774\uD2B8 \uD569\uB2C8\uB2E4.");
		addButton.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				String projectCode = projectCombo.getText();
				if (projectCode.isEmpty())
				{
					MessageBox.post(shell, "아래 Project Category No 편집에 추가할 Project가 선택되지 않았습니다.", "Project", MessageBox.WARNING);
					return;
				}
				String mainCategory = mainCategoryText.getText();
				if (mainCategory.isEmpty())
				{
					MessageBox.post(shell, "아래 Project Category No 편집에 MAIN Category No가 입력되지 않았습니다.", "Category No", MessageBox.WARNING);
					return;
				}
				String engineCategory = engineCategoryText.getText();
				if (engineCategory.isEmpty())
				{
					MessageBox.post(shell, "아래 Project Category No 편집에 ENGINE Category No가 입력되지 않았습니다.", "Category No", MessageBox.WARNING);
					return;
				}
				String floorCategory = floorCategoryText.getText();
				if (floorCategory.isEmpty())
				{
					MessageBox.post(shell, "아래 Project Category No 편집에 FLOOR Category No가 입력되지 않았습니다.", "Category No", MessageBox.WARNING);
					return;
				}
				String exceptCatString = "[]";
				try
				{
					exceptCatString = checkExceptCategory(projectCode, mainCategory, engineCategory, floorCategory);
				} catch (Exception e1)
				{
					MessageBox.post(shell, e1, true);
					return;
				}
				if (!exceptCatString.equals("[]"))
				{
					MessageBox.post(shell, "입력된 Category No 중 Project Category No에 없는 값이 아래와 같이 포함되어 있습니다.\n수정 버튼을 클릭하여 정보를 업데이트 하시기 바랍니다.\nCategory No : " + exceptCatString, "Category No", MessageBox.WARNING);
					return;
				}
				GridItem[] gridItems = getSelectedGridItemSet(projectCode);
				if (gridItems == null)
				{
					int r = ConfirmDialog.prompt(shell, "추가", "입력한 Project(" + projectCode + ") 및 Category No를 추가하시겠습니까?");
					if (r == 2)
					{
						GridItem mainGridItem = new GridItem(grid, SWT.NONE);
						mainGridItem.setText(0, projectCode);
						mainGridItem.setText(1, "1.MAIN");
						mainGridItem.setText(2, mainCategory);
						GridItem engineGridItem = new GridItem(grid, SWT.NONE);
						engineGridItem.setText(0, projectCode);
						engineGridItem.setText(1, "2.ENGINE");
						engineGridItem.setText(2, engineCategory);
						GridItem floorGridItem = new GridItem(grid, SWT.NONE);
						floorGridItem.setText(0, projectCode);
						floorGridItem.setText(1, "3.FLOOR");
						floorGridItem.setText(2, floorCategory);
						setGridColor();
					}
				} else
				{
					int r = ConfirmDialog.prompt(shell, "변경", "입력한 Project(" + projectCode + ") 및 Category No를 변경하시겠습니까?");
					if (r == 2)
					{
						for (GridItem gridItem : gridItems)
						{
							String wiringType = gridItem.getText(1);
							if (wiringType.equals("1.MAIN"))
							{
								gridItem.setText(2, mainCategory);
							}
							if (wiringType.equals("2.ENGINE"))
							{
								gridItem.setText(2, engineCategory);
							}
							if (wiringType.equals("3.FLOOR"))
							{
								gridItem.setText(2, floorCategory);
							}
						}
						setGridColor();
					}
				}
			}
		});
		addButton.setImage(SWTResourceManager.getImage(WiringCategoryNoDialog.class, "/icons/add_16.png"));
		addButton.setText("\uCD94\uAC00 \uBC0F \uC5C5\uB370\uC774\uD2B8");

		removeButton = new Button(composite_1, SWT.NONE);
		removeButton.setToolTipText("\uC704 \"Project\uBCC4 Category No\" \uB9AC\uC2A4\uD2B8\uC758 \uC120\uD0DD\uB41C \uD504\uB85C\uC81D\uD2B8\uB97C \uC0AD\uC81C\uD569\uB2C8\uB2E4.");
		removeButton.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				GridItem[] gridItems = getSelectedGridItemSet(null);
				if (gridItems == null)
				{
					MessageBox.post(shell, "위 Table 리스트에서 삭제할 Project Category No를 선택하세요", "선택", MessageBox.WARNING);
					return;
				}
				String projectCode = gridItems[0].getText(0);
				int r = ConfirmDialog.prompt(shell, "삭제", "선택한 Project(" + projectCode + ")를 삭제 하시겠습니까?");
				if (r == 2)
				{
					for (GridItem gridItem : gridItems)
					{
						gridItem.dispose();
					}
					setGridColor();
				}
			}
		});
		removeButton.setImage(SWTResourceManager.getImage(WiringCategoryNoDialog.class, "/icons/remove_16.png"));
		removeButton.setText("\uC120\uD0DD\uB41C \uD504\uB85C\uC81D\uD2B8 \uC0AD\uC81C");

		modifyCategoryNoGroup = new Group(allCategoryNoGroup, SWT.NONE);
		modifyCategoryNoGroup.setText("Project Category No \uD3B8\uC9D1");
		modifyCategoryNoGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		modifyCategoryNoGroup.setLayout(new GridLayout(5, false));

		lblNewLabel = new Label(modifyCategoryNoGroup, SWT.NONE);
		lblNewLabel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		lblNewLabel.setText("Project");

		projectCombo = new Combo(modifyCategoryNoGroup, SWT.READ_ONLY);
		projectCombo.setItems(new String[] {});
		GridData gd_projectCombo = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_projectCombo.widthHint = 72;
		projectCombo.setLayoutData(gd_projectCombo);
		new Label(modifyCategoryNoGroup, SWT.NONE);
		new Label(modifyCategoryNoGroup, SWT.NONE);
		new Label(modifyCategoryNoGroup, SWT.NONE);

		lblNewLabel_1 = new Label(modifyCategoryNoGroup, SWT.NONE);
		lblNewLabel_1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_1.setText("1.MAIN");

		mainCategoryText = new Text(modifyCategoryNoGroup, SWT.BORDER);
		mainCategoryText.setEditable(false);
		mainCategoryText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));

		modifyMainButton = new Button(modifyCategoryNoGroup, SWT.NONE);
		modifyMainButton.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				modifyCategoryAction("MAIN", mainCategoryText);
			}
		});
		modifyMainButton.setImage(SWTResourceManager.getImage(WiringCategoryNoDialog.class, "/com/teamcenter/rac/util/images/editor_16.png"));
		modifyMainButton.setText("\uC218\uC815");

		lblNewLabel_2 = new Label(modifyCategoryNoGroup, SWT.NONE);
		lblNewLabel_2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_2.setText("2.ENGINE");

		engineCategoryText = new Text(modifyCategoryNoGroup, SWT.BORDER);
		engineCategoryText.setEditable(false);
		engineCategoryText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));

		modifyEngineButton = new Button(modifyCategoryNoGroup, SWT.NONE);
		modifyEngineButton.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				modifyCategoryAction("ENGINE", engineCategoryText);
			}
		});
		modifyEngineButton.setText("\uC218\uC815");
		modifyEngineButton.setImage(SWTResourceManager.getImage(WiringCategoryNoDialog.class, "/com/teamcenter/rac/util/images/editor_16.png"));

		lblNewLabel_3 = new Label(modifyCategoryNoGroup, SWT.NONE);
		lblNewLabel_3.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_3.setText("3.FLOOR");

		floorCategoryText = new Text(modifyCategoryNoGroup, SWT.BORDER);
		floorCategoryText.setEditable(false);
		floorCategoryText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));

		modifyFloorButton = new Button(modifyCategoryNoGroup, SWT.NONE);
		modifyFloorButton.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				modifyCategoryAction("FLOOR", floorCategoryText);
			}
		});
		modifyFloorButton.setText("\uC218\uC815");
		modifyFloorButton.setImage(SWTResourceManager.getImage(WiringCategoryNoDialog.class, "/com/teamcenter/rac/util/images/editor_16.png"));
		composite = new Composite(shell, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		composite.setLayout(new GridLayout(4, false));

		exportExcelButton = new Button(composite, SWT.NONE);
		exportExcelButton.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				ExportPOIExcel.exportDialog(shell, grid, "Wiring Category No", "프로젝트별 Wiring Category No", "", null);
			}
		});
		exportExcelButton.setImage(SWTResourceManager.getImage(WiringCategoryNoDialog.class, "/com/ssangyong/common/images/excel_16.png"));
		exportExcelButton.setText("\uC5D1\uC140 \uC800\uC7A5");

		reLoadButton = new Button(composite, SWT.NONE);
		reLoadButton.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				int r = ConfirmDialog.prompt(shell, "다시 불러오기", "저장된 데이터를 다시 불러오기 하시겠습니까?\n편집중인 데이터는 모두 사라집니다.");
				if (r == 2)
				{
					new Thread(new Runnable()
					{
						@Override
						public void run()
						{
							loadAction();
							MessageBox.post(shell, "다시 불러오기가 완료되었습니다", "불러오기", MessageBox.INFORMATION);
						}
					}).start();
				}
			}
		});
		reLoadButton.setImage(SWTResourceManager.getImage(WiringCategoryNoDialog.class, "/icons/refresh_16.png"));
		reLoadButton.setText("\uB2E4\uC2DC \uBD88\uB7EC\uC624\uAE30");

		saveButton = new Button(composite, SWT.NONE);
		saveButton.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				saveAction();
			}
		});
		saveButton.setImage(SWTResourceManager.getImage(WiringCategoryNoDialog.class, "/icons_16/save_16.png"));
		saveButton.setText("\uC800\uC7A5");

		closeButton = new Button(composite, SWT.NONE);
		closeButton.setImage(SWTResourceManager.getImage(WiringCategoryNoDialog.class, "/com/teamcenter/rac/util/images/close_16.png"));
		closeButton.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				shell.dispose();
			}
		});
		closeButton.setText("\uB2EB\uAE30");
	}

	protected String checkExceptCategory(String projectCode, String mainCategory, String engineCategory, String floorCategory) throws Exception
	{
		ArrayList<String> currentCategoryList = new ArrayList<String>();
		String[] mainCatArray = mainCategory.split(" ");
		for (String mainCat : mainCatArray)
		{
			currentCategoryList.add(mainCat);
		}
		String[] engineCatArray = engineCategory.split(" ");
		for (String engineCat : engineCatArray)
		{
			if (!currentCategoryList.contains(engineCat))
			{
				currentCategoryList.add(engineCat);
			}
		}
		String[] floorCatArray = floorCategory.split(" ");
		for (String floorCat : floorCatArray)
		{
			if (!currentCategoryList.contains(floorCat))
			{
				currentCategoryList.add(floorCat);
			}
		}
		ArraySorter.sort(currentCategoryList);
		Hashtable<String, String> categoryHash = getOspecCategoryInfo(projectCode);
		if (categoryHash == null)
		{
			throw new Exception("Project에 대한 Category 정보(OSpec)를 찾을 수 없습니다.");
		}

		ArrayList<String> exceptCatList = new ArrayList<String>();
		for (String catString : currentCategoryList)
		{
			//카테고리 리스트에 없는 대상이 포함 된 경우
			if (!categoryHash.containsKey(catString))
			{
				exceptCatList.add(catString);
			}
		}
		return exceptCatList.toString();
	}

	protected void modifyCategoryAction(String categoryType, Text categoryText)
	{
		String projectCode = projectCombo.getText();
		if (projectCode.isEmpty())
		{
			MessageBox.post(shell, "Project를 선택하세요", "선택", MessageBox.WARNING);
			return;
		}
		Hashtable<String, String> categoryHash = getOspecCategoryInfo(projectCode);
		if (categoryHash == null)
		{
			MessageBox.post(shell, "Project에 대한 Category 정보(OSpec)를 찾을 수 없습니다.", "오류", MessageBox.ERROR);
			return;
		}
		String categoryNo = categoryText.getText();
		ModifyCategoryNoDialog modifyCategoryDialog = new ModifyCategoryNoDialog(shell, projectCode + ", " + categoryType + " Category", categoryHash, categoryNo);
		categoryNo = modifyCategoryDialog.open();
		categoryText.setText(categoryNo);
	}

	private Hashtable<String, String> getOspecCategoryInfo(final String projectCode)
	{
		Hashtable<String, String> categoryHash = ospecHash.get(projectCode);
		if (categoryHash != null && !categoryHash.isEmpty())
		{
			return categoryHash;
		}
		categoryHash = new Hashtable<String, String>();
		OSpec ospec = null;
		try
		{
			ospec = OpUtil.getOspec(projectCode);
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		if (ospec == null)
		{
			return null;
		}
		//카테고리 데이터 추출.
		Collection<HashMap<String, OpCategory>> col = ospec.getCategory().values();
		for (HashMap<String, OpCategory> colMap : col)
		{
			Set<String> set = colMap.keySet();
			for (String s : set)
			{
				OpCategory oc = colMap.get(s);
				String cat = oc.getCategory();
				String catName = oc.getCategoryName();
				if (!categoryHash.containsKey(cat) && !cat.isEmpty())
				{
					categoryHash.put(cat, catName);
				}
			}
		}
		ospecHash.put(projectCode, categoryHash);
		if (categoryHash.isEmpty())
		{
			categoryHash = null;
		}
		return categoryHash;
	}

	protected void saveAction()
	{
		GridItem[] gridItems = grid.getItems();
		if (gridItems == null || gridItems.length == 0)
		{
			MessageBox.post(shell, "저장할 데이터가 없습니다.", "저장", MessageBox.WARNING);
			return;
		}
		int r = ConfirmDialog.prompt(shell, "저장", "저장하시겠습니까?");
		if (r == 2)
		{
			SYMCRemoteUtil remoteQuery = new SYMCRemoteUtil();
			try
			{
				remoteQuery.execute("com.ssangyong.service.WiringCheckService", "deleteWiringCategoryNo", null);
			} catch (Exception e1)
			{
				e1.printStackTrace();
				MessageBox.post(shell, e1, true);
				return;
			}
			ArrayList userList = new ArrayList();
			for (GridItem gridItem : gridItems)
			{
				String projectCode = gridItem.getText(0);
				String wiringType = gridItem.getText(1);
				String categoryNoString = gridItem.getText(2);
				String[] categoryNoArray = categoryNoString.split(" ");
				for (String categoryNo : categoryNoArray)
				{
					DataSet ds = new DataSet();
					ds.put("PROJECT_CODE", projectCode);
					ds.put("WIRING_TYPE", wiringType);
					ds.put("CATEGORY_NO", categoryNo);
					userList.add(ds);
				}
			}
			try
			{
				DataSet ds = new DataSet();
				ds.put("CATEGORY_NO_LIST", userList);
				remoteQuery.execute("com.ssangyong.service.WiringCheckService", "insertWiringCategoryNo", ds);
			} catch (Exception e)
			{
				e.printStackTrace();
				MessageBox.post(shell, e, true);
				return;
			}
			MessageBox.post(shell, "저장이 완료되었습니다", "저장", MessageBox.INFORMATION);
		}
	}

	protected void loadAction()
	{
		//프로젝트 코드를 combo에 넣는다.
		Display.getDefault().syncExec(new Runnable()
		{
			@Override
			public void run()
			{
				grid.removeAll();
				projectCombo.setEnabled(false);
				projectCombo.setText("loading...");
			}
		});
		String[] lovs = null;
		try
		{
			lovs = SYMCLOVLoader.getLOV("S7_PROJECT_CODE").getListOfValues().getLOVDisplayValues();
		} catch (Exception e1)
		{
			e1.printStackTrace();
			MessageBox.post(shell, e1, true);
			return;
		}
		ArraySorter.sort(lovs);
		for (final String lov : lovs)
		{
			final String userString = lov;
			Display.getDefault().syncExec(new Runnable()
			{
				@Override
				public void run()
				{
					projectCombo.add(userString);
				}
			});
		}
		Display.getDefault().syncExec(new Runnable()
		{
			@Override
			public void run()
			{
				projectCombo.setEnabled(true);
				projectCombo.setText("");
			}
		});

		//category 정보를 불러온다.
		SYMCRemoteUtil remoteQuery = new SYMCRemoteUtil();
		ArrayList<HashMap> resultList = null;
		try
		{
			resultList = (ArrayList<HashMap>) remoteQuery.execute("com.ssangyong.service.WiringCheckService", "getWiringCategoryNo", null);
		} catch (Exception e1)
		{
			e1.printStackTrace();
			MessageBox.post(shell, e1, true);
			return;
		}
		if (resultList == null)
		{
			return;
		}
		for (HashMap resultRow : resultList)
		{
			final String projectCode = (String) resultRow.get("PROJECT_CODE");
			final String wiringType = (String) resultRow.get("WIRING_TYPE");
			final String categoryNo = (String) resultRow.get("CATEGORY_NO");
			Display.getDefault().syncExec(new Runnable()
			{
				@Override
				public void run()
				{
					GridItem gridItem = new GridItem(grid, SWT.NONE);
					gridItem.setText(0, projectCode);
					gridItem.setText(1, wiringType);
					gridItem.setText(2, categoryNo);
				}
			});
		}
		//색깔...
		Display.getDefault().asyncExec(new Runnable()
		{
			@Override
			public void run()
			{
				setGridColor();
			}
		});
		//관리자인지 체크함...
		try
		{
			DataSet ds = new DataSet();
			ds.put("USER_ID", session.getUser().getUserId());
			final ArrayList<HashMap> rList = (ArrayList<HashMap>) remoteQuery.execute("com.ssangyong.service.WiringCheckService", "getWiringMailList", ds);
			Display.getDefault().syncExec(new Runnable()
			{
				@Override
				public void run()
				{
					try
					{
						isWritable((rList != null && !rList.isEmpty() && rList.get(0).get("IS_ADMIN").equals("Y")) || session.isUserSystemAdmin());
					} catch (TCException e)
					{
						e.printStackTrace();
						MessageBox.post(shell, e, true);
					}
				}
			});
		} catch (Exception e)
		{
			e.printStackTrace();
			MessageBox.post(shell, e, true);
			return;
		}
	}

	private GridItem[] getSelectedGridItemSet(String selectProjectCode)
	{
		GridItem[] selectedGridItems = null;
		String projectCode = selectProjectCode;
		if (selectProjectCode == null || selectProjectCode.isEmpty())
		{
			GridItem[] gridItems = grid.getSelection();
			if (gridItems == null || gridItems.length == 0)
			{
				return null;
			}
			GridItem gridItem = gridItems[0];
			projectCode = gridItem.getText(0);
		}
		if (projectCode == null || projectCode.isEmpty())
		{
			return null;
		}
		int count = 0;
		for (GridItem rowItem : grid.getItems())
		{
			String pjtCode = rowItem.getText(0);
			if (projectCode.equals(pjtCode))
			{
				if (selectedGridItems == null)
				{
					selectedGridItems = new GridItem[3];
				}
				selectedGridItems[count] = rowItem;
				count++;
				if (count == 3)
				{
					break;
				}
			}
		}
		return selectedGridItems;
	}

	private void setGridColor()
	{
		boolean isColor = true;
		GridItem[] gridItems = grid.getItems();
		for (int i = 0; gridItems != null && i < gridItems.length; i++)
		{
			if (i % 3 == 0)
			{
				isColor = !isColor;
			}
			GridItem gridItem = gridItems[i];
			if (isColor)
			{
				gridItem.setBackground(0, SWTResourceManager.getColor(184, 215, 252));
				gridItem.setBackground(1, SWTResourceManager.getColor(184, 215, 252));
				gridItem.setBackground(2, SWTResourceManager.getColor(184, 215, 252));
			} else
			{
				gridItem.setBackground(0, SWTResourceManager.getColor(SWT.COLOR_WHITE));
				gridItem.setBackground(1, SWTResourceManager.getColor(SWT.COLOR_WHITE));
				gridItem.setBackground(2, SWTResourceManager.getColor(SWT.COLOR_WHITE));
			}
		}
	}

	private void isWritable(boolean isWritable)
	{
		addButton.setEnabled(isWritable);
		removeButton.setEnabled(isWritable);
		modifyMainButton.setEnabled(isWritable);
		modifyEngineButton.setEnabled(isWritable);
		modifyFloorButton.setEnabled(isWritable);
		saveButton.setEnabled(isWritable);
	}
}

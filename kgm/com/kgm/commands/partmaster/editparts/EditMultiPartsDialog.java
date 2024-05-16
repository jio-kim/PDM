package com.kgm.commands.partmaster.editparts;

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;

import com.kgm.common.swtsearch.SearchItemDialog;
import com.kgm.common.swtsearch.SearchItemRevDialog;
import com.kgm.common.utils.CustomUtil;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentListOfValues;
import com.teamcenter.rac.kernel.TCComponentListOfValuesType;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.util.ConfirmDialog;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.SWTUIUtilities;

public class EditMultiPartsDialog extends Dialog
{
	private String[] columnProperties = { "s7_PROJECT_CODE", "item_id", "item_revision_id", "object_name", "s7_REGULATION", "s7_SHOWN_PART_NO", "s7_MATERIAL", "s7_EST_WEIGHT", "s7_ACT_WEIGHT", "s7_RESPONSIBILITY", "s7_BUDGET_CODE" };
	private String[] columnDisplayNames = { "Proj", "Part No", "Part Rev", "Part Name", "CAT", "Shown-On", "Material", "Est. Weight", "Act. Weight", "DWG Creator", "System Code" };
	private int[] columnWidths = { 45, 100, 40, 250, 50, 100, 100, 100, 100, 100, 100 };
	private ArrayList<TCComponentItemRevision> revisionList;
	private EditMultiPartsManager editMultiPartsManager;
	protected Object result;
	protected Shell shell;
	private Composite composite;
	private Label label;
	private Composite composite_1;
	private Button okButton;
	private Button applyButton;
	private Button cancelButton;
	private Composite composite_2;
	private Table table;
	private TableColumn tblclmnNewColumn;
	private Label lblNewLabel;
	private Combo projectCodeCombo;
	private Label lblNewLabel_1;
	private Combo categoryCombo;
	private Button updateProjectCodeButton;
	private Button updateCategoryButton;
	private Label lblNewLabel_2;
	private Text shownOnText;
	private Button searchShownOnButton;
	private Button updateShownOnButton;
	private Label lblNewLabel_3;
	private Button updateMaterialButton;
	private Label lblNewLabel_4;
	private Text estWeightText;
	private Button updateEstWeightButton;
	private Label lblNewLabel_5;
	private Text actWeightText;
	private Button updateActWeightButton;
	private Label lblNewLabel_6;
	private Combo dwgCreatorCombo;
	private Button updateDwgCreatorButton;
	private Label lblNewLabel_7;
	private Combo systemCodeCombo;
	private Button updateSystemCodeButton;
	private Button searchMaterialButton;
	private Text materialText;
	private Button removeMaterialButton;
	private Button removeShownOnButton;
	private Menu menu;
	private MenuItem mntmNewItem;
	private MenuItem mntmNewItem_1;
	private MenuItem mntmNewItem_2;

	/**
	 * Create the dialog.
	 * 
	 * @param parent
	 * @param style
	 */
	public EditMultiPartsDialog(Shell parent, ArrayList<TCComponentItemRevision> _revisionList)
	{
		super(parent);
		revisionList = _revisionList;
	}

	/**
	 * Open the dialog.
	 * 
	 * @return the result
	 */
	public Object open()
	{
		try
		{
			createContents();
		} catch (TCException e)
		{
			e.printStackTrace();
			MessageBox.post(shell, e, true);
			return null;
		}
		SWTUIUtilities.centerInParent(getParent(), shell);
		shell.open();
		shell.layout();
		loadData();
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

	private void loadData()
	{
		//화면 UI 데이터 로딩...
		Display.getDefault().asyncExec(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					TCComponentListOfValues lov = TCComponentListOfValuesType.findLOVByName("S7_PROJECT_CODE");
					String[] projectCodeLovString = lov.getListOfValues().getStringListOfValues();
					for (int i = 0; i < projectCodeLovString.length; i++)
					{
						projectCodeCombo.add(projectCodeLovString[i]);
					}
					lov = TCComponentListOfValuesType.findLOVByName("S7_CATEGORY");
					String[] categoryLovString = lov.getListOfValues().getStringListOfValues();
					for (int i = 0; i < categoryLovString.length; i++)
					{
						categoryCombo.add(categoryLovString[i]);
					}
					lov = TCComponentListOfValuesType.findLOVByName("S7_RESPONSIBILITY");
					String[] str = lov.getListOfValues().getStringListOfValues();
					for (int i = 0; i < str.length; i++)
					{
						if (!str[i].startsWith("White Box") && !str[i].startsWith("Black Box") && !str[i].startsWith("Gray Box") && !str[i].startsWith("SYMC"))
						{
							dwgCreatorCombo.add(str[i]);
						}
					}
					lov = TCComponentListOfValuesType.findLOVByName("s7_SYSTEM_CODE");
					String[] systemCodeLovString = lov.getListOfValues().getStringListOfValues();
					for (int i = 0; i < systemCodeLovString.length; i++)
					{
						systemCodeCombo.add(systemCodeLovString[i]);
					}
				} catch (TCException e)
				{
					e.printStackTrace();
					MessageBox.post(shell, e, true);
				}
			}
		});
		//리비전 정보 데이터 로딩...
		editMultiPartsManager = new EditMultiPartsManager(revisionList, columnProperties, table);
		editMultiPartsManager.loadData();
	}

	/**
	 * Create contents of the dialog.
	 */
	private void createContents() throws TCException
	{
		shell = new Shell(getParent(), SWT.BORDER | SWT.RESIZE | SWT.TITLE | SWT.PRIMARY_MODAL);
		shell.setImage(SWTResourceManager.getImage(EditMultiPartsDialog.class, "/icons/properties_16.png"));
		shell.setBackgroundMode(SWT.INHERIT_FORCE);
		shell.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		shell.setSize(1146, 531);
		shell.setText("Edit Multi Parts(Vehpart Rev)");
		shell.setLayout(new GridLayout());

		composite = new Composite(shell, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		composite.setLayout(new GridLayout(1, false));

		table = new Table(composite, SWT.BORDER | SWT.FULL_SELECTION | SWT.VIRTUAL | SWT.MULTI);
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		menu = new Menu(table);
		table.setMenu(menu);

		mntmNewItem = new MenuItem(menu, SWT.NONE);
		mntmNewItem.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				table.selectAll();
			}
		});
		mntmNewItem.setText("\uC804\uCCB4\uC120\uD0DD");

		mntmNewItem_1 = new MenuItem(menu, SWT.NONE);
		mntmNewItem_1.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				table.deselectAll();
			}
		});
		mntmNewItem_1.setText("\uC120\uD0DD\uD574\uC81C");

		new MenuItem(menu, SWT.SEPARATOR);

		mntmNewItem_2 = new MenuItem(menu, SWT.NONE);
		mntmNewItem_2.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				editMultiPartsManager.loadData();
			}
		});
		mntmNewItem_2.setText("\uB370\uC774\uD130 \uCD08\uAE30\uD654");

		for (int col = 0; col < columnProperties.length; col++)
		{
			tblclmnNewColumn = new TableColumn(table, SWT.NONE);
			tblclmnNewColumn.setWidth(columnWidths[col]);
			tblclmnNewColumn.setText(columnDisplayNames[col]);
			tblclmnNewColumn.setData(columnProperties[col]);
		}

		composite_2 = new Composite(shell, SWT.NONE);
		composite_2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		composite_2.setLayout(new GridLayout(16, false));

		lblNewLabel = new Label(composite_2, SWT.NONE);
		lblNewLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel.setText("Project Code : ");

		projectCodeCombo = new Combo(composite_2, SWT.READ_ONLY);
		projectCodeCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

		updateProjectCodeButton = new Button(composite_2, SWT.NONE);
		updateProjectCodeButton.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				if (table.getSelectionCount() == 0)
				{
					MessageBox.post(shell, "업데이트 할 대상을 선택하여 주십시오.", "선택오류", MessageBox.ERROR);
					return;
				}
				String projectCodeString = projectCodeCombo.getText();
				if (projectCodeString.trim().isEmpty())
				{
					MessageBox.post(shell, "Project Code는 필수 항목입니다.", "입력오류", MessageBox.ERROR);
					return;
				}
				int r = ConfirmDialog.prompt(shell, "업데이트", "선택된 행(" + table.getSelectionCount() + "건)에 입력한 데이터를 업데이트 하시겠습니까?");
				if (r != 2)
				{
					return;
				}
				try
				{
					editMultiPartsManager.setData("s7_PROJECT_CODE", projectCodeString);
				} catch (Exception e1)
				{
					e1.printStackTrace();
					MessageBox.post(shell, e1, true);
				}
			}
		});
		updateProjectCodeButton.setToolTipText("\uC120\uD0DD\uD55C \uB300\uC0C1\uC5D0 \uC5C5\uB370\uC774\uD2B8");
		updateProjectCodeButton.setImage(SWTResourceManager.getImage(EditMultiPartsDialog.class, "/com/kgm/common/images/apply_16.png"));

		lblNewLabel_1 = new Label(composite_2, SWT.NONE);
		lblNewLabel_1.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_1.setText("Category : ");

		categoryCombo = new Combo(composite_2, SWT.READ_ONLY);
		categoryCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

		updateCategoryButton = new Button(composite_2, SWT.NONE);
		updateCategoryButton.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				if (table.getSelectionCount() == 0)
				{
					MessageBox.post(shell, "업데이트 할 대상을 선택하여 주십시오.", "선택오류", MessageBox.ERROR);
					return;
				}
				String categoryString = categoryCombo.getText();
				if (categoryString.trim().isEmpty())
				{
					MessageBox.post(shell, "Category는 필수 항목입니다.", "입력오류", MessageBox.ERROR);
					return;
				}
				int r = ConfirmDialog.prompt(shell, "업데이트", "선택된 행(" + table.getSelectionCount() + "건)에 입력한 데이터를 업데이트 하시겠습니까?");
				if (r != 2)
				{
					return;
				}
				try
				{
					editMultiPartsManager.setData("s7_REGULATION", categoryString);
				} catch (Exception e1)
				{
					e1.printStackTrace();
					MessageBox.post(shell, e1, true);
				}
			}
		});
		updateCategoryButton.setToolTipText("\uC120\uD0DD\uD55C \uB300\uC0C1\uC5D0 \uC5C5\uB370\uC774\uD2B8");
		updateCategoryButton.setImage(SWTResourceManager.getImage(EditMultiPartsDialog.class, "/com/kgm/common/images/apply_16.png"));

		lblNewLabel_2 = new Label(composite_2, SWT.NONE);
		lblNewLabel_2.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_2.setText("Shown On No. : ");

		shownOnText = new Text(composite_2, SWT.BORDER);
		shownOnText.setEditable(false);
		GridData gd_shownOnText = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_shownOnText.widthHint = 118;
		shownOnText.setLayoutData(gd_shownOnText);

		searchShownOnButton = new Button(composite_2, SWT.NONE);
		searchShownOnButton.setToolTipText("\uAC80\uC0C9");
		searchShownOnButton.setImage(SWTResourceManager.getImage(EditMultiPartsDialog.class, "/com/kgm/common/images/search_16.png"));
		searchShownOnButton.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				SearchItemDialog itemDialog = new SearchItemDialog(shell, SWT.SINGLE, "S7_Vehpart");
				// 선택된 Revision
				TCComponentItem[] selectedItems = (TCComponentItem[]) itemDialog.open();
				if (selectedItems != null)
				{
					try
					{
						selectedItems[0].refresh();
						String shownOnPartDrawingSize = selectedItems[0].getLatestItemRevision().getProperty("s7_DRW_SIZE");
						if (shownOnPartDrawingSize.trim().isEmpty() || shownOnPartDrawingSize.trim().equals("."))
						{
							MessageBox.post(shell, "선택한 Shown On Part에 Drw Size 값이 없습니다.", "Shown On Part", MessageBox.WARNING);
							return;
						}
						shownOnText.setText(selectedItems[0].getProperty("item_id"));
						shownOnText.setData(selectedItems[0]);
					} catch (TCException e1)
					{
						e1.printStackTrace();
						MessageBox.post(shell, e1, true);
					}
				}
			}
		});

		removeShownOnButton = new Button(composite_2, SWT.NONE);
		removeShownOnButton.setToolTipText("\uC9C0\uC6B0\uAE30");
		removeShownOnButton.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				shownOnText.setText("");
				shownOnText.setData(null);
			}
		});
		removeShownOnButton.setImage(SWTResourceManager.getImage(EditMultiPartsDialog.class, "/com/kgm/common/images/clear_16.png"));

		updateShownOnButton = new Button(composite_2, SWT.NONE);
		updateShownOnButton.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				if (table.getSelectionCount() == 0)
				{
					MessageBox.post(shell, "업데이트 할 대상을 선택하여 주십시오.", "선택오류", MessageBox.ERROR);
					return;
				}
				Object shownOnPartObject = shownOnText.getData();
				int r = ConfirmDialog.prompt(shell, "업데이트", "선택된 행(" + table.getSelectionCount() + "건)에 입력한 데이터를 업데이트 하시겠습니까?");
				if (r != 2)
				{
					return;
				}
				try
				{
					editMultiPartsManager.setData("s7_SHOWN_PART_NO", shownOnPartObject);
				} catch (Exception e1)
				{
					e1.printStackTrace();
					MessageBox.post(shell, e1, true);
				}
			}
		});
		updateShownOnButton.setToolTipText("\uC120\uD0DD\uD55C \uB300\uC0C1\uC5D0 \uC5C5\uB370\uC774\uD2B8");
		updateShownOnButton.setImage(SWTResourceManager.getImage(EditMultiPartsDialog.class, "/com/kgm/common/images/apply_16.png"));

		lblNewLabel_3 = new Label(composite_2, SWT.NONE);
		lblNewLabel_3.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_3.setText("Material : ");

		materialText = new Text(composite_2, SWT.BORDER);
		materialText.setEditable(false);
		GridData gd_materialText = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_materialText.widthHint = 105;
		materialText.setLayoutData(gd_materialText);

		searchMaterialButton = new Button(composite_2, SWT.NONE);
		searchMaterialButton.setToolTipText("\uAC80\uC0C9");
		searchMaterialButton.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				SearchItemRevDialog itemDialog = new SearchItemRevDialog(shell, SWT.SINGLE, "S7_MaterialRevision");
				// 선택된 Revision
				//20230831 cf-4357 seho 파트 검색 dialog 수정으로..  검색 결과 부분 수정.
				TCComponent[] selectedItems = (TCComponent[]) itemDialog.open();
				if (selectedItems != null)
				{
					try
					{
						materialText.setText(selectedItems[0].getProperty("item_id"));
						materialText.setData(selectedItems[0]);
					} catch (TCException e1)
					{
						e1.printStackTrace();
						MessageBox.post(shell, e1, true);
					}
				}
			}
		});
		searchMaterialButton.setImage(SWTResourceManager.getImage(EditMultiPartsDialog.class, "/com/kgm/common/images/search_16.png"));

		removeMaterialButton = new Button(composite_2, SWT.NONE);
		removeMaterialButton.setToolTipText("\uC9C0\uC6B0\uAE30");
		removeMaterialButton.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				materialText.setText("");
				materialText.setData(null);
			}
		});
		removeMaterialButton.setImage(SWTResourceManager.getImage(EditMultiPartsDialog.class, "/com/kgm/common/images/clear_16.png"));

		updateMaterialButton = new Button(composite_2, SWT.NONE);
		updateMaterialButton.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				if (table.getSelectionCount() == 0)
				{
					MessageBox.post(shell, "업데이트 할 대상을 선택하여 주십시오.", "선택오류", MessageBox.ERROR);
					return;
				}
				Object materialData = materialText.getData();
				int r = ConfirmDialog.prompt(shell, "업데이트", "선택된 행(" + table.getSelectionCount() + "건)에 입력한 데이터를 업데이트 하시겠습니까?");
				if (r != 2)
				{
					return;
				}
				try
				{
					editMultiPartsManager.setData("s7_MATERIAL", materialData);
				} catch (Exception e1)
				{
					e1.printStackTrace();
					MessageBox.post(shell, e1, true);
				}
			}
		});
		updateMaterialButton.setToolTipText("\uC120\uD0DD\uD55C \uB300\uC0C1\uC5D0 \uC5C5\uB370\uC774\uD2B8");
		updateMaterialButton.setImage(SWTResourceManager.getImage(EditMultiPartsDialog.class, "/com/kgm/common/images/apply_16.png"));

		lblNewLabel_4 = new Label(composite_2, SWT.NONE);
		lblNewLabel_4.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_4.setText("Est. Weight : ");

		estWeightText = new Text(composite_2, SWT.BORDER);
		estWeightText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		estWeightText.addListener(SWT.Verify, new Listener()
		{
			public void handleEvent(Event e)
			{
				String string = e.text;
				char[] chars = new char[string.length()];
				string.getChars(0, chars.length, chars, 0);
				for (int i = 0; i < chars.length; i++)
				{
					if (!(('0' <= chars[i] && chars[i] <= '9') || chars[i] == '.'))
					{
						e.doit = false;
						return;
					}
				}
			}
		});

		updateEstWeightButton = new Button(composite_2, SWT.NONE);
		updateEstWeightButton.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				if (table.getSelectionCount() == 0)
				{
					MessageBox.post(shell, "업데이트 할 대상을 선택하여 주십시오.", "선택오류", MessageBox.ERROR);
					return;
				}
				String weightString = estWeightText.getText();
				if (weightString.trim().isEmpty())
				{
					MessageBox.post(shell, "Est. Weight를 입력하여주십시오.", "입력오류", MessageBox.ERROR);
					return;
				}
				double estWeight = 0.0;
				try
				{
					estWeight = Double.parseDouble(weightString);
				} catch (NumberFormatException e1)
				{
					e1.printStackTrace();
					MessageBox.post(shell, "Est. Weight를 정확히 입력하여주십시오.", "입력오류", MessageBox.ERROR);
					return;
				}
				if (estWeight == 0)
				{
					MessageBox.post(shell, "Est. Weight는 0을 입력 할 수 없습니다.", "입력오류", MessageBox.ERROR);
					return;
				}
				if (!checkDoubleLimitingSize(weightString, 3, 4))
				{
					MessageBox.post(shell, "Est. Weight는 정수 3, 소수점 이하 4자리 까지 가능합니다.", "입력오류", MessageBox.ERROR);
					return;
				}
				int r = ConfirmDialog.prompt(shell, "업데이트", "선택된 행(" + table.getSelectionCount() + "건)에 입력한 데이터를 업데이트 하시겠습니까?");
				if (r != 2)
				{
					return;
				}
				try
				{
					editMultiPartsManager.setData("s7_EST_WEIGHT", estWeight);
				} catch (Exception e1)
				{
					e1.printStackTrace();
					MessageBox.post(shell, e1, true);
				}
			}
		});
		updateEstWeightButton.setToolTipText("\uC120\uD0DD\uD55C \uB300\uC0C1\uC5D0 \uC5C5\uB370\uC774\uD2B8");
		updateEstWeightButton.setImage(SWTResourceManager.getImage(EditMultiPartsDialog.class, "/com/kgm/common/images/apply_16.png"));

		lblNewLabel_5 = new Label(composite_2, SWT.NONE);
		lblNewLabel_5.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_5.setText("Act. Weight : ");

		actWeightText = new Text(composite_2, SWT.BORDER);
		actWeightText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

		updateActWeightButton = new Button(composite_2, SWT.NONE);
		updateActWeightButton.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				if (table.getSelectionCount() == 0)
				{
					MessageBox.post(shell, "업데이트 할 대상을 선택하여 주십시오.", "선택오류", MessageBox.ERROR);
					return;
				}
				String weightString = actWeightText.getText();
				if (weightString.trim().isEmpty())
				{
					MessageBox.post(shell, "Act. Weight를 입력하여주십시오.", "입력오류", MessageBox.ERROR);
					return;
				}
				double actWeight = 0.0;
				try
				{
					actWeight = Double.parseDouble(weightString);
				} catch (NumberFormatException e1)
				{
					e1.printStackTrace();
					MessageBox.post(shell, "Act. Weight를 정확히 입력하여주십시오.", "입력오류", MessageBox.ERROR);
					return;
				}
				if (actWeight == 0)
				{
					MessageBox.post(shell, "Act. Weight는 0을 입력 할 수 없습니다.", "입력오류", MessageBox.ERROR);
					return;
				}
				if (!checkDoubleLimitingSize(weightString, 8, 4))
				{
					MessageBox.post(shell, "Act. Weight는 정수 8, 소수점 이하 4자리 까지 가능합니다.", "입력오류", MessageBox.ERROR);
					return;
				}
				int r = ConfirmDialog.prompt(shell, "업데이트", "선택된 행(" + table.getSelectionCount() + "건)에 입력한 데이터를 업데이트 하시겠습니까?");
				if (r != 2)
				{
					return;
				}
				try
				{
					editMultiPartsManager.setData("s7_ACT_WEIGHT", actWeight);
				} catch (Exception e1)
				{
					e1.printStackTrace();
					MessageBox.post(shell, e1, true);
				}
			}
		});
		updateActWeightButton.setToolTipText("\uC120\uD0DD\uD55C \uB300\uC0C1\uC5D0 \uC5C5\uB370\uC774\uD2B8");
		updateActWeightButton.setImage(SWTResourceManager.getImage(EditMultiPartsDialog.class, "/com/kgm/common/images/apply_16.png"));

		lblNewLabel_6 = new Label(composite_2, SWT.NONE);
		lblNewLabel_6.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_6.setText("DWG Creator : ");

		dwgCreatorCombo = new Combo(composite_2, SWT.READ_ONLY);
		dwgCreatorCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 3, 1));

		updateDwgCreatorButton = new Button(composite_2, SWT.NONE);
		updateDwgCreatorButton.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				if (table.getSelectionCount() == 0)
				{
					MessageBox.post(shell, "업데이트 할 대상을 선택하여 주십시오.", "선택오류", MessageBox.ERROR);
					return;
				}
				String dwgCreatorString = dwgCreatorCombo.getText();
				if (dwgCreatorString.trim().isEmpty())
				{
					MessageBox.post(shell, "DWG Creator는 필수 항목입니다.", "입력오류", MessageBox.ERROR);
					return;
				}
				int r = ConfirmDialog.prompt(shell, "업데이트", "선택된 행(" + table.getSelectionCount() + "건)에 입력한 데이터를 업데이트 하시겠습니까?");
				if (r != 2)
				{
					return;
				}
				try
				{
					editMultiPartsManager.setData("s7_RESPONSIBILITY", dwgCreatorString);
				} catch (Exception e1)
				{
					e1.printStackTrace();
					MessageBox.post(shell, e1, true);
				}
			}
		});
		updateDwgCreatorButton.setToolTipText("\uC120\uD0DD\uD55C \uB300\uC0C1\uC5D0 \uC5C5\uB370\uC774\uD2B8");
		updateDwgCreatorButton.setImage(SWTResourceManager.getImage(EditMultiPartsDialog.class, "/com/kgm/common/images/apply_16.png"));

		lblNewLabel_7 = new Label(composite_2, SWT.NONE);
		lblNewLabel_7.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_7.setText("System Code : ");

		systemCodeCombo = new Combo(composite_2, SWT.READ_ONLY);
		systemCodeCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 3, 1));

		updateSystemCodeButton = new Button(composite_2, SWT.NONE);
		updateSystemCodeButton.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				if (table.getSelectionCount() == 0)
				{
					MessageBox.post(shell, "업데이트 할 대상을 선택하여 주십시오.", "선택오류", MessageBox.ERROR);
					return;
				}
				String systemCodeString = systemCodeCombo.getText();
				if (systemCodeString.trim().isEmpty())
				{
					MessageBox.post(shell, "System Code는 필수 항목입니다.", "입력오류", MessageBox.ERROR);
					return;
				}
				int r = ConfirmDialog.prompt(shell, "업데이트", "선택된 행(" + table.getSelectionCount() + "건)에 입력한 데이터를 업데이트 하시겠습니까?");
				if (r != 2)
				{
					return;
				}
				try
				{
					editMultiPartsManager.setData("s7_BUDGET_CODE", systemCodeString);
				} catch (Exception e1)
				{
					e1.printStackTrace();
					MessageBox.post(shell, e1, true);
				}
			}
		});
		updateSystemCodeButton.setToolTipText("\uC120\uD0DD\uD55C \uB300\uC0C1\uC5D0 \uC5C5\uB370\uC774\uD2B8");
		updateSystemCodeButton.setImage(SWTResourceManager.getImage(EditMultiPartsDialog.class, "/com/kgm/common/images/apply_16.png"));

		label = new Label(shell, SWT.SEPARATOR | SWT.HORIZONTAL);
		label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

		composite_1 = new Composite(shell, SWT.NONE);
		composite_1.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, false, false, 1, 1));
		GridLayout gl_composite_1 = new GridLayout(3, false);
		gl_composite_1.marginHeight = 0;
		composite_1.setLayout(gl_composite_1);

		okButton = new Button(composite_1, SWT.NONE);
		okButton.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				if(!isChangeData())
				{
					MessageBox.post(shell, "변경된 정보가 없습니다.", "경고", MessageBox.WARNING);
					return;
				}
				int r = ConfirmDialog.prompt(shell, "저장", "변경된 정보를 저장하시겠습니까?");
				if (r == 2)
				{
					editMultiPartsManager.save(EditMultiPartsManager.OK);
				}
			}
		});
		okButton.setToolTipText("\uC800\uC7A5\uD6C4 \uCC3D \uB2EB\uAE30");
		okButton.setImage(SWTResourceManager.getImage(EditMultiPartsDialog.class, "/com/kgm/common/images/ok_16.png"));
		okButton.setText("OK");

		applyButton = new Button(composite_1, SWT.NONE);
		applyButton.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				if(!isChangeData())
				{
					MessageBox.post(shell, "변경된 정보가 없습니다.", "경고", MessageBox.WARNING);
					return;
				}
				int r = ConfirmDialog.prompt(shell, "저장", "변경된 정보를 저장하시겠습니까?");
				if (r == 2)
				{
					editMultiPartsManager.save(EditMultiPartsManager.APPLY);
				}
			}
		});
		applyButton.setToolTipText("\uC800\uC7A5");
		applyButton.setImage(SWTResourceManager.getImage(EditMultiPartsDialog.class, "/com/kgm/common/images/apply_16.png"));
		applyButton.setText("Apply");

		cancelButton = new Button(composite_1, SWT.NONE);
		cancelButton.setToolTipText("\uCC3D \uB2EB\uAE30");
		cancelButton.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				String changeDataMsg = "";
				if(isChangeData())
				{
					changeDataMsg = "변경된 내역이 존재합니다.\n그래도 ";
				}
				int r = ConfirmDialog.prompt(shell, "취소", changeDataMsg + "창을 닫으시겠습니까?");
				if (r == 2)
				{
					shell.dispose();
				}
			}
		});
		cancelButton.setImage(SWTResourceManager.getImage(EditMultiPartsDialog.class, "/com/kgm/common/images/cancel_16.png"));
		cancelButton.setText("Cancel");
	}

	private boolean checkDoubleLimitingSize(String text, int nLength, int fLength)
	{
		if (CustomUtil.isEmpty(text))
		{
			return true;
		}
		if (text.contains("."))
		{
			String first = text.substring(0, text.lastIndexOf("."));
			String second = text.substring(text.lastIndexOf(".") + 1, text.length());
			if (first.length() > nLength || second.length() > fLength)
			{
				return false;
			}
		} else
		{
			if (text.length() > 3)
			{
				return false;
			}
		}
		return true;
	}

	private boolean isChangeData()
	{
		boolean isChange = false;
		for(TableItem tableItem : table.getItems())
		{
			for(TableColumn tableColumn : table.getColumns())
			{
				int c = table.indexOf(tableColumn);
				String propertyName = (String) tableColumn.getData();
				String propertyString = tableItem.getText(table.indexOf(tableColumn));
				if (!propertyString.equals(tableItem.getData("OLD_" + propertyName)) && c != 1 && c != 2 && c != 3)
				{
					return true;
				}
			}
		}
		return isChange;
	}
}

package com.kgm.commands.variantoptioneditor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import com.kgm.common.dialog.SYMCAbstractDialog;
import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.pse.AbstractPSEApplicationPanel;
import com.teamcenter.rac.pse.common.BOMPanel;
import com.teamcenter.rac.pse.common.BOMTreeTable;
import com.teamcenter.rac.pse.variants.modularvariants.ModularOptionModel;
import com.teamcenter.rac.pse.variants.modularvariants.OVEOption;
import com.teamcenter.rac.util.Registry;

public class MVConditionDialog extends SYMCAbstractDialog implements SelectionListener {

	@SuppressWarnings("unused")
    private TCComponent componentTarget;
	private TCComponentBOMLine bomLine;
	private Registry registry;
	private ModularOptionModel mvModel;
	private Composite optionTableGroup;
	private Composite resultTableGroup;
	private Tree OVTree;
	private Table ovTable;
	private Table resultTable;
	private BOMTreeTable treeTable;
	private Button addButton;
	private Button removeButton;
	private Button clearButton;
	private Composite composit;
	private Button addButton2;
	private Button removeButton2;
	private Button clearButton2;

	/**
	 * 
	 * @copyright : S-PALM
	 * @author : �ְ��
	 * @since : 2013. 1. 7.
	 * @param shell
	 */
	public MVConditionDialog(Shell shell, TCComponentBOMLine paramOfTCComponentBOMLine,
			BOMTreeTable paramBOMTreeTable) {
		super(shell);
		this.registry = Registry.getRegistry(this);

		// Utility���� CurrentApplication �� Application Panel�� ������ ��.
		AbstractAIFUIApplication currentApplication = AIFUtility.getCurrentApplication();
		AbstractPSEApplicationPanel PSEpanel = (AbstractPSEApplicationPanel) ((AbstractAIFUIApplication) currentApplication)
				.getApplicationPanel();
		// BOM Panel�� ������ ��.
		BOMPanel currentBOMPanel = PSEpanel.getCurrentBOMPanel();
		this.treeTable = currentBOMPanel.getTreeTable();
		this.bomLine = paramOfTCComponentBOMLine;
	}

	/**
	 * Option Tree Initialize
	 * 
	 * @Copyright : S-PALM
	 * @author : �ְ��
	 * @since : 2013. 1. 10.
	 * @param bomLine
	 * @param OVTree
	 */
	private void initTree(TCComponentBOMLine bomLine, Tree OVTree) {
		try {
			TCComponentBOMLine localTCComponentBOMLine2 = bomLine;
			componentTarget = localTCComponentBOMLine2;

			// BOMLine�� Option �� Valueȹ��
			this.mvModel = new ModularOptionModel(this.treeTable, localTCComponentBOMLine2, false);
			int[] moduleOptionIdx = mvModel.getAllModuleOptions();
			// Option ȹ��
			for (int idx = 0; idx < moduleOptionIdx.length; idx++) {
				TreeItem optionItem = new TreeItem(OVTree, SWT.NONE | SWT.MULTI);
				OVEOption oveOption = mvModel.getOption(moduleOptionIdx[idx]);
				optionItem.setData(oveOption);
				optionItem.setText(oveOption.toString());
				String[] ValueList = oveOption.comboValues;
				// Value ȹ��
				for (String values : ValueList) {
					TreeItem valueItem = new TreeItem(optionItem, SWT.NONE | SWT.MULTI);
					valueItem.setText(values);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * OV Table ����
	 * 
	 * @Copyright : S-PALM
	 * @author : �ְ��
	 * @since : 2013. 1. 10.
	 */
	private void makeOptionTable() {

		ovTable = new Table(optionTableGroup, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
		ovTable.setHeaderVisible(true);
		ovTable.setLinesVisible(true);
		// TODO Registry ��� �ʿ�
		String[] ovTableTitles = { "Category Description", "Category", "Option Code", "Option Description" };
		for (int i = 0; i < ovTableTitles.length; i++) {
			TableColumn column = new TableColumn(ovTable, SWT.NONE);
			column.setText(ovTableTitles[i]);
		}

		for (int i = 0; i < ovTableTitles.length; i++) {
			if (i == 0) {
				// Category Description�� �����.
			} else {
				ovTable.getColumn(i).pack();
			}
		}
		FormData ovTableData = new FormData(250, 300);
		ovTable.setLayoutData(ovTableData);
	}

	/**
	 * Result Table ����
	 * 
	 * @Copyright : S-PALM
	 * @author : �ְ��
	 * @since : 2013. 1. 10.
	 */
	private void makeResultTable() {

		resultTable = new Table(resultTableGroup, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
		resultTable.setHeaderVisible(true);
		resultTable.setLinesVisible(true);
		String[] resultTableTitles = { "Category Description", "Category", "Option Code",
				"Option Description" }; // TODO Header�κ� Registry ��� �ʿ�

		for (int i = 0; i < resultTableTitles.length; i++) {
			TableColumn column = new TableColumn(resultTable, SWT.NONE);
			column.setText(resultTableTitles[i]);
		}

		for (int i = 0; i < resultTableTitles.length; i++) {
			resultTable.getColumn(i).pack();
		}
		FormData resultTableData = new FormData(400, 300);
		resultTable.setLayoutData(resultTableData);
	}

	/**
	 * Mouse Action ����
	 * 
	 * @Copyright : S-PALM
	 * @author : �ְ��
	 * @since : 2013. 1. 10.
	 * @override
	 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
	 * @param e
	 */
	@Override
	public void widgetSelected(SelectionEvent e) {
		// Selecet Event���� �߻��Ǵ� Action�� ������ �´�.
		Object source = e.getSource();

		if (source == addButton) {
			/**
			 * Add Button Action : Tree���� ������ ���� ���� ��, Add Button �����, ���� Table�� �߰�
			 */
			System.out.println("Add");
			TreeItem[] selectItem = OVTree.getSelection();
			for (int i = 0; i < selectItem.length; i++) {
				TreeItem item = selectItem[i];
				// ���õ� ���� �迭�� �Ǿ��־ �迭���� Default�� 0�� �����Ѵ�.
				String selectedItem = item.getText(0);
				// Category�� ����������, ���� Child�� Table�� ����
				if (item.getParentItem() == null) {
					TreeItem[] childrenItems = item.getItems();
					int childrenItemSize = childrenItems.length;
					for (int j = 0; j < childrenItemSize; j++) {
						TreeItem childrenItem = childrenItems[j];

						OVEOption data = (OVEOption) item.getData();
						String optionName = data.option.name;
						String valueName = childrenItem.getText(0);
						String description = data.option.desc;

						TableItem addTableItem = new TableItem(ovTable, SWT.NONE);
						addTableItem.setText(new String[] { description, optionName, valueName, "pending" });
					}
					// Option�� ����������, ���� Parent�� Table�� ����
				} else {
					TreeItem parentItem = item.getParentItem();
					System.out.println("Select Item : " + selectedItem);
					System.out.println("Parent Item : " + parentItem.getText());
					OVEOption data = (OVEOption) parentItem.getData();
					// Option Name, Value Name, Description Name�� ������ �´�.
					String optionName = data.option.name;
					String valueName = selectedItem;
					String description = data.option.desc;
					// �迭�� �����ͼ�, ���̺� ���� �־��ش�
					TableItem addTableItem = new TableItem(ovTable, SWT.NONE);
					addTableItem.setText(new String[] { description, optionName, valueName, "pending" });
				}
			}
			/**
			 * Remove Button Action : Option Table���� ���� ���� ���� ��, Remove Button ����� ���� ����
			 */
		} else if (source == removeButton) {
			System.out.println("Remove");
			int[] selectedItem = ovTable.getSelectionIndices();
			ovTable.remove(selectedItem);
			ovTable.redraw();

			/**
			 * Clear Button Action : Option Table ��ü ����
			 */
		} else if (source == clearButton) {
			System.out.println("Clear");
			ovTable.removeAll();
			ovTable.redraw();

			/**
			 * Add Button2 Action : ���õ� Option Table�� ���� Result Table���� �̵�
			 */
		} else if (source == addButton2) {
			System.out.println("Add!!");
			TableItem[] selectItem = ovTable.getSelection();

			for (int i = 0; i < selectItem.length; i++) {
				TableItem item = selectItem[i];
				TableItem addTableItem = new TableItem(resultTable, SWT.NONE);
				addTableItem.setText(new String[] { item.getText(0), item.getText(1), item.getText(2),
						item.getText(3) });
			}

			/**
			 * Result Table���� ���� ���� ���� ��, Remove Button ����� ���� ����
			 */
		} else if (source == removeButton2) {
			System.out.println("Remove!!");
			int[] selectedItem = resultTable.getSelectionIndices();
			resultTable.remove(selectedItem);
			resultTable.redraw();

			/**
			 * Clear Button Action : Result Table ��ü ����
			 */
		} else if (source == clearButton2) {
			System.out.println("Clear!!");
			resultTable.removeAll();
			resultTable.redraw();
		}

	}

	@Override
	protected Composite createDialogPanel(ScrolledComposite parentScrolledComposite) {
		setDialogTextAndImage("Option", registry.getImage("MVConditionDialogHeader.ICON"));

		GridLayout gridLayout = new GridLayout(5, false);

		composit = new Composite(parentScrolledComposite, SWT.NONE);
		composit.setLayout(gridLayout);

		// Composite Grid Data
		// GridData(Style(Size), Style(Size), Width, Height)
		GridData treeGridData = new GridData(10, 10, false, false);
		GridData moveBtnGridData1 = new GridData(10, 10, false, false);
		GridData ovTableGridData = new GridData(10, 10, false, false);
		GridData moveBtnGridData2 = new GridData(10, 10, false, false);
		GridData resultTableGridData = new GridData(10, 10, false, false);

		// Composite Setting
		Composite treeComposite = new Composite(composit, SWT.NONE);
		Composite moveBtnComposite1 = new Composite(composit, SWT.NONE);
		Composite ovTableComposite = new Composite(composit, SWT.NONE);
		Composite moveBtnComposite2 = new Composite(composit, SWT.NONE);
		Composite resultTableComposite = new Composite(composit, SWT.NONE);

		// Composite LayOut Setting
		treeComposite.setLayout(new FillLayout());
		treeComposite.setLayoutData(treeGridData);
		moveBtnComposite1.setLayout(new FillLayout());
		moveBtnComposite1.setLayoutData(moveBtnGridData1);
		ovTableComposite.setLayout(new FillLayout());
		ovTableComposite.setLayoutData(ovTableGridData);
		moveBtnComposite2.setLayout(new FillLayout());
		moveBtnComposite2.setLayoutData(moveBtnGridData2);
		resultTableComposite.setLayout(new FillLayout());
		resultTableComposite.setLayoutData(resultTableGridData);

		// Tree UI ����
		Group treeGroup = new Group(treeComposite, SWT.NONE);
		FormLayout formLayout = new FormLayout();
		treeGroup.setLayout(formLayout);
		OVTree = new Tree(treeGroup, SWT.BORDER | SWT.MULTI);
		FormData data = new FormData(150, 300);
		OVTree.setLayoutData(data);

		// Move Button1 UI ����
		Group buttonGroup = new Group(moveBtnComposite1, SWT.None);
		RowLayout rowLayout = new RowLayout(SWT.CENTER);
		rowLayout.type = SWT.VERTICAL;
		rowLayout.center = true;
		rowLayout.fill = true;
		rowLayout.marginWidth = 5;
		rowLayout.marginHeight = 5;
		rowLayout.marginLeft = 5;
		rowLayout.marginRight = 5;
		rowLayout.marginTop = 100;
		rowLayout.marginBottom = 5;
		rowLayout.spacing = 5;
		buttonGroup.setLayout(rowLayout);
		addButton = new Button(buttonGroup, SWT.PUSH);
		addButton.addSelectionListener(this);
		addButton.setImage(registry.getImage("ProuctOptionManageForwardArrow.ICON"));
		removeButton = new Button(buttonGroup, SWT.PUSH);
		removeButton.addSelectionListener(this);
		removeButton.setImage(registry.getImage("ProuctOptionManageBackArrow.ICON"));
		clearButton = new Button(buttonGroup, SWT.PUSH);
		clearButton.addSelectionListener(this);
		clearButton.setImage(registry.getImage("ProuctOptionManageClear.ICON"));

		// OV Table UI ����
		optionTableGroup = new Group(ovTableComposite, SWT.NONE);
		optionTableGroup.setLayout(formLayout);

		// Move Button2 UI ����
		Group buttonGroup2 = new Group(moveBtnComposite2, SWT.None);
		RowLayout rowLayout2 = new RowLayout(SWT.CENTER);
		rowLayout2.type = SWT.VERTICAL;
		rowLayout2.center = true;
		rowLayout2.fill = true;
		rowLayout2.marginWidth = 5;
		rowLayout2.marginHeight = 5;
		rowLayout2.marginLeft = 5;
		rowLayout2.marginRight = 5;
		rowLayout2.marginTop = 100;
		rowLayout2.marginBottom = 5;
		rowLayout2.spacing = 5;
		buttonGroup2.setLayout(rowLayout2);
		addButton2 = new Button(buttonGroup2, SWT.PUSH);
		addButton2.addSelectionListener(this);
		addButton2.setImage(registry.getImage("ProuctOptionManageForwardArrow2.ICON"));
		removeButton2 = new Button(buttonGroup2, SWT.PUSH);
		removeButton2.addSelectionListener(this);
		removeButton2.setImage(registry.getImage("ProuctOptionManageBackArrow2.ICON"));
		clearButton2 = new Button(buttonGroup2, SWT.PUSH);
		clearButton2.addSelectionListener(this);
		clearButton2.setImage(registry.getImage("ProuctOptionManageClear2.ICON"));

		// Result Table UI ����
		resultTableGroup = new Group(resultTableComposite, SWT.NONE);
		resultTableGroup.setLayout(formLayout);

		initTree(bomLine, OVTree);
		makeOptionTable();
		makeResultTable();

		return composit;
	}

	@Override
	protected boolean apply() {
		return true;
	}

	@Override
	protected boolean validationCheck() {

		return false;
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent e) {

	}
}

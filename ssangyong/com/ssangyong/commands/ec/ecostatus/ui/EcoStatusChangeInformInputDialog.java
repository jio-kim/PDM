package com.ssangyong.commands.ec.ecostatus.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.collections.CollectionUtils;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.AbstractRegistryConfiguration;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.config.EditableRule;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.IEditableRule;
import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.convert.DefaultBooleanDisplayConverter;
import org.eclipse.nebula.widgets.nattable.data.validate.DataValidator;
import org.eclipse.nebula.widgets.nattable.data.validate.IDataValidator;
import org.eclipse.nebula.widgets.nattable.data.validate.ValidationFailedException;
import org.eclipse.nebula.widgets.nattable.edit.EditConfigAttributes;
import org.eclipse.nebula.widgets.nattable.edit.action.ToggleCheckBoxColumnAction;
import org.eclipse.nebula.widgets.nattable.edit.config.DialogErrorHandling;
import org.eclipse.nebula.widgets.nattable.edit.editor.CheckBoxCellEditor;
import org.eclipse.nebula.widgets.nattable.edit.editor.ComboBoxCellEditor;
import org.eclipse.nebula.widgets.nattable.edit.editor.ICellEditor;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.groupBy.ModernGroupByThemeExtension;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ColumnLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.layer.cell.ColumnOverrideLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.painter.cell.CheckBoxPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.ColumnHeaderCheckBoxPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.ComboBoxPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.ICellPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.TextPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.decorator.CellPainterDecorator;
import org.eclipse.nebula.widgets.nattable.painter.cell.decorator.CustomLineBorderDecorator;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.sort.config.SingleClickSortConfiguration;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.HorizontalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.eclipse.nebula.widgets.nattable.style.theme.ThemeConfiguration;
import org.eclipse.nebula.widgets.nattable.ui.binding.UiBindingRegistry;
import org.eclipse.nebula.widgets.nattable.ui.matcher.CellPainterMouseEventMatcher;
import org.eclipse.nebula.widgets.nattable.ui.matcher.MouseEventMatcher;
import org.eclipse.nebula.widgets.nattable.ui.util.CellEdgeEnum;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.wb.swt.SWTResourceManager;

import ca.odell.glazedlists.EventList;

import com.ssangyong.commands.ec.dao.CustomECODao;
import com.ssangyong.commands.ec.ecostatus.model.EcoOspecCateData;
import com.ssangyong.commands.ec.ecostatus.model.EcoStatusData;
import com.ssangyong.commands.ec.ecostatus.operation.EcoStatusChangeInformSaveOperation;
import com.ssangyong.commands.ec.ecostatus.utility.BasicGridEditorGridLayer;
import com.ssangyong.commands.ec.ecostatus.utility.TableStylingThemeConfiguration;
import com.ssangyong.common.remote.DataSet;
import com.ssangyong.common.remote.SYMCRemoteUtil;
import com.ssangyong.common.utils.CustomUtil;
import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.ConfirmDialog;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;

/**
 * 변경정보 입력 Dialog
 * 
 * @author baek
 * 
 */
public class EcoStatusChangeInformInputDialog extends Dialog {

	private Button btnSearch;
	private NatTable leftNattable;
	private NatTable rightNattable;
	private EventList<EcoOspecCateData> leftTableDataList; // 왼쪽 Table Data 리스트
	private EventList<EcoOspecCateData> rightTableDataList; // 오른쪽 Table Data 리스트
	private ArrayList<String> targetCategoryList = null;// 대상 Category 리스트
	private CCombo comboCategory = null; // Option Category
	private ArrayList<String> addOrExLabelList = null; // 추가/제외 라벨 정보
	private Registry registry = null;
	private TCSession tcSession = null;

	private EcoStatusData stdInformData = null; // 기준정보 리스트

	private EcoStatusManagerDialog mainDialog = null;

	private ArrayList<String> validVariantList = null; // 전사옵션 리스트

	private EcoStatusOptionTable afterTotalOSpecTable = null; // 변경후 전체 OSPEC Table
	private EcoStatusOptionTable beforeTotalOSpecTable = null; // 변경전 전체 OSPEC Table
	@SuppressWarnings("rawtypes")
	private Vector<Vector> onlyAfterData = null; // 변경후 변경된 Data
	@SuppressWarnings("rawtypes")
	private Vector<Vector> onlyBeforeData = null; // 변경전 변경된 Data

	// Category 별 추가/삭제 리스트 정보
	private HashMap<String, ArrayList<EcoOspecCateData>> addOrRemoveMap = new HashMap<String, ArrayList<EcoOspecCateData>>();

	private SelectionLayer leftSelectionLayer = null; // 오른쪽 테이블 Table Body의 선택되는 Layer

	/**
	 * 
	 * @param targetCategoryList
	 *            대상 카테고리 리스트
	 * @param stdInformData
	 *            기준 정보
	 * @param parentShell
	 */
	public EcoStatusChangeInformInputDialog(ArrayList<String> targetCategoryList, EcoStatusData stdInformData, Shell parentShell,
			EcoStatusManagerDialog mainDialog) {
		super(parentShell);
		setShellStyle(SWT.DIALOG_TRIM | SWT.MIN | SWT.RESIZE);
		this.targetCategoryList = targetCategoryList;
		this.stdInformData = stdInformData;
		this.registry = Registry.getRegistry(this);
		this.tcSession = CustomUtil.getTCSession();
		this.mainDialog = mainDialog;
		setAddOrExList();
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("변경정보 입력");
	}

	/**
	 * Create contents of the dialog.
	 * 
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		parent.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		Composite container = (Composite) super.createDialogArea(parent);

		container.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
		container.setBackgroundMode(SWT.INHERIT_FORCE);

		GridLayout gl_container = new GridLayout(1, false);
		gl_container.verticalSpacing = 0;
		gl_container.verticalSpacing = 0;
		gl_container.marginWidth = 0;
		gl_container.marginHeight = 0;
		gl_container.horizontalSpacing = 0;
		container.setLayout(gl_container);

		FormToolkit toolkit = new FormToolkit(parent.getDisplay());

		Section sectionbBasic = toolkit.createSection(container, Section.TITLE_BAR);
		GridData gd_sectionbBasic = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
		gd_sectionbBasic.minimumWidth = 0;
		sectionbBasic.setLayoutData(gd_sectionbBasic);
		Composite compositeBasic = toolkit.createComposite(sectionbBasic, SWT.NONE);
		compositeBasic.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		sectionbBasic.setClient(compositeBasic);
		compositeBasic.setLayout(new GridLayout(3, false));

		Label lblOptionCategory = new Label(compositeBasic, SWT.NONE);
		toolkit.adapt(lblOptionCategory, true, true);
		lblOptionCategory.setText("Option Categroy");

		comboCategory = new CCombo(compositeBasic, SWT.BORDER);
		comboCategory.setEditable(false);
		GridData gd_comboCategory = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_comboCategory.widthHint = 100;
		comboCategory.setLayoutData(gd_comboCategory);
		toolkit.adapt(comboCategory);
		toolkit.paintBordersFor(comboCategory);

		Composite compositeTopRightButton = new Composite(compositeBasic, SWT.NONE);
		compositeTopRightButton.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, true, false, 1, 1));
		compositeTopRightButton.setLayout(new GridLayout(2, false));

		btnSearch = new Button(compositeTopRightButton, SWT.NONE);
		btnSearch.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		btnSearch.setText("EPL Search");
		btnSearch.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				doEplSearch();
			}
		});
		btnSearch.setImage(SWTResourceManager.getImage(EcoChangeListMgrComposite.class, "/icons/search_16.png"));
		GridData gd_btnSearch = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
		gd_btnSearch.widthHint = 100;
		btnSearch.setLayoutData(gd_btnSearch);

		Button btnOspecCompare = new Button(compositeTopRightButton, SWT.NONE);
		GridData gd_btnOspecCompare = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_btnOspecCompare.widthHint = 130;
		btnOspecCompare.setLayoutData(gd_btnOspecCompare);
		toolkit.adapt(btnOspecCompare, true, true);
		btnOspecCompare.setText("O/Spec Compare");
		btnOspecCompare.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				doCompareOspecWithTarget();
			}
		});

		Composite compositeCenter = new Composite(container, SWT.NONE);
		compositeCenter.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true, 1, 1));
		GridLayout gl_compositeCenter = new GridLayout(2, false);
		gl_compositeCenter.horizontalSpacing = 1;
		gl_compositeCenter.verticalSpacing = 0;
		gl_compositeCenter.marginWidth = 0;
		gl_compositeCenter.marginHeight = 0;
		compositeCenter.setLayout(gl_compositeCenter);

		Section sectionLeft = toolkit.createSection(compositeCenter, Section.TITLE_BAR);
		sectionLeft.setText("변경검토 내용 입력");
		GridData gd_sectionLeft = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_sectionLeft.minimumWidth = 0;
		sectionLeft.setLayoutData(gd_sectionLeft);

		Composite compositeLeft = toolkit.createComposite(sectionLeft, SWT.BORDER);
		GridLayout gl_compositeLeft = new GridLayout(1, false);
		gl_compositeLeft.marginWidth = 0;
		gl_compositeLeft.horizontalSpacing = 0;
		gl_compositeLeft.verticalSpacing = 0;
		gl_compositeLeft.marginHeight = 0;
		GridData gd_compositeLeft = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		compositeLeft.setLayout(gl_compositeLeft);
		compositeLeft.setLayoutData(gd_compositeLeft);
		sectionLeft.setClient(compositeLeft);

		Section sectionRight = toolkit.createSection(compositeCenter, Section.TITLE_BAR);
		sectionRight.setText("추가 검토옵션 설정");
		GridData gd_sectionRight = new GridData(SWT.FILL, SWT.FILL, false, true, 1, 1);
		gd_sectionRight.widthHint = 280;
		gd_sectionRight.minimumWidth = 0;
		sectionRight.setLayoutData(gd_sectionRight);

		Composite compositeRight = toolkit.createComposite(sectionRight, SWT.BORDER);
		GridLayout gl_compositeRight = new GridLayout(1, false);
		gl_compositeRight.marginWidth = 0;
		gl_compositeRight.horizontalSpacing = 0;
		gl_compositeRight.verticalSpacing = 0;
		gl_compositeRight.marginHeight = 0;
		GridData gd_compositeRight = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 2);
		compositeRight.setLayout(gl_compositeRight);
		compositeRight.setLayoutData(gd_compositeRight);
		sectionRight.setClient(compositeRight);

		String[] propertyNames = EcoOspecCateData.getPropertyNames();
		Map<String, String> propertyToLabelMap = EcoOspecCateData.getPropertyToLabelMap();
		ConfigRegistry configRegistry = new ConfigRegistry();
		BasicGridEditorGridLayer<EcoOspecCateData> gridLayer = new BasicGridEditorGridLayer<EcoOspecCateData>(new ArrayList<EcoOspecCateData>(),
				configRegistry, propertyNames, propertyToLabelMap);
		DataLayer bodyDataLayer = gridLayer.getBodyLayer().getDataLayer();
		leftTableDataList = gridLayer.getTableDataList();

		ColumnOverrideLabelAccumulator columnLabelAccumulator = new ColumnOverrideLabelAccumulator(bodyDataLayer);
		bodyDataLayer.setConfigLabelAccumulator(columnLabelAccumulator);
		ColumnHeaderCheckBoxPainter columnLeftHeaderCheckBoxPainter = new ColumnHeaderCheckBoxPainter(bodyDataLayer);

		leftNattable = new NatTable(compositeLeft, gridLayer, false);
		leftNattable.setLayout(new FillLayout(SWT.HORIZONTAL));
		leftNattable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		leftNattable.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		leftNattable.setConfigRegistry(configRegistry);
		leftNattable.addConfiguration(new DefaultNatTableStyleConfiguration()); // 기본 Style 설정
		leftNattable.addConfiguration(new SingleClickSortConfiguration()); // Single Click 시 Sort 설정
		leftNattable.addConfiguration(setModelEditorConfiguration(columnLabelAccumulator, bodyDataLayer, new CheckBoxPainter(),
				columnLeftHeaderCheckBoxPainter, true));
		leftNattable.configure();

		leftSelectionLayer = gridLayer.getBodyLayer().getSelectionLayer();
		bodyDataLayer.setDefaultColumnWidthByPosition(EcoOspecCateData.getColumnIndexOfProperty(EcoOspecCateData.PROP_NAME_ROW_CHECK), 35);
		bodyDataLayer.setDefaultColumnWidthByPosition(EcoOspecCateData.getColumnIndexOfProperty(EcoOspecCateData.PROP_NAME_CATEGORY), 90);
		bodyDataLayer.setDefaultColumnWidthByPosition(EcoOspecCateData.getColumnIndexOfProperty(EcoOspecCateData.PROP_NAME_REVIEW_CONTENTS), 385);
		bodyDataLayer.setDefaultColumnWidthByPosition(EcoOspecCateData.getColumnIndexOfProperty(EcoOspecCateData.PROP_NAME_CATEGORY_EDITABLE), 0);

		// add modern styling
		ThemeConfiguration theme = new TableStylingThemeConfiguration();
		theme.addThemeExtension(new ModernGroupByThemeExtension());
		leftNattable.setTheme(theme);

		addLeftTableCellSectionEvent();

		Composite compositeLeftBottom = new Composite(compositeLeft, SWT.BORDER);
		compositeLeftBottom.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		GridLayout gl_compositeLeftBottom = new GridLayout(2, false);
		gl_compositeLeftBottom.marginHeight = 0;
		compositeLeftBottom.setLayout(gl_compositeLeftBottom);

		Button btnLeftAdd = new Button(compositeLeftBottom, SWT.NONE);
		btnLeftAdd.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				addLeftTableRow();
			}
		});
		GridData gd_btnLeftAdd = new GridData(SWT.RIGHT, SWT.BOTTOM, false, false, 1, 1);
		gd_btnLeftAdd.widthHint = 30;
		btnLeftAdd.setLayoutData(gd_btnLeftAdd);
		btnLeftAdd.setImage(SWTResourceManager.getImage(EcoChangeListMgrComposite.class, "/icons/add_16.png"));

		Button btnLeftRemove = new Button(compositeLeftBottom, SWT.NONE);
		btnLeftRemove.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				removeLeftTableRow();
			}
		});
		GridData gd_btnLeftRemove = new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1);
		gd_btnLeftRemove.widthHint = 30;
		btnLeftRemove.setLayoutData(gd_btnLeftRemove);
		btnLeftRemove.setImage(SWTResourceManager.getImage(EcoChangeListMgrComposite.class, "/icons/remove_16.png"));

		String[] rightPropertyNames = EcoOspecCateData.getOptPropertyNames();
		Map<String, String> rightPropertyToLabelMap = EcoOspecCateData.getOptPropertyToLabelMap();
		ConfigRegistry rightConfigRegistry = new ConfigRegistry();
		BasicGridEditorGridLayer<EcoOspecCateData> rightGridLayer = new BasicGridEditorGridLayer<EcoOspecCateData>(new ArrayList<EcoOspecCateData>(),
				rightConfigRegistry, rightPropertyNames, rightPropertyToLabelMap);
		DataLayer rightBodyDataLayer = rightGridLayer.getBodyLayer().getDataLayer();
		rightTableDataList = rightGridLayer.getTableDataList();

		ColumnOverrideLabelAccumulator rightColumnLabelAccumulator = new ColumnOverrideLabelAccumulator(rightBodyDataLayer);
		rightBodyDataLayer.setConfigLabelAccumulator(rightColumnLabelAccumulator);

		ColumnHeaderCheckBoxPainter columnHeaderCheckBoxPainter = new ColumnHeaderCheckBoxPainter(rightBodyDataLayer);

		rightNattable = new NatTable(compositeRight, rightGridLayer, false);
		rightNattable.setLayout(new FillLayout(SWT.HORIZONTAL));
		GridData gd_rightNattable = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_rightNattable.widthHint = 265;
		rightNattable.setLayoutData(gd_rightNattable);
		rightNattable.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));

		rightNattable.setConfigRegistry(rightConfigRegistry);
		rightNattable.addConfiguration(new DefaultNatTableStyleConfiguration()); // 기본 Style 설정
		rightNattable.addConfiguration(new SingleClickSortConfiguration()); // Single Click 시 Sort 설정
		rightNattable.addConfiguration(setModelEditorConfiguration(rightColumnLabelAccumulator, rightBodyDataLayer, new CheckBoxPainter(),
				columnHeaderCheckBoxPainter, false));
		rightNattable.configure();

		rightBodyDataLayer.setDefaultColumnWidthByPosition(EcoOspecCateData.getOptColumnIndexOfProperty(EcoOspecCateData.PROP_NAME_ROW_CHECK), 35);
		rightBodyDataLayer.setDefaultColumnWidthByPosition(EcoOspecCateData.getOptColumnIndexOfProperty(EcoOspecCateData.PROP_NAME_ADD_REVIEW_OPTION), 95);
		rightBodyDataLayer.setDefaultColumnWidthByPosition(EcoOspecCateData.getOptColumnIndexOfProperty(EcoOspecCateData.PROP_NAME_ADD_OR_EX_CONDITION), 95);
		// add modern styling
		ThemeConfiguration rightTheme = new TableStylingThemeConfiguration();
		rightTheme.addThemeExtension(new ModernGroupByThemeExtension());
		rightNattable.setTheme(rightTheme);

		Composite compositeRigthBottom = new Composite(compositeRight, SWT.BORDER);
		compositeRigthBottom.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		GridLayout gl_compositeRigthBottom = new GridLayout(2, false);
		gl_compositeRigthBottom.marginHeight = 0;
		compositeRigthBottom.setLayout(gl_compositeRigthBottom);

		Button btnAdd = new Button(compositeRigthBottom, SWT.NONE);
		btnAdd.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				addRow();
			}
		});
		GridData gd_btnAdd = new GridData(SWT.RIGHT, SWT.BOTTOM, true, false, 1, 1);
		gd_btnAdd.widthHint = 30;
		btnAdd.setLayoutData(gd_btnAdd);
		btnAdd.setImage(SWTResourceManager.getImage(EcoChangeListMgrComposite.class, "/icons/add_16.png"));

		Button btnRemove = new Button(compositeRigthBottom, SWT.NONE);
		btnRemove.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				removeRow();
			}
		});
		GridData gd_btnRemove = new GridData(SWT.LEFT, SWT.TOP, true, false, 1, 1);
		gd_btnRemove.widthHint = 30;
		btnRemove.setLayoutData(gd_btnRemove);
		btnRemove.setImage(SWTResourceManager.getImage(EcoChangeListMgrComposite.class, "/icons/remove_16.png"));
		initDataLoad();

		return container;
	}

	@Override
	protected Control createButtonBar(Composite parent) {
		final Composite buttonBar = new Composite(parent, SWT.NONE);

		final GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.makeColumnsEqualWidth = false;
		layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		buttonBar.setLayout(layout);

		final GridData data = new GridData(SWT.FILL, SWT.BOTTOM, true, false);
		data.grabExcessHorizontalSpace = true;
		data.grabExcessVerticalSpace = false;
		buttonBar.setLayoutData(data);

		buttonBar.setFont(parent.getFont());

		// place a button on the left
		final Button latestOpspecView = new Button(buttonBar, SWT.PUSH);
		latestOpspecView.setText("최신 O/Spec 조회");

		latestOpspecView.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				doViewLatestOpsec();
			}
		});

		final GridData leftButtonData = new GridData(SWT.LEFT, SWT.CENTER, true, true);
		leftButtonData.grabExcessHorizontalSpace = true;
		leftButtonData.horizontalIndent = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
		latestOpspecView.setLayoutData(leftButtonData);

		// add the dialog's button bar to the right
		final Control buttonControl = super.createButtonBar(buttonBar);
		buttonControl.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));
		buttonBar.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));

		return buttonBar;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {

		parent.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));

		Button btnSave = createButton(parent, 100, "Save", false);
		btnSave.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				doSave();
			}
		});

		btnSave.setImage(SWTResourceManager.getImage(EcoChangeListMgrComposite.class, "/icons/save_16.png"));
		createButton(parent, IDialogConstants.CANCEL_ID, "Close", false);
	}

	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(900, 600);
	}

	/**
	 * 초기 Data Load
	 */
	private void initDataLoad() {

		if (targetCategoryList == null)
			return;

		InitDataLoadOperation operation = new InitDataLoadOperation();
		tcSession.queueOperation(operation);

	}

	/**
	 * 최신 O/Spec 조회
	 */
	private void doViewLatestOpsec() {
		LastestOspecViewDialog dialog = new LastestOspecViewDialog(stdInformData, this.getShell());
		dialog.open();
	}

	/**
	 * 선택된 Category 에 해당하는 OSPEC Compare 기능
	 */
	private void doCompareOspecWithTarget() {

		String selectedCategory = comboCategory.getText().equals("ALL") ? null : comboCategory.getText();
		CategoryOspecCompareDialog dialog = new CategoryOspecCompareDialog(selectedCategory, afterTotalOSpecTable, beforeTotalOSpecTable, onlyAfterData,
				onlyBeforeData, this.getShell());
		dialog.open();

	}

	/**
	 * 오른쪽 테이블 행 추가
	 */
	private void addRow() {

		String category = getSelectedCategory();
		if (category == null) {
			MessageBox.post(this.getShell(), "왼쪽 변경 Category가 선택되어 있지 않습니다.", "Warning", MessageBox.WARNING);
			return;
		}

		ArrayList<EcoOspecCateData> addOrRemoveDataList = null;
		EcoOspecCateData newData = new EcoOspecCateData();

		if (addOrRemoveMap.get(category) == null) {
			addOrRemoveDataList = new ArrayList<EcoOspecCateData>();
			addOrRemoveDataList.add(newData);
			addOrRemoveMap.put(category, addOrRemoveDataList);
		} else {
			addOrRemoveDataList = addOrRemoveMap.get(category);
			addOrRemoveDataList.add(newData);
		}

		rightTableDataList.add(newData);
	}

	/**
	 * 오른쪽 테이블 행 삭제
	 */
	private void removeRow() {
		String category = getSelectedCategory();
		if (category == null) {
			MessageBox.post(this.getShell(), "왼쪽 변경 Category가 선택되어 있지 않습니다.", "Warning", MessageBox.WARNING);
			return;
		}

		ArrayList<EcoOspecCateData> addOrRemoveList = addOrRemoveMap.get(category);

		ArrayList<EcoOspecCateData> removeDataList = new ArrayList<EcoOspecCateData>();
		for (EcoOspecCateData rowData : rightTableDataList) {
			boolean isChecked = rowData.isRowCheck();
			if (!isChecked)
				continue;
			removeDataList.add(rowData);
		}

		for (EcoOspecCateData removeData : removeDataList)
			rightTableDataList.remove(removeData);

		ArrayList<EcoOspecCateData> toRemove = new ArrayList<EcoOspecCateData>();
		for (EcoOspecCateData savedData : addOrRemoveList) {
			for (EcoOspecCateData removeData : removeDataList) {
				if (removeData.equals(savedData))
					toRemove.add(savedData);
			}
		}
		addOrRemoveList.removeAll(toRemove);

		if (addOrRemoveList.size() == 0)
			addOrRemoveMap.remove(category);

		rightNattable.refresh();
	}

	/**
	 * 왼쪽 테이블 행 추가
	 */
	private void addLeftTableRow() {
		EcoOspecCateData rowData = new EcoOspecCateData();
		rowData.setCategoryEditable(true);
		leftTableDataList.add(rowData);

	}

	/**
	 * 왼쪽 테이블 Row 를 삭제
	 */
	private void removeLeftTableRow() {
		PositionCoordinate[] selectCellPos = leftSelectionLayer.getSelectedCellPositions();
		if (selectCellPos.length == 0)
			return;

		ArrayList<EcoOspecCateData> removeDataList = new ArrayList<EcoOspecCateData>();
		for (EcoOspecCateData rowData : leftTableDataList) {
			boolean isChecked = rowData.isRowCheck();
			if (!isChecked)
				continue;
			removeDataList.add(rowData);
		}

		// 삭제 되는 Category 정보를 저장함. 해당 추가 검토 옵션 정보도 삭제하기 위함
		ArrayList<String> removedCategoryList = new ArrayList<String>();
		for (EcoOspecCateData removeData : removeDataList) {
			String category = removeData.getCategory();
			leftTableDataList.remove(removeData);
			if (category == null)
				continue;
			removedCategoryList.add(category);

		}
		// 해당 Category 에 해당하는 추가 검토 옵션을 삭제한다.
		for (String category : removedCategoryList) {
			addOrRemoveMap.remove(category);
		}

		leftNattable.refresh();
	}

	/**
	 * EPL 조회
	 */
	private void doEplSearch() {
		StringBuffer addOptCondSb = new StringBuffer();
		// 조회조건 Option 정보. KEY: Category 2자리, Value: 2자리를 제외한 이하 리스트. EX) KEY:A0 VALUE: 0,1,2,3
		LinkedHashMap<String, ArrayList<String>> searchOptionListMap = new LinkedHashMap<String, ArrayList<String>>();
		for (int i = 0; i < leftTableDataList.size(); i++) {
			EcoOspecCateData data = leftTableDataList.get(i);
			String category = data.getCategory();
			if (category == null)
				continue;
			String seachAddOptCondtion = getSearchOptionClause(category);
			// REGEXP_LIKE 정규식으로 만들기 위한 분류 EX) A0[0,1,2,3]|A1[0,1] ....
			String catePrefix = seachAddOptCondtion.substring(0, 2); // 분류하기위한 Prefix EX) A0
			String cateSubfix = seachAddOptCondtion.substring(2, seachAddOptCondtion.length());
			// 하위 Prefix 리스트 EX) 0,1,2,3
			ArrayList<String> cateSubfixList = new ArrayList<String>();
			if (!searchOptionListMap.containsKey(catePrefix)) {
				cateSubfixList.add(cateSubfix);
				searchOptionListMap.put(catePrefix, cateSubfixList);
			} else {
				cateSubfixList = searchOptionListMap.get(catePrefix);
				cateSubfixList.add(cateSubfix);
			}
		}
		// 검색 옵션 조건을 정규식 기준으로 만듬 EX) A0[0,1,2,3]|A1[0,1] ....
		for (String catePrefix : searchOptionListMap.keySet()) {
			ArrayList<String> cateSubfixList = searchOptionListMap.get(catePrefix);
			if (cateSubfixList.size() == 1)
				addOptCondSb.append(catePrefix + cateSubfixList.get(0)).append("|");
			else {
				addOptCondSb.append(catePrefix + "[");
				for (int i = 0; i < cateSubfixList.size(); i++) {
					String cateSubfix = cateSubfixList.get(i);
					if (i != cateSubfixList.size() - 1) {
						addOptCondSb.append(cateSubfix + ",");
					} else {
						addOptCondSb.append(cateSubfix + "]");
					}
				}
				addOptCondSb.append("|");
			}
		}

		String addOptionCondtions = addOptCondSb.toString().endsWith("|") ? addOptCondSb.toString().substring(0, addOptCondSb.lastIndexOf("|")) : addOptCondSb
				.toString();
		EcoOspecCateData categoryData = new EcoOspecCateData();
		categoryData.setAddConditions(addOptionCondtions);
		categoryData.setAddOrRemoveMap(addOrRemoveMap);
		categoryData.setChangeCategoryList(leftTableDataList);

		EcoStatusEPLSearchDialog dialog = new EcoStatusEPLSearchDialog(stdInformData, categoryData, tcSession, this.getShell());
		dialog.open();

	}

	/**
	 * 저장
	 */
	private void doSave() {
		StringBuffer addOptCondSb = new StringBuffer(); // 추가 옵션 검색 조건
		// Category 별 추가 검토 조건 정보
		HashMap<ArrayList<String>, ArrayList<EcoOspecCateData>> categoryConditionMap = new HashMap<ArrayList<String>, ArrayList<EcoOspecCateData>>();
		// 조회조건 Option 정보. KEY: Category 2자리, Value: 2자리를 제외한 이하 리스트. EX) KEY:A0 VALUE: 0,1,2,3
		LinkedHashMap<String, ArrayList<String>> searchOptionListMap = new LinkedHashMap<String, ArrayList<String>>();

		/**
		 * Category 에서 옵션 검색 조건을 만듬
		 */
		for (int i = 0; i < leftTableDataList.size(); i++) {
			EcoOspecCateData data = leftTableDataList.get(i);
			String category = data.getCategory();
			String reviewContents = data.getReviewContents();

			if (category == null)
				continue;

			// Category 별 추가 검토 조건 리스트 저장
			ArrayList<EcoOspecCateData> addOrRemoveList = addOrRemoveMap.get(category);
			ArrayList<String> opCategoryKeyList = new ArrayList<String>(Arrays.asList(category, reviewContents));
			categoryConditionMap.put(opCategoryKeyList, addOrRemoveList);

			String seachAddOptCondtion = getSearchOptionClause(category);
			// REGEXP_LIKE 정규식으로 만들기 위한 분류 EX) A0[0,1,2,3]|A1[0,1] ....
			String catePrefix = seachAddOptCondtion.substring(0, 2); // 분류하기위한 Prefix EX) A0
			String cateSubfix = seachAddOptCondtion.substring(2, seachAddOptCondtion.length());
			// 하위 Prefix 리스트 EX) 0,1,2,3
			ArrayList<String> cateSubfixList = new ArrayList<String>();
			if (!searchOptionListMap.containsKey(catePrefix)) {
				cateSubfixList.add(cateSubfix);
				searchOptionListMap.put(catePrefix, cateSubfixList);
			} else {
				cateSubfixList = searchOptionListMap.get(catePrefix);
				cateSubfixList.add(cateSubfix);
			}
		}

		// 검색 옵션 조건을 정규식 기준으로 만듬 EX) A0[0,1,2,3]|A1[0,1] ....
		for (String catePrefix : searchOptionListMap.keySet()) {
			ArrayList<String> cateSubfixList = searchOptionListMap.get(catePrefix);
			if (cateSubfixList.size() == 1)
				addOptCondSb.append(catePrefix + cateSubfixList.get(0)).append("|");
			else {
				addOptCondSb.append(catePrefix + "[");
				for (int i = 0; i < cateSubfixList.size(); i++) {
					String cateSubfix = cateSubfixList.get(i);
					if (i != cateSubfixList.size() - 1) {
						addOptCondSb.append(cateSubfix + ",");
					} else {
						addOptCondSb.append(cateSubfix + "]");
					}
				}
				addOptCondSb.append("|");
			}
		}

		String addOptionCondtions = addOptCondSb.toString().endsWith("|") ? addOptCondSb.toString().substring(0, addOptCondSb.lastIndexOf("|")) : addOptCondSb
				.toString();

		EcoOspecCateData inputData = new EcoOspecCateData();
		inputData.setAddConditions(addOptionCondtions);
		inputData.setAddOrRemoveMap(addOrRemoveMap);
		inputData.setChangeCategoryList(leftTableDataList);
		inputData.setCategoryConditionMap(categoryConditionMap);

		this.okPressed();

		EcoStatusChangeInformSaveOperation operation = new EcoStatusChangeInformSaveOperation(inputData, stdInformData, mainDialog);
		tcSession.queueOperation(operation);

	}

	/**
	 * ECO 발행 선택 값리스트
	 */
	private void setAddOrExList() {
		String[] addOrExArray = registry.getStringArray("ADD_OR_EX.LIST");
		addOrExLabelList = new ArrayList<String>(Arrays.asList(addOrExArray));
	}

	/**
	 * Cell Label Editor
	 * 
	 * @param columnLabelAccumulator
	 * @param bodyDataLayer
	 * @return
	 */
	private AbstractRegistryConfiguration setModelEditorConfiguration(final ColumnOverrideLabelAccumulator columnLabelAccumulator,
			final DataLayer bodyDataLayer, final CheckBoxPainter checkBoxPainter, final ColumnHeaderCheckBoxPainter columnHeaderCheckBoxPainter,
			final boolean isLeftTable) {

		return new AbstractRegistryConfiguration() {
			@Override
			public void configureRegistry(IConfigRegistry configRegistry) {

				ICellPainter columnHeaderPainter = new CustomLineBorderDecorator(new CellPainterDecorator(new TextPainter(), CellEdgeEnum.BOTTOM,
						columnHeaderCheckBoxPainter));
				configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER, columnHeaderPainter, DisplayMode.NORMAL,
						ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + 0);
				if (!isLeftTable) {
					// 추가/제외 조건 Combo Box 설정
					addOrExComboBoxEditor(configRegistry, new ComboBoxPainter());
				}

				// Column 마다 적용된 Label 설정
				registerConfigLabelsOnColumns(columnLabelAccumulator, isLeftTable);
				// 가운데 정렬
				registerAignCenterCellStyle(configRegistry);

				// 수정여부를 설정하는 Rule 지정
				registerEditableRules(configRegistry, bodyDataLayer.getDataProvider());
				// 체크박스 Editor 설정
				registerCheckBoxEditor(configRegistry, new CheckBoxPainter(), new CheckBoxCellEditor());

				if (isLeftTable) {
					configRegistry.registerConfigAttribute(EditConfigAttributes.DATA_VALIDATOR, getCategoryValueValidator(), DisplayMode.EDIT,
							EcoOspecCateData.PROP_NAME_CATEGORY);
					configRegistry.registerConfigAttribute(EditConfigAttributes.VALIDATION_ERROR_HANDLER, new ValidationDialogErrorHandling(),
							DisplayMode.EDIT, EcoOspecCateData.PROP_NAME_CATEGORY);
				} else {
					configRegistry.registerConfigAttribute(EditConfigAttributes.DATA_VALIDATOR, getAddOptionValidator(), DisplayMode.EDIT,
							EcoOspecCateData.PROP_NAME_ADD_REVIEW_OPTION);
					configRegistry.registerConfigAttribute(EditConfigAttributes.VALIDATION_ERROR_HANDLER, new ValidationDialogErrorHandling(),
							DisplayMode.EDIT, EcoOspecCateData.PROP_NAME_ADD_REVIEW_OPTION);
				}
				registerErrorHandlingStyles(configRegistry, isLeftTable);

			}

			@Override
			public void configureUiBindings(UiBindingRegistry uiBindingRegistry) {
				// 전체선택 Click
				uiBindingRegistry.registerFirstSingleClickBinding(new CellPainterMouseEventMatcher(GridRegion.COLUMN_HEADER, MouseEventMatcher.LEFT_BUTTON,
						columnHeaderCheckBoxPainter), new ToggleCheckBoxColumnAction(columnHeaderCheckBoxPainter, bodyDataLayer));
			}
		};
	}

	/**
	 * 옵션값 에러시 Color 지정
	 * 
	 * @param configRegistry
	 */
	private void registerErrorHandlingStyles(IConfigRegistry configRegistry, boolean isLeftTable) {

		Style validationErrorStyle = new Style();
		validationErrorStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, GUIHelper.COLOR_WHITE);
		validationErrorStyle.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR, GUIHelper.COLOR_RED);
		if (isLeftTable)
			configRegistry.registerConfigAttribute(EditConfigAttributes.VALIDATION_ERROR_STYLE, validationErrorStyle, DisplayMode.EDIT,
					EcoOspecCateData.PROP_NAME_CATEGORY);
		else
			configRegistry.registerConfigAttribute(EditConfigAttributes.VALIDATION_ERROR_STYLE, validationErrorStyle, DisplayMode.EDIT,
					EcoOspecCateData.PROP_NAME_ADD_REVIEW_OPTION);
	}

	/**
	 * 옵션값 Validation 지정. 해당 Category에 해당하는 값만 입력되도록함
	 * 
	 * @return
	 */
	private IDataValidator getAddOptionValidator() {
		return new DataValidator() {

			@Override
			public boolean validate(int columnIndex, int rowIndex, Object newValue) {

				String category = getSelectedCategory();
				if (category == null)
					return true;
				if (newValue == null)
					return true;
				else if (newValue instanceof String) {
					String newValueStr = (String) newValue;

					// boolean isValidOption = isValidOption(category, newValueStr);
					if (validVariantList == null)
						return true;

					if (validVariantList.contains(newValueStr))
						return true;
					else
						throw new ValidationFailedException("Category에 허용되는 값이 아닙니다.");
				} else
					throw new ValidationFailedException("Category에 허용되는 값이 아닙니다.");
			}
		};
	}

	/**
	 * 옵션값 Validation 지정. 해당 Category에 해당하는 값만 입력되도록함
	 * 
	 * @return
	 */
	private IDataValidator getCategoryValueValidator() {
		return new DataValidator() {

			@Override
			public boolean validate(int columnIndex, int rowIndex, Object newValue) {
				if (newValue == null)
					return true;
				else if (newValue instanceof String) {
					String newValueStr = (String) newValue;
					PositionCoordinate[] selectCellPos = leftSelectionLayer.getSelectedCellPositions();
					if (selectCellPos.length == 0)
						return true;
					int leftRowIndex = leftSelectionLayer.getSelectedCellPositions()[0].rowPosition;
					Object categoryObj = leftSelectionLayer.getDataValueByPosition(
							EcoOspecCateData.getColumnIndexOfProperty(EcoOspecCateData.PROP_NAME_CATEGORY), leftRowIndex);
					ArrayList<String> categoryList = new ArrayList<String>();
					for (EcoOspecCateData data : leftTableDataList) {
						String category = data.getCategory();
						if (category == null)
							continue;
						categoryList.add(category);
					}

					if (categoryObj == null) {
						if (!categoryList.contains(newValueStr))
							return true;
						else
							throw new ValidationFailedException("이미 Category가 존재합니다.");
					} else {
						String selecteCategory = (String) categoryObj;
						if (!categoryList.contains(newValueStr) || categoryList.contains(newValueStr) && newValueStr.equals(selecteCategory))
							return true;
						else
							throw new ValidationFailedException("이미 Category가 존재합니다.");
					}
				} else
					throw new ValidationFailedException("유효한 Category가 아닙니다.");
			}
		};
	}

	/**
	 * Column 마다 적용되는 Label 을 등록
	 * 
	 * @param columnLabelAccumulator
	 */
	private void registerConfigLabelsOnColumns(ColumnOverrideLabelAccumulator columnLabelAccumulator, boolean isLeftTable) {

		if (isLeftTable) {
			columnLabelAccumulator.registerColumnOverrides(EcoOspecCateData.getColumnIndexOfProperty(EcoOspecCateData.PROP_NAME_ROW_CHECK),
					EcoOspecCateData.ALIGN_CELL_CONTENTS_CENTER_CONFIG_LABEL, EcoOspecCateData.CHECK_BOX_CONFIG_LABEL,
					EcoOspecCateData.CHECK_BOX_EDITOR_CONFIG_LABEL, EcoOspecCateData.EDITABLE_CONFIG_LABEL);
			columnLabelAccumulator.registerColumnOverrides(EcoOspecCateData.getColumnIndexOfProperty(EcoOspecCateData.PROP_NAME_CATEGORY),
					EcoOspecCateData.ALIGN_CELL_CONTENTS_CENTER_CONFIG_LABEL, EcoOspecCateData.CELL_EDITABLE_RULE_APPLY_LABLEL,
					EcoOspecCateData.PROP_NAME_CATEGORY);
			columnLabelAccumulator.registerColumnOverrides(EcoOspecCateData.getColumnIndexOfProperty(EcoOspecCateData.PROP_NAME_REVIEW_CONTENTS),
					EcoOspecCateData.EDITABLE_CONFIG_LABEL);
		} else {
			columnLabelAccumulator.registerColumnOverrides(EcoOspecCateData.getOptColumnIndexOfProperty(EcoOspecCateData.PROP_NAME_ROW_CHECK),
					EcoOspecCateData.ALIGN_CELL_CONTENTS_CENTER_CONFIG_LABEL, EcoOspecCateData.CHECK_BOX_CONFIG_LABEL,
					EcoOspecCateData.CHECK_BOX_EDITOR_CONFIG_LABEL, EcoOspecCateData.EDITABLE_CONFIG_LABEL);
			columnLabelAccumulator.registerColumnOverrides(EcoOspecCateData.getOptColumnIndexOfProperty(EcoOspecCateData.PROP_NAME_ADD_REVIEW_OPTION),
					EcoOspecCateData.ALIGN_CELL_CONTENTS_CENTER_CONFIG_LABEL, EcoOspecCateData.EDITABLE_CONFIG_LABEL,
					EcoOspecCateData.PROP_NAME_ADD_REVIEW_OPTION);
			columnLabelAccumulator.registerColumnOverrides(EcoOspecCateData.getOptColumnIndexOfProperty(EcoOspecCateData.PROP_NAME_ADD_OR_EX_CONDITION),
					EcoOspecCateData.ALIGN_CELL_CONTENTS_CENTER_CONFIG_LABEL, EcoOspecCateData.COMBO_ADD_OR_EX_CONFIG_LABEL,
					EcoOspecCateData.EDITABLE_CONFIG_LABEL);
		}
	}

	/**
	 * Cell 가운데 정렬
	 * 
	 * @param configRegistry
	 */
	private void registerAignCenterCellStyle(IConfigRegistry configRegistry) {
		Style cellStyle = new Style();
		cellStyle.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, HorizontalAlignmentEnum.CENTER);
		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, cellStyle, DisplayMode.NORMAL,
				EcoOspecCateData.ALIGN_CELL_CONTENTS_CENTER_CONFIG_LABEL);
	}

	/**
	 * 수정여부를 설정하는 Rule 등록
	 * 
	 * @param configRegistry
	 * @param dataProvider
	 */
	private void registerEditableRules(IConfigRegistry configRegistry, IDataProvider dataProvider) {
		configRegistry.registerConfigAttribute(EditConfigAttributes.CELL_EDITABLE_RULE, IEditableRule.ALWAYS_EDITABLE, DisplayMode.EDIT,
				EcoOspecCateData.EDITABLE_CONFIG_LABEL);
		configRegistry.registerConfigAttribute(EditConfigAttributes.CELL_EDITABLE_RULE, IEditableRule.ALWAYS_EDITABLE, DisplayMode.EDIT,
				EcoOspecCateData.COMBO_ADD_OR_EX_CONFIG_LABEL);
		configRegistry.registerConfigAttribute(EditConfigAttributes.CELL_EDITABLE_RULE, IEditableRule.ALWAYS_EDITABLE, DisplayMode.EDIT,
				EcoOspecCateData.CHECK_BOX_CONFIG_LABEL);
		configRegistry.registerConfigAttribute(EditConfigAttributes.CELL_EDITABLE_RULE, getEditRule(dataProvider), DisplayMode.EDIT,
				EcoOspecCateData.CELL_EDITABLE_RULE_APPLY_LABLEL);
	}

	/**
	 * 추가/제외 조건 Combo Box 설정
	 * 
	 * @param configRegistry
	 * @param comboBoxCellPainter
	 */
	private void addOrExComboBoxEditor(IConfigRegistry configRegistry, ICellPainter comboBoxCellPainter) {
		ComboBoxCellEditor comboBoxCellEditor = new ComboBoxCellEditor(addOrExLabelList);
		configRegistry.registerConfigAttribute(EditConfigAttributes.CELL_EDITOR, comboBoxCellEditor, DisplayMode.EDIT,
				EcoOspecCateData.COMBO_ADD_OR_EX_CONFIG_LABEL);
	}

	/**
	 * 체크박스 Editor
	 * 
	 * @param configRegistry
	 * @param checkBoxCellPainter
	 * @param checkBoxCellEditor
	 */
	private void registerCheckBoxEditor(IConfigRegistry configRegistry, ICellPainter checkBoxCellPainter, ICellEditor checkBoxCellEditor) {
		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER, checkBoxCellPainter, DisplayMode.NORMAL,
				EcoOspecCateData.CHECK_BOX_CONFIG_LABEL);
		configRegistry.registerConfigAttribute(CellConfigAttributes.DISPLAY_CONVERTER, new DefaultBooleanDisplayConverter(), DisplayMode.NORMAL,
				EcoOspecCateData.CHECK_BOX_CONFIG_LABEL);
		configRegistry.registerConfigAttribute(EditConfigAttributes.CELL_EDITOR, checkBoxCellEditor, DisplayMode.NORMAL,
				EcoOspecCateData.CHECK_BOX_EDITOR_CONFIG_LABEL);
	}

	/**
	 * Cell 수정 할수 있는지 Edit Rule을 설정함
	 * 
	 * @param dataProvider
	 * @return
	 */
	private IEditableRule getEditRule(final IDataProvider dataProvider) {
		return new EditableRule() {
			@Override
			public boolean isEditable(int columnIndex, int rowIndex) {

				Object isEditableCategoryObj = dataProvider.getDataValue(
						EcoOspecCateData.getColumnIndexOfProperty(EcoOspecCateData.PROP_NAME_CATEGORY_EDITABLE), rowIndex);
				if (isEditableCategoryObj == null)
					return false;
				boolean isEditableCategory = (Boolean) isEditableCategoryObj;

				return isEditableCategory;
			}
		};
	}

	/**
	 * 왼쪽 Table Cell Event 처리
	 */
	private void addLeftTableCellSectionEvent() {
		leftNattable.addMouseListener(new MouseListener() {

			@Override
			public void mouseUp(MouseEvent arg0) {

			}

			@Override
			public void mouseDown(MouseEvent arg0) {

				String category = getSelectedCategory();

				if (category == null)
					return;

				rightTableDataList.clear();

				if (addOrRemoveMap.get(category) == null)
					return;
				// 오른쪽 Table 에 Category 에 해당하는 Data 를 Load 함
				ArrayList<EcoOspecCateData> addOrRemoveList = addOrRemoveMap.get(category);
				for (EcoOspecCateData addOrRemoveData : addOrRemoveList) {
					rightTableDataList.add(addOrRemoveData);
				}

			}

			@Override
			public void mouseDoubleClick(MouseEvent arg0) {

			}
		});

	}

	/**
	 * 현재 선택된 Catergory를 가져옴
	 * 
	 * @return
	 */
	private String getSelectedCategory() {
		PositionCoordinate[] selectCellPos = leftSelectionLayer.getSelectedCellPositions();
		if (selectCellPos.length == 0)
			return null;

		int leftRowIndex = leftSelectionLayer.getSelectedCellPositions()[0].rowPosition;
		Object categoryObj = leftSelectionLayer.getDataValueByPosition(EcoOspecCateData.getColumnIndexOfProperty(EcoOspecCateData.PROP_NAME_CATEGORY),
				leftRowIndex);
		if (categoryObj == null)
			return null;

		return (String) categoryObj;
	}

	/**
	 * 비교 정보 설정
	 * 
	 * @param afterOspec
	 * @param beforeOspec
	 * @param onlyAfterData
	 * @param onlyBeforeData
	 */
	@SuppressWarnings("rawtypes")
	public void setCompareData(EcoStatusOptionTable afterTotalOSpecTable, EcoStatusOptionTable beforeTotalOSpecTable, Vector<Vector> onlyAfterData,
			Vector<Vector> onlyBeforeData) {
		this.afterTotalOSpecTable = afterTotalOSpecTable;
		this.beforeTotalOSpecTable = beforeTotalOSpecTable;
		this.onlyAfterData = onlyAfterData;
		this.onlyBeforeData = onlyBeforeData;
	}

	/**
	 * Category 에 유효한 Option 값인지 여부
	 * 
	 * @param category
	 * @param optionValue
	 * @return
	 */
	public boolean isValidOption(String category, String optionValue) {
		if (category.equals(optionValue))
			return false;
		if (category.equals("301")) {
			if (optionValue.equals("3C61") || optionValue.equals("3WCC"))
				return true;
		} else if (category.equals("302")) {
			if (optionValue.equals("3F02") || optionValue.equals("3W02"))
				return true;
		} else if (category.equals("303")) {
			if (optionValue.equals("3D00") || optionValue.equals("3WDD"))
				return true;
		} else if (category.equals("304")) {
			if (optionValue.equals("3B16") || optionValue.equals("3W16"))
				return true;
		} else if (category.equals("305")) {
			if (optionValue.equals("3A17") || optionValue.equals("3W17"))
				return true;
		} else if (category.equals("321")) {
			if (optionValue.equals("3A51") || optionValue.equals("3W51"))
				return true;
		} else if (category.equals("342")) {
			if (optionValue.equals("3E35") || optionValue.equals("3W35"))
				return true;
		} else if (category.equals("344")) {
			if (optionValue.equals("3D01") || optionValue.equals("3W01"))
				return true;
		} else if (category.equals("345")) {
			if (optionValue.equals("3A46") || optionValue.equals("3W46"))
				return true;
		} else if (category.equals("346")) {
			if (optionValue.equals("3D25") || optionValue.equals("3W25"))
				return true;
		} else {
			if (optionValue.startsWith(category))
				return true;
		}
		return false;
	}

	/**
	 * 옵션 검색시 옵션조건 입력 구문
	 * 
	 * @param category
	 * @return
	 */
	private String getSearchOptionClause(String category) {
		if (category.equals("301"))
			return "3C61|3WCC";
		else if (category.equals("302"))
			return "3F02|3W02";
		else if (category.equals("303"))
			return "3D00|3WDD";
		else if (category.equals("304"))
			return "3B16|3W16";
		else if (category.equals("305"))
			return "3A17|3W17";
		else if (category.equals("321"))
			return "3A51|3W51";
		else if (category.equals("342"))
			return "3E35|3W35";
		else if (category.equals("344"))
			return "3D01|3W01";
		else if (category.equals("345"))
			return "3A46|3W46";
		else if (category.equals("346"))
			return "3D25|3W25";
		else
			return category;
	}

	/**
	 * DB Table에 저장된 Category Data 를 Load 함
	 * 
	 * @param savedCategoryMap
	 */
	private void loadSavedCategoryData(LinkedHashMap<String, ArrayList<EcoOspecCateData>> savedCategoryMap) {
		for (String category : savedCategoryMap.keySet()) {
			ArrayList<EcoOspecCateData> valueList = savedCategoryMap.get(category);
			if (valueList.size() == 1) {
				EcoOspecCateData cateData = valueList.get(0);
				String addReviewOption = cateData.getAddReviewOption();
				if (addReviewOption != null)
					addOrRemoveMap.put(category, valueList);
			} else
				addOrRemoveMap.put(category, valueList);
			EcoOspecCateData rowData = new EcoOspecCateData();
			String revieContents = valueList.get(0).getReviewContents();
			rowData.setCategory(category);
			rowData.setReviewContents(revieContents);
			rowData.setCategoryEditable(false);
			leftTableDataList.add(rowData);
		}
	}

	/**
	 * 신규 Category Data 를 Load함
	 */
	private void loadNewCategoryData() {
		for (String category : targetCategoryList) {
			EcoOspecCateData rowData = new EcoOspecCateData();
			rowData.setCategory(category);
			rowData.setCategoryEditable(false);
			leftTableDataList.add(rowData);
		}
	}

	/**
	 * Category ComboBox Data 를 Load 함
	 * 
	 * @param targetCategoryList
	 */
	private void loadCategoryComboBoxData() {
		comboCategory.removeAll();
		comboCategory.add("ALL");
		comboCategory.select(0);
		for (String category : targetCategoryList) {
			comboCategory.add(category);
			comboCategory.setData(category);
		}
	}

	/**
	 * ECO 현황 변경검토 Category 정보 리스트
	 * 
	 * @param masterPuid
	 * @return
	 * @throws Exception
	 */
	private ArrayList<HashMap<String, String>> getRptChgReviewCategory(String masterPuid) throws Exception {
		CustomECODao dao = new CustomECODao();
		DataSet ds = new DataSet();
		ds.put("MASTER_PUID", masterPuid);
		ArrayList<HashMap<String, String>> reviewCategoryList = dao.getRptChgReviewCategory(ds);
		return reviewCategoryList;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void setValidVariantList() {

		validVariantList = new ArrayList<String>();
		SYMCRemoteUtil remote = new SYMCRemoteUtil();
		try {
			DataSet ds = new DataSet();
			ds.put("code_name", null);
			ArrayList<HashMap> list = (ArrayList) remote.execute("com.ssangyong.service.VariantService", "getVariantValueDesc", ds);
			if (list != null) {
				for (HashMap map : list) {
					validVariantList.add((String) map.get("CODE_NAME"));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Cell Error Dialog Open and Post 처리
	 * 
	 * @author baek
	 * 
	 */
	private class ValidationDialogErrorHandling extends DialogErrorHandling {

		@Override
		protected void showWarningDialog(String dialogMessage, String dialogTitle) {
			if (!isWarningDialogActive()) {
				// conversion/validation failed - so open dialog with error
				// message

				if (dialogMessage != null) {
					MessageDialog warningDialog = new MessageDialog(Display.getCurrent().getActiveShell(), dialogTitle, null, dialogMessage,
							MessageDialog.WARNING, new String[] { getDiscardButtonLabel() }, 0);

					// if discard was selected close the editor
					int returnCode = warningDialog.open();
					if (returnCode == 0 || returnCode == -1) {
						this.editor.close();
					}
				}
			}
		}
	}

	/**
	 * 초기 데이터 로딩
	 * 
	 * @author baek
	 * 
	 */
	public class InitDataLoadOperation extends AbstractAIFOperation {

		public InitDataLoadOperation() {
		}

		@Override
		public void executeOperation() throws Exception {
			try {

				Display.getDefault().syncExec(new Runnable() {
					@Override
					public void run() {

						try {
							// Key: Category, Value: Category 별 Condition 정보
							LinkedHashMap<String, ArrayList<EcoOspecCateData>> savedCategoryMap = new LinkedHashMap<String, ArrayList<EcoOspecCateData>>();
							// 저장된 Category 정보를 가져옴
							ArrayList<HashMap<String, String>> reviewCategoryMap = getRptChgReviewCategory(stdInformData.getMasterPuid());
							for (HashMap<String, String> reviewCateMap : reviewCategoryMap) {
								String category = reviewCateMap.get("OPTION_CATEGORY");
								String reviewContents = reviewCateMap.get("REVIEW_CONTENTS");
								String addReviewOption = reviewCateMap.get("OPTION_CODE");
								String addOrExConditionCode = reviewCateMap.get("OPTION_CONDITION");
								String addOrExCondition = null;
								if ("Y".equals(addOrExConditionCode))
									addOrExCondition = EcoOspecCateData.ADD_REVIEW_CONDTIOIN_ADD;
								else if ("N".equals(addOrExConditionCode))
									addOrExCondition = EcoOspecCateData.ADD_REVIEW_CONDTIOIN_EXCLUSION;

								EcoOspecCateData data = new EcoOspecCateData();
								data.setCategory(category);
								data.setReviewContents(reviewContents);
								data.setAddReviewOption(addReviewOption);
								data.setAddOrExCondition(addOrExCondition);
								ArrayList<EcoOspecCateData> savedValueList = null;

								if (!savedCategoryMap.containsKey(category)) {
									savedValueList = new ArrayList<EcoOspecCateData>();
									savedValueList.add(data);
									savedCategoryMap.put(category, savedValueList);
								} else {
									savedValueList = savedCategoryMap.get(category);
									savedValueList.add(data);
								}
							}

							// 이미 생성된 정보가 없으면 Load 함
							if (savedCategoryMap.size() == 0) {
								/**
								 * Option Category 테이블에 Load 함
								 */
								loadNewCategoryData();
								/**
								 * Option Category Combo 에 값을 추가함
								 */
								loadCategoryComboBoxData();
								return;
							}

							ArrayList<String> savedCategoryList = new ArrayList<String>(savedCategoryMap.keySet());
							Collections.sort(savedCategoryList);

							/**
							 * 비교된 결과 Category 정보와 저장된 Category 정보가 같으면
							 */
							if (CollectionUtils.isEqualCollection(targetCategoryList, savedCategoryList)) {
								/**
								 * 저장된 Option Category Data 를 Load 함
								 */
								loadSavedCategoryData(savedCategoryMap);
							} else {
								/**
								 * 비교된 결과 Category 정보와 저장된 Category 정보가 다르면
								 */
								int returnValue = ConfirmDialog.prompt(EcoStatusChangeInformInputDialog.this.getShell(), "Confirm",
										"이미 생성된 정보와 현재 비교된 정보가 다릅니다.\n신규 정보를 가져오시겠습니까?");
								/**
								 * (예) 신규 정보를 가져옴
								 */
								if (returnValue == IDialogConstants.YES_ID) {
									/**
									 * Option Category 테이블에 Load 함
									 */
									loadNewCategoryData();
								} else {
									/**
									 * (아니요)저장된 정보를 가져옴
									 */
									targetCategoryList.clear();
									targetCategoryList = savedCategoryList;
									/**
									 * 저장된 Option Category Data 를 Load 함
									 */
									loadSavedCategoryData(savedCategoryMap);
								}
							}
							/**
							 * Option Category Combo 에 값을 추가함
							 */
							loadCategoryComboBoxData();

							/**
							 * 전사옵션 정보를 설정함
							 */
							setValidVariantList();

						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});

			} catch (final Exception ex) {
				setAbortRequested(true);
				ex.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {
		EcoStatusChangeInformInputDialog dialog = new EcoStatusChangeInformInputDialog(null, null, null, null);
		dialog.open();
	}
}

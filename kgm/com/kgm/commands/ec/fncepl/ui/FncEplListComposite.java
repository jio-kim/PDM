package com.kgm.commands.ec.fncepl.ui;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.AbstractRegistryConfiguration;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.IEditableRule;
import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.nebula.widgets.nattable.data.convert.DefaultBooleanDisplayConverter;
import org.eclipse.nebula.widgets.nattable.edit.EditConfigAttributes;
import org.eclipse.nebula.widgets.nattable.edit.action.ToggleCheckBoxColumnAction;
import org.eclipse.nebula.widgets.nattable.edit.editor.CheckBoxCellEditor;
import org.eclipse.nebula.widgets.nattable.edit.editor.ICellEditor;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.groupBy.ModernGroupByThemeExtension;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ColumnLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.layer.cell.ColumnOverrideLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.painter.cell.CheckBoxPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.ColumnHeaderCheckBoxPainter;
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
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.wb.swt.SWTResourceManager;

import ca.odell.glazedlists.EventList;

import com.kgm.commands.ec.ecostatus.model.EcoStatusData;
import com.kgm.commands.ec.ecostatus.model.OspecSelectData;
import com.kgm.commands.ec.ecostatus.utility.BasicGridEditorGridLayer;
import com.kgm.commands.ec.ecostatus.utility.TableStylingThemeConfiguration;
import com.kgm.commands.ec.fncepl.model.FncEplCheckData;
import com.kgm.commands.ec.fncepl.operation.DownLoadUploadFileOperation;
import com.kgm.commands.ec.fncepl.operation.ExportFncEplRptOperation;
import com.kgm.commands.ec.fncepl.operation.SaveFncEplCheckListOperation;
import com.kgm.commands.ec.fncepl.operation.SearchFncEplCheckListOperation;
import com.kgm.common.utils.CustomUtil;
import com.kgm.common.utils.SYMTcUtil;
import com.teamcenter.rac.kernel.TCComponentListOfValues;
import com.teamcenter.rac.kernel.TCComponentListOfValuesType;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.ConfirmDialog;
import com.teamcenter.rac.util.MessageBox;

/**
 * Function EPL List Composite
 * 
 * @author baek
 * 
 */
public class FncEplListComposite extends Composite {

	private CCombo comboProdNo;
	private CCombo comboAddEcoPublish;
	private Text textFunctionNo;
	private Text textApplyEco;
	private Button btnLatest;

	private NatTable resultTable;
	private EventList<FncEplCheckData> tableDataList;
	private TCSession tcSession = null;

	private ArrayList<FncEplCheckData> removeDataList = null; // 제거되는 리스트
	private SelectionLayer selectionLayer = null; // Table Body의 선택되는 Layer
	private boolean isInitDataLoad;
	private Button btnExport;

	/**
	 * Create the composite.
	 * 
	 * @param parent
	 * @param style
	 */
	public FncEplListComposite(Composite parent, int style) {
		super(parent, style);
		initUI();
		initDataLoad();
		this.tcSession = CustomUtil.getTCSession();
	}

	public void initUI() {
		GridLayout gridLayout = new GridLayout(1, false);
		gridLayout.verticalSpacing = 0;
		gridLayout.marginWidth = 0;
		gridLayout.marginHeight = 0;
		gridLayout.horizontalSpacing = 0;
		this.setLayout(gridLayout);
		FormToolkit toolkit = new FormToolkit(getParent().getDisplay());

		Section sectionbBasic = toolkit.createSection(this, Section.TITLE_BAR);
		GridData gd_sectionbBasic = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
		gd_sectionbBasic.minimumWidth = 0;
		sectionbBasic.setLayoutData(gd_sectionbBasic);

		Composite compositeBasic = toolkit.createComposite(sectionbBasic, SWT.NONE);
		compositeBasic.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		sectionbBasic.setClient(compositeBasic);
		GridLayout gl_compositeBasic = new GridLayout(11, false);
		gl_compositeBasic.marginHeight = 10;
		gl_compositeBasic.horizontalSpacing = 10;
		compositeBasic.setLayout(gl_compositeBasic);

		Label lblProdNo = new Label(compositeBasic, SWT.NONE);
		toolkit.adapt(lblProdNo, true, true);
		lblProdNo.setText("Veh/Prod No.");

		comboProdNo = new CCombo(compositeBasic, SWT.BORDER);
		GridData gd_comboProdNo = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_comboProdNo.widthHint = 150;
		comboProdNo.setLayoutData(gd_comboProdNo);
		toolkit.adapt(comboProdNo);
		toolkit.paintBordersFor(comboProdNo);
		comboProdNo.setEditable(false);

		Label lblLatestCheck = new Label(compositeBasic, SWT.NONE);
		toolkit.adapt(lblLatestCheck, true, true);
		lblLatestCheck.setText("Latest");

		btnLatest = new Button(compositeBasic, SWT.CHECK);
		btnLatest.setSelection(true);
		toolkit.adapt(btnLatest, true, true);

		Label lblFunctionNo = new Label(compositeBasic, SWT.NONE);
		lblFunctionNo.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		toolkit.adapt(lblFunctionNo, true, true);
		lblFunctionNo.setText("Function No");

		textFunctionNo = new Text(compositeBasic, SWT.BORDER);
		GridData gd_textFunctionNo = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_textFunctionNo.widthHint = 80;
		textFunctionNo.setLayoutData(gd_textFunctionNo);
		toolkit.adapt(textFunctionNo, true, true);

		Label lblApplyEco = new Label(compositeBasic, SWT.NONE);
		lblApplyEco.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		toolkit.adapt(lblApplyEco, true, true);
		lblApplyEco.setText("\uBC18\uC601 ECO");

		textApplyEco = new Text(compositeBasic, SWT.BORDER);
		GridData gd_textApplyEco = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_textApplyEco.widthHint = 80;
		textApplyEco.setLayoutData(gd_textApplyEco);
		toolkit.adapt(textApplyEco, true, true);

		Label lblAddEcoPublish = new Label(compositeBasic, SWT.NONE);
		toolkit.adapt(lblAddEcoPublish, true, true);
		lblAddEcoPublish.setText("\uCD94\uAC00 ECO \uBC1C\uD589");

		comboAddEcoPublish = new CCombo(compositeBasic, SWT.BORDER);
		GridData gd_comboAddEcoPublish = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_comboAddEcoPublish.widthHint = 100;
		comboAddEcoPublish.setLayoutData(gd_comboAddEcoPublish);
		toolkit.adapt(comboAddEcoPublish);
		toolkit.paintBordersFor(comboAddEcoPublish);
		comboAddEcoPublish.setEditable(false);

		Button btnSearch = new Button(compositeBasic, SWT.NONE);
		GridData gd_btnSearch = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_btnSearch.widthHint = 80;
		btnSearch.setLayoutData(gd_btnSearch);
		toolkit.adapt(btnSearch, true, true);
		btnSearch.setText("\uC870\uD68C");
		btnSearch.setImage(SWTResourceManager.getImage(FncEplListComposite.class, "/icons/search_16.png"));
		btnSearch.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				doSearch();
			};
		});

		Section sectionResult = toolkit.createSection(this, Section.TITLE_BAR);
		GridData gd_sectionResult = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_sectionResult.heightHint = 5;
		gd_sectionResult.minimumWidth = 0;
		sectionResult.setLayoutData(gd_sectionResult);

		Composite compositeResult = toolkit.createComposite(sectionResult, SWT.WRAP);
		compositeResult.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		GridLayout gl_compositeResult = new GridLayout(1, false);
		gl_compositeResult.marginWidth = 0;
		gl_compositeResult.marginHeight = 0;
		gl_compositeResult.horizontalSpacing = -1;
		gl_compositeResult.verticalSpacing = 0;
		compositeResult.setLayout(gl_compositeResult);
		sectionResult.setClient(compositeResult);

		String[] propertyNames = FncEplCheckData.getPropertyNames();
		Map<String, String> propertyToLabelMap = FncEplCheckData.getPropertyToLabelMap();

		ConfigRegistry configRegistry = new ConfigRegistry();
		BasicGridEditorGridLayer<FncEplCheckData> gridLayer = new BasicGridEditorGridLayer<FncEplCheckData>(new ArrayList<FncEplCheckData>(), configRegistry,
				propertyNames, propertyToLabelMap);

		final DataLayer bodyDataLayer = gridLayer.getBodyLayer().getDataLayer();
		tableDataList = gridLayer.getTableDataList();

		ColumnOverrideLabelAccumulator columnLabelAccumulator = new ColumnOverrideLabelAccumulator(bodyDataLayer);
		bodyDataLayer.setConfigLabelAccumulator(columnLabelAccumulator);
		ColumnHeaderCheckBoxPainter columnHeaderCheckBoxPainter = new ColumnHeaderCheckBoxPainter(bodyDataLayer);

		resultTable = new NatTable(compositeResult, gridLayer, false);
		resultTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		resultTable.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));

		resultTable.setConfigRegistry(configRegistry);
		resultTable.addConfiguration(new DefaultNatTableStyleConfiguration()); // 기본 Style 설정
		resultTable.addConfiguration(new SingleClickSortConfiguration()); // Single Click 시 Sort 설정
		resultTable.addConfiguration(setModelEditorConfiguration(columnLabelAccumulator, bodyDataLayer, columnHeaderCheckBoxPainter));
		resultTable.configure();

		// 선택된 Table Layer
		selectionLayer = gridLayer.getBodyLayer().getSelectionLayer();
		bodyDataLayer.setDefaultColumnWidthByPosition(FncEplCheckData.getColumnIndexOfProperty(FncEplCheckData.PROP_NAME_ROW_CHECK), 35);
		bodyDataLayer.setDefaultColumnWidthByPosition(FncEplCheckData.getColumnIndexOfProperty(FncEplCheckData.PROP_NAME_IS_LATEST_CHECK), 50);
		bodyDataLayer.setDefaultColumnWidthByPosition(FncEplCheckData.getColumnIndexOfProperty(FncEplCheckData.PROP_NAME_FUNCTION_NO), 100);
		bodyDataLayer.setDefaultColumnWidthByPosition(FncEplCheckData.getColumnIndexOfProperty(FncEplCheckData.PROP_NAME_BASE_DATE), 100);
		bodyDataLayer.setDefaultColumnWidthByPosition(FncEplCheckData.getColumnIndexOfProperty(FncEplCheckData.PROP_NAME_APPLY_ECO_NO), 80);
		bodyDataLayer.setDefaultColumnWidthByPosition(FncEplCheckData.getColumnIndexOfProperty(FncEplCheckData.PROP_NAME_ADD_ECO_PUBLISH), 100);
		bodyDataLayer.setDefaultColumnWidthByPosition(FncEplCheckData.getColumnIndexOfProperty(FncEplCheckData.PROP_NAME_DESCRIPTION), 250);
		bodyDataLayer.setDefaultColumnWidthByPosition(FncEplCheckData.getColumnIndexOfProperty(FncEplCheckData.PROP_NAME_CREATE_DATE), 100);
		bodyDataLayer.setDefaultColumnWidthByPosition(FncEplCheckData.getColumnIndexOfProperty(FncEplCheckData.PROP_NAME_FNC_EPL_PUID), 0);
		bodyDataLayer.setDefaultColumnWidthByPosition(FncEplCheckData.getColumnIndexOfProperty(FncEplCheckData.PROP_NAME_ROW_DATA_OBJ), 0);

		// Cell 선택 이벤트
		addCellSectionEvent();

		ThemeConfiguration theme = new TableStylingThemeConfiguration();
		theme.addThemeExtension(new ModernGroupByThemeExtension());
		resultTable.setTheme(theme);
		gridLayer.getColumnHeaderDataLayer().setDefaultRowHeight(25);

		Composite compositeBottom = toolkit.createComposite(this, SWT.BORDER);
		compositeBottom.setLayout(new GridLayout(2, false));
		compositeBottom.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Composite compositeRowButton = new Composite(compositeBottom, SWT.NONE);
		toolkit.adapt(compositeRowButton);
		toolkit.paintBordersFor(compositeRowButton);
		compositeRowButton.setLayout(new GridLayout(2, false));

		Button btnAdd = new Button(compositeRowButton, SWT.NONE);
		btnAdd.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				createFncEplCheckCreate();
			}
		});
		btnAdd.setImage(SWTResourceManager.getImage(FncEplListComposite.class, "/icons/plus_16.png"));
		GridData gd_btnAdd = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_btnAdd.widthHint = 25;
		btnAdd.setLayoutData(gd_btnAdd);

		Button btnRemove = new Button(compositeRowButton, SWT.NONE);
		btnRemove.setImage(SWTResourceManager.getImage(FncEplListComposite.class, "/icons/minus_16.png"));
		GridData gd_btnRemove = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_btnRemove.widthHint = 25;
		btnRemove.setLayoutData(gd_btnRemove);
		btnRemove.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				removeRow();
			}
		});

		Composite compositeRightButton = new Composite(compositeBottom, SWT.NONE);
		compositeRightButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1));
		toolkit.adapt(compositeRightButton);
		toolkit.paintBordersFor(compositeRightButton);
		compositeRightButton.setLayout(new GridLayout(4, false));

		Button btnDownload = new Button(compositeRightButton, SWT.NONE);
		toolkit.adapt(btnDownload, true, true);
		btnDownload.setText("Download");
		btnDownload.setImage(SWTResourceManager.getImage(FncEplListComposite.class, "/icons/Download_16.png"));
		btnDownload.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				doDownLoad();
			}

		});

		btnExport = new Button(compositeRightButton, SWT.NONE);
		btnExport.setBounds(0, 0, 76, 25);
		toolkit.adapt(btnExport, true, true);
		btnExport.setText("Excel Export");
		btnExport.setImage(SWTResourceManager.getImage(FncEplListComposite.class, "/com/kgm/common/images/export_16.png"));
		btnExport.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				doExport();
			}
		});

		Button btnSave = new Button(compositeRightButton, SWT.NONE);
		btnSave.setImage(SWTResourceManager.getImage(FncEplListComposite.class, "/icons/save_16.png"));
		GridData gd_btnSave = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
		gd_btnSave.widthHint = 60;
		btnSave.setLayoutData(gd_btnSave);
		toolkit.adapt(btnSave, true, true);
		btnSave.setText("Save");
		btnSave.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				doSave();
			}

		});
		new Label(compositeRightButton, SWT.NONE);
	}

	/**
	 * 조회
	 */
	private void doSearch() {
		tableDataList.clear();
		if (removeDataList != null)
			removeDataList.clear();

		String selectedProdNo = comboProdNo.getText();
		String prodNo = comboProdNo.getData(selectedProdNo) == null ? null : (String) comboProdNo.getData(selectedProdNo);
		String isLatestCheck = btnLatest.getSelection() ? "V" : null;
		String functionNo = "".equals(textFunctionNo.getText()) ? null : textFunctionNo.getText().replace("*", "%");
		String applyEcoNo = "".equals(textApplyEco.getText()) ? null : textApplyEco.getText().replace("*", "%");
		String addEcoPublish = "".equals(comboAddEcoPublish.getText()) ? null : comboAddEcoPublish.getText();

		FncEplCheckData inputData = new FncEplCheckData();
		inputData.setProdNo(prodNo);
		inputData.setIsLatestCheck(isLatestCheck);
		inputData.setFunctionNo(functionNo);
		inputData.setApplyEcoNo(applyEcoNo);
		inputData.setAddEcoPublish(addEcoPublish);
		inputData.setTableDataList(tableDataList);

		SearchFncEplCheckListOperation operation = new SearchFncEplCheckListOperation(inputData);
		tcSession.queueOperation(operation);

	}

	public AbstractRegistryConfiguration setModelEditorConfiguration(final ColumnOverrideLabelAccumulator columnLabelAccumulator,
			final DataLayer bodyDataLayer, final ColumnHeaderCheckBoxPainter columnHeaderCheckBoxPainter) {

		return new AbstractRegistryConfiguration() {
			@Override
			public void configureRegistry(IConfigRegistry configRegistry) {
				// 전체선택 Header
				ICellPainter columnHeaderPainter = new CustomLineBorderDecorator(new CellPainterDecorator(new TextPainter(), CellEdgeEnum.BOTTOM,
						columnHeaderCheckBoxPainter));
				configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER, columnHeaderPainter, DisplayMode.NORMAL,
						ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + 0);

				// Column 마다 적용된 Label 설정
				registerConfigLabelsOnColumns(columnLabelAccumulator);

				// 체크박스 Editor 설정
				registerCheckBoxEditor(configRegistry, new CheckBoxPainter(), new CheckBoxCellEditor());

				registerAignCenterCellStyle(configRegistry);

				configRegistry.registerConfigAttribute(EditConfigAttributes.CELL_EDITABLE_RULE, IEditableRule.ALWAYS_EDITABLE, DisplayMode.EDIT,
						OspecSelectData.CHECK_BOX_CONFIG_LABEL);
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
	 * 등록
	 */
	private void createFncEplCheckCreate() {

		FncEplCheckCreateDialog dialog = new FncEplCheckCreateDialog(this.getShell(), null, tableDataList);
		dialog.open();
	}

	/**
	 * Row Data 삭제
	 */
	private void removeRow() {
		if (removeDataList == null)
			removeDataList = new ArrayList<FncEplCheckData>();
		try {
			for (FncEplCheckData rowData : tableDataList) {
				boolean isChecked = rowData.isRowCheck();

				if (!isChecked)
					continue;
				if (!removeDataList.contains(rowData))
					removeDataList.add(rowData);
			}

			for (FncEplCheckData rowData : removeDataList)
				tableDataList.remove(rowData);

			resultTable.refresh();
		} catch (Exception ex) {
			MessageBox.post(FncEplListComposite.this.getShell(), ex.toString(), "Error", MessageBox.ERROR);
		}
	}

	/**
	 * 선택된 Function 파일 저장
	 */
	private void doDownLoad() {
		ArrayList<FncEplCheckData> selectDataList = new ArrayList<FncEplCheckData>();
		for (FncEplCheckData rowData : tableDataList) {
			boolean isChecked = rowData.isRowCheck();
			if (!isChecked)
				continue;
			selectDataList.add(rowData);
		}

		DirectoryDialog dialog = new DirectoryDialog(this.getShell());
		dialog.setFilterPath("c:\\");
		String selectedDirectory = dialog.open();
		if (selectedDirectory == null)
			return;
		if (selectDataList.size() == 0) {
			MessageBox.post(this.getShell(), "다운로드할 Function(행)을 체크하여 주십시오", "WARNING", MessageBox.WARNING);
			return;
		}

		DownLoadUploadFileOperation operation = new DownLoadUploadFileOperation(selectDataList, selectedDirectory);
		tcSession.queueOperation(operation);

	};

	/**
	 * 저장
	 */
	private void doSave() {
		/**
		 * 삭제 수행
		 */
		ArrayList<FncEplCheckData> deleteDataList = new ArrayList<FncEplCheckData>(); // 삭제되는 Data 리스트
		if (removeDataList == null)
			return;
		for (FncEplCheckData rowData : removeDataList) {
			deleteDataList.add(rowData);
		}
		removeDataList.clear();
		removeDataList = null;

		FncEplCheckData saveData = new FncEplCheckData();
		saveData.setDeleteDataList(deleteDataList);
		SaveFncEplCheckListOperation operation = new SaveFncEplCheckListOperation(saveData);
		tcSession.queueOperation(operation);
	}

	/**
	 * Excel 출력
	 */
	private void doExport() {
		File selectedFile = null;
		try {
			selectedFile = selectFile(FncEplCheckData.TEMPLATE_FNC_EPL_CHECK_RPT, null);

			if (selectedFile == null)
				return;

			FncEplCheckData inputData = new FncEplCheckData();
			inputData.setTableDataList(tableDataList);
			ExportFncEplRptOperation operation = new ExportFncEplRptOperation(inputData, selectedFile);
			tcSession.queueOperation(operation);

		} catch (Exception ex) {
			MessageBox.post(this.getShell(), ex.toString(), "Error", MessageBox.ERROR);
		}

	}

	/**
	 * 초기 Data Load
	 */
	public void initDataLoad() {
		/**
		 * Veh/Prod No
		 */
		comboProdNo.removeAll();
		comboProdNo.add("");
		comboValueSetting(comboProdNo, "S7_PLANT_CODE");
		comboValueSetting(comboProdNo, "S7_PRODUCT_CODE");
		// 추가 ECO
		comboAddEcoPublish.add("");
		String[] ecoPublishList = FncEplCheckData.ECO_PUBLISH_LIST;
		for (String status : ecoPublishList)
			comboAddEcoPublish.add(status);

		isInitDataLoad = true;

	}

	private void comboValueSetting(CCombo combo, String lovName) {
		try {
			tcSession = CustomUtil.getTCSession();
			if (lovName != null) {
				TCComponentListOfValuesType listofvaluestype = (TCComponentListOfValuesType) tcSession.getTypeComponent("ListOfValues");
				TCComponentListOfValues[] listofvalues = listofvaluestype.find(lovName);
				if (listofvalues == null || listofvalues.length == 0) {
					return;
				}
				TCComponentListOfValues listofvalue = listofvalues[0];
				String[] lovValues = listofvalue.getListOfValues().getStringListOfValues();
				String[] lovDesces = listofvalue.getListOfValues().getDescriptions();
				int i = 0;
				for (String lovValue : lovValues) {
					combo.add(lovValue + " (" + lovDesces[i] + ")");
					combo.setData(lovValue + " (" + lovDesces[i] + ")", lovValue);
					i++;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 파일 선택
	 * 
	 * @param templateName
	 * @return
	 * @throws Exception
	 */
	private File selectFile(String templateName, String appendName) throws Exception {
		File selectedFile = null;
		FileDialog fDialog = new FileDialog(this.getShell(), SWT.SINGLE | SWT.SAVE);

		SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmm");
		Date toDay = new Date();
		String fileName = appendName == null ? templateName.replace("template.xlsx", "") + df.format(toDay) : templateName.replace("template.xlsx", "")
				+ appendName + "_" + df.format(toDay);

		fDialog.setFilterNames(new String[] { "Excel File" });
		fDialog.setFileName(fileName);

		fDialog.setFilterExtensions(new String[] { "*.xlsx" });
		String strRet = fDialog.open();

		if ((strRet == null) || (strRet.equals("")))
			return null;

		String strfileName = fDialog.getFileName();
		if ((strfileName == null) || (strfileName.equals("")))
			return null;

		String strDownLoadFilePath = fDialog.getFilterPath() + File.separatorChar + strfileName;

		File checkFile = new File(strDownLoadFilePath);
		if (checkFile.exists()) {
			int retValue = ConfirmDialog.prompt(getShell(), "Confirm", strDownLoadFilePath + " File already exists.\nDo you want to overwrite it?");
			if (retValue != IDialogConstants.YES_ID)
				return null;
		}

		File tempFile = SYMTcUtil.getTemplateFile(tcSession, templateName, null);
		if (checkFile.exists())
			checkFile.delete();
		selectedFile = new File(strDownLoadFilePath);
		tempFile.renameTo(selectedFile);

		return selectedFile;
	}

	/**
	 * Cell 선택 이벤트
	 */
	private void addCellSectionEvent() {
		resultTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				PositionCoordinate[] selectCellPos = selectionLayer.getSelectedCellPositions();
				if (selectCellPos.length == 0)
					return;

				int rowIndex = selectionLayer.getSelectedCellPositions()[0].rowPosition;
				int columnIndex = selectionLayer.getSelectedCellPositions()[0].columnPosition;

				if (columnIndex == FncEplCheckData.getColumnIndexOfProperty(FncEplCheckData.PROP_NAME_ROW_CHECK))
					return;

				Object rowDataObj = selectionLayer.getDataValueByPosition(FncEplCheckData.getColumnIndexOfProperty(FncEplCheckData.PROP_NAME_ROW_DATA_OBJ),
						rowIndex);
				if (rowDataObj == null)
					return;

				FncEplCheckCreateDialog dialog = new FncEplCheckCreateDialog(FncEplListComposite.this.getShell(), (FncEplCheckData) rowDataObj, tableDataList);
				dialog.open();

			}
		});

	}

	private void registerConfigLabelsOnColumns(ColumnOverrideLabelAccumulator columnLabelAccumulator) {
		columnLabelAccumulator.registerColumnOverrides(FncEplCheckData.getColumnIndexOfProperty(FncEplCheckData.PROP_NAME_ROW_CHECK),
				FncEplCheckData.CHECK_BOX_EDITOR_CONFIG_LABEL, FncEplCheckData.CHECK_BOX_CONFIG_LABEL, FncEplCheckData.ALIGN_CELL_CONTENTS_CENTER_CONFIG_LABEL);
		columnLabelAccumulator.registerColumnOverrides(FncEplCheckData.getColumnIndexOfProperty(FncEplCheckData.PROP_NAME_IS_LATEST_CHECK),
				FncEplCheckData.ALIGN_CELL_CONTENTS_CENTER_CONFIG_LABEL);
		columnLabelAccumulator.registerColumnOverrides(FncEplCheckData.getColumnIndexOfProperty(FncEplCheckData.PROP_NAME_FUNCTION_NO),
				FncEplCheckData.ALIGN_CELL_CONTENTS_CENTER_CONFIG_LABEL);
		columnLabelAccumulator.registerColumnOverrides(FncEplCheckData.getColumnIndexOfProperty(FncEplCheckData.PROP_NAME_BASE_DATE),
				FncEplCheckData.ALIGN_CELL_CONTENTS_CENTER_CONFIG_LABEL);
		columnLabelAccumulator.registerColumnOverrides(FncEplCheckData.getColumnIndexOfProperty(FncEplCheckData.PROP_NAME_APPLY_ECO_NO),
				FncEplCheckData.ALIGN_CELL_CONTENTS_CENTER_CONFIG_LABEL);
		columnLabelAccumulator.registerColumnOverrides(FncEplCheckData.getColumnIndexOfProperty(FncEplCheckData.PROP_NAME_ADD_ECO_PUBLISH),
				FncEplCheckData.ALIGN_CELL_CONTENTS_CENTER_CONFIG_LABEL);
		columnLabelAccumulator.registerColumnOverrides(FncEplCheckData.getColumnIndexOfProperty(FncEplCheckData.PROP_NAME_CREATE_DATE),
				FncEplCheckData.ALIGN_CELL_CONTENTS_CENTER_CONFIG_LABEL);
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
				FncEplCheckData.CHECK_BOX_CONFIG_LABEL);
		configRegistry.registerConfigAttribute(CellConfigAttributes.DISPLAY_CONVERTER, new DefaultBooleanDisplayConverter(), DisplayMode.NORMAL,
				EcoStatusData.CHECK_BOX_CONFIG_LABEL);
		configRegistry.registerConfigAttribute(EditConfigAttributes.CELL_EDITOR, checkBoxCellEditor, DisplayMode.NORMAL,
				EcoStatusData.CHECK_BOX_EDITOR_CONFIG_LABEL);
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
				OspecSelectData.ALIGN_CELL_CONTENTS_CENTER_CONFIG_LABEL);
	}

	/**
	 * 검색 조건 초기 Data 가 이미 설정되었는지 체크함
	 * 
	 * @return the isInitDataLoad
	 */
	public boolean isInitDataLoad() {
		return isInitDataLoad;
	}
}

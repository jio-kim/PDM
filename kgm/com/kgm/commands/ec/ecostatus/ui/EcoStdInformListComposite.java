package com.kgm.commands.ec.ecostatus.ui;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.AbstractRegistryConfiguration;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.IEditableRule;
import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.data.convert.DefaultBooleanDisplayConverter;
import org.eclipse.nebula.widgets.nattable.edit.EditConfigAttributes;
import org.eclipse.nebula.widgets.nattable.edit.action.MouseEditAction;
import org.eclipse.nebula.widgets.nattable.edit.command.UpdateDataCommand;
import org.eclipse.nebula.widgets.nattable.edit.command.UpdateDataCommandHandler;
import org.eclipse.nebula.widgets.nattable.edit.editor.CheckBoxCellEditor;
import org.eclipse.nebula.widgets.nattable.edit.editor.ICellEditor;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.groupBy.ModernGroupByThemeExtension;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.AbstractOverrider;
import org.eclipse.nebula.widgets.nattable.layer.event.CellVisualChangeEvent;
import org.eclipse.nebula.widgets.nattable.painter.cell.CheckBoxPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.ICellPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.TextPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.decorator.BeveledBorderDecorator;
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
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.wb.swt.SWTResourceManager;

import ca.odell.glazedlists.EventList;

import com.kgm.commands.ec.ecostatus.model.EcoStatusData;
import com.kgm.commands.ec.ecostatus.model.EcoStatusData.STATUS_COLOR;
import com.kgm.commands.ec.ecostatus.operation.SearchEcoStatusStdListOperation;
import com.kgm.commands.ec.ecostatus.utility.GroupGridEditorGridLayer;
import com.kgm.commands.ec.ecostatus.utility.TableStylingThemeConfiguration;
import com.kgm.common.remote.DataSet;
import com.kgm.common.remote.SYMCRemoteUtil;
import com.kgm.common.utils.CustomUtil;
import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;
import com.teamcenter.rac.util.controls.DateControl;

/**
 * 기준 정보 리스트 UI 구성
 * 
 * @author baek
 * 
 */
public class EcoStdInformListComposite extends Composite {

	private boolean isInitDataLoad = false; // 초기 Data 로드 되었는지 여부
	private EventList<EcoStatusData> tableDataList; // 테이블 데이터 리스트
	private NatTable table; // 테이블
	private CCombo comboGModel = null; // GMODEL
	private CCombo comboProject = null; // PROJECT
	private DateControl regStartDate;
	private DateControl regEndDate;
	private CCombo comboStatus;
	private Button btnSearch;
	private Registry registry;
	private SelectionLayer selectionLayer = null; // Table Body의 선택되는 Layer
	private TCSession tcSession = null;
	private ArrayList<String> gModelList = null;
	private ArrayList<String> projectList = null;

	/**
	 * Create the composite.
	 * 
	 * @param parent
	 * @param style
	 */
	public EcoStdInformListComposite(Composite parent, int style) {
		super(parent, style);
		tcSession = CustomUtil.getTCSession();
		this.registry = Registry.getRegistry(this);
		initUI();
	}

	private void initUI() {
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
		GridLayout gl_compositeBasic = new GridLayout(12, false);
		gl_compositeBasic.marginHeight = 10;
		gl_compositeBasic.horizontalSpacing = 10;
		compositeBasic.setLayout(gl_compositeBasic);

		Label lblGModel = new Label(compositeBasic, SWT.NONE);
		toolkit.adapt(lblGModel, true, true);
		lblGModel.setText("G-Model");

		comboGModel = new CCombo(compositeBasic, SWT.BORDER);
		comboGModel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					// Project 리스트 설정
					setProjectList();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});
		comboGModel.setEditable(false);
		toolkit.adapt(comboGModel);
		toolkit.paintBordersFor(comboGModel);
		GridData gd_comboGModel = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		comboGModel.setLayoutData(gd_comboGModel);
		gd_comboGModel.widthHint = 50;

		Label lblProjectCode = new Label(compositeBasic, SWT.NONE);
		toolkit.adapt(lblProjectCode, true, true);
		lblProjectCode.setText("Project");

		comboProject = new CCombo(compositeBasic, SWT.BORDER);
		comboProject.setEditable(false);

		GridData gd_comboProject = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_comboProject.widthHint = 100;
		comboProject.setLayoutData(gd_comboProject);
		toolkit.adapt(comboProject);
		toolkit.paintBordersFor(comboProject);

		Label lblRegisterDate = new Label(compositeBasic, SWT.NONE);
		toolkit.adapt(lblRegisterDate, true, true);
		lblRegisterDate.setText("등록일");

		regStartDate = new DateControl(compositeBasic, null, "yyyy-MM-dd");
		GridData gd_firstDate = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_firstDate.widthHint = 120;
		regStartDate.setLayoutData(gd_firstDate);
		regStartDate.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		regStartDate.setBackgroundMode(SWT.INHERIT_FORCE);

		Label lblTag1 = new Label(compositeBasic, SWT.NONE);
		toolkit.adapt(lblTag1, true, true);
		lblTag1.setText("~");

		regEndDate = new DateControl(compositeBasic, null, "yyyy-MM-dd");
		GridData gd_endDate = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_endDate.widthHint = 120;
		regEndDate.setLayoutData(gd_endDate);
		regEndDate.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		regEndDate.setBackgroundMode(SWT.INHERIT_FORCE);

		toolkit.adapt(regEndDate);
		toolkit.paintBordersFor(regEndDate);

		Label lblStatus = new Label(compositeBasic, SWT.NONE);
		toolkit.adapt(lblStatus, true, true);
		lblStatus.setText("상태");

		comboStatus = new CCombo(compositeBasic, SWT.BORDER);
		GridData gd_comboStatus = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_comboStatus.widthHint = 80;
		comboStatus.setLayoutData(gd_comboStatus);
		comboStatus.setEditable(false);
		toolkit.adapt(comboStatus);
		toolkit.paintBordersFor(comboStatus);
		Composite compositeTopButton = new Composite(compositeBasic, SWT.NONE);
		compositeTopButton.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, true, false, 1, 1));
		compositeTopButton.setLayout(new GridLayout(1, false));

		btnSearch = new Button(compositeTopButton, SWT.NONE);
		btnSearch.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		btnSearch.setText("조회");
		btnSearch.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				doSearch();
			}
		});
		btnSearch.setImage(SWTResourceManager.getImage(EcoChangeListMgrComposite.class, "/icons/search_16.png"));
		GridData gd_btnSearch = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
		gd_btnSearch.widthHint = 70;
		btnSearch.setLayoutData(gd_btnSearch);

		Section sectionResult = toolkit.createSection(this, Section.TITLE_BAR);
		sectionResult.setText("기준정보 리스트");
		GridData gd_sectionResult = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_sectionResult.heightHint = 0;
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

		String[] propertyNames = EcoStatusData.getStdPropertyNames();
		Map<String, String> propertyToLabelMap = EcoStatusData.getStdPropertyToLabelMap();

		ConfigRegistry configRegistry = new ConfigRegistry();
		GroupGridEditorGridLayer<EcoStatusData> gridLayer = new GroupGridEditorGridLayer<EcoStatusData>(new ArrayList<EcoStatusData>(), configRegistry,
				propertyNames, propertyToLabelMap);

		final DataLayer bodyDataLayer = gridLayer.getBodyLayer().getDataLayer();
		tableDataList = gridLayer.getTableDataList();
		/**
		 * 한 Row만 체크되도록 함
		 */
		bodyDataLayer.registerCommandHandler(new UpdateDataCommandHandler(bodyDataLayer) {
			@Override
			protected boolean doCommand(UpdateDataCommand command) {
				int columnPosition = command.getColumnPosition();
				int rowPosition = command.getRowPosition();

				if (columnPosition == 0) {
					EcoStatusData data = (EcoStatusData) bodyDataLayer.getDataProvider().getDataValue(
							EcoStatusData.getStdColumnIndexOfProperty(EcoStatusData.PROP_NAME_ROW_DATA_OBJ), rowPosition);
					Boolean isCheck = (Boolean) command.getNewValue();
					data.setRowCheck(isCheck);
					bodyDataLayer.fireLayerEvent(new CellVisualChangeEvent(bodyDataLayer, columnPosition, rowPosition));
					for (EcoStatusData tableData : tableDataList) {
						if (data.equals(tableData))
							continue;
						if (!isCheck)
							continue;
						if (tableData.isRowCheck())
							tableData.setRowCheck(false);
					}
					return true;
				} else {
					return super.doCommand(command);
				}
			}
		});

		@SuppressWarnings("unchecked")
		IRowDataProvider<EcoStatusData> bodyDataProvider = (IRowDataProvider<EcoStatusData>) bodyDataLayer.getDataProvider();
		CustomLabelAccumulator columnLabelAccumulator = new CustomLabelAccumulator(bodyDataProvider);
		bodyDataLayer.setConfigLabelAccumulator(columnLabelAccumulator);

		table = new NatTable(compositeResult, gridLayer, false);
		GridData gd_changeTable = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_changeTable.heightHint = 150;

		table.setLayoutData(gd_changeTable);
		table.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		table.setConfigRegistry(configRegistry);
		table.addConfiguration(new DefaultNatTableStyleConfiguration()); // 기본 Style 설정
		table.addConfiguration(new SingleClickSortConfiguration()); // Single Click 시 Sort 설정
		table.addConfiguration(setModelEditorConfiguration(columnLabelAccumulator, bodyDataLayer, new CheckBoxPainter()));
		table.configure();

		// 선택된 Table Layer
		selectionLayer = gridLayer.getBodyLayer().getSelectionLayer();
		bodyDataLayer.setDefaultColumnWidthByPosition(EcoStatusData.getStdColumnIndexOfProperty(EcoStatusData.PROP_NAME_ROW_CHECK), 35);
		bodyDataLayer.setDefaultColumnWidthByPosition(EcoStatusData.getStdColumnIndexOfProperty(EcoStatusData.PROP_NAME_STATUS), 50);
		bodyDataLayer.setDefaultColumnWidthByPosition(EcoStatusData.getStdColumnIndexOfProperty(EcoStatusData.PROP_NAME_STAGE_TYPE), 60);
		bodyDataLayer.setDefaultColumnWidthByPosition(EcoStatusData.getStdColumnIndexOfProperty(EcoStatusData.PROP_NAME_PROJECT_ID), 50);
		bodyDataLayer.setDefaultColumnWidthByPosition(EcoStatusData.getStdColumnIndexOfProperty(EcoStatusData.PROP_NAME_OSPECT_ID), 200);
		bodyDataLayer.setDefaultColumnWidthByPosition(EcoStatusData.getStdColumnIndexOfProperty(EcoStatusData.PROP_NAME_CHANGE_DESC), 300);
		bodyDataLayer.setDefaultColumnWidthByPosition(EcoStatusData.getStdColumnIndexOfProperty(EcoStatusData.PROP_NAME_EST_APPLY_DATE), 85);
		bodyDataLayer.setDefaultColumnWidthByPosition(EcoStatusData.getStdColumnIndexOfProperty(EcoStatusData.PROP_NAME_RECEIPT_DATE), 80);
		bodyDataLayer.setDefaultColumnWidthByPosition(EcoStatusData.getStdColumnIndexOfProperty(EcoStatusData.PROP_NAME_COMPLETE_REQ_DATE), 80);
		bodyDataLayer.setDefaultColumnWidthByPosition(EcoStatusData.getStdColumnIndexOfProperty(EcoStatusData.PROP_NAME_EST_CHANGE_PERIOD), 100);
		bodyDataLayer.setDefaultColumnWidthByPosition(EcoStatusData.getStdColumnIndexOfProperty(EcoStatusData.PROP_NAME_REGISTER_DATE), 80);
		bodyDataLayer.setDefaultColumnWidthByPosition(EcoStatusData.getStdColumnIndexOfProperty(EcoStatusData.PROP_NAME_MASTER_PUID), 0);
		bodyDataLayer.setDefaultColumnWidthByPosition(EcoStatusData.getStdColumnIndexOfProperty(EcoStatusData.PROP_NAME_IS_STATUS_COLOR), 0);
		bodyDataLayer.setDefaultColumnWidthByPosition(EcoStatusData.getStdColumnIndexOfProperty(EcoStatusData.PROP_NAME_ROW_DATA_OBJ), 0);

		// Cell 선택 이벤트
		addCellSectionEvent();
		// add modern styling
		ThemeConfiguration theme = new TableStylingThemeConfiguration();
		theme.addThemeExtension(new ModernGroupByThemeExtension());
		table.setTheme(theme);
		gridLayer.getColumnHeaderDataLayer().setDefaultRowHeight(25);

		Composite compositeBottom = toolkit.createComposite(this, SWT.BORDER);
		compositeBottom.setLayout(new GridLayout(1, false));
		compositeBottom.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Composite compositeRightButton = new Composite(compositeBottom, SWT.NONE);
		compositeRightButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1));
		toolkit.adapt(compositeRightButton);
		toolkit.paintBordersFor(compositeRightButton);
		GridLayout gl_compositeRightButton = new GridLayout(1, false);
		compositeRightButton.setLayout(gl_compositeRightButton);

		Button btnCreateStdInform = new Button(compositeRightButton, SWT.NONE);
		GridData gd_btnCreateStdInform = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_btnCreateStdInform.widthHint = 80;
		btnCreateStdInform.setLayoutData(gd_btnCreateStdInform);
		toolkit.adapt(btnCreateStdInform, true, true);
		btnCreateStdInform.setText("New");
		btnCreateStdInform.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				createStdInform();
			}
		});
		btnCreateStdInform.setImage(SWTResourceManager.getImage(EcoChangeListMgrComposite.class, "/com/teamcenter/rac/aif/images/create_16.png"));

	}

	/**
	 * 기준정보 생성
	 */
	protected void createStdInform() {
		EcoStdInformCreateDialog dialog = new EcoStdInformCreateDialog(this.getShell(), gModelList, projectList, null, tableDataList);
		dialog.open();

	}

	/**
	 * G Model 리스트를 가져옴
	 */
	@SuppressWarnings("unchecked")
	private ArrayList<String> setGModelList() throws Exception {
		comboGModel.removeAll();
		comboGModel.add("ALL");
		comboGModel.select(0);
		SYMCRemoteUtil remote = new SYMCRemoteUtil();
		DataSet ds = new DataSet();
		ds.put("NO-PARAM", null);

		ArrayList<String> gModelList = (ArrayList<String>) remote.execute("com.kgm.service.OSpecService", "getGModel", ds);
		Collections.sort(gModelList);
		for (String gmodel : gModelList) {
			comboGModel.add(gmodel);
			comboGModel.setData(gmodel);
		}
		return gModelList;
	}

	/**
	 * Project 리스트
	 * 
	 * @return
	 */
	private ArrayList<String> setProjectList() throws Exception {
		comboProject.removeAll();
		comboProject.add("ALL");
		comboProject.select(0);

		SYMCRemoteUtil remote = new SYMCRemoteUtil();
		DataSet ds = new DataSet();

		if (comboGModel.getSelectionIndex() > 0) {
			String gModel = (String) comboGModel.getText();
			ds.put("G_MODEL", gModel);
		} else {
			ds.put("NO-PARAM", null);
		}

		@SuppressWarnings("unchecked")
		ArrayList<String> projectList = (ArrayList<String>) remote.execute("com.kgm.service.OSpecService", "getProject", ds);
		Collections.sort(projectList);
		for (String project : projectList) {
			comboProject.add(project);
			comboProject.setData(project);
		}
		return projectList;

	}

	/**
	 * 상태 리스트
	 */
	private void setStatusList() {
		comboStatus.removeAll();
		String[] statusList = registry.getStringArray("STATUS.LIST");
		comboStatus.setItems(statusList);
		comboStatus.select(0);
	}

	/**
	 * 초기 Data Load
	 */
	public void loadInitData() {
		EcoStdInformListComposite.this.getShell().setDefaultButton(btnSearch);// 조회 버튼 Default 버튼으로
		InitDataLoadOperation operation = new InitDataLoadOperation();
		tcSession.queueOperation(operation);
	}

	/**
	 * 검색
	 */
	protected void doSearch() {
		tableDataList.clear();
		EcoStatusData data = new EcoStatusData();

		String gModel = "".equals(comboGModel.getText()) || "ALL".equals(comboGModel.getText()) ? null : comboGModel.getText();
		String projectId = "".equals(comboProject.getText()) || "ALL".equals(comboProject.getText()) ? null : comboProject.getText();
		String status = "".equals(comboStatus.getText()) || "ALL".equals(comboStatus.getText()) ? null : comboStatus.getText();

		data.setgModel(gModel);
		data.setProjectId(projectId);
		data.setStatus(status);
		data.setStartRegDate(regStartDate.getDate());
		data.setEndRegDate(regEndDate.getDate());

		data.setSearchChangeStatusList(tableDataList);
		SearchEcoStatusStdListOperation op = new SearchEcoStatusStdListOperation(data);
		tcSession.queueOperation(op);
	}

	/**
	 * Table Row Cell 에 Label 을 부여하여 줘서 Cell 의 Foreground Color 를 설정하도록 함
	 * 
	 * @author baek
	 * 
	 */
	class CustomLabelAccumulator extends AbstractOverrider {

		private IRowDataProvider<EcoStatusData> bodyDataProvider;

		CustomLabelAccumulator(IRowDataProvider<EcoStatusData> bodyDataProvider) {
			this.bodyDataProvider = bodyDataProvider;
		}

		public void registerColumnOverrides(int columnIndex, String... configLabels) {
			super.registerOverrides(columnIndex, configLabels);
		}

		private void addOverrides(LabelStack configLabels, Serializable key) {
			List<String> overrides = getOverrides(key);
			if (overrides != null) {
				for (String configLabel : overrides) {
					configLabels.addLabel(configLabel);
				}
			}
		}

		@Override
		public void accumulateConfigLabels(LabelStack configLabels, int columnPosition, int rowPosition) {
			EcoStatusData rowObject = this.bodyDataProvider.getRowObject(rowPosition);

			if (rowObject == null)
				return;
			addOverrides(configLabels, columnPosition);

			if (columnPosition == EcoStatusData.getStdColumnIndexOfProperty(EcoStatusData.PROP_NAME_STATUS)) {
				// boolean isStatusWarning = rowObject.isStausWarning();
				// if (isStatusWarning)
				// configLabels.addLabel(EcoStatusData.CELL_RED_LABLEL);
				STATUS_COLOR statusColor = rowObject.getStatusColor();
				if (statusColor.equals(STATUS_COLOR.BLUE))
					configLabels.addLabel(EcoStatusData.CELL_BLUE_LABLEL);
				else if (statusColor.equals(STATUS_COLOR.RED))
					configLabels.addLabel(EcoStatusData.CELL_RED_LABLEL);
			}

		}
	}

	/**
	 * Cell Label Editor
	 * 
	 * @param columnLabelAccumulator
	 * @param bodyDataLayer
	 * @return
	 */
	public AbstractRegistryConfiguration setModelEditorConfiguration(final CustomLabelAccumulator columnLabelAccumulator, final DataLayer bodyDataLayer,
			final CheckBoxPainter checkBoxPainter) {

		return new AbstractRegistryConfiguration() {
			@Override
			public void configureRegistry(IConfigRegistry configRegistry) {
				// Header multi line
				ICellPainter cellPainter = new BeveledBorderDecorator(new TextPainter(true, true, true, true));
				configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER, cellPainter, DisplayMode.NORMAL, GridRegion.COLUMN_HEADER);

				// Column 마다 적용된 Label 설정
				registerConfigLabelsOnColumns(columnLabelAccumulator);

				setCellColorStyle(configRegistry);

				registerAignCenterCellStyle(configRegistry);

				// 체크박스 Editor 설정
				registerCheckBoxEditor(configRegistry, checkBoxPainter, new CheckBoxCellEditor());

				// 수정여부를 설정하는 Rule 지정
				registerEditableRules(configRegistry, bodyDataLayer.getDataProvider());

			}

			@Override
			public void configureUiBindings(UiBindingRegistry uiBindingRegistry) {
				uiBindingRegistry.registerFirstSingleClickBinding(new CellPainterMouseEventMatcher(GridRegion.BODY, MouseEventMatcher.LEFT_BUTTON,
						checkBoxPainter), new MouseEditAction());
			}
		};
	}

	/**
	 * Column 마다 적용되는 Label 을 등록
	 * 
	 * @param columnLabelAccumulator
	 */
	private void registerConfigLabelsOnColumns(CustomLabelAccumulator columnLabelAccumulator) {
		// 체크박스
		columnLabelAccumulator.registerColumnOverrides(EcoStatusData.getStdColumnIndexOfProperty(EcoStatusData.PROP_NAME_ROW_CHECK),
				EcoStatusData.CHECK_BOX_EDITOR_CONFIG_LABEL, EcoStatusData.CHECK_BOX_CONFIG_LABEL, EcoStatusData.ALIGN_CELL_CONTENTS_CENTER_CONFIG_LABEL);

		columnLabelAccumulator.registerColumnOverrides(EcoStatusData.getStdColumnIndexOfProperty(EcoStatusData.PROP_NAME_OSPECT_ID),
				EcoStatusData.ALIGN_CELL_CONTENTS_CENTER_CONFIG_LABEL);
		columnLabelAccumulator.registerColumnOverrides(EcoStatusData.getStdColumnIndexOfProperty(EcoStatusData.PROP_NAME_STATUS),
				EcoStatusData.ALIGN_CELL_CONTENTS_CENTER_CONFIG_LABEL);
		columnLabelAccumulator.registerColumnOverrides(EcoStatusData.getStdColumnIndexOfProperty(EcoStatusData.PROP_NAME_EST_CHANGE_PERIOD),
				EcoStatusData.ALIGN_CELL_CONTENTS_CENTER_CONFIG_LABEL);
	}

	/**
	 * Cell Color 스타일을 등록함
	 * 
	 * @param configRegistry
	 */
	private void setCellColorStyle(IConfigRegistry configRegistry) {
		Style style = new Style();
		style.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR, GUIHelper.COLOR_RED);
		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, style, DisplayMode.NORMAL, EcoStatusData.CELL_RED_LABLEL);
		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, style, DisplayMode.SELECT, EcoStatusData.CELL_RED_LABLEL);

		style = new Style();
		style.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR, GUIHelper.COLOR_BLUE);
		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, style, DisplayMode.NORMAL, EcoStatusData.CELL_BLUE_LABLEL);
		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, style, DisplayMode.SELECT, EcoStatusData.CELL_BLUE_LABLEL);
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
				EcoStatusData.ALIGN_CELL_CONTENTS_CENTER_CONFIG_LABEL);
	}

	/**
	 * 수정여부를 설정하는 Rule 등록
	 * 
	 * @param configRegistry
	 * @param dataProvider
	 */
	private void registerEditableRules(IConfigRegistry configRegistry, IDataProvider dataProvider) {
		configRegistry.registerConfigAttribute(EditConfigAttributes.CELL_EDITABLE_RULE, IEditableRule.ALWAYS_EDITABLE, DisplayMode.EDIT,
				EcoStatusData.CHECK_BOX_CONFIG_LABEL);
	}

	/**
	 * 체크박스 Editor
	 * 
	 * @param configRegistry
	 * @param checkBoxCellPainter
	 * @param checkBoxCellEditor
	 */
	private void registerCheckBoxEditor(IConfigRegistry configRegistry, ICellPainter checkBoxCellPainter, ICellEditor checkBoxCellEditor) {
		configRegistry
				.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER, checkBoxCellPainter, DisplayMode.NORMAL, EcoStatusData.CHECK_BOX_CONFIG_LABEL);
		configRegistry.registerConfigAttribute(CellConfigAttributes.DISPLAY_CONVERTER, new DefaultBooleanDisplayConverter(), DisplayMode.NORMAL,
				EcoStatusData.CHECK_BOX_CONFIG_LABEL);
		configRegistry.registerConfigAttribute(EditConfigAttributes.CELL_EDITOR, checkBoxCellEditor, DisplayMode.NORMAL,
				EcoStatusData.CHECK_BOX_EDITOR_CONFIG_LABEL);
	}

	/**
	 * Table 이벤트 처리
	 */
	private void addCellSectionEvent() {
		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				PositionCoordinate[] selectCellPos = selectionLayer.getSelectedCellPositions();
				if (selectCellPos.length == 0)
					return;

				int rowIndex = selectionLayer.getSelectedCellPositions()[0].rowPosition;
				int columnIndex = selectionLayer.getSelectedCellPositions()[0].columnPosition;

				if (columnIndex == EcoStatusData.getStdColumnIndexOfProperty(EcoStatusData.PROP_NAME_ROW_CHECK))
					return;

				Object rowDataObj = selectionLayer.getDataValueByPosition(EcoStatusData.getStdColumnIndexOfProperty(EcoStatusData.PROP_NAME_ROW_DATA_OBJ),
						rowIndex);
				if (rowDataObj == null)
					return;
				EcoStdInformCreateDialog dialog = new EcoStdInformCreateDialog(EcoStdInformListComposite.this.getShell(), gModelList, projectList,
						(EcoStatusData) rowDataObj, tableDataList);
				dialog.open();

			}
		});
	}

	/**
	 * 검색 조건 초기 Data 가 이미 설정되었는지 체크함
	 * 
	 * @return the isInitDataLoad
	 */
	public boolean isInitDataLoad() {
		return isInitDataLoad;
	}

	/**
	 * 초기 데이터 로드 Operation
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
							/**
							 * G-MODEL 리스트 설정
							 */
							gModelList = setGModelList();
							/**
							 * Project No
							 */
							projectList = setProjectList();

							/**
							 * 상태
							 */
							setStatusList();
						} catch (Exception e) {

							e.printStackTrace();
						}
					}
				});

				isInitDataLoad = true;
			} catch (final Exception ex) {
				setAbortRequested(true);
				ex.printStackTrace();
			}
		}
	}

	/**
	 * 선택된 Row 정보를 가져옴
	 * 
	 * @return
	 */
	public EcoStatusData getSelectedRowData() {
		EcoStatusData selectedRowData = null;
		for (EcoStatusData data : tableDataList) {
			boolean isRowCheck = data.isRowCheck();
			if (isRowCheck) {
				selectedRowData = data;
				break;
			}
		}

		if (selectedRowData == null) {
			MessageBox.post(this.getShell(), "작성할 기준정보(행)를  체크하여 선택해 주십시오", "Warning", MessageBox.WARNING);
			return null;
		}
		return selectedRowData;
	}
}

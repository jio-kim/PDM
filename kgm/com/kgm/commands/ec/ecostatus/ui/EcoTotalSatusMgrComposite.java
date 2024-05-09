package com.kgm.commands.ec.ecostatus.ui;

import java.io.File;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.AbstractRegistryConfiguration;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.groupBy.ModernGroupByThemeExtension;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.AbstractOverrider;
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
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.wb.swt.SWTResourceManager;

import ca.odell.glazedlists.EventList;

import com.kgm.commands.ec.ecostatus.model.EcoStatusData;
import com.kgm.commands.ec.ecostatus.model.EcoStatusData.STATUS_COLOR;
import com.kgm.commands.ec.ecostatus.operation.ExportEcoStatusDescReportOperation;
import com.kgm.commands.ec.ecostatus.operation.ExportEcoTotalStatusListOperation;
import com.kgm.commands.ec.ecostatus.operation.SearchEcoTotalStatusListOperation;
import com.kgm.commands.ec.ecostatus.utility.GroupGridEditorGridLayer;
import com.kgm.commands.ec.ecostatus.utility.TableStylingThemeConfiguration;
import com.kgm.common.remote.DataSet;
import com.kgm.common.remote.SYMCRemoteUtil;
import com.kgm.common.utils.CustomUtil;
import com.kgm.common.utils.SYMTcUtil;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.ConfirmDialog;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;
import com.teamcenter.rac.util.controls.DateControl;

/**
 * 설계 변경 현황 관리 UI 기능 처릴
 */
public class EcoTotalSatusMgrComposite extends Composite {

	private EventList<EcoStatusData> tableDataList; // 테이블 데이터 리스트
	private NatTable changeTable; // 테이블
	private boolean isInitDataLoad = false; // 초기 Data 로드 되었는지 여부
	private CCombo comboProject;
	private Button btnSearch;
	private TCSession tcSession = null;
	private DateControl regStartDate;
	private DateControl regEndDate;
	private CCombo comboStageType;
	private CCombo comboStatus;
	private CCombo comboDesc;
	private Registry registry;
	private SelectionLayer selectionLayer = null; // Table Body의 선택되는 Layer

	public EcoTotalSatusMgrComposite(Composite parent, int style) {
		super(parent, style);
		tcSession = CustomUtil.getTCSession();
		this.registry = Registry.getRegistry(this);
		initUI();
	}

	/**
	 * 화면 UI
	 */
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
		GridLayout gl_compositeBasic = new GridLayout(13, false);
		gl_compositeBasic.marginHeight = 10;
		gl_compositeBasic.horizontalSpacing = 10;
		compositeBasic.setLayout(gl_compositeBasic);

		Label lblProjectCode = new Label(compositeBasic, SWT.NONE);
		toolkit.adapt(lblProjectCode, true, true);
		lblProjectCode.setText("Project");

		comboProject = new CCombo(compositeBasic, SWT.BORDER);
		comboProject.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					// setEcoStatusOptCategoryList();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});
		GridData gd_comboProject = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_comboProject.widthHint = 100;
		comboProject.setLayoutData(gd_comboProject);
		toolkit.adapt(comboProject);
		toolkit.paintBordersFor(comboProject);

		Label lblStageType = new Label(compositeBasic, SWT.NONE);
		toolkit.adapt(lblStageType, true, true);
		lblStageType.setText("분류");

		comboStageType = new CCombo(compositeBasic, SWT.BORDER);
		GridData gd_comboStageType = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_comboStageType.widthHint = 80;
		comboStageType.setLayoutData(gd_comboStageType);
		toolkit.adapt(comboStageType);
		toolkit.paintBordersFor(comboStageType);

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
		toolkit.adapt(comboStatus);
		toolkit.paintBordersFor(comboStatus);

		Label lblDescription = new Label(compositeBasic, SWT.NONE);
		toolkit.adapt(lblDescription, true, true);
		lblDescription.setText("비고");

		comboDesc = new CCombo(compositeBasic, SWT.BORDER);
		GridData gd_combo = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_combo.widthHint = 100;
		comboDesc.setLayoutData(gd_combo);
		toolkit.adapt(comboDesc);
		toolkit.paintBordersFor(comboDesc);

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
		gd_btnSearch.widthHint = 60;
		btnSearch.setLayoutData(gd_btnSearch);

		Section sectionResult = toolkit.createSection(this, Section.TITLE_BAR);
		sectionResult.setText("변경현황");
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

		String[] propertyNames = EcoStatusData.getPropertyNames();
		Map<String, String> propertyToLabelMap = EcoStatusData.getPropertyToLabelMap();

		ConfigRegistry configRegistry = new ConfigRegistry();
		GroupGridEditorGridLayer<EcoStatusData> gridLayer = new GroupGridEditorGridLayer<EcoStatusData>(new ArrayList<EcoStatusData>(), configRegistry,
				propertyNames, propertyToLabelMap);

		DataLayer bodyDataLayer = gridLayer.getBodyLayer().getDataLayer();
		tableDataList = gridLayer.getTableDataList();

		@SuppressWarnings("unchecked")
		IRowDataProvider<EcoStatusData> bodyDataProvider = (IRowDataProvider<EcoStatusData>) bodyDataLayer.getDataProvider();
		CustomLabelAccumulator columnLabelAccumulator = new CustomLabelAccumulator(bodyDataProvider);
		bodyDataLayer.setConfigLabelAccumulator(columnLabelAccumulator);

		changeTable = new NatTable(compositeResult, gridLayer, false);
		GridData gd_changeTable = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_changeTable.heightHint = 150;

		changeTable.setLayoutData(gd_changeTable);
		changeTable.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		changeTable.setConfigRegistry(configRegistry);
		changeTable.addConfiguration(new DefaultNatTableStyleConfiguration()); // 기본 Style 설정
		changeTable.addConfiguration(new SingleClickSortConfiguration()); // Single Click 시 Sort 설정
		changeTable.addConfiguration(setModelEditorConfiguration(columnLabelAccumulator, bodyDataLayer));
		changeTable.configure();

		// 선택된 Table Layer
		selectionLayer = gridLayer.getBodyLayer().getSelectionLayer();

		gridLayer.getColumnGroupHeader().addColumnsIndexesToGroup("완료", EcoStatusData.getColumnIndexOfProperty(EcoStatusData.PROP_NAME_IN_COMPLETE_CNT),
				EcoStatusData.getColumnIndexOfProperty(EcoStatusData.PROP_NAME_DELAY_COMPLETE_CNT),
				EcoStatusData.getColumnIndexOfProperty(EcoStatusData.PROP_NAME_MISS_COMPLETE_CNT));
		gridLayer.getColumnGroupHeader().addColumnsIndexesToGroup("진행중", EcoStatusData.getColumnIndexOfProperty(EcoStatusData.PROP_NAME_IN_PROC_CNT),
				EcoStatusData.getColumnIndexOfProperty(EcoStatusData.PROP_NAME_DELAY_PROC_CNT),
				EcoStatusData.getColumnIndexOfProperty(EcoStatusData.PROP_NAME_MISS_PROC_CNT));

		bodyDataLayer.setDefaultColumnWidthByPosition(EcoStatusData.getColumnIndexOfProperty(EcoStatusData.PROP_NAME_STATUS), 50);
		bodyDataLayer.setDefaultColumnWidthByPosition(EcoStatusData.getColumnIndexOfProperty(EcoStatusData.PROP_NAME_STAGE_TYPE), 60);
		bodyDataLayer.setDefaultColumnWidthByPosition(EcoStatusData.getColumnIndexOfProperty(EcoStatusData.PROP_NAME_PROJECT_ID), 50);
		bodyDataLayer.setDefaultColumnWidthByPosition(EcoStatusData.getColumnIndexOfProperty(EcoStatusData.PROP_NAME_OSPECT_ID), 120);
		bodyDataLayer.setDefaultColumnWidthByPosition(EcoStatusData.getColumnIndexOfProperty(EcoStatusData.PROP_NAME_CHANGE_DESC), 160);
		bodyDataLayer.setDefaultColumnWidthByPosition(EcoStatusData.getColumnIndexOfProperty(EcoStatusData.PROP_NAME_EST_APPLY_DATE), 85);
		bodyDataLayer.setDefaultColumnWidthByPosition(EcoStatusData.getColumnIndexOfProperty(EcoStatusData.PROP_NAME_RECEIPT_DATE), 80);
		// bodyDataLayer.setDefaultColumnWidthByPosition(EcoStatusData.getColumnIndexOfProperty(EcoStatusData.PROP_NAME_FIRST_MAIL_SEND_DATE), 83);
		bodyDataLayer.setDefaultColumnWidthByPosition(EcoStatusData.getColumnIndexOfProperty(EcoStatusData.PROP_NAME_COMPLETE_REQ_DATE), 80);
		bodyDataLayer.setDefaultColumnWidthByPosition(EcoStatusData.getColumnIndexOfProperty(EcoStatusData.PROP_NAME_EST_CHANGE_PERIOD), 60);
		bodyDataLayer.setDefaultColumnWidthByPosition(EcoStatusData.getColumnIndexOfProperty(EcoStatusData.PROP_NAME_ECO_LAST_COMPLETE_DATE), 80);
		bodyDataLayer.setDefaultColumnWidthByPosition(EcoStatusData.getColumnIndexOfProperty(EcoStatusData.PROP_NAME_REAL_CHG_REIOAD), 60);
		bodyDataLayer.setDefaultColumnWidthByPosition(EcoStatusData.getColumnIndexOfProperty(EcoStatusData.PROP_NAME_TOTAL_REVIEWLIST), 70);
		bodyDataLayer.setDefaultColumnWidthByPosition(EcoStatusData.getColumnIndexOfProperty(EcoStatusData.PROP_NAME_REQUIRED_ECO_LIST), 90);
		bodyDataLayer.setDefaultColumnWidthByPosition(EcoStatusData.getColumnIndexOfProperty(EcoStatusData.PROP_NAME_IN_COMPLETE_CNT), 80);
		bodyDataLayer.setDefaultColumnWidthByPosition(EcoStatusData.getColumnIndexOfProperty(EcoStatusData.PROP_NAME_DELAY_COMPLETE_CNT), 70);
		bodyDataLayer.setDefaultColumnWidthByPosition(EcoStatusData.getColumnIndexOfProperty(EcoStatusData.PROP_NAME_MISS_COMPLETE_CNT), 70);
		bodyDataLayer.setDefaultColumnWidthByPosition(EcoStatusData.getColumnIndexOfProperty(EcoStatusData.PROP_NAME_IN_PROC_CNT), 80);
		bodyDataLayer.setDefaultColumnWidthByPosition(EcoStatusData.getColumnIndexOfProperty(EcoStatusData.PROP_NAME_DELAY_PROC_CNT), 70);
		bodyDataLayer.setDefaultColumnWidthByPosition(EcoStatusData.getColumnIndexOfProperty(EcoStatusData.PROP_NAME_MISS_PROC_CNT), 70);
		bodyDataLayer.setDefaultColumnWidthByPosition(EcoStatusData.getColumnIndexOfProperty(EcoStatusData.PROP_NAME_DESCRIPTION), 100);
		bodyDataLayer.setDefaultColumnWidthByPosition(EcoStatusData.getColumnIndexOfProperty(EcoStatusData.PROP_NAME_REGISTER_DATE), 80);
		bodyDataLayer.setDefaultColumnWidthByPosition(EcoStatusData.getColumnIndexOfProperty(EcoStatusData.PROP_NAME_SPEC_ARRANGE), 0);
		bodyDataLayer.setDefaultColumnWidthByPosition(EcoStatusData.getColumnIndexOfProperty(EcoStatusData.PROP_NAME_MASTER_PUID), 0);
		bodyDataLayer.setDefaultColumnWidthByPosition(EcoStatusData.getColumnIndexOfProperty(EcoStatusData.PROP_NAME_IS_STATUS_COLOR), 0);
		bodyDataLayer.setDefaultColumnWidthByPosition(EcoStatusData.getColumnIndexOfProperty(EcoStatusData.PROP_NAME_ECO_FIRST_COMPLETE_DATE), 0);
		bodyDataLayer.setDefaultColumnWidthByPosition(EcoStatusData.getColumnIndexOfProperty(EcoStatusData.PROP_NAME_ECO_LAST_FIRST_PERIOD), 0);
		bodyDataLayer.setDefaultColumnWidthByPosition(EcoStatusData.getColumnIndexOfProperty(EcoStatusData.PROP_NAME_ROW_DATA_OBJ), 0);

		// add modern styling
		ThemeConfiguration theme = new TableStylingThemeConfiguration();
		theme.addThemeExtension(new ModernGroupByThemeExtension());
		changeTable.setTheme(theme);
		gridLayer.getColumnHeaderDataLayer().setDefaultRowHeight(50);

		Composite compositeBottom = toolkit.createComposite(this, SWT.NONE);
		compositeBottom.setLayout(new GridLayout(1, false));
		compositeBottom.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Composite compositeRightButton = new Composite(compositeBottom, SWT.NONE);
		compositeRightButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1));
		toolkit.adapt(compositeRightButton);
		toolkit.paintBordersFor(compositeRightButton);
		GridLayout gl_compositeRightButton = new GridLayout(2, false);
		compositeRightButton.setLayout(gl_compositeRightButton);

		Button btnListExport = new Button(compositeRightButton, SWT.NONE);
		GridData gd_btnListExport = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_btnListExport.widthHint = 110;
		btnListExport.setLayoutData(gd_btnListExport);
		toolkit.adapt(btnListExport, true, true);
		btnListExport.setText("현황 Excel Export");
		btnListExport.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				excelListExport();
			}
		});

		Button btnDescExport = new Button(compositeRightButton, SWT.NONE);
		GridData gd_btnUpload = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_btnUpload.widthHint = 110;
		btnDescExport.setLayoutData(gd_btnUpload);
		toolkit.adapt(btnDescExport, true, true);
		btnDescExport.setText("상세 Excel Export");
		btnDescExport.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				excelDescExport();
			}
		});

	}

	/**
	 * Cell Label Editor
	 * 
	 * @param columnLabelAccumulator
	 * @param bodyDataLayer
	 * @return
	 */
	public AbstractRegistryConfiguration setModelEditorConfiguration(final CustomLabelAccumulator columnLabelAccumulator, final DataLayer bodyDataLayer) {

		return new AbstractRegistryConfiguration() {
			@Override
			public void configureRegistry(IConfigRegistry configRegistry) {
				// Header multi line
				ICellPainter cellPainter = new BeveledBorderDecorator(new TextPainter(true, true, true, true));
				configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER, cellPainter, DisplayMode.NORMAL, GridRegion.COLUMN_HEADER);

				columnLabelAccumulator.registerColumnOverrides(EcoStatusData.getColumnIndexOfProperty(EcoStatusData.PROP_NAME_STATUS),
						EcoStatusData.ALIGN_CELL_CONTENTS_CENTER_CONFIG_LABEL);
				columnLabelAccumulator.registerColumnOverrides(EcoStatusData.getColumnIndexOfProperty(EcoStatusData.PROP_NAME_EST_CHANGE_PERIOD),
						EcoStatusData.ALIGN_CELL_CONTENTS_CENTER_CONFIG_LABEL);
				columnLabelAccumulator.registerColumnOverrides(EcoStatusData.getColumnIndexOfProperty(EcoStatusData.PROP_NAME_REAL_CHG_REIOAD),
						EcoStatusData.ALIGN_CELL_CONTENTS_CENTER_CONFIG_LABEL);
				columnLabelAccumulator.registerColumnOverrides(EcoStatusData.getColumnIndexOfProperty(EcoStatusData.PROP_NAME_TOTAL_REVIEWLIST),
						EcoStatusData.ALIGN_CELL_CONTENTS_CENTER_CONFIG_LABEL);
				columnLabelAccumulator.registerColumnOverrides(EcoStatusData.getColumnIndexOfProperty(EcoStatusData.PROP_NAME_TOTAL_REVIEWLIST),
						EcoStatusData.ALIGN_CELL_CONTENTS_CENTER_CONFIG_LABEL);
				columnLabelAccumulator.registerColumnOverrides(EcoStatusData.getColumnIndexOfProperty(EcoStatusData.PROP_NAME_REQUIRED_ECO_LIST),
						EcoStatusData.ALIGN_CELL_CONTENTS_CENTER_CONFIG_LABEL);
				columnLabelAccumulator.registerColumnOverrides(EcoStatusData.getColumnIndexOfProperty(EcoStatusData.PROP_NAME_IN_COMPLETE_CNT),
						EcoStatusData.ALIGN_CELL_CONTENTS_CENTER_CONFIG_LABEL);
				columnLabelAccumulator.registerColumnOverrides(EcoStatusData.getColumnIndexOfProperty(EcoStatusData.PROP_NAME_DELAY_COMPLETE_CNT),
						EcoStatusData.ALIGN_CELL_CONTENTS_CENTER_CONFIG_LABEL);
				columnLabelAccumulator.registerColumnOverrides(EcoStatusData.getColumnIndexOfProperty(EcoStatusData.PROP_NAME_MISS_COMPLETE_CNT),
						EcoStatusData.ALIGN_CELL_CONTENTS_CENTER_CONFIG_LABEL);
				columnLabelAccumulator.registerColumnOverrides(EcoStatusData.getColumnIndexOfProperty(EcoStatusData.PROP_NAME_IN_PROC_CNT),
						EcoStatusData.ALIGN_CELL_CONTENTS_CENTER_CONFIG_LABEL);
				columnLabelAccumulator.registerColumnOverrides(EcoStatusData.getColumnIndexOfProperty(EcoStatusData.PROP_NAME_DELAY_PROC_CNT),
						EcoStatusData.ALIGN_CELL_CONTENTS_CENTER_CONFIG_LABEL);
				columnLabelAccumulator.registerColumnOverrides(EcoStatusData.getColumnIndexOfProperty(EcoStatusData.PROP_NAME_MISS_PROC_CNT),
						EcoStatusData.ALIGN_CELL_CONTENTS_CENTER_CONFIG_LABEL);

				setCellColorStyle(configRegistry);

				registerAignCenterCellStyle(configRegistry);

			}
		};
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

			if (columnPosition == EcoStatusData.getColumnIndexOfProperty(EcoStatusData.PROP_NAME_DELAY_COMPLETE_CNT)) {
				int delayCompleteCnt = rowObject.getDelayCompleteCnt();
				if (delayCompleteCnt > 0)
					configLabels.addLabel(EcoStatusData.CELL_RED_LABLEL);
			}

			if (columnPosition == EcoStatusData.getColumnIndexOfProperty(EcoStatusData.PROP_NAME_MISS_COMPLETE_CNT)) {
				int missCompleteCnt = rowObject.getMissCompleteCnt();
				if (missCompleteCnt > 0)
					configLabels.addLabel(EcoStatusData.CELL_RED_LABLEL);
			}

			if (columnPosition == EcoStatusData.getColumnIndexOfProperty(EcoStatusData.PROP_NAME_DELAY_PROC_CNT)) {
				int delayProcessCnt = rowObject.getDelayProcessCnt();
				if (delayProcessCnt > 0)
					configLabels.addLabel(EcoStatusData.CELL_RED_LABLEL);
			}

			if (columnPosition == EcoStatusData.getColumnIndexOfProperty(EcoStatusData.PROP_NAME_MISS_PROC_CNT)) {
				int missProcessCnt = rowObject.getMissProcess();
				if (missProcessCnt > 0)
					configLabels.addLabel(EcoStatusData.CELL_RED_LABLEL);
			}

			if (columnPosition == EcoStatusData.getColumnIndexOfProperty(EcoStatusData.PROP_NAME_DESCRIPTION)) {
				String value = rowObject.getDescription();
				if (value.startsWith("지연") || value.startsWith("누락"))
					configLabels.addLabel(EcoStatusData.CELL_RED_LABLEL);
			}

			if (columnPosition == EcoStatusData.getColumnIndexOfProperty(EcoStatusData.PROP_NAME_STATUS)) {
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
	 * 초기 Data Load
	 */
	public void loadInitData() {

		try {
			/**
			 * Project No
			 */
			setProjectList();

			/**
			 * 분류
			 */
			setStageList();

			/**
			 * 상태
			 */
			setStatusList();

			/**
			 * 비고
			 */
			setDescList();

			EcoTotalSatusMgrComposite.this.getShell().getShell().setDefaultButton(btnSearch);// 조회버튼 Default 버튼

			isInitDataLoad = true;
		} catch (Exception ex) {
			MessageBox.post(this.getShell(), ex.toString(), "Error", MessageBox.ERROR);
		}
	}

	protected void doSearch() {
		tableDataList.clear();
		EcoStatusData data = new EcoStatusData();

		String projectId = "".equals(comboProject.getText()) || "ALL".equals(comboProject.getText()) ? null : comboProject.getText();
		String stageType = "".equals(comboStageType.getText()) || "ALL".equals(comboStageType.getText()) ? null : comboStageType.getText();
		String status = "".equals(comboStatus.getText()) || "ALL".equals(comboStatus.getText()) ? null : comboStatus.getText();
		String desc = "".equals(comboDesc.getText()) || "ALL".equals(comboDesc.getText()) ? null : comboDesc.getText();

		data.setProjectId(projectId);
		data.setStageType(stageType);
		data.setStatus(status);
		data.setDescription(desc);
		data.setStartRegDate(regStartDate.getDate());
		data.setEndRegDate(regEndDate.getDate());

		data.setSearchChangeStatusList(tableDataList);
		SearchEcoTotalStatusListOperation op = new SearchEcoTotalStatusListOperation(data);
		tcSession.queueOperation(op);
	}

	/**
	 * 설계 변경현황 상세 리스트 Export
	 */
	protected void excelDescExport() {
		File selectedFile = null;
		try {
			PositionCoordinate[] selectCellPos = selectionLayer.getSelectedCellPositions();
			if (selectCellPos.length == 0) {
				MessageBox.post(AIFUtility.getActiveDesktop().getShell(), "Select Row ", "Information", MessageBox.INFORMATION);
				return;
			}
			int rowIndex = selectionLayer.getSelectedCellPositions()[0].rowPosition;
			Object rowDataObj = selectionLayer.getDataValueByPosition(EcoStatusData.getColumnIndexOfProperty(EcoStatusData.PROP_NAME_ROW_DATA_OBJ), rowIndex);

			if (rowDataObj == null)
				return;
			EcoStatusData inputData = new EcoStatusData();
			inputData.setRowDataObj((EcoStatusData) rowDataObj);

			String appendTitle = inputData.getRowDataObj().getProjectId();

			selectedFile = selectFile(EcoStatusData.TEMPLATE_DS_ECO_TOTAL_DESC_RPT, appendTitle);

			if (selectedFile == null)
				return;

			// inputData.setSearchChangeStatusList(tableDataList);
			ExportEcoStatusDescReportOperation op = new ExportEcoStatusDescReportOperation(inputData, selectedFile);
			tcSession.queueOperation(op);

		} catch (Exception ex) {
			MessageBox.post(this.getShell(), ex.toString(), "Error", MessageBox.ERROR);
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
	 * 설계 변경 현황 리스트 Export
	 */
	protected void excelListExport() {
		File selectedFile = null;
		try {
			selectedFile = selectFile(EcoStatusData.TEMPLATE_DS_ECO_TOTAL_LIST_RPT, null);

			if (selectedFile == null)
				return;

			EcoStatusData inputData = new EcoStatusData();
			inputData.setSearchChangeStatusList(tableDataList);
			ExportEcoTotalStatusListOperation op = new ExportEcoTotalStatusListOperation(inputData, selectedFile);
			tcSession.queueOperation(op);

		} catch (Exception ex) {
			MessageBox.post(this.getShell(), ex.toString(), "Error", MessageBox.ERROR);
		}

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
		ds.put("NO-PARAM", null);

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
	 * 분류 리스트
	 */
	private void setStageList() {
		comboStageType.removeAll();
		String[] stageList = registry.getStringArray("STAGE_TYPE.LIST");
		comboStageType.setItems(stageList);
		comboStageType.select(0);
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
	 * 비고 리스트
	 */
	private void setDescList() {
		comboDesc.removeAll();
		String[] descList = registry.getStringArray("DESCRIPTION.LIST");
		comboDesc.setItems(descList);
		comboDesc.select(0);
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

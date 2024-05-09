package com.kgm.commands.ec.fncepl.ui;

import java.util.ArrayList;
import java.util.Map;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.AbstractRegistryConfiguration;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.IConfiguration;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.groupBy.ModernGroupByThemeExtension;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ColumnOverrideLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.painter.cell.ICellPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.TextPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.decorator.BeveledBorderDecorator;
import org.eclipse.nebula.widgets.nattable.sort.config.SingleClickSortConfiguration;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.HorizontalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.eclipse.nebula.widgets.nattable.style.theme.ThemeConfiguration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.wb.swt.SWTResourceManager;

import ca.odell.glazedlists.EventList;

import com.kgm.commands.ec.ecostatus.ui.EcoChangeListMgrComposite;
import com.kgm.commands.ec.ecostatus.utility.BasicGridEditorGridLayer;
import com.kgm.commands.ec.ecostatus.utility.TableStylingThemeConfiguration;
import com.kgm.commands.ec.fncepl.model.FncEplCheckData;
import com.kgm.commands.ec.fncepl.operation.SearchFncEplCheckStatusOperation;
import com.kgm.common.utils.CustomUtil;
import com.teamcenter.rac.kernel.TCComponentListOfValues;
import com.teamcenter.rac.kernel.TCComponentListOfValuesType;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.controls.DateControl;

public class FncEplStatusComposite extends Composite {

	private CCombo comboProdNo;
	private EventList<FncEplCheckData> tableDataList = null;
	private NatTable resultTable;
	private DateControl regStartDate;
	private DateControl regEndDate;
	private Button btnSearch;
	private boolean isInitDataLoad;
	private TCSession tcSession = null;

	/**
	 * Create the composite.
	 * 
	 * @param parent
	 * @param style
	 */
	public FncEplStatusComposite(Composite parent, int style) {
		super(parent, style);
		tcSession = CustomUtil.getTCSession();
		initUI();
		initDataLoad();
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
		GridLayout gl_compositeBasic = new GridLayout(8, false);
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

		Composite compositeTopButton = new Composite(compositeBasic, SWT.NONE);
		compositeTopButton.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, true, false, 1, 1));
		compositeTopButton.setLayout(new GridLayout(1, false));

		btnSearch = new Button(compositeBasic, SWT.NONE);
		btnSearch.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		btnSearch.setText("조회");
		btnSearch.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				doSearch();
			}

		});
		btnSearch.setImage(SWTResourceManager.getImage(EcoChangeListMgrComposite.class, "/icons/search_16.png"));

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

		String[] propertyNames = FncEplCheckData.getStsPropertyNames();
		Map<String, String> propertyToLabelMap = FncEplCheckData.getStsPropertyToLabelMap();

		ConfigRegistry configRegistry = new ConfigRegistry();
		BasicGridEditorGridLayer<FncEplCheckData> gridLayer = new BasicGridEditorGridLayer<FncEplCheckData>(new ArrayList<FncEplCheckData>(), configRegistry,
				propertyNames, propertyToLabelMap);

		final DataLayer bodyDataLayer = gridLayer.getBodyLayer().getDataLayer();
		tableDataList = gridLayer.getTableDataList();

		ColumnOverrideLabelAccumulator columnLabelAccumulator = new ColumnOverrideLabelAccumulator(bodyDataLayer);
		bodyDataLayer.setConfigLabelAccumulator(columnLabelAccumulator);

		resultTable = new NatTable(compositeResult, gridLayer, false);
		resultTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		resultTable.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));

		resultTable.setConfigRegistry(configRegistry);
		resultTable.addConfiguration(new DefaultNatTableStyleConfiguration()); // 기본 Style 설정
		resultTable.addConfiguration(new SingleClickSortConfiguration()); // Single Click 시 Sort 설정
		resultTable.addConfiguration(setModelEditorConfiguration(columnLabelAccumulator, bodyDataLayer));
		resultTable.configure();

		bodyDataLayer.setDefaultColumnWidthByPosition(FncEplCheckData.getStsColumnIndexOfProperty(FncEplCheckData.PROP_NAME_PROD_NO), 150);
		bodyDataLayer.setDefaultColumnWidthByPosition(FncEplCheckData.getStsColumnIndexOfProperty(FncEplCheckData.PROP_NAME_REG_FNC_CNT), 100);
		bodyDataLayer.setDefaultColumnWidthByPosition(FncEplCheckData.getStsColumnIndexOfProperty(FncEplCheckData.PROP_NAME_NEED_FNC_CNT), 100);
		bodyDataLayer.setDefaultColumnWidthByPosition(FncEplCheckData.getStsColumnIndexOfProperty(FncEplCheckData.PROP_NAME_SPEC_FNC_CNT), 100);

		ThemeConfiguration theme = new TableStylingThemeConfiguration();
		theme.addThemeExtension(new ModernGroupByThemeExtension());
		resultTable.setTheme(theme);
		gridLayer.getColumnHeaderDataLayer().setDefaultRowHeight(30);
	}

	public void initDataLoad() {
		comboProdNo.removeAll();
		comboProdNo.add("");
		comboValueSetting(comboProdNo, "S7_PLANT_CODE");
		comboValueSetting(comboProdNo, "S7_PRODUCT_CODE");
		isInitDataLoad = true;
	}

	private IConfiguration setModelEditorConfiguration(final ColumnOverrideLabelAccumulator columnLabelAccumulator, DataLayer bodyDataLayer) {
		return new AbstractRegistryConfiguration() {

			@Override
			public void configureRegistry(IConfigRegistry configRegistry) {
				// Header multi line
				ICellPainter cellPainter = new BeveledBorderDecorator(new TextPainter(true, true, true, true));
				configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER, cellPainter, DisplayMode.NORMAL, GridRegion.COLUMN_HEADER);

				columnLabelAccumulator.registerColumnOverrides(FncEplCheckData.getStsColumnIndexOfProperty(FncEplCheckData.PROP_NAME_REG_FNC_CNT),
						FncEplCheckData.ALIGN_CELL_CONTENTS_CENTER_CONFIG_LABEL);
				columnLabelAccumulator.registerColumnOverrides(FncEplCheckData.getStsColumnIndexOfProperty(FncEplCheckData.PROP_NAME_NEED_FNC_CNT),
						FncEplCheckData.ALIGN_CELL_CONTENTS_CENTER_CONFIG_LABEL);
				columnLabelAccumulator.registerColumnOverrides(FncEplCheckData.getStsColumnIndexOfProperty(FncEplCheckData.PROP_NAME_SPEC_FNC_CNT),
						FncEplCheckData.ALIGN_CELL_CONTENTS_CENTER_CONFIG_LABEL);

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
				FncEplCheckData.ALIGN_CELL_CONTENTS_CENTER_CONFIG_LABEL);
	}

	private void comboValueSetting(CCombo combo, String lovName) {
		try {
			TCSession tcSession = CustomUtil.getTCSession();
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
	 * 검색 조건 초기 Data 가 이미 설정되었는지 체크함
	 * 
	 * @return the isInitDataLoad
	 */
	public boolean isInitDataLoad() {
		return isInitDataLoad;
	}

	/**
	 * 조회
	 */
	private void doSearch() {
		tableDataList.clear();
		String selectedProdNo = comboProdNo.getText();

		String prodNo = comboProdNo.getData(selectedProdNo) == null ? null : (String) comboProdNo.getData(selectedProdNo);

		FncEplCheckData inputData = new FncEplCheckData();
		inputData.setProdNo(prodNo);
		inputData.setStartRegDate(regStartDate.getDate());
		inputData.setEndRegDate(regEndDate.getDate());
		inputData.setTableDataList(tableDataList);

		SearchFncEplCheckStatusOperation opeartion = new SearchFncEplCheckStatusOperation(inputData);
		tcSession.queueOperation(opeartion);

	}

}

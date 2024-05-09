package com.kgm.commands.ec.ecostatus.ui.template;

import java.util.ArrayList;
import java.util.Map;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.AbstractRegistryConfiguration;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.IEditableRule;
import org.eclipse.nebula.widgets.nattable.edit.EditConfigAttributes;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.groupBy.ModernGroupByThemeExtension;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ColumnOverrideLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.painter.cell.ICellPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.TextPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.decorator.BeveledBorderDecorator;
import org.eclipse.nebula.widgets.nattable.sort.config.SingleClickSortConfiguration;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.theme.ModernNatTableThemeConfiguration;
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

import com.kgm.commands.ec.ecostatus.model.EcoChangeData;
import com.kgm.commands.ec.ecostatus.utility.BasicGridEditorGridLayer;

public class EcoChangeListMgrCompositeTemplate extends Composite {

	//private EventList<EcoChangeData> tableDataList;
	private NatTable changeTable;

	/**
	 * Create the composite.
	 * 
	 * @param parent
	 * @param style
	 */
	public EcoChangeListMgrCompositeTemplate(Composite parent, int style) {
		super(parent, style);
		initUI();
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
		GridLayout gl_compositeBasic = new GridLayout(7, false);
		gl_compositeBasic.marginHeight = 10;
		gl_compositeBasic.horizontalSpacing = 10;
		compositeBasic.setLayout(gl_compositeBasic);

		Label lblGModel = new Label(compositeBasic, SWT.NONE);
		toolkit.adapt(lblGModel, true, true);
		lblGModel.setText("G-Model");

		CCombo comboGModel = new CCombo(compositeBasic, SWT.BORDER);
		toolkit.adapt(comboGModel);
		toolkit.paintBordersFor(comboGModel);
		GridData gd_comboGModel = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		comboGModel.setLayoutData(gd_comboGModel);
		gd_comboGModel.widthHint = 50;

		Label lblProjectCode = new Label(compositeBasic, SWT.NONE);
		toolkit.adapt(lblProjectCode, true, true);
		lblProjectCode.setText("Project");

		CCombo comboProject = new CCombo(compositeBasic, SWT.BORDER);
		GridData gd_comboProject = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_comboProject.widthHint = 100;
		comboProject.setLayoutData(gd_comboProject);
		toolkit.adapt(comboProject);
		toolkit.paintBordersFor(comboProject);

		Label lblOptCategory = new Label(compositeBasic, SWT.NONE);
		toolkit.adapt(lblOptCategory, true, true);
		lblOptCategory.setText("Option Category");

		CCombo comboOptCategory = new CCombo(compositeBasic, SWT.BORDER);
		GridData gd_comboOptCategory = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_comboOptCategory.widthHint = 60;

		comboOptCategory.setLayoutData(gd_comboOptCategory);
		toolkit.adapt(comboOptCategory);
		toolkit.paintBordersFor(comboOptCategory);

		Composite compositeTopButton = new Composite(compositeBasic, SWT.NONE);
		compositeTopButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1));
		compositeTopButton.setLayout(new GridLayout(2, false));
		
		Button btnRegister = new Button(compositeTopButton, SWT.NONE);
		btnRegister.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
			}
		});

		btnRegister.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		toolkit.adapt(btnRegister, true, true);
		btnRegister.setText("변경 관리 리스트 생성");

		Button btnSearch = new Button(compositeTopButton, SWT.NONE);
		btnSearch.setImage(SWTResourceManager.getImage(EcoChangeListMgrCompositeTemplate.class, "/icons/search_16.png"));
		GridData gd_btnSearch = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
		gd_btnSearch.widthHint = 70;
		btnSearch.setLayoutData(gd_btnSearch);
		toolkit.adapt(btnSearch, true, true);
		btnSearch.setText("조회");

		Section sectionResult = toolkit.createSection(this, Section.TITLE_BAR);
		GridData gd_sectionResult = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_sectionResult.heightHint = 5;
		gd_sectionResult.minimumWidth = 0;
		sectionResult.setLayoutData(gd_sectionResult);

		Composite compositeResult = toolkit.createComposite(sectionResult, SWT.WRAP);
		compositeResult.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		GridLayout gl_compositeResult = new GridLayout(1, false);
		gl_compositeResult.horizontalSpacing = -1;
		gl_compositeResult.verticalSpacing = 0;
		compositeResult.setLayout(gl_compositeResult);
		sectionResult.setClient(compositeResult);

		String[] propertyNames = EcoChangeData.getPropertyNames();
		Map<String, String> propertyToLabelMap = EcoChangeData.getPropertyToLabelMap();

		ConfigRegistry configRegistry = new ConfigRegistry();
		BasicGridEditorGridLayer<EcoChangeData> gridLayer = new BasicGridEditorGridLayer<EcoChangeData>(new ArrayList<EcoChangeData>(), configRegistry, propertyNames, propertyToLabelMap);

		DataLayer bodyDataLayer = gridLayer.getBodyLayer().getDataLayer();
		//tableDataList = gridLayer.getTableDataList();

		ColumnOverrideLabelAccumulator columnLabelAccumulator = new ColumnOverrideLabelAccumulator(bodyDataLayer);
		bodyDataLayer.setConfigLabelAccumulator(columnLabelAccumulator);

		changeTable = new NatTable(compositeResult, gridLayer, false);

		GridData gd_selectMbomTable = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_selectMbomTable.heightHint = 150;
		changeTable.setLayoutData(gd_selectMbomTable);
		changeTable.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));

		changeTable.setConfigRegistry(configRegistry);
		changeTable.addConfiguration(new DefaultNatTableStyleConfiguration()); // 기본 Style 설정
		changeTable.addConfiguration(new SingleClickSortConfiguration()); // Single Click 시 Sort 설정
		changeTable.addConfiguration(setModelEditorConfiguration(columnLabelAccumulator, bodyDataLayer));
		changeTable.configure();

		
		bodyDataLayer.setDefaultColumnWidthByPosition(EcoChangeData.getColumnIndexOfProperty(EcoChangeData.PROP_NAME_GROUP_SEQ_NO), 60);
		bodyDataLayer.setDefaultColumnWidthByPosition(EcoChangeData.getColumnIndexOfProperty(EcoChangeData.PROP_NAME_REGISTER_TYPE), 80);
		bodyDataLayer.setDefaultColumnWidthByPosition(EcoChangeData.getColumnIndexOfProperty(EcoChangeData.PROP_NAME_CATEGORY), 65);
		bodyDataLayer.setDefaultColumnWidthByPosition(EcoChangeData.getColumnIndexOfProperty(EcoChangeData.PROP_NAME_CREATION_DATE), 80);
		bodyDataLayer.setDefaultColumnWidthByPosition(EcoChangeData.getColumnIndexOfProperty(EcoChangeData.PROP_NAME_ECO_PUBLISH), 100);
		bodyDataLayer.setDefaultColumnWidthByPosition(EcoChangeData.getColumnIndexOfProperty(EcoChangeData.PROP_NAME_CHANGE_STATUS), 100);
		bodyDataLayer.setDefaultColumnWidthByPosition(EcoChangeData.getColumnIndexOfProperty(EcoChangeData.PROP_NAME_ENGINE_FLAG), 60);
		bodyDataLayer.setDefaultColumnWidthByPosition(EcoChangeData.getColumnIndexOfProperty(EcoChangeData.PROP_NAME_FUNCTION_NO), 120);
		bodyDataLayer.setDefaultColumnWidthByPosition(EcoChangeData.getColumnIndexOfProperty(EcoChangeData.PROP_NAME_PART_NAME), 150);
		bodyDataLayer.setDefaultColumnWidthByPosition(EcoChangeData.getColumnIndexOfProperty(EcoChangeData.PROP_NAME_PROJECT_ID), 60);
		bodyDataLayer.setDefaultColumnWidthByPosition(EcoChangeData.getColumnIndexOfProperty(EcoChangeData.PROP_NAME_OSPECT_ID), 120);
		bodyDataLayer.setDefaultColumnWidthByPosition(EcoChangeData.getColumnIndexOfProperty(EcoChangeData.PROP_NAME_CHANGE_DESC), 150);
		bodyDataLayer.setDefaultColumnWidthByPosition(EcoChangeData.getColumnIndexOfProperty(EcoChangeData.PROP_NAME_REVIEW_CONTENTS), 200);
		bodyDataLayer.setDefaultColumnWidthByPosition(EcoChangeData.getColumnIndexOfProperty(EcoChangeData.PROP_NAME_SYSTEM_NO), 60);
		bodyDataLayer.setDefaultColumnWidthByPosition(EcoChangeData.getColumnIndexOfProperty(EcoChangeData.PROP_NAME_USER_ID), 100);
		bodyDataLayer.setDefaultColumnWidthByPosition(EcoChangeData.getColumnIndexOfProperty(EcoChangeData.PROP_NAME_TEAM_NAME), 100);
		bodyDataLayer.setDefaultColumnWidthByPosition(EcoChangeData.getColumnIndexOfProperty(EcoChangeData.PROP_NAME_MAIL_STATUS), 100);
		bodyDataLayer.setDefaultColumnWidthByPosition(EcoChangeData.getColumnIndexOfProperty(EcoChangeData.PROP_NAME_ECO_NO), 80);
		bodyDataLayer.setDefaultColumnWidthByPosition(EcoChangeData.getColumnIndexOfProperty(EcoChangeData.PROP_NAME_ECO_COMPLETE_DATE), 100);
		bodyDataLayer.setDefaultColumnWidthByPosition(EcoChangeData.getColumnIndexOfProperty(EcoChangeData.PROP_NAME_DESCRIPTION), 150);
		bodyDataLayer.setDefaultColumnWidthByPosition(EcoChangeData.getColumnIndexOfProperty(EcoChangeData.PROP_NAME_MASTER_PUID), 0);
		
        // add modern styling
        ThemeConfiguration theme = new ModernNatTableThemeConfiguration();
        theme.addThemeExtension(new ModernGroupByThemeExtension());
        changeTable.setTheme(theme);
        gridLayer.getColumnHeaderDataLayer().setDefaultRowHeight(40);
        
		Composite compositeBottom = toolkit.createComposite(this, SWT.BORDER);
		compositeBottom.setLayout(new GridLayout(2, false));
		compositeBottom.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Composite compositeRowButton = new Composite(compositeBottom, SWT.NONE);
		toolkit.adapt(compositeRowButton);
		toolkit.paintBordersFor(compositeRowButton);
		compositeRowButton.setLayout(new GridLayout(2, false));

		Button btnAdd = new Button(compositeRowButton, SWT.NONE);
		btnAdd.setImage(SWTResourceManager.getImage(EcoChangeListMgrCompositeTemplate.class, "/icons/plus_16.png"));
		GridData gd_btnAdd = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_btnAdd.widthHint = 25;
		btnAdd.setLayoutData(gd_btnAdd);
		
		Button btnRemove = new Button(compositeRowButton, SWT.NONE);
		btnRemove.setImage(SWTResourceManager.getImage(EcoChangeListMgrCompositeTemplate.class, "/icons/minus_16.png"));
		GridData gd_btnRemove = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_btnRemove.widthHint = 25;
		btnRemove.setLayoutData(gd_btnRemove);

		Composite compositeRightButton = new Composite(compositeBottom, SWT.NONE);
		compositeRightButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1));
		toolkit.adapt(compositeRightButton);
		toolkit.paintBordersFor(compositeRightButton);
		compositeRightButton.setLayout(new GridLayout(4, false));

		Button btnTemplateDown = new Button(compositeRightButton, SWT.NONE);
		toolkit.adapt(btnTemplateDown, true, true);
		btnTemplateDown.setText("Template Download");

		Button btnUpload = new Button(compositeRightButton, SWT.NONE);
		GridData gd_btnUpload = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_btnUpload.widthHint = 50;
		btnUpload.setLayoutData(gd_btnUpload);
		btnUpload.setBounds(0, 0, 76, 25);
		toolkit.adapt(btnUpload, true, true);
		btnUpload.setText("Upload");

		Button btnExport = new Button(compositeRightButton, SWT.NONE);
		btnExport.setBounds(0, 0, 76, 25);
		toolkit.adapt(btnExport, true, true);
		btnExport.setText("Excel Export");

		Button btnSave = new Button(compositeRightButton, SWT.NONE);
		btnSave.setImage(SWTResourceManager.getImage(EcoChangeListMgrCompositeTemplate.class, "/icons/save_16.png"));
		GridData gd_btnSave = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
		gd_btnSave.widthHint = 60;
		btnSave.setLayoutData(gd_btnSave);
		toolkit.adapt(btnSave, true, true);
		btnSave.setText("Save");
	}

	public AbstractRegistryConfiguration setModelEditorConfiguration(final ColumnOverrideLabelAccumulator columnLabelAccumulator, final DataLayer bodyDataLayer) {

		return new AbstractRegistryConfiguration() {
			@Override
			public void configureRegistry(IConfigRegistry configRegistry) {
				columnLabelAccumulator.registerColumnOverrides(0, EcoChangeData.CHECK_BOX_EDITOR_CONFIG_LABEL, EcoChangeData.CHECK_BOX_CONFIG_LABEL);

				configRegistry.registerConfigAttribute(EditConfigAttributes.CELL_EDITABLE_RULE, IEditableRule.ALWAYS_EDITABLE, DisplayMode.EDIT, EcoChangeData.CHECK_BOX_CONFIG_LABEL);
				//Header multi line
				ICellPainter cellPainter = new BeveledBorderDecorator(new TextPainter(true, true, true, true));
				configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER, cellPainter, DisplayMode.NORMAL, GridRegion.COLUMN_HEADER);
				//registerCheckBoxEditor(configRegistry, new CheckBoxPainter(), new CheckBoxCellEditor());
			}
		};
	}

}

package com.kgm.commands.ec.ecostatus.ui;

import java.io.File;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.window.Window;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.AbstractRegistryConfiguration;
import org.eclipse.nebula.widgets.nattable.config.AbstractUiBindingConfiguration;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.config.EditableRule;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.IEditableRule;
import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.data.convert.DefaultBooleanDisplayConverter;
import org.eclipse.nebula.widgets.nattable.edit.EditConfigAttributes;
import org.eclipse.nebula.widgets.nattable.edit.action.ToggleCheckBoxColumnAction;
import org.eclipse.nebula.widgets.nattable.edit.command.UpdateDataCommand;
import org.eclipse.nebula.widgets.nattable.edit.editor.CheckBoxCellEditor;
import org.eclipse.nebula.widgets.nattable.edit.editor.ComboBoxCellEditor;
import org.eclipse.nebula.widgets.nattable.edit.editor.ICellEditor;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.groupBy.ModernGroupByThemeExtension;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.AbstractOverrider;
import org.eclipse.nebula.widgets.nattable.layer.cell.ColumnLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.painter.cell.CheckBoxPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.ColumnHeaderCheckBoxPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.ComboBoxPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.ICellPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.TextPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.decorator.BeveledBorderDecorator;
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
import org.eclipse.nebula.widgets.nattable.ui.menu.IMenuItemProvider;
import org.eclipse.nebula.widgets.nattable.ui.menu.PopupMenuAction;
import org.eclipse.nebula.widgets.nattable.ui.menu.PopupMenuBuilder;
import org.eclipse.nebula.widgets.nattable.ui.util.CellEdgeEnum;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.wb.swt.SWTResourceManager;

import ca.odell.glazedlists.EventList;

import com.kgm.commands.ec.dao.CustomECODao;
import com.kgm.commands.ec.ecostatus.model.EcoChangeData;
import com.kgm.commands.ec.ecostatus.model.EcoOspecCateData;
import com.kgm.commands.ec.ecostatus.operation.EcoChangeListCreateOperation;
import com.kgm.commands.ec.ecostatus.operation.EcoChangeListSaveOperation;
import com.kgm.commands.ec.ecostatus.operation.EcoRptListUploadOperation;
import com.kgm.commands.ec.ecostatus.operation.ExportEcoChangeListOperation;
import com.kgm.commands.ec.ecostatus.operation.SearchEcoChangeListOperation;
import com.kgm.commands.ec.ecostatus.utility.BasicGridEditorGridLayer;
import com.kgm.commands.ec.ecostatus.utility.TableStylingThemeConfiguration;
import com.kgm.commands.ec.search.ECOSearchDialog;
import com.kgm.commands.ec.search.SearchUserDialog;
import com.kgm.common.remote.DataSet;
import com.kgm.common.remote.SYMCRemoteUtil;
import com.kgm.common.utils.CustomUtil;
import com.kgm.common.utils.SYMTcUtil;
import com.teamcenter.rac.aif.InterfaceAIFOperationListener;
import com.teamcenter.rac.kernel.IPropertyName;
import com.teamcenter.rac.kernel.TCComponentGroup;
import com.teamcenter.rac.kernel.TCComponentGroupMember;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentUser;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.ConfirmDialog;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;

/**
 * 설계변경 리스트 UI 및 UI 기능 구현
 * 
 * @author baek
 * 
 */
public class EcoChangeListMgrComposite extends Composite {

	private EventList<EcoChangeData> tableDataList; // 테이블 데이터 리스트
	private NatTable changeTable; // 테이블
	private boolean isInitDataLoad = false; // 초기 Data 로드 되었는지 여부
	private CCombo comboGModel = null; // GMODEL
	private CCombo comboProject = null; // PROJECT
	private CCombo comboVorEType; // V/E 타입
	private CCombo comboEcoPublish = null; // ECO 발행
	private CCombo comboStatus = null; // 상태
	// private CCombo comboOptCategory = null; // Option Category 제거함

	private Text textOspecId; // OSPEC 구분
	private Text textFunctionNo;// Function Name
	private Text textUserName;// 담당자

	private Button btnSave = null; // 저장 버튼
	private Button btnRegister = null; // 변경 관리 리스트 생성 버튼
	private Button btnSearch = null; // 조회 버튼
	private TCSession tcSession = null;
	private Registry registry = null;
	private ArrayList<String> ecoPublishList = null;
	private SelectionLayer selectionLayer = null; // Table Body의 선택되는 Layer

	private ArrayList<EcoChangeData> removeDataList = null; // 제거되는 리스트
	private ArrayList<String> systemCodeList = null;

	private boolean IS_ALL_CHANGE_MODE = false;// 변경 관리 리스트 생성 중인지 여부
	// 변경 관리 리스트 생성중일 경우 Category 별 추가 검토 조건 정보 key: category, 변경검토내용
	private HashMap<ArrayList<String>, ArrayList<EcoOspecCateData>> categoryConditionMap = null;

	/**
	 * 
	 * @param parent
	 * @param allChangeMode
	 *            변경 관리 리스트 생성 모드인지 여부
	 * @param style
	 */
	public EcoChangeListMgrComposite(Composite parent, int style) {
		super(parent, style);
		tcSession = CustomUtil.getTCSession();
		this.registry = Registry.getRegistry(this);
		/**
		 * ECO 발행 리스트 설정
		 */
		setEcoPublishList();
		// System Code 리스트 설정
		setSystemCodes();
		initUI();
	}

	/**
	 * 화면 UI
	 */
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
		GridLayout gl_compositeBasic = new GridLayout(17, false);
		gl_compositeBasic.marginWidth = 10;
		gl_compositeBasic.marginHeight = 10;
		gl_compositeBasic.horizontalSpacing = 8;
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
					// Category 설정
					// setEcoStatusOptCategoryList();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});
		toolkit.adapt(comboGModel);
		toolkit.paintBordersFor(comboGModel);
		GridData gd_comboGModel = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		comboGModel.setLayoutData(gd_comboGModel);
		gd_comboGModel.widthHint = 40;
		comboGModel.setEditable(false);

		Label lblProjectCode = new Label(compositeBasic, SWT.NONE);
		toolkit.adapt(lblProjectCode, true, true);
		lblProjectCode.setText("Project");

		comboProject = new CCombo(compositeBasic, SWT.BORDER);
		comboProject.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// try {
				// setEcoStatusOptCategoryList();
				// } catch (Exception e1) {
				// e1.printStackTrace();
				// }
			}
		});
		GridData gd_comboProject = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_comboProject.widthHint = 75;
		comboProject.setLayoutData(gd_comboProject);
		toolkit.adapt(comboProject);
		toolkit.paintBordersFor(comboProject);
		comboProject.setEditable(false);

		Label lblOspecId = new Label(compositeBasic, SWT.NONE);
		lblOspecId.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		toolkit.adapt(lblOspecId, true, true);
		lblOspecId.setText("구분(O/Spec)");

		textOspecId = new Text(compositeBasic, SWT.BORDER);
		GridData gd_textOspecId = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_textOspecId.widthHint = 80;
		textOspecId.setLayoutData(gd_textOspecId);
		toolkit.adapt(textOspecId, true, true);

		Label lblEcoPublish = new Label(compositeBasic, SWT.NONE);
		toolkit.adapt(lblEcoPublish, true, true);
		lblEcoPublish.setText("ECO 발행");

		comboEcoPublish = new CCombo(compositeBasic, SWT.BORDER);
		GridData gd_comboEcoPublish = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_comboEcoPublish.widthHint = 66;
		comboEcoPublish.setLayoutData(gd_comboEcoPublish);
		toolkit.adapt(comboEcoPublish);
		toolkit.paintBordersFor(comboEcoPublish);
		comboEcoPublish.setEditable(false);

		Label lblStatus = new Label(compositeBasic, SWT.NONE);
		toolkit.adapt(lblStatus, true, true);
		lblStatus.setText("상태");

		comboStatus = new CCombo(compositeBasic, SWT.BORDER);
		GridData gd_comboStatus = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_comboStatus.widthHint = 112;
		comboStatus.setLayoutData(gd_comboStatus);
		toolkit.adapt(comboStatus);
		toolkit.paintBordersFor(comboStatus);
		comboStatus.setEditable(false);

		Label lblVorEType = new Label(compositeBasic, SWT.NONE);
		toolkit.adapt(lblVorEType, true, true);
		lblVorEType.setText("V/E");

		comboVorEType = new CCombo(compositeBasic, SWT.BORDER);
		GridData gd_comboVorCType = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_comboVorCType.widthHint = 40;
		comboVorEType.setLayoutData(gd_comboVorCType);
		toolkit.adapt(comboVorEType);
		toolkit.paintBordersFor(comboVorEType);
		comboVorEType.setEditable(false);

		Label lblFunctionNo = new Label(compositeBasic, SWT.NONE);
		lblFunctionNo.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		toolkit.adapt(lblFunctionNo, true, true);
		lblFunctionNo.setText("Function No");

		textFunctionNo = new Text(compositeBasic, SWT.BORDER);
		GridData gd_textFunctionNo = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_textFunctionNo.widthHint = 77;
		textFunctionNo.setLayoutData(gd_textFunctionNo);
		toolkit.adapt(textFunctionNo, true, true);

		Label lblUserName = new Label(compositeBasic, SWT.NONE);
		lblUserName.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		toolkit.adapt(lblUserName, true, true);
		lblUserName.setText("담당자");

		textUserName = new Text(compositeBasic, SWT.BORDER);
		GridData gd_textUserName = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_textUserName.widthHint = 70;
		textUserName.setLayoutData(gd_textUserName);
		toolkit.adapt(textUserName, true, true);

		// 주석처리
		// Label lblOptCategory = new Label(compositeBasic, SWT.NONE);
		// toolkit.adapt(lblOptCategory, true, true);
		// lblOptCategory.setText("Option Category");
		//
		// comboOptCategory = new CCombo(compositeBasic, SWT.BORDER);
		// GridData gd_comboOptCategory = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		// gd_comboOptCategory.widthHint = 60;
		//
		// comboOptCategory.setLayoutData(gd_comboOptCategory);
		// toolkit.adapt(comboOptCategory);
		// toolkit.paintBordersFor(comboOptCategory);

		Composite compositeTopButton = new Composite(compositeBasic, SWT.NONE);
		compositeTopButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1));
		GridLayout gl_compositeTopButton = new GridLayout(2, false);
		gl_compositeTopButton.marginWidth = 0;
		compositeTopButton.setLayout(gl_compositeTopButton);

		btnRegister = new Button(compositeTopButton, SWT.NONE);
		btnRegister.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		toolkit.adapt(btnRegister, true, true);
		btnRegister.setText("변경 관리 리스트 생성");
		btnRegister.setImage(SWTResourceManager.getImage(EcoChangeListMgrComposite.class, "/com/teamcenter/rac/aif/images/create_16.png"));
		btnRegister.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				registerChangeList();
			}
		});

		btnRegister.setEnabled(IS_ALL_CHANGE_MODE);

		btnSearch = new Button(compositeTopButton, SWT.NONE);
		btnSearch.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				doSearch();
			}
		});
		btnSearch.setImage(SWTResourceManager.getImage(EcoChangeListMgrComposite.class, "/icons/search_16.png"));
		GridData gd_btnSearch = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
		gd_btnSearch.widthHint = 58;
		btnSearch.setLayoutData(gd_btnSearch);
		toolkit.adapt(btnSearch, true, true);
		btnSearch.setText("조회");

		Section sectionResult = toolkit.createSection(this, Section.TITLE_BAR);
		sectionResult.setText("\uC124\uACC4\uBCC0\uACBD \uB9AC\uC2A4\uD2B8");
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

		String[] propertyNames = EcoChangeData.getPropertyNames();
		Map<String, String> propertyToLabelMap = EcoChangeData.getPropertyToLabelMap();

		ConfigRegistry configRegistry = new ConfigRegistry();
		BasicGridEditorGridLayer<EcoChangeData> gridLayer = new BasicGridEditorGridLayer<EcoChangeData>(new ArrayList<EcoChangeData>(), configRegistry,
				propertyNames, propertyToLabelMap);

		DataLayer bodyDataLayer = gridLayer.getBodyLayer().getDataLayer();
		tableDataList = gridLayer.getTableDataList();

		// ColumnOverrideLabelAccumulator columnLabelAccumulator = new ColumnOverrideLabelAccumulator(bodyDataLayer);
		// bodyDataLayer.setConfigLabelAccumulator(columnLabelAccumulator);

		// @SuppressWarnings({ "unchecked", "rawtypes" })
		// CellOverrideLabelAccumulator cellLabelAccumulator = new CellOverrideLabelAccumulator((IRowDataProvider) bodyDataLayer.getDataProvider());
		// cellLabelAccumulator.registerOverride("누락/오류 진행중", EcoChangeData.getColumnIndexOfProperty(EcoChangeData.PROP_NAME_CHANGE_STATUS), EcoChangeData.CELL_LABLEL);
		// bodyDataLayer.setConfigLabelAccumulator(cellLabelAccumulator);

		@SuppressWarnings("unchecked")
		IRowDataProvider<EcoChangeData> bodyDataProvider = (IRowDataProvider<EcoChangeData>) bodyDataLayer.getDataProvider();
		CustomLabelAccumulator columnLabelAccumulator = new CustomLabelAccumulator(bodyDataProvider);
		bodyDataLayer.setConfigLabelAccumulator(columnLabelAccumulator);

		ColumnHeaderCheckBoxPainter columnHeaderCheckBoxPainter = new ColumnHeaderCheckBoxPainter(bodyDataLayer);

		changeTable = new NatTable(compositeResult, gridLayer, false);
		GridData gd_selectMbomTable = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_selectMbomTable.heightHint = 150;

		changeTable.setLayoutData(gd_selectMbomTable);
		changeTable.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		changeTable.setConfigRegistry(configRegistry);
		changeTable.addConfiguration(new DefaultNatTableStyleConfiguration()); // 기본 Style 설정
		changeTable.addConfiguration(new SingleClickSortConfiguration()); // Single Click 시 Sort 설정
		changeTable.addConfiguration(setModelEditorConfiguration(columnLabelAccumulator, bodyDataLayer, columnHeaderCheckBoxPainter));
		changeTable.addConfiguration(new PopupMenuConfiguration(changeTable));
		changeTable.configure();

		// 선택된 Table Layer
		selectionLayer = gridLayer.getBodyLayer().getSelectionLayer();
		bodyDataLayer.setDefaultColumnWidthByPosition(EcoChangeData.getColumnIndexOfProperty(EcoChangeData.PROP_NAME_ROW_CHECK), 35);
		bodyDataLayer.setDefaultColumnWidthByPosition(EcoChangeData.getColumnIndexOfProperty(EcoChangeData.PROP_NAME_GROUP_SEQ_NO), 50);
		bodyDataLayer.setDefaultColumnWidthByPosition(EcoChangeData.getColumnIndexOfProperty(EcoChangeData.PROP_NAME_REGISTER_TYPE), 60);
		bodyDataLayer.setDefaultColumnWidthByPosition(EcoChangeData.getColumnIndexOfProperty(EcoChangeData.PROP_NAME_CREATION_DATE), 80);
		bodyDataLayer.setDefaultColumnWidthByPosition(EcoChangeData.getColumnIndexOfProperty(EcoChangeData.PROP_NAME_ECO_PUBLISH), 70);
		bodyDataLayer.setDefaultColumnWidthByPosition(EcoChangeData.getColumnIndexOfProperty(EcoChangeData.PROP_NAME_CHANGE_STATUS), 110);
		bodyDataLayer.setDefaultColumnWidthByPosition(EcoChangeData.getColumnIndexOfProperty(EcoChangeData.PROP_NAME_ENGINE_FLAG), 30);
		bodyDataLayer.setDefaultColumnWidthByPosition(EcoChangeData.getColumnIndexOfProperty(EcoChangeData.PROP_NAME_FUNCTION_NO), 85);
		bodyDataLayer.setDefaultColumnWidthByPosition(EcoChangeData.getColumnIndexOfProperty(EcoChangeData.PROP_NAME_PART_NAME), 150);
		bodyDataLayer.setDefaultColumnWidthByPosition(EcoChangeData.getColumnIndexOfProperty(EcoChangeData.PROP_NAME_PROJECT_ID), 50);
		bodyDataLayer.setDefaultColumnWidthByPosition(EcoChangeData.getColumnIndexOfProperty(EcoChangeData.PROP_NAME_OSPECT_ID), 120);
		bodyDataLayer.setDefaultColumnWidthByPosition(EcoChangeData.getColumnIndexOfProperty(EcoChangeData.PROP_NAME_CHANGE_DESC), 160);
		bodyDataLayer.setDefaultColumnWidthByPosition(EcoChangeData.getColumnIndexOfProperty(EcoChangeData.PROP_NAME_REVIEW_CONTENTS), 200);
		bodyDataLayer.setDefaultColumnWidthByPosition(EcoChangeData.getColumnIndexOfProperty(EcoChangeData.PROP_NAME_CATEGORY), 60);
		bodyDataLayer.setDefaultColumnWidthByPosition(EcoChangeData.getColumnIndexOfProperty(EcoChangeData.PROP_NAME_SYSTEM_NO), 53);
		bodyDataLayer.setDefaultColumnWidthByPosition(EcoChangeData.getColumnIndexOfProperty(EcoChangeData.PROP_NAME_USER_ID), 100);
		bodyDataLayer.setDefaultColumnWidthByPosition(EcoChangeData.getColumnIndexOfProperty(EcoChangeData.PROP_NAME_TEAM_NAME), 100);
		bodyDataLayer.setDefaultColumnWidthByPosition(EcoChangeData.getColumnIndexOfProperty(EcoChangeData.PROP_NAME_MAIL_STATUS), 80);
		bodyDataLayer.setDefaultColumnWidthByPosition(EcoChangeData.getColumnIndexOfProperty(EcoChangeData.PROP_NAME_ECO_NO), 80);
		bodyDataLayer.setDefaultColumnWidthByPosition(EcoChangeData.getColumnIndexOfProperty(EcoChangeData.PROP_NAME_ECO_COMPLETE_DATE), 80);
		bodyDataLayer.setDefaultColumnWidthByPosition(EcoChangeData.getColumnIndexOfProperty(EcoChangeData.PROP_NAME_DESCRIPTION), 150);
		bodyDataLayer.setDefaultColumnWidthByPosition(EcoChangeData.getColumnIndexOfProperty(EcoChangeData.PROP_NAME_MASTER_PUID), 0);
		bodyDataLayer.setDefaultColumnWidthByPosition(EcoChangeData.getColumnIndexOfProperty(EcoChangeData.PROP_NAME_ROW_DATA_OBJ), 0);
		bodyDataLayer.setDefaultColumnWidthByPosition(EcoChangeData.getColumnIndexOfProperty(EcoChangeData.PROP_NAME_ROW_INIT_DATA_OBJ), 0);
		bodyDataLayer.setDefaultColumnWidthByPosition(EcoChangeData.getColumnIndexOfProperty(EcoChangeData.PROP_NAME_ROW_CHANGE_TYPE), 0);
		bodyDataLayer.setDefaultColumnWidthByPosition(EcoChangeData.getColumnIndexOfProperty(EcoChangeData.PROP_NAME_ROW_CHG_LIST_PUID), 0);

		addCellSectionEvent();
		// add modern styling
		ThemeConfiguration theme = new TableStylingThemeConfiguration();
		theme.addThemeExtension(new ModernGroupByThemeExtension());
		changeTable.setTheme(theme);
		gridLayer.getColumnHeaderDataLayer().setDefaultRowHeight(40);

		Composite compositeBottom = toolkit.createComposite(this, SWT.NONE);
		compositeBottom.setLayout(new GridLayout(2, false));
		GridData gd_compositeBottom = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_compositeBottom.widthHint = 80;
		compositeBottom.setLayoutData(gd_compositeBottom);

		Composite compositeRowButton = new Composite(compositeBottom, SWT.NONE);
		toolkit.adapt(compositeRowButton);
		toolkit.paintBordersFor(compositeRowButton);
		compositeRowButton.setLayout(new GridLayout(2, false));

		Button btnAdd = new Button(compositeRowButton, SWT.NONE);
		btnAdd.setImage(SWTResourceManager.getImage(EcoChangeListMgrComposite.class, "/icons/plus_16.png"));
		GridData gd_btnAdd = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_btnAdd.widthHint = 25;
		btnAdd.setLayoutData(gd_btnAdd);

		btnAdd.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				addRow();
			}
		});

		Button btnRemove = new Button(compositeRowButton, SWT.NONE);
		btnRemove.setImage(SWTResourceManager.getImage(EcoChangeListMgrComposite.class, "/icons/minus_16.png"));
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

		Button btnTemplateDown = new Button(compositeRightButton, SWT.NONE);
		toolkit.adapt(btnTemplateDown, true, true);
		btnTemplateDown.setText("Template Download");
		btnTemplateDown.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				downloadTemplate();
			}
		});
		btnTemplateDown.setImage(SWTResourceManager.getImage(EcoChangeListMgrComposite.class, "/icons/Download_16.png"));

		Button btnUpload = new Button(compositeRightButton, SWT.NONE);
		GridData gd_btnUpload = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_btnUpload.widthHint = 70;
		btnUpload.setLayoutData(gd_btnUpload);
		btnUpload.setBounds(0, 0, 76, 25);
		toolkit.adapt(btnUpload, true, true);
		btnUpload.setText("Upload");
		btnUpload.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				uploadTemplateData();
			}
		});
		btnUpload.setImage(SWTResourceManager.getImage(EcoChangeListMgrComposite.class, "/icons/upload_16.png"));

		Button btnExport = new Button(compositeRightButton, SWT.NONE);
		btnExport.setBounds(0, 0, 76, 25);
		toolkit.adapt(btnExport, true, true);
		btnExport.setText("Excel Export");
		btnExport.setImage(SWTResourceManager.getImage(EcoChangeListMgrComposite.class, "/com/ssangyong/common/images/export_16.png"));
		btnExport.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				excelListExport();
			}

		});

		btnSave = new Button(compositeRightButton, SWT.NONE);
		btnSave.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				doSave();
			}

		});
		btnSave.setImage(SWTResourceManager.getImage(EcoChangeListMgrComposite.class, "/icons/save_16.png"));
		GridData gd_btnSave = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
		gd_btnSave.widthHint = 60;
		btnSave.setLayoutData(gd_btnSave);
		toolkit.adapt(btnSave, true, true);
		btnSave.setText("Save");
	}

	public AbstractRegistryConfiguration setModelEditorConfiguration(final CustomLabelAccumulator columnLabelAccumulator, final DataLayer bodyDataLayer,
			final ColumnHeaderCheckBoxPainter columnHeaderCheckBoxPainter) {

		return new AbstractRegistryConfiguration() {
			@Override
			public void configureRegistry(IConfigRegistry configRegistry) {
				// Header MULTI line 적용
				ICellPainter cellPainter = new BeveledBorderDecorator(new TextPainter(true, true, true, true));
				configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER, cellPainter, DisplayMode.NORMAL, GridRegion.COLUMN_HEADER);

				// 전체선택 Header
				ICellPainter columnHeaderPainter = new CustomLineBorderDecorator(new CellPainterDecorator(new TextPainter(), CellEdgeEnum.BOTTOM,
						columnHeaderCheckBoxPainter));
				configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER, columnHeaderPainter, DisplayMode.NORMAL,
						ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + 0);

				// Column 마다 적용된 Label 설정
				registerConfigLabelsOnColumns(columnLabelAccumulator);

				// 체크박스 Editor 설정
				registerCheckBoxEditor(configRegistry, new CheckBoxPainter(), new CheckBoxCellEditor());

				// Cell Color 설정
				setCellColorStyle(configRegistry);

				// ECO 발행 Combo Box 적용
				ecoPublishComboBoxEditor(configRegistry, new ComboBoxPainter());

				// System Code Combo Box
				systemComboBoxEditor(configRegistry, new ComboBoxPainter());

				// 가운데 정렬
				registerAignCenterCellStyle(configRegistry);

				// 수정여부를 설정하는 Rule 지정
				registerEditableRules(configRegistry, bodyDataLayer.getDataProvider());
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
	 * 초기 Data Load
	 */
	public void loadInitData() {

		try {
			/**
			 * G-MODEL 리스트 설정
			 */
			setGModelList();

			/**
			 * Project No
			 */
			setProjectList();

			// /**
			// * Option Category 리스트
			// */
			// comboOptCategory.removeAll();
			// comboOptCategory.add("ALL");
			// comboOptCategory.select(0);
			// setEcoStatusOptCategoryList();
			/**
			 * V/E 구분
			 */
			comboVorEType.removeAll();
			String[] vOrETypeList = new String[] { "ALL", "V", "E", "" };
			comboVorEType.setItems(vOrETypeList);
			comboVorEType.select(0);

			/**
			 * ECO 발행
			 */
			comboEcoPublish.removeAll();
			comboEcoPublish.add("ALL");
			for (String ecoPublish : ecoPublishList) {
				comboEcoPublish.add(ecoPublish);
				comboEcoPublish.setData(ecoPublish);
			}

			/**
			 * 상태
			 */
			comboStatus.removeAll();
			// String[] statusList = registry.getStringArray("DESC_STATUS.LIST");

			comboStatus.setItems(EcoChangeData.DESC_STATUS);

			EcoChangeListMgrComposite.this.getShell().setDefaultButton(btnSearch);// 조회버튼 Default 버튼
			isInitDataLoad = true;
		} catch (Exception ex) {
			MessageBox.post(this.getShell(), ex.toString(), "Error", MessageBox.ERROR);
		}
	}

	/**
	 * G Model 리스트를 가져옴
	 */
	private ArrayList<String> setGModelList() throws Exception {
		comboGModel.removeAll();
		comboGModel.add("ALL");
		comboGModel.select(0);
		SYMCRemoteUtil remote = new SYMCRemoteUtil();
		DataSet ds = new DataSet();
		ds.put("NO-PARAM", null);
		@SuppressWarnings("unchecked")
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
	 * Option Category 정보를 가져옴
	 * 
	 * @param gmodelNo
	 * @param productNo
	 * @throws Exception
	 */
	// private void setEcoStatusOptCategoryList() throws Exception {
	// String gmodelNo = null, productNo = null;
	//
	// comboOptCategory.removeAll();
	// comboOptCategory.add("ALL");
	// comboOptCategory.select(0);
	//
	// if (comboGModel.getSelectionIndex() > 0)
	// gmodelNo = "".equals(comboGModel.getText()) ? null : comboGModel.getText();
	//
	// if (comboProject.getSelectionIndex() > 0) {
	// productNo = "".equals(comboProject.getText()) ? null : comboProject.getText();
	// gmodelNo = null;
	// }
	//
	// if (gmodelNo == null && productNo == null)
	// return;
	//
	// CustomECODao dao = new CustomECODao();
	// ArrayList<HashMap<String, String>> optCategoryList = dao.getEcoStatusOptCategoryList(gmodelNo, productNo);
	// for (HashMap<String, String> optCategoryMap : optCategoryList) {
	// String category = optCategoryMap.get("OPTION_CATEGORY");
	// comboOptCategory.add(category);
	// comboOptCategory.setData(category);
	// }
	//
	// }

	/**
	 * 검색 조건 초거 Data 가 이미 설정되었는지 체크함
	 * 
	 * @return the isInitDataLoad
	 */
	public boolean isInitDataLoad() {
		return isInitDataLoad;
	}

	/**
	 * 변경리스트 조회
	 */
	private void doSearch() {
		tableDataList.clear();
		if (removeDataList != null)
			removeDataList.clear();

		String gmodel = "".equals(comboGModel.getText()) || "ALL".equals(comboGModel.getText()) ? null : comboGModel.getText();
		// String category = "".equals(comboOptCategory.getText()) || "ALL".equals(comboOptCategory.getText()) ? null : comboOptCategory.getText();
		String projectNo = "".equals(comboProject.getText()) || "ALL".equals(comboProject.getText()) ? null : comboProject.getText();
		String ospecId = "".equals(textOspecId.getText()) ? null : textOspecId.getText().replace("*", "%");
		String ecoPublish = "".equals(comboEcoPublish.getText()) || "ALL".equals(comboEcoPublish.getText()) ? null : comboEcoPublish.getText();
		String status = "".equals(comboStatus.getText()) || "ALL".equals(comboStatus.getText()) ? null : comboStatus.getText();

		String vOrEType = null;
		if ("ALL".equals(comboVorEType.getText()))
			vOrEType = null;
		else if ("".equals(comboVorEType.getText()))
			vOrEType = "N";
		else
			vOrEType = comboVorEType.getText();

		String functionNo = "".equals(textFunctionNo.getText()) ? null : textFunctionNo.getText().replace("*", "%");
		String userName = "".equals(textUserName.getText()) ? null : textUserName.getText().replace("*", "%");

		EcoChangeData data = new EcoChangeData();
		data.setgModel(gmodel);
		// data.setCategory(category);
		data.setProjectId(projectNo);
		data.setOspecId(ospecId);
		data.setvOrEType(vOrEType);
		data.setFunctionNo(functionNo);
		data.setUserName(userName);
		data.setEcoPublish(ecoPublish);
		data.setChangeStatus(status);
		data.setSearchEcoChangeList(tableDataList);

		SearchEcoChangeListOperation op = new SearchEcoChangeListOperation(data);
		tcSession.queueOperation(op);
		// 변경 관리 리스트 생성 모드 해제
		setAllChangeMode(false);
	}

	/**
	 * Template 다운 로드
	 */
	private void downloadTemplate() {

		try {
			FileDialog fDialog = new FileDialog(this.getShell(), SWT.SINGLE | SWT.SAVE);
			fDialog.setFilterNames(new String[] { "Excel File" });
			fDialog.setFileName(EcoChangeData.TEMPLATE_DS_ECO_RPT);
			// *.xls, *.xlsx Filter 설정
			fDialog.setFilterExtensions(new String[] { "*.xlsx" });
			String strRet = fDialog.open();

			if ((strRet == null) || (strRet.equals("")))
				return;

			String strfileName = fDialog.getFileName();
			if ((strfileName == null) || (strfileName.equals("")))
				return;

			String strDownLoadFilePath = fDialog.getFilterPath() + File.separatorChar + strfileName;

			File checkFile = new File(strDownLoadFilePath);
			if (checkFile.exists()) {
				int retValue = ConfirmDialog.prompt(getShell(), "Confirm", strDownLoadFilePath + " File already exists.\nDo you want to overwrite it?");
				if (retValue != IDialogConstants.YES_ID)
					return;
			}

			File tempFile = SYMTcUtil.getTemplateFile(tcSession, EcoChangeData.TEMPLATE_DS_ECO_RPT, null);
			if (checkFile.exists())
				checkFile.delete();

			tempFile.renameTo(new File(strDownLoadFilePath));
			MessageBox.post(getShell(), strDownLoadFilePath + " file download completed.", "Notification", 2);
		} catch (Exception ex) {
			MessageBox.post(this.getShell(), ex.toString(), "Error", MessageBox.ERROR);
		}
	}

	/**
	 * Template 에 작성된 Data 를 Upload함
	 */
	private void uploadTemplateData() {
		String templateFilePath = selectUploadFile();
		if (templateFilePath == null || templateFilePath.isEmpty()) {
			MessageBox.post(this.getShell(), " The file to upload is not selected.", "WARNING", MessageBox.WARNING);
			return;
		}
		tableDataList.clear();
		EcoChangeData inputData = new EcoChangeData();
		inputData.setSearchEcoChangeList(tableDataList);

		EcoRptListUploadOperation uploadOp = new EcoRptListUploadOperation(templateFilePath, inputData);
		tcSession.queueOperation(uploadOp);
		// 변경 관리 리스트 생성 모드 해제
		setAllChangeMode(false);
	}

	/**
	 * 파일 선택
	 * 
	 * @return
	 */
	private String selectUploadFile() {
		FileDialog fDialog = new FileDialog(this.getShell(), SWT.SINGLE);
		fDialog.setFilterNames(new String[] { "Excel File" });
		fDialog.setFilterExtensions(new String[] { "*.xls*" });
		String selectFile = fDialog.open();
		if (selectFile == null)
			return null;
		String selectFileName = fDialog.getFileName();
		if ((selectFileName == null) || (selectFileName.equals("")))
			return null;

		String filterPath = fDialog.getFilterPath();
		return filterPath + File.separatorChar + selectFileName;
		// textSelectExcelPath.setText(filterPath + File.separatorChar + selectFileName);
	}

	/**
	 * Row 추가
	 */
	private void addRow() {
		PositionCoordinate[] selectCellPos = selectionLayer.getSelectedCellPositions();
		if (selectCellPos.length == 0)
			return;
		try {
			int rowIndex = selectionLayer.getSelectedCellPositions()[0].rowPosition;
			Object rowDataObj = selectionLayer.getDataValueByPosition(EcoChangeData.getColumnIndexOfProperty(EcoChangeData.PROP_NAME_ROW_DATA_OBJ), rowIndex);

			if (rowDataObj == null)
				return;
			EcoChangeData rowData = (EcoChangeData) rowDataObj;

			String registerId = rowData.getRegisterId();
			if (!isRowEditableUser(registerId)) {
				MessageBox.post(this.getShell(), "현재 사용자는 " + rowData.getProjectId() + "," + rowData.getOspecId() + "을(를) 등록한 사용자가 아니어서 수정권한이 없습니다.",
						"WARNING", MessageBox.WARNING);
				return;
			}

			EcoChangeData newData = (EcoChangeData) rowData.clone();
			SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd");
			Date toDate = new Date();
			String creationDate = sd.format(toDate);
			newData.setRowCheck(false);
			newData.setGroupSeqNo(EcoChangeData.ROW_CHANGE_TYPE_NEW);
			newData.setRegisterType("시스템");
			newData.setCreationDate(creationDate);
			newData.setEcoPublish("");
			newData.setChangeStatus(null);
			newData.setEngineFlag(null);
			newData.setFunctionNo(null);
			newData.setPartName(null);
			newData.setReviewContents(null);
			newData.setSystemNo(null);
			newData.setUserId(null);
			newData.setTeamName(null);
			newData.setMailStatus(null);
			newData.setEcoNo(null);
			newData.setEcoCompleteDate(null);
			newData.setDescription(null);
			newData.setOpCategoryPuid(null);
			newData.setRowChangeType(EcoChangeData.ROW_CHANGE_TYPE_NEW);
			newData.setRowDataObj(newData);
			EcoChangeData rowInitDataObj = (EcoChangeData) newData.clone();// 초기 데이터
			newData.setRowInitDataObj(rowInitDataObj);

			tableDataList.add(rowIndex + 1, newData);
			changeTable.refresh();

		} catch (Exception ex) {
			MessageBox.post(EcoChangeListMgrComposite.this.getShell(), ex.toString(), "Error", MessageBox.ERROR);
		}

	}

	/**
	 * Row 삭제
	 */
	private void removeRow() {
		if (removeDataList == null)
			removeDataList = new ArrayList<EcoChangeData>();
		try {

			StringBuilder sbMsg = new StringBuilder();
			for (EcoChangeData rowData : tableDataList) {
				boolean isChecked = rowData.isRowCheck();
				String registerId = rowData.getRegisterId();
				if (!isChecked)
					continue;
				if (!isRowEditableUser(registerId)) {
					String projectId = rowData.getProjectId();
					String ospecId = rowData.getOspecId();
					String functionNo = rowData.getFunctionNo();
					String partNo = rowData.getPartName();
					sbMsg.append(projectId + ", " + ospecId + ", " + functionNo + ", " + partNo + "\n");
					continue;
				}
				rowData.setRowChangeType(EcoChangeData.ROW_CHANGE_TYPE_REMOVE);
				if (!removeDataList.contains(rowData))
					removeDataList.add(rowData);
			}

			if (sbMsg.length() > 0) {
				String msg = sbMsg.substring(0, sbMsg.lastIndexOf("\n"));
				MessageBox.post(EcoChangeListMgrComposite.this.getShell(), "수정권한이 없는 행은 제거되지 않았습니다.", msg, null, false, "WARNING", MessageBox.WARNING, false,
						null);
			}

			for (EcoChangeData rowData : removeDataList) {
				tableDataList.remove(rowData);
			}

			changeTable.refresh();
		} catch (Exception ex) {
			MessageBox.post(EcoChangeListMgrComposite.this.getShell(), ex.toString(), "Error", MessageBox.ERROR);
		}
	}

	/**
	 * 
	 * @param event
	 */
	private void doSave() {

		String gmodel = "".equals(comboGModel.getText()) || "ALL".equals(comboGModel.getText()) ? null : comboGModel.getText();
		// String category = "".equals(comboOptCategory.getText()) || "ALL".equals(comboOptCategory.getText()) ? null : comboOptCategory.getText();
		String projectNo = "".equals(comboProject.getText()) || "ALL".equals(comboProject.getText()) ? null : comboProject.getText();
		String ospecId = "".equals(textOspecId.getText()) ? null : textOspecId.getText().replace("*", "%");
		String ecoPublish = "".equals(comboEcoPublish.getText()) || "ALL".equals(comboEcoPublish.getText()) ? null : comboEcoPublish.getText();
		String status = "".equals(comboStatus.getText()) || "ALL".equals(comboStatus.getText()) ? null : comboStatus.getText();

		String vOrEType = null;
		if ("ALL".equals(comboVorEType.getText()))
			vOrEType = null;
		else if ("".equals(comboVorEType.getText()))
			vOrEType = "N";
		else
			vOrEType = comboVorEType.getText();

		String functionNo = "".equals(textFunctionNo.getText()) ? null : textFunctionNo.getText().replace("*", "%");
		String userName = "".equals(textUserName.getText()) ? null : textUserName.getText().replace("*", "%");

		/**
		 * 삭제 수행
		 */
		ArrayList<EcoChangeData> deleteDataList = new ArrayList<EcoChangeData>(); // 삭제되는 Data 리스트
		if (removeDataList != null) {
			for (EcoChangeData rowData : removeDataList) {
				EcoChangeData initRowData = rowData.getRowInitDataObj();
				String initRowChangeType = initRowData.getRowChangeType();
				if (initRowChangeType.equals(EcoChangeData.ROW_CHANGE_TYPE_NEW))
					continue;
				deleteDataList.add(rowData);
			}
			removeDataList.clear();
			removeDataList = null;
		}

		EcoChangeData saveData = new EcoChangeData();
		saveData.setSearchEcoChangeList(tableDataList);
		saveData.setDeleteDataList(deleteDataList);
		saveData.setgModel(gmodel);
		// saveData.setCategory(category);
		saveData.setProjectId(projectNo);
		saveData.setOspecId(ospecId);
		saveData.setvOrEType(vOrEType);
		saveData.setFunctionNo(functionNo);
		saveData.setUserName(userName);
		saveData.setEcoPublish(ecoPublish);
		saveData.setChangeStatus(status);

		EcoChangeListSaveOperation saveOp = new EcoChangeListSaveOperation(saveData);
		tcSession.queueOperation(saveOp);

	}

	/**
	 * 변경관리리스트 생성
	 */
	private void registerChangeList() {
		/**
		 * 삭제항목들 초기화
		 */
		if (removeDataList != null) {
			removeDataList.clear();
			removeDataList = null;
		}

		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				String mainMasterPuid = null;
				// Master PUID 설정
				for (EcoChangeData rowData : tableDataList) {
					String masterPuid = rowData.getMasterPuid();
					if (masterPuid != null) {
						mainMasterPuid = masterPuid;
						break;
					}
				}
				try {
					/**
					 * 이 생성된 Data 가 존재하는 체크
					 */
					CustomECODao dao = new CustomECODao();
					int isCreatedCount = dao.getRptReviewCount(mainMasterPuid);
					if (isCreatedCount > 0) {
						ConfirmDialog confirmDialog = new ConfirmDialog(EcoChangeListMgrComposite.this.getShell(), "확인",
								"이미 생성된 Data가 존재합니다. 입력된 모든값이 다 사라집니다.", true);
						confirmDialog.open();
						if (confirmDialog.isOkayClicked()) {
							confirmDialog = new ConfirmDialog(EcoChangeListMgrComposite.this.getShell(), "확인", "재생성하시겠습니까?", true);
							confirmDialog.open();
							if (confirmDialog.isOkayClicked()) {
								/**
								 * 변경관리 리스트 삭제
								 */
								dao = new CustomECODao();
								dao.deleteRptReviewList(mainMasterPuid);
							} else
								return;
						} else
							return;
					}
				} catch (Exception ex) {
					MessageBox.post(EcoChangeListMgrComposite.this.getShell(), ex.toString(), "Error", MessageBox.ERROR);
				}

				EcoChangeData saveData = new EcoChangeData();
				saveData.setSearchEcoChangeList(tableDataList);
				final EcoChangeListCreateOperation op = new EcoChangeListCreateOperation(saveData, categoryConditionMap, mainMasterPuid);
				tcSession.queueOperation(op);
				op.addOperationListener(new InterfaceAIFOperationListener() {

					@Override
					public void startOperation(String arg0) {
					}

					@Override
					public void endOperation() {
						Display.getDefault().syncExec(new Runnable() {

							@Override
							public void run() {
								// 성공적으로 생성이 되면 변경 관리 리스트 생성 모드 해제
								if (!op.isCreationSucces())
									return;

								setAllChangeMode(false);
							}
						});
					}
				});

			}
		});

	}

	/**
	 * Cell 값 변경 Event 처리
	 */
	private void addCellSectionEvent() {
		changeTable.addMouseListener(new MouseListener() {

			@Override
			public void mouseUp(MouseEvent paramMouseEvent) {
			}

			@Override
			public void mouseDown(MouseEvent paramMouseEvent) {
			}

			@Override
			public void mouseDoubleClick(MouseEvent paramMouseEvent) {
				PositionCoordinate[] selectCellPos = selectionLayer.getSelectedCellPositions();
				if (selectCellPos.length == 0)
					return;

				int rowIndex = selectionLayer.getSelectedCellPositions()[0].rowPosition;
				int columnIndex = selectionLayer.getSelectedCellPositions()[0].columnPosition;

				/**
				 * FIXME: Part Name 검색 주석
				 */
				// Object rowChangeType = selectionLayer.getDataValueByPosition(EcoChangeData.getColumnIndexOfProperty(EcoChangeData.PROP_NAME_ROW_CHANGE_TYPE),
				// rowIndex);
				// if (columnIndex == EcoChangeData.getColumnIndexOfProperty(EcoChangeData.PROP_NAME_PART_NAME)) {
				//
				// if (rowChangeType == null || !rowChangeType.equals(EcoChangeData.ROW_CHANGE_TYPE_NEW))
				// return;
				//
				// HashMap<String, Object> attrMap = new HashMap<String, Object>();
				// PartNameDialog itemDialog = new PartNameDialog(getShell(), SWT.SINGLE, attrMap, false);
				// itemDialog.open();
				// Object partNameObj = attrMap.get("object_name");
				// if (attrMap.get("object_name") == null)
				// return;
				//
				// selectionLayer.doCommand(new UpdateDataCommand(selectionLayer, EcoChangeData.getColumnIndexOfProperty(EcoChangeData.PROP_NAME_PART_NAME),
				// rowIndex, (String) partNameObj));
				//
				// } else
				//
				/**
				 * 담당자 검색
				 */
				if (columnIndex == EcoChangeData.getColumnIndexOfProperty(EcoChangeData.PROP_NAME_USER_ID)) {
					Object rowDataObj = selectionLayer.getDataValueByPosition(EcoChangeData.getColumnIndexOfProperty(EcoChangeData.PROP_NAME_ROW_DATA_OBJ),
							rowIndex);
					if (rowDataObj == null)
						return;
					EcoChangeData rowData = (EcoChangeData) rowDataObj;
					String registerId = rowData.getRegisterId();
					if (!isRowEditableUser(registerId)) {
						MessageBox.post(EcoChangeListMgrComposite.this.getShell(), "현재 사용자는 " + rowData.getProjectId() + "," + rowData.getOspecId()
								+ "을(를) 등록한 사용자가 아니어서 수정권한이 없습니다.", "WARNING", MessageBox.WARNING);
						return;
					}

					SearchUserDialog dialog = new SearchUserDialog(getShell());
					int iResult = dialog.open();
					if (iResult != Window.OK)
						return;
					TCComponentGroupMember userGroupMember = dialog.getSelectedMember();
					try {
						Object userInformObj = selectionLayer.getDataValueByPosition(EcoChangeData.getColumnIndexOfProperty(EcoChangeData.PROP_NAME_USER_ID),
								rowIndex);

						TCComponentUser userComp = userGroupMember.getUser();
						TCComponentGroup userGroup = userGroupMember.getGroup();
						String userId = userComp.getUserId();
						String userName = userComp.getProperty(IPropertyName.USER_NAME);
						String userInform = userName + "(" + userId + ")";
						String groupName = userGroup.getGroupName();

						if (userInformObj != null && userInform.equals((String) userInformObj))
							return;

						selectionLayer.doCommand(new UpdateDataCommand(selectionLayer, EcoChangeData.getColumnIndexOfProperty(EcoChangeData.PROP_NAME_USER_ID),
								rowIndex, userInform));
						selectionLayer.doCommand(new UpdateDataCommand(selectionLayer, EcoChangeData
								.getColumnIndexOfProperty(EcoChangeData.PROP_NAME_TEAM_NAME), rowIndex, groupName));

						selectionLayer.doCommand(new UpdateDataCommand(selectionLayer, EcoChangeData
								.getColumnIndexOfProperty(EcoChangeData.PROP_NAME_ENGINE_FLAG), rowIndex, groupName));

						// if (!rowChangeType.equals(EcoChangeData.ROW_CHANGE_TYPE_NEW))
						// selectionLayer.doCommand(new UpdateDataCommand(selectionLayer, EcoChangeData
						// .getColumnIndexOfProperty(EcoChangeData.PROP_NAME_ROW_CHANGE_TYPE), rowIndex, EcoChangeData.ROW_CHANGE_TYPE_MODIFY));

					} catch (TCException e) {
						e.printStackTrace();
					}

					/**
					 * ECO 검색 추가
					 */
				} else if (columnIndex == EcoChangeData.getColumnIndexOfProperty(EcoChangeData.PROP_NAME_ECO_NO)) {

					Object rowDataObj = selectionLayer.getDataValueByPosition(EcoChangeData.getColumnIndexOfProperty(EcoChangeData.PROP_NAME_ROW_DATA_OBJ),
							rowIndex);
					if (rowDataObj == null)
						return;
					EcoChangeData rowData = (EcoChangeData) rowDataObj;
					String registerId = rowData.getRegisterId();
					if (!isRowEditableUser(registerId)) {
						MessageBox.post(EcoChangeListMgrComposite.this.getShell(), "현재 사용자는 " + rowData.getProjectId() + "," + rowData.getOspecId()
								+ "을(를) 등록한 사용자가 아니어서 수정권한이 없습니다.", "WARNING", MessageBox.WARNING);
						return;
					}

					ECOSearchDialog ecoSearchDialog = new ECOSearchDialog(getShell(), SWT.SINGLE, false, true);
					ecoSearchDialog.setAllMaturityButtonsEnabled(false);
					ecoSearchDialog.setBInProcessSelect(false);
					ecoSearchDialog.setBWorkingSelect(false);
					ecoSearchDialog.clearOwningUser();
					ecoSearchDialog.open();

					TCComponentItemRevision[] selectedEcoRevList = ecoSearchDialog.getSelectctedECO();
					if (selectedEcoRevList == null || selectedEcoRevList.length == 0)
						return;

					TCComponentItemRevision selectedEcoRevision = selectedEcoRevList[0];

					try {
						SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd");
						String ecoId = selectedEcoRevision.getProperty(IPropertyName.ITEM_ID);
						Date releasedDate = selectedEcoRevision.getDateProperty("date_released");
						String releasedDateStr = sd.format(releasedDate);

						// 주석처리. 같은 경우도 담당자가 입력안될 경우가 있으므로
						// Object ecoNoObj = selectionLayer.getDataValueByPosition(EcoChangeData.getColumnIndexOfProperty(EcoChangeData.PROP_NAME_ECO_NO),
						// rowIndex);
						// if (ecoNoObj != null && ecoId.equals((String) ecoNoObj))
						// return;

						String owingUser = selectedEcoRevision.getProperty(IPropertyName.OWNING_USER);
						String groupName = selectedEcoRevision.getProperty(IPropertyName.OWNING_GROUP);
						String engineType = EcoChangeData.getEngineType(groupName);

						selectionLayer.doCommand(new UpdateDataCommand(selectionLayer, EcoChangeData.getColumnIndexOfProperty(EcoChangeData.PROP_NAME_ECO_NO),
								rowIndex, (String) ecoId));
						selectionLayer.doCommand(new UpdateDataCommand(selectionLayer, EcoChangeData
								.getColumnIndexOfProperty(EcoChangeData.PROP_NAME_ECO_COMPLETE_DATE), rowIndex, (String) releasedDateStr));
						// Eingine 타입변경
						selectionLayer.doCommand(new UpdateDataCommand(selectionLayer, EcoChangeData
								.getColumnIndexOfProperty(EcoChangeData.PROP_NAME_ENGINE_FLAG), rowIndex, engineType));
						// 담당자 변경
						selectionLayer.doCommand(new UpdateDataCommand(selectionLayer, EcoChangeData.getColumnIndexOfProperty(EcoChangeData.PROP_NAME_USER_ID),
								rowIndex, owingUser));
						selectionLayer.doCommand(new UpdateDataCommand(selectionLayer, EcoChangeData
								.getColumnIndexOfProperty(EcoChangeData.PROP_NAME_TEAM_NAME), rowIndex, groupName));

						// if (!rowChangeType.equals(EcoChangeData.ROW_CHANGE_TYPE_NEW))
						// selectionLayer.doCommand(new UpdateDataCommand(selectionLayer, EcoChangeData
						// .getColumnIndexOfProperty(EcoChangeData.PROP_NAME_ROW_CHANGE_TYPE), rowIndex, EcoChangeData.ROW_CHANGE_TYPE_MODIFY));

					} catch (Exception e) {
						e.printStackTrace();
					}

				}

			}
		});
	}

	/**
	 * Table Row Cell 에 Label 을 부여하여 줘서 Cell 의 Foreground Color 를 설정하도록 함
	 * 
	 * @author baek
	 * 
	 */
	class CustomLabelAccumulator extends AbstractOverrider {

		private IRowDataProvider<EcoChangeData> bodyDataProvider;

		CustomLabelAccumulator(IRowDataProvider<EcoChangeData> bodyDataProvider) {
			this.bodyDataProvider = bodyDataProvider;
		}

		@Override
		public void accumulateConfigLabels(LabelStack configLabels, int columnPosition, int rowPosition) {
			EcoChangeData rowObject = this.bodyDataProvider.getRowObject(rowPosition);
			if (rowObject == null)
				return;
			EcoChangeData initRowData = rowObject.getRowInitDataObj(); // 초기 Data

			// 변경 타입
			String rowChangeType = EcoChangeData.objToStr(rowObject.getRowChangeType());

			// 상태
			if (columnPosition == EcoChangeData.getColumnIndexOfProperty(EcoChangeData.PROP_NAME_CHANGE_STATUS)) {
				// 상태
				String status = EcoChangeData.objToStr(rowObject.getChangeStatus());
				if (status.startsWith("누락/오류") || status.startsWith("지연"))
					configLabels.addLabel(EcoChangeData.CELL_FRG_RED_COLOR_LABLEL);
				else
					addOverrides(configLabels, Integer.valueOf(columnPosition));
				/**
				 * ECO 발행: 수정시 색상 및 상태 변경
				 */
			} else if (columnPosition == EcoChangeData.getColumnIndexOfProperty(EcoChangeData.PROP_NAME_ECO_PUBLISH)) {
				String ecoPublish = EcoChangeData.objToStr(rowObject.getEcoPublish());
				String ecoIntPublish = EcoChangeData.objToStr(initRowData.getEcoPublish());
				if (!rowChangeType.equals(EcoChangeData.ROW_CHANGE_TYPE_NEW) && !ecoPublish.equals(ecoIntPublish)) {
					configLabels.addLabel(EcoChangeData.CELL_BG_GREEN_COLOR_LABLEL);
					configLabels.addLabel(EcoChangeData.EDITABLE_CONFIG_LABEL);
					configLabels.addLabel(EcoChangeData.COMBO_ECO_PUBLISH_CONFIG_LABEL);
					// 변경관리 리스트 생성일 경우는 상태변경Pass
					if (rowChangeType.equals(EcoChangeData.ROW_CHANGE_TYPE_ALL_NEW))
						return;
					// 수정시 상태를 변경함
					rowObject.setRowChangeType(EcoChangeData.ROW_CHANGE_TYPE_MODIFY);
				} else
					addOverrides(configLabels, Integer.valueOf(columnPosition));
				/**
				 * 순번
				 */
			} else if (columnPosition == EcoChangeData.getColumnIndexOfProperty(EcoChangeData.PROP_NAME_GROUP_SEQ_NO)) {
				// 추가될 때 색깔적용하는 Label 추가
				if (rowChangeType.equals(EcoChangeData.ROW_CHANGE_TYPE_NEW))
					configLabels.addLabel(EcoChangeData.CELL_BG_GREEN_COLOR_LABLEL);
				else
					addOverrides(configLabels, Integer.valueOf(columnPosition));
				/**
				 * 담당자 ID: 수정시 색상 및 상태 변경
				 */
			} else if (columnPosition == EcoChangeData.getColumnIndexOfProperty(EcoChangeData.PROP_NAME_USER_ID)) {
				String currentUserId = rowObject.getUserId();
				String initUserId = initRowData.getUserId();
				if (!rowChangeType.equals(EcoChangeData.ROW_CHANGE_TYPE_NEW)
						&& !EcoChangeData.objToStr(currentUserId).equals(EcoChangeData.objToStr(initUserId))) {
					configLabels.addLabel(EcoChangeData.CELL_BG_GREEN_COLOR_LABLEL);
					// 변경관리 리스트 생성일 경우는 상태변경Pass
					if (rowChangeType.equals(EcoChangeData.ROW_CHANGE_TYPE_ALL_NEW))
						return;
					// 수정시 상태를 변경함
					rowObject.setRowChangeType(EcoChangeData.ROW_CHANGE_TYPE_MODIFY);
				} else
					addOverrides(configLabels, Integer.valueOf(columnPosition));
				/**
				 * ECO NO : 수정시 색상 및 상태 변경
				 */
			} else if (columnPosition == EcoChangeData.getColumnIndexOfProperty(EcoChangeData.PROP_NAME_ECO_NO)) {
				String currentEcoNo = rowObject.getEcoNo();
				String initEcoNo = initRowData.getEcoNo();
				if (!rowChangeType.equals(EcoChangeData.ROW_CHANGE_TYPE_NEW) && !EcoChangeData.objToStr(currentEcoNo).equals(EcoChangeData.objToStr(initEcoNo))) {
					configLabels.addLabel(EcoChangeData.CELL_BG_GREEN_COLOR_LABLEL);
					// 변경관리 리스트 생성일 경우는 상태변경Pass
					if (rowChangeType.equals(EcoChangeData.ROW_CHANGE_TYPE_ALL_NEW))
						return;
					rowObject.setRowChangeType(EcoChangeData.ROW_CHANGE_TYPE_MODIFY);
				} else
					addOverrides(configLabels, Integer.valueOf(columnPosition));
				/**
				 * 비고 : 수정시 색상 및 상태 변경
				 */
			} else if (columnPosition == EcoChangeData.getColumnIndexOfProperty(EcoChangeData.PROP_NAME_DESCRIPTION)) {
				String currentDesc = rowObject.getDescription();
				String initDesc = initRowData.getDescription();
				if (!rowChangeType.equals(EcoChangeData.ROW_CHANGE_TYPE_NEW) && !EcoChangeData.objToStr(currentDesc).equals(EcoChangeData.objToStr(initDesc))) {
					configLabels.addLabel(EcoChangeData.CELL_BG_GREEN_COLOR_LABLEL);
					configLabels.addLabel(EcoChangeData.EDITABLE_CONFIG_LABEL);
					// 변경관리 리스트 생성일 경우는 상태변경Pass
					if (rowChangeType.equals(EcoChangeData.ROW_CHANGE_TYPE_ALL_NEW))
						return;
					rowObject.setRowChangeType(EcoChangeData.ROW_CHANGE_TYPE_MODIFY);
				} else
					addOverrides(configLabels, Integer.valueOf(columnPosition));
				// } else if (columnPosition == EcoChangeData.getColumnIndexOfProperty(EcoChangeData.PROP_NAME_REVIEW_CONTENTS)) {
				// // 변경관리 리스트 생성 모드에서 Category 가 없고 수정가능토록 함
				// String category = rowObject.getCategory();
				// if (IS_ALL_CHANGE_MODE && category == null) {
				// configLabels.addLabel(EcoChangeData.EDITABLE_CONFIG_LABEL);
				// } else
				addOverrides(configLabels, Integer.valueOf(columnPosition));
			} else {
				addOverrides(configLabels, Integer.valueOf(columnPosition));
			}

		}

		private void addOverrides(LabelStack configLabels, Serializable key) {
			List<String> overrides = getOverrides(key);
			if (overrides != null) {
				for (String configLabel : overrides) {
					configLabels.addLabel(configLabel);
				}
			}
		}

		public void registerColumnOverrides(int columnIndex, String... configLabels) {
			super.registerOverrides(columnIndex, configLabels);
		}

	}

	/**
	 * ECO 발행 선택 값리스트
	 */
	private void setEcoPublishList() {
		ecoPublishList = new ArrayList<String>();
		String[] ecoPublishArray = registry.getStringArray("ECOPUBLISH.LIST");
		ecoPublishList = new ArrayList<String>(Arrays.asList(ecoPublishArray));
	}

	/**
	 * System Code 리스트 설정
	 */
	private void setSystemCodes() {
		try {
			String[] systemCodes = CustomUtil.getLOVDisplayValues(tcSession, "s7_SYSTEM_CODE");
			systemCodeList = new ArrayList<String>(Arrays.asList(systemCodes));
		} catch (TCException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Column 마다 적용되는 Label 을 등록
	 * 
	 * @param columnLabelAccumulator
	 */
	private void registerConfigLabelsOnColumns(CustomLabelAccumulator columnLabelAccumulator) {

		// 체크박스
		columnLabelAccumulator.registerColumnOverrides(EcoChangeData.getColumnIndexOfProperty(EcoChangeData.PROP_NAME_ROW_CHECK),
				EcoChangeData.CHECK_BOX_EDITOR_CONFIG_LABEL, EcoChangeData.CHECK_BOX_CONFIG_LABEL, EcoChangeData.ALIGN_CELL_CONTENTS_CENTER_CONFIG_LABEL);

		// 순번
		columnLabelAccumulator.registerColumnOverrides(EcoChangeData.getColumnIndexOfProperty(EcoChangeData.PROP_NAME_GROUP_SEQ_NO),
				EcoChangeData.ALIGN_CELL_CONTENTS_CENTER_CONFIG_LABEL);

		// ECO 발행
		columnLabelAccumulator.registerColumnOverrides(EcoChangeData.getColumnIndexOfProperty(EcoChangeData.PROP_NAME_ECO_PUBLISH),
				EcoChangeData.COMBO_ECO_PUBLISH_CONFIG_LABEL, EcoChangeData.CELL_USER_RIGHT_EDITABLE_RULE_LABEL);
		// Category
		columnLabelAccumulator.registerColumnOverrides(EcoChangeData.getColumnIndexOfProperty(EcoChangeData.PROP_NAME_CATEGORY),
				EcoChangeData.CELL_EDITABLE_RULE_APPLY_LABLEL);
		// Function No
		columnLabelAccumulator.registerColumnOverrides(EcoChangeData.getColumnIndexOfProperty(EcoChangeData.PROP_NAME_FUNCTION_NO),
				EcoChangeData.CELL_EDITABLE_RULE_APPLY_LABLEL);
		// Part No
		columnLabelAccumulator.registerColumnOverrides(EcoChangeData.getColumnIndexOfProperty(EcoChangeData.PROP_NAME_PART_NAME),
				EcoChangeData.CELL_EDITABLE_RULE_APPLY_LABLEL);
		// 변경 검토 내용
		columnLabelAccumulator.registerColumnOverrides(EcoChangeData.getColumnIndexOfProperty(EcoChangeData.PROP_NAME_REVIEW_CONTENTS),
				EcoChangeData.CELL_EDITABLE_RULE_APPLY_LABLEL);
		// 비고
		columnLabelAccumulator.registerColumnOverrides(EcoChangeData.getColumnIndexOfProperty(EcoChangeData.PROP_NAME_DESCRIPTION),
				EcoChangeData.CELL_USER_RIGHT_EDITABLE_RULE_LABEL);

		// System Code
		columnLabelAccumulator.registerColumnOverrides(EcoChangeData.getColumnIndexOfProperty(EcoChangeData.PROP_NAME_SYSTEM_NO),
				EcoChangeData.COMBO_SYSTEM_CONFIG_LABEL, EcoChangeData.CELL_USER_RIGHT_EDITABLE_RULE_LABEL);

	}

	/**
	 * 수정여부를 설정하는 Rule 등록
	 * 
	 * @param configRegistry
	 * @param dataProvider
	 */
	private void registerEditableRules(IConfigRegistry configRegistry, IDataProvider dataProvider) {
		configRegistry.registerConfigAttribute(EditConfigAttributes.CELL_EDITABLE_RULE, getUserCheckEditRule(dataProvider), DisplayMode.EDIT,
				EcoChangeData.COMBO_ECO_PUBLISH_CONFIG_LABEL);
		configRegistry.registerConfigAttribute(EditConfigAttributes.CELL_EDITABLE_RULE, IEditableRule.ALWAYS_EDITABLE, DisplayMode.EDIT,
				EcoChangeData.EDITABLE_CONFIG_LABEL);
		configRegistry.registerConfigAttribute(EditConfigAttributes.CELL_EDITABLE_RULE, IEditableRule.ALWAYS_EDITABLE, DisplayMode.EDIT,
				EcoChangeData.CHECK_BOX_CONFIG_LABEL);

		configRegistry.registerConfigAttribute(EditConfigAttributes.CELL_EDITABLE_RULE, getEditRule(dataProvider), DisplayMode.EDIT,
				EcoChangeData.CELL_EDITABLE_RULE_APPLY_LABLEL);

		configRegistry.registerConfigAttribute(EditConfigAttributes.CELL_EDITABLE_RULE, getEditRule(dataProvider), DisplayMode.EDIT,
				EcoChangeData.COMBO_SYSTEM_CONFIG_LABEL);

		configRegistry.registerConfigAttribute(EditConfigAttributes.CELL_EDITABLE_RULE, getUserCheckEditRule(dataProvider), DisplayMode.EDIT,
				EcoChangeData.CELL_USER_RIGHT_EDITABLE_RULE_LABEL);

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
				.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER, checkBoxCellPainter, DisplayMode.NORMAL, EcoChangeData.CHECK_BOX_CONFIG_LABEL);
		configRegistry.registerConfigAttribute(CellConfigAttributes.DISPLAY_CONVERTER, new DefaultBooleanDisplayConverter(), DisplayMode.NORMAL,
				EcoChangeData.CHECK_BOX_CONFIG_LABEL);
		configRegistry.registerConfigAttribute(EditConfigAttributes.CELL_EDITOR, checkBoxCellEditor, DisplayMode.NORMAL,
				EcoChangeData.CHECK_BOX_EDITOR_CONFIG_LABEL);
	}

	/**
	 * ECO 발행 Combo Box 설정
	 * 
	 * @param configRegistry
	 * @param comboBoxCellPainter
	 */
	private void ecoPublishComboBoxEditor(IConfigRegistry configRegistry, ICellPainter comboBoxCellPainter) {
		ComboBoxCellEditor comboBoxCellEditor = new ComboBoxCellEditor(ecoPublishList);
		configRegistry.registerConfigAttribute(EditConfigAttributes.CELL_EDITOR, comboBoxCellEditor, DisplayMode.EDIT,
				EcoChangeData.COMBO_ECO_PUBLISH_CONFIG_LABEL);
	}

	/**
	 * System Code Combo Box 설정
	 */
	private void systemComboBoxEditor(IConfigRegistry configRegistry, ICellPainter comboBoxCellPainter) {
		ComboBoxCellEditor comboBoxCellEditor = new ComboBoxCellEditor(systemCodeList);
		configRegistry.registerConfigAttribute(EditConfigAttributes.CELL_EDITOR, comboBoxCellEditor, DisplayMode.EDIT, EcoChangeData.COMBO_SYSTEM_CONFIG_LABEL);
	}

	/**
	 * Cell Color 스타일을 등록함
	 * 
	 * @param configRegistry
	 */
	private void setCellColorStyle(IConfigRegistry configRegistry) {
		// 글자 색 Red
		Style style = new Style();
		style.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR, GUIHelper.COLOR_RED);
		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, style, DisplayMode.NORMAL, EcoChangeData.CELL_FRG_RED_COLOR_LABLEL);
		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, style, DisplayMode.SELECT, EcoChangeData.CELL_FRG_RED_COLOR_LABLEL);
		// 배경색 Green
		style = new Style();
		style.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, GUIHelper.COLOR_GREEN);
		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, style, DisplayMode.NORMAL, EcoChangeData.CELL_BG_GREEN_COLOR_LABLEL);
		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, style, DisplayMode.SELECT, EcoChangeData.CELL_BG_GREEN_COLOR_LABLEL);
		// 배경색 Red
		style = new Style();
		style.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, GUIHelper.COLOR_RED);
		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, style, DisplayMode.NORMAL, EcoChangeData.CELL_BG_RED_COLOR_LABLEL);
		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, style, DisplayMode.SELECT, EcoChangeData.CELL_BG_RED_COLOR_LABLEL);

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

				// Object rowChangeTypeObj = dataProvider.getDataValue(EcoChangeData.getColumnIndexOfProperty(EcoChangeData.PROP_NAME_ROW_CHANGE_TYPE), rowIndex);
				// if (rowChangeTypeObj == null)
				// return false;
				// String rowChangeType = (String) rowChangeTypeObj;
				Object rowDataObj = dataProvider.getDataValue(EcoChangeData.getColumnIndexOfProperty(EcoChangeData.PROP_NAME_ROW_DATA_OBJ), rowIndex);
				if (rowDataObj == null)
					return false;
				EcoChangeData rowData = (EcoChangeData) rowDataObj;

				String rowChangeType = rowData.getRowChangeType();
				if (rowChangeType == null)
					return false;

				/**
				 * 로그인 사용자와 등록자가 다르면 수정 못함
				 */
				String registerId = rowData.getRegisterId();
				if (!isRowEditableUser(registerId)) {
					return false;
				}

				return rowChangeType.equals(EcoChangeData.ROW_CHANGE_TYPE_NEW) ? true : false;
			}
		};
	}

	/**
	 * 사용자 권한 체크로 Cell Edit Rule을 설정함
	 * 
	 * @param dataProvider
	 * @return
	 */
	private IEditableRule getUserCheckEditRule(final IDataProvider dataProvider) {
		return new EditableRule() {
			@Override
			public boolean isEditable(int columnIndex, int rowIndex) {

				Object rowDataObj = dataProvider.getDataValue(EcoChangeData.getColumnIndexOfProperty(EcoChangeData.PROP_NAME_ROW_DATA_OBJ), rowIndex);
				if (rowDataObj == null)
					return false;
				EcoChangeData rowData = (EcoChangeData) rowDataObj;

				/**
				 * 로그인 사용자와 등록자가 다르면 수정 못함
				 */
				String registerId = rowData.getRegisterId();
				if (!isRowEditableUser(registerId))
					return false;
				else
					return true;
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
				EcoChangeData.ALIGN_CELL_CONTENTS_CENTER_CONFIG_LABEL);
	}

	/**
	 * Table Data 리스트를 가져옴
	 * 
	 * @return
	 */
	public EventList<EcoChangeData> getTableDataList() {
		return tableDataList;
	}

	/**
	 * 변경 관리 리스트 생성모드 인지 여부
	 * 
	 * @param IS_ALL_CHANGE_MODE
	 */
	public void setAllChangeMode(boolean IS_ALL_CHANGE_MODE) {
		this.IS_ALL_CHANGE_MODE = IS_ALL_CHANGE_MODE;
		btnRegister.setEnabled(IS_ALL_CHANGE_MODE);
		btnSave.setEnabled(!IS_ALL_CHANGE_MODE);
	}

	/**
	 * 변경 관리 리스트 생성 중일 경우 Category 추가 검토 조건을 저장함
	 */
	public void setCategoryAddCondList(HashMap<ArrayList<String>, ArrayList<EcoOspecCateData>> categoryConditionMap) {
		this.categoryConditionMap = categoryConditionMap;
	}

	private void excelListExport() {
		File selectedFile = null;
		try {
			selectedFile = selectFile(EcoChangeData.TEMPLATE_DS_ECO_RPT, null);

			if (selectedFile == null)
				return;
			EcoChangeData inputData = new EcoChangeData();
			inputData.setSearchEcoChangeList(tableDataList);
			ExportEcoChangeListOperation op = new ExportEcoChangeListOperation(inputData, selectedFile);
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
	 * Row 를 수정할 수 있는 사용자 인지 체크
	 * 
	 * @param registerId
	 * @return
	 */
	private boolean isRowEditableUser(String registerId) {
		String logInUserId = null, roleName = null;
		try {
			if (registerId == null)
				return true;
			logInUserId = tcSession.getUser().getUserId();
			roleName = tcSession.getCurrentRole().getProperty("role_name");
		} catch (TCException e) {
			e.printStackTrace();
		}

		if (logInUserId.equals(registerId) || roleName.equalsIgnoreCase("DBA"))
			return true;
		else
			return false;
	}

	/**
	 * PopUp Menu
	 * 
	 * @author baek
	 * 
	 */
	private class PopupMenuConfiguration extends AbstractUiBindingConfiguration {

		private final Menu menu;

		public PopupMenuConfiguration(final NatTable natTable) {
			this.menu = new PopupMenuBuilder(natTable).withMenuItemProvider(new IMenuItemProvider() {
				@Override
				public void addMenuItem(final NatTable natTable, Menu popupMenu) {
					MenuItem menuItem = new MenuItem(popupMenu, SWT.PUSH);
					menuItem.setText("ECO 발행 일괄입력");
					menuItem.setEnabled(true);
					menuItem.addSelectionListener(new SelectionAdapter() {
						@Override
						public void widgetSelected(SelectionEvent event) {
							MultiInputValueDialog dialog = new MultiInputValueDialog(EcoChangeListMgrComposite.this.getShell(), tableDataList, ecoPublishList);
							dialog.open();
						}
					});
				}
			}).build();
		}

		@Override
		public void configureUiBindings(UiBindingRegistry uiBindingRegistry) {
			uiBindingRegistry.registerMouseDownBinding(new MouseEventMatcher(SWT.NONE, GridRegion.BODY, MouseEventMatcher.RIGHT_BUTTON), new PopupMenuAction(
					menu) {
				@Override
				public void run(NatTable natTable, MouseEvent event) {
					// int columnPosition = natTable.getColumnPositionByX(event.x);
					// int rowPosition = natTable.getRowPositionByY(event.y);
					// if (!selectionLayer.isRowPositionFullySelected(rowPosition)) {
					// natTable.doCommand(new SelectRowsCommand(natTable, columnPosition, rowPosition, false, false));
					// }
					super.run(natTable, event);
				}
			});
		}
	}
}

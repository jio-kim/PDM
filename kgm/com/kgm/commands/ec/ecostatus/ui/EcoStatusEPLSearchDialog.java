package com.kgm.commands.ec.ecostatus.ui;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.AbstractRegistryConfiguration;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.groupBy.ModernGroupByThemeExtension;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ColumnOverrideLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.painter.cell.TextPainter;
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
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.wb.swt.SWTResourceManager;

import ca.odell.glazedlists.EventList;

import com.kgm.commands.ec.ecostatus.model.EcoOspecCateData;
import com.kgm.commands.ec.ecostatus.model.EcoStatusData;
import com.kgm.commands.ec.ecostatus.model.EplSearchData;
import com.kgm.commands.ec.ecostatus.operation.ExportEplSearchListOperation;
import com.kgm.commands.ec.ecostatus.operation.SearchEplListOperation;
import com.kgm.commands.ec.ecostatus.utility.BasicGridEditorGridLayer;
import com.kgm.commands.ec.ecostatus.utility.TableStylingThemeConfiguration;
import com.kgm.common.utils.SYMTcUtil;
import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.aif.InterfaceAIFOperationListener;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.ConfirmDialog;
import com.teamcenter.rac.util.MessageBox;

/**
 * EPL 검색
 * 
 * @author baek
 * 
 */
public class EcoStatusEPLSearchDialog extends Dialog {

	private CCombo comboCategory;
	private Button btnExport;
	private EcoStatusData stdInformData = null; // 기준정보
	private EcoOspecCateData categoryData = null; // Category 정보
	private EventList<EplSearchData> tableDataList;
	private NatTable table;
	private TCSession tcSession = null;
	private ArrayList<EplSearchData> allEplList = null; // 전체 EPL 리스트
	private ArrayList<EplSearchData> eplWithDeDuplList = null; // 중복제거된 EPL 리스트
	private HashMap<String, ArrayList<EplSearchData>> allEplListSet = null;// Category 별 EPL 전체 리스트
	private HashMap<String, ArrayList<EplSearchData>> eplWithDeDuplListSet = null;// Category 별 EPL 중복제거된 리스트
	private Button btnDeDeplication;
	private DataLayer bodyDataLayer;
	private Composite compositeResult;

	/**
	 * Create the dialog.
	 * 
	 * @param parentShell
	 */
	public EcoStatusEPLSearchDialog(EcoStatusData stdInformData, EcoOspecCateData categoryData, TCSession tcSession, Shell parentShell) {
		super(parentShell);
		// setShellStyle(SWT.BORDER | SWT.CLOSE | SWT.RESIZE | SWT.APPLICATION_MODAL);
		setShellStyle(SWT.BORDER | SWT.CLOSE | SWT.RESIZE);
		this.tcSession = tcSession;
		this.stdInformData = stdInformData;
		this.categoryData = categoryData;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("EPL Search");
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
		gl_container.marginWidth = 0;
		gl_container.marginHeight = 0;
		gl_container.verticalSpacing = 0;
		container.setLayout(gl_container);

		FormToolkit toolkit = new FormToolkit(parent.getDisplay());

		Section sectionbBasic = toolkit.createSection(container, Section.TITLE_BAR);
		GridData gd_sectionbBasic = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
		gd_sectionbBasic.minimumWidth = 0;
		sectionbBasic.setLayoutData(gd_sectionbBasic);

		Composite compositeBasic = toolkit.createComposite(sectionbBasic, SWT.NONE);
		compositeBasic.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		sectionbBasic.setClient(compositeBasic);
		GridLayout gl_compositeBasic = new GridLayout(5, false);
		gl_compositeBasic.marginWidth = 0;
		gl_compositeBasic.marginHeight = 0;
		gl_compositeBasic.horizontalSpacing = 10;
		compositeBasic.setLayout(gl_compositeBasic);

		Label lblOptionCategory = new Label(compositeBasic, SWT.NONE);
		toolkit.adapt(lblOptionCategory, true, true);
		lblOptionCategory.setText("Option Categroy");

		comboCategory = new CCombo(compositeBasic, SWT.BORDER);
		comboCategory.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					refreshEplList();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});
		comboCategory.setEditable(false);
		GridData gd_comboCategory = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_comboCategory.widthHint = 100;
		comboCategory.setLayoutData(gd_comboCategory);
		toolkit.adapt(comboCategory);
		new Label(compositeBasic, SWT.NONE);

		btnDeDeplication = new Button(compositeBasic, SWT.CHECK);
		toolkit.adapt(btnDeDeplication, true, true);
		btnDeDeplication.setText("중복제거");
		btnDeDeplication.setSelection(false);
		btnDeDeplication.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				refreshEplList();
			}
		});

		Composite compositeTopRightButton = new Composite(compositeBasic, SWT.NONE);
		compositeTopRightButton.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, true, false, 1, 1));
		compositeTopRightButton.setLayout(new GridLayout(2, false));

		btnExport = new Button(compositeTopRightButton, SWT.NONE);
		btnExport.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		btnExport.setText("Excel Export");
		btnExport.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				doExport();
			}
		});
		btnExport.setImage(SWTResourceManager.getImage(EcoStatusEPLSearchDialog.class, "/com/kgm/common/images/export_16.png"));
		new Label(compositeTopRightButton, SWT.NONE);

		Section sectionResult = toolkit.createSection(container, Section.TITLE_BAR);
		GridData gd_sectionResult = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_sectionResult.heightHint = 5;
		gd_sectionResult.minimumWidth = 0;
		sectionResult.setLayoutData(gd_sectionResult);

		compositeResult = toolkit.createComposite(sectionResult, SWT.BORDER);
		compositeResult.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		GridLayout gl_compositeResult = new GridLayout(1, false);
		gl_compositeResult.marginWidth = 0;
		gl_compositeResult.marginHeight = 0;
		gl_compositeResult.horizontalSpacing = -1;
		gl_compositeResult.verticalSpacing = 0;
		compositeResult.setLayout(gl_compositeResult);
		sectionResult.setClient(compositeResult);
		/**
		 * Table UI
		 */
		makeTableUI();
		// 초기 Data Load
		initDataLoad();
		// EPL Search
		doEplSearch();

		return container;
	}

	/**
	 * Table UI 생성
	 */
	private void makeTableUI() {
		String[] propertyNames = EplSearchData.getPropertyNames();
		Map<String, String> propertyToLabelMap = EplSearchData.getPropertyToLabelMap();

		ConfigRegistry configRegistry = new ConfigRegistry();
		BasicGridEditorGridLayer<EplSearchData> gridLayer = new BasicGridEditorGridLayer<EplSearchData>(new ArrayList<EplSearchData>(), configRegistry,
				propertyNames, propertyToLabelMap);

		bodyDataLayer = gridLayer.getBodyLayer().getDataLayer();
		tableDataList = gridLayer.getTableDataList();

		ColumnOverrideLabelAccumulator columnLabelAccumulator = new ColumnOverrideLabelAccumulator(bodyDataLayer);
		bodyDataLayer.setConfigLabelAccumulator(columnLabelAccumulator);

		table = new NatTable(compositeResult, gridLayer, false);
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		table.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));

		table.setConfigRegistry(configRegistry);
		table.addConfiguration(new DefaultNatTableStyleConfiguration()); // 기본 Style 설정
		table.addConfiguration(setModelEditorConfiguration(columnLabelAccumulator, bodyDataLayer));
		table.addConfiguration(new SingleClickSortConfiguration()); // Single Click 시 Sort 설정
		table.configure();

		bodyDataLayer.setDefaultColumnWidthByPosition(EplSearchData.getColumnIndexOfProperty(EplSearchData.PROP_NAME_FUNCTION_NO), 85);
		bodyDataLayer.setDefaultColumnWidthByPosition(EplSearchData.getColumnIndexOfProperty(EplSearchData.PROP_NAME_PART_NAME), 150);
		bodyDataLayer.setDefaultColumnWidthByPosition(EplSearchData.getColumnIndexOfProperty(EplSearchData.PROP_NAME_OPTIONS), 250);
		bodyDataLayer.setDefaultColumnWidthByPosition(EplSearchData.getColumnIndexOfProperty(EplSearchData.PROP_NAME_SYSTEM_NO), 53);
		bodyDataLayer.setDefaultColumnWidthByPosition(EplSearchData.getColumnIndexOfProperty(EplSearchData.PROP_NAME_USER_NAME), 100);
		bodyDataLayer.setDefaultColumnWidthByPosition(EplSearchData.getColumnIndexOfProperty(EplSearchData.PROP_NAME_TEAM_NAME), 100);

		// add modern styling
		ThemeConfiguration theme = new TableStylingThemeConfiguration();
		theme.addThemeExtension(new ModernGroupByThemeExtension());
		table.setTheme(theme);
	}

	/**
	 * Create contents of the button bar.
	 * 
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		parent.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		createButton(parent, IDialogConstants.CANCEL_ID, "Close", false);
	}

	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(840, 600);
	}

	/**
	 * 초기 Data Load
	 */
	private void initDataLoad() {

		if (categoryData == null)
			return;

		EventList<EcoOspecCateData> targetCategoryList = categoryData.getChangeCategoryList();

		if (targetCategoryList == null)
			return;
		/**
		 * Option Category Combo 에 값을 추가함
		 */
		comboCategory.removeAll();
		comboCategory.add("ALL");
		comboCategory.select(0);
		for (EcoOspecCateData targetCategory : targetCategoryList) {
			String category = targetCategory.getCategory();
			if (category == null)
				continue;
			comboCategory.add(category);
			comboCategory.setData(category);
		}
	}

	/**
	 * EPL를 조회함
	 */
	private void doEplSearch() {
		final SearchEplListOperation operation = new SearchEplListOperation(stdInformData, categoryData, tableDataList);
		tcSession.queueOperation(operation);
		operation.addOperationListener(new InterfaceAIFOperationListener() {
			@Override
			public void startOperation(String arg0) {

			}

			@Override
			public void endOperation() {
				allEplList = operation.getAllEplList();
				eplWithDeDuplList = operation.getEplWithDeDuplList();

				allEplListSet = operation.getAllEplListSet();
				eplWithDeDuplListSet = operation.getEplWithDeDuplListSet();

			}
		});

	}

	/**
	 * EPL 테이블 Data 를 다시 가져옴
	 */
	protected void refreshEplList() {
		// Table UI를 다시 그림. nattable 에서는 Context 줄 수가 줄어들때, Row Height가 자동적으로 줄어들지 않기 때문에 Table 을 제거후 다시 붙임
		// Table 제거
		table.dispose();
		/**
		 * Table UI 생성
		 */
		makeTableUI();
		// UI Refresh
		compositeResult.layout();

		String selectedCategory = comboCategory.getText().equals("ALL") ? null : comboCategory.getText();
		boolean isDeDuplicate = btnDeDeplication.getSelection();
		RefreshTableDataListOperation operation = new RefreshTableDataListOperation(selectedCategory, isDeDuplicate);
		tcSession.queueOperation(operation);
	}

	/**
	 * Excel Export를 함
	 */
	protected void doExport() {
		File selectedFile = null;
		try {
			selectedFile = selectFile(EplSearchData.TEMPLATE_EPL_LIST_RPT, null);

			if (selectedFile == null)
				return;

			EplSearchData inputData = new EplSearchData();
			inputData.setTableDataList(tableDataList);
			ExportEplSearchListOperation operation = new ExportEplSearchListOperation(inputData, selectedFile);
			tcSession.queueOperation(operation);

		} catch (Exception ex) {
			MessageBox.post(this.getShell(), ex.toString(), "Error", MessageBox.ERROR);
		}
	}

	public AbstractRegistryConfiguration setModelEditorConfiguration(final ColumnOverrideLabelAccumulator columnLabelAccumulator, final DataLayer bodyDataLayer) {

		return new AbstractRegistryConfiguration() {
			@Override
			public void configureRegistry(IConfigRegistry configRegistry) {
				registerConfigLabelsOnColumns(columnLabelAccumulator);
				registerColumnOptionPainter(configRegistry);
			}
		};
	}

	private void registerConfigLabelsOnColumns(ColumnOverrideLabelAccumulator columnLabelAccumulator) {
		columnLabelAccumulator
				.registerColumnOverrides(EplSearchData.getColumnIndexOfProperty(EplSearchData.PROP_NAME_OPTIONS), EplSearchData.PROP_NAME_OPTIONS);
	}

	private void registerColumnOptionPainter(IConfigRegistry configRegistry) {
		Style style = new Style();
		style.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, HorizontalAlignmentEnum.LEFT);
		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, style, DisplayMode.NORMAL, EplSearchData.PROP_NAME_OPTIONS);
		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER, new TextPainter(true, true, false, true), DisplayMode.NORMAL,
				EplSearchData.PROP_NAME_OPTIONS);
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
	 * Table Data 리스트를 다시 가져옴
	 * 
	 * @author baek
	 * 
	 */
	public class RefreshTableDataListOperation extends AbstractAIFOperation {
		private String selectedCategory = null;
		private boolean isDeDuplicate = false;

		public RefreshTableDataListOperation(String selectedCategory, boolean isDeDuplicate) {
			this.selectedCategory = selectedCategory;
			this.isDeDuplicate = isDeDuplicate;
		}

		@Override
		public void executeOperation() throws Exception {
			tableDataList.clear();
			// 전체 옵션
			if (selectedCategory == null) {
				// 중복제거 체크
				if (isDeDuplicate) {
					if (eplWithDeDuplList == null)
						return;
					for (EplSearchData rowData : eplWithDeDuplList)
						tableDataList.add(rowData);
				} else {
					// 전체데이터
					if (allEplList == null)
						return;
					for (EplSearchData rowData : allEplList)
						tableDataList.add(rowData);
				}
				// 옵션 선택
			} else {
				ArrayList<EplSearchData> eplDataList = null;
				// 중복체크
				if (isDeDuplicate) {
					if (eplWithDeDuplListSet == null)
						return;
					eplDataList = eplWithDeDuplListSet.get(selectedCategory);
				} else {
					// 전체 데이터
					if (allEplListSet == null)
						return;
					eplDataList = allEplListSet.get(selectedCategory);
				}
				if (eplDataList == null)
					return;
				for (EplSearchData rowData : eplDataList)
					tableDataList.add(rowData);
			}

		}
	}
}

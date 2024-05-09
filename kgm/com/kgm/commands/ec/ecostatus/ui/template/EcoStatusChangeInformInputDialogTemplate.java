package com.kgm.commands.ec.ecostatus.ui.template;

import java.util.ArrayList;
import java.util.Map;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.groupBy.ModernGroupByThemeExtension;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ColumnOverrideLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.sort.config.SingleClickSortConfiguration;
import org.eclipse.nebula.widgets.nattable.style.theme.ThemeConfiguration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
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

import com.kgm.commands.ec.ecostatus.model.EcoOspecCateData;
import com.kgm.commands.ec.ecostatus.utility.BasicGridEditorGridLayer;
import com.kgm.commands.ec.ecostatus.utility.TableStylingThemeConfiguration;

import ca.odell.glazedlists.EventList;

/**
 * 변경정보 입력 Dialog
 * 
 * @author baek
 * 
 */
public class EcoStatusChangeInformInputDialogTemplate extends Dialog {

	private Button btnSearch;
	private NatTable leftNattable;
	private NatTable rightNattable;
	private EventList<EcoOspecCateData> leftTableDataList; // 왼쪽 Table Data 리스트
	//private EventList<EcoOspecCateData> rightTableDataList; // 오른쪽 Table Data 리스트
	private ArrayList<String> targetCategoryList = null;// 대상 Category 리스트
	private CCombo comboCategory = null; // Option Category

	/**
	 * Create the dialog.
	 * 
	 * @param parentShell
	 */
	public EcoStatusChangeInformInputDialogTemplate(ArrayList<String> targetCategoryList, Shell parentShell) {
		super(parentShell);
		setShellStyle(SWT.DIALOG_TRIM | SWT.MIN | SWT.RESIZE);
		this.targetCategoryList = targetCategoryList;
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
				doSearch();
			}
		});
		btnSearch.setImage(SWTResourceManager.getImage(EcoStatusChangeInformInputDialogTemplate.class, "/icons/search_16.png"));
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
		GridData gd_compositeRight = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
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

		leftNattable = new NatTable(compositeLeft, gridLayer, false);
		leftNattable.setLayout(new FillLayout(SWT.HORIZONTAL));
		leftNattable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		leftNattable.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));

		leftNattable.setConfigRegistry(configRegistry);
		leftNattable.addConfiguration(new DefaultNatTableStyleConfiguration()); // 기본 Style 설정
		leftNattable.addConfiguration(new SingleClickSortConfiguration()); // Single Click 시 Sort 설정
		leftNattable.configure();

		bodyDataLayer.setDefaultColumnWidthByPosition(EcoOspecCateData.getColumnIndexOfProperty(EcoOspecCateData.PROP_NAME_CATEGORY), 100);
		bodyDataLayer.setDefaultColumnWidthByPosition(EcoOspecCateData.getColumnIndexOfProperty(EcoOspecCateData.PROP_NAME_REVIEW_CONTENTS), 460);

		// add modern styling
		ThemeConfiguration theme = new TableStylingThemeConfiguration();
		theme.addThemeExtension(new ModernGroupByThemeExtension());
		leftNattable.setTheme(theme);

		String[] rightPropertyNames = EcoOspecCateData.getOptPropertyNames();
		Map<String, String> rightPropertyToLabelMap = EcoOspecCateData.getOptPropertyToLabelMap();
		ConfigRegistry rightConfigRegistry = new ConfigRegistry();
		BasicGridEditorGridLayer<EcoOspecCateData> rightGridLayer = new BasicGridEditorGridLayer<EcoOspecCateData>(new ArrayList<EcoOspecCateData>(),
				rightConfigRegistry, rightPropertyNames, rightPropertyToLabelMap);
		DataLayer rightBodyDataLayer = rightGridLayer.getBodyLayer().getDataLayer();
		//rightTableDataList = rightGridLayer.getTableDataList();

		ColumnOverrideLabelAccumulator rightColumnLabelAccumulator = new ColumnOverrideLabelAccumulator(rightBodyDataLayer);
		rightBodyDataLayer.setConfigLabelAccumulator(rightColumnLabelAccumulator);

		rightNattable = new NatTable(compositeRight, rightGridLayer, false);
		rightNattable.setLayout(new FillLayout(SWT.HORIZONTAL));
		GridData gd_rightNattable = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_rightNattable.widthHint = 250;
		rightNattable.setLayoutData(gd_rightNattable);
		rightNattable.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));

		rightNattable.setConfigRegistry(rightConfigRegistry);
		rightNattable.addConfiguration(new DefaultNatTableStyleConfiguration()); // 기본 Style 설정
		rightNattable.addConfiguration(new SingleClickSortConfiguration()); // Single Click 시 Sort 설정
		rightNattable.configure();

		rightBodyDataLayer.setDefaultColumnWidthByPosition(EcoOspecCateData.getOptColumnIndexOfProperty(EcoOspecCateData.PROP_NAME_ADD_REVIEW_OPTION), 100);
		rightBodyDataLayer.setDefaultColumnWidthByPosition(EcoOspecCateData.getOptColumnIndexOfProperty(EcoOspecCateData.PROP_NAME_ADD_OR_EX_CONDITION), 100);

		// add modern styling
		ThemeConfiguration rightTheme = new TableStylingThemeConfiguration();
		rightTheme.addThemeExtension(new ModernGroupByThemeExtension());
		rightNattable.setTheme(rightTheme);

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

		Button btnSave = createButton(parent, IDialogConstants.OK_ID, "Save", false);
		btnSave.setImage(SWTResourceManager.getImage(EcoStatusChangeInformInputDialogTemplate.class, "/icons/save_16.png"));
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(937, 415);
	}

	/**
	 * 초기 Data Load
	 */
	private void initDataLoad() {

		if (targetCategoryList == null)
			return;
		/**
		 * Option Category Combo 에 값을 추가함
		 */
		comboCategory.removeAll();
		comboCategory.add("ALL");
		comboCategory.select(0);
		for (String category : targetCategoryList) {
			comboCategory.add(category);
			comboCategory.setData(category);
		}

		/**
		 * Option Category 테이블에 Load 함
		 */
		for (String category : targetCategoryList) {
			EcoOspecCateData rowData = new EcoOspecCateData();
			rowData.setCategory(category);
			leftTableDataList.add(rowData);
		}

	}

	/**
	 * EPL 조회
	 */
	private void doSearch() {
		// TODO Auto-generated method stub

	}

	/**
	 * 최신 O/Spec 조회
	 */
	private void doViewLatestOpsec() {

	}

	/**
	 * 선택된 Category 에 해당하는 OSPEC Compare 기능
	 */
	private void doCompareOspecWithTarget() {
		// TODO Auto-generated method stub

	}

	public static void main(String[] args) {
		EcoStatusChangeInformInputDialogTemplate dialog = new EcoStatusChangeInformInputDialogTemplate(null, null);
		dialog.open();
	}
}

package com.kgm.commands.ec.ecostatus.ui;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.AbstractRegistryConfiguration;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.IEditableRule;
import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.data.convert.DefaultBooleanDisplayConverter;
import org.eclipse.nebula.widgets.nattable.edit.EditConfigAttributes;
import org.eclipse.nebula.widgets.nattable.edit.action.ToggleCheckBoxColumnAction;
import org.eclipse.nebula.widgets.nattable.edit.editor.CheckBoxCellEditor;
import org.eclipse.nebula.widgets.nattable.edit.editor.ICellEditor;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.groupBy.ModernGroupByThemeExtension;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.AbstractOverrider;
import org.eclipse.nebula.widgets.nattable.layer.cell.ColumnLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.painter.cell.CheckBoxPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.ColumnHeaderCheckBoxPainter;
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
import org.eclipse.nebula.widgets.nattable.ui.util.CellEdgeEnum;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.wb.swt.SWTResourceManager;

import ca.odell.glazedlists.EventList;

import com.kgm.commands.ec.ecostatus.model.OspecSelectData;
import com.kgm.commands.ec.ecostatus.utility.BasicGridEditorGridLayer;
import com.kgm.commands.ec.ecostatus.utility.TableStylingThemeConfiguration;
import com.kgm.commands.ospec.OSpecMainDlg;
import com.kgm.common.WaitProgressBar;
import com.kgm.common.utils.CustomUtil;
import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.IPropertyName;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCSession;

/**
 * OSPEC 선택
 * 
 * @author baek
 * 
 */
public class EcoStatusSelectOspecDialog extends Dialog {

	private TCSession tcSession = null;
	private OspecSelectData initData = null;
	private EventList<OspecSelectData> tableDataList = null;
	private NatTable table;
	private SelectionLayer selectionLayer = null; // Table Body의 선택되는 Layer
	private String selectedOspecId = null; // 더블클릭시 선택된 Opsec Id

	/**
	 * Create the dialog.
	 * 
	 * @param parentShell
	 */
	public EcoStatusSelectOspecDialog(Shell parentShell, OspecSelectData initData) {
		super(parentShell);
		setShellStyle(SWT.DIALOG_TRIM | SWT.RESIZE | SWT.PRIMARY_MODAL);
		this.tcSession = CustomUtil.getTCSession();
		this.initData = initData;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Select Ospec");
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
		GridData gd_sectionbBasic = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_sectionbBasic.minimumWidth = 0;
		sectionbBasic.setLayoutData(gd_sectionbBasic);
		sectionbBasic.setText("Ospec 정보 ");

		Composite compositeResult = toolkit.createComposite(sectionbBasic, SWT.WRAP);
		compositeResult.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		GridLayout gl_compositeResult = new GridLayout(1, false);
		gl_compositeResult.marginWidth = 0;
		gl_compositeResult.marginHeight = 0;
		gl_compositeResult.horizontalSpacing = -1;
		gl_compositeResult.verticalSpacing = 0;
		compositeResult.setLayout(gl_compositeResult);
		sectionbBasic.setClient(compositeResult);

		String[] propertyNames = OspecSelectData.getPropertyNames();
		Map<String, String> propertyToLabelMap = OspecSelectData.getPropertyToLabelMap();

		ConfigRegistry configRegistry = new ConfigRegistry();
		BasicGridEditorGridLayer<OspecSelectData> gridLayer = new BasicGridEditorGridLayer<OspecSelectData>(new ArrayList<OspecSelectData>(), configRegistry,
				propertyNames, propertyToLabelMap);

		DataLayer bodyDataLayer = gridLayer.getBodyLayer().getDataLayer();
		tableDataList = gridLayer.getTableDataList();

		@SuppressWarnings("unchecked")
		IRowDataProvider<OspecSelectData> bodyDataProvider = (IRowDataProvider<OspecSelectData>) bodyDataLayer.getDataProvider();
		CustomLabelAccumulator columnLabelAccumulator = new CustomLabelAccumulator(bodyDataProvider);
		bodyDataLayer.setConfigLabelAccumulator(columnLabelAccumulator);

		ColumnHeaderCheckBoxPainter columnHeaderCheckBoxPainter = new ColumnHeaderCheckBoxPainter(bodyDataLayer);

		table = new NatTable(compositeResult, gridLayer, false);
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		table.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));

		table.setConfigRegistry(configRegistry);
		table.addConfiguration(new DefaultNatTableStyleConfiguration()); // 기본 Style 설정
		table.addConfiguration(new SingleClickSortConfiguration()); // Single Click 시 Sort 설정
		table.addConfiguration(setModelEditorConfiguration(columnLabelAccumulator, bodyDataLayer, columnHeaderCheckBoxPainter));
		table.configure();
		// 선택된 Table Layer
		selectionLayer = gridLayer.getBodyLayer().getSelectionLayer();
		bodyDataLayer.setDefaultColumnWidthByPosition(OspecSelectData.getColumnIndexOfProperty(OspecSelectData.PROP_NAME_ROW_CHECK), 30);
		bodyDataLayer.setDefaultColumnWidthByPosition(OspecSelectData.getColumnIndexOfProperty(OspecSelectData.PROP_NAME_OSPEC_ID), 100);
		bodyDataLayer.setDefaultColumnWidthByPosition(OspecSelectData.getColumnIndexOfProperty(OspecSelectData.PROP_NAME_RELEASE), 100);

		addCellSectionEvent();// Mouse 이벤트
		// add modern styling
		ThemeConfiguration theme = new TableStylingThemeConfiguration();
		theme.addThemeExtension(new ModernGroupByThemeExtension());
		table.setTheme(theme);
		gridLayer.getColumnHeaderDataLayer().setDefaultRowHeight(25);

		InitDataLoadOperation operation = new InitDataLoadOperation(initData);
		tcSession.queueOperation(operation);

		return container;
	}

	/**
	 * Create contents of the button bar.
	 * 
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		parent.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID, "Close", false);
	}

	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(350, 300);
	}

	public AbstractRegistryConfiguration setModelEditorConfiguration(final CustomLabelAccumulator columnLabelAccumulator, final DataLayer bodyDataLayer,
			final ColumnHeaderCheckBoxPainter columnHeaderCheckBoxPainter) {

		return new AbstractRegistryConfiguration() {
			@Override
			public void configureRegistry(IConfigRegistry configRegistry) {

				// Header multi line
				ICellPainter cellPainter = new BeveledBorderDecorator(new TextPainter(true, true, true, true));
				configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER, cellPainter, DisplayMode.NORMAL, GridRegion.COLUMN_HEADER);

				// 전체선택 Header
				ICellPainter columnHeaderPainter = new CustomLineBorderDecorator(new CellPainterDecorator(new TextPainter(), CellEdgeEnum.BOTTOM,
						columnHeaderCheckBoxPainter));
				configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER, columnHeaderPainter, DisplayMode.NORMAL,
						ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + 0);

				columnLabelAccumulator.registerColumnOverrides(OspecSelectData.getColumnIndexOfProperty(OspecSelectData.PROP_NAME_ROW_CHECK),
						OspecSelectData.CHECK_BOX_EDITOR_CONFIG_LABEL, OspecSelectData.CHECK_BOX_CONFIG_LABEL,
						OspecSelectData.ALIGN_CELL_CONTENTS_CENTER_CONFIG_LABEL);

				registerCheckBoxEditor(configRegistry, new CheckBoxPainter(), new CheckBoxCellEditor());

				// 수정여부를 설정하는 Rule 지정
				configRegistry.registerConfigAttribute(EditConfigAttributes.CELL_EDITABLE_RULE, IEditableRule.ALWAYS_EDITABLE, DisplayMode.EDIT,
						OspecSelectData.CHECK_BOX_CONFIG_LABEL);

				registerAignCenterCellStyle(configRegistry);

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
	 * 체크박스 Editor
	 * 
	 * @param configRegistry
	 * @param checkBoxCellPainter
	 * @param checkBoxCellEditor
	 */
	private void registerCheckBoxEditor(IConfigRegistry configRegistry, ICellPainter checkBoxCellPainter, ICellEditor checkBoxCellEditor) {
		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER, checkBoxCellPainter, DisplayMode.NORMAL,
				OspecSelectData.CHECK_BOX_CONFIG_LABEL);
		configRegistry.registerConfigAttribute(CellConfigAttributes.DISPLAY_CONVERTER, new DefaultBooleanDisplayConverter(), DisplayMode.NORMAL,
				OspecSelectData.CHECK_BOX_CONFIG_LABEL);
		configRegistry.registerConfigAttribute(EditConfigAttributes.CELL_EDITOR, checkBoxCellEditor, DisplayMode.NORMAL,
				OspecSelectData.CHECK_BOX_EDITOR_CONFIG_LABEL);
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
	 * Ospec 검색
	 * 
	 * @author baek
	 * 
	 */
	public class InitDataLoadOperation extends AbstractAIFOperation {
		private OspecSelectData initData = null;
		private WaitProgressBar waitProgress;

		public InitDataLoadOperation(OspecSelectData initData) {
			this.initData = initData;
		}

		@Override
		public void executeOperation() throws Exception {
			try {
				waitProgress = new WaitProgressBar(AIFUtility.getActiveDesktop());
				waitProgress.start();
				waitProgress.setWindowSize(480, 300);
				waitProgress.setStatus("Search Data...");

				/**
				 * 정보저장
				 */
				executeSearch();
				waitProgress.setStatus("Complete");
				waitProgress.close();

			} catch (Exception ex) {
				if (waitProgress != null) {
					waitProgress.setStatus("＠ Error Message : ");
					waitProgress.setStatus(ex.toString());
					waitProgress.close("Error", false);
				}
				setAbortRequested(true);
				ex.printStackTrace();
			}
		}

		private void executeSearch() throws Exception {
			TCComponentItemRevision[] revisions = OSpecMainDlg.getOspecRevision(initData.getgModel(), initData.getProjectNo(), "", null);
			if(revisions == null)
				return;
			HashMap<String, TCComponentItemRevision> revisionMap = new HashMap<String, TCComponentItemRevision>();
			for (TCComponentItemRevision revision : revisions) {
				String ospecId = revision.getProperty(IPropertyName.ITEM_ID);
				String revId = revision.getProperty(IPropertyName.ITEM_REVISION_ID);
				revisionMap.put(ospecId + "-" + revId, revision);
			}
			Map<String, TCComponentItemRevision> treeMap = new TreeMap<String, TCComponentItemRevision>(revisionMap);

			SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd");

			for (String key : treeMap.keySet()) {
				TCComponentItemRevision revision = revisionMap.get(key);
				Date releaseDate = revision.getDateProperty("s7_OspecReleasedDate");
				String releaseDateStr = sd.format(releaseDate);
				OspecSelectData data = new OspecSelectData();
				data.setOspecId(key);
				data.setReleaseDate(releaseDateStr);
				tableDataList.add(data);
			}

		}

	}

	class CustomLabelAccumulator extends AbstractOverrider {

		private IRowDataProvider<OspecSelectData> bodyDataProvider;

		CustomLabelAccumulator(IRowDataProvider<OspecSelectData> bodyDataProvider) {
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
			OspecSelectData rowObject = this.bodyDataProvider.getRowObject(rowPosition);

			if (rowObject == null)
				return;
			addOverrides(configLabels, columnPosition);

		}
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

				Object ospecIdObj = selectionLayer.getDataValueByPosition(OspecSelectData.getColumnIndexOfProperty(OspecSelectData.PROP_NAME_OSPEC_ID),
						rowIndex);
				if (ospecIdObj == null)
					return;
				selectedOspecId = (String) ospecIdObj;
				okPressed();
			}
		});
	}

	/**
	 * 선택된 Ospec Id를 가져옴
	 * 
	 * @return
	 */
	public String getSelectedOspecId() {
		ArrayList<String> selectedOspecList = new ArrayList<String>();
		String projectNo = initData.getProjectNo();
		String ospecIds = null;
		// Row 더블클릭을 했을 경우
		if (selectedOspecId != null) {
			ospecIds = selectedOspecId.replace(projectNo.concat("-"), "");
		} else {
			// 확인 버튼 클릭시
			for (OspecSelectData data : tableDataList) {
				String ospecId = data.getOspecId();
				boolean isRowCheck = data.isRowCheck();
				if (!isRowCheck)
					continue;
				ospecId = ospecId.replace(projectNo.concat("-"), "");
				selectedOspecList.add(ospecId);
			}
			Collections.sort(selectedOspecList);
			StringBuffer ospecIdSb = new StringBuffer();
			for (int i = 0; i < selectedOspecList.size(); i++) {
				String ospecId = selectedOspecList.get(i);
				if (i > 0)
					ospecId = "/" + ospecId.replace("OSI-", "");
				ospecIdSb.append(ospecId);
			}
			ospecIds = ospecIdSb.toString();
		}

		return ospecIds;
	}
}

package com.ssangyong.commands.ec.ecostatus.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Panel;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollBar;
import javax.swing.JSplitPane;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

import com.ssangyong.commands.ec.SYMCECConstant;
import com.ssangyong.commands.ec.dao.CustomECODao;
import com.ssangyong.commands.ec.ecostatus.model.EcoStatusData;
import com.ssangyong.commands.ospec.op.OSpec;
import com.ssangyong.commands.ospec.op.OpUtil;
import com.ssangyong.common.WaitProgressBar;
import com.ssangyong.common.remote.DataSet;
import com.ssangyong.common.ui.mergetable.MultiSpanCellTable;
import com.ssangyong.common.utils.CustomUtil;
import com.ssangyong.common.utils.DatasetService;
import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.IPropertyName;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;

/**
 * 변경 리스트 작성 Composite
 * 
 * @author baek
 * 
 */
public class EcoChangeListRegisterComposite extends Composite {
	private Text textBefore;
	private Text textAfter;
	private Text textProject;
	private Text textOspecId;
	private TCComponentItemRevision selectedOspecRevision = null; // 선택된 OSPEC Revision
	private TCComponentItemRevision beforeOspecRevision = null;// 선택된 OSPEC Revisoin의 변경전 Revision
	private TCSession tcSession = null;
	private EcoStatusData selectedRowData = null; // 선택된 기준정보 Row 정보

	private EcoStatusOptionTable afterOSpecTable;
	private EcoStatusOptionTable beforeOSpecTable;

	private EcoStatusOptionTable afterTotalOSpecTable; // 변경후 전체 OSPEC Table
	private EcoStatusOptionTable beforeTotalOSpecTable;// 변경전 전체 OSPEC Table

	@SuppressWarnings("rawtypes")
	private Vector<Vector> onlyAfterData = null; // 변경 후 변경된 OSPEC Table Data
	@SuppressWarnings("rawtypes")
	private Vector<Vector> onlyBeforeData = null; // 변경 전 변경된 OSPEC Table Data

	private JSplitPane splitPane;
	private JPanel leftPanel;
	private JPanel rightPanel;

	protected JPanel afterAllDataPanel;
	protected JPanel beforeAllDataPanel;

	protected JPanel onlyAfterDataPanel;
	protected JPanel onlyBeforeDataPanel;

	private Container contentPane;
	private JRootPane rootPane;
	private Panel embbedTopPanel;

	private EcoStatusManagerDialog mainDialog = null;
	public OSpec afterOspec = null;
	public OSpec beforeOspec = null;

	/**
	 * Create the composite.
	 * 
	 * @param parent
	 * @param style
	 */
	public EcoChangeListRegisterComposite(Composite parent, EcoStatusManagerDialog mainDialog, int style) {
		super(parent, style);
		this.mainDialog = mainDialog;
		tcSession = CustomUtil.getTCSession();
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
		gd_sectionbBasic.widthHint = 829;
		gd_sectionbBasic.minimumWidth = 0;
		sectionbBasic.setLayoutData(gd_sectionbBasic);

		Composite compositeBasic = toolkit.createComposite(sectionbBasic, SWT.NONE);
		compositeBasic.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		sectionbBasic.setClient(compositeBasic);
		GridLayout gl_compositeBasic = new GridLayout(12, false);
		gl_compositeBasic.marginLeft = 30;
		compositeBasic.setLayout(gl_compositeBasic);

		Label lblAfter = new Label(compositeBasic, SWT.NONE);
		lblAfter.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblAfter.setText("변경전");

		textBefore = new Text(compositeBasic, SWT.BORDER);
		GridData gd_textBefore = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_textBefore.widthHint = 120;
		textBefore.setLayoutData(gd_textBefore);
		toolkit.adapt(textBefore, true, true);
		textBefore.setEditable(false);

		Label lblBlank = new Label(compositeBasic, SWT.NONE);
		GridData gd_lblBlank = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_lblBlank.widthHint = 30;
		lblBlank.setLayoutData(gd_lblBlank);
		toolkit.adapt(lblBlank, true, true);

		Label lblBefore = new Label(compositeBasic, SWT.NONE);
		lblBefore.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		toolkit.adapt(lblBefore, true, true);
		lblBefore.setText("변경후");

		textAfter = new Text(compositeBasic, SWT.BORDER);
		GridData gd_textAfter = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_textAfter.widthHint = 120;
		textAfter.setLayoutData(gd_textAfter);
		toolkit.adapt(textAfter, true, true);
		textAfter.setEditable(false);

		Label lblBlank2 = new Label(compositeBasic, SWT.NONE);
		GridData gd_lblBlank2 = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_lblBlank2.widthHint = 30;
		lblBlank2.setLayoutData(gd_lblBlank2);
		toolkit.adapt(lblBlank2, true, true);

		Label lblProject = new Label(compositeBasic, SWT.NONE);
		lblProject.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		toolkit.adapt(lblProject, true, true);
		lblProject.setText("Project");

		textProject = new Text(compositeBasic, SWT.BORDER);
		GridData gd_textProject = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_textProject.widthHint = 60;
		textProject.setLayoutData(gd_textProject);
		toolkit.adapt(textProject, true, true);
		textProject.setEditable(false);

		Label lblBlank3 = new Label(compositeBasic, SWT.NONE);
		GridData gd_lblBlank3 = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_lblBlank3.widthHint = 30;
		lblBlank3.setLayoutData(gd_lblBlank3);
		toolkit.adapt(lblBlank3, true, true);

		Label lblOspecId = new Label(compositeBasic, SWT.NONE);
		lblOspecId.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblOspecId.setText("구분(O/SPEC)");
		toolkit.adapt(lblOspecId, true, true);

		textOspecId = new Text(compositeBasic, SWT.BORDER);
		GridData gd_textOspecId = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_textOspecId.widthHint = 150;
		textOspecId.setLayoutData(gd_textOspecId);
		toolkit.adapt(textOspecId, true, true);
		textOspecId.setEditable(false);

		Composite compositeTopButton = new Composite(compositeBasic, SWT.NONE);
		compositeTopButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1));
		compositeTopButton.setLayout(new GridLayout(2, false));

		Button btnCompare = new Button(compositeTopButton, SWT.NONE);
		btnCompare.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ospecComare();
			}
		});

		btnCompare.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		toolkit.adapt(btnCompare, true, true);
		btnCompare.setText("O/Spec Compare");

		Button btnChangeInformInput = new Button(compositeTopButton, SWT.NONE);
		btnChangeInformInput.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		toolkit.adapt(btnChangeInformInput, true, true);
		btnChangeInformInput.setText("변경 정보 입력");
		btnChangeInformInput.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				openChangeInformDialog();
			}
		});

		Composite compositeCenter = new Composite(this, SWT.BORDER);
		compositeCenter.setLayout(new FillLayout(SWT.HORIZONTAL));
		compositeCenter.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		toolkit.adapt(compositeCenter);
		toolkit.paintBordersFor(compositeCenter);

		Composite composite = new Composite(compositeCenter, SWT.NO_BACKGROUND | SWT.EMBEDDED);
		toolkit.adapt(composite);
		toolkit.paintBordersFor(composite);

		Frame frameEmbedded = SWT_AWT.new_Frame(composite);
		embbedTopPanel = new Panel(new BorderLayout()) {
			private static final long serialVersionUID = 1L;

			public void update(java.awt.Graphics g) {
				/* Do not erase the background */
				paint(g);
			}
		};
		frameEmbedded.add(embbedTopPanel);
		rootPane = new JRootPane();
		rootPane.getContentPane().setBackground(Color.WHITE);
		embbedTopPanel.add(rootPane);
		contentPane = rootPane.getContentPane();

	}

	/**
	 * 초기 Data Load
	 * 
	 * @param selectedRowData
	 */
	public void loadInitData(EcoStatusData selectedRowData) {
		try {
			this.selectedRowData = selectedRowData;
			String projectId = selectedRowData.getProjectId();
			String ospecInform = selectedRowData.getOspecId();

			if (ospecInform.equals(textOspecId.getText()))
				return;
			else {
				initializeUIComponent();
				/**
				 * UI 교체
				 */
				if (rootPane != null) {
					embbedTopPanel.remove(rootPane);
					rootPane = new JRootPane();
					rootPane.getContentPane().setBackground(Color.WHITE);
					embbedTopPanel.add(rootPane);
					contentPane = rootPane.getContentPane();
				}
			}

			textProject.setText(projectId);
			textOspecId.setText(ospecInform);

			String ospecId = "OSI-".concat(projectId);
			String[] ospecArray = ospecInform.split("/");
			String selectedLastOspecId = null; // 선택된 마지막 OSPEC ID
			String selectedFirstOspecId = null; // 선택된 첫번째 OSPEC ID
			int selectedLastOspecIndex = -1; // 선택된 마지막 OSPEC 의 Index
			int selectedFirstOspecIndex = -1; // 선택된 첫번째 OSPEC 의 Index
			if (ospecArray.length > 1) {
				selectedFirstOspecId = ospecId + "-" + ospecInform.split("/")[0].replace("OSI-", "");
				selectedLastOspecId = ospecId + "-" + ospecInform.split("/")[ospecArray.length - 1];
			} else {
				selectedLastOspecId = ospecId + "-" + ospecInform.replace("OSI-", "");
				selectedFirstOspecId = selectedLastOspecId;
			}

			HashMap<Integer, String> ospecRevUidMap = new HashMap<Integer, String>(); // Key: index , Value: OSPEC Rev Puid
			ArrayList<HashMap<String, Object>> ospecRevList = getOspecRevList(ospecId);
			for (int i = 0; i < ospecRevList.size(); i++) {
				HashMap<String, Object> ospecRevValue = ospecRevList.get(i);
				String revPuid = (String) ospecRevValue.get("REV_PUID");
				String osiNo = (String) ospecRevValue.get("OSI_NO");
				ospecRevUidMap.put(i, revPuid);
				if (osiNo.equals(selectedLastOspecId))
					selectedLastOspecIndex = i;
				if (osiNo.equals(selectedFirstOspecId))
					selectedFirstOspecIndex = i;
			}

			// 선택된 OSPEC Revision PUID
			String ospecRevPuid = ospecRevUidMap.get(selectedLastOspecIndex);
			if (ospecRevPuid != null) {
				TCComponent selectedOspecComp = tcSession.stringToComponent(ospecRevPuid);
				if (selectedOspecComp != null) {
					selectedOspecRevision = (TCComponentItemRevision) selectedOspecComp;
					String afterOspec = selectedOspecRevision.getProperty(IPropertyName.ITEM_ID) + "-"
							+ selectedOspecRevision.getProperty(IPropertyName.ITEM_REVISION_ID);
					textAfter.setText(afterOspec);
				} else {
					selectedOspecRevision = null;
					textAfter.setText("");
				}
			} else {
				selectedOspecRevision = null;
				textAfter.setText("");
			}

			// 비교대상 변경전 OSPEC REV PUID
			String beforeLastRevPuid = ospecRevUidMap.get(selectedFirstOspecIndex - 1);
			if (beforeLastRevPuid != null) {
				TCComponent beforeLastRevComp = tcSession.stringToComponent(beforeLastRevPuid);
				if (beforeLastRevComp != null) {
					beforeOspecRevision = (TCComponentItemRevision) beforeLastRevComp;
					String beforeOspec = beforeOspecRevision.getProperty(IPropertyName.ITEM_ID) + "-"
							+ beforeOspecRevision.getProperty(IPropertyName.ITEM_REVISION_ID);
					textBefore.setText(beforeOspec);
				} else {
					beforeOspecRevision = null;
					textBefore.setText("");
				}
			} else {
				beforeOspecRevision = null;
				textBefore.setText("");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * OSPEC 비교
	 */
	private void ospecComare() {

		if (selectedOspecRevision == null)
			return;

		if (beforeOspecRevision == null)
			return;

		initializeUIComponent();
		/**
		 * UI 교체
		 */
		if (rootPane != null) {
			embbedTopPanel.remove(rootPane);
			rootPane = new JRootPane();
			rootPane.getContentPane().setBackground(Color.WHITE);
			embbedTopPanel.add(rootPane);
			contentPane = rootPane.getContentPane();
		}

		OspecCompareOperation operation = new OspecCompareOperation();
		tcSession.queueOperation(operation);

	}

	/**
	 * OSPEC Revision 정보 리스트
	 * 
	 * @param ds
	 * @return
	 * @throws Exception
	 */
	public ArrayList<HashMap<String, Object>> getOspecRevList(String ospecId) throws Exception {
		ArrayList<HashMap<String, Object>> resultList = null;
		DataSet ds = new DataSet();
		ds.put("OSPEC_ID", ospecId);
		CustomECODao dao = new CustomECODao();
		resultList = dao.getOspecRevList(ds);
		return resultList;
	}

	/**
	 * OSPEC 파일을 가져옴
	 * 
	 * @param ospecRev
	 * @return
	 * @throws Exception
	 */
	private File getOspecFile(TCComponentItemRevision ospecRev) throws Exception {
		String ospecStr = ospecRev.getProperty(IPropertyName.ITEM_ID) + "-" + ospecRev.getProperty(IPropertyName.ITEM_REVISION_ID);
		AIFComponentContext[] context = ospecRev.getChildren(SYMCECConstant.ITEM_DATASET_REL);
		for (int i = 0; context != null && i < context.length; i++) {
			TCComponentDataset ds = (TCComponentDataset) context[i].getComponent();
			if (ospecStr.equals(ds.getProperty("object_name"))) {
				File[] files = DatasetService.getFiles(ds);
				return files[0];
			}
		}
		return null;
	}

	private void setAutoScroll(EcoStatusOptionTable source, EcoStatusOptionTable target) {
		final JScrollBar sourceHBar = source.getScroll().getHorizontalScrollBar();
		final JScrollBar targetHBar = target.getScroll().getHorizontalScrollBar();
		final JScrollBar sourceVBar = source.getScroll().getVerticalScrollBar();
		final JScrollBar targetVBar = target.getScroll().getVerticalScrollBar();
		sourceHBar.addAdjustmentListener(new AdjustmentListener() {

			@Override
			public void adjustmentValueChanged(AdjustmentEvent adjustmentevent) {
				targetHBar.setValue(sourceHBar.getValue());
			}
		});

		targetHBar.addAdjustmentListener(new AdjustmentListener() {

			@Override
			public void adjustmentValueChanged(AdjustmentEvent adjustmentevent) {
				sourceHBar.setValue(targetHBar.getValue());
			}
		});

		sourceVBar.addAdjustmentListener(new AdjustmentListener() {

			@Override
			public void adjustmentValueChanged(AdjustmentEvent adjustmentevent) {
				targetVBar.setValue(sourceVBar.getValue());
			}
		});

		targetVBar.addAdjustmentListener(new AdjustmentListener() {

			@Override
			public void adjustmentValueChanged(AdjustmentEvent adjustmentevent) {
				sourceVBar.setValue(targetVBar.getValue());
			}
		});
	}

	/**
	 * 변경 정보 입력
	 */
	protected void openChangeInformDialog() {
		if (afterOSpecTable == null) {
			MessageBox.post(EcoChangeListRegisterComposite.this.getShell(), "O/Spec Compare 된 Datat가 존재하지 않습니다.", "Warning", MessageBox.WARNING);
			return;
		}
		HashMap<String, ArrayList<String>> afterOspecDataMap = afterOSpecTable.getSimpleDataMap();
		if (afterOspecDataMap == null) {
			MessageBox.post(EcoChangeListRegisterComposite.this.getShell(), "O/Spec Compare 된 Datat가 존재하지 않습니다.", "Warning", MessageBox.WARNING);
			return;
		}

		MultiSpanCellTable afterFixedOspecTable = afterOSpecTable.getFixedOspecViewTable();
		MultiSpanCellTable beforefixedOspecTable = beforeOSpecTable.getFixedOspecViewTable();
		ArrayList<String> categoryList = new ArrayList<String>(); // Category 리스트

		for (int row = 0; afterFixedOspecTable != null && row < afterFixedOspecTable.getRowCount(); row++) {
			String opCode = (String) afterFixedOspecTable.getValueAt(row, 3);
			String category = OpUtil.getCategory(opCode);
			if (categoryList.contains(category))
				continue;
			categoryList.add(category);
		}

		for (int row = 0; beforefixedOspecTable != null && row < beforefixedOspecTable.getRowCount(); row++) {
			String opCode = (String) beforefixedOspecTable.getValueAt(row, 3);
			String category = OpUtil.getCategory(opCode);
			if (categoryList.contains(category))
				continue;
			categoryList.add(category);
		}

		Collections.sort(categoryList);

		EcoStatusChangeInformInputDialog dialog = new EcoStatusChangeInformInputDialog(categoryList, selectedRowData,
				EcoChangeListRegisterComposite.this.getShell(), mainDialog);
		dialog.setCompareData(afterTotalOSpecTable, beforeTotalOSpecTable, onlyAfterData, onlyBeforeData);
		dialog.open();
	}

	/**
	 * UI Component 초기화
	 */
	private void initializeUIComponent() {
		contentPane = null;
		afterOSpecTable = null;
		beforeOSpecTable = null;
		afterTotalOSpecTable = null;
		beforeTotalOSpecTable = null;
		splitPane = null;
		leftPanel = null;
		rightPanel = null;
		onlyAfterDataPanel = null;
		onlyBeforeDataPanel = null;
		afterAllDataPanel = null;
		beforeAllDataPanel = null;
	}

	/**
	 * OSPEC 비교. 변경된것만 나타나도록 함
	 */
	public class OspecCompareOperation extends AbstractAIFOperation {
		private WaitProgressBar waitProgress;

		@SuppressWarnings("rawtypes")
		@Override
		public void executeOperation() throws Exception {
			try {
				waitProgress = new WaitProgressBar(AIFUtility.getActiveDesktop());
				waitProgress.start();
				waitProgress.setWindowSize(300, 200);
				waitProgress.setStatus("Comparing O/Spec...");

				File afterOspecFile = getOspecFile(selectedOspecRevision);
				File beforeOspecFile = getOspecFile(beforeOspecRevision);

				if (afterOspecFile != null)
					afterOspec = OpUtil.getOSpec(afterOspecFile);
				if (beforeOspecFile != null)
					beforeOspec = OpUtil.getOSpec(beforeOspecFile);

				if (afterOspecFile.exists())
					afterOspecFile.delete();
				if (beforeOspecFile.exists())
					beforeOspecFile.delete();

				afterTotalOSpecTable = new EcoStatusOptionTable(afterOspec, null); // 변경후 전체 OSPEC Table
				beforeTotalOSpecTable = new EcoStatusOptionTable(beforeOspec, null); // 변경전 전체 OSPEC Table

				Vector<Vector> beforeOrginData = beforeTotalOSpecTable.getData(); // 변경후 전체 OSPEC Table Data
				Vector<Vector> afterOrginData = afterTotalOSpecTable.getData(); // 변경후 전체 OSPEC Table Data

				onlyAfterData = new Vector<Vector>(); // 변경후 변경된 OSPEC Table Data
				onlyBeforeData = new Vector<Vector>(); // 변경전 변경된 OSPEC Table Data
				onlyAfterData = afterTotalOSpecTable.minus(beforeTotalOSpecTable.getData(), false);
				onlyBeforeData = beforeTotalOSpecTable.minus(afterTotalOSpecTable.getData(), false);

				// 유효한 Category 리스트
				ArrayList<String> validCategoryList = new ArrayList<String>();

				for (Vector rowVec : onlyAfterData) {
					String optionValue = (String) rowVec.get(3);
					String category = OpUtil.getCategory(optionValue);
					if (!validCategoryList.contains(category))
						validCategoryList.add(category);
				}
				for (Vector rowVec : onlyBeforeData) {
					String optionValue = (String) rowVec.get(3);
					String category = OpUtil.getCategory(optionValue);
					if (!validCategoryList.contains(category))
						validCategoryList.add(category);
				}

				// 변경된 Option 이 포함된 Category 에 해당하는 Option 으로 OSPEC Table 를 구성함
				afterOSpecTable = new EcoStatusOptionTable(afterOspec, null, validCategoryList);
				beforeOSpecTable = new EcoStatusOptionTable(beforeOspec, null, validCategoryList);

				afterAllDataPanel = afterOSpecTable.getOspecTable();
				beforeAllDataPanel = beforeOSpecTable.getOspecTable();

				// 전체 OSPEC Data 를 저장함, 이 저장된 정보를 가지고 변경여부를 확인하여 Table 에 붉은색 표시를 해준다.
				HashMap<String, ArrayList<String>> afterDataMap = EcoStatusOptionTable.getSimpleDataMap(afterOrginData, afterOSpecTable.getHeader());
				HashMap<String, ArrayList<String>> beforeDataMap = EcoStatusOptionTable.getSimpleDataMap(beforeOrginData, beforeOSpecTable.getHeader());

				afterOSpecTable.setSimpleDataMap(beforeDataMap);
				beforeOSpecTable.setSimpleDataMap(afterDataMap);

				JPanel contentPanel = new JPanel();
				contentPane.setLayout(new BorderLayout());
				contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
				contentPane.add(contentPanel, BorderLayout.CENTER);

				contentPanel.setLayout(new BorderLayout(0, 0));
				{
					splitPane = new JSplitPane();
					splitPane.setPreferredSize(new Dimension(1500, 800));

					leftPanel = new JPanel();
					leftPanel.setBorder(new TitledBorder(null, "변경전", TitledBorder.LEADING, TitledBorder.TOP, null, null));
					leftPanel.setLayout(new BorderLayout(0, 0));
					leftPanel.add(beforeAllDataPanel, BorderLayout.CENTER);
					splitPane.setLeftComponent(leftPanel);

					rightPanel = new JPanel();
					rightPanel.setBorder(new TitledBorder(null, "변경후", TitledBorder.LEADING, TitledBorder.TOP, null, null));
					rightPanel.setLayout(new BorderLayout(0, 0));
					rightPanel.add(afterAllDataPanel, BorderLayout.CENTER);
					splitPane.setRightComponent(rightPanel);

					setAutoScroll(afterOSpecTable, beforeOSpecTable);

					splitPane.setOneTouchExpandable(true);
					splitPane.setDividerSize(10);
					contentPanel.add(splitPane);
					splitPane.setDividerLocation(660);

				}
				embbedTopPanel.validate();
				embbedTopPanel.revalidate();
				embbedTopPanel.repaint();

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
	}
}

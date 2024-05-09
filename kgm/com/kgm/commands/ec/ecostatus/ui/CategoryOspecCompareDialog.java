package com.kgm.commands.ec.ecostatus.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Panel;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollBar;
import javax.swing.JSplitPane;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
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
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.wb.swt.SWTResourceManager;

import com.kgm.commands.ec.ecostatus.operation.OspecCompareExportOperation;
import com.kgm.commands.ospec.op.OpUtil;
import com.kgm.common.WaitProgressBar;
import com.kgm.common.utils.CustomUtil;
import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.ConfirmDialog;
import com.teamcenter.rac.util.MessageBox;

/**
 * O/Spec Compare Category 별로 OSPEC Compare 확인 Dialog
 * 
 * @author baek
 * 
 */
public class CategoryOspecCompareDialog extends Dialog {

	private Container contentPane;
	private String selectedCategory = null; // 선택된 카테고리
	private Panel embbedTopPanel = null;
	private TCSession tcSession = null;

	private EcoStatusOptionTable afterTotalOSpecTable = null;
	private EcoStatusOptionTable beforeTotalOSpecTable = null;
	@SuppressWarnings("rawtypes")
	private Vector<Vector> onlyAfterData = null;
	@SuppressWarnings("rawtypes")
	private Vector<Vector> onlyBeforeData = null;
	private EcoStatusOptionTable afterOSpecTable = null;
	private EcoStatusOptionTable beforeOSpecTable = null;
	private HashMap<String, ArrayList<String>> afterDataMap = null;
	private HashMap<String, ArrayList<String>> beforeDataMap = null;

	/**
	 * 
	 * @param selectedCategory
	 *            선택된 Category
	 * @param afterTotalOSpecTable
	 *            변경후 전체 OSPEC Table
	 * @param beforeTotalOSpecTable
	 *            변경전 전체 OSPEC Table
	 * @param onlyAfterData
	 *            변경후 변경된 Data
	 * @param onlyBeforeData
	 *            변경전 변경된 Data
	 * @param parentShell
	 */
	@SuppressWarnings("rawtypes")
	public CategoryOspecCompareDialog(String selectedCategory, EcoStatusOptionTable afterTotalOSpecTable, EcoStatusOptionTable beforeTotalOSpecTable,
			Vector<Vector> onlyAfterData, Vector<Vector> onlyBeforeData, Shell parentShell) {
		super(parentShell);
		setShellStyle(SWT.DIALOG_TRIM | SWT.MIN | SWT.RESIZE);
		this.selectedCategory = selectedCategory;
		this.afterTotalOSpecTable = afterTotalOSpecTable;
		this.beforeTotalOSpecTable = beforeTotalOSpecTable;
		this.onlyAfterData = onlyAfterData;
		this.onlyBeforeData = onlyBeforeData;
		tcSession = CustomUtil.getTCSession();
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("O/Spec Compare");
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
		sectionbBasic.setText("OSpec Category: " + (selectedCategory == null ? "ALL" : selectedCategory));

		Composite compositeCenter = toolkit.createComposite(sectionbBasic, SWT.WRAP | SWT.BORDER | SWT.EMBEDDED);
		compositeCenter.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		sectionbBasic.setClient(compositeCenter);
		compositeCenter.setLayout(new FillLayout(SWT.HORIZONTAL));

		Frame frameEmbedded = SWT_AWT.new_Frame(compositeCenter);
		embbedTopPanel = new Panel(new BorderLayout()) {
			private static final long serialVersionUID = 1L;

			public void update(java.awt.Graphics g) {
				/* Do not erase the background */
				paint(g);
			}
		};
		frameEmbedded.add(embbedTopPanel);
		JRootPane rootPane = new JRootPane();
		rootPane.getContentPane().setBackground(Color.WHITE);
		embbedTopPanel.add(rootPane);
		contentPane = rootPane.getContentPane();

		OspecCompareOperation operation = new OspecCompareOperation();
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
		Button btnExport = createButton(parent, 100, "Export", false);
		btnExport.setImage(SWTResourceManager.getImage(CategoryOspecCompareDialog.class, "/com/ssangyong/common/images/export_16.png"));
		btnExport.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				doExport();
			}
		});

		createButton(parent, IDialogConstants.CANCEL_ID, "Close", false);
	}

	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(1200, 500);
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

				ArrayList<String> validCategoryList = new ArrayList<String>();
				// 유효한 변경후 Category 리스트
				// ArrayList<String> afterValidCategoryList = new ArrayList<String>();
				for (Vector rowVec : onlyAfterData) {
					String optionValue = (String) rowVec.get(3);
					String category = OpUtil.getCategory(optionValue);

					if (selectedCategory == null) {
						// afterValidCategoryList.add(category);
						validCategoryList.add(category);
					} else {
						if (category.equals(selectedCategory))
							validCategoryList.add(category);
						// afterValidCategoryList.add(category);

					}
				}
				// 유효한 변경전 Category 리스트
				// ArrayList<String> beforeValidCategoryList = new ArrayList<String>();
				for (Vector rowVec : onlyBeforeData) {
					String optionValue = (String) rowVec.get(3);
					String category = OpUtil.getCategory(optionValue);
					if (selectedCategory == null) {
						validCategoryList.add(category);
						// beforeValidCategoryList.add(category);
					} else {
						if (category.equals(selectedCategory))
							validCategoryList.add(category);
						// beforeValidCategoryList.add(category);
					}
				}

				// 변경된 Option 이 포함된 Category 에 해당하는 Option 으로 OSPEC Table 를 구성함
				afterOSpecTable = new EcoStatusOptionTable(afterTotalOSpecTable.getOspec(), null, validCategoryList);
				beforeOSpecTable = new EcoStatusOptionTable(beforeTotalOSpecTable.getOspec(), null, validCategoryList);

				// 전체 OSPEC Data 를 저장함, 이 저장된 정보를 가지고 변경여부를 확인하여 Table 에 붉은색 표시를 해준다.
				afterDataMap = EcoStatusOptionTable.getSimpleDataMap(afterTotalOSpecTable.getData(), afterTotalOSpecTable.getHeader());
				beforeDataMap = EcoStatusOptionTable.getSimpleDataMap(beforeTotalOSpecTable.getData(), beforeTotalOSpecTable.getHeader());

				afterOSpecTable.setSimpleDataMap(beforeDataMap);
				beforeOSpecTable.setSimpleDataMap(afterDataMap);

				JPanel onlyAfterDataPanel = afterOSpecTable.getOspecTable();
				JPanel onlyBeforeDataPanel = beforeOSpecTable.getOspecTable();

				JPanel contentPanel = new JPanel();
				contentPane.setLayout(new BorderLayout());
				contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
				contentPane.add(contentPanel, BorderLayout.CENTER);

				contentPanel.setLayout(new BorderLayout(0, 0));
				{
					JSplitPane splitPane = new JSplitPane();
					splitPane.setPreferredSize(new Dimension(700, 800));

					JPanel leftPanel = new JPanel();
					leftPanel.setBorder(new TitledBorder(null, "변경전", TitledBorder.LEADING, TitledBorder.TOP, null, null));
					leftPanel.setLayout(new BorderLayout(0, 0));
					leftPanel.add(onlyBeforeDataPanel, BorderLayout.CENTER);
					splitPane.setLeftComponent(leftPanel);

					JPanel rightPanel = new JPanel();
					rightPanel.setBorder(new TitledBorder(null, "변경후", TitledBorder.LEADING, TitledBorder.TOP, null, null));
					rightPanel.setLayout(new BorderLayout(0, 0));
					rightPanel.add(onlyAfterDataPanel, BorderLayout.CENTER);
					splitPane.setRightComponent(rightPanel);

					setAutoScroll(afterOSpecTable, beforeOSpecTable);

					splitPane.setOneTouchExpandable(true);
					splitPane.setDividerSize(10);
					contentPanel.add(splitPane);
					splitPane.setDividerLocation(567);

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
	 * Excel 출력
	 */
	private void doExport() {
		try {
			File selectFile = selectFile();
			OspecCompareExportOperation operation = new OspecCompareExportOperation(beforeOSpecTable, afterOSpecTable, selectFile, beforeDataMap, afterDataMap);
			tcSession.queueOperation(operation);
		} catch (Exception ex) {
			MessageBox.post(this.getShell(), ex.toString(), "Error", MessageBox.ERROR);
		}

	}

	/**
	 * 파일선택
	 * 
	 * @return
	 * @throws Exception
	 */
	private File selectFile() throws Exception {
		File selectedFile = null;
		FileDialog fDialog = new FileDialog(this.getShell(), SWT.SINGLE | SWT.SAVE);

		SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmm");
		Date toDay = new Date();
		String fileName = "OSPEC_COMPARE_" + df.format(toDay);

		fDialog.setFilterNames(new String[] { "Excel File" });
		fDialog.setFileName(fileName);

		fDialog.setFilterExtensions(new String[] { "*.xls" });
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
		if (checkFile.exists())
			checkFile.delete();
		selectedFile = new File(strDownLoadFilePath);
		return selectedFile;
	}

}

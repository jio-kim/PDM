package com.ssangyong.commands.namegroup;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.TitledBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import jxl.Cell;
import jxl.CellView;
import jxl.Workbook;
import jxl.format.Alignment;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.Colour;
import jxl.format.UnderlineStyle;
import jxl.format.VerticalAlignment;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.ssangyong.commands.namegroup.dao.PartNameGroupDao;
import com.ssangyong.commands.namegroup.model.NameGroupCountResult;
import com.ssangyong.commands.namegroup.model.PngCondition;
import com.ssangyong.commands.namegroup.model.PngMaster;
import com.ssangyong.commands.namegroup.model.SpecHeader;
import com.ssangyong.commands.namegroup.model.SpecOptionChangeInfo;
import com.ssangyong.commands.ospec.OSpecImportDlg;
import com.ssangyong.common.WaitProgressBar;
import com.ssangyong.common.remote.DataSet;
import com.ssangyong.common.remote.SYMCRemoteUtil;
import com.ssangyong.common.utils.CustomUtil;
import com.ssangyong.common.utils.SYMTcUtil;
import com.teamcenter.rac.aif.AIFShell;
import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.aif.InterfaceAIFOperationListener;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;

public class PngVerificationPanel extends JPanel {

	private PngDlg parentDlg;
	private JComboBox cbProduct;
	private JTable specTable = null;
	private final static String SELECTED_SPEC = "Selected Spec";
	private PngVerificationTablePanel verificationTablePanel = null, verificationErrorTablePanel = null;
	private JPanel resultPanel = null;
	// private HashMap<String, HashMap<String ,String>> resultData = new HashMap();
	private HashMap<String, HashMap<String, NameGroupCountResult>> resultGroupData = new HashMap();
	private JLabel lbTargetProd = null;
	private int endThreadCount = 0;
	private JCheckBox cbOnlyError = null;
	HashMap<String, ArrayList<HashMap<String, Object>>> verificationResult = new HashMap();
	HashMap<String, ArrayList<HashMap<String, Object>>> specEndItemResult = new HashMap();
	private JCheckBox chckbxVerifyAndExport = null; //Verify 후 에 Export 할 지 유무 체크
	private File selectedVerifyExportFile = null; // Verify시 선택된 Export 파일

	/**
	 * Create the panel.
	 * 
	 * @throws Exception
	 */
	public PngVerificationPanel(PngDlg parentDlg) throws Exception {
		this.parentDlg = parentDlg;
		init();
	}

	public void refreshProductList() throws Exception {
		cbProduct.removeAllItems();
		Vector<String> list = parentDlg.getProductHeader();
		for (String productID : list) {
			cbProduct.addItem(productID);
		}

		cbProduct.setSelectedItem(lbTargetProd.getText());
	}

	private void init() throws Exception {
		setLayout(new BorderLayout(0, 0));

		JPanel panel = new JPanel();
		add(panel, BorderLayout.WEST);
		panel.setLayout(new BorderLayout(0, 0));

		JPanel panel_2 = new JPanel();
		panel.add(panel_2, BorderLayout.NORTH);
		panel_2.setLayout(new BorderLayout(0, 0));

		JPanel panel_1 = new JPanel();
		FlowLayout flowLayout_3 = (FlowLayout) panel_1.getLayout();
		flowLayout_3.setAlignment(FlowLayout.LEADING);
		panel_2.add(panel_1, BorderLayout.CENTER);

		cbProduct = new JComboBox();
		cbProduct.setModel(new DefaultComboBoxModel(new String[] { PngDlg.SELECT_PRODUCT }));
		Vector<String> list = parentDlg.getProductHeader();
		for (String productID : list) {
			cbProduct.addItem(productID);
		}
		panel_1.add(cbProduct);

		JPanel panel_4 = new JPanel();

		// [SR160412-013][20160414][jclee] Spec Excel Export
		Registry registry = Registry.getRegistry("/com/ssangyong/common/common");

		JButton btnSpecExport = new JButton("");
		btnSpecExport.setIcon(new ImageIcon(PngVerificationPanel.class.getResource("/com/ssangyong/common/images/export_16.png")));
		btnSpecExport.setToolTipText("Excel Export");
		btnSpecExport.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionevent) {
				exportSpec();
			}
		});
		btnSpecExport.setPreferredSize(new Dimension(38, 25));
		panel_4.add(btnSpecExport);

		// [SR160412-013][20160414][jclee] Spec Excel Import
		JButton btnSpecImport = new JButton("");
		btnSpecImport.setIcon(new ImageIcon(PngVerificationPanel.class.getResource("/com/ssangyong/common/images/import_16.png")));
		btnSpecImport.setToolTipText("Excel Import");
		btnSpecImport.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionevent) {
				importSpec();
			}
		});
		btnSpecImport.setPreferredSize(new Dimension(38, 25));
		panel_4.add(btnSpecImport);
		
		/**
		 * Template 파일 다운로드
		 */
		JButton btnDownloadTemplate = new JButton("");
		btnDownloadTemplate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent paramActionEvent) {
				doDownloadTemplate();
			}
		});
		btnDownloadTemplate.setIcon(new ImageIcon(OSpecImportDlg.class.getResource("/icons/templates_16.png")));
		btnDownloadTemplate.setPreferredSize(new Dimension(38, 25));
		btnDownloadTemplate.setToolTipText("Template Download");
		panel_4.add(btnDownloadTemplate);

		JButton btnAdd = new JButton("");
		btnAdd.setIcon(new ImageIcon(PngVerificationPanel.class.getResource("/com/teamcenter/rac/aif/images/add_16.png")));
		btnAdd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionevent) {
				if (cbProduct.getSelectedItem().equals(PngDlg.SELECT_PRODUCT)) {
					MessageBox.post(parentDlg, PngDlg.SELECT_PRODUCT, "Information", MessageBox.INFORMATION);
					return;
				}
				PngSpecFindDlg findDlg = new PngSpecFindDlg(parentDlg, (String) cbProduct.getSelectedItem());
				findDlg.showDialog();
			}
		});
		btnAdd.setPreferredSize(new Dimension(38, 25));

		panel_4.add(btnAdd);
		JButton btnRemove = new JButton("");
		btnRemove.setIcon(new ImageIcon(PngVerificationPanel.class.getResource("/com/teamcenter/rac/aif/images/remove_16.png")));
		btnRemove.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionevent) {
				parentDlg.removeSelectedRows(specTable);
			}
		});
		btnRemove.setPreferredSize(new Dimension(38, 25));
		panel_4.add(btnRemove);
		panel_2.add(panel_4, BorderLayout.EAST);

		final JPanel selectedSpecPanel = new JPanel();
		selectedSpecPanel.setBorder(new TitledBorder(null, SELECTED_SPEC, TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel.add(selectedSpecPanel, BorderLayout.CENTER);
		selectedSpecPanel.setLayout(new BorderLayout(0, 0));

		JPanel panel_7 = new JPanel();
		selectedSpecPanel.add(panel_7, BorderLayout.CENTER);
		panel_7.setLayout(new BorderLayout(0, 0));

		Vector<String> headerVec = new Vector();
		headerVec.add("Spec No");
		headerVec.add("Type");
		headerVec.add("Create Date");
		headerVec.add("PUID");

		DefaultTableModel specModel = new DefaultTableModel(null, headerVec);
		specModel.addTableModelListener(new TableModelListener() {

			@Override
			public void tableChanged(TableModelEvent tablemodelevent) {
				TitledBorder border = (TitledBorder) selectedSpecPanel.getBorder();
				border.setTitle(SELECTED_SPEC + "(" + specTable.getRowCount() + ")");
				selectedSpecPanel.repaint();
			}
		});
		specTable = new JTable(specModel);
		specTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(specModel);
		specTable.setRowSorter(sorter);

		TableColumnModel tcm = specTable.getColumnModel();
		tcm.removeColumn(tcm.getColumn(3));
		int width[] = { 200, 70, 150 };
		for (int i = 0; i < tcm.getColumnCount(); i++) {
			tcm.getColumn(i).setPreferredWidth(width[i]);
		}
		JScrollPane scrollPane = new JScrollPane(specTable);
		scrollPane.setPreferredSize(new Dimension(300, 402));
		panel_7.add(scrollPane);

		JPanel panel_8 = new JPanel();
		FlowLayout flowLayout_2 = (FlowLayout) panel_8.getLayout();
		flowLayout_2.setAlignment(FlowLayout.TRAILING);
		selectedSpecPanel.add(panel_8, BorderLayout.SOUTH);

		JPanel panel_3 = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel_3.getLayout();
		flowLayout.setAlignment(FlowLayout.TRAILING);
		panel.add(panel_3, BorderLayout.SOUTH);

		/**
		 * [SR150421-010][2015.06.03][jclee] 선택한 Spec End Item List Excel Export 추가
		 * [SR181211-012][CSH] 1레벨 아이템 전체 검토(P+, C+와 같이 1레벨의 모든 내용 검토) 이보현책임
		 */
		JButton btnSpecEndItemExcelExport = new JButton("1 Level Item List Export");
		btnSpecEndItemExcelExport.setIcon(new ImageIcon(PngVerificationPanel.class.getResource("/com/ssangyong/common/images/excel_16.png")));
		btnSpecEndItemExcelExport.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionevent) {
				final WaitProgressBar waitProgress = new WaitProgressBar(parentDlg);
				try {
					waitProgress.start();
					waitProgress.setStatus("Loading....");

					TCSession session = CustomUtil.getTCSession();
					specEndItemResult.clear();
					endThreadCount = 0;
					final SpecEndItemExcelExportOperation op = new SpecEndItemExcelExportOperation(waitProgress);
					op.addOperationListener(new InterfaceAIFOperationListener() {
						@Override
						public void startOperation(String arg0) {

						}

						@Override
						public void endOperation() {
							if (!op.isExceptionFired) {
								waitProgress.dispose();
							}
						}
					});
					session.queueOperationLater(op);
				} catch (Exception e) {
					e.printStackTrace();
					waitProgress.setStatus(e.getMessage());
					waitProgress.setShowButton(true);
					MessageBox.post(parentDlg, e.getMessage(), "ERROR", MessageBox.ERROR);
				}
			}
		});
		panel_3.add(btnSpecEndItemExcelExport);

		JButton btnVerify = new JButton("Verify");
		btnVerify.setIcon(new ImageIcon(PngVerificationPanel.class.getResource("/com/ssangyong/common/images/ok_16.png")));
		btnVerify.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionevent) {
				
				/**
				 * Verify and Export 가 체크가 되어 있으면 
				 */
				selectedVerifyExportFile = null;
				if(chckbxVerifyAndExport.isSelected())
				{
					selectedVerifyExportFile = selectVerifyExportFile();
					if(selectedVerifyExportFile ==null)
					{
						MessageBox.post(parentDlg, "'Verify and Export' was checked\nYou have to select file location to export", "ERROR", MessageBox.ERROR);
						return;
					}
				}

				final WaitProgressBar waitProgress = new WaitProgressBar(parentDlg);
				try {
					waitProgress.start();
					waitProgress.setStatus("Loading....");

					TCSession session = CustomUtil.getTCSession();
					final VerifyOperation op = new VerifyOperation(waitProgress);
					op.addOperationListener(new InterfaceAIFOperationListener() {

						@Override
						public void startOperation(String arg0) {

						}

						@Override
						public void endOperation() {
							if (!op.isExceptionFired) {
								waitProgress.dispose();
							}
						}
					});
					session.queueOperationLater(op);
				} catch (Exception e) {
					e.printStackTrace();
					waitProgress.setStatus(e.getMessage());
					waitProgress.setShowButton(true);
					MessageBox.post(parentDlg, e.getMessage(), "ERROR", MessageBox.ERROR);
				}
			}
		});
		panel_3.add(btnVerify);

		resultPanel = new JPanel();
		resultPanel.setBorder(new TitledBorder(null, "Verification Result", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		add(resultPanel, BorderLayout.CENTER);
		resultPanel.setLayout(new BorderLayout(0, 0));

		JPanel panel_5 = new JPanel();
		resultPanel.add(panel_5, BorderLayout.NORTH);
		panel_5.setLayout(new BorderLayout(0, 0));

		JPanel panel_9 = new JPanel();
		FlowLayout flowLayout_4 = (FlowLayout) panel_9.getLayout();
		flowLayout_4.setAlignment(FlowLayout.LEADING);
		panel_5.add(panel_9, BorderLayout.CENTER);

		JLabel lblNewLabel = new JLabel("Target Product : ");
		panel_9.add(lblNewLabel);

		lbTargetProd = new JLabel(".");
		panel_9.add(lbTargetProd);
		lbTargetProd.setFont(new Font("굴림", Font.BOLD, 12));

		JPanel panel_10 = new JPanel();
		panel_5.add(panel_10, BorderLayout.EAST);

		cbOnlyError = new JCheckBox("Only Error Data");
		cbOnlyError.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionevent) {
				if (cbOnlyError.isSelected()) {
					resultPanel.remove(verificationTablePanel);
					resultPanel.add(verificationErrorTablePanel, BorderLayout.CENTER);
					resultPanel.revalidate();
				} else {
					resultPanel.remove(verificationErrorTablePanel);
					resultPanel.add(verificationTablePanel, BorderLayout.CENTER);
					resultPanel.revalidate();
				}
			}
		});
		
		chckbxVerifyAndExport  = new JCheckBox("Verify and Export");
		panel_10.add(chckbxVerifyAndExport);
		panel_10.add(cbOnlyError);

		verificationTablePanel = new PngVerificationTablePanel(parentDlg, null, null);
		resultPanel.add(verificationTablePanel, BorderLayout.CENTER);

		JPanel panel_6 = new JPanel();
		FlowLayout flowLayout_1 = (FlowLayout) panel_6.getLayout();
		flowLayout_1.setAlignment(FlowLayout.TRAILING);
		resultPanel.add(panel_6, BorderLayout.SOUTH);

		JButton btnExport = new JButton("Export");
		btnExport.setIcon(new ImageIcon(PngVerificationPanel.class.getResource("/com/ssangyong/common/images/excel_16.png")));
		btnExport.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionevent) {
				if (!lbTargetProd.getText().equals(".")) {
					exportToExcel();
				}
			}
		});
		panel_6.add(btnExport);
	}

	public void addSpec(Vector rowData) {
		DefaultTableModel model = (DefaultTableModel) specTable.getModel();
		if (!model.getDataVector().contains(rowData)) {
			model.addRow(rowData);
		}
	}

	private void verify(WaitProgressBar waitProgress) throws Exception {
//		System.out.println("Start Time : "+ new Date());
		endThreadCount = 0;
		parentDlg.specMap.clear();
		resultGroupData.clear();
		verificationResult.clear();

		boolean isOnlyErrorExport = false; //에러된 것만 Export 되는지 유무
		/* [NoSR][20160516][jclee] Name Verify 후 UI 오류 해결
			: Error 발생 원인
			 - 1차 Name Verify
			 - Only Error Data Check
			 - Only Error Data를 Check 해제하지 않고 2차 Name Verify
			 - 대부분 정상 완료되지만 UI Thread 오류로 인해 Table 영역 클릭 시 이전 결과를 갖고있는 Table이 Click Focus를 가져가버리는 문제 발생
			=> Verify 버튼 클릭 시 자동으로 Only Error Data를 체크해제하고 All Data Table을 교체한 후 Verify를 수행하도록 변경.
		 */
		if (cbOnlyError.isSelected()) {
			try {
				resultPanel.remove(verificationErrorTablePanel);
				resultPanel.add(verificationTablePanel, BorderLayout.CENTER);
				resultPanel.revalidate();
			} catch (Exception e) {
				/* Verify를 한번도 수행하지 않은 상태에서 Only Error Data를 체크한 후 Verify를 수행할 경우
					- 상기 resultPanel.remove(verificationErrorTablePanel) 수행 시 resultPanel에 아직 verificationErrorTablePanel이 포함되어있지 않으므로 NullPointerException 발생
					- 해당 Exception에 대해 하기 내용 Print 후 Bypass.
				 */
				System.out.println("verificationErrorTablePanel is not contained in resultPanel.");
			}
			isOnlyErrorExport = true;
		}
		cbOnlyError.setSelected(false);

		String product = (String) cbProduct.getSelectedItem();
		lbTargetProd.setText(product);
		
//		String rowKey = "";
		String rowKey = parentDlg.getRowkey();
//		System.out.println("1Level Item List 조회 Start : "+ new Date());
		waitProgress.setStatus(product + " 1Level Item List 조회 Start...");
		set1LevelItemList(product, rowKey);
		waitProgress.setStatus(product + " 1Level Item List 조회 End...");
		
		Vector<Vector> customData = new Vector();
		Vector<SpecHeader> specHeaderList = new Vector();
		DefaultTableModel model = (DefaultTableModel) specTable.getModel();

		ExecutorService executor = Executors.newFixedThreadPool(5);

		for (int i = 0; i < specTable.getRowCount(); i++) {
			int modelRowIdx = specTable.convertRowIndexToModel(i);
			String spec = (String) model.getValueAt(modelRowIdx, 0);
			String type = (String) model.getValueAt(modelRowIdx, 1);
			String puid = (String) model.getValueAt(modelRowIdx, 3);

			SpecHeader header = new SpecHeader(spec, puid);
			specHeaderList.add(header);
			// specKeyHeader.add(spec + "_" + puid);
			// verifyHeader.add(spec);

			BomExpander expander = new BomExpander(waitProgress, spec, product, type, puid, rowKey);
			executor.execute(expander);
		}
		executor.shutdown();
		while (!executor.isTerminated()) {
		}

//		deletePngEpl(rowKey);
		
		Vector<Vector> data = getData(specHeaderList);
		Vector<Vector> errorData = getErrorData(data);
		verificationErrorTablePanel = new PngVerificationTablePanel(parentDlg, errorData, specHeaderList);
		PngVerificationTablePanel newVerificationTablePanel = new PngVerificationTablePanel(parentDlg, data, specHeaderList);
		resultPanel.remove(verificationTablePanel);
		resultPanel.add(newVerificationTablePanel, BorderLayout.CENTER);
		verificationTablePanel = newVerificationTablePanel;
		resultPanel.revalidate();
		
		/**
		 * Verify and Export 가 선택되었으면 Verify 후에 Export를 수행한다.
		 */
		if(chckbxVerifyAndExport.isSelected() && selectedVerifyExportFile !=null)
		{
			exportXSSF(selectedVerifyExportFile, isOnlyErrorExport);
			selectedVerifyExportFile = null;
		}
			isOnlyErrorExport = false;
			
			
			
//			System.out.println("End Time : "+ new Date());
	}

	private Vector<Vector> getErrorData(Vector<Vector> data) {
		Vector errorData = new Vector() {
			@Override
			public synchronized Object clone() {
				Vector cloneData = new Vector();
				for (int i = 0; i < this.elementCount; i++) {
					Vector row = new Vector();
					Vector source = (Vector) this.elementData[i];
					row.addAll(source);
					cloneData.add(row);
				}
				return cloneData;
			}
		};

		for (Vector row : data) {
			for (Object obj : row) {
				if (obj instanceof NameGroupCountResult) {
					NameGroupCountResult result = (NameGroupCountResult) obj;
					if (!result.isValid()) {
						errorData.add(row);
						break;
					}
				}
			}
		}

		return errorData;
	}

	private Vector<Vector> getData(Vector<SpecHeader> specKeyHeader) throws Exception {
		Vector data = new Vector() {
			@Override
			public synchronized Object clone() {
				Vector cloneData = new Vector();
				for (int i = 0; i < this.elementCount; i++) {
					Vector row = new Vector();
					Vector source = (Vector) this.elementData[i];
					row.addAll(source);
					cloneData.add(row);
				}
				return cloneData;
			}
		};

		String currentProduct = (String) cbProduct.getSelectedItem();

		ArrayList<HashMap<String, Object>> list = parentDlg.getPngList(currentProduct, null, "1", false);

		for (int i = 0; list != null && i < list.size(); i++) {
			HashMap<String, Object> map = list.get(i);
			Vector row = new Vector();
			String groupID = (String) map.get("GROUP_ID");
			String groupName = (String) map.get("GROUP_NAME");
			BigDecimal defaultQtyObj = (BigDecimal) map.get("DEFAULT_QTY");
			String defaultCnt = "" + defaultQtyObj.intValue();

			// if( groupID.equals("PG4001")){
			// System.out.println("alsdkfj");
			// }

			PngMaster pngMaster = parentDlg.getPngDetail(groupID);

			row.add(pngMaster);
			row.add(groupName);
			row.add(defaultCnt);

			for (int j = 0; specKeyHeader != null && j < specKeyHeader.size(); j++) {

				String specKey = specKeyHeader.get(j).getKey();

				NameGroupCountResult ngResult = null;
				HashMap<String, NameGroupCountResult> resultMap = resultGroupData.get(groupID);
				if (resultMap == null) {
					ngResult = new NameGroupCountResult();
				} else {
					ngResult = resultMap.get(specKey);
					if (ngResult == null) {
						ngResult = new NameGroupCountResult();
					}
				}
				int qty = ngResult.getTotCount();
				
				// Name Group 에 설정된 Condition 에 속하고 갯수가 일치하는지 유무
				boolean isPartExistInValidCondition =false; 
				//Name Group 에 설정된 Condition 에 해당하는 실제 파트 정보. cf) Condition에 속하지 않은 Part Name도 Count에 포함이 되어 있어서 다시 수정함
				NameGroupCountResult validNgResult = new NameGroupCountResult();

				// NameGroupCountResult countResult = new NameGroupCountResult(Integer.parseInt(qty));
				ArrayList<PngCondition> conditionList = pngMaster.getConditionList();
				if (conditionList == null || conditionList.isEmpty()) {
					if (qty != defaultQtyObj.intValue()) {
						ngResult.setValid(false);
						ngResult.setReason(defaultQtyObj.intValue() + " &lt;&gt; " + qty);
					}
				} else {
					boolean isChecked = false;
					int preGroupNumber = -1;
					boolean preResult = true;
					for (int k = 0; conditionList != null && k < conditionList.size(); k++) {
						PngCondition pngCondition = conditionList.get(k);

						int groupNumber = pngCondition.getGroupNumber();
						if (preGroupNumber != groupNumber && !ngResult.isValid()) {
							break;
						}
						ArrayList<String> partNameList = pngCondition.getPartNameList();
						String specStr = parentDlg.specMap.get(specKey);
						if (currentProduct.equals(pngCondition.getProduct())) {
							if (parentDlg.isAvailable(pngCondition.getCondition(), specStr)) {
								String op = null;
								if (pngCondition.getOperator().equals("=")) {
									op = "==";
								} else {
									op = pngCondition.getOperator();
								}
								String operation = null;

								int cnt = 0;
								if (partNameList == null || partNameList.isEmpty()) {
									cnt = qty;
								} else {
									// 특정 Part Name들의 합에 의한 수만 더하기
									for (String partName : partNameList) {
										//cnt += ngResult.getNameCount(partName);
										int nameCnt = ngResult.getNameCount(partName);
										cnt += nameCnt;
										// 해당 Condition 에 존재하는 중복되지않은 Part Name 을 저장한다.
										int partNameCnt = validNgResult.getNameCount(partName);
										//BOM에 실제로 존재하고, 중복되지 않은 정보를 저장함
										if(cnt > 0 && partNameCnt ==0)
											validNgResult.addNameCount(partName, nameCnt);
									}
								}

								operation = cnt + op + pngCondition.getQuantity();
								Object obj = parentDlg.getEngine().eval(operation);
								if (obj instanceof Boolean) {

									Boolean b = (Boolean) obj;
									if (!b.booleanValue()) {

										String tmp = "";
										if (pngCondition.getOperator().equals("<")) {
											tmp = "&lt;";
										} else if (pngCondition.getOperator().equals(">")) {
											tmp = "&gt;";
										} else {
											tmp = pngCondition.getOperator();
										}

										// 조건식을 모두 보여주기위함.
										if (ngResult.getReason() == null || ngResult.getReason().equals("")) {
											ngResult.setReason("[" + pngCondition.getProduct() + "] " + pngCondition.getCondition() + ".Quantity " + tmp + " " + pngCondition.getQuantity());
										} else {
											ngResult.setReason(ngResult.getReason() + "." + "[" + pngCondition.getProduct() + "] " + pngCondition.getCondition() + ".Quantity " + tmp + " " + pngCondition.getQuantity());
										}

										ngResult.setValid(false);

										// 오류처리된 원인 표기.
										ngResult.setReason("[" + pngCondition.getProduct() + "] " + pngCondition.getCondition() + ".Quantity " + tmp + " " + pngCondition.getQuantity());
										// break;
									}else
									{
										// Name Group 에 설정된 Condition 에 속하고 갯수가 일치하면 true
										isPartExistInValidCondition = true;
									}
									
									isChecked = true;
								} else {
									throw new Exception("Not available Operation : " + operation);
								}

							}
						}

						preGroupNumber = groupNumber;
					}

					if (!isChecked) {
						if (qty != defaultQtyObj.intValue()) {
							ngResult.setValid(false);
							ngResult.setReason(defaultCnt + " &lt;&gt; " + qty);
						}
					}
				}
				// Name Group 에 설정된 Condition 에 속하고 갯수가 일치하는 경우. 유효한 갯수로 Condtion에 해당하는 갯수로 저장
				if(ngResult.isValid() && isPartExistInValidCondition)
					row.add(validNgResult);
				else
					row.add(ngResult);
			}

			data.add(row);
		}

		return data;
	}

	private ArrayList getEndItemNameList(String product, String type, String puid, String rowkey) throws Exception {
		SYMCRemoteUtil remote = new SYMCRemoteUtil();
		DataSet ds = new DataSet();

		ds.put("PRODUCT", product);
		ds.put("SPEC_TYPE", type);
		ds.put("PUID", puid);
		ds.put("ROWKEY", rowkey);
		try {

			ArrayList<HashMap<String, Object>> list = (ArrayList<HashMap<String, Object>>) remote.execute("com.ssangyong.service.PartNameGroupService", "getEndItemNameList", ds);

			return list;

		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw e;
		}
	}

	private ArrayList getSpecEndItemNameList(String product, String type, String puid, String rowkey) throws Exception {
		SYMCRemoteUtil remote = new SYMCRemoteUtil();
		DataSet ds = new DataSet();

		ds.put("PRODUCT", product);
		ds.put("SPEC_TYPE", type);
		ds.put("PUID", puid);
		ds.put("ROWKEY", rowkey);
		try {
			ArrayList<HashMap<String, Object>> list = (ArrayList<HashMap<String, Object>>) remote.execute("com.ssangyong.service.PartNameGroupService", "getSpecEndItemNameList", ds);
			return list;
		} catch (Exception e) {
			throw e;
		}
	}

	private String getSpec(String type, String puid) throws Exception {
		SYMCRemoteUtil remote = new SYMCRemoteUtil();
		DataSet ds = new DataSet();

		ds.put("SPEC_TYPE", type);
		ds.put("PUID", puid);
		try {

			String specStr = (String) remote.execute("com.ssangyong.service.PartNameGroupService", "getSpec", ds);

			return specStr;

		} catch (Exception e) {
			throw e;
		}
	}

	private ArrayList<HashMap<String, Object>> getSpecWithCategory(String type, String puid) {
		try {
			SYMCRemoteUtil remote = new SYMCRemoteUtil();
			DataSet ds = new DataSet();

			ds.put("PUID", puid);
			try {
				ArrayList<HashMap<String, Object>> spec = null;
				if (type.equals("USER SPEC")) {
					spec = (ArrayList<HashMap<String, Object>>) remote.execute("com.ssangyong.service.PartNameGroupService", "getUserSpecWithCategory", ds);
				} else if (type.equals("BUILD SPEC")) {
					spec = (ArrayList<HashMap<String, Object>>) remote.execute("com.ssangyong.service.PartNameGroupService", "getBuildSpecWithCategory", ds);
				} else if (type.equals("PLAN SPEC") || type.equals("RESULT SPEC")) {
					spec = (ArrayList<HashMap<String, Object>>) remote.execute("com.ssangyong.service.PartNameGroupService", "getPlanResultSpecWithCategory", ds);
				}
				return spec;
			} catch (Exception e) {
				MessageBox.post(e);
			}
		} catch (Exception e) {
			MessageBox.post(e);
		}

		return null;
	}

	private void exportToExcel() {

		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		Calendar now = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddkkmmss");
		sdf.format(now.getTime());
		String sFileName = "";
		//sFileName = "NameGroupVerification_Result_" + lbTargetProd.getText() + "_" + sdf.format(now.getTime()) + ".xls";
		sFileName = "NameGroupVerification_Result_" + lbTargetProd.getText() + "_" + sdf.format(now.getTime()) + ".xlsx";
		File defaultFile = new File(sFileName);
		fileChooser.setSelectedFile(defaultFile);
		fileChooser.setFileFilter(new FileFilter() {
			public boolean accept(File f) {
				if (f.isFile()) {
					//return f.getName().endsWith("xls");
					return f.getName().endsWith("xlsx");
				}
				return false;
			}

			public String getDescription() {
				//return "*.xls";
				return "*.xlsx";
			}
		});
		int result = fileChooser.showSaveDialog(parentDlg);
		if (result == JFileChooser.APPROVE_OPTION) {
			File selectedFile = fileChooser.getSelectedFile();
			try {
				//exportFor(selectedFile);
				exportXSSF(selectedFile, cbOnlyError.isSelected());
				AIFShell aif = new AIFShell("application/vnd.ms-excel", selectedFile.getAbsolutePath());
				aif.start();
			} catch (Exception e) {
				e.printStackTrace();
				MessageBox.post(parentDlg, e.getMessage(), "ERROR", MessageBox.ERROR);
			}
		}
	}

	private void exportFor(File selectedFile) throws Exception {
		WritableWorkbook workBook = Workbook.createWorkbook(selectedFile);
		// 0번째 Sheet 생성
		WritableSheet sheet = workBook.createSheet("Result", 0);

		WritableCellFormat cellFormat = new WritableCellFormat(); // 셀의 스타일을 지정하기 위한 부분입니다.
		cellFormat.setBorder(Border.ALL, BorderLineStyle.THIN); // 셀의 스타일을 지정합니다. 테두리에 라인그리는거에요
		cellFormat.setAlignment(Alignment.CENTRE);
		cellFormat.setFont(new WritableFont(WritableFont.ARIAL, 9, WritableFont.NO_BOLD, false, UnderlineStyle.NO_UNDERLINE, Colour.BLACK));
		cellFormat.setVerticalAlignment(VerticalAlignment.CENTRE);

		WritableCellFormat autoLineFormat = new WritableCellFormat(); // 셀의 스타일을 지정하기 위한 부분입니다.
		autoLineFormat.setBorder(Border.ALL, BorderLineStyle.THIN); // 셀의 스타일을 지정합니다. 테두리에 라인그리는거에요
		autoLineFormat.setFont(new WritableFont(WritableFont.ARIAL, 9, WritableFont.NO_BOLD, false, UnderlineStyle.NO_UNDERLINE, Colour.BLACK));
		autoLineFormat.setWrap(true);
		autoLineFormat.setVerticalAlignment(VerticalAlignment.CENTRE);

		WritableCellFormat autoLineErrorFormat = new WritableCellFormat(); // 셀의 스타일을 지정하기 위한 부분입니다.
		autoLineErrorFormat.setBorder(Border.ALL, BorderLineStyle.THIN); // 셀의 스타일을 지정합니다. 테두리에 라인그리는거에요
		autoLineErrorFormat.setFont(new WritableFont(WritableFont.ARIAL, 9, WritableFont.NO_BOLD, false, UnderlineStyle.NO_UNDERLINE, Colour.BLACK));
		autoLineErrorFormat.setWrap(true);
		autoLineErrorFormat.setVerticalAlignment(VerticalAlignment.CENTRE);
		autoLineErrorFormat.setBackground(Colour.RED);

		WritableCellFormat headerCellFormat = new WritableCellFormat(); // 셀의 스타일을 지정하기 위한 부분입니다.
		headerCellFormat.setBorder(Border.ALL, BorderLineStyle.THIN);
		headerCellFormat.setFont(new WritableFont(WritableFont.ARIAL, 9, WritableFont.NO_BOLD, false, UnderlineStyle.NO_UNDERLINE, Colour.BLACK));
		headerCellFormat.setWrap(true);
		headerCellFormat.setBackground(Colour.GREY_25_PERCENT);
		
		WritableCellFormat noLineCellFormat = new WritableCellFormat(); // 셀의 스타일을 지정하기 위한 부분입니다.
		noLineCellFormat.setBorder(Border.ALL, BorderLineStyle.NONE);
		noLineCellFormat.setAlignment(Alignment.CENTRE);
		noLineCellFormat.setFont(new WritableFont(WritableFont.ARIAL, 9, WritableFont.NO_BOLD, false, UnderlineStyle.NO_UNDERLINE, Colour.BLACK));
		noLineCellFormat.setVerticalAlignment(VerticalAlignment.CENTRE);

		Label label = null;

		int startRow = 0;
		int initColumnNum = 0;

		Vector verifyHeader = null;
		Vector<Vector> data = null;
		if (cbOnlyError.isSelected()) {
			data = verificationErrorTablePanel.getData();
			verifyHeader = verificationErrorTablePanel.getVerifyHeader();
		} else {
			data = verificationTablePanel.getData();
			verifyHeader = verificationTablePanel.getVerifyHeader();
		}

		Vector excelColumnHeader = new Vector();
		excelColumnHeader.add("Group ID");
		excelColumnHeader.add("Group Name");
		excelColumnHeader.add("Default Qty.");
		excelColumnHeader.add("Condition");
		excelColumnHeader.addAll(verifyHeader);

		for (int i = 0; i < excelColumnHeader.size(); i++) {
			// label = new jxl.write.Label(i + initColumnNum, startRow, excelColumnHeader.get(i).toString(), headerCellFormat);
			String sHeader = excelColumnHeader.get(i).toString();

			// [SR150616-009][2015.06.17][jclee] Excel Export 시 Excel Header의 Spec No를 세로로 표시
			// - Column Header의 Index
			// 0 : Group ID, 1 : Group Name, 2 : Default QTY, 3 : Condition
			// Index4 부터 Spec이므로 아래와 같이 구현. 즉, 추 후 index가 변경될 경우 아래의 index 제한 조건도 함께 변경해야함.
			if (i > 3) {
				String sTemp = "";
				for (int j = 0; j < sHeader.length(); j++) {
					if (j != sHeader.length() - 1) {
						sTemp += sHeader.substring(j, j + 1) + "\012";
					} else {
						sTemp += sHeader.substring(j);
					}
				}

				sHeader = sTemp;
			}

			label = new jxl.write.Label(i + initColumnNum, startRow, sHeader, headerCellFormat);

			sheet.addCell(label);
			// CellView cv = sheet.getColumnView(i + initColumnNum);
			// cv.setSize(1500);
			// cv.setAutosize(true);

			// sheet.setColumnView(i + initColumnNum, cv);
		}

		int rowNum = 0;
		startRow = 1;

		String value = null;
		WritableCellFormat format = null;
		for (int i = 0; i < data.size(); i++) {
			Vector row = data.get(i);
			for (int j = 0; row != null && j < row.size() + 1; j++) {
				value = null;
				if (j < 3) {
					value = row.get(j).toString();
					format = autoLineFormat;
				} else if (j == 3) {
					PngMaster pngMaster = (PngMaster) row.get(0);
					ArrayList<PngCondition> conditionList = pngMaster.getConditionList();

					/**
					 * [SR150416-025][2015.05.27][jclee] Excel 출력 시 수량 별 Part Name 표시 변경
					 */
					ArrayList<String> alConditionList = new ArrayList<String>();
					for (int k = 0; conditionList != null && k < conditionList.size(); k++) {
						PngCondition condition = conditionList.get(k);
						String condStr = "[" + condition.getProduct() + "] " + condition.getCondition() + "[Qty " + condition.getOperator() + " " + condition.getQuantity() + "]";

						if (alConditionList.contains(condStr)) {
							continue;
						}
						alConditionList.add(condStr);

						if (value == null) {
							value = condStr;
							for (int inx = 0; inx < condition.getPartNameList().size(); inx++) {
								value += "\012" + condition.getPartNameList().get(inx);
							}

							value += "\012";
						} else {
							value += "\012" + condStr;
							for (int inx = 0; inx < condition.getPartNameList().size(); inx++) {
								value += "\012" + condition.getPartNameList().get(inx);
							}
							value += "\012";
						}
					}
					format = autoLineFormat;
				} else {
					NameGroupCountResult result = (NameGroupCountResult) row.get(j - 1);
					value = result.toString();
					if (result.isValid()) {
						format = autoLineFormat;
					} else {
						format = autoLineErrorFormat;
					}
				}

				label = new jxl.write.Label(j + initColumnNum, (rowNum) + startRow, value, format);
				sheet.addCell(label);
			}
			rowNum++;
		}
		
		// [SR160503-014][20160509][jclee] Create Spec Condition Sheet
		WritableSheet sheetSpec = workBook.createSheet("Spec", 1);
		int iRowInfo = 0;
		int iColInfo = 0;
		
		int iRowHeader = 2;
		
		int iColOptionCode = 0;
		int iColOptionDesc = 1;
		
		int iRowStart = 3;
		int iColStart = 2;
		
		label = new jxl.write.Label(iColInfo, iRowInfo, "■ Option_Info", noLineCellFormat);
		sheetSpec.addCell(label);

		label = new jxl.write.Label(iColOptionCode, iRowHeader, "Option", headerCellFormat);
		sheetSpec.addCell(label);
		
		label = new jxl.write.Label(iColOptionDesc, iRowHeader, "Description", headerCellFormat);
		sheetSpec.addCell(label);
		
		// 1. Verification 대상 모든 Spec내 Option Code, Description 추출
		ArrayList<String> alOptions = new ArrayList<String>();
		HashMap<String, ArrayList<String>> hmSpecs = new HashMap<String, ArrayList<String>>();
		
		int iSpecCount = specTable.getRowCount();
		
		int iPUIDCol = 3;
		int iTypeCol = 1;
		
		DefaultTableModel model = (DefaultTableModel) specTable.getModel();
		for (int inx = 0; inx < iSpecCount; inx++) {
			ArrayList<String> alOptionForSpec = new ArrayList<String>();
			
			int iModelRow = specTable.convertRowIndexToModel(inx);
			String sType = (String) model.getValueAt(iModelRow, iTypeCol);
			String sPUID = (String) model.getValueAt(iModelRow, iPUIDCol);
//			System.out.println( "sPUID : " + sPUID);
			String sSpecCondition = getSpec(sType, sPUID);
			String[] saSpecOptions = sSpecCondition.toUpperCase().split("AND");
			
			for (int jnx = 0; jnx < saSpecOptions.length; jnx++) {
				String sOption = saSpecOptions[jnx].trim();
				
				// 모든 Option을 담아두는 Arraylist에 값 Add
				if (alOptions.isEmpty() || !alOptions.contains(sOption)) {
					alOptions.add(sOption);
				}
				
				// 각 Spec별 Option을 담아두는 Hashmap에 값 Put
				alOptionForSpec.add(sOption);
			}
			
			hmSpecs.put(sPUID, alOptionForSpec);
		}
		
		Collections.sort(alOptions);	// Alphabet 순으로 정렬
		
		// 2. 모든 Option을 Print
		SYMCRemoteUtil remote = new SYMCRemoteUtil();
		DataSet ds = new DataSet();
		ds.put("DATA", null);
		ArrayList<HashMap<String, String>> alAllOptions = (ArrayList<HashMap<String, String>>)remote.execute("com.ssangyong.service.VariantService", "getVariantValueDesc", ds);
		
		for (int inx = 0; inx < alOptions.size(); inx++) {
			String sOptionCode = alOptions.get(inx);
			String sOptionDesc = "";
			
			for (int jnx = 0; jnx < alAllOptions.size(); jnx++) {
				HashMap<String, String> hmAllOption = alAllOptions.get(jnx);
				String sOptionCodeTemp = hmAllOption.get("CODE_NAME");
				String sOptionDescTemp = hmAllOption.get("CODE_DESC");
				
				if (sOptionCodeTemp.equals(sOptionCode)) {
					sOptionDesc = sOptionDescTemp;
					break;
				}
			}
			
			label = new jxl.write.Label(iColOptionCode, iRowStart + inx, sOptionCode, cellFormat);
			sheetSpec.addCell(label);
			
			label = new jxl.write.Label(iColOptionDesc, iRowStart + inx, sOptionDesc, cellFormat);
			sheetSpec.addCell(label);
		}
		
		// 3. 각 Spec 별 조건 Print
		for (int inx = 0; inx < iSpecCount; inx++) {
			int iModelRow = specTable.convertRowIndexToModel(inx);
			String sSpecNo = (String) model.getValueAt(iModelRow, iPUIDCol);
			
			// Header Print
			label = new jxl.write.Label(iColStart + inx, iRowHeader, sSpecNo, headerCellFormat);
			sheetSpec.addCell(label);
			
			ArrayList<String> alOptionForSpec = hmSpecs.get(sSpecNo);
			
			// Contents Print
			for (int jnx = 0; jnx < alOptions.size(); jnx++) {
				String sOptionCode = alOptions.get(jnx);
				
				if (alOptionForSpec.contains(sOptionCode)) {
					label = new jxl.write.Label(iColStart + inx, iRowStart + jnx, "O", cellFormat);
				} else {
					label = new jxl.write.Label(iColStart + inx, iRowStart + jnx, "-", cellFormat);
				}
				
				sheetSpec.addCell(label);
			}
		}

		// 셀 Merge
		// initColumnNum = opNameList.size();
		// int startIdxToMerge = startRow;
		// int endIdxToMerge = startRow;
		// for (int i = 0; i < data.size(); i++){
		//
		// Cell cell = sheet.getCell(initColumnNum, i + startRow);
		// Cell nextCell = sheet.getCell(initColumnNum, i + startRow + 1);
		//
		// if( cell.getContents().equals(nextCell.getContents())){
		// endIdxToMerge = i + 1 + startRow;
		// }else{
		// if( startIdxToMerge < endIdxToMerge){
		// sheet.mergeCells(initColumnNum, startIdxToMerge, initColumnNum, endIdxToMerge);
		// WritableCell wCell = sheet.getWritableCell(initColumnNum, startIdxToMerge);
		// WritableCellFormat cf = new WritableCellFormat(); // 셀의 스타일을 지정하기 위한 부분입니다.
		// cf.setWrap(true);
		// cf.setBorder(Border.ALL, BorderLineStyle.THIN);
		// cf.setVerticalAlignment(VerticalAlignment.CENTRE);
		// wCell.setCellFormat(cf);
		// }
		// startIdxToMerge = i + 1 + startRow;
		// }
		// }
		sheetAutoFitColumns(sheet);
		sheetAutoFitColumns(sheetSpec);
		workBook.write();
		workBook.close();
	}

	/**
	 * Excel Sheet Size 자동 조절
	 * 
	 * @param sheet
	 */
	private void sheetAutoFitColumns(WritableSheet sheet) {
		for (int i = 0; i < sheet.getColumns(); i++) {
			Cell[] cells = sheet.getColumn(i);
			int longestStrLen = -1;

			if (cells.length == 0)
				continue;

			/* Find the widest cell in the column. */
			for (int j = 0; j < cells.length; j++) {
				String sContents = cells[j].getContents();
				String[] split = sContents.split("\n");

				for (int k = 0; k < split.length; k++) {
					if (split[k].length() > longestStrLen) {
						String str = split[k];
						if (str == null || str.isEmpty())
							continue;
						longestStrLen = str.trim().length();
					}
				}
			}

			/* If not found, skip the column. */
			if (longestStrLen == -1)
				continue;

			/* If wider than the max width, crop width */
			if (longestStrLen > 80)
				longestStrLen = 80;

			CellView cv = sheet.getColumnView(i);
			cv.setSize(longestStrLen * 256 + 200); /* Every character is 256 units wide, so scale it. */
			sheet.setColumnView(i, cv);
		}
	}
	
	/**
	 * POI API로 Excel Export
	 * @param selectedFile
	 * @throws Exception
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void exportXSSF(File selectedFile, boolean isOnlyError) throws Exception {

		org.apache.poi.ss.usermodel.Workbook wb = new XSSFWorkbook();

		Sheet sheet = wb.createSheet("Result");

		org.apache.poi.ss.usermodel.Font cellFormatFont = null;
		cellFormatFont = wb.createFont();
		cellFormatFont.setFontHeightInPoints((short) 9);
		cellFormatFont.setFontName("Arial");

		CellStyle cellFormat = wb.createCellStyle();
		cellFormat.setBorderTop(CellStyle.BORDER_THIN);
		cellFormat.setBorderBottom(CellStyle.BORDER_THIN);
		cellFormat.setBorderRight(CellStyle.BORDER_THIN);
		cellFormat.setBorderLeft(CellStyle.BORDER_THIN);
		cellFormat.setAlignment(CellStyle.ALIGN_CENTER);
		cellFormat.setFont(cellFormatFont);
		
		CellStyle addedOptioncellFormat = wb.createCellStyle();
		addedOptioncellFormat.setBorderTop(CellStyle.BORDER_THIN);
		addedOptioncellFormat.setBorderBottom(CellStyle.BORDER_THIN);
		addedOptioncellFormat.setBorderRight(CellStyle.BORDER_THIN);
		addedOptioncellFormat.setBorderLeft(CellStyle.BORDER_THIN);
		addedOptioncellFormat.setAlignment(CellStyle.ALIGN_CENTER);
		addedOptioncellFormat.setFont(cellFormatFont);
		addedOptioncellFormat.setFillForegroundColor(SpecOptionChangeInfo.getAddDisplayColor());
		addedOptioncellFormat.setFillPattern(CellStyle.SOLID_FOREGROUND);
		
		
		CellStyle deletedOptioncellFormat = wb.createCellStyle();
		deletedOptioncellFormat.setBorderTop(CellStyle.BORDER_THIN);
		deletedOptioncellFormat.setBorderBottom(CellStyle.BORDER_THIN);
		deletedOptioncellFormat.setBorderRight(CellStyle.BORDER_THIN);
		deletedOptioncellFormat.setBorderLeft(CellStyle.BORDER_THIN);
		deletedOptioncellFormat.setAlignment(CellStyle.ALIGN_CENTER);
		deletedOptioncellFormat.setFont(cellFormatFont);
		deletedOptioncellFormat.setFillForegroundColor(SpecOptionChangeInfo.getDeleteDisplayColor());
		deletedOptioncellFormat.setFillPattern(CellStyle.SOLID_FOREGROUND);
		
		CellStyle changedOptioncellFormat = wb.createCellStyle();
		changedOptioncellFormat.setBorderTop(CellStyle.BORDER_THIN);
		changedOptioncellFormat.setBorderBottom(CellStyle.BORDER_THIN);
		changedOptioncellFormat.setBorderRight(CellStyle.BORDER_THIN);
		changedOptioncellFormat.setBorderLeft(CellStyle.BORDER_THIN);
		changedOptioncellFormat.setAlignment(CellStyle.ALIGN_CENTER);
		changedOptioncellFormat.setFont(cellFormatFont);
		changedOptioncellFormat.setFillForegroundColor(SpecOptionChangeInfo.getChangeDisplayColor());
		changedOptioncellFormat.setFillPattern(CellStyle.SOLID_FOREGROUND);
		
		CellStyle autoLineFormat = wb.createCellStyle();
		autoLineFormat.setBorderTop(CellStyle.BORDER_THIN);
		autoLineFormat.setBorderBottom(CellStyle.BORDER_THIN);
		autoLineFormat.setBorderRight(CellStyle.BORDER_THIN);
		autoLineFormat.setBorderLeft(CellStyle.BORDER_THIN);
		autoLineFormat.setWrapText(true);
		autoLineFormat.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
		autoLineFormat.setFont(cellFormatFont);

		CellStyle autoLineErrorFormat = wb.createCellStyle();
		autoLineErrorFormat.setBorderTop(CellStyle.BORDER_THIN);
		autoLineErrorFormat.setBorderBottom(CellStyle.BORDER_THIN);
		autoLineErrorFormat.setBorderRight(CellStyle.BORDER_THIN);
		autoLineErrorFormat.setBorderLeft(CellStyle.BORDER_THIN);
		autoLineErrorFormat.setWrapText(true);
		autoLineErrorFormat.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
		autoLineErrorFormat.setFillForegroundColor(IndexedColors.RED.getIndex());
		autoLineErrorFormat.setFillPattern(CellStyle.SOLID_FOREGROUND);
		autoLineErrorFormat.setFont(cellFormatFont);

		CellStyle headerCellFormat = wb.createCellStyle();
		headerCellFormat.setBorderTop(CellStyle.BORDER_THIN);
		headerCellFormat.setBorderBottom(CellStyle.BORDER_THIN);
		headerCellFormat.setBorderRight(CellStyle.BORDER_THIN);
		headerCellFormat.setBorderLeft(CellStyle.BORDER_THIN);
		headerCellFormat.setWrapText(true);
		headerCellFormat.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
		headerCellFormat.setFillPattern(CellStyle.SOLID_FOREGROUND);
		headerCellFormat.setFont(cellFormatFont);

		CellStyle noLineCellFormat = wb.createCellStyle();
		noLineCellFormat.setAlignment(CellStyle.ALIGN_CENTER);
		noLineCellFormat.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
		noLineCellFormat.setFont(cellFormatFont);

		int startRow = 0;
		int initColumnNum = 0;

		Vector verifyHeader = null;
		Vector<Vector> data = null;
		if (isOnlyError) {
			data = verificationErrorTablePanel.getData();
			verifyHeader = verificationErrorTablePanel.getVerifyHeader();
		} else {
			data = verificationTablePanel.getData();
			verifyHeader = verificationTablePanel.getVerifyHeader();
		}

		Vector excelColumnHeader = new Vector();
		excelColumnHeader.add("Group ID");
		excelColumnHeader.add("Group Name");
		excelColumnHeader.add("Default Qty.");
		excelColumnHeader.add("Condition");
		excelColumnHeader.addAll(verifyHeader);
		
		Row firstRow = sheet.createRow(startRow);
		for (int i = 0; i < excelColumnHeader.size(); i++) {
			String sHeader = excelColumnHeader.get(i).toString();
			if (i > 3) {
				String sTemp = "";
				for (int j = 0; j < sHeader.length(); j++) {
					if (j != sHeader.length() - 1) {
						sTemp += sHeader.substring(j, j + 1) + "\012";
					} else {
						sTemp += sHeader.substring(j);
					}
				}

				sHeader = sTemp;
			}

			org.apache.poi.ss.usermodel.Cell cell = firstRow.createCell(i + initColumnNum);
			cell.setCellValue(sHeader);
			cell.setCellStyle(headerCellFormat);
		}

		int rowNum = 0;
		startRow = 1;

		String value = null;
		CellStyle format = null;
		
		for (int i = 0; i < data.size(); i++) {
			Vector row = data.get(i);
			Row cellRow = sheet.createRow(rowNum + startRow);
			for (int j = 0; row != null && j < row.size() + 1; j++) {
				value = null;
				if (j < 3) {
					value = row.get(j).toString();
					format = autoLineFormat;
				} else if (j == 3) {
					PngMaster pngMaster = (PngMaster) row.get(0);
					ArrayList<PngCondition> conditionList = pngMaster.getConditionList();

					/**
					 * [SR150416-025][2015.05.27][jclee] Excel 출력 시 수량 별 Part Name 표시 변경
					 */
					ArrayList<String> alConditionList = new ArrayList<String>();
					for (int k = 0; conditionList != null && k < conditionList.size(); k++) {
						PngCondition condition = conditionList.get(k);
						String condStr = "[" + condition.getProduct() + "] " + condition.getCondition() + "[Qty " + condition.getOperator() + " "
								+ condition.getQuantity() + "]";

						if (alConditionList.contains(condStr)) {
							continue;
						}
						alConditionList.add(condStr);

						if (value == null) {
							value = condStr;
							for (int inx = 0; inx < condition.getPartNameList().size(); inx++) {
								value += "\012" + condition.getPartNameList().get(inx);
							}

							value += "\012";
						} else {
							value += "\012" + condStr;
							for (int inx = 0; inx < condition.getPartNameList().size(); inx++) {
								value += "\012" + condition.getPartNameList().get(inx);
							}
							value += "\012";
						}
					}
					format = autoLineFormat;
				} else {
					NameGroupCountResult result = (NameGroupCountResult) row.get(j - 1);
					value = result.toString();
					if (result.isValid()) {
						format = autoLineFormat;
					} else {
						format = autoLineErrorFormat;
					}
				}
				org.apache.poi.ss.usermodel.Cell cell = cellRow.createCell(j + initColumnNum);
				cell.setCellValue(value);
				cell.setCellStyle(format);
			}
			rowNum++;
		}

		Sheet sheetSpec = wb.createSheet("Spec");

		int iRowInfo = 0;
		int iColInfo = 0;

		int iRowHeader = 2;

		int iColOptionCode = 0;
		int iColOptionDesc = 1;

		int iRowStart = 3;
		int iColStart = 2;

		Row specRow = sheetSpec.createRow(iRowInfo);
		org.apache.poi.ss.usermodel.Cell cell = specRow.createCell(iColInfo);
		cell.setCellValue("■ Option_Info");
		cell.setCellStyle(noLineCellFormat);
		
		cell = specRow.createCell(iColInfo+2);
		cell.setCellValue("Added");
		cell.setCellStyle(addedOptioncellFormat);
		
		cell = specRow.createCell(iColInfo+3);
		cell.setCellValue("Changed");
		cell.setCellStyle(changedOptioncellFormat);
		
		cell = specRow.createCell(iColInfo+4);
		cell.setCellValue("Deleted");
		cell.setCellStyle(deletedOptioncellFormat);

		Row specHeaderRow = sheetSpec.createRow(iRowHeader);
		cell = specHeaderRow.createCell(iColOptionCode);
		cell.setCellValue("Option");
		cell.setCellStyle(headerCellFormat);

		cell = specHeaderRow.createCell(iColOptionDesc);
		cell.setCellValue("Description");
		cell.setCellStyle(headerCellFormat);

		// 1. Verification 대상 모든 Spec내 Option Code, Description 추출
		ArrayList<String> alOptions = new ArrayList<String>();
		HashMap<String, ArrayList<String>> hmSpecs = new HashMap<String, ArrayList<String>>();

		int iSpecCount = specTable.getRowCount();

		int iPUIDCol = 3;
		int iTypeCol = 1;

		DefaultTableModel model = (DefaultTableModel) specTable.getModel();
		
		for (int inx = 0; inx < iSpecCount; inx++) {
			ArrayList<String> alOptionForSpec = new ArrayList<String>();

			int iModelRow = specTable.convertRowIndexToModel(inx);
			String sType = (String) model.getValueAt(iModelRow, iTypeCol);
			String sPUID = (String) model.getValueAt(iModelRow, iPUIDCol);

			String sSpecCondition = getSpec(sType, sPUID);
			String[] saSpecOptions = sSpecCondition.toUpperCase().split("AND");

			for (int jnx = 0; jnx < saSpecOptions.length; jnx++) {
				String sOption = saSpecOptions[jnx].trim();

				// 모든 Option을 담아두는 Arraylist에 값 Add
				if (alOptions.isEmpty() || !alOptions.contains(sOption)) {
					alOptions.add(sOption);
				}

				// 각 Spec별 Option을 담아두는 Hashmap에 값 Put
				alOptionForSpec.add(sOption);
			}

			hmSpecs.put(sPUID, alOptionForSpec);
		}

		Collections.sort(alOptions); // Alphabet 순으로 정렬

		// 2. 모든 Option을 Print
		SYMCRemoteUtil remote = new SYMCRemoteUtil();
		DataSet ds = new DataSet();
		ds.put("DATA", null);
		ArrayList<HashMap<String, String>> alAllOptions = (ArrayList<HashMap<String, String>>) remote.execute("com.ssangyong.service.VariantService",
				"getVariantValueDesc", ds);
		
		HashMap<Integer, Row > specContentsRowMap = new HashMap<Integer, Row >(); // KEY: ROW Index, Value: Row

		for (int inx = 0; inx < alOptions.size(); inx++) {
			String sOptionCode = alOptions.get(inx);
			String sOptionDesc = "";
			specRow = sheetSpec.createRow(iRowStart + inx);
			specContentsRowMap.put(iRowStart + inx, specRow);
			for (int jnx = 0; jnx < alAllOptions.size(); jnx++) {
				HashMap<String, String> hmAllOption = alAllOptions.get(jnx);
				String sOptionCodeTemp = hmAllOption.get("CODE_NAME");
				String sOptionDescTemp = hmAllOption.get("CODE_DESC");

				if (sOptionCodeTemp.equals(sOptionCode)) {
					sOptionDesc = sOptionDescTemp;
					break;
				}
			}

			cell = specRow.createCell(iColOptionCode);
			cell.setCellValue(sOptionCode);
			cell.setCellStyle(cellFormat);

			cell = specRow.createCell(iColOptionDesc);
			cell.setCellValue(sOptionDesc);
			cell.setCellStyle(cellFormat);
		}
		PartNameGroupDao partNameGroupDao = new PartNameGroupDao();
		
		///**  spec no 의 변경 정보를 갖고 온다. [SR180410-037] beenlaho **/
		ArrayList<SpecOptionChangeInfo> specOptionChangeInfoList = partNameGroupDao.getSpecOptionChangeInfo(hmSpecs.keySet());
		
		//Test 데이터 생성  SR180410-037
//		specOptionChangeInfoList.addAll( makeTestDataForSpecOptionChangeInfo () );
		
//		System.out.println(" specOptionChangeInfoList  " + specOptionChangeInfoList.size() );
		
		// 3. 각 Spec 별 조건 Print
		for (int inx = 0; inx < iSpecCount; inx++) {
			int iModelRow = specTable.convertRowIndexToModel(inx);
			String sSpecNo = (String) model.getValueAt(iModelRow, iPUIDCol);
			String sSpecName = (String) model.getValueAt(iModelRow, 0);
			
			// Header Print
			cell = specHeaderRow.createCell(iColStart + inx);
			cell.setCellValue(sSpecName);
			cell.setCellStyle(headerCellFormat);
			
			
			ArrayList<String> alOptionForSpec = hmSpecs.get(sSpecNo);
			// Contents Print
			for (int jnx = 0; jnx < alOptions.size(); jnx++) {
				
				specRow = specContentsRowMap.get(iRowStart + jnx);
				String sOptionCode = alOptions.get(jnx);
				cell = specRow.createCell(iColStart + inx);
				cell.setCellStyle(cellFormat);
				
				if (alOptionForSpec.contains(sOptionCode)) {
					cell.setCellValue("O");
					
				} else {
					cell.setCellValue("-");
				}
				
				/** [SR180410-037]  이력정보 표시 beenlaho **/
				if( specOptionChangeInfoList.size() > 0 ) {
					
					//비교하기 위해 현재 spec no 와 option 정보를 갖고 있는 SpecOptionChangeInfo 클래스를 생성한다. 
					SpecOptionChangeInfo specOptionChangeInfo = new SpecOptionChangeInfo();
					specOptionChangeInfo.setSpecNo(sSpecNo);
					specOptionChangeInfo.setOptions(sOptionCode);
					
//					System.out.println(" compare specOptionChangeInfo  " + specOptionChangeInfo);
					//spec no 와 option 정보가 동일한 객체을 찾는다.
					int existIndex = specOptionChangeInfoList.indexOf(specOptionChangeInfo);
					
					if( existIndex > -1 ){ 
						specOptionChangeInfo = specOptionChangeInfoList.get(existIndex);
						
						CellStyle cellFormat1 = wb.createCellStyle();
						cellFormat1.setBorderTop(CellStyle.BORDER_THIN);
						cellFormat1.setBorderBottom(CellStyle.BORDER_THIN);
						cellFormat1.setBorderRight(CellStyle.BORDER_THIN);
						cellFormat1.setBorderLeft(CellStyle.BORDER_THIN);
						cellFormat1.setAlignment(CellStyle.ALIGN_CENTER);
						cellFormat1.setFont(cellFormatFont);
						cellFormat1.setFillForegroundColor(specOptionChangeInfo.getDisplayColor());
						cellFormat1.setFillPattern(CellStyle.SOLID_FOREGROUND);
						cell.setCellStyle(cellFormat1);
					
					}
				}
			}
		}
		
		autoSizeColumn(sheet, 0, false);
		autoSizeColumn(sheetSpec, 2, true);

		FileOutputStream fos = new FileOutputStream(selectedFile.getAbsolutePath());

		wb.write(fos);
		fos.flush();
		fos.close();

		fos = null;
		wb = null;

	}
	
	/**
	 * Excel Sheet Size 자동 조절
	 * 
	 * @param sheet
	 */
	private void autoSizeColumn(Sheet sheet, int startRow, boolean isSpecSheet) {
		
		int rowCount = sheet.getPhysicalNumberOfRows();
		int colCount = sheet.getRow(startRow).getPhysicalNumberOfCells();
		int[] longestStrLen =  new int[colCount];
        for (int i = startRow; i < rowCount; i++) {
           Row row = sheet.getRow(i);
			   
           for(int j = 0 ; j < colCount; j++)
           {
        	   org.apache.poi.ss.usermodel.Cell cell = row.getCell(j);
        	   String sContents = cell.getStringCellValue();
        	   String[] split = sContents.split("\n");
        	   boolean isExistLongerLength =false; // 더 긴게 존재할 경우만 늘림
				for (int k = 0; k < split.length; k++) {
					if (split[k].length() > longestStrLen[j]) {
						String str = split[k];
						if (str == null || str.isEmpty())
							continue;
						longestStrLen[j] = str.trim().length();
						isExistLongerLength = true;
					}
				}
				
				if(!isExistLongerLength)
					continue;
				
				/* If not found, skip the column. */
				if (longestStrLen[j] == -1)
					continue;
				/* If wider than the max width, crop width */
				if (longestStrLen[j] > 80)
					longestStrLen[j] = 80;
				
				if(!isSpecSheet || isSpecSheet && i != 0)
					sheet.setColumnWidth(j, longestStrLen[j] * 256 + 200);
           }
        }
		if(isSpecSheet)
			sheet.setColumnWidth(0, 12 * 256);
		
	}

	class SpecEndItemExcelExportOperation extends AbstractAIFOperation {

		private WaitProgressBar waitProgress;
		boolean isExceptionFired = false;

		public SpecEndItemExcelExportOperation(WaitProgressBar waitProgress) {
			this.waitProgress = waitProgress;
		}

		@Override
		public void executeOperation() throws Exception {
			try {
				exportSpecEndItemExcelProcess(waitProgress);
			} catch (Exception e) {
				isExceptionFired = true;
				throw e;
			}
		}
	}
	
	private void set1LevelItemList(String product, String rowKey){
		SYMCRemoteUtil remote = new SYMCRemoteUtil();
		DataSet ds = new DataSet();

		ds.put("PRODUCT", product);
		ds.put("ROWKEY", rowKey);

		try {
			remote.execute("com.ssangyong.service.PartNameGroupService", "set1LevelItemList", ds);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
//	private String getRowkey(String product){
//		SYMCRemoteUtil remote = new SYMCRemoteUtil();
//		String rowKey = "";
//		DataSet ds = new DataSet();
//
//		ds.put("PRODUCT", product);
//
//		try {
//			rowKey = (String) remote.execute("com.ssangyong.service.PartNameGroupService", "getRowKey", ds);
//
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		
//		return rowKey;
//	}
	
//	private void deletePngEpl(String rowkey){
//		SYMCRemoteUtil remote = new SYMCRemoteUtil();
//		String rowKey = "";
//		DataSet ds = new DataSet();
//
//		ds.put("ROWKEY", rowkey);
//
//		try {
//			
//			remote.execute("com.ssangyong.service.PartNameGroupService", "deletePngEpl", ds);
//
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		
//	}

	/**
	 * [SR150421-010][2015.06.04][jclee] End Item List Excel Report
	 * 
	 * @param waitProgress
	 */
	private void exportSpecEndItemExcelProcess(WaitProgressBar waitProgress) {
//		System.out.println("Start Time : "+ new Date());
		ExecutorService executor = Executors.newFixedThreadPool(5);
//		Vector<SpecHeader> specHeaderList = new Vector();
		DefaultTableModel model = (DefaultTableModel) specTable.getModel();
		String product = (String) cbProduct.getSelectedItem();
		
		//[SR181211-012][CSH]Product 하위 1Level Part까지 EPL List 생성
		String rowKey = parentDlg.getRowkey();
//		System.out.println("1Level Item List 조회 Start : "+ new Date());
		waitProgress.setStatus(product + " 1Level Item List 조회 Start...");
		set1LevelItemList(product, rowKey);
//		System.out.println("1Level Item List 조회 END : "+ new Date());
		waitProgress.setStatus(product + " 1Level Item List 조회 End...");
		
		for (int i = 0; i < specTable.getRowCount(); i++) {
			int modelRowIdx = specTable.convertRowIndexToModel(i);
			String spec = (String) model.getValueAt(modelRowIdx, 0);
			String type = (String) model.getValueAt(modelRowIdx, 1);
			String puid = (String) model.getValueAt(modelRowIdx, 3);

//			SpecHeader header = new SpecHeader(spec, puid);
//			specHeaderList.add(header);

			CollectSpecEndItem collecter = new CollectSpecEndItem(waitProgress, spec, product, type, puid, rowKey);
			executor.execute(collecter);
		}
		executor.shutdown();
		while (!executor.isTerminated()) {
		}

//		deletePngEpl(rowKey);
		// 1. End Item List
		ArrayList<HashMap<String, Object>> alEndItemRemoveDuplicate = new ArrayList<HashMap<String, Object>>();

//		System.out.println("후속처리 Start : "+ new Date());
		// 1.1. 중복 Part No를 제거한 List 생성
		Object[] alEndItemKeySet = specEndItemResult.keySet().toArray();
		for (int inx = 0; inx < specEndItemResult.size(); inx++) {
			if (alEndItemRemoveDuplicate.isEmpty()) {
				alEndItemRemoveDuplicate = (ArrayList<HashMap<String, Object>>) specEndItemResult.get(alEndItemKeySet[inx].toString()).clone();
			} else {
				ArrayList<HashMap<String, Object>> alTemp = specEndItemResult.get(alEndItemKeySet[inx].toString());
				for (int jnx = 0; jnx < alTemp.size(); jnx++) {
					HashMap<String, Object> hmTemp = alTemp.get(jnx);
					String sTempPartNo = hmTemp.get("CHILD_NO").toString();
					String sTempFunctionNo = hmTemp.get("FUNCTION_NO").toString();
					String sTempCondition = hmTemp.get("CONDITION") == null ? "" : hmTemp.get("CONDITION").toString();
					String sTempSMNo = hmTemp.get("SUPPLY_MODE").toString();
//					if(sTempPartNo.equals("9719010801")){
//						System.out.println("Child_no ============= : "+hmTemp.get("CHILD_NO").toString());
//						System.out.println("FUNCTION_NO ========== : "+hmTemp.get("FUNCTION_NO").toString());
//					}
					

					boolean isInclude = false;
					for (int knx = 0; knx < alEndItemRemoveDuplicate.size(); knx++) {
						HashMap<String, Object> hm = alEndItemRemoveDuplicate.get(knx);
						String sPartNo = hm.get("CHILD_NO").toString();
						String sFunctionNo = hm.get("FUNCTION_NO").toString();
						String sCondition = hm.get("CONDITION") == null ? "" : hm.get("CONDITION").toString();
						String sSMNo = hm.get("SUPPLY_MODE").toString();

//						if(sPartNo.equals("9719010801")){
//							System.out.println("Child_no : "+alEndItemRemoveDuplicate.get(knx).get("CHILD_NO").toString());
//							System.out.println("FUNCTION_NO : "+alEndItemRemoveDuplicate.get(knx).get("FUNCTION_NO").toString());
//						}
						
						//Function이 동일한 Child No는 Skip
						if (sPartNo.equals(sTempPartNo) && sFunctionNo.equals(sTempFunctionNo) && sCondition.equals(sTempCondition) && sSMNo.equals(sTempSMNo)) {
							isInclude = true;
//							if(sPartNo.equals("9719010801")){
//								System.out.println("isInclude : "+sPartNo+"/"+sTempPartNo);
//							}
							break;
						}
					}

					if (!isInclude) {
						alEndItemRemoveDuplicate.add(hmTemp);
					}
				}
			}
		}
		
		// 1.2. 각 Part No 별 Spec 내 수량 표시
		for (int inx = 0; inx < alEndItemKeySet.length; inx++) {
			ArrayList<HashMap<String, Object>> alTemp = specEndItemResult.get(alEndItemKeySet[inx].toString());

			for (int jnx = 0; jnx < alTemp.size(); jnx++) {
				HashMap<String, Object> hmTemp = alTemp.get(jnx);
				String sTempPartNo = hmTemp.get("CHILD_NO").toString();
				String sTempFunctionNo = hmTemp.get("FUNCTION_NO").toString();
				String sTempCondition = hmTemp.get("CONDITION") == null ? "" : hmTemp.get("CONDITION").toString();
				String sTempSMNo = hmTemp.get("SUPPLY_MODE").toString();
				for (int knx = 0; knx < alEndItemRemoveDuplicate.size(); knx++) {
					HashMap<String, Object> hmRemoveDuplicate = alEndItemRemoveDuplicate.get(knx);
					String sPartNo = hmRemoveDuplicate.get("CHILD_NO").toString();
					String sFunctionNo = hmRemoveDuplicate.get("FUNCTION_NO").toString();
					String sCondition = hmRemoveDuplicate.get("CONDITION") == null ? "" : hmRemoveDuplicate.get("CONDITION").toString();
					String sSMNo = hmRemoveDuplicate.get("SUPPLY_MODE").toString();
					//Function이 동일한 Child No
					if (sPartNo.equals(sTempPartNo) && sFunctionNo.equals(sTempFunctionNo) && sCondition.equals(sTempCondition) && sSMNo.equals(sTempSMNo)) {
						// Object oCount = alEndItemRemoveDuplicate.get(jnx).get("NAME_COUNT");
						Object oCount = hmTemp.get("NAME_COUNT");
						hmRemoveDuplicate.put(alEndItemKeySet[inx].toString(), oCount);
						break;
					}
				}
			}
		}

		// 1.3. 각 Part No별 Spec Check. (값이 없는 Spec의 경우 '-' 표시)
		for (int inx = 0; inx < alEndItemRemoveDuplicate.size(); inx++) {
			HashMap<String, Object> hmRemoveDuplicate = alEndItemRemoveDuplicate.get(inx);
			for (int jnx = 0; jnx < alEndItemKeySet.length; jnx++) {
				try {
					Object oKey = hmRemoveDuplicate.get(alEndItemKeySet[jnx].toString());
					if (oKey == null) {
						hmRemoveDuplicate.put(alEndItemKeySet[jnx].toString(), "-");
					}
				} catch (Exception e) {
					hmRemoveDuplicate.put(alEndItemKeySet[jnx].toString(), "-");
				}
			}
		}
		
//		System.out.println("후속처리 End : "+ new Date());
//		System.out.println("Spec 정리 Start : "+ new Date());
		// 2. Spec List
		ArrayList<HashMap<String, Object>> alSpecResult = new ArrayList<HashMap<String, Object>>();
		HashMap<String, ArrayList<HashMap<String, Object>>> hmSpecList = new HashMap<String, ArrayList<HashMap<String, Object>>>();
		ArrayList<String> alCategory = new ArrayList<String>();
		
		// 2.1. Spec List 조회
		for (int inx = 0; inx < specTable.getRowCount(); inx++) {
			int modelRowIdx = specTable.convertRowIndexToModel(inx);
			String spec = (String) model.getValueAt(modelRowIdx, 0);
			String type = (String) model.getValueAt(modelRowIdx, 1);
			String puid = (String) model.getValueAt(modelRowIdx, 3);
		
			// Spec Category, Value 수집
			ArrayList<HashMap<String, Object>> alSpecWithCategory = getSpecWithCategory(type, puid);
			hmSpecList.put(puid, alSpecWithCategory);
		}
		
		//전체 Category 정리
		Object[] specPuids = hmSpecList.keySet().toArray();
		
		for(int i = 0; i < hmSpecList.size(); i++){
			ArrayList<HashMap<String, Object>> specCategoryList = hmSpecList.get(specPuids[i]);
			
			for (int jnx = 0; jnx < specCategoryList.size(); jnx++) {
				String sCategory = specCategoryList.get(jnx).get("CATE_NO").toString();
				if (!alCategory.contains(sCategory)) {
					alCategory.add(sCategory);
				}
			}
		}
//		Collections.sort(alCategory);

				
		for (int inx = 0; inx < specTable.getRowCount(); inx++) {
			int modelRowIdx = specTable.convertRowIndexToModel(inx);
			String spec = (String) model.getValueAt(modelRowIdx, 0);
			String type = (String) model.getValueAt(modelRowIdx, 1);
			String puid = (String) model.getValueAt(modelRowIdx, 3);
			
			HashMap<String, Object> hmTempSpecResult = new HashMap<String, Object>();
			hmTempSpecResult.put("SPEC_NO", spec);
			
			ArrayList<HashMap<String, Object>> specCategoryList = hmSpecList.get(puid);
			
			for (int jnx = 0; jnx < alCategory.size(); jnx++) {
				String sTempCategory = alCategory.get(jnx);
				boolean isInclude = false;
				
				for (int knx = 0; knx < specCategoryList.size(); knx++) {
					Object oTempCategoryNo = specCategoryList.get(knx).get("CATE_NO");
					Object oTempOptionNo = specCategoryList.get(knx).get("OPTION_NO");

					if (sTempCategory.equals(oTempCategoryNo.toString())) {
						hmTempSpecResult.put(sTempCategory, oTempOptionNo);
						isInclude = true;
						break;
					}
				}

				if (!isInclude) {
					hmTempSpecResult.put(sTempCategory, "-");
				}
			}

			alSpecResult.add(hmTempSpecResult);
		}
//		System.out.println("End Time : "+ new Date());
		// 3. Excel 출력
		exportSpecEndItemToExcel(alSpecResult, alEndItemRemoveDuplicate);
	}
	
	private Comparator<HashMap<String, Object>> comparator = new Comparator<HashMap<String, Object>>(){
        @Override
        public int compare(HashMap<String, Object> o1, HashMap<String, Object> o2) {
        	String funcionNo1 = o1.get("FUNCTION_NO").toString() + o1.get("SEQ_NO").toString();
        	String funcionNo2 = o2.get("FUNCTION_NO").toString() + o2.get("SEQ_NO").toString();
            return funcionNo1.compareTo(funcionNo2);
        }
   };

	/**
	 * Excel 출력
	 * 
	 * @param alRemoveDuplicate
	 */
	private void exportSpecEndItemToExcel(ArrayList<HashMap<String, Object>> alSpecResult, ArrayList<HashMap<String, Object>> alRemoveDuplicate) {
		try {
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			Calendar now = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddkkmmss");
			String sFileName = "";
			sFileName = "Spec_1Level_Item_List_" + sdf.format(now.getTime()) + ".xls";
			File defaultFile = new File(sFileName);
			fileChooser.setSelectedFile(defaultFile);
			fileChooser.setFileFilter(new FileFilter() {
				public boolean accept(File f) {
					if (f.isFile()) {
						return f.getName().endsWith("xls");
					}
					return false;
				}

				public String getDescription() {
					return "*.xls";
				}
			});
			int result = fileChooser.showSaveDialog(parentDlg);
			if (result == JFileChooser.APPROVE_OPTION) {
				// Cell Style
				// Info Cell Style
				WritableCellFormat wcfInfo = new WritableCellFormat();
				wcfInfo.setFont(new WritableFont(WritableFont.ARIAL, 9, WritableFont.BOLD, false, UnderlineStyle.NO_UNDERLINE, Colour.BLACK));
				wcfInfo.setAlignment(Alignment.LEFT);

				// Header Cell Style
				WritableCellFormat wcfHeader = new WritableCellFormat();
				wcfHeader.setBorder(Border.ALL, BorderLineStyle.THIN);
				wcfHeader.setWrap(true);
				wcfHeader.setFont(new WritableFont(WritableFont.ARIAL, 9, WritableFont.BOLD, false, UnderlineStyle.NO_UNDERLINE, Colour.BLACK));
				wcfHeader.setAlignment(Alignment.CENTRE);
				wcfHeader.setBackground(Colour.GREY_25_PERCENT);

				// Center Alignment Value Cell Style
				WritableCellFormat wcfValueCenter = new WritableCellFormat();
				wcfValueCenter.setBorder(Border.ALL, BorderLineStyle.THIN);
				wcfValueCenter.setFont(new WritableFont(WritableFont.ARIAL, 9, WritableFont.NO_BOLD, false, UnderlineStyle.NO_UNDERLINE, Colour.BLACK));
				wcfValueCenter.setAlignment(Alignment.CENTRE);

				// Left Alignment Value Cell Style
				WritableCellFormat wcfValueLeft = new WritableCellFormat();
				wcfValueLeft.setFont(new WritableFont(WritableFont.ARIAL, 9, WritableFont.NO_BOLD, false, UnderlineStyle.NO_UNDERLINE, Colour.BLACK));
				wcfValueLeft.setBorder(Border.ALL, BorderLineStyle.THIN);

				File selectedFile = fileChooser.getSelectedFile();
				WritableWorkbook workBook = Workbook.createWorkbook(selectedFile);
				WritableSheet sheetSpecs = workBook.createSheet("Specs", 0);
				WritableSheet sheetEndItems = workBook.createSheet("1 Level Items", 1);

				sheetSpecs.addCell(new Label(0, 0, "■ SPEC Information", wcfInfo));
				sheetEndItems.addCell(new Label(0, 0, "■ Spec Part List", wcfInfo));

				// Spec Sheet
				// Header
//				Object[] oSpecHeader = alSpecResult.get(0).keySet().toArray();
//				List<String> header = new ArrayList<String>();
//				Collections.sort(oSpecHeader);
				List headerList  = new ArrayList();
				headerList.addAll(alSpecResult.get(0).keySet());
				Collections.sort(headerList);
				Object[] oSpecHeader = headerList.toArray();
				
				int iColumnHeader = 1;
				sheetSpecs.addCell(new Label(0, 1, "Spec No", wcfHeader));

				for (int inx = 0; inx < oSpecHeader.length; inx++) {
					if (!oSpecHeader[inx].toString().equals("SPEC_NO")) {
						Label lblHeader = new Label(iColumnHeader, 1, oSpecHeader[inx].toString(), wcfHeader);
						sheetSpecs.addCell(lblHeader);
						iColumnHeader++;
					}
				}

				// Spec
				for (int inx = 0; inx < alSpecResult.size(); inx++) {
					HashMap<String, Object> hmSpecResult = alSpecResult.get(inx);
//					Object[] oTempSpecResultColumn = hmSpecResult.keySet().toArray();
					int iColumnValue = 1;
					sheetSpecs.addCell(new Label(0, inx + 2, hmSpecResult.get("SPEC_NO").toString(), wcfValueLeft));

					for (int jnx = 0; jnx < oSpecHeader.length; jnx++) {
						Object oOptionNo = hmSpecResult.get(oSpecHeader[jnx].toString());
						Label lblValue = null;

						if (!oSpecHeader[jnx].toString().equals("SPEC_NO")) {
							if (oOptionNo == null) {
								lblValue = new Label(iColumnValue, inx + 2, "-", wcfValueCenter);
							} else {
								lblValue = new Label(iColumnValue, inx + 2, oOptionNo.toString(), wcfValueCenter);
							}

							sheetSpecs.addCell(lblValue);
							iColumnValue++;
						}
					}
				}

				// End Item List Sheet
				// End Item Header
				ArrayList<String> alSpec = new ArrayList<String>();
				for (int inx = 0; inx < alSpecResult.size(); inx++) {
					HashMap<String, Object> hmSpecResult = alSpecResult.get(inx);
					alSpec.add(hmSpecResult.get("SPEC_NO").toString());
				}

				Label lblFuncionNoHeader = new Label(0, 1, "Funcion No", wcfHeader);
				sheetEndItems.addCell(lblFuncionNoHeader);

				Label lblLVHeader = new Label(1, 1, "LV", wcfHeader);
				sheetEndItems.addCell(lblLVHeader);

				Label lblPartNoHeader = new Label(2, 1, "Part No", wcfHeader);
				sheetEndItems.addCell(lblPartNoHeader);

				Label lblPartNameHeader = new Label(3, 1, "Part Name", wcfHeader);
				sheetEndItems.addCell(lblPartNameHeader);

				Label lblSModeHeader = new Label(4, 1, "S/Mode", wcfHeader);
				sheetEndItems.addCell(lblSModeHeader);

				for (int inx = 0; inx < alSpec.size(); inx++) {
					// [SR150616-009][2015.06.17][jclee] Excel Export 시 Excel Header의 Spec No를 세로로 표시
					String sValue = alSpec.get(inx).toString();
					String sTemp = "";
					for (int lnx = 0; lnx < sValue.length(); lnx++) {
						if (lnx != sValue.length() - 1) {
							sTemp += sValue.substring(lnx, lnx + 1) + "\012";
						} else {
							sTemp += sValue.substring(lnx);
						}
					}
					sValue = sTemp;

					Label lblOptionHeader = new Label(5 + inx, 1, sValue, wcfHeader);
					sheetEndItems.addCell(lblOptionHeader);
				}

				Label lblOptionHeader = new Label(5 + alSpec.size(), 1, "Option", wcfHeader);
				sheetEndItems.addCell(lblOptionHeader);

				// End Item List

				//function 으로 리스트 정렬이 필요하면 여기서....
				Collections.sort(alRemoveDuplicate, comparator);
				
				for (int inx = 0; inx < alRemoveDuplicate.size(); inx++) {
					HashMap<String, Object> hmRow = alRemoveDuplicate.get(inx);
					Object[] oColumns = hmRow.keySet().toArray();

					Object oFuncionNo = hmRow.get("FUNCTION_NO");
					Label lblFuncionNo = new Label(0, inx + 2, oFuncionNo.toString(), wcfValueCenter);
					sheetEndItems.addCell(lblFuncionNo);

					Object oLV = hmRow.get("LV");
					Label lblLV = new Label(1, inx + 2, oLV.toString(), wcfValueCenter);
					sheetEndItems.addCell(lblLV);

					Object oPartNo = hmRow.get("CHILD_NO");
					Label lblPartNo = new Label(2, inx + 2, oPartNo.toString(), wcfValueCenter);
					sheetEndItems.addCell(lblPartNo);

					Object oPartName = hmRow.get("CHILD_NAME");
					Label lblPartName = new Label(3, inx + 2, oPartName.toString(), wcfValueLeft);
					sheetEndItems.addCell(lblPartName);

					Object oSMode = hmRow.get("SUPPLY_MODE");
					Label lblSMode = new Label(4, inx + 2, oSMode.toString(), wcfValueCenter);
					sheetEndItems.addCell(lblSMode);

					int iTemp = 0;
					for (int jnx = 0; jnx < alSpec.size(); jnx++) {
						String sSpec = alSpec.get(jnx);
						for (int knx = 0; knx < oColumns.length; knx++) {
							String sColumn = oColumns[knx].toString();
							if (!(sColumn.equals("FUNCTION_NO") || sColumn.equals("LV") || sColumn.equals("CHILD_NO") || sColumn.equals("CHILD_NAME") || sColumn.equals("SUPPLY_MODE") || sColumn.equals("CONDITION"))) {
								if (sColumn.equals(sSpec)) {
									String sValue = hmRow.get(sColumn).toString();
									Label lblValue = new Label(5 + iTemp, inx + 2, sValue, wcfValueCenter);
									sheetEndItems.addCell(lblValue);

									iTemp++;
								}
							}
						}
					}

					Object oOption = hmRow.get("CONDITION");
					Label lblOption = new Label(5 + iTemp--, inx + 2, oOption == null ? "-" : oOption.toString(), wcfValueLeft);
					sheetEndItems.addCell(lblOption);
				}

				sheetAutoFitColumns(sheetSpecs);
				sheetAutoFitColumns(sheetEndItems);

				workBook.write();
				workBook.close();

				try {
					AIFShell aif = new AIFShell("application/vnd.ms-excel", selectedFile.getAbsolutePath());
					aif.start();
				} catch (Exception e) {
					e.printStackTrace();
					MessageBox.post(parentDlg, e.getMessage(), "ERROR", MessageBox.ERROR);
				}
			}
		} catch (Exception e) {
			MessageBox.post(e);
		}
	}

	class CollectSpecEndItem extends Thread {
		private WaitProgressBar waitProgress = null;
		private String spec;
		private String product;
		private String type;
		private String puid;
		private String specKey = null;
		private String rowkey;

		public CollectSpecEndItem(WaitProgressBar waitProgress, String spec, String product, String type, String puid, String rowkey) {
			this.waitProgress = waitProgress;
			this.spec = spec;
			this.product = product;
			this.type = type;
			this.puid = puid;
			this.specKey = this.spec + "_" + this.puid;
			this.rowkey = rowkey;
		}

		public void run() {
			try {
				synchronized (waitProgress) {
					waitProgress.setStatus(spec + " start...");
//					System.out.println("Collect Start (" + spec + ") : "+ new Date());
				}

				ArrayList<HashMap<String, Object>> nameList = getSpecEndItemNameList(product, type, puid, rowkey);
				specEndItemResult.put(spec, nameList);

				synchronized (PngVerificationPanel.this) {
					endThreadCount++;
					waitProgress.setStatus(spec + " End(" + endThreadCount + "/" + specTable.getRowCount() + ")");
//					System.out.println("Collect End (" + spec + ") : "+ new Date());
				}
			} catch (Exception e) {
				if (waitProgress != null) {
					waitProgress.setStatus(e.getMessage());
					waitProgress.setShowButton(true);
				}
				e.printStackTrace();
			}
		}
	}

	class VerifyOperation extends AbstractAIFOperation {

		private WaitProgressBar waitProgress;
		boolean isExceptionFired = false;

		public VerifyOperation(WaitProgressBar waitProgress) {
			this.waitProgress = waitProgress;
		}

		@Override
		public void executeOperation() throws Exception {
			try {
				verify(waitProgress);
			} catch (Exception e) {
				if (this.waitProgress != null) {
					waitProgress.setShowButton(true);
				}
				
				isExceptionFired = true;
				throw e;
			}
		}

	}

	class BomExpander extends Thread {

		private WaitProgressBar waitProgress = null;
		private String spec;
		private String product;
		private String type;
		private String puid;
		private String specKey = null;
		private String rowkey;

		public BomExpander(WaitProgressBar waitProgress, String spec, String product, String type, String puid, String rowkey) {
			this.waitProgress = waitProgress;
			this.spec = spec;
			this.product = product;
			this.type = type;
			this.puid = puid;
			this.specKey = this.spec + "_" + this.puid;
			this.rowkey = rowkey;
		}

		public void run() {
			try {
				synchronized (waitProgress) {
					waitProgress.setStatus(spec + " start...");
//					System.out.println(spec + " Start Time : "+ new Date());
				}

				synchronized (parentDlg.specMap) {
					parentDlg.specMap.put(specKey, getSpec(type, puid));
				}

				ArrayList<HashMap<String, Object>> nameList = getEndItemNameList(product, type, puid, rowkey);
				verificationResult.put(specKey, nameList);

				// ArrayList<HashMap<String, Object>> list = getSumResult(nameList);
				HashMap<String, NameGroupCountResult> resultMap = getResult(nameList);
				String[] groupIds = resultMap.keySet().toArray(new String[resultMap.size()]);
				synchronized (resultGroupData) {
					for (String groupId : groupIds) {
						NameGroupCountResult ngResult = resultMap.get(groupId);
						HashMap<String, NameGroupCountResult> dataMap = resultGroupData.get(groupId);
						if (dataMap == null) {
							dataMap = new HashMap();
							dataMap.put(specKey, ngResult);
							resultGroupData.put(groupId, dataMap);
						} else {
							if (!dataMap.containsKey(specKey)) {
								dataMap.put(specKey, ngResult);
							}
						}
					}
				}

				synchronized (PngVerificationPanel.this) {
					endThreadCount++;
					waitProgress.setStatus(spec + " End(" + endThreadCount + "/" + specTable.getRowCount() + ")");
//					System.out.println(spec + " End Time : "+ new Date());
				}
			} catch (Exception e) {
				if (waitProgress != null) {
					waitProgress.setStatus(e.getMessage());
					waitProgress.setShowButton(true);
				}
				e.printStackTrace();
			}
		}

		private HashMap<String, NameGroupCountResult> getResult(ArrayList<HashMap<String, Object>> list) {
			HashMap<String, NameGroupCountResult> tmpMap = new HashMap();
			for (HashMap<String, Object> map : list) {
				String groupID = (String) map.get("GROUP_ID");
				String childName = (String) map.get("CHILD_NAME");
				BigDecimal cntObj = (BigDecimal) map.get("NAME_COUNT");

				NameGroupCountResult ngResult = tmpMap.get(groupID);
				if (ngResult == null) {
					ngResult = new NameGroupCountResult();
					tmpMap.put(groupID, ngResult);
				}
				ngResult.addNameCount(childName, cntObj.intValue());
			}
			return tmpMap;
		}

		private ArrayList<HashMap<String, Object>> getSumResult(ArrayList<HashMap<String, Object>> list) {
			ArrayList<HashMap<String, Object>> countSumList = new ArrayList();
			HashMap<String, Integer> tmpMap = new HashMap();
			for (HashMap<String, Object> map : list) {
				String groupID = (String) map.get("GROUP_ID");
				String childName = (String) map.get("CHILD_NAME");
				BigDecimal cntObj = (BigDecimal) map.get("NAME_COUNT");

				Integer iObj = tmpMap.get(groupID);
				if (iObj == null) {
					tmpMap.put(groupID, new Integer(cntObj.intValue()));
				} else {
					tmpMap.put(groupID, new Integer(iObj.intValue() + cntObj.intValue()));
				}
				// nameList.GROUP_ID, p_master.group_name, p_master.default_qty, a.function_no, a.child_no, a.child_name, a.condition, a.supply_mode, a.lv
			}

			String[] groupIds = tmpMap.keySet().toArray(new String[tmpMap.size()]);
			for (int i = 0; groupIds != null && i < groupIds.length; i++) {
				Integer iObj = tmpMap.get(groupIds[i]);
				HashMap<String, Object> map = new HashMap();
				map.put("GROUP_ID", groupIds[i]);
				map.put("NAME_COUNT", iObj);
				countSumList.add(map);
			}

			return countSumList;
		}
	}

	/**
	 * [SR160412-013][20160414][jclee] Spec Export
	 */
	private void exportSpec() {
		int iRowCount = specTable.getRowCount();
		int iColCount = specTable.getColumnCount();
		ArrayList<String> alHeaders = new ArrayList<String>();
		ArrayList<HashMap<String, String>> alDatas = new ArrayList<HashMap<String, String>>();

		if (!(iRowCount > 0)) {
			MessageBox.post("Select Specs.", "Error", MessageBox.ERROR);
			return;
		}

		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		Calendar now = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddkkmmss");
		sdf.format(now.getTime());
		String sFileName = "";
		sFileName = "Spec_Result_" + lbTargetProd.getText() + "_" + sdf.format(now.getTime()) + ".xls";
		File defaultFile = new File(sFileName);
		fileChooser.setSelectedFile(defaultFile);
		fileChooser.setFileFilter(new FileFilter() {
			public boolean accept(File f) {
				if (f.isFile()) {
					return f.getName().endsWith("xls");
				}
				return false;
			}

			public String getDescription() {
				return "*.xls";
			}
		});
		int result = fileChooser.showSaveDialog(parentDlg);
		if (result == JFileChooser.APPROVE_OPTION) {
			File selectedFile = fileChooser.getSelectedFile();
			try {
				// Selected Spec Data 수집
				DefaultTableModel model = (DefaultTableModel) specTable.getModel();

				String sHeaderSpecNo = model.getColumnName(0);
				String sHeaderType = model.getColumnName(1);
				String sHeaderCreateDate = model.getColumnName(2);
				String sHeaderPUID = model.getColumnName(3);
				
				alHeaders.add(sHeaderSpecNo);
				alHeaders.add(sHeaderType);
				alHeaders.add(sHeaderCreateDate);
				alHeaders.add(sHeaderPUID);
				
				for (int inx = 0; inx < iRowCount; inx++) {
					HashMap<String, String> hmData = new HashMap<String, String>();
					int iModelRow = specTable.convertRowIndexToModel(inx);
					
					String sSpec = (String) model.getValueAt(iModelRow, 0);
					String sType = (String) model.getValueAt(iModelRow, 1);
					
					String sCreateDate = "";
					Object oCreateDate = model.getValueAt(iModelRow, 2);
					if (oCreateDate instanceof java.sql.Timestamp) {
						java.sql.Timestamp ts = (java.sql.Timestamp) oCreateDate;
						sCreateDate = (new java.sql.Date(ts.getTime())).toString();
					} else {
						sCreateDate = (String) oCreateDate;
					}
					
					String sPUID = (String) model.getValueAt(iModelRow, 3);
					
					hmData.put(sHeaderSpecNo, sSpec);
					hmData.put(sHeaderType, sType);
					hmData.put(sHeaderCreateDate, sCreateDate);
					hmData.put(sHeaderPUID, sPUID);
					
					alDatas.add(hmData);
				}
				

				WritableWorkbook workBook = Workbook.createWorkbook(selectedFile);
				// 0번째 Sheet 생성
				WritableSheet sheet = workBook.createSheet("Spec", 0);

				WritableCellFormat cfValue = new WritableCellFormat();
				cfValue.setBorder(Border.ALL, BorderLineStyle.THIN);
				cfValue.setAlignment(Alignment.CENTRE);
				cfValue.setFont(new WritableFont(WritableFont.ARIAL, 9, WritableFont.NO_BOLD, false, UnderlineStyle.NO_UNDERLINE, Colour.BLACK));
				cfValue.setVerticalAlignment(VerticalAlignment.CENTRE);

				WritableCellFormat cfHeader = new WritableCellFormat();
				cfHeader.setBorder(Border.ALL, BorderLineStyle.THIN);
				cfHeader.setAlignment(Alignment.CENTRE);
				cfHeader.setFont(new WritableFont(WritableFont.ARIAL, 9, WritableFont.BOLD, false, UnderlineStyle.NO_UNDERLINE, Colour.BLACK));
				cfHeader.setWrap(true);
				cfHeader.setBackground(Colour.GREY_25_PERCENT);

				int iHeaderRow = 0;
				int iStartRow = 1;

				// Header
				for (int inx = 0; inx < alHeaders.size(); inx++) {
					String sHeader = alHeaders.get(inx);
					jxl.write.Label lblHeader = new jxl.write.Label(inx, iHeaderRow, sHeader, cfHeader);
					sheet.addCell(lblHeader);
				}

				// Values
				for (int inx = 0; inx < alDatas.size(); inx++) {
					HashMap<String, String> hmData = alDatas.get(inx);

					for (int jnx = 0; jnx < alHeaders.size(); jnx++) {
						String sHeader = alHeaders.get(jnx);
						String sValue = hmData.get(sHeader);

						jxl.write.Label lblValue = new jxl.write.Label(jnx, iStartRow + inx, sValue, cfValue);
						sheet.addCell(lblValue);
					}
				}

				sheetAutoFitColumns(sheet);

				workBook.write();
				workBook.close();

				AIFShell aif = new AIFShell("application/vnd.ms-excel", selectedFile.getAbsolutePath());
				aif.start();
			} catch (Exception e) {
				e.printStackTrace();
				MessageBox.post(parentDlg, e.getMessage(), "ERROR", MessageBox.ERROR);
			}
		}
	}

	/**
	 * [SR160412-013][20160414][jclee] Spec import
	 */
	private void importSpec() {
		try {
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			fileChooser.setFileFilter(new FileFilter() {
				public boolean accept(File f) {
					if (f.isFile()) {
						return f.getName().endsWith("xls") || f.getName().endsWith("xlsx");
					}
					return false;
				}

				public String getDescription() {
					return "*.xls;*.xlsx";
				}
			});
			int result = fileChooser.showOpenDialog(parentDlg);
			if (result == JFileChooser.APPROVE_OPTION) {
				File file = fileChooser.getSelectedFile();
				InputStream is = new FileInputStream(file);
				
				String sFileName = file.getName();
				String sExt = sFileName.substring(sFileName.lastIndexOf(".") + 1);
				
		        org.apache.poi.ss.usermodel.Workbook workBook = null;
		        
				if (sExt.equalsIgnoreCase("xlsx")) {
					workBook = new XSSFWorkbook(is);
				} else if (sExt.equalsIgnoreCase("xls")) {
					workBook = new HSSFWorkbook(is);
				} else {
					is.close();
					return;
				}
				
				Sheet sheet = workBook.getSheetAt(0);
				
				int iStartRow = 1;
				
				int iRows = sheet.getPhysicalNumberOfRows();
				int iCols = sheet.getRow(0).getPhysicalNumberOfCells();
				
				ArrayList<Vector> alDatas = new ArrayList<Vector>();
				
				for (int inx = iStartRow; inx < iRows; inx++) {
					Vector vData = new Vector();
					
					org.apache.poi.ss.usermodel.Cell cSpecNo = sheet.getRow(inx).getCell(0);
					org.apache.poi.ss.usermodel.Cell cType = sheet.getRow(inx).getCell(1);
					org.apache.poi.ss.usermodel.Cell cCreateDate = sheet.getRow(inx).getCell(2);
					org.apache.poi.ss.usermodel.Cell cPUID = sheet.getRow(inx).getCell(3);
					
					if (cSpecNo == null || cSpecNo.getStringCellValue().equals("")) {
						MessageBox.post("Spec No cannot be null or empty.", "Error", MessageBox.ERROR);
						is.close();
						return;
					}
					
					if (cType == null || cType.getStringCellValue().equals("")) {
						MessageBox.post("Spec Type cannot be null or empty.", "Error", MessageBox.ERROR);
						is.close();
						return;
					}
					
					String sSpecNo = cSpecNo.getStringCellValue();
					String sType = cType.getStringCellValue();
					
					vData.add(sSpecNo);
					vData.add(sType);
					vData.add(cCreateDate == null || cCreateDate.getStringCellValue().equals("") ? "" : cCreateDate.getStringCellValue());
					
					if (sType.equals("USER SPEC")) {
						String sPUID = cPUID == null || cPUID.getStringCellValue().equals("") ? "" : cPUID.getStringCellValue();
						
						if (sPUID == null || sPUID.equals("") || sPUID.length() == 0) {
							MessageBox.post("A PUID cannot be null or empty of the User Spec.", "Error", MessageBox.ERROR);
							is.close();
							return;
						}
						
						vData.add(sPUID);
					} else {
						// Spec No 총 자리수 : 15
						if (sSpecNo.length() > 15) {
							MessageBox.post(sSpecNo + "(" + sSpecNo.length() + ") is not available for verify.", "Error", MessageBox.ERROR);
							is.close();
							return;
						}
						
						// 자리수가 맞지 않는 경우 Spec 앞에 공백 입력
						while (sSpecNo.length() != 15) {
							sSpecNo = " " + sSpecNo;
						}
						
						vData.add(sSpecNo);
					}
					
					alDatas.add(vData);
				}
				
				for (int inx = 0; inx < alDatas.size(); inx++) {
					Vector vData = alDatas.get(inx);
					addSpec(vData);
				}
					
				is.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
			MessageBox.post(parentDlg, e.getMessage(), "ERROR", MessageBox.ERROR);
		}
	}
	
	/**
	 * Verify 시 Excel 파일 선택
	 * @return
	 */
	private File selectVerifyExportFile() {
		File selectedFile = null;
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		Calendar now = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddkkmmss");
		sdf.format(now.getTime());
		String sFileName = "";
		String product = (String) cbProduct.getSelectedItem();
		sFileName = "NameGroupVerification_Result_" + product + "_" + sdf.format(now.getTime()) + ".xlsx";
		File defaultFile = new File(sFileName);
		fileChooser.setSelectedFile(defaultFile);
		fileChooser.setFileFilter(new FileFilter() {
			public boolean accept(File f) {
				if (f.isFile()) {
					return f.getName().endsWith("xlsx");
				}
				return false;
			}

			public String getDescription() {
				return "*.xlsx";
			}
		});
		int result = fileChooser.showSaveDialog(parentDlg);
		if (result == JFileChooser.APPROVE_OPTION) 
			selectedFile = fileChooser.getSelectedFile();
		return selectedFile;
	}
	
	/**
	 * Template 파일 다운로드
	 */
	private void doDownloadTemplate()
	{
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		Calendar now = Calendar.getInstance();
		String templateFileName = "ssangyong_verification_target_spec_template.xls";
		
		File defaultFile = new File(templateFileName);
		fileChooser.setSelectedFile(defaultFile);
		fileChooser.setFileFilter(new FileFilter() {
			public boolean accept(File f) {
				if (f.isFile()) {
					return f.getName().endsWith("xls");
				}
				return false;
			}

			public String getDescription() {
				return "*.xls";
			}
		});
		int result = fileChooser.showSaveDialog(parentDlg);
		
		File selectedFile = null;
		if (result == JFileChooser.APPROVE_OPTION) 
			selectedFile = fileChooser.getSelectedFile();
		
		if(selectedFile == null)
			return;
		
		if (selectedFile.exists()) {
			int ret = JOptionPane.showConfirmDialog(null, selectedFile.getAbsolutePath() + " File already exists.\nDo you want to overwrite it?", "Confirm", JOptionPane.YES_NO_OPTION);
			if (ret != JOptionPane.YES_OPTION)
    			return;
		}
		
		TCSession tcSession = CustomUtil.getTCSession();
		
		try {
			File tempFile = SYMTcUtil.getTemplateFile(tcSession, templateFileName, null);
			if (selectedFile.exists())
				selectedFile.delete();
		
			tempFile.renameTo(new File(selectedFile.getAbsolutePath()));
			MessageBox.post(parentDlg,  "Upload template file was downloaded.\n("+selectedFile.getAbsolutePath() +")", "Information", MessageBox.INFORMATION);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	/**
	 * SR180410-037
	 * beenlaho
	 * SpecOptonChangeInfo Test용 데이터 생성
	 */
	private ArrayList<SpecOptionChangeInfo> makeTestDataForSpecOptionChangeInfo() throws Exception {
		
		List<String> addOptions = new ArrayList<String>();
		addOptions.add("3W02");
		addOptions.add("3W17");
		
		List<String> changeOptions = new ArrayList<String>();
		changeOptions.add("C02A");
//		changeOptions.add("C20H");
		
		List<String> deleteOptions = new ArrayList<String>();
		deleteOptions.add("C20H");
		deleteOptions.add("F05M");
		
		ArrayList<SpecOptionChangeInfo> testDataList = new ArrayList<SpecOptionChangeInfo> ();
		
		
		String specNo1 = "EL0F0XB5 JD0000";
		
		for (Iterator iterator = addOptions.iterator(); iterator.hasNext();) {
			
			String option = (String) iterator.next();
			SpecOptionChangeInfo data = new SpecOptionChangeInfo();
			data.setSpecNo(specNo1);
			data.setOptions(option);
			data.setFlag(SpecOptionChangeInfo.ADD_FLAG);
			testDataList.add(data);
			
		}
		
		
		for (Iterator iterator = changeOptions.iterator(); iterator.hasNext();) {
			
			String option = (String) iterator.next();
			SpecOptionChangeInfo data = new SpecOptionChangeInfo();
			data.setSpecNo(specNo1);
			data.setOptions(option);
			data.setFlag(SpecOptionChangeInfo.CHANGE_FLAG);
			testDataList.add(data);
		}
		
		for (Iterator iterator = deleteOptions.iterator(); iterator.hasNext();) {
			
			String option = (String) iterator.next();
			SpecOptionChangeInfo data = new SpecOptionChangeInfo();
			data.setSpecNo(specNo1);
			data.setOptions(option);
			data.setFlag(SpecOptionChangeInfo.DELETE_FLAG);
			testDataList.add(data);
		}
		
		
		String specNo2 = "EL0F0XB5 JD0001";
		
		List<String> addOptions1 = new ArrayList<String>();
		addOptions1.add("3W09");
		addOptions1.add("3W51");
		
		List<String> changeOptions1 = new ArrayList<String>();
		changeOptions1.add("B00L");
		changeOptions1.add("C09G");
		
		List<String> deleteOptions1 = new ArrayList<String>();
		deleteOptions1.add("S51X");
		deleteOptions1.add("U10X");
		
		for (Iterator iterator = addOptions1.iterator(); iterator.hasNext();) {
			
			String option = (String) iterator.next();
			SpecOptionChangeInfo data = new SpecOptionChangeInfo();
			data.setSpecNo(specNo2);
			data.setOptions(option);
			data.setFlag(SpecOptionChangeInfo.ADD_FLAG);
			testDataList.add(data);
			
		}
		
		
		for (Iterator iterator = changeOptions1.iterator(); iterator.hasNext();) {
			
			String option = (String) iterator.next();
			SpecOptionChangeInfo data = new SpecOptionChangeInfo();
			data.setSpecNo(specNo2);
			data.setOptions(option);
			data.setFlag(SpecOptionChangeInfo.CHANGE_FLAG);
			testDataList.add(data);
		}
		
		for (Iterator iterator = deleteOptions1.iterator(); iterator.hasNext();) {
			
			String option = (String) iterator.next();
			SpecOptionChangeInfo data = new SpecOptionChangeInfo();
			data.setSpecNo(specNo2);
			data.setOptions(option);
			data.setFlag(SpecOptionChangeInfo.DELETE_FLAG);
			testDataList.add(data);
		}
		
		return testDataList;
	}
}

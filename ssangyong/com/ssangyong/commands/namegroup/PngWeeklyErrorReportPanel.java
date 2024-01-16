package com.ssangyong.commands.namegroup;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

import com.ssangyong.commands.namegroup.model.PngWeeklyReportData;
import com.ssangyong.common.remote.DataSet;
import com.ssangyong.common.remote.SYMCRemoteUtil;
import com.ssangyong.common.ui.RowNoTableRowHeader;
import com.ssangyong.common.utils.CustomUtil;
import com.ssangyong.common.utils.ExcelService;
import com.teamcenter.rac.aif.AIFShell;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.DateButton;
import com.teamcenter.rac.util.MessageBox;

/**
 * 주간 오류 결과 리스트 UI
 * [20170614][ljg] Model Year 컬럼 추가 및 Model Year 표시
 * [20170615][ljg] 검색 결과 Excel Export
 */
// 2024.01.09 수정
public class PngWeeklyErrorReportPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JTable resultTable;
	private DefaultTableModel tableModel = null;

	private JTextField txtSpecNo;

	private DateButton fromDateButton = null;
	private DateButton endDateButton;
	private JButton btnSearch = null;;

	private JComboBox comboBoxProductNo = null;

	private Vector<String> productNoList = null; // ProjectNo 리스트

	private PngDlg parentDialog = null;
	private TCSession tcSession = null;

	private boolean isInitDataLoaded = false; // 초기 데이타 로드 되었는지 유무

	public PngWeeklyErrorReportPanel(PngDlg parentDialog) {
		this.parentDialog = parentDialog;
		tcSession = CustomUtil.getTCSession();
		initUI();
	}

	/**
	 * Create the panel.
	 */
	public void initUI() {
		setLayout(new BorderLayout(0, 0));

		try {
			JPanel topPanel = new JPanel();
			add(topPanel, BorderLayout.NORTH);
			topPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 11));

			JLabel lblProjectNo = new JLabel("Product");
			topPanel.add(lblProjectNo);
			comboBoxProductNo = new JComboBox(new Vector<String>());
			topPanel.add(comboBoxProductNo);

			JLabel lblSepec = new JLabel("Spec. No");
			topPanel.add(lblSepec);

			txtSpecNo = new JTextField();
			topPanel.add(txtSpecNo);
			txtSpecNo.setColumns(10);

			JLabel lblFromDate = new JLabel("From");
			topPanel.add(lblFromDate);

			Date initDate = null;
			fromDateButton = new DateButton();
			fromDateButton.setDate(initDate);
			fromDateButton.setDisplayFormat("yyyy-MM-dd");
			topPanel.add(fromDateButton);

			JLabel lblDash = new JLabel("~");
			topPanel.add(lblDash);

			JLabel lblTo = new JLabel("To");
			topPanel.add(lblTo);

			endDateButton = new DateButton();
			endDateButton.setDate(initDate);
			endDateButton.setDisplayFormat("yyyy-MM-dd");
			topPanel.add(endDateButton);

			btnSearch = new JButton("Search");
			btnSearch.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent paramActionEvent) {
					doSearch();
				}
			});
			topPanel.add(btnSearch);
			btnSearch.requestFocus();
			
			JButton btnExport = new JButton("Export");
			btnExport.setIcon(new ImageIcon(PngWeeklyErrorReportPanel.class.getResource("/com/ssangyong/common/images/excel_16.png")));
			btnExport.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent paramActionEvent) {
					if(tableModel.getRowCount() <= 0){
						MessageBox.post(parentDialog, "Result is Empty", "Warning", MessageBox.WARNING);
						return;
					}
					excelExport();
				}
			});
			topPanel.add(btnExport);

			Vector<String> headerVec = new Vector<String>();
			headerVec.add("Product No");
			headerVec.add("Group ID");
			headerVec.add("Group Name");
			headerVec.add("Spec No.");
			headerVec.add("Error Reason");
			headerVec.add("Date");
			//[20170613][ljg] 추가
			headerVec.add("M/Y");

			tableModel = new DefaultTableModel(null, headerVec);

			resultTable = new JTable(tableModel);
			resultTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

			TableColumnModel tableColModel = resultTable.getColumnModel();

			int width[] = { 80, 80, 220, 200, 360, 100, 100 };
			for (int i = 0; i < tableColModel.getColumnCount(); i++) {
				tableColModel.getColumn(i).setPreferredWidth(width[i]);
			}

			TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(tableModel);
			resultTable.setRowSorter(sorter);

			JScrollPane resultScrollPane = new JScrollPane(resultTable);
			resultScrollPane.setPreferredSize(new Dimension(400, 400));
			add(resultScrollPane, BorderLayout.CENTER);

			RowNoTableRowHeader rowNoHeader = new RowNoTableRowHeader(resultScrollPane, resultTable);
			rowNoHeader.setBackground(Color.LIGHT_GRAY);
			resultScrollPane.setRowHeaderView(rowNoHeader);

		} catch (Exception ex) {
			ex.printStackTrace();
			MessageBox.post(parentDialog, ex.toString(), "ERROR", MessageBox.ERROR);
		}

	}

	/**
	 * UI 초기 값 설정
	 */
	public void initLoadData() {
		// 이미 데이터가 한번 로드 되었으면 PASS
		if (isInitDataLoaded)
			return;
		try {
			/**
			 * Product No 리스트 Combo 값 설정
			 */
			productNoList = getProductNoList();
			comboBoxProductNo.setModel(new DefaultComboBoxModel(productNoList));
			/**
			 * 최종 수행 날짜 자동 입력
			 */
			String lastDateStr = getPngWeeklyRepLastDate();
			SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd");

			
			if (lastDateStr != null && !"".equals(lastDateStr) ) {
				Date lastDate = sd.parse(lastDateStr);
				fromDateButton.setDate(lastDate);
				endDateButton.setDate(lastDate);
			}
			// 초기 데이터 로드. Tab 이 처음 선택되었을 경우 로드함
			isInitDataLoaded = true;
		} catch (Exception ex) {
			MessageBox.post(parentDialog, ex.toString(), "ERROR", MessageBox.ERROR);
		}

	}

	/**
	 * Product 정보를 가져옴
	 * 
	 * @return
	 * @throws Exception
	 */
	public Vector<String> getProductNoList() throws Exception {
		Vector<String> productNoList = new Vector<String>();
		SYMCRemoteUtil remote = new SYMCRemoteUtil();
		DataSet ds = new DataSet();
		ds.put("DATA", null);
		try {
			@SuppressWarnings("unchecked")
			ArrayList<String> productList = ((ArrayList<String>) remote.execute("com.ssangyong.service.PartNameGroupService", "getPngProdOrder", ds));

			if (productList == null) {
				return productNoList;
			}
			productNoList.add("Select a Product");
			productNoList.addAll(productList);
			return productNoList;

		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * 마지막에 수행한 날짜 정보 가져옴
	 * 
	 * @return
	 * @throws Exception
	 */
	private String getPngWeeklyRepLastDate() throws Exception {
		SYMCRemoteUtil remote = new SYMCRemoteUtil();
		DataSet ds = new DataSet();
		ds.put("DATA", null);
		try {
			String lastDate = (String) remote.execute("com.ssangyong.service.PartNameGroupService", "getPngWeeklyRepLastDate", ds);
			return lastDate;
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * 검색 조건에 맞는 에러 결과 리스트를 조회함
	 */
	private void doSearch() {
		parentDialog.removeAllRow(resultTable);
		PngWeeklyReportData data = new PngWeeklyReportData();

		String productNo = comboBoxProductNo.getSelectedIndex() > 0 ? (String) comboBoxProductNo.getSelectedItem() : null;
		SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd");
		Date fromDate = fromDateButton.getDate();
		Date endDate = endDateButton.getDate();

		data.setProductNo(productNo);
		data.setSpecNo(txtSpecNo.getText().isEmpty() ? null : txtSpecNo.getText());
		data.setFromDate(fromDate == null ? null : sd.format(fromDate));
		data.setEndDate(endDate == null ? null : sd.format(endDate));
		data.setResultTable(resultTable);

		PngWeeklySearchOperation searchOperation = new PngWeeklySearchOperation(data, parentDialog);

		tcSession.queueOperationLater(searchOperation);

	}

	/**
	 * @return the isInitDataLoaded
	 */
	public boolean isInitDataLoaded() {
		return isInitDataLoaded;
	}

	/**
	 * @param isInitDataLoaded
	 *            the isInitDataLoaded to set
	 */
	public void setInitDataLoaded(boolean isInitDataLoaded) {
		this.isInitDataLoaded = isInitDataLoaded;
	}
	
	/**
	 * 검색 결과 Excel Export
	 * @Copyright : Plmsoft
	 * @author : 이정건
	 * @since  : 2017. 6. 14.
	 */
	private void excelExport(){
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY );
		Calendar now = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddkkmmss");
		sdf.format(now.getTime());
		File defaultFile = new File("WeeklyErrorReport" +"_" + sdf.format(now.getTime()) + ".xls");
		fileChooser.setSelectedFile(defaultFile);
		fileChooser.setFileFilter(new FileFilter(){

			public boolean accept(File f) {
				if( f.isFile()){
					return f.getName().endsWith("xls");
				}
				return false;
			}

			public String getDescription() {
				return "*.xls";
			}
		});
		int result = fileChooser.showSaveDialog(parentDialog);
		if( result == JFileChooser.APPROVE_OPTION){
			File selectedFile = fileChooser.getSelectedFile();
			try
            {
				exportToExcel(selectedFile);
				AIFShell aif = new AIFShell("application/vnd.ms-excel", selectedFile.getAbsolutePath());
				aif.start();
            }
            catch (Exception e)
            {
            	e.printStackTrace();
            	MessageBox.post(parentDialog, e.getMessage(), "ERROR", MessageBox.ERROR);
            }
		}
	}
	
	private void exportToExcel(File selectedFile) throws RowsExceededException, WriteException, IOException{

	    String[] columns = {"Product No.", "Group ID", "Group Name", "SPEC No.", "Error Reason", "Date", "M/Y"};
	    String columnsWidth[] = { "80", "80", "220", "200", "360", "100", "100" };
	    
	    ExcelService.createService();
	    ExcelService.downloadTable(selectedFile, resultTable, columns, columnsWidth);
	    
	}
}

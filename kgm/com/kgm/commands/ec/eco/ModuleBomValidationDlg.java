package com.kgm.commands.ec.eco;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import jxl.CellView;
import jxl.Workbook;
import jxl.format.Alignment;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.Colour;
import jxl.format.UnderlineStyle;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

import org.eclipse.swt.widgets.TableItem;

import com.kgm.commands.ec.eco.module.ModuleBomTableCellRenderer;
import com.kgm.commands.ec.eco.module.ModuleBomValidationInfo;
import com.kgm.common.WaitProgressBar;
import com.kgm.common.remote.DataSet;
import com.kgm.common.remote.SYMCRemoteUtil;
import com.kgm.rac.kernel.SYMCBOMEditData;
import com.teamcenter.rac.aif.AIFShell;
import com.teamcenter.rac.aif.AbstractAIFDialog;

/**
 * 모듈 BOM 검증 결과 창.
 *[SR140722-022][20140522] swyoon Module BOM 검증 결과 창.
 */
@SuppressWarnings({"serial", "rawtypes", "unchecked", "unused"})
public class ModuleBomValidationDlg extends AbstractAIFDialog {

	private final JPanel contentPanel = new JPanel();
	private JTable table;
	private Vector header = new Vector();
	private final String[] titles = { "No", "Parent_NO", "Part_NO", "S_Mode", "M_Code", "Condition", "EPL_ID" };
	private final int[] columnWidth = {30, 100, 100, 60, 60, 200, 1};
	
	private String ecoNo;
	private HashMap<String, ArrayList<ModuleBomValidationInfo>> validationResultMap = new HashMap();
	private ArrayList<String> validationErrorList = new ArrayList();
	private ArrayList<String> validationWarningList = new ArrayList();
	private ArrayList<HashMap<String, String>> result = null;
	private JTextArea msgArea = null;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			ModuleBomValidationDlg dialog = new ModuleBomValidationDlg(null, null,  null);
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 * @throws Exception 
	 */
	public ModuleBomValidationDlg(String ecoNo, org.eclipse.swt.widgets.Button moduleChkBtn, ArrayList<org.eclipse.swt.widgets.Button> btnList) throws Exception {
		setTitle("Module BOM Validation");
		this.ecoNo = ecoNo;
		
		try {
			result = getValidationResult(ecoNo);
			
		} catch (Exception e1) {
			throw e1;
		}
		
		// Validation 결과를 분리함.
		analyzeValidationResult(result, btnList);
		moduleChkBtn.setEnabled(false);
		
		setAlwaysOnTop(true);
		setBounds(100, 100, 578, 416);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBackground(Color.WHITE);
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));
		JSplitPane splitPane = new JSplitPane();
		splitPane.setDividerSize(10);
		splitPane.setBackground(Color.WHITE);
		splitPane.setOneTouchExpandable(true);
		splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		splitPane.setResizeWeight(0.8);
		splitPane.setDividerLocation(0.8);
		
		header = new Vector();
		for( String title : titles){
			header.add(title);
		}
		
		{
			JPanel panel = new JPanel();
			FlowLayout flowLayout = (FlowLayout) panel.getLayout();
			flowLayout.setAlignment(FlowLayout.LEADING);
			panel.setBorder(new TitledBorder(null, "Validation Result", TitledBorder.LEADING, TitledBorder.TOP, null, null));
			contentPanel.add(panel, BorderLayout.NORTH);
			{
				JLabel errLabel = new JLabel("Error Count :");
				errLabel.setForeground(Color.RED);
				panel.add(errLabel);
			}
			{
				JLabel errCntLabel = new JLabel("" + validationErrorList.size());
				panel.add(errCntLabel);
			}
			{
				JLabel emptyLabel = new JLabel("     ");
				panel.add(emptyLabel);
			}
			{
				JLabel warningLabel = new JLabel("Warning Count :");
				
				warningLabel.setForeground(new Color(63, 72, 204));
				panel.add(warningLabel);
			}
			{
				JLabel warningCntLabel = new JLabel("" + validationWarningList.size());
				panel.add(warningCntLabel);
			}
			{
				JLabel emptyLabel2 = new JLabel("     ");
				panel.add(emptyLabel2);
			}
			{
				final JCheckBox chckbxNewCheckBox = new JCheckBox("Only Problem Items");
				chckbxNewCheckBox.setSelected(true);
				chckbxNewCheckBox.addItemListener(new ItemListener() {
					public void itemStateChanged(ItemEvent itemevent) {
						table.clearSelection();
						DefaultTableModel model = (DefaultTableModel)table.getModel();
						model.setDataVector(getEpl(chckbxNewCheckBox.isSelected()), header);
						setTableColumn();
					}
				});
				
				panel.add(chckbxNewCheckBox);
			}
		}
		
		contentPanel.add(splitPane);
		{
			JPanel panel = new JPanel();
			splitPane.setLeftComponent(panel);
			panel.setLayout(new BorderLayout(0, 0));
			{
				
				DefaultTableModel model = new DefaultTableModel(getEpl(true), header) {
					public Class getColumnClass(int col) {
						return String.class;
					}

					public boolean isCellEditable(int row, int col) {
						return false;
					}
			    };
				table = new JTable(model);
				table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
					
					@Override
					public void valueChanged(ListSelectionEvent listselectionevent) {
						String msg = null;
						DefaultTableModel model = (DefaultTableModel)table.getModel();
						int modelRow = table.convertRowIndexToModel(table.getSelectedRow());
						if( modelRow < 0 ) return;
						String eplId = (String)model.getValueAt(modelRow, 6);
						if( validationResultMap.containsKey(eplId)){
							ArrayList<ModuleBomValidationInfo> list = validationResultMap.get(eplId);
							for( ModuleBomValidationInfo info : list){
								if( msg == null){
									msg = info.getMsg();
								}else{
									msg += "\n" + info.getMsg();
								}
							}
							msgArea.setText(msg);
						}else{
							msgArea.setText("");
						}
						
						
					}
				});
				
				setTableColumn();
				TableCellRenderer rendererFromHeader = table.getTableHeader().getDefaultRenderer();
				JLabel headerLabel = (JLabel)rendererFromHeader;
				headerLabel.setHorizontalAlignment(JLabel.CENTER);
				
				table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				JScrollPane scrollPane = new JScrollPane();
				scrollPane.setViewportView(table);
				scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
				scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
				scrollPane.getViewport().setBackground(Color.WHITE);
				panel.add(scrollPane, BorderLayout.CENTER);
			}
		}
		{
			JPanel panel = new JPanel();
			splitPane.setRightComponent(panel);
			panel.setLayout(new BorderLayout(0, 0));
			{
				msgArea = new JTextArea();
				msgArea.setEditable(false);
				JScrollPane scrollPane = new JScrollPane();
				scrollPane.setViewportView(msgArea);
				panel.add(scrollPane, BorderLayout.CENTER);
			}
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("Excel");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						
						JFileChooser fileChooser = new JFileChooser();
						fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY );
						Calendar now = Calendar.getInstance();
						SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddkkmmss");
						sdf.format(now.getTime());
						File defaultFile = new File("Module_Val_" + ModuleBomValidationDlg.this.ecoNo + "_" + sdf.format(now.getTime()) + ".xls");
						fileChooser.setSelectedFile(defaultFile);
						fileChooser.setFileFilter(new FileFilter(){

							@Override
							public boolean accept(File f) {
								if( f.isFile()){
									return f.getName().endsWith("xls");
								}
								return false;
							}

							@Override
							public String getDescription() {
								return "*.xls";
							}

						});
						int result = fileChooser.showSaveDialog(ModuleBomValidationDlg.this);
						if( result == JFileChooser.APPROVE_OPTION){
							File selectedFile = fileChooser.getSelectedFile();
							try
				            {
								exportToExcel(selectedFile);
								AIFShell aif = new AIFShell("application/vnd.ms-excel", selectedFile.getAbsolutePath());
								aif.start();
								
				            }
				            catch (Exception ioe)
				            {
				            	ioe.printStackTrace();
				            }finally{
//				            	if( selectedFile != null ){
//				            		OptionSetDialog.this.dispose();
//				            	}
				            }
						}						
						
					}
				});
				okButton.setIcon(new ImageIcon(ModuleBomValidationDlg.class.getResource("/com/kgm/common/images/excel_16.png")));
				okButton.setActionCommand("");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton closeBtn = new JButton("Close");
				closeBtn.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						dispose();
					}
				});
				buttonPane.add(closeBtn);
			}
		}
		
		setVisible(true);
		centerToScreen();
	}
	
	private void exportToExcel(File selectedFile) throws IOException, WriteException{
		WritableWorkbook workBook = Workbook.createWorkbook(selectedFile);
		// 0번째 Sheet 생성
		WritableSheet sheet = workBook.createSheet("new sheet", 0);

		WritableCellFormat cellFormat = new WritableCellFormat(); // 셀의 스타일을
																	// 지정하기 위한
																	// 부분입니다.
		cellFormat.setBorder(Border.ALL, BorderLineStyle.THIN); // 셀의 스타일을
																// 지정합니다. 테두리에
																// 라인그리는거에요
		cellFormat.setWrap(true);
		Label label = null;

		WritableCellFormat headerCellFormat = new WritableCellFormat(); // 셀의
																		// 스타일을
																		// 지정하기
																		// 위한
																		// 부분입니다.
		headerCellFormat.setBorder(Border.ALL, BorderLineStyle.THIN);
		headerCellFormat.setBackground(Colour.GREY_25_PERCENT);

		int startRow = 1;
		int initColumnNum = 1;

		WritableCellFormat titleFormat = new WritableCellFormat();
		titleFormat.setAlignment(Alignment.CENTRE);    
		WritableFont titleFont = new WritableFont(WritableFont.ARIAL, 15, WritableFont.BOLD,false, UnderlineStyle.NO_UNDERLINE, Colour.BLACK);
		titleFormat.setFont(titleFont);
		label = new jxl.write.Label(1, startRow, "Module BOM Validation Report",
				titleFormat);
		sheet.addCell(label);
		sheet.mergeCells(1, 1, 6, 1);

		TableColumnModel cm = table.getColumnModel();
		startRow = 3;
		Vector excelColumnHeader = new Vector();
		for (int i = 0; i < cm.getColumnCount(); i++) {
			TableColumn tc = cm.getColumn(i);
			excelColumnHeader.add(tc.getHeaderValue());
		}

		for (int i = 0; i < excelColumnHeader.size(); i++) {
			String str = null;
			str = excelColumnHeader.get(i).toString();
			label = new jxl.write.Label(i + initColumnNum, startRow, str,
					headerCellFormat);
			sheet.addCell(label);
			CellView cv = sheet.getColumnView(i + initColumnNum);
//			 cv.setAutosize(true);
			switch(i){
			case 0:
				cv.setSize(1000);
				break;
			case 1:
			case 2:
				cv.setSize(4000);
				break;
			case 3:
			case 4:
				cv.setSize(2000);
				break;	
			case 5:
				cv.setSize(20000);
				break;	
			}
			
			sheet.setColumnView(i + initColumnNum, cv);
		}

		startRow = 4;
		DefaultTableModel model = (DefaultTableModel) table.getModel();
		Vector<Vector> data = model.getDataVector();
		for (int i = 0; i < data.size(); i++) {
			Vector<String> row = data.get(i);
			for (int j = 0; j < excelColumnHeader.size(); j++) {
				
				WritableCellFormat tmpCellFormat = new WritableCellFormat(); 
				tmpCellFormat.setBorder(Border.ALL, BorderLineStyle.THIN);
				WritableFont customFormat = null;
				if(validationErrorList.contains(row.get(6))){
					customFormat = new WritableFont(WritableFont.ARIAL, 8, WritableFont.BOLD,false, UnderlineStyle.NO_UNDERLINE, Colour.RED);
					tmpCellFormat.setFont(customFormat);
				}else if(validationWarningList.contains(row.get(6))){
					
					customFormat = new WritableFont(WritableFont.ARIAL, 8, WritableFont.BOLD,false, UnderlineStyle.NO_UNDERLINE, Colour.OCEAN_BLUE);
					tmpCellFormat.setFont(customFormat);
				}
				
				String cellStr = row.get(j);
				
				if( (i/2) %2 == 1 ) {
					tmpCellFormat.setBackground(Colour.TURQOISE2 );
				}else{
					tmpCellFormat.setBackground(Colour.WHITE  );
				}
				label = new jxl.write.Label(j + initColumnNum, i + startRow,
						cellStr, tmpCellFormat);
				sheet.addCell(label);
			}
		}
		
		workBook.write();
		workBook.close();		
	}
	
	private void analyzeValidationResult(ArrayList<HashMap<String, String>> result, ArrayList<org.eclipse.swt.widgets.Button> btnList){
		if( result != null ){
			for(HashMap<String, String> map : this.result){
				String eplId = map.get("EPL_ID");
				String msgType = map.get("MSG_TYPE");
				ModuleBomValidationInfo info = new ModuleBomValidationInfo(map.get("EPL_ID"), map.get("MSG_TYPE"), map.get("MSG"));
				if( validationResultMap.containsKey(eplId)){
					ArrayList resultList = validationResultMap.get(eplId);
					resultList.add(info);
				}else{
					ArrayList resultList = new ArrayList();
					resultList.add(info);
					validationResultMap.put(eplId, resultList);
//					if(eplId == null || eplId.equals("")){
//						MessageBox.post(AIFUtility.getActiveDesktop().getShell() , map.get("MSG") , "Module BOM Validation", MessageBox.ERROR);
//					}
				}
				
				if( msgType.equals("ERROR")){
					 if( !validationErrorList.contains(eplId)){
						 validationErrorList.add(eplId);
					 }
				}
				
				if( msgType.equals("WARNING")){
					 if( !validationWarningList.contains(eplId)){
						 validationWarningList.add(eplId);
					 }
				}
			}
			
			if(validationErrorList.isEmpty()){
				if( btnList != null){
					for(org.eclipse.swt.widgets.Button btn : btnList){
						btn.setEnabled(true);
					}
				}
			}
		}else{
			if( btnList != null){
				for(org.eclipse.swt.widgets.Button btn : btnList){
					btn.setEnabled(true);
				}
			}
		}
	}
	
	private ArrayList<HashMap<String, String>> getValidationResult(String ecoNo) throws Exception{
		
		ArrayList<HashMap<String, String>> result = null;
		final WaitProgressBar waitProgress = new WaitProgressBar(this);
		waitProgress.setAlwaysOnTop(true);
		waitProgress.start();
		waitProgress.setStatus("This job takes about several tens of seconds.");
		
//		String WAS_URL = "http://127.0.0.1:8080/ssangyongweb/HomeServlet";
		SYMCRemoteUtil remote = new SYMCRemoteUtil();
		try{
			DataSet ds = new DataSet();
//			ds.put("eco_no", ecoNo);
			ds.setString("eco_no", ecoNo);
			result = (ArrayList)remote.execute("com.kgm.service.ModuleBomValidationService", "validateModule", ds);
			waitProgress.dispose();
			
//			if(result == null){
//				throw new Exception("SQL 패키지가 컴파일 되었습니다.  다시 실행해 주세요."); 
//			} else 
			
			if (result.size() > 0){
				result = (ArrayList)remote.execute("com.kgm.service.ModuleBomValidationService", "getModuleValidationResult", ds);
			}
		}catch( Exception e){
			waitProgress.dispose();
			throw e;
		}finally{
			
		}
		
		return result;
	}
	
	private void setTableColumn(){
		TableColumnModel columnModel = table.getColumnModel();
		int n = header.size();
		for (int i = 0; i < n; i++) {
			
			columnModel.getColumn(i).setCellRenderer(new ModuleBomTableCellRenderer(validationErrorList, validationWarningList));
			if( i == n-1 ){
				columnModel.removeColumn(columnModel.getColumn(i));
			}else{
				columnModel.getColumn(i).setPreferredWidth(columnWidth[i]);
				columnModel.getColumn(i).setWidth(columnWidth[i]);
			}
		}
	}

	private Vector getEpl(boolean bOnlyProbem){
		
		Vector data = new Vector();
		SYMCRemoteUtil remote = new SYMCRemoteUtil();
		try{
			DataSet ds = new DataSet();
			ds.put("ecoNo", this.ecoNo);
			ArrayList<SYMCBOMEditData> list = (ArrayList)remote.execute("com.kgm.service.ECOHistoryService", "selectECOEplList", ds);
			if( list != null ){
				int epl_no = 1;
				TableItem item = null;
				String mcode_old = "";
				String mcode_new = "";
				for(SYMCBOMEditData epl:list){
					
					mcode_old = epl.getModuleCodeOld() == null ? "" : epl.getModuleCodeOld();
					mcode_new = epl.getModuleCodeNew() == null ? "" : epl.getModuleCodeNew();
					
					if(mcode_old.equals("") && mcode_new.equals("")){
						epl_no++;
						continue;
					}
					
					if( bOnlyProbem ){
						if( !validationErrorList.contains( epl.getEplId()) && !validationWarningList.contains( epl.getEplId())){
							epl_no++;
							continue;
						}
					}
					
					Vector row = new Vector();
					row.add(epl.getPartNoOld() == null ? "" : "" + epl_no);
					row.add(epl.getPartNoOld() == null ? "" : epl.getParentNo());
					row.add(epl.getPartNoOld() == null ? "" : epl.getPartNoOld());
					row.add(epl.getSupplyModeOld() == null ? "" : epl.getSupplyModeOld());
					row.add(epl.getModuleCodeOld() == null ? "" : epl.getModuleCodeOld());
					row.add(epl.getVcOld() == null ? "" : epl.getVcOld());
					row.add(epl.getEplId());
					data.add(row);
					
					row = new Vector();
					row.add(epl.getPartNoNew() == null ? "" : "" + epl_no);
					row.add(epl.getPartNoNew() == null ? "" : epl.getParentNo());
					row.add(epl.getPartNoNew() == null ? "" : epl.getPartNoNew());
					row.add(epl.getSupplyModeNew() == null ? "" : epl.getSupplyModeNew());
					row.add(epl.getModuleCodeNew() == null ? "" : epl.getModuleCodeNew());
					row.add(epl.getVcNew() == null ? "" : epl.getVcNew());
					row.add(epl.getEplId());
					data.add(row);
					
					epl_no++;
				}
			}
		}catch( Exception e){
			e.printStackTrace();
		}	
		
		return data;
	}
}

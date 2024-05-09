package com.kgm.commands.nmcd;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.kgm.commands.commonpartcheck.IconColorCellRenderer;
import com.kgm.commands.nmcd.RegisterPanel.SaveOperation;
import com.kgm.commands.weight.TypeConstant;
import com.kgm.commands.weight.EBOMWeightDialog.CustomCellRenderer;
import com.kgm.common.WaitProgressBar;
import com.kgm.common.remote.DataSet;
import com.kgm.common.remote.SYMCRemoteUtil;
import com.kgm.common.utils.CustomUtil;
import com.kgm.common.utils.SYMTcUtil;
import com.teamcenter.rac.aif.AIFShell;
import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.aif.InterfaceAIFOperationListener;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.ConfirmationDialog;
import com.teamcenter.rac.util.Separator;
import com.teamcenter.rac.util.VerticalLayout;

/**
 * 
 * @Copyright : Plmsoft
 * @author   : 조석훈
 * @since    : 2018. 8. 30.
 * Package ID : com.kgm.commands.nmcd.Register.java
 */
public class SearchPanel extends JPanel{
	
	public static final String COMMONPARTCHECK_QUERY_SERVICE = "com.kgm.service.CommonPartCheckService";
	public DefaultTableModel tableModel = null;
	private NmcdDialog nmcdDialog;
	//20201217 CF-1708 seho Weight 관리 column 추가.
	private String[] column = {"No", "Product No", "Function No", "Parent No.", "Part No", "Weight 관리(STD)", "NMCD", "Project Code", "NEW Team", "Description"};
	int[] width = {20,100,100,100,100,30, 30,50,200,600};
	private Integer[] aligns = new Integer[]{SwingConstants.CENTER,SwingConstants.LEFT, SwingConstants.LEFT, SwingConstants.LEFT, SwingConstants.LEFT, SwingConstants.CENTER, SwingConstants.CENTER, SwingConstants.CENTER, SwingConstants.CENTER, SwingConstants.LEFT};
	private JTextField tfFilePath;
	private JButton btnValidation;
	private File selectedFile;
	private HashMap<String,Object> executeMap;
	private JButton executeButton;
	private JTextArea area;
	private TCSession session;
	private String target_project;
	private String eaiDate = "";
	private static final int ITEM_START_Y_POS = 1;
	private final int product_idx = 1;
	private final int function_idx = 2;
	private final int parent_idx = 3;
	private final int part_idx = 4;
	private final int weightmngt_idx = 5;
	private final int nmcd_idx = 6;
	private final int pcode_idx = 7;
	private final int nteam_idx = 8;
	private final int desc_idx = 9;
	
	JTable table = null;
	public SearchPanel(NmcdDialog nmcdDialog) throws Exception{
		super(new VerticalLayout(5));
		this.nmcdDialog = nmcdDialog;
		session = CustomUtil.getTCSession();
		initUI();
	}

	private void initUI() throws Exception{
		add("top.bind.center.center", createSearchPanel());
		add("unbound.bind.center.center", createTabPanel());
		add("bottom.bind.center.center", createButtonPanel());
		add("bottom.bind.center.center", new Separator());
		add("unbound.bind.center.center", resultPanel());
	}
	
	private JPanel resultPanel(){
		JPanel panel = new JPanel(new VerticalLayout());
		panel.setBorder(new TitledBorder(null, "Execute Result", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		area = new JTextArea();
		area.setEditable(false);
		
		panel.add("unbound.bind.center.center", new JScrollPane(area));
		return panel;
	}
	
public class SaveOperation extends AbstractAIFOperation {
		
		public SaveOperation() {
		}

		@Override
		public void executeOperation() throws Exception {
			StringBuffer resultBufMessage = new StringBuffer();
			WaitProgressBar progressBar = new WaitProgressBar(AIFUtility.getActiveDesktop());
			progressBar.setWindowSize(500, 300);
			progressBar.start();
			progressBar.setStatus("Update Start...."	, true);
			  
			try {
				int countRow = tableModel.getRowCount();
				String no = "";
				String prodId = "";
				String functionId = "";
				String pid = "";
				String cid = "";
				String weightmngt = "";
				String nmcd = "";
				String pcode = "";
				String nteam = "";
				SYMCRemoteUtil remoteQuery = new SYMCRemoteUtil();
				String updateType = "";
				
				for( int row=0; row < countRow; row++) {
					no = (String)tableModel.getValueAt(row, 0);
					prodId = (String)tableModel.getValueAt(row, product_idx);
					functionId = (String)tableModel.getValueAt(row, function_idx);
					pid = (String)tableModel.getValueAt(row, parent_idx);
					cid = (String)tableModel.getValueAt(row, part_idx);
					weightmngt = (String)tableModel.getValueAt(row, weightmngt_idx);
					nmcd = (String)tableModel.getValueAt(row, nmcd_idx);
					pcode = (String)tableModel.getValueAt(row, pcode_idx);
					nteam = (String)tableModel.getValueAt(row, nteam_idx);
					
//					System.out.println(prodId + ";" + functionId + ";" + pid + ";" + cid + ";" + nmcd);
					
					DataSet ds = new DataSet();
					ds.put("PRDNO", prodId);
			        ds.put("FUNCNO", functionId);
			        ds.put("PARENTNO", pid);
			        ds.put("PARTNO", cid);
			        ds.put("WEIGHTMNGT", weightmngt);
			        ds.put("NMCD", nmcd);
			        ds.put("PCODE", pcode);
			        ds.put("NTEAM", nteam);
			        
			        //nmcd가 없으면 delete
			        if((weightmngt == null || weightmngt.equals("")) && (nmcd == null || nmcd.equals("")) && (pcode == null || pcode.equals("")) && (nteam == null || nteam.equals(""))){
			        	updateType = "deleteNmcd";
			        //나머지는 merge
			        } else {
			        	updateType = "mergeNmcd";
			        }
			        
			        progressBar.setStatus("  Update... (" + (row+1)+ " / "+countRow + ") " + prodId + ";" + functionId + ";" + pid + ";" + cid + ";" + weightmngt + ";" + nmcd + ";" + pcode + ";" + nteam, true);
			        
			        try {
			        	remoteQuery.execute(COMMONPARTCHECK_QUERY_SERVICE, updateType, ds);
			        } catch (Exception e){
			        	progressBar.setStatus(e.getMessage(), true);
			        	resultBufMessage.append("No. " + no + " : Error (" +e.getMessage() + ")\n");
			        }
					
				}
				
				progressBar.setStatus("Update End...."	, true);
				progressBar.close("Update End", false);
				area.setText("Update Completed...");
			
			} catch (Exception e) {
				progressBar.setStatus(e.getMessage(), true);
				progressBar.close("오류 발생", false);
				resultBufMessage.append("\n"+e.getMessage());
			} finally {
				area.setText(resultBufMessage.toString());
//				saveButton.setEnabled(true);
			}
		}
	}
	
	private void execute(){
		
		try {
			session.setStatus("Update Start...");
			executeButton.setEnabled(false);
			
			if( tableModel != null ){
				SaveOperation op = new SaveOperation();
				op.addOperationListener(new InterfaceAIFOperationListener() {
					@Override
					public void startOperation(String arg0) {
					}
				
					@Override
					public void endOperation() {
						
					}
				});
				session.queueOperation(op);
			}
			
			session.setStatus("Update End...");
		} catch (Exception e1) {
			e1.printStackTrace();
			area.setText(e1.getMessage());
		} 
	}

	private JPanel createButtonPanel(){
		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
		
		executeButton = new JButton("Execute");
		executeButton.setEnabled(false);
		executeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionevent) {
				execute();
			}

		});
		buttonPane.add(executeButton);
		
		return buttonPane;
	}
	
	private JPanel createTabPanel(){
		JPanel panel = new JPanel(new VerticalLayout());
		panel.setBorder(new TitledBorder(null, "Validation Result", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		
		tableModel = new DefaultTableModel(){
			@Override
			public boolean isCellEditable(int paramInt1, int paramInt2) {
				// TODO Auto-generated method stub
				return false;
			}
		};
		tableModel.setColumnIdentifiers(column);

		table = new JTable(tableModel);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
		((DefaultTableCellRenderer)table.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);

		CustomCellRenderer customRenderer;
		TableColumnModel model = table.getColumnModel();
		for(int i=0; i<model.getColumnCount(); i++ ){
			customRenderer = new CustomCellRenderer();
			customRenderer.setHorizontalAlignment(aligns[i]);
			model.getColumn(i).setCellRenderer( customRenderer );
			model.getColumn(i).setPreferredWidth(width[i]);
		}
		
		panel.add("unbound.bind.center.center", new JScrollPane(table));

		return panel;
	}
	
	private JPanel createSearchPanel() throws Exception{
		JPanel regPanel = new JPanel();
		
		regPanel.setLayout(new BorderLayout(0, 0));
		FlowLayout flowLayout = null;
		
		JPanel msgPanel = new JPanel();
		msgPanel.setLayout(new BorderLayout(0, 0));
		msgPanel.setBorder(new TitledBorder(null, "Search", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		regPanel.add(msgPanel, BorderLayout.NORTH);
		JLabel lblAttachFileMsg = new JLabel();
        lblAttachFileMsg.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        lblAttachFileMsg.setForeground(Color.RED);
        lblAttachFileMsg.setText("※ 암호화된 문서는 반드시 암호를 해제하신 후 등록하셔야 합니다. ※");
        msgPanel.add(lblAttachFileMsg, BorderLayout.NORTH);
	        
		
		JPanel searchPanel = new JPanel();
		flowLayout = (FlowLayout) searchPanel.getLayout();
		flowLayout.setAlignment(FlowLayout.LEADING);
		msgPanel.add(searchPanel, BorderLayout.CENTER);
		tfFilePath = new JTextField();
		searchPanel.add(tfFilePath);
		tfFilePath.setColumns(60);
		JButton btnFind = new JButton("Find..");
		btnFind.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				
				JFileChooser fileChooser = new JFileChooser();
//				fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY );
				fileChooser.setFileFilter(new FileFilter(){
	
					@Override
					public boolean accept(File f) {
						if (f.isDirectory()) {
					        return true;
					    }
						
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
				int result = fileChooser.showOpenDialog(nmcdDialog);
				if( result == JFileChooser.APPROVE_OPTION){
					selectedFile = fileChooser.getSelectedFile();
					tfFilePath.setText( selectedFile.getAbsolutePath() );
					btnValidation.setEnabled(true);
				}						
				
			}
		});
		searchPanel.add(btnFind);
		btnValidation = new JButton("Validation");
		btnValidation.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				validation();
			}
		});
		btnValidation.setEnabled(false);
		searchPanel.add(btnValidation);
		
		JButton btnTemplate = new JButton("Template File Download");
		btnTemplate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				templateDown();
			}
		});
		searchPanel.add(btnTemplate);

		return regPanel;
	}
	
	private String getTodayDate() {
		  Date date = new Date();
		  SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmm");
		  
		  return dateFormat.format(date).toString();
	  }
	
	private void templateDown(){
		try {
			String filePath = "c:\\temp\\nmcdMigration_"+getTodayDate()+".xls";
			File tempFile = SYMTcUtil.getTemplateFile(this.session, "ssangyong_nmcd_mig_template.xls", null);
			
			tempFile.renameTo(new File(filePath));
			
            
            selectedFile = tempFile;
			tfFilePath.setText( filePath );
			btnValidation.setEnabled(true);
            
			AIFShell aif = new AIFShell("application/vnd.ms-excel", filePath);
	        aif.start();
			
        } catch (Exception e) {
        	e.printStackTrace();
        }
	}
	
	public String getFormatedString(double value) {
      DecimalFormat df = new DecimalFormat("#####################.####");//
      return df.format(value);
    }
	
	public String getCellText(Cell cell) {
        String value = "";
        if (cell != null) {
            
            switch (cell.getCellType()) {
                case XSSFCell.CELL_TYPE_FORMULA:
                    value = cell.getCellFormula();
                    break;
                
                // Integer로 Casting하여 반환함
                case XSSFCell.CELL_TYPE_NUMERIC:
                    value = "" +  getFormatedString(cell.getNumericCellValue());
                    break;
                
                case XSSFCell.CELL_TYPE_STRING:
                    value = "" + cell.getStringCellValue();
                    break;
                
                case XSSFCell.CELL_TYPE_BLANK:
                    // value = "" + cell.getBooleanCellValue();
                    value = "";
                    break;
                
                case XSSFCell.CELL_TYPE_ERROR:
                    value = "" + cell.getErrorCellValue();
                    break;
                default:
            }
            
        }
        
        return value;
    }
	
	public class LoadOperation extends AbstractAIFOperation {
		public LoadOperation() {
		}

		@Override
		public void executeOperation() throws Exception {
			Workbook wb = null;
	        FileInputStream fis = null;
			WaitProgressBar progressBar = new WaitProgressBar(AIFUtility.getActiveDesktop());
			progressBar.setWindowSize(500, 300);
			progressBar.start();
			progressBar.setStatus("Validation Start...."	, true);
			
			try {
				String strFilePath = tfFilePath.getText();
	            fis = new FileInputStream(strFilePath);
	            
	            String strExt = strFilePath.substring(strFilePath.lastIndexOf(".")+1);
	            
	            if( strExt.toLowerCase().equals("xls") ) {
	                // Excel WorkBook
	                wb = new HSSFWorkbook(fis);
	            } else {
	                // Excel WorkBook
	                wb = new XSSFWorkbook(fis);
	            }
	            
	            fis.close();
	            fis = null;
			
				Sheet sheet = wb.getSheetAt(0);
		        int rows = sheet.getPhysicalNumberOfRows();
		        Row row;
		        String product;
		        String function;
		        String parent;
		        String part;
		        String weightmngt;
		        String nmcd;
		        String pcode;
		        String nteam;
		        String desc;
		        String[] nmcdValues = nmcdDialog.getNmcdValues();
		        String[] projectCodes = nmcdDialog.getProjectCodeValues();
		        ArrayList teamNames = nmcdDialog.getTeamList();
		        HashMap<String,String> nmcdMap = new HashMap<String,String>();
		        HashMap<String,String> projectCodeMap = new HashMap<String,String>();
		        StringBuffer resultBufMessage = new StringBuffer();
		        nmcdMap.put("", "");
		        
		        for(int i=0; i<nmcdValues.length; i++){
		        	nmcdMap.put(nmcdValues[i], nmcdValues[i]);
		        }
		        
		        projectCodeMap.put("", "");
		        for(int j=0; j<projectCodes.length; j++){
		        	projectCodeMap.put(projectCodes[j], projectCodes[j]);
		        }
		        
		        boolean isVal;
		        int valCnt = 0;
		        for (int r = ITEM_START_Y_POS; r < rows; r++) {
		        	isVal = false;
		            row = sheet.getRow(r);
		            product = getCellText(row.getCell(product_idx)).toUpperCase();
		            function = getCellText(row.getCell(function_idx)).toUpperCase();
		            parent = getCellText(row.getCell(parent_idx)).toUpperCase();
		            part = getCellText(row.getCell(part_idx)).toUpperCase();
		            weightmngt = getCellText(row.getCell(weightmngt_idx)).toUpperCase().trim();
		            nmcd = getCellText(row.getCell(nmcd_idx)).toUpperCase();
		            pcode = getCellText(row.getCell(pcode_idx)).toUpperCase();
		            nteam = getCellText(row.getCell(nteam_idx));
		            desc = "";
		            
		            //Item check
		            String itemid = product+";"+function+";"+parent+";"+part;
		            TCComponent[] findProducts = CustomUtil.queryComponent("Item...", new String[]{"Item ID"}, new String[]{itemid});
		            if(findProducts == null || findProducts.length != 4){
		            	desc = desc + "유효한 Item No 값이 아닙니다. ";
		            	isVal = true;
		            }
		            if(!weightmngt.isEmpty() && !weightmngt.equals("O"))
		            {
		            	desc = desc + "유효한 Weight 관리(STD) 값이 아닙니다. ";
		            	isVal = true;
		            }
		            //nmcd check
		            if(!nmcdMap.containsKey(nmcd)){
		            	desc = desc + "유효한 NMCD 값이 아닙니다. ";
		            	isVal = true;
		            }
		          //project check
		            if(!projectCodeMap.containsKey(pcode)){
		            	desc = desc + "유효한 Project Code 값이 아닙니다. ";
		            	isVal = true;
		            }
		          //team check
		            if(!teamNames.contains(nteam)){
		            	desc = desc + "유효한 팀명이 아닙니다. ";
		            	isVal = true;
		            }
		            if(isVal){
		            	resultBufMessage.append("No. " + r + " : " +desc + "\n");
		            	valCnt++;
		            }
		            tableModel.addRow(new String[]{String.valueOf(r), product, function, parent, part, weightmngt, nmcd, pcode, nteam, desc});
//		            session.setStatus(r + " / " + (rows-1));
		            progressBar.setStatus("  Loading... (" + r + " / " + (rows-1) + ") " + product + ";" + function + ";" + parent + ";" + part + ";" + weightmngt + ";" + nmcd + ";" + pcode + ";" + nteam, true);
		        }
		        if(valCnt == 0){
		        	executeButton.setEnabled(true);
		        } else {
		        	resultBufMessage.append("\n※ 출력된 (" + valCnt + ") 건에 대한 데이타 검증 후 Migration을 진행하여주세요.\n");
		        }
		        area.setText(resultBufMessage.toString());
//		        session.setStatus("Validation End...");
		        progressBar.setStatus("Validation End...."	, true);
		        progressBar.close("Validation End", false);
	        
			} catch (Exception e) {
				progressBar.setStatus(e.getMessage(), true);
				progressBar.close("오류 발생", false);
			} finally {
				if( fis != null ) {
	                try {
	                    fis.close();
	                    fis = null;
	                } catch(IOException e) {
	                    e.printStackTrace();
	                }
	            }
//				saveButton.setEnabled(true);
			}
		}
	}
	
	public class CustomCellRenderer extends DefaultTableCellRenderer {

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			Component com = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

			JLabel label = (JLabel) com;

			if (isSelected) {
				label.setBackground(table.getSelectionBackground());
				label.setForeground(table.getSelectionForeground());
			} else {
				String desc = table.getValueAt(row, desc_idx) + "";
				if (table.getRowCount() > 0 && (desc != null && !desc.equals("") )) {
					label.setBackground(Color.PINK);
					label.setForeground(table.getForeground());
				} else {
					if(row%2 == 1){
						label.setBackground(new Color(230,230,230));
					} else {
						label.setBackground(table.getBackground());
					}
					label.setForeground(table.getForeground());
				}
			}
			return com;
		}
	}
	
	private void validation() {
        
		try {
			session.setStatus("Validation Start...");
			tableModel.setRowCount(0);
			executeButton.setEnabled(false);
			
            LoadOperation op = new LoadOperation();
			op.addOperationListener(new InterfaceAIFOperationListener() {
				@Override
				public void startOperation(String arg0) {
				}
			
				@Override
				public void endOperation() {
					
				}
			});
			session.queueOperation(op);
                
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
		}
	}

	
}

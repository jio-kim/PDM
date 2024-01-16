package com.ssangyong.commands.namegroup;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

import jxl.CellView;
import jxl.Workbook;
import jxl.format.Alignment;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.Colour;
import jxl.format.VerticalAlignment;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

import com.ssangyong.commands.namegroup.model.NameGroupCountResult;
import com.ssangyong.commands.namegroup.model.PngProd;
import com.ssangyong.commands.namegroup.model.SpecHeader;
import com.teamcenter.rac.aif.AIFShell;
import com.teamcenter.rac.util.MessageBox;

public class PngVerificationTablePanel extends JPanel {
	
	private Vector fixedHeader = null;
	private String[] fixedColumns = {"Group ID", "Group Name", "Req. Qty"};
	private Vector<Vector> data = null;
	private JTable fixedNameGroupTable = null; 
	private JTable specResultTable = null;
	private PngDlg parentDlg = null;
	private Vector<SpecHeader> specHeaderList = null;
	private SpecHeader selectedHeader = null;
	
	public PngVerificationTablePanel(PngDlg parentDlg, Vector<Vector> customData, Vector<SpecHeader> specHeaderList) throws Exception{
		this.parentDlg = parentDlg;
		this.specHeaderList = specHeaderList;
		createVerificationTable(customData);
	}

	private JPanel createVerificationTable(Vector<Vector> customData) throws Exception{
		ArrayList<PngProd> prodList = null; //getTrim(osiNo);
		
		fixedHeader = new Vector();
		for(String s : fixedColumns){
			fixedHeader.add(s);
		}
		Vector dataHeader = new Vector();
		Vector addVec = null;
		Vector removeVec = null;
		
		if( customData == null){
			data = getData(specHeaderList);
		}else{
			data = customData;
		}
		
		if( specHeaderList != null){
			fixedHeader.addAll(specHeaderList);
		}
		
		dataHeader.addAll(fixedHeader);
		
		final Vector<Vector> fixedData = (Vector<Vector>)data.clone();
		final Vector<Vector> optionData = (Vector<Vector>)data.clone();
		final int columnCount = dataHeader.size();
		final int fixedColumnCount = 3;
		
		DefaultTableModel fixedModel = new DefaultTableModel(fixedData, fixedHeader){

			@Override
			public boolean isCellEditable(int i,
					int j) {
				// TODO Auto-generated method stub
				return false;
			}
			
			public int getColumnCount() {
		        return fixedColumnCount;
		    }
			
			public Object getValueAt(int row, int col) {
				return data.get(row).get(col);
			}

			public Class getColumnClass(int columnIndex) {
				return String.class;
			}
		};									
		
		DefaultTableModel model = new DefaultTableModel(optionData, dataHeader){

			@Override
			public boolean isCellEditable(int i, int j) {
				// TODO Auto-generated method stub
				return true;
			}

			public Class getColumnClass(int column) {
				return NameGroupCountResult.class;
			}

			public int getColumnCount() {
				return columnCount - fixedColumnCount;
			}

			public int getRowCount() {
				return dataVector.size();
			}

			public String getColumnName(int i) {
				Object obj = null;
				if (i < columnIdentifiers.size() && i >= 0)
					obj = columnIdentifiers.elementAt(i + fixedColumnCount);
				return obj != null ? obj.toString() : super.getColumnName(i);
			}

			public Object getValueAt(int row, int col) {
				return data.get(row).get(col + fixedColumnCount);
			}

			public void setValueAt(Object obj, int row, int col) {
				Vector rowVec = data.get(row);
				rowVec.set(col + fixedColumnCount, obj);
			}
		};
		
	    fixedNameGroupTable = new JTable(fixedModel){
	    	
			public void valueChanged(ListSelectionEvent e) {
				super.valueChanged(e);
				checkSelection(true);
			}

			public Component prepareRenderer(TableCellRenderer renderer,
					int row, int column) {
				Component c = super.prepareRenderer(renderer, row, column);
				if (c instanceof JComponent) {
					JComponent jc = (JComponent) c;
					jc.setToolTipText(getValueAt(row, column).toString());
				}
				return c;
			}
			
		};
		
		TableColumnModel cm = fixedNameGroupTable.getColumnModel();
		int[] width = new int[]{65, 150, 60, 10};
		for( int i = 0; i < cm.getColumnCount(); i++){
			cm.getColumn(i).setPreferredWidth(width[i]);
			cm.getColumn(i).setResizable(false);
		}
	      
		specResultTable = new JTable(model) {

			public void valueChanged(ListSelectionEvent e) {
				super.valueChanged(e);
				checkSelection(false);
			}

			@Override
			public boolean isCellEditable(int i, int j) {
				// TODO Auto-generated method stub
				return false;
			}

//			public Component prepareRenderer(TableCellRenderer renderer,
//					int row, int column) {
//				Component c = super.prepareRenderer(renderer, row, column);
//				if (c instanceof JComponent) {
//					JComponent jc = (JComponent) c;
//					String toolTip = "Package : " + getValueAt(row, 0).toString() + "." + getValueAt(row, column).toString();
//					jc.setToolTipText("<html>"+toolTip.replaceAll("\\.","<br>")+"</html>");
//				}
//				
//				if( c instanceof JLabel){
//					JLabel label = (JLabel)c;
//					String qty = label.getText();
//					Object obj = getValueAt(row, column);
//					if( obj instanceof NameGroupCountResult){
//						NameGroupCountResult result = (NameGroupCountResult)obj;
//						if(!result.isValid()){
//							label.setBackground(Color.RED);
//						}
//					}
//				}
//				return c;
//			}
			
		};
		
		final JPopupMenu popupMenu = new JPopupMenu();
	    JMenuItem menuItem = new JMenuItem("Export Spec to Excel");
	    menuItem.setIcon(new ImageIcon(PngVerificationTablePanel.class.getResource("/com/ssangyong/common/images/excel_16.png")));
	    menuItem.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent actionevent) {
				// TODO Auto-generated method stub
				export();
			}
		});
	    popupMenu.add(menuItem);
		
		specResultTable.getTableHeader().addMouseListener(new MouseAdapter() {

			@Override
			public void mouseReleased(MouseEvent mouseevent) {
				// TODO Auto-generated method stub
				if( SwingUtilities.isRightMouseButton(mouseevent)){
					JTableHeader header = specResultTable.getTableHeader();
					int col = specResultTable.columnAtPoint(mouseevent.getPoint());
					int modelIdx = specResultTable.convertColumnIndexToModel(col);
					selectedHeader = specHeaderList.get(modelIdx);
					popupMenu.show(mouseevent.getComponent(), mouseevent.getX(), mouseevent.getY());
				}
				super.mouseReleased(mouseevent);
			}
			
		});
		
	    fixedNameGroupTable.getColumnModel().getColumn(2).setResizable(false);
	    fixedNameGroupTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		specResultTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		fixedNameGroupTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	    specResultTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	    
	    fixedNameGroupTable.setDefaultRenderer(String.class, new DefaultTableCellRenderer());
	    specResultTable.setDefaultRenderer(NameGroupCountResult.class, new DefaultTableCellRenderer(){

			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
				// TODO Auto-generated method stub
				Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
				if( c instanceof JLabel){
					JLabel label = (JLabel)c;
					String qty = label.getText();
					Object obj = specResultTable.getValueAt(row, column);
					if( !isSelected){
						if( obj instanceof NameGroupCountResult){
							NameGroupCountResult result = (NameGroupCountResult)obj;
							String reason = result.getReason();
							if( reason != null){
								label.setToolTipText("<html>"+reason.replaceAll("\\.","<br>")+"</html>");
							}else{
								label.setToolTipText(null);
							}
							
							if(!result.isValid()){
								label.setBackground(Color.RED);
							}else{
								label.setBackground(Color.WHITE);
							}
						}
					}
					
				}
				return c;
			}
	    	
	    });
	    JScrollPane scroll = new JScrollPane(specResultTable);
	    JViewport viewport = new JViewport();
	    viewport.setView(fixedNameGroupTable);
	    viewport.setPreferredSize(fixedNameGroupTable.getPreferredSize());
	    scroll.setRowHeaderView(viewport);
	    scroll.setCorner(JScrollPane.UPPER_LEFT_CORNER, fixedNameGroupTable
	        .getTableHeader());			
		
		setLayout(new BorderLayout(0, 0));
		add(scroll, BorderLayout.CENTER);
	    return this;
	}
	
	private void export(){
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY );
		Calendar now = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddkkmmss");
		sdf.format(now.getTime());
		File defaultFile = new File(selectedHeader +"_" + sdf.format(now.getTime()) + ".xls");
		fileChooser.setSelectedFile(defaultFile);
//		fileChooser.addChoosableFileFilter(new OptionDefinitionFileFilter("MSEXCEL"));
		fileChooser.setFileFilter(new FileFilter(){

			public boolean accept(File f) {
				// TODO Auto-generated method stub
				if( f.isFile()){
					return f.getName().endsWith("xls");
				}
				return false;
			}

			public String getDescription() {
				// TODO Auto-generated method stub
				return "*.xls";
			}
			
		});
		int result = fileChooser.showSaveDialog(parentDlg);
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
            	MessageBox.post(parentDlg, e.getMessage(), "ERROR", MessageBox.ERROR);
            }
		}	
	}
	
	private void exportToExcel(File selectedFile) throws RowsExceededException, WriteException, IOException{
		WritableWorkbook workBook = Workbook.createWorkbook(selectedFile);
	    // 0번째 Sheet 생성
	    WritableSheet sheet = workBook.createSheet("new sheet", 0);
	    
	    WritableCellFormat centerFormat = new WritableCellFormat(); // 셀의 스타일을 지정하기 위한 부분입니다.
	    centerFormat.setBorder(Border.ALL, BorderLineStyle.THIN); // 셀의 스타일을 지정합니다. 테두리에 라인그리는거에요
	    centerFormat.setAlignment(Alignment.CENTRE);
	    centerFormat.setVerticalAlignment(VerticalAlignment.CENTRE);
	    
	    WritableCellFormat leftFormat = new WritableCellFormat(); // 셀의 스타일을 지정하기 위한 부분입니다.
	    leftFormat.setBorder(Border.ALL, BorderLineStyle.THIN); // 셀의 스타일을 지정합니다. 테두리에 라인그리는거에요
	    leftFormat.setAlignment(Alignment.LEFT);
	    leftFormat.setVerticalAlignment(VerticalAlignment.CENTRE);
	    
	    WritableCellFormat autoLineFormat = new WritableCellFormat(); // 셀의 스타일을 지정하기 위한 부분입니다.
	    autoLineFormat.setBorder(Border.ALL, BorderLineStyle.THIN); // 셀의 스타일을 지정합니다. 테두리에 라인그리는거에요
	    autoLineFormat.setWrap(true);
	    autoLineFormat.setVerticalAlignment(VerticalAlignment.CENTRE);
	    
	    Label label = null;
	    
	    WritableCellFormat headerCellFormat = new WritableCellFormat(); // 셀의 스타일을 지정하기 위한 부분입니다.
	    headerCellFormat.setBorder(Border.ALL, BorderLineStyle.THIN);
	    headerCellFormat.setBackground(Colour.GREY_25_PERCENT);

	    int startRow = 0;
	    int initColumnNum = 0;

	    Vector excelColumnHeader = new Vector();
	    excelColumnHeader.add("Group ID");
	    excelColumnHeader.add("Group Name");
	    excelColumnHeader.add("Default Qty.");
	    excelColumnHeader.add("Function");
	    excelColumnHeader.add("Part No");
	    excelColumnHeader.add("Part Name");
	    excelColumnHeader.add("Condition");
	    excelColumnHeader.add("Supply Mode");
	    excelColumnHeader.add("Level");
	    excelColumnHeader.add("Name Count");
	    
	    
	    for (int i = 0; i < excelColumnHeader.size(); i++)
	    {
	      label = new jxl.write.Label(i + initColumnNum, startRow, excelColumnHeader.get(i).toString(), headerCellFormat);
	      sheet.addCell(label);
	      CellView cv = sheet.getColumnView(i + initColumnNum);
//	      cv.setSize(1500);
	      cv.setAutosize(true);
	      sheet.setColumnView(i + initColumnNum, cv);
	    }

	    int rowNum = 0;
	    startRow = 1;
	    
	    ArrayList<String> list = new ArrayList();
	    String value = null;
	    ArrayList<HashMap<String, Object>> data = parentDlg.verificationPanel.verificationResult.get(selectedHeader.getKey());
		WritableCellFormat format = null;
	    for (int i = 0; i < data.size(); i++)
	    {
	    	list.clear();
	    	HashMap<String, Object> row = data.get(i);
	    	String groupID = (String)row.get("GROUP_ID");
	    	list.add(groupID);
	    	String groupName = (String)row.get("GROUP_NAME");
	    	list.add(groupName);
	    	String defaultQty = "" + row.get("DEFAULT_QTY");
	    	list.add(defaultQty);
	    	String functionNo = (String)row.get("FUNCTION_NO");
	    	list.add(functionNo);
	    	String childNo = (String)row.get("CHILD_NO");
	    	list.add(childNo);
	    	String childName = (String)row.get("CHILD_NAME");
	    	list.add(childName);
	    	String condition = (String)row.get("CONDITION");
	    	list.add(condition);
	    	String supplyMode = (String)row.get("SUPPLY_MODE");
	    	list.add(supplyMode);
	    	String level = "" + row.get("LV");
	    	list.add(level);
	    	String nameCount = "" + row.get("NAME_COUNT");
	    	list.add(nameCount);
	    	
	    	for( int j = 0; j < list.size(); j++){
		    	format = leftFormat;
		    	value = list.get(j);
	    		label = new jxl.write.Label(j + initColumnNum, (rowNum) + startRow, value, format);
	    		sheet.addCell(label);
	    	}
	    	rowNum++;
	    }

	    //셀 Merge
//	    initColumnNum = opNameList.size();
//	    int startIdxToMerge = startRow;
//	    int endIdxToMerge = startRow;
//	    for (int i = 0; i < data.size(); i++){
//	    	
//	    	Cell cell = sheet.getCell(initColumnNum, i + startRow);
//	    	Cell nextCell = sheet.getCell(initColumnNum, i + startRow + 1);
//	    	
//	    	if( cell.getContents().equals(nextCell.getContents())){
//	    		endIdxToMerge = i + 1 + startRow;
//	    	}else{
//	    		if( startIdxToMerge < endIdxToMerge){
//		    		sheet.mergeCells(initColumnNum, startIdxToMerge, initColumnNum, endIdxToMerge);
//		    		WritableCell wCell = sheet.getWritableCell(initColumnNum, startIdxToMerge);
//	    			WritableCellFormat cf = new WritableCellFormat(); // 셀의 스타일을 지정하기 위한 부분입니다.
//	    			cf.setWrap(true);
//	    			cf.setBorder(Border.ALL, BorderLineStyle.THIN); 
//	    		    cf.setVerticalAlignment(VerticalAlignment.CENTRE);
//	    		    wCell.setCellFormat(cf);
//	    		}
//	    		startIdxToMerge = i + 1 + startRow;
//	    	}
//	    }
	    
	    workBook.write();
	    workBook.close();			
	}
	
	private void checkSelection(boolean isFixedTable) {
		int fixedSelectedIndex = fixedNameGroupTable.getSelectedRow();
		int selectedIndex = specResultTable.getSelectedRow();
		if (fixedSelectedIndex != selectedIndex) {
			if (isFixedTable) {
				if (selectedIndex > specResultTable.getRowCount() - 1) {
					return;
				}
				specResultTable.setRowSelectionInterval(fixedSelectedIndex,
						fixedSelectedIndex);
			} else {
				if (selectedIndex > fixedNameGroupTable.getRowCount() - 1) {
					return;
				}
				fixedNameGroupTable.setRowSelectionInterval(selectedIndex,
						selectedIndex);
			}
		}
	}
	
	private Vector<Vector> getData(Vector verifyHeader) throws Exception{
		
		Vector data = new Vector(){
			@Override
			public synchronized Object clone() {
				Vector cloneData = new Vector();
				for( int i = 0; i < this.elementCount; i++){
					Vector row = new Vector();
					Vector source = (Vector)this.elementData[i];
					row.addAll(source);
					cloneData.add(row);
				}
				return cloneData;
			}
		};		
		
		return data;
	}

	public Vector<Vector> getData() {
		return data;
	}

	public Vector getVerifyHeader() {
		return specHeaderList;
	}
	
}

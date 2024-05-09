package com.kgm.commands.variantoptioncompare;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import jxl.CellView;
import jxl.Workbook;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.Colour;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

import com.kgm.commands.optiondefine.VariantOptionDefinitionDialog;
import com.kgm.common.utils.variant.OptionManager;
import com.kgm.common.utils.variant.VariantValue;
import com.teamcenter.rac.aif.AIFShell;
import com.teamcenter.rac.aif.AbstractAIFDialog;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCException;

/**
 * 옵션 비교창을 오픈.
 * 현재 선택된 Product에 설정된 Option과 Child인 Variant 아이템에 설정된 Option을 Table구조로 표기됨.
 * Product에 설정된 Option이 하위 Variant의 어디에서도 사용되지 않으면 노란색으로 표기함.
 * 
 * @author slobbie
 *
 */
@SuppressWarnings({"serial", "unchecked", "rawtypes", "unused"})
public class OptionCompareDialog extends AbstractAIFDialog {

	private final JPanel contentPanel = new JPanel();
	private JTable table;
	private Vector<VariantValue> productValueList = null;
	private HashMap<TCComponentBOMLine, List<VariantValue>> map = null;
	private TCComponentBOMLine target = null;
	private OptionManager manager = null;
	private Vector<Vector> allData = new Vector();
	private JCheckBox chkOnlyNotFound = null;
	private Vector headerVector = new Vector();

	/**
	 * Create the dialog.
	 */
	public OptionCompareDialog(Vector<VariantValue> productValueList, HashMap<TCComponentBOMLine, List<VariantValue>> map, TCComponentBOMLine target, OptionManager manager) 
		throws TCException{
		super(AIFUtility.getActiveDesktop().getFrame(), false);
		this.productValueList = productValueList;
		this.map = map;
		this.target = target;
		this.manager = manager;
		setTitle("Option Comparison");
		setBounds(100, 100, 450, 300);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBackground(Color.WHITE);
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));
		{
			JPanel panel = new JPanel();
			panel.setBackground(Color.WHITE);
			contentPanel.add(panel, BorderLayout.NORTH);
			panel.setLayout(new BorderLayout(0, 0));
			{
				JPanel panel_1 = new JPanel();
				panel_1.setBackground(Color.WHITE);
				FlowLayout flowLayout = (FlowLayout) panel_1.getLayout();
				flowLayout.setAlignment(FlowLayout.LEADING);
				panel.add(panel_1, BorderLayout.CENTER);
				{
					JLabel lblNewLabel = new JLabel("* " + target.getItem().toDisplayString());
					lblNewLabel.setBackground(Color.WHITE);
					lblNewLabel.setFont(new Font("굴림", Font.BOLD, 13));
					panel_1.add(lblNewLabel);
				}
			}
			{
				JPanel panel_1 = new JPanel();
				panel_1.setBackground(Color.WHITE);
				panel.add(panel_1, BorderLayout.EAST);
				{
					chkOnlyNotFound = new JCheckBox("Only Not Found");
					panel_1.add(chkOnlyNotFound);
					chkOnlyNotFound.setBackground(Color.WHITE);
					chkOnlyNotFound.addActionListener(new ActionListener(){

						@Override
						public void actionPerformed(ActionEvent arg0) {
//						show All Options가 체크되어 있으면, Model에서 체크 안된 모든 옵션을 보여준다.
							DefaultTableModel model = (DefaultTableModel)table.getModel();
							Vector newData = getData(chkOnlyNotFound.isSelected());
							model.setDataVector(newData, headerVector);
							initTable();	
						}
						
					});
				}
				
			}
		}
		{
			JPanel panel = new JPanel();
			panel.setBackground(Color.WHITE);
			contentPanel.add(panel, BorderLayout.CENTER);
			panel.setLayout(new BorderLayout(0, 0));
			{
				
				//컬럼 헤더 초기화
				headerVector.add("Category Code");
				headerVector.add("Category Desc");
				headerVector.add(target.getItem().getProperty("item_id") + "[Product]");
				Iterator<TCComponentBOMLine> its = this.map.keySet().iterator();
				while( its.hasNext()){
					TCComponentBOMLine variant = its.next();
					headerVector.add(variant.getItem().getProperty("item_id") + "[Variant]");
				}
				headerVector.insertElementAt("Variant SUM", 3);
				
				//데이타 초기화
				for( VariantValue value : this.productValueList){
					Vector row = new Vector();
					row.add(value.getOption().getOptionName());
					row.add(value.getOption().getOptionDesc());
					row.add(value);
					its = this.map.keySet().iterator();
					boolean isFound = false;
					while( its.hasNext()){
						TCComponentBOMLine variant = its.next();
						List<VariantValue> list = map.get(variant);
						if( list == null || list.isEmpty()){
							row.add("-");
						}else{
							if( list.contains(value)){
								row.add(value);
								isFound = true;
							}else{
								row.add("-");
							}
						}
					}
					if( isFound ){
						row.insertElementAt(value, 3);
					}else{
						row.insertElementAt("-", 3);
					}
					allData.add(row);
				}
				
				TableModel model = new DefaultTableModel(getData(chkOnlyNotFound.isSelected()), headerVector) {
					public Class getColumnClass(int col) {
						return VariantValue.class;
					}

					public boolean isCellEditable(int row, int col) {
						return false;
					}
			    };
			    
			    table = new JTable(model);
			    table.setBackground(Color.WHITE);

			    TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(model);
			    table.setRowSorter(sorter);
				table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
				initTable();
				
				JScrollPane scrollPane = new JScrollPane();
				scrollPane.setViewportView(table);
				panel.add(scrollPane, BorderLayout.CENTER);
			}
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setBackground(Color.WHITE);
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton exportButton = new JButton("Export");
				exportButton.setBackground(Color.WHITE);
				exportButton.setIcon(new ImageIcon(VariantOptionDefinitionDialog.class.getResource("/com/ssangyong/common/images/excel_16.png")));
				exportButton.addActionListener(new ActionListener(){

					@Override
					public void actionPerformed(ActionEvent actionevent) {
						JFileChooser fileChooser = new JFileChooser();
						fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY );
						Calendar now = Calendar.getInstance();
						SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddkkmmss");
						sdf.format(now.getTime());
						File defaultFile = new File("Option_Compare_" + sdf.format(now.getTime()) + ".xls");
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
						int result = fileChooser.showSaveDialog(OptionCompareDialog.this);
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
				            	if( selectedFile != null ){
				            		OptionCompareDialog.this.dispose();
				            	}
				            }
						}
						
						
					}
					
				});
				exportButton.setActionCommand("Excel Export");
				buttonPane.add(exportButton);
				getRootPane().setDefaultButton(exportButton);
			}
			{
				JButton closeButton = new JButton("Close");
				closeButton.setBackground(Color.WHITE);
				closeButton.addActionListener(new ActionListener(){

					@Override
					public void actionPerformed(ActionEvent actionevent) {
						OptionCompareDialog.this.dispose();
					}
					
				});
				closeButton.setActionCommand("Close");
				buttonPane.add(closeButton);
			}
		}
	}

	/**
	 * 테이블 폭과 렌더러를 초기화함.
	 */
	private void initTable(){
		
		TableColumnModel columnModel = table.getColumnModel();
		int n = headerVector.size();
		for (int i = 0; i < n; i++) {
			columnModel.getColumn(i).setPreferredWidth(100);
			columnModel.getColumn(i).setWidth(100);
		}

		OptionCompareTableCellRenderer cellRenderer = new OptionCompareTableCellRenderer();
		for( int i = 0; i < columnModel.getColumnCount(); i++){
			TableColumn column = columnModel.getColumn(i);
			column.setCellRenderer(cellRenderer);
		}
	}
	
	/**
	 * bChkOnlyNotFound : true ==> 하위 Variant에서 사용되지 않는 옵션값만 표기
	 * bChkOnlyNotFound : false ==> Product에 정의된 모든 옵션 값을 표기.
	 * @param bChkOnlyNotFound
	 * @return
	 */
	private Vector getData(boolean bChkOnlyNotFound){
		
		Vector newData = new Vector();
		if( bChkOnlyNotFound ){
			for( Vector row : allData){
				Object obj = row.get(3);
				if( obj instanceof String){
					newData.add(row);
				}
				
			}
		}else{
			return allData;
		}
		
		return newData;
		
	}
	
	/**
	 * 현재 테이블의 데이타를 엑셀로 Export함.
	 * 
	 * @param file
	 * @throws RowsExceededException
	 * @throws WriteException
	 * @throws IOException
	 */
	private void exportToExcel(File file) throws RowsExceededException, WriteException, IOException{
	    WritableWorkbook workBook = Workbook.createWorkbook(file);
	    // 0번째 Sheet 생성
	    WritableSheet sheet = workBook.createSheet("new sheet", 0);

	    WritableCellFormat cellFormat = new WritableCellFormat(); // 셀의 스타일을 지정하기 위한 부분입니다.
	    cellFormat.setBorder(Border.ALL, BorderLineStyle.THIN); // 셀의 스타일을 지정합니다. 테두리에 라인그리는거에요
	    Label label = null;
	    
	    WritableCellFormat headerCellFormat = new WritableCellFormat(); // 셀의 스타일을 지정하기 위한 부분입니다.
	    headerCellFormat.setBorder(Border.ALL, BorderLineStyle.THIN);
	    headerCellFormat.setBackground(Colour.GREY_25_PERCENT);

	    WritableCellFormat problemCellFormat = new WritableCellFormat(); // 셀의 스타일을 지정하기 위한 부분입니다.
	    problemCellFormat.setBorder(Border.ALL, BorderLineStyle.THIN);
	    problemCellFormat.setBackground(Colour.ORANGE);
	    
	    int startRow = 1;

	    label = new jxl.write.Label(1, startRow, this.target.toDisplayString(), cellFormat);
	    sheet.addCell(label);
	    sheet.mergeCells(1, 1, 3, 1);
	    
	    startRow = 3;
	    for (int i = 0; i < headerVector.size(); i++)
	    {
	      label = new jxl.write.Label(i, startRow, headerVector.get(i).toString(), headerCellFormat);
	      sheet.addCell(label);
	      CellView cv = sheet.getColumnView(i);
	      cv.setAutosize(true);
	      sheet.setColumnView(i, cv);
	    }

	    startRow = 4;
	    Vector<Vector> data = getData(chkOnlyNotFound.isSelected());
	    for (int i = 0; i < data.size(); i++)
	    {
	    	Vector row = data.get(i);
	    	for (int j = 0; j < row.size(); j++)
	    	{
	    		String str = "";
	    		if( row.get(j) instanceof VariantValue){
	    			str = ((VariantValue)row.get(j)).getValueName();
	    		}else if( row.get(j) instanceof String){
	    			str = (String)row.get(j);
	    		}else{
	    			str = "-";
	    		}	
	    		
	    		if( row.get(3) instanceof VariantValue){
	    			label = new jxl.write.Label(j, i + startRow, str, cellFormat);
	    		}else{
	    			label = new jxl.write.Label(j, i + startRow, str, problemCellFormat);
	    		}
	    		sheet.addCell(label);
	    	}
	    }

	    workBook.write();
	    workBook.close();
	}
}

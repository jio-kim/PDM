package com.kgm.commands.ospec;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;

import jxl.Cell;
import jxl.CellView;
import jxl.Workbook;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.Colour;
import jxl.format.VerticalAlignment;
import jxl.write.Label;
import jxl.write.WritableCell;
import jxl.write.WritableCellFormat;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

import com.kgm.commands.ospec.op.OSpec;
import com.kgm.commands.ospec.op.OpGroup;
import com.kgm.commands.ospec.op.OpUtil;
import com.kgm.commands.ospec.panel.OSpecTable;
import com.kgm.common.ui.mergetable.MultiSpanCellTable;
import com.teamcenter.rac.aif.AIFShell;
import com.teamcenter.rac.aif.AbstractAIFDialog;
import com.teamcenter.rac.util.MessageBox;

/**
 * [SR150408-022] [20150529] [ymjang] Variant/Function EPL 수량표현 개선방안(양산 및 개발프로젝트 반영하여 출력할수 있도록)
 */
public class OSpecCompareDlg extends AbstractAIFDialog {

	private final JPanel contentPanel = new JPanel();
	private OSpecTable sourceOSpecTable, targetOSpecTable, onlySourceOSpecTable, onlyTargetOSpecTable;
	private JTextArea textArea = null;
//	private Vector<Vector> onlySourceData = null, onlyTargetData = null, onlySourceDataFill = null, onlyTargetDataFill = null;
	private JCheckBox chkOnlyDiff = null;
	private JSplitPane splitPane = null;
	private JPanel leftPanel = null;
	private JPanel rightPanel = null;
	private JPanel sourceAllDataPanel = null;
	private JPanel targetAllDataPanel = null;
	private JPanel onlySourceDataPanel = null;
	private JPanel onlyTargetDataPanel = null;
	private HashMap<String, HashMap<String, ArrayList<OpGroup>>> referedOpGroupMap = null; // 비교되는 소스
	
	// [SR150408-022] [20150529] [ymjang] Variant/Function EPL 수량표현 개선방안(양산 및 개발프로젝트 반영하여 출력할수 있도록)
	private String mergedOspecNo = null;
	
	
	/**
	 * parentDialog를 인자로 주기 위해 추가한 생성자
	 * @copyright : Plmsoft
	 * @author : 이정건
	 * @since  : 2017. 4. 10.
	 * @param sourceOSpecTable
	 * @param targetOSpecTable
	 * @param onlySourceOSpecTable
	 * @param onlyTargetOSpecTable
	 * @param referedOpGroupMap
	 * @throws Exception
	 */
	public OSpecCompareDlg(final OSpecTable sourceOSpecTable, final OSpecTable targetOSpecTable, OSpecTable onlySourceOSpecTable, OSpecTable onlyTargetOSpecTable, HashMap<String, HashMap<String, ArrayList<OpGroup>>> referedOpGroupMap) throws Exception {
		this(null, sourceOSpecTable, targetOSpecTable, onlySourceOSpecTable, onlyTargetOSpecTable, referedOpGroupMap);
	}
	
	/**
	 * Create the dialog.
	 * @throws Exception 
	 */
	public OSpecCompareDlg(JDialog parentDialog, final OSpecTable sourceOSpecTable, final OSpecTable targetOSpecTable, OSpecTable onlySourceOSpecTable, OSpecTable onlyTargetOSpecTable, HashMap<String, HashMap<String, ArrayList<OpGroup>>> referedOpGroupMap) throws Exception {
		super(parentDialog, true);
		this.sourceOSpecTable = sourceOSpecTable;
		this.targetOSpecTable = targetOSpecTable;
		this.onlySourceOSpecTable = onlySourceOSpecTable;
		this.onlyTargetOSpecTable = onlyTargetOSpecTable;
		this.referedOpGroupMap = referedOpGroupMap;
		
		this.sourceAllDataPanel = sourceOSpecTable.getOspecTable();
		this.targetAllDataPanel = targetOSpecTable.getOspecTable();
		
		HashMap<String, ArrayList<String>> sourceDataMap = OSpecTable.getSimpleDataMap(sourceOSpecTable.getData(), sourceOSpecTable.getHeader());
		HashMap<String, ArrayList<String>> targetDataMap = OSpecTable.getSimpleDataMap(targetOSpecTable.getData(), targetOSpecTable.getHeader());

		sourceOSpecTable.setSimpleDataMap(targetDataMap);
		onlySourceOSpecTable.setSimpleDataMap(targetDataMap);
		
		targetOSpecTable.setSimpleDataMap(sourceDataMap);
		onlyTargetOSpecTable.setSimpleDataMap(sourceDataMap);
		
		// [SR150408-022] [20150529] [ymjang] Variant/Function EPL 수량표현 개선방안(양산 및 개발프로젝트 반영하여 출력할수 있도록)
		this.mergedOspecNo = sourceOSpecTable.getOspec().getOspecNo().replaceAll("OSI-", "") + "&" + 
		                     targetOSpecTable.getOspec().getOspecNo().replaceAll("OSI-", "") ;
		
		setMouseEvent(sourceOSpecTable);
		setMouseEvent(targetOSpecTable);
		setMouseEvent(onlySourceOSpecTable);
		setMouseEvent(onlyTargetOSpecTable);
		
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));
		{
			splitPane = new JSplitPane();
			splitPane.setPreferredSize(new Dimension(1500, 800));
			
			leftPanel = new JPanel();
			leftPanel.setBorder(new TitledBorder(null, "Source", TitledBorder.LEADING, TitledBorder.TOP, null, null));
			leftPanel.setLayout(new BorderLayout(0, 0));
			leftPanel.add(sourceAllDataPanel, BorderLayout.CENTER);
			splitPane.setLeftComponent(leftPanel);
			
			rightPanel = new JPanel();
			rightPanel.setBorder(new TitledBorder(null, "Target", TitledBorder.LEADING, TitledBorder.TOP, null, null));
			rightPanel.setLayout(new BorderLayout(0, 0));
			rightPanel.add(targetAllDataPanel, BorderLayout.CENTER);
			splitPane.setRightComponent(rightPanel);
			
			setAutoScroll(sourceOSpecTable, targetOSpecTable);
			
			splitPane.setOneTouchExpandable(true);
			splitPane.setDividerSize(10);
			contentPanel.add(splitPane);
			splitPane.setDividerLocation(750);
		}
		{
			JPanel panel = new JPanel();
			contentPanel.add(panel, BorderLayout.SOUTH);
			panel.setLayout(new BorderLayout(0, 0));
			{
				JPanel panel_1 = new JPanel();
				FlowLayout flowLayout = (FlowLayout) panel_1.getLayout();
				flowLayout.setAlignment(FlowLayout.LEADING);
				panel_1.setBorder(new TitledBorder(null, "Related Option Group", TitledBorder.LEADING, TitledBorder.TOP, null, null));
				panel.add(panel_1, BorderLayout.CENTER);
				{
					textArea = new JTextArea();
					textArea.setEditable(false);
					textArea.setColumns(80);
					textArea.setRows(2);
					JScrollPane scrollPane = new JScrollPane(textArea);
					panel_1.add(scrollPane);
				}
			}
			{
				chkOnlyDiff = new JCheckBox("Only Difference");
				chkOnlyDiff.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent actionevent) {
						if( chkOnlyDiff.isSelected()){
							splitPane.invalidate();
							try {
								onlySourceDataPanel = OSpecCompareDlg.this.onlySourceOSpecTable.getOspecTable();
								onlyTargetDataPanel = OSpecCompareDlg.this.onlyTargetOSpecTable.getOspecTable();
							} catch (Exception e) {
								e.printStackTrace();
								MessageBox.post(OSpecCompareDlg.this, e.getMessage(), "ERROR", MessageBox.ERROR);
								return;
							}
							setAutoScroll(OSpecCompareDlg.this.onlySourceOSpecTable, OSpecCompareDlg.this.onlyTargetOSpecTable);
							leftPanel.removeAll();
							leftPanel.add(onlySourceDataPanel, BorderLayout.CENTER);
							rightPanel.removeAll();
							rightPanel.add(onlyTargetDataPanel, BorderLayout.CENTER);
//							splitPane.setLeftComponent(onlySourceDataPanel);
//							splitPane.setRightComponent(onlyTargetDataPanel);
							splitPane.revalidate();
						}else{
							splitPane.invalidate();
							try {
								sourceAllDataPanel = OSpecCompareDlg.this.sourceOSpecTable.getOspecTable();
								targetAllDataPanel = OSpecCompareDlg.this.targetOSpecTable.getOspecTable();
							} catch (Exception e) {
								e.printStackTrace();
								MessageBox.post(OSpecCompareDlg.this, e.getMessage(), "ERROR", MessageBox.ERROR);
								return;
							}
							setAutoScroll(OSpecCompareDlg.this.sourceOSpecTable, OSpecCompareDlg.this.targetOSpecTable);
							
							leftPanel.removeAll();
							leftPanel.add(sourceAllDataPanel, BorderLayout.CENTER);
							rightPanel.removeAll();
							rightPanel.add(targetAllDataPanel, BorderLayout.CENTER);
//							splitPane.setLeftComponent(sourceAllDataPanel);
//							splitPane.setRightComponent(targetAllDataPanel);
							splitPane.revalidate();
						}
						
						splitPane.setDividerLocation(750);
					}
				});
				panel.add(chkOnlyDiff, BorderLayout.EAST);
			}
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton closeButton = new JButton("Close");
				closeButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						dispose();
					}
				});
				
				/*
				 * [SR150408-022] [20150529] [ymjang] Variant/Function EPL 수량표현 개선방안(양산 및 개발프로젝트 반영하여 출력할수 있도록)
				 */
				{
					JButton btnExcelExport = new JButton("Export Merged O/Spec");
					btnExcelExport.setIcon(new ImageIcon(OSpecCompareDlg.class.getResource("/com/kgm/common/images/excel_16.png")));
					btnExcelExport.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent actionevent) {
							// [20150728][jclee] Source보다 Target이 더 높아야만 OSpec Merge를 할 수 있도록 수정.
							String sSourceOSpec = sourceOSpecTable.getOspec().getOspecNo();
							String sTargetOSpec = targetOSpecTable.getOspec().getOspecNo();
							
							if (sSourceOSpec.compareTo(sTargetOSpec) > 0)
							{
								MessageBox.post("You must select target to " + sSourceOSpec + "\n\r" + "Source Project must be prior to Target Project!", "Information", MessageBox.INFORMATION);
								return;
							}
							
							JFileChooser fileChooser = new JFileChooser();
							fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY );
							Calendar now = Calendar.getInstance();
							SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddkkmmss");
							sdf.format(now.getTime());
							File defaultFile = new File(mergedOspecNo + "_" + sdf.format(now.getTime()) + ".xlsx");
							fileChooser.setSelectedFile(defaultFile);
//							fileChooser.addChoosableFileFilter(new OptionDefinitionFileFilter("MSEXCEL"));
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
							int result = fileChooser.showSaveDialog(OSpecCompareDlg.this);
							if( result == JFileChooser.APPROVE_OPTION){
								File selectedFile = fileChooser.getSelectedFile();
								try
					            {
									OSpecToXls oSpecToXls = new OSpecToXls (OSpecCompareDlg.this.sourceOSpecTable, OSpecCompareDlg.this.targetOSpecTable);
									
									// OSpec 통합
									oSpecToXls.mergedOspec();
									
									// O/Spec 출력
									oSpecToXls.export(selectedFile);
									
									AIFShell aif = new AIFShell("application/vnd.ms-excel", selectedFile.getAbsolutePath());
									aif.start();
					            }
					            catch (Exception e)
					            {
					            	e.printStackTrace();
					            	MessageBox.post(OSpecCompareDlg.this, e.getMessage(), "ERROR", MessageBox.ERROR);
					            }
							}
						}
					});
					buttonPane.add(btnExcelExport);
				}
				
				{
					JButton btnExcelExport = new JButton("Excel Export");
					btnExcelExport.setIcon(new ImageIcon(OSpecCompareDlg.class.getResource("/com/kgm/common/images/excel_16.png")));
					btnExcelExport.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent actionevent) {
							
							JFileChooser fileChooser = new JFileChooser();
							fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY );
							Calendar now = Calendar.getInstance();
							SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddkkmmss");
							sdf.format(now.getTime());
							File defaultFile = new File("Option_Comparison_" + sdf.format(now.getTime()) + ".xls");
							fileChooser.setSelectedFile(defaultFile);
//							fileChooser.addChoosableFileFilter(new OptionDefinitionFileFilter("MSEXCEL"));
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
							int result = fileChooser.showSaveDialog(OSpecCompareDlg.this);
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
					            	MessageBox.post(e);
					            }
							}
						}
					});
					buttonPane.add(btnExcelExport);
				}
				closeButton.setActionCommand("Closse");
				buttonPane.add(closeButton);
			}
		}
	}
	
	private void setMouseEvent(final OSpecTable ospecTable){
		MultiSpanCellTable fixedTable = ospecTable.getFixedOspecViewTable();
		fixedTable.addMouseListener(new MouseAdapter(){

			@Override
			public void mouseReleased(MouseEvent event) {
				MultiSpanCellTable table = (MultiSpanCellTable)event.getSource();
				int rowIdx = table.getSelectedRow();
				String opValue = null;
				String opCategory = null;
				
				opValue = (String)table.getValueAt(rowIdx, 3);
				if( opValue == null || opValue.length() < 3){
					textArea.setText("");
					return;
				}
				opCategory = OpUtil.getCategory(opValue);
				OSpec ospec = ospecTable.getOspec();
				textArea.setText("");
				HashMap<String, ArrayList<OpGroup>> list = referedOpGroupMap.get(ospec.getProject());
				if( list == null){
					return;
				}
				ArrayList<OpGroup> groupList = list.get(opCategory);
				
				for( int i = 0; groupList != null && i < groupList.size(); i++){
					if( textArea.getText().trim().equals("")){
						textArea.setText(groupList.get(i).getOpGroupName());
					}else{
						textArea.setText(textArea.getText() + ", " + groupList.get(i).getOpGroupName());
					}
				}
			}
			
		});
			
		MultiSpanCellTable dataTable = ospecTable.getOspecViewTable();
		dataTable.addMouseListener(new MouseAdapter(){

			@Override
			public void mouseReleased(MouseEvent event) {
				MultiSpanCellTable fiexedTable = ospecTable.getFixedOspecViewTable();
				MultiSpanCellTable table = (MultiSpanCellTable)event.getSource();
				int rowIdx = table.getSelectedRow();
				String opValue = null;
				String opCategory = null;
				opValue = (String)fiexedTable.getValueAt(rowIdx, 3);
				if( opValue == null || opValue.length() < 3){
					textArea.setText("");
					return;
				}
				opCategory = OpUtil.getCategory(opValue);
				OSpec ospec = ospecTable.getOspec();
				textArea.setText("");
				HashMap<String, ArrayList<OpGroup>> list = referedOpGroupMap.get(ospec.getProject());
				if( list == null){
					return;
				}
				ArrayList<OpGroup> groupList = list.get(opCategory);
				
				for( int i = 0; groupList != null && i < groupList.size(); i++){
					if( textArea.getText().trim().equals("")){
						textArea.setText(groupList.get(i).getOpGroupName());
					}else{
						textArea.setText(textArea.getText() + ", " + groupList.get(i).getOpGroupName());
					}
				}
			}
			
		});
	}

	private void setAutoScroll(OSpecTable source, OSpecTable target){
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
	
	protected void exportToExcel(File selectedFile) throws IOException, WriteException {
		// TODO Auto-generated method stub
		WritableWorkbook workBook = Workbook.createWorkbook(selectedFile);
	    // 0번째 Sheet 생성
	    WritableSheet sheet = workBook.createSheet("new sheet", 0);

	    WritableCellFormat cellFormat = new WritableCellFormat(); // 셀의 스타일을 지정하기 위한 부분입니다.
	    cellFormat.setBorder(Border.ALL, BorderLineStyle.THIN); // 셀의 스타일을 지정합니다. 테두리에 라인그리는거에요
	    Label label = null;
	    
	    WritableCellFormat headerCellFormat = new WritableCellFormat(); // 셀의 스타일을 지정하기 위한 부분입니다.
	    headerCellFormat.setBorder(Border.ALL, BorderLineStyle.THIN);
	    headerCellFormat.setBackground(Colour.GREY_25_PERCENT);

	    int startRow = 1;
	    int initColumnNum = 0;

	    label = new jxl.write.Label(0, startRow, "Option Comparison", cellFormat);
	    sheet.addCell(label);
	    sheet.mergeCells(0, 1, 2, 1);
	    
	    startRow = 3;
	    Vector excelColumnHeader = new Vector();
	    excelColumnHeader.add("CATEGORY");
	    excelColumnHeader.add("OPTION GROUP");
	    excelColumnHeader.add("OWNER");
//	    excelColumnHeader.add("PROJECT");
	    
	    for (int i = 0; i < excelColumnHeader.size(); i++)
	    {
	      label = new jxl.write.Label(i + initColumnNum, startRow, excelColumnHeader.get(i).toString(), headerCellFormat);
	      sheet.addCell(label);
	      CellView cv = sheet.getColumnView(i + initColumnNum);
	      cv.setAutosize(true);
	      sheet.setColumnView(i + initColumnNum, cv);
	    }

	    int rowNum = 0;
	    startRow = 4;
	    
	    Vector<Vector> data = getData();
	    for (int i = 0; i < data.size(); i++)
	    {
	    	Vector row = data.get(i);
	    	for (int j = 0; j < row.size(); j++)
	    	{
	    		label = new jxl.write.Label(j + initColumnNum, (rowNum) + startRow, (String)row.get(j), cellFormat);
	    		sheet.addCell(label);
	    	}
	    	rowNum++;
	    }

	    //셀 Merge
	    int startIdxToMerge = startRow;
	    int endIdxToMerge = startRow;
	    for (int i = 0; i < data.size(); i++){
	    	
	    	Cell cell = sheet.getCell(initColumnNum, i + startRow);
	    	Cell nextCell = sheet.getCell(initColumnNum, i + startRow + 1);
	    	
	    	if( cell.getContents().equals(nextCell.getContents())){
	    		endIdxToMerge = i + 1 + startRow;
	    	}else{
	    		if( startIdxToMerge < endIdxToMerge){
		    		sheet.mergeCells(initColumnNum, startIdxToMerge, initColumnNum, endIdxToMerge);
		    		WritableCell wCell = sheet.getWritableCell(initColumnNum, startIdxToMerge);
	    			WritableCellFormat cf = new WritableCellFormat(); // 셀의 스타일을 지정하기 위한 부분입니다.
	    			cf.setBorder(Border.ALL, BorderLineStyle.THIN); 
	    		    cf.setVerticalAlignment(VerticalAlignment.CENTRE);
	    		    wCell.setCellFormat(cf);
	    		}
	    		startIdxToMerge = i + 1 + startRow;
	    	}
	    }
	    
	    workBook.write();
	    workBook.close();
	}	
	
	private Vector<Vector> getData(){
		HashMap<String, ArrayList<OpGroup>> optionGroupMap = new HashMap();//<Catory명, 관련 Option Group List>
	    OSpec ospec = onlySourceOSpecTable.getOspec();
	    HashMap<String, ArrayList<OpGroup>> list = referedOpGroupMap.get(ospec.getProject());
	    MultiSpanCellTable table = onlySourceOSpecTable.getFixedOspecViewTable();
	    for( int i = 0; i < table.getRowCount(); i++ ){
	    	String opValue = (String)table.getValueAt(i, 3);
	    	String category = OpUtil.getCategory(opValue);
	    	ArrayList<OpGroup> groupList = list.get(category);
	    	
	    	ArrayList<OpGroup> opGroupList = optionGroupMap.get(category);
	    	if( opGroupList == null){
	    		opGroupList = new ArrayList();
	    		if( groupList != null){
		    		opGroupList.addAll(groupList);
		    		optionGroupMap.put(category, opGroupList);
	    		}
	    	}else{
	    		for( OpGroup group : groupList){
	    			if( !opGroupList.contains(group)){
	    				opGroupList.add(group);
	    			}
	    		}
	    	}
	    	Collections.sort(opGroupList);
	    }
	    
	    ospec = onlyTargetOSpecTable.getOspec();
	    HashMap<String, ArrayList<OpGroup>> list1 = referedOpGroupMap.get(ospec.getProject());
	    table = onlyTargetOSpecTable.getFixedOspecViewTable();
	    for( int i = 0; i < table.getRowCount(); i++ ){
	    	String opValue = (String)table.getValueAt(i, 3);
	    	String category = OpUtil.getCategory(opValue);
	    	ArrayList<OpGroup> groupList = list1.get(category);
	    	
	    	ArrayList<OpGroup> opGroupList = optionGroupMap.get(category);
	    	if( opGroupList == null){
	    		opGroupList = new ArrayList();
	    		if( groupList != null){
		    		opGroupList.addAll(groupList);
		    		optionGroupMap.put(category, opGroupList);
	    		}
	    	}else{
	    		for( OpGroup group : groupList){
	    			if( !opGroupList.contains(group)){
	    				opGroupList.add(group);
	    			}
	    		}
	    	}
	    	Collections.sort(opGroupList);
	    }
	    
	    List<String> keyList = Arrays.asList( optionGroupMap.keySet().toArray(new String[optionGroupMap.size()]));
	    Collections.sort(keyList);
	    
	    Vector<Vector> data = new Vector();
	    for( String category : keyList){
	    	ArrayList<OpGroup> groupList = optionGroupMap.get(category);
	    	for( OpGroup group : groupList){
	    		Vector row = new Vector();
	    		row.add(category);
	    		row.add(group.getOpGroupName());
	    		row.add(group.getOwner());
	    		data.add(row);
	    	}
	    }
	    return data;
	}
}

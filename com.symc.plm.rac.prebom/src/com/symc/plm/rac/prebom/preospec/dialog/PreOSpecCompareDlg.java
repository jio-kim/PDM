package com.symc.plm.rac.prebom.preospec.dialog;

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
import com.kgm.common.ui.mergetable.MultiSpanCellTable;
import com.symc.plm.rac.prebom.preospec.ui.OSpecTable;
import com.teamcenter.rac.aif.AIFShell;
import com.teamcenter.rac.aif.AbstractAIFDialog;
import com.teamcenter.rac.util.MessageBox;

public class PreOSpecCompareDlg extends AbstractAIFDialog {

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
	private HashMap<String, HashMap<String, ArrayList<OpGroup>>> referedOpGroupMap = null;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			PreOSpecCompareDlg dialog = new PreOSpecCompareDlg(null, null, null, null, null);
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
	public PreOSpecCompareDlg(OSpecTable sourceOSpecTable, OSpecTable targetOSpecTable
			, OSpecTable onlySourceOSpecTable, OSpecTable onlyTargetOSpecTable
			, HashMap<String, HashMap<String, ArrayList<OpGroup>>> referedOpGroupMap) throws Exception {
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
		
		setMouseEvent(sourceOSpecTable);
		setMouseEvent(targetOSpecTable);
		setMouseEvent(onlySourceOSpecTable);
		setMouseEvent(onlyTargetOSpecTable);
		
		setBounds(100, 100, 1117, 636);
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
			splitPane.setDividerLocation(560);
		}
		{
			JPanel panel = new JPanel();
			contentPanel.add(panel, BorderLayout.SOUTH);
			panel.setLayout(new BorderLayout(0, 0));
		}
		{
			JPanel buttonPane = new JPanel();
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			buttonPane.setLayout(new BorderLayout(0, 0));
			{
				{
					{
						JPanel panel = new JPanel();
						FlowLayout flowLayout = (FlowLayout) panel.getLayout();
						flowLayout.setAlignment(FlowLayout.LEADING);
						buttonPane.add(panel, BorderLayout.CENTER);
						{
							chkOnlyDiff = new JCheckBox("Only Difference");
							panel.add(chkOnlyDiff);
							chkOnlyDiff.addActionListener(new ActionListener() {
								public void actionPerformed(ActionEvent actionevent) {
									if( chkOnlyDiff.isSelected()){
										splitPane.invalidate();
										try {
											onlySourceDataPanel = PreOSpecCompareDlg.this.onlySourceOSpecTable.getOspecTable();
											onlyTargetDataPanel = PreOSpecCompareDlg.this.onlyTargetOSpecTable.getOspecTable();
										} catch (Exception e) {
											e.printStackTrace();
											MessageBox.post(PreOSpecCompareDlg.this, e.getMessage(), "ERROR", MessageBox.ERROR);
											return;
										}
										setAutoScroll(PreOSpecCompareDlg.this.onlySourceOSpecTable, PreOSpecCompareDlg.this.onlyTargetOSpecTable);
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
											sourceAllDataPanel = PreOSpecCompareDlg.this.sourceOSpecTable.getOspecTable();
											targetAllDataPanel = PreOSpecCompareDlg.this.targetOSpecTable.getOspecTable();
										} catch (Exception e) {
											e.printStackTrace();
											MessageBox.post(PreOSpecCompareDlg.this, e.getMessage(), "ERROR", MessageBox.ERROR);
											return;
										}
										setAutoScroll(PreOSpecCompareDlg.this.sourceOSpecTable, PreOSpecCompareDlg.this.targetOSpecTable);
										
										leftPanel.removeAll();
										leftPanel.add(sourceAllDataPanel, BorderLayout.CENTER);
										rightPanel.removeAll();
										rightPanel.add(targetAllDataPanel, BorderLayout.CENTER);
//							splitPane.setLeftComponent(sourceAllDataPanel);
//							splitPane.setRightComponent(targetAllDataPanel);
										splitPane.revalidate();
									}
									
									splitPane.setDividerLocation(560);
								}
							});
						}
					}
					{
						JPanel panel = new JPanel();
						FlowLayout flowLayout = (FlowLayout) panel.getLayout();
						flowLayout.setAlignment(FlowLayout.TRAILING);
						buttonPane.add(panel, BorderLayout.EAST);
						JButton closeButton = new JButton("Close");
						panel.add(closeButton);
						closeButton.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent arg0) {
								dispose();
							}
						});
						closeButton.setActionCommand("Close");
					}
				}
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
	    list = referedOpGroupMap.get(ospec.getProject());
	    table = onlyTargetOSpecTable.getFixedOspecViewTable();
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

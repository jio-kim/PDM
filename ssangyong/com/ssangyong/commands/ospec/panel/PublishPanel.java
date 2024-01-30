package com.ssangyong.commands.ospec.panel;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.script.ScriptException;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

import jxl.Cell;
import jxl.CellView;
import jxl.Workbook;
import jxl.format.Alignment;
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

import org.apache.axis.utils.StringUtils;

import com.ssangyong.commands.ospec.OSpecBomCompareDlg;
import com.ssangyong.commands.ospec.OSpecConditionSetDlg;
import com.ssangyong.commands.ospec.OSpecMainDlg;
import com.ssangyong.commands.ospec.op.OSpec;
import com.ssangyong.commands.ospec.op.OpCategory;
import com.ssangyong.commands.ospec.op.OpComboValue;
import com.ssangyong.commands.ospec.op.OpComparableConditionSet;
import com.ssangyong.commands.ospec.op.OpGroup;
import com.ssangyong.commands.ospec.op.OpTrim;
import com.ssangyong.commands.ospec.op.OpUtil;
import com.ssangyong.commands.ospec.op.OpValueName;
import com.ssangyong.commands.ospec.op.Option;
import com.ssangyong.common.WaitProgressBar;
import com.ssangyong.common.remote.DataSet;
import com.ssangyong.common.remote.SYMCRemoteUtil;
import com.ssangyong.common.ui.mergetable.AttributiveCellTableModel;
import com.ssangyong.common.ui.mergetable.CellSpan;
import com.ssangyong.common.ui.mergetable.MultiSpanCellTable;
import com.teamcenter.rac.aif.AIFShell;
import com.teamcenter.rac.util.MessageBox;

/**
 * @author slobbie_vm
 *
 */
public class PublishPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	
	private OSpecMainDlg parentDlg = null;
	private JTable conditionTable = null;
	private MultiSpanCellTable allDataTable = null;
	private MultiSpanCellTable filteredDataTable = null;
	private MultiSpanCellTable conditionDataTable = null;
	private JScrollPane dataScroll = null;
	private OSpec changedOspec = null;
	private JLabel countLabel= null;
	public JCheckBox chckbxShowAllCondition;
	private JButton btnBomCompare = null;
	private JButton btnExportDwg = null;
	private JButton btnExportCondition = null;
	private TitledBorder titledBorder = null;
	private JPanel dataPanel = null;

	private ArrayList<OpGroup> selectedOpGroup = null; //선택된 Option Group
	private static final String EXPORT_FOR_CONDITION = "exportForCondition";
	private static final String EXPORT_FOR_DRAWING = "exportForDrawing";
//	private static final String MANDATORY_OPTION = "MANDATORY";
//	private static final String OPTIONAL_OPTION = "OPTIONAL";
	
//	private ArrayList<String> alCache = new ArrayList<String>();
	
	public PublishPanel(OSpecMainDlg parentDlg) {
		
		this.parentDlg = parentDlg;
		init();
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void init(){
		setLayout(new BorderLayout(0, 0));
		
		JPanel panel = new JPanel();
		panel.setBorder(new TitledBorder(null, "OR Combination List", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		add(panel, BorderLayout.WEST);
		panel.setLayout(new BorderLayout(0, 0));
		
		JPanel panel_2 = new JPanel();
		panel.add(panel_2, BorderLayout.CENTER);
		panel_2.setLayout(new BorderLayout(0, 0));
		
		Vector<String> conditionHeader = new Vector();
		conditionHeader.add("Condition");
		
		DefaultTableModel model = new DefaultTableModel(null, conditionHeader);
		
		conditionTable = new JTable(model);
		JScrollPane scrollPane = new JScrollPane(conditionTable);
		scrollPane.setPreferredSize(new Dimension(250, 402));
		panel_2.add(scrollPane, BorderLayout.CENTER);
		
		JPanel panel_3 = new JPanel();
		panel.add(panel_3, BorderLayout.SOUTH);
		panel_3.setLayout(new BorderLayout(0, 0));
		
		JPanel panel_4 = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel_4.getLayout();
		flowLayout.setAlignment(FlowLayout.LEADING);
		panel_3.add(panel_4, BorderLayout.CENTER);
		
		JButton btnConditionAdd = new JButton("");
		btnConditionAdd.setPreferredSize(new Dimension(25, 25));
		btnConditionAdd.setIcon(new ImageIcon(PublishPanel.class.getResource("/com/teamcenter/rac/aif/images/add_16.png")));
		btnConditionAdd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if( changedOspec == null)
					return;
				OSpecConditionSetDlg dlg = new OSpecConditionSetDlg(parentDlg, changedOspec);
				dlg.setVisible(true);
			}
		});
		panel_4.add(btnConditionAdd);
		
		JButton btnConditionDel = new JButton("");
		btnConditionDel.setPreferredSize(new Dimension(25, 25));
		btnConditionDel.setIcon(new ImageIcon(PublishPanel.class.getResource("/com/teamcenter/rac/aif/images/remove_16.png")));
		btnConditionDel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				removeCondition();
			}
		});
		panel_4.add(btnConditionDel);
		
		JButton btnLoad = new JButton("");
		btnLoad.setToolTipText("Load condition of selected Option Group");
		btnLoad.setPreferredSize(new Dimension(25, 25));
		btnLoad.setIcon(new ImageIcon(PublishPanel.class.getResource("/icons/condLoad_16.png")));
		btnLoad.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent paramActionEvent) {
				try
				{
					loadSavedCondition();
				}catch(Exception ex)
				{
					MessageBox.post(parentDlg, ex.toString(), "ERROR", MessageBox.ERROR);
				}
			}
		});
		panel_4.add(btnLoad);
		
		JButton btnSave = new JButton("");
		btnSave.setToolTipText("Save current Condition");
		btnSave.setPreferredSize(new Dimension(25, 25));
		btnSave.setIcon(new ImageIcon(PublishPanel.class.getResource("/icons/save_16.png")));
		btnSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent paramActionEvent) {
				try
				{
					saveCondtion();
				}catch(Exception ex)
				{
					MessageBox.post(parentDlg, ex.toString(), "ERROR", MessageBox.ERROR);
				}
			}
		});
		panel_4.add(btnSave);
		
		JPanel panel_5 = new JPanel();
		FlowLayout flowLayout_1 = (FlowLayout) panel_5.getLayout();
		flowLayout_1.setAlignment(FlowLayout.TRAILING);
		panel_3.add(panel_5, BorderLayout.EAST);
		
		JButton btnApply = new JButton("Apply");
		btnApply.setIcon(new ImageIcon(PublishPanel.class.getResource("/com/ssangyong/common/images/ok_16.png")));
		btnApply.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					apply();
				} catch (Exception e) {
					e.printStackTrace();
					MessageBox.post(parentDlg, e.getMessage(), "ERROR", MessageBox.ERROR);
				}
			}
		});
		panel_5.add(btnApply);
		
		JPanel panel_1 = new JPanel();
		add(panel_1, BorderLayout.CENTER);
		panel_1.setLayout(new BorderLayout(0, 0));
		
		JPanel panel_6 = new JPanel();
		panel_1.add(panel_6, BorderLayout.NORTH);
		panel_6.setLayout(new BorderLayout(0, 0));
		
		JPanel panel_9 = new JPanel();
		FlowLayout flowLayout_2 = (FlowLayout) panel_9.getLayout();
		flowLayout_2.setAlignment(FlowLayout.LEADING);
		panel_6.add(panel_9, BorderLayout.CENTER);
		
		JLabel lblEnableCount = new JLabel("Conditional Count / Filter Count / Total Count  : ");
		panel_9.add(lblEnableCount);
		
		countLabel= new JLabel("0 / 0 / 0");
		panel_9.add(countLabel);
		
		JPanel panel_10 = new JPanel();
		FlowLayout flowLayout_4 = (FlowLayout) panel_10.getLayout();
		flowLayout_4.setAlignment(FlowLayout.TRAILING);
		panel_6.add(panel_10, BorderLayout.EAST);
		
		chckbxShowAllCondition = new JCheckBox("Show All Condition");
		chckbxShowAllCondition.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionevent) {
				if( chckbxShowAllCondition.isSelected()){
					dataScroll.setViewportView(allDataTable);
					btnBomCompare.setEnabled(false);
					btnExportDwg.setEnabled(false);
					btnExportCondition.setEnabled(false);
					titledBorder.setTitle(OSpecMainDlg.TOTAL_DATA);
				}else{
					dataScroll.setViewportView(filteredDataTable);
					btnBomCompare.setEnabled(true);
					btnExportDwg.setEnabled(true);
					btnExportCondition.setEnabled(true);
					titledBorder.setTitle(OSpecMainDlg.FILTERED_DATA);
				}
				dataPanel.repaint();
				dataScroll.revalidate();
			}
		});
		panel_10.add(chckbxShowAllCondition);
		
		dataPanel = new JPanel();
		titledBorder = new TitledBorder(null, OSpecMainDlg.FILTERED_DATA, TitledBorder.LEADING, TitledBorder.TOP, null, null);
		dataPanel.setBorder(titledBorder);
		panel_1.add(dataPanel, BorderLayout.CENTER);
		dataPanel.setLayout(new BorderLayout(0, 0));
		
		dataScroll = new JScrollPane();
		dataPanel.add(dataScroll, BorderLayout.CENTER);
		
		JPanel panel_8 = new JPanel();
		FlowLayout flowLayout_3 = (FlowLayout) panel_8.getLayout();
		flowLayout_3.setAlignment(FlowLayout.TRAILING);
		panel_1.add(panel_8, BorderLayout.SOUTH);
		
		btnBomCompare = new JButton("Compare with BOM Line Condition");
		btnBomCompare.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				OSpecBomCompareDlg dlg;
				try {
					dlg = new OSpecBomCompareDlg(parentDlg);
					dlg.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
					MessageBox.post(parentDlg, e.getMessage(), "ERROR", MessageBox.ERROR);
				}
				
			}
		});
		panel_8.add(btnBomCompare);
		
		btnExportDwg = new JButton("Export for drawing");
		btnExportDwg.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				exportToExcel(EXPORT_FOR_DRAWING);
			}
		});
		panel_8.add(btnExportDwg);
		
		btnExportCondition = new JButton("Export for Condition");
		btnExportCondition.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				exportToExcel(EXPORT_FOR_CONDITION);
			}
		});
		panel_8.add(btnExportCondition);	
		
		setOspec(null);
	}
	
	@SuppressWarnings("unchecked")
	private void apply() throws Exception{
		
		DefaultTableModel model = (DefaultTableModel)filteredDataTable.getModel();
		JTable table = getConditionDataTable(changedOspec, model.getDataVector());
		
		titledBorder.setTitle(OSpecMainDlg.CONDITIONAL_DATA);
		dataPanel.repaint();
		this.dataScroll.setViewportView(table);
		chckbxShowAllCondition.setSelected(false);
		btnBomCompare.setEnabled(true);
		btnExportDwg.setEnabled(true);
		btnExportCondition.setEnabled(true);
	}
	
	@SuppressWarnings("unchecked")
	public void setOspec(OSpec changedOspec){
		this.changedOspec = changedOspec;
		WaitProgressBar waitProgress = new WaitProgressBar(parentDlg);
		try
        {
			waitProgress.start();
			waitProgress.setStatus("Loading....");
			
			parentDlg.removeAllRow(conditionTable);
			
			chckbxShowAllCondition.setSelected(false);
			btnBomCompare.setEnabled(true);
			btnExportDwg.setEnabled(true);
			btnExportCondition.setEnabled(true);
			
			JTable table = getAllDataTable(changedOspec);
			DefaultTableModel model = (DefaultTableModel)table.getModel();
			
			//수정 getFilteredDataTable 메소드는 인자로 OSpec, Vector<OpComparableConditionSet> 타입을 받기 때문에 
			//model.getDataVector() 메소드로 반환된 Vector<Vector>를 Vector<OpComparableConditionSet> 타입으로 캐스팅함.
			Vector<OpComparableConditionSet> castedModel = new Vector<>();
			Vector<Vector> rawData = model.getDataVector();
			for (Vector vector : rawData) {
				OpComparableConditionSet opComparableConditionSet = (OpComparableConditionSet) vector;
			    castedModel.add(opComparableConditionSet);
			}
			JTable filterTable = getFilteredDataTable(changedOspec, castedModel);
			
//			JTable filterTable = getFilteredDataTable(changedOspec, model.getDataVector());
			
			model = (DefaultTableModel)filterTable.getModel();
			JTable conditionTable =  getConditionDataTable(changedOspec, model.getDataVector());
			countLabel.setText(conditionTable.getRowCount() + " / " + filterTable.getRowCount() + " / " + table.getRowCount());
			titledBorder.setTitle(OSpecMainDlg.FILTERED_DATA);
			dataScroll.setViewportView(filterTable);
			waitProgress.dispose();
		} catch (Exception e) {
			e.printStackTrace();
			waitProgress.setStatus(e.getMessage());
        	waitProgress.setShowButton(true);
			
//			MessageBox.post(parentDlg, e.getMessage(), "ERROR", MessageBox.ERROR);
			return;
		}
		dataScroll.revalidate();
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes", "serial" })
	private JTable getAllDataTable(OSpec ospec) throws Exception{
		
		Vector<String> dataHeader = new Vector();
		int[] columnWidth = null;
		Vector<OpComparableConditionSet> data = null;
		if( ospec == null){
			
			dataHeader.add("Trim");
			ArrayList<OpValueName> valueList = parentDlg.getOspec().getOpNameList();
			columnWidth = new int[valueList.size() + 2];
			int colIdx = 0;
			columnWidth[colIdx++] = 60;
			
			for( OpValueName opValueName : valueList){
				dataHeader.add(opValueName.getOption());
				columnWidth[colIdx++] = 50;
			}
			dataHeader.add("EPL");
			columnWidth[colIdx++] = 1200;
			
			data = new Vector();
		}else{
			
			dataHeader.add("Trim");
			ArrayList<OpValueName> valueList = ospec.getOpNameList();
			columnWidth = new int[valueList.size() + 2];
			int colIdx = 0;
			columnWidth[colIdx++] = 60;
			
			for( OpValueName opValueName : valueList){
				dataHeader.add(opValueName.getOption());
				columnWidth[colIdx++] = 50;
			}
			dataHeader.add("EPL");
			columnWidth[colIdx++] = 1200;
			
			data = getCaseOption(ospec);
			if( data != null){
				Collections.sort(data);
			}
//			countLabel.setText("0/" + data.size());
		}
		
		AttributiveCellTableModel dataModel = new AttributiveCellTableModel(data, dataHeader){

			@Override
			public boolean isCellEditable(int i, int j) {
				return false;
			}
			
		};
		allDataTable = new MultiSpanCellTable(dataModel);
		allDataTable.getColumnModel().setColumnMargin(0);
		allDataTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {

			@Override
			public Component getTableCellRendererComponent(JTable table, Object value,
				      boolean isSelected, boolean hasFocus, int row, int column) {
				Component com = super.getTableCellRendererComponent(table, value,
					      isSelected, hasFocus, row, column);
				if( com instanceof JLabel){
					JLabel label = (JLabel)com;
					if( value instanceof Option){
						Option option = (Option)value;
						
						String v = option.getValue();
						label.setText(v);
						if( v != null && !v.equals("-")){
							String toolTip = "Package : " + option.getPackageName() + "." + option.getRemark();
							label.setToolTipText("<html>"+toolTip.replaceAll("\\.","<br>")+"</html>");
						}else{
							label.setToolTipText("");
						}
					}else{
						label.setToolTipText("");
					}
					
					if( column == (table.getColumnCount() - 1)){
						Object obj = table.getValueAt(row, column);
						if( obj instanceof String){
							label.setToolTipText((String)obj);
						}
					}
				}
				
				return com;
			}
		});
		allDataTable.setColumnSelectionAllowed(true);
		allDataTable.getTableHeader().setReorderingAllowed(false);
		allDataTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		
		TableColumnModel cm = allDataTable.getColumnModel();
		for(int i = 0; i < cm.getColumnCount(); i++){
			cm.getColumn(i).setPreferredWidth(columnWidth[i]);
		}
		
		return allDataTable;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes", "serial" })
	private JTable getFilteredDataTable(OSpec ospec, Vector<OpComparableConditionSet> data) throws Exception{
		
		Vector<String> dataHeader = new Vector();
		int[] columnWidth = null;
		Vector<OpComparableConditionSet> newData = new Vector();
		if( ospec == null){
			
//			dataHeader.add("Trim");
			ArrayList<OpValueName> valueList = parentDlg.getOspec().getOpNameList();
			columnWidth = new int[valueList.size() + 1];
			int colIdx = 0;
//			columnWidth[colIdx++] = 60;
			
			for( OpValueName opValueName : valueList){
				dataHeader.add(opValueName.getOption());
				columnWidth[colIdx++] = 50;
			}
			dataHeader.add("EPL");
			columnWidth[colIdx++] = 1200;
			
			data = new Vector();
		}else{
			
//			dataHeader.add("Trim");
			ArrayList<OpValueName> valueList = ospec.getOpNameList();
			columnWidth = new int[valueList.size() + 1];
			int colIdx = 0;
//			columnWidth[colIdx++] = 60;
			
			for( OpValueName opValueName : valueList){
				dataHeader.add(opValueName.getOption());
				columnWidth[colIdx++] = 50;
			}
			dataHeader.add("EPL");
			columnWidth[colIdx++] = 1200;
			
			if( data != null){
				for( int i = 0; i < data.size(); i++){
					OpComparableConditionSet conditionSet = data.get(i);
					OpComparableConditionSet row = (OpComparableConditionSet)conditionSet.clone();
					row.remove(0);
					if( !isContained(newData, row)){
						newData.add(row);
					}
				}
				Collections.sort(newData);
			}
		}
		
		AttributiveCellTableModel dataModel = new AttributiveCellTableModel(newData, dataHeader){

			@Override
			public boolean isCellEditable(int i, int j) {
				return false;
			}
			
		};
		filteredDataTable = new MultiSpanCellTable(dataModel);
		filteredDataTable.getColumnModel().setColumnMargin(0);
		filteredDataTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {

			@Override
			public Component getTableCellRendererComponent(JTable table, Object value,
				      boolean isSelected, boolean hasFocus, int row, int column) {
				Component com = super.getTableCellRendererComponent(table, value,
					      isSelected, hasFocus, row, column);
				if( com instanceof JLabel){
					JLabel label = (JLabel)com;
					if( value instanceof Option){
						Option option = (Option)value;
						
						String v = option.getValue();
						label.setText(v);
						if( v != null && !v.equals("-")){
							String toolTip = "Package : " + option.getPackageName() + "." + option.getRemark();
							label.setToolTipText("<html>"+toolTip.replaceAll("\\.","<br>")+"</html>");
						}else{
							label.setToolTipText("");
						}
					}else{
						label.setToolTipText("");
					}
					
					if( column == (table.getColumnCount() - 1)){
						Object obj = table.getValueAt(row, column);
						if( obj instanceof String){
							label.setToolTipText((String)obj);
						}
					}
				}
				
				return com;
			}
		});
		filteredDataTable.setColumnSelectionAllowed(true);
		filteredDataTable.getTableHeader().setReorderingAllowed(false);
		filteredDataTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		
		TableColumnModel cm = filteredDataTable.getColumnModel();
		for(int i = 0; i < cm.getColumnCount(); i++){
			cm.getColumn(i).setPreferredWidth(columnWidth[i]);
		}
		
		return filteredDataTable;
	}	
	
	@SuppressWarnings({ "unchecked", "rawtypes", "serial" })
	private JTable getConditionDataTable(OSpec ospec, Vector<Vector> data) throws Exception{
		
		Vector<String> dataHeader = new Vector();
		int[] columnWidth = null;
		Vector<OpComparableConditionSet> newData = new Vector();
		HashMap<String, HashMap<String, ArrayList>> conditionMap = new HashMap();
		if( ospec == null){
			
//			dataHeader.add("Trim");
			ArrayList<OpValueName> valueList = parentDlg.getOspec().getOpNameList();
			columnWidth = new int[valueList.size() + 1];
			int colIdx = 0;
//			columnWidth[colIdx++] = 60;
			
			for( OpValueName opValueName : valueList){
				dataHeader.add(opValueName.getOption());
				columnWidth[colIdx++] = 50;
			}
			dataHeader.add("EPL");
			columnWidth[colIdx++] = 1200;
			
			data = new Vector();
		}else{
			
//			dataHeader.add("Trim");
			ArrayList<OpValueName> valueList = ospec.getOpNameList();
			columnWidth = new int[valueList.size() + 1];
			int colIdx = 0;
//			columnWidth[colIdx++] = 60;
			
			for( OpValueName opValueName : valueList){
				dataHeader.add(opValueName.getOption());
				columnWidth[colIdx++] = 50;
			}
			dataHeader.add("EPL");
			columnWidth[colIdx++] = 1200;
			
			if( data != null){
				DefaultTableModel model = (DefaultTableModel)conditionTable.getModel();
				Vector<Vector> conditionData = model.getDataVector();
				for( int i = 0; i < data.size(); i++){
					Vector conditionSet = data.get(i);
					OpComparableConditionSet row = (OpComparableConditionSet)conditionSet.clone();
					
					
					for( Vector conditionRow : conditionData){
						String conditionOprator = conditionRow.get(0).toString();
						//String conditionOprator = coditionSb.toString();
						String conditionStr = (String)row.get(row.size()-1);
						if( isAvailable(conditionOprator, row)){
							String exclusiveCondition = getExclusiveCondition(conditionOprator, conditionStr);
							HashMap<String, ArrayList> exConditionMap = conditionMap.get(conditionOprator);
							
							ArrayList list = null;
							if( exConditionMap == null){
								exConditionMap = new HashMap();
								list = new ArrayList();
								list.add(row);
								exConditionMap.put(exclusiveCondition, list);
								conditionMap.put(conditionOprator, exConditionMap);
							}else{
								list = exConditionMap.get(exclusiveCondition);
								if( list == null){
									list = new ArrayList();
									list.add(row);
									exConditionMap.put(exclusiveCondition, list);
								}else{
									list.add(row);
								}
							}
						}
					}
					if( !isContained(newData, row)){
						newData.add(row);
					}
				}
				
				ArrayList<String> tmpList = new ArrayList();
				List<String> coditionOperatorArray = Arrays.asList( conditionMap.keySet().toArray(new String[conditionMap.size()]));
				for( String conditionOperator : coditionOperatorArray){
					HashMap<String, ArrayList> exConditionMap = conditionMap.get(conditionOperator);
					String[] exclusiveConditions = exConditionMap.keySet().toArray(new String[exConditionMap.size()]);
					for( String exclusiveCondition:exclusiveConditions){
						ArrayList<OpComparableConditionSet> list = exConditionMap.get(exclusiveCondition);
						String conditionStr = "";
						ArrayList<String> conditionList = new ArrayList();
						
						for( OpComparableConditionSet row:list){
							conditionList.add((String)row.get(row.size() - 1));
						}
						
						Collections.sort(conditionList);
						for( String cndStr:conditionList){
							if( conditionStr.equals("")){
								conditionStr = cndStr;
							} else{
								conditionStr += " OR " + cndStr;
							}
						}
						
						/**
						 * Condition String 내에서 서로 중복되는 항목들 제거
						 */
						conditionStr = removeDuplicateCondition(conditionStr);
						
						for( OpComparableConditionSet row:list){
							row.set(row.size() - 1, conditionStr);
						}
					}
					
				}
				
				Collections.sort(newData);
				
				for(OpComparableConditionSet row:newData){
					String conStr = (String)row.get(row.size() - 1);
					if( !tmpList.contains(conStr) ){
						tmpList.add(conStr);
					}
				}
				
				countLabel.setText(tmpList.size() + " / " + filteredDataTable.getRowCount() + " / " + allDataTable.getRowCount());
			}
		}
		
		AttributiveCellTableModel dataModel = new AttributiveCellTableModel(newData, dataHeader){
			@Override
			public boolean isCellEditable(int i, int j) {
				return false;
			}
			
		};
		
		conditionDataTable = new MultiSpanCellTable(dataModel);
		cellMerge(conditionDataTable, conditionDataTable.getColumnCount());
		conditionDataTable.getColumnModel().setColumnMargin(0);
		conditionDataTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {

			@Override
			public Component getTableCellRendererComponent(JTable table, Object value,
				      boolean isSelected, boolean hasFocus, int row, int column) {
				Component com = super.getTableCellRendererComponent(table, value,
					      isSelected, hasFocus, row, column);
				if( com instanceof JLabel){
					JLabel label = (JLabel)com;
					if( value instanceof Option){
						Option option = (Option)value;
						
						String v = option.getValue();
						label.setText(v);
						if( v != null && !v.equals("-")){
							String toolTip = "Package : " + option.getPackageName() + "." + option.getRemark();
							label.setToolTipText("<html>"+toolTip.replaceAll("\\.","<br>")+"</html>");
						}else{
							label.setToolTipText("");
						}
					}else{
						label.setToolTipText("");
					}
					
					if( column == (table.getColumnCount() - 1)){
						Object obj = table.getValueAt(row, column);
						if( obj instanceof String){
							String toolTip = "<html>"+((String)obj).replaceAll(" OR ","<br>")+"</html>";
							label.setToolTipText(toolTip);
							label.setText(toolTip);
						}
					}
				}
				
				return com;
			}
		});
		conditionDataTable.setColumnSelectionAllowed(true);
		conditionDataTable.getTableHeader().setReorderingAllowed(false);
		conditionDataTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		
		TableColumnModel cm = conditionDataTable.getColumnModel();
		for(int i = 0; i < cm.getColumnCount(); i++){
			cm.getColumn(i).setPreferredWidth(columnWidth[i]);
		}
		
		return conditionDataTable;
	}	

	/**
	 * Condition 중복 제거
	 * @param conditionStr
	 * @return
	 */
	private String removeDuplicateCondition(String conditionStr) {
		String sRemoveDupCond = "";
		String[] saConditions = conditionStr.split(" OR ");	// OR을 Seperate로 해서 Condition 배열 생성
		ArrayList<String> alCondition = new ArrayList<String>();
		
		// OR이 없을 경우 다시 원래 Condition 반환
		if (saConditions.length == 0) {
			return conditionStr;
		}
		
		// 중복제거
		for (int inx = 0; inx < saConditions.length; inx++) {
			if (alCondition.isEmpty()) {
				alCondition.add(saConditions[inx]);
			} else {
				if (alCondition.contains(saConditions[inx])) {
					continue;
				} else {
					alCondition.add(saConditions[inx]);
				}
			}
		}
		
		// 중복제거한 내용을 바탕으로 다시 OR로 묶은 Condition 생성
		for (int inx = 0; inx < alCondition.size(); inx++) {
			if (sRemoveDupCond.equals("")) {
				sRemoveDupCond = alCondition.get(inx);
			} else {
				sRemoveDupCond += " OR " + alCondition.get(inx);
			}
		}
		
		return sRemoveDupCond;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void cellMerge(MultiSpanCellTable table, int toColumn){
		
		
		ArrayList<Integer> rowList = new ArrayList(); 
		int[] rows = null;
		
		String preValue = null;
//		TableColumnModel cm = table.getColumnModel();
		AttributiveCellTableModel model = (AttributiveCellTableModel)table.getModel();
		CellSpan cellAtt = (CellSpan) model.getCellAttribute();
		for( int column = 0; column < toColumn; column++){
			preValue = null;
			rowList.clear();
			for( int row = 0; row < table.getRowCount(); row++){
				Object obj = table.getValueAt(row, column);
				if( !(obj instanceof String)){
					continue;
				}
				String value = (String)table.getValueAt(row, column);
				if( preValue == null ){
					preValue = value;
					rowList.add(new Integer(row));
				}else{
					if( preValue.equals(value)){
						rowList.add(new Integer(row));
					}else{
						rows = new int[rowList.size()];
						for(int i = 0; i< rowList.size(); i++){
							Integer in = rowList.get(i);
							rows[i] = in.intValue();
						}
						cellAtt.combine(rows, new int[]{column});
						preValue = value;
						rowList.clear();
						rowList.add(new Integer(row));
					}
				}
			}
			
			if( rowList.size() > 1){
				rows = new int[rowList.size()];
				for(int i = 0; i< rowList.size(); i++){
					Integer in = rowList.get(i);
					rows[i] = in.intValue();
				}
				cellAtt.combine(rows, new int[]{column});
			}
		}
		
		if( rowList.size() > 1){
			rows = new int[rowList.size()];
			for(int i = 0; i< rowList.size(); i++){
				Integer in = rowList.get(i);
				rows[i] = in.intValue();
			}
			cellAtt.combine(rows, new int[]{toColumn - 1});
		}
		
		table.clearSelection();
		table.revalidate();
		table.repaint();
	}	
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Vector<OpComparableConditionSet> getCaseOption(OSpec ospec) throws Exception{
		
		HashMap<String, HashMap<String, HashMap<String, OpComparableConditionSet>>> allData = new HashMap();
		ArrayList<OpValueName> valueList = ospec.getOpNameList();
		ArrayList<OpTrim> trimList = ospec.getTrimList();
		HashMap<String, HashMap<String, OpCategory>> categoryAllMap = ospec.getCategory();
//		HashMap<String, ArrayList<Option>> options = ospec.getOptions();
		for( OpTrim trim:trimList){

			HashMap<String, HashMap<String, OpComparableConditionSet>> packedData = new HashMap(); 
			
			HashMap<String, OpCategory> categoryMap = categoryAllMap.get(trim.getTrim());
			if( categoryMap == null){
				continue;
			}
			
			List<String> categoryList = Arrays.asList(categoryMap.keySet().toArray(new String[categoryMap.size()]));
			Collections.sort(categoryList);
			for( String category : categoryList){
				
				OpCategory opCategory = categoryMap.get(category);
				ArrayList<Option> list = opCategory.getOpValueList();
				Collections.sort(list);
				HashMap<String, OpComparableConditionSet> categorySet = packedData.get(category);
				for( OpValueName opValueName : valueList ){
					
					boolean isFound = false;
					OpComparableConditionSet vec = null;
					for( Option option : list){
						
						if( categorySet == null){
							categorySet = new HashMap<String, OpComparableConditionSet>();
						}

						vec = categorySet.get(option.getOpValue());
						
						if( opValueName.getOption().equals(option.getOpValue())){
							if( vec == null){
								vec = new OpComparableConditionSet();
							}
							vec.add(option);
							categorySet.put(option.getOpValue(), vec);
							isFound = true;
						}
						
					}
					
					if( !isFound){
						vec = new OpComparableConditionSet();
						Option option = new Option(opValueName.getCategory(),opValueName.getCategoryName(), opValueName.getOption(), opValueName.getOptionName(), "-", ".", "-", "-", "","",-1, -1);
						vec.add(option);
						categorySet.put(opValueName.getOption(), vec);
					}
					
					if( !categorySet.isEmpty()){
						packedData.put(category, categorySet);
					}
				}
				
				if( !packedData.isEmpty()){
					allData.put(trim.getTrim(), packedData);
				}
			}
			
		}
		
		ExecutorService executor = Executors.newFixedThreadPool(100);
		Vector<OpComparableConditionSet> data = new Vector();
		
		// [SR없음][20150921][jclee] 메모리 부족으로 인한 프로그램 종료 BUG Cache 적용
//		alCache = new ArrayList<String>();
		for( OpTrim trim:trimList){
			ConditionCombinationThread t = new ConditionCombinationThread(allData, data, valueList, trim.getTrim());
			executor.execute(t);
		}
		
		executor.shutdown();
		while (!executor.isTerminated()) {
		}
		
		return data;
	}
	
	private ArrayList<OpComparableConditionSet> getPackagedOption(ArrayList<OpComparableConditionSet> data, String trim) {
//		OSpec ospec = parentDlg.getOspec();
		HashMap<String, HashMap<String, HashMap<String, ArrayList<String>>>> packageMap = changedOspec.getPackageMap();
		HashMap<String, HashMap<String, ArrayList<String>>> pkgPackageMap = packageMap.get(trim);
		
		for( int i = data.size() - 1; i >= 0 ; i--){
			OpComparableConditionSet row = data.get(i);
			
//			String condition = (String)row.get(row.size() - 1);
//			
//			if( condition.equals("A50G AND E10A AND H10E AND S00P AND S03F")){
//				System.out.println("TEST");
//			}
			
//			String currentPkgName = null;
			boolean isDeleted = false;
			for( Object obj : row){
				if( obj instanceof Option){
					Option option = (Option)obj;
					
					if( option.getValue().equals("-") || option.getValue().equals("")){
						continue;
					}
					
					String packageName = option.getPackageName();
					if( !packageName.equals("") && !packageName.equals("-")){
//						String category = option.getOp();
						
						//<Category, OpValue List>
						HashMap<String, ArrayList<String>> categoryPackageMap = pkgPackageMap.get(packageName);
						if( categoryPackageMap == null){
							continue;
						}
						
						for( Object obj2 : row){
							if( obj2 instanceof Option){
								Option option2 = (Option)obj2;
								if( !categoryPackageMap.containsKey(option2.getOp())){
									continue;
								}
								
								if( option2.getValue().equals("-") || option2.getValue().equals("")){
									continue;
								}
								
								//Package가 없는 옵션은 패스.
//								String curPackage = option2.getPackageName();
//								if( curPackage.equals("") || curPackage.equals(".") || curPackage.equals("-") ){
//									continue;
//								}
								
								ArrayList<String> packageOpValues = categoryPackageMap.get(option2.getOp());
								if( !packageOpValues.contains(option2.getOpValue())){
									data.remove(i);
									isDeleted = true;
									break;
								}
							}
						}
					}
					
					if( isDeleted){
						break;
					}
				}
			}
		}
		
		return data;
	}
	
	private String getExclusiveCondition(String availableStr, String condition) throws Exception{
		String exclusiveCondition = condition;
//		ArrayList<String> foundOpValueList = new ArrayList();
		String tmpStr = availableStr;
		Pattern p = Pattern.compile("[a-zA-Z0-9]{4}");
		Matcher m = p.matcher(tmpStr);
		while (m.find()) {
			
			int idx = exclusiveCondition.indexOf(m.group());
			if( idx > -1){
				if( exclusiveCondition.indexOf(m.group()) > 8){
					exclusiveCondition = exclusiveCondition.replaceAll(" AND " + m.group(), "");
				}else{
					// 하나의 옵션만 남을 경우 AND와 같이 없어 지지 않으므로, 옵션만으로 Replace.
					exclusiveCondition = exclusiveCondition.replaceAll(m.group() + " AND ", "");
					if( exclusiveCondition.indexOf(m.group()) > -1){
						exclusiveCondition = exclusiveCondition.replaceAll(m.group(), "");
					}
				}
			}
		}
		
		return exclusiveCondition;
	}
	
	private boolean isAvailable(String reqCondition, OpComparableConditionSet conditionVec) throws Exception{
		return isAvailable(reqCondition, null, null, conditionVec);
	}
	
	private boolean isAvailable(String reqCondition, String availableStr, String type, OpComparableConditionSet conditionVec) throws Exception{
		return isAvailable(reqCondition, availableStr, type, conditionVec, null);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private boolean isAvailable(String reqCondition, String availableStr, String type, OpComparableConditionSet conditionVec, HashMap<String, ArrayList<Option>> categoryMNOMap) throws Exception{
		
		String condition = (String)conditionVec.get(conditionVec.size() - 1);
		if( reqCondition == null || reqCondition.equals("")){
			return true;
		}
		
		if( reqCondition.trim().equalsIgnoreCase("RLL") || reqCondition.trim().equalsIgnoreCase("RLR") || reqCondition.trim().equalsIgnoreCase("STS")){
			return true;
		}
		
		String tmpStr = reqCondition;
		//Pattern이 맞는 옵셥값을 찾고, 앞에 #을 붙인다.
		ArrayList<String> foundOpValueList = new ArrayList();
		Pattern p = Pattern.compile("[a-zA-Z0-9]{4}");
		Matcher m = p.matcher(tmpStr);
		while (m.find()) {
			
			if( !foundOpValueList.contains(m.group())){
				foundOpValueList.add(m.group());
			}
		}
		
		// Available에 있는 조건식이 사용자가 선택한 Option값 리스트에 포함되지 않으면 비교대상에서 제외함.
		// ==> False Return;
		ArrayList<String> list = new ArrayList<String>();
		ArrayList<OpValueName> opValueList= changedOspec.getOpNameList();
		for( OpValueName opValue : opValueList){
			list.add(opValue.getOption());
		}
		
		ArrayList<String> foundOpValueListClone = (ArrayList<String>)foundOpValueList.clone();
		for( int i = foundOpValueListClone.size() - 1; i >= 0 ; i--){
			 String opValue = foundOpValueListClone.get(i);
			 if( !list.contains(opValue)){
				 foundOpValueListClone.remove(i);
				 //현재 선택한 옵션 중에 조건식의 옵션이 포함되지 않으면, 해당 옵션은 무시하고 처리함.
				 tmpStr = tmpStr.replaceAll(opValue, "true");
			 }
		}
		
//		if( foundOpValueListClone.isEmpty()){
//			return false;
//		}
		
		for( String opValue : foundOpValueList){
			tmpStr = tmpStr.replaceAll(opValue, "#" + opValue);
		}
		
		tmpStr = tmpStr.replaceAll("AND", "&&");
		tmpStr = tmpStr.replaceAll("OR", "||");		
		for( String opValue : foundOpValueList){
			tmpStr = tmpStr.replaceAll("#" + opValue, "('##CONDITION##'.indexOf('" + opValue + "') > -1)");
		}
		
		boolean isEnable = false;
		String defaultStr = tmpStr;
		String[] subConditions = condition.split("OR");
		for( String subCondition : subConditions){
			defaultStr = tmpStr;
			defaultStr = defaultStr.replaceAll("##CONDITION##", subCondition);
			
			Object obj = parentDlg.getEngine().eval(defaultStr);
			if( obj instanceof Boolean){
				Boolean b = (Boolean)obj;
				isEnable = b.booleanValue();
				if( isEnable){
					return true;
				}
				
			}else{
				throw new Exception("Not available Option : " + reqCondition);
			}
		}
		
		return false;
	}
	
	/**
	 * 사용자가 선택한 옵션들 중에 reqCondtion에 해당하는 값이 전혀 없는지 확인.
	 * 
	 * @param reqCondition
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private boolean isIgnored(String reqCondition){
		
		if( reqCondition == null || reqCondition.equals("")){
			return false;
		}
		
		ArrayList<String> foundOpValueList = new ArrayList();
		Pattern p = Pattern.compile("[a-zA-Z0-9]{4}");
		Matcher m = p.matcher(reqCondition);
		while (m.find()) {
			
			if( !foundOpValueList.contains(m.group())){
				foundOpValueList.add(m.group());
			}
		}
		
		// Available에 있는 조건식이 사용자가 선택한 Option값 리스트에 포함되지 않으면 비교대상에서 제외함.
		// ==> False Return;
		ArrayList<String> list = new ArrayList<String>();
		ArrayList<OpValueName> opValueList= changedOspec.getOpNameList();
		for( OpValueName opValue : opValueList){
			list.add(opValue.getOption());
		}
		
//		ArrayList<String> foundOpValueListClone = (ArrayList<String>)foundOpValueList.clone();
		for( int i = foundOpValueList.size() - 1; i >= 0 ; i--){
			 String opValue = foundOpValueList.get(i);
			 if( !list.contains(opValue)){
				 foundOpValueList.remove(i);
				 //현재 선택한 옵션 중에 조건식의 옵션이 포함되지 않으면, 해당 옵션은 무시하고 처리함.
			 }
		}
		
		if( foundOpValueList.isEmpty()){
			return true;
		}
		
		return false;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private boolean isInclude(String reqCondition, OpComparableConditionSet conditionVec){
		String condition = (String)conditionVec.get(conditionVec.size() - 1);
		if( reqCondition == null || reqCondition.equals("")){
			return true;
		}
		
		if( reqCondition.trim().equalsIgnoreCase("RLL") || reqCondition.trim().equalsIgnoreCase("RLR") || reqCondition.trim().equalsIgnoreCase("STS")){
			return true;
		}
		
		String tmpStr = reqCondition;
		//Pattern이 맞는 옵셥값을 찾고, 앞에 #을 붙인다.
		ArrayList<String> foundOpValueList = new ArrayList();
		Pattern p = Pattern.compile("[a-zA-Z0-9]{4}");
		Matcher m = p.matcher(tmpStr);
		while (m.find()) {
			
			if( !foundOpValueList.contains(m.group())){
				foundOpValueList.add(m.group());
			}
		}
		
		// Available에 있는 조건식이 사용자가 선택한 Option값 리스트에 포함되지 않으면 비교대상에서 제외함.
		// ==> False Return;
		ArrayList<String> list = new ArrayList<String>();
		ArrayList<OpValueName> opValueList= changedOspec.getOpNameList();
		for( OpValueName opValue : opValueList){
			list.add(opValue.getOption());
		}
		
		ArrayList<String> foundOpValueListClone = (ArrayList<String>)foundOpValueList.clone();
		for( int i = foundOpValueListClone.size() - 1; i >= 0 ; i--){
			 String opValue = foundOpValueListClone.get(i);
			 if( !list.contains(opValue)){
				 foundOpValueListClone.remove(i);
				 // 현재 선택한 옵션 중에 조건식의 옵션이 포함되지 않으면, 해당 옵션은 무시하고 처리함.
				 // [SR없음][20150724][jclee] 단, AND로 묶인 Option의 경우 해당 옵션을 무시하지 않음
//				 if (!(tmpStr.contains(opValue + " AND") || tmpStr.contains("AND " + opValue))) {
//					 tmpStr = tmpStr.replaceAll(opValue, "true");
//				 }
				 tmpStr = tmpStr.replaceAll(opValue, "true");
			 }
		}
		
		for( String opValue : foundOpValueList){
			tmpStr = tmpStr.replaceAll(opValue, "#" + opValue);
		}
		
		tmpStr = tmpStr.replaceAll("AND", "&&");
		tmpStr = tmpStr.replaceAll("OR", "||");		
		for( String opValue : foundOpValueList){
			tmpStr = tmpStr.replaceAll("#" + opValue, "('##CONDITION##'.indexOf('" + opValue + "') > -1)");
		}
		
		boolean isEnable = false;
		String defaultStr = tmpStr;
		String[] subConditions = condition.split("OR");
		for( String subCondition : subConditions){
			defaultStr = tmpStr;
			defaultStr = defaultStr.replaceAll("##CONDITION##", subCondition);
			
			Object obj;
			try {
				obj = parentDlg.getEngine().eval(defaultStr);
				if( obj instanceof Boolean){
					Boolean b = (Boolean)obj;
					isEnable = b.booleanValue();
					break;
				}else{
					throw new Exception("Not available Option : " + reqCondition);
				}
			} catch (Exception e) {
				return false;
			}
			
		}
		
		return isEnable;
	}
	
	private boolean isAvailable2(OpComparableConditionSet conditionVec, HashMap<String, ArrayList<Option>> categoryMNOMap){
		String condition = (String)conditionVec.get(conditionVec.size() - 1);

//		if( condition.equals("E102 AND H10E AND S00P AND S03F")){
//			System.out.println("TEST");
//		}
		
		for( int i = 0; i < conditionVec.size(); i++){
			Object obj = conditionVec.get(i);
			if( obj instanceof Option){
				Option option = (Option)obj;
				if( option.getValue().equals("") || option.getValue().equals("-") || option.getValue().equals(".")){
					continue;
				}
				
				// [jclee] 국가코드 중복 시 Row 제거, 송대영CJ 요청
				boolean isContainSpecialCountry = false;
				if (option.getOpName().indexOf("SPECIAL COUNTRY") > -1) {
					for (int inx = 0; inx < conditionVec.size(); inx++) {
						Object obj2 = conditionVec.get(inx);
						if (obj2 instanceof Option) {
							Option option2 = (Option) obj2;
							if (!option2.getValue().equals("-") && 
								option2.getOpValueName().indexOf("NON") == -1 && 
								option2.getOpName().indexOf("SPECIAL COUNTRY") > -1 && 
								!option.getOp().equals(option2.getOp()) &&
								option.getOpValueName().indexOf("NON") == -1) {
								isContainSpecialCountry = true;
								break;
							}
						}
					}
				}
				
				if (isContainSpecialCountry) {
					return false;
				}
				
				String availableC1 = option.getAvailableCondition();
				String notAvailableC2 = option.getNotAvailableCondition();
				String reqCondition2 = null;
				
//				if (availableC1.equals("(H10S OR H10E)")) {
//					System.out.println("TEST");
//				}
				
				boolean isAvailableCondition = false;
				if( availableC1 != null && !availableC1.equals("")){
					isAvailableCondition = true;
					reqCondition2 = availableC1;
				}else{
					isAvailableCondition = false;
					reqCondition2 = notAvailableC2;
				}
				
				/**
				 * [SR없음][2015.06.19][jclee] 자신의 Mandatory Condition이 아님에도 불구하고 Logic이 수행되는 경우가 있어 수정
				 */
				if (isIgnored(reqCondition2)) {
					reqCondition2 = "";
				}
				
				// 다른 옵션값에 의해 삭제되어 질 옵션 조합인지 결정.
				if( reqCondition2 == null || reqCondition2.equals("")){
					ArrayList<Option> opList = categoryMNOMap.get(option.getOp());
					if( opList != null){
						for( Option tOption : opList){
							
							String reqCondition = null;
							String availableCondition = tOption.getAvailableCondition();
							String notAvailableCondition = tOption.getNotAvailableCondition();
							if( availableCondition != null && !availableCondition.equals("")){
								reqCondition = availableCondition;
							}else{
								reqCondition = notAvailableCondition;
							}
							
							//사용자가 선택한 옵션 리스트에 reqCondition 이 하나도 존재하지 않으면 False;
							if( !reqCondition.equals("") && isIgnored(reqCondition)){
								continue;
							}
							
							if( isInclude(reqCondition, conditionVec)){
								if( condition.indexOf( tOption.getOpValue() ) < 0 ){
									if (tOption.getValue().startsWith("M")) {
										return false;
									}
								}
							}
						}
					}
				} else {
					if( isIgnored(reqCondition2)){
						if (isAvailableCondition) {
							if (option.getValue().startsWith("O")) {
//								return true;
								continue;
							}
							return false;
						} else {
							return false;
						}
					}else{
						boolean isInclude = isInclude(reqCondition2, conditionVec);
						if (isAvailableCondition) {
							// False는 바로 Return.
							// True는 다음 Validation Check 수행
//							return isInclude;
							if (isInclude) {
								continue;
							} else {
								return false;
							}
						} else {
//							return !isInclude;
							if (isInclude) {
								return false;
							} else {
								continue;
							}
						}
					}
				}
				
			}
		}
		
		return true;
	}
	
	private boolean isContained(Vector<OpComparableConditionSet> data, OpComparableConditionSet opSet){
		
		boolean result = false;
		for( OpComparableConditionSet t : data){
			if( isEquals(t, opSet)){
				return true;
			}
		}
		
		return result;
	}
	
	/**
	 * 완전한 행(조건식까지 포함한)인  두개의 OpComparableConditionSet를 비교시에는 조건식만으로 판단함.
	 * 
	 * @param source
	 * @param target
	 * @return
	 */
	private boolean isEquals(OpComparableConditionSet source, OpComparableConditionSet target){
		return source.get(source.size()-1).equals(target.get(target.size()-1));
	}
	
	/**
	 * 카테고리 별로 분류된 옵션값들을 파라미터로 받으며, 결과값은 아래와 같다.
	 * 	 		- O - O 일 경우
	 *	   ==>  - O - -
	 *			- - - O 
	 *			- - - - 
	 *        로 표기되어야 함.
	 * 
	 * @param source
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private ArrayList<OpComparableConditionSet> getExplosion(OpComparableConditionSet source){
		
		OpComparableConditionSet defaultRow = new OpComparableConditionSet();
		ArrayList<OpComparableConditionSet> data = new ArrayList<OpComparableConditionSet>();
		
		boolean[] hasOp = new boolean[source.size()];
		for( int i = 0; i < source.size(); i++){
			Object obj = source.get(i);
			Option option = (Option)obj;
			String value = option.getValue();
			if( !value.equals("-") && !value.equals("")){
				hasOp[i] = true;
			}
			
			Option defaultOption = (Option)option.clone();
			defaultOption.setValue("-");
			defaultRow.add(defaultOption);
		}
		
		//data.add(defaultRow);
		
		// 		- O - O 일 경우
		// ==>  - O - -
		//		- - - O 
		//      로 표기되어야 함.
		OpComparableConditionSet newRow = null;
		for( int i = 0; i < source.size(); i++){
			Object obj = source.get(i);
			Option option = (Option)obj;
			if( hasOp[i]){
				newRow = (OpComparableConditionSet)defaultRow.clone();
				newRow.set(i, option);
				data.add(newRow);
			}
		}
		return data;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void addCondition(Vector conditionVec){
		DefaultTableModel model = (DefaultTableModel)conditionTable.getModel();
		
		String str = conditionVec.toString();
		for( int i = 0; i < model.getRowCount(); i++){
			Vector vec = (Vector)model.getValueAt(i, 0);
			if( str.equals(vec.toString())){
				return;
			}
		}
		OpComparableConditionSet row = new OpComparableConditionSet();
		row.add(conditionVec);
		model.addRow(row);
		
		//수정 getDataVector() 메소드는 Vector<Vector> 타입을 반환. 하지만 Collections.sort() 메소드는 인자로 2차원 배열이나 Vector를 사용하지 않음.
		Vector sortedVector = new Vector();
		Vector<Vector> rawData = model.getDataVector();
		for(Vector vector : rawData) {
			sortedVector.add(vector);
		}
		Collections.sort(sortedVector);
//		Collections.sort(model.getDataVector());
	}
	
	public void removeCondition(){
		int idx = conditionTable.getSelectedRow();
		if( idx < 0) 
			return;
		int modelIdx = conditionTable.convertRowIndexToModel(idx);
		DefaultTableModel model = (DefaultTableModel)conditionTable.getModel();
		model.removeRow(modelIdx);
	}

	public JPanel getDataPanel() {
		return dataPanel;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ArrayList<String> getOptionCombinations(String selectedTitle){
		
		ArrayList<String> result = new ArrayList();
		DefaultTableModel model = null;
		String title = null;
		
		if( selectedTitle != null){
			title = selectedTitle;
		}else{
			TitledBorder border = (TitledBorder) dataPanel.getBorder();
			title = border.getTitle();
		}
		
		if( title.equals(OSpecMainDlg.TOTAL_DATA)){
			model = (DefaultTableModel)allDataTable.getModel();
		}else if(title.equals(OSpecMainDlg.FILTERED_DATA)){
			model = (DefaultTableModel)filteredDataTable.getModel();
		}else if(title.equals(OSpecMainDlg.CONDITIONAL_DATA)){
			model = (DefaultTableModel)conditionDataTable.getModel();
		}
		
		Vector<Vector> data = model.getDataVector();
		for( Vector row : data){
			String condition = (String)row.get(row.size()-1);
			if( !result.contains(condition)){
				result.add(condition);
			}
		}
		
		return result;
	}
	
	private void exportToExcel(String type){
		
		String preStr = "";
		if( type.equals(EXPORT_FOR_DRAWING)){
			preStr = "Option_Combination_Drawing_ ";
		}else if( type.equals(EXPORT_FOR_CONDITION)){
			preStr = "Option_Combination_";
		}
		
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY );
		Calendar now = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddkkmmss");
		sdf.format(now.getTime());
		File defaultFile = new File(preStr + sdf.format(now.getTime()) + ".xls");
		fileChooser.setSelectedFile(defaultFile);
//		fileChooser.addChoosableFileFilter(new OptionDefinitionFileFilter("MSEXCEL"));
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
		int result = fileChooser.showSaveDialog(PublishPanel.this.parentDlg);
		if( result == JFileChooser.APPROVE_OPTION){
			File selectedFile = fileChooser.getSelectedFile();
			try
            {
				if( type.equals(EXPORT_FOR_DRAWING)){
					exportForDrawing(selectedFile);
				}else if( type.equals(EXPORT_FOR_CONDITION)){
					exportForCondition(selectedFile);
				}
				AIFShell aif = new AIFShell("application/vnd.ms-excel", selectedFile.getAbsolutePath());
				aif.start();
            }
            catch (Exception e)
            {
            	e.printStackTrace();
            	MessageBox.post(PublishPanel.this.parentDlg, e.getMessage(), "ERROR", MessageBox.ERROR);
            }
		}
		
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void exportForDrawing(File selectedFile) throws IOException, WriteException{
		WritableWorkbook workBook = Workbook.createWorkbook(selectedFile);
	    // 0번째 Sheet 생성
	    WritableSheet sheet = workBook.createSheet("new sheet", 0);
	    
	    WritableCellFormat cellFormat = new WritableCellFormat(); // 셀의 스타일을 지정하기 위한 부분입니다.
	    cellFormat.setBorder(Border.ALL, BorderLineStyle.THIN); // 셀의 스타일을 지정합니다. 테두리에 라인그리는거에요
	    cellFormat.setAlignment(Alignment.CENTRE);
	    cellFormat.setVerticalAlignment(VerticalAlignment.CENTRE);
	    
	    WritableCellFormat eplFormat = new WritableCellFormat(); // 셀의 스타일을 지정하기 위한 부분입니다.
	    eplFormat.setBorder(Border.ALL, BorderLineStyle.THIN); // 셀의 스타일을 지정합니다. 테두리에 라인그리는거에요
	    eplFormat.setWrap(true);
	    eplFormat.setVerticalAlignment(VerticalAlignment.CENTRE);
	    
	    Label label = null;
	    
	    WritableCellFormat headerCellFormat = new WritableCellFormat(); // 셀의 스타일을 지정하기 위한 부분입니다.
	    headerCellFormat.setBorder(Border.ALL, BorderLineStyle.THIN);
	    headerCellFormat.setBackground(Colour.GREY_25_PERCENT);

	    int startRow = 0;
	    int initColumnNum = 0;

	    ArrayList<OpValueName> opNameList = changedOspec.getOpNameList();
	    Vector excelColumnHeader = new Vector();
	    
	    for( OpValueName opValue : opNameList){
	    	excelColumnHeader.add(opValue.getOption());
	    }
	    excelColumnHeader.add("EPL");
	    
	    for (int i = 0; i < excelColumnHeader.size(); i++)
	    {
	      label = new jxl.write.Label(i + initColumnNum, startRow, excelColumnHeader.get(i).toString(), headerCellFormat);
	      sheet.addCell(label);
	      CellView cv = sheet.getColumnView(i + initColumnNum);
	      if( i == excelColumnHeader.size() - 1){
	    	  cv.setSize(15000);  
	      }else{
	    	  cv.setSize(1500);
	      }
//	      cv.setAutosize(true);
	      sheet.setColumnView(i + initColumnNum, cv);
	    }

	    int rowNum = 0;
	    startRow = 1;
	    
	    String opVal = null;
	    DefaultTableModel model = null;
	    TitledBorder border = (TitledBorder) dataPanel.getBorder();
		String title = border.getTitle();
		if( title.equals(OSpecMainDlg.TOTAL_DATA)){
			model = (DefaultTableModel)allDataTable.getModel();
		}else if(title.equals(OSpecMainDlg.FILTERED_DATA)){
			model = (DefaultTableModel)filteredDataTable.getModel();
		}else if(title.equals(OSpecMainDlg.CONDITIONAL_DATA)){
			model = (DefaultTableModel)conditionDataTable.getModel();
		}
		
		WritableCellFormat format = null;
	    Vector<Vector> data = model.getDataVector();
	    for (int i = 0; i < data.size(); i++)
	    {
	    	Vector row = data.get(i);
	    	for (int j = 0; j < row.size(); j++)
	    	{
	    		opVal = "-";
	    		Object obj =  row.get(j);
	    		if( obj instanceof Option){
	    			Option option = (Option)obj;
	    			String value = option.getValue();
	    			if( !value.equals("") && !value.equals("-")){
	    				opVal = "S";
	    			}
	    			format = cellFormat;
	    		}else{
		    		String condition = (String)row.get(j);
		    		opVal = condition.replaceAll(" OR ", "\012");
		    		format = eplFormat;
	    		}
	    		label = new jxl.write.Label(j + initColumnNum, (rowNum) + startRow, opVal, format);
	    		sheet.addCell(label);
	    	}
	    	rowNum++;
	    }

	    //셀 Merge
	    initColumnNum = opNameList.size();
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
	    			cf.setWrap(true);
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
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void exportForCondition(File selectedFile) throws IOException, WriteException{
		WritableWorkbook workBook = Workbook.createWorkbook(selectedFile);
	    // 0번째 Sheet 생성
	    WritableSheet sheet = workBook.createSheet("new sheet", 0);
	    
	    WritableCellFormat cellFormat = new WritableCellFormat(); // 셀의 스타일을 지정하기 위한 부분입니다.
	    cellFormat.setBorder(Border.ALL, BorderLineStyle.THIN); // 셀의 스타일을 지정합니다. 테두리에 라인그리는거에요
	    cellFormat.setWrap(true);
	    Label label = null;
	    
	    WritableCellFormat dataHeaderCellFormat = new WritableCellFormat(); // 셀의 스타일을 지정하기 위한 부분입니다.
	    dataHeaderCellFormat.setBorder(Border.ALL, BorderLineStyle.THIN);
//	    CustomColour colour = new CustomColour(60000, "light Blue", 0, 176, 240);
	    
	    dataHeaderCellFormat.setBackground(Colour.BRIGHT_GREEN);
	    
	    WritableCellFormat sampleHeaderCellFormat = new WritableCellFormat(); // 셀의 스타일을 지정하기 위한 부분입니다.
	    sampleHeaderCellFormat.setBorder(Border.ALL, BorderLineStyle.THIN);
	    sampleHeaderCellFormat.setBackground(Colour.GREY_25_PERCENT);

	    int startRow = 0;
	    int initColumnNum = 1;

	    label = new jxl.write.Label(1, startRow, "<Sample>", cellFormat);
	    sheet.addCell(label);
//	    sheet.mergeCells(0, 1, 2, 1);
	    
	    Vector excelColumnHeader = new Vector();
	    excelColumnHeader.add("FMPNO");
	    excelColumnHeader.add("PARTNO");
	    excelColumnHeader.add("PARTQTY");
	    excelColumnHeader.add("OPTION_RESULT");
	    
	    // SAMPLE Start
	    startRow = 1;
	    int[] width = {4000, 5000, 3000, 100000};
	    for (int i = 0; i < excelColumnHeader.size(); i++)
	    {
	      label = new jxl.write.Label(i + initColumnNum, startRow, excelColumnHeader.get(i).toString(), sampleHeaderCellFormat);
	      sheet.addCell(label);
	      CellView cv = sheet.getColumnView(i + initColumnNum);
//	      cv.setAutosize(true);
	      cv.setSize(width[i]);
	      sheet.setColumnView(i + initColumnNum, cv);
	    }
	    
	    startRow = 2;
	    for (int i = 0; i < excelColumnHeader.size(); i++)
	    {
	    	String tmpStr = null;
	    	switch(i){
	    	case 0:
	    		tmpStr = "M000XASY12";
	    		break;
	    	case 1:
	    		tmpStr = "XXXXXXXXXX";
	    		break;
	    	case 2:
	    		tmpStr = "1";
	    		break;
	    	case 3:
	    		tmpStr = "A01A AND BOOL\012A01A AND BOOR";
	    		break;
	    	}
	      label = new jxl.write.Label(i + initColumnNum, startRow, tmpStr, cellFormat);
	      sheet.addCell(label);
	    }
	      
	    //SAMPLE End
	    
	    startRow = 4;
	    for (int i = 0; i < excelColumnHeader.size(); i++)
	    {
	      label = new jxl.write.Label(i + initColumnNum, startRow, excelColumnHeader.get(i).toString(), dataHeaderCellFormat);
	      sheet.addCell(label);
//	      CellView cv = sheet.getColumnView(i + initColumnNum);
//	      cv.setAutosize(true);
//	      sheet.setColumnView(i + initColumnNum, cv);
	    }

	    int rowNum = 0;
	    startRow = 5;
	    
	    ArrayList<String> list =  getOptionCombinations(null);
	    Vector<Vector> data = new Vector();
	    for( String condition : list){
	    	Vector row = new Vector();
	    	row.add("");
	    	row.add("");
	    	row.add("");
	    	row.add(condition);
	    	data.add(row);
	    }
//	    Vector<Vector> data = getData();
	    for (int i = 0; i < data.size(); i++)
	    {
	    	Vector row = data.get(i);
	    	for (int j = 0; j < row.size(); j++)
	    	{
	    		String condition = (String)row.get(j);
	    		condition = condition.replaceAll(" OR ", "\012");
	    		label = new jxl.write.Label(j + initColumnNum, (rowNum) + startRow, condition, cellFormat);
	    		sheet.addCell(label);
	    	}
	    	rowNum++;
	    }

	    //셀 Merge
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
	
	class CustomColour extends Colour{

		protected CustomColour(int val, String s, int r, int g, int b) {
			super(val, s, r, g, b);
		}
		
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	class ConditionCombinationThread extends Thread{
		
		private HashMap<String, HashMap<String, HashMap<String, OpComparableConditionSet>>> allData = null;
		private Vector<OpComparableConditionSet> data = null;
		private ArrayList<OpValueName> valueList = null;
		private String trimName = null;
		private ArrayList<String> cacheList = new ArrayList();
		
		public ConditionCombinationThread(HashMap<String, HashMap<String, HashMap<String, OpComparableConditionSet>>> allData
				, Vector<OpComparableConditionSet> data, ArrayList<OpValueName> valueList, String trimName){
			this.allData = allData;
			this.data = data;
			this.valueList = valueList;
			this.trimName = trimName;
		}
		
		/**
		 * Row를 Category 별로 분리하고, 각 Category는 하나의 옵션값만 표기되도록 경우를 수를 푼다.
		 * 각 Category별 경우의 수를 곱연산하여 전체적인 경우의 수를 펼침.
		 * 
		 * @param data
		 * @return
		 */
		private ArrayList<OpComparableConditionSet> getEnableConditionRow(ArrayList<OpComparableConditionSet> data) {

			if( data == null)
				return null;
			
			ArrayList<OpComparableConditionSet> newData = null;
			ArrayList<OpComparableConditionSet> resultData = new ArrayList<OpComparableConditionSet>();
			String category = null;
			StringBuffer sb = new StringBuffer();
			OpComparableConditionSet categoryOp = new OpComparableConditionSet();
			
			newData = new ArrayList<OpComparableConditionSet>();
			OpComparableConditionSet trimOp = new OpComparableConditionSet();
			
			int preSize = 0;
			ArrayList<String> tmpCacheList = new ArrayList();
			
			for (int j = data.size() - 1; j >= 0; j--) {
				
				OpComparableConditionSet row = data.get(j);
				
				//Category 순서로 정렬.
				Collections.sort(row, new Comparator() {

					@Override
					public int compare(Object o1, Object o2) {
						if( (o1 instanceof String) && (o2 instanceof String) ){
							String s1 = (String)o1;
							String s2 = (String)o2;
							return s1.compareTo(s2);
						}else if( (o1 instanceof String) && !(o2 instanceof String)){
							return -1;
						}else if( !(o1 instanceof String) && (o2 instanceof String)){
							return 1;
						}else if((o1 instanceof Option) && (o2 instanceof Option) ){
							Option p1 = (Option)o1;
							Option p2 = (Option)o2;
							String c1 = p1.getOp();
							String c2 = p2.getOp();
							int cResult = c1.compareTo(c2);
							if( cResult == 0){
								return p1.getOpValue().compareTo(p2.getOpValue());
							}else{
								return cResult;
							}
						}else{
							return o1.toString().compareTo(o2.toString());
						}
						
					}
				});
				
				categoryOp.clear();
				newData.clear();
				trimOp.clear();
				
				
				
				for( int i = 0; i < row.size(); i++){
					Object obj = row.get(i);
					if( i == 0){
						trimOp.add(obj);
						newData.add(trimOp);
						continue;
					}
					
					if( obj instanceof Option){
						Option option = (Option)obj;
						
						if( category == null){
							category = option.getOp();
							categoryOp.clear();
							categoryOp.add(option);
							continue;
						}
						
						if( !option.getOp().equals(category)){
							if( categoryOp != null && !categoryOp.isEmpty()){
								newData = getCaseOption(newData, getExplosion(categoryOp), true);
								categoryOp.clear();
							}
						}
						
						categoryOp.add(option);
						category = option.getOp();
					}
				}
				
//				duplicateCheck(newData, sb, tmpCacheList);
				if( categoryOp != null && !categoryOp.isEmpty()){
					newData = getCaseOption(newData, getExplosion(categoryOp), true);
//					newData = getCaseOption(newData, getExplosion(categoryOp), true, tmpCacheList, sb);
				}
				
				//Duplication Check
				if( newData == null){
					continue;
				}
				
//				if( (resultData.size() - preSize) > 5000){
//					duplicateCheck(resultData, sb, tmpCacheList);
//					preSize = resultData.size();
//				}
//				resultData.addAll(newData);
				
				if( (resultData.size() - preSize) > 500000){
//					duplicateCheck(resultData, sb, tmpCacheList);
					removeDuplicate(resultData, tmpCacheList);
					preSize = resultData.size();
				}
				
				resultData.addAll(newData);
				
				// 중복체크
			}
			
//			SYMCRemoteUtil remoteQuery = new SYMCRemoteUtil();
//			 
//			for (int inx = 0; inx < resultData.size(); inx++) {
//				OpComparableConditionSet occs = (OpComparableConditionSet) resultData.get(inx);
//				String str = "";
//				
//				for (int jnx = 0; jnx < occs.size(); jnx++) {
//					Object obj = occs.get(jnx);
//					
//					if (obj instanceof Option) {
//						Option option = (Option) obj;
//						
//						str = str + "[OpValue:" + option.getOpValue() + "]";
//						str = str + "[PackageName:" + option.getPackageName() + "]";
//						str = str + "[DriveType:" + option.getDriveType() + "]";
//						str = str + "[Value:" + option.getValue() + "]";
//					} else {
//						str = obj.toString();
//					}
//				}
//				
//				try {
//					DataSet ds = new DataSet();
//					ds.put("COL1", str);
//					
//					remoteQuery.execute("com.ssangyong.service.SMTestService", "insertTest", ds);
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
			
			return resultData;
		}
		
		private void duplicateCheck(ArrayList<OpComparableConditionSet> newData, StringBuffer sb, ArrayList<String> cacheList){
			for( int i = newData.size() - 1; i >= 0; i--){
				OpComparableConditionSet conditionSet2 = newData.get(i);
				sb.delete(0, sb.length());
				
				//Trim 추가.
				sb.append((String)conditionSet2.get(0));
				for( Object obj2 : conditionSet2){
					if( obj2 instanceof Option){
						Option option = (Option)obj2;
						if( sb.length() == 0){
							sb.append("[" + (option.getValue().equals("-") ? "-":option.getPackageName()) + "]" + option.getValue());
//							sb.append(option.getValue());
						}else{
							sb.append(" AND " + "[" + (option.getValue().equals("-") ? "-":option.getPackageName()) + "]" + option.getValue());
//							sb.append(" AND " + option.getValue());
						}
					}
				}
				
				String str = sb.toString();
				if( !cacheList.contains(str)){
					cacheList.add(str);
				}else{
					newData.remove(i);
				}
			}
		}		
		
		/**
		 * 중복제거
		 * @param resultData
		 */
		private void removeDuplicate(ArrayList<OpComparableConditionSet> resultData, ArrayList<String> tmpCacheList) {
//			ArrayList<String> alTemp = new ArrayList<String>();
			
			for (int inx = resultData.size() - 1; inx >= 0; inx--) {
				OpComparableConditionSet occs = (OpComparableConditionSet) resultData.get(inx);
				String str = "";
				
				for (int jnx = 0; jnx < occs.size(); jnx++) {
					Object obj = occs.get(jnx);
					
					if (obj instanceof Option) {
						Option option = (Option) obj;
						
						str = str + "[" + option.getOpValue() + "]";		// OpValue
						str = str + "[" + option.getPackageName() + "]";	// PackageName
						str = str + "[" + option.getDriveType() + "]";		// DriveType
						str = str + "[" + option.getValue() + "]";			// Value
					} else {
						str = obj.toString();
					}
				}
				
//				if (alCache.contains(str)) {
//					resultData.remove(inx);
//				} else {
//					alCache.add(str);
//				}
				
				boolean isHit = false;
				for (int jnx = tmpCacheList.size() - 1; jnx >= 0 ; jnx--) {
//					String sTemp = alCache.get(jnx);
					String sTemp = tmpCacheList.get(jnx);
					
					if (sTemp == null || sTemp.equals("") || sTemp.length() == 0) {
						continue;
					}
					
					if (sTemp.equals(str)) {
						resultData.remove(inx);
						isHit = true;
						break;
					}
				}
				
				if (!isHit) {
//					alCache.add(str);
					tmpCacheList.add(str);
				}
				
				// 10만건 이상이 될 경우 오히려 Cache가 메모리를 많이 먹을 수 있으므로 어느정도 중복허용을 감수하고 값 Clear
//				if (alCache.size() > 100000) {
//					alCache = new ArrayList<String>();
//				}
				if (tmpCacheList.size() > 100000) {
					tmpCacheList = new ArrayList<String>();
				}
			}
		}
		
		private ArrayList<OpComparableConditionSet> getCaseOption(ArrayList<OpComparableConditionSet> vec1, ArrayList<OpComparableConditionSet> vec2){
			return getCaseOption(vec1, vec2, false);
		}
		
		private boolean isContain(OpComparableConditionSet conditionSet, ArrayList<String> cacheList){
			
			StringBuffer sb = new StringBuffer();
			
			sb.append((String)conditionSet.get(0));
			for( Object obj2 : conditionSet){
				if( obj2 instanceof Option){
					Option option = (Option)obj2;
					if( sb.length() == 0){
						sb.append("[" + (option.getValue().equals("-") ? "-":option.getPackageName()) + "]" + option.getValue());
					}else{
						sb.append(" AND " + "[" + (option.getValue().equals("-") ? "-":option.getPackageName()) + "]" + option.getValue());
					}
				}
			}
			
			String str = sb.toString();
			
			if( cacheList.contains(str)){
				return true;
			}else{
				cacheList.add(str);
			}
			
			return false;
		}
		
		private ArrayList<OpComparableConditionSet> getCaseOption(ArrayList<OpComparableConditionSet> vec1, ArrayList<OpComparableConditionSet> vec2, boolean bFlag){
			
			ArrayList<String> cacheList = new ArrayList();
			ArrayList<OpComparableConditionSet> data = new ArrayList();
			for( Vector obj1 : vec1){
				for( Vector obj2 : vec2){
					if ( bFlag){
						OpComparableConditionSet row = new OpComparableConditionSet();
						row.addAll(obj1);
						row.addAll(obj2);
						
						if( !isContain(row, cacheList)){
							data.add(row);
						}
					}else{
						for( Object obj : obj2){
							OpComparableConditionSet row = new OpComparableConditionSet();
							row.addAll(obj1);
							row.add(obj);
							if( !isContain(row, cacheList)){
								data.add(row);
							}
						}
					}
					
//					System.gc();
				}
			}
			
			return data;
		}		
		
		public void run(){
			HashMap<String, HashMap<String, OpComparableConditionSet>> packedData = allData.get(trimName);
			if( packedData == null){
				return;
			}
			List<String> keyList = (List<String>)Arrays.asList( packedData.keySet().toArray(new String[packedData.size()]));
			
			//Category Sorting.
			Collections.sort(keyList);
			
//			String preCategory = null;
			ArrayList<OpComparableConditionSet> preData = new ArrayList();
			OpComparableConditionSet trimVec = new OpComparableConditionSet();
			
			HashMap<String, OpComparableConditionSet> categorySet = null;
			OpComparableConditionSet opVec = null;
			
			trimVec.add(trimName);
			preData.add(trimVec);
			
			for( OpValueName opValueName : valueList ){
				
				String category = OpUtil.getCategory(opValueName.getOption());
				
				ArrayList<OpComparableConditionSet> sVec = new ArrayList();
				categorySet = packedData.get(category);
				if( categorySet == null){
					OpComparableConditionSet tmpRow = new OpComparableConditionSet();
					Option option = new Option(opValueName.getCategory(),opValueName.getCategoryName(), opValueName.getOption(), opValueName.getOptionName(), "-", ".", "-", "-", "","",-1, -1);
					tmpRow.add(option);
					sVec.add(tmpRow);
				}else{
					
					opVec = categorySet.get(opValueName.getOption());
					if( opVec == null){
						OpComparableConditionSet tmpRow = new OpComparableConditionSet();
						Option option = new Option(opValueName.getCategory(),opValueName.getCategoryName(), opValueName.getOption(), opValueName.getOptionName(), "-", ".", "-", "-", "","",-1, -1);
						tmpRow.add(option);
						sVec.add(tmpRow);
					}else{
						sVec.add(opVec);
					}
				}
				preData = getCaseOption(preData, sVec);
//				preCategory = category;
			}
			
			//각각의 경우의 수로 펼친다.
			preData = getEnableConditionRow(preData);
			
			StringBuffer sb = new StringBuffer();
			duplicateCheck(preData, sb, cacheList);
			
//			if( trimName.equals("G5AWD")){
//			if( trimName.startsWith("G")){
//				System.out.println("TEST");
//			}
			
			//조건식 추가
			preData = getAvailableData(preData);
			
//			for (int i = 0; i < preData.size(); i++) {
//				OpComparableConditionSet row = preData.get(i);
//				String condition = (String)row.get(row.size() - 1);
//				System.out.println(condition);
//			}
			
			//옵션에 B00L 또는 B00R이 포함된 경우 Drive Type에 따라 조건을 분기함.
			preData = getDriveTypeData(preData, trimName);
			
			//Package로 묶인 경우에 대해, 유효하지 않는 Row제거.
			preData = getPackagedOption(preData, trimName);			
			
			data.addAll(preData);
			cacheList.clear();
			
			System.gc();
		}
	}
	
	private ArrayList<OpComparableConditionSet> getDriveTypeData(ArrayList<OpComparableConditionSet> data, String trim){
		
//		HashMap<String, HashMap<String, HashMap<String, ArrayList<String>>>> driveMap = changedOspec.getDriveTypeMap();
//		HashMap<String, HashMap<String, ArrayList<String>>> pkgDriveMap = driveMap.get(trim);
		for( int i = data.size() - 1; i >= 0 ; i--){
			OpComparableConditionSet row = data.get(i);
//			String conStr = (String)row.get(row.size() - 1);
//			if (conStr.equals("B00R AND C03C AND E00L")) {
//				System.out.println("TEST");
//			}
			boolean isDeleted = false;
			for( Object obj : row){
				if( obj instanceof Option){
					Option option = (Option)obj;
					
					if( option.getValue().equals("-") || option.getValue().equals(".") || option.getValue().equals("")){
						continue;
					}
					
					//Drive Type이 존재하면, 해당 Type외의 다른 Type일 경우 삭제.
					String driveType = option.getDriveType();
					if( !driveType.equals("") && !driveType.equals("-") && !driveType.equals(".")){
//						String category = option.getOp();
						
						//<Category, OpValue List>
//						HashMap<String, ArrayList<String>> categoryDriveMap = pkgDriveMap.get(driveType);
//						if( categoryDriveMap == null){
//							continue;
//						}
						
						for( Object obj2 : row){
							if( obj2 instanceof Option){
								Option option2 = (Option)obj2;
//								if( !categoryDriveMap.containsKey(option2.getOp())){
//									continue;
//								}
								
								if( option2.getValue().equals("-") || option2.getValue().equals("")){
									continue;
								}
								
//								if (option2.getValue().equals("M28")) {
//									System.out.println("TEST");
//								}
								//Drive Type이 없는 옵션은 패스.
								String curDirveType = option2.getDriveType();
								if( curDirveType.equals("") || curDirveType.equals(".") || curDirveType.equals("-") ){
									continue;
								}
								
								if( !driveType.equals(curDirveType)){
									data.remove(i);
									isDeleted = true;
									break;
								}
								
//								ArrayList<String> driveOpValues = categoryDriveMap.get(option2.getOp());
//								if( !driveOpValues.contains(option2.getOpValue())){
//									data.remove(i);
//									isDeleted = true;
//									break;
//								}
							}
						}
					}
					
					if( isDeleted){
						break;
					}
					
				}
			}
		}
		return data;		
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private ArrayList<OpComparableConditionSet> getAvailableData(ArrayList<OpComparableConditionSet> preData){
//		HashMap<String, ArrayList<Object[]>> availableAllMap = new HashMap();
//		HashMap<String, ArrayList<Object[]>> notAvailableAllMap = new HashMap();
		
//		ArrayList<String[]> availableAllList = new ArrayList();
//		ArrayList<String[]> notAvailableAllList = new ArrayList();
		
//		HashMap<String, String[]> availableMap = new HashMap();
//		HashMap<String, String[]> notAvailableMap = new HashMap();
		HashMap<String, ArrayList<Option>> categoryMNOMap = new HashMap(); 
		
		for( int i = preData.size() - 1; i >= 0; i--){
			OpComparableConditionSet condition = preData.get(i);
			String conStr = null;
			
			for( Object obj: condition){
				if( obj instanceof Option){
					Option option = (Option)obj;
					if( option.getValue().equals("-")){
						continue;
					}

					// [jclee] Special Country Code일 경우 NON 국가 코드는 일반 국가코드가 들어올 경우 Skip되어야 함. 송대영CJ 요청
					// [2015.04.14][jclee] Special Country Code일 경우 NON 국가 코드와 A40D(내수) Code가 함께 들어온 경우 A40D만 표현. 이보현 책임 요청
					boolean isContainSpecialCountry = false;
//					boolean isContainDomestic = false;
					if (option.getOpName().indexOf("SPECIAL COUNTRY") > -1) {
						for (int inx = 0; inx < condition.size(); inx++) {
							Object obj2 = condition.get(inx);
							if (obj2 instanceof Option) {
								Option option2 = (Option) obj2;
								if (!option2.getValue().equals("-") && 
									option2.getOpValueName().indexOf("NON") == -1 && 
									option2.getOpName().indexOf("SPECIAL COUNTRY") > -1 && 
									!option.getOp().equals(option2.getOp()) &&
									option.getOpValueName().indexOf("NON") > -1) {
									isContainSpecialCountry = true;
									break;
								}
								
//								// A40D(내수) Code가 있는 경우 NON국가 Code는 Skip
//								if (!option2.getValue().equals("-") && 
//									option2.getOpValue().equals("A40D") && 
//									!option.getOp().equals(option2.getOp()) &&
//									option.getOpValueName().indexOf("NON") > -1) {
//									isContainDomestic = true;
//									break;
//								}
							}
						}
					}
					
					if (isContainSpecialCountry) {
						continue;
					}
					
//					if (isContainDomestic) {
//						continue;
//					}
					
					if( (option.getAvailableCondition() != null && !option.getAvailableCondition().equals(""))
							|| (option.getNotAvailableCondition() != null && !option.getNotAvailableCondition().equals(""))){
						ArrayList<Option> optionList = categoryMNOMap.get(option.getOp());
						
						if( optionList == null ){
							optionList = new ArrayList();
							optionList.add(option);
							categoryMNOMap.put(option.getOp(), optionList);
						}else{
							boolean isFound = false;
							for( Option tOption : optionList){
								if(option.getValue().equals(tOption.getValue()) && option.getRemark().equals(tOption.getRemark())){
									isFound = true;
									break;
								}
							}
							
							// [SR없음][2015.04.16][jclee] Optional Option의 경우 Mendatory처럼 단일 조건이 아니라 다른 Option Value의 Value가 S인 경우 Case를 추출하도록 수정.
							if( !isFound && !option.getValue().startsWith("O")){
								optionList.add(option);
							}
						}
					}
					
					if( conStr == null){
						conStr = ((Option)obj).getOpValue();
					}else{
						conStr = conStr + " AND " + ((Option)obj).getOpValue();
					}
				}
			}
			if( conStr == null || conStr.equals("")){
				preData.remove(i);
				continue;
			}
			
			condition.add(conStr);
			
			//=======================================================
			/*
			for( Object obj: condition){
				if( obj instanceof Option){
					Option option = (Option)obj;
					
					if( option.getValue().equals("-")){
						continue;
					}
					
					if( option.getAvailableCondition() != null && !option.getAvailableCondition().equals("")){
						
						ArrayList<Option> optionList = categoryMNOMap.get(option.getOp());
						if( optionList == null ){
							optionList = new ArrayList();
							optionList.add(option);
							categoryMNOMap.put(option.getOp(), optionList);
						}else{
							boolean isFound = false;
							for( Option tOption : optionList){
								if(option.getValue().equals(tOption.getValue()) && option.getAvailableCondition().equals(tOption.getAvailableCondition())
										 && option.getNotAvailableCondition().equals(tOption.getNotAvailableCondition())){
									isFound = true;
									break;
								}
							}
							if( !isFound ){
								optionList.add(option);
							}
						}
						
						String str[] = new String[4];
						str[0] = option.getAvailableCondition();
						str[1] = option.getOpValue();
						String tmpStr = option.getValue();
						if( tmpStr.indexOf("M") == 0){
							str[2] = MANDATORY_OPTION;
						}else if( tmpStr.indexOf("O") == 0 && tmpStr.length() > 1 ){
							str[2] = OPTIONAL_OPTION;
						}
//						str[2] = (option.getValue().indexOf("M") == 0) ? MANDATORY_OPTION:OPTIONAL_OPTION;
						str[3] = option.getRemark();
						availableMap.put(str[3], str);
						try {
							if(!isAvailable(str[0], str[1], str[2], condition, categoryMNOMap)){
								preData.remove(i);
								break;
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					
					if( option.getNotAvailableCondition() != null && !option.getNotAvailableCondition().equals("")){
						
						ArrayList<Option> optionList = categoryMNOMap.get(option.getOp());
						if( optionList == null ){
							optionList = new ArrayList();
							optionList.add(option);
							categoryMNOMap.put(option.getOp(), optionList);
						}else{
							boolean isFound = false;
							for( Option tOption : optionList){
								if(option.getValue().equals(tOption.getValue()) && option.getAvailableCondition().equals(tOption.getAvailableCondition())
										 && option.getNotAvailableCondition().equals(tOption.getNotAvailableCondition())){
									isFound = true;
									break;
								}
							}
							if( !isFound ){
								optionList.add(option);
							}
						}						
						
						String str[] = new String[4];
						str[0] = option.getNotAvailableCondition();
						str[1] = option.getOpValue();
						String tmpStr = option.getValue();
						if( tmpStr.indexOf("M") == 0){
							str[2] = MANDATORY_OPTION;
						}else if( tmpStr.indexOf("O") == 0 && tmpStr.length() > 1 ){
							str[2] = OPTIONAL_OPTION;
						}
//						str[2] = (option.getValue().indexOf("M") == 0) ? MANDATORY_OPTION:OPTIONAL_OPTION;
						str[3] = option.getRemark();
						notAvailableMap.put(str[3], str);
						try {
							if(isAvailable(str[0], str[1], str[2], condition, categoryMNOMap)){
								preData.remove(i);
								break;
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
			*/
			
			
			
//			String[] keys = availableMap.keySet().toArray(new String[availableMap.size()]);
//			for( int k = 0; keys != null && k < keys.length; k++){
//				availableAllList.add(availableMap.get(keys[k]));
//			}
//			
//			keys = notAvailableMap.keySet().toArray(new String[notAvailableMap.size()]);
//			for( int k = 0; keys != null && k < keys.length; k++){
//				notAvailableAllList.add(notAvailableMap.get(keys[k]));
//			}
			//=================================================
		}
		
//		boolean isRemoved = false;
		for( int i = preData.size() - 1; i >= 0; i--){
//			isRemoved = false;
			OpComparableConditionSet condition = preData.get(i);
//			if (((String)condition.get(condition.size() - 1)).equals("A40E AND C00A AND R00A AND R02A AND R20X AND R30X AND S07N AND S30D")) {
//				System.out.println("TEST");
//			}
//			if (((String)condition.get(condition.size() - 1)).equals("A40E AND C00A AND R00A AND R02A AND R20X AND R30X AND S07N AND S30R")) {
//				System.out.println("TEST");
//			}
			if( !isAvailable2(condition, categoryMNOMap)){
				preData.remove(i);
				continue;
			}
		}
		
		/*
		String[][] availableArray = availableMap.values().toArray(new String[availableMap.size()][4]);
		String[][] notAvailableArray = notAvailableMap.values().toArray(new String[notAvailableMap.size()][4]);
		
		boolean isRemoved = false;
		for( int i = preData.size() - 1; i >= 0; i--){
			isRemoved = false;
			OpComparableConditionSet condition = preData.get(i);
			
			String conStr = (String)condition.get(condition.size() - 1);
			
			if( conStr.equals("S30R AND S32M")){
				System.out.println("TEST");
			}
			
			
			for( String[] availableStr : availableArray){
				try {
					if(!isAvailable(availableStr[0], availableStr[1], availableStr[2], condition, categoryMNOMap)){
						preData.remove(i);
						isRemoved = true;
						break;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			if( isRemoved ){
				continue;
			}
			
			for( String[] notAvailableStr : notAvailableArray){
				try {
					if(isAvailable(notAvailableStr[0], notAvailableStr[1], notAvailableStr[2], condition, categoryMNOMap)){
						preData.remove(i);
						isRemoved = true;
						break;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		*/
		return preData;
	}
	
	/**
	 * 저장된 조건을 가져옴
	 * @param selectedCondition
	 */
	public void setSelectedCondition(ArrayList<OpGroup> selectedOpGroup)
	{
		this.selectedOpGroup = selectedOpGroup;
	}
	
	/**
	 * 저장된 Condition을 가져옴
	 */
	private void loadSavedCondition() throws Exception{
		if (selectedOpGroup == null || selectedOpGroup.size() ==0)
			return;
		//FIXME: 첫번째 입력된 Condition 만 가져옴. 여러 Option Group 선택이 되었을 경우 어떻게 할지 협의필요
		StringBuffer conditionSb = new StringBuffer(); ;
		for( OpGroup opGroup : selectedOpGroup)
		{
			String condition = getOpGroupCondition(opGroup);
			conditionSb.append(condition);
			break;
		}
		
		String selectedCondition = conditionSb.toString();
		
		HashMap<String, OpValueName> opValueNameHash = new HashMap<String, OpValueName>();
		OSpec ospec = parentDlg.getOspec();
		for (OpValueName opValueName : ospec.getOpNameList()) 
			opValueNameHash.put(opValueName.toString(), opValueName);

		String[] selectCondSplit = selectedCondition.split(",");
		// 여러 줄 구분을 ' , '로 하기 때문에 ' , '  로 구분하여 한 줄씩 정보를 저장한다.
		for (String rowCondition : selectCondSplit) {
			rowCondition = StringUtils.strip(rowCondition);
			String[] rowConditionValueSplit = rowCondition.split(" ");
			//conditionVec : OR Combination에 추가되는 정보
			@SuppressWarnings("serial")
			Vector<Object> conditionVec = new Vector<Object>() {
				@Override
				public synchronized String toString() {
					// TODO Auto-generated method stub
					String conditionStr = "";
					for (int i = 0; i < elementCount; i++) {
						if (elementData[i] instanceof OpComboValue)
							conditionStr += " " + ((OpComboValue) elementData[i]).getOption();
						else
							conditionStr += " " + elementData[i].toString();
					}
					return conditionStr.trim();
				}
			};

			/**
			 * 한 줄의 Condition 값들을 OR Combination List에 저장되는 유형인 conditionVec에 저장함
			 */
			for (String rowConditionValue : rowConditionValueSplit) {
				if ("AND".equals(rowConditionValue) || "OR".equals(rowConditionValue) || "(".equals(rowConditionValue) || ")".equals(rowConditionValue))
					conditionVec.add(rowConditionValue);
				else {
					OpValueName opValueName = opValueNameHash.get(rowConditionValue);
					if (opValueName == null)
						continue;
					conditionVec.add(opValueName);
				}
			}
			/**
			 * 저장된 conditionVec 정보 값이 유효하면 추가함
			 */
			String condition = conditionVec.toString();
			if (condition.equals(""))
				continue;
			condition = condition.replaceAll(" AND ", " && ");
			condition = condition.replaceAll(" OR ", " || ");
			Pattern p = Pattern.compile("[a-zA-Z0-9]{4}");
			Matcher m = p.matcher(condition);
			String result = m.replaceAll("true");
			try {
				Object obj = parentDlg.getEngine().eval(result);
				if (!(obj instanceof Boolean))
					continue;
				parentDlg.addCondition(conditionVec);
			} catch (ScriptException e1) {
				continue;
			}
		}
	}
	
	/**
	 * Conditon 저장
	 */
	@SuppressWarnings({ "unchecked", "rawtypes"})
	private void saveCondtion() throws Exception
	{
		if (selectedOpGroup == null || selectedOpGroup.size() ==0)
			return;

		DefaultTableModel model = (DefaultTableModel)conditionTable.getModel();
		Vector<Vector> conditionData = model.getDataVector();
		
		/**
		 * Condition 조합 구성
		 */
		StringBuffer coditionSb = new StringBuffer();
		for(int i=0 ; i <  conditionData.size() ; i ++)
		{
			Vector conditionVec = conditionData.get(i);
			String conditionOprator = conditionVec.get(0).toString();
			if(i == 0)
				coditionSb.append(conditionOprator);
			else
			{
				coditionSb.append(",");
				coditionSb.append(conditionOprator);
			}
		}
		if(coditionSb.length() == 0)
			return;
		
		updateOpGroupCondition(coditionSb.toString());
		
		MessageBox.post(parentDlg, "Condition was saved.", "Information", MessageBox.INFORMATION);

	}
	
	/**
	 * 현재 Option Group에 현재 Condition을 저장함
	 * @throws Exception
	 */
	private void updateOpGroupCondition(String condition) throws Exception
	{
			OSpec ospec = parentDlg.getOspec();
			SYMCRemoteUtil remote = new SYMCRemoteUtil();
			
			for(OpGroup opGroup :selectedOpGroup)
			{
				DataSet ds = new DataSet();
				ds.put("PROJECT", ospec.getProject());
				ds.put("OWNER", opGroup.getOwner());
				ds.put("GROUP_NAME", opGroup.getOpGroupName());
				ds.put("CONDITION", condition );
		        remote.execute("com.ssangyong.service.OSpecService", "updateOpGroupCondition", ds);
			}
	}	
	
	
	/**
	 * Option Group Condition을 가져옴
	 * @param opGroup
	 * @return
	 * @throws Exception
	 */
	private String getOpGroupCondition(OpGroup opGroup) throws Exception {
		OSpec ospec = parentDlg.getOspec();
		SYMCRemoteUtil remote = new SYMCRemoteUtil();
		DataSet ds = new DataSet();
		try {
			ds.put("PROJECT", ospec.getProject());
			ds.put("OWNER", opGroup.getOwner());
			ds.put("GROUP_NAME", opGroup.getOpGroupName());
			String condition = (String) remote.execute("com.ssangyong.service.OSpecService", "getOpGroupCondition", ds);
			return condition;
		} catch (Exception e) {
			throw e;
		}
	}
}

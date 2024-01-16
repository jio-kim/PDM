package com.ssangyong.commands.namegroup;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

import com.ssangyong.common.remote.DataSet;
import com.ssangyong.common.remote.SYMCRemoteUtil;
import com.ssangyong.common.utils.ExcelService;
//import com.ssangyong.dto.ExcludeFromCommonPartInEcoData;
import com.ssangyong.dto.ExcludeFromNameGroup;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.VerticalLayout;

/**
 * 
 * @Copyright : Plmsoft
 * @author   : Á¶¼®ÈÆ
 * @since    : 2018. 10. 25.
 * Package ID : com.ssangyong.commands.namegroup.PngExceptionPanel.java
 * SR181025-028
 */
public class PngExceptionPanel extends JPanel{
	
	public static final String PARTNAMEGROUP_QUERY_SERVICE = "com.ssangyong.service.PartNameGroupService";
	public DefaultTableModel tableModel = null;
	private List<ExcludeFromNameGroup> deletedData = new ArrayList<ExcludeFromNameGroup> ();
	private PngDlg pngDlg;
	private String[] column = {"Id", "Seq", "Product", "M/Y", "Group Name", "Remarks", "Date"};
	
	JTable table = null;
	public PngExceptionPanel(PngDlg pngDlg) {
		super(new VerticalLayout(5));
		this.pngDlg = pngDlg;
		initUI();
	}

	private void initUI(){
		add("top.bind.center.center", createButtonPanel());
		add("unbound.bind.center.center", createTablePanel());
	}

	private JPanel createTablePanel(){
		JPanel panel = new JPanel(new VerticalLayout());

		tableModel = new DefaultTableModel() {

		    @Override
		    public boolean isCellEditable(int row, int column) {
		       
		       if( column == 1 || column == 6 )
		    	   return false;
		       else
		    	   return true;
		    }
		};

		tableModel.setColumnIdentifiers(column);

		table = new JTable(tableModel);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
		((DefaultTableCellRenderer)table.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);
		
		TableColumnModel model = table.getColumnModel();
		
		Integer[] aligns = new Integer[]{SwingConstants.CENTER, SwingConstants.CENTER, SwingConstants.CENTER, SwingConstants.CENTER, SwingConstants.LEFT, SwingConstants.LEFT, SwingConstants.CENTER};
		int[] width = {0,10,30,30,120,400,30};
		
		for(int i=0; i<model.getColumnCount(); i++ ){
			
			if( i== 0 ){
				table.getColumnModel().getColumn(0).setPreferredWidth(0);
				table.getColumnModel().getColumn(0).setMinWidth(0);
				table.getColumnModel().getColumn(0).setMaxWidth(0);
			}
			IconColorCellRenderer cellRenderer = new IconColorCellRenderer(new Color(230,230,230));
			cellRenderer.setHorizontalAlignment(aligns[i]);
			model.getColumn(i).setCellRenderer( cellRenderer );
			model.getColumn(i).setPreferredWidth(width[i]);
			
			
			
			
		}
		panel.add("unbound.bind.center.center", new JScrollPane(table));
		
		search();
		
		return panel;
	}

	private JPanel createButtonPanel(){

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		
		JButton addButton = new JButton("ADD");
		addButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionevent) {
				addRpw();
			}
		});
		
		JButton deleteButton = new JButton("DELETE");
		deleteButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionevent) {
				deleteRow();
			}
		});
		
		JButton searchButton = new JButton("Save");
		searchButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionevent) {
				save();
			}
		});

		JButton exportButton = new JButton("Export");
		exportButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionevent) {
				export();
			}
		});
		buttonPanel.add(addButton);
		buttonPanel.add(deleteButton);
		buttonPanel.add(searchButton);
		buttonPanel.add(exportButton);

		return buttonPanel;
	}

	private void save(){
//		System.out.println("sava start ");
		
		try {
			table.editCellAt(-1, -1);
		} catch (Exception ex) {}
		
		ArrayList<ExcludeFromNameGroup> seveData = new ArrayList<ExcludeFromNameGroup> ();
		
			
		if( tableModel != null ){
			
			tableModel.fireTableDataChanged();
			
			int countRow = tableModel.getRowCount();
			
			
			for( int row=0; row < countRow; row++) {
				
				ExcludeFromNameGroup data = new ExcludeFromNameGroup();
				String id = (String)tableModel.getValueAt(row, 0);
				
				String product = (String) tableModel.getValueAt(row, 2);
				String my = (String) tableModel.getValueAt(row, 3);
				String groupName = (String) tableModel.getValueAt(row, 4);
				String remarks = (String) tableModel.getValueAt(row, 5);
				
				if(product == null || product.trim().equals("") || my == null || my.trim().equals("")) {
					continue;
				}
				
				product = product.toUpperCase();
				my = my.toUpperCase();
				if(groupName != null && !groupName.equals("")){
					groupName = groupName.toUpperCase();
				}
				
				
				if( id != null && id.equals("")== false){
					data.setChangedFlag(ExcludeFromNameGroup.CHANGED_FLAG );
				}else{
					data.setChangedFlag(ExcludeFromNameGroup.ADDED_FLAG );
				}
				
				data.setId(id);
				data.setProduct(product);
				data.setMy(my);
				data.setGroupName(groupName);
				data.setRemarks(remarks);
				
				seveData.add(data);

			}

		}
		
		if( deletedData != null && deletedData.size() > 0 )
			seveData.addAll(deletedData);
		
		if( seveData.size() > 0 ) {
		
			SYMCRemoteUtil remoteQuery = new SYMCRemoteUtil();
			
			DataSet ds = new DataSet();
			ds.put("saveData", seveData);
			
			try {
				
				remoteQuery.execute(PARTNAMEGROUP_QUERY_SERVICE, "saveExcludePartData", ds );
			} catch (Exception e) {
				e.printStackTrace();
			}
		
		}
		
		search();
		
	}

	private void export(){
		
		if(table.getRowCount() <= 0){
			MessageBox.post(pngDlg, "Search Result is Empty.", "Information", MessageBox.WARNING);
			return;
		}
		JFileChooser fileChooser = new JFileChooser();
		Calendar now = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddkkmmss");
		sdf.format(now.getTime());
		File defaultFile = new File("Exception_" + sdf.format(now.getTime()) + ".xls");
		fileChooser.setSelectedFile(defaultFile);
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
		int result = fileChooser.showOpenDialog(this);
		if(result == JFileChooser.APPROVE_OPTION){
			ExcelService.createService();
			ExcelService.downloadTable(fileChooser.getSelectedFile(), table, column);
		}
	}
	
	private void addRpw(){
		tableModel.addRow(new String[]{"", "", "", "", "", "",""});
	}
	
	private void deleteRow(){
		int[] selectedRows=table.getSelectedRows();//get selected row's count
	    if(selectedRows.length >0 )
	    {
	       for(int i=0 ; i < selectedRows.length; i++ ) {
	    	   
	    	   int row = selectedRows[i];
	    	   
	    	   String id = (String)tableModel.getValueAt(row, 0);
	    	   if( id != null && id.equals("")== false){
	    		   
	    		   ExcludeFromNameGroup data = new ExcludeFromNameGroup();
	    		   data.setId(id);
	    		   data.setChangedFlag(ExcludeFromNameGroup.DELETED_FLAG);
	    		   data.setProduct((String) tableModel.getValueAt(row, 2));
	    		   data.setMy((String) tableModel.getValueAt(row, 3));
	    		   data.setGroupName((String) tableModel.getValueAt(row, 4));
	    		   data.setRemarks((String) tableModel.getValueAt(row, 5));
	    		   
	    		   deletedData.add(data);
	    	   }
	    	   tableModel.removeRow(row);
	    	   
	       }
	    }
	}
	
	
	public void search(){

		SYMCRemoteUtil remoteQuery = new SYMCRemoteUtil();
		
		DataSet ds = new DataSet();
		ds.put("aa", "aaa");
		
		tableModel.setNumRows(0);
		deletedData.clear();
		
		try {
			ArrayList<ExcludeFromNameGroup> tableData = (ArrayList<ExcludeFromNameGroup>) remoteQuery.execute(PARTNAMEGROUP_QUERY_SERVICE, "getExcludePartData", ds);
			if(tableData != null){
				int resultListSize = tableData.size();

				for (int i = 0; i < resultListSize; i++) {
					ExcludeFromNameGroup part  = tableData.get(i);
					

					tableModel.addRow(new String[]{part.getId(), String.valueOf(i+1), part.getProduct(), part.getMy(), part.getGroupName(), part.getRemarks(), part.getCreateDate().toString()});
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}

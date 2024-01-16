package com.ssangyong.commands.namegroup;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JViewport;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

import com.ssangyong.commands.namegroup.model.PngProd;
import com.ssangyong.common.remote.DataSet;
import com.ssangyong.common.remote.SYMCRemoteUtil;

public class PngAssignTablePanel extends JPanel {

	private Vector<Vector> data = null;
//	private PngMaster pngMaster = null;
//	private JPanel pngAssignTable = null;
	private JTable fixedPngViewTable = null;
	private JTable pngViewTable = null; 
	private PngDlg parentDlg = null;
	private static String[] fixedColumns = new String[]{"Group ID", "Group Name", "Part Names", "Functions"};
	private Vector fixedHeader = null;
	private Vector currentProductHeader = null;
	private JScrollPane scroll = null;
	
//	private Vector prodVec = null;
	
	/**
	 * Create the panel.
	 * @throws Exception 
	 */
	public PngAssignTablePanel(PngDlg parentDlg, String addProduct, String removeProduct, Vector currentProductHeader) throws Exception {
		this.parentDlg = parentDlg;
		
		this.currentProductHeader = currentProductHeader;
		if( this.currentProductHeader == null ){
			this.currentProductHeader = parentDlg.getProductHeader();
		}
		createPngAssignTable(null, this.currentProductHeader, addProduct, removeProduct);
	}

	public Vector getCurrentProductHeader(){
		return currentProductHeader;
	}

	private JPanel createPngAssignTable(Vector<Vector> customData, Vector productHeader, String addProduct, String removeProduct) throws Exception{
		ArrayList<PngProd> prodList = null; //getTrim(osiNo);
		
		fixedHeader = new Vector();
		for(String s : fixedColumns){
			fixedHeader.add(s);
		}
		Vector dataHeader = new Vector();
		Vector addVec = null;
		Vector removeVec = null;
		
		if( customData == null){
			if( addProduct != null){
				addVec = new Vector();
				addVec.add(addProduct);
//				productHeader.add(addProduct);
			}
			
			if( removeProduct != null){
				removeVec = new Vector();
				removeVec.add(removeProduct);
			}
			data = getData(productHeader, addVec, removeVec);
		}else{
			data = customData;
		}
		
		fixedHeader.addAll(productHeader);
		if( addProduct != null){
			fixedHeader.add(addProduct);
			currentProductHeader.add(addProduct);
		}
		if( removeProduct != null){
			fixedHeader.remove(removeProduct);
			currentProductHeader.remove(removeProduct);
		}
		
		dataHeader.addAll(fixedHeader);
		
		final Vector<Vector> fixedData = (Vector<Vector>)data.clone();
		final Vector<Vector> optionData = (Vector<Vector>)data.clone();
		final int columnCount = dataHeader.size();
		final int fixedColumnCount = 4;
		
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
				if( (column + fixedColumnCount) > 3){
					return Boolean.class;
				}else{
					return String.class;
				}
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
		
	    fixedPngViewTable = new JTable(fixedModel){
	    	
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
			
			public void columnMarginChanged(ChangeEvent changeevent) {
				// TODO Auto-generated method stub
				if( scroll != null){
					scroll.getRowHeader().setPreferredSize(fixedPngViewTable.getPreferredSize());
				}
				super.columnMarginChanged(changeevent);
			}
		};
		
		TableColumnModel cm = fixedPngViewTable.getColumnModel();
		int[] width = new int[]{65, 150, 250, 60};
		for( int i = 0; i < cm.getColumnCount(); i++){
			cm.getColumn(i).setPreferredWidth(width[i]);
			cm.getColumn(i).setResizable(true);
		}
	      
		pngViewTable = new JTable(model) {

			public void valueChanged(ListSelectionEvent e) {
				super.valueChanged(e);
				checkSelection(false);
			}

			public Component prepareRenderer(TableCellRenderer renderer,
					int row, int column) {
				Component c = super.prepareRenderer(renderer, row, column);
				if (c instanceof JComponent) {
					JComponent jc = (JComponent) c;
					String toolTip = "Package : " + getValueAt(row, 0).toString() + "." + getValueAt(row, column).toString();
					jc.setToolTipText("<html>"+toolTip.replaceAll("\\.","<br>")+"</html>");
				}
				return c;
			}
			
		};
		
	    fixedPngViewTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		pngViewTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		fixedPngViewTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	    pngViewTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	    
	    fixedPngViewTable.setDefaultRenderer(String.class, new MultiLineTableCellRenderer());

	    scroll = new JScrollPane(pngViewTable);
	    JViewport viewport = new JViewport();
	    viewport.setView(fixedPngViewTable);
	    
	    viewport.setPreferredSize(fixedPngViewTable.getPreferredSize());
	    scroll.setRowHeaderView(viewport);
	    scroll.setCorner(JScrollPane.UPPER_LEFT_CORNER, fixedPngViewTable
	        .getTableHeader());			
	    
		scroll.getViewport().addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent changeevent) {
				// TODO Auto-generated method stub
				scroll.getRowHeader().setView(fixedPngViewTable);
				pngViewTable.revalidate();
			}
		});
		setLayout(new BorderLayout(0, 0));
		add(scroll, BorderLayout.CENTER);
	    return this;
	}
	
	private void checkSelection(boolean isFixedTable) {
		int fixedSelectedIndex = fixedPngViewTable.getSelectedRow();
		int selectedIndex = pngViewTable.getSelectedRow();
		if (fixedSelectedIndex != selectedIndex) {
			if (isFixedTable) {
				if (selectedIndex > pngViewTable.getRowCount() - 1) {
					return;
				}
				pngViewTable.setRowSelectionInterval(fixedSelectedIndex,
						fixedSelectedIndex);
			} else {
				if (selectedIndex > fixedPngViewTable.getRowCount() - 1) {
					return;
				}
				fixedPngViewTable.setRowSelectionInterval(selectedIndex,
						selectedIndex);
			}
		}
	}
	  
	private Vector<Vector> getData(Vector<String> productHeader, Vector<String> addedProducts, Vector<String> removedProducts) throws Exception{
		
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
		
//		Vector<String> header = prodVec;
		
		HashMap<String, HashMap<String, Boolean>> productMap = getAssignData();
		
		ArrayList<HashMap<String, Object>> groupList = parentDlg.getPngList(null, null, "1", true);
		for( int i = 0; groupList != null && i < groupList.size(); i++){
			HashMap map = groupList.get(i);
			String groupID = (String)map.get("GROUP_ID");
			String groupName = (String)map.get("GROUP_NAME");
			String partNames = (String)map.get("PART_NAMES");
			partNames = partNames.replaceAll(",", "\n");
			String refFuncs = (String)map.get("REF_FUNCS");
			refFuncs = refFuncs.replaceAll(",", "\n");
			Vector row = new Vector();
			row.add(groupID);
			row.add(groupName);
			row.add(partNames);
			row.add(refFuncs);
			
			for(String str : productHeader){
				
				if( removedProducts != null && removedProducts.contains(str)){
					continue;
				}
				
				HashMap<String, Boolean> assignMap = productMap.get(str);
				if( assignMap == null){
					row.add(Boolean.FALSE);
				}else{
					Boolean isUse = assignMap.get(groupID);
					if( isUse == null){
						row.add(Boolean.FALSE);
					}else{
						row.add(isUse);
					}
				}
			}
			
			if( addedProducts != null){
				for(String str : addedProducts){
					row.add(Boolean.FALSE);
				}
			}
			data.add(row);
		}
		
		return data;
	}	  
	
	private HashMap<String, HashMap<String, Boolean>> getAssignData() throws Exception{
		
		SYMCRemoteUtil remote = new SYMCRemoteUtil();
		DataSet ds = new DataSet();
		
		HashMap<String, HashMap<String, Boolean>> productMap = new HashMap<String, HashMap<String, Boolean>>();
		ds.put("PARAM", null);
		ArrayList<HashMap<String, Object>> assignList = (ArrayList<HashMap<String, Object>>)remote.execute("com.ssangyong.service.PartNameGroupService", "getPngAssign", ds);
		
		for( int i = 0; assignList != null && i < assignList.size(); i++){
			HashMap map = assignList.get(i);
			String groupID = (String)map.get("GROUP_ID");
			String product = (String)map.get("PRODUCT");
			String isUse = (String)map.get("IS_USE");
			
			HashMap<String, Boolean> assignMap = productMap.get(product);
			if( assignMap == null){
				assignMap = new HashMap();
				assignMap.put(groupID, "1".equals(isUse) ? Boolean.TRUE:Boolean.FALSE);
				productMap.put(product, assignMap);
			}else{
				assignMap.put(groupID, "1".equals(isUse) ? Boolean.TRUE:Boolean.FALSE);
			}
		}
		
		return productMap;
	}
	
	public void save() throws Exception{
		
		DefaultTableModel fixedModel = (DefaultTableModel)fixedPngViewTable.getModel();
		DefaultTableModel model = (DefaultTableModel)pngViewTable.getModel();
		TableColumnModel tcm = pngViewTable.getColumnModel();
		
		
		ArrayList<String> productList = new ArrayList();
		ArrayList<HashMap<String, String>> dataList = new ArrayList<HashMap<String, String>>();
		for( int i = 0; i < pngViewTable.getRowCount(); i++){
			for(int j = 0; j < tcm.getColumnCount(); j++){
				
				String product = (String)tcm.getColumn(j).getHeaderValue();
				if( !productList.contains(product)){
					productList.add(product);
				}
				int modelColumIdx = pngViewTable.convertColumnIndexToModel(j);
				Boolean bUse = (Boolean)model.getValueAt(i, modelColumIdx);
				String isUse = bUse ? "1":"0";
				String groupID = (String)fixedModel.getValueAt(i, 0);
				HashMap<String, String> map = new HashMap<String, String>();
				map.put("PRODUCT", product);
				map.put("GROUP_ID", groupID);
				map.put("IS_USE", isUse);
				dataList.add(map);
//				System.out.println("[" + i + ", " + j + "] = " + bUse);
			}
		}
		
		SYMCRemoteUtil remote = new SYMCRemoteUtil();
		DataSet ds = new DataSet();
		
		ds.put("PRODUCT_LIST", productList);
		ds.put("DATA", dataList);
		try {
			
			remote.execute("com.ssangyong.service.PartNameGroupService", "savePngAssign", ds);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw e;
		}
		
	}
	
	class MultiLineTableCellRenderer extends JTextArea implements TableCellRenderer {
		private List<List<Integer>> rowColHeight = new ArrayList<List<Integer>>();

		public MultiLineTableCellRenderer() {
			setLineWrap(true);
			setWrapStyleWord(true);
			setOpaque(true);
		}
		
		public List<List<Integer>> getRowColHeight() {
			return rowColHeight;
		}

		public Component getTableCellRendererComponent(JTable table, Object value,
				boolean isSelected, boolean hasFocus, int row, int column) {
			if (isSelected) {
				setForeground(table.getSelectionForeground());
				setBackground(table.getSelectionBackground());
			} else {
				setForeground(table.getForeground());
				setBackground(table.getBackground());
			}
			setFont(table.getFont());
			if (hasFocus) {
				setBorder(UIManager.getBorder("Table.focusCellHighlightBorder"));
				if (table.isCellEditable(row, column)) {
					setForeground(UIManager.getColor("Table.focusCellForeground"));
					setBackground(UIManager.getColor("Table.focusCellBackground"));
				}
			} else {
				setBorder(new EmptyBorder(1, 2, 1, 2));
			}
			if (value != null) {
				setText(value.toString());
			} else {
				setText("");
			}
			adjustRowHeight(table, row, column);
			return this;
		}

		/**
		 * Calculate the new preferred height for a given row, and sets the height
		 * on the table.
		 */
		private void adjustRowHeight(JTable table, int row, int column) {
			// The trick to get this to work properly is to set the width of the
			// column to the
			// textarea. The reason for this is that getPreferredSize(), without a
			// width tries
			// to place all the text in one line. By setting the size with the with
			// of the column,
			// getPreferredSize() returnes the proper height which the row should
			// have in
			// order to make room for the text.
			int cWidth = table.getTableHeader().getColumnModel().getColumn(column)
					.getWidth();
			setSize(new Dimension(cWidth, 1000));
			int prefH = getPreferredSize().height;
			while (rowColHeight.size() <= row) {
				rowColHeight.add(new ArrayList<Integer>(column));
			}
			List<Integer> colHeights = rowColHeight.get(row);
			while (colHeights.size() <= column) {
				colHeights.add(0);
			}
			colHeights.set(column, prefH);
			int maxH = prefH;
			for (Integer colHeight : colHeights) {
				if (colHeight > maxH) {
					maxH = colHeight;
				}
			}
			
			if (table.getRowHeight(row) != maxH) {
				table.setRowHeight(row, maxH);
				
			}
			pngViewTable.setRowHeight(row, table.getRowHeight(row));
		}
	}
	
	/**
	 * [SR150416-025][2015.05.27][jclee] Assignment 체크박스 일괄 체크 기능 추가
	 * get Fixed Table
	 * @return
	 */
	public JTable getFixedPngViewTable() {
		return fixedPngViewTable;
	}
	
	/**
	 * [SR150416-025][2015.05.27][jclee] Assignment 체크박스 일괄 체크 기능 추가
	 * get Table
	 * @return
	 */
	public JTable getPngViewTable() {
		return pngViewTable;
	}
}



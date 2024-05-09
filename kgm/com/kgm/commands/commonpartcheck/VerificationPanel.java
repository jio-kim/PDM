package com.kgm.commands.commonpartcheck;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

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

import com.kgm.common.remote.DataSet;
import com.kgm.common.remote.SYMCRemoteUtil;
import com.kgm.common.utils.ExcelService;
import com.kgm.dto.TCEcoModel;
import com.kgm.dto.TCPartModel;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.VerticalLayout;

/**
 * 
 * @Copyright : Plmsoft
 * @author   : ¿Ã¡§∞«
 * @since    : 2018. 4. 6.
 * Package ID : com.kgm.commands.commonpartcheck.VerificationPanel.java
 */
public class VerificationPanel extends JPanel{
	
	public static final String COMMONPARTCHECK_QUERY_SERVICE = "com.kgm.service.CommonPartCheckService";
	
	public DefaultTableModel tableModel = null;
	
	private CommonPartCheckDialog commonPartCheckDialog;
	
	private String[] column = {"Seq", "ECO No.", "Status", "Part No.", "Part Name", "Date"};
	
	JTable table = null;
	
	public VerificationPanel( CommonPartCheckDialog commonPartCheckDialog ) {
		super(new VerticalLayout(5));
		this.commonPartCheckDialog = commonPartCheckDialog;
		initUI();
	}

	private void initUI(){
		add("top.bind.center.center", createButtonPanel());
		add("unbound.bind.center.center", createTablePanel());
	}

	private JPanel createTablePanel(){
		JPanel panel = new JPanel(new VerticalLayout());

		tableModel = new DefaultTableModel();
		tableModel.setColumnIdentifiers(column);
		
		table = new JTable(tableModel);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
		
		table.setAutoCreateRowSorter(true);
		
		((DefaultTableCellRenderer)table.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);
		Integer[] aligns = new Integer[]{SwingConstants.CENTER, SwingConstants.CENTER, SwingConstants.CENTER, SwingConstants.CENTER, SwingConstants.LEFT, SwingConstants.CENTER};
		
		TableColumnModel model = table.getColumnModel();
		int[] width = {10,10,30,30,150,30};
		for(int i=0; i<model.getColumnCount(); i++ ){
			
			IconColorCellRenderer cellRenderer = new IconColorCellRenderer(new Color(230,230,230));
			cellRenderer.setHorizontalAlignment(aligns[i]);
			model.getColumn(i).setCellRenderer( cellRenderer );
			model.getColumn(i).setPreferredWidth(width[i]);
			
			if( i== 5 ){
				table.getColumnModel().getColumn(i).setPreferredWidth(0);
				table.getColumnModel().getColumn(i).setMinWidth(0);
				table.getColumnModel().getColumn(i).setMaxWidth(0);
			}
		}
		panel.add("unbound.bind.center.center", new JScrollPane(table));

		return panel;
	}

	private JPanel createButtonPanel(){

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

		JButton searchButton = new JButton("Search");
		searchButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionevent) {
				search();
			}
		});

		JButton exportButton = new JButton("Export");
		exportButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionevent) {
				export();
			}
		});
		buttonPanel.add(searchButton);
		buttonPanel.add(exportButton);

		return buttonPanel;
	}

	private void search(){
		
		SYMCRemoteUtil remoteQuery = new SYMCRemoteUtil();
		ArrayList<TCPartModel> resultList = null;
		
		tableModel.setNumRows(0);
		
		DataSet ds = new DataSet();
		ds.put("aa", "aaa");

		try {
			resultList = (ArrayList<TCPartModel>) remoteQuery.execute(COMMONPARTCHECK_QUERY_SERVICE, "getOldPartListWithN1", ds);
			if(resultList != null){
				int resultListSize = resultList.size();
				String eco_no = "";
				String status = "";
				String partNo = "";
				String partName = "";
				String date = "";
				
				for (int i = 0; i < resultListSize; i++) {
					TCPartModel part  = resultList.get(i);
					eco_no = part.getEcoModel().getEcoNo();
					status = part.getEcoModel().getEcoStatus();
					partNo = part.getPartNo();
					partName = part.getPartName();
	
					tableModel.addRow(new String[]{String.valueOf(i+1), eco_no, status, partNo, partName, date});
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void export(){
		if(table.getRowCount() <= 0){
			MessageBox.post(commonPartCheckDialog, "Search Result is Empty.", "Information", MessageBox.WARNING);
			return;
		}
		JFileChooser fileChooser = new JFileChooser();
		Calendar now = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddkkmmss");
		sdf.format(now.getTime());
		File defaultFile = new File("Verification_" + sdf.format(now.getTime()) + ".xls");
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
}

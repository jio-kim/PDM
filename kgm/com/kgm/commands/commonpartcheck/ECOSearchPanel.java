package com.kgm.commands.commonpartcheck;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

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
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.VerticalLayout;

/**
 * 
 * @Copyright : Plmsoft
 * @author   : ¿Ã¡§∞«
 * @since    : 2018. 4. 6.
 * Package ID : com.kgm.commands.commonpartcheck.ECOSearchPanel.java
 */
public class ECOSearchPanel extends JPanel{
	private JTable table;
	private CommonPartCheckDialog commonPartCheckDialog;
	private String[] column = {"Seq", "ECO No.", "Status", "ECO Dept.", "Owner", "Description"};

	public static final String COMMONPARTCHECK_QUERY_SERVICE = "com.kgm.service.CommonPartCheckService";
	public DefaultTableModel tableModel = null;

	public ECOSearchPanel(CommonPartCheckDialog commonPartCheckDialog) {
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
		
		GregorianCalendar cal = new GregorianCalendar(); 
		cal.add(cal.DATE,-1); 
		
		JLabel label = new JLabel(" ¢∫ Search Date : " + new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime()));
		label.setForeground(Color.BLUE);

		tableModel = new DefaultTableModel(){
			@Override
			public boolean isCellEditable(int paramInt1, int paramInt2) {
				// TODO Auto-generated method stub
				return false;
			}
		};
		
		tableModel.setColumnIdentifiers(column);
		Integer[] aligns = new Integer[]{SwingConstants.CENTER, SwingConstants.CENTER, SwingConstants.CENTER, SwingConstants.LEFT, SwingConstants.CENTER, SwingConstants.LEFT };
		
		
		table = new JTable(tableModel);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
		table.setAutoCreateRowSorter(true);
		((DefaultTableCellRenderer)table.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);
//		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

		int[] width = {1,10,30,120,10,400};
		TableColumnModel model = table.getColumnModel();
		for(int i=0; i<model.getColumnCount(); i++ ){
			
			IconColorCellRenderer cellRenderer = new IconColorCellRenderer(new Color(230,230,230));
			cellRenderer.setHorizontalAlignment(aligns[i]);
			model.getColumn(i).setCellRenderer( cellRenderer );
			model.getColumn(i).setPreferredWidth(width[i]);
		
		}

		panel.add("top.bind.center.center", label);
		panel.add("unbound.bind.center.center", new JScrollPane(table));

		return panel;
	}

	private JPanel createButtonPanel(){

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

		JButton saveButton = new JButton("Search");
		saveButton.addActionListener(new ActionListener() {
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
		buttonPanel.add(saveButton);
		buttonPanel.add(exportButton);

		return buttonPanel;
	}

	private void search(){
		initRow();
		getEcoList();

	}
	
	private void initRow(){
		tableModel.setRowCount(0);
		tableModel.setNumRows(0);
	}

	private void getEcoList(){
		SYMCRemoteUtil remoteQuery = new SYMCRemoteUtil();
		ArrayList<TCEcoModel> resultList = null;

		DataSet ds = new DataSet();
		ds.put("aa", "aaa");

		try {
			resultList = (ArrayList<TCEcoModel>) remoteQuery.execute(COMMONPARTCHECK_QUERY_SERVICE, "getEcoList", ds);
			if(resultList != null){
				int resultListSize = resultList.size();
				String eco_no = "";
				String status = "";
				String dept = "";
				String owner = "";
				String description = "";
				
				for (int i = 0; i < resultListSize; i++) {
					TCEcoModel ecoModel  = resultList.get(i);
					eco_no = ecoModel.getEcoNo();
					status = ecoModel.getEcoStatus();
					dept = ecoModel.getOwningTeam();
					owner = ecoModel.getOwningUser();
					description = ecoModel.getEcoDesc();
	
					tableModel.addRow(new String[]{String.valueOf(i+1), eco_no, status, dept, owner, description});
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
		File defaultFile = new File("ECOSearch_" + sdf.format(now.getTime()) + ".xls");
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

package com.ssangyong.commands.commonpartcheck;

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

import com.ssangyong.common.WaitProgressBar;
import com.ssangyong.common.remote.DataSet;
import com.ssangyong.common.remote.SYMCRemoteUtil;
import com.ssangyong.common.utils.ExcelService;
import com.ssangyong.dto.TCBomLineData;
import com.ssangyong.dto.TCPartModel;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.util.DateButton;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.VerticalLayout;

/**
 * 
 * @Copyright : Plmsoft
 * @author   : ¿Ã¡§∞«
 * @since    : 2018. 4. 6.
 * Package ID : com.ssangyong.commands.commonpartcheck.ErrorReportPanel.java
 */
public class ErrorReportPanel extends JPanel{
	
	public static final String COMMONPARTCHECK_QUERY_SERVICE = "com.ssangyong.service.CommonPartCheckService";
	public DefaultTableModel tableModel = null;
	private CommonPartCheckDialog commonPartCheckDialog;
	private String[] column = {"Seq", "ECO No.", "Status", "Function", "Parent Part", "Part No.", "Part Name", "Seq", "S/Mode", "Date"};
	JTable table = null;
	
	DateButton fromDateButton;
	DateButton toDatebutton;
	
	
	public ErrorReportPanel( CommonPartCheckDialog commonPartCheckDialog ) {
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
		Integer[] aligns = new Integer[]{SwingConstants.CENTER, SwingConstants.CENTER, SwingConstants.CENTER, SwingConstants.CENTER, SwingConstants.CENTER, SwingConstants.CENTER, SwingConstants.CENTER
				, SwingConstants.CENTER, SwingConstants.CENTER, SwingConstants.CENTER };
		
		table = new JTable(tableModel);
		
		table.setAutoCreateRowSorter(true);
		
		table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
		((DefaultTableCellRenderer)table.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);
		
		table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);

		TableColumnModel model = table.getColumnModel();
		for(int i=0; i<model.getColumnCount(); i++ ){
			IconColorCellRenderer cellRenderer = new IconColorCellRenderer(new Color(230,230,230));
			cellRenderer.setHorizontalAlignment(aligns[i]);
			model.getColumn(i).setCellRenderer( cellRenderer );
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
		
		JButton createButton = new JButton("Create");
		createButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionevent) {
				create();
			}
		});
		
		fromDateButton = new DateButton(new SimpleDateFormat("yyyy-MM-dd"));
		toDatebutton = new DateButton(new SimpleDateFormat("yyyy-MM-dd"));
		
		buttonPanel.add(new JLabel("From "));
		buttonPanel.add(fromDateButton);
		buttonPanel.add(new JLabel("~ To "));
		buttonPanel.add(toDatebutton);
		buttonPanel.add(searchButton);
		buttonPanel.add(exportButton);
//		buttonPanel.add(createButton);
		
		
		
		return buttonPanel;
	}
	
	private void search(){
		SYMCRemoteUtil remoteQuery = new SYMCRemoteUtil();
		ArrayList<TCBomLineData> resultList = null;
		
		tableModel.setNumRows(0);
		
		DataSet ds = new DataSet();
		ds.put("FROM", fromDateButton.getDateString());
		ds.put("TO", toDatebutton.getDateString());

		try {
			resultList = (ArrayList<TCBomLineData>) remoteQuery.execute(COMMONPARTCHECK_QUERY_SERVICE, "getCommonPartCheckReport", ds);
			if(resultList != null){
				int resultListSize = resultList.size();
				
				
				for (int i = 0; i < resultListSize; i++) {
					TCBomLineData bomLine  = resultList.get(i);
	
					tableModel.addRow(new String[]{String.valueOf(i+1), bomLine.getEco().getEcoNo() , bomLine.getEco().getEcoStatus(),
							  bomLine.getFunctionNo(), bomLine.getParent().getPartNo(), bomLine.getChild().getPartNo(), bomLine.getChild().getPartName(),
							  bomLine.getSeq(), bomLine.getSupplyMode(), bomLine.getCreateDate().toString() });
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void create(){
		SYMCRemoteUtil remoteQuery = new SYMCRemoteUtil();
		DataSet ds = new DataSet();
		
		WaitProgressBar progress = new WaitProgressBar(AIFUtility.getActiveDesktop().getFrame());
		try {
			progress.setWindowSize(500, 400);
			progress.start();						
			progress.setShowButton(true);
			progress.setStatus("Report create start.");
			progress.setAlwaysOnTop(true);
			
			remoteQuery.execute(COMMONPARTCHECK_QUERY_SERVICE, "createReport", ds);
		
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			if(progress != null){
				progress.close();
			}
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
		File defaultFile = new File("Error Report_" + sdf.format(now.getTime()) + ".xls");
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

package com.kgm.commands.nmcd;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
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

import com.kgm.commands.weight.PropertyConstant;
import com.kgm.commands.weight.TypeConstant;
import com.kgm.common.WaitProgressBar;
import com.kgm.common.lov.SYMCLOVLoader;
import com.kgm.common.remote.DataSet;
import com.kgm.common.remote.SYMCRemoteUtil;
import com.kgm.common.utils.CustomUtil;
import com.kgm.common.utils.ExcelService;
import com.kgm.common.utils.PreferenceService;
import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.aif.AIFShell;
import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.aif.InterfaceAIFOperationListener;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCPreferenceService;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.VerticalLayout;

/**
 * 
 * @Copyright : Plmsoft
 * @author   : 조석훈
 * @since    : 2018. 8. 30.
 * Package ID : com.kgm.commands.nmcd.Register.java
 */
//2024.01.09  수정   generic 삭제
public class ValidationPanel extends JPanel{
	
	public static final String COMMONPARTCHECK_QUERY_SERVICE = "com.kgm.service.CommonPartCheckService";
	public DefaultTableModel tableModel = null;
	private NmcdDialog nmcdDialog;
	private String[] column = {"No", "Function", "Parent P/No", "Seq", "Lev", "Part No", "Part Name", "Group", "Owner"};
	private Integer[] aligns = new Integer[]{SwingConstants.CENTER,SwingConstants.CENTER, SwingConstants.CENTER, SwingConstants.CENTER, SwingConstants.CENTER, SwingConstants.CENTER, SwingConstants.LEFT, SwingConstants.CENTER, SwingConstants.CENTER};
	int[] width = {10,100,100,40,10,100,400,200,100};
	private JComboBox comboProduct;
	private HashMap<String, TCComponentItem> productMap = new HashMap<String, TCComponentItem>();
	private String productId = "";
	private TCSession session = null;
	private JTable table = null;
	private JButton searchButton;

	public ValidationPanel(NmcdDialog nmcdDialog) throws Exception{
		super(new VerticalLayout(5));
		this.nmcdDialog = nmcdDialog;
		session = (TCSession) AIFDesktop.getActiveDesktop().getCurrentApplication().getSession();
		initUI();
		initOpereation();
	}

	private void initUI() throws Exception{
		add("top.bind.center.center", topButtonPanel());
		add("unbound.bind.center.center", createTablePanel());
	}

	private JPanel createTablePanel(){
		JPanel panel = new JPanel(new VerticalLayout());

		tableModel = new DefaultTableModel() {

		    @Override
		    public boolean isCellEditable(int row, int column) {
		       
	    	   return false;
		    }
		};

		tableModel.setColumnIdentifiers(column);

		table = new JTable(tableModel);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
		((DefaultTableCellRenderer)table.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);
		
		TableColumnModel model = table.getColumnModel();
		
		JComboBox combo = null;
		IconColorCellRenderer cellRenderer;
		for(int i=0; i<model.getColumnCount(); i++ ){
			cellRenderer = new IconColorCellRenderer(new Color(230,230,230));
			cellRenderer.setHorizontalAlignment(aligns[i]);
			model.getColumn(i).setCellRenderer( cellRenderer );
			model.getColumn(i).setPreferredWidth(width[i]);
			
		}
		panel.add("unbound.bind.center.center", new JScrollPane(table));

		return panel;
	}
	
	private JPanel topButtonPanel(){
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		
		//product Combo
		JLabel lblProduct = new JLabel("Product");
		lblProduct.setHorizontalAlignment(SwingConstants.CENTER);
		buttonPanel.add(lblProduct);

		comboProduct = new JComboBox();
		comboProduct.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent paramItemEvent) {
				Object selectItem = ((JComboBox) paramItemEvent.getSource()).getSelectedItem();

				if (!paramItemEvent.getItem().equals(selectItem) && selectItem != null) {
					productChangeAction(selectItem.toString());
				}
			}
		});
		buttonPanel.add(comboProduct);
		
		//Button
		searchButton = new JButton("Search");
		searchButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionevent) {
				searchButton.setEnabled(false);
				search();
			}
		});

		buttonPanel.add(searchButton);
		
		JButton exportButton = new JButton("Excel Export");
		exportButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionevent) {
				export();
			}
		});
		
		buttonPanel.add(exportButton);

		return buttonPanel;
	}
	
	protected void productChangeAction(String selectedProduct) {
		try {
			productId = "";
			tableModel.setNumRows(0);
			if(selectedProduct.equals("")){
				return;
			}
			TCComponentItem prodItem = productMap.get(selectedProduct);
			TCComponentItemRevision prodRev = prodItem.getLatestItemRevision();;
			productId = prodItem.getStringProperty(PropertyConstant.ATTR_NAME_ITEMID);
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	
	private void initOpereation() throws Exception{
		String[] possibleProducts = PreferenceService.getValues(TCPreferenceService.TC_preference_site, "SYMC_Selected_Product_NMCD");
		
		String includeProductValues = "";
		if( possibleProducts != null ){
			for( int i =0; i < possibleProducts.length ; i++){
				includeProductValues = includeProductValues + (i != 0 ?";":"") + possibleProducts[i];
			}
		}
		
		TCComponent[] findProducts = CustomUtil.queryComponent("Item...", new String[]{"Type", "Item ID"}, new String[]{TypeConstant.S7_PRODUCTTYPE, includeProductValues});
		
		if(findProducts != null && findProducts.length > 0){
			ArrayList<String> productList = new ArrayList<String>(findProducts.length);
			String item_id = "";
			String name = "";
			String product = "";
			for (TCComponent prod : findProducts) {
				TCComponentItemRevision prodRev = ((TCComponentItem)prod).getLatestItemRevision();
				item_id = prodRev.getItem().getProperty(PropertyConstant.ATTR_NAME_ITEMID);
				name = prodRev.getItem().getProperty(PropertyConstant.ATTR_NAME_ITEMNAME);
				product = item_id+" - "+name;
				productList.add(product);
				if (!productMap.containsKey(product)){
					productMap.put(product, prodRev.getItem());
				}
			}
			Collections.sort(productList);
			comboProduct.addItem("");
			for(String prd : productList){
				comboProduct.addItem(prd);
			}
		}
	}

	public class SearchOperation extends AbstractAIFOperation {
		
		public SearchOperation() {
		}

		@Override
		public void executeOperation() throws Exception {
//			WaitProgressBar progressBar = new WaitProgressBar(AIFUtility.getActiveDesktop().getFrame());
			WaitProgressBar progressBar = new WaitProgressBar(nmcdDialog);
			progressBar.setWindowSize(500, 300);
			progressBar.start();
			progressBar.setAlwaysOnTop(true);
			progressBar.setStatus("BOM Load Start...."	, true);
			  
			try {
				ArrayList<HashMap> resultList = null;
				SYMCRemoteUtil remoteQuery = new SYMCRemoteUtil();
				DataSet ds = new DataSet();
				ds.put("PRD_NO", productId);
				resultList = (ArrayList<HashMap>) remoteQuery.execute(COMMONPARTCHECK_QUERY_SERVICE, "getEplList", ds);
				if(resultList != null && resultList.size() > 0){
					int resultListSize = resultList.size();
					String fid = "";
					String pid = "";
					String cid = "";
					String cname = "";
					String lev = "";
					String seq = "";
					String group = "";
					String user = "";
					for (int i = 0; i < resultListSize; i++) {
						HashMap rowHash  = resultList.get(i);
						fid = (String)rowHash.get("FID");
						pid = (String)rowHash.get("PID");
						cid = (String)rowHash.get("CID");
						cname = (String)rowHash.get("CNAME");
						lev = rowHash.get("LEV").toString();
						seq = (String)rowHash.get("SEQ");
						group = (String)rowHash.get("GRP");
						user = (String)rowHash.get("OWNR");
						
						tableModel.addRow(new String[]{(i+1)+"", fid, pid, seq, lev, cid, cname, group, user});
						progressBar.setStatus("  Loding... " + (i+1)+ " / "+resultListSize, true);
					}
					progressBar.close();
				} else {
					progressBar.setStatus("조회 결과가 없습니다."	, true);
					progressBar.close("조회 결과가 없습니다", false);
				}
			
			} catch (Exception e) {
				progressBar.setStatus(e.getMessage(), true);
				progressBar.close("오류 발생", false);
			} finally{
				searchButton.setEnabled(true);
			}
			
		}
	}
	
	public void search(){
		
		try {
			tableModel.setNumRows(0);
			if(productId.equals("")){
				MessageBox.post(nmcdDialog, "Product을 선택하십시오.", "Search", MessageBox.INFORMATION);
				searchButton.setEnabled(true);
	            return;
			}
			
			SearchOperation op = new SearchOperation();
			op.addOperationListener(new InterfaceAIFOperationListener() {
				@Override
				public void startOperation(String arg0) {
				}
			
				@Override
				public void endOperation() {
					
				}
			});
			session.queueOperation(op);
			
		} catch (Exception e) {
			searchButton.setEnabled(true);
			e.printStackTrace();
		}
	}
	
	
private void export(){
		
		if(table.getRowCount() <= 0){
			MessageBox.post(nmcdDialog, "Search Result is Empty.", "Information", MessageBox.WARNING);
			return;
		}
		JFileChooser fileChooser = new JFileChooser();
		Calendar now = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddkkmmss");
		sdf.format(now.getTime());
		File defaultFile = new File(productId + "_" +"NMCD_Value_Null_Part" + sdf.format(now.getTime()) + ".xls");
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
			ExcelService.downloadTable(fileChooser.getSelectedFile(), table, column, "");
		}
		
		AIFShell aif = new AIFShell("application/vnd.ms-excel", fileChooser.getSelectedFile().getAbsolutePath());
        aif.start();
	}
}

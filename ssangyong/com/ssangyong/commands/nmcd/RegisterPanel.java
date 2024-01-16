package com.ssangyong.commands.nmcd;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

import com.ssangyong.commands.weight.PropertyConstant;
import com.ssangyong.commands.weight.TypeConstant;
import com.ssangyong.common.WaitProgressBar;
import com.ssangyong.common.remote.DataSet;
import com.ssangyong.common.remote.SYMCRemoteUtil;
import com.ssangyong.common.utils.CustomUtil;
import com.ssangyong.common.utils.ExcelService;
import com.ssangyong.common.utils.PreferenceService;
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
 * Package ID : com.ssangyong.commands.nmcd.Register.java
 */

//2024.01.09  수정   generic 삭제
public class RegisterPanel extends JPanel{
	
	public static final String COMMONPARTCHECK_QUERY_SERVICE = "com.ssangyong.service.CommonPartCheckService";
	public DefaultTableModel tableModel = null;
	private NmcdDialog nmcdDialog;
	//20201217 CF-1708 seho Weight 관리 column 추가.
	private String[] column = {"No", "Function", "Parent P/No", "Seq", "Lev", "Part No", "Part Name", "Weight 관리(STD)", "NMCD", "S/Mode", "M Project Code", "New Team", "Group", "Owner", "Update Date"};
	private Integer[] aligns = new Integer[]{SwingConstants.CENTER,SwingConstants.CENTER, SwingConstants.CENTER, SwingConstants.CENTER, SwingConstants.CENTER, SwingConstants.CENTER, SwingConstants.LEFT, SwingConstants.CENTER, SwingConstants.CENTER, SwingConstants.CENTER, SwingConstants.CENTER, SwingConstants.CENTER, SwingConstants.CENTER, SwingConstants.CENTER, SwingConstants.CENTER};
	int[] width = {20,100,100,40,20,100,350,50, 50,50,80,200,200,100,150};
	private JComboBox comboProduct;
	private JComboBox comboFunction;
	private JComboBox comboFmp;
	private JComboBox comboNmcd;
	private HashMap<String, TCComponentItem> productMap = new HashMap<String, TCComponentItem>();
	private HashMap<String, String> functionMap = new HashMap<String, String>();
	private HashMap<String, String> fmpMap = new HashMap<String, String>();
	private String productId = "";
	private String functionId = "";
	private String functionRevId = "";
	private String fmpId = "";
	private String fmpRevId = "";
	private TCSession session = null;
	private HashMap<String, String[]> oldMap = new HashMap<String, String[]>();
	private HashMap<String, String[]> insertMap = new HashMap<String, String[]>();
	private HashMap<String, String[]> updateMap = new HashMap<String, String[]>();
	private HashMap<String, String[]> deleteMap = new HashMap<String, String[]>();
	private JTable table = null;
	private JButton searchButton;
	private JButton saveButton;
	private JButton applyButton;
	private String[] nmcdValues;
//	private String[] projectCodes;
	private String[] teamNameArray;
	private final int parent_idx = 2;
	private final int part_idx = 5;
	//20201217 CF-1708 seho index 추가 및 기존 index 뒤로 밀림.
	private final int weightmngt_idx = 7;
	private final int nmcd_idx = 8;
	private final int pcode_idx = 10;
	private final int nteam_idx = 11;
	//20190522 kch add - Copy & Paste 기능 추가
	private int[] copiedRows = null;
	private int[] copiedColumns = null;
	private ArrayList<Integer> notEditableColumn = new ArrayList();
	private StringBuffer teamValidationResult;
	private StringBuffer nmcdValidationResult;
	private StringBuffer prjValidationResult;
	private ArrayList<String> projectValueList;
	private ArrayList<String> nmcdValueList;

	public RegisterPanel(NmcdDialog nmcdDialog) throws Exception{
		super(new VerticalLayout(5));
		this.nmcdDialog = nmcdDialog;
		session = (TCSession) AIFDesktop.getActiveDesktop().getCurrentApplication().getSession();
		String[] oldNmcdValue = nmcdDialog.getNmcdValues();
		int len  = oldNmcdValue.length;
		nmcdValues = new String[len+1];
		for(int i=0; i < len+1; i++){
			if(i == 0){
				nmcdValues[i] = "";
			} else {
				nmcdValues[i] = oldNmcdValue[i-1];
			}
		}
		nmcdValueList = new ArrayList(Arrays.asList(oldNmcdValue));
		
		String[] oldProjectCodeValue = nmcdDialog.getProjectCodeValues();
//		int llen  = oldProjectCodeValue.length;
//		projectCodes = new String[llen+1];
//		for(int i=0; i < llen+1; i++){
//			if(i == 0){
//				projectCodes[i] = "";
//			} else {
//				projectCodes[i] = oldProjectCodeValue[i-1];
//			}
//		}
		projectValueList = new ArrayList(Arrays.asList(oldProjectCodeValue));
		
		Object[] oldTeamNameArray = nmcdDialog.getTeamList().toArray();
		int tlen  = oldTeamNameArray.length;
		teamNameArray = new String[tlen+1];
		for(int i=0; i < tlen+1; i++){
			if(i == 0){
				teamNameArray[i] = "";
			} else {
				teamNameArray[i] = (String)oldTeamNameArray[i-1];
			}
		}
		
		initUI();
		initOpereation();
	}

	private void initUI() throws Exception{
		add("top.bind.center.center", topButtonPanel());
		add("unbound.bind.center.center", createTablePanel());
//		add("bottom.bind.center.center", saveButtonPanel());
	}

	private JPanel createTablePanel(){
		JPanel panel = new JPanel(new VerticalLayout());

		tableModel = new DefaultTableModel() {

		    @Override
		    public boolean isCellEditable(int row, int column) {
		       //20201217 CF-1708 seho Weight 관리 column 도 수정 가능하도록...
		       if( column == nmcd_idx || column == pcode_idx || column == nteam_idx || column == weightmngt_idx)
		    	   return true;
		       else
		    	   return false;
		    }
		};

		tableModel.setColumnIdentifiers(column);
		
		for(int i= 0; i < column.length; i++){
			//20201217 CF-1708 seho Weight 관리 column 도 수정 가능하도록...
			if(!(i == nmcd_idx || i == pcode_idx || i == nteam_idx || i == weightmngt_idx)){
				notEditableColumn.add(i);
			}
		}

		table = new JTable(tableModel);
		//20190522 Copy & Paste 기능 추가
		//Cell 선택 기능
		table.setCellSelectionEnabled(true);
		table.setColumnSelectionAllowed(true);
		table.setRowSelectionAllowed(true);
		table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		//column 이동 불가
		table.getTableHeader().setReorderingAllowed(false);
		//Table 마우스 리스너 추가
		table.addMouseListener(new TableMouseListener());
		//20190522 kch add End
		
		table.addKeyListener(new TableKeyListener());
				
		table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
		
		((DefaultTableCellRenderer)table.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);
		
		DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer();
		headerRenderer.setBackground(Color.ORANGE);
		headerRenderer.setHorizontalAlignment(JLabel.CENTER);
		for (int i = 0; i < table.getModel().getColumnCount(); i++) {
			//20201217 CF-1708 seho Weight 관리 column 도 헤더 색깔 변경
			if(i == nmcd_idx || i == pcode_idx || i == nteam_idx || i == weightmngt_idx)
				table.getColumnModel().getColumn(i).setHeaderRenderer(headerRenderer);
		}
		
		
				
		TableColumnModel model = table.getColumnModel();
		
		JComboBox combo = null;
		JTextField f;
		IconColorCellRenderer cellRenderer;
		AutoSuggestor autoSuggestor;
//		ComboAgent agent;
		for(int i=0; i<model.getColumnCount(); i++ ){
			cellRenderer = new IconColorCellRenderer(new Color(230,230,230));
			cellRenderer.setHorizontalAlignment(aligns[i]);
			model.getColumn(i).setCellRenderer( cellRenderer );
			model.getColumn(i).setPreferredWidth(width[i]);
			
			switch(i){
				case weightmngt_idx://20201217 CF-1708 seho Weight 관리 column 에디터 적용
					JComboBox wmcombo = new JComboBox(new String[] {"", "O"});
					wmcombo.setEditable(false);
					DefaultCellEditor wmeditor = new DefaultCellEditor(wmcombo);
					wmeditor.setClickCountToStart(2);
					model.getColumn(i).setCellEditor(wmeditor);
					break;
			case nmcd_idx:
				combo = new JComboBox(nmcdValues);
				DefaultCellEditor editor = new DefaultCellEditor(combo){
					
				};
				editor.setClickCountToStart(2);
				model.getColumn(i).setCellEditor(editor);
				break;
			case pcode_idx:
				f = new JTextField(10);
				model.getColumn(i).setCellEditor(new DefaultCellEditor(f));
				autoSuggestor = new AutoSuggestor(f, this.nmcdDialog, table, projectValueList, Color.WHITE.brighter(), Color.BLUE, Color.RED, 0.75f) {
					 @Override
					 boolean wordTyped(String typedWord) {
						 return super.wordTyped(typedWord);//now call super to check for any matches against newest dictionary
					 }
				};
				
				//comboAgent
//				combo = new JComboBox(projectCodes);
//				combo.setEditable(true);
//				agent = new ComboAgent(combo);
//				editor = new DefaultCellEditor(combo){
//					
//				};
//				editor.setClickCountToStart(2);
//				model.getColumn(i).setCellEditor(editor);

				
				//default jcombo
//				combo = new JComboBox(projectCodes);
//				editor = new DefaultCellEditor(combo){
//					
//				};
//				editor.setClickCountToStart(2);
//				model.getColumn(i).setCellEditor(editor);
				break;
			case nteam_idx:
				
				
//				combo = new JComboBox(teamNameArray);
//				combo.setEditable(true);
//				agent = new ComboAgent(combo);
//				editor = new DefaultCellEditor(combo){
//					
//				};
//				editor.setClickCountToStart(2);
//				model.getColumn(i).setCellEditor(editor);
				
				f = new JTextField(30);
				model.getColumn(i).setCellEditor(new DefaultCellEditor(f));
				autoSuggestor = new AutoSuggestor(f, this.nmcdDialog, table, nmcdDialog.getTeamList(), Color.WHITE.brighter(), Color.BLUE, Color.RED, 0.75f) {
					 @Override
					 boolean wordTyped(String typedWord) {
						 return super.wordTyped(typedWord);//now call super to check for any matches against newest dictionary
					 }
				};
				
			default:
			}
		}
		panel.add("unbound.bind.center.center", new JScrollPane(table));

		return panel;
	}
	
	class ComboAgent extends KeyAdapter {
        JComboBox jcombo;
        JTextField jeditor;
        String newStr;
        
        public ComboAgent(JComboBox box)
        {
        	jcombo = box;
        	jeditor = (JTextField)jcombo.getEditor().getEditorComponent();
        	jeditor.addKeyListener(this);
        	jeditor.addFocusListener(new FocusListener());
        }
        
        public void keyPressed(KeyEvent e) {
        	if (e.getKeyCode() == KeyEvent.VK_ENTER) {
        		jeditor.setText(newStr);
        		jcombo.setSelectedItem(newStr);
        	}
        }
        
        public void keyReleased(KeyEvent e) {
            char ch = e.getKeyChar();
            
//            if(ch == KeyEvent.CHAR_UNDEFINED || Character.isISOControl(ch))
//                return;
            
            int pos = jeditor.getCaretPosition();
            String str = jeditor.getText();
            int slen = str.length();
            byte[] b = str.getBytes();
            int blen = b.length;
            
            if(str.length() == 0)
                return;
            
            boolean search = false;
            for(int k=0; k<jcombo.getItemCount(); k++) {
                String iteml = jcombo.getItemAt(k).toString().toLowerCase();
                String item = jcombo.getItemAt(k).toString();
                // 조건 비교.. 입력한 문자열이 리스트에 있는 아이템의 첫머리로 일치하는지..
                if( item.startsWith(str) || iteml.startsWith(str) ||
                    item.startsWith(str.toLowerCase()) || iteml.startsWith(str.toLowerCase()) ) {
                    // 일치한다면 field에 매치된 아이템을 셋팅하고
                    // 자동으로 완성된 부분을 선택표시로 하여 강조한다.
                	newStr = item;
                    jeditor.setText(item);
                    //커서 위치를 자동완성문자열 다음으로....
                    jeditor.setCaretPosition(item.length());
                    //자동완성부분 표시하는 역할.. 한글일때는 pos위치가 하나 앞이고 마지막 글자가 한번 더 출력됨.
                    jeditor.moveCaretPosition(pos); 
                    search = true;
                    break;
                }
            }
            if(!search){
            	newStr = "";
            }
            
        }
        
        class FocusListener extends  FocusAdapter{
    		public void focusLost(FocusEvent paramFocusEvent) {
    			jeditor.setText(newStr);
        		jcombo.setSelectedItem(newStr);
    		}
    	}
        
    }
	
	
	

//	private void nmcdUpdate(){
//		try{
//			int[] rows = table.getSelectedRows();
//			int ct = table.getSelectedRowCount();
//			String nmcd = comboNmcd.getSelectedItem().toString();
//			for(int i=0 ; i<ct ; i++){
//				tableModel.setValueAt(nmcd, rows[i], nmcd_idx);
//			}
//		} catch (Exception e){
//			e.printStackTrace();
//		}
//	}
	
//	private JPanel saveButtonPanel() throws Exception{
//		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
//		
////		JLabel lblProduct = new JLabel("NMCD");
////		lblProduct.setHorizontalAlignment(SwingConstants.CENTER);
////		buttonPanel.add(lblProduct);
////
////		comboNmcd = new JComboBox<String>(nmcdValues);
////		buttonPanel.add(comboNmcd);
////		
////		applyButton = new JButton("일괄적용");
////		applyButton.addActionListener(new ActionListener() {
////			public void actionPerformed(ActionEvent actionevent) {
//////				applyButton.setEnabled(false);
////				nmcdUpdate();
//////				applyButton.setEnabled(true);
////			}
////		});
////
////		buttonPanel.add(applyButton);
//		
//		saveButton = new JButton("Save");
//		saveButton.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent actionevent) {
//				saveButton.setEnabled(false);
//				save();
//			}
//		});
//
//		buttonPanel.add(saveButton);
//
//		return buttonPanel;
//		
//	}
	
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
		
		//function Combo
		JLabel lblfunction = new JLabel("function");
		lblfunction.setHorizontalAlignment(SwingConstants.CENTER);
		buttonPanel.add(lblfunction);

		comboFunction = new JComboBox();
		comboFunction.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent paramItemEvent) {
				Object selectItem = ((JComboBox) paramItemEvent.getSource()).getSelectedItem();

				if (!paramItemEvent.getItem().equals(selectItem) && selectItem != null) {
					functionChangeAction(selectItem.toString());
				}
			}
		});
		buttonPanel.add(comboFunction);
		
		//fmp Combo
		JLabel lblfmp = new JLabel("FMP");
		lblfmp.setHorizontalAlignment(SwingConstants.CENTER);
		buttonPanel.add(lblfmp);

		comboFmp = new JComboBox();
		comboFmp.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent paramItemEvent) {
				Object selectItem = ((JComboBox) paramItemEvent.getSource()).getSelectedItem();

				if (!paramItemEvent.getItem().equals(selectItem) && selectItem != null) {
					fmpChangeAction(selectItem.toString());
				}
			}
		});
		buttonPanel.add(comboFmp);
		
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
		
		saveButton = new JButton("Save");
		saveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionevent) {
				saveButton.setEnabled(false);
				save();
			}
		});

		buttonPanel.add(saveButton);

		return buttonPanel;
	}
	
	protected void productChangeAction(String selectedProduct) {
		try {
//			if (selectedProduct == null || selectedProduct.equals("")) {
				comboFunction.removeAllItems();
				comboFunction.addItem("");
				comboFmp.removeAllItems();
				functionMap.clear();
				productId = "";
				functionId = "";
				functionRevId = "";
				fmpId = "";
				fmpRevId = "";
				tableModel.setNumRows(0);
//				return;
//			}
				
			if(selectedProduct.equals("")){
				return;
			}
			TCComponentItem prodItem = productMap.get(selectedProduct);
			TCComponentItemRevision prodRev = prodItem.getLatestItemRevision();;
			productId = prodItem.getStringProperty(PropertyConstant.ATTR_NAME_ITEMID);
			String prdRevId = prodRev.getStringProperty(PropertyConstant.ATTR_NAME_ITEMREVID);
			
			ArrayList<HashMap> resultList = null;
			SYMCRemoteUtil remoteQuery = new SYMCRemoteUtil();
			DataSet ds = new DataSet();
			ds.put("ID", productId);
			ds.put("REV", prdRevId);
			ds.put("TYPE", "S7_FunctionRevision");
			resultList = (ArrayList<HashMap>) remoteQuery.execute(COMMONPARTCHECK_QUERY_SERVICE, "getFunctionList", ds);
			if(resultList != null && resultList.size() > 0){
				int resultListSize = resultList.size();
				String id = "";
				String name = "";
				String function = "";
				String rev = "";
				for (int i = 0; i < resultListSize; i++) {
					HashMap rowHash  = resultList.get(i);
					id = (String)rowHash.get("ID");
					name = (String)rowHash.get("NAME");
					rev = id + ";" +(String)rowHash.get("REV");
					function = id+" - "+name;
					if (!functionMap.containsKey(function)){
						functionMap.put(function, rev);
						comboFunction.addItem(function);
					}
				}
			}
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	protected void functionChangeAction(String selectedFunction) {
		try {
//			if (selectedProduct == null || selectedProduct.equals("")) {
				comboFmp.removeAllItems();
				comboFmp.addItem("");
				fmpMap.clear();
				functionId = "";
				functionRevId = "";
				fmpId = "";
				fmpRevId = "";
				tableModel.setNumRows(0);
//				return;
//			}
				if(selectedFunction.equals("")){
					return;
				}
				String value[] = functionMap.get(selectedFunction).split(";",2);
				functionId = value[0];
				functionRevId = value[1];
//				functionId = functionMap.get(selectedFunction);
//				TCComponent[] fItems = CustomUtil.queryComponent("Item...", new String[]{"ItemID"}, new String[]{functionId});
//				TCComponentItem funcItem = (TCComponentItem)fItems[0];
//				TCComponentItemRevision funcRev = funcItem.getLatestItemRevision();;
//				functionRevId = funcRev.getStringProperty(PropertyConstant.ATTR_NAME_ITEMREVID);
				
				ArrayList<HashMap> resultList = null;
				SYMCRemoteUtil remoteQuery = new SYMCRemoteUtil();
				DataSet ds = new DataSet();
				ds.put("ID", functionId);
				ds.put("REV", functionRevId);
				ds.put("TYPE", "S7_FunctionMastRevision");
				resultList = (ArrayList<HashMap>) remoteQuery.execute(COMMONPARTCHECK_QUERY_SERVICE, "getFunctionList", ds);
				if(resultList != null && resultList.size() > 0){
					int resultListSize = resultList.size();
					String id = "";
					String name = "";
					String fmp = "";
					String rev = "";
					for (int i = 0; i < resultListSize; i++) {
						HashMap rowHash  = resultList.get(i);
						id = (String)rowHash.get("ID");
						name = (String)rowHash.get("NAME");
						fmp = id+" - "+name;
						rev = id + ";" +(String)rowHash.get("REV");
						if (!fmpMap.containsKey(fmp)){
							fmpMap.put(fmp, rev);
							comboFmp.addItem(fmp);
						}
					}
				}
			
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	protected void fmpChangeAction(String selectedFmp) {
		try {
			fmpId = "";
			fmpRevId = "";
			if(selectedFmp.equals("")){
				tableModel.setNumRows(0);
				return;
			}
			String value[] = fmpMap.get(selectedFmp).split(";",2);
			fmpId = value[0];
			fmpRevId = value[1];
			search();
//			fmpId = fmpMap.get(selectedFmp);
//			TCComponent[] fItems = CustomUtil.queryComponent("Item...", new String[]{"ItemID"}, new String[]{fmpId});
//			TCComponentItem fmpItem = (TCComponentItem)fItems[0];
//			TCComponentItemRevision fmpRev = fmpItem.getLatestItemRevision();;
//			fmpRevId = fmpRev.getStringProperty(PropertyConstant.ATTR_NAME_ITEMREVID);
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
		private String searchId = "";
		private String searchRev = "";
		
		public SearchOperation(String searchId, String searchRev) {
			this.searchId = searchId;
			this.searchRev = searchRev;
		}

		@Override
		public void executeOperation() throws Exception {
			WaitProgressBar progressBar = new WaitProgressBar(AIFUtility.getActiveDesktop());
			progressBar.setWindowSize(500, 300);
			progressBar.start();
			progressBar.setStatus("BOM Load Start...."	, true);
			  
			try {
				ArrayList<HashMap> resultList = null;
				SYMCRemoteUtil remoteQuery = new SYMCRemoteUtil();
				DataSet ds = new DataSet();
				ds.put("ID", searchId);
				ds.put("REV", searchRev);
				ds.put("PRD_NO", productId);
				ds.put("FUNC_NO", functionId);
				resultList = (ArrayList<HashMap>) remoteQuery.execute(COMMONPARTCHECK_QUERY_SERVICE, "getChildList", ds);
				if(resultList != null && resultList.size() > 0){
					int resultListSize = resultList.size();
					String pid = "";
					String cid = "";
					String cname = "";
					//20201217 CF-1708 seho Weight 관리 불러오기
					String weightmngt = "";
					String nmcd = "";
					String lev = "";
					String seq = "";
					String key = "";
					String group = "";
					String user = "";
					String smode = "";
					String udate = "";
					String pcode = "";
					String nteam = "";
//					String inputValue = "";
					String[] inputValues = null;
					for (int i = 0; i < resultListSize; i++) {
						HashMap rowHash  = resultList.get(i);
						pid = (String)rowHash.get("PID");
						cid = (String)rowHash.get("CID");
						cname = (String)rowHash.get("CNAME");
						nmcd = (String)rowHash.get("NMCD");
						lev = rowHash.get("LEV").toString();
						if(fmpId.equals("")){
							lev = (Integer.parseInt(lev) -1) + "";
						}
						seq = (String)rowHash.get("MAXSEQ");
						group = (String)rowHash.get("GRP");
						user = (String)rowHash.get("OWNR");
						smode = (String)rowHash.get("SMODE");
						udate = (String)rowHash.get("UDATE");
						pcode = (String)rowHash.get("PCODE");
						nteam = (String)rowHash.get("NTEAM");
						//20201217 CF-1708 seho Weight 관리 불러오기
						weightmngt = (String)rowHash.get("WEIGHTMNGT");
						if(weightmngt == null){
							weightmngt = "";
						}

						if(nmcd == null){
							nmcd = "";
						}
						if(pcode == null){
							pcode = "";
						}
						if(nteam == null){
							nteam = "";
						}
						
						//column = {"No", "Function", "Parent P/No", "Seq", "Lev", "Part No", "Part Name", "NMCD", "Group", "Owner"};
						//20201217 CF-1708 seho Weight 관리 불러오기
						tableModel.addRow(new String[]{(i+1)+"", functionId, pid, seq, lev, cid, cname, weightmngt, nmcd, smode, pcode, nteam, group, user, udate});
						key = productId+";"+functionId+";"+pid+";"+cid;
						if(!oldMap.containsKey(key)){
//							inputValue = nmcd + "|"+pcode + "|"+nteam;
//							oldMap.put(key, nmcd);
							inputValues = new String[4];
							inputValues[0] = nmcd;
							inputValues[1] = pcode;
							inputValues[2] = nteam;
							//20201217 CF-1708 seho Weight 관리 불러오기
							inputValues[3] = weightmngt;
							oldMap.put(key, inputValues);
						}
						progressBar.setStatus("  Loding... " + (i+1)+ " / "+resultListSize, true);
					}
					progressBar.close();
				} else {
	//				tableModel.addRow(new String[]{productId, functionId, "", "", "조회 결과가 없습니다.", ""});
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
//		searchButton.setVisible(false);
		
		try {
			tableModel.setNumRows(0);
			String searchId = "";
			String searchRev = "";
			oldMap.clear();
			
			if(fmpId.equals("")){
				searchId = functionId;
				searchRev = functionRevId;
			} else {
				searchId = fmpId;
				searchRev = fmpRevId;
			}
			if(searchId.equals("")){
				MessageBox.post(nmcdDialog, "Function이나 FMP를 선택하십시오.", "Search", MessageBox.INFORMATION);
				searchButton.setEnabled(true);
	            return;
			}
			
			SearchOperation op = new SearchOperation(searchId, searchRev);
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
	
	public class SaveOperation extends AbstractAIFOperation {
		
		public SaveOperation() {
		}

		@Override
		public void executeOperation() throws Exception {
			WaitProgressBar progressBar = new WaitProgressBar(AIFUtility.getActiveDesktop());
			progressBar.setWindowSize(500, 300);
			progressBar.start();
			progressBar.setAlwaysOnTop(true);
			progressBar.setStatus("Update Start...."	, true);
			  
			try {
				int insertCount = insertMap.size();
				Set<String> insertSet = insertMap.keySet();
				Iterator<String> inIt = insertSet.iterator();
				//20201217 CF-1708 seho Weight 관리 저장
				String weightmngt = "";
				String nmcd = "";
				String prdNo = "";
				String functionNo = "";
				String parentNo = "";
				String partNo = "";
				String[] keyValues = null;
				String pcode = "";
				String nteam = "";
				String[] values = null;
				String[] oldValues = null;
				String value = "";
				String oldValue = "";
				SYMCRemoteUtil remoteQuery = new SYMCRemoteUtil();
				int i = 0;
				
				//insert
				while (inIt.hasNext()) {
					i++;
					String key = inIt.next();
					values = insertMap.get(key);
					nmcd = values[0];
					pcode = values[1];
					nteam = values[2];
					//20201217 CF-1708 seho Weight 관리 저장
					weightmngt = values[3];
					value = nmcd + "|" + pcode + "|" + nteam;
					keyValues = key.split(";",4);
					prdNo = keyValues[0].trim();
					functionNo = keyValues[1].trim();
					parentNo = keyValues[2].trim();
					partNo = keyValues[3].trim();
					
					DataSet ds = new DataSet();
			        ds.put("PRDNO", prdNo);
			        ds.put("FUNCNO", functionNo);
			        ds.put("PARENTNO", parentNo);
			        ds.put("PARTNO", partNo);
			        ds.put("NMCD", nmcd);
			        ds.put("PCODE", pcode);
			        ds.put("NTEAM", nteam);
			      //20201217 CF-1708 seho Weight 관리 저장
			        ds.put("WEIGHTMNGT", weightmngt);
			        progressBar.setStatus("  Insert... (" + i+ " / "+insertCount + ") " + partNo + " : " + value, true);
					remoteQuery.execute(COMMONPARTCHECK_QUERY_SERVICE, "insertNmcd", ds);
					if(oldMap.containsKey(key)){
						oldMap.remove(key);
						oldMap.put(key, values);
					}
				}
				
				//update
				i = 0;
				int updateCount = updateMap.size();
				Set<String> updateSet = updateMap.keySet();
				Iterator<String> upIt = updateSet.iterator();
				
				while (upIt.hasNext()) {
					i++;
					String key = upIt.next();
					values = updateMap.get(key);
					nmcd = values[0];
					pcode = values[1];
					nteam = values[2];
					//20201217 CF-1708 seho Weight 관리 저장
					weightmngt = values[3];
					value = nmcd + "|" + pcode + "|" + nteam + "|" + weightmngt;
					oldValues = oldMap.get(key);
					oldValue = oldValues[0] + "|" + oldValues[1] + "|" + oldValues[2] + "|" + oldValues[3];
					keyValues = key.split(";",4);
					prdNo = keyValues[0].trim();
					functionNo = keyValues[1].trim();
					parentNo = keyValues[2].trim();
					partNo = keyValues[3].trim();
					
					DataSet ds = new DataSet();
					ds.put("PRDNO", prdNo);
			        ds.put("FUNCNO", functionNo);
			        ds.put("PARENTNO", parentNo);
			        ds.put("PARTNO", partNo);
			        ds.put("NMCD", nmcd);
			        ds.put("PCODE", pcode);
			        ds.put("NTEAM", nteam);
				      //20201217 CF-1708 seho Weight 관리 저장
			        ds.put("WEIGHTMNGT", weightmngt);
			        
			        progressBar.setStatus("  Update... (" + i+ " / "+updateCount + ") " + partNo + " : " + oldValue + " > " + value, true);
					remoteQuery.execute(COMMONPARTCHECK_QUERY_SERVICE, "updateNmcd", ds);
					if(oldMap.containsKey(key)){
						oldMap.remove(key);
						oldMap.put(key, values);
					}
				}
				
				//delete
				i = 0;
				int deleteCount = deleteMap.size();
				Set<String> deleteSet = deleteMap.keySet();
				Iterator<String> delIt = deleteSet.iterator();
				
				while (delIt.hasNext()) {
					i++;
					String key = delIt.next();
					values = deleteMap.get(key);
					keyValues = key.split(";",4);
					prdNo = keyValues[0].trim();
					functionNo = keyValues[1].trim();
					parentNo = keyValues[2].trim();
					partNo = keyValues[3].trim();
					
					DataSet ds = new DataSet();
					ds.put("PRDNO", prdNo);
			        ds.put("FUNCNO", functionNo);
			        ds.put("PARENTNO", parentNo);
			        ds.put("PARTNO", partNo);
			        
			        progressBar.setStatus("  Delete... (" + i+ " / "+deleteCount + ") " + partNo, true);
					remoteQuery.execute(COMMONPARTCHECK_QUERY_SERVICE, "deleteNmcd", ds);
					if(oldMap.containsKey(key)){
						oldMap.remove(key);
						oldMap.put(key, values);
					}
				}
				progressBar.setStatus("Update End...."	, true);
				progressBar.close("Update End", false);
			
			} catch (Exception e) {
				progressBar.setStatus(e.getMessage(), true);
				progressBar.close("오류 발생", false);
			} finally {
				saveButton.setEnabled(true);
			}
		}
	}
	
	private void save(){
		try {
			if( tableModel != null ){
				insertMap.clear();
				updateMap.clear();
				deleteMap.clear();
				int countRow = tableModel.getRowCount();
//				String prodId = "";
//				String functionId = "";
				String pid = "";
				String cid = "";
				//20201217 CF-1708 seho Weight 관리 저장
				String weight_management = "";
				String nmcd = "";
				String key = "";
				String[] oldValues = null;
				String[] newValues = null;
				String oldValue = "";
				String newValue = "";
				String pcode = "";
				String nteam = "";
				for( int row=0; row < countRow; row++) {
//					prodId = (String)tableModel.getValueAt(row, 1);
//					functionId = (String)tableModel.getValueAt(row, 2);
					pid = (String)tableModel.getValueAt(row, parent_idx);
					cid = (String)tableModel.getValueAt(row, part_idx);
					//20201217 CF-1708 seho Weight 관리 저장
					weight_management = (String)tableModel.getValueAt(row, weightmngt_idx);
					nmcd = (String)tableModel.getValueAt(row, nmcd_idx);
					key = productId+";"+functionId+";"+pid+";"+cid;
					pcode = (String)tableModel.getValueAt(row, pcode_idx);
					nteam = (String)tableModel.getValueAt(row, nteam_idx);
					//20201217 CF-1708 seho Weight 관리 저장 아래는 비교로직인듯 한데... 이전버전과 비교해서 알아서 보길.
					newValue = nmcd + "|" + pcode + "|" + nteam + "|" + weight_management;
					newValues = new String[4];
					newValues[0] = nmcd;
					newValues[1] = pcode;
					newValues[2] = nteam;
					newValues[3] = weight_management;

					if(oldMap.containsKey(key)){
						oldValues = oldMap.get(key);
						oldValue = oldValues[0] + "|" + oldValues[1] + "|" + oldValues[2] + "|" + oldValues[3];
						//db에 nmcd가 없고
						if(oldValue.equals("|||")){
							//table에 nmcd가 있으면 insert
							if(!oldValue.equals(newValue)){
								if(!insertMap.containsKey(key)){
									insertMap.put(key, newValues);
								}
							}
						//db에 nmcd가 있고
						} else {
							//table의 nmcd 값과 다르면
							if(!oldValue.equals(newValue)){
								//table의 nmcd값이 null 이면 delete
								if(newValue.equals("|||")){
									if(!deleteMap.containsKey(key)){
										deleteMap.put(key, newValues);
									}
								//table의 nmcd값이 null이 아니면 update
								} else {
									if(!updateMap.containsKey(key)){
										updateMap.put(key, newValues);
									}
								}
								
							}
						}
						
					}
				}
				
				int updateCount = updateMap.size();
				int insertCount = insertMap.size();
				int deleteCount = deleteMap.size();
				if(insertCount == 0 && updateCount == 0 && deleteCount == 0){
					MessageBox.post(nmcdDialog, "변경 사항이 없습니다..", "Save", MessageBox.INFORMATION);
					saveButton.setEnabled(true);
					return;
				}
				
				if(!validation()){
					StringBuffer Result = new StringBuffer();
					if(!nmcdValidationResult.toString().equals("")){
						Result.append("※ NMCD를 확인하세요\n");
						Result.append(nmcdValidationResult.toString());
//						MessageBox.post(nmcdDialog, prjValidationResult.toString(), "M Project Code를 확인하세요", MessageBox.INFORMATION);
					}
					if(!prjValidationResult.toString().equals("")){
						Result.append("※ M Project Code를 확인하세요\n");
						Result.append(prjValidationResult.toString());
//						MessageBox.post(nmcdDialog, prjValidationResult.toString(), "M Project Code를 확인하세요", MessageBox.INFORMATION);
					}
					if(!teamValidationResult.toString().equals("")){
						Result.append("※ New Team 명을 확인하세요\n");
						Result.append(teamValidationResult.toString());
//						MessageBox.post(nmcdDialog, teamValidationResult.toString(), "New Team 명을 확인하세요", MessageBox.INFORMATION);
					}
					MessageBox.post(nmcdDialog, Result.toString(), "입력값이 올바르지 않습니다.", MessageBox.INFORMATION);
					saveButton.setEnabled(true);
					return;
				}
				
				SaveOperation op = new SaveOperation();
				op.addOperationListener(new InterfaceAIFOperationListener() {
					@Override
					public void startOperation(String arg0) {
					}
				
					@Override
					public void endOperation() {
						search();
					}
				});
				session.queueOperation(op,true);
				
			}
		} catch (Exception ex) {
			saveButton.setEnabled(true);
			ex.printStackTrace();
		}
	}
	
	private boolean validation() {
		
		boolean result = true;
		teamValidationResult = new StringBuffer();
		prjValidationResult = new StringBuffer();
		nmcdValidationResult = new StringBuffer();
		List teamList = nmcdDialog.getTeamList();
		//projectValueList
		List<String> teamNames = new ArrayList<String>();
		List<String> prjNames = new ArrayList<String>();
		List<String> nmcdNames = new ArrayList<String>();
		
		for (String partKey : insertMap.keySet()) {
			String[] values = insertMap.get(partKey);
			String newTeam = values[2];
			if(!newTeam.equals("") && !teamList.contains(newTeam)){
				if(!teamNames.contains(newTeam)){
					teamNames.add(newTeam);
				}
				result = false;
			}
			
			String prjCode = values[1];
			if(!prjCode.equals("") && !projectValueList.contains(prjCode)){
				if(!prjNames.contains(prjCode)){
					prjNames.add(prjCode);
				}
				result = false;
			}
			
			String nmcdCode = values[0];
			if(!nmcdCode.equals("") && !nmcdValueList.contains(nmcdCode)){
				if(!nmcdNames.contains(nmcdCode)){
					nmcdNames.add(nmcdCode);
				}
				result = false;
			}
		}
		
		for (String partKey : updateMap.keySet()) {
			String[] values = updateMap.get(partKey);
			String newTeam = values[2];
			if(!newTeam.equals("") && !teamList.contains(newTeam)){
				if(!teamNames.contains(newTeam)){
					teamNames.add(newTeam);
				}
				result = false;
			}
			String prjCode = values[1];
			if(!prjCode.equals("") && !projectValueList.contains(prjCode)){
				if(!prjNames.contains(prjCode)){
					prjNames.add(prjCode);
				}
				result = false;
			}
			String nmcdCode = values[0];
			if(!nmcdCode.equals("") && !nmcdValueList.contains(nmcdCode)){
				if(!nmcdNames.contains(nmcdCode)){
					nmcdNames.add(nmcdCode);
				}
				result = false;
			}
		}
		
		for(String team : teamNames){
			teamValidationResult.append("   " +team + "\n");
		}
		
		for(String prj : prjNames){
			prjValidationResult.append("   " +prj + "\n");
		}
		
		for(String nmcd : nmcdNames){
			nmcdValidationResult.append("   " +nmcd + "\n");
		}
		
		return result;
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
		File defaultFile = new File(productId + "_"+ functionId + "_" +"NMCD" + sdf.format(now.getTime()) + ".xls");
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
		
		AIFShell aif = new AIFShell("application/vnd.ms-excel", fileChooser.getSelectedFile().getAbsolutePath());
        aif.start();
        //		MessageBox.post(nmcdDialog, "Excel 내보내기가 완료되었습니다.\n 파일경로 : "+fileChooser.getSelectedFile().getAbsolutePath(), "Export", MessageBox.INFORMATION);
	}
	
	class TableKeyListener extends  KeyAdapter{
		public void keyPressed(KeyEvent e) {
			if (e.isControlDown()) {
	            if (e.getKeyCode() == KeyEvent.VK_C) {
	            	int selectedIndex = table.getSelectedColumn();
	    			//이벤트가 발생할 Cell 지정
	    			if(selectedIndex == nmcd_idx || selectedIndex == pcode_idx || selectedIndex == nteam_idx || selectedIndex == weightmngt_idx) {
	    				copy();
	    			}
	            }
	            if (e.getKeyCode() == KeyEvent.VK_V) {
	            	int selectedIndex = table.getSelectedColumn();
	            	if(selectedIndex == nmcd_idx || selectedIndex == pcode_idx || selectedIndex == nteam_idx || selectedIndex == weightmngt_idx) {
	            		paste();
	            	}
	            }
			}
        }
        
        public void keyReleased(KeyEvent e) {
        }
	}


//마우스 이벤트 발생 리스너
	class TableMouseListener extends  MouseAdapter{

		public void mouseClicked(MouseEvent event) {
			
			if(table.getSelectedRowCount() <= 1 && table.getSelectedColumnCount() <= 1){
			
				 int column = table.columnAtPoint(event.getPoint());
				 int row = table.rowAtPoint(event.getPoint());
	
				 table.changeSelection(row, column, false, false);
			}

			   
			int selectedIndex = table.getSelectedColumn();
			//이벤트가 발생할 Cell 지정
			if(selectedIndex == weightmngt_idx || selectedIndex == nmcd_idx || selectedIndex == pcode_idx || selectedIndex == nteam_idx) {
				   
				//이벤트 발생 소스 가져오기
				JTable jt = (JTable)event.getSource();
				if (SwingUtilities.isRightMouseButton(event) && event.getClickCount() == 1) {
					
					TablePopup tablePopup = new TablePopup();
					tablePopup.show(table, event.getX(), event.getY());
				}
			}
			   
		}
	}

	//20190522 Copy & Paste 기능 추가
	class TablePopup extends JPopupMenu {
		JMenuItem menuPaste = null;
		JMenuItem menuParentInput = null;
		JMenuItem menuGenerateSeq = null;
		JMenuItem menuCarryOver = null;
		
		public TablePopup() {
	
			JMenuItem menuCopyRow = new JMenuItem("Copy Cell", null);
			menuCopyRow.addActionListener(new ActionListener() {
	
				@Override
				public void actionPerformed(ActionEvent actionevent) {
					new CopyAction().actionPerformed(null);
				}
			});
			add(menuCopyRow);
	
			menuPaste = new JMenuItem("Paste Cell", null);
			menuPaste.addActionListener(new ActionListener() {
	
				@Override
				public void actionPerformed(ActionEvent actionevent) {
					new PasteAction().actionPerformed(null);
				}
			});
			add(menuPaste);
			
		}
	}
	
	class CopyAction extends AbstractAction {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			// Auto-generated method stub
			copy();
		}
	}

	class PasteAction extends AbstractAction {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			// Auto-generated method stub
			paste();
		}
	}

	private void copy() {
		JTextTransfer textTransfer = new JTextTransfer();
		
		copiedColumns = table.getSelectedColumns();
		Arrays.sort(copiedColumns);
		copiedRows = table.getSelectedRows();
		Arrays.sort(copiedRows);
		
		int preColumn = -1;
		String contents = null;
		for( int i = 0; i < copiedRows.length; i++){
			String rowContents = "";
			for( int j = 0; j < copiedColumns.length; j++){
				String str = table.getValueAt(copiedRows[i], copiedColumns[j]).toString();
				if( j > 0){
					while( preColumn < copiedColumns[j]){
						rowContents += "\t";
						preColumn ++;
					}
					
					rowContents += str;
				}else{
					rowContents = str;
					preColumn = copiedColumns[j];
				}
			}
			if( contents == null){
				contents = rowContents;
			}else{
				contents += "\n" + rowContents;
			}
		}
		//System.out.println(contents);
		textTransfer.setClipboardContents(contents);
		
		table.repaint();
	}
	
	private void paste() {
		
	//	if( !parentDlg.isEditable()){
	//		return;
	//	}
	//	
		int selectedColumn = table.getSelectedColumn();
		int selectedRow = table.getSelectedRow();
		
	//	if (copiedRows == null || copiedRows.length == 0) {
	//		return;
	//	}M1	P+	Y200	구매원가/개발팀M1	P+	Y200	구매원가/개발팀
	
		for (int row = 0; copiedRows != null && row < copiedRows.length; row++) {
			for (int column = 0; copiedColumns != null && column < copiedColumns.length; column++) {
				if (selectedRow + row >= table.getRowCount()) {
					System.out.println("Out of Row Index!!!!!!");
					return;
				}
	
				if (selectedColumn + column >= table.getColumnCount()) {
					System.out.println("Out of Column Index!!!!!!");
					return;
				}
			}
		}
	
//		ArrayList<Integer> rowList = new ArrayList();
//		ArrayList<Integer> columnList = new ArrayList();
//		
//		if( copiedRows != null && copiedColumns != null){
//			if( copiedRows.length == 1 && copiedColumns.length == 1){
//				int[] selectedRows = table.getSelectedRows();
//				int[] selectedColumns = table.getSelectedColumns();
//				if( selectedRows.length > 0 && selectedColumns.length > 0){
//					
//					Object obj = table.getValueAt(copiedRows[0],copiedColumns[0]);
//					for( int i = 0; i < selectedRows.length; i++){
//						for( int j = 0; j < selectedColumns.length; j++){
//							
//							int modelColumn = table.convertColumnIndexToModel(selectedColumns[j]);
//							if( notEditableColumn.contains(modelColumn)){
//								continue;
//							}
//							
//							if (obj instanceof JCellValue) {
//								JCellValue cellValue = (JCellValue) obj;
//								obj = new JCellValue(cellValue.getValue());
//								// cellValue.setSortValue(cellValue.getValue());
//							} else if (obj instanceof String) {
//								obj = new JCellValue(obj.toString(), obj.toString(), 0);
//							}
//							table.setValueAt(obj, selectedRows[i], selectedColumns[j]);
//			
//							rowList.add(selectedRows[i]);
//							columnList.add(selectedColumns[j]);
//						}
//					}
//				}
//			}else{
//				for (int row = 0; row < copiedRows.length; row++) {
//					for (int column = 0; column < copiedColumns.length; column++) {
//						
//						int modelColumn = table.convertColumnIndexToModel(selectedColumn + column);
//						if( notEditableColumn.contains(modelColumn)){
//							continue;
//						}
//						
//						Object obj = table.getValueAt(copiedRows[row],
//								copiedColumns[column]);
//						if (obj instanceof JCellValue) {
//							JCellValue cellValue = (JCellValue) obj;
//							obj = new JCellValue(cellValue.getValue());
//							// cellValue.setSortValue(cellValue.getValue());
//						} else if (obj instanceof String) {
//							obj = new JCellValue(obj.toString(), obj.toString(), 0);
//						}
//						table.setValueAt(obj, selectedRow + row, selectedColumn + column);
//		
//						rowList.add(selectedRow + row);
//						columnList.add(selectedColumn + column);
//		
//					}
//				}	
//				
//				if (rowList.size() > 0) {
//					table.setRowSelectionInterval(rowList.get(0),rowList.get(rowList.size() - 1));
//				}
//				if (columnList.size() > 0) {
//					table.setColumnSelectionInterval(columnList.get(0),columnList.get(columnList.size() - 1));
//				}
//			}
//		}
		
		
//		JTextTransfer textTransfer = new JTextTransfer();
//		String contents = textTransfer.getClipboardContents();
//		if( contents != null && !contents.equals("")){
//			String[] contentsArray = contents.split("\n");
//			for( int i = 0; i < contentsArray.length; i++){
//				String[] rowContents = contentsArray[i].split("\t");
//				for( int j = 0; j < rowContents.length; j++){
//					if( selectedRow + i >= table.getRowCount()){
//						continue;
//					}
//					
//					int modelColumn = table.convertColumnIndexToModel(selectedColumn + j);
//					if( notEditableColumn.contains(modelColumn)){
//						continue;
//					}
//					
//					if( (selectedColumn + j >= table.getColumnCount())){
//						continue;
//					}
//					
//					table.setValueAt(rowContents[j], selectedRow + i, selectedColumn + j);
//				}
//			}
//			textTransfer.setClipboardContents(null);
//			table.repaint();
//			return;
//		}
		
		JTextTransfer textTransfer = new JTextTransfer();
		String contents = textTransfer.getClipboardContents();
		int[] selectedRows = table.getSelectedRows();
		int[] selectedColumns = table.getSelectedColumns();
//		if( contents != null && !contents.equals("")){
		if( contents != null){
			String[] contentsArray = contents.split("\n", copiedRows.length);
			
			for(int sr = 0; sr < selectedRows.length; sr++){
				selectedRow = selectedRows[sr];
				selectedColumn = selectedColumns[0];
				
				for( int i = 0; i < contentsArray.length; i++){
					String[] rowContents = contentsArray[i].split("\t", copiedColumns.length);
					for( int j = 0; j < rowContents.length; j++){
						if( selectedRow + i >= table.getRowCount()){
							continue;
						}
						
						int modelColumn = table.convertColumnIndexToModel(selectedColumn + j);
						if( notEditableColumn.contains(modelColumn)){
							continue;
						}
						
						if( (selectedColumn + j >= table.getColumnCount())){
							continue;
						}
						
						table.setValueAt(rowContents[j], selectedRow + i, selectedColumn + j);
//						System.out.println(rowContents[j] + " : " + (selectedRow + i) + " : " + (selectedColumn + j));
					}
				}
			
			}
			
//			textTransfer.setClipboardContents(null);
			table.repaint();
			return;
		}
		
		table.repaint();
	}
	
}

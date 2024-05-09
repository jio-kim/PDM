package com.kgm.commands.namegroup;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;

import com.kgm.commands.namegroup.model.PngCondition;
import com.kgm.commands.namegroup.model.PngMaster;
import com.kgm.common.remote.DataSet;
import com.kgm.common.remote.SYMCRemoteUtil;
import com.teamcenter.rac.aif.AbstractAIFDialog;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.util.MessageBox;

public class PngDlg extends AbstractAIFDialog {

	private final JPanel contentPanel = new JPanel();
	PngRegPanel nameGroupRegPanel = null;
	private PngAssignPanel assignPanel = null;
	PngVerificationPanel verificationPanel = null;
	//[CSH][SR181025-028]오류 리포터 예외처리
	private PngExceptionPanel exceptionPanel = null;
	private PngWeeklyErrorReportPanel weeklyErrorReportPanel = null;
	
	private ScriptEngineManager manager = new ScriptEngineManager();
    private ScriptEngine engine = manager.getEngineByName("js"); 
    static final String SELECT_PRODUCT = "Select a Product";
    HashMap<String, String> specMap = new HashMap();
	private JTabbedPane tabbedPane = null;
	private String rowkey = "";
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			PngDlg dialog = new PngDlg();
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
	public PngDlg() throws Exception {
		super(AIFUtility.getActiveDesktop().getFrame(), false);
		setBounds(100, 100, 965, 584);
		setTitle("Part Name Group Manager");
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));
		{
			tabbedPane = new JTabbedPane(JTabbedPane.TOP);
			tabbedPane.addChangeListener(new ChangeListener(){

				@Override
				public void stateChanged(ChangeEvent arg0) {
					// TODO Auto-generated method stub
					if( tabbedPane.getSelectedIndex() == 2){
						try {
							verificationPanel.refreshProductList();
							setRowkey();
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							MessageBox.post(PngDlg.this, e.getMessage(), "ERROR", MessageBox.ERROR);
						}
					}else if(tabbedPane.getSelectedIndex() == 4)
					{
						//Weekly Error Report Tab 클릭시 초기 데이터를 로드함
						weeklyErrorReportPanel.initLoadData();
					}
				}
				
			});
			
			nameGroupRegPanel = new PngRegPanel(this);
			tabbedPane.add("Registration", nameGroupRegPanel);
			
			assignPanel = new PngAssignPanel(this);
			tabbedPane.add("Assignment", assignPanel);
			
			verificationPanel = new PngVerificationPanel(this);
			tabbedPane.add("Verification", verificationPanel);
			
			exceptionPanel = new PngExceptionPanel(this);
			tabbedPane.add("Exception", exceptionPanel);
			
			weeklyErrorReportPanel = new PngWeeklyErrorReportPanel(this);
			tabbedPane.add("Weekly Error Report", weeklyErrorReportPanel);
			
			contentPanel.add(tabbedPane);
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton cancelButton = new JButton("Close");
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent actionevent) {
						dispose();
					}
				});
				buttonPane.add(cancelButton);
			}
		}
	}
	
	private void setRowkey(){
		if(rowkey == null || rowkey.equals("")){
			SYMCRemoteUtil remote = new SYMCRemoteUtil();
			DataSet ds = new DataSet();
	
			ds.put("PRODUCT", "");
	
			try {
				rowkey = (String) remote.execute("com.kgm.service.PartNameGroupService", "getRowKey", ds);
	
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public String getRowkey(){
		return rowkey;
	}

	public void addNewName(ArrayList<String> newNameList){
		nameGroupRegPanel.addNewName(newNameList);
	}
	
	public void removeAllRow(JTable table){
		DefaultTableModel model = (DefaultTableModel)table.getModel();
		
		for( int i = model.getRowCount() - 1; i >= 0 ; i--){
			model.removeRow(i);
		}
	}
	
	public ScriptEngine getEngine() {
		return engine;
	}
	
	public int getMaxNumber(){
		int maxNum = 0;
		DefaultTableModel model = (DefaultTableModel)nameGroupRegPanel.conditionTable.getModel();
		for( int i = 0; i < model.getRowCount(); i++){
			int num = (Integer)model.getValueAt(i, 0);
			if( num > maxNum){
				maxNum = num;
			}
		}
		
		return maxNum;
	}
	
	public PngCondition getSamePngCondition(String groupId, int groupNumber, String product, String conditionStr, String operator, int qty, boolean bTable) throws Exception{
		
		if( bTable){
			PngCondition pngCondition = null;
			
			DefaultTableModel model = (DefaultTableModel)nameGroupRegPanel.conditionTable.getModel();
			for( int i = 0; i < model.getRowCount(); i++){
				if( model.getValueAt(i, 0).equals(groupNumber) && model.getValueAt(i, 1).equals(product)
				&& model.getValueAt(i, 2).equals(conditionStr) && model.getValueAt(i, 3).equals(operator + " " + qty)){
					pngCondition = (PngCondition)model.getValueAt(i, 5);
					break;
				}
			}
			
			return pngCondition;
		}else{
			SYMCRemoteUtil remote = new SYMCRemoteUtil();
			DataSet ds = new DataSet();
			ds.put("GROUP_ID", groupId);
			ds.put("GROUP_NUM", groupNumber);
			ds.put("PRODUCT", product);
			ds.put("CONDITION", conditionStr);
			ds.put("OPERATOR", operator);
			ds.put("QTY", qty);
			try {
				
				ArrayList<HashMap<String, Object>> conditionList = (ArrayList<HashMap<String, Object>>)remote.execute("com.kgm.service.PartNameGroupService", "getPngConditionList", ds);
				
				if( conditionList == null){
					return null; 
				}
				//int groupNumber, String product, String condition, String operator, int quantity
				PngCondition condition = new PngCondition(groupNumber, product, conditionStr, operator, qty);
				ArrayList partNameList = new ArrayList();
				condition.setPartNameList(partNameList);
				for( HashMap map:conditionList){
					String partName = (String)map.get("PART_NAME");
					if( partName != null && !partNameList.contains(partName)){
						partNameList.add(partName);
					}
				}
				
				return condition;
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				throw e;
			}	
		}
	}
	
	public void addCondition(int groupNumber, String product, String conditionStr, String operator, int qty, String partName) throws Exception{
		PngCondition pngCondition = null;
		DefaultTableModel model = (DefaultTableModel)nameGroupRegPanel.conditionTable.getModel();
		DefaultTableModel pgModel = (DefaultTableModel)nameGroupRegPanel.partNameGroupTable.getModel();
		int idx = nameGroupRegPanel.partNameGroupTable.getSelectedRow();
		int modelIdx = nameGroupRegPanel.partNameGroupTable.convertRowIndexToModel(idx);
		String groupId = (String)pgModel.getValueAt(modelIdx, 0);
		pngCondition = getSamePngCondition(groupId, groupNumber, product, conditionStr, operator, qty, true);
		ArrayList<String> nameList = null;
		if( pngCondition == null){
			pngCondition = new PngCondition(groupNumber, product, conditionStr, operator, qty);
			nameList = new ArrayList();
			nameList.add(partName);
			pngCondition.setPartNameList(nameList);
			
			Vector row = new Vector();
			row.add(groupNumber);
			row.add(product);
			row.add(conditionStr);
			row.add(operator + " " + qty);
			
			row.add(nameList);
			//MultiLineTableCellRenderer 로 처리함.
			
			row.add(pngCondition);
			model.addRow(row);
			
		}else{
			nameList = pngCondition.getPartNameList();
			if( nameList == null || nameList.isEmpty()){
				nameList = new ArrayList();
				nameList.add(partName);
				pngCondition.setPartNameList(nameList);
			}else{
				if( !nameList.contains(partName)){
					nameList.add(partName);
				}
			}
		}
	}
	
	/**
	 * Modify Condition
	 * @param groupNumber
	 * @param product
	 * @param conditionStr
	 * @param operator
	 * @param qty
	 * @param partName
	 * @throws Exception
	 */
	public void modifyCondition(int groupNumber, String product, String conditionStr, String operator, int qty, ArrayList<String> alPartNames) throws Exception{
		PngCondition pngCondition = null;
		DefaultTableModel model = (DefaultTableModel)nameGroupRegPanel.conditionTable.getModel();
		pngCondition = new PngCondition(groupNumber, product, conditionStr, operator, qty);
		pngCondition.setPartNameList(alPartNames);
		
		model.setValueAt(groupNumber, nameGroupRegPanel.conditionTable.getSelectedRow(), 0);
		model.setValueAt(product, nameGroupRegPanel.conditionTable.getSelectedRow(), 1);
		model.setValueAt(conditionStr, nameGroupRegPanel.conditionTable.getSelectedRow(), 2);
		model.setValueAt(operator + " " + qty, nameGroupRegPanel.conditionTable.getSelectedRow(), 3);
		model.setValueAt(alPartNames, nameGroupRegPanel.conditionTable.getSelectedRow(), 4);
		model.setValueAt(pngCondition, nameGroupRegPanel.conditionTable.getSelectedRow(), 5);
	}
	
	ArrayList<String> getProductList() throws Exception{
		
		SYMCRemoteUtil remote = new SYMCRemoteUtil();
		DataSet ds = new DataSet();
		
		ds.put("DATA", null);
		try {
			
			ArrayList<String> productList = (ArrayList<String>)remote.execute("com.kgm.service.PartNameGroupService", "getProductList", ds);
			
			if( productList == null){
				return new ArrayList(); 
			}
			
			return productList;
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw e;
		}		
	}
	
	Vector getPngDataVec() throws Exception{
		
		try {
			
			Vector<Vector<Object>> data = new Vector<Vector<Object>>();
			ArrayList<HashMap<String, Object>> groupList = getPngList(null, null, null, false);

			for( int i = 0; groupList != null && i < groupList.size(); i++){
				HashMap map = groupList.get(i);
				String groupID = (String)map.get("GROUP_ID");
				String groupName = (String)map.get("GROUP_NAME");
				PngMaster master = new PngMaster(groupID, groupName);
				Vector row = new Vector();
				row.add(groupID);
				row.add(groupName);
				row.add(master);
				
				data.add(row);
			}
			
			return data;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw e;
		}			
	}	
	
	ArrayList<HashMap<String, Object>> getPngList(String product, String groupID, String isEnabled, boolean bShowPartName) throws Exception{
		SYMCRemoteUtil remote = new SYMCRemoteUtil();
		DataSet ds = new DataSet();
		
		Vector<Vector<Object>> data = new Vector<Vector<Object>>();
		ds.put("PRODUCT", product);
		ds.put("GROUP_ID", groupID);
		ds.put("IS_ENABLED", isEnabled);
		if( bShowPartName){
			ds.put("SHOW_PART_NAME", "1");
		}
		ArrayList<HashMap<String, Object>> groupList = (ArrayList<HashMap<String, Object>>)remote.execute("com.kgm.service.PartNameGroupService", "getPngMaster", ds);	
			
		return groupList;
	}
	
	public PngMaster getPngDetail(String groupID) throws Exception{
		
		SYMCRemoteUtil remote = new SYMCRemoteUtil();
		DataSet ds = new DataSet();
		
		try {
			
			Vector<Vector<Object>> data = new Vector<Vector<Object>>();
			ds.put("GROUP_ID", groupID);
			HashMap<String, Object> resultMap = (HashMap<String, Object>)remote.execute("com.kgm.service.PartNameGroupService", "getPngDetail", ds);

			HashMap pngInfo = (HashMap)resultMap.get("PNG_MASTER");
			ArrayList pngNamelist = (ArrayList)resultMap.get("PNG_NAME_LIST");
			ArrayList conditionList = (ArrayList)resultMap.get("PNG_CONDITION_LIST");
			
			if( pngInfo == null){
				return null;
			}
			PngMaster pngMaster = new PngMaster((String)pngInfo.get("GROUP_ID"), (String)pngInfo.get("GROUP_NAME"));
			pngMaster.setChanged(false);
			BigDecimal bigDefaultQty = (BigDecimal)pngInfo.get("DEFAULT_QTY");
			pngMaster.setDefaultQuantity(bigDefaultQty.intValue());
			pngMaster.setRefFunctions((String)pngInfo.get("REF_FUNCS"));
			
			/**
			 * [SR150416-025][2015.05.27][jclee] Description 컬럼 추가
			 */
			pngMaster.setDescription((String)pngInfo.get("DESCRIPTION"));
			String tmp = (String)pngInfo.get("IS_ENABLED");
			pngMaster.setEnable("1".equals(tmp) ? true:false);
			
			ArrayList<String> nameList = new ArrayList();
			for( int i = 0; pngNamelist != null && i < pngNamelist.size(); i++){
				HashMap map = (HashMap)pngNamelist.get(i);
				String partName = (String)map.get("PART_NAME");
				if( partName != null){
					nameList.add(partName);
				}
			}
			pngMaster.setPartNameList(nameList);
			
			ArrayList<PngCondition> pngConditionList = new ArrayList();
			for( int i = 0; conditionList != null && i < conditionList.size(); i++){
				HashMap conditionMap = (HashMap)conditionList.get(i);
				
				BigDecimal groupNum = (BigDecimal)conditionMap.get("GROUP_NUM");
				BigDecimal bigQty = (BigDecimal)conditionMap.get("QTY");
				String partName = (String)conditionMap.get("PART_NAME");
//				PngCondition tmpCondition = new PngCondition( groupNum.intValue(), (String)conditionMap.get("PRODUCT")
//						, (String)conditionMap.get("CONDITION"), (String)conditionMap.get("OPERATOR"), bigQty.intValue());
				
				PngCondition pngCondition = getSamePngCondition(pngMaster.getGroupID(), groupNum.intValue(), (String)conditionMap.get("PRODUCT"), (String)conditionMap.get("CONDITION"), (String)conditionMap.get("OPERATOR"), bigQty.intValue(), false);
				if( pngCondition == null){
					pngCondition = new PngCondition( groupNum.intValue(), (String)conditionMap.get("PRODUCT")
							, (String)conditionMap.get("CONDITION"), (String)conditionMap.get("OPERATOR"), bigQty.intValue());
					ArrayList<String> partNameList = new ArrayList();
					if( partName != null){
						partNameList.add(partName);
					}
					pngCondition.setPartNameList(partNameList);
				}else{
					ArrayList<String> partNameList = pngCondition.getPartNameList();
					if( partNameList == null || partNameList.isEmpty()){
						partNameList = new ArrayList();
						if( partName != null){
							partNameList.add(partName);
						}
						pngCondition.setPartNameList(partNameList);
					}else{
						if( partName != null && !partNameList.contains(partName)){
							partNameList.add(partName);
						}
					}
				}
				pngConditionList.add(pngCondition);
			}
			
			pngMaster.setConditionList(pngConditionList);
			
			return pngMaster;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw e;
		}			
	}		
	
	public void removeSelectedRows(JTable table){
		DefaultTableModel model = (DefaultTableModel)table.getModel();
		int[] rows = table.getSelectedRows();
		if( rows == null)
			return;
		int[] modelIdxes = new int[rows.length];
		for( int i = 0; rows != null && i < rows.length; i++){
			modelIdxes[i] = table.convertRowIndexToModel(rows[i]);
		}
		Arrays.sort(modelIdxes);
		
		for( int i = modelIdxes.length - 1; i >= 0; i--){
			model.removeRow(modelIdxes[i]);
		}
	}
	
	public void addSpec(Vector rowData){
		verificationPanel.addSpec(rowData);
	}
	
	public boolean isAvailable(String availableStr, String condition) throws Exception{
		
		if( availableStr == null || availableStr.equals("")){
			return true;
		}
		
		String tmpStr = availableStr;
		//Pattern이 맞는 옵셥값을 찾고, 앞에 #을 붙인다.
		ArrayList<String> foundOpValueList = new ArrayList();
		Pattern p = Pattern.compile("[a-zA-Z0-9]{4}");
		Matcher m = p.matcher(tmpStr);
		while (m.find()) {
			
			if( !foundOpValueList.contains(m.group())){
				foundOpValueList.add(m.group());
			}
		}
		
		if( foundOpValueList.isEmpty()){
			return true;
		}
		
		for( String opValue : foundOpValueList){
			tmpStr = tmpStr.replaceAll(opValue, "#" + opValue);
		}
		
		tmpStr = tmpStr.replaceAll("NOT", "!");
		tmpStr = tmpStr.replaceAll("AND", "&&");
		tmpStr = tmpStr.replaceAll("OR", "||");		
		for( String opValue : foundOpValueList){
			tmpStr = tmpStr.replaceAll("#" + opValue, "('##CONDITION##'.indexOf('" + opValue + "') > -1)");
		}
		
		String defaultStr = tmpStr;
		String[] subConditions = condition.split("OR");
		for( String subCondition : subConditions){
			defaultStr = tmpStr;
			defaultStr = defaultStr.replaceAll("##CONDITION##", subCondition);
			
			Object obj = getEngine().eval(defaultStr);
			if( obj instanceof Boolean){
				Boolean b = (Boolean)obj;
				if( b.booleanValue()){
					return b.booleanValue();
				};
			}else{
				throw new Exception("Not available Option : " + availableStr);
			}
		}
		
		return false;	
	}		
	
	public static void checkNewPartName(String ecoNo) throws Exception{
		SYMCRemoteUtil remote = new SYMCRemoteUtil();
		DataSet ds = new DataSet();
		
		ds.put("ECO_NO", ecoNo);
		try {
			
			remote.execute("com.kgm.service.PartNameGroupService", "insertPngNewNameFromECO", ds);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw (new Exception("insertPngNewNameFromECO error"));
		}		
	}
	
	public Vector getProductHeader() throws Exception{
		
		Vector header = new Vector();
		
		SYMCRemoteUtil remote = new SYMCRemoteUtil();
		DataSet ds = new DataSet();
		
		ds.put("DATA", null);
		try {
			
			ArrayList<String> productList = (ArrayList<String>)remote.execute("com.kgm.service.PartNameGroupService", "getPngProdOrder", ds);
			
			if( productList == null){
				return header; 
			}
			
			header.addAll(productList);
			return header;
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw e;
		}	
		
	}	
	
	public void dispose() {
		deletePngEpl();
		super.dispose();
	}
	
	private void deletePngEpl(){
		SYMCRemoteUtil remote = new SYMCRemoteUtil();
		String rowKey = "";
		DataSet ds = new DataSet();

		ds.put("ROWKEY", rowkey);

		try {
			
			remote.execute("com.kgm.service.PartNameGroupService", "deletePngEpl", ds);

		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
}

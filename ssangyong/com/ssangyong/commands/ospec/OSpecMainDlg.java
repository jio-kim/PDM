package com.ssangyong.commands.ospec;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import com.ssangyong.commands.ec.SYMCECConstant;
import com.ssangyong.commands.ospec.op.OSpec;
import com.ssangyong.commands.ospec.op.OpCategory;
import com.ssangyong.commands.ospec.op.OpGroup;
import com.ssangyong.commands.ospec.op.OpTrim;
import com.ssangyong.commands.ospec.op.OpUtil;
import com.ssangyong.commands.ospec.op.OpValueName;
import com.ssangyong.commands.ospec.op.Option;
import com.ssangyong.commands.ospec.panel.ComparablePanel;
import com.ssangyong.commands.ospec.panel.OSpecTable;
import com.ssangyong.commands.ospec.panel.OptionGroupManagerPanel;
import com.ssangyong.commands.ospec.panel.PublishPanel;
import com.ssangyong.commands.ospec.panel.WorkspacePanel;
import com.ssangyong.common.remote.DataSet;
import com.ssangyong.common.remote.SYMCRemoteUtil;
import com.ssangyong.common.ui.mergetable.MultiSpanCellTable;
import com.ssangyong.common.utils.CustomUtil;
import com.ssangyong.common.utils.DatasetService;
import com.teamcenter.rac.aif.AbstractAIFDialog;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCProperty;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;


public class OSpecMainDlg extends AbstractAIFDialog {

	private final JPanel contentPanel = new JPanel();
	private TCComponentItemRevision ospecRev = null;
	private OSpec ospec = null;
	private OSpecViewDlg viewDlg = null;
	private String userID = null;
	
	private WorkspacePanel workspacePanel = null;
	private PublishPanel publicationPanel = null;
	private JTabbedPane tabbedPane = null;
	private ScriptEngineManager manager = new ScriptEngineManager();
    private ScriptEngine engine = manager.getEngineByName("js"); 
    
    public static final String TOTAL_DATA = "Total Data";
    public static final String FILTERED_DATA = "Filter Data";
    public static final String CONDITIONAL_DATA = "Conditional Data";
	
	/**
	 * Create the dialog.
	 * @throws Exception 
	 */
	public OSpecMainDlg(TCComponentItemRevision ospecRev) throws Exception {
		super(AIFUtility.getActiveDesktop().getFrame(), true);
		
		this.ospecRev = ospecRev;
		
		userID = CustomUtil.getTCSession().getUser().getUserId();
		ospec = getOSpec(ospecRev);
		setTitle(ospec.getOspecNo());
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
							workspacePanel.refreshOpGroupList();
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							MessageBox.post(OSpecMainDlg.this, e.getMessage(), "ERROR", MessageBox.ERROR);
						}
					}
				}
				
			});
			contentPanel.add(tabbedPane);
			
			{
				ComparablePanel comparePanel = new ComparablePanel(this);
				tabbedPane.addTab("O/Spec Compare", null, comparePanel, null);
			}
			{
				OptionGroupManagerPanel optionGroupManagerPanel = new OptionGroupManagerPanel(this);
				tabbedPane.addTab("Option Group Manager", null, optionGroupManagerPanel, null);
			}
			{
				workspacePanel = new WorkspacePanel(this);
				tabbedPane.addTab("Workspace", null, workspacePanel, null);
				
			}
			{
				publicationPanel = new PublishPanel(this);
				tabbedPane.addTab("Publication", null, publicationPanel, null);
			}
		}
		{
			JPanel buttonPane = new JPanel();
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			buttonPane.setLayout(new BorderLayout(0, 0));
			{
				JPanel panel = new JPanel();
				buttonPane.add(panel, BorderLayout.WEST);
				{
					JButton btnNewButton = new JButton("Ospec View");
					btnNewButton.setIcon(new ImageIcon(OSpecMainDlg.class.getResource("/icons/viewer_16.png")));
					btnNewButton.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent arg0) {
							if( viewDlg == null){
								try{
									
									OSpecTable ospecTb = new OSpecTable(ospec, null);
								    
								    viewDlg = new OSpecViewDlg(ospec.getOspecNo(), ospecTb.getOspecTable());
								    // [20240313][UPGRADE] 창이 뒤로 숨는 문제를 해결하기 위해 추가
									viewDlg.setModal(true);
								    viewDlg.setSize(1200, 700);
								}catch(Exception e){
									e.printStackTrace();
								}
															
							}
							viewDlg.setVisible(true);
						}
					});
					panel.add(btnNewButton);
				}
			}
			{
				JPanel panel = new JPanel();
				FlowLayout flowLayout = (FlowLayout) panel.getLayout();
				flowLayout.setAlignment(FlowLayout.TRAILING);
				buttonPane.add(panel);
				{
					JButton closeButton = new JButton("Close");
					closeButton.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent actionevent) {
							dispose();
						}
					});
					panel.add(closeButton);
				}
			}
		}
	}
	
	
	public void deleteOptionGroup(OpGroup opGroup) throws Exception{
		
		
		SYMCRemoteUtil remote = new SYMCRemoteUtil();
		DataSet ds = new DataSet();
		
		ds.put("OWNER", userID);
		ds.put("GROUP_NAME", opGroup.getOpGroupName());
		ds.put("PROJECT", ospec.getProject());
		
		try {
			
			remote.execute("com.ssangyong.service.OSpecService", "deleteOptionGroup", ds);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw e;
		}
	}	
	

	
	public ArrayList<OpValueName> getOptionGroupDetail(String groupName, boolean bOnlyOwn) throws Exception{
		
		ArrayList<OpValueName> result = new ArrayList();
		SYMCRemoteUtil remote = new SYMCRemoteUtil();
		DataSet ds = new DataSet();
		
		ds.put("GROUP_NAME", groupName);
		if( bOnlyOwn ){
			ds.put("OWNER", userID);
		}
		ds.put("PROJECT", ospec.getProject());
		try {
			ArrayList<HashMap<String, String>> list = (ArrayList<HashMap<String, String>>)remote.execute("com.ssangyong.service.OSpecService", "getOptionGroupDetail", ds);
			for( int i = 0; list != null && i < list.size(); i++){
				HashMap<String, String> map = list.get(i);
				OpValueName value = new OpValueName("","",map.get("VALUE"), map.get("VALUE_NAME"));
				result.add(value);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw e;
		}
		
		return result;
	}
	
	public ArrayList<OpGroup> getOptionGroup(boolean bOnlyOwn) throws Exception{
		
		ArrayList<OpGroup> result = new ArrayList();
		SYMCRemoteUtil remote = new SYMCRemoteUtil();
		DataSet ds = new DataSet();
		
		if( bOnlyOwn ){
			ds.put("OWNER", userID);
		}
		ds.put("PROJECT", ospec.getProject());
		try {
			ArrayList<HashMap<String, String>> list = (ArrayList<HashMap<String, String>>)remote.execute("com.ssangyong.service.OSpecService", "getOptionGroup", ds);
			for( int i = 0; list != null && i < list.size(); i++){
				HashMap<String, String> map = list.get(i);
				//OpGroup group = new OpGroup(map.get("GROUP_NAME"), map.get("OWNER"));
				//2016-05-30 수정: 비고 추가
				String description = map.get("DESCRIPTION") ==null? "": map.get("DESCRIPTION"); // 비고
				String condition = map.get("CONDITION") ==null? "": map.get("CONDITION"); // 비고
				OpGroup group = new OpGroup(map.get("GROUP_NAME"), map.get("OWNER"), description, condition);
				group.setChanged(false);
				result.add(group);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw e;
		}
		
		return result;
	}
	
	public HashMap<String, HashMap<String, ArrayList<OpGroup>>> getReferedOptionGroup(HashMap<String, HashMap<String, ArrayList<OpGroup>>> result, Vector<Vector> data, String project) throws Exception{
		
		if( data == null){
			return null;
		}
		
		HashMap<String, ArrayList<String>> paraMap = new HashMap();
		for( Vector row : data){
			
			String category = ""; 
			String opValue = (String)row.get(3);
			if( opValue != null && opValue.length() > 3){
				category = OpUtil.getCategory(opValue);
			}else{
				continue;
			}
			ArrayList<String> list = paraMap.get(project);
			if( !paraMap.containsKey(project) ){
				list = new ArrayList();
				list.add(category);
				paraMap.put(project, list);
			}else{
				list = paraMap.get(project);
				if( !list.contains(category)){
					list.add(category);
				}
			}
		}

		if( !paraMap.isEmpty()){
			HashMap<String, HashMap<String, ArrayList<OpGroup>>> opGroupList = getReferedOptionGroup(result, paraMap, false);
			
			return opGroupList;
		}
		
		return null;
	}
	
	public HashMap<String, HashMap<String, ArrayList<OpGroup>>> getReferedOptionGroup(HashMap<String, HashMap<String, ArrayList<OpGroup>>> result, HashMap<String, ArrayList<String>> paraMap, boolean bOnlyOwn) throws Exception{
		
//		HashMap<String, HashMap<String, ArrayList<OpGroup>>>  result = new HashMap();// HashMap<project, HashMap<opValue, ArrayList<OpGroup>>>
		SYMCRemoteUtil remote = new SYMCRemoteUtil();
		DataSet ds = new DataSet();
		
		ds.put("DATA", paraMap);
		if( bOnlyOwn ){
			ds.put("OWNER", userID);
		}
		try {
			HashMap<String, ArrayList> resultList = (HashMap<String, ArrayList>)remote.execute("com.ssangyong.service.OSpecService", "getReferedOptionGroup", ds);
			if( !resultList.isEmpty()){
				Iterator<String> its = resultList.keySet().iterator();
				while(its.hasNext()){
					String key = its.next();
					int idx = key.lastIndexOf("_");
					String project = key.substring(0, idx);
					String opCategory = key.substring(idx + 1);
					ArrayList<HashMap<String, String>> list = resultList.get(key);
					HashMap<String, ArrayList<OpGroup>> valueMap = result.get(project);
					if( valueMap == null){
						valueMap = new HashMap();
						ArrayList<OpGroup> opGroupList = new ArrayList();
						
						
						for(HashMap<String, String> map : list){
							OpGroup group = new OpGroup(map.get("GROUP_NAME"), map.get("OWNER"));
							opGroupList.add(group);
						}
						
						if( opGroupList.isEmpty()){
							continue;
						}
						
						valueMap.put(opCategory, opGroupList);
						result.put(project, valueMap);
						
					}else{
						ArrayList<OpGroup> opGroupList = valueMap.get(opCategory);
						if( opGroupList == null){
							opGroupList = new ArrayList();
							for(HashMap<String, String> map : list){
								OpGroup group = new OpGroup(map.get("GROUP_NAME"), map.get("OWNER"));
								opGroupList.add(group);
							}
							valueMap.put(opCategory, opGroupList);
						}else{
							for(HashMap<String, String> map : list){
								OpGroup group = new OpGroup(map.get("GROUP_NAME"), map.get("OWNER"));
								if( !opGroupList.contains(group)){
									opGroupList.add(group);
								}
							}
						}
						
					}
				}
			}
			
		} catch (Exception e) {
			throw e;
		}
		
		return result;
	}	
	
	public OSpec getOSpec(TCComponentItemRevision ospecRev) throws Exception{
		
		String ospecStr = ospecRev.getProperty("item_id") + "-" + ospecRev.getProperty("item_revision_id");
		TCProperty tcproperty = ospecRev.getTCProperty("s7_OspecReleasedDate");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		OSpec ospec = null;
		
		AIFComponentContext[] context = ospecRev.getChildren(SYMCECConstant.ITEM_DATASET_REL);
		for( int i = 0; context != null && i < context.length; i++){
			TCComponentDataset ds = (TCComponentDataset)context[i].getComponent();
			if( ospecStr.equals(ds.getProperty("object_name"))){
				File t = new File(".");
				File[] files = DatasetService.getFiles(ds);
				ospec = OpUtil.getOSpec(files[0]);
				break;
			};
		}
		
		return ospec;
	}
	
	private ArrayList getTrim(String ospecNo) throws Exception{
		
		ArrayList list = new ArrayList();
		SYMCRemoteUtil remote = new SYMCRemoteUtil();
		DataSet ds = new DataSet();
		try {
			ds.put("O_SPEC_NO", ospecNo);
			ArrayList<HashMap<String,Object>> trims = (ArrayList<HashMap<String,Object>>)remote.execute("com.ssangyong.service.OSpecService", "getOspecTrim", ds);
			for( int i = 0; trims!=null && i < trims.size(); i++){
				HashMap trimMap = trims.get(i);
				OpTrim trim = new OpTrim();
				trim.setArea(trimMap.get("AREA").toString());
				trim.setPassenger(trimMap.get("PASSENGER").toString());
				trim.setEngine(trimMap.get("ENGINE").toString());
				trim.setGrade(trimMap.get("GRADE").toString());
				trim.setTrim(trimMap.get("TRIM").toString());
				trim.setColOrder(Integer.parseInt(trimMap.get("COL_ORDER").toString()));
				list.add(trim);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw e;
		}		
		return list;
	}

	public static TCComponentItemRevision[] getOspecRevision(String gModel, String project, String version, String dateStr) throws Exception{
		TCSession session = CustomUtil.getTCSession();
		HashMap<String, String> param = new HashMap();
		if( gModel != null && !gModel.equals("")){
			param.put("Gmodel", gModel);
		}
		
		if( project != null && !project.equals("") ){
			param.put("Project", project);
		}
		
		if( version != null && !version.equals("")){
			param.put("Revision", version);
		}
		
		if( dateStr != null && !dateStr.equals("")){
			param.put("Ospec_Released_Date", dateStr);
		}
		
		ArrayList names = new ArrayList();
		ArrayList values = new ArrayList();
		Set<String> keys = param.keySet();
		for( String name : keys){
			String value = param.get(name);
			names.add(name);
			values.add(value);
		}
		TCComponent[] coms = CustomUtil.queryComponent("SYMC_Search_OspecSet_Revision", (String[])names.toArray(new String[names.size()]), (String[])values.toArray( new String[values.size()]));
		
		TCComponentItemRevision[] revisions = null;
		if( coms != null && coms.length > 0){
			revisions = new TCComponentItemRevision[coms.length];
			System.arraycopy(coms, 0, revisions, 0, coms.length);
		}
		return revisions;
	}
	

	
	public void removeAllRow(JTable table){
		DefaultTableModel model = (DefaultTableModel)table.getModel();
		
		for( int i = model.getRowCount() - 1; i >= 0 ; i--){
			model.removeRow(i);
		}
	}

	public OSpec getOspec() {
		return ospec;
	}

	public String getUserID() {
		return userID;
	}

	public void sendToPublish(OSpecTable ospecTable, ArrayList<OpGroup> selectedOpGroup) throws CloneNotSupportedException{
		
		OSpec changedOspec = (OSpec)ospec.clone();
		HashMap<String, ArrayList<Option>> options = new HashMap();
		changedOspec.setOptions(options);
		changedOspec.setOpNameList(new ArrayList<OpValueName>());
		changedOspec.setCategory(new HashMap<String, HashMap<String, OpCategory>>());
		changedOspec.setPackageMap(new HashMap<String, HashMap<String, HashMap<String, ArrayList<String>>>>());
		changedOspec.setDriveTypeMap(new HashMap<String, HashMap<String, HashMap<String, ArrayList<String>>>>());
		
		MultiSpanCellTable fixedTable = ospecTable.getFixedOspecViewTable();
		MultiSpanCellTable dataTable = ospecTable.getOspecViewTable();
		TableColumnModel cm = dataTable.getColumnModel();
		
		for( int col = 3; col < cm.getColumnCount() - 2; col++){
			
			TableColumn tableColumn = cm.getColumn(col);
			OpTrim opTrim = ospec.getTrims().get(tableColumn.getHeaderValue().toString());
			
			for( int row = 0; dataTable != null && row < dataTable.getRowCount(); row++){
			
				String opCode = (String)fixedTable.getValueAt(row, 3);
				String value = (String)dataTable.getValueAt(row, col);
				if( "".equals(value) || "-".equals(value)){
					continue;
				}
				
				String category = OpUtil.getCategory(opCode);
				String categoryDesc = (String)fixedTable.getValueAt(row, 1);
				String opDesc = (String)fixedTable.getValueAt(row, 2);
//				String optionCategory = OpUtil.getCategory(opCode);
				String packageName = (String)dataTable.getValueAt(row, 0);
				String driveType = (String)dataTable.getValueAt(row, 1);
				String all = (String)dataTable.getValueAt(row, 2);
				String effIn = (String)dataTable.getValueAt(row, cm.getColumnCount() - 2);
				String remark = (String)dataTable.getValueAt(row, cm.getColumnCount() - 1);
				
				Option option = new Option(category, categoryDesc, opCode, opDesc, packageName, driveType, all, value, effIn, remark, col, row);
				ArrayList<Option> opList = options.get(opTrim.getTrim());
				if( opList == null){
					opList = new ArrayList<Option>();
					opList.add(option);
					options.put(opTrim.getTrim(), opList);
				}else{
					if(!opList.contains(option)){
						opList.add(option);
					}
				}
				
				HashMap<String, HashMap<String, OpCategory>> categories = changedOspec.getCategory();
				HashMap<String, OpCategory> categoryMap = categories.get(opTrim.getTrim());
				if( categoryMap == null){
					categoryMap = new HashMap();
					
					OpCategory opCategory = new OpCategory(category, categoryDesc);
					ArrayList<Option> opValueList = new ArrayList();
					opValueList.add(option);
					opCategory.setOpValueList(opValueList);
					
					categoryMap.put(category, opCategory);
					categories.put(opTrim.getTrim(), categoryMap);
					
				}else{
					OpCategory opCategory = categoryMap.get(category);
					if( opCategory == null){
						opCategory = new OpCategory(category, categoryDesc);
						ArrayList<Option> opValueList = new ArrayList();
						opValueList.add(option);
						opCategory.setOpValueList(opValueList);
						categoryMap.put(category, opCategory);
					}else{
						if( !opCategory.getOpValueList().contains(option)){
							opCategory.getOpValueList().add(option);
						}
					}
					
				}
				
				//Package Map 셋팅
				if( packageName != null && !packageName.equals("")){
					HashMap<String, HashMap<String, HashMap<String, ArrayList<String>>>> packageMap = changedOspec.getPackageMap();
					HashMap<String, HashMap<String, ArrayList<String>>> pkgPackageMap = packageMap.get(opTrim.getTrim());
					if( pkgPackageMap == null ){
						pkgPackageMap = new HashMap();
						HashMap<String, ArrayList<String>> categoryPackageMap = new HashMap();
						ArrayList<String> packageOpValues = new ArrayList();
						packageOpValues.add(opCode);
						categoryPackageMap.put(category, packageOpValues);
						pkgPackageMap.put(packageName, categoryPackageMap);
						packageMap.put(opTrim.getTrim(), pkgPackageMap);
					}else{
						HashMap<String, ArrayList<String>> categoryPackageMap = pkgPackageMap.get(packageName);
						if( categoryPackageMap == null ){
							categoryPackageMap = new HashMap();
							ArrayList<String> packageOpValues = new ArrayList();
    						packageOpValues.add(opCode);
    						categoryPackageMap.put(category, packageOpValues);
    						pkgPackageMap.put(packageName, categoryPackageMap);
						}else{
							ArrayList<String> packageOpValues = categoryPackageMap.get(category);
							if( packageOpValues == null){
								packageOpValues = new ArrayList();
								packageOpValues.add(opCode);
	    						categoryPackageMap.put(category, packageOpValues);
							}else{
								if( !packageOpValues.contains(opCode) ){
									packageOpValues.add(opCode);
								}
							}
						}
					}
				}	
				
				//drive Map 셋팅
				if( driveType != null && !driveType.equals("") && !driveType.equals(".") && !driveType.equals("-")){
					HashMap<String, HashMap<String, HashMap<String, ArrayList<String>>>> driveMap = changedOspec.getDriveTypeMap();
					HashMap<String, HashMap<String, ArrayList<String>>> trimDriveMap = driveMap.get(opTrim.getTrim());
					if( trimDriveMap == null ){
						trimDriveMap = new HashMap();
						HashMap<String, ArrayList<String>> categoryDriveMap = new HashMap();
						ArrayList<String> driveOpValues = new ArrayList();
						driveOpValues.add(opCode);
						categoryDriveMap.put(category, driveOpValues);
						trimDriveMap.put(driveType, categoryDriveMap);
						driveMap.put(opTrim.getTrim(), trimDriveMap);
					}else{
						HashMap<String, ArrayList<String>> categoryDriveMap = trimDriveMap.get(driveType);
						if( categoryDriveMap == null ){
							categoryDriveMap = new HashMap();
							ArrayList<String> driveOpValues = new ArrayList();
    						driveOpValues.add(opCode);
    						categoryDriveMap.put(category, driveOpValues);
    						trimDriveMap.put(driveType, categoryDriveMap);
						}else{
							ArrayList<String> driveOpValues = categoryDriveMap.get(category);
							if( driveOpValues == null){
								driveOpValues = new ArrayList();
								driveOpValues.add(opCode);
	    						categoryDriveMap.put(category, driveOpValues);
							}else{
								if( !driveOpValues.contains(opCode) ){
									driveOpValues.add(opCode);
								}
							}
						}
					}
				}				
				
				
				OpValueName ovn = new OpValueName(category, categoryDesc, opCode, opDesc);
				if (!changedOspec.getOpNameList().contains(ovn)) {
					changedOspec.getOpNameList().add(ovn);
				} 
			}
			
		}
		publicationPanel.setOspec(changedOspec);
		publicationPanel.setSelectedCondition(selectedOpGroup);
		tabbedPane.setSelectedComponent(publicationPanel);
	}

	public ArrayList<HashMap<String, String>> getFuntionList() throws Exception{
		SYMCRemoteUtil remote = new SYMCRemoteUtil();
		DataSet ds = new DataSet();
		try {
			ds.put("PROJECT", ospec.getProject());
			ArrayList<HashMap<String, String>> functionList = (ArrayList<HashMap<String, String>>)remote.execute("com.ssangyong.service.OSpecService", "getFunctionList", ds);
			return functionList;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw e;
		}
	}
	
	public void addCondition(Vector conditionRow){
		publicationPanel.addCondition(conditionRow);
	}

	public ScriptEngine getEngine() {
		return engine;
	}

	public PublishPanel getPublicationPanel() {
		return publicationPanel;
	}
	
	public TCComponentItemRevision getOspecRev()
	{
		return  ospecRev;
	}
	
}

package com.ssangyong.commands.ospec;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import com.ssangyong.commands.ec.SYMCECConstant;
import com.ssangyong.commands.ospec.op.OSpec;
import com.ssangyong.commands.ospec.op.OpUtil;
import com.ssangyong.common.WaitProgressBar;
import com.ssangyong.common.remote.DataSet;
import com.ssangyong.common.remote.SYMCRemoteUtil;
import com.ssangyong.common.utils.CustomUtil;
import com.teamcenter.rac.aif.AbstractAIFDialog;
import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.aif.InterfaceAIFOperationListener;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentDatasetType;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCProperty;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.toedter.calendar.JDateChooser;
//import java.io.FileFilter;

public class OSpecImportDlg extends AbstractAIFDialog {

	private final JPanel contentPanel = new JPanel();
//	private JTextField textField;
	private JTextField tfFilePath;
	private JTable table;
	
	private String selectedOSpec = "";
	private Vector headerVector = new Vector();
	private JComboBox cbGmodel = null;
	private JComboBox cbProject = null;
	private JDateChooser dateChooser = null;
	private JTabbedPane tabbedPane = null;
	private JPanel searchPanel = null;
	private JPanel regPanel = null;
	
	private final int ALREADY_EXIST_ITEM_REVISION = 2;
	private final int CREATION_SUCCESS = 1;
	
	private TCComponentItemRevision ospecRevision = null;
	
	/**
	 * Create the dialog.
	 */
	public OSpecImportDlg() {
		super(AIFUtility.getActiveDesktop().getFrame(), false);
		setTitle("O/Spec Importer");
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));
		{
			tabbedPane = new JTabbedPane(JTabbedPane.TOP);
			contentPanel.add(tabbedPane);
			{
				searchPanel = new JPanel();
				tabbedPane.addTab("O/Spec Selection", null, searchPanel, null);
				searchPanel.setLayout(new BorderLayout(0, 0));
				{
					JPanel panel = new JPanel();
					searchPanel.add(panel, BorderLayout.NORTH);
					panel.setLayout(new BorderLayout(0, 0));
					{
						JPanel fndOptPanel = new JPanel();
						FlowLayout flowLayout = (FlowLayout) fndOptPanel.getLayout();
						flowLayout.setAlignment(FlowLayout.LEADING);
						panel.add(fndOptPanel);
						{
							JLabel lblGmodel = new JLabel("G-Model : ");
							fndOptPanel.add(lblGmodel);
						}
						{
							cbGmodel = new JComboBox();
							cbGmodel.addItem(OpUtil.SELECT_G_MODEL);
							cbGmodel.addItemListener(new ItemListener() {
								public void itemStateChanged(ItemEvent event) {
									if( event.getStateChange() == ItemEvent.SELECTED){
										String gModelStr = (String)cbGmodel.getSelectedItem();
										try{
											OpUtil.refreshProject(cbGmodel, cbProject);
//											if( gModelStr.equals(OpUtil.SELECT_G_MODEL)){
//												refreshProject();
//											}else{
//												for( int i = cbProject.getModel().getSize() - 1; i >= 0; i--){
//													cbProject.removeItemAt(i);
//												}
//												
//												cbProject.addItem(OpUtil.SELECT_PROJECT);
//												ArrayList<String> projects = getProjectList();
//												for( int i = 0; projects != null && i < projects.size(); i++){
//													String project = projects.get(i);
//													if( project.indexOf(gModelStr) == 0){
//														cbProject.addItem(project);
//													}
//												}
//											}
										}catch(Exception e){
											MessageBox.post(OSpecImportDlg.this, e.getMessage(), "ERROR", MessageBox.ERROR);
										}
									}
								}
							});
							fndOptPanel.add(cbGmodel);
						}
						{
							JLabel lblProject = new JLabel("  Project : ");
							fndOptPanel.add(lblProject);
						}
						{
							cbProject = new JComboBox();
							cbProject.addItem(OpUtil.SELECT_PROJECT);
							fndOptPanel.add(cbProject);
						}
						{
							JLabel lblNewLabel = new JLabel(" Date : ");
							fndOptPanel.add(lblNewLabel);
						}
						{
							dateChooser = new JDateChooser(null, "yyyy-MM-dd", false, null);
							fndOptPanel.add(dateChooser);
						}
					}
					{
						JPanel fndPanel = new JPanel();
						panel.add(fndPanel, BorderLayout.EAST);
						{
							final JButton btnNewButton = new JButton("Search");
							btnNewButton.setIcon(new ImageIcon(OSpecImportDlg.class.getResource("/icons/search_16.png")));
							btnNewButton.addActionListener(new ActionListener() {
								public void actionPerformed(ActionEvent event) {
									
									final WaitProgressBar waitProgress = new WaitProgressBar(OSpecImportDlg.this);
									waitProgress.start();
									waitProgress.setStatus("Searching....");
									TCSession session = CustomUtil.getTCSession();
									final AbstractAIFOperation operation = new AbstractAIFOperation() {
										
										@Override
										public void executeOperation() throws Exception {
											// TODO Auto-generated method stub
											try{
												refreshTable();
											}catch(Exception e){
												// [20240228] 오류가 발생 시 ProgressBar 를 닫도록 추가
												waitProgress.dispose();
												e.printStackTrace();
												storeOperationResult(e.getMessage());
											}
										}
									};
									
									operation.addOperationListener(new InterfaceAIFOperationListener() {
										
										@Override
										public void startOperation(String s) {
											// TODO Auto-generated method stub
											
										}
										
										@Override
										public void endOperation() {
											// TODO Auto-generated method stub
											String result = (String)operation.getOperationResult();
											if( result != null && !result.equals("") ){
												waitProgress.setStatus(result);
												waitProgress.setShowButton(true);
											}else{
												waitProgress.close();
											}
										}
									});
									session.queueOperation(operation);
								}
							});
							fndPanel.add(btnNewButton);
						}
					}
				}
				{
					JPanel panel = new JPanel();
					searchPanel.add(panel, BorderLayout.CENTER);
					panel.setLayout(new BorderLayout(0, 0));
					{
						JScrollPane scrollPane = new JScrollPane();
						panel.add(scrollPane, BorderLayout.CENTER);
						{
//							table = new JTable();
							headerVector.add("G-Model");
							headerVector.add("Project");
							headerVector.add("Released Date");
							headerVector.add("OSI-No");
							headerVector.add("ospec");
							TableModel model = new DefaultTableModel(null, headerVector) {
								public Class getColumnClass(int col) {
									return String.class;
								}

								public boolean isCellEditable(int row, int col) {
									return false;
								}
						    };
						    table = new JTable(model);
						    TableColumnModel cm = table.getColumnModel();
						    cm.removeColumn(cm.getColumn(4));
						    table.addMouseListener(new MouseAdapter(){

								@Override
								public void mouseReleased(MouseEvent e) {
									if( e.getClickCount()==2 && SwingUtilities.isLeftMouseButton(e) 
											&& e.isControlDown()==false) {
										if( table.getSelectedRow() > -1){
											DefaultTableModel model = (DefaultTableModel)table.getModel();
											int rowIdx = table.convertRowIndexToModel(table.getSelectedRow());
					//						int columnIdx = table.convertColumnIndexToModel(table.getSelectedColumn());
//											selectedOSpec = model.getValueAt(rowIdx, 3).toString();
											ospecRevision = (TCComponentItemRevision)model.getValueAt(rowIdx, 4);
										}
										// O/Spec 상세 정보를 가져온다.
										
										
										OSpecImportDlg.this.dispose();
									}
									super.mouseReleased(e);
								}
								
							});

						    TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(model);
						    table.setRowSorter(sorter);							
							
							scrollPane.setViewportView(table);
						}
					}
				}
			}
			{
				regPanel = new JPanel();
				tabbedPane.addTab("O/Spec Registration", null, regPanel, null);
				regPanel.setLayout(new BorderLayout(0, 0));
				{
					JPanel panel = new JPanel();
					FlowLayout flowLayout = (FlowLayout) panel.getLayout();
					flowLayout.setAlignment(FlowLayout.LEADING);
					panel.setBorder(new TitledBorder(null, "New O/Spec Registration", TitledBorder.LEADING, TitledBorder.TOP, null, null));
					regPanel.add(panel, BorderLayout.CENTER);
					{
						tfFilePath = new JTextField();
						panel.add(tfFilePath);
						tfFilePath.setColumns(30);
					}
					{
						JButton btnFind = new JButton("Find..");
						btnFind.setIcon(new ImageIcon(OSpecImportDlg.class.getResource("/icons/search_16.png")));
						btnFind.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent arg0) {
								
								JFileChooser fileChooser = new JFileChooser();
//								fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY );
								fileChooser.setFileFilter(new FileFilter(){

									@Override
									public boolean accept(File f) {
										// TODO Auto-generated method stub
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
										// TODO Auto-generated method stub
										return "*.xls";
									}

								});
								int result = fileChooser.showOpenDialog(OSpecImportDlg.this);
								if( result == JFileChooser.APPROVE_OPTION){
									File selectedFile = fileChooser.getSelectedFile();
									OSpecImportDlg.this.tfFilePath.setText( selectedFile.getAbsolutePath() );
								}						
								
							}
						});
						panel.add(btnFind);
					}
				}
				{
					JPanel panel = new JPanel();
					FlowLayout flowLayout = (FlowLayout) panel.getLayout();
					flowLayout.setAlignment(FlowLayout.RIGHT);
					regPanel.add(panel, BorderLayout.SOUTH);
					{
						final JButton regBtn = new JButton("Register");
						regBtn.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent arg0) {
								
								final WaitProgressBar waitProgress = new WaitProgressBar(OSpecImportDlg.this);
								Thread t = new Thread(){
									public void run(){
										File selectedFile = new File(tfFilePath.getText());
										
										try
							            {
											regBtn.setEnabled(false);
											waitProgress.setStatus("Saving....");
											OSpec ospec = OpUtil.getOSpec(selectedFile);
											importOspecToTC(ospec, selectedFile);
											
											refreshGmodel();
											refreshProject();
											tabbedPane.setSelectedComponent(searchPanel);
											waitProgress.dispose();
							            }
							            catch (Exception e)
							            {
							            	e.printStackTrace();
							            	waitProgress.setStatus(e.getMessage());
							            	waitProgress.setShowButton(true);
							            }finally{
							            	regBtn.setEnabled(true);
							            }			
									}
								};
								t.start();
								
					
							}
						});
						panel.add(regBtn);
					}
				}
			}
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						
						if( table.getSelectedRow() > -1){
							DefaultTableModel model = (DefaultTableModel)table.getModel();
							int rowIdx = table.convertRowIndexToModel(table.getSelectedRow());
							ospecRevision = (TCComponentItemRevision)model.getValueAt(rowIdx, 4);
						}
						// O/Spec 상세 정보를 가져온다.
						
						OSpecImportDlg.this.dispose();
					}
				});
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						ospecRevision = null;
						selectedOSpec = "";
						OSpecImportDlg.this.dispose();
					}
				});
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
		
		//Combo Box Data Load..
		try {
			refreshGmodel();
			refreshProject();
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}
	
	private ArrayList<String> getGModelList() throws Exception{
		SYMCRemoteUtil remote = new SYMCRemoteUtil();
		DataSet ds = new DataSet();
		try {
			ds.put("NO-PARAM", null);
			ArrayList<String> gModelList = (ArrayList<String>)remote.execute("com.ssangyong.service.OSpecService", "getGModel", ds);
			return gModelList;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw e;
		}
	}
	
	private void refreshGmodel() throws Exception{
		
		try {
			for( int i = cbGmodel.getModel().getSize() - 1; i >= 0; i--){
				cbGmodel.removeItemAt(i);
			}
			
			cbGmodel.addItem(OpUtil.SELECT_G_MODEL);
			ArrayList<String> gModelList = getGModelList();
			for( int i = 0; gModelList!=null && i < gModelList.size(); i++){
				cbGmodel.addItem(gModelList.get(i));
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw e;
		}
		
	}	
	
	private ArrayList<String> getProjectList() throws Exception{
		SYMCRemoteUtil remote = new SYMCRemoteUtil();
		DataSet ds = new DataSet();
		if( cbGmodel.getSelectedIndex() > -1){
			String gModel = (String)cbGmodel.getSelectedItem();
			ds.put("G-MODEL", gModel);
		}
		
		try {
			ArrayList<String> gModelList = (ArrayList<String>)remote.execute("com.ssangyong.service.OSpecService", "getProject", ds);
			return gModelList;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw e;
		}
	}
	
	private void refreshProject() throws Exception{
		
		try {
			for( int i = cbProject.getModel().getSize() - 1; i >= 0; i--){
				cbProject.removeItemAt(i);
			}
			
			cbProject.addItem(OpUtil.SELECT_PROJECT);
			ArrayList<String> gModelList = getProjectList();
			for( int i = 0; gModelList!=null && i < gModelList.size(); i++){
				cbProject.addItem(gModelList.get(i));
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw e;
		}
		
	}		
	
	public ArrayList<HashMap<String, String>> refreshTable() throws Exception{
		
		String gModel = cbGmodel.getSelectedItem().toString();
		if( gModel.equals(OpUtil.SELECT_G_MODEL)){
			throw new Exception(OpUtil.SELECT_G_MODEL);
		}
		String project = cbProject.getSelectedItem().toString();
		if( project.equals(OpUtil.SELECT_PROJECT)){
			throw new Exception(OpUtil.SELECT_PROJECT);
		}
		
		String released_date = null;
		Date date = dateChooser.getDate();
		SimpleDateFormat sdf  = new SimpleDateFormat("dd-MMM-yyyy HH:mm");
		if( date != null ){
			released_date = sdf.format(dateChooser.getDate());
		}
		
		TCComponentItemRevision[] revisions =  OSpecMainDlg.getOspecRevision(gModel, project, "", released_date);
		
		try {
			
			DefaultTableModel model = (DefaultTableModel)table.getModel();
			for (int i = model.getRowCount() - 1; i >= 0; i--) {
				model.removeRow(i);
			}
			
			HashMap<String, TCComponentItemRevision> revisionMap = new HashMap();
			for( int i = 0; revisions != null && i < revisions.length; i++){
				String itemId = revisions[i].getProperty("item_id");
				String revId = revisions[i].getProperty("item_revision_id");
				revisionMap.put(itemId + "/" + revId, revisions[i]);
			}
			String[] keys = revisionMap.keySet().toArray(new String[revisionMap.size()]);
			Arrays.sort(keys);
			
			sdf  = new SimpleDateFormat("yyyy-MM-dd");
			for( int i = 0; keys != null && i < keys.length; i++){
				TCComponentItemRevision revision = revisionMap.get(keys[i]);
				String itemId = revision.getProperty("item_id");
				String revId = revision.getProperty("item_revision_id");
				gModel = revision.getItem().getProperty("s7_Gmodel");
				project = revision.getItem().getProperty("s7_Project");
				TCProperty tcProp = revision.getTCProperty("s7_OspecReleasedDate");
				Date releasedDate = tcProp.getDateValue();
				
				String dateStr = sdf.format(releasedDate);
				
				Vector row = new Vector();
				row.add(gModel);
				row.add(project);
				row.add(dateStr);
				row.add(itemId + "-" + revId);
				row.add(revision);
				model.addRow(row);
			}
			
			TableColumnModel cm = table.getColumnModel();
			if( cm.getColumnCount() > 4){
				cm.removeColumn(cm.getColumn(4));
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw e;
		}		
		return null;
	}
	
	public int importOspecToTC(OSpec ospec, File file) throws Exception{
		String osiNo = ospec.getOspecNo().substring(0, ospec.getOspecNo().lastIndexOf("-"));
		
		TCComponentItemRevision latestRevision = null, revision = null;
		try{
			revision = CustomUtil.findItemRevision("S7_OspecSetRevision", osiNo, ospec.getVersion());
			if( revision != null ){
				return ALREADY_EXIST_ITEM_REVISION;
			}
			
			TCComponentItem item = CustomUtil.findItem("S7_OspecSet", osiNo);
			if( item == null ){
				item = CustomUtil.createItem("S7_OspecSet", osiNo, ospec.getVersion(), osiNo, osiNo);
				item.setProperties(new String[]{"s7_Gmodel","s7_Project"}, new String[]{ospec.getgModel(), ospec.getProject()});
				revision = item.getLatestItemRevision();
			}else{
				latestRevision = item.getLatestItemRevision();
				revision = latestRevision.saveAs(ospec.getVersion());
			}
			
			TCProperty tcproperty = revision.getTCProperty("s7_OspecReleasedDate");
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
			tcproperty.setDateValue(sdf.parse(ospec.getReleasedDate() + " 00:00:00"));
			
			revision.lock();
			revision.setTCProperty(tcproperty);
			revision.save();
			revision.unlock();
			
			if( latestRevision != null ){
				AIFComponentContext[] context = revision.getChildren(SYMCECConstant.ITEM_DATASET_REL);
				for( int i = 0; context != null && i < context.length; i++){
					TCComponentDataset ds = (TCComponentDataset)context[i].getComponent();
					String name = ds.getProperty("object_name");
					if( (latestRevision.getProperty("item_id") + "-" + latestRevision.getProperty("item_revision_id")).equals(name)){
						revision.remove(SYMCECConstant.ITEM_DATASET_REL, ds);
					}
				}
			}
			TCComponentDatasetType datasetType = (TCComponentDatasetType) revision.getSession().getTypeComponent("Dataset");
			TCComponentDataset dataset = datasetType.create(ospec.getOspecNo(), "", "MSExcel");
			dataset.setFiles(new String[] { file.getAbsolutePath() }, new String[] { "MSExcel" },
					new String[] { "Plain" }, new String[] { "excel" });
			
			revision.lock();
			revision.add(SYMCECConstant.ITEM_DATASET_REL, dataset);
			revision.unlock();
		}catch(Exception e){
			throw e;
		}finally{
//			if( revision != null ){
//				revision.unlock();
//			}
		}
		
		return CREATION_SUCCESS;
	}
	
	public TCComponentItemRevision getSelectedOSpec() {
		return ospecRevision;
	}

}

package com.ssangyong.commands.ospec.panel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

import com.ssangyong.commands.ospec.OSpecCompareDlg;
import com.ssangyong.commands.ospec.OSpecMainDlg;
import com.ssangyong.commands.ospec.OspecReportExportOperation;
import com.ssangyong.commands.ospec.OspecSelectOptGroupDlog;
import com.ssangyong.commands.ospec.op.OSpec;
import com.ssangyong.commands.ospec.op.OpGroup;
import com.ssangyong.commands.ospec.op.OpValueName;
import com.ssangyong.common.utils.CustomUtil;
import com.ssangyong.common.utils.DatasetService;
import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.aif.InterfaceAIFOperationListener;
import com.teamcenter.rac.kernel.IRelationName;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.util.MessageBox;

public class WorkspacePanel extends JPanel {

	private JComboBox cbOpGroup = null;
	private OSpecMainDlg parentDlg = null;
	private JCheckBox chkOnlyOwnOp = null;
	private JTable groupTable = null;
	private ArrayList<OpGroup> selectedOpGroup = new ArrayList();
	private OSpecTable ospecTable = null;
	private JPanel ospecViewPanel = null;
	private JDialog comparableDlg;
	private Vector selectedOptionData = null;
	private ArrayList<OpGroup> groupList = null;;
	
	/**
	 * Create the panel.
	 * @throws Exception 
	 */
	public WorkspacePanel(OSpecMainDlg parentDlg) throws Exception {
		this.parentDlg = parentDlg;
		
		init();
	}
	
	private void init() throws Exception{
		setLayout(new BorderLayout(0, 0));
		{
			JPanel panel = new JPanel();
			add(panel, BorderLayout.WEST);
			panel.setLayout(new BorderLayout(0, 0));
			{
				JPanel panel_1 = new JPanel();
				panel_1.setBorder(new TitledBorder(null, "Option Groups", TitledBorder.LEADING, TitledBorder.TOP, null, null));
				panel.add(panel_1, BorderLayout.CENTER);
				panel_1.setLayout(new BorderLayout(0, 0));
				{
					chkOnlyOwnOp = new JCheckBox("Only own Option Group");
					chkOnlyOwnOp.setSelected(true);
					chkOnlyOwnOp.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							//ArrayList<OpGroup> groupList;
							groupList = new ArrayList<OpGroup>();
							try {
								cbOpGroup.removeAllItems();
								groupList = WorkspacePanel.this.parentDlg.getOptionGroup(chkOnlyOwnOp.isSelected());
								for(int i = 0;groupList != null && i < groupList.size();i++){
									cbOpGroup.addItem(groupList.get(i));
								}
							} catch (Exception e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
							
						}
					});
					panel_1.add(chkOnlyOwnOp, BorderLayout.NORTH);
				}
				{
					JPanel panel_2 = new JPanel();
					panel_1.add(panel_2, BorderLayout.CENTER);
					panel_2.setLayout(new BorderLayout(0, 0));
					{
						
						Vector header = new Vector();
						header.add("Owner");
						header.add("Option Group");
						
						DefaultTableModel model = new DefaultTableModel(null, header){

							@Override
							public boolean isCellEditable(int arg0, int arg1) {
								// TODO Auto-generated method stub
								return false;
							}
							
						};
						groupTable = new JTable(model);
						TableColumnModel cm = groupTable.getColumnModel();
						int[] width = new int[]{50, 120};
					    for( int i = 0; i < cm.getColumnCount(); i++){
					    	cm.getColumn(i).setPreferredWidth(width[i]);
					    }
					    
						JScrollPane scrollPane = new JScrollPane(groupTable);
						scrollPane.setPreferredSize(new Dimension(200, 402));
						panel_2.add(scrollPane);
					}
				}
				{
					JPanel panel_2 = new JPanel();
					FlowLayout flowLayout = (FlowLayout) panel_2.getLayout();
					flowLayout.setAlignment(FlowLayout.LEADING);
					panel_1.add(panel_2, BorderLayout.SOUTH);
					{
						cbOpGroup = new JComboBox();
						//ArrayList<OpGroup> groupList = parentDlg.getOptionGroup(chkOnlyOwnOp.isSelected());
						groupList = new ArrayList<OpGroup>();
						groupList = parentDlg.getOptionGroup(chkOnlyOwnOp.isSelected());
						for(int i = 0;groupList != null && i < groupList.size();i++){
							cbOpGroup.addItem(groupList.get(i));
						}
						panel_2.add(cbOpGroup);
					}
					{
						JButton btnNewButton_3 = new JButton("");
						btnNewButton_3.setPreferredSize(new Dimension(25, 25));
						btnNewButton_3.setIcon(new ImageIcon(WorkspacePanel.class.getResource("/com/teamcenter/rac/aif/images/add_16.png")));
						btnNewButton_3.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent arg0) {
								DefaultTableModel model = (DefaultTableModel)groupTable.getModel();
								
								OpGroup opGroup = (OpGroup)cbOpGroup.getSelectedItem();
								if( !selectedOpGroup.contains(opGroup)){
									selectedOpGroup.add(opGroup);
								}
								
								parentDlg.removeAllRow(groupTable);
								Collections.sort(selectedOpGroup);
								for( OpGroup group:selectedOpGroup){
									Vector row = new Vector();
									row.add(group.getOwner());
									row.add(group);
									model.addRow(row);
								}
								
							}
						});
						panel_2.add(btnNewButton_3);
					}
					{
						/**
						 * [20160719]Option Group Name 을 리스트에서 검색하여 추가할수 있는 기능 추가
						 */
						JButton searchButton = new JButton("");
						searchButton.setPreferredSize(new Dimension(25, 25));
						//searchButton.setIcon(new ImageIcon(WorkspacePanel.class.getResource("/icons/search_16.png")));
						searchButton.setIcon(new ImageIcon(WorkspacePanel.class.getResource("/com/teamcenter/rac/common/images/add_content_16.png")));
						searchButton.setToolTipText("add selected items in list");
						searchButton.addActionListener( new ActionListener() {
							
							@Override
							public void actionPerformed(ActionEvent paramActionEvent) {
	
								OspecSelectOptGroupDlog dialog = new OspecSelectOptGroupDlog(parentDlg,groupList);
								dialog.setVisible(true);
								
								ArrayList<OpGroup> selectedOpGroupList = dialog.getSelectOpGroupList();
								
								for(OpGroup findedOpGroup : selectedOpGroupList)
								{
									String findedOpGroupName = findedOpGroup.getOpGroupName();
									String findedOwner = findedOpGroup.getOwner();
									boolean isAlreadyExist = false;
									for(OpGroup savedOpGroup :  selectedOpGroup)
									{
										if (findedOpGroupName.equals(savedOpGroup.getOpGroupName()) && 
												findedOwner.equals(savedOpGroup.getOwner())) 
											isAlreadyExist = true;
									}
									
									if(isAlreadyExist)
										continue;
									selectedOpGroup.add(findedOpGroup);
								}
								
								parentDlg.removeAllRow(groupTable);
								Collections.sort(selectedOpGroup);
								DefaultTableModel model = (DefaultTableModel)groupTable.getModel();
								for( OpGroup group:selectedOpGroup){
									Vector<Object> row = new Vector<Object>();
									row.add(group.getOwner());
									row.add(group);
									model.addRow(row);
								}
								
							}
						});
						panel_2.add(searchButton);
					}
					{
						JButton button = new JButton("");
						button.setPreferredSize(new Dimension(25, 25));
						button.setIcon(new ImageIcon(WorkspacePanel.class.getResource("/com/teamcenter/rac/aif/images/remove_16.png")));
						button.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								int rowIdx = groupTable.getSelectedRow();
								
								if( rowIdx < 0 ) return;
								
								int modelIdx = groupTable.convertRowIndexToModel(rowIdx);
								
								DefaultTableModel model = (DefaultTableModel)groupTable.getModel();
								OpGroup opGroup = (OpGroup)model.getValueAt(modelIdx, 1);
								selectedOpGroup.remove(opGroup);
								model.removeRow(modelIdx);
							}
						});
						panel_2.add(button);
					}
				}
			}
			{
				JPanel panel_1 = new JPanel();
				FlowLayout flowLayout = (FlowLayout) panel_1.getLayout();
				flowLayout.setAlignment(FlowLayout.TRAILING);
				panel.add(panel_1, BorderLayout.SOUTH);
				{
					JButton btnLoad = new JButton("Load");
					btnLoad.setIcon(new ImageIcon(WorkspacePanel.class.getResource("/com/ssangyong/common/images/ok_16.png")));
					btnLoad.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							
							try {
								load();
							} catch (Exception e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
								MessageBox.post(WorkspacePanel.this.parentDlg, e1.getMessage(), "ERROR", MessageBox.ERROR);
							}
						}
					});
					panel_1.add(btnLoad);
				}
			}
		}
		{
			ospecViewPanel = new JPanel();
			ospecViewPanel.setBorder(new TitledBorder(null, "Selected Options", TitledBorder.LEADING, TitledBorder.TOP, null, null));
			add(ospecViewPanel, BorderLayout.CENTER);
			ospecViewPanel.setLayout(new BorderLayout(0, 0));
			{
				Vector<Vector> data = new Vector();
				ospecTable = new OSpecTable(parentDlg.getOspec(), data);
				ospecViewPanel.add(ospecTable.getOspecTable());
			}
			{
				JPanel panel = new JPanel();
				FlowLayout flowLayout = (FlowLayout) panel.getLayout();
				flowLayout.setAlignment(FlowLayout.TRAILING);
				ospecViewPanel.add(panel, BorderLayout.SOUTH);
				{
					JButton btnCompare = new JButton("Compare with O/Spec");
					btnCompare.setIcon(new ImageIcon(WorkspacePanel.class.getResource("/com/teamcenter/rac/common/images/compare2d_16.png")));
					btnCompare.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent arg0) {
							
							try {
								compare();								
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
								MessageBox.post(WorkspacePanel.this.parentDlg, e.getMessage(), "ERROR", MessageBox.ERROR);
							}							
						}
					});
					panel.add(btnCompare);
				}
				{
					final JButton btnSendToPub = new JButton("Send to publication");
					btnSendToPub.setIcon(new ImageIcon(WorkspacePanel.class.getResource("/icons/sendto_16.png")));
					btnSendToPub.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							
							if( groupTable.getRowCount() < 1){
								MessageBox.post(WorkspacePanel.this.parentDlg, "Please select a option.", "INFORMATION", MessageBox.INFORMATION);
								return;
							}
							
//							final WaitProgressBar waitBar = new WaitProgressBar(parentDlg);
//							waitBar.start();
							
							btnSendToPub.setEnabled(false);
							
//							waitBar.setStatus("Loading...");
							SendToPublishOperation operation = new SendToPublishOperation();
							operation.addOperationListener(new InterfaceAIFOperationListener() {
								
								@Override
								public void startOperation(String arg0) {
									// TODO Auto-generated method stub
									Calendar c = Calendar.getInstance();
									SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
									String startTime = sdf.format(c.getTime());
									System.out.println("Start Time : " + startTime);
								}
								
								@Override
								public void endOperation() {
									// TODO Auto-generated method stub
									btnSendToPub.setEnabled(true);
									Calendar c = Calendar.getInstance();
									SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
									String endTime = sdf.format(c.getTime());
									System.out.println("End Time : " + endTime);
//									if( waitBar != null){
//										waitBar.dispose();
//									}
								}
							});
							CustomUtil.getTCSession().queueOperation(operation);
						}
					});
					panel.add(btnSendToPub);
				}
				{
					JButton btnExport = new JButton("Export");
					btnExport.setIcon(new ImageIcon(WorkspacePanel.class.getResource("/com/ssangyong/common/images/excel_16.png")));
					panel.add(btnExport);
					btnExport.addActionListener(new ActionListener() {
						
						@Override
						public void actionPerformed(ActionEvent paramActionEvent) {
							

							try {
								JFileChooser fileChooser = new JFileChooser();
								fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY );
								Calendar now = Calendar.getInstance();
								SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm");
								sdf.format(now.getTime());
								fileChooser.setFileFilter(new FileFilter(){
									public boolean accept(File f) {
										if( f.isFile()){
											return f.getName().endsWith("xls");
										}
										return false;
									}
	
									public String getDescription() {
										return "*.xls";
									}
								});
								
								File defaultFile = new File(ospecTable.getOspec().getOspecNo() + "_OPTION_GROUP_" + sdf.format(now.getTime()) + ".xls");
								fileChooser.setSelectedFile(defaultFile);
								
								int result = fileChooser.showSaveDialog(parentDlg);
								if( result != JFileChooser.APPROVE_OPTION)
									return;
								File selectedFile = fileChooser.getSelectedFile();
								
								if (selectedFile.exists()) {
									int ret = JOptionPane.showConfirmDialog(null, selectedFile.getAbsoluteFile() + " File already exists.\nDo you want to overwrite it?", "Confirm", JOptionPane.YES_NO_OPTION);
									if (ret != JOptionPane.YES_OPTION)
						    			return;
								}
								
								TCComponentItemRevision ospecRevision = parentDlg.getOspecRev();
								TCComponent refCom = ospecRevision.getRelatedComponent(IRelationName.IMAN_reference);
								File[] files = DatasetService.getFiles((TCComponentDataset)refCom);
								files[0].renameTo(selectedFile);
								
								OspecReportExportOperation exportOp = new OspecReportExportOperation(WorkspacePanel.this.parentDlg, ospecTable,selectedFile);
								CustomUtil.getTCSession().queueOperation(exportOp);

							} catch (Exception e) {
								MessageBox.post(WorkspacePanel.this.parentDlg, e.getMessage(), "ERROR", MessageBox.ERROR);
								e.printStackTrace();
							}

						}
					});
				}
			}
		}
	}
	
	private void load() throws Exception{
		if( groupTable.getRowCount() < 1){
			
			ospecViewPanel.remove(ospecTable.getOspecTable());
			Vector data = new Vector();
			ospecTable = new OSpecTable(parentDlg.getOspec(), data);
			ospecViewPanel.add(ospecTable.getOspecTable(), BorderLayout.CENTER);
			ospecViewPanel.revalidate();
			return;
		}
		
		ArrayList<String> tmpList = new ArrayList();
		DefaultTableModel model = (DefaultTableModel)groupTable.getModel();
		for( int i = 0; i < model.getRowCount(); i++){
			OpGroup opGroup = (OpGroup)model.getValueAt(i, 1);
			ArrayList<OpValueName> list = parentDlg.getOptionGroupDetail(opGroup.getOpGroupName(), chkOnlyOwnOp.isSelected());
			
			if( list != null && !list.isEmpty()){
				opGroup.setOptionList(list);
				if( tmpList.isEmpty()){
					for( OpValueName opValue:list){
						tmpList.add(opValue.getOption());
					}
				}else{
					for( OpValueName opValue : list){
						if( !tmpList.contains(opValue.getOption())){
							tmpList.add(opValue.getOption());
						}
					}
				}
			}
		}
		
		if( !tmpList.isEmpty()){
			OSpec ospec = parentDlg.getOspec();
			selectedOptionData = OSpecTable.getData(ospec, tmpList);
			Vector cloneData = OSpecTable.getData(ospec, tmpList);
			
			ospecViewPanel.remove(ospecTable.getOspecTable());
			ospecTable = new OSpecTable(parentDlg.getOspec(), cloneData, true);
			ospecViewPanel.add(ospecTable.getOspecTable(), BorderLayout.CENTER);
			ospecViewPanel.revalidate();
			
		}
		
	}

	public void refreshOpGroupList() throws Exception{
		groupList = new ArrayList<OpGroup>();
		cbOpGroup.removeAllItems();
		//ArrayList<OpGroup> groupList  = parentDlg.getOptionGroup(chkOnlyOwnOp.isSelected());
		groupList  = parentDlg.getOptionGroup(chkOnlyOwnOp.isSelected());
		for(int i = 0;groupList != null && i < groupList.size();i++){
			cbOpGroup.addItem(groupList.get(i));
		}
	}
	
	private void compare() throws Exception{
		
		if( comparableDlg != null){
			comparableDlg.dispose();
		}
		
		HashMap<String, HashMap<String, ArrayList<OpGroup>>> opGroupMap = new HashMap();
		
		OSpec sourceOspec = parentDlg.getOspec();
		OSpecTable sourceOSpecTable = new OSpecTable(sourceOspec, selectedOptionData);
		OSpecTable targetOSpecTable = new OSpecTable(sourceOspec, ospecTable.getData());
		
		//차후에 Vector를 Clone하므로 데이타 Clone가능하도록 clone OverRide함.
		Vector<Vector> onlySourceData = sourceOSpecTable.minus(targetOSpecTable.getData(), false);
		Vector<Vector> onlyTargetData = targetOSpecTable.minus(sourceOSpecTable.getData(), false);
		
		parentDlg.getReferedOptionGroup(opGroupMap, sourceOSpecTable.getData(), sourceOspec.getProject());
		parentDlg.getReferedOptionGroup(opGroupMap, targetOSpecTable.getData(), sourceOspec.getProject());
		
		OSpecTable onlySourceOSpecTable = new OSpecTable(sourceOspec, onlySourceData);
		OSpecTable onlyTargetOSpecTable = new OSpecTable(sourceOspec, onlyTargetData);
		
		comparableDlg = new OSpecCompareDlg(sourceOSpecTable, targetOSpecTable, onlySourceOSpecTable, onlyTargetOSpecTable, opGroupMap);
		comparableDlg.setVisible(true);

	}	
	
	class SendToPublishOperation extends AbstractAIFOperation{

		@Override
		public void executeOperation() throws Exception {
			WorkspacePanel.this.parentDlg.sendToPublish(ospecTable, selectedOpGroup);
		}

	}
}

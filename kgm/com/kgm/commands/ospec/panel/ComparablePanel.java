package com.kgm.commands.ospec.panel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import com.kgm.commands.ospec.OSpecCompareDlg;
import com.kgm.commands.ospec.OSpecMainDlg;
import com.kgm.commands.ospec.op.OSpec;
import com.kgm.commands.ospec.op.OpGroup;
import com.kgm.commands.ospec.op.OpUtil;
import com.kgm.common.WaitProgressBar;
import com.kgm.common.remote.DataSet;
import com.kgm.common.remote.SYMCRemoteUtil;
import com.kgm.common.utils.CustomUtil;
import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCProperty;
import com.teamcenter.rac.util.MessageBox;
import com.toedter.calendar.JDateChooser;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;

/**
 * [20150529] [ymjang] NullPointerException 오류 수정 (sourceOspec --> targetOspec 로 변경함.)
 * [20150608] [ymjang] 나중 Project 를 항상 Target 으로 지정해야 함.
 */
public class ComparablePanel extends JPanel {

	private JComboBox cbGmodel = null;
	private JComboBox cbProject = null;
	private JDateChooser dateChooser;
	private JTable comparableOspecTable;
	private JLabel labelGModel = null;
	private JLabel labelProject = null;
	private JLabel labelVersion = null;
	private JLabel labelDate = null;
	private JLabel labelOsiNo = null;
	private OSpecMainDlg parentDlg;
	private OSpecCompareDlg comparableDlg;
	private JButton btnCompare = null;
	
	public ComparablePanel(OSpecMainDlg parentDlg) throws Exception {
		this.parentDlg = parentDlg;
		
		init();
			
	}
	
	private void init() throws Exception{
		setLayout(new BorderLayout(0, 0));
		{
			JPanel panel = new JPanel();
			add(panel, BorderLayout.CENTER);
			panel.setLayout(new BorderLayout(0, 0));
			{
				JPanel panel_1 = new JPanel();
				panel_1.setBorder(new TitledBorder(null, "Target O/Spec Selection", TitledBorder.LEADING, TitledBorder.TOP, null, null));
				panel.add(panel_1, BorderLayout.CENTER);
				panel_1.setLayout(new BorderLayout(0, 0));
				{
					JPanel panel_1_1 = new JPanel();
					panel_1.add(panel_1_1, BorderLayout.NORTH);
					panel_1_1.setLayout(new BorderLayout(0, 0));
					{
						JPanel panel_2 = new JPanel();
						FlowLayout flowLayout = (FlowLayout) panel_2.getLayout();
						flowLayout.setAlignment(FlowLayout.LEADING);
						panel_1_1.add(panel_2);
						{
							JLabel lblNewLabel_8 = new JLabel("G-Model");
							panel_2.add(lblNewLabel_8);
						}
						{
							cbGmodel = new JComboBox();
							cbGmodel.addItemListener(new ItemListener() {
								public void itemStateChanged(ItemEvent event) {
									if( event.getStateChange() == ItemEvent.SELECTED){
//										String gModelStr = (String)cbGmodel.getSelectedItem();
										try{
											OpUtil.refreshProject(cbGmodel, cbProject);
										}catch(Exception e){
											MessageBox.post(parentDlg, e.getMessage(), "ERROR", MessageBox.ERROR);
										}
									}									
								}
							});
							
							panel_2.add(cbGmodel);
						}
						{
							JLabel lblNewLabel_9 = new JLabel("   Project");
							panel_2.add(lblNewLabel_9);
						}
						{
							cbProject = new JComboBox();
							panel_2.add(cbProject);
						}
						{
							JLabel lblNewLabel_11 = new JLabel("  Date");
							panel_2.add(lblNewLabel_11);
						}
						{
							dateChooser = new JDateChooser(null, "yyyy-MM-dd", false, null);
							panel_2.add(dateChooser);
						}
					}
					{
						JPanel panel_2 = new JPanel();
						panel_1_1.add(panel_2, BorderLayout.EAST);
						{
							JButton btnNewButton_2 = new JButton("Search");
							btnNewButton_2.setIcon(new ImageIcon(ComparablePanel.class.getResource("/com/teamcenter/rac/pse/genealogy/images/watcher_16.png")));
							btnNewButton_2.addActionListener(new ActionListener() {
								public void actionPerformed(ActionEvent arg0) {
									try {
										refreshTable();
									} catch (Exception e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}
							});
							panel_2.add(btnNewButton_2);
						}
					}
				}
				{
					JPanel panel_2 = new JPanel();
					panel_1.add(panel_2);
					panel_2.setLayout(new BorderLayout(0, 0));
					{
						Vector headerVector = new Vector();
						headerVector.add("G-Model");
						headerVector.add("Project");
						headerVector.add("Released Date");
						headerVector.add("OSI-No");
						headerVector.add("ospec");
						DefaultTableModel model = new DefaultTableModel(null, headerVector) {
							public Class getColumnClass(int col) {
								if( col < 4 ){
									return String.class;
								}else{
									return Object.class;
								}
							}

							public boolean isCellEditable(int row, int col) {
								return false;
							}
					    };

					    TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(model);
						comparableOspecTable = new JTable(model);
						TableColumnModel cm = comparableOspecTable.getColumnModel();
					    cm.removeColumn(cm.getColumn(4));
						comparableOspecTable.setRowSorter(sorter);
						comparableOspecTable.addMouseListener(new MouseAdapter(){

							@Override
							public void mouseReleased(MouseEvent e) {
								// TODO Auto-generated method stub
								if( comparableOspecTable.getSelectedRow() > -1){
									DefaultTableModel model = (DefaultTableModel)comparableOspecTable.getModel();
									int rowIdx = comparableOspecTable.convertRowIndexToModel(comparableOspecTable.getSelectedRow());
									String gModel = (String)model.getValueAt(rowIdx, 0);
									String project = (String)model.getValueAt(rowIdx, 1);
									String releasedDate = (String)model.getValueAt(rowIdx, 2);
									String osiNo = (String)model.getValueAt(rowIdx, 3);
									String version = osiNo.substring(osiNo.lastIndexOf("-") + 1);
									
									labelGModel.setText(gModel);
									labelProject.setText(project);
									labelVersion.setText(version);
									labelDate.setText(releasedDate);
									labelOsiNo.setText(osiNo);
								}										
								super.mouseReleased(e);
							}
							
						});
						
						comparableOspecTable.addMouseListener(new MouseAdapter(){

							@Override
							public void mouseReleased(MouseEvent e) {
								if( e.getClickCount()==2 && SwingUtilities.isLeftMouseButton(e) 
										&& e.isControlDown()==false) {
									btnCompare.doClick();
								}
							}
							
						});
						
						JScrollPane pane = new JScrollPane(comparableOspecTable);
						panel_2.add(pane);
					}
				}
			}
			{
				JPanel panel_1 = new JPanel();
				panel_1.setBorder(new TitledBorder(null, "Selected O/Spec", TitledBorder.LEADING, TitledBorder.TOP, null, null));
				panel.add(panel_1, BorderLayout.EAST);
				panel_1.setLayout(new BorderLayout(0, 0));
				{
					JPanel panel_2 = new JPanel();
					panel_2.setPreferredSize(new Dimension(180, 120));
					panel_1.add(panel_2, BorderLayout.CENTER);
					panel_2.setLayout(new GridLayout(5, 1, 0, 0));
					{
						JLabel lblNewLabel = new JLabel("G-Model : ");
						panel_2.add(lblNewLabel);
					}
					{
						labelGModel = new JLabel("-");
						panel_2.add(labelGModel);
					}
					{
						JLabel lblNewLabel_2 = new JLabel("Project : ");
						panel_2.add(lblNewLabel_2);
					}
					{
						labelProject = new JLabel("-");
						panel_2.add(labelProject);
					}
					{
						JLabel lblNewLabel_4 = new JLabel("Version : ");
						panel_2.add(lblNewLabel_4);
					}
					{
						labelVersion = new JLabel("-");
						panel_2.add(labelVersion);
					}
					{
						JLabel labelDate = new JLabel("Date : ");
						panel_2.add(labelDate);
					}
					{
						labelDate = new JLabel("-");
						panel_2.add(labelDate);
					}
					{
						JLabel lblNewLabel_1 = new JLabel("OSI-NO : ");
						panel_2.add(lblNewLabel_1);
					}
					{
						labelOsiNo = new JLabel("-");
						panel_2.add(labelOsiNo);
					}
				}
				{
					JPanel panel_2 = new JPanel();
					panel_2.setPreferredSize(new Dimension(10, 350));
					panel_1.add(panel_2, BorderLayout.SOUTH);
					{
						btnCompare = new JButton("Compare");
						btnCompare.setIcon(new ImageIcon(ComparablePanel.class.getResource("/com/teamcenter/rac/common/images/compare2d_16.png")));
						btnCompare.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent arg0) {
								final WaitProgressBar waitProgress = new WaitProgressBar(parentDlg);
								waitProgress.start();
								waitProgress.setStatus("In comparison....");
								
								btnCompare.setEnabled(false);
								CustomUtil.getTCSession().queueOperation(new AbstractAIFOperation(){

									@Override
									public void executeOperation() throws Exception {
										try{
											compare(waitProgress);
											waitProgress.dispose();
										}catch(Exception e){
											waitProgress.setStatus(e.getMessage());
											waitProgress.setShowButton(true);
										}finally{
											btnCompare.setEnabled(true);
										}
									}
									
								});
							}
						});
						panel_2.add(btnCompare);
					}
				}
			}
		}
		OpUtil.refreshGmodel(cbGmodel);
		OpUtil.refreshProject(cbGmodel, cbProject);
	}
	
	private void compare(WaitProgressBar pregressBar){
		if( comparableDlg != null){
			comparableDlg.dispose();
		}
		
		HashMap<String, HashMap<String, ArrayList<OpGroup>>> referedOpGroupMap = new HashMap();
		
		int idx = comparableOspecTable.getSelectedRow();
		if( idx < 0) return;
		
		DefaultTableModel model = (DefaultTableModel)comparableOspecTable.getModel();
		int modelIdx = comparableOspecTable.convertRowIndexToModel(idx);
		TCComponentItemRevision revision = (TCComponentItemRevision)model.getValueAt(modelIdx, 4);
		OSpec sourceOspec = parentDlg.getOspec();
		OSpec targetOspec = null;
		try {
			targetOspec = parentDlg.getOSpec(revision);
			
			// [20150608] [ymjang] 나중 Project 를 항상 Target 으로 지정해야 함.
			// [20150608] [jclee] Project 순서와 관계없이 수행. 
//			if (sourceOspec.getOspecNo().compareTo(targetOspec.getOspecNo()) > 0)
//			{
//				MessageBox.post(parentDlg, "You must select target to " + sourceOspec.getOspecNo() + "\n\r" + "Source Project must be prior to Target Project!", "Information", MessageBox.INFORMATION);
//				return;
//			}
			
			OSpecTable sourceOSpecTable = new OSpecTable(sourceOspec, null);
			OSpecTable targetOSpecTable = new OSpecTable(targetOspec, null);
			
			//차후에 Vector를 Clone하므로 데이타 Clone가능하도록 clone OverRide함.
			Vector<Vector> onlySourceData = sourceOSpecTable.minus(targetOSpecTable.getData(), false);
			Vector<Vector> onlyTargetData = targetOSpecTable.minus(sourceOSpecTable.getData(), false);
			
			parentDlg.getReferedOptionGroup(referedOpGroupMap, sourceOSpecTable.getData(), sourceOspec.getProject());
			
    		// [20150529] [ymjang] NullPointerException 오류 수정 (sourceOspec --> targetOspec 로 변경함.)
			parentDlg.getReferedOptionGroup(referedOpGroupMap, targetOSpecTable.getData(), targetOspec.getProject());
			
			OSpecTable onlySourceOSpecTable = new OSpecTable(sourceOspec, onlySourceData);
			OSpecTable onlyTargetOSpecTable = new OSpecTable(targetOspec, onlyTargetData);
			
			comparableDlg = new OSpecCompareDlg(sourceOSpecTable, targetOSpecTable, onlySourceOSpecTable, onlyTargetOSpecTable, referedOpGroupMap);
			if(pregressBar != null){
				pregressBar.close();
			}
			comparableDlg.showDialog();
		} catch (Exception e) {
			if(pregressBar != null){
				pregressBar.close();
			}
			e.printStackTrace();
			MessageBox.post(parentDlg, e.getMessage(), "ERROR", MessageBox.ERROR);
		}
	}
	

	
	public ArrayList<HashMap<String, String>> refreshTable() throws Exception{
		
		String gModel = cbGmodel.getSelectedItem().toString();
		String project = cbProject.getSelectedItem().toString();
		String released_date = null;
		Date date = dateChooser.getDate();
		SimpleDateFormat sdf  = new SimpleDateFormat("dd-MMM-yyyy HH:mm");
		if( date != null ){
			released_date = sdf.format(dateChooser.getDate());
		}
		
		TCComponentItemRevision[] revisions =  OSpecMainDlg.getOspecRevision(gModel, project, "", released_date);
		
		SYMCRemoteUtil remote = new SYMCRemoteUtil();
		DataSet ds = new DataSet();
		try {
			
			DefaultTableModel model = (DefaultTableModel)comparableOspecTable.getModel();
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
			
//			sdf  = new SimpleDateFormat("yyyy-MM-dd");
//			for( int i = 0; revisions != null && i < revisions.length; i++){
//				String itemId = revisions[i].getProperty("item_id");
//				String revId = revisions[i].getProperty("item_revision_id");
//				gModel = revisions[i].getItem().getProperty("s7_Gmodel");
//				project = revisions[i].getItem().getProperty("s7_Project");
//				TCProperty tcProp = revisions[i].getTCProperty("s7_OspecReleasedDate");
//				Date releasedDate = tcProp.getDateValue();
//				String dateStr = sdf.format(releasedDate);
//				
//				Vector row = new Vector();
//				row.add(gModel);
//				row.add(project);
//				row.add(dateStr);
//				row.add(itemId + "-" + revId);
//				row.add(revisions[i]);
//				model.addRow(row);
//			}
			
			TableColumnModel cm = comparableOspecTable.getColumnModel();
			if( cm.getColumnCount() > 4){
				cm.removeColumn(cm.getColumn(4));
			}
		} catch (Exception e) {
			throw e;
		}		
		return null;	
	}	
}

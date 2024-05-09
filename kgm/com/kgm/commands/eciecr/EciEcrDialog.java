package com.kgm.commands.eciecr;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.widgets.Shell;

import com.kgm.commands.ec.SYMCECConstant;
import com.kgm.commands.namegroup.IconColorCellRenderer;
import com.kgm.commands.ospec.op.OpValueName;
import com.kgm.common.WaitProgressBar;
import com.kgm.common.remote.DataSet;
import com.kgm.common.remote.SYMCRemoteUtil;
import com.kgm.common.utils.CustomUtil;
import com.kgm.common.utils.SYMTcUtil;
import com.kgm.common.utils.StringUtil;
import com.teamcenter.rac.aif.AbstractAIFDialog;
import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.aif.InterfaceAIFOperationListener;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.IPropertyName;
import com.teamcenter.rac.kernel.Markpoint;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;
import com.teamcenter.rac.util.VerticalLayout;


/**
 * ���ʻ��� : 2019-12-01
 * ECO ECR,ECR Interface Dialog
 * ����Ϸ�� ECO�� ���� ECI, ECR ��ȣ�� �����ϰ� Interface �ϱ� ���� ���α׷�
 * ������������� �
 * ECO �ߺ� ǥ�ø� ���� ���� ���� �̹ݿ� ����(2019-12-24)
 * @author 188955
 *
 */
public class EciEcrDialog extends AbstractAIFDialog {
	/** TC Registry */
    private Registry registry;
    private TCSession session;
    
    private String dualEcoNo = "";
    
	private JPanel contentPanel = new JPanel();
	private String[] column = {"ECO No", "ECI No", "ECR No"};
	public DefaultTableModel tableModel = null;
	private JTable table = null;
	Shell shell;
	public static final String ECOHISTORY_QUERY_SERVICE = "com.kgm.service.ECOHistoryService";
	/**
	 * Create the dialog.
	 */
	public EciEcrDialog(Shell paramShell, TCSession session) throws Exception {
		super(AIFUtility.getActiveDesktop().getFrame(), false);
		this.shell = paramShell;
		this.registry = Registry.getRegistry(this);
		this.session = session;
		
		initUI();
	}
	
	private void initUI() throws Exception{
		setTitle("ECO ECI,ECR Interface");
		
		contentPanel.add(createPanel());
		
		getContentPane().setLayout(new VerticalLayout(5,5,5,5,5));
		getContentPane().add("unbound.bind.center.center", contentPanel);
		
		setPreferredSize(new Dimension(900,500));
	}
	
	private JPanel createPanel() {
		contentPanel = new JPanel(new VerticalLayout());
		contentPanel.add("top.bind.center.center", createControllBtnPanel());
		contentPanel.add("unbound.bind.center.center", createTablePanel());
		contentPanel.add("bottom.bind.center.center", createSaveBtnPanel());
		
		return contentPanel;
	}
	
	private JPanel createControllBtnPanel(){

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		
		JButton addButton = new JButton("ADD");
		addButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionevent) {
				addRow();
			}
		});
		
		JButton deleteButton = new JButton("DELETE");
		deleteButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionevent) {
				deleteRow();
			}
		});
		
		buttonPanel.add(addButton);
		buttonPanel.add(deleteButton);

		return buttonPanel;
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
		table.addMouseListener(new MouseAdapter());
		table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
		((DefaultTableCellRenderer)table.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);
		
		TableColumnModel model = table.getColumnModel();
		
		Integer[] aligns = new Integer[]{SwingConstants.CENTER, SwingConstants.CENTER, SwingConstants.CENTER};
		int[] width = {1,300,300};
		
		for(int i=0; i<model.getColumnCount(); i++ ){
			IconColorCellRenderer cellRenderer = new IconColorCellRenderer(new Color(230,230,230));
			cellRenderer.setHorizontalAlignment(aligns[i]);
			model.getColumn(i).setCellRenderer( cellRenderer );
			model.getColumn(i).setPreferredWidth(width[i]);
		}
		panel.add("unbound.bind.center.center", new JScrollPane(table));
		
		return panel;
	}
	
	private JPanel createSaveBtnPanel(){

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		
		JButton saveButton = new JButton("Save");
		saveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionevent) {
				save();
			}
		});

		JButton closeButton = new JButton("Close");
		closeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionevent) {
				dispose();
			}
		});
		buttonPanel.add(saveButton);
		buttonPanel.add(closeButton);

		return buttonPanel;
	}
	
	private void addRow(){
		//tableModel.addRow(new String[]{"", "", ""});
		try {
			CheckDialog chkDialog = new CheckDialog(AIFUtility.getActiveDesktop().getFrame(), tableModel, -1);
			chkDialog.pack();
			chkDialog.run();
		}catch(Exception ex) {
			ex.printStackTrace();
			MessageBox.post(this, ex.toString(), "����", MessageBox.ERROR);
		}
		
	}
	
	private void deleteRow(){
		int confirm = JOptionPane.showConfirmDialog(null, "������ Data�� �����Ͻðڽ��ϱ�?", "Confirm", JOptionPane.YES_NO_OPTION);
		if(confirm != 0) {
			return;
		}
		int[] selectedRows=table.getSelectedRows();//get selected row's count
	    if(selectedRows.length >0 )
	    {
	    	for(int i = selectedRows.length-1; i >= 0 ; i --) {
	    		int row = selectedRows[i];
	    		//System.out.println("row:"+row);
	    		tableModel.removeRow(row);
	    	}
	    }
	}
	
	/**
	 * ���� �� Validation Check
	 * @return
	 */
	private boolean validationChk() {
		boolean result = true;
		int cnt = tableModel.getRowCount();
		boolean initByPass = false;
		String ecoNo = "";
		dualEcoNo = "";
		
		Map<String, String> ecoNoChk = new HashMap<String, String>();
		// ECO No �ߺ��Ȱ� �ִ��� üũ...
		if(cnt == 0) {
			MessageBox.post(this, "�Էµ� ���� �����ϴ�.", "����", MessageBox.ERROR);
			result = false;
		}
		
		for(int i = 0; i < cnt ; i++) {
			ecoNo = (String)tableModel.getValueAt(i, 0);
			if(ecoNoChk.get(ecoNo) == null) {
				ecoNoChk.put(ecoNo, ecoNo);
			}
			else {
				//�ߺ��Ǵ�  ECO ���ڻ� ����(red)�� ���� ������
				dualEcoNo = ecoNo;
				setCellColor();
				MessageBox.post(this, "ECO No ["+ecoNo+"] �� �ߺ��Ǿ����ϴ�.", "����", MessageBox.ERROR);
				result = false;
				break;
			}
			
		}
		
		return result;
	}
	
	
	
	public class SaveOperation extends AbstractAIFOperation
    {
        private WaitProgressBar waitBar = null;
        private EciEcrDialog dialog = null;
        public SaveOperation(EciEcrDialog dialog, WaitProgressBar waitBar) {
            this.waitBar = waitBar;
            this.dialog = dialog;
        }

        @Override
        public void executeOperation() throws Exception {
            try
            {
            	boolean errFlag = false;
        		int cnt = tableModel.getRowCount();
        		boolean initByPass = false;
        		
        		waitBar.setStatus("���� ��...."	, true);
        		
        		TCComponentItem ecoItem = null;
        		TCComponentItemRevision ecoRevision = null;
        		String[] sECINos = null;
        		String[] sNewECINos = null;
        		String oldEcrNo = "";
        		String ecoNo = null;
        		String eciNo = null;
        		String ecrNo = null;
        		DataSet ds = new DataSet();
        		
        		try {
        			SYMCRemoteUtil remoteQuery = new SYMCRemoteUtil();
        			
        			initByPass = session.hasBypass();
        			
        			if(!initByPass) {
        				session.enableBypass(true);
        			}
        			
        			for(int i = 0; i < cnt ; i++) {
        				ecoNo = ((String)tableModel.getValueAt(i, 0)).trim();
        				eciNo = ((String)tableModel.getValueAt(i, 1)).trim();
        				ecrNo = ((String)tableModel.getValueAt(i, 2)).trim();
        				try {
        					ecoItem = CustomUtil.findItem(SYMCECConstant.ECOTYPE, ecoNo);
        					ecoRevision = ecoItem.getLatestItemRevision();
        					
        					sECINos = ecoRevision.getTCProperty("s7_ECI_NO").getStringValueArray();
        					ds.put("ECONO", ecoNo);
        					
        					if(!StringUtil.nullToString(eciNo).equals("")) {
        						String[] eciNoSplit = eciNo.split(",");
        						//���� ECI�� ���� ���
        						if(sECINos != null) {
        							sNewECINos = new String[sECINos.length + eciNoSplit.length];
        							for(int j = 0; j < sECINos.length; j++) {
        								sNewECINos[j] = sECINos[j];
        							}
        							
        							for(int j = 0, k = sECINos.length; j < eciNoSplit.length; j++, k++) {
        								sNewECINos[k] = eciNoSplit[j].trim();
        							}
        						} else { //���� ECI�� ���� ���
        							sNewECINos = new String[eciNoSplit.length];
        							for(int j = 0; j < sNewECINos.length; j++) {
        								sNewECINos[j] = eciNoSplit[j].trim();
        							}
        						}
        						//TC Update
        						ecoRevision.getTCProperty("s7_ECI_NO").setStringValueArray(sNewECINos);
        						
        						//I/F Table Update
        						for(int j = 0; j < eciNoSplit.length; j++) {
        							ds.put("ECINO", eciNoSplit[j].trim());
        							//System.out.println(ds.get("ECONO")+", ECINO:"+ds.get("ECINO"));
        							remoteQuery.execute(ECOHISTORY_QUERY_SERVICE, "updateEci", ds);
        						}
        					}
        					
        					if(!StringUtil.nullToString(ecrNo).equals("")) {
        						String[] ecrNoSplit = ecrNo.split(",");
        						//TC Update
        						oldEcrNo = ecoRevision.getTCProperty("s7_ECR_NO").getStringValue();
        						if(!StringUtil.nullToString(oldEcrNo).equals(""))
        							ecrNo = oldEcrNo +","+ ecrNo;
        						//System.out.println(ds.get("ECONO")+", FINAL ECRNO:"+ecrNo);
        						ecoRevision.getTCProperty("s7_ECR_NO").setStringValue(ecrNo);
        						
        						//I/F Table Update
        						for(int j = 0; j < ecrNoSplit.length ; j++) {
        							ds.put("ECRNO", ecrNoSplit[j].trim());
        							//System.out.println(ds.get("ECONO")+", ECRNO:"+ds.get("ECRNO"));
        							remoteQuery.execute(ECOHISTORY_QUERY_SERVICE, "updateEcr", ds);
        						}
        					}
        					waitBar.setStatus(ecoNo +" : ����");
        				}catch(Exception ex) {
        					errFlag = true;
        					waitBar.setStatus(ecoNo +" : ���� > "+ex.toString());
        					//System.out.println(ecoNo+":"+ex.getMessage());
        				}
        				
        			}
        			
        			if(errFlag) {
        				waitBar.setStatus("������ �߻��߽��ϴ�!!\n�ý��� �����ڿ��� ���� �ٶ��ϴ�.");
        				waitBar.close("Error", true, true);
        			} else {
        				waitBar.setStatus("������ �Ϸ�Ǿ����ϴ�.");
        				waitBar.close("Success", false, true);
        			}
        			
        			
        		}catch(Exception ex) {
        			waitBar.setStatus(ex.toString());
        			waitBar.close("Error", true, true);
        			ex.printStackTrace();
        		}finally {
        			try {
        				if(!initByPass) {
        					session.enableBypass(false);
        				}
        			} catch (TCException e) {
        				// TODO Auto-generated catch block
        				e.printStackTrace();
        			}
        			
        		}

            }
            catch (Exception ex)
            {
                throw ex;
            }
        }
    }
	
	/**
	 * ���� ��ư Ŭ�� �� ȣ��Ǵ� method
	 */
	private void save() {
		if(!validationChk()) return;
		
		int confirm = JOptionPane.showConfirmDialog(null, "�����Ͻðڽ��ϱ�?", "Confirm", JOptionPane.YES_NO_OPTION);
		if(confirm != 0) {
			return;
		}
		
		final WaitProgressBar waitBar = new WaitProgressBar(this);
        waitBar.start();

        final SaveOperation postInit = new SaveOperation(this, waitBar);
        postInit.addOperationListener(new InterfaceAIFOperationListener() {
            @Override
            public void startOperation(String paramString) {
            }

            @Override
            public void endOperation() {
 
            }
        });

        session.queueOperation(postInit);

	}
	
	private class MouseAdapter implements MouseListener {
		public void mouseClicked(MouseEvent e) {
			if(e.getClickCount() == 2) {
				JTable target = (JTable) e.getSource();
				int row = target.getSelectedRow();
				int col = target.getSelectedColumn();
				//System.out.println(row + " " + col);
				try {
					CheckDialog chkDialog = new CheckDialog(AIFUtility.getActiveDesktop().getFrame(), tableModel, target.getSelectedRow());
					chkDialog.pack();
					chkDialog.run();
				}catch(Exception ex) {
					ex.printStackTrace();
				}
			}
		}

		@Override
		public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		}

		@Override
		public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		}

		@Override
		public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		}

		@Override
		public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		}
	}
	
	public void setCellColor() {
		 TableColumn column = table.getColumnModel().getColumn(0); 
		 column.setCellRenderer(new MyTableCellRenderer());
		 table.repaint();

	}
	
	public class MyTableCellRenderer extends DefaultTableCellRenderer {
	    @Override
	    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		    Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		    if(!dualEcoNo.equals("") && dualEcoNo.equals(value)) {
		    	cell.setForeground(Color.red);
		    } else {
		    	cell.setForeground(Color.black);
		    }

	    return cell;
	    }
	}

}



package com.symc.plm.rac.prebom.masterlist.dialog;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Date;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

import com.ssangyong.common.WaitProgressBar;
import com.symc.plm.rac.prebom.common.TypeConstant;
import com.symc.plm.rac.prebom.common.util.BomUtil;
import com.teamcenter.rac.aif.AbstractAIFDialog;
import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.aif.InterfaceAIFOperationListener;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.kernel.TCComponentBOMWindowType;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;

public class SelectFMPDlg extends AbstractAIFDialog {
	private static final long serialVersionUID = 1L;
	private final JPanel contentPanel = new JPanel();
	private TCComponentItemRevision selectedRevision;
	private Date selectedDate;
	private JTable table;
	private ArrayList<String> alFMPs = new ArrayList<String>();
	private Vector<Vector> fmpList;
	
	private int totFmpCount = 0;
	private boolean isOKClicked = false;
	
	public SelectFMPDlg(TCComponentItemRevision selectedRevision, Date selectedDate) {
		super(AIFUtility.getActiveDesktop(), true);
		this.selectedRevision = selectedRevision;
		this.selectedDate = selectedDate;
		init();
	}
	
	private void init() {
		setTitle("Master List View");
		setBounds(100, 100, 300, 200);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setLayout(new BorderLayout());
		contentPanel.setBorder(new TitledBorder(null, "FMP Selection", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		contentPanel.add(createSelectAll(), BorderLayout.NORTH);
		contentPanel.add(createTable(), BorderLayout.CENTER);
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		getContentPane().add(createButtons(), BorderLayout.SOUTH);
	}

	private Component createSelectAll() {
		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new FlowLayout(FlowLayout.LEFT));
		
		JLabel lblSelectAll = new JLabel("Select All");
		
		JCheckBox cSelectAll = new JCheckBox();
		cSelectAll.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent paramMouseEvent) {
				JCheckBox checkBox = (JCheckBox) paramMouseEvent.getComponent();
				for (int inx = 0; inx < table.getRowCount(); inx++) {
					table.setValueAt(checkBox.isSelected(), inx, 0);
				}
			}
		});
		
		buttonPane.add(lblSelectAll);
		buttonPane.add(cSelectAll);
		return buttonPane;
	}

	/**
	 * Create Table
	 * @return
	 */
	private JScrollPane createTable() {
		JScrollPane scroll = null;
		
		try {
			Vector<String> vHeader = new Vector<String>();
			vHeader.add(" ");
			vHeader.add("FMP No");
			
			fmpList = searchFMPList();
			
			DefaultTableModel model = new DefaultTableModel(fmpList, vHeader);
			table = new JTable(model) {
				private static final long serialVersionUID = 1L;

				@Override
				public Class getColumnClass(int column) {
					switch (column) {
					case 0:
						return Boolean.class;
					default:
						return String.class;
					}
				}
				
				@Override
				public boolean isCellEditable(int row, int column) {
					if (column == 0) {
						return true;
					}
					
					return false;
				}
			};
			
			table.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent paramMouseEvent) {
					paramMouseEvent.getClickCount();
					int[] iSelectedRows = table.getSelectedRows();
					
					for (int inx = 0; inx < iSelectedRows.length; inx++) {
						table.setValueAt(true, iSelectedRows[inx], 0);
					}
				}
			});
			
			table.getColumnModel().getColumn(0).setPreferredWidth(20);
			table.getColumnModel().getColumn(1).setPreferredWidth(200);
			
			scroll = new JScrollPane();
			scroll.setViewportView(table);
			scroll.setPreferredSize(new Dimension(300, 200));
			scroll.setCorner(JScrollPane.UPPER_LEFT_CORNER, table.getTableHeader());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return scroll;
	}
	
	private JPanel createButtons() {
		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
		JButton okButton = new JButton("OK");
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				isOKClicked = true;
				
				try {
					int iRowCount = table.getRowCount();
					
					for (int inx = 0; inx < iRowCount; inx++) {
						Object oSelected = table.getModel().getValueAt(inx, 0);
						
						if (oSelected instanceof Boolean) {
							boolean isSelected = Boolean.parseBoolean(oSelected.toString());
							
							if (isSelected) {
								alFMPs.add(table.getModel().getValueAt(inx, 1).toString());
							}
						}
					}
				}catch(Exception e){
					MessageBox.post(e);
					return;
				}finally{
					dispose();
				}
			}
		});
		okButton.setActionCommand("OK");
		buttonPane.add(okButton);
		
		return buttonPane;
	}
	
	public ArrayList<String> getSelectedFMPs() {
		return alFMPs;
	}
	
	public void setFMPList(Vector<Vector> fmpList) {
		this.fmpList = fmpList;
	}
	
	public boolean isOKClicked() {
		return isOKClicked;
	}
	
	/**
	 * Get FMP List
	 * @return
	 * @throws Exception
	 */
	public Vector<Vector> searchFMPList() throws Exception {
		//Release扁霖
		TCSession session = selectedRevision.getSession();
		String sType = selectedRevision.getType();
		TCComponentBOMWindowType windowType = (TCComponentBOMWindowType) session.getTypeComponent("BOMWindow");
        TCComponentBOMWindow bomWindow = null;
        Vector<Vector> fmpList = new Vector<Vector>();
        
        try{
        	TCComponentBOMLine topLine = BomUtil.getBomLine(selectedRevision.getItem(), selectedDate);
        	if( topLine != null){
        		bomWindow = topLine.window();
        	}
        	
        	if( sType.equals(TypeConstant.S7_PREPRODUCTREVISIONTYPE)){
        		//Product老 版快.
	        	AIFComponentContext[] funcContext = topLine.getChildren();
	        	for( int i = 0; funcContext != null && i < funcContext.length; i++){
	        		TCComponentBOMLine funcLine = (TCComponentBOMLine)funcContext[i].getComponent();
	        		AIFComponentContext[] fmpContext = funcLine.getChildren();
	        		for( int j = 0; fmpContext != null && j < fmpContext.length; j++){
	        			TCComponentBOMLine fmpLine = (TCComponentBOMLine)fmpContext[j].getComponent();
	        			Object[] objFMP = new Object[2];
	        			Vector vFMP = new Vector();
	        			String sFMPNo = fmpLine.getItem().getProperty("item_id");
//	        			objFMP[0] = false;
//	        			objFMP[1] = sFMPNo;
	        			
	        			vFMP.add(false);
	        			vFMP.add(sFMPNo);
	        			
	        			fmpList.add(vFMP);
	        		}
	        	}
        	}else{
        		//Function老 版快
        		AIFComponentContext[] fmpContext = topLine.getChildren();
        		for( int j = 0; fmpContext != null && j < fmpContext.length; j++){
        			TCComponentBOMLine fmpLine = (TCComponentBOMLine)fmpContext[j].getComponent();
        			Object[] objFMP = new Object[2];
        			Vector vFMP = new Vector();
        			String sFMPNo = fmpLine.getItem().getProperty("item_id");
//        			objFMP[0] = false;
//        			objFMP[1] = sFMPNo;
        			
        			vFMP.add(false);
        			vFMP.add(sFMPNo);
        			
        			fmpList.add(vFMP);
        		}
        	}
        	return fmpList;
        }catch(Exception e){
        	e.printStackTrace();
        }finally{
        	if( bomWindow != null){
        		bomWindow.close();
        	}
        }
        
        return fmpList;
	}
}

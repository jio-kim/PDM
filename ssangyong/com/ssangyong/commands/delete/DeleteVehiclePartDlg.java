package com.ssangyong.commands.delete;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import com.ssangyong.common.utils.CustomUtil;
import com.teamcenter.rac.aif.AbstractAIFDialog;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.SYMCBOMLine;
import com.teamcenter.rac.kernel.SYMCBOMWindow;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMViewRevision;
import com.teamcenter.rac.kernel.TCComponentChangeItemRevision;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentFolder;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentProcess;
import com.teamcenter.rac.kernel.TCComponentTask;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.VerticalLayout;

public class DeleteVehiclePartDlg extends AbstractAIFDialog {
	private static final long serialVersionUID = 1L;
	private TCComponent[] targets;
	private ArrayList<HashMap<String, Object>> alResult;
	
	private Vector<String> headerVector = new Vector<String>();
	private JTable table;
	
	public DeleteVehiclePartDlg(TCComponent[] targets) {
		super(AIFUtility.getActiveDesktop().getFrame(), false);
		this.targets = targets;
		
		setTitle("Delete Vehicle Part");
		setPreferredSize(new Dimension(1000, 700));
		setSize(1000, 700);
		
		uiInit();
		dataInit();
		dataSetting();
	}
	
	/**
	 * 초기화 및 Dialog UI 생성
	 */
	private void dataInit(){
		try {
			alResult = new ArrayList<HashMap<String,Object>>();
			
			if (this.targets.length == 0) {
				return;
			}
			
			DefaultTableModel model = (DefaultTableModel)table.getModel();
			model.getDataVector().removeAllElements();
			model.fireTableDataChanged();
			
			// Referenced 목록 및 유형 추출
			for (int inx = 0; inx < targets.length; inx++) {
				if (targets[inx] == null) {
					continue;
				}
				
				AIFComponentContext[] whereReferenceds = targets[inx].whereReferenced();
				
				for (int jnx = 0; jnx < whereReferenceds.length; jnx++) {
					getReferenceType(targets[inx], whereReferenceds[jnx]);
				}
			}
			
		} catch (Exception e) {
			MessageBox.post(e);
		}
	}
	
	/**
	 * Reference 유형
	 * @param target
	 * @param whereReferenced
	 * @return
	 */
	private void getReferenceType(TCComponent target, AIFComponentContext whereReferenced) throws Exception {
		TCComponent component = (TCComponent)whereReferenced.getComponent();
		int iRefType = 0;
		
		HashMap<String, Object> hmResult = new HashMap<String, Object>();
		hmResult.put(DeletePartConstants.KEY_TARGET, target);
		hmResult.put(DeletePartConstants.KEY_COMPONENT, component);
		
		if (target instanceof TCComponentItem) {
			TCComponentItem item = (TCComponentItem)target;
			if (isReleasedOrProcessing(item.getLatestItemRevision())) {
				iRefType |= DeletePartConstants.REF_TYPE_RELEASED;
			}
			
			if (component instanceof TCComponentItem) {
				// Iman Based On
				iRefType |= DeletePartConstants.REF_TYPE_IMAN_BASED_ON;
			}
			
			if (component instanceof TCComponentItemRevision) {
				if (component instanceof TCComponentChangeItemRevision) {
					// ECO
					TCComponent[] cReferences = component.getReferenceListProperty("CMReferences");
					TCComponent[] cProblems = component.getReferenceListProperty("CMHasProblemItem");
					TCComponent[] cSolutions = component.getReferenceListProperty("CMHasSolutionItem");
					
					if (cSolutions.length > 0) {
						String sTargetUID = target.getUid();
						for (int inx = 0; inx < cSolutions.length; inx++) {
							String sSolutionUID = cSolutions[inx].getUid();
							
							if (sSolutionUID.equals(sTargetUID)) {
								iRefType |= DeletePartConstants.REF_TYPE_SOLUTION;
							}
						}
					}
					
					if (cReferences.length > 0) {
						String sTargetUID = target.getUid();
						for (int inx = 0; inx < cReferences.length; inx++) {
							String sReferenceUID = cReferences[inx].getUid();
							
							if (sReferenceUID.equals(sTargetUID)) {
								iRefType |= DeletePartConstants.REF_TYPE_REFERENCE;
							}
						}
					}
					
					if (cProblems.length > 0) {
						String sTargetUID = target.getUid();
						for (int inx = 0; inx < cProblems.length; inx++) {
							String sProblemUID = cProblems[inx].getUid();
							
							if (sProblemUID.equals(sTargetUID)) {
								iRefType |= DeletePartConstants.REF_TYPE_PROBLEM;
							}
						}
					}
				} else {
					// Shown On
					iRefType |= DeletePartConstants.REF_TYPE_SHOWN_ON;
				}
			}
			
			if (component instanceof TCComponentBOMViewRevision) {
				// Occurence
				iRefType |= DeletePartConstants.REF_TYPE_OCCURENCE;
			}
			
			if (component instanceof TCComponentFolder) {
				// Folder
				iRefType |= DeletePartConstants.REF_TYPE_FOLDER;
			}
			
			if (component instanceof TCComponentDataset) {
				// Dataset
				iRefType |= DeletePartConstants.REF_TYPE_DATASET;
			}
			
			if (component instanceof TCComponentTask) {
				// Task
				iRefType |= DeletePartConstants.REF_TYPE_TASK;
			}
			
			if (component instanceof TCComponentProcess) {
				// Process
				iRefType |= DeletePartConstants.REF_TYPE_PROCESS;
			}
			
			if (iRefType == 0) {
				iRefType |= DeletePartConstants.REF_TYPE_UNKNOWN;
			}
		} else if (target instanceof TCComponentItemRevision) {
			if (isReleasedOrProcessing(target)) {
				iRefType |= DeletePartConstants.REF_TYPE_RELEASED;
			}
			
			if (component instanceof TCComponentItem) {
				if (target.getProperty("item_revision_id").equals("000")) {
					TCComponent targetItem = ((TCComponentItemRevision) target).getItem();
					AIFComponentContext[] accTargetItems = targetItem.whereReferenced();
					for (int inx = 0; inx < accTargetItems.length; inx++) {
						getReferenceType(targetItem, accTargetItems[inx]);
					}
					return;
				}
				iRefType |= DeletePartConstants.REF_TYPE_UNKNOWN;
			}
			
			if (component instanceof TCComponentItemRevision) {
				if (component instanceof TCComponentChangeItemRevision) {
					// ECO
					TCComponent[] cReferences = component.getReferenceListProperty("CMReferences");
					TCComponent[] cProblems = component.getReferenceListProperty("CMHasProblemItem");
					TCComponent[] cSolutions = component.getReferenceListProperty("CMHasSolutionItem");
					
					if (cSolutions.length > 0) {
						String sTargetUID = target.getUid();
						for (int inx = 0; inx < cSolutions.length; inx++) {
							String sSolutionUID = cSolutions[inx].getUid();
							
							if (sSolutionUID.equals(sTargetUID)) {
								iRefType |= DeletePartConstants.REF_TYPE_SOLUTION;
							}
						}
					}
					
					if (cReferences.length > 0) {
						String sTargetUID = target.getUid();
						for (int inx = 0; inx < cReferences.length; inx++) {
							String sReferenceUID = cReferences[inx].getUid();
							
							if (sReferenceUID.equals(sTargetUID)) {
								iRefType |= DeletePartConstants.REF_TYPE_REFERENCE;
							}
						}
					}
					
					if (cProblems.length > 0) {
						String sTargetUID = target.getUid();
						for (int inx = 0; inx < cProblems.length; inx++) {
							String sProblemUID = cProblems[inx].getUid();
							
							if (sProblemUID.equals(sTargetUID)) {
								iRefType |= DeletePartConstants.REF_TYPE_PROBLEM;
							}
						}
					}
				} else {
					// Iman Based On
					iRefType |= DeletePartConstants.REF_TYPE_IMAN_BASED_ON;
				}
			}
			
			if (component instanceof TCComponentFolder) {
				// Folder
				iRefType |= DeletePartConstants.REF_TYPE_FOLDER;
			}
			
			if (component instanceof TCComponentDataset) {
				// Dataset
				iRefType |= DeletePartConstants.REF_TYPE_DATASET;
			}
			
			if (component instanceof TCComponentTask) {
				// Task
				iRefType |= DeletePartConstants.REF_TYPE_TASK;
			}
			
			if (component instanceof TCComponentProcess) {
				// Process
				iRefType |= DeletePartConstants.REF_TYPE_PROCESS;
			}
			
			if (iRefType == 0) {
				iRefType |= DeletePartConstants.REF_TYPE_UNKNOWN;
			}
		}

		hmResult.put(DeletePartConstants.KEY_REF_TYPE, iRefType);
		alResult.add(hmResult);
		
		return;
	}
	
	/**
	 * UI Initialize
	 */
	private void uiInit() {
		Container container = getContentPane();
		JPanel pnlMain = new JPanel();
		JPanel pnlNorth = new JPanel();
		JPanel pnlButton = new JPanel();
		JPanel pnlTargets = new JPanel();
		JPanel pnlChkBox = new JPanel();
		JScrollPane pnlTable = new JScrollPane();
		
		container.setLayout(new BorderLayout());
		
		pnlMain.setBorder(new EmptyBorder(5, 5, 5, 5));
		pnlMain.setLayout(new BorderLayout(0, 0));
		
		container.add(pnlMain, BorderLayout.CENTER);
		
		pnlNorth = new JPanel();
		pnlNorth.setLayout(new VerticalLayout());
		pnlMain.add(pnlNorth, BorderLayout.NORTH);
		
		pnlButton = new JPanel();
		pnlButton.setLayout(new FlowLayout(FlowLayout.RIGHT));
		
		JButton btnCutSelected = new JButton("Cut Selected Reference");
		btnCutSelected.setIcon(new ImageIcon(DeleteVehiclePartDlg.class.getResource("/com/ssangyong/common/images/removeAll_16.png")));
		btnCutSelected.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					cutSelectedReference();
				} catch (Exception exception) {
					MessageBox.post(exception);
				}
			}
		});
		
		pnlButton.add(btnCutSelected);
		
		JButton btnDelete = new JButton("Delete Part");
		btnDelete.setIcon(new ImageIcon(DeleteVehiclePartDlg.class.getResource("/com/ssangyong/common/images/ok_16.png")));
		btnDelete.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					deletePart();
				} catch (Exception exception) {
					MessageBox.post(exception);
				}
			}
		});
		
		pnlButton.add(btnDelete);
		pnlNorth.add("top.unbound", pnlButton);
		
		pnlTargets = new JPanel();
		pnlTargets.setLayout(new BoxLayout(pnlTargets, BoxLayout.Y_AXIS));
		for (int inx = 0; inx < targets.length; inx++) {
			JPanel pnlTarget = new JPanel();
			pnlTarget.setLayout(new FlowLayout(FlowLayout.RIGHT));
			
			JLabel lblTargets = new JLabel();
			lblTargets.setText(targets[inx].toString());
			lblTargets.setFont(new Font(lblTargets.getFont().getName(), Font.BOLD, lblTargets.getFont().getSize()));
			
			pnlTarget.add(lblTargets);
			pnlTargets.add(pnlTarget);
		}
		
		pnlNorth.add("top.unbound", pnlTargets);
		
		pnlChkBox = new JPanel();
		pnlChkBox.setLayout(new FlowLayout(FlowLayout.RIGHT));
		
		JLabel lblSelectAll = new JLabel();
		lblSelectAll.setText("Select All");
		pnlChkBox.add(lblSelectAll);
		
		final JCheckBox chkSelectAll = new JCheckBox();
		chkSelectAll.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent paramActionEvent) {
				for (int inx = 0; inx < table.getRowCount(); inx++) {
					table.setValueAt(chkSelectAll.isSelected(), inx, 4);
				}
			}
		});
		chkSelectAll.setSelected(true);
		pnlChkBox.add(chkSelectAll);
		pnlNorth.add("top.unbound", pnlChkBox);
		
		headerVector.add("Target Part No");
		headerVector.add("Reference Component");
		headerVector.add("Reference Type No");
		headerVector.add("Reference Type");
		headerVector.add(" ");
		
		TableModel modelDefault = new DefaultTableModel(null, headerVector) {
			private static final long serialVersionUID = 1L;

			public Class<String> getColumnClass(int col) {
				return String.class;
			}

			public boolean isCellEditable(int row, int col) {
				if (col == 4) {
					return true;
				}
				return false;
			}
		};
		
		table = new JTable(modelDefault);
		table.setRowHeight(30);
		TableColumnModel columnModel = table.getColumnModel();
		int[] columnWidth = {480, 280, 0, 150, 30 };
		int iHeaderSize = headerVector.size();
		for (int inx = 0; inx < iHeaderSize; inx++) {
			columnModel.getColumn(inx).setPreferredWidth(columnWidth[inx]);
			columnModel.getColumn(inx).setWidth(columnWidth[inx]);
			
			// Hide Reference Type Column
			if (inx == 2) {
				columnModel.getColumn(inx).setMinWidth(columnWidth[inx]);
				columnModel.getColumn(inx).setMaxWidth(columnWidth[inx]);
			}
		}
		
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(modelDefault);
		table.setRowSorter(sorter);
		table.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION); 
		
		pnlTable.setViewportView(table);
		pnlTable.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		pnlTable.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		pnlTable.getViewport().setBackground(Color.WHITE);
		pnlMain.add(pnlTable);
	}
	
	/**
	 * 
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void dataSetting() {
		if (alResult.isEmpty() || alResult.size() ==0) {
			return;
		}
		
		DefaultTableModel model = (DefaultTableModel)table.getModel();

		Vector<Vector> allData = new Vector<Vector>();
		allData.removeAllElements();
		for( int i = 0; alResult != null && i < alResult.size(); i++){
			HashMap map = (HashMap)alResult.get(i);
			Vector row = new Vector();
			
			row.add(map.get(DeletePartConstants.KEY_TARGET));
			row.add(map.get(DeletePartConstants.KEY_COMPONENT));
			row.add(map.get(DeletePartConstants.KEY_REF_TYPE));
			row.add(getReferenceTypeForTable(map.get(DeletePartConstants.KEY_REF_TYPE)));
			row.add(true);
			
			allData.add(row);
		}
		
		model.setDataVector(allData, headerVector);
		
		TableColumnModel columnModel = table.getColumnModel();
		int[] columnWidth = {480, 280, 0, 150, 30 };
		int iHeaderSize = headerVector.size();
		for (int inx = 0; inx < iHeaderSize; inx++) {
			columnModel.getColumn(inx).setPreferredWidth(columnWidth[inx]);
			columnModel.getColumn(inx).setWidth(columnWidth[inx]);
			
			// Hide Reference Type Column
			if (inx == 2) {
				columnModel.getColumn(inx).setMinWidth(columnWidth[inx]);
				columnModel.getColumn(inx).setMaxWidth(columnWidth[inx]);
			}
		}
		
		DefaultTableCellRenderer dcr = new DefaultTableCellRenderer() {
			private static final long serialVersionUID = 1L;
			
			public Component getTableCellRendererComponent(JTable paramJTable, Object paramObject, boolean paramBoolean1, boolean paramBoolean2, int paramInt1, int paramInt2) {
				JCheckBox chkBox = new JCheckBox();
				chkBox.setSelected((Boolean)paramObject);
				chkBox.setHorizontalAlignment(JLabel.CENTER);
				return chkBox;
			}
		};
		table.getColumn(" ").setCellRenderer(dcr);
		table.getColumn(" ").setCellEditor(new DefaultCellEditor(new JCheckBox()));
	}
	
	/**
	 * 
	 * @param oRefType
	 * @return
	 */
	private String getReferenceTypeForTable(Object oRefType) {
		String sReferenceTypeForTable = "";
		int iRefType = 0;
		
		if (oRefType instanceof Integer) {
			iRefType = (Integer) oRefType;
			
			if ((iRefType & DeletePartConstants.REF_TYPE_IMAN_BASED_ON) == DeletePartConstants.REF_TYPE_IMAN_BASED_ON) {
				if (!sReferenceTypeForTable.equals("")) {
					sReferenceTypeForTable += ", ";
				}
				sReferenceTypeForTable += "Iman Based On";
			}
			
			if ((iRefType & DeletePartConstants.REF_TYPE_SHOWN_ON) == DeletePartConstants.REF_TYPE_SHOWN_ON) {
				if (!sReferenceTypeForTable.equals("")) {
					sReferenceTypeForTable += ", ";
				}
				sReferenceTypeForTable += "Shownon No";
			}
			
			if ((iRefType & DeletePartConstants.REF_TYPE_OCCURENCE) == DeletePartConstants.REF_TYPE_OCCURENCE) {
				if (!sReferenceTypeForTable.equals("")) {
					sReferenceTypeForTable += ", ";
				}
				sReferenceTypeForTable += "BOM 구성";
			}
			
			if ((iRefType & DeletePartConstants.REF_TYPE_FOLDER) == DeletePartConstants.REF_TYPE_FOLDER) {
				if (!sReferenceTypeForTable.equals("")) {
					sReferenceTypeForTable += ", ";
				}
				sReferenceTypeForTable += "Folder";
			}
			
			if ((iRefType & DeletePartConstants.REF_TYPE_DATASET) == DeletePartConstants.REF_TYPE_DATASET) {
				if (!sReferenceTypeForTable.equals("")) {
					sReferenceTypeForTable += ", ";
				}
				sReferenceTypeForTable += "Dataset";
			}
			
			if ((iRefType & DeletePartConstants.REF_TYPE_TASK) == DeletePartConstants.REF_TYPE_TASK) {
				if (!sReferenceTypeForTable.equals("")) {
					sReferenceTypeForTable += ", ";
				}
				sReferenceTypeForTable += "Task";
			}
			
			if ((iRefType & DeletePartConstants.REF_TYPE_PROCESS) == DeletePartConstants.REF_TYPE_PROCESS) {
				if (!sReferenceTypeForTable.equals("")) {
					sReferenceTypeForTable += ", ";
				}
				sReferenceTypeForTable += "Workflow";
			}
			
			if ((iRefType & DeletePartConstants.REF_TYPE_RELEASED) == DeletePartConstants.REF_TYPE_RELEASED) {
				if (!sReferenceTypeForTable.equals("")) {
					sReferenceTypeForTable += ", ";
				}
				sReferenceTypeForTable += "Released";
			}
			
			if ((iRefType & DeletePartConstants.REF_TYPE_SOLUTION) == DeletePartConstants.REF_TYPE_SOLUTION) {
				if (!sReferenceTypeForTable.equals("")) {
					sReferenceTypeForTable += ", ";
				}
				sReferenceTypeForTable += "Solution Items";
			}
			
			if ((iRefType & DeletePartConstants.REF_TYPE_PROBLEM) == DeletePartConstants.REF_TYPE_PROBLEM) {
				if (!sReferenceTypeForTable.equals("")) {
					sReferenceTypeForTable += ", ";
				}
				sReferenceTypeForTable += "Problem Items";
			}
			
			if ((iRefType & DeletePartConstants.REF_TYPE_REFERENCE) == DeletePartConstants.REF_TYPE_REFERENCE) {
				if (!sReferenceTypeForTable.equals("")) {
					sReferenceTypeForTable += ", ";
				}
				sReferenceTypeForTable += "Reference Items";
			}
			
			if ((iRefType & DeletePartConstants.REF_TYPE_UNKNOWN) == DeletePartConstants.REF_TYPE_UNKNOWN) {
				if (!sReferenceTypeForTable.equals("")) {
					sReferenceTypeForTable += ", ";
				}
				sReferenceTypeForTable += "Unknown";
			}
		}
		
		return sReferenceTypeForTable;
	}
	
	/**
	 * 
	 * @throws Exception
	 */
	private void cutSelectedReference() throws Exception {
		int iRowCount = table.getRowCount();
		StringBuffer sbResult = new StringBuffer();
		
		for (int inx = 0; inx < iRowCount; inx++) {
			boolean isSelected = (Boolean)table.getValueAt(inx, 4);
			
			if (isSelected) {
				TCComponent cTarget = (TCComponent)table.getValueAt(inx, 0);
				TCComponent cReference = (TCComponent)table.getValueAt(inx, 1);
				
				int iRefType = (Integer)table.getValueAt(inx, 2);
				
				if ((iRefType & DeletePartConstants.REF_TYPE_IMAN_BASED_ON) == DeletePartConstants.REF_TYPE_IMAN_BASED_ON) {
					cutImanBasedOn(cTarget, cReference);
					sbResult.append(cTarget.toString()).append(" : Cut from ").append(cReference).append(" COMPLETE.\n");
				}
				
				if ((iRefType & DeletePartConstants.REF_TYPE_SHOWN_ON) == DeletePartConstants.REF_TYPE_SHOWN_ON) {
					cutShownon(cTarget, cReference);
					sbResult.append(cTarget.toString()).append(" : Cut from ").append(cReference).append(" COMPLETE.\n");
				}
				
				if ((iRefType & DeletePartConstants.REF_TYPE_OCCURENCE) == DeletePartConstants.REF_TYPE_OCCURENCE) {
					cutOccurence(cTarget, cReference);
					sbResult.append(cTarget.toString()).append(" : Cut from ").append(cReference).append(" INCOMPLETE.\n").append("Contact to administrator.");
				}
				
				if ((iRefType & DeletePartConstants.REF_TYPE_FOLDER) == DeletePartConstants.REF_TYPE_FOLDER) {
					cutFolder(cTarget, cReference);
					sbResult.append(cTarget.toString()).append(" : Cut from ").append(cReference).append(" COMPLETE.\n");
				}
				
				if ((iRefType & DeletePartConstants.REF_TYPE_DATASET) == DeletePartConstants.REF_TYPE_DATASET) {
					cutDataset(cTarget, cReference);
					sbResult.append(cTarget.toString()).append(" : Cut from ").append(cReference).append(" COMPLETE.\n");
				}
				
				if ((iRefType & DeletePartConstants.REF_TYPE_TASK) == DeletePartConstants.REF_TYPE_TASK) {
					cutTask(cTarget, cReference);
					sbResult.append(cTarget.toString()).append(" : Cut from ").append(cReference).append(" INCOMPLETE.\n").append("Contact to administrator.");
				}
				
				if ((iRefType & DeletePartConstants.REF_TYPE_PROCESS) == DeletePartConstants.REF_TYPE_PROCESS) {
					cutProcess(cTarget, cReference);
					sbResult.append(cTarget.toString()).append(" : Cut from ").append(cReference).append(" INCOMPLETE.\n").append("Contact to administrator.");
				}
				
				if ((iRefType & DeletePartConstants.REF_TYPE_RELEASED) == DeletePartConstants.REF_TYPE_RELEASED) {
				}
				
				if ((iRefType & DeletePartConstants.REF_TYPE_SOLUTION) == DeletePartConstants.REF_TYPE_SOLUTION) {
					cutSolutionItems(cTarget, cReference);
					sbResult.append(cTarget.toString()).append(" : Cut from ").append(cReference).append(" COMPLETE.\n");
				}
				
				if ((iRefType & DeletePartConstants.REF_TYPE_PROBLEM) == DeletePartConstants.REF_TYPE_PROBLEM) {
					cutProblemItems(cTarget, cReference);
					sbResult.append(cTarget.toString()).append(" : Cut from ").append(cReference).append(" COMPLETE.\n");
				}
				
				if ((iRefType & DeletePartConstants.REF_TYPE_REFERENCE) == DeletePartConstants.REF_TYPE_REFERENCE) {
					cutReferenceItems(cTarget, cReference);
					sbResult.append(cTarget.toString()).append(" : Cut from ").append(cReference).append(" COMPLETE.\n");
				}
				
				if ((iRefType & DeletePartConstants.REF_TYPE_UNKNOWN) == DeletePartConstants.REF_TYPE_UNKNOWN) {
					cutUnknown(cTarget, cReference);
					sbResult.append(cTarget.toString()).append(" : Cut from ").append(cReference).append(" INCOMPLETE.\n").append("Contact to administrator.");
				}
				
				sbResult.append("----------------------------------------------------------------------\n");
				
				cTarget.refresh();
				cReference.refresh();
			}
		}
		
		if (sbResult.length() > 0) {
			MessageBox.post(sbResult.toString(), "Cut Selected Reference", MessageBox.INFORMATION);
		} else {
			MessageBox.post("There are no selected reference for cut.", "Cut Selected Reference", MessageBox.INFORMATION);
		}
		
		dataInit();
		dataSetting();
	}

	/**
	 * Cut Iman Based On
	 * @param cTarget
	 * @param cReference
	 */
	private void cutImanBasedOn(TCComponent cTarget, TCComponent cReference) throws Exception {
		if (isReleasedOrProcessing(cReference)) {
			MessageBox.post(AIFUtility.getActiveDesktop().getShell() , "A target component can't Cut from a referenced component because a referenced component has been released.\nPlease check to delete a target component." , "Released", MessageBox.WARNING);
			return;
		}
		
		cReference.cutOperation("IMAN_based_on", new TCComponent[] {cTarget});
	}

	/**
	 * Cut Shown On
	 * @param cTarget
	 * @param cReference
	 */
	private void cutShownon(TCComponent cTarget, TCComponent cReference)  throws Exception {
		if (isReleasedOrProcessing(cReference)) {
			MessageBox.post(AIFUtility.getActiveDesktop().getShell() , "A target component can't Cut from a referenced component because a referenced component has been released.\nPlease check to delete a target component." , "Released", MessageBox.WARNING);
			return;
		}
		
		cReference.cutOperation("s7_SHOWN_PART_NO", new TCComponent[] {cTarget});
	}

	/**
	 * Cut BOM
	 * @param cTarget
	 * @param cReference
	 */
	private void cutOccurence(TCComponent cTarget, TCComponent cReference) throws Exception {
		if (isReleasedOrProcessing(cReference)) {
			MessageBox.post(AIFUtility.getActiveDesktop().getShell() , "A target component can't Cut from a parent because a parent component has been released.\nPlease check to cut BOM Line a target component." , "Released", MessageBox.WARNING);
			return;
		}
		
		SYMCBOMWindow window = (SYMCBOMWindow)CustomUtil.createBOMWindow();
		try {
			window.setWindowTopLine(cReference);
			AIFComponentContext[] children = window.getChildren();
			for (int inx = 0; inx < children.length; inx++) {
				InterfaceAIFComponent component = children[inx].getComponent();
				
				if (component instanceof SYMCBOMLine) {
					SYMCBOMLine bomLine = (SYMCBOMLine)component;
					if (bomLine.getItemRevision().getUid().equals(cTarget.getUid())) {
						bomLine.cut();
					}
				}
			}
			
			window.save();
		} catch (Exception e) {
			MessageBox.post(e);
		} finally {
			window.close();
		}
		
		MessageBox.post(AIFUtility.getActiveDesktop().getShell() , "Open the Structure Manager and Cut operation." , "BOM", MessageBox.WARNING);
	}

	/**
	 * Cut Folder
	 * @param cTarget
	 * @param cReference
	 */
	private void cutFolder(TCComponent cTarget, TCComponent cReference) throws Exception {
		cReference.cutOperation("contents", new TCComponent[] {cTarget});
	}

	/**
	 * Cut Dataset(CATDrawing)
	 * @param cTarget
	 * @param cReference
	 */
	private void cutDataset(TCComponent cTarget, TCComponent cReference) throws Exception {
		cReference.cutOperation("catiaV5_DWGLink", new TCComponent[] {cTarget});
	}

	/**
	 * Cut Task
	 * @param cTarget
	 * @param cReference
	 */
	private void cutTask(TCComponent cTarget, TCComponent cReference) {
		MessageBox.post(AIFUtility.getActiveDesktop().getShell() , "It have a Workflow and Processing.\nContact to administrator." , "Workflow", MessageBox.WARNING);
	}

	/**
	 * Cut Process
	 * @param cTarget
	 * @param cReference
	 */
	private void cutProcess(TCComponent cTarget, TCComponent cReference) {
		MessageBox.post(AIFUtility.getActiveDesktop().getShell() , "It have a Workflow and Processing.\nContact to administrator." , "Workflow", MessageBox.WARNING);
	}
	
	/**
	 * Cut Solution Items From ECO
	 * @param cTarget
	 * @param cReference
	 */
	private void cutSolutionItems(TCComponent cTarget, TCComponent cReference) throws Exception {
		if (isReleasedOrProcessing(cReference)) {
			MessageBox.post(AIFUtility.getActiveDesktop().getShell() , "A target component can't Cut from a referenced component because a referenced component has been released.\nPlease check to delete a target component." , "Released", MessageBox.WARNING);
			return;
		}
		
		if (cReference instanceof TCComponentChangeItemRevision) {
			TCComponentChangeItemRevision ecoRevision = (TCComponentChangeItemRevision)cReference;
			ecoRevision.cutOperation("CMHasSolutionItem", new TCComponent[] {cTarget});
		}
	}
	
	/**
	 * Cut Problem Items from ECO
	 * @param cTarget
	 * @param cReference
	 */
	private void cutProblemItems(TCComponent cTarget, TCComponent cReference) throws Exception {
		if (isReleasedOrProcessing(cReference)) {
			MessageBox.post(AIFUtility.getActiveDesktop().getShell() , "A target component can't Cut from a referenced component because a referenced component has been released.\nPlease check to delete a target component." , "Released", MessageBox.WARNING);
			return;
		}
		
		if (cReference instanceof TCComponentChangeItemRevision) {
			TCComponentChangeItemRevision ecoRevision = (TCComponentChangeItemRevision)cReference;
			ecoRevision.cutOperation("CMHasProblemItem", new TCComponent[] {cTarget});
		}
	}
	
	/**
	 * Cut Reference Items From ECO
	 * @param cTarget
	 * @param cReference
	 */
	private void cutReferenceItems(TCComponent cTarget, TCComponent cReference) throws Exception {
		if (isReleasedOrProcessing(cReference)) {
			MessageBox.post(AIFUtility.getActiveDesktop().getShell() , "A target component can't Cut from a referenced component because a referenced component has been released.\nPlease check to delete a target component." , "Released", MessageBox.WARNING);
			return;
		}
		
		if (cReference instanceof TCComponentChangeItemRevision) {
			TCComponentChangeItemRevision ecoRevision = (TCComponentChangeItemRevision)cReference;
			ecoRevision.cutOperation("CMReferences", new TCComponent[] {cTarget});
		}
	}

	/**
	 * Cut Unknown
	 * @param cTarget
	 * @param cReference
	 */
	private void cutUnknown(TCComponent cTarget, TCComponent cReference) {
		MessageBox.post(AIFUtility.getActiveDesktop().getShell() , "Contact to administrator." , "Workflow", MessageBox.WARNING);
	}
	
	/**
	 * Part 삭제
	 */
	private void deletePart() throws Exception {
		StringBuffer sbResult = new StringBuffer();
		String sTargetName = "";
		for (int inx = 0; inx < targets.length; inx++) {
			try {
				if (targets[inx] == null) {
					continue;
				}
				
				sTargetName = targets[inx].toString();
				
				if (targets[inx] instanceof TCComponentItemRevision) {
					TCComponentItemRevision itemRevision = (TCComponentItemRevision)targets[inx];
					String sItemRevisionID = itemRevision.getProperty("item_revision_id");
					
					if (sItemRevisionID.equals("000")) {
						TCComponentItem item = itemRevision.getItem();
						item.delete();
					} else {
						itemRevision.delete();
					}
				} else {
					targets[inx].delete();
				}
				
				targets[inx] = null;
				sbResult.append(sTargetName.toString()).append(" is deleted COMPLETE\n");
			} catch (Exception e) {
				sbResult.append(sTargetName.toString()).append(" is deleted INCOMPLETE\n\n").append(e).append("\n\n").append("Contact to administrator or do cut selected reference again.").append("\n");
			}
			
			sbResult.append("----------------------------------------------------------------------\n");
		}
		
		if (sbResult.length() > 0) {
			MessageBox.post(sbResult.toString(), "Delete Part", MessageBox.INFORMATION);
		} else {
			MessageBox.post("All target parts are already deleted or delete fail.", "Delete Part", MessageBox.INFORMATION);
		}
	}
	
	/**
	 * Released or Processing
	 * @param component
	 * @return
	 */
	private boolean isReleasedOrProcessing(TCComponent component) throws Exception {
		return !(component.getProperty("last_release_status").equals("") || component.getProperty("process_stage").equals(""));
	}
}

package com.symc.plm.rac.prebom.preospec.dialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.EventObject;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import com.kgm.commands.ospec.op.OpUtil;
import com.kgm.common.utils.CustomUtil;
import com.symc.plm.rac.prebom.preospec.ui.OSpecTable;
import com.teamcenter.rac.aif.AbstractAIFDialog;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCProperty;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;

public class PreOSpecViewDlg extends AbstractAIFDialog {

	private final JPanel contentPanel = new JPanel();
	private ArrayList<Integer> selectedCategories = new ArrayList<Integer>();
	private OSpecTable ospecTable = null;
	private TCComponentItemRevision preProdRevision = null;
	private ArrayList<String> reqCategoryList = new ArrayList<String>();
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			PreOSpecViewDlg dialog = new PreOSpecViewDlg(null, null);
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
	public PreOSpecViewDlg(String title, OSpecTable ospecTable) throws Exception {
		
		this.ospecTable = ospecTable;
		
		TCComponent[] tcComponent = CustomUtil.queryComponent("__SYMC_S7_PreProductRevision", new String[]{"Project Code"}, new String[]{ospecTable.getOspec().getProject()});
		if( tcComponent == null || tcComponent.length == 0){
			Registry registry = Registry.getRegistry(PreOSpecImportDlg.class);
			throw new Exception(registry.getString("PRE_OSPEC.IMP.COULD_NOT_FIND_PRODUCT"));
		}
		
		for( int i = 0; i < tcComponent.length; i++){
			String type = tcComponent[i].getType();
			if( type.equals("S7_PreProductRevision")){
				TCComponentItemRevision rev = (TCComponentItemRevision)tcComponent[i];
				preProdRevision = rev.getItem().getLatestItemRevision();
				TCProperty prop = preProdRevision.getTCProperty("s7_REQ_CATEGORY");
				String[] reqCategory = prop.getStringArrayValue();
				
				for( int j = 0; reqCategory != null && j < reqCategory.length; j++){
					if( !reqCategoryList.contains(reqCategory[j])){
						reqCategoryList.add(reqCategory[j]);
					}
				}
				
				DefaultTableModel model = (DefaultTableModel)ospecTable.getFixedOspecViewTable().getModel();
				for( int j = 0; j < model.getRowCount(); j++){
					String variantValue = (String)model.getValueAt(j, 3);
					String category = OpUtil.getCategory(variantValue);
					if( reqCategoryList.contains(category)){
						if( !selectedCategories.contains(j)){
							selectedCategories.add(j);
						}
					}
				}
				
				break;
			}
		}
		
		setBounds(new Rectangle(0, 0, 1000, 600));
		setTitle(title);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));
		{
			contentPanel.add(ospecTable.getOspecTable(), BorderLayout.CENTER);
		}
		{
			JPanel buttonPane = new JPanel();
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			buttonPane.setLayout(new FlowLayout(FlowLayout.TRAILING, 5, 5));
			{
				JButton closelButton = new JButton("Close");
				closelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						setVisible(false);
					}
				});
				{
					JButton btnSave = new JButton("Save a required option");
					btnSave.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent actionevent) {
							try {
								saveCategory();
								MessageBox.post(PreOSpecViewDlg.this, "Saved successfully", "Information", MessageBox.INFORMATION);
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
								MessageBox.post(PreOSpecViewDlg.this, e.getMessage(), "ERROR", MessageBox.ERROR);
								return;
							}
						}
					});
					buttonPane.add(btnSave);
				}
				buttonPane.add(closelButton);
			}
		}
		ospecTable.getFixedOspecViewTable().getColumnModel().getColumn(1).setCellRenderer(new CategoryCellRenerer());
		ospecTable.getFixedOspecViewTable().getColumnModel().getColumn(1).setCellEditor(new CategoryCellEditor(new JCheckBox()));
	}

	private void saveCategory() throws Exception{
		
		ArrayList<String> categories = new ArrayList<String>();
		for( int i = 0; i < selectedCategories.size(); i++){
			String optionValue = (String)ospecTable.getFixedOspecViewTable().getModel().getValueAt(selectedCategories.get(i), 3);
			String category = OpUtil.getCategory(optionValue);
			
			if( !categories.contains(category)){
				categories.add(category);
			}
		}
		
		// 필수 카테고리를 Pre Product Rev에 저장.
		TCProperty prop = preProdRevision.getTCProperty("s7_REQ_CATEGORY");
		prop.setStringValueArray(categories.toArray(new String[categories.size()]));
		preProdRevision.setTCProperty(prop);
	}
	
	class CategoryCellRenerer implements TableCellRenderer {

		@Override
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {
			
			// TODO Auto-generated method stub
			JCheckBox check = new JCheckBox();
			check.setBackground(Color.WHITE);
			check.setText(value.toString());
			
			int modelRowIdx = table.convertRowIndexToModel(row);
			check.setSelected(selectedCategories.contains(modelRowIdx));
			return check;
		}

	}

	class CategoryCellEditor extends DefaultCellEditor{
		protected JCheckBox checkBox = null; 
		
		public CategoryCellEditor(JCheckBox checkBox) {
			super(checkBox);
			this.checkBox = checkBox;
			// TODO Auto-generated constructor stub
		}

		@Override
		public Component getTableCellEditorComponent(final JTable table,
	            Object value,
	            boolean isSelected,
	            final int row,
	            int column) {
			// TODO Auto-generated method stub
//			final JCheckBox checkBox = (JCheckBox)super.getTableCellEditorComponent(table,
//		            value,
//		            isSelected,
//		            row,
//		            column);
			final int modelRowIdx = table.convertRowIndexToModel(row);
			final JCheckBox checkBox = new JCheckBox(value.toString(),selectedCategories.contains(modelRowIdx));
			checkBox.setBackground(Color.WHITE);
			checkBox.addItemListener(new ItemListener() {
				
				@Override
				public void itemStateChanged(ItemEvent itemevent) {
					// TODO Auto-generated method stub
					
					if( row < 0 ){
						return;
					}
					
					if( itemevent.getStateChange() == ItemEvent.SELECTED){
						
						if( !selectedCategories.contains(modelRowIdx)){
							selectedCategories.add(modelRowIdx);
						}
//						ItemListener[] listener = checkBox.getItemListeners();
//						for( int i = 0; listener != null && i < listener.length; i++){
//							checkBox.removeItemListener(listener[i]);
//						}
					}else{
						if( selectedCategories.contains(modelRowIdx)){
							
							selectedCategories.remove(new Integer(modelRowIdx));
							
							TableModel model = table.getModel();
							String variantValue = (String)model.getValueAt(modelRowIdx, 3);
							String category = variantValue.substring(0, 3);
							
							for( int i = modelRowIdx + 1; i < model.getRowCount(); i++){
								String tmpVariantValue = (String)model.getValueAt(i, 3);
								String tmpCategory = tmpVariantValue.substring(0, 3);
								if( category.equals(tmpCategory)){
									selectedCategories.remove(new Integer(i));
								}else{
									break;
								}
							}
							
						}
						System.out.println("Item Deselected.");
					}
				}
			});
//			checkBox.setText(value.toString());
			return checkBox;
		}

		@Override
		public boolean isCellEditable(EventObject eventobject) {
			// TODO Auto-generated method stub
			return true;
		}

		@Override
		public Object getCellEditorValue() {
			// TODO Auto-generated method stub
			return super.getCellEditorValue();
		}

	    @Override  
	    public boolean shouldSelectCell(EventObject anEvent) {  
	        return true;  
	    }  
	  
	    @Override  
	    public boolean stopCellEditing() {  
	        return true;  
	    }  
	}
}

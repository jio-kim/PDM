package com.kgm.common.utils.variant;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.Vector;

import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.event.CellEditorListener;
  
/* 
 * can extends AbstractCellEditor 
 */  
  
@SuppressWarnings("serial")
public class VariantCheckBoxTableCellEditor extends DefaultCellEditor implements ItemListener {  
      
    protected JCheckBox checkBox = null;  
    private VariantValue variantValue = null;
    public static Vector<VariantValue> unUsedValueList = new Vector<VariantValue>();	//���� �߰��� Value�� �ƴ����� �ʱ� checked ==> unChecked �� ���, ����Variant������ Unused�ǵ��� �ؾ���.
    private ArrayList<VariantValue> valueList = null;
    private JTable detailTable = null;
    
    public VariantCheckBoxTableCellEditor(JCheckBox checkBox, ArrayList<VariantValue> valueList, JTable detailTable) {  
        super(checkBox);  
        this.checkBox = checkBox;
        checkBox.setHorizontalAlignment(SwingConstants.CENTER);  
        checkBox.setBackground(Color.white);  
        this.valueList = valueList;
        this.detailTable = detailTable; 
    }  
    
    public VariantCheckBoxTableCellEditor(JCheckBox checkBox) {  
        this(checkBox, null, null);
    }
    
    public Component getTableCellEditorComponent(JTable table, Object value,  
            boolean isSelected, int row, int column) {  
        if (value == null)  
            return checkBox;  
        checkBox.addItemListener(this);  
        variantValue = (VariantValue)value;
        if (variantValue.getValueStatus() == VariantValue.VALUE_USE) {
            checkBox.setSelected(true);  
        }else{  
            checkBox.setSelected(false);
        }
        
//		SRME:: [][20140812] swyoon  Prouct, Variant, Function�� �ɼ� ���� �ӵ� ����(�������� ��뿩�� üũ ����).        
		//�������� Variant value�� �������� �ٲ� ��� ������ ����.
        if( valueList != null){
	        if( !valueList.contains( table.getValueAt(row, 0)) && checkBox.isSelected()){
				checkBox.setBackground(Color.ORANGE);
				return checkBox;
			}
        }else{
        	if( !table.isCellEditable(row, 0)){
    			checkBox.setBackground(Color.ORANGE);
    			return checkBox;
    		}
        }
  
        if( !variantValue.isNew() ){
        	checkBox.setBackground(Color.LIGHT_GRAY);
        	if( !variantValue.isUsing()){
        		checkBox.setBackground(Color.white);
//				int[] selectedIdx = table.getSelectedRows();
//				for( int idx : selectedIdx){
//					if( idx == row){
//						checkBox.setBackground(new Color(51,153,255));
//						return checkBox;
//					}
//				}
//				checkBox.setBackground(Color.LIGHT_GRAY);
			}
		}else{
			checkBox.setBackground(Color.white);
		}
        
//        checkBox.setBackground(new Color(51,153,255));
        return checkBox;  
    }  
  
    public Object getCellEditorValue() {  
        if(checkBox.isSelected() == true)  
            return new Boolean(true);  
        else   
            return new Boolean(false);  
    }  
  
    @Override  
    public void addCellEditorListener(CellEditorListener l) {  
    }  
  
    @Override  
    public void cancelCellEditing() {  
  
    }  
  
    @Override  
    public boolean isCellEditable(EventObject anEvent) {  
        return true;  
    }  
  
    @Override  
    public void removeCellEditorListener(CellEditorListener l) {  
  
    }  
  
    @Override  
    public boolean shouldSelectCell(EventObject anEvent) {  
        return true;  
    }  
  
    @Override  
    public boolean stopCellEditing() {  
        return true;  
    }  
  
    @Override  
    public void itemStateChanged(ItemEvent e) {  
    	if( checkBox.isSelected() ){
    		variantValue.setValueStatus(VariantValue.VALUE_USE);
    		if( !variantValue.isNew()){
    			unUsedValueList.remove(variantValue);
    		}
    	}else{
    		variantValue.setValueStatus(VariantValue.VALUE_NOT_USE);
    		
    		//���� �߰��� Value�� �ƴ� ���, unUsed�� �����Ǹ�
    		//���� Variant���� �� Value�� ����� ���, unUsed�� �����ؾ� �Ѵ�.
    		if( !variantValue.isNew()){
    			if( !unUsedValueList.contains(variantValue))
    				unUsedValueList.add(variantValue);
    		}
    	}
    	
    	if( detailTable != null){
//    		checkBox.repaint();
    		detailTable.repaint();
    	}
    }  
}  

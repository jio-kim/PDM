package com.ssangyong.common.utils.variant;
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
    public static Vector<VariantValue> unUsedValueList = new Vector<VariantValue>();	//새로 추가된 Value가 아니지만 초기 checked ==> unChecked 된 경우, 하위Variant에서도 Unused되도록 해야함.
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
        
//		SRME:: [][20140812] swyoon  Prouct, Variant, Function에 옵션 설정 속도 개선(하위에서 사용여부 체크 제거).        
		//상위에서 Variant value를 비사용으로 바꾼 경우 렌더링 변경.
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
    		
    		//새로 추가한 Value가 아닌 경우, unUsed로 설정되면
    		//하위 Variant에서 이 Value를 사용할 경우, unUsed로 변경해야 한다.
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

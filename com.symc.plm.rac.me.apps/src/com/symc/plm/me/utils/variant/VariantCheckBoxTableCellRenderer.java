/**
 * 
 */
package com.symc.plm.me.utils.variant;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

public class VariantCheckBoxTableCellRenderer implements TableCellRenderer {

    public Component getTableCellRendererComponent(JTable table,
            Object obj, boolean flag, boolean flag1, int row, int j) {
        JCheckBox cb = new JCheckBox();
        cb.setHorizontalAlignment(JLabel.CENTER);
        VariantValue value = (VariantValue)obj;
        if( value.getValueStatus() == VariantValue.VALUE_USE){
            cb.setSelected(true);
        }else{
            cb.setSelected(false);
        }
        
        //상위에서 Variant value를 비사용으로 바꾼 경우 렌더링 변경.
        if( !table.isCellEditable(row, 0)){
            cb.setBackground(Color.ORANGE);
            return cb;
        }
        
        if( !value.isNew()){
            cb.setBackground(Color.LIGHT_GRAY);
            if( !value.isUsing()){
                int[] selectedIdx = table.getSelectedRows();
                for( int idx : selectedIdx){
                    if( idx == row){
                        cb.setBackground(new Color(51,153,255));
                        return cb;
                    }
                }
                cb.setBackground(Color.white);
            }
        }else{
            
            int[] selectedIdx = table.getSelectedRows();
            for( int idx : selectedIdx){
                if( idx == row){
                    cb.setBackground(new Color(51,153,255));
                    return cb;
                }
            }
            cb.setBackground(Color.white);
        }
        
//      if( !value.isNew() ){
//          cb.setBackground(Color.LIGHT_GRAY);
//          if( !value.isUsing()){
//              int[] selectedIdx = jtable.getSelectedRows();
//              for( int idx : selectedIdx){
//                  if( idx == row){
//                      cb.setBackground(new Color(51,153,255));
//                      return cb;
//                  }
//              }
//              cb.setBackground(Color.white);
//          }
//      }else{
//          int[] selectedIdx = jtable.getSelectedRows();
//          for( int idx : selectedIdx){
//              if( idx == row){
//                  cb.setBackground(new Color(51,153,255));
//                  return cb;
//              }
//          }
//          cb.setBackground(Color.white);
//      }
        return cb;
    }
}

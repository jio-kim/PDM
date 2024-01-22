/**
 * 
 */
package com.symc.plm.me.utils.variant;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

public class VariantNode extends DefaultMutableTreeNode {
    
    private static final long serialVersionUID = 218086526780487318L;
    private VariantOption option;
    private VariantValue value;
    private int type;
    final private static int OPTION_TYPE = 100;
    final private static int VALUE_TYPE = 200;
    
    public VariantNode( VariantOption option){
        this.option = option;
        this.type = OPTION_TYPE;
        setUserObject( option );
        if( option.hasValues()){
            List<VariantValue> list = option.getValues();
            for( VariantValue value : list){
                VariantNode valueNode = new VariantNode(value);
                if( value.getValueStatus() == VariantValue.VALUE_USE){
                    this.add(valueNode);
                }
            }
        }
    }
    
    public VariantNode( VariantValue value){
        this.value = value;
        this.option = value.getOption();
        this.type = VALUE_TYPE;
        setUserObject(value);
    }

    @Override
    public String toString() {
        if( this.type == OPTION_TYPE){
            return this.option.getOptionName() + (this.option.getOptionDesc() == null || this.option.getOptionDesc().equals("") ? "" : " - " + this.option.getOptionDesc());
        }else if(this.type == VALUE_TYPE){
            return this.value.getValueName() + (this.value.getValueDesc() == null || this.value.getValueDesc().equals("") ? "" : " - " + this.value.getValueDesc());
        }
        return super.toString();
    }
    
    
}

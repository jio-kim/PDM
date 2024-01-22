/**
 * 
 */
package org.sdv.core.ui.view.layout;

/**
 * Class Name : LayoutType
 * Class Description : 
 * @date 	2013. 12. 3.
 * @author  CS.Park
 * 
 */
public enum LayoutType {

    FILL_LAYOUT(0, "fillLayoutView"), 
    GRID_LAYOUT(1, "gridLayoutView"), 
    BORDER_LAYOUT(2, "fillLayoutView"),
    TAB_LAYOUT(3, "tabLayout"),
    SPLIT_LAYOUT(4, "splitLayout");
    
    private String typeName;
    private int type;
    
    private LayoutType(int type, String typeName){
        this.type = type;
        this.typeName = typeName;
    }
    
    public String getTypeName(){
        return typeName;
    }
    
    public int getType(){
        return type;
    }
}

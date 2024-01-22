/**
 * 
 */
package com.symc.plm.me.sdv.service.migration.model.tcdata.basic;

import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;

import com.symc.plm.activator.Activator;
import com.symc.plm.me.sdv.service.migration.model.tcdata.TCData;

/**
 * Class Name : DatasetData
 * Class Description :
 * 
 * @date 2013. 11. 14.
 * 
 */
public class DatasetData extends TCData {

    public DatasetData(Tree parentTree, int index, String classType, TreeColumn[] columns) {
        super(parentTree, index, classType, columns);        
    }
    
    public DatasetData(TCData parentItem, int index, String classType, TreeColumn[] columns) {
        super(parentItem, index, classType, columns);        
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see com.symc.plm.me.sdv.service.migration.model.TCData#setClassImage()
     */
    @Override
    protected void setClassImage() {
       setImage(Activator.imageDescriptorFromPlugin("com.teamcenter.rac.common", "icons/newdataset_16.png").createImage());
    }
}

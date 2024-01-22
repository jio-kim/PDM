/**
 * 
 */
package com.symc.plm.me.sdv.service.migration.model.tcdata.bop;

import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;

import com.symc.plm.activator.Activator;
import com.symc.plm.me.sdv.service.migration.model.tcdata.TCData;

/**
 * Class Name : EquipmentData
 * Class Description : 
 * @date 2013. 11. 22.
 *
 */
public class EquipmentData extends OccurrenceData {

    /**
     * @param parentItem
     * @param index
     * @param classType
     * @param columns
     */
    public EquipmentData(TCData parentItem, int index, String classType, TreeColumn[] columns) {
        super(parentItem, index, classType, columns);
    }

    /**
     * @param parentTree
     * @param index
     * @param classType
     * @param columns
     */
    public EquipmentData(Tree parentTree, int index, String classType, TreeColumn[] columns) {
        super(parentTree, index, classType, columns);
    }
    
    /* (non-Javadoc)
     * @see com.symc.plm.me.sdv.service.migration.model.TCData#setClassImage()
     */
    @Override
    protected void setClassImage() {
        setImage(Activator.imageDescriptorFromPlugin("com.teamcenter.rac.common", "icons/mencmachinerevision_16.png").createImage());
        
    }   

}

/**
 * 
 */
package com.symc.plm.me.sdv.service.migration.model.tcdata.bop;

import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;

import com.symc.plm.activator.Activator;
import com.symc.plm.me.sdv.service.migration.model.tcdata.TCData;
import com.symc.plm.me.sdv.service.migration.model.tcdata.basic.ItemData;

/**
 * Class Name : LineItemData
 * Class Description :
 * 
 * @date 2013. 12. 2.
 * 
 */
public class LineItemData extends ItemData {

    /**
     * @param parentItem
     * @param index
     * @param classType
     * @param columns
     */
    public LineItemData(TCData parentItem, int index, String classType, TreeColumn[] columns) {
        super(parentItem, index, classType, columns);
    }

    /**
     * @param parentTree
     * @param index
     * @param classType
     * @param columns
     */
    public LineItemData(Tree parentTree, int index, String classType, TreeColumn[] columns) {
        super(parentTree, index, classType, columns);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.symc.plm.me.sdv.service.migration.model.TCData#setClassImage()
     */
    @Override
    protected void setClassImage() {
        setImage(Activator.imageDescriptorFromPlugin("com.teamcenter.rac.common", "icons/process_line_rev_16.png").createImage());
    }
}

/**
 * 
 */
package com.symc.plm.me.sdv.service.migration.model.tcdata.bop;

import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;

import com.symc.plm.me.sdv.service.migration.model.tcdata.TCData;
import com.symc.plm.activator.Activator;

/**
 * Class Name : SubsidiaryData
 * Class Description :
 * 
 * @date 2013. 11. 22.
 * 
 */
public class SubsidiaryData extends OccurrenceData {
    // Conversion Option Condition
    String conversionOptionCondition;
    
    /**
     * @param parentItem
     * @param index
     * @param classType
     * @param columns
     */
    public SubsidiaryData(TCData parentItem, int index, String classType, TreeColumn[] columns) {
        super(parentItem, index, classType, columns);
    }

    /**
     * @param parentTree
     * @param index
     * @param classType
     * @param columns
     */
    public SubsidiaryData(Tree parentTree, int index, String classType, TreeColumn[] columns) {
        super(parentTree, index, classType, columns);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.symc.plm.me.sdv.service.migration.model.TCData#setClassImage()
     */
    @Override
    protected void setClassImage() {
        setImage(Activator.imageDescriptorFromPlugin("com.teamcenter.rac.common", "icons/meresourcerevision_16.png").createImage());

    }

    /**
     * @return the conversionOptionCondition
     */
    public String getConversionOptionCondition() {
        return conversionOptionCondition;
    }

    /**
     * @param conversionOptionCondition
     *            the conversionOptionCondition to set
     */
    public void setConversionOptionCondition(String conversionOptionCondition) {
        this.conversionOptionCondition = conversionOptionCondition;
    }
}

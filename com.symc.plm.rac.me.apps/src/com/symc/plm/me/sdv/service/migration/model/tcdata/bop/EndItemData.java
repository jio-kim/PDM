/**
 * 
 */
package com.symc.plm.me.sdv.service.migration.model.tcdata.bop;

import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;

import com.symc.plm.activator.Activator;
import com.symc.plm.me.sdv.service.migration.model.tcdata.TCData;
import com.teamcenter.rac.kernel.TCComponentBOMLine;

/**
 * Class Name : EndItemData
 * Class Description :
 * 
 * @date 2013. 11. 22.
 * 
 */
public class EndItemData extends OccurrenceData {
    protected String functionItemId;
    protected String absOccPuids;
    protected String occPuid;

    // MBOM과 BOP 간의 링크된 BOMLine
    protected TCComponentBOMLine endItemMBOMLine;

    /**
     * @param parentItem
     * @param index
     * @param classType
     * @param columns
     */
    public EndItemData(TCData parentItem, int index, String classType, TreeColumn[] columns) {
        super(parentItem, index, classType, columns);
    }

    /**
     * @param parentTree
     * @param index
     * @param classType
     * @param columns
     */
    public EndItemData(Tree parentTree, int index, String classType, TreeColumn[] columns) {
        super(parentTree, index, classType, columns);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.symc.plm.me.sdv.service.migration.model.TCData#setClassImage()
     */
    @Override
    protected void setClassImage() {
        setImage(Activator.imageDescriptorFromPlugin("com.teamcenter.rac.common", "icons/manufacturer_part_rev_16.png").createImage());

    }

    /**
     * @return the functionItemId
     */
    public String getFunctionItemId() {
        return functionItemId;
    }

    /**
     * @param functionItemId
     *            the functionItemId to set
     */
    public void setFunctionItemId(String functionItemId) {
        this.functionItemId = functionItemId;
    }

    /**
     * @return the absOccPuids
     */
    public String getAbsOccPuids() {
        return absOccPuids;
    }

    /**
     * @param absOccPuids
     *            the absOccPuids to set
     */
    public void setAbsOccPuids(String absOccPuids) {
        this.absOccPuids = absOccPuids;
    }

    /**
     * @return the endItemMBOMLine
     */
    public TCComponentBOMLine getEndItemMBOMLine() {
        return endItemMBOMLine;
    }

    /**
     * @param endItemMBOMLine
     *            the endItemMBOMLine to set
     */
    public void setEndItemMBOMLine(TCComponentBOMLine endItemMBOMLine) {
        this.endItemMBOMLine = endItemMBOMLine;
    }

    /**
     * @return the occPuid
     */
    public String getOccPuid() {
        return occPuid;
    }

    /**
     * @param occPuid
     *            the occPuid to set
     */
    public void setOccPuid(String occPuid) {
        this.occPuid = occPuid;
    }

}

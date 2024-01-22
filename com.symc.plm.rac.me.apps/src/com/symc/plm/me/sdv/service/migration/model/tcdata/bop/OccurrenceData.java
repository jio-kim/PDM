/**
 * 
 */
package com.symc.plm.me.sdv.service.migration.model.tcdata.bop;

import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;

import com.symc.plm.me.sdv.service.migration.model.tcdata.TCData;
import com.symc.plm.activator.Activator;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentItem;

/**
 * Class Name : OccurrenceData
 * Class Description :
 * 
 * @date 2013. 11. 22.
 * 
 */
public class OccurrenceData extends TCData {
    // Resource Item ID
    private String itemId;
    
    // Resource resourceItem
    private TCComponentItem resourceItem;
    
    // BOP BOMLine
    private TCComponentBOMLine bopBomLine;
    
    // Product BOMLine
    private TCComponentBOMLine productBomLine;
    
    // PE - TC간 BOMLine 속성정보가 변경사항이 있는지 체크
    private boolean isBOMLineModifiable;
    
    // FindNo.
    private String findNo;

    /**
     * @return the bopBomLine
     */
    public TCComponentBOMLine getBopBomLine() {
        return bopBomLine;
    }

    /**
     * @param bopBomLine
     *            the bopBomLine to set
     */
    public void setBopBomLine(TCComponentBOMLine bopBomLine) {
        this.bopBomLine = bopBomLine;
    }
    
    /**
     * @return the bopBomLine
     */
    public TCComponentBOMLine getProductBomLine() {
        return productBomLine;
    }

    /**
     * @param bopBomLine
     *            the bopBomLine to set
     */
    public void setProductBomLine(TCComponentBOMLine productBomLine) {
        this.productBomLine = productBomLine;
    }

    /**
     * @return the resourceItem
     */
    public TCComponentItem getResourceItem() {
        return resourceItem;
    }

    /**
     * @param resourceItem
     *            the resourceItem to set
     */
    public void setResourceItem(TCComponentItem resourceItem) {
        this.resourceItem = resourceItem;
    }

    /**
     * @param parentTree
     * @param index
     * @param classType
     * @param columns
     */
    public OccurrenceData(Tree parentTree, int index, String classType, TreeColumn[] columns) {
        super(parentTree, index, classType, columns);
    }

    /**
     * @param parentItem
     * @param index
     * @param classType
     * @param columns
     */
    public OccurrenceData(TCData parentItem, int index, String classType, TreeColumn[] columns) {
        super(parentItem, index, classType, columns);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.symc.plm.me.sdv.service.migration.model.tc.TCData#setClassImage()
     */
    @Override
    protected void setClassImage() {
        setImage(Activator.imageDescriptorFromPlugin("com.teamcenter.rac.common", "icons/absoccincontext_16.png").createImage());

    }

    /**
     * @return the itemId
     */
    public String getItemId() {
        return itemId;
    }

    /**
     * @param itemId
     *            the itemId to set
     */
    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    /**
     * @return the isBOMLineModifiable
     */
    public boolean isBOMLineModifiable() {
        return isBOMLineModifiable;
    }

    /**
     * @param isBOMLineModifiable
     *            the isBOMLineModifiable to set
     */
    public void setBOMLineModifiable(boolean isBOMLineModifiable) {
        this.isBOMLineModifiable = isBOMLineModifiable;
    }

    /**
     * @return the findNo
     */
    public String getFindNo() {
        return findNo;
    }

    /**
     * @param findNo
     *            the findNo to set
     */
    public void setFindNo(String findNo) {
        this.findNo = findNo;
    }

}

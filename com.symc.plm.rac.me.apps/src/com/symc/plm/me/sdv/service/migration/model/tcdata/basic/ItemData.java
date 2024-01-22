/**
 * 
 */
package com.symc.plm.me.sdv.service.migration.model.tcdata.basic;

import java.util.HashMap;

import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.w3c.dom.Node;

import com.symc.plm.activator.Activator;
import com.symc.plm.me.sdv.service.migration.model.tcdata.TCData;
import com.teamcenter.rac.kernel.TCComponentBOMLine;

/**
 * Class Name : ItemData
 * Class Description :
 * 
 * @date 2013. 11. 14.
 * 
 */
public class ItemData extends TCData {

    protected boolean isExistItem; // Item이 TC에 생성되어있는지 유무
    protected String itemId;
    protected String itemName;
    protected String revId;
    protected boolean isReleased;
    protected int itemDataType = 0;
    
    // Item Properties
    protected HashMap<String, Object> itemProperties;
    // BOMLine Properties
    protected HashMap<String, Object> bomlineProperties;

    public ItemData(Tree parentTree, int index, String classType, TreeColumn[] columns) {
        super(parentTree, index, classType, columns);
    }

    public ItemData(TCData parentItem, int index, String classType, TreeColumn[] columns) {
        super(parentItem, index, classType, columns);
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see com.symc.plm.me.sdv.service.migration.model.TCData#setClassImage()
     */
    @Override
    protected void setClassImage() {
        setImage(Activator.imageDescriptorFromPlugin("com.teamcenter.rac.common", "icons/itemrevision_16.png").createImage());
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
     * @return the itemName
     */
    public String getItemName() {
        return itemName;
    }

    /**
     * @param itemName
     *            the itemName to set
     */
    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    /**
     * @return the revId
     */
    public String getRevId() {
        return revId;
    }

    /**
     * @param revId
     *            the revId to set
     */
    public void setRevId(String revId) {
        this.revId = revId;
    }

    /**
     * @return the itemProperties
     */
    public HashMap<String, Object> getItemProperties() {
        return itemProperties;
    }

    /**
     * @param itemProperties
     *            the itemProperties to set
     */
    public void setItemProperties(HashMap<String, Object> itemProperties) {
        this.itemProperties = itemProperties;
    }

    /**
     * @return the bomlineProperties
     */
    public HashMap<String, Object> getBomlineProperties() {
        return bomlineProperties;
    }

    /**
     * @param bomlineProperties
     *            the bomlineProperties to set
     */
    public void setBomlineProperties(HashMap<String, Object> bomlineProperties) {
        this.bomlineProperties = bomlineProperties;
    }

    /**
     * @return the isReleased
     */
    public boolean isReleased() {
        return isReleased;
    }

    /**
     * @param isReleased
     *            the isReleased to set
     */
    public void setReleased(boolean isReleased) {
        this.isReleased = isReleased;
    }

    /**
     * @return the isExistItem
     */
    public boolean isExistItem() {
        return isExistItem;
    }

    /**
     * @param isExistItem
     *            the isExistItem to set
     */
    public void setExistItem(boolean isExistItem) {
        this.isExistItem = isExistItem;
    }

}

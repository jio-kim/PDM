/**
 * 
 */
package com.symc.plm.me.sdv.service.migration.model.tcdata.bop;

import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;

import com.symc.plm.activator.Activator;
import com.symc.plm.me.sdv.service.migration.model.tcdata.TCData;

/**
 * Class Name : ActivityMasterData
 * Class Description :
 * 
 * @date 2013. 11. 22.
 * 
 */
public class ActivityMasterData extends TCData {
    // PE-TC I/F의 Activity가 다른지 유효성 체크
    boolean createable;

    /**
     * @param parentTree
     * @param index
     * @param classType
     * @param columns
     */
    public ActivityMasterData(Tree parentTree, int index, String classType, TreeColumn[] columns) {
        super(parentTree, index, classType, columns);
    }

    /**
     * @param parentItem
     * @param index
     * @param classType
     * @param columns
     */
    public ActivityMasterData(TCData parentItem, int index, String classType, TreeColumn[] columns) {
        super(parentItem, index, classType, columns);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.symc.plm.me.sdv.service.migration.model.tc.TCData#setClassImage()
     */
    @Override
    protected void setClassImage() {
        setImage(Activator.imageDescriptorFromPlugin("com.teamcenter.rac.common", "icons/activity_view_16.png").createImage());

    }

    /**
     * @return the createable
     */
    public boolean isCreateable() {
        return createable;
    }

    /**
     * @param createable
     *            the createable to set
     */
    public void setCreateable(boolean createable) {
        this.createable = createable;
    }

}

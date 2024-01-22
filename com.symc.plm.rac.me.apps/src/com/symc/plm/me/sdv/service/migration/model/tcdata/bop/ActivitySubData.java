/**
 * 
 */
package com.symc.plm.me.sdv.service.migration.model.tcdata.bop;

import org.eclipse.swt.widgets.TreeColumn;

import com.symc.plm.activator.Activator;
import com.symc.plm.me.sdv.service.migration.model.tcdata.TCData;
import com.teamcenter.rac.kernel.TCComponent;

/**
 * Class Name : ActivitySubData
 * Class Description :
 * 
 * @date 2013. 11. 22.
 * 
 */
public class ActivitySubData extends TCData {
    
    // PE-TC I/F의 Activity가 다른지 유효성 체크
    boolean createable;
    int activitySeq = -1;
    TCComponent activityComponent;

	/**
     * @param parentItem
     * @param index
     * @param classType
     * @param columns
     */
    public ActivitySubData(ActivityMasterData parentItem, int index, String classType, TreeColumn[] columns) {
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
     * @param createable the createable to set
     */
    public void setCreateable(boolean createable) {
        this.createable = createable;
    }    
    
    public int getActivitySeq() {
		return activitySeq;
	}

	public void setActivitySeq(int activitySeq) {
		this.activitySeq = activitySeq;
	}
	
	public TCComponent getActivityComponent() {
		return activityComponent;
	}

	public void setActivityComponent(TCComponent activityComponent) {
		this.activityComponent = activityComponent;
	}

}

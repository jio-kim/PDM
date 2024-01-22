/**
 * 
 */
package com.symc.plm.me.sdv.service.migration.model.tcdata.bop;

import java.util.HashMap;
import java.util.List;

import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;

import com.symc.plm.me.sdv.service.migration.model.tcdata.TCData;
import com.symc.plm.me.sdv.service.migration.model.tcdata.basic.ItemData;
import com.symc.plm.activator.Activator;
import com.teamcenter.rac.kernel.TCComponentBOMLine;

/**
 * Class Name : OperationItemData
 * Class Description :
 * 
 * @date 2013. 11. 22.
 * 
 */
public class OperationItemData extends ItemData {
    // PE - TC간 Master 속성정보가 변경사항이 있는지 체크
    boolean isMasterModifiable;
    // PE - TC간 BOMLine 속성정보가 변경사항이 있는지 체크
    boolean isBOMLineModifiable;

    // TC에서 조회한 Operation하위 BOMLine List
    TCComponentBOMLine[] operationChildComponent;
    // TC에서 조회한 Operation Activity List
    List<HashMap<String, Object>> operationActivityList;

    // Conversion Option Condition
    String conversionOptionCondition;
    
    boolean haveWorkInstructionVolumeFileError = false;
    
	private boolean isWorkInstructionUpdateTarget = false;

	/**
     * @param parentItem
     * @param index
     * @param classType
     * @param columns
     */
    public OperationItemData(TCData parentItem, int index, String classType, TreeColumn[] columns) {
        super(parentItem, index, classType, columns);
    }

    /**
     * @param parentTree
     * @param index
     * @param classType
     * @param columns
     */
    public OperationItemData(Tree parentTree, int index, String classType, TreeColumn[] columns) {
        super(parentTree, index, classType, columns);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.symc.plm.me.sdv.service.migration.model.TCData#setClassImage()
     */
    @Override
    protected void setClassImage() {
        setImage(Activator.imageDescriptorFromPlugin("com.teamcenter.rac.common", "icons/meoperation_16.png").createImage());
    }

    /**
     * @return the operationChildComponent
     */
    public TCComponentBOMLine[] getOperationChildComponent() {
        return operationChildComponent;
    }

    /**
     * @param operationChildComponent
     *            the operationChildComponent to set
     */
    public void setOperationChildComponent(TCComponentBOMLine[] operationChildComponent) {
        this.operationChildComponent = operationChildComponent;
    }

    /**
     * @return the operationActivityList
     */
    public List<HashMap<String, Object>> getOperationActivityList() {
        return operationActivityList;
    }

    /**
     * @param operationActivityList
     *            the operationActivityList to set
     */
    public void setOperationActivityList(List<HashMap<String, Object>> operationActivityList) {
        this.operationActivityList = operationActivityList;
    }

    /**
     * @return the isMasterModifiable
     */
    public boolean isMasterModifiable() {
        return isMasterModifiable;
    }

    /**
     * @param isMasterModifiable
     *            the isMasterModifiable to set
     */
    public void setMasterModifiable(boolean isMasterModifiable) {
        this.isMasterModifiable = isMasterModifiable;
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
     * 조립작업표준서 Update 대상인지 기록하는 Falg의 값을 Return 한다.
     * @return
     */
    public boolean isWorkInstructionUpdateTarget() {
		return isWorkInstructionUpdateTarget;
	}

    /**
     * 조립작업표준서 Update 대상인지 기록하는 Falg의 값을 설정 한다.
     * @param isWorkInstructionUpdateTarget
     */
	public void setWorkInstructionUpdateTarget(boolean isWorkInstructionUpdateTarget) {
		this.isWorkInstructionUpdateTarget = isWorkInstructionUpdateTarget;
	}

	/**
	 * Validation 과정에 발견된 Volume에 실제 하지 않는 Work Instruction 파일이 있는지 여부를 Return 한다.
	 * @return Volumn에 파일이 실제 하지 않으면 true를 Return
	 */
	public boolean isHaveWorkInstructionVolumeFileError() {
		return haveWorkInstructionVolumeFileError;
	}

	/**
	 * Validation 과정에 발견된 Volume에 실제 하지 않는 Work Instruction 파일이 있는지 여부를 설정
	 * @param haveWorkInstructionVolumeFileError
	 */
	public void setHaveWorkInstructionVolumeFileError(
			boolean haveWorkInstructionVolumeFileError) {
		this.haveWorkInstructionVolumeFileError = haveWorkInstructionVolumeFileError;
	}
}

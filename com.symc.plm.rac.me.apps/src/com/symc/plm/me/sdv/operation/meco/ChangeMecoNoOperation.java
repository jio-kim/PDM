/**
 * 
 */
package com.symc.plm.me.sdv.operation.meco;

import java.util.ArrayList;

import org.sdv.core.common.data.IDataSet;
import org.sdv.core.ui.operation.AbstractSDVActionOperation;

import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.utils.CustomUtil;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentItemRevision;

/**
 * Class Name : ModifyMecoNoOperation
 * Class Description : MECO No 를 수정하고, 이전 MECO의 Solution Item에서 제거함
 * 
 * @date 2014. 2. 6.
 * 
 */
public class ChangeMecoNoOperation extends AbstractSDVActionOperation {

    /**
     * @param actionId
     * @param ownerId
     * @param dataset
     */
    public ChangeMecoNoOperation(int actionId, String ownerId, IDataSet dataset) {
        super(actionId, ownerId, dataset);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sdv.core.common.ISDVOperation#startOperation(java.lang.String)
     */
    @Override
    public void startOperation(String commandId) {

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sdv.core.common.ISDVOperation#endOperation()
     */
    @Override
    public void endOperation() {

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.teamcenter.rac.aif.AbstractAIFOperation#executeOperation()
     */
    @Override
    public void executeOperation() throws Exception {
        IDataSet dataSet = getDataSet();
        try {
            TCComponentItemRevision mecoItemRevision = (TCComponentItemRevision) dataSet.getValue(SDVPropertyConstant.ITEM_REV_MECO_NO);
            @SuppressWarnings("unchecked")
            ArrayList<TCComponentBOMLine> targetBOMList = (ArrayList<TCComponentBOMLine>) dataSet.getValue("TARGET_LIST");
            for (TCComponentBOMLine targetBOMLine : targetBOMList) {
                TCComponent mecoComp = targetBOMLine.getItemRevision().getReferenceProperty(SDVPropertyConstant.ITEM_REV_MECO_NO);
                if (mecoComp != null) {
                    try {
                        // Old MECO에서 제거함
                        mecoComp.remove(SDVTypeConstant.MECO_SOLUTION_ITEM, targetBOMLine.getItemRevision());

                        // [SR141117-014][2014.12.19][jclee]
                        // OLD MECO의 Problem Items에 현재 선택한 BOM Line Item Revision의 이전 Revision이 존재할 경우 함께 제거
                        TCComponentItemRevision prevItemRevision = CustomUtil.getPreviousRevision(targetBOMLine.getItemRevision());

                        if (prevItemRevision != null) {
                        	mecoComp.remove(SDVTypeConstant.MECO_PROBLEM_ITEM, prevItemRevision);
						}

                    } catch (Exception ex) {
                    }
                }
                // MECO No 변경
                targetBOMLine.getItemRevision().setReferenceProperty(SDVPropertyConstant.ITEM_REV_MECO_NO, mecoItemRevision);
                targetBOMLine.refresh();
            }

        } catch (Exception ex) {
            setAbortRequested(true);
            ex.printStackTrace();
            throw ex;
        }

    }

}

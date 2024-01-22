/**
 * 
 */
package com.symc.plm.me.sdv.operation.body;

import org.sdv.core.common.data.DataSet;
import org.sdv.core.common.data.IData;
import org.sdv.core.common.data.RawDataMap;
import org.sdv.core.ui.operation.AbstractSDVInitOperation;

import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.utils.CustomUtil;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.cme.application.MFGLegacyApplication;
import com.teamcenter.rac.kernel.TCComponentBOMLine;

/**
 * Class Name : CreateBodyLineInitOperation
 * Class Description :
 * 
 * @date 2013. 12. 9.
 * 
 */
public class CreateBodyLineInitOperation extends AbstractSDVInitOperation {

    /**
     * 
     */
    public CreateBodyLineInitOperation() {
        super();
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see com.teamcenter.rac.aif.AbstractAIFOperation#executeOperation()
     */
    @Override
    public void executeOperation() throws Exception {

        try {
            if (!(AIFUtility.getCurrentApplication() instanceof MFGLegacyApplication)) {
                // MPPApplication Check
                throw new Exception("MPP Application에서 작업해야 합니다.");
            }
            InterfaceAIFComponent[] selectedTargets = CustomUtil.getCurrentApplicationTargets();
            if (selectedTargets.length > 1)
                throw new Exception("대상 SHOP을 하나만 선택해 주세요.");

            if ((!(selectedTargets[0] instanceof TCComponentBOMLine)) || (!((TCComponentBOMLine) selectedTargets[0]).getItem().getType().equals(SDVTypeConstant.BOP_PROCESS_SHOP_ITEM))) {
                throw new Exception("LINE을 생성할 SHOP을 선택해 주세요.");
            }

            RawDataMap targetDataMap = new RawDataMap();
            targetDataMap.put(SDVTypeConstant.BOP_PROCESS_SHOP_ITEM, selectedTargets[0], IData.OBJECT_FIELD);
            //targetDataMap.put(CreateBodyLineView.BodyOPViewType, CreateBodyOPView.CreateViewType, IData.INTEGER_FIELD);
            //targetDataMap.put(SDVPropertyConstant.SHOP_REV_SHOP_CODE, ((TCComponentBOMLine) selectedTargets[0]).getItemRevision().getProperty(SDVPropertyConstant.SHOP_REV_SHOP_CODE), IData.STRING_FIELD);
            
            DataSet targetDataset = new DataSet();
            targetDataset.addDataMap(SDVTypeConstant.BOP_PROCESS_SHOP_ITEM, targetDataMap);

            setData(targetDataset);
        } catch (Exception ex) {
            throw ex;
        }
    }
}

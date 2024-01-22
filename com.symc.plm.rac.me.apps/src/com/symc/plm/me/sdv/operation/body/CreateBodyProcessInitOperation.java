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
import com.teamcenter.rac.util.Registry;

/**
 * Class Name : CreateBodyProcessInitOperation
 * Class Description :
 * 
 * @date 2013. 12. 8.
 * 
 */
public class CreateBodyProcessInitOperation extends AbstractSDVInitOperation {
    private Registry registry = Registry.getRegistry(CreateBodyOPInitOperation.class);

    public CreateBodyProcessInitOperation() {
        super();
    }

    @Override
    public void executeOperation() throws Exception {
        try {
            if (!(AIFUtility.getCurrentApplication() instanceof MFGLegacyApplication)) {
                // MPPApplication Check
                throw new Exception(registry.getString("WorkInMPPApplication.MESSAGE", "MPP Application에서 작업해야 합니다."));
            }
            InterfaceAIFComponent[] selectedTargets = CustomUtil.getCurrentApplicationTargets();
            if (selectedTargets.length != 1)
                throw new Exception(registry.getString("SelectOneTargetLine.MESSAGE", "대상 라인을 하나만 선택해 주세요."));

            if ((!(selectedTargets[0] instanceof TCComponentBOMLine)) || (!((TCComponentBOMLine) selectedTargets[0]).getItem().getType().equals(SDVTypeConstant.BOP_PROCESS_LINE_ITEM))) {

                throw new Exception(registry.getString("SelectTargetLine.MESSAGE", "공정을 생성할 라인을 선택해 주세요."));
            }

            RawDataMap targetDataMap = new RawDataMap();

            targetDataMap.put(SDVTypeConstant.BOP_PROCESS_LINE_ITEM, selectedTargets[0], IData.OBJECT_FIELD);

            DataSet targetDataset = new DataSet();
            targetDataset.addDataMap(SDVTypeConstant.BOP_PROCESS_LINE_ITEM, targetDataMap);

            setData(targetDataset);
        } catch (Exception ex) {
            throw ex;
        }
    }

}

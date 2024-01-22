package com.symc.plm.me.sdv.validate;

import java.util.Map;

import org.sdv.core.common.ISDVValidator;
import org.sdv.core.common.exception.SDVException;
import org.sdv.core.common.exception.ValidateSDVException;

import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.sdv.service.Plant.PlantUtilities;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.cme.application.MFGLegacyApplication;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.util.Registry;

public class SelectedPlantShopSDVValidator implements ISDVValidator {

    private Registry registry = Registry.getRegistry(SelectedPlantShopSDVValidator.class);

    public SelectedPlantShopSDVValidator() {
    }

    @Override
    public void validate(String commandId, Map<String, Object> parameter, Object applicationCtx) throws SDVException {
        if (!(AIFUtility.getCurrentApplication() instanceof MFGLegacyApplication)) {
            throw new ValidateSDVException(registry.getString("Common.SelectedMPPApp.MESSAGE"));
        }

        try {
            MFGLegacyApplication mfgApp = (MFGLegacyApplication) AIFUtility.getCurrentApplication();
            InterfaceAIFComponent interfaceAIFComponent = mfgApp.getTargetComponent();
            
            if (interfaceAIFComponent == null) {
                throw new ValidateSDVException(registry.getString("SelectedPlant.Select.MESSAGE"));
            }

            TCComponentItemRevision itemRevision = PlantUtilities.getItemRevision(interfaceAIFComponent);

            if (itemRevision != null) {
                String itemRevisionType = itemRevision.getType();
                // 선택한 targetItem이 Shop이면 오류 처리
                if (itemRevisionType.equals(SDVTypeConstant.PLANT_SHOP_ITEM_REVISION)) {
                    throw new ValidateSDVException(registry.getString("SelectedPlantShop.Shop.MESSAGE"));
                }
            }
        } catch (Exception ex) {
            throw new ValidateSDVException(ex.getMessage(), ex);
        }

    }

}

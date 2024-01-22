package com.symc.plm.me.sdv.validate;

import java.util.Map;

import org.sdv.core.common.ISDVValidator;
import org.sdv.core.common.exception.SDVException;
import org.sdv.core.common.exception.ValidateSDVException;

import com.symc.plm.me.common.SDVBOPUtilities;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVTypeConstant;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.cme.application.MFGLegacyApplication;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.kernel.TCComponentBOPLine;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCPreferenceService;
import com.teamcenter.rac.kernel.TCPreferenceService.TCPreferenceLocation;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.Registry;
import com.teamcenter.services.internal.rac.structuremanagement._2011_06.VariantManagement.ModularOption;

public class MProductAssemblyTreeOptionSDVValidator implements ISDVValidator {

    private Registry registry = Registry.getRegistry(MProductAssemblyTreeOptionSDVValidator.class);

    public MProductAssemblyTreeOptionSDVValidator() {
    }

    @Override
    public void validate(String commandId, Map<String, Object> parameter, Object applicationCtx) throws SDVException {
        if(!(AIFUtility.getCurrentApplication() instanceof MFGLegacyApplication)) {
            throw new ValidateSDVException(registry.getString("Common.SelectedMPPApp.MESSAGE"));
        }

        MFGLegacyApplication mfgApp = (MFGLegacyApplication) AIFUtility.getCurrentApplication();
        TCComponentBOMWindow bomWindow = mfgApp.getBOMWindow();

        try {
            TCSession tcSession = SDVBOPUtilities.getTCSession();
            TCPreferenceService preferenceService = tcSession.getPreferenceService();
//            String[] corpIds = preferenceService.getStringArray(TCPreferenceService.TC_preference_site, "PSM_global_option_assemblytree_check");
            String[] corpIds = preferenceService.getStringValuesAtLocation("PSM_global_option_assemblytree_check", TCPreferenceLocation.OVERLAY_LOCATION);
            TCComponentBOPLine bopLine = (TCComponentBOPLine) bomWindow.getTopBOMLine();
            TCComponent[] mProduct = bopLine.getItemRevision().getRelatedComponents(SDVTypeConstant.MFG_TARGETS);
            if (mProduct.length == 1 && corpIds[0].equals("1")){
                String mProductType = mProduct[0].getProperty(SDVPropertyConstant.ITEM_OBJECT_TYPE);
                if (mProductType.equals(SDVTypeConstant.EBOM_MPRODUCT_REV)){
                    if (!isMProductOptionCheck(mProduct[0].getProperty(SDVPropertyConstant.ITEM_ITEM_ID))) {
                        throw new ValidateSDVException(registry.getString("MProductAssemblyTreeOptionUncheck.Check.MESSAGE", "Is External M-Product options, uncheck it."));
                    }
                }else{
                    throw new ValidateSDVException(registry.getString("MProductAssemblyTreeOption.Check.MESSAGE", "The M-Product BOP connection is not correct."));
                }
            }

        } catch(TCException e) {
            e.printStackTrace();
        }
    }

    /**
     *  MProduct 의 적용된 옵션이 implements 인지 uses 인지 체크한다
     *  적용된 옵션이 uses 로 적용되어 있으면 false 를 리턴한다
     */
    public static boolean isMProductOptionCheck(String mproductItemRevisionID) throws SDVException {
        try {
            TCComponentItem mproductItem = SDVBOPUtilities.FindItem(mproductItemRevisionID, SDVTypeConstant.EBOM_MPRODUCT);

            TCComponentItemRevision mproductItemRevision = mproductItem.getLatestItemRevision();

            // 복제되는 Top BOM Window
            TCComponentBOMWindow srcTopBomWindow = SDVBOPUtilities.getBOMWindow(mproductItemRevision, "Latest Working", "bom_view");
            // 복제되는 옵션을 가진 Top BOMLINE
            TCComponentBOMLine mproductTopBomLine = srcTopBomWindow.getTopBOMLine();
            // 복제되는 원본 옵션 리스트
            ModularOption[] mproductOptions = SDVBOPUtilities.getModularOptions(mproductTopBomLine);

            for (ModularOption modularOption : mproductOptions) {
                String[] firstOption = modularOption.mvlDefinitions;
                int optionStatus = 0;
                if (firstOption[0] != null) {
                    optionStatus = firstOption[0].indexOf("uses");
                }
                if (optionStatus > 0) {
                    return false;
                }
            }

        } catch (Exception ex) {
            throw new ValidateSDVException(ex.getMessage(), ex);
        }
        return true;
    }

}

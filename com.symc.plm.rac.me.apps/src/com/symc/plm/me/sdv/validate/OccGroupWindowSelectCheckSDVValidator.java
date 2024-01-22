/**
 *
 */
package com.symc.plm.me.sdv.validate;

import java.util.Map;

import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.sdv.core.common.ISDVValidator;
import org.sdv.core.common.exception.SDVException;
import org.sdv.core.common.exception.ValidateSDVException;

import com.symc.plm.me.common.SDVBOPUtilities;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.utils.CustomUtil;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.cme.framework.treetable.CMEBOMTreeTable;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentAppGroupBOPLine;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOPLine;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.util.AdapterUtil;
import com.teamcenter.rac.util.PlatformHelper;
import com.teamcenter.rac.util.Registry;

/**
 * Class Name : OccGroupWindowSelectCheckSDVValidator
 * Class Description :
 * 
 *  [SR150122-027][20150309]shcho, 공법 할당 E/Item의 설계 DPV에 의한 자동 변경 문제 해결 - Link해제된 MProduct를 찾을 수 있도록 수정
 *
 * @date 2014. 4. 3.
 *
 */
public class OccGroupWindowSelectCheckSDVValidator implements ISDVValidator {
    private Registry registry = Registry.getRegistry(OccGroupWindowSelectCheckSDVValidator.class);

    /**
     * Description :
     *
     * @method :
     * @date : 2014. 4. 3.
     * @param :
     * @return :
     * @see org.sdv.core.common.ISDVValidator#validate(java.lang.String, java.util.Map)
     */
    @Override
    public void validate(String commandId, Map<String, Object> parameter, Object applicationCtx) throws SDVException {
        try {
            InterfaceAIFComponent[] selectedTargets = CustomUtil.getCurrentApplicationTargets();

            TCComponentBOMLine bomLine = getMBOMLine((TCComponentBOPLine)selectedTargets[0]);

            // PERT 정보가 올바르게 되어 있는지 확인한다 (BOP 의 공정 갯수 = PERT 의 순서대로 가져온 공정 갯수)
            if (bomLine instanceof TCComponentAppGroupBOPLine) {
                throw new ValidateSDVException(registry.getString("occBomLineWindow.OccGroup.MESSAGE", "OccGroup current target of M-Product Window open to view. \nPlease change the M-Product view."));
            }


        } catch (ValidateSDVException ve) {
            throw ve;
        } catch (Exception e) {
            throw new ValidateSDVException(e.getMessage(), e);
        }

    }

    /**
     *       MPP의 활성화 되어있는 view 에서 BOMView 를 선택하여 BOMLine 정보를 담아간다
     *       MPP 의 여러개에 TAB View 중에 cc 정보와 TYPE 정보를 비교하여 선택한 BOP 탭과
     *       연관되어 있는 BOMVIEW 데이터를 가져온다
     *
     *  [SR150122-027][20150309]shcho, 공법 할당 E/Item의 설계 DPV에 의한 자동 변경 문제 해결 - Link해제된 MProduct를 찾을 수 있도록 수정
     * 
     * @method getMBOMLine
     * @date 2014. 3. 31.
     * @param
     * @return TCComponentBOMLine
     * @exception
     * @throws
     * @see
     */
    public TCComponentBOMLine getMBOMLine(TCComponentBOPLine selectBOPLine) {
        String rootBomView = null;
        String targetProduct = null;
        TCComponentBOMLine rootBomline = null;
        try {
            TCComponentItemRevision itemRevision = selectBOPLine.window().getTopBOMLine().getItemRevision();
            String productCode = itemRevision.getProperty(SDVPropertyConstant.SHOP_REV_PRODUCT_CODE);
            if(productCode != null) {
                targetProduct = "M".concat(productCode.substring(1));
            }
        } catch (TCException e) {
            e.printStackTrace();
        }
        IViewReference[] arrayOfIViewReference = PlatformHelper.getCurrentPage().getViewReferences();
        //ISelection iSelection = PlatformHelper.getCurrentPage().getSelection();
        for (IViewReference viewRerence : arrayOfIViewReference) {
            IViewPart localIViewPart = viewRerence.getView(false);
            if (localIViewPart == null){
                continue;
            }
            CMEBOMTreeTable cmeBOMTreeTable = (CMEBOMTreeTable) AdapterUtil.getAdapter(localIViewPart, CMEBOMTreeTable.class);
            if (cmeBOMTreeTable == null){
                continue;
            }

            rootBomline = cmeBOMTreeTable.getBOMRoot();
            try {
                rootBomView = rootBomline.getProperty(SDVPropertyConstant.BL_ITEM_ID);
            } catch (TCException e) {
                e.printStackTrace();
            }
            if (targetProduct != null && rootBomView != null){
                if (targetProduct.equals(rootBomView)){
                    return rootBomline;
                }
            }
        }
        return null;
    }

    /**
     * BOP 최상의인 SHOP에 연결되어 있는 BOMView 정보(M7_Product) 를 확인하여 M_Product ID 정보를 반환 한다
     *
     * @method getMProduct
     * @date 2014. 3. 31.
     * @param
     * @return String
     * @exception
     * @throws
     * @see
     */
    /* [SR150122-027][20150309]shcho, 공법 할당 E/Item의 설계 DPV에 의한 자동 변경 문제 해결 - Shop과 MProduct Link해제로 더이상 사용안함.
    public String getMProduct(TCComponentItemRevision revision) throws TCException {
        String mProductType = null;
        String mProductId = null;
        TCComponent[] mProduct = revision.getRelatedComponents(SDVTypeConstant.MFG_TARGETS);
        if (mProduct.length == 1){
            mProductType = mProduct[0].getProperty(SDVPropertyConstant.ITEM_OBJECT_TYPE);
            if (mProductType.equals(SDVTypeConstant.EBOM_MPRODUCT_REV)){
                return mProductId = mProduct[0].getProperty(SDVPropertyConstant.ITEM_ITEM_ID);
            }
        }
        return mProductId;
    }*/


}

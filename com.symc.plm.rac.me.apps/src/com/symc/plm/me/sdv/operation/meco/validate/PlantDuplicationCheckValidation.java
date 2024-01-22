/**
 *
 */
package com.symc.plm.me.sdv.operation.meco.validate;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;

import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVTypeConstant;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.cme.framework.treetable.CMEBOMTreeTable;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.kernel.TCComponentBOPLine;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentMEAppearancePathNode;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.util.AdapterUtil;
import com.teamcenter.rac.util.PlatformHelper;

/**
 * BOP 의 할당된 Plant 자원(Gun)의 중복 할당여부 체크
 *
 * Class Name : NotFoundValidation
 * Class Description :
 *
 * @date 2013. 12. 19.
 * @author jwlee
 *
 */
public class PlantDuplicationCheckValidation extends OperationValidation<TCComponentBOMLine, String> {

    /*
     * (non-Javadoc)
     *
     * @see com.symc.plm.me.sdv.operation.meco.validate.NotFoundValidation#executeValidation()
     */
    @Override
    protected void executeValidation() throws Exception {

        StringBuilder errorMsgBuilder = new StringBuilder();
        AIFComponentContext[] comps = target.getChildren();
        //String[] vehicleCode = target.getProperty(SDVPropertyConstant.BL_ITEM_ID).split("-");
        String blOccId = "";
        //int gunTotal = 0;
        String weldOpId = target.getProperty(SDVPropertyConstant.BL_ITEM_ID);
        String gunId = "";
        TCComponentBOMWindow bopWindow = target.window();

        //TCSession session = CustomUtil.getTCSession();
        // Taget 으로 지정된 BOP View 와 연결된 Plant View 정보를 가져온다
        //TCComponentBOMLine plantLine = getPlantBOMLine(target);
        //TCComponentBOMWindow plantWindow = plantLine.window();

        for (AIFComponentContext comp : comps)
        {
            TCComponentBOPLine childen = (TCComponentBOPLine) comp.getComponent();

            String errorMsg = ""; // 에러 메세지
            if (childen.getItem().getType().equals(SDVTypeConstant.BOP_PROCESS_GUN_ITEM))
            {
                int bopAssignedSize = 0;
                blOccId = childen.getProperty(SDVPropertyConstant.BL_ABS_OCC_ID);
                gunId = childen.getProperty(SDVPropertyConstant.BL_ITEM_ID);
//                TCComponentMEAppearancePathNode[] linkedAppearances = childen.askLinkedAppearances(false);
//                TCComponentBOMLine linkedBOMLine = null;
//                for (TCComponentMEAppearancePathNode linkedAppearance : linkedAppearances)
//                    linkedBOMLine = plantWindow.getBOMLineFromAppearancePathNode(linkedAppearance, plantLine);

                TCComponentBOMLine[] bomLines = bopWindow.findAppearance(blOccId);
                for (TCComponentBOMLine bomLine : bomLines)
                {
                    if (bomLine.parent().getItem().getType().equals(SDVTypeConstant.BOP_PROCESS_BODY_WELD_OPERATION_ITEM))
                        bopAssignedSize++;
                }

                if (bopAssignedSize > 1)
                {
                    errorMsg = getMessage(ERROR_TYPE_GUN_DUPLICATE_ASSIGNED, weldOpId+ "(" + gunId + "->" + bopAssignedSize + ")");
                    errorMsgBuilder.append(errorMsg);
                }
                break;
            }
        }
        if (errorMsgBuilder.length() > 0)
            result = errorMsgBuilder.toString();
    }

    protected String getAppearancePathNode(TCComponentBOMLine targetBomLine, TCComponentBOMWindow targetWindow) throws TCException
    {
        AIFComponentContext[] childs =  targetBomLine.getChildren();
        for (AIFComponentContext child : childs)
        {
            TCComponentBOMLine bomLine = (TCComponentBOMLine) child.getComponent();
            String occUid = "";
            if (bomLine.getItem().getType().equals(SDVTypeConstant.BOP_PROCESS_GUN_ITEM))
            {
                TCComponentMEAppearancePathNode[] linkedAppearances = bomLine.askLinkedAppearances(false);
                TCComponentBOMLine linkedBOMLine = null;
                for (TCComponentMEAppearancePathNode linkedAppearance : linkedAppearances)
                {
                    linkedBOMLine = targetWindow.getBOMLineFromAppearancePathNode(linkedAppearance, targetWindow.getTopBOMLine());
                }
                occUid = linkedBOMLine.getProperty(SDVPropertyConstant.BL_ABS_OCC_ID);
                    //occUid = linkedAppearance.getUid();
                return occUid;
            }
        }
        return null;
    }

    /**
     * Gun 을 bl_abs_occ_id 를 비교하여 같은 Gun 인지 비교하여 갯수를 리턴한다
     *
     * @method compareGun
     * @date 2013. 12. 27.
     * @param
     * @return int
     * @exception
     * @throws
     * @see
     */
    protected List<TCComponentBOPLine> compareGun(List<TCComponentBOPLine> weldOperationList, String occId, TCComponentBOMWindow bopWindow) throws TCException
    {
        List<TCComponentBOPLine> weldOpList = new ArrayList<TCComponentBOPLine>();
        //int gunTotalNo = 0;
        for (TCComponentBOPLine weldOperation : weldOperationList)
        {
            AIFComponentContext[] weldChilds = weldOperation.getChildren();
            for (AIFComponentContext weldChild : weldChilds)
            {
                TCComponentBOPLine child = (TCComponentBOPLine) weldChild.getComponent();
                if (child.getItem().getType().equals(SDVTypeConstant.BOP_PROCESS_GUN_ITEM))
                {
                    weldOpList.add(weldOperation);
                }
            }
        }
        return weldOpList;
    }

    /**
     * 선택된 BOP의 연결된 Plant BOMLine 정보를 가져온다
     *
     * @method getPlantBOMLine
     * @date 2013. 12. 27.
     * @param
     * @return TCComponentBOMLine
     * @exception
     * @throws
     * @see
     */
    protected TCComponentBOMLine getPlantBOMLine(TCComponentBOMLine selectBOMLine) {
        TCComponentBOMLine rootBomline;
        String rootBomView = null;
        String targetPlant = null;
        try {
            TCComponentItemRevision itemRevision = selectBOMLine.window().getTopBOMLine().getItemRevision();
            targetPlant = getPlant(itemRevision);
        } catch (TCException e) {
            e.printStackTrace();
        }
        IViewReference[] arrayOfIViewReference = PlatformHelper.getCurrentPage().getViewReferences();
        for (IViewReference viewRerence : arrayOfIViewReference) {
            IViewPart localIViewPart = viewRerence.getView(false);
            if (localIViewPart == null)
                continue;
            CMEBOMTreeTable cmeBOMTreeTable = (CMEBOMTreeTable) AdapterUtil.getAdapter(localIViewPart, CMEBOMTreeTable.class);
            if (cmeBOMTreeTable == null)
                continue;

            rootBomline = cmeBOMTreeTable.getBOMRoot();
            try {
                rootBomView = rootBomline.getProperty("bl_item_item_id");
            } catch (TCException e) {
                e.printStackTrace();
            }
            if (targetPlant != null && rootBomView != null)
            {
                if (targetPlant.equals(rootBomView))
                    return rootBomline;
            }

        }
        return null;
    }

    /**
     *
     *
     * @method getPlant
     * @date 2013. 12. 27.
     * @param
     * @return String
     * @exception
     * @throws
     * @see
     */
    protected static String getPlant(TCComponentItemRevision revision) throws TCException {
        String plantType = null;
        String plantId = null;
        TCComponent[] plant = revision.getRelatedComponents("IMAN_MEWorkArea");
        if (plant.length == 1)
        {
            plantType = plant[0].getProperty("object_type");
            if (plantType.equals("PlantShopRevision"))
                return plantId = plant[0].getProperty("item_id");
        }
        return plantId;
    }

}

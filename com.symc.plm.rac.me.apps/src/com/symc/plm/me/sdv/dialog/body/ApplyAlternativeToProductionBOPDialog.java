/**
 *
 */
package com.symc.plm.me.sdv.dialog.body;

import org.eclipse.swt.widgets.Shell;
import org.sdv.core.beans.DialogStubBean;
import org.sdv.core.common.IViewPane;
import org.sdv.core.common.data.IDataMap;
import org.sdv.core.common.data.IDataSet;
import org.sdv.core.ui.dialog.SimpleSDVDialog;

import com.symc.plm.me.common.SDVBOPUtilities;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.utils.CustomUtil;
import com.symc.plm.me.utils.SYMTcUtil;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOPLine;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.util.Registry;

/**
 *
 */
public class ApplyAlternativeToProductionBOPDialog extends SimpleSDVDialog {
    Registry registry = Registry.getRegistry(ApplyAlternativeToProductionBOPDialog.class);

    /**
     * @param shell
     * @param dialogStub
     */
    public ApplyAlternativeToProductionBOPDialog(Shell shell, DialogStubBean dialogStub) {
        super(shell, dialogStub);
    }

    /**
     * @param shell
     * @param dialogStub
     * @param configId
     */
    public ApplyAlternativeToProductionBOPDialog(Shell shell, DialogStubBean dialogStub, int configId) {
        super(shell, dialogStub, configId);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.sdv.core.ui.dialog.AbstractSDVSWTDialog#validationCheck()
     */
    @Override
    protected boolean validationCheck() {
        try {
            IDataSet dataSet = getSelectDataSetAll();
            Object alt_shop_obj = dataSet.getValue(SDVTypeConstant.BOP_PROCESS_SHOP_ITEM);
            Object mproduct_obj = dataSet.getValue(SDVTypeConstant.EBOM_MPRODUCT);
            Object meco_object = dataSet.getValue(SDVTypeConstant.MECO_ITEM);

            if (alt_shop_obj == null || (!(alt_shop_obj instanceof TCComponentBOPLine))) {
                showErrorMessage("[" + registry.getString("ApplyAltToProduct.AltShopRequired.MESSAGE", "Alternative BOP") + "]" + registry.getString("RequiredField.MESSAGE", "is a required field."), null);
                return false;
            }

            if (meco_object == null || meco_object.toString().trim().length() == 0) {
                showErrorMessage("[" + SYMTcUtil.getPropertyDisplayName(SDVTypeConstant.BOP_PROCESS_SHOP_ITEM_REV, SDVPropertyConstant.SHOP_REV_MECO_NO) + "]" + registry.getString("RequiredField.MESSAGE", "is a required field."), null);
                return false;
            }

            if (meco_object != null && meco_object.toString().startsWith("MEW")) {
                showErrorMessage(registry.getString("MECOTypeOnlyWeldOP.MESSAGE", "MECO Type is only for Weld Operation."), null);
                return false;
            }

            if (mproduct_obj == null || (!(mproduct_obj instanceof TCComponentItemRevision))) {
                showErrorMessage("[" + registry.getString("ApplyAltToProduct.ProductShopRequired.MESSAGE", "M-Product") + "]" + registry.getString("RequiredField.MESSAGE", "is a required field."), null);
                return false;
            }

            // 선택한 Alternative BOP가 라인이면 M-Product에 해당하는 BOPShop가 존재하는지 체크해서 존재하지 않으면 에러 표시해야 함.
            if (alt_shop_obj != null && alt_shop_obj instanceof TCComponentBOPLine && ((TCComponentBOPLine) alt_shop_obj).getItem().getType().equals(SDVTypeConstant.BOP_PROCESS_LINE_ITEM)) {
                TCComponentItem targetShopItem = ((TCComponentBOPLine) alt_shop_obj).parent().getItem();
                String targetItemID = targetShopItem.getProperty(SDVPropertyConstant.ITEM_ITEM_ID);
                String altPrefix = targetShopItem.getLatestItemRevision().getProperty(SDVPropertyConstant.SHOP_REV_ALT_PREFIX);
                targetItemID = targetItemID.substring(altPrefix.length() + 1);
                TCComponentItem productionBOPShop = SYMTcUtil.findItem(CustomUtil.getTCSession(), targetItemID);
                if (productionBOPShop == null) {
                    showErrorMessage(registry.getString("ProductionBOPShopNotFound.MESSAGE", "Production BOP Shop Item not found. connect to BOPAdmin."), null);
                    return false;
                }
                // if (SYMTcUtil.isReleased(productionBOPShop.getLatestItemRevision()))
                // {
                // showErrorMessage(registry.getString("", ""), null);
                // }
            }

             // PERT 를 구성하는 라인의 존재 유무 체크
             String pre_line = checkPreDecessorsLine((TCComponentBOPLine) alt_shop_obj);
             if (!pre_line.equals("")) {
             showErrorMessage(registry.getString("." + "ApplyAltToProduct.ProductLineRequired.MESSAGE", "Sub Line Decessors does not exist in the Production BOP.\n\n" + "[" + pre_line + "]" + "\n\nPlease create Production BOP Line."), null);
             return false;
             }

             String ext_station = checkExtDecessorsStation((TCComponentBOPLine) alt_shop_obj);
             if (!ext_station.equals("")) {
             showErrorMessage(registry.getString("." + "ApplyAltToProduct.ProductLineRequired.MESSAGE", "Sub Line Decessors does not exist in the Production BOP.\n\n" + "[" + ext_station + "]" + "\n\nPlease create Production BOP Line."), null);
             return false;
             }

            // BOP 라인일경우
            if (((TCComponentBOPLine) alt_shop_obj).getItem().getType().equals(SDVTypeConstant.BOP_PROCESS_LINE_ITEM)) {
                String altPrefix = ((TCComponentBOMLine)alt_shop_obj).getItemRevision().getProperty(SDVPropertyConstant.LINE_REV_ALT_PREFIX);
                TCComponentItem shopItem = SDVBOPUtilities.FindItem(((TCComponentBOMLine)alt_shop_obj).window().getTopBOMLine().getItem().getProperty(SDVPropertyConstant.ITEM_ITEM_ID).replace(altPrefix + "-", ""), SDVTypeConstant.BOP_PROCESS_SHOP_ITEM);
                TCComponentItem lineItem = SDVBOPUtilities.FindItem(((TCComponentBOMLine)alt_shop_obj).getItem().getProperty(SDVPropertyConstant.ITEM_ITEM_ID).replace(altPrefix + "-", ""), SDVTypeConstant.BOP_PROCESS_SHOP_ITEM);
                // 라인이 존재하지 않을때
                if (lineItem == null) {
                    showErrorMessage(registry.getString("CreateLine.ConnectToAdmin.MESSAGE", "Create Line error.\n\n Please contact to BOP Admin."), null);
                    return false;
                }

                TCComponent[] releaseStatusListShop = shopItem.getLatestItemRevision().getReferenceListProperty(SDVPropertyConstant.ITEM_REV_RELEASE_STATUS_LIST);
                TCComponent[] releaseStatusListLine = lineItem.getLatestItemRevision().getReferenceListProperty(SDVPropertyConstant.ITEM_REV_RELEASE_STATUS_LIST);
                // SHOP 이 결제가 되어있는지-> BOPAdmin 에게 개정을 요청하세요
                if (releaseStatusListShop.length > 0) {
                    showErrorMessage(registry.getString("ReviseShop.ConnectToAdmin.MESSAGE", "Revise Shop error.\n\n Please contact to BOP Admin."), null);
                    return false;
                }
                // 넘기는 대상 Line이 결제가 되어 있는지 -> BOPAdmin 에게 개정을 요청하세요
                if (releaseStatusListLine.length > 0) {
                    showErrorMessage(registry.getString("ReviseLine.ConnectToAdmin.MESSAGE", "Revise Line error.\n\n Please contact to BOP Admin."), null);
                    return false;
                }
                // 봄뷰리비젼에 Write 권한 체크 하는 소스
                TCComponent[] bomViewTypes = lineItem.getLatestItemRevision().getReferenceListProperty(SDVTypeConstant.BOMLINE_RELATION);
                boolean result = CustomUtil.isWritable(bomViewTypes[0]);
                if (!result) {
                    showErrorMessage(registry.getString("WritablePermissions.ConnectToAdmin.MESSAGE", "Writable permissions error.\n\n Please contact to BOP Admin."), null);
                    return false;
                }
            // 공정일 경우
            }else if (((TCComponentBOPLine) alt_shop_obj).getItem().getType().equals(SDVTypeConstant.BOP_PROCESS_STATION_ITEM)) {
                String altPrefix = ((TCComponentBOMLine)alt_shop_obj).getItemRevision().getProperty(SDVPropertyConstant.STATION_ALT_PREFIX);
                TCComponentItem lineItem = SDVBOPUtilities.FindItem(((TCComponentBOMLine)alt_shop_obj).parent().getItem().getProperty(SDVPropertyConstant.ITEM_ITEM_ID).replace(altPrefix + "-", ""), SDVTypeConstant.BOP_PROCESS_LINE_ITEM);
                //TCComponentItem stationItem = SDVBOPUtilities.FindItem(((TCComponentBOMLine)alt_shop_obj).getItem().getProperty(SDVPropertyConstant.ITEM_ITEM_ID).replace(altPrefix + "-", ""), SDVTypeConstant.BOP_PROCESS_STATION_ITEM);
                // 라인이 존재하지 않을때
                if (lineItem == null) {
                    showErrorMessage(registry.getString("CreateLine.ConnectToAdmin.MESSAGE", "Create Line error.\n\n Please contact to BOP Admin."), null);
                    return false;
                }

                // 넘기는 대상 Line이 결제가 되어 있는지 -> BOPAdmin 에게 개정을 요청하세요
                TCComponent[] releaseStatusListLine = lineItem.getLatestItemRevision().getReferenceListProperty(SDVPropertyConstant.ITEM_REV_RELEASE_STATUS_LIST);
                if (releaseStatusListLine.length > 0) {
                    showErrorMessage(registry.getString("ReviseLine.ConnectToAdmin.MESSAGE", "Revise Line error.\n\n Please contact to BOP Admin."), null);
                    return false;
                }
                // 봄뷰리비젼에 Write 권한 체크 하는 소스
                TCComponent[] bomViewTypes = lineItem.getLatestItemRevision().getReferenceListProperty(SDVTypeConstant.BOMLINE_RELATION);
                boolean result = CustomUtil.isWritable(bomViewTypes[0]);
                if (!result) {
                    showErrorMessage(registry.getString("WritablePermissions.ConnectToAdmin.MESSAGE", "Writable permissions error.\n\n Please contact to BOP Admin."), null);
                    return false;
                }
            }

        } catch (Exception ex) {
            showErrorMessage(ex.getMessage(), ex);
            return false;
        }

        return false;
    }

    protected String checkExtDecessorsStation(TCComponentBOPLine bopLines) throws Exception {
        String extDecessors = "";
        String alt_prefix = null;
        alt_prefix = bopLines.getItemRevision().getProperty(SDVPropertyConstant.OPERATION_REV_ALT_PREFIX);
        for (AIFComponentContext bomLine : bopLines.getChildren()) {
            TCComponentBOMLine stationBomLine = (TCComponentBOMLine) bomLine.getComponent();
            TCComponent[] decessorsStations = stationBomLine.getItemRevision().getReferenceListProperty(SDVPropertyConstant.ME_EXT_DECESSORS);
            if (decessorsStations != null && decessorsStations.length > 0) {
                for (TCComponent decessorsStation : decessorsStations) {

                    String station_id = decessorsStation.getProperty(SDVPropertyConstant.ITEM_ITEM_ID);
                    station_id = station_id.replace(alt_prefix + "-", "");
                    TCComponentItem decessorsItem = SDVBOPUtilities.FindItem(station_id, SDVTypeConstant.BOP_PROCESS_STATION_ITEM);

                    // TCComponentItem decessorsItem = SDVBOPUtilities.FindItem(alt_prefix + "-" + decessorsLine.getStringProperty(SDVPropertyConstant.ITEM_ITEM_ID), SDVTypeConstant.BOP_PROCESS_LINE_ITEM);
                    if (decessorsItem == null) {
                        String decess_id = decessorsStation.getStringProperty(SDVPropertyConstant.ITEM_ITEM_ID);
                        decess_id = decess_id.replace(alt_prefix + "-", "");
                        extDecessors += decess_id + ", ";
                    }
                }
            }
        }
        return extDecessors;
    }

    private String checkPreDecessorsLine(TCComponentBOPLine bopLines) throws Exception {
        String PreDecessors = "";
        String alt_prefix = null;
        alt_prefix = bopLines.getItemRevision().getProperty(SDVPropertyConstant.OPERATION_REV_ALT_PREFIX);
        TCComponent[] decessorsLines = bopLines.getReferenceListProperty(SDVPropertyConstant.ME_PREDECESSORS);
        if (decessorsLines != null && decessorsLines.length > 0) {
            for (TCComponent decessorsLine : decessorsLines) {

                String line_id = decessorsLine.getProperty(SDVPropertyConstant.BL_ITEM_ID);
                line_id = line_id.replace(alt_prefix + "-", "");
                TCComponentItem decessorsItem = SDVBOPUtilities.FindItem(line_id, SDVTypeConstant.BOP_PROCESS_LINE_ITEM);

                // TCComponentItem decessorsItem = SDVBOPUtilities.FindItem(alt_prefix + "-" + decessorsLine.getStringProperty(SDVPropertyConstant.ITEM_ITEM_ID), SDVTypeConstant.BOP_PROCESS_LINE_ITEM);
                if (decessorsItem == null) {
                    String decess_id = decessorsLine.getStringProperty(SDVPropertyConstant.BL_ITEM_ID);
                    decess_id = decess_id.replace(alt_prefix + "-", "");
                    PreDecessors += decess_id + ", ";
                }
            }
        }
        return PreDecessors;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.sdv.core.common.IViewPane#initalizeLocalData(int, org.sdv.core.common.IViewPane, org.sdv.core.common.data.IDataSet)
     */
    @Override
    public void initalizeLocalData(int result, IViewPane owner, IDataSet dataset) {
    }

    /*
     * (non-Javadoc)
     *
     * @see org.sdv.core.ui.dialog.AbstractSDVSWTDialog#afterCreateContents()
     */
    @Override
    protected void afterCreateContents() {
    }

    /*
     * (non-Javadoc)
     *
     * @see org.sdv.core.ui.dialog.AbstractSDVSWTDialog#setLocalDataMap(org.sdv.core.common.data.IDataMap)
     */
    @Override
    public void setLocalDataMap(IDataMap dataMap) {
    }

    /*
     * (non-Javadoc)
     *
     * @see org.sdv.core.ui.dialog.AbstractSDVSWTDialog#getLocalDataMap()
     */
    @Override
    public IDataMap getLocalDataMap() {
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.sdv.core.ui.dialog.AbstractSDVSWTDialog#getLocalSelectDataMap()
     */
    @Override
    public IDataMap getLocalSelectDataMap() {
        return null;
    }

}

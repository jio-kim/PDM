package com.symc.plm.me.sdv.operation.occgroup;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.sdv.core.common.data.DataSet;
import org.sdv.core.common.data.IData;
import org.sdv.core.common.data.RawDataMap;
import org.sdv.core.ui.operation.AbstractSDVInitOperation;

import com.ssangyong.common.remote.SYMCRemoteUtil;
import com.symc.plm.me.common.SDVBOPUtilities;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVTypeConstant;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.cme.application.MFGLegacyApplication;
import com.teamcenter.rac.kernel.TCComponentAppGroupBOPLine;
import com.teamcenter.rac.kernel.TCComponentAppearanceGroup;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOPLine;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCException;

/**
 * [SR150529-025][20150828] shcho, 누적파트 생성/업데이트 방법 추가 개발 - Shop, Line 단위로 일괄적으로 누적파트 생성(업데이트) 할 수 있도록 기능 개선
 * 
 */
public class OccGroupCreateUpdateInitOperation extends AbstractSDVInitOperation {
    private final String serviceClassName = "com.ssangyong.service.BopPertService";

    @Override
    public void executeOperation() throws Exception {
        try {
            DataSet targetDataset = new DataSet();
            RawDataMap targetDataMap = new RawDataMap();

            ArrayList<HashMap<String, Object>> resultLastModDateList;
            ArrayList<HashMap<String, Object>> resultStationList;
            ArrayList<String> bopLineList = new ArrayList<String>();

            ArrayList<TCComponentBOPLine> stationBOPLineList = null;
            MFGLegacyApplication mfgApplication = (MFGLegacyApplication) AIFUtility.getCurrentApplication();
            TCComponentBOMLine[] selectedBOMLines = mfgApplication.getSelectedBOMLines();
            TCComponentBOPLine shopBopLine = (TCComponentBOPLine)selectedBOMLines[0].window().getTopBOMLine();
            stationBOPLineList = getStationBOPLineList(selectedBOMLines);

            targetDataMap.put("targetShop", shopBopLine, IData.OBJECT_FIELD);
            targetDataMap.put("targetStation", stationBOPLineList, IData.LIST_FIELD);

            // OccGroup 의 LastModDate을 가져온다
            Date oldestOCCDate = null;
            for(TCComponentBOPLine stationBOPLine : stationBOPLineList) {
                Date occDate = getOccLastModDate(stationBOPLine);
                if(occDate != null) {
                    if(oldestOCCDate == null) {
                        oldestOCCDate = occDate;
                        continue;
                    } 
                    
                    if(occDate.compareTo(oldestOCCDate) < 0) {
                        oldestOCCDate = occDate;
                    }
                }
            }
            
            // OccGroup 이 생성이 되어 있다면 BOP의 공법까지의 BOMViewRevision last_mod_date 정보까지 가져온다
            if (oldestOCCDate != null) {

                String shopID = shopBopLine.getProperty(SDVPropertyConstant.BL_ITEM_ID);
                String stationID = stationBOPLineList.get(stationBOPLineList.size() -1).getProperty(SDVPropertyConstant.BL_ITEM_ID);
                
                resultLastModDateList = getBopProcessBomViewLastModDate(shopID);
                resultStationList = getPredecessorsList(shopID, stationID);
                
                for (HashMap<String, Object> preDecessorStation : resultStationList) {
                    String predecessorSationID = (String) preDecessorStation.get("ID");
                    for (HashMap<String, Object> lastModDateInfo : resultLastModDateList) {
                        String parentID = (String) lastModDateInfo.get("PARENTS_ID");
                        if (predecessorSationID.equals(parentID)) {
                            Object lastModeDate = lastModDateInfo.get("LAST_MOD_DATE");
                            if (lastModeDate != null && oldestOCCDate.compareTo((Date) lastModeDate) < 0) {
                                bopLineList.add((String) lastModDateInfo.get("CHILD_ID"));
                            }
                        }
                    }
                }
            }
            
            targetDataMap.put("bopLineList", bopLineList, IData.LIST_FIELD);
            targetDataset.addDataMap("OccGroupCreateUpdateView", targetDataMap);
            setData(targetDataset);
        } catch (Exception ex) {
            throw ex;
        }
    }


    @SuppressWarnings("unchecked")
    private ArrayList<HashMap<String, Object>> getPredecessorsList(String shopID, String stationID) throws Exception{
        SYMCRemoteUtil remoteUtil = new SYMCRemoteUtil();

        com.ssangyong.common.remote.DataSet ds = new com.ssangyong.common.remote.DataSet();
        ds.put("SHOP_ID", shopID);
        ds.put("STATION_ID", stationID);

        ArrayList<HashMap<String, Object>> results;

        results = (ArrayList<HashMap<String, Object>>) remoteUtil.execute(serviceClassName, "selectStationDecessorsList", ds);
        return results;
    }
    
    
    @SuppressWarnings({ "unchecked" })
    public ArrayList<HashMap<String, Object>> getBopProcessBomViewLastModDate(String shopID) throws Exception{
        ArrayList<HashMap<String, Object>> resultDate = null;
        SYMCRemoteUtil remoteUtil = new SYMCRemoteUtil();

        com.ssangyong.common.remote.DataSet ds = new com.ssangyong.common.remote.DataSet();
        //((Map<String, Object>) ds).put("SHOP_ID", shopBopLine.getProperty(SDVPropertyConstant.BL_ITEM_ID));
        ds.put("SHOP_ID", shopID);

        resultDate = (ArrayList<HashMap<String, Object>>) remoteUtil.execute(serviceClassName, "selectBopStationDecessorsLastModDateList", ds);

        return resultDate;
    }

    
    public Date getOccLastModDate(TCComponentBOPLine stationBopLine) throws TCException{
        Date occDate = null;
        for (AIFComponentContext stationChild : stationBopLine.getChildren()) {
            if (stationChild.getComponent() instanceof TCComponentAppGroupBOPLine) {
                TCComponentAppGroupBOPLine occBopLine = (TCComponentAppGroupBOPLine) stationChild.getComponent();
                TCComponentAppearanceGroup occBopGroup = (TCComponentAppearanceGroup) occBopLine.getComponentIdentifierInContext();
                occDate = occBopGroup.getDateProperty(SDVPropertyConstant.ITEM_LAST_MODIFY_DATE);
                return occDate;
            }
        }
        return occDate;
    }


    private ArrayList<TCComponentBOPLine> getStationBOPLineList(TCComponentBOMLine[] tcComponentBOMLines) throws Exception {
        ArrayList<TCComponentBOPLine> stationBOPLineList = new ArrayList<TCComponentBOPLine>();
        for (TCComponentBOMLine selectedBOMLine : tcComponentBOMLines) 
        {
            TCComponentItem selectedItem = selectedBOMLine.getItem();
            if (selectedItem.getType().equals(SDVTypeConstant.BOP_PROCESS_SHOP_ITEM) || selectedItem.getType().equals(SDVTypeConstant.BOP_PROCESS_LINE_ITEM)) {

                /** Child BOMLine 수집 **/
                TCComponentBOMLine[] arrChildrenBOMLine = SDVBOPUtilities.getChildrenBOMLine(selectedBOMLine);

                /** 재귀호출 **/
                stationBOPLineList.addAll(getStationBOPLineList(arrChildrenBOMLine));
                
            } else if(selectedItem.getType().equals(SDVTypeConstant.BOP_PROCESS_STATION_ITEM)) {
                stationBOPLineList.add((TCComponentBOPLine)selectedBOMLine);
            }
        }
        
        return stationBOPLineList;
    }

}

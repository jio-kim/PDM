package com.symc.work.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.symc.common.dao.TcCommonDao;
import com.symc.common.soa.biz.Session;
import com.symc.common.soa.biz.TcItemUtil;
import com.symc.common.soa.util.TcConstants;
import com.symc.common.util.CCNUtil;
import com.symc.soa.service.MECOReportService;
import com.teamcenter.services.strong.core._2006_03.DataManagement.ItemProperties;
import com.teamcenter.soa.client.model.ModelObject;
import com.teamcenter.soa.client.model.strong.Group;
import com.teamcenter.soa.client.model.strong.Item;
import com.teamcenter.soa.client.model.strong.ItemRevision;

/**
 * [20160913][ymjang] Project Code 를 Item Revision 으로 부터 가져올 경우, OSpec 이 없는 Project 가 존재함. --> I/F 테이블로 부터 가져오도록 개선
 * [20160913][ymjang] PreVehicle Part 이외의 Part 의 경우, 업체 속성이 존재하지 않음 --> Vehicle/Std Part 는 대상에서 제외함.
 * [20160913][ymjang] CCN ID 채번 오류 (동일한 ID를 가져옴) 개선
 * [20160913][ymjang] CCN IF 생성 기능 프로시저로 이관함.
 * 현재 사용 안함 20180214
 */
public class InterfaceCostService {
    private Session tcSession;
    private TcItemUtil tcItemUtil;
    private TcCommonDao dbDao;
    private CCNUtil ccnUtil;

    public Object startInterfaceCostService() throws Exception {
        try
        {
            // 1. TC Session 생성
            if (tcSession == null)
            {
                TcLoginService tcLoginService = new TcLoginService();

                tcSession = tcLoginService.getTcSession();
            }

            tcItemUtil = new TcItemUtil(tcSession);
            dbDao = TcCommonDao.getTcCommonDao();
            ccnUtil = new CCNUtil(tcSession);

            /**
             * [20160913][ymjang] Project Code 를 Item Revision 으로 부터 가져올 경우, OSpec 이 없는 Project 가 존재함. --> I/F 테이블로 부터 가져오도록 개선
             * [20160913][ymjang] PreVehicle Part 이외의 Part 의 경우, 업체 속성이 존재하지 않음 --> Vehicle/Std Part 는 대상에서 제외함.
             */
            // DB Table에서 Interface 내용을 읽어온다.
            List<HashMap<String, String>> retList = getTargetItemList();
            HashMap<String, ArrayList<ItemRevision>> sysGrouppingRevision = new HashMap<String, ArrayList<ItemRevision>>();
            for (HashMap<String, String> ret : retList)
            {
                String projCode = ret.get("PJT_CD");
                String partNo = ret.get("PART_NO");
                
                // Item Revision을 찾는다.
                ItemRevision targetRev = tcItemUtil.getLatestRevItem(partNo);
                if (targetRev == null)
                {
                    HashMap<String, String> updateMap = new HashMap<>();
                    updateMap.put("ITEM_ID", partNo);
                    updateMap.put("UPDATE_FLAG", "E");
                    updateMap.put("UPDATE_MSG", "Can not find in Teamcenter");

                    dbDao.update("com.symc.costvendor.updateVendorInterfaceTable", updateMap);
                    
                    continue;
                }
                
                if (!targetRev.get_object_type().equals("S7_PreVehPartRevision")) {
                    HashMap<String, String> updateMap = new HashMap<>();
                    updateMap.put("ITEM_ID", partNo);
                    updateMap.put("UPDATE_FLAG", "E");
                    updateMap.put("UPDATE_MSG", "Not Target");

                    dbDao.update("com.symc.costvendor.updateVendorInterfaceTable", updateMap);
                    
                    continue;
                }
                
                tcItemUtil.getProperties(new ModelObject[]{targetRev}, new String[]{TcConstants.PROP_ITEM_ID, TcConstants.PROP_S7_PROJCODE, TcConstants.PROP_RELEASE_STATUS_LIST, TcConstants.PROP_OWNING_USER, TcConstants.PROP_OWNING_GROUP});
                
                //String sysCode = targetRev.getPropertyObject(TcConstants.PROP_S7_BUDGETCODE).getStringValue();
                String sysCode = "";
                String mapKey = projCode + "&" + sysCode;

                if (sysGrouppingRevision.containsKey(mapKey))
                {
                    if (! sysGrouppingRevision.get(mapKey).contains(targetRev))
                    {
                        sysGrouppingRevision.get(mapKey).add(targetRev);
                    }
                }
                else
                {
                    ArrayList<ItemRevision> revList = new ArrayList<>();

                    revList.add(targetRev);
                    sysGrouppingRevision.put(mapKey, revList);
                }
            }
            
            /*
            ArrayList<ItemRevision> targetRevList = new ArrayList<>();
            for (HashMap<String, String> ret : retList)
            {
                String partNo = ret.get("PART_NO");

                // Item Revision을 찾는다.
                ItemRevision targetRev = tcItemUtil.getLatestRevItem(partNo);
                if (targetRev == null)
                {
                    HashMap<String, String> updateMap = new HashMap<>();
                    updateMap.put("ITEM_ID", partNo);
                    updateMap.put("UPDATE_FLAG", "E");
                    updateMap.put("UPDATE_MSG", "Can not find in Teamcenter");

                    dbDao.update("com.symc.costvendor.updateCostInterfaceTable", updateMap);
                }
                if (! targetRevList.contains(targetRev))
                    targetRevList.add(targetRev);
            }

            HashMap<String, ArrayList<ItemRevision>> sysGrouppingRevision = new HashMap<>();
            tcItemUtil.getProperties(targetRevList.toArray(new ModelObject[0]), new String[]{TcConstants.PROP_ITEM_ID, TcConstants.PROP_S7_PROJCODE, TcConstants.PROP_S7_BUDGETCODE, TcConstants.PROP_RELEASE_STATUS_LIST, TcConstants.PROP_OWNING_USER, TcConstants.PROP_OWNING_GROUP, TcConstants.PROP_S7_CCN_NO});
            for (ItemRevision rev : targetRevList)
            {
                String sysCode = rev.getPropertyObject(TcConstants.PROP_S7_BUDGETCODE).getStringValue();
                String projCode = rev.getPropertyObject(TcConstants.PROP_S7_PROJCODE).getStringValue();
                String mapKey = projCode + "&" + sysCode;

                if (sysGrouppingRevision.containsKey(mapKey))
                {
                    if (! sysGrouppingRevision.get(mapKey).contains(rev))
                    {
                        sysGrouppingRevision.get(mapKey).add(rev);
                    }
                }
                else
                {
                    ArrayList<ItemRevision> revList = new ArrayList<>();

                    revList.add(rev);
                    sysGrouppingRevision.put(mapKey, revList);
                }
            }
			*/
            
            HashMap<String, HashMap<String, Object>> ccnAttachedItemList = new HashMap<>();
            ArrayList<ItemRevision> ccnList = new ArrayList<>();
            for (String mapKey : sysGrouppingRevision.keySet())
            {
                // CCN을 생성한다.
                String []codes = mapKey.split("&");
                String projCode = codes[0];
                String sysCode= codes[1];

                boolean isRevised = false;
                for (ItemRevision rev : sysGrouppingRevision.get(mapKey))
                {
                    if (! (rev.get_release_status_list() == null || rev.get_release_status_list().length == 0))
                    {
                        isRevised = true;
                        break;
                    }
                }

                ItemRevision ccnRev = null;
                String ccnId = "";
                if (isRevised)
                {
                    ccnRev = createCCNRevision(projCode, sysCode);
                    tcItemUtil.getProperties(new ModelObject[]{ccnRev}, new String[]{TcConstants.PROP_ITEM_ID});
                    ccnId = ccnRev.get_item_id();
                }

                HashMap<String, HashMap<String, Object>> revCostMap = getAllRevisionCosts(sysGrouppingRevision.get(mapKey));
                ArrayList<ItemRevision> probList = new ArrayList<>();
                ArrayList<ItemRevision> solList = new ArrayList<>();

                try
                {
                    for (ItemRevision rev : sysGrouppingRevision.get(mapKey))
                    {
                        HashMap<String, Object> revInfoMap = new HashMap<>();
                        String itemId = rev.get_item_id();
                        boolean isRevRevised = false;

                        ItemRevision newRev = null;
                        if (! (rev.get_release_status_list() == null || rev.get_release_status_list().length == 0))
                        {
                            probList.add(rev);

                            revInfoMap.put(TcConstants.PROP_ITEM_ID, itemId);
                            revInfoMap.put("OLD_REV_ID", rev.get_item_revision_id());

                            // Revision을 개정한다.
                            newRev = tcItemUtil.revise(rev.getUid(), tcSession);
                            isRevRevised = true;
                            
                            if (! solList.contains(newRev))
                                solList.add(newRev);
                        }else{
                            ItemRevision lastReleaseRev = tcItemUtil.getLastReleaseRevItem(itemId);
                            tcItemUtil.getProperties(new ModelObject[]{lastReleaseRev}, new String[]{TcConstants.PROP_ITEM_ID, TcConstants.PROP_S7_PROJCODE, TcConstants.PROP_RELEASE_STATUS_LIST, TcConstants.PROP_OWNING_USER, TcConstants.PROP_OWNING_GROUP});
                            revInfoMap.put("OLD_REV_ID", lastReleaseRev.get_item_revision_id());
                        }

                        if (newRev == null)
                        {
                            newRev = rev;
                            revInfoMap.put(TcConstants.PROP_ITEM_ID, itemId);
                        }
                        else
                        {
                            revInfoMap.put("NEW_REV_ID", newRev.get_item_revision_id());
                        }

                        ModelObject newComp = null;
                        if (isRevRevised)
                        {
                            newComp = tcItemUtil.createApplicationObject(TcConstants.S7_PREVEHTYPEDREFERENCE, new String[]{TcConstants.PROP_S7_EST_COST_MATERIAL, TcConstants.PROP_S7_TARGET_COST_MATERIAL}, new Object[]{revCostMap.get(itemId).get("EST_COST"), revCostMap.get(itemId).get("TARGET_COST")});
                        // 단가 정보를 수정한다.
//                        updateProperties.put(TcConstants.PROP_S7_EST_COST_MATERIAL, revCostMap.get(itemId).get("EST_COST"));
//                        updateProperties.put(TcConstants.PROP_S7_TARGET_COST_MATERIAL, revCostMap.get(itemId).get("TARGET_COST"));
//                            tcItemUtil.changeOwnerShip(newComp.getUid(), rev.get_owning_user().getUid(), (Group) rev.get_owning_group());
                        }
                        else
                        {
                            ModelObject relObjs = tcItemUtil.getRelatedFromModelObjectValue(rev.getUid(), TcConstants.PROP_PRE_VEH_TYPE_REF);
                            if (relObjs == null)
                            {
//                                HashMap<String, String> updateMap = new HashMap<>();
//                                updateMap.put("ITEM_ID", partNo);
//                                updateMap.put("UPDATE_FLAG", "E");
//                                updateMap.put("UPDATE_MSG", "");
//
//                                dbDao.update("com.symc.costvendor.updateCostInterfaceTable", updateMap);
                            }
                            else
                            {
                                HashMap<String, Object> propMap = new HashMap<>();
                                propMap.put(TcConstants.PROP_S7_EST_COST_MATERIAL, revCostMap.get(itemId).get("EST_COST"));
                                propMap.put(TcConstants.PROP_S7_TARGET_COST_MATERIAL, revCostMap.get(itemId).get("TARGET_COST"));
                                tcItemUtil.setAttributes(relObjs, propMap);
                            }
                        }

                        if (isRevRevised)
                        {
                            HashMap<String, Object> updateProperties = new HashMap<>();
                            updateProperties.put(TcConstants.PROP_S7_CCN_NO, ccnRev.getUid());
                            updateProperties.put(TcConstants.PROP_PRE_VEH_TYPE_REF, newComp.getUid());

                            tcItemUtil.setAttributes(newRev, updateProperties);
                            
                            tcItemUtil.changeOwnerShip(newRev.getUid(), rev.get_owning_user().getUid(), (Group) rev.get_owning_group());
                            
                            if (! solList.contains(newRev))
                                solList.add(newRev);
                        }

                        revInfoMap.put(TcConstants.PROP_S7_EST_COST_MATERIAL, revCostMap.get(itemId).get("EST_COST"));
                        revInfoMap.put(TcConstants.PROP_S7_TARGET_COST_MATERIAL, revCostMap.get(itemId).get("TARGET_COST"));
                        if (ccnRev != null)
                            revInfoMap.put("CCN_NO", ccnId);

                        ccnAttachedItemList.put(itemId, revInfoMap);;
                    }
                }
                catch (Exception ex)
                {
                    if (ccnRev != null)
                    {
                        tcItemUtil.deleteItems(new Item[]{ccnRev.get_items_tag()});
                    }
                    if (solList.size() > 0)
                    {
                        tcItemUtil.deleteRevs(solList.toArray(new ItemRevision[0]));
                    }

                    throw ex;
                }

                if (ccnRev != null)
                {
                    if (probList.size() > 0)
                        tcItemUtil.insertRelated(ccnRev, probList.toArray(new ItemRevision[0]), TcConstants.CMHAS_PROBLEM_ITEM);
                    if (solList.size() > 0)
                        tcItemUtil.insertRelated(ccnRev, solList.toArray(new ItemRevision[0]), TcConstants.CMHAS_SOLUTION_ITEM);

                    ccnList.add(ccnRev);
                }
            }

            // CCN을 결재처리한다.
            // Master 정보를 발췌하여 CCN_PREBOM_MASTER_LIST 테이블에 Insert 한다.
            // Usage 정보는 CCN_PREBOM_USAGE 에서 가장 최신의 정보를 복사해서 CCN_PREBOM_USAGE 테이블에 Insert 한다.
            if (null != ccnList && ccnList.size() > 0) {
                tcItemUtil.createNewProcess(ccnList.toArray(new ModelObject[0]), "Self Release for Update Cost", "CSR");
                ccnUtil.insertCCNInformation(ccnAttachedItemList);
                ccnUtil.insertCCNMaster(ccnList, "cost");
                // [20160913][ymjang] CCN IF 생성 기능 프로시저로 이관함.
                ccnUtil.insertIfCCN(ccnList, "vendor");
            }

            // mark to Interface Table Success
            updateInterfaceTable("com.symc.costvendor.updateCostInterfaceTable", ccnAttachedItemList);

            return "Success";
        }
        catch (Exception ex)
        {
            throw ex;
        } finally {
        	if (tcSession != null)
        		tcSession.logout();
        }
    }

    private void updateInterfaceTable(String sql, HashMap<String, HashMap<String, Object>> ccnAttachedItemList) throws Exception
    {
        try
        {
            ArrayList<HashMap<String, String>> updateList = new ArrayList<>();

            for (String key : ccnAttachedItemList.keySet())
            {
                HashMap<String, String> updateMap = new HashMap<>();

                updateMap.put("ITEM_ID", key);
                updateMap.put("UPDATE_FLAG", "C");
                updateMap.put("UPDATE_MSG", "");

                updateList.add(updateMap);
            }

            if (updateList.size() > 0)
                dbDao.insertList(sql, updateList);
        }
        catch (Exception e)
        {
            throw e;
        }
    }

    private HashMap<String, HashMap<String, Object>> getAllRevisionCosts(ArrayList<ItemRevision> revList) throws Exception
    {
        HashMap<String, HashMap<String, Object>> revProperties = new HashMap<>();

        try
        {
            for (ItemRevision rev : revList)
            {
                String itemID = rev.get_item_id();

                HashMap<String, String> inputMap = new HashMap<>();
                inputMap.put("PART_NO", itemID);

                @SuppressWarnings("unchecked")
                List<HashMap<String, Object>> retList = (List<HashMap<String, Object>>) dbDao.selectList("com.symc.costvendor.getDatasForUpdateCost", inputMap);
                for (HashMap<String, Object> ret : retList)
                {
                    revProperties.put(itemID, ret);
                }
            }

            return revProperties;
        }
        catch (Exception ex)
        {
            throw ex;
        }
    }

    @SuppressWarnings("static-access")
    private ItemRevision createCCNRevision(String projCode, String sysCode) throws Exception
    {
        try
        {
            HashMap<String, Object> ccnPropMap = new HashMap<String, Object>();
            ccnPropMap.put(TcConstants.PROP_S7_PROJCODE, projCode);
            ccnPropMap.put(TcConstants.PROP_S7_SYSTEM_CODE, sysCode);
            ccnPropMap.put(TcConstants.PROP_S7_PROJECTTYPE, "02");
            ccnPropMap.put(TcConstants.PROP_S7_REGULATION, false);
            ccnPropMap.put(TcConstants.PROP_S7_COSTDOWN, false);
            ccnPropMap.put(TcConstants.PROP_S7_ORDERINGSPEC, false);
            ccnPropMap.put(TcConstants.PROP_S7_QUALITYIMPROVEMENT, false);
            ccnPropMap.put(TcConstants.PROP_S7_CORRECTIONOFEPL, false);
            ccnPropMap.put(TcConstants.PROP_S7_STYLINGUPDATE, false);
            ccnPropMap.put(TcConstants.PROP_S7_WEIGHTCHANGE, false);
            ccnPropMap.put(TcConstants.PROP_S7_MATERIALCOSTCHANGE, true);
            ccnPropMap.put(TcConstants.PROP_S7_THEOTHERS, false);
            ccnPropMap.put(TcConstants.PROP_OBJECT_DESC, "modify Cost");
            String[] preProductInfo = ccnUtil.getOspecId(projCode);
            ccnPropMap.put(TcConstants.PROP_S7_OSPEC_NO, preProductInfo[0]); 
            ccnPropMap.put(TcConstants.PROP_S7_GATENO, preProductInfo[1]);
            
            String yyMM = MECOReportService.getToday("yy");
            String ccnID = projCode + sysCode.substring(0, 1) + yyMM + "-";

            HashMap<String, Object> newIdMap = new HashMap<>();
            newIdMap.put("PRE_FIX", ccnID);
            
            // [NoSR][2016.03.02][jclee] Engine Product CCN No 채번 시 12자리로 자리수가 결정될 경우 Seq No가 2자리 밖에 생성되지 않는 현상 발생 (Engine Project Code가 6자리로 총 Length가 14자리가 되어야함)
            // 해결방법 1. CCN No Prefix Length + 4로 뒤 Seq No의 자리수를 4로 확정지어줌
            // 해결방법 2. Project Code가 Engine Project Code일 경우 총 자리수를 14자리로 확정지어줌
            //  : 이 중 해결방법 1을 적용. -> Engine Project Code인지 아닌지를 확인하는 로직이 추가되면 속도에도 문제가 있을뿐 아니라
            //    13 혹은 14자리 이상의 CCN No가 나와야할 경우 또 로직을 변경해야 하므로 Seq No를 4자리로 확정지어주는 방식 채택.
//            newIdMap.put("TO_LEN", 12);
            newIdMap.put("TO_LEN", ccnID.length() + 4);
            
            /**
             * [20160913][ymjang] CCN ID 채번 오류 (동일한 ID를 가져옴) 개선
             */
            TcCommonDao.getTcCommonDao().update("com.symc.costvendor.updNewId", newIdMap);
            String newCCNID = (String) TcCommonDao.getTcCommonDao().selectOne("com.symc.costvendor.getNewIdWithTable", newIdMap);
            System.out.println("newCCNID : " + newCCNID);
            //Object newCCNID = TcCommonDao.getTcCommonDao().selectOne("com.symc.costvendor.getNewId", newIdMap);
            Item ccnItem = tcItemUtil.getItem("S7_PreCCN", newCCNID.toString());
            if (ccnItem != null)
            {
                throw new Exception("Maximum CCN ID count was over. contact to Admin.");
            }

            ItemProperties itemProperty = new ItemProperties();
            itemProperty.clientId = "1";
            itemProperty.itemId = newCCNID.toString();
            itemProperty.revId = "000";
            itemProperty.name = newCCNID.toString();
            itemProperty.type = "S7_PreCCN";
            itemProperty.description = "";

            tcItemUtil.createItems(new ItemProperties[]{itemProperty});

            ItemRevision ccnRevision = tcItemUtil.getLatestRevItem(newCCNID.toString());
            tcItemUtil.setAttributes(ccnRevision, ccnPropMap);

            return ccnRevision;
        }
        catch (Exception ex)
        {
            throw ex;
        }
    }

    @SuppressWarnings("unchecked")
    private List<HashMap<String, String>> getTargetItemList() throws Exception
    {
        return (List<HashMap<String, String>>) dbDao.selectList("com.symc.costvendor.getIdListForUpdateCost");
    }

    public void setSession(Session session) throws Exception {
        this.tcSession = session;
    }

}

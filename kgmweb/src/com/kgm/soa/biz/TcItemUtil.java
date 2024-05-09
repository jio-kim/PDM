package com.kgm.soa.biz;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import com.kgm.common.util.StringUtil;
import com.kgm.soa.service.TcServiceManager;
import com.kgm.soa.util.TcConstants;
import com.kgm.soa.util.TcUtil;
import com.teamcenter.schemas.soa._2006_03.exceptions.ServiceException;
import com.teamcenter.services.strong.core.DataManagementService;
import com.teamcenter.services.strong.core._2006_03.DataManagement.CreateItemsOutput;
import com.teamcenter.services.strong.core._2006_03.DataManagement.CreateItemsResponse;
import com.teamcenter.services.strong.core._2006_03.DataManagement.ItemProperties;
import com.teamcenter.services.strong.core._2006_03.DataManagement.ObjectOwner;
import com.teamcenter.services.strong.core._2007_01.DataManagement.GetItemFromIdPref;
import com.teamcenter.services.strong.core._2007_01.DataManagement.RelationFilter;
import com.teamcenter.services.strong.core._2007_01.DataManagement.VecStruct;
import com.teamcenter.services.strong.core._2008_06.DataManagement.BVROutput;
import com.teamcenter.services.strong.core._2008_06.DataManagement.DatasetInfo;
import com.teamcenter.services.strong.core._2008_06.DataManagement.DatasetOutput;
import com.teamcenter.services.strong.core._2008_06.DataManagement.GetItemAndRelatedObjectsInfo;
import com.teamcenter.services.strong.core._2008_06.DataManagement.GetItemAndRelatedObjectsItemOutput;
import com.teamcenter.services.strong.core._2008_06.DataManagement.GetItemAndRelatedObjectsResponse;
import com.teamcenter.services.strong.core._2008_06.DataManagement.ItemInfo;
import com.teamcenter.services.strong.core._2008_06.DataManagement.NamedReferenceList;
import com.teamcenter.services.strong.core._2008_06.DataManagement.RevInfo;
import com.teamcenter.services.strong.core._2008_06.DataManagement.ReviseInfo;
import com.teamcenter.services.strong.core._2008_06.DataManagement.ReviseResponse2;
import com.teamcenter.services.strong.core._2008_06.DataManagement.RevisionOutput;
import com.teamcenter.services.strong.core._2009_10.DataManagement.GetItemFromAttributeInfo;
import com.teamcenter.services.strong.core._2009_10.DataManagement.GetItemFromAttributeResponse;
import com.teamcenter.services.strong.query._2006_03.SavedQuery.GetSavedQueriesResponse;
import com.teamcenter.services.strong.query._2007_09.SavedQuery.QueryResults;
import com.teamcenter.services.strong.query._2007_09.SavedQuery.SavedQueriesResponse;
import com.teamcenter.services.strong.query._2008_06.SavedQuery.QueryInput;
import com.teamcenter.soa.client.model.ModelObject;
import com.teamcenter.soa.client.model.ServiceData;
import com.teamcenter.soa.client.model.strong.Dataset;
import com.teamcenter.soa.client.model.strong.Group;
import com.teamcenter.soa.client.model.strong.ImanQuery;
import com.teamcenter.soa.client.model.strong.Item;
import com.teamcenter.soa.client.model.strong.ItemRevision;
import com.teamcenter.soa.client.model.strong.PSBOMViewRevision;
import com.teamcenter.soa.client.model.strong.User;
import com.teamcenter.soa.client.model.strong.WorkspaceObject;
import com.teamcenter.soa.exceptions.NotLoadedException;

public class TcItemUtil {

    private Session tcSession = null;
    private TcServiceManager tcServiceManager;

    public TcItemUtil(Session tcSession) {

        this.tcSession = tcSession;
        tcServiceManager = new TcServiceManager(tcSession);

    }

    public RevisionOutput[] getRevisionOutputFromItem(String itemId, String revision_id) throws Exception {

        RevisionOutput[] revisionoutputs = null;
        GetItemFromAttributeResponse gifares = getItemFromID(itemId);
        Item item = gifares.output[0].item;
        GetItemAndRelatedObjectsInfo relatedInfo = new GetItemAndRelatedObjectsInfo();

        String[] namedRefTemplates = TcUtil.getNamedReferenceTemplateList();
        NamedReferenceList[] namedRef = new NamedReferenceList[namedRefTemplates.length];
        int cnt = 0;
        for (String namedRefTemplate : namedRefTemplates) {

            namedRef[cnt] = new NamedReferenceList();
            namedRef[cnt].namedReference = namedRefTemplate;
            namedRef[cnt].ticket = true;
            cnt++;
        }

        relatedInfo.bvrTypeNames = new String[] { TcConstants.TYPE_VIEW_DEFAULT };
        relatedInfo.clientId = "ItemAndRelatedObjectsInfo";
        relatedInfo.datasetInfo = new DatasetInfo();
        relatedInfo.itemInfo = new ItemInfo();
        relatedInfo.revInfo = new RevInfo();
        relatedInfo.itemInfo.uid = item.getUid();

        relatedInfo.datasetInfo.clientId = "datasetInfo";
        relatedInfo.datasetInfo.filter.processing = "All";
        relatedInfo.datasetInfo.namedRefs = namedRef;

        relatedInfo.revInfo.clientId = "revInfo";
        relatedInfo.revInfo.id = revision_id;
        relatedInfo.revInfo.useIdFirst = false;
        relatedInfo.revInfo.revisionRule = "Imprecise";

        if ("".equals(revision_id) || null == revision_id) {
            relatedInfo.revInfo.processing = "All"; // "Nrev";
        } else {
            relatedInfo.revInfo.processing = "Ids"; // "Nrev, Min, All,Ids";
            relatedInfo.revInfo.nRevs = 1;
        }

        GetItemAndRelatedObjectsResponse resp = tcServiceManager.getDataService().getItemAndRelatedObjects(new GetItemAndRelatedObjectsInfo[] { relatedInfo });

        if (!tcServiceManager.getDataService().ServiceDataError(resp.serviceData)) {

            for (GetItemAndRelatedObjectsItemOutput itemsOut : resp.output) {
                revisionoutputs = new RevisionOutput[itemsOut.itemRevOutput.length];
                int i = 0;
                for (RevisionOutput rev : itemsOut.itemRevOutput) {
                    revisionoutputs[i] = rev;
                    System.out.println(i);
                    i++;
                }
            }
        } else {
            throw new Exception(TcUtil.makeMessageOfFail(resp.serviceData).get(TcConstants.TC_RETURN_FAIL_REASON).toString());
        }
        return revisionoutputs;

    }

    /**
     * Desc : retrieve ItemAttribute via itemid
     * 
     * @Method Name : getItemFromID
     * @param String
     *            itemId
     * @return GetItemFromAttributeResponse
     * @throws Exception
     * @Comment
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public GetItemFromAttributeResponse getItemFromID(String itemId) throws Exception {
        GetItemFromAttributeResponse gifiRes = null;

        Map itemAttributes = new HashMap<String, Object>();
        itemAttributes.put(TcConstants.PROP_ITEM_ID, itemId);

        GetItemFromAttributeInfo agetitemfromattributeinfo = new GetItemFromAttributeInfo();
        agetitemfromattributeinfo.itemAttributes = itemAttributes;

        RelationFilter rf = new RelationFilter();
        GetItemFromIdPref getitemfromidpref = new GetItemFromIdPref();
        getitemfromidpref.prefs = new RelationFilter[] { rf };

        gifiRes = tcServiceManager.getDataService().getItemFromAttribute(new GetItemFromAttributeInfo[] { agetitemfromattributeinfo }, 1, getitemfromidpref);

        return gifiRes;
    }

    public Item getItem(String itemId) throws Exception {
        GetItemFromAttributeResponse itemRep = getItemFromID(itemId);
        if (!tcServiceManager.getDataService().ServiceDataError(itemRep.serviceData)) {
            if (itemRep.output != null && itemRep.output.length > 0) {
                ServiceData serviceData = getProperties(new ModelObject[] {itemRep.output[0].item},  new String[]{TcConstants.PROP_ITEM_ID, "revision_list"});
                if(!tcServiceManager.getDataService().ServiceDataError(serviceData)){             
                    return itemRep.output[0].item;
                }else{
                    throw new Exception(TcUtil.makeMessageOfFail(serviceData).get(TcConstants.TC_RETURN_FAIL_REASON).toString());            
                }                
            } else {
                return null;
            }
        } else {
            throw new Exception(TcUtil.makeMessageOfFail(itemRep.serviceData).get(TcConstants.TC_RETURN_FAIL_REASON).toString());
        }

    }

    /**
     * Item 생성
     * 
     * @method createItems
     * @date 2013. 4. 5.
     * @param aitemproperties
     *            [0].itemId = "test-000001"; aitemproperties[0].revId = "000";
     *            aitemproperties[0].type = "S7_Product";
     *            aitemproperties[0].name = "test1";
     * @return void
     * @exception
     * @throws
     * @see
     */
    public Item[] createItems(ItemProperties aitemproperties[]) throws Exception {
        Item[] crateItems = null;
        if (aitemproperties == null) {
            return null;
        }
        CreateItemsResponse resp = tcServiceManager.getDataService().createItems(aitemproperties, null, "");
        if (!tcServiceManager.getDataService().ServiceDataError(resp.serviceData)) {
            crateItems = new Item[resp.output.length];
            for (int idx = 0; idx < resp.output.length; idx++) {
                crateItems[idx] = resp.output[idx].item;
                // System.out.println(resp.output[idx].item);
                // System.out.println(resp.output[idx].itemRev);

            }
        } else {
            throw new Exception(TcUtil.makeMessageOfFail(resp.serviceData).get(TcConstants.TC_RETURN_FAIL_REASON).toString());
        }
        return crateItems;
    }

    /**
     * Create Items
     * 
     * @param itemIds
     *            Array of Item and Revision IDs
     * @param itemType
     *            Type of item to create
     * 
     * @return Set of Items and ItemRevisions
     * @throws Exception
     */
    public CreateItemsOutput[] createItems(String id, String name, String revNo, String unit, String itemType, String sParentObjUid, HashMap<String, String> revMasterProp, Session tcSession) throws Exception {
        // Get the service stub
        ItemProperties itemProperty = new ItemProperties();
        itemProperty.clientId = "1";
        itemProperty.itemId = id;
        itemProperty.revId = revNo;
        itemProperty.name = name;
        itemProperty.type = itemType;
        itemProperty.description = name;
        itemProperty.uom = unit;

        // String sRevMasterName = "";
        /*
         * if( itemType.equals("Item") ) { sRevMasterName =
         * "ItemRevision Master"; } else if( itemType.equals("Drawing") ) {
         * sRevMasterName = "DrawingVerMaster"; } else if(
         * itemType.equals("G2_LGItem") ) { sRevMasterName =
         * "G2_LGItemRevision"; }
         */
        /*
         * if (itemType.equals("Item")) { sRevMasterName =
         * "ItemRevision Master"; } else if
         * (itemType.equals(SYMCClass.S7_PRODUCTPARTTYPE)) { sRevMasterName =
         * SYMCClass.S7_PRODUCTPARTREVISIONTYPE; } else if
         * (itemType.equals(SYMCClass.S7_FNCPARTTYPE)) { sRevMasterName = ""; }
         * else if (itemType.equals(SYMCClass.S7_FNCMASTPARTTYPE)) {
         * sRevMasterName = ""; } else if
         * (itemType.equals(SYMCClass.S7_VARIANTPARTTYPE)) { sRevMasterName =
         * ""; } else if (itemType.equals(SYMCClass.S7_VEHPARTTYPE)) {
         * sRevMasterName = ""; } else if
         * (itemType.equals(SYMCClass.S7_MATPARTTYPE)) { sRevMasterName = ""; }
         * else if (itemType.equals(SYMCClass.S7_STDPARTTYPE)) { sRevMasterName
         * = SYMCClass.S7_STDPARTREVISIONTYPE; } else if
         * (itemType.equals(SYMCClass.S7_SOFTPARTTYPE)) { sRevMasterName = ""; }
         * else if (itemType.equals(SYMCClass.S7_TECHDOCTYPE)) { sRevMasterName
         * = ""; } ExtendedAttributes extendedattributes1 = new
         * ExtendedAttributes(); extendedattributes1 = new ExtendedAttributes();
         * extendedattributes1.objectType = sRevMasterName;
         * extendedattributes1.attributes = revMasterProp;
         * itemProperty.extendedAttributes = new ExtendedAttributes[] {
         * extendedattributes1 };
         */

        // 붙여야할 parent object
        ModelObject parentComp = null;
        if (sParentObjUid != null && sParentObjUid.length() > 0) {
            parentComp = getModelObjectFromUid(sParentObjUid, tcSession);

        }
        // *****************************
        // Execute the service operation
        // *****************************
        CreateItemsResponse response = tcServiceManager.getDataService().createItems(new ItemProperties[] { itemProperty }, parentComp, "");
        if (!tcServiceManager.getDataService().ServiceDataError(response.serviceData)) {
            return response.output;
        } else {
            throw new Exception(TcUtil.makeMessageOfFail(response.serviceData).get(TcConstants.TC_RETURN_FAIL_REASON).toString());
        }
    }

    /**
     * uid to ModelObject
     */
    @SuppressWarnings("static-access")
    public static ModelObject getModelObjectFromUid(String sUid, Session tcSession) throws ServiceException {
        if (sUid == null || sUid.equals("") || sUid.equals("AAAAAAAAAAAAAA")) {
            return null;
        }
        ModelObject modelObj = null;
        DataManagementService dmService = DataManagementService.getService(tcSession.getConnection());
        ServiceData data = dmService.loadObjects(new String[] { sUid });
        if (data == null) {
            System.out.println("error!!!!");
        } else {
            modelObj = (ModelObject) data.getPlainObject(0);
            System.out.println("obj uid : " + modelObj.getUid());
            // revision master or item master인 경우, 정의된 속성 정보를 가져올 수 있도록 loading
            // 함.
            dmService.getProperties(new ModelObject[] { modelObj }, new String[] { "" });
        }

        return modelObj;
    }

    /**
     * 
     * Desc : retrieve ItemRevision by uids
     * 
     * @Method Name : getItemRevisionObjects
     * @param String
     *            [] uid
     * @return ItemRevision[]
     * @throws Exception
     * @Comment
     */
    public ItemRevision[] getItemRevisionObjects(String[] uid) throws Exception {

        ModelObject[] itemrevisionModels = tcServiceManager.getDataService().loadModelObjects(uid);
        int cnt = itemrevisionModels.length;
        ItemRevision[] itemRevision = new ItemRevision[cnt];

        int loop = 0;
        for (ModelObject itemrevisionModel : itemrevisionModels) {

            itemRevision[loop] = (ItemRevision) itemrevisionModel;
            loop++;
        }
        return itemRevision;
    }

    /**
     * Object에 속성값등록
     * 
     * @method setAttributes
     * @date 2013. 4. 8.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    public void setAttributes(ModelObject modelObject, Map<String, Object> mapFormProp) throws Exception {
        // ModelObject[] itemrevisionModels =
        // tcServiceManager.getDataService().loadModelObjects(new String[]
        // {revPuid});
        // 입력되어야 될 form 속성값을, 처리되어야 되는 형식으로 변환 함.
        Object oKey[] = mapFormProp.keySet().toArray();
        Object oValue[] = mapFormProp.values().toArray();
        VecStruct vsValue = null;
        HashMap<String, Object> hashmap = new HashMap<String, Object>();
        for (int i = 0; i < mapFormProp.size(); i++) {
            vsValue = new VecStruct();
            if (oValue[i] != null) {
                vsValue.stringVec = new String[] { oValue[i].toString() };
                hashmap.put(oKey[i].toString(), vsValue);
            }
        }
        ServiceData serviceData = tcServiceManager.getDataService().setProperties(new ModelObject[] { modelObject }, hashmap);
        if (tcServiceManager.getDataService().ServiceDataError(serviceData)) {
            throw new Exception(TcUtil.makeMessageOfFail(serviceData).get(TcConstants.TC_RETURN_FAIL_REASON).toString());
        }
    }

    /**
     * Revision 가져오기
     * 
     * @method getRevisionInfo 
     * @date 2013. 5. 7.
     * @param
     * @return ItemRevision
     * @exception
     * @throws
     * @see
     */
    public ItemRevision getRevisionInfo(String rev_ouid) throws Exception {
        ItemRevision itemRevision = (ItemRevision) tcServiceManager.getDataService().loadModelObject(rev_ouid);        
        // Property를 설정한다.(비지니스 properties 설정)
        ServiceData serviceData = getProperties(new ModelObject[] { itemRevision }, new String[] { TcConstants.PROP_ITEM_ID, TcConstants.PROP_ITEM_REVISION_ID, "s7_MATURITY" });        
        if (tcServiceManager.getDataService().ServiceDataError(serviceData)) {
            throw new Exception(TcUtil.makeMessageOfFail(serviceData).get(TcConstants.TC_RETURN_FAIL_REASON).toString());
        } else {
            return itemRevision;
        }
    }

    /**
     * 
     * Desc : get RevisionOutput via item
     * 
     * @Method Name : getRevisionOutputFromItem
     * @param String
     *            itemId
     * @param String
     *            revision_id
     * @param String
     *            revision_uid
     * @return RevisionOutput[]
     * @throws Exception
     * @Comment
     */
    public RevisionOutput[] getRevisionOutputFromItem(String itemId, String revision_id, String revision_uid) throws Exception {
        RevisionOutput[] revisionoutputs = null;
        GetItemFromAttributeResponse gifares = getItemFromID(itemId);
        Item item = gifares.output[0].item;
        GetItemAndRelatedObjectsInfo relatedInfo = new GetItemAndRelatedObjectsInfo();
        String[] namedRefTemplates = TcUtil.getNamedReferenceTemplateList();
        NamedReferenceList[] namedRef = new NamedReferenceList[namedRefTemplates.length];
        int cnt = 0;
        for (String namedRefTemplate : namedRefTemplates) {

            namedRef[cnt] = new NamedReferenceList();
            namedRef[cnt].namedReference = namedRefTemplate;
            namedRef[cnt].ticket = true;
            cnt++;
        }
        relatedInfo.bvrTypeNames = new String[] { TcConstants.TYPE_VIEW_DEFAULT, TcConstants.TYPE_VIEW_TOTALBOM, TcConstants.TYPE_VIEW_EBOM };
        relatedInfo.clientId = "ItemAndRelatedObjectsInfo";
        relatedInfo.datasetInfo = new DatasetInfo();
        relatedInfo.itemInfo = new ItemInfo();
        relatedInfo.revInfo = new RevInfo();
        relatedInfo.itemInfo.uid = item.getUid();

        relatedInfo.datasetInfo.clientId = "datasetInfo";
        relatedInfo.datasetInfo.filter.processing = "All";
        relatedInfo.datasetInfo.namedRefs = namedRef;

        relatedInfo.revInfo.clientId = "revInfo";
        relatedInfo.revInfo.id = revision_id;
        relatedInfo.revInfo.useIdFirst = false;
        relatedInfo.revInfo.revisionRule = "Precise";
        if ("".equals(revision_id) || null == revision_id) {
            relatedInfo.revInfo.processing = "All"; // "Nrev";

        } else {
            relatedInfo.revInfo.processing = "Ids"; // "Nrev, Min, All,Ids";
            relatedInfo.revInfo.nRevs = 1;
        }

        if (!"".equals(revision_uid) || null != revision_uid) {
            relatedInfo.revInfo.uid = revision_uid;
        }

        GetItemAndRelatedObjectsResponse resp = tcServiceManager.getDataService().getItemAndRelatedObjects(new GetItemAndRelatedObjectsInfo[] { relatedInfo });

        if (!tcServiceManager.getDataService().ServiceDataError(resp.serviceData)) {

            for (GetItemAndRelatedObjectsItemOutput itemsOut : resp.output) {
                revisionoutputs = new RevisionOutput[itemsOut.itemRevOutput.length];
                int i = 0;
                for (RevisionOutput rev : itemsOut.itemRevOutput) {
                    revisionoutputs[i] = rev;
                    System.out.println();
                    i++;
                }
            }
        } else {
            throw new Exception(TcUtil.makeMessageOfFail(resp.serviceData).get(TcConstants.TC_RETURN_FAIL_REASON).toString());
        }
        return revisionoutputs;
    }

    protected BVROutput[] getBVROutputFromRevisionOutput(RevisionOutput revisionOutput) {

        BVROutput[] bvrInfos = new BVROutput[revisionOutput.bvrs.length];
        int i = 0;
        for (BVROutput bvrout : revisionOutput.bvrs) {
            bvrInfos[i] = bvrout;
            i++;
        }
        return bvrInfos;
    }

    public ServiceData getProperties(ModelObject[] modelObject, String[] reltypes) throws Exception {
        ServiceData serviceData = tcServiceManager.getDataService().getProperties(modelObject, reltypes);
        if (tcServiceManager.getDataService().ServiceDataError(serviceData)) {
            throw new Exception(TcUtil.makeMessageOfFail(serviceData).get(TcConstants.TC_RETURN_FAIL_REASON).toString());
        } else {
            return serviceData;
        }
    }

    /**
     * Delete the Items
     * 
     * @param items
     *            Array of Items to delete
     * 
     * @throws ServiceException
     *             If any partial errors are returned
     */
    @SuppressWarnings("static-access")
    public void deleteItems(Item[] items) throws ServiceException {
        // Get the service stub
        DataManagementService dmService = DataManagementService.getService(tcSession.getConnection());

        // *****************************
        // Execute the service operation
        // *****************************
        ServiceData serviceData = dmService.deleteObjects(items);

        // The AppXPartialErrorListener is logging the partial errors returned
        // In this simple example if any partial errors occur we will throw a
        // ServiceException
        if (serviceData.sizeOfPartialErrors() > 0)
            throw new ServiceException("DataManagementService.deleteObjects returned a partial error.");
    }
    
    /**
     * Delete the Items
     * 
     * @param items
     *            Array of Items to delete
     * 
     * @throws ServiceException
     *             If any partial errors are returned
     */
    @SuppressWarnings("static-access")
    public void deleteRevs(ItemRevision[] revs) throws ServiceException {
        // Get the service stub
        DataManagementService dmService = DataManagementService.getService(tcSession.getConnection());

        // *****************************
        // Execute the service operation
        // *****************************
        ServiceData serviceData = dmService.deleteObjects(revs);

        // The AppXPartialErrorListener is logging the partial errors returned
        // In this simple example if any partial errors occur we will throw a
        // ServiceException
        if (serviceData.sizeOfPartialErrors() > 0)
            throw new ServiceException("DataManagementService.deleteObjects returned a partial error.");
    }

    public ItemRevision revise(String revPuid, Session tcSession) throws Exception {
        ModelObject[] itemrevisionModels = tcServiceManager.getDataService().loadModelObjects(new String[] { revPuid });
        if (itemrevisionModels == null || itemrevisionModels.length == 0) {
            throw new Exception("Item revision does not exist.");
        }
        ItemRevision itemRev = (ItemRevision) itemrevisionModels[0];
        String revId = itemRev.getPropertyObject("item_revision_id").getStringValue();
        System.out.println("itemRev : " + itemRev.toString());
        ReviseInfo[] revInfo = new ReviseInfo[1];
        revInfo[0] = new ReviseInfo();
        revInfo[0].clientId = "";
        revInfo[0].baseItemRevision = itemRev;
        // revInfo[0].description = ".";
        revInfo[0].name = itemRev.get_object_name();
        revInfo[0].newRevId = getNextRevID(revId);

        /* deep copy 보다는 dataset saveas 동작이 바람직할 것 */
        // DeepCopyData deepcopydata[] = new DeepCopyData[2];
        // deepcopydata[0] = new DeepCopyData();
        // deepcopydata[0].action = 0;
        // deepcopydata[0].copyRelations = true;
        // deepcopydata[0].isRequired = true;
        // deepcopydata[0].isTargetPrimary = true;
        // deepcopydata[0].otherSideObjectTag =
        // getModelObjectFromUid(sOldDatasetUid,tcSession);
        // deepcopydata[0].newName = sObjName;
        // deepcopydata[0].relationTypeName = FileUtil.REL_SPECIFICATION;
        // revInfo[0].deepCopyInfo = deepcopydata;
        // deepcopydata[1] = new DeepCopyData();
        // deepcopydata[1].action = 0;
        // deepcopydata[1].copyRelations = true;
        // deepcopydata[1].isRequired = true;
        // deepcopydata[1].isTargetPrimary = true;
        // deepcopydata[1].otherSideObjectTag =
        // getModelObjectFromUid(sOldFormUid, tcSession);
        // deepcopydata[1].newName = sObjName;
        // deepcopydata[1].relationTypeName = FileUtil.REL_MASTER_FORM;
        // revInfo[0].deepCopyInfo = deepcopydata;

        /**
         * 기존에 사용하 하위 Dataset 복사용 Revise
         */
        // ReviseResponse2 response =
        // tcServiceManager.getDataService().revise2(revInfo);
        // if
        // (tcServiceManager.getDataService().ServiceDataError(response.serviceData))
        // {
        // throw new Exception("<REVISE ERROR> : " +
        // TcUtil.makeMessageOfFail(response.serviceData).get(TcConstants.TC_RETURN_FAIL_REASON).toString());
        // }
        ReviseResponse2 response = tcServiceManager.getInternalDataService().reviseObject(revInfo, false);
        if (tcServiceManager.getDataService().ServiceDataError(response.serviceData)) {
            throw new Exception("<REVISE ERROR> : " + TcUtil.makeMessageOfFail(response.serviceData).get(TcConstants.TC_RETURN_FAIL_REASON).toString());
        }
        for (int i = 0; i < response.serviceData.sizeOfCreatedObjects(); i++) {
            if (response.serviceData.getCreatedObject(i) instanceof ItemRevision) {
                return getRevisionInfo(response.serviceData.getCreatedObject(i).getUid());
            }
        }
        return null;
    }

    /**
     * Revision Revise (쌍용 자동차 VPM I/F 용)
     * 
     * @method revise
     * @date 2013. 4. 10.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    public ItemRevision reviseVPMIf(String revPuid, Session tcSession) throws Exception {
        return revise(revPuid, tcSession);
    }

    /**
     * 현재 Revision ID를 가지고 다음 Revision ID를 얻어온다.
     * 
     * @method getNextRevID
     * @date 2013. 4. 10.
     * @param
     * @return String
     * @exception
     * @throws
     * @see
     */
    public static String getNextRevID(String currentRevID) throws Exception {
        String nextRevId = "";
        try {
            if (currentRevID == null || "".equals(currentRevID)) {
                return "000";
            }
            int nextRev = Integer.parseInt(currentRevID) + 1;
            nextRevId = nextRev + "";
            if (nextRevId.length() == 1) {
                nextRevId = "00" + nextRevId;
            } else if (nextRevId.length() == 2) {
                nextRevId = "0" + nextRevId;
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw new Exception("<NEXT Rev ID> Revision error occurs : " + currentRevID);
        }
        return nextRevId;
    }

    /**
     * Vehicle Part/Function Master Revision ID 생성 Stage 값이 "P"가 아닌경우 해당
     * 
     * A -> Z -> AA -> ZZ
     */
    public static String getNextCustomRevID(String currentRevID) throws Exception {
        if (currentRevID == null || currentRevID.trim().equals("") || currentRevID.length() > 2) {
            throw new Exception("올바르지 않은 Revision ID 입니다.");
        }
        char[] szChar = currentRevID.toCharArray();
        for (int i = (szChar.length - 1); i >= 0; i--) {
            if (('A' > szChar[i]) || (szChar[i] > 'Z')) {
                throw new Exception("올바르지 않은 Revision ID 입니다.");
            }
        }
        if ("ZZ".equals(currentRevID))
            throw new Exception("더 이상 개정하실 수 없습니다.");
        for (int i = (szChar.length - 1); i >= 0; i--) {
            if ((szChar[i] + 1) > 'Z') {
                if (i == 0) {
                    szChar[i] = 'A';
                    return "A" + new String(szChar);
                }
                szChar[i] = 'A';
                continue;
            } else {
                szChar[i] = (char) (szChar[i] + 1);
                break;
            }

        }
        return new String(szChar);
    }

    private static String getNextDataSetRevID(String revId) {
        Vector<String> temp = new Vector<String>();
        String nextRevId = "";
        boolean next = true;

        for (int i = revId.length(); i > 0; i--) {
            if (revId.charAt(i - 1) != 'Z') {
                if (next) {
                    int intchar = revId.charAt(i - 1);
                    intchar++;
                    char[] char1 = Character.toChars(intchar);
                    if (revId.length() > 1) {
                        temp.add(String.valueOf(char1[0]));
                        next = false;
                    } else {
                        temp.add(String.valueOf(char1[0]));
                    }
                } else {
                    temp.add(String.valueOf(revId.charAt(i - 1)));
                    next = false;
                }
            } else if (revId.charAt(i - 1) == 'Z') {
                if (next) {
                    if (i == 1) {
                        temp.add("AA");
                    } else {
                        temp.add("A");
                        next = true;
                        ;
                    }
                } else {
                    temp.add("Z");
                }
            } else {
                return revId;
            }
        }

        for (int i = temp.size(); i > 0; i--) {
            nextRevId += temp.get(i - 1);
        }
        return nextRevId;
    }

    public static String getDataSetName(String strItemID, String revId, String dataSetName, boolean isSucceeded) throws Exception {
        if (!StringUtil.isEmpty(dataSetName) && dataSetName.indexOf("/") == -1) {
            String errMsg = "Dataset의 Name형식이 잘못되었습니다. 관리자에게 문의하세요.!!";
            throw new Exception(errMsg);
        }

        if (dataSetName.indexOf(";") > 0) {
            dataSetName = dataSetName.substring(0, dataSetName.indexOf(";"));
        }

        // 이전 Dataset Revision ID를 승계하지 않는 경우
        if (!isSucceeded) {
            dataSetName = strItemID + "/" + revId;
            return dataSetName;
        }

        if (dataSetName.contains("/") && dataSetName.contains("-")) {
            String dataSetRevId = dataSetName.substring(dataSetName.lastIndexOf("-") + 1, dataSetName.length());

            dataSetName = strItemID + "/" + revId + "-" + getNextDataSetRevID(dataSetRevId);
        } else if (!dataSetName.contains("-")) {
            dataSetName = strItemID + "/" + revId + "-" + "A";
        }
        return dataSetName;
    }
    
    /**
     * 
     * Desc : change owner of specific object.
     * @Method Name : changeOwnerShip
     * @param String obj_uid
     * @param String user_uid
     * @param Group owner_group
     * @return boolean
     * @throws Exception
     * @Comment
     */
    public boolean changeOwnerShip(String obj_uid, String user_uid, Group owner_group) throws Exception {        
        User user = (User)tcServiceManager.getDataService().loadModelObject(user_uid);
        Group group = null;
        if(owner_group == null) {
            group =  (Group)user.get_default_group();
        }else{
            group = owner_group;
        }               
        ObjectOwner[] objectowner = new ObjectOwner[1];
        objectowner[0] = new ObjectOwner(); 
        objectowner[0].owner = user;
        objectowner[0].object = tcServiceManager.getDataService().loadModelObject(obj_uid);
        objectowner[0].group = group;
        ServiceData serviceData = tcServiceManager.getDataService().changeOwnership(objectowner);        
        if (tcServiceManager.getDataService().ServiceDataError(serviceData)) {
            throw new Exception(TcUtil.makeMessageOfFail(serviceData).get(TcConstants.TC_RETURN_FAIL_REASON).toString());
        } else {
            if(serviceData.sizeOfUpdatedObjects() > 0){
                return true;
            }
        } 
        return false;
    }
    
    /**
     * 
     * Desc : 
     * @Method Name : changeOwnerShipFromItemRevisions
     * @param rev_puid
     * @param sUser_puid
     * @return
     * @throws Exception
     * @Comment
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void changeOwnerShipFromItemRevisions(String[] rev_puid, String user_uid) throws Exception{        
        TcItemUtil tcItemUtil = new TcItemUtil(tcSession);
        ArrayList arrRollback = new ArrayList();
        HashMap infoMap = null;
        
        try{
            ModelObject[] revisionModels = tcServiceManager.getDataService().loadModelObjects(rev_puid);
            String validateStatus = validateChangeOwner(revisionModels);
            if(TcConstants.TC_RETURN_OK.equals(validateStatus)){
                
                    for(ModelObject revisionModel : revisionModels) {
                        ItemRevision itemRevision = (ItemRevision)revisionModel;                        
                        ServiceData serviceData = getProperties(new ModelObject[] { itemRevision } , new String[]{TcConstants.PROP_ITEM_ID, TcConstants.PROP_ITEM_REVISION_ID, TcConstants.PROP_ITEM_MASTER_TAG});
                        if(tcServiceManager.getDataService().ServiceDataError(serviceData)){
                            throw new Exception(TcUtil.makeMessageOfFail(serviceData).get(TcConstants.TC_RETURN_FAIL_REASON).toString());
                        }
                        
                        RevisionOutput[] revisionOutput = tcItemUtil.getRevisionOutputFromItem(itemRevision.get_item_id(), itemRevision.get_item_revision_id(), itemRevision.getUid());
                        DatasetOutput[] datasetOutput = tcItemUtil.getDatasetOutputFromRevisionOutput(revisionOutput[0]);
                        BVROutput[] bvrOutputs = tcItemUtil.getBVROutputFromRevisionOutput(revisionOutput[0]);
                        
                        //ItemRevsion                        
                        serviceData = getProperties(new ModelObject[] { itemRevision } , new String[]{TcConstants.PROP_OWNING_USER, TcConstants.PROP_OWNING_GROUP});
                        if(tcServiceManager.getDataService().ServiceDataError(serviceData)){
                            throw new Exception(TcUtil.makeMessageOfFail(serviceData).get(TcConstants.TC_RETURN_FAIL_REASON).toString());
                        }
                        
                        infoMap = new HashMap<String, Object>();
                        infoMap.put("OBJECT", itemRevision);
                        infoMap.put("USER", itemRevision.get_owning_user().getUid());
                        infoMap.put("GROUP", itemRevision.get_owning_group());
                        arrRollback.add(infoMap);                        
                        
                        if(!changeOwnerShip(itemRevision.getUid(), user_uid, null)) {
                            throw new Exception("change owner ship - unknown Error... - itemRevision");
                        }
                        
                        //ItemRevisionMasterForm
                        /*
                        ItemRevision_Master itemRevsion_master = (ItemRevision_Master)tcServiceManager.getDataService().loadModelObject(itemRevision.get_item_master_tag().getUid());
                        serviceData = getProperties(new ModelObject[] { itemRevision } , new String[]{TcConstants.PROP_OWNING_USER, TcConstants.PROP_OWNING_GROUP});
                        infoMap = new HashMap<String, Object>();
                        infoMap.put("OBJECT", itemRevsion_master);
                        infoMap.put("USER", itemRevsion_master.get_owning_user().getUid());
                        infoMap.put("GROUP", itemRevsion_master.get_owning_group());
                        arrRollback.add(infoMap);
                        
                        if(!changeOwnerShip(itemRevsion_master.getUid(), user_uid, null)) {
                            throw new Exception("change owner ship - unknown Error... - itemRevision Master");
                        }
                        */
                        
                        //Dataset
                        for(DatasetOutput datasetinfo : datasetOutput) {
                            Dataset dataset = datasetinfo.dataset;                            
                            serviceData = getProperties(new ModelObject[] { dataset } , new String[]{TcConstants.PROP_OWNING_USER, TcConstants.PROP_OWNING_GROUP});
                            infoMap = new HashMap<String, Object>();
                            infoMap.put("OBJECT", dataset);
                            infoMap.put("USER", dataset.get_owning_user().getUid());
                            infoMap.put("GROUP", dataset.get_owning_group());
                            arrRollback.add(infoMap);

                            if(!changeOwnerShip(dataset.getUid(), user_uid, null)) {
                                throw new Exception("change owner ship - unknown Error... - dataset");
                            }                           
                        }
                        
                        //BOMView
                        for(BVROutput bvrOutput : bvrOutputs) {
                            PSBOMViewRevision bvr = bvrOutput.bvr;                            
                            serviceData = getProperties(new ModelObject[] { bvr } , new String[]{TcConstants.PROP_OWNING_USER, TcConstants.PROP_OWNING_GROUP});
                            infoMap = new HashMap<String, Object>();
                            infoMap.put("OBJECT", bvr);
                            infoMap.put("USER", bvr.get_owning_user().getUid());
                            infoMap.put("GROUP", bvr.get_owning_group());
                            arrRollback.add(infoMap);
                            
                            if(!changeOwnerShip(bvr.getUid(), user_uid, null)) {
                                throw new Exception("change owner ship - unknown Error... - BOM VIEW REVISION");
                            }                           
                        }
                       
                    }
             }
        }catch(Exception ex){
            ex.printStackTrace();
            if(!arrRollback.isEmpty()){
                int cnt = arrRollback.size();
                for(int i=0; i< cnt; i++){
                    infoMap = new HashMap<String, Object>();
                    infoMap = (HashMap)arrRollback.get(i);
                    ItemRevision itemRevision = (ItemRevision)infoMap.get("OBJECT");
                    changeOwnerShip(itemRevision.getUid(), (String)infoMap.get("USER"), (Group)infoMap.get("GROUP"));
                }
            }
            throw ex;
        }finally{
        }
    }
    
    /**
     * 
     * Desc : perform validation for changing owner of object
     * @Method Name : validateChangeOwner
     * @param ModelObject[] revisionModels
     * @return LData
     * @throws NotLoadedException
     * @throws Exception
     * @Comment
     */
    @SuppressWarnings("rawtypes")
    protected String validateChangeOwner(ModelObject[] revisionModels) throws Exception {
        ArrayList<String> arrCheckOut = new ArrayList<String>();
        ArrayList<String> arrUser = new ArrayList<String>();
        ArrayList<String> arrStatus = new ArrayList<String>();
        HashMap ldata = null;
        TcItemUtil tcItemUtil = new TcItemUtil(tcSession);
        String itemId = "";
        for(ModelObject revisionModel : revisionModels) {
            ItemRevision itemRevision = (ItemRevision)revisionModel;
            //tcServiceManager.getDataService().getProperties(itemRevision, new String[]{TcConstants.PROP_ITEM_ID, TcConstants.PROP_ITEM_REVISION_ID});
            ServiceData serviceData = getProperties(new ModelObject[] { itemRevision } , new String[]{TcConstants.PROP_ITEM_ID, TcConstants.PROP_ITEM_REVISION_ID});
            if(tcServiceManager.getDataService().ServiceDataError(serviceData)){
                throw new Exception(TcUtil.makeMessageOfFail(serviceData).get(TcConstants.TC_RETURN_FAIL_REASON).toString());
            }         
            RevisionOutput[] revisionOutput = tcItemUtil.getRevisionOutputFromItem(itemRevision.get_item_id(), itemRevision.get_item_revision_id(), itemRevision.getUid());
            DatasetOutput[] datasetOutput = tcItemUtil.getDatasetOutputFromRevisionOutput(revisionOutput[0]);
            BVROutput[] bvrOutputs = tcItemUtil.getBVROutputFromRevisionOutput(revisionOutput[0]);
            itemId = itemRevision.get_item_id();
            ldata = new HashMap();
            ldata = validateChangeOwner(itemRevision.getUid(), itemId );
            if(!ldata.isEmpty()) {
                if(ldata.containsKey("CHECKOUT")) {
                    arrCheckOut.add((String)ldata.get("CHECKOUT"));
                }
                // DAMON 권한이 DBA권한이므로 유져체크는 SKIP
                /*
                if(ldata.containsKey("USER")) {
                    arrUser.add((String)ldata.get("USER")); 
                }
                */
                if(ldata.containsKey("STATUS")) {
                    arrStatus.add((String)ldata.get("STATUS"));
                }           
            }            
            for(DatasetOutput datasetinfo : datasetOutput) {
                Dataset dataset = datasetinfo.dataset;
                ldata = new HashMap();
                ldata = validateChangeOwner(dataset.getUid(), itemId);
                if(!ldata.isEmpty()) {
                    if(ldata.containsKey("CHECKOUT")) {
                        arrCheckOut.add((String)ldata.get("CHECKOUT"));
                    }
                    // DAMON 권한이 DBA권한이므로 유져체크는 SKIP
                    /*
                    if(ldata.containsKey("USER")) {
                        arrUser.add((String)ldata.get("USER")); 
                    }
                    */
                    if(ldata.containsKey("STATUS")) {
                        arrStatus.add((String)ldata.get("STATUS"));
                    }  
                }
            }            
            for(BVROutput bvrOutput : bvrOutputs) {
                PSBOMViewRevision bvr = bvrOutput.bvr;
                ldata = new HashMap();
                ldata = validateChangeOwner(bvr.getUid(), itemId);
                if(!ldata.isEmpty()) {
                    if(ldata.containsKey("CHECKOUT")) {
                        arrCheckOut.add((String)ldata.get("CHECKOUT"));
                    }
                    // DAMON 권한이 DBA권한이므로 유져체크는 SKIP
                    /*
                    if(ldata.containsKey("USER")) {
                        arrUser.add((String)ldata.get("USER")); 
                    }
                    */
                    if(ldata.containsKey("STATUS")) {
                        arrStatus.add((String)ldata.get("STATUS"));
                    }  
                }
            }
        }
        
        if(!arrCheckOut.isEmpty() || !arrStatus.isEmpty() || !arrUser.isEmpty()){
            int cnt =0;
            StringBuffer sb = new StringBuffer();
            sb.append("Failed to validate. Must verify check-out, status, owner of below items - ");
            sb.append("CHECKOUT ERROR : ");
            cnt = arrCheckOut.size();
            for(int i=0; i<cnt; i++) {
                if(i>0 && cnt-1>=i ){
                    sb.append(",");
                }
                sb.append(arrCheckOut.get(i));
            }
            sb.append("  ;  ");
            sb.append("STATUS ERROR : ");
            cnt = arrStatus.size();
            for(int i=0; i<cnt; i++) {
                if(i>0 && cnt-1>=i ){
                    sb.append(",");
                }
                sb.append(arrStatus.get(i));
            }
            sb.append("  ;  ");
            sb.append("USER ERROR : ");
            cnt = arrUser.size();
            for(int i=0; i<cnt; i++) {
                if(i>0 && cnt-1>=i ){
                    sb.append(",");
                }
                sb.append(arrUser.get(i));
            }
            /** 검증 처리에 실패한 경우, 체크아웃, 상태, 소유자를 확인이 필요한 안내 메시지의 경우*/
            throw new Exception(sb.toString());
        }       
        return TcConstants.TC_RETURN_OK;
    }
    
    /**
     * 
     * Desc : perform validation for changing owner of object
     * @Method Name : validateChangeOwner
     * @param String obj_uid
     * @param String itemId
     * @return LData
     * @throws NotLoadedException
     * @throws Exception
     * @Comment
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public HashMap validateChangeOwner(String obj_uid, String itemId) throws Exception {
        TcUtil tcUtil = new TcUtil(tcSession);
        HashMap validateInfo = new HashMap();  
        HashMap checkoutInfo = getCheckedoutInfo(obj_uid);
        //01.check checkout
        if(checkoutInfo != null) {
            validateInfo.put("CHECKOUT", itemId);
        }
        
        //02. compare user
        if(!tcUtil.compareUserOfModel(obj_uid)) {
            validateInfo.put("USER", itemId);            
        }
        
        //03. check status (all object)
        String chkstatus = this.getProcessStatusFromItemRevision(obj_uid);
        if("PASS".equals(chkstatus)) {
            // PASS 인 경우 Dataset Working 확인이므로 PASS
        } 
        // RELEASED인 경우 에는 CHANG OWNER를 하지 못한다.
        else if(TcConstants.TC_PROCESS_RELEASED.equals(chkstatus)){            
            validateInfo.put("STATUS", itemId);
        }
        return validateInfo;
    }
    
    /**
     * 
     * Desc : return status of itemrevision.
     * @Method Name : getProcessStatusFromItemRevision
     * @param String revision_puid
     * @return String
     * @throws Exception
     * @Comment
     */
    public String getProcessStatusFromItemRevision(String revision_puid) throws Exception {
        WorkspaceObject object = (WorkspaceObject)tcServiceManager.getDataService().loadModelObject(revision_puid);
        tcServiceManager.getDataService().getProperties(new ModelObject[] {object}, new String[]{"s7_MATURITY"}); 
        if(object instanceof ItemRevision) {
            return object.getPropertyObject("s7_MATURITY").getStringValue();
        } else {
            return "PASS";
        }
    }
    
    /**
     * 
     * Desc : get information about object is checked-out
     * @Method Name : getCheckedoutInfo
     * @param  String obj_uid
     * @return LData
     * @throws NotLoadedException
     * @throws Exception
     * @Comment
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public HashMap getCheckedoutInfo(String obj_uid) throws Exception{
        WorkspaceObject wos = (WorkspaceObject)tcServiceManager.getDataService().loadModelObject(obj_uid);
        ServiceData serviceData = getProperties(new ModelObject[] { wos } , new String[] {TcConstants.PROP_CHECKED_OUT, TcConstants.PROP_CHECKED_OUT_USER, TcConstants.PROP_CHECKED_OUT_CHANGE_ID, TcConstants.PROP_CHECKED_OUT_DATE});
        if(!tcServiceManager.getDataService().ServiceDataError(serviceData)){             
            if(!"".equals(wos.get_checked_out_user())&& null != wos.get_checked_out_user()) {
                HashMap checkOutInfo = new HashMap();
                checkOutInfo.put(TcConstants.PROP_CHECKED_OUT, wos.get_checked_out());
                checkOutInfo.put(TcConstants.PROP_CHECKED_OUT_CHANGE_ID, wos.get_checked_out_change_id()); 
                checkOutInfo.put(TcConstants.PROP_CHECKED_OUT_USER, wos.get_checked_out_user());
                checkOutInfo.put(TcConstants.PROP_CHECKED_OUT_DATE, wos.get_checked_out_date());
                return checkOutInfo;
            } else {
                return null;
            }            
        }else{
            throw new Exception(TcUtil.makeMessageOfFail(serviceData).get(TcConstants.TC_RETURN_FAIL_REASON).toString());            
        }
    }
    
    /**
     * Desc : retrieve information of dataset on itemrevision.
     * @Method Name : getDatasetOutputFromRevisionOutput
     * @param RevisionOutput revisionOutput 
     * @return DatasetOutput[]
     * @Comment
     */
    protected DatasetOutput[] getDatasetOutputFromRevisionOutput(RevisionOutput revisionOutput) {

        DatasetOutput[] datsetInfos = new DatasetOutput[revisionOutput.datasetOutput.length];
        int i=0;
        for(DatasetOutput datsetInfo : revisionOutput.datasetOutput)
        {
            datsetInfos[i] = datsetInfo;
            i++;
        }
        return datsetInfos;
    }
    
   
    
    /**
     * 사용자 검색 (USER_ID)
     * Desc : retrieve user object from saved query.
     * @Method Name : getUserFromSavedQuery
     * @param user_name
     * @return
     * @throws Exception
     * @Comment
     *    Must be exist below saved query.
     *      "__WEB_find_person"
     */
    public String[] getUserFromSavedQuery(String user_id) throws Exception {        
        String queryName = TcConstants.SEARCH_USER;
        String[] entries = {"User ID"};
        String[] values =  {user_id};
        SavedQueriesResponse executeSavedQueries = tcServiceManager.getQueryService().executeSavedQueries(this.setQueryInputforSingle(queryName,entries,values, "user"));
        
        ServiceData serviceData = executeSavedQueries.serviceData;
        if(!tcServiceManager.getDataService().ServiceDataError(serviceData)){            
            String[] uids = this.executeQueryResult(executeSavedQueries)[0].objectUIDS;
            return uids;
        }else{
            throw new Exception(TcUtil.makeMessageOfFail(serviceData).get(TcConstants.TC_RETURN_FAIL_REASON).toString());
        }
    }
    
    protected QueryInput[] setQueryInputforSingle (String queryName,  String[] entries, String[] values, String clientId) throws Exception {
        /*
        The type of results expected from this operation: 
            0 (top-level objects only), 
            1 (top-level objects plus children: Hierarchical/Indented results), 
            2 (default value as specified on the query object
        */
        QueryInput[] queryInput = new QueryInput[1];
        queryInput[0] = new QueryInput();
        queryInput[0].clientId= clientId;
        queryInput[0].query = getQueryObject(queryName);
        queryInput[0].resultsType=2; 
        queryInput[0].entries = entries;
        queryInput[0].values = values;
        
        return queryInput;
    }
    
    private ImanQuery getQueryObject(String queryName) throws Exception {

        try {
            GetSavedQueriesResponse savedQueries = tcServiceManager.getQueryService().getQueryObject();
            for (int i = 0; i < savedQueries.queries.length; i++) {

                if (savedQueries.queries[i].name.equals(queryName)){
                    return savedQueries.queries[i].query;
                }
            }  
        } catch (ServiceException e) {
            e.printStackTrace();                  
        }
        return null;          
    }
    
    protected QueryResults[] executeQueryResult(SavedQueriesResponse executeSavedQueries) {
        
        QueryResults[] queryresults = executeSavedQueries.arrayOfResults ;
        return queryresults;
    }

}

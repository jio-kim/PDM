package com.kgm.soa.biz;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.lang.StringUtils;

import com.kgm.common.util.StringUtil;
import com.kgm.soa.service.TcServiceManager;
import com.kgm.soa.util.TcConstants;
import com.kgm.soa.util.TcUtil;
import com.teamcenter.services.loose.core._2006_03.FileManagement.GetDatasetWriteTicketsInputData;
import com.teamcenter.services.strong.cad._2008_03.DataManagement.DatasetInfo3;
import com.teamcenter.services.strong.core._2006_03.DataManagement.Relationship;
import com.teamcenter.services.strong.core._2007_09.DataManagement;
import com.teamcenter.services.strong.core._2007_09.DataManagement.ExpandGRMRelationsData2;
import com.teamcenter.services.strong.core._2007_09.DataManagement.ExpandGRMRelationsOutput2;
import com.teamcenter.services.strong.core._2007_09.DataManagement.ExpandGRMRelationsResponse2;
import com.teamcenter.services.strong.core._2007_09.DataManagement.RemoveNamedReferenceFromDatasetInfo;
import com.teamcenter.services.strong.core._2008_06.DataManagement.CreateOrUpdateRelationsInfo;
import com.teamcenter.services.strong.core._2008_06.DataManagement.CreateOrUpdateRelationsResponse;
import com.teamcenter.services.strong.core._2008_06.DataManagement.DatasetOutput;
import com.teamcenter.services.strong.core._2008_06.DataManagement.RevisionOutput;
import com.teamcenter.services.strong.core._2010_04.DataManagement.AttributeInfo;
import com.teamcenter.services.strong.core._2010_04.DataManagement.CreateDatasetsResponse;
import com.teamcenter.services.strong.core._2010_04.DataManagement.DatasetFileInfo;
import com.teamcenter.services.strong.core._2010_04.DataManagement.DatasetInfo;
import com.teamcenter.services.strong.core._2010_04.DataManagement.NamedReferenceObjectInfo;
import com.teamcenter.soa.client.FileManagementUtility;
import com.teamcenter.soa.client.model.ModelObject;
import com.teamcenter.soa.client.model.ServiceData;
import com.teamcenter.soa.client.model.strong.Dataset;
import com.teamcenter.soa.client.model.strong.ImanFile;
import com.teamcenter.soa.client.model.strong.ItemRevision;
//import com.teamcenter.lge.cmd.TcItemUtil;
//import com.teamcenter.lge.util.TcQuery;
//import com.teamcenter.services.internal.loose.cad._2007_12.DataManagement;
//import com.teamcenter.schemas.core._2007_06.datamanagement.RelationAndTypesFilter2;
//import com.teamcenter.services.strong.core._2007_06.DataManagement.ExpandGRMRelationsData;
//import com.teamcenter.services.strong.core._2007_06.DataManagement.ExpandGRMRelationsOutput;
//import com.teamcenter.services.strong.core._2007_06.DataManagement.RelationAndTypesFilter;
//import com.teamcenter.services.strong.core._2007_06.DataManagement.ExpandGRMRelationsPref;
//import com.teamcenter.services.strong.core._2007_06.DataManagement.ReferenceInfo;
//import com.teamcenter.services.strong.core._2007_09.DataManagement.ExpandGRMRelationsPref2;
//import com.teamcenter.services.strong.core._2008_06.DataManagement.DatasetProperties2;
//import com.teamcenter.soa.exceptions.NotLoadedException;

/**
 * 
 * Desc :
 * @author yunjae.jung
 */
public class TcDatasetUtil {

//  private TcItemUtil tcItemUtil;
//  private TcQuery  tcQuery;    
    private ServiceData serviceData;
    private Session tcSession = null;
    private TcServiceManager tcServiceManager;
    
    public TcDatasetUtil(Session tcSession) {
        this.tcSession = tcSession;
        tcServiceManager = new TcServiceManager(tcSession);       
    }
    

    
    /**
     * 
     * Desc :
     * @Method Name : createDataSetForNamedReference
     * @param String datsetname
     * @param String datsettype
     * @param String namedreference_name
     * @param File file
     * @param boolean isText
     * @return ModelObject
     * @throws Exception
     * @Comment
     * No using in MCAD Project
     */
    @SuppressWarnings("unused")
	private ModelObject createDataSetForNamedReference(String datsetname, 
            String datsettype, 
            String namedreference_name, 
            File file, 
            boolean isText) throws Exception
    {
        TcFileUtil tcFileUtil = new TcFileUtil(tcSession);
        FileManagementUtility fMSFileManagement = tcServiceManager.getFileService().newUtility();

        GetDatasetWriteTicketsInputData[] inputs  = new GetDatasetWriteTicketsInputData[1];
        inputs[0] = tcFileUtil.getGetDatasetWriteTicketsInputData(datsetname, datsettype, namedreference_name, file, isText);

        serviceData = fMSFileManagement.putFiles(inputs);

        if (serviceData.sizeOfPartialErrors() > 0)
            System.out.println("FileManagementService upload returned partial errors: " + serviceData.sizeOfPartialErrors());

        // Delete all objects created
//      DataManagementService dmService = DataManagementService.getService(tcSession.getConnection());
        ModelObject [] datasets = new ModelObject[1];
        datasets[0] = inputs[0].dataset;

        // Close FMS connection since done
        fMSFileManagement.term();

        return datasets[0]; 
    }
    
    /**
     * 
     * Desc : create datasetinfo for attaching dataset
     * @Method Name : createDatasetInfo
     * @param ModelObject ref_obj
     * @param HashMap datasetProps
     * @param AttributeInfo[] pattrs
     * @param DatasetFileInfo[] pdatasetFileInfos
     * @param NamedReferenceObjectInfo[] pnrObjectInfos
     * @return com.teamcenter.services.strong.core._2010_04.DataManagement.DatasetInfo[]
     * @throws Exception
     * @Comment
     */
    protected  com.teamcenter.services.strong.core._2010_04.DataManagement.DatasetInfo[] createDatasetInfo(ModelObject ref_obj, HashMap<String, String> datasetProps, AttributeInfo[] pattrs,
            DatasetFileInfo[] pdatasetFileInfos, NamedReferenceObjectInfo[] pnrObjectInfos) throws Exception {
    
        com.teamcenter.services.strong.core._2010_04.DataManagement.DatasetInfo[] datasetinfo = new DatasetInfo[1];
        

        
//      AttributeInfo[] attrs = pattrs;
//      DatasetFileInfo[] datasetFileInfos = pdatasetFileInfos;
//      NamedReferenceObjectInfo[] nrObjectInfos = pnrObjectInfos;
        
        datasetinfo[0] = new DatasetInfo();
        datasetinfo[0].clientId= (String)datasetProps.get("clientId");
        datasetinfo[0].datasetId= (String)datasetProps.get("datasetId");
        datasetinfo[0].datasetRev=(String)datasetProps.get("datasetRev");
        datasetinfo[0].description=(String)datasetProps.get("description");
        datasetinfo[0].name= (String)datasetProps.get("name");
        datasetinfo[0].relationType= (String)datasetProps.get("relationType");
        datasetinfo[0].toolUsed=(String)datasetProps.get("toolUsed");
        datasetinfo[0].type= (String)datasetProps.get("type");
        datasetinfo[0].container=ref_obj;
        
        if(pattrs != null && pattrs.length > 0)
        datasetinfo[0].attrs = pattrs; 
        if(pdatasetFileInfos != null && pdatasetFileInfos.length > 0)
        datasetinfo[0].datasetFileInfos = pdatasetFileInfos;
        if(pnrObjectInfos !=null && pnrObjectInfos.length > 0)
        datasetinfo[0].nrObjectInfos = pnrObjectInfos;
        
        return datasetinfo;
    }
       
    /**
     * 
     * Desc : create datasetinfo for attaching dataset
     * @Method Name : createDatasetInfo3
     * @param ModelObject ref_obj
     * @param HashMap datasetProps
     * @param com.teamcenter.services.strong.cad._2007_01.DataManagement.AttributeInfo[] pattrs
     * @param com.teamcenter.services.strong.cad._2007_01.DataManagement.DatasetFileInfo[] pdatasetFileInfos
     * @param com.teamcenter.services.strong.cad._2007_12.DataManagement.NamedReferenceObjectInfo2[] pnrObjectInfos
     * @return DatasetInfo3[]
     * @throws Exception
     * @Comment
     */
    public DatasetInfo3[] createDatasetInfo3(ModelObject ref_obj, 
                                             HashMap<String, Object> datasetProps, 
                                             com.teamcenter.services.strong.cad._2007_01.DataManagement.AttributeInfo[] pattrs, 
                                             com.teamcenter.services.strong.cad._2007_01.DataManagement.DatasetFileInfo[] pdatasetFileInfos, 
                                             com.teamcenter.services.strong.cad._2007_12.DataManagement.NamedReferenceObjectInfo2[] pnrObjectInfos) throws Exception {
        
//        com.teamcenter.services.strong.cad._2007_01.DataManagement.AttributeInfo[] attrs = pattrs;
//        com.teamcenter.services.strong.cad._2007_01.DataManagement.ExtraObjectInfo extraObject[] = null;       
        
        DatasetInfo3[] datasetinfo3 = new DatasetInfo3[1];
        datasetinfo3[0] = new DatasetInfo3();
        
//      datasetinfo3[0].attrList = attrs;
//      datasetinfo3[0].basisName ="";
        datasetinfo3[0].clientId = (String)datasetProps.get("clientId");
        datasetinfo3[0].createNewVersion= false; //(new Boolean((String)datasetProps.get("createNewVersion"))).booleanValue();
                //new Boolean((Boolean)datasetProps.get("createNewVersion")).booleanValue(); //if need to create new version, define true;
        datasetinfo3[0].dataset = (Dataset)ref_obj;
        datasetinfo3[0].datasetFileInfos = pdatasetFileInfos;
//      datasetinfo3[0].datasetRev = (String)datasetProps.get("datasetRev");
//      datasetinfo3[0].description = (String)datasetProps.get("description");
//      datasetinfo3[0].extraObject = extraObject;
//      datasetinfo3[0].id = (String)datasetProps.get("datasetId");
        datasetinfo3[0].itemRevRelationName = (String)datasetProps.get("itemRevRelationName");
//      datasetinfo3[0].lastModifiedOfDataset = null;
//      datasetinfo3[0].mapAttributesWithoutDataset = false;
//      datasetinfo3[0].mappingAttributes = attrs;
//      datasetinfo3[0].name = (String)datasetProps.get("name");
        datasetinfo3[0].namedReferenceObjectInfos = pnrObjectInfos;
//      datasetinfo3[0].namedReferencePreference="";
        TcUtil tcUtil = new TcUtil(tcSession);
        datasetinfo3[0].type = tcUtil.getTypeNameFromObject(ref_obj.getUid());
        
        
//        attrList = new com.teamcenter.services.strong.cad._2007_01.AttributeInfo[0];
//        mappingAttributes = new com.teamcenter.services.strong.cad._2007_01.AttributeInfo[0];
//        extraObject = new com.teamcenter.services.strong.cad._2007_01.ExtraObjectInfo[0];
//        datasetFileInfos = new com.teamcenter.services.strong.cad._2007_01.DatasetFileInfo[0];
//        namedReferenceObjectInfos = new com.teamcenter.services.strong.cad._2007_12.NamedReferenceObjectInfo2[0];
                
        return datasetinfo3;
    }
    
    /**
     * 
     * Desc :
     * @Method Name : createDataSetNoFile
     * @param DatasetInfo[] input
     * @return CreateDatasetsResponse
     * @throws Exception
     * @Comment
     */
    public  CreateDatasetsResponse createDataSetNoFile(DatasetInfo[] input) throws Exception {
        CreateDatasetsResponse createdatasetresponse = null;
        createdatasetresponse = tcServiceManager.getDataService().createDatasets(input);

        return createdatasetresponse;
    }
    
    
    //  public  String getStreamFromURL(String callUrl) throws Exception {
//
//      URL url;
//      URLConnection urlcon;
//      HttpURLConnection httpurlcon;
//      InputStream is;
//      InputStreamReader isr;
//      BufferedReader br;
//
//      url = new URL(callUrl);
//
//      urlcon = url.openConnection();
//      httpurlcon = (HttpURLConnection)urlcon;
//      httpurlcon.setDoInput(true);
//      httpurlcon.setDoOutput(true);
//      httpurlcon.setUseCaches(false); 
//      httpurlcon.setDefaultUseCaches(false);
//      Map headerMap = new HashMap<String, List>();
//      headerMap = httpurlcon.getHeaderFields();
//
//      br = new BufferedReader(new InputStreamReader(httpurlcon.getInputStream()));
//      String buf = null;
//
//      while(true) {
//          buf = br.readLine();
//          if(buf==null) break;
//          System.out.println(buf);
//      }
//
//      return buf;
//  }

    public Dataset[] retrieveDatasetObjects (ItemRevision itemRevision) throws Exception { 
        TcItemUtil tcItemUtil = new TcItemUtil(tcSession);
        tcServiceManager.getDataService().getProperties(new ModelObject[] { itemRevision }, new String[]{TcConstants.PROP_ITEM_ID, TcConstants.PROP_ITEM_REVISION_ID, TcConstants.PROP_ITEM_MASTER_TAG});
        RevisionOutput[] revisionOutput = tcItemUtil.getRevisionOutputFromItem(itemRevision.get_item_id(), itemRevision.get_item_revision_id(), itemRevision.getUid());
        DatasetOutput[] datasetOutput = getDatasetOutputFromRevisionOutput(revisionOutput[0]);
        Dataset[] datasets = new Dataset[datasetOutput.length];
        for (int i = 0; i < datasetOutput.length; i++) {
            datasets[i] = datasetOutput[i].dataset;
        }
        ServiceData serviceData = tcItemUtil.getProperties(datasets,  new String[]{TcConstants.PROP_DATASET_TYPE, TcConstants.PROP_DATASET_TYPE_NAME, "object_name"});
        if(!tcServiceManager.getDataService().ServiceDataError(serviceData)){             
            return datasets;
        }else{
            throw new Exception(TcUtil.makeMessageOfFail(serviceData).get(TcConstants.TC_RETURN_FAIL_REASON).toString());            
        }
    }
    
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
     * 
     * Desc : create dataset and attaching datset on itemrevision
     * @Method Name : createDataSetOnItemRevision
     * @param String rev_puid
     * @param String dataset_type
     * @return LData
     * @throws Exception
     * @Comment
     */    
    public Dataset createDataSetOnItemRevision(ItemRevision rev, String dataset_type, String cadRevId) throws Exception {        
//      com.teamcenter.services.strong.core._2008_06.DataManagement.DatasetProperties2[] datsetprops = new DatasetProperties2[1];
//      com.teamcenter.services.strong.core._2006_03.DataManagement.CreateDatasetsResponse createdatasetresponse = null;
        com.teamcenter.services.strong.core._2010_04.DataManagement.CreateDatasetsResponse createdatasetresponse = null;        
        tcServiceManager.getDataService().getProperties(new ModelObject[] {rev}, new String[] {TcConstants.PROP_ITEM_ID, TcConstants.PROP_ITEM_REVISION_ID});        
        if("".equals(dataset_type) || null == dataset_type) {
           throw new Exception("Please define the type of dataset (dataset type is null)");
        }
        HashMap<String, String> datasetProps = new HashMap<String, String>();
        datasetProps.put("clientId", "1");        
        // 이전 데이터셋 정보를 확인하여 Dataset 네이밍 룰을 따른다.        
//        datasetProps.put("datasetId", TcItemUtil.getDataSetName(rev.get_item_id(), rev.get_item_revision_id(), preDataset.get_object_name(), true));        
//        datasetProps.put("name", TcItemUtil.getDataSetName(rev.get_item_id(), rev.get_item_revision_id(), preDataset.get_object_name(), true));        
        // 이전 리비전 정보가 없으면 디폴트 데이터셋 생성 룰을 따른다. (최초생성 시)
        String defaultDatasetObjectName = rev.get_item_id() + "/" + rev.get_item_revision_id();
        // CAD Version ID를 Dataset Object Name last에 add 한다.
        String strCadPartRev = TcDatasetUtil.getCadPartRev(cadRevId);
        if(strCadPartRev != null && !"".equals(strCadPartRev)) {
            defaultDatasetObjectName = defaultDatasetObjectName + strCadPartRev;
        }
        datasetProps.put("datasetId", defaultDatasetObjectName);
        datasetProps.put("name", defaultDatasetObjectName);        
        datasetProps.put("datasetRev", rev.get_item_revision_id());
        datasetProps.put("description", "");        
        datasetProps.put("relationType", TcConstants.RELATION_SPECIFICATION);
//        datasetProps.put("toolUsed", TcConstants.TOOL_NX_UGMASTER);
        if("model".equals(dataset_type)) {
            dataset_type = "catia";
        }
        datasetProps.put("type", dataset_type);
        
        //need to create object, not allow null object
        AttributeInfo[] attrs = null;
        DatasetFileInfo[] datasetFileInfos = null;
        NamedReferenceObjectInfo[] nrObjectInfos = null;
        com.teamcenter.services.strong.core._2010_04.DataManagement.DatasetInfo[] datasetinfo = createDatasetInfo(rev, datasetProps, attrs, datasetFileInfos, nrObjectInfos);
        createdatasetresponse = createDataSetNoFile(datasetinfo);
        
        if(!tcServiceManager.getDataService().ServiceDataError(createdatasetresponse.servData)){             
            return (Dataset)createdatasetresponse.servData.getCreatedObject(0);
        }else{
            throw new Exception(TcUtil.makeMessageOfFail(createdatasetresponse.servData).get(TcConstants.TC_RETURN_FAIL_REASON).toString());            
        }
    }
    
    /**
     * VPM CAD Version ID를 TC에 맞게 변경하여 가져온다.
     * 
     * ORG ID : --- -> CONV ID : 
     * ORG ID : --A -> CONV ID : -A 
     * ORG ID : -AA -> CONV ID : -AA 
     * 
     * @method getCadPartRev 
     * @date 2013. 4. 26.
     * @param
     * @return String
     * @exception
     * @throws
     * @see
     */
    public static String getCadPartRev(String strCadPartRev) {
        String rtCadPartRev = StringUtils.trimToEmpty(StringUtils.replace(StringUtil.nullToString(strCadPartRev), "-", "")); 
        if("".equals(rtCadPartRev)) {
            return "";
        } else {
            return "-" + rtCadPartRev;
        }
    }
    
    /**
     * TC의 Dataset Name을 가지고 CAD Version IDfmf 가져온다.
     * 
     * @method getTCadDatasetRev 
     * @date 2014. 3. 18.
     * @param
     * @return String
     * @exception
     * @throws
     * @see
     */
    public static String getTCadDatasetRev(String datasetName) throws Exception {
        String[] datasetNames = StringUtils.split(datasetName, "/");
        if (datasetNames.length == 2) {
            // 11111/000-A -> [11111], [000-A] -> SUCCESS
            if (StringUtils.contains(datasetNames[1], "-")) {
                return "-" + StringUtils.split(datasetNames[1], "-")[1];
            } else {
                // 11111/000 -> [11111], [000] -> SUCCESS
                if (datasetNames[1].length() == 3) {
                    return "";
                } 
                // 11111/000XA -> [11111], [000XA] -> ERROR & SUCCESS
                else if(datasetNames[1].length() > 3) {
                    // XA 만 추출
                    return "-" + datasetNames[1].substring(3);
                }                
            }
        }
        throw new Exception("올바른 CAD Dataset name이 아닙니다. - " + datasetName);
    }

    /**
     * 
     * Desc : delete dataset
     * @Method Name : deleteDataset
     * @param Dataset dataset
     * @param String[] otherSideObjectTypes
     * @param String relationTypeName
     * @return LData
     * @throws Exception
     * @Comment
     */
    private void deleteDataset(Dataset dataset, String[] otherSideObjectTypes, String relationTypeName) throws Exception {        
        TcUtil tcUtil = new TcUtil(tcSession);
        ExpandGRMRelationsResponse2 relationResp = tcUtil.getRelatedObjectForSecondary(new ModelObject[]{dataset}, otherSideObjectTypes, relationTypeName);
        
        if(!tcServiceManager.getDataService().ServiceDataError(relationResp.serviceData))
        {
            for(ExpandGRMRelationsOutput2 grmRelations : relationResp.output)
            {
                for(ExpandGRMRelationsData2 grmReationData : grmRelations.relationshipData)
                {
                    Relationship[] relationships = new Relationship[grmReationData.relationshipObjects.length];
                    
                    for(int ii = 0; ii < grmReationData.relationshipObjects.length; ii++)
                    {
                        Relationship relationship = new Relationship();
                        relationship.primaryObject = grmReationData.relationshipObjects[ii].otherSideObject;
                        relationship.secondaryObject = dataset;
                        relationship.relationType = grmReationData.relationName;
                        relationships[ii] = relationship;
                    }
                    
                    tcServiceManager.getDataService().ServiceDataError(tcServiceManager.getDataService().deleteRelations(relationships));
                    
                }
            }
            ServiceData serviceData = tcServiceManager.getDataService().deleteObjects(new ModelObject[]{dataset});
            
            if(!tcServiceManager.getDataService().ServiceDataError(serviceData)){
                 //ldata.setString(TcConstants.TC_RETURN_MESSAGE, TcConstants.TC_RETURN_OK);
                 //ldata.setString(TcConstants.PROP_PUID, serviceData.getDeletedObject(0));
            }else{
                //ldata = TcUtil.makeMessageOfFail(serviceData);
                //ldata.setString(TcConstants.PROP_PUID, serviceData.getDeletedObject(0));
                throw new Exception(TcUtil.makeMessageOfFail(serviceData).get(TcConstants.TC_RETURN_FAIL_REASON).toString());
            }
        }else{
            //ldata = TcUtil.makeMessageOfFail(relationResp.serviceData);
            //ldata.setString(TcConstants.PROP_PUID, dataset.getUid());
            throw new Exception(TcUtil.makeMessageOfFail(relationResp.serviceData).get(TcConstants.TC_RETURN_FAIL_REASON).toString());
        }

    }
    
    /**
     * 
     * Desc : delete dataset on itemrevision
     * @Method Name : deleteDatasetFromItemRevision
     * @param String dataset_puid
     * @return LData
     * @throws Exception
     * @Comment
     */
    public void deleteDatasetFromItemRevision (String dataset_puid) throws Exception {        
        Dataset dataset = (Dataset)tcServiceManager.getDataService().loadModelObject(dataset_puid);
        deleteDataset(dataset, new String[]{TcConstants.TYPE_ITEM_REVISION}, TcConstants.RELATION_SPECIFICATION );
    }
    
    
    /**
     * 
     * Desc : get information on getReferencedFilterFromDataset
     * @Method Name : getFilesFromDataset
     * @param String dataset_puid
     * @param boolean reqFile
     * @return LMultiData
     * @throws ServiceException
     * @throws Exception
     * @Comment
     */
//    public void getFilesFromDataset(String dataset_puid, boolean reqFile) throws ServiceException, Exception {
//        
//        LMultiData lmultidata = null;
//        tcFileUtil = new TcFileUtil(tcSession);
//        Dataset[] dataset = new Dataset[1];
//        dataset[0] = (Dataset)tcServiceManager.getDataService().loadModelObject(dataset_puid);
//        lmultidata = tcFileUtil.getFilesFromDataset(dataset, "", reqFile);
//        
//        return lmultidata;
//    }
    
    
    /**
     * 
     * Desc : get imanfiles is referenced on dataset
     * @Method Name : getReferencedFileFromDataset
     * @param String dataset_puid
     * @param reference_names
     * @return
     * @throws Exception
     * @Comment
     */
    public ImanFile[] getReferencedFileFromDataset(String dataset_puid, String[] reference_names) 
            throws Exception {
        ImanFile[] files = null;
        ModelObject[] refObjs = getReferencedObjectFromDataset(dataset_puid, reference_names);
        
        files = new ImanFile[refObjs.length];
        
        for(int i=0; i< refObjs.length; i++) {
            files[i] = (ImanFile)refObjs[i];
        }
        return files;
    }
    

    /**
     * 
     * Desc : retrieve referenced file on dataset
     * @Method Name : getReferencedListFromDataset
     * @param Dataset[] dataset
     * @param String[] reference_names
     * @return ModelObject[]
     * @throws Exception
     * @Comment
     */
    public  ModelObject[] getReferencedListFromDataset (Dataset[] dataset, String[] reference_names) throws Exception {
        ModelObject[] reflistComps = null;
        ModelObject[] fileComps = null;
        tcServiceManager.getDataService().getProperties(dataset, new String[] {TcConstants.PROP_REF_LIST, TcConstants.PROP_REF_NAMES});
        reflistComps = dataset[0].get_ref_list();
        fileComps = new ModelObject[reflistComps.length];
        String idx_names[] = dataset[0].get_ref_names();
        
        int name_idx=0;
        int obj_idx=0;
        if( reference_names.length > 0) {
            for(String reference_name : reference_names) {
                
                  for(String idx_name : idx_names) {
                      
                        if(idx_name.equals(reference_name)){
                            fileComps[obj_idx] = reflistComps[name_idx];
                            obj_idx++;
                        }
                        name_idx++;
                    }
            }

        }else{
                for(ModelObject tmpComp : reflistComps) {
                        fileComps[name_idx] = tmpComp;
                        name_idx++;
                }
        }
        
        return fileComps;
    }   
    
    /**
     * 
     * Desc : get information on getReferencedFileFromDataset
     * @Method Name : getReferencedObjectFromDataset
     * @param dataset_puid
     * @param reference_names
     * @return ModelObject[]
     * @throws Exception
     * @Comment
     */
    public ModelObject[] getReferencedObjectFromDataset(String dataset_puid, String[] reference_names) 
            throws Exception {
        Dataset[] arrDataset = new Dataset[1];
        arrDataset[0] = (Dataset)tcServiceManager.getDataService().loadModelObject(dataset_puid);
        ModelObject[] refObjs = getReferencedListFromDataset(arrDataset, reference_names);
        
        return refObjs;

    }
    

    /**
     * 
     * Desc : retrieve modelobject is referenced on dataset by quick-access-binary
     * @Method Name : getReferencedQuickAccessBinaryListFromDataset
     * @param Dataset[] dataset
     * @return ModelObject[]
     * @throws Exception
     * @Comment
     */
    public  ModelObject[] getReferencedQuickAccessBinaryListFromDataset (Dataset[] dataset) throws Exception {
//        ModelObject[] reflistComps = null;
//        ModelObject[] fileComps = null;
//        
//        tcServiceManager.getDataService().getProperties(dataset, new String[] {TcConstants.PROP_REF_LIST, TcConstants.PROP_REF_NAMES});
//        reflistComps = dataset[0].get_ref_list();
//        String idx_names[] = dataset[0].get_ref_names();
//        fileComps = new ModelObject[reflistComps.length];
//        
//        int idx=0;
//        for(String idx_name : idx_names) {
//        	//TODOS : 여기 idx_names => idx_name으로 수정함.
//            if(idx_name.equals(TcConstants.RELATION_QUICK_ACCESS_BINARY_REFERENCES)){
//                fileComps[idx] = reflistComps[idx];
//            }
//            idx++;
//        }
//
//        return fileComps;
        ModelObject[] reflistComps = null;
        ArrayList<ModelObject> fileComps = null;
        
        tcServiceManager.getDataService().getProperties(dataset, new String[] {TcConstants.PROP_REF_LIST, TcConstants.PROP_REF_NAMES});
        reflistComps = dataset[0].get_ref_list();
        String idx_names[] = dataset[0].get_ref_names();

        for(int i = 0; i < idx_names.length; i++) {
            if(idx_names[i].equals(TcConstants.RELATION_QUICK_ACCESS_BINARY_REFERENCES)){
                fileComps.add(reflistComps[i]);
            }
        }

        return fileComps.toArray(new ModelObject[0]);
    }
    
    /**
     * 
     * Desc : get name of reference
     * @Method Name : getReferenecedTypeFromDataset
     * @param Dataset dataset
     * @param ImanFile ifile
     * @return String
     * @throws Exception
     * @Comment
     */
    public String getReferenecedTypeFromDataset (Dataset dataset, ImanFile ifile) throws Exception {
        ModelObject[] reflistComps = null;
        tcServiceManager.getDataService().getProperties(new ModelObject[] {dataset}, new String[] {TcConstants.PROP_REF_LIST, TcConstants.PROP_REF_NAMES});
        reflistComps = dataset.get_ref_list();
        String idx_names[] = dataset.get_ref_names();
        
        int name_idx=0;
        
        for(String idx_name : idx_names) {
            
             if((reflistComps[name_idx].getUid()).equals(ifile.getUid())){
                 return idx_name;
             }
               name_idx++;
        }
        return null;
    }
    

    /**
     * 
     * Desc : remove object on dataset by reference name
     * @Method Name : removeNamedReferenceFromDataset
     * @param String dataset_puid
     * @param String file_puid
     * @return LData
     * @throws Exception
     * @Comment
     */
    public void removeNamedReferenceFromDataset(String dataset_puid, String file_puid) throws Exception {        
        
        ModelObject fileComp = tcServiceManager.getDataService().loadModelObject(file_puid);
        DataManagement.NamedReferenceInfo[] nrinfo = new DataManagement.NamedReferenceInfo[1];
        Dataset dataset = (Dataset)tcServiceManager.getDataService().loadModelObject(dataset_puid);
        nrinfo[0] = new DataManagement.NamedReferenceInfo();
        nrinfo[0].deleteTarget= true;
        nrinfo[0].targetObject= fileComp;
        nrinfo[0].clientId ="2";
        nrinfo[0].type = getReferenecedTypeFromDataset(dataset,(ImanFile)fileComp);

                //fileComp.getType().getName();
        
        RemoveNamedReferenceFromDatasetInfo[] aremovenamedreferencefromdatasetinfo = new RemoveNamedReferenceFromDatasetInfo[1];
        aremovenamedreferencefromdatasetinfo[0] = new RemoveNamedReferenceFromDatasetInfo();
        aremovenamedreferencefromdatasetinfo[0].dataset = dataset;
        aremovenamedreferencefromdatasetinfo[0].clientId = "1";
        aremovenamedreferencefromdatasetinfo[0].nrInfo = nrinfo;
        
        ServiceData servicedata = tcServiceManager.getDataService().removeNamedReferenceFromDataset(aremovenamedreferencefromdatasetinfo);
        
        if(!tcServiceManager.getDataService().ServiceDataError(servicedata)){
//            ldataResult.setString(TcConstants.TC_RETURN_MESSAGE, TcConstants.TC_RETURN_OK);
//            ldataResult.setString("sDataSetPuid", dataset_puid);
        }else{
//            ldataResult = TcUtil.makeMessageOfFail(servicedata);
//            ldataResult.setString("sDataSetPuid", dataset_puid);
            throw new Exception(TcUtil.makeMessageOfFail(servicedata).get(TcConstants.TC_RETURN_FAIL_REASON).toString());
        }
        
        
    }
    
    /**
     * Create Relation
     * 
     * @method createRelation 
     * @date 2013. 4. 26.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    public void createRelation( ModelObject parent, ModelObject child, String sRelationType) throws Exception {        
        com.teamcenter.services.strong.core._2008_06.DataManagement.SecondaryData[] secObj = new com.teamcenter.services.strong.core._2008_06.DataManagement.SecondaryData[1];
        secObj[0] = new com.teamcenter.services.strong.core._2008_06.DataManagement.SecondaryData();
        secObj[0].clientId = "";
        secObj[0].secondary = child; 
        
        CreateOrUpdateRelationsInfo[] relInfo = new CreateOrUpdateRelationsInfo[1];
        relInfo[0] = new CreateOrUpdateRelationsInfo();
        relInfo[0].primaryObject = parent; 
        relInfo[0].secondaryData = secObj;
        relInfo[0].relationType = sRelationType;            
        CreateOrUpdateRelationsResponse createOrUpdateRelationsResponse = tcServiceManager.getDataService().createOrUpdateRelations(relInfo, false);   
        if(tcServiceManager.getDataService().ServiceDataError(createOrUpdateRelationsResponse.serviceData)){
            throw new Exception(TcUtil.makeMessageOfFail(createOrUpdateRelationsResponse.serviceData).get(TcConstants.TC_RETURN_FAIL_REASON).toString());
      }
    }
    
}
